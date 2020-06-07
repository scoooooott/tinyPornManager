package org.tinymediamanager.core;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;

import org.apache.commons.io.FilenameUtils;
import org.junit.BeforeClass;
import org.junit.Test;
import org.tinymediamanager.BasicTest;
import org.tinymediamanager.scraper.util.LanguageUtils;
import org.tinymediamanager.scraper.util.StrgUtils;

public class UtilsTest extends BasicTest {

  @BeforeClass
  public static void setup() {
    // create a fresh default config
    deleteSettingsFolder();
    Settings.getInstance(getSettingsFolder());
  }

  // @Test
  // public void parseIp() throws UnknownHostException, SecurityException {
  // System.out.println(getIP("localhost"));
  // System.out.println(getIP("localhost:22"));
  // System.out.println(getIP("::1"));
  // System.out.println(getIP("2001:0000:3238:DFE1:63:0000:0000:FEFB"));
  // System.out.println(getIP("[2001:0000:3238:DFE1:63:0000:0000:FEFB]:22"));
  // System.out.println(getIP(""));
  // System.out.println(getIP(""));
  // System.out.println(getIP(""));
  // System.out.println(getIP(""));
  // System.out.println(getIP(""));
  // System.out.println(getIP(""));
  // System.out.println(getIP(""));
  //
  // }

  private String getIP(String ip) {
    // WORKAROUND: add any scheme to make the resulting URI valid.
    try {
      URI uri = new URI("my://" + ip);
      return uri.getHost();
    }
    catch (URISyntaxException e) {
      return "";
    }
  }

  // @Test
  // public void div() {
  // // Utils.trackEvent("test");
  //
  // Path sub = Paths.get("cache\\image");
  // Path fil = Paths.get("C:\\Users\\User\\workspaceGIT\\tinyMediaManager\\cache\\image\\yyy");
  // System.out.println(sub.resolve(fil));
  //
  // Path p = Paths.get("C:\\Users\\User").resolve("").resolve("file.ext");
  // System.out.println(p);
  // }

  @Test
  public void zip() {
    Path zip = Paths.get("target", "test.zip");
    Path add = Paths.get("pom.xml");
    Utils.createZip(zip, add, "/pom.xml");
    Utils.createZip(zip, add, "/sub/pom.xml");
  }

  @Test
  public void compareVersions() {
    assertEqual(true, StrgUtils.compareVersion("GIT", "GIT") < 0); // GIT always "lower"
    assertEqual(true, StrgUtils.compareVersion("GIT", "2.7.2") < 0); // GIT always "lower"
    assertEqual(true, StrgUtils.compareVersion("2.7.2-SNAPSHOT", "2.7.2") < 0);
    assertEqual(true, StrgUtils.compareVersion("2.7.2", "2.7.2") == 0);
    assertEqual(true, StrgUtils.compareVersion("2.7.3-SNMAPSHOT", "2.7.2") > 0);
  }

  // @Test
  // public void map() {
  // HashMap<String, Object> ids = new HashMap<>(0);
  // ids.put("STR1", "str1");
  // ids.put("STR2", "1000");
  // ids.put("INT1", Integer.parseInt("1000"));
  // ids.put("DUPE", "2000");
  // ids.put("DUPE", 1000);
  // for (String s : ids.keySet()) {
  // Object o = ids.get(s);
  // System.out.println(s + " " + o + " " + (o instanceof String ? "String" : "") + (o instanceof Integer ? "Integer" : ""));
  // }
  // }

  @Test
  public void getSortableName() {
    // http://www.imdb.com/find?q=die&s=tt&ref_=fn_al_tt_mr
    assertEqual("Dark Knight, The", Utils.getSortableName("The Dark Knight"));
    assertEqual("Dark Knight, The", Utils.getSortableName("tHE Dark Knight"));
    assertEqual("hard days night, A", Utils.getSortableName("a hard days night"));
    assertEqual("Die Hard", Utils.getSortableName("Die Hard")); // wohoo
    assertEqual("Die Hard 2", Utils.getSortableName("Die Hard 2")); // wohoo
    assertEqual("Die Hard Year One", Utils.getSortableName("Die Hard Year One")); // wohoo
    assertEqual("Die Hard: with a Vengeance", Utils.getSortableName("Die Hard: with a Vengeance")); // wohoo
    assertEqual("Good Day to Die Hard, A", Utils.getSortableName("A Good Day to Die Hard")); // wohoo
    assertEqual("Hardyboys, Die", Utils.getSortableName("Die Hardyboys"));
    assertEqual("Team, A", Utils.getSortableName("A Team"));
    assertEqual("A-Team", Utils.getSortableName("A-Team"));
    assertEqual("A!Team", Utils.getSortableName("A!Team"));
    assertEqual("Âge de Glace, L'", Utils.getSortableName("L' Âge de Glace"));
    assertEqual("Âge de Glace, L'", Utils.getSortableName("L'Âge de Glace"));
    assertEqual("'Âge de Glace, L'", Utils.getSortableName("L''Âge de Glace"));
    assertEqual("Âge de Glace, L´", Utils.getSortableName("L´ Âge de Glace"));
    assertEqual("Âge de Glace, L`", Utils.getSortableName("L` Âge de Glace"));
    assertEqual("Âge de Glace, L´", Utils.getSortableName("L´Âge de Glace"));
    assertEqual("Âge de Glace, L`", Utils.getSortableName("L`Âge de Glace"));
    assertEqual("Die Another Day", Utils.getSortableName("Die Another Day"));
    assertEqual("Die Another Day 2", Utils.getSortableName("Die Another Day 2"));
    assertEqual("Die, Monster, Die!", Utils.getSortableName("Die, Monster, Die!"));
    assertEqual("Lonely Place to Die, A", Utils.getSortableName("A Lonely Place to Die"));
    assertEqual("Die! Die! My Darling!", Utils.getSortableName("Die! Die! My Darling!"));
    assertEqual("", Utils.getSortableName(""));
    assertEqual("", Utils.getSortableName(""));

  }

  @Test
  public void removeSortableName() {
    assertEqual("The Dark Knight", Utils.removeSortableName("Dark Knight, The"));
    assertEqual("The Dark Knight", Utils.removeSortableName("Dark Knight, tHE"));
    assertEqual("A hard days night", Utils.removeSortableName("hard days night, a"));
    assertEqual("Die Hard", Utils.removeSortableName("Die Hard"));
    assertEqual("L'Âge de Glace", Utils.removeSortableName("Âge de Glace, L'"));
    assertEqual("L`Âge de Glace", Utils.removeSortableName("Âge de Glace, L`"));
    assertEqual("L´Âge de Glace", Utils.removeSortableName("Âge de Glace, L´"));
    assertEqual("Die Another Day", Utils.removeSortableName("Die Another Day"));
    assertEqual("Live and Let Die", Utils.removeSortableName("Live and Let Die"));
    assertEqual("Truth or Die", Utils.removeSortableName("Truth or Die"));
    assertEqual("Die, Monster, Die!", Utils.removeSortableName("Die, Monster, Die!"));
    assertEqual("Die! Die! My Darling!", Utils.removeSortableName("Die! Die! My Darling!"));
    assertEqual("", Utils.removeSortableName(""));
    assertEqual("", Utils.removeSortableName(""));
  }

  @Test
  public void detectStackingMarkers() {
    assertEqual("Easy A", FilenameUtils.getBaseName(Utils.cleanStackingMarkers("Easy A.avi"))); // not a stacking format!
    assertEqual("", Utils.getStackingMarker("2 Guns (2013) x264-720p DTS-6ch.mkv"));

    assertEqual(Utils.getStackingMarker("Movie Name (2013)-cd0.mkv"), "");
    assertEqual(Utils.getStackingMarker("Movie Name (2013)-cd1.mkv"), "cd1");
    assertEqual(Utils.getStackingMarker("Movie Name (2013)-cd12.mkv"), "cd12");
    assertEqual(Utils.getStackingMarker("Movie Name (2013)-PaRt10.mkv"), "PaRt10");
    assertEqual(Utils.getStackingMarker("Movie Name (2013)-PaRt1.mkv"), "PaRt1");
    assertEqual(Utils.getStackingMarker("Movie Name (2013)-PaRt12.mkv"), "PaRt12");
    assertEqual(Utils.getStackingMarker("Movie Name (2013) DvD1.mkv"), "DvD1");
    assertEqual(Utils.getStackingMarker("Movie Name (2013).disk3.mkv"), "disk3");
    assertEqual(Utils.getStackingMarker("Movie Name (2013).disk30.mkv"), "disk30");
    assertEqual(Utils.getStackingMarker("Movie Name (2013).disk31.mkv"), "disk31");
    assertEqual(Utils.getStackingMarker("Movie Name (2013)-cd 1.mkv"), "cd 1");
    assertEqual(Utils.getStackingMarker("Movie Name (2013)-cd 10.mkv"), "cd 10");
    assertEqual(Utils.getStackingMarker("Movie Name (2013)-cd 12.mkv"), "cd 12");
    assertEqual(Utils.getStackingMarker("Movie Name (2013)-PaRt 1.mkv"), "PaRt 1");
    assertEqual(Utils.getStackingMarker("Movie Name (2013)-PaRt 12.mkv"), "PaRt 12");
    assertEqual(Utils.getStackingMarker("Movie Name (2013) DvD 1.mkv"), "DvD 1");
    assertEqual(Utils.getStackingMarker("Movie Name (2013) DvD 12.mkv"), "DvD 12");
    assertEqual(Utils.getStackingMarker("Movie Name (2013).disk 3.mkv"), "disk 3");
    assertEqual(Utils.getStackingMarker("Movie Name (2013).disk 31.mkv"), "disk 31");
    assertEqual(Utils.getStackingMarker("Movie Name (2013)-cd1.mkv"), "cd1");
    assertEqual(Utils.getStackingMarker("Movie Name (2013)-cd12.mkv"), "cd12");
    assertEqual(Utils.getStackingMarker("Movie Name (2013)-1of2.mkv"), "1of2");
    assertEqual(Utils.getStackingMarker("Movie Name (2013)-10of20.mkv"), "10of20");
    assertEqual(Utils.getStackingMarker("Movie Name (2013)-12of21.mkv"), "12of21");
    assertEqual(Utils.getStackingMarker("Movie Name (2013)-1 of 2.mkv"), "1 of 2");
    assertEqual(Utils.getStackingMarker("Movie Name (2013)-12 of 21.mkv"), "12 of 21");
    assertEqual(Utils.getStackingMarker("Movie Name (2013)-(1 of 2).mkv"), "1 of 2");
    assertEqual(Utils.getStackingMarker("Movie Name (2013)-(10 of 20).mkv"), "10 of 20");
    assertEqual(Utils.getStackingMarker("Movie Name (2013)-(12 of 21).mkv"), "12 of 21");
    assertEqual(Utils.getStackingMarker("Movie Name (2013)-(1-2).mkv"), ""); // nah
    assertEqual(Utils.getStackingMarker("Movie Name (2013)-(12-21).mkv"), ""); // nah
    assertEqual(Utils.getStackingMarker("Movie Name (2013)-1.mkv"), ""); // do not detect - could be prequel in MMD
    assertEqual(Utils.getStackingMarker("Movie Name (2013)-12.mkv"), ""); // do not detect - could be prequel in MMD
    assertEqual(Utils.getStackingMarker("Movie Name (2013)-2.mkv"), ""); // do not detect - could be sequel in MMD
    assertEqual(Utils.getStackingMarker("Movie Name (2013)-a.mkv"), "a");
    assertEqual(Utils.getStackingMarker("Movie Name (2013)-b.mkv"), "b");
    assertEqual(Utils.getStackingMarker("Movie Name Part 4 (2013).mkv"), ""); // no inbetween matching
    assertEqual(Utils.getStackingMarker("Movie Name CD 1 (2013).mkv"), ""); // no inbetween matching
    assertEqual(Utils.getStackingMarker("Movie Name 3of4 (2013).mkv"), ""); // no inbetween matching
    assertEqual(Utils.getStackingMarker("Movie Name Part 4 (2013)-Part 1.mkv"), "Part 1"); // no inbetween matching, but ending matching

    assertEqual(Utils.getStackingNumber("Movie Name (2013)-cd1.mkv"), 1);
    assertEqual(Utils.getStackingNumber("Movie Name (2013)-cd12.mkv"), 12);
    assertEqual(Utils.getStackingNumber("Movie Name (2013)-PaRt1.mkv"), 1);
    assertEqual(Utils.getStackingNumber("Movie Name (2013)-PaRt12.mkv"), 12);
    assertEqual(Utils.getStackingNumber("Movie Name (2013) DvD1.mkv"), 1);
    assertEqual(Utils.getStackingNumber("Movie Name (2013) DvD12.mkv"), 12);
    assertEqual(Utils.getStackingNumber("Movie Name (2013).disk3.mkv"), 3);
    assertEqual(Utils.getStackingNumber("Movie Name (2013).disk31.mkv"), 31);
    assertEqual(Utils.getStackingNumber("Movie Name (2013)-cd 1.mkv"), 1);
    assertEqual(Utils.getStackingNumber("Movie Name (2013)-PaRt 1.mkv"), 1);
    assertEqual(Utils.getStackingNumber("Movie Name (2013) DvD 1.mkv"), 1);
    assertEqual(Utils.getStackingNumber("Movie Name (2013).disk 3.mkv"), 3);
    assertEqual(Utils.getStackingNumber("Movie Name (2013)-cd1.mkv"), 1);
    assertEqual(Utils.getStackingNumber("Movie Name (2013)-1of2.mkv"), 1);
    assertEqual(Utils.getStackingNumber("Movie Name (2013)-1 of 2.mkv"), 1);
    assertEqual(Utils.getStackingNumber("Movie Name (2013)-(1 of 2).mkv"), 1);
    assertEqual(Utils.getStackingNumber("Movie Name (2013)-(1-2).mkv"), 0); // nah
    assertEqual(Utils.getStackingNumber("Movie Name (2013)-1.mkv"), 0); // nah
    assertEqual(Utils.getStackingNumber("Movie Name (2013)-2.mkv"), 0); // nah
    assertEqual(Utils.getStackingNumber("Movie Name (2013)-a.mkv"), 1);
    assertEqual(Utils.getStackingNumber("Movie Name (2013)-b.mkv"), 2);

    assertEqual(Utils.cleanStackingMarkers("Movie Name (2013)-cd1.mkv"), "Movie Name (2013).mkv");
    assertEqual(Utils.cleanStackingMarkers("Movie Name (2013)-cd12.mkv"), "Movie Name (2013).mkv");
    assertEqual(Utils.cleanStackingMarkers("Movie Name (2013)-PaRt1.mkv"), "Movie Name (2013).mkv");
    assertEqual(Utils.cleanStackingMarkers("Movie Name (2013)-PaRt12.mkv"), "Movie Name (2013).mkv");
    assertEqual(Utils.cleanStackingMarkers("Movie Name (2013) DvD1.mkv"), "Movie Name (2013).mkv");
    assertEqual(Utils.cleanStackingMarkers("Movie Name (2013) DvD12.mkv"), "Movie Name (2013).mkv");
    assertEqual(Utils.cleanStackingMarkers("Movie Name (2013).disk3.mkv"), "Movie Name (2013).mkv");
    assertEqual(Utils.cleanStackingMarkers("Movie Name (2013).disk31.mkv"), "Movie Name (2013).mkv");
    assertEqual(Utils.cleanStackingMarkers("Movie Name (2013)-cd 1.mkv"), "Movie Name (2013).mkv");
    assertEqual(Utils.cleanStackingMarkers("Movie Name (2013)-PaRt 1.mkv"), "Movie Name (2013).mkv");
    assertEqual(Utils.cleanStackingMarkers("Movie Name (2013)-part a.mkv"), "Movie Name (2013).mkv");
    assertEqual(Utils.cleanStackingMarkers("Movie Name (2013) DvD 1.mkv"), "Movie Name (2013).mkv");
    assertEqual(Utils.cleanStackingMarkers("Movie Name (2013).disk 3.mkv"), "Movie Name (2013).mkv");
    assertEqual(Utils.cleanStackingMarkers("Movie Name (2013)-cd1.mkv"), "Movie Name (2013).mkv");
    assertEqual(Utils.cleanStackingMarkers("Movie Name (2013)-1of2.mkv"), "Movie Name (2013).mkv");
    assertEqual(Utils.cleanStackingMarkers("Movie Name (2013)-1 of 2.mkv"), "Movie Name (2013).mkv");
    assertEqual(Utils.cleanStackingMarkers("Movie Name (2013)-(1 of 2).mkv"), "Movie Name (2013).mkv");
    assertEqual(Utils.cleanStackingMarkers("Movie Name (2013)-(1-2).mkv"), "Movie Name (2013)-(1-2).mkv"); // nah
    assertEqual(Utils.cleanStackingMarkers("Movie Name (2013)-1.mkv"), "Movie Name (2013)-1.mkv"); // nah
    assertEqual(Utils.cleanStackingMarkers("Movie Name (2013)-2.mkv"), "Movie Name (2013)-2.mkv"); // nah
    assertEqual(Utils.cleanStackingMarkers("Movie Name (2013)-a.mkv"), "Movie Name (2013).mkv");
    assertEqual(Utils.cleanStackingMarkers("Movie Name (2013)-b.mkv"), "Movie Name (2013).mkv");

    // FOLDER - stacking MUST be the last part of name !!
    assertEqual("dvd1", Utils.getFolderStackingMarker("Movie Name (2013)-dvd1"));
    assertEqual("dvd12", Utils.getFolderStackingMarker("Movie Name (2013)-dvd12"));
    assertEqual("CD1", Utils.getFolderStackingMarker("moviename CD1"));
    assertEqual("CD12", Utils.getFolderStackingMarker("moviename CD12"));
    assertEqual("", Utils.getFolderStackingMarker("CD0"));
    assertEqual("CD1", Utils.getFolderStackingMarker("CD1"));
    assertEqual("", Utils.getFolderStackingMarker("CD1 whatever"));
    assertEqual("", Utils.getFolderStackingMarker("CD12 whatever"));
    assertEqual("PT1", Utils.getFolderStackingMarker("PT1"));
    assertEqual("PT12", Utils.getFolderStackingMarker("PT12"));
    assertEqual("PT1", Utils.getFolderStackingMarker("asdf PT1"));
    assertEqual("", Utils.getFolderStackingMarker("asdf PT1asdf"));
    assertEqual("", Utils.getFolderStackingMarker("PT109"));

    assertEqual("CD0", Utils.cleanFolderStackingMarkers("CD0"));
    assertEqual("", Utils.cleanFolderStackingMarkers("CD1"));
    assertEqual("moviename", Utils.cleanFolderStackingMarkers("moviename CD1"));
    assertEqual("moviename CD0", Utils.cleanFolderStackingMarkers("moviename CD0"));
    assertEqual("moviename CD1 whatever", Utils.cleanFolderStackingMarkers("moviename CD1 whatever")); // there must be nothing after pattern
    assertEqual("", Utils.cleanFolderStackingMarkers("PT1"));
    assertEqual("PT109", Utils.cleanFolderStackingMarkers("PT109"));
  }

  @Test
  public void backup() {
    Utils.createBackupFile(Paths.get("pom.xml"));
    Utils.deleteOldBackupFile(Paths.get("pom.xml"), 2);
  }

  // @SuppressWarnings("rawtypes")
  // @Test
  // public void env() {
  // Map<String, String> env = System.getenv();
  // for (String envName : env.keySet()) {
  // System.out.format("%s=%s%n", envName, env.get(envName));
  // }
  //
  // Properties props = System.getProperties();
  // Enumeration e = props.propertyNames();
  // while (e.hasMoreElements()) {
  // String key = (String) e.nextElement();
  // System.out.println(key + " -- " + props.getProperty(key));
  // }
  // }

  @Test
  public void locale() {
    // for (String s : LanguageUtils.KEY_TO_LOCALE_MAP.keySet()) {
    // System.out.println(s + " - " + LanguageUtils.KEY_TO_LOCALE_MAP.get(s));
    // }

    assertEqual(LanguageUtils.KEY_TO_LOCALE_MAP.get("tur").getISO3Language(), new Locale("tr").getISO3Language());
  }

  @Test
  public void localeCountry() {
    // for (String s : LanguageUtils.KEY_TO_COUNTRY_LOCALE_MAP.keySet()) {
    // System.out.println(s + " - " + LanguageUtils.KEY_TO_COUNTRY_LOCALE_MAP.get(s));
    // }

    // Java 8: Vereinigte Staaten von Amerika
    // Java 9: Vereinigte Staaten
    assertThat(LanguageUtils.getLocalizedCountryForLanguage("de", "United States of America", "US")).startsWith("Vereinigte Staaten");
    assertThat(LanguageUtils.getLocalizedCountryForLanguage(Locale.GERMANY, "US")).startsWith("Vereinigte Staaten");
    assertThat(LanguageUtils.getLocalizedCountryForLanguage("de", "USA", "en_US", "US")).startsWith("Vereinigte Staaten");

    assertEqual("United States", LanguageUtils.getLocalizedCountryForLanguage("en", "USA", "en_US", "US"));
    assertEqual("United States", LanguageUtils.getLocalizedCountryForLanguage("en", "Vereinigte Staaten von Amerika", "Vereinigte Staaten"));

    // Java 8: Etats-Unis
    // Java 9: États-Unis
    assertThat(LanguageUtils.getLocalizedCountryForLanguage("fr", "United States of America", "US")).matches("(E|É)tats\\-Unis");
    assertThat(LanguageUtils.getLocalizedCountryForLanguage(Locale.FRENCH, "United States of America", "US")).matches("(E|É)tats\\-Unis");
    assertThat(LanguageUtils.getLocalizedCountryForLanguage(Locale.FRANCE, "United States of America", "US")).matches("(E|É)tats\\-Unis");
    assertEqual("West Germany", LanguageUtils.getLocalizedCountryForLanguage("de", "West Germany", "XWG"));
  }

  @Test
  public void testReplacement() {
    assertEqual("Test done", Utils.replacePlaceholders("Test {}", new String[] { "done" }));
    assertEqual("Test ", Utils.replacePlaceholders("Test {}", new String[] {}));
    assertEqual("Test one two three", Utils.replacePlaceholders("Test {} {} {}", new String[] { "one", "two", "three" }));
    assertEqual("Test with empty spaces", Utils.replacePlaceholders("Test {} with {}{}empty spaces", new String[] {}));
  }
}
