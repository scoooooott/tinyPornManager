/*
 * Copyright 2012 - 2017 Manuel Laggner
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
package org.tinymediamanager.core.movie;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Test;
import org.tinymediamanager.BasicTest;
import org.tinymediamanager.core.Utils;
import org.tinymediamanager.core.movie.entities.Movie;
import org.tinymediamanager.scraper.util.ParserUtils;
import org.tinymediamanager.scraper.util.StrgUtils;

/**
 * @author Manuel Laggner
 * 
 */
public class MovieTest extends BasicTest {
  private Movie m = new Movie();

  @Test
  public void testNamingDetection() {
    String longest = StrgUtils.getLongestString(new String[] { "exq-theequalizer-720p.mkv", "The.Equalizer.German.720p.BluRay.x264-EXQUiSiTE" });
    String[] video = ParserUtils.detectCleanMovienameAndYear(longest);
    System.out.println(video[0]);
  }

  @Test
  public void detectCleanness() {
    System.out.println(ParserUtils.getCleanerString("Kill.the.Boss.2.GERMAN.DL.720p.BluRay.x264-WodkaE", "WodkaE-kill.the.boss.2.720p",
        "Wodkae Kill The Boss 2", "Kill The Boss 2"));
    System.out.println(ParserUtils.getCleanerString("The.Equalizer.German.720p.BluRay.x264-EXQUiSiTE", "exq-theequalizer-720p", "The Equalizer",
        "Exq The Equalizer", "Exq TheEqualizer"));
    System.out.println(ParserUtils.getCleanerString("lebo_b.avi", "lebow.avi", "The Big Lebowski"));
  }

  @Test
  public void testCleanStackingMarkers() {
    System.out.println(Utils.cleanStackingMarkers("Movie Name (2013)-cd1.mkv"));
  }

  @Test
  public void renamerPattern() {
    m.setTitle(" Abraham Lincoln - Vapire Hunter");
    m.setYear("");
    m.setOriginalTitle("OrigTit");

    String str = "$T {($Y)}      {$F} $M   $O ";
    System.out.println(str);

    // replace optional group
    Pattern regex = Pattern.compile("\\{(.*?)\\}");
    Matcher mat = regex.matcher(str);
    while (mat.find()) {
      str = str.replace(mat.group(0), replaceVariable(mat.group(1)));
    }

    // replace normal vars
    str = MovieRenamer.createDestinationForFilename(str, m);

    System.out.println(str);
  }

  private String replaceVariable(String s) {
    Pattern regex = Pattern.compile("\\$.{1}"); // $x
    Matcher mat = regex.matcher(s);
    if (mat.find()) {
      String rep = MovieRenamer.createDestinationForFilename(mat.group(), m);
      if (rep.isEmpty()) {
        return "";
      }
      else {
        return s.replace(mat.group(), rep);
      }
    }
    else {
      return "";
    }
  }
}
