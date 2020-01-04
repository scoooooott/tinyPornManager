/*
 * Copyright 2012 - 2020 Manuel Laggner
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
package org.tinymediamanager.thirdparty.upnp;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.fourthline.cling.DefaultUpnpServiceConfiguration;
import org.fourthline.cling.UpnpService;
import org.fourthline.cling.UpnpServiceImpl;
import org.fourthline.cling.binding.LocalServiceBindingException;
import org.fourthline.cling.binding.annotations.AnnotationLocalServiceBinder;
import org.fourthline.cling.binding.xml.DescriptorBindingException;
import org.fourthline.cling.controlpoint.ActionCallback;
import org.fourthline.cling.model.DefaultServiceManager;
import org.fourthline.cling.model.DiscoveryOptions;
import org.fourthline.cling.model.ValidationException;
import org.fourthline.cling.model.action.ActionInvocation;
import org.fourthline.cling.model.message.UpnpResponse;
import org.fourthline.cling.model.message.header.UDADeviceTypeHeader;
import org.fourthline.cling.model.meta.Device;
import org.fourthline.cling.model.meta.DeviceDetails;
import org.fourthline.cling.model.meta.DeviceIdentity;
import org.fourthline.cling.model.meta.Icon;
import org.fourthline.cling.model.meta.LocalDevice;
import org.fourthline.cling.model.meta.LocalService;
import org.fourthline.cling.model.meta.ManufacturerDetails;
import org.fourthline.cling.model.meta.ModelDetails;
import org.fourthline.cling.model.meta.Service;
import org.fourthline.cling.model.profile.RemoteClientInfo;
import org.fourthline.cling.model.types.DLNACaps;
import org.fourthline.cling.model.types.DLNADoc;
import org.fourthline.cling.model.types.DeviceType;
import org.fourthline.cling.model.types.UDADeviceType;
import org.fourthline.cling.model.types.UDAServiceId;
import org.fourthline.cling.model.types.UDN;
import org.fourthline.cling.registry.RegistrationException;
import org.fourthline.cling.support.avtransport.callback.Play;
import org.fourthline.cling.support.avtransport.callback.SetAVTransportURI;
import org.fourthline.cling.support.avtransport.callback.Stop;
import org.fourthline.cling.support.connectionmanager.ConnectionManagerService;
import org.fourthline.cling.support.contentdirectory.DIDLParser;
import org.fourthline.cling.support.model.DIDLContent;
import org.fourthline.cling.support.model.ProtocolInfos;
import org.fourthline.cling.support.model.dlna.DLNAProfiles;
import org.fourthline.cling.support.model.dlna.DLNAProtocolInfo;
import org.fourthline.cling.transport.RouterException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.ReleaseInfo;
import org.tinymediamanager.core.entities.MediaEntity;
import org.tinymediamanager.core.entities.MediaFile;
import org.tinymediamanager.core.movie.entities.Movie;
import org.tinymediamanager.core.tvshow.entities.TvShowEpisode;
import org.tinymediamanager.thirdparty.NetworkUtil;

public class Upnp {
  private static final Logger LOGGER         = LoggerFactory.getLogger(Upnp.class);
  public static final String  IP             = NetworkUtil.getMachineIPAddress();
  public static final int     UPNP_PORT      = 8008;
  public static final int     WEBSERVER_PORT = 8009;

  // ROOT is fix 0 , do not change!!
  public static final String  ID_ROOT        = "0";
  public static final String  ID_MOVIES      = "1";
  public static final String  ID_TVSHOWS     = "2";

  private static Upnp         instance;
  private UpnpService         upnpService    = null;
  private WebServer           webServer      = null;
  private Service             playerService  = null;
  private LocalDevice         localDevice    = null;

  private Upnp() {
  }

  public synchronized static Upnp getInstance() {
    if (Upnp.instance == null) {
      Upnp.instance = new Upnp();
    }
    return Upnp.instance;
  }

  public UpnpService getUpnpService() {
    return this.upnpService;
  }

  public LocalDevice getLocalDevice() {
    return this.localDevice;
  }

  /**
   * Starts out UPNP Service / Listener
   */
  public void createUpnpService() {
    if (this.upnpService == null) {
      this.upnpService = new UpnpServiceImpl(new DefaultUpnpServiceConfiguration(UPNP_PORT), UpnpListener.getListener());
      try {
        this.upnpService.getRouter().enable();
      }
      catch (RouterException e) {
        LOGGER.warn("Could not start UPNP router: {}", e);
      }
    }
  }

  private LocalDevice getDevice()
      throws ValidationException, LocalServiceBindingException, IOException, IllegalArgumentException, URISyntaxException {

    if (localDevice == null) {
      DeviceIdentity identity = new DeviceIdentity(UDN.uniqueSystemIdentifier("tinyMediaManager"), 600);
      DeviceType type = new UDADeviceType("MediaServer", 1);
      String hostname = NetworkUtil.getMachineHostname();
      if (hostname == null) {
        hostname = IP;
      }

      // @formatter:off
      DeviceDetails details = new DeviceDetails("tinyMediaManager",
        new ManufacturerDetails("tinyMediaManager", "https://www.tinymediamanager.org/"),
        new ModelDetails("tinyMediaManager", "tinyMediaManager - Media Server", ReleaseInfo.getVersion()), 
        // @Namespace default /dev/<udn>/desc
        new URI("http://" + hostname + ":" + UPNP_PORT + "/dev/" + identity.getUdn().getIdentifierString() + "/desc"),
        new DLNADoc[] {
            new DLNADoc("DMS", DLNADoc.Version.V1_5), 
            new DLNADoc("M-DMS", DLNADoc.Version.V1_5) 
        },
        new DLNACaps(new String[] { "av-upload", "image-upload", "audio-upload" }));
      // @formatter:on

      final ProtocolInfos protocols = new ProtocolInfos();
      for (DLNAProfiles dlnaProfile : DLNAProfiles.values()) {
        if (dlnaProfile == DLNAProfiles.NONE) {
          continue;
        }
        try {
          protocols.add(new DLNAProtocolInfo(dlnaProfile));
        }
        catch (Exception e) {
          // Silently ignored.
        }
      }

      LOGGER.info("Hello, i'm " + identity.getUdn().getIdentifierString());

      // Content Directory Service
      LocalService<ContentDirectoryService> cds = new AnnotationLocalServiceBinder().read(ContentDirectoryService.class);
      cds.setManager(new DefaultServiceManager<ContentDirectoryService>(cds, ContentDirectoryService.class));

      // Connection Manager Service
      LocalService<ConnectionManagerService> cms = new AnnotationLocalServiceBinder().read(ConnectionManagerService.class);
      // cms.setManager(new DefaultServiceManager<>(cms, ConnectionManagerService.class));
      cms.setManager(new DefaultServiceManager<ConnectionManagerService>(cms, ConnectionManagerService.class) {
        @Override
        protected ConnectionManagerService createServiceInstance() throws Exception {
          return new ConnectionManagerService(protocols, null);
        }
      });

      LocalService<MSMediaReceiverRegistrarService> mss = new AnnotationLocalServiceBinder().read(MSMediaReceiverRegistrarService.class);
      mss.setManager(new DefaultServiceManager<MSMediaReceiverRegistrarService>(mss, MSMediaReceiverRegistrarService.class));

      Icon icon = null;
      try {
        // only when deployed
        icon = new Icon("image/png", 128, 128, 24, new File("tmm.png"));
      }
      catch (Exception e) {
        // in eclipse
        try {
          icon = new Icon("image/png", 128, 128, 24, new File("AppBundler/tmm.png"));
        }
        catch (Exception e2) {
          LOGGER.warn("Did not find device icon...");
        }
      }

      this.localDevice = new LocalDevice(identity, type, details, new Icon[] { icon }, new LocalService[] { cds, cms, mss });
    }

    return this.localDevice;
  }

  /**
   * Sends a UPNP broadcast message, to find some players.<br>
   * Should be available shortly via getAvailablePlayers()
   */
  public void sendPlayerSearchRequest() {
    createUpnpService();
    this.upnpService.getControlPoint().search(new UDADeviceTypeHeader(new UDADeviceType("MediaRenderer")));
  }

  /**
   * Finds all available Players (implementing the MediaRenderer stack)<br>
   * You might want to call sendPlayerSearchRequest() a few secs before, to populate freshly
   * 
   * @return List of devices
   */
  public List<Device> getAvailablePlayers() {
    createUpnpService();
    List<Device> ret = new ArrayList<>();
    for (Device device : this.upnpService.getRegistry().getDevices()) {
      if (device.getType().getType().equals("MediaRenderer")) {
        ret.add(device);
      }
    }
    return ret;
  }

  /**
   * Sets a device as our player for play/stop and other services<br>
   * Use getAvailablePlayers() for a list of them.
   * 
   * @param device
   *          device for playing
   */
  public void setPlayer(Device device) {
    this.playerService = device.findService(new UDAServiceId("AVTransport"));
    if (this.playerService == null) {
      LOGGER.warn("Could not find AVTransportService on device " + device.getDisplayString());
    }
  }

  /**
   * Plays a file/url
   * 
   * @param me
   *          the media entity
   * @param mf
   *          the media file
   */
  public void playFile(MediaEntity me, MediaFile mf) {
    if (this.playerService == null) {
      LOGGER.warn("No player set - did you call setPlayer(Device) ?");
      return;
    }
    if (mf == null) {
      LOGGER.warn("parameters empty!");
      return;
    }

    String url = "";
    String meta = "NO METADATA";

    if (me != null) {
      try {
        DIDLContent didl = new DIDLContent();
        DIDLParser dip = new DIDLParser();
        if (me instanceof Movie) {
          didl.addItem(Metadata.getUpnpMovie((Movie) me, true));
        }
        else if (me instanceof TvShowEpisode) {
          didl.addItem(Metadata.getUpnpTvShowEpisode(((TvShowEpisode) me).getTvShow(), (TvShowEpisode) me, true));
        }

        // get url from didl, no need to regenerate this
        url = didl.getItems().get(0).getResources().get(0).getValue();

        meta = dip.generate(didl);
      }
      catch (Exception e) {
        LOGGER.warn("Could not generate metadata / url");
        return;
      }
    }

    ActionCallback setAVTransportURIAction = new SetAVTransportURI(this.playerService, url, meta) {
      @Override
      public void failure(ActionInvocation invocation, UpnpResponse operation, String defaultMsg) {
        LOGGER.warn("Setting URL for player failed! " + defaultMsg);
      }
    };
    this.upnpService.getControlPoint().execute(setAVTransportURIAction);

    ActionCallback playAction = new Play(this.playerService) {
      @Override
      public void failure(ActionInvocation invocation, UpnpResponse operation, String defaultMsg) {
        LOGGER.warn("Playing failed! " + defaultMsg);
      }
    };
    this.upnpService.getControlPoint().execute(playAction);
  }

  /**
   * stop the player
   */
  public void stopPlay() {
    if (this.playerService == null) {
      return;
    }

    ActionCallback stopAction = new Stop(this.playerService) {
      @Override
      public void failure(ActionInvocation invocation, UpnpResponse operation, String defaultMsg) {
        LOGGER.warn("Stopping failed! " + defaultMsg);
      }
    };
    this.upnpService.getControlPoint().execute(stopAction);
  }

  public String getDeviceDescriptorXML() {
    String xml = "";
    try {
      xml = upnpService.getConfiguration().getDeviceDescriptorBinderUDA10().generate(localDevice, new RemoteClientInfo(),
          upnpService.getConfiguration().getNamespace());
    }
    catch (DescriptorBindingException e) {
      LOGGER.warn("Could not generate UPNP device descriptor", e);
    }
    return xml;
  }

  /**
   * starts a WebServer for accessing MediaFiles over UPNP<br>
   * In /upnp/(movie|tvshow)/UUIDofMediaEntity/(folder)/file.ext format
   */
  public void startWebServer() {
    try {
      if (this.webServer == null) {
        this.webServer = new WebServer(WEBSERVER_PORT);
      }
    }
    catch (IOException e) {
      LOGGER.warn("Could not start WebServer!", e);
    }
  }

  public void stopWebServer() {
    if (this.webServer != null) {
      this.webServer.closeAllConnections();
    }
  }

  public void startMediaServer() {
    createUpnpService();
    try {
      this.upnpService.getRegistry().addDevice(getDevice(), new DiscoveryOptions(true));
    }
    catch (RegistrationException | LocalServiceBindingException | ValidationException | IOException | IllegalArgumentException
        | URISyntaxException e) {
      LOGGER.warn("could not start UPNP MediaServer!", e);
    }
  }

  public void stopMediaServer() {
    if (this.upnpService != null) {
      this.upnpService.getRegistry().removeAllLocalDevices();
    }
  }

  public void shutdown() {
    stopPlay();
    stopWebServer();
    stopMediaServer();
    if (this.upnpService != null) {
      try {
        this.upnpService.getRouter().shutdown();
      }
      catch (RouterException e) {
        LOGGER.warn("Could not shutdown the UPNP router.");
      }
      this.upnpService.shutdown();
    }
  }
}
