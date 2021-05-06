package org.tinymediamanager.scraper.pornhub.util;

import java.util.Locale;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.Optional;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;

public class BigNumberFormat {

  private static final NavigableMap<Long, String> suffixes = new TreeMap<>();
  private static final Pattern PATTERN = Pattern.compile("^(\\d+)([KMGTPE])$");


  static {
    suffixes.put(1_000L, "K");
    suffixes.put(1_000_000L, "M");
    suffixes.put(1_000_000_000L, "G");
    suffixes.put(1_000_000_000_000L, "T");
    suffixes.put(1_000_000_000_000_000L, "P");
    suffixes.put(1_000_000_000_000_000_000L, "E");
  }

  public static Long format(String value) {
    if (value == null) {
      return null;
    }
    value = value.trim().toUpperCase(Locale.ROOT);
    if (StringUtils.isEmpty(value)) {
      return null;
    }
    if (value.matches("^\\d+$")) {
      return Long.parseLong(value);
    }

    Matcher matcher = PATTERN.matcher(value);
    if (matcher.matches()) {
      String suffix = matcher.group(2);
      Optional<Entry<Long, String>> e = suffixes.entrySet().stream()
          .filter(item -> item.getValue().equals(suffix)).findFirst();
      if (e.isPresent()) {
        long multiplyBy = e.get().getKey();
        return Long.parseLong(matcher.group(1)) * multiplyBy;
      }
    }

    return null;
  }

  public static String format(long value) {
    //Long.MIN_VALUE == -Long.MIN_VALUE so we need an adjustment here
    if (value == Long.MIN_VALUE) {
      return format(Long.MIN_VALUE + 1);
    }
    if (value < 0) {
      return "-" + format(-value);
    }
    if (value < 1000) {
      return Long.toString(value); //deal with easy case
    }

    Entry<Long, String> e = suffixes.floorEntry(value);
    Long divideBy = e.getKey();
    String suffix = e.getValue();

    long truncated = value / (divideBy / 10); //the number part of the output times 10
    boolean hasDecimal = truncated < 100 && (truncated / 10d) != (truncated / 10);
    return hasDecimal ? (truncated / 10d) + suffix : (truncated / 10) + suffix;
  }

}
