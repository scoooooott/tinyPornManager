/*
 *      Copyright (c) 2004-2012 YAMJ Members
 *      http://code.google.com/p/moviejukebox/people/list
 *
 *      Web: http://code.google.com/p/moviejukebox/
 *
 *      This software is licensed under a Creative Commons License
 *      See this page: http://code.google.com/p/moviejukebox/wiki/License
 *
 *      For any reuse or distribution, you must make clear to others the
 *      license terms of this work.
 */
package org.tinymediamanager.scraper.imdb;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

public class HTMLTools {

  private static final Map<Character, String> AGGRESSIVE_HTML_ENCODE_MAP = new HashMap<Character, String>();
  private static final Map<Character, String> DEFENSIVE_HTML_ENCODE_MAP  = new HashMap<Character, String>();
  private static final Map<String, Character> HTML_DECODE_MAP            = new HashMap<String, Character>();
  private static final Logger                 logger                     = Logger.getLogger(HTMLTools.class);

  static {
    /*
     * Html encoding mapping according to the HTML 4.0 spec
     * http://www.w3.org/TR/REC-html40/sgml/entities.html
     */

    // Special characters for HTML
    AGGRESSIVE_HTML_ENCODE_MAP.put('\u0026', "&amp;");
    AGGRESSIVE_HTML_ENCODE_MAP.put('\u003C', "&lt;");
    AGGRESSIVE_HTML_ENCODE_MAP.put('\u003E', "&gt;");
    AGGRESSIVE_HTML_ENCODE_MAP.put('\u0022', "&quot;");

    DEFENSIVE_HTML_ENCODE_MAP.put('\u0152', "&OElig;");
    DEFENSIVE_HTML_ENCODE_MAP.put('\u0153', "&oelig;");
    DEFENSIVE_HTML_ENCODE_MAP.put('\u0160', "&Scaron;");
    DEFENSIVE_HTML_ENCODE_MAP.put('\u0161', "&scaron;");
    DEFENSIVE_HTML_ENCODE_MAP.put('\u0178', "&Yuml;");
    DEFENSIVE_HTML_ENCODE_MAP.put('\u02C6', "&circ;");
    DEFENSIVE_HTML_ENCODE_MAP.put('\u02DC', "&tilde;");
    DEFENSIVE_HTML_ENCODE_MAP.put('\u2002', "&ensp;");
    DEFENSIVE_HTML_ENCODE_MAP.put('\u2003', "&emsp;");
    DEFENSIVE_HTML_ENCODE_MAP.put('\u2009', "&thinsp;");
    DEFENSIVE_HTML_ENCODE_MAP.put('\u200C', "&zwnj;");
    DEFENSIVE_HTML_ENCODE_MAP.put('\u200D', "&zwj;");
    DEFENSIVE_HTML_ENCODE_MAP.put('\u200E', "&lrm;");
    DEFENSIVE_HTML_ENCODE_MAP.put('\u200F', "&rlm;");
    DEFENSIVE_HTML_ENCODE_MAP.put('\u2013', "&ndash;");
    DEFENSIVE_HTML_ENCODE_MAP.put('\u2014', "&mdash;");
    DEFENSIVE_HTML_ENCODE_MAP.put('\u2018', "&lsquo;");
    DEFENSIVE_HTML_ENCODE_MAP.put('\u2019', "&rsquo;");
    DEFENSIVE_HTML_ENCODE_MAP.put('\u201A', "&sbquo;");
    DEFENSIVE_HTML_ENCODE_MAP.put('\u201C', "&ldquo;");
    DEFENSIVE_HTML_ENCODE_MAP.put('\u201D', "&rdquo;");
    DEFENSIVE_HTML_ENCODE_MAP.put('\u201E', "&bdquo;");
    DEFENSIVE_HTML_ENCODE_MAP.put('\u2020', "&dagger;");
    DEFENSIVE_HTML_ENCODE_MAP.put('\u2021', "&Dagger;");
    DEFENSIVE_HTML_ENCODE_MAP.put('\u2030', "&permil;");
    DEFENSIVE_HTML_ENCODE_MAP.put('\u2039', "&lsaquo;");
    DEFENSIVE_HTML_ENCODE_MAP.put('\u203A', "&rsaquo;");
    DEFENSIVE_HTML_ENCODE_MAP.put('\u20AC', "&euro;");

    // Character entity references for ISO 8859-1 characters
    DEFENSIVE_HTML_ENCODE_MAP.put('\u00A0', "&nbsp;");
    DEFENSIVE_HTML_ENCODE_MAP.put('\u00A1', "&iexcl;");
    DEFENSIVE_HTML_ENCODE_MAP.put('\u00A2', "&cent;");
    DEFENSIVE_HTML_ENCODE_MAP.put('\u00A3', "&pound;");
    DEFENSIVE_HTML_ENCODE_MAP.put('\u00A4', "&curren;");
    DEFENSIVE_HTML_ENCODE_MAP.put('\u00A5', "&yen;");
    DEFENSIVE_HTML_ENCODE_MAP.put('\u00A6', "&brvbar;");
    DEFENSIVE_HTML_ENCODE_MAP.put('\u00A7', "&sect;");
    DEFENSIVE_HTML_ENCODE_MAP.put('\u00A8', "&uml;");
    DEFENSIVE_HTML_ENCODE_MAP.put('\u00A9', "&copy;");
    DEFENSIVE_HTML_ENCODE_MAP.put('\u00AA', "&ordf;");
    DEFENSIVE_HTML_ENCODE_MAP.put('\u00AB', "&laquo;");
    DEFENSIVE_HTML_ENCODE_MAP.put('\u00AC', "&not;");
    DEFENSIVE_HTML_ENCODE_MAP.put('\u00AD', "&shy;");
    DEFENSIVE_HTML_ENCODE_MAP.put('\u00AE', "&reg;");
    DEFENSIVE_HTML_ENCODE_MAP.put('\u00AF', "&macr;");
    DEFENSIVE_HTML_ENCODE_MAP.put('\u00B0', "&deg;");
    DEFENSIVE_HTML_ENCODE_MAP.put('\u00B1', "&plusmn;");
    DEFENSIVE_HTML_ENCODE_MAP.put('\u00B2', "&sup2;");
    DEFENSIVE_HTML_ENCODE_MAP.put('\u00B3', "&sup3;");
    DEFENSIVE_HTML_ENCODE_MAP.put('\u00B4', "&acute;");
    DEFENSIVE_HTML_ENCODE_MAP.put('\u00B5', "&micro;");
    DEFENSIVE_HTML_ENCODE_MAP.put('\u00B6', "&para;");
    DEFENSIVE_HTML_ENCODE_MAP.put('\u00B7', "&middot;");
    DEFENSIVE_HTML_ENCODE_MAP.put('\u00B8', "&cedil;");
    DEFENSIVE_HTML_ENCODE_MAP.put('\u00B9', "&sup1;");
    DEFENSIVE_HTML_ENCODE_MAP.put('\u00BA', "&ordm;");
    DEFENSIVE_HTML_ENCODE_MAP.put('\u00BB', "&raquo;");
    DEFENSIVE_HTML_ENCODE_MAP.put('\u00BC', "&frac14;");
    DEFENSIVE_HTML_ENCODE_MAP.put('\u00BD', "&frac12;");
    DEFENSIVE_HTML_ENCODE_MAP.put('\u00BE', "&frac34;");
    DEFENSIVE_HTML_ENCODE_MAP.put('\u00BF', "&iquest;");
    DEFENSIVE_HTML_ENCODE_MAP.put('\u00C0', "&Agrave;");
    DEFENSIVE_HTML_ENCODE_MAP.put('\u00C1', "&Aacute;");
    DEFENSIVE_HTML_ENCODE_MAP.put('\u00C2', "&Acirc;");
    DEFENSIVE_HTML_ENCODE_MAP.put('\u00C3', "&Atilde;");
    DEFENSIVE_HTML_ENCODE_MAP.put('\u00C4', "&Auml;");
    DEFENSIVE_HTML_ENCODE_MAP.put('\u00C5', "&Aring;");
    DEFENSIVE_HTML_ENCODE_MAP.put('\u00C6', "&AElig;");
    DEFENSIVE_HTML_ENCODE_MAP.put('\u00C7', "&Ccedil;");
    DEFENSIVE_HTML_ENCODE_MAP.put('\u00C8', "&Egrave;");
    DEFENSIVE_HTML_ENCODE_MAP.put('\u00C9', "&Eacute;");
    DEFENSIVE_HTML_ENCODE_MAP.put('\u00CA', "&Ecirc;");
    DEFENSIVE_HTML_ENCODE_MAP.put('\u00CB', "&Euml;");
    DEFENSIVE_HTML_ENCODE_MAP.put('\u00CC', "&Igrave;");
    DEFENSIVE_HTML_ENCODE_MAP.put('\u00CD', "&Iacute;");
    DEFENSIVE_HTML_ENCODE_MAP.put('\u00CE', "&Icirc;");
    DEFENSIVE_HTML_ENCODE_MAP.put('\u00CF', "&Iuml;");
    DEFENSIVE_HTML_ENCODE_MAP.put('\u00D0', "&ETH;");
    DEFENSIVE_HTML_ENCODE_MAP.put('\u00D1', "&Ntilde;");
    DEFENSIVE_HTML_ENCODE_MAP.put('\u00D2', "&Ograve;");
    DEFENSIVE_HTML_ENCODE_MAP.put('\u00D3', "&Oacute;");
    DEFENSIVE_HTML_ENCODE_MAP.put('\u00D4', "&Ocirc;");
    DEFENSIVE_HTML_ENCODE_MAP.put('\u00D5', "&Otilde;");
    DEFENSIVE_HTML_ENCODE_MAP.put('\u00D6', "&Ouml;");
    DEFENSIVE_HTML_ENCODE_MAP.put('\u00D7', "&times;");
    DEFENSIVE_HTML_ENCODE_MAP.put('\u00D8', "&Oslash;");
    DEFENSIVE_HTML_ENCODE_MAP.put('\u00D9', "&Ugrave;");
    DEFENSIVE_HTML_ENCODE_MAP.put('\u00DA', "&Uacute;");
    DEFENSIVE_HTML_ENCODE_MAP.put('\u00DB', "&Ucirc;");
    DEFENSIVE_HTML_ENCODE_MAP.put('\u00DC', "&Uuml;");
    DEFENSIVE_HTML_ENCODE_MAP.put('\u00DD', "&Yacute;");
    DEFENSIVE_HTML_ENCODE_MAP.put('\u00DE', "&THORN;");
    DEFENSIVE_HTML_ENCODE_MAP.put('\u00DF', "&szlig;");
    DEFENSIVE_HTML_ENCODE_MAP.put('\u00E0', "&agrave;");
    DEFENSIVE_HTML_ENCODE_MAP.put('\u00E1', "&aacute;");
    DEFENSIVE_HTML_ENCODE_MAP.put('\u00E2', "&acirc;");
    DEFENSIVE_HTML_ENCODE_MAP.put('\u00E3', "&atilde;");
    DEFENSIVE_HTML_ENCODE_MAP.put('\u00E4', "&auml;");
    DEFENSIVE_HTML_ENCODE_MAP.put('\u00E5', "&aring;");
    DEFENSIVE_HTML_ENCODE_MAP.put('\u00E6', "&aelig;");
    DEFENSIVE_HTML_ENCODE_MAP.put('\u00E7', "&ccedil;");
    DEFENSIVE_HTML_ENCODE_MAP.put('\u00E8', "&egrave;");
    DEFENSIVE_HTML_ENCODE_MAP.put('\u00E9', "&eacute;");
    DEFENSIVE_HTML_ENCODE_MAP.put('\u00EA', "&ecirc;");
    DEFENSIVE_HTML_ENCODE_MAP.put('\u00EB', "&euml;");
    DEFENSIVE_HTML_ENCODE_MAP.put('\u00EC', "&igrave;");
    DEFENSIVE_HTML_ENCODE_MAP.put('\u00ED', "&iacute;");
    DEFENSIVE_HTML_ENCODE_MAP.put('\u00EE', "&icirc;");
    DEFENSIVE_HTML_ENCODE_MAP.put('\u00EF', "&iuml;");
    DEFENSIVE_HTML_ENCODE_MAP.put('\u00F0', "&eth;");
    DEFENSIVE_HTML_ENCODE_MAP.put('\u00F1', "&ntilde;");
    DEFENSIVE_HTML_ENCODE_MAP.put('\u00F2', "&ograve;");
    DEFENSIVE_HTML_ENCODE_MAP.put('\u00F3', "&oacute;");
    DEFENSIVE_HTML_ENCODE_MAP.put('\u00F4', "&ocirc;");
    DEFENSIVE_HTML_ENCODE_MAP.put('\u00F5', "&otilde;");
    DEFENSIVE_HTML_ENCODE_MAP.put('\u00F6', "&ouml;");
    DEFENSIVE_HTML_ENCODE_MAP.put('\u00F7', "&divide;");
    DEFENSIVE_HTML_ENCODE_MAP.put('\u00F8', "&oslash;");
    DEFENSIVE_HTML_ENCODE_MAP.put('\u00F9', "&ugrave;");
    DEFENSIVE_HTML_ENCODE_MAP.put('\u00FA', "&uacute;");
    DEFENSIVE_HTML_ENCODE_MAP.put('\u00FB', "&ucirc;");
    DEFENSIVE_HTML_ENCODE_MAP.put('\u00FC', "&uuml;");
    DEFENSIVE_HTML_ENCODE_MAP.put('\u00FD', "&yacute;");
    DEFENSIVE_HTML_ENCODE_MAP.put('\u00FE', "&thorn;");
    DEFENSIVE_HTML_ENCODE_MAP.put('\u00FF', "&yuml;");

    // Mathematical, Greek and Symbolic characters for HTML
    DEFENSIVE_HTML_ENCODE_MAP.put('\u0192', "&fnof;");
    DEFENSIVE_HTML_ENCODE_MAP.put('\u0391', "&Alpha;");
    DEFENSIVE_HTML_ENCODE_MAP.put('\u0392', "&Beta;");
    DEFENSIVE_HTML_ENCODE_MAP.put('\u0393', "&Gamma;");
    DEFENSIVE_HTML_ENCODE_MAP.put('\u0394', "&Delta;");
    DEFENSIVE_HTML_ENCODE_MAP.put('\u0395', "&Epsilon;");
    DEFENSIVE_HTML_ENCODE_MAP.put('\u0396', "&Zeta;");
    DEFENSIVE_HTML_ENCODE_MAP.put('\u0397', "&Eta;");
    DEFENSIVE_HTML_ENCODE_MAP.put('\u0398', "&Theta;");
    DEFENSIVE_HTML_ENCODE_MAP.put('\u0399', "&Iota;");
    DEFENSIVE_HTML_ENCODE_MAP.put('\u039A', "&Kappa;");
    DEFENSIVE_HTML_ENCODE_MAP.put('\u039B', "&Lambda;");
    DEFENSIVE_HTML_ENCODE_MAP.put('\u039C', "&Mu;");
    DEFENSIVE_HTML_ENCODE_MAP.put('\u039D', "&Nu;");
    DEFENSIVE_HTML_ENCODE_MAP.put('\u039E', "&Xi;");
    DEFENSIVE_HTML_ENCODE_MAP.put('\u039F', "&Omicron;");
    DEFENSIVE_HTML_ENCODE_MAP.put('\u03A0', "&Pi;");
    DEFENSIVE_HTML_ENCODE_MAP.put('\u03A1', "&Rho;");
    DEFENSIVE_HTML_ENCODE_MAP.put('\u03A3', "&Sigma;");
    DEFENSIVE_HTML_ENCODE_MAP.put('\u03A4', "&Tau;");
    DEFENSIVE_HTML_ENCODE_MAP.put('\u03A5', "&Upsilon;");
    DEFENSIVE_HTML_ENCODE_MAP.put('\u03A6', "&Phi;");
    DEFENSIVE_HTML_ENCODE_MAP.put('\u03A7', "&Chi;");
    DEFENSIVE_HTML_ENCODE_MAP.put('\u03A8', "&Psi;");
    DEFENSIVE_HTML_ENCODE_MAP.put('\u03A9', "&Omega;");
    DEFENSIVE_HTML_ENCODE_MAP.put('\u03B1', "&alpha;");
    DEFENSIVE_HTML_ENCODE_MAP.put('\u03B2', "&beta;");
    DEFENSIVE_HTML_ENCODE_MAP.put('\u03B3', "&gamma;");
    DEFENSIVE_HTML_ENCODE_MAP.put('\u03B4', "&delta;");
    DEFENSIVE_HTML_ENCODE_MAP.put('\u03B5', "&epsilon;");
    DEFENSIVE_HTML_ENCODE_MAP.put('\u03B6', "&zeta;");
    DEFENSIVE_HTML_ENCODE_MAP.put('\u03B7', "&eta;");
    DEFENSIVE_HTML_ENCODE_MAP.put('\u03B8', "&theta;");
    DEFENSIVE_HTML_ENCODE_MAP.put('\u03B9', "&iota;");
    DEFENSIVE_HTML_ENCODE_MAP.put('\u03BA', "&kappa;");
    DEFENSIVE_HTML_ENCODE_MAP.put('\u03BB', "&lambda;");
    DEFENSIVE_HTML_ENCODE_MAP.put('\u03BC', "&mu;");
    DEFENSIVE_HTML_ENCODE_MAP.put('\u03BD', "&nu;");
    DEFENSIVE_HTML_ENCODE_MAP.put('\u03BE', "&xi;");
    DEFENSIVE_HTML_ENCODE_MAP.put('\u03BF', "&omicron;");
    DEFENSIVE_HTML_ENCODE_MAP.put('\u03C0', "&pi;");
    DEFENSIVE_HTML_ENCODE_MAP.put('\u03C1', "&rho;");
    DEFENSIVE_HTML_ENCODE_MAP.put('\u03C2', "&sigmaf;");
    DEFENSIVE_HTML_ENCODE_MAP.put('\u03C3', "&sigma;");
    DEFENSIVE_HTML_ENCODE_MAP.put('\u03C4', "&tau;");
    DEFENSIVE_HTML_ENCODE_MAP.put('\u03C5', "&upsilon;");
    DEFENSIVE_HTML_ENCODE_MAP.put('\u03C6', "&phi;");
    DEFENSIVE_HTML_ENCODE_MAP.put('\u03C7', "&chi;");
    DEFENSIVE_HTML_ENCODE_MAP.put('\u03C8', "&psi;");
    DEFENSIVE_HTML_ENCODE_MAP.put('\u03C9', "&omega;");
    DEFENSIVE_HTML_ENCODE_MAP.put('\u03D1', "&thetasym;");
    DEFENSIVE_HTML_ENCODE_MAP.put('\u03D2', "&upsih;");
    DEFENSIVE_HTML_ENCODE_MAP.put('\u03D6', "&piv;");
    DEFENSIVE_HTML_ENCODE_MAP.put('\u2022', "&bull;");
    DEFENSIVE_HTML_ENCODE_MAP.put('\u2026', "&hellip;");
    DEFENSIVE_HTML_ENCODE_MAP.put('\u2032', "&prime;");
    DEFENSIVE_HTML_ENCODE_MAP.put('\u2033', "&Prime;");
    DEFENSIVE_HTML_ENCODE_MAP.put('\u203E', "&oline;");
    DEFENSIVE_HTML_ENCODE_MAP.put('\u2044', "&frasl;");
    DEFENSIVE_HTML_ENCODE_MAP.put('\u2118', "&weierp;");
    DEFENSIVE_HTML_ENCODE_MAP.put('\u2111', "&image;");
    DEFENSIVE_HTML_ENCODE_MAP.put('\u211C', "&real;");
    DEFENSIVE_HTML_ENCODE_MAP.put('\u2122', "&trade;");
    DEFENSIVE_HTML_ENCODE_MAP.put('\u2135', "&alefsym;");
    DEFENSIVE_HTML_ENCODE_MAP.put('\u2190', "&larr;");
    DEFENSIVE_HTML_ENCODE_MAP.put('\u2191', "&uarr;");
    DEFENSIVE_HTML_ENCODE_MAP.put('\u2192', "&rarr;");
    DEFENSIVE_HTML_ENCODE_MAP.put('\u2193', "&darr;");
    DEFENSIVE_HTML_ENCODE_MAP.put('\u2194', "&harr;");
    DEFENSIVE_HTML_ENCODE_MAP.put('\u21B5', "&crarr;");
    DEFENSIVE_HTML_ENCODE_MAP.put('\u21D0', "&lArr;");
    DEFENSIVE_HTML_ENCODE_MAP.put('\u21D1', "&uArr;");
    DEFENSIVE_HTML_ENCODE_MAP.put('\u21D2', "&rArr;");
    DEFENSIVE_HTML_ENCODE_MAP.put('\u21D3', "&dArr;");
    DEFENSIVE_HTML_ENCODE_MAP.put('\u21D4', "&hArr;");
    DEFENSIVE_HTML_ENCODE_MAP.put('\u2200', "&forall;");
    DEFENSIVE_HTML_ENCODE_MAP.put('\u2202', "&part;");
    DEFENSIVE_HTML_ENCODE_MAP.put('\u2203', "&exist;");
    DEFENSIVE_HTML_ENCODE_MAP.put('\u2205', "&empty;");
    DEFENSIVE_HTML_ENCODE_MAP.put('\u2207', "&nabla;");
    DEFENSIVE_HTML_ENCODE_MAP.put('\u2208', "&isin;");
    DEFENSIVE_HTML_ENCODE_MAP.put('\u2209', "&notin;");
    DEFENSIVE_HTML_ENCODE_MAP.put('\u220B', "&ni;");
    DEFENSIVE_HTML_ENCODE_MAP.put('\u220F', "&prod;");
    DEFENSIVE_HTML_ENCODE_MAP.put('\u2211', "&sum;");
    DEFENSIVE_HTML_ENCODE_MAP.put('\u2212', "&minus;");
    DEFENSIVE_HTML_ENCODE_MAP.put('\u2217', "&lowast;");
    DEFENSIVE_HTML_ENCODE_MAP.put('\u221A', "&radic;");
    DEFENSIVE_HTML_ENCODE_MAP.put('\u221D', "&prop;");
    DEFENSIVE_HTML_ENCODE_MAP.put('\u221E', "&infin;");
    DEFENSIVE_HTML_ENCODE_MAP.put('\u2220', "&ang;");
    DEFENSIVE_HTML_ENCODE_MAP.put('\u2227', "&and;");
    DEFENSIVE_HTML_ENCODE_MAP.put('\u2228', "&or;");
    DEFENSIVE_HTML_ENCODE_MAP.put('\u2229', "&cap;");
    DEFENSIVE_HTML_ENCODE_MAP.put('\u222A', "&cup;");
    DEFENSIVE_HTML_ENCODE_MAP.put('\u222B', "&int;");
    DEFENSIVE_HTML_ENCODE_MAP.put('\u2234', "&there4;");
    DEFENSIVE_HTML_ENCODE_MAP.put('\u223C', "&sim;");
    DEFENSIVE_HTML_ENCODE_MAP.put('\u2245', "&cong;");
    DEFENSIVE_HTML_ENCODE_MAP.put('\u2248', "&asymp;");
    DEFENSIVE_HTML_ENCODE_MAP.put('\u2260', "&ne;");
    DEFENSIVE_HTML_ENCODE_MAP.put('\u2261', "&equiv;");
    DEFENSIVE_HTML_ENCODE_MAP.put('\u2264', "&le;");
    DEFENSIVE_HTML_ENCODE_MAP.put('\u2265', "&ge;");
    DEFENSIVE_HTML_ENCODE_MAP.put('\u2282', "&sub;");
    DEFENSIVE_HTML_ENCODE_MAP.put('\u2283', "&sup;");
    DEFENSIVE_HTML_ENCODE_MAP.put('\u2284', "&nsub;");
    DEFENSIVE_HTML_ENCODE_MAP.put('\u2286', "&sube;");
    DEFENSIVE_HTML_ENCODE_MAP.put('\u2287', "&supe;");
    DEFENSIVE_HTML_ENCODE_MAP.put('\u2295', "&oplus;");
    DEFENSIVE_HTML_ENCODE_MAP.put('\u2297', "&otimes;");
    DEFENSIVE_HTML_ENCODE_MAP.put('\u22A5', "&perp;");
    DEFENSIVE_HTML_ENCODE_MAP.put('\u22C5', "&sdot;");
    DEFENSIVE_HTML_ENCODE_MAP.put('\u2308', "&lceil;");
    DEFENSIVE_HTML_ENCODE_MAP.put('\u2309', "&rceil;");
    DEFENSIVE_HTML_ENCODE_MAP.put('\u230A', "&lfloor;");
    DEFENSIVE_HTML_ENCODE_MAP.put('\u230B', "&rfloor;");
    DEFENSIVE_HTML_ENCODE_MAP.put('\u2329', "&lang;");
    DEFENSIVE_HTML_ENCODE_MAP.put('\u232A', "&rang;");
    DEFENSIVE_HTML_ENCODE_MAP.put('\u25CA', "&loz;");
    DEFENSIVE_HTML_ENCODE_MAP.put('\u2660', "&spades;");
    DEFENSIVE_HTML_ENCODE_MAP.put('\u2663', "&clubs;");
    DEFENSIVE_HTML_ENCODE_MAP.put('\u2665', "&hearts;");
    DEFENSIVE_HTML_ENCODE_MAP.put('\u2666', "&diams;");

    Set<Map.Entry<Character, String>> aggresiveEntries = AGGRESSIVE_HTML_ENCODE_MAP.entrySet();
    for (Map.Entry<Character, String> entry : aggresiveEntries) {
      HTML_DECODE_MAP.put(entry.getValue(), entry.getKey());
    }

    Set<Map.Entry<Character, String>> defensiveEntries = DEFENSIVE_HTML_ENCODE_MAP.entrySet();
    for (Map.Entry<Character, String> entry : defensiveEntries) {
      HTML_DECODE_MAP.put(entry.getValue(), entry.getKey());
    }
  }

  public static String decodeHtml(String source) {
    if (null == source || 0 == source.length()) {
      return source;
    }

    int currentIndex = 0;
    int delimiterStartIndex;
    int delimiterEndIndex;

    StringBuilder result = null;

    while (currentIndex <= source.length()) {
      delimiterStartIndex = source.indexOf('&', currentIndex);
      if (delimiterStartIndex != -1) {
        delimiterEndIndex = source.indexOf(';', delimiterStartIndex + 1);
        if (delimiterEndIndex != -1) {
          // ensure that the string builder is setup correctly
          if (null == result) {
            result = new StringBuilder();
          }

          // add the text that leads up to this match
          if (delimiterStartIndex > currentIndex) {
            result.append(new String(source.substring(currentIndex, delimiterStartIndex)));
          }

          // add the decoded entity
          String entity = new String(source.substring(delimiterStartIndex, delimiterEndIndex + 1));

          currentIndex = delimiterEndIndex + 1;

          // try to decoded numeric entities
          if (entity.charAt(1) == '#') {
            int start = 2;
            int radix = 10;
            // check if the number is hexadecimal
            if (entity.charAt(2) == 'X' || entity.charAt(2) == 'x') {
              start++;
              radix = 16;
            }
            try {
              Character c = Character.valueOf((char) Integer.parseInt(entity.substring(start, entity.length() - 1), radix));
              result.append(c);
            } // when the number of the entity can't be parsed, add the entity
              // as-is
            catch (NumberFormatException error) {
              result.append(entity);
            }
          }
          else {
            // try to decode the entity as a literal
            Character decoded = HTML_DECODE_MAP.get(entity);
            if (decoded != null) {
              result.append(decoded);
            } // if there was no match, add the entity as-is
            else {
              result.append(entity);
            }
          }
        }
        else {
          break;
        }
      }
      else {
        break;
      }
    }

    if (null == result) {
      return source;
    }
    else if (currentIndex < source.length()) {
      result.append(new String(source.substring(currentIndex)));
    }

    return result.toString();
  }

  public static String decodeUrl(String url) {
    if (url != null && url.length() != 0) {
      try {
        return URLDecoder.decode(url, "UTF-8");
      }
      catch (UnsupportedEncodingException ignored) {
        logger.info("Could not decode URL string: " + url + ", will proceed with undecoded string.");
      }
    }
    return url;
  }

  public static String encodeUrl(String url) {
    String returnUrl = url;

    if (url != null && url.length() != 0) {
      try {
        returnUrl = URLEncoder.encode(url, "UTF-8");
        returnUrl = returnUrl.replace((CharSequence) "+", (CharSequence) "%20"); // why
                                                                                 // does
                                                                                 // URLEncoder
                                                                                 // do
                                                                                 // that??!!
      }
      catch (UnsupportedEncodingException ignored) {
        logger.info("Could not decode URL string: " + returnUrl + ", will proceed with undecoded string.");
      }
    }
    return returnUrl;
  }

  public static String encodeUrlPath(String url) {
    if (url != null && url.length() != 0) {
      int slash = url.lastIndexOf('/');
      String parentPart = "";
      if (slash != -1) {
        parentPart = encodeUrlPath(new String(url.substring(0, slash))) + '/';
      }
      return parentPart + encodeUrl(new String(url.substring(slash + 1)));
    }
    return url;
  }

  public static List<String> extractHtmlTags(String src, String sectionStart, String sectionEnd, String startTag, String endTag) {
    ArrayList<String> tags = new ArrayList<String>();
    int index = src.indexOf(sectionStart);
    if (index == -1) {
      return tags;
    }
    index += sectionStart.length();
    int endIndex = src.indexOf(sectionEnd, index);
    if (endIndex == -1) {
      return tags;
    }

    String sectionText = new String(src.substring(index, endIndex));
    int lastIndex = sectionText.length();
    index = 0;
    int endLen = endTag.length();

    if (startTag != null) {
      index = sectionText.indexOf(startTag);
    }

    while (index != -1) {
      endIndex = sectionText.indexOf(endTag, index);
      if (endIndex == -1) {
        endIndex = lastIndex;
      }
      endIndex += endLen;
      String text = new String(sectionText.substring(index, endIndex));
      tags.add(text);
      if (endIndex > lastIndex) {
        break;
      }
      if (startTag != null) {
        index = sectionText.indexOf(startTag, endIndex);
      }
      else {
        index = endIndex;
      }
    }
    return tags;
  }

  public static String extractTag(String src, String findStr) {
    return extractTag(src, findStr, 0);
  }

  public static String extractTag(String src, String findStr, int skip) {
    return extractTag(src, findStr, skip, "><");
  }

  public static String extractTag(String src, String findStr, int skip, String separator) {
    return extractTag(src, findStr, skip, separator, true);
  }

  public static String extractTag(String src, String findStr, int skip, String separator, boolean checkDirty) {
    int beginIndex = src.indexOf(findStr);

    String value = "";

    if (beginIndex >= 0) {
      StringTokenizer st = new StringTokenizer(new String(src.substring(beginIndex + findStr.length())), separator);
      for (int i = 0; i < skip; i++) {
        st.nextToken();
      }

      value = HTMLTools.decodeHtml(st.nextToken().trim());

      if (checkDirty && value.indexOf("uiv=\"content-ty") != -1 || value.indexOf("cast") != -1 || value.indexOf("title") != -1
          || value.indexOf('<') != -1) {
        value = "";
      }
    }

    return value;
  }

  public static String extractTag(String src, String startStr, String endStr) {
    int beginIndex = src.indexOf(startStr);

    if (beginIndex < 0) {
      return "";
    }

    try {
      String subString = new String(src.substring(beginIndex + startStr.length()));
      int endIndex = subString.indexOf(endStr);
      if (endIndex < 0) {
        return "";
      }
      subString = new String(subString.substring(0, endIndex));
      return HTMLTools.decodeHtml(subString.trim());
    }
    catch (Exception error) {
      return "";
    }
  }

  public static List<String> extractTags(String src, String sectionStart) {
    return extractTags(src, sectionStart, "</div>");
  }

  public static List<String> extractTags(String src, String sectionStart, String sectionEnd) {
    return extractTags(src, sectionStart, sectionEnd, null, "|");
  }

  public static List<String> extractTags(String src, String sectionStart, String sectionEnd, String startTag, String endTag) {
    return extractTags(src, sectionStart, sectionEnd, startTag, endTag, true);
  }

  public static List<String> extractTags(String src, String sectionStart, String sectionEnd, String startTag, String endTag, boolean forceCloseTag) {
    ArrayList<String> tags = new ArrayList<String>();
    int startIndex = src.indexOf(sectionStart);
    if (startIndex == -1) {
      return tags;
    }
    startIndex += sectionStart.length();
    int endIndex = src.indexOf(sectionEnd, startIndex);
    if (endIndex == -1) {
      return tags;
    }

    String sectionText = new String(src.substring(startIndex, endIndex));
    int lastIndex = sectionText.length();
    startIndex = 0;
    int startLen = 0;
    int endLen = endTag.length();

    if (startTag != null) {
      startIndex = sectionText.indexOf(startTag);
      startLen = startTag.length();
    }

    while (startIndex != -1) {
      startIndex += startLen;
      if (forceCloseTag) {
        int close = sectionText.indexOf('>', startIndex);
        if (close != -1) {
          startIndex = close + 1;
        }
      }
      endIndex = sectionText.indexOf(endTag, startIndex);
      if (endIndex == -1) {
        endIndex = lastIndex;
      }
      String text = new String(sectionText.substring(startIndex, endIndex));

      tags.add(HTMLTools.decodeHtml(text.trim()));
      endIndex += endLen;
      if (endIndex > lastIndex) {
        break;
      }
      if (startTag != null) {
        startIndex = sectionText.indexOf(startTag, endIndex);
      }
      else {
        startIndex = endIndex;
      }
    }
    return tags;
  }

  public static String getTextAfterElem(String src, String findStr) {
    return getTextAfterElem(src, findStr, 0);
  }

  public static String getTextAfterElem(String src, String findStr, int skip) {
    return getTextAfterElem(src, findStr, skip, 0);
  }

  /**
   * Example: src = "<a id="specialID"><br/>
   * <img src="a.gif"/>my text</a> findStr = "specialID" result = "my text"
   * 
   * @param src
   *          html text
   * @param findStr
   *          string to find in src
   * @param skip
   *          count of found texts to skip
   * @param fromIndex
   *          begin index in src
   * @return string from html text which is plain text without html tags
   */
  public static String getTextAfterElem(String src, String findStr, int skip, int fromIndex) {
    int beginIndex = src.indexOf(findStr, fromIndex);
    if (beginIndex == -1) {
      return "";
    }
    StringTokenizer st = new StringTokenizer(new String(src.substring(beginIndex + findStr.length())), "<");
    int i = 0;
    while (st.hasMoreElements()) {
      String elem = st.nextToken().replaceAll("&nbsp;|&#160;", "").trim();
      if (elem.length() != 0 && !elem.endsWith(">") && i++ >= skip) {
        String[] elems = elem.split(">");
        if (elems.length > 1) {
          return HTMLTools.decodeHtml(elems[1].trim());
        }
        else {
          return HTMLTools.decodeHtml(elems[0].trim());
        }
      }
    }
    return "";
  }

  public static String removeHtmlTags(String src) {
    return src.replaceAll("\\<.*?>", "");
  }

  public static String stripTags(String s) {
    Pattern stripTagsRegex = Pattern.compile("([^\\<]*)(?:\\<[^\\>]*\\>)?");
    Matcher m = stripTagsRegex.matcher(s);

    StringBuilder res = new StringBuilder();
    while (m.find()) {
      res.append(m.group(1));
    }

    // Replace escaped spaces
    String finalRes = res.toString().replaceAll("&nbsp;", " ");

    return finalRes.trim();
  }
}