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

import org.fourthline.cling.model.meta.LocalDevice;
import org.fourthline.cling.model.meta.RemoteDevice;
import org.fourthline.cling.registry.Registry;
import org.fourthline.cling.registry.RegistryListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UpnpListener {

  private static final Logger LOGGER = LoggerFactory.getLogger(UpnpListener.class);

  public static RegistryListener getListener() {

    RegistryListener listener = new RegistryListener() {

      public void remoteDeviceDiscoveryStarted(Registry registry, RemoteDevice device) {
        LOGGER.debug("Discovery started: " + device.getDisplayString());
      }

      public void remoteDeviceDiscoveryFailed(Registry registry, RemoteDevice device, Exception ex) {
        LOGGER.debug("Discovery failed: " + device.getDisplayString() + " => " + ex);
      }

      public void remoteDeviceAdded(Registry registry, RemoteDevice device) {
        LOGGER.debug("Remote device available: " + device.getDisplayString());
      }

      public void remoteDeviceUpdated(Registry registry, RemoteDevice device) {
        // use TRACE, do not care about that log spam
        LOGGER.trace("Remote device updated: " + device.getDisplayString());
      }

      public void remoteDeviceRemoved(Registry registry, RemoteDevice device) {
        LOGGER.debug("Remote device removed: " + device.getDisplayString());
      }

      public void localDeviceAdded(Registry registry, LocalDevice device) {
        LOGGER.debug("Local device added: " + device.getDisplayString());
      }

      public void localDeviceRemoved(Registry registry, LocalDevice device) {
        LOGGER.debug("Local device removed: " + device.getDisplayString());
      }

      public void beforeShutdown(Registry registry) {
        LOGGER.debug("Before shutdown, the registry has devices: " + registry.getDevices().size());
      }

      public void afterShutdown() {
        LOGGER.debug("Shutdown of registry complete!");
      }
    };
    return listener;
  }
}
