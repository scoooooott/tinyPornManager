package org.tinymediamanager.thirdparty;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.core.MediaFileType;
import org.tinymediamanager.core.Message;
import org.tinymediamanager.core.MessageManager;
import org.tinymediamanager.core.Settings;
import org.tinymediamanager.core.entities.MediaEntity;
import org.tinymediamanager.core.entities.MediaFile;
import org.tinymediamanager.core.movie.MovieList;
import org.tinymediamanager.core.movie.MovieModuleManager;
import org.tinymediamanager.core.movie.entities.Movie;
import org.tinymediamanager.core.tvshow.TvShowList;
import org.tinymediamanager.core.tvshow.TvShowModuleManager;
import org.tinymediamanager.core.tvshow.entities.TvShow;
import org.tinymediamanager.core.tvshow.entities.TvShowEpisode;
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
import org.tinymediamanager.jsonrpc.api.model.VideoModel.EpisodeDetail;
import org.tinymediamanager.jsonrpc.api.model.VideoModel.EpisodeFields;
import org.tinymediamanager.jsonrpc.api.model.VideoModel.MovieDetail;
import org.tinymediamanager.jsonrpc.api.model.VideoModel.MovieFields;
import org.tinymediamanager.jsonrpc.api.model.VideoModel.TVShowDetail;
import org.tinymediamanager.jsonrpc.api.model.VideoModel.TVShowFields;
import org.tinymediamanager.jsonrpc.config.HostConfig;
import org.tinymediamanager.jsonrpc.io.ApiCallback;
import org.tinymediamanager.jsonrpc.io.ApiException;
import org.tinymediamanager.jsonrpc.io.ConnectionListener;
import org.tinymediamanager.jsonrpc.io.JavaConnectionManager;
import org.tinymediamanager.jsonrpc.io.JsonApiRequest;
import org.tinymediamanager.jsonrpc.notification.AbstractEvent;

public class KodiRPC {
  private static final Logger          LOGGER           = LoggerFactory.getLogger(KodiRPC.class);
  private static KodiRPC               instance;
  private static JavaConnectionManager cm               = new JavaConnectionManager();

  private String                       kodiVersion      = "";
  private ArrayList<SplitUri>          videodatasources = new ArrayList<>();
  private ArrayList<SplitUri>          audiodatasources = new ArrayList<>();

  // TMM DbId-to-KodiId mappings
  private HashMap<UUID, Integer>       moviemappings    = new HashMap<>();
  private HashMap<UUID, Integer>       tvshowmappings   = new HashMap<>();

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
    if (call.getResults() != null && !call.getResults().isEmpty()) {
      for (ListModel.SourceItem res : call.getResults()) {
        this.videodatasources.add(new SplitUri(res.file, res.label, cm.getHostConfig().getAddress()));
      }
    }
  }

  /**
   * builds the moviemappings: DBid -> Kodi ID
   */
  protected void getAndSetMovieMappings() {
    final VideoLibrary.GetMovies call = new VideoLibrary.GetMovies(MovieFields.FILE);
    send(call);
    if (call.getResults() != null && !call.getResults().isEmpty()) {

      // cache our video files/paths as SplitUris
      Map<SplitUri, UUID> tmmFiles = new HashMap<SplitUri, UUID>();
      for (Movie movie : MovieList.getInstance().getMovies()) {
        MediaFile main = movie.getMainVideoFile();
        if (movie.isDisc()) {
          // Kodi RPC sends only those disc files
          for (MediaFile mf : movie.getMediaFiles(MediaFileType.VIDEO)) {
            if (mf.getFilename().equalsIgnoreCase("VIDEO_TS.IFO") || mf.getFilename().equalsIgnoreCase("INDEX.BDMV")) {
              tmmFiles.put(new SplitUri(mf.getFileAsPath().toString()), movie.getDbId());
            }
          }
        }
        else {
          tmmFiles.put(new SplitUri(main.getFileAsPath().toString()), movie.getDbId());
        }
      }

      // iterate over all Kodi resources
      for (MovieDetail res : call.getResults()) {
        if (res.file.startsWith("stack")) {
          String[] files = res.file.split(" , ");
          for (String s : files) {
            s = s.replaceFirst("^stack://", "");
            SplitUri sp = new SplitUri(s, res.label, cm.getHostConfig().getAddress()); // generate clean object

            for (Map.Entry<SplitUri, UUID> entry : tmmFiles.entrySet()) {
              SplitUri tmmsp = entry.getKey();
              UUID uuid = entry.getValue();
              if (sp.equals(tmmsp)) {
                LOGGER.trace(sp.toString());
                moviemappings.put(uuid, res.movieid);
                break;
              }
            }
          }
        }
        else {
          SplitUri sp = new SplitUri(res.file, res.label, cm.getHostConfig().getAddress()); // generate clean object

          for (Map.Entry<SplitUri, UUID> entry : tmmFiles.entrySet()) {
            SplitUri tmmsp = entry.getKey();
            UUID uuid = entry.getValue();
            if (sp.equals(tmmsp)) {
              LOGGER.trace(sp.toString());
              moviemappings.put(uuid, res.movieid);
              break;
            }
          }
        }
      }

      // debug output
      for (Map.Entry<UUID, Integer> entry : moviemappings.entrySet()) {
        UUID key = entry.getKey();
        Integer value = entry.getValue();
        LOGGER.debug("TMM: {} - Kodi: {}", key, value);
      }
      LOGGER.debug("mapped {} items", moviemappings.size());

      // intersect
      for (Map.Entry<SplitUri, UUID> entry : tmmFiles.entrySet()) {
        if (!moviemappings.containsKey(entry.getValue())) {
          LOGGER.warn("could not map: {}", entry.getKey());
        }
      }
      tmmFiles.clear();
    }
  }

  /**
   * builds the show/episode mappings: DBid -> Kodi ID
   */
  protected void getAndSetTvShowMappings() {
    final VideoLibrary.GetTVShows call = new VideoLibrary.GetTVShows(TVShowFields.FILE);
    send(call);
    if (call.getResults() != null && !call.getResults().isEmpty()) {

      // cache our video files/paths as SplitUris
      Map<SplitUri, UUID> tmmFiles = new HashMap<SplitUri, UUID>();
      for (TvShow show : TvShowList.getInstance().getTvShows()) {
        tmmFiles.put(new SplitUri(show.getPathNIO().toString()), show.getDbId()); // folder

        for (TvShowEpisode ep : show.getEpisodes()) {
          if (ep.isDisc()) {
            // Kodi RPC sends only those disc files
            for (MediaFile mf : ep.getMediaFiles(MediaFileType.VIDEO)) {
              if (mf.getFilename().equalsIgnoreCase("VIDEO_TS.IFO") || mf.getFilename().equalsIgnoreCase("INDEX.BDMV")) {
                tmmFiles.put(new SplitUri(mf.getFileAsPath().toString()), ep.getDbId());
              }
            }
          }
          else {
            tmmFiles.put(new SplitUri(ep.getMainVideoFile().getFileAsPath().toString()), ep.getDbId()); // file
          }
        }

      }

      // iterate over all Kodi shows
      for (TVShowDetail show : call.getResults()) {
        SplitUri sp = new SplitUri(show.file, show.label, cm.getHostConfig().getAddress()); // generate clean object

        for (Map.Entry<SplitUri, UUID> entry : tmmFiles.entrySet()) {
          SplitUri tmmsp = entry.getKey();
          UUID uuid = entry.getValue();
          if (sp.equals(tmmsp)) {
            LOGGER.trace(sp.toString());
            tvshowmappings.put(uuid, show.tvshowid);
            break;
          }
        }

        // inner call to get all episodes
        final VideoLibrary.GetEpisodes epCall = new VideoLibrary.GetEpisodes(show.tvshowid, EpisodeFields.FILE);
        send(epCall);
        if (epCall.getResults() != null && !epCall.getResults().isEmpty()) {

          for (EpisodeDetail ep : epCall.getResults()) {
            SplitUri spEp = new SplitUri(ep.file, ep.label, cm.getHostConfig().getAddress()); // generate clean object

            for (Map.Entry<SplitUri, UUID> entry : tmmFiles.entrySet()) {
              SplitUri tmmsp = entry.getKey();
              UUID uuid = entry.getValue();
              if (spEp.equals(tmmsp)) {
                LOGGER.trace(spEp.toString());
                tvshowmappings.put(uuid, ep.episodeid);
                break;
              }
            }
          }
        }

      }

      // debug output
      for (Map.Entry<UUID, Integer> entry : tvshowmappings.entrySet()) {
        UUID key = entry.getKey();
        Integer value = entry.getValue();
        LOGGER.debug("TMM: {} - Kodi: {}", key, value);
      }
      LOGGER.debug("mapped {} items", tvshowmappings.size());

      // intersect
      for (Map.Entry<SplitUri, UUID> entry : tmmFiles.entrySet()) {
        if (!tvshowmappings.containsKey(entry.getValue())) {
          LOGGER.warn("could not map: {}", entry.getKey());
        }
      }
      tmmFiles.clear();
    }
  }

  public void refreshFromNfo(List<MediaEntity> entities) {
    for (MediaEntity entity : entities) {
      refreshFromNfo(entity);
    }
  }

  public void refreshFromNfo(MediaEntity entity) {
    Integer kodiID = moviemappings.get(entity.getDbId());
    if (kodiID == null) {
      kodiID = tvshowmappings.get(entity.getDbId());
    }
    if (kodiID != null) {

      if (entity instanceof Movie) {
        final VideoLibrary.RefreshMovie call = new VideoLibrary.RefreshMovie(kodiID, false); // always refresh from NFO
        send(call);
      }
      else if (entity instanceof TvShow) {
        final VideoLibrary.RefreshTVShow call = new VideoLibrary.RefreshTVShow(kodiID, false); // always refresh from NFO
        send(call);
      }
      else if (entity instanceof TvShowEpisode) {
        final VideoLibrary.RefreshEpisode call = new VideoLibrary.RefreshEpisode(kodiID, false); // always refresh from NFO
        send(call);
      }

    }
    else {
      LOGGER.error("Unable to refresh - could not map '{}' to Kodi library! {}", entity.getTitle(), entity.getDbId());
    }
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
    if (call.getResults() != null && !call.getResults().isEmpty()) {
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
    if (props.getResults() != null && !props.getResults().isEmpty()) {
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
      call.setResponse(JsonApiRequest.execute(cm.getHostConfig(), call.getRequest()));
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
      getAndSetMovieMappings();
      getAndSetTvShowMappings();
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
}
