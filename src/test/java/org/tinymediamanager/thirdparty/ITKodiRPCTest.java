package org.tinymediamanager.thirdparty;

import java.util.ArrayList;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.core.TmmModuleManager;
import org.tinymediamanager.core.movie.MovieModuleManager;
import org.tinymediamanager.jsonrpc.api.model.VideoModel.MovieDetail;
import org.tinymediamanager.jsonrpc.config.HostConfig;
import org.tinymediamanager.jsonrpc.io.ApiException;

public class ITKodiRPCTest {
  private static final Logger LOGGER = LoggerFactory.getLogger(ITKodiRPCTest.class);
  // *************************************************************************************
  // you need to enable Kodi -> remote control from OTHER machines (to open TCP port 9090)
  // and you need to enable webserver -> some calls are POSTed (not async)
  // *************************************************************************************

  @Test
  public void events() {
    KodiRPC.getInstance();
    while (true) {
      // do nothing, just wait for events...
    }
  }

  @Test
  public void getVersion() {
    System.out.println(KodiRPC.getInstance().getVersion());
  }

  @Test
  public void getMappings() {
    KodiRPC.getInstance().getAndSetMovieMappings();
  }

  @Test
  public void testUris() {
    testUri(".a");
    testUri(
        "zip:///C%3a%5cUsers%5cmamk%5cVideos%5cFilme%5cAvatar%20-%20Aufbruch%20nach%20Pandora%20(2009).zip/Avatar - Aufbruch nach Pandora (2009)/Avatar - Aufbruch nach Pandora (2009) (7.0) cd1.avi");
    testUri("upnp://886fc236-b611-0730-0000-000017107649/1/");
    testUri("\\\\NAS\\video");
    testUri("\\\\12.2.3.4\\video");
    testUri("smb://NAS/video/Bird.sampla.x264.mkv.mp4");
    testUri("file://hostname/asdf.org"); // unknown remote host - blocking 5 sec on getting IP
    testUri("file:///video/local.txt");
    testUri("file:\\\\video\\local.txt");
    testUri("file:\\\\\\video\\local.txt");
    testUri(".\\video\\relative.txt");
    testUri("file:///video/local.txt");
    testUri("D:\\_neu\\TMM\\TEST_TV_DVD\\");

    // datasource
    testUri("D:\\_neu\\TMM\\", "D:\\_neu\\TMM\\");
    testUri("smb://NAS/video/", "smb://NAS/video/");
    testUri("smb://", "smb://");

  }

  private void testUri(String s) {
    testUri("", s);
  }

  private void testUri(String ds, String s) {
    System.out.println(new SplitUri(ds, s));
  }

  @Test
  public void testUriMatching() {
    // enter a valid hostname, else it will take long ;)

    // same
    String s1 = "smb://localhost/public/TMM/testmovies/101 Dalmatiner/101 Dalmatiner #2.avi";
    String s2 = "\\\\127.0.0.1\\public\\TMM\\testmovies\\101 Dalmatiner\\101 Dalmatiner #2.avi";
    Assert.assertEquals(new SplitUri("smb://localhost/public/TMM/testmovies", s1), new SplitUri("\\\\127.0.0.1\\public\\TMM\\testmovies", s2));

    // no file
    s1 = "smb://192.168.1.10/Series/The Magicians (2015)/";
    s2 = "\\\\127.0.0.1\\Series\\The Magicians (2015)";
    Assert.assertEquals(new SplitUri("smb://192.168.1.10/Series", s1), new SplitUri("\\\\127.0.0.1\\Series", s2));

    // datasource only
    s1 = "smb://127.0.0.1/share";
    s2 = "\\\\127.0.0.1\\share";
    Assert.assertEquals(new SplitUri(s1, s1), new SplitUri(s2, s2));
    Assert.assertEquals(new SplitUri(s1, ""), new SplitUri(s2, ""));

    // other datasource
    s1 = "smb://localhost/public/TMM/testmovies/101 Dalmatiner/101 Dalmatiner #2.avi";
    s2 = "\\\\127.0.0.1\\public\\TMM\\newmovies\\101 Dalmatiner\\101 Dalmatiner #2.avi";
    Assert.assertEquals(new SplitUri("smb://localhost/public/TMM/testmovies/", s1), new SplitUri("\\\\127.0.0.1\\public\\TMM\\newmovies", s2));

    /////////////////////////////// NEGATIVE TESTS
    // wrong parent
    s1 = "smb://localhost/public/TMM/testmovies/101 Dalmatiner/101 Dalmatiner #2.avi";
    s2 = "\\\\127.0.0.1\\public\\TMM\\testmovies\\no Dalmatiner\\101 Dalmatiner #2.avi";
    Assert.assertNotEquals(new SplitUri("smb://localhost/public/TMM/testmovies/", s1), new SplitUri("\\\\127.0.0.1\\public\\TMM\\testmovies", s2));

    // no datasource
    s1 = "smb://192.168.1.10/Series/The Magicians (2015)/";
    s2 = "\\\\127.0.0.1\\Series\\The Magicians (2015)";
    Assert.assertNotEquals(new SplitUri("", s1), new SplitUri("", s2));
  }

  @Test
  public void getAllMoviesSYNC() {
    ArrayList<MovieDetail> movies = KodiRPC.getInstance().getAllMoviesSYNC();
    if (movies == null) {
      LOGGER.error("no movies found");
    }
    else {
      LOGGER.info("found " + movies.size() + " movies");
      for (MovieDetail res : movies) {
        for (String c : res.country) {
          System.out.println(c);
        }
        LOGGER.debug(res.toString());
      }
    }
  }

  // TODO: delimit on " , "

  // C:\Users\mamk\Videos\Filme\Abyss, The\Abyss, The.avi
  // C:\Users\mamk\Videos\Filme\testDVD\VIDEO_TS\VIDEO_TS.IFO

  // =stack://C:\Users\mamk\Videos\Filme\Mulholland Drive\Mulholland Drive cd1.avi , C:\Users\mamk\Videos\Filme\Mulholland Drive\Mulholland Drive
  // cd2.avi

  // =stack://
  // zip://C%3a%5cUsers%5cmamk%5cVideos%5cFilme%5cAvatar%20-%20Aufbruch%20nach%20Pandora%20(2009).zip/Avatar - Aufbruch nach Pandora (2009)/Avatar -
  // Aufbruch nach Pandora (2009) (7.0) cd1.avi ,
  // zip://C%3a%5cUsers%5cmamk%5cVideos%5cFilme%5cAvatar%20-%20Aufbruch%20nach%20Pandora%20(2009).zip/Avatar - Aufbruch nach Pandora (2009)/Avatar -
  // Aufbruch nach Pandora (2009) (7.0) cd2.avi

  @Test
  public void getDataSources() {
    for (SplitUri ds : KodiRPC.getInstance().getVideoDataSources()) {
      System.out.println(ds);
    }

    KodiRPC.getInstance().getDataSources();
  }

  @Test
  public void getAllTvShows() {
    KodiRPC.getInstance().getAllTvShows();
  }

  @BeforeClass
  public static void setUp() {
    TmmModuleManager.getInstance().startUp();
    MovieModuleManager.getInstance().startUp();

    // Upnp.getInstance().createUpnpService();
    // Upnp.getInstance().sendPlayerSearchRequest();
    try {
      HostConfig config = new HostConfig("127.0.0.1", 8080, "kodi", "kodi");
      KodiRPC.getInstance().connect(config);
    }
    catch (ApiException e) {
      System.err.println(e.getMessage());
      Assert.fail(e.getMessage());
    }
  }

  @AfterClass
  public static void tearDown() throws Exception {
    Thread.sleep(10000); // wait a bit - async
    KodiRPC.getInstance().disconnect();
    MovieModuleManager.getInstance().shutDown();
    TmmModuleManager.getInstance().shutDown();
    Thread.sleep(200); // wait a bit - async
  }
}
