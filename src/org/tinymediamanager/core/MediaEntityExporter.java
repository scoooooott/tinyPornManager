package org.tinymediamanager.core;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.core.entities.MediaEntity;

import com.floreysoft.jmte.Engine;
import com.floreysoft.jmte.NamedRenderer;
import com.floreysoft.jmte.RenderFormatInfo;
import com.floreysoft.jmte.encoder.Encoder;
import com.floreysoft.jmte.encoder.XMLEncoder;
import com.floreysoft.jmte.message.ParseException;

public abstract class MediaEntityExporter {
  private final static Logger   LOGGER             = LoggerFactory.getLogger(MediaEntityExporter.class);
  protected static final String TEMPLATE_DIRECTORY = "templates";

  protected Engine              engine;
  protected Properties          properties;
  protected String              fileExtension;
  protected String              listTemplate       = "";
  protected String              detailTemplate     = "";
  protected File                templateDir;

  public enum TemplateType {
    MOVIE, TV_SHOW
  }

  protected MediaEntityExporter(String pathToTemplate, TemplateType type) throws Exception {
    // check if template exists and is valid
    templateDir = new File(pathToTemplate);
    if (!templateDir.exists() || !templateDir.isDirectory()) {
      throw new Exception("illegal template");
    }

    File configFile = new File(pathToTemplate, "template.conf");
    if (!configFile.exists() || !configFile.isFile()) {
      throw new Exception("illegal template");
    }

    // load settings from template
    properties = new Properties();
    BufferedInputStream stream = new BufferedInputStream(new FileInputStream(configFile));
    properties.load(stream);
    stream.close();

    // check needed settings
    String typeInConfig = properties.getProperty("type");
    if (!typeInConfig.equalsIgnoreCase(type.name())) {
      throw new Exception("illegal template");
    }

    String listTemplateFile = properties.getProperty("list");
    if (StringUtils.isBlank(listTemplateFile)) {
      throw new Exception("illegal template");
    }

    // get other settings
    String detailTemplateFile = properties.getProperty("detail");
    fileExtension = properties.getProperty("extension");
    if (StringUtils.isBlank(fileExtension)) {
      fileExtension = "html";
    }

    // set up engine
    engine = Engine.createCachingEngine();

    if (fileExtension.equalsIgnoreCase("html")) {
      engine.setEncoder(new HtmlEncoder()); // special char replacement
    }
    if (fileExtension.equalsIgnoreCase("xml")) {
      engine.setEncoder(new XMLEncoder()); // special char replacement
    }

    // load list template from File
    listTemplate = FileUtils.readFileToString(new File(pathToTemplate, listTemplateFile), "UTF-8");
    if (StringUtils.isNotBlank(detailTemplateFile)) {
      detailTemplate = FileUtils.readFileToString(new File(pathToTemplate, detailTemplateFile), "UTF-8");
    }
  }

  abstract public <T extends MediaEntity> void export(List<T> entitiesToExport, String pathToExport) throws Exception;

  /**
   * Find templates for the given type.
   * 
   * @param type
   *          the template type
   * @return the list of all found template types
   */
  public static List<ExportTemplate> findTemplates(TemplateType type) {
    List<ExportTemplate> templatesFound = new ArrayList<ExportTemplate>();

    // search in template folder for templates
    File root = new File(TEMPLATE_DIRECTORY);
    if (!root.exists() || !root.isDirectory()) {
      return templatesFound;
    }

    // search ever subdir
    File[] templateDirs = root.listFiles();
    for (File dir : templateDirs) {
      if (!dir.isDirectory()) {
        continue;
      }

      // get type of template
      File config = new File(dir, "template.conf");
      if (!config.exists()) {
        continue;
      }

      // load settings from template
      Properties properties = new Properties();
      try {
        BufferedInputStream stream = new BufferedInputStream(new FileInputStream(config));
        properties.load(stream);
        stream.close();
      }
      catch (Exception e) {
        LOGGER.warn("error in config: " + dir.getAbsolutePath() + " | " + e.getMessage());
        continue;
      }

      // get template type
      String typeInConfig = properties.getProperty("type");
      if (StringUtils.isBlank(typeInConfig)) {
        continue;
      }

      if (typeInConfig.equalsIgnoreCase(type.name())) {
        ExportTemplate template = new ExportTemplate();
        template.setName(properties.getProperty("name"));
        template.setType(type);
        template.setPath(dir.getAbsolutePath());
        template.setUrl(properties.getProperty("url"));
        template.setDescription(properties.getProperty("description"));
        if (StringUtils.isNotBlank(properties.getProperty("detail"))) {
          template.setDetail(true);
        }
        else {
          template.setDetail(false);
        }

        templatesFound.add(template);
      }
    }

    return templatesFound;
  }

  public static class NamedDateRenderer implements NamedRenderer {
    private static final String DEFAULT_PATTERN = "dd.MM.yyyy HH:mm:ss Z";

    private Date convert(Object o, DateFormat dateFormat) {
      if (o instanceof Date) {
        return (Date) o;
      }
      else if (o instanceof Number) {
        long longValue = ((Number) o).longValue();
        return new Date(longValue);
      }
      else if (o instanceof String) {
        try {
          try {
            return dateFormat.parse((String) o);
          }
          catch (java.text.ParseException e) {
            LOGGER.warn("cannot convert date format", e);
          }
        }
        catch (ParseException e) {
        }
      }
      return null;
    }

    @Override
    public String getName() {
      return "date";
    }

    @Override
    public Class<?>[] getSupportedClasses() {
      return new Class[] { Date.class, String.class, Integer.class, Long.class };
    }

    @Override
    public String render(Object o, String pattern, Locale locale) {
      String patternToUse = pattern != null ? pattern : DEFAULT_PATTERN;
      try {
        DateFormat dateFormat = new SimpleDateFormat(patternToUse);
        Date value = convert(o, dateFormat);
        if (value != null) {
          String format = dateFormat.format(value);
          return format;
        }
      }
      catch (IllegalArgumentException iae) {
      }
      catch (NullPointerException npe) {
      }
      return null;
    }

    @Override
    public RenderFormatInfo getFormatInfo() {
      return null;
    }
  }

  public static class HtmlEncoder implements Encoder {
    @Override
    public String encode(String arg0) {
      return StringEscapeUtils.escapeHtml4(arg0);
    }
  }
}
