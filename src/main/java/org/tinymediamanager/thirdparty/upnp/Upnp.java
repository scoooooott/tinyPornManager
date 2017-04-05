/*
 * Copyright 2012 - 2017 Manuel Laggner
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.fourthline.cling.UpnpService;
import org.fourthline.cling.UpnpServiceImpl;
import org.fourthline.cling.binding.LocalServiceBindingException;
import org.fourthline.cling.controlpoint.ActionCallback;
import org.fourthline.cling.model.ValidationException;
import org.fourthline.cling.model.action.ActionInvocation;
import org.fourthline.cling.model.message.UpnpResponse;
import org.fourthline.cling.model.message.header.UDADeviceTypeHeader;
import org.fourthline.cling.model.meta.Device;
import org.fourthline.cling.model.meta.Service;
import org.fourthline.cling.model.types.UDADeviceType;
import org.fourthline.cling.model.types.UDAServiceId;
import org.fourthline.cling.registry.RegistrationException;
import org.fourthline.cling.support.avtransport.callback.Play;
import org.fourthline.cling.support.avtransport.callback.SetAVTransportURI;
import org.fourthline.cling.support.avtransport.callback.Stop;
import org.fourthline.cling.support.contentdirectory.DIDLParser;
import org.fourthline.cling.support.model.DIDLContent;
import org.fourthline.cling.transport.RouterException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.core.entities.MediaEntity;
import org.tinymediamanager.core.entities.MediaFile;
import org.tinymediamanager.core.movie.entities.Movie;
import org.tinymediamanager.core.tvshow.entities.TvShowEpisode;
import org.tinymediamanager.thirdparty.NetworkUtil;

public class Upnp {
  private static final Logger LOGGER        = LoggerFactory.getLogger(Upnp.class);
  public static final String  IP            = NetworkUtil.getMachineIPAddress();

  // ROOT is fix 0 , do not change!!
  public static final String  ID_ROOT       = "0";
  public static final String  ID_MOVIES     = "1";
  public static final String  ID_TVSHOWS    = "2";

  private static Upnp         instance;
  private UpnpService         upnpService   = null;
  private WebServer           webServer     = null;
  private Service             playerService = null;

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

  /**
   * Starts out UPNP Service / Listener
   */
  public void createUpnpService() {
    if (this.upnpService == null) {
      this.upnpService = new UpnpServiceImpl(UpnpListener.getListener());
    }
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

  /**
   * starts a WebServer for accessing MediaFiles over UPNP<br>
   * In /upnp/(movie|tvshow)/UUIDofMediaEntity/(folder)/file.ext format
   */
  public void startWebServer() {
    try {
      if (this.webServer == null) {
        this.webServer = new WebServer();
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
      this.upnpService.getRegistry().addDevice(MediaServer.createDevice());
    }
    catch (RegistrationException | LocalServiceBindingException | ValidationException | IOException e) {
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
