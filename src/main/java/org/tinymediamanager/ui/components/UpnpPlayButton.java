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
package org.tinymediamanager.ui.components;

import java.awt.event.ActionEvent;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;

import org.fourthline.cling.model.meta.Device;
import org.tinymediamanager.Globals;
import org.tinymediamanager.core.Message;
import org.tinymediamanager.core.MessageManager;
import org.tinymediamanager.core.entities.MediaEntity;
import org.tinymediamanager.core.entities.MediaFile;
import org.tinymediamanager.thirdparty.upnp.Upnp;
import org.tinymediamanager.ui.IconManager;
import org.tinymediamanager.ui.TmmUIHelper;

/**
 * the class UpnpPlayButton is used to create a UPNP aware Play-Button
 * 
 * @author Manuel Laggner
 */
public abstract class UpnpPlayButton extends JButton {

  public UpnpPlayButton() {
    super();
    setAction(new PlayAction());
  }

  /**
   * get the media file to be played
   * 
   * @return the media file
   */
  public abstract MediaFile getMediaFile();

  /**
   * get the media entity for the media file to be played
   * 
   * @return the media entity
   */
  public abstract MediaEntity getMediaEntity();

  /**
   * play the media file on the local player
   */
  private void playLocal() {
    MediaFile mf = getMediaFile();
    if (mf == null) {
      return;
    }
    try {
      TmmUIHelper.openFile(mf.getFileAsPath());
    }
    catch (Exception ex) {
      MessageManager.instance
          .pushMessage(new Message(Message.MessageLevel.ERROR, mf, "message.erroropenfile", new String[] { ":", ex.getLocalizedMessage() }));
    }
  }

  /**
   * play the media file via UPNP on the chosen device
   * 
   * @param device
   *          the device to play the file on
   */
  private void playViaUpnp(Device device) {
    Upnp instance = Upnp.getInstance();
    instance.setPlayer(device);
    instance.playFile(getMediaEntity(), getMediaFile());
  }

  private class PlayAction extends AbstractAction {

    private PlayAction() {
      putValue(SMALL_ICON, IconManager.PLAY);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      // do we want to play via UPNP?
      if (!Globals.settings.isUpnpRemotePlay()) {
        playLocal();
      }
      else {
        // show a popup with upnp devices if some are found in the network
        List<Device> upnpDevices = Upnp.getInstance().getAvailablePlayers();
        if (upnpDevices.isEmpty()) {
          playLocal();
        }
        else {
          JPopupMenu menu = new JPopupMenu();
          menu.add(new DeviceAction("System player", null));
          menu.add(new JSeparator());

          for (Device device : upnpDevices) {
            menu.add(new DeviceAction(device.getDetails().getFriendlyName(), device));
          }

          // show popup menu
          menu.show(UpnpPlayButton.this, 0, UpnpPlayButton.this.getBounds().height);
        }
      }
    }
  }

  private class DeviceAction extends AbstractAction {
    private Device device;

    private DeviceAction(String title, Device device) {
      putValue(NAME, title);
      this.device = device;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      // play on local media player
      if (device == null) {
        playLocal();
      }
      else {
        playViaUpnp(device);
      }
    }
  }
}
