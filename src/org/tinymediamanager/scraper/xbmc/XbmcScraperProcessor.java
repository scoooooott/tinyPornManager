package org.tinymediamanager.scraper.xbmc;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

// clean file regex
// \\+(ac3|custom|dc|divx|dsr|dsrip|dutch|dvd|dvdrip|dvdscr|fragment|fs|hdtv|internal|limited|multisubs|ntsc|ogg|ogm|pal|pdtv|proper|repack|rerip|retail|se|svcd|swedish|unrated|ws|xvid|xxx|cd[1-9]|\\[.*\\])(\\+|$)
// // new logic - select the crap then drop anything to the right of it

public class XbmcScraperProcessor {
  public static final String  FUNCTION_SETTINGS = "GetSettings";
  private static final Logger log               = Logger.getLogger(XbmcScraperProcessor.class);
  private boolean             truncateLogging   = true;

  private XbmcScraper         scraper           = null;

  // 20 buffers in total; buffer 0 is always blank
  private String              buffers[]         = new String[21];

  // options that match those in the <Settings> elements.
  Map<String, String>         options           = new HashMap<String, String>();

  private static final int    PATTERN_OPTIONS   = Pattern.MULTILINE + Pattern.CASE_INSENSITIVE + Pattern.DOTALL;

  // private XbmcScraperConfiguration cfg = new XbmcScraperConfiguration();

  public XbmcScraperProcessor(XbmcScraper scraper) {
    if (scraper == null)
      throw new RuntimeException("Scraper cannot be null!");

    this.scraper = scraper;

    mergeOptions(this.options);

    log.debug("XbmcScraperProcessor created using Scraper: " + scraper.getName() + "; Content: " + scraper.getContent() + "; Complete Logging: "
        + !truncateLogging);

    clearBuffers();
  }

  private XbmcScraperProcessor(XbmcScraper scraper, Map<String, String> options, String[] buffers) {
    this.scraper = scraper;
    this.options = options;
    if (buffers != null) {
      for (int i = 0; i < buffers.length; i++) {
        this.buffers[i] = buffers[i];
      }
      // this.buffers = buffers;
    } else {
      clearBuffers();
    }
  }

  private void mergeOptions(Map<String, String> dest) {
    try {
      // if (!containsFunction(FUNCTION_SETTINGS)) {
      // return;
      // } else {
      // String xmlString = executeFunction(FUNCTION_SETTINGS, null);
      File scraperSettings = new File(scraper.getSettingsPath());
      if (scraperSettings != null) {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder parser = factory.newDocumentBuilder();
        // ByteArrayInputStream xmlStream = new
        // ByteArrayInputStream(xmlString.getBytes());

        Document d = parser.parse(scraperSettings);

        NodeList nl = d.getElementsByTagName("setting");
        for (int i = 0; i < nl.getLength(); i++) {
          Element e = (Element) nl.item(i);
          String id = e.getAttribute("id");
          if (StringUtils.isEmpty(id))
            continue;

          if (dest.get(id) == null) {
            String defValue = e.getAttribute("default");
            if (StringUtils.isEmpty(defValue))
              continue;
            log.debug("Default Option: " + scraper.getId() + "; " + id + "; " + defValue);
            // dest.put(id, cfg.getScraperProperty(scraper.getId(), id,
            // defValue));
            dest.put(id, defValue);
          }
        }
        // }
      }
    } catch (Exception e) {
      log.error("Failed to merge options!", e);
    }
  }

  public String executeFunction(String function, String input[]) {
    ScraperFunction func = scraper.getFunction(function);
    if (func != null) {
      log.debug("** BEGIN Function: " + func.getName() + "; Dest: " + func.getDest() + "; ClearBuffers: " + func.isClearBuffers());

      // if (func.isClearBuffers()) {
      // clearBuffers();
      // }

      setBuffers(input);

      executeRegexps(func.getRegExps());

      log.debug("** END Function: " + func.getName() + "; Dest: " + func.getDest() + "; ClearBuffers: " + func.isClearBuffers());
      return getBuffer(func.getDest());
    } else {
      log.debug("** Could not locate Function: " + function + " in the scraper " + scraper.getId());
      return "";
    }
  }

  private void executeRegexps(RegExp[] regExps) {
    for (RegExp r : regExps) {
      executeRegexp(r);
    }
  }

  private void executeRegexp(RegExp regex) {
    String cond = regex.getConditional();
    if (cond != null) {
      boolean not = cond.startsWith("!");
      if (not)
        cond = cond.substring(1);
      Boolean b = BooleanUtils.toBooleanObject(options.get(cond));
      log.debug("Processing Conditional: " + regex.getConditional() + "; " + b);
      boolean b2 = (b == null || b.booleanValue() == true);
      if (!(b2 || (not && !b2))) {
        log.debug("Condition Not Met: " + regex.getConditional() + "; " + b2);
        return;
      }
    }

    if (regex.hasRegExps()) {
      executeRegexps(regex.getRegExps());
    }
    executeExpression(regex);
  }

  private void executeExpression(RegExp r) {
    log.debug(String.format("Processing Expression: %s; Dest: %s; Input: %s; Output: %s", r.getExpression().getExpression(), r.getDest(),
        r.getInput(), r.getOutput()));
    Expression exp = r.getExpression();

    String in = getBuffer(r.getInput());
    if (in == null)
      in = "";

    String expr = exp.getExpression();
    if (expr == null || expr.trim().length() == 0) {
      log.debug("Expression was empty.  Returning processed output buffer using input as replacement array.");
      setBuffer(r.getDest(), processOutputBuffers(r.getOutput(), new String[] { "", in }), r.isAppendBuffer());
      return;
    }

    log.debug("Expression: " + expr);
    expr = processOutputBuffersForInputBufferReferences(expr);
    log.debug("Expression: " + expr);
    log.debug("     Input: " + logBuffer(in));
    Pattern p = Pattern.compile(expr, PATTERN_OPTIONS);
    Matcher m = p.matcher(in);
    if (m.find()) {
      log.debug("Matched: Group Count: " + m.groupCount());
      setBuffer(r.getDest(), processOutputBuffers(r.getOutput(), toGroupArray(exp.getNoCleanArray(), m)), r.isAppendBuffer());

      if (exp.isRepeat()) {
        while (m.find()) {
          log.debug("Repeat Matched.  Group Count: " + m.groupCount());
          setBuffer(r.getDest(), processOutputBuffers(r.getOutput(), toGroupArray(exp.getNoCleanArray(), m)), r.isAppendBuffer());
        }
      }
    } else {
      log.debug(String.format("No Match! Expression: %s; Text: %s;", expr, logBuffer(in)));
      if (exp.isClear()) {
        log.debug("Clearing Destination Buffer: " + r.getDest());
        setBuffer(r.getDest(), "", false);
      }
    }
  }

  private String logBuffer(String in) {
    // if debug is not enabled, then return the whole buffer.
    if (!log.isDebugEnabled())
      return in;

    if (isTruncateLogging() && in != null && in.length() > 50) {
      in = "TRUNCATED(50): " + in.substring(0, 50) + "...";
    }
    return in;
  }

  private String[] toGroupArray(String noCleanArray[], Matcher groups) {
    int c = groups.groupCount();
    String g[] = new String[c + 1];
    for (int i = 0; i <= c; i++) {
      String s = groups.group(i);
      if (noCleanArray != null && noCleanArray[i] != null) {
        // don clean
        g[i] = groups.group(i);
      } else {
        g[i] = cleanHtml(groups.group(i));
      }
    }
    return g;
  }

  private String cleanHtml(String group) {
    if (group == null)
      return "";
    log.debug("Before Clean Html: " + group);
    String s = group.replaceAll("<[^>]+>", "");
    log.debug("After Clean Html: " + s);
    return s;
  }

  private String processOutputBuffers(String output, String groups[]) {
    log.debug("Processing output buffer replacement.");
    Pattern p = Pattern.compile("\\\\([0-9])");
    Matcher m = p.matcher(output);
    StringBuffer sb = new StringBuffer();

    int lastStart = 0;
    while (m.find()) {
      sb.append(output.substring(lastStart, m.start()));
      lastStart = m.end();
      int g = Integer.parseInt(m.group(1));
      if (g > groups.length) {
        log.debug("No Group Replacement for: " + g);
        continue;
      }

      // TODO: check noClean flag, and clean otherwise
      int index = Integer.parseInt(m.group(1));
      String val = "";
      if (index < groups.length) {
        val = groups[index];
      }
      if (val == null)
        val = "";
      sb.append(val);
    }

    sb.append(output.substring(lastStart));

    return processOutputBuffersForPropertyReferences(processOutputBuffersForInputBufferReferences(sb.toString()));
  }

  private String processOutputBuffersForInputBufferReferences(String output) {
    log.debug("Processing output buffers for input buffer references.");
    Pattern p = Pattern.compile("\\$\\$([0-9]+)");
    Matcher m = p.matcher(output);
    StringBuffer sb = new StringBuffer();

    int lastStart = 0;
    while (m.find()) {
      sb.append(output.substring(lastStart, m.start()));
      lastStart = m.end();
      sb.append(getBuffer(Integer.parseInt(m.group(1))));
    }

    sb.append(output.substring(lastStart));

    return sb.toString();
  }

  private String processOutputBuffersForPropertyReferences(String output) {
    log.debug("Processing output buffers for property references.");
    Pattern p = Pattern.compile("\\$INFO\\[([^\\]]+)\\]");
    Matcher m = p.matcher(output);
    StringBuffer sb = new StringBuffer();

    int lastStart = 0;
    while (m.find()) {
      sb.append(output.substring(lastStart, m.start()));
      lastStart = m.end();
      sb.append(options.get(m.group(1)));
    }

    sb.append(output.substring(lastStart));

    return sb.toString();
  }

  // private String processOutputBuffersForChaining(String output) {
  // log.debug("Processing output buffers for chaining.");
  // Pattern p = Pattern.compile("<chain function=\"(.*)\">(.*)</chain>");
  // Matcher m = p.matcher(output);
  // StringBuffer sb = new StringBuffer();
  //
  // int lastStart = 0;
  // while (m.find()) {
  // String function = m.group(1);
  // String buffer = m.group(2);
  // XbmcScraperProcessor proc = new XbmcScraperProcessor(scraper);
  // // sb.append("<" + function + ">" + proc.executeFunction(function, new
  // // String[] { "", buffer }) + "</" + function + ">");
  // sb.append(proc.executeFunction(function, new String[] { "", buffer }));
  // }
  //
  // if (sb.length() == 0) {
  // sb.append(output);
  // }
  //
  // return sb.toString();
  // }

  private String getBuffer(int buffer) {
    String text = buffers[buffer];
    if (text == null)
      text = "";
    if (log.isDebugEnabled()) {
      log.debug("Get Int Buffer: " + buffer + "; Text: " + logBuffer(text));
    }
    return text;
  }

  private String getBuffer(String buffer) {
    if (buffer == null)
      buffer = "";
    log.debug(String.format("Get String Buffer: %s", buffer));
    Pattern bufferPattern = Pattern.compile("\\$\\$([0-9]+)");
    Matcher m = bufferPattern.matcher(buffer);
    if (m.find()) {
      StringBuffer sb = new StringBuffer();
      sb.append(getBuffer(Integer.parseInt(m.group(1))));
      while (m.find()) {
        sb.append(getBuffer(Integer.parseInt(m.group(1))));
      }
      return sb.toString();
    } else {
      log.debug("getBuffer(): Using raw input: " + logBuffer(buffer));
    }
    return buffer;
  }

  private void setBuffer(int buffer, String text, boolean append) {
    if (text == null)
      text = "";

    if (log.isDebugEnabled()) {
      log.debug(String.format("Set Buffer: %s; Append: %s; Text: %s", buffer, append, logBuffer(text)));
    }

    Pattern p = Pattern.compile("<url\\s+.*function=");
    Matcher m = p.matcher(text);
    if (m.find()) {
      log.debug("Processing Sub Function: " + text);
      try {
        XbmcUrl url = new XbmcUrl(text);
        ScraperFunction func = scraper.getFunction(url.getFunctionName());
        if (func == null) {
          throw new Exception("Invalid Function Name: " + url.getFunctionName());
        }
        XbmcScraperProcessor proc = newSubProcessor(func.isClearBuffers());

        // call the set buffer again with this result
        // setBuffer(buffer, proc.executeFunction(url.getFunctionName(), new
        // String[] {"", url.getTextContent()}), append);
        text = proc.executeFunction(url.getFunctionName(), new String[] { "", url.getTextContent() });
      } catch (Exception e) {
        log.error("Failed to process function: " + text, e);
        text = "\n<error>" + text + "\n<msg>" + e.getMessage() + "</msg></error>\n";
      }
    }

    // sub Function
    p = Pattern.compile("<chain function=\"(.*)\">(.*)</chain>");
    m = p.matcher(text);
    if (m.find()) {
      log.debug("Processing Sub Function: " + text);
      try {
        ScraperFunction func = scraper.getFunction(m.group(1));
        if (func == null) {
          throw new Exception("Invalid Function Name: " + m.group(1));
        }
        XbmcScraperProcessor proc = newSubProcessor(func.isClearBuffers());

        // call the set buffer again with this result
        // setBuffer(buffer, proc.executeFunction(url.getFunctionName(), new
        // String[] {"", url.getTextContent()}), append);
        text = "<" + m.group(1) + ">" + proc.executeFunction(m.group(1), new String[] { "", m.group(2) }) + "</" + m.group(1) + ">";
      } catch (Exception e) {
        log.error("Failed to process function: " + text, e);
        text = "\n<error>" + text + "\n<msg>" + e.getMessage() + "</msg></error>\n";
      }
    }

    if (append) {
      String s = buffers[buffer];
      if (s != null) {
        log.debug("Appending to buffer: " + buffer);
        text = s + text;
      }
    }
    buffers[buffer] = text;
  }

  public void clearBuffers() {
    for (int i = 0; i < buffers.length; i++) {
      setBuffer(i, "", false);
    }
  }

  private void setBuffers(String[] input) {
    if (input == null)
      return;
    log.debug("Set Buffers: # of input Buffers: " + input.length);
    for (int i = 0; i < input.length; i++) {
      if (input[i] != null)
        setBuffer(i, input[i], false);
    }
  }

  public boolean containsFunction(String functionName) {
    return scraper.containsFunction(functionName);
  }

  public XbmcScraper getScraper() {
    return scraper;
  }

  public boolean isTruncateLogging() {
    return truncateLogging;
  }

  public void setTruncateLogging(boolean truncateLogging) {
    this.truncateLogging = truncateLogging;
  }

  /**
   * Return a copy of this processor. Clear the buffers if necessary.
   * 
   */
  public XbmcScraperProcessor newSubProcessor(boolean clearBuffers) {
    return new XbmcScraperProcessor(scraper, options, (clearBuffers) ? null : buffers);
  }
}
