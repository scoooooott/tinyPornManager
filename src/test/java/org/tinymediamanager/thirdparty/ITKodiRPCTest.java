package org.tinymediamanager.thirdparty;

import java.util.ArrayList;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.jsonrpc.api.model.VideoModel.MovieDetail;
import org.tinymediamanager.jsonrpc.config.HostConfig;
import org.tinymediamanager.jsonrpc.io.ApiException;
import org.tinymediamanager.thirdparty.KodiRPC.SplitDataSource;

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
  public void getAllMoviesASYNC() {
    KodiRPC.getInstance().getAllMoviesASYNC();
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
    for (SplitDataSource ds : KodiRPC.getInstance().getVideoDataSources()) {
      System.out.println(ds);
    }
  }

  @Test
  public void getAllTvShows() {
    KodiRPC.getInstance().getAllTvShows();
  }

  @BeforeClass
  public static void setUp() {
    // Upnp.getInstance().createUpnpService();
    // Upnp.getInstance().sendPlayerSearchRequest();
    try {
      HostConfig config = new HostConfig("127.0.0.1");
      KodiRPC.getInstance().connect(config);
    }
    catch (ApiException e) {
      // fail(e.getMessage());
    }
  }

  @AfterClass
  public static void tearDown() throws InterruptedException {
    Thread.sleep(10000); // wait a bit - async
    KodiRPC.getInstance().disconnect();
    Thread.sleep(200); // wait a bit - async
  }
}
