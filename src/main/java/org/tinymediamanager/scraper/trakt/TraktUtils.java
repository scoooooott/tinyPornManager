package org.tinymediamanager.scraper.trakt;

import java.util.Date;

import org.threeten.bp.DateTimeUtils;
import org.threeten.bp.Instant;
import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalTime;
import org.threeten.bp.OffsetDateTime;
import org.threeten.bp.ZoneId;

public class TraktUtils {

  /**
   * converts a LocalDate to Date
   * 
   * @param date
   * @return Date or NULL
   */
  public static Date toDate(LocalDate date) {
    try {
      // we need to add time and zone to be able to convert :|
      LocalTime time = LocalTime.of(0, 0);
      Instant instant = date.atTime(time).atZone(ZoneId.systemDefault()).toInstant();
      Date d = DateTimeUtils.toDate(instant);
      return d;
    }
    catch (Exception e) {
    }
    return null;
  }

  /**
   * converts a OffsetDateTime to Date
   * 
   * @param date
   * @return Date or NULL
   */
  public static Date toDate(OffsetDateTime date) {
    try {
      Date d = DateTimeUtils.toDate(date.toInstant());
      return d;
    }
    catch (Exception e) {
    }
    return null;
  }
}
