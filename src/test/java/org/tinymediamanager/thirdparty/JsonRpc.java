//package org.tinymediamanager.thirdparty;
//
//import java.util.List;
//
//import org.junit.AfterClass;
//import org.junit.BeforeClass;
//import org.junit.Test;
//import org.tinymediamanager.jsonrpc.api.AbstractCall;
//import org.tinymediamanager.jsonrpc.api.call.Files;
//import org.tinymediamanager.jsonrpc.api.call.VideoLibrary;
//import org.tinymediamanager.jsonrpc.api.model.FilesModel;
//import org.tinymediamanager.jsonrpc.api.model.ListModel;
//import org.tinymediamanager.jsonrpc.api.model.VideoModel;
//import org.tinymediamanager.jsonrpc.api.model.VideoModel.MovieDetail;
//import org.tinymediamanager.jsonrpc.api.model.VideoModel.TVShowDetail;
//import org.tinymediamanager.jsonrpc.config.HostConfig;
//import org.tinymediamanager.jsonrpc.io.ApiCallback;
//import org.tinymediamanager.jsonrpc.io.ConnectionListener;
//import org.tinymediamanager.jsonrpc.io.JavaConnectionManager;
//import org.tinymediamanager.jsonrpc.notification.AbstractEvent;
//
//public class JsonRpc {
//
//  // *************************************************************************************
//  // you need to enable Kodi -> remote control from OTHER machines (to open TCP port 9090)
//  // *************************************************************************************
//
//  private static JavaConnectionManager cm = new JavaConnectionManager();
//
//  // @Test
//  public void events() {
//    while (true) {
//      // do nothing, just wait for events...
//    }
//  }
//
//  @Test
//  public void getDataSources() {
//    final Files.GetSources f = new Files.GetSources(FilesModel.Media.VIDEO); // movies + tv !!!
//    cm.call(f, new ApiCallback<ListModel.SourceItem>() {
//
//      @Override
//      public void onResponse(AbstractCall<ListModel.SourceItem> call) {
//        System.out.println(" found " + call.getResults().size() + " sources");
//        for (ListModel.SourceItem res : call.getResults()) {
//          System.out.println("  " + res);
//        }
//      }
//
//      @Override
//      public void onError(int code, String message, String hint) {
//        System.out.println("Error " + code + ": " + message);
//      }
//    });
//  }
//
//  @Test
//  public void getAllMovies() {
//    final VideoLibrary.GetMovies vl = new VideoLibrary.GetMovies();
//    cm.call(vl, new ApiCallback<VideoModel.MovieDetail>() {
//
//      @Override
//      public void onResponse(AbstractCall<MovieDetail> call) {
//        System.out.println(" found " + call.getResults().size() + " movies");
//        for (MovieDetail res : call.getResults()) {
//          System.out.println("  " + res);
//        }
//      }
//
//      @Override
//      public void onError(int code, String message, String hint) {
//        System.out.println("Error " + code + ": " + message);
//      }
//    });
//  }
//
//  @Test
//  public void getAllTvShows() {
//    final VideoLibrary.GetTVShows vl = new VideoLibrary.GetTVShows();
//    cm.call(vl, new ApiCallback<VideoModel.TVShowDetail>() {
//
//      @Override
//      public void onResponse(AbstractCall<TVShowDetail> call) {
//        System.out.println(" found " + call.getResults().size() + " shows");
//        for (TVShowDetail res : call.getResults()) {
//          System.out.println("  " + res);
//        }
//      }
//
//      @Override
//      public void onError(int code, String message, String hint) {
//        System.out.println("Error " + code + ": " + message);
//      }
//    });
//  }
//
//  /**
//   * activated UPNP will find it automatically ;)
//   *
//   * @return IP of first found Kodi instance
//   */
//  private static String detectKodi() {
//    try {
//      Upnp client = new Upnp();
//      List<UpnpDevice> upnpDevices = client.getUpnpDevices();
//      for (UpnpDevice d : upnpDevices) {
//        if (d.getModelName().equals("Kodi")) {
//          String loc = d.getLocation();
//          System.out.println(loc.substring(loc.indexOf("//") + 2, loc.indexOf(":", 10))); // IPv4, check IPv6
//        }
//      }
//    }
//    catch (Exception e) {
//      System.err.println("Error in UPNP" + e);
//    }
//    return "localhost";
//  }
//
//  @BeforeClass
//  public static void setUp() {
//    HostConfig config = new HostConfig("localhost", 80, 9090);
//    cm.registerConnectionListener(new ConnectionListener() {
//
//      @Override
//      public void notificationReceived(AbstractEvent event) {
//        // System.out.println("Event received: " + event.getClass().getCanonicalName());
//        System.out.println("Event received: " + event);
//      }
//
//      @Override
//      public void disconnected() {
//        System.out.println("Event: Disconnected");
//
//      }
//
//      @Override
//      public void connected() {
//        System.out.println("Event: Connected");
//
//      }
//    });
//    System.out.println("Connecting...");
//    cm.connect(config);
//  }
//
//  @AfterClass
//  public static void tearDown() throws InterruptedException {
//    Thread.sleep(1000); // wait a bit - async
//    System.out.println("Exiting...");
//    cm.disconnect();
//  }
//}
