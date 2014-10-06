package org.tinymediamanager.thirdparty;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.xml.sax.SAXException;

/**
 * This class detects a running MediaServer via UPNP<br>
 * we can get the IP address out of this
 * 
 * @author Myron Boyle
 */
public class Upnp {

  // @formatter:off
  // http://upnp.org/sdcps-and-certification/standards/sdcps/
  private final static String DISCOVER_MESSAGE = "M-SEARCH * HTTP/1.1\r\n" + 
                                                 "MAN: \"ssdp:discover\"\r\n" + 
                                                 "MX: 3\r\n" +
                                                 "HOST: 239.255.255.250:1900\r\n" +
                                                 // "ST: ssdp:all\r\n" +
                                                 // "ST: upnp:rootdevice\r\n" +
                                                 "ST: urn:schemas-upnp-org:device:MediaServer:1\r\n" + 
                                                 "\r\n";
  // @formatter:on

  /**
   * MAIN for testing
   */
  public static void main(String[] args) throws Exception {
    Upnp client = new Upnp();
    List<UpnpDevice> upnpDevices = client.getUpnpDevices();
    for (UpnpDevice d : upnpDevices) {
      System.out.println(d);
    }

  }

  /**
   * UPNP/SSDP client to demonstrate the usage of UDP multicast sockets.
   * 
   * @throws IOException
   */
  public List<UpnpDevice> getUpnpDevices() throws IOException {
    List<UpnpDevice> upnpDevices = new ArrayList<UpnpDevice>();
    try {
      InetAddress multicastAddress = InetAddress.getByName("239.255.255.250");
      // InetSocketAddress multicastAddress = new InetSocketAddress("239.255.255.250", 1900);
      // InetAddress local = getLocalInetAddresses(true, true, true).get(0);

      // multicast address for SSDP
      final int port = 1900; // standard port for SSDP
      MulticastSocket socket = new MulticastSocket();
      socket.setReuseAddress(true);
      socket.setTimeToLive(2);
      socket.setSoTimeout(3000);
      // socket.setReceiveBufferSize(32768);
      // socket.joinGroup(multicastAddress, NetworkInterface.getByInetAddress(local));
      socket.joinGroup(multicastAddress);

      // send discover
      byte[] txbuf = DISCOVER_MESSAGE.getBytes("UTF-8");
      DatagramPacket hi = new DatagramPacket(txbuf, txbuf.length, multicastAddress, port);
      socket.send(hi);
      System.out.println("SSDP discover sent");

      do {
        byte[] rxbuf = new byte[8192];
        DatagramPacket packet = new DatagramPacket(rxbuf, rxbuf.length);
        socket.receive(packet);

        // dumpPacket(packet);
        UpnpDevice dev = parseMSearchReplay(packet.getData());
        dev.setLocalAddress(packet.getAddress());
        try {
          dev.loadDescription();
        }
        catch (SAXException e) {
          System.out.println("Error loading description");
        }
        if (!upnpDevices.contains(dev)) {
          upnpDevices.add(dev);
        }
      } while (true); // should leave loop by SocketTimeoutException

    }
    catch (SocketTimeoutException e) {
      System.out.println("Timeout");
    }
    return upnpDevices;
  }

  /**
   * Parses the reply from UPnP devices
   * 
   * @param reply
   *          the raw bytes received as a reply
   * @return the representation of a GatewayDevice
   */
  private UpnpDevice parseMSearchReplay(byte[] reply) {

    UpnpDevice device = new UpnpDevice();

    String replyString = new String(reply);
    StringTokenizer st = new StringTokenizer(replyString, "\n");

    while (st.hasMoreTokens()) {
      String line = st.nextToken().trim();

      if (line.isEmpty())
        continue;

      if (line.startsWith("HTTP/1."))
        continue;

      String key = line.substring(0, line.indexOf(':'));
      String value = line.length() > key.length() + 1 ? line.substring(key.length() + 1) : null;

      key = key.trim();
      if (value != null) {
        value = value.trim();
      }

      if (key.compareToIgnoreCase("location") == 0) {
        device.setLocation(value);

      }
      else if (key.compareToIgnoreCase("st") == 0) { // Search Target
        device.setSt(value);
      }
    }

    return device;
  }
}