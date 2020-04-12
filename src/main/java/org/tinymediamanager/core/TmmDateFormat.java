/*
 * Copyright 2012 - 2020 Manuel Laggner
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.tinymediamanager.core;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.win32.StdCallLibrary;

/**
 * the class {@link TmmDateFormat} is a utility class to get the best possible date format:<br />
 * it tries to parse the preferred formats out of the system settings in Windows, macOS and Linux.<br />
 * Inspired by IntelliJ (https://github.com/JetBrains/intellij-community/blob/master/platform/util/src/com/intellij/util/text/DateFormatUtil.java)
 *
 * @author Manuel Laggner
 */
public class TmmDateFormat {
  public static final Logger     LOGGER = LoggerFactory.getLogger(TmmDateFormat.class);

  /**
   * short date format: 01/01/18 - 01.01.18 - 01.01.2018
   */
  public static final DateFormat SHORT_DATE_FORMAT;

  /**
   * medium date format: Jun 30, 2009 - 30 juin 2009
   */
  public static final DateFormat MEDIUM_DATE_FORMAT;

  /**
   * long date format: November 23, 1937 - 30 juin 2009
   */
  public static final DateFormat LONG_DATE_FORMAT;

  /**
   * short time format: 3:30 PM - 15:30
   */
  public static final DateFormat SHORT_TIME_FORMAT;

  /**
   * medium time format: 7:03:47 AM - 07:03:07
   */
  public static final DateFormat MEDIUM_TIME_FORMAT;

  /**
   * short date short time format: 01/01/18 3:30 PM - 01.01.18 15:30
   */
  public static final DateFormat SHORT_DATE_SHORT_TIME_FORMAT;

  /**
   * short date medium time format: 01/01/18 7:03:47 AM - 01.01.18 07:03:47
   */
  public static final DateFormat SHORT_DATE_MEDIUM_TIME_FORMAT;

  /**
   * medium date short time format: Jun 30, 2009 7:03 AM - Jun 30, 2009 07:03
   */
  public static final DateFormat MEDIUM_DATE_SHORT_TIME_FORMAT;

  /**
   * medium date medium time format: Jun 30, 2009 7:03:47 AM - Jun 30, 2009 07:03:47
   */
  public static final DateFormat MEDIUM_DATE_MEDIUM_TIME_FORMAT;

  /**
   * long date short time format: November 23, 1937 7:03 AM - November 23, 1937 07:03
   */
  public static final DateFormat LONG_DATE_SHORT_TIME_FORMAT;

  /**
   * long date medium time format: November 23, 1937 7:03:47 AM - November 23, 1937 07:03:47
   */
  public static final DateFormat LONG_DATE_MEDIUM_TIME_FORMAT;

  static {
    DateFormat[] formats = getDateTimeFormats();
    SHORT_DATE_FORMAT = formats[0];
    MEDIUM_DATE_FORMAT = formats[1];
    LONG_DATE_FORMAT = formats[2];

    SHORT_TIME_FORMAT = formats[3];
    MEDIUM_TIME_FORMAT = formats[4];

    SHORT_DATE_SHORT_TIME_FORMAT = formats[5];
    SHORT_DATE_MEDIUM_TIME_FORMAT = formats[6];
    MEDIUM_DATE_SHORT_TIME_FORMAT = formats[7];
    MEDIUM_DATE_MEDIUM_TIME_FORMAT = formats[8];
    LONG_DATE_SHORT_TIME_FORMAT = formats[9];
    LONG_DATE_MEDIUM_TIME_FORMAT = formats[10];
  }

  private static DateFormat[] getDateTimeFormats() {
    boolean jnaAvailable = false;

    try {
      int ptrSize = Native.POINTER_SIZE;
      jnaAvailable = true;
    }
    catch (Throwable e) {
      LOGGER.error("could not load JNA: " + e.getMessage());
    }

    DateFormat[] formats = null;
    try {
      if (SystemUtils.IS_OS_MAC && jnaAvailable) {
        formats = getMacFormats();
      }
      else if (SystemUtils.IS_OS_LINUX || SystemUtils.IS_OS_UNIX) {
        formats = getUnixFormats();
      }
      else if (SystemUtils.IS_OS_WINDOWS && jnaAvailable) {
        formats = getWindowsFormats();
      }
    }
    catch (Throwable e) {
      LOGGER.error("could not load native date formats: " + e.getMessage());
    }

    if (formats == null || formats.length < 11) {
      // @formatter:off
      formats = new DateFormat[] { 
        DateFormat.getDateInstance(DateFormat.SHORT, Locale.getDefault()),
        DateFormat.getDateInstance(DateFormat.MEDIUM, Locale.getDefault()),
        DateFormat.getDateInstance(DateFormat.LONG, Locale.getDefault()),
              
        DateFormat.getTimeInstance(DateFormat.SHORT, Locale.getDefault()),
        DateFormat.getTimeInstance(DateFormat.MEDIUM, Locale.getDefault()),
              
        DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, Locale.getDefault()),
        DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.MEDIUM, Locale.getDefault()),
        DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT, Locale.getDefault()),
        DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM, Locale.getDefault()),
        DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.SHORT, Locale.getDefault()),
        DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.MEDIUM, Locale.getDefault())
      };
      // @formatter:on
    }

    return formats;
  }

  private interface CF extends Library {
    // https://developer.apple.com/documentation/corefoundation/cfdateformatter/date_formatter_styles
    long kCFDateFormatterNoStyle     = 0;
    long kCFDateFormatterShortStyle  = 1;
    long kCFDateFormatterMediumStyle = 2;
    long kCFDateFormatterLongStyle   = 3;

    class CFRange extends Structure implements Structure.ByValue {
      @Override
      protected List<String> getFieldOrder() {
        return Arrays.asList("location", "length");
      }

      public long location;
      public long length;

      public CFRange(long location, long length) {
        this.location = location;
        this.length = length;
      }
    }

    Pointer CFDateFormatterCreate(Pointer allocator, Pointer locale, long dateStyle, long timeStyle);

    Pointer CFDateFormatterGetFormat(Pointer formatter);

    long CFStringGetLength(Pointer str);

    void CFStringGetCharacters(Pointer str, CFRange range, char[] buffer);

    void CFRelease(Pointer p);
  }

  // platform-specific patterns: http://www.unicode.org/reports/tr35/tr35-31/tr35-dates.html#Date_Format_Patterns
  private static DateFormat[] getMacFormats() {
    CF cf = Native.load("CoreFoundation", CF.class);
    // @formatter:off
    return new DateFormat[] { 
      getMacFormat(cf, CF.kCFDateFormatterShortStyle, CF.kCFDateFormatterNoStyle), // short date
      getMacFormat(cf, CF.kCFDateFormatterMediumStyle, CF.kCFDateFormatterNoStyle), // medium date
      getMacFormat(cf, CF.kCFDateFormatterLongStyle, CF.kCFDateFormatterNoStyle), // long date     
            
      getMacFormat(cf, CF.kCFDateFormatterNoStyle, CF.kCFDateFormatterShortStyle), // short time
      getMacFormat(cf, CF.kCFDateFormatterNoStyle, CF.kCFDateFormatterLongStyle), // long time (medium not available acc. to docs)
            
      getMacFormat(cf, CF.kCFDateFormatterShortStyle, CF.kCFDateFormatterShortStyle), // short date short time
      getMacFormat(cf, CF.kCFDateFormatterShortStyle, CF.kCFDateFormatterMediumStyle), // short date medium time
      getMacFormat(cf, CF.kCFDateFormatterMediumStyle, CF.kCFDateFormatterShortStyle), // medium date short time
      getMacFormat(cf, CF.kCFDateFormatterMediumStyle, CF.kCFDateFormatterMediumStyle), // medium date medium time
      getMacFormat(cf, CF.kCFDateFormatterLongStyle, CF.kCFDateFormatterShortStyle), // long date short time
      getMacFormat(cf, CF.kCFDateFormatterLongStyle, CF.kCFDateFormatterMediumStyle) // long date medium time
    };
  }

  private static DateFormat getMacFormat(CF cf, long dateStyle, long timeStyle) {
    Pointer formatter = cf.CFDateFormatterCreate(null, null, dateStyle, timeStyle);
    if (formatter == null)
      throw new IllegalStateException("CFDateFormatterCreate: null");
    try {
      Pointer format = cf.CFDateFormatterGetFormat(formatter);
      int length = (int) cf.CFStringGetLength(format);
      char[] buffer = new char[length];
      cf.CFStringGetCharacters(format, new CF.CFRange(0, length), buffer);
      return formatFromString(new String(buffer));
    }
    finally {
      cf.CFRelease(formatter);
    }
  }

  private static DateFormat[] getUnixFormats() {
    String localeStr = System.getenv("LC_TIME");
    if (localeStr == null) {
      return null;
    }

    localeStr = localeStr.trim();
    int p = localeStr.indexOf('.');
    if (p > 0)
      localeStr = localeStr.substring(0, p);
    p = localeStr.indexOf('@');
    if (p > 0)
      localeStr = localeStr.substring(0, p);

    Locale locale;
    p = localeStr.indexOf('_');
    if (p < 0) {
      locale = new Locale(localeStr);
    }
    else {
      locale = new Locale(localeStr.substring(0, p), localeStr.substring(p + 1));
    }
    // @formatter:off
    return new DateFormat[] {
      DateFormat.getDateInstance(DateFormat.SHORT, locale),
      DateFormat.getDateInstance(DateFormat.MEDIUM, locale),
      DateFormat.getDateInstance(DateFormat.LONG, locale),

      DateFormat.getTimeInstance(DateFormat.SHORT, locale),
      DateFormat.getTimeInstance(DateFormat.MEDIUM, locale),

      DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, locale),
      DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.MEDIUM, locale),
      DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT, locale),
      DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM, locale),
      DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.SHORT, locale),
      DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.MEDIUM, locale),
    };
    // @formatter:on
  }

  @SuppressWarnings("SpellCheckingInspection")
  private interface Kernel32 extends StdCallLibrary {
    int LOCALE_SSHORTDATE  = 0x0000001F;
    int LOCALE_SLONGDATE   = 0x00000020;

    int LOCALE_SSHORTTIME  = 0x00000079;
    int LOCALE_STIMEFORMAT = 0x00001003;

    int GetLocaleInfoEx(String localeName, int lcType, char[] lcData, int dataSize);

    int GetLastError();
  }

  private static DateFormat[] getWindowsFormats() {
    Kernel32 kernel32 = Native.load("Kernel32", Kernel32.class);
    int bufferSize = 128, rv;
    char[] buffer = new char[bufferSize];

    rv = kernel32.GetLocaleInfoEx(null, Kernel32.LOCALE_SSHORTDATE, buffer, bufferSize);
    if (rv < 2) {
      throw new IllegalStateException("GetLocaleInfoEx: " + kernel32.GetLastError());
    }
    String shortDate = fixWindowsFormat(new String(buffer, 0, rv - 1));

    // no medium date available in windows
    String mediumDate = shortDate;

    rv = kernel32.GetLocaleInfoEx(null, Kernel32.LOCALE_SLONGDATE, buffer, bufferSize);
    if (rv < 2) {
      throw new IllegalStateException("GetLocaleInfoEx: " + kernel32.GetLastError());
    }
    String longDate = fixWindowsFormat(new String(buffer, 0, rv - 1));

    rv = kernel32.GetLocaleInfoEx(null, Kernel32.LOCALE_SSHORTTIME, buffer, bufferSize);
    if (rv < 2) {
      throw new IllegalStateException("GetLocaleInfoEx: " + kernel32.GetLastError());
    }
    String shortTime = fixWindowsFormat(new String(buffer, 0, rv - 1));

    rv = kernel32.GetLocaleInfoEx(null, Kernel32.LOCALE_STIMEFORMAT, buffer, bufferSize);
    if (rv < 2) {
      throw new IllegalStateException("GetLocaleInfoEx: " + kernel32.GetLastError());
    }
    String mediumTime = fixWindowsFormat(new String(buffer, 0, rv - 1));

    // @formatter:off
    return new DateFormat[] {
      formatFromString(shortDate),
      formatFromString(mediumDate),
      formatFromString(longDate),

      formatFromString(shortTime),
      formatFromString(mediumTime),

      formatFromString(shortDate + " " + shortTime),
      formatFromString(shortDate + " " + mediumTime),
      formatFromString(mediumDate + " " + shortTime),
      formatFromString(mediumDate + " " + mediumTime),
      formatFromString(longDate + " " + shortTime),
      formatFromString(longDate + " " + mediumTime)
    };
    // @formatter:on
  }

  private static String fixWindowsFormat(String format) {
    format = format.replaceAll("g+", "G");
    format = StringUtils.replace(format, "tt", "a");
    return format;
  }

  private static DateFormat formatFromString(String format) {
    try {
      return new SimpleDateFormat(format.trim());
    }
    catch (IllegalArgumentException e) {
      throw new IllegalArgumentException("unrecognized format string '" + format + "'");
    }
  }
}
