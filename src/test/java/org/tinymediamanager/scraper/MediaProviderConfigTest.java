package org.tinymediamanager.scraper;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

@SuppressWarnings("deprecation")
public class MediaProviderConfigTest {

  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
    FileUtils.deleteQuietly(new File("target/scraper_config.conf"));
    FileUtils.copyFile(new File("target/test-classes/scraper_config.conf"), new File("target/scraper_config.conf"));
  }

  @Test
  public void getSettings() throws IOException {

    MediaProviderInfo mpi = new MediaProviderInfo("config", "name", "description");

    // define defaults
    mpi.settings.addBoolean("filterUnwantedCategories", false);
    mpi.settings.addBoolean("useTmdb", false);
    mpi.settings.addBoolean("scrapeCollectionInfo", true);
    mpi.settings.addBoolean("someBool", true);
    mpi.settings.addText("someInput", "none");
    mpi.settings.addSelect("language", new String[] { "aa", "bb", "cc", "dd", "ee" }, "dd");
    mpi.settings.addSelectIndex("languageInt",
        "bg|cs|da|de|el|en|es|fi|fr|he|hr|hu|it|ja|ko|nb|nl|no|pl|pt|ro|ru|sk|sl|sr|sv|th|tr|uk|zh".split("\\|"), "en");

    // check default settings
    assertEqual(false, mpi.settings.getValueAsBool("filterUnwantedCategories"));
    assertEqual(false, mpi.settings.getValueAsBool("useTmdb"));
    assertEqual(true, mpi.settings.getValueAsBool("scrapeCollectionInfo"));
    assertEqual("dd", mpi.settings.getValue("language"));
    assertEqual("5", mpi.settings.getValue("languageInt"));

    // load actual values
    mpi.settings.loadFromDir("target");

    // tests
    mpi.settings.setValue("someBool", false);
    mpi.settings.setValue("language", "bb");
    assertEqual("bb", mpi.settings.getValue("language"));
    mpi.settings.setValue("language", "ff");
    assertEqual("bb", mpi.settings.getValue("language")); // value not in rage, should stay at last known

    mpi.settings.setValue("languageInt", "de"); // set correct as string
    assertEqual("3", mpi.settings.getValue("languageInt")); // will return a int (string) index
    mpi.settings.setValue("languageInt", "unknown"); // not possible
    assertEqual("3", mpi.settings.getValue("languageInt")); // value not in rage, should stay at last known

    assertEqual(null, mpi.settings.getValueAsBool("languageInt")); // not a bool value
    assertEqual(true, mpi.settings.getValueAsBool("useTmdb"));

    System.out.println("--- current settings ---");
    for (String entry : mpi.settings.getAllEntries()) {
      System.out.println(entry + " = " + mpi.settings.getValue(entry));
    }
    System.out.println(mpi.settings);
    mpi.settings.saveToDir("target");
  }

  @Test
  public void invalid() {
    MediaProviderInfo mpi = new MediaProviderInfo("save", "name", "description");
    mpi.settings.addBoolean("bool1", false);
    mpi.settings.addText("someInput", "none");
    mpi.settings.addSelect("language", new String[] { "aa", "bb", "cc", "dd", "ee" }, "dd");
    mpi.settings.addSelectIndex("languageInt", "bg|cs|da|de|el|en|es".split("\\|"), "en");

    mpi.settings.addSelect("invalid", new String[] { "aa", "bb", "cc", "dd", "ee" }, "invalidEntry");
    mpi.settings.addSelectIndex("invalidInt", "bg|cs|da|de|el|en|es".split("\\|"), "invalidEntry");
    assertEqual("", mpi.settings.getValue("invalid"));
    assertEqual(null, mpi.settings.getValueAsBool("invalid"));
    assertEqual("", mpi.settings.getValue("invalidInt"));
  }

  @Test
  public void emptySettingsLoadSave() {
    MediaProviderInfo mpi = new MediaProviderInfo("asdfasdf", "name", "description");
    mpi.settings.load();
    mpi.settings.save();
  }

  @Test
  public void unknownConfigLoadSave() {
    MediaProviderInfo mpi = new MediaProviderInfo("asdfasdf", "name", "description");
    mpi.settings.addText("language", "de");
    mpi.settings.load();
    mpi.settings.save();
  }

  @Test
  public void getUnknownValue() {
    MediaProviderInfo mpi = new MediaProviderInfo("config", "name", "description");
    mpi.settings.loadFromDir("target");
    assertEqual("", mpi.settings.getValue("asdfasdfasdfasdf"));
    assertEqual(null, mpi.settings.getValueAsBool("sdfgsdfgsdfg"));
  }

  @Test
  public void setNotAvailableConfig() {
    MediaProviderInfo mpi = new MediaProviderInfo("asdfasdf", "name", "description");
    mpi.settings.setValue("language", "de");
  }

  // own method to get some logging ;)
  public static void assertEqual(Object expected, Object actual) {
    try {
      Assert.assertEquals(expected, actual);
      System.out.println(expected + " - passed");
    }
    catch (AssertionError e) {
      System.err.println(expected + " - FAILED: " + e.getMessage());
      throw e;
    }
  }
}
