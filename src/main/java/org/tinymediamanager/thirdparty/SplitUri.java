package org.tinymediamanager.thirdparty;

import java.io.UnsupportedEncodingException;
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
  private static final Logger LOGGER   = LoggerFactory.getLogger(SplitUri.class);

  public String               label    = "";
  public String               type     = "";
  public String               ip       = "";
  public String               hostname = "";
  public String               file     = "";

  private Map<String, String> lookup   = new HashMap<>();

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
        LOGGER.warn(e2.getMessage());
      }
    }

    if (u != null && !StringUtils.isBlank(u.getHost())) {
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
        }
      }
    }
    else {
      this.type = "LOCAL";
      this.file = ds;
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
        }
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

    if (file == null || file.isEmpty() || other.file == null || other.file.isEmpty()) {
      return false;
    }

    // 1: same? - step directly out
    if (file.equals(other.file)) {
      return true;
    }

    // 2: at least filename AND parent folder match
    Path p1 = Paths.get(file);
    String filename1 = p1.getFileName() == null ? "nofile" : p1.getFileName().toString();
    String parent1 = p1.getParent() == null || p1.getParent().getFileName() == null ? "noparent" : p1.getParent().getFileName().toString();
    LOGGER.debug("P1 parent {}, file {}", parent1, filename1);

    Path p2 = Paths.get(other.file);
    String filename2 = p2.getFileName() == null ? "nofile" : p2.getFileName().toString();
    String parent2 = p2.getParent() == null || p2.getParent().getFileName() == null ? "noparent" : p2.getParent().getFileName().toString();
    LOGGER.debug("P2 parent {}, file {}", parent2, filename2);

    if (parent1.equals(parent2) && filename1.equals(filename2)) {

      // 2: - check either matching IP or hostname
      if (ip != null && !ip.isEmpty() && ip.equals(other.ip)) {
        return true;
      }
      if (hostname != null && !hostname.isEmpty() && hostname.equalsIgnoreCase(other.hostname)) {
        return true;
      }
    }

    // 3: did not match? return false
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
