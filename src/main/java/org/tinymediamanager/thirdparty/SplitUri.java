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

package org.tinymediamanager.thirdparty;

import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.fourthline.cling.UpnpService;
import org.fourthline.cling.model.meta.Device;
import org.fourthline.cling.model.types.UDN;
import org.fourthline.cling.registry.Registry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.thirdparty.upnp.Upnp;

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
public class SplitUri {
  private static final Logger LOGGER     = LoggerFactory.getLogger(SplitUri.class);

  public String               file       = "";
  public String               datasource = "";
  public String               label      = "";
  public String               type       = "";
  public String               ip         = "";
  public String               hostname   = "";

  private Map<String, String> lookup     = new HashMap<>();

  @SuppressWarnings("unused")
  private SplitUri() {
  }

  public SplitUri(String ds, String file) {
    this(ds, file, "", "");
  }

  public SplitUri(String ds, String file, String label, String ipForLocal) {

    // remove trailing slashes
    if (ds.matches(".*[\\\\/]$")) {
      ds = ds.substring(0, ds.length() - 1);
    }
    // remove datasource from file
    if (file.startsWith(ds)) {
      file = file.substring(ds.length());
    }

    this.datasource = ds;
    this.file = file;

    int schema = file.indexOf("://");
    if (schema == -1) {
      schema = file.indexOf(":\\\\");
    }
    if (schema > 0) {
      this.file = this.file.substring(schema + 3);
    }
    this.file = Paths.get(this.file).toString(); // unify slashes
    this.label = label;

    // try to parse datasource and unify
    URI u = null;
    try {
      try {
        ds = URLDecoder.decode(ds, "UTF-8");
        ds = URLDecoder.decode(ds, "UTF-8");
        ds = URLDecoder.decode(ds, "UTF-8");
      }
      catch (Exception e) {
        LOGGER.warn("Could not decode uri '{}': {}", ds, e.getMessage());
      }
      ds = ds.replaceAll("\\\\", "/");
      // for directories, we might get a trailing delimiter - remove
      if (ds.endsWith("/")) {
        ds = ds.substring(0, ds.length() - 1);
      }
      if (ds.contains(":///")) {
        // 3 = file with scheme - parse as URI, but keep one slash
        ds = ds.replaceAll(" ", "%20"); // space in urls
        ds = ds.replaceAll("#", "%23"); // hash sign in urls
        u = new URI(ds.substring(ds.indexOf(":///") + 3));
      }
      else if (ds.contains("://")) {
        // 2 = //hostname/path - parse as URI
        ds = ds.replaceAll(" ", "%20"); // space in urls
        ds = ds.replaceAll("#", "%23"); // hash sign in urls
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
        LOGGER.warn("Invalid path: {} - {}", ds, e2.getMessage());
      }
    }
    catch (InvalidPathException e) {
      LOGGER.warn("Invalid path: {} - {}", ds, e.getMessage());
    }

    if (u != null && !StringUtils.isBlank(u.getHost())) {
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
        this.type = u.getScheme().toUpperCase(Locale.ROOT);
        this.hostname = u.getHost();
        try {
          String ip = lookup.get(u.getHost());
          // cache
          if (ip == null) {
            InetAddress i = InetAddress.getByName(u.getHost());
            ip = i.getHostAddress();
            lookup.put(u.getHost(), ip);
          }
          this.ip = ip;
        }
        catch (Exception e) {
          LOGGER.warn("Could not lookup IP for {}: {}", u.getHost(), e.getMessage());
        }
      }
      this.datasource = u.getPath(); // datasource is just path without server
    }
    else {
      this.type = "LOCAL";
      if (ipForLocal.isEmpty()) {
        this.ip = "127.0.0.1";
        this.hostname = "localhost";
      }
      else {
        try {
          String tmp = lookup.get(ipForLocal);
          if (tmp == null) {
            InetAddress i = InetAddress.getByName(ipForLocal);
            this.ip = i.getHostAddress();
            this.hostname = i.getHostName();
            lookup.put(ipForLocal, ip);
            lookup.put(hostname, ip);
          }
        }
        catch (Exception e) {
          LOGGER.warn("Could not lookup hostname for {}: {}", ipForLocal, e.getMessage());
        }
      }
    }

    try {
      this.datasource = Paths.get(this.datasource).toString(); // convert forward & backslashes to same format
    }
    catch (Exception e) {
      LOGGER.warn("Invalid path: {} - {}", ds, e.getMessage());
    }
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
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

    if (datasource == null || datasource.isEmpty() || other.datasource == null || other.datasource.isEmpty()) {
      return false;
    }

    // 1: same? - step directly out
    if (file.equals(other.file) && datasource.equals(other.datasource)) {
      return true;
    }

    // 2: - check either matching IP or hostname
    if (file.equals(other.file) && ip != null && !ip.isEmpty() && ip.equals(other.ip)) {
      return true;
    }
    if (file.equals(other.file) && hostname != null && !hostname.isEmpty() && hostname.equalsIgnoreCase(other.hostname)) {
      return true;
    }

    // 3: file same, and at least the last folder of datasource
    Path ds = Paths.get(datasource);
    Path otherds = Paths.get(other.datasource);
    if (file.equals(other.file) && ds.getFileName().equals(otherds.getFileName())) {
      return true;
    }

    // 4: did not match? return false
    return false;
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
    return result.toString().toUpperCase(Locale.ROOT);
  }

}
