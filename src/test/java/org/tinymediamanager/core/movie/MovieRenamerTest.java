package org.tinymediamanager.core.movie;

import java.nio.file.Paths;
import java.util.regex.Pattern;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.core.TmmModuleManager;
import org.tinymediamanager.core.entities.MediaFile;
import org.tinymediamanager.core.movie.entities.Movie;
import org.tinymediamanager.core.tvshow.TvShowModuleManager;
import org.tinymediamanager.thirdparty.MediaInfoUtils;

public class MovieRenamerTest {
  private final static Logger LOGGER = LoggerFactory.getLogger(MovieRenamerTest.class);

  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
    TmmModuleManager.getInstance().startUp();
    MovieModuleManager.getInstance().startUp();
    TvShowModuleManager.getInstance().startUp();
  }

  @AfterClass
  public static void tearDownAfterClass() throws Exception {
    TvShowModuleManager.getInstance().shutDown();
    MovieModuleManager.getInstance().shutDown();
    TmmModuleManager.getInstance().shutDown();
  }

  @Test
  public void special() {
    System.out.println(MovieRenamer.replaceInvalidCharacters("jb: the bla"));
    System.out.println(MovieRenamer.replaceInvalidCharacters("jb : the bla"));
    System.out.println(MovieRenamer.replaceInvalidCharacters("2:22"));
    System.out.println(MovieRenamer.replaceInvalidCharacters("2 :22"));
  }

  @Test
  public void checkPathConsistency() {
    // get all movie paths
    // check whether path contains another
    //
    // ds/file1.mkv <- MultiMovieDir
    // ds/file2.mkv <- MultiMovieDir
    // ds/A/file3.mkv <- single, but since we have another movie deeper, this MUST be a MultiMovieDir
    // ds/A/title/file5.mkv single

  }

  @Test
  public void testRename() {
    MediaInfoUtils.loadMediaInfo();

    Movie m = new Movie();
    m.setTitle("The Dish");
    m.setYear("2000");
    MediaFile mf = new MediaFile(Paths.get("target/test-classes/samples", "thx_scarface-DWEU.vob"));
    mf.gatherMediaInformation();
    m.addToMediaFiles(mf);

    Assert.assertEquals("The Dish (2000) MPEG-480p AC3-6ch", MovieRenamer.createDestinationForFilename("$T ($Y) $V $A", m));
    Assert.assertEquals("The Dish (2000)", MovieRenamer.createDestinationForFoldername("$T ($Y)", m));
  }

  @Test
  public void testFirstAlphaNum() {
    Assert.assertEquals("A", MovieRenamer.getFirstAlphaNum("... and then came Polly"));
    Assert.assertEquals("5", MovieRenamer.getFirstAlphaNum("(500) days of summer"));
    Assert.assertEquals("3", MovieRenamer.getFirstAlphaNum("300"));
    Assert.assertEquals("B", MovieRenamer.getFirstAlphaNum("Batman"));
    Assert.assertEquals("", MovieRenamer.getFirstAlphaNum(""));
    Assert.assertEquals("", MovieRenamer.getFirstAlphaNum(null));
  }

  @Test
  public void testPattern() {
    testAll(getRegexForPattern("$T($Y)"));
    testAll(getRegexForPattern("$T.($Y)"));
    testAll(getRegexForPattern("$T ($$$$)"));
  }

  private void testAll(String regex) {
    testRegex(regex, "this is the title (2012)");
    testRegex(regex, "this.is.the.scene.title (2012)");
    testRegex(regex, "this_is_another_title (2012)");
    testRegex(regex, "this is the title(2012)");
    testRegex(regex, "blabla [r12] (2012)");
    System.out.println();
  }

  private void testRegex(String regex, String text) {
    System.out.println("Test:  " + text + " -> " + text.matches(regex));
  }

  public String getRegexForPattern(String p) {
    Pattern SPECIAL_REGEX_CHARS = Pattern.compile("[{}()\\[\\].+*?^\\\\|]");
    System.out.println("Start: " + p);

    p = SPECIAL_REGEX_CHARS.matcher(p).replaceAll("\\\\$0");
    System.out.println("RegEx: " + p);

    p = p.replaceAll("\\$T", "([\\\\s\\\\w\\\\.]+)"); // alphanum+_ & whitespaces & dots
    p = p.replaceAll("\\$Y", "(\\\\d{4})");
    p = p.replaceAll("\\$1", "(\\\\w)");
    System.out.println("Patrn: " + p);

    return p;
  }
}
