package org.tinymediamanager;

import java.io.IOException;
import java.nio.file.Paths;

import org.junit.Assert;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.core.Utils;
import org.tinymediamanager.core.movie.MovieList;
import org.tinymediamanager.core.movie.entities.Movie;
import org.tinymediamanager.core.tvshow.TvShowList;
import org.tinymediamanager.core.tvshow.entities.TvShow;
import org.tinymediamanager.core.tvshow.entities.TvShowEpisode;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;

public class BasicTest {

  // own method to get some logging ;)
  public static void assertEqual(Object expected, Object actual) {
    try {
      Assert.assertEquals(expected, actual);
      // System.out.println(actual + " - passed");
    }
    catch (AssertionError e) {
      System.err.println(actual + " - FAILED: " + e.getMessage());
      throw e;
    }
  }

  public static void assertEqual(String message, Object expected, Object actual) {
    try {
      Assert.assertEquals(message, expected, actual);
      // System.out.println(expected + " - passed");
    }
    catch (AssertionError e) {
      System.err.println(expected + " - FAILED: " + message + "(" + e.getMessage() + ")");
      throw e;
    }
  }

  public static void setTraceLogging() {
    LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
    lc.getLogger("org.tinymediamanager").setLevel(Level.TRACE);
  }

  public static String getSettingsFolder() {
    StackTraceElement ste = Thread.currentThread().getStackTrace()[2];
    return "target/testdata/" + ste.getClassName();
  }

  public static void deleteSettingsFolder() {
    StackTraceElement ste = Thread.currentThread().getStackTrace()[2];
    try {
      Utils.deleteDirectoryRecursive(Paths.get("target", "testdata", ste.getClassName()));
    }
    catch (IOException e) {
      e.printStackTrace();
    }
  }

  public static void createFakeMovie(String title) {
    Movie m = new Movie();
    m.setTitle(title);
    MovieList.getInstance().addMovie(m);
    m.saveToDb();
    System.out.println("Created movie " + m.getDbId());
  }

  public static void createFakeShow(String title) {
    TvShow s = new TvShow();
    s.setTitle(title);
    TvShowEpisode ep = new TvShowEpisode();
    ep.setTitle(title + "-EP");
    ep.setSeason(1);
    ep.setEpisode(2);
    s.addEpisode(ep);
    TvShowList.getInstance().addTvShow(s);
    s.saveToDb();
    System.out.println("Created show " + s.getDbId());
  }

}
