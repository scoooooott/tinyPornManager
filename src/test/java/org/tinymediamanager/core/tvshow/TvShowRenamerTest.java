package org.tinymediamanager.core.tvshow;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Test;

public class TvShowRenamerTest {
  private static final String[] seasonNumbers  = { "$1", "$2" };
  private static final String[] episodeNumbers = { "$3", "$4", "$E", "$D" };
  private static final String[] episodeTitles  = { "$T" };
  // private static final String[] showTitles = { "$N", "$M" };

  private static Pattern        epDelimiter    = Pattern.compile("(\\s?(episode|[epx]+)\\s?)?\\$[34ED]", Pattern.CASE_INSENSITIVE);
  private static Pattern        seDelimiter    = Pattern.compile("((season|s)\\s?)?[\\$][12]", Pattern.CASE_INSENSITIVE);

  @Test
  public void tvRenamerPatterns() {
    detect("Season $1", "$N - S$2E$E - $T"); // Season 1/Showname - S01E01 - episode.avi
    detect("", "$N - S$2 Episode $E - $T"); // Showname - S01E01 - episode.avi // empty season dir!
    detect("Season $1", "E$E - $T"); // Season 1/E01 - episode.avi
    detect("Season $1", "$T - E$E"); // Season 1/episode - E01.avi
    detect("", "E$E - $T"); // E01 - episode.avi // no season at all!
    detect("", "$1$E"); // 102.avi
    detect("", "$1x$E"); // 1x02.avi
    detect("", "$T"); // episode.avi // only title!
    detect("", "S$1EP$E $D"); // episode.avi // FIXME: double episode pattern
  }

  private void detect(String seasonPattern, String filePattern) {
    System.out.println("Processing pattern: " + seasonPattern + "/" + filePattern);
    if (!isRecommended(seasonPattern, filePattern)) {
      System.out.println(" -> NOT recommended, but valid.");
    }

    genExampleSingleFile(seasonPattern, filePattern);
    genExampleMultiFile(seasonPattern, filePattern);

    System.out.println("------------");
  }

  private void genExampleSingleFile(String seasonPattern, String filePattern) {
    String combined = new File("showname (year)", seasonPattern + "/" + filePattern + ".avi").toString();
    combined = combined.replaceAll("[$][12]", "01"); // season
    combined = combined.replaceAll("[$][34ED]", "02"); // episode
    combined = combined.replaceAll("[$][T]", "first name"); // title
    combined = combined.replaceAll("[$][NM]", "showname"); // show
    System.out.println("Single file would be: \"" + combined + "\"");
  }

  private void genExampleMultiFile(String seasonPattern, String filePattern) {
    String combined = new File("showname (year)", seasonPattern + "/" + filePattern + ".avi").toString();

    String loopNumbers = "";
    String loopTitles = "";

    // *******************
    // LOOP 1 - season/episode
    // *******************
    int sePos = getPatternPos(filePattern, seasonNumbers);
    if (sePos > -1) {
      Matcher m = seDelimiter.matcher(filePattern);
      if (m.find()) {
        if (m.group(1) != null) {
          loopNumbers += m.group(1); // delimiter
        }
        loopNumbers += filePattern.substring(m.end() - 2, m.end()); // add replacer
      }
    }

    int epPos = getPatternPos(filePattern, episodeNumbers);
    if (epPos > -1) {
      Matcher m = epDelimiter.matcher(filePattern);
      if (m.find()) {
        if (m.group(1) != null) {
          loopNumbers += m.group(1); // delimiter
        }
        loopNumbers += filePattern.substring(m.end() - 2, m.end()); // add replacer
      }
    }
    loopNumbers = loopNumbers.trim();

    // foreach multifile, replace and append pattern:
    String mf1 = loopNumbers.replaceAll("[$][12]", "01").replaceAll("[$][34ED]", "02");
    String mf2 = loopNumbers.replaceAll("[$][12]", "01").replaceAll("[$][34ED]", "03");
    // replace original pattern, with our combined
    if (!loopNumbers.isEmpty()) {
      combined = combined.replace(loopNumbers, mf1 + " " + mf2);
    }

    // *******************
    // LOOP 2 - title
    // *******************
    int titlePos = getPatternPos(filePattern, episodeTitles);
    if (titlePos > -1) {
      loopTitles += filePattern.substring(titlePos, titlePos + 2); // add replacer
    }
    loopTitles = loopTitles.trim();
    String tit1 = loopTitles.replaceAll("[$][T]", "first name");
    String tit2 = loopTitles.replaceAll("[$][T]", "second name");
    // replace original pattern, with our combined
    if (!loopTitles.isEmpty()) {
      combined = combined.replace(loopTitles, tit1 + " " + tit2);
    }

    // *******************
    // replace other patterns
    // *******************
    combined = combined.replaceAll("[$][12]", "01"); // season (eg for folder)
    combined = combined.replaceAll("[$][NM]", "showname"); // show

    System.out.println("Multi  file would be: \"" + combined + "\"");
  }

  /**
   * checks, if the pattern has a recommended structure (S/E numbers, title filled)<br>
   * when false, it might lead to some unpredictable renamings...
   * 
   * @param pattern
   * @return
   */
  private boolean isRecommended(String seasonPattern, String filePattern) {
    // count em
    int epCnt = count(filePattern, episodeNumbers);
    int titleCnt = count(filePattern, episodeTitles);
    int seCnt = count(filePattern, seasonNumbers);
    int seFolderCnt = count(seasonPattern, seasonNumbers);// check season folder pattern

    // check rules
    if (epCnt != 1 || titleCnt != 1 || (seCnt + seFolderCnt) > 2 || (seCnt + seFolderCnt) == 0) {
      System.out.println("Too many/less episode/season/title replacer patterns");
      return false;
    }

    int epPos = getPatternPos(filePattern, episodeNumbers);
    int sePos = getPatternPos(filePattern, seasonNumbers);
    int titlePos = getPatternPos(filePattern, episodeTitles);

    if (sePos > epPos) {
      System.out.println("Season pattern should be before episode pattern!");
      return false;
    }

    // check if title not in-between season/episode pattern in file
    if (titleCnt == 1 && seCnt == 1) {
      if (titlePos < epPos && titlePos > sePos) {
        System.out.println("Title should not be between season/episode pattern");
        return false;
      }
    }

    return true;
  }

  /**
   * Count the amount of renamer tokens per group
   * 
   * @param pattern
   * @param possibleValues
   * @return 0, or amount
   */
  private int count(String pattern, String[] possibleValues) {
    int count = 0;
    for (String r : possibleValues) {
      if (pattern.contains(r)) {
        count++;
      }
    }
    return count;
  }

  /**
   * Returns first position of any matched patterns
   * 
   * @param pattern
   * @param possibleValues
   * @return
   */
  private int getPatternPos(String pattern, String[] possibleValues) {
    int pos = -1;
    for (String r : possibleValues) {
      if (pattern.contains(r)) {
        pos = pattern.indexOf(r);
      }
    }
    return pos;
  }

}
