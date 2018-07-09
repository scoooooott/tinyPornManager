package org.tinymediamanager.thirdparty;

import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.net.UnknownHostException;
import java.nio.file.InvalidPathException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.fourthline.cling.UpnpService;
import org.fourthline.cling.model.meta.Device;
import org.fourthline.cling.model.types.UDN;
import org.fourthline.cling.registry.Registry;
import org.jsoup.helper.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.core.Message;
import org.tinymediamanager.core.MessageManager;
import org.tinymediamanager.core.Settings;
import org.tinymediamanager.core.movie.MovieModuleManager;
import org.tinymediamanager.core.movie.entities.Movie;
import org.tinymediamanager.core.tvshow.TvShowModuleManager;
import org.tinymediamanager.jsonrpc.api.AbstractCall;
import org.tinymediamanager.jsonrpc.api.call.Application;
import org.tinymediamanager.jsonrpc.api.call.AudioLibrary;
import org.tinymediamanager.jsonrpc.api.call.Files;
import org.tinymediamanager.jsonrpc.api.call.JSONRPC;
import org.tinymediamanager.jsonrpc.api.call.System;
import org.tinymediamanager.jsonrpc.api.call.VideoLibrary;
import org.tinymediamanager.jsonrpc.api.model.FilesModel;
import org.tinymediamanager.jsonrpc.api.model.GlobalModel;
import org.tinymediamanager.jsonrpc.api.model.ListModel;
import org.tinymediamanager.jsonrpc.api.model.VideoModel;
import org.tinymediamanager.jsonrpc.api.model.VideoModel.MovieDetail;
import org.tinymediamanager.jsonrpc.api.model.VideoModel.MovieFields;
import org.tinymediamanager.jsonrpc.api.model.VideoModel.TVShowDetail;
import org.tinymediamanager.jsonrpc.config.HostConfig;
import org.tinymediamanager.jsonrpc.io.ApiCallback;
import org.tinymediamanager.jsonrpc.io.ApiException;
import org.tinymediamanager.jsonrpc.io.ConnectionListener;
import org.tinymediamanager.jsonrpc.io.JavaConnectionManager;
import org.tinymediamanager.jsonrpc.io.JsonApiRequest;
import org.tinymediamanager.jsonrpc.notification.AbstractEvent;
import org.tinymediamanager.thirdparty.upnp.Upnp;

public class KodiRPC {
  private static final Logger          LOGGER           = LoggerFactory.getLogger(KodiRPC.class);
  private static KodiRPC               instance;
  private static JavaConnectionManager cm               = new JavaConnectionManager();

  private String                       kodiVersion      = "";
  private ArrayList<SplitUri>          videodatasources = new ArrayList<>();
  private ArrayList<SplitUri>          audiodatasources = new ArrayList<>();

  private HashMap<String, Integer>     moviemappings    = new HashMap<>();

  private KodiRPC() {
    cm.registerConnectionListener(new ConnectionListener() {

      @Override
      public void notificationReceived(AbstractEvent event) {
        // System.out.println("Event received: " + event.getClass().getCanonicalName());
        LOGGER.info("Event received: " + event);
        MessageManager.instance.pushMessage(new Message(Message.MessageLevel.INFO, event, "Event received"));
      }

      @Override
      public void disconnected() {
        LOGGER.info("Event: Disconnected");
        MessageManager.instance.pushMessage(new Message(Message.MessageLevel.INFO, "Kodi disconnected"));
      }

      @Override
      public void connected() {
        LOGGER.info("Event: Connected");
        MessageManager.instance.pushMessage(new Message(Message.MessageLevel.INFO, "Kodi connected"));
      }
    });
  }

  public synchronized static KodiRPC getInstance() {
    if (KodiRPC.instance == null) {
      KodiRPC.instance = new KodiRPC();
    }
    return KodiRPC.instance;
  }

  public boolean isConnected() {
    return cm.isConnected();
  }

  // -----------------------------------------------------------------------------------

  /**
   * asks the API for version, and maps it correctly<br>
   * http://kodi.wiki/view/JSON-RPC_API#API_versions
   * 
   * @return Kodi XX (codename)
   */
  public String getVersion() {
    if (kodiVersion.isEmpty() && isConnected()) {
      getAndSetKodiVersion();
    }
    return (kodiVersion.isEmpty() || kodiVersion.contains("nknown")) ? "Kodi (Unknown)" : kodiVersion;
  }

  private void getAndSetKodiVersion() {
    final JSONRPC.Version call = new JSONRPC.Version();
    send(call);
    if (call.getResult() != null) {
      kodiVersion = call.getResult().getKodiVersion();
    }
  }

  // -----------------------------------------------------------------------------------

  public void LibraryVideoScan() {
    final VideoLibrary.Scan call = new VideoLibrary.Scan(null, true);
    send(call);
  }

  public void LibraryVideoScan(String dir) {
    final VideoLibrary.Scan call = new VideoLibrary.Scan(dir, true);
    send(call);
  }

  public ArrayList<SplitUri> getVideoDataSources() {
    return this.videodatasources;
  }

  private void getAndSetVideoDataSources() {
    final Files.GetSources call = new Files.GetSources(FilesModel.Media.VIDEO); // movies + tv !!!
    this.videodatasources = new ArrayList<>();
    send(call);
    if (call.getResults() != null) {
      for (ListModel.SourceItem res : call.getResults()) {
        this.videodatasources.add(new SplitUri(res.file, res.label, cm.getHostConfig().getAddress()));
      }
    }
  }

  public void getAndSetEntityMappings() {
    final VideoLibrary.GetMovies call = new VideoLibrary.GetMovies(MovieFields.FILE);
    send(call);
    if (call.getResults() != null) {

      for (MovieDetail res : call.getResults()) {
        if (res.file.startsWith("stack")) {
          String[] files = res.file.split(" , ");
          for (String s : files) {
            s = s.replaceFirst("^stack://", "");
            SplitUri sp = new SplitUri(s, res.label, cm.getHostConfig().getAddress()); // generate clean object
            for (SplitUri ds : videodatasources) {
              if (sp.file.startsWith(ds.file)) {
                moviemappings.put(sp.file, res.movieid);
              }
            }
          }
        }
        else {
          SplitUri sp = new SplitUri(res.file, res.label, cm.getHostConfig().getAddress()); // generate clean object
          for (SplitUri ds : videodatasources) {
            if (sp.file.startsWith(ds.file)) {
              moviemappings.put(sp.file, res.movieid);
            }
          }
        }
      }

      for (Map.Entry<String, Integer> entry : moviemappings.entrySet()) {
        String key = entry.getKey();
        Integer value = entry.getValue();
        LOGGER.debug(key + " - " + value);
      }
    }
  }

  public void refreshMovieFromNfo(Movie m) {

  }

  // -----------------------------------------------------------------------------------

  public void LibraryAudioScan() {
    final AudioLibrary.Scan call = new AudioLibrary.Scan(null);
    send(call);
  }

  public void LibraryAudioScan(String dir) {
    final AudioLibrary.Scan call = new AudioLibrary.Scan(dir);
    send(call);
  }

  public ArrayList<SplitUri> getAudioDataSources() {
    return this.audiodatasources;
  }

  private void getAndSetAudioDataSources() {
    final Files.GetSources call = new Files.GetSources(FilesModel.Media.MUSIC);
    this.audiodatasources = new ArrayList<>();
    send(call);
    if (call.getResults() != null) {
      for (ListModel.SourceItem res : call.getResults()) {
        this.audiodatasources.add(new SplitUri(res.file, res.label, cm.getHostConfig().getAddress()));
      }
    }
  }

  // -----------------------------------------------------------------------------------

  /**
   * quit remote Kodi instance
   */
  public void ApplicationQuit() {
    final Application.Quit call = new Application.Quit();
    send(call);
  }

  /**
   * Toggles mute on/off
   */
  public void ApplicationMute() {
    final Application.GetProperties props = new Application.GetProperties("muted");
    send(props); // get current
    if (props.getResults() != null) {
      final Application.SetMute call = new Application.SetMute(new GlobalModel.Toggle(!props.getResult().muted));
      send(call); // toggle true/false
    }
  }

  /**
   * set volume 0-100
   * 
   * @param vol
   */
  public void ApplicationVolume(int vol) {
    final Application.SetVolume call = new Application.SetVolume(vol);
    send(call);
  }

  // -----------------------------------------------------------------------------------

  public void SystemEjectOpticalDrive() {
    final System.EjectOpticalDrive call = new System.EjectOpticalDrive();
    send(call);
  }

  public void SystemHibernate() {
    final System.EjectOpticalDrive call = new System.EjectOpticalDrive();
    send(call);
  }

  public void SystemShutdown() {
    final System.Shutdown call = new System.Shutdown();
    send(call);
  }

  public void SystemReboot() {
    final System.Reboot call = new System.Reboot();
    send(call);
  }

  public void SystemSuspend() {
    final System.Suspend call = new System.Suspend();
    send(call);
  }

  // -----------------------------------------------------------------------------------

  /**
   * Call getResult() / getResults() afterwards
   * 
   * @param call
   * @return call result
   * @throws ApiException
   */
  @SuppressWarnings("rawtypes")
  public void send(AbstractCall call) {
    if (!isConnected()) {
      LOGGER.warn("Cannot send RPC call - not connected");
      return;
    }
    try {
      LOGGER.debug("Calling Kodi: {}", call.getRequest());
      call.setResponse(JsonApiRequest.execute(cm.getHostConfig(), call.getRequest()));
      LOGGER.trace("Kodi response: {}", call.getResult());
    }
    catch (ApiException e) {
      LOGGER.error("Error calling Kodi: {}", e.getMessage());
    }
  }

  /**
   * Connect to Kodi with specified TCP port
   * 
   * @param config
   *          Host configuration
   * @throws ApiException
   *           Throws ApiException when something goes wrong with the initialization of the API.
   */
  public void connect(HostConfig config) throws ApiException {
    if (isConnected()) {
      cm.disconnect();
    }
    LOGGER.info("Connecting...");
    cm.connect(config);

    if (isConnected()) {
      getAndSetKodiVersion();
      getAndSetVideoDataSources();
      getAndSetAudioDataSources();
    }
  }

  public void connect() throws ApiException {
    Settings s = Settings.getInstance();
    if (s.getKodiHost().isEmpty()) {
      return;
    }
    HostConfig c = new HostConfig(s.getKodiHost(), s.getKodiHttpPort(), s.getKodiTcpPort(), s.getKodiUsername(), s.getKodiPassword());
    connect(c);
  }

  public void disconnect() {
    cm.disconnect();
  }

  public void getDataSources() {
    Settings.getInstance();
    final Files.GetSources f = new Files.GetSources(FilesModel.Media.VIDEO); // movies + tv !!!
    cm.call(f, new ApiCallback<ListModel.SourceItem>() {

      @Override
      public void onResponse(AbstractCall<ListModel.SourceItem> call) {
        LOGGER.info("found " + call.getResults().size() + " sources");

        LOGGER.info("--- KODI DATASOURCES ---");
        for (ListModel.SourceItem res : call.getResults()) {
          LOGGER.debug(res.file + " - " + new SplitUri(res.file, res.label, cm.getHostConfig().getAddress()));
        }

        LOGGER.info("--- TMM DATASOURCES ---");
        for (String ds : MovieModuleManager.SETTINGS.getMovieDataSource()) {
          LOGGER.info(ds + " - " + new SplitUri(ds));
        }
        for (String ds : TvShowModuleManager.SETTINGS.getTvShowDataSource()) {
          LOGGER.info(ds + " - " + new SplitUri(ds));
        }

        String ds = "//server/asdf";
        LOGGER.info(ds + " - " + new SplitUri(ds));

      }

      @Override
      public void onError(int code, String message, String hint) {
        LOGGER.error("Error {}: {}", code, message);
      }
    });
  }

  /**
   * @return json movie list or NULL
   */
  public ArrayList<MovieDetail> getAllMoviesSYNC() {
    final VideoLibrary.GetMovies call = new VideoLibrary.GetMovies(MovieFields.FILE);
    send(call);
    return call.getResults();
  }

  public void getAllMoviesASYNC() {
    // MovieFields.values.toArray(new String[0]) // all values
    final VideoLibrary.GetMovies vl = new VideoLibrary.GetMovies(MovieFields.FILE); // ID & label are always set; just add additional
    cm.call(vl, new ApiCallback<VideoModel.MovieDetail>() {

      @Override
      public void onResponse(AbstractCall<MovieDetail> call) {
        LOGGER.info("found " + call.getResults().size() + " movies");
        for (MovieDetail res : call.getResults()) {
          LOGGER.debug(res.toString());
        }
      }

      @Override
      public void onError(int code, String message, String hint) {
        LOGGER.error("Error {}: {}", code, message);
      }
    });
  }

  /**
   * Forces Kodi to reload movie from NFO
   * 
   * @param movie
   */
  public void triggerReload(Movie movie) {
    // MovieFields.values.toArray(new String[0]) // all values
    final VideoLibrary.GetMovies vl = new VideoLibrary.GetMovies(MovieFields.FILE); // ID & label are always set; just add additional
    cm.call(vl, new ApiCallback<VideoModel.MovieDetail>() {

      @Override
      public void onResponse(AbstractCall<MovieDetail> call) {
        LOGGER.info("found " + call.getResults().size() + " movies");
        for (MovieDetail res : call.getResults()) {
          LOGGER.debug(res.toString());
        }
      }

      @Override
      public void onError(int code, String message, String hint) {
        LOGGER.error("Error {}: {}", code, message);
      }
    });
  }

  public void getAllTvShows() {
    final VideoLibrary.GetTVShows vl = new VideoLibrary.GetTVShows();
    cm.call(vl, new ApiCallback<VideoModel.TVShowDetail>() {

      @Override
      public void onResponse(AbstractCall<TVShowDetail> call) {
        LOGGER.info("found " + call.getResults().size() + " shows");
        for (TVShowDetail res : call.getResults()) {
          LOGGER.debug(res.toString());
        }
      }

      @Override
      public void onError(int code, String message, String hint) {
        LOGGER.error("Error {}: {}", code, message);
      }
    });
  }

  /**
   * gets the MAC from an upnp UUID string (= last 6 bytes reversed)<br>
   * like upnp://00113201-aac2-0011-c2aa-02aa01321100 -> 00113201AA02
   *
   * @param uuid
   * @return
   */
  private static String getMacFromUpnpUUID(String uuid) {
    String s = uuid.substring(uuid.lastIndexOf('-') + 1);
    StringBuilder result = new StringBuilder();
    for (int i = s.length() - 2; i >= 0; i = i - 2) {
      result.append(new StringBuilder(s.substring(i, i + 2)));
      result.append(i > 1 ? ":" : ""); // skip last
    }
    return result.toString().toUpperCase();
  }

  /**
   * Splits an URI (Kodi datasource, file, UNC, ...) in it's parameters<br>
   * <br>
   * <b>Type:</b><br>
   * LOCAL for local datasources<br>
   * UPNP for UPNP ones<br>
   * SMB/NFS/... Url schema for other remotes
   * 
   * @author Myron Boyle
   *
   */
  public static class SplitUri {
    public String label    = "";
    public String type     = "";
    public String ip       = "";
    public String hostname = "";
    public String file     = "";

    @SuppressWarnings("unused")
    private SplitUri() {
    }

    public SplitUri(String ds) {
      this(ds, ds);
    }

    public SplitUri(String ds, String label) {
      this(ds, ds, "");
    }

    public SplitUri(String ds, String label, String ipForLocal) {
      this.label = label;

      URI u = null;
      try {
        try {
          ds = URLDecoder.decode(ds, "UTF-8");
          ds = URLDecoder.decode(ds, "UTF-8");
          ds = URLDecoder.decode(ds, "UTF-8");
        }
        catch (UnsupportedEncodingException e) {
          LOGGER.warn(e.getMessage());
        }
        ds = ds.replaceAll("\\\\", "/");
        if (ds.contains(":///")) {
          // 3 = file with scheme - parse as URI, but keep one slash
          u = new URI(ds.substring(ds.indexOf(":///") + 3));
        }
        else if (ds.contains("://")) {
          // 2 = //hostname/path - parse as URI
          u = new URI(ds);
        }
        else {
          // 0 = local file - parse as Path
          u = Paths.get(ds).toUri();
        }
      }
      catch (URISyntaxException e) {
        try {
          ds = ds.replaceAll(".*?:/{2,3}", ""); // replace scheme
          u = Paths.get(ds).toAbsolutePath().toUri();
        }
        catch (InvalidPathException e2) {
          LOGGER.warn(e2.getMessage());
        }
      }

      if (u != null && !StringUtil.isBlank(u.getHost())) {
        this.file = u.getPath();
        if (ds.startsWith("upnp")) {
          this.type = "UPNP";
          this.hostname = getMacFromUpnpUUID(u.getHost());

          UpnpService us = Upnp.getInstance().getUpnpService();
          if (us != null) {
            Registry registry = us.getRegistry();
            if (registry != null) {
              @SuppressWarnings("rawtypes")
              Device foundDevice = registry.getDevice(UDN.valueOf(u.getHost()), true);
              if (foundDevice != null) {
                this.ip = foundDevice.getDetails().getPresentationURI().getHost();
              }
            }
          }
        }
        else {
          try {
            this.type = u.getScheme().toUpperCase(Locale.ROOT);
            this.hostname = u.getHost();
            InetAddress i = InetAddress.getByName(u.getHost()); // FIXME: will block for ~5 secs
            this.ip = i.getHostAddress();
          }
          catch (UnknownHostException e) {
            this.hostname = u.getHost();
          }
        }
      }
      else {
        this.type = "LOCAL";
        this.file = ds;
        if (ipForLocal.isEmpty()) {
          this.ip = "127.0.0.1";
          try {
            this.hostname = InetAddress.getLocalHost().getHostName(); // "our" hostname, but not remote
          }
          catch (UnknownHostException e) {
            this.hostname = "localhost";
          }
        }
        else {
          // remote
          this.ip = ipForLocal;
          this.hostname = ipForLocal;
        }
      }
    }

    @Override
    public String toString() {
      return "SplitUri [label=" + label + ", type=" + type + ", ip=" + ip + ", hostname=" + hostname + ", file=" + file + "]";
    }

    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((file == null) ? 0 : file.hashCode());
      result = prime * result + ((hostname == null) ? 0 : hostname.hashCode());
      result = prime * result + ((ip == null) ? 0 : ip.hashCode());
      return result;
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj)
        return true;
      if (obj == null)
        return false;
      if (getClass() != obj.getClass())
        return false;
      SplitUri other = (SplitUri) obj;

      if (file == null || file.isEmpty()) {
        return false;
      }
      else {
        // 1: mandatory file does not match - step out
        if (!file.equals(other.file)) {
          return false;
        }
      }

      // 2: - check either matching IP or hostname
      if (ip != null && !ip.isEmpty() && ip.equals(other.ip)) {
        return true;
      }
      if (hostname != null && !hostname.isEmpty() && hostname.equalsIgnoreCase(other.hostname)) {
        return true;
      }

      // 3: did not match? return false
      return false;
    }

  }
}
