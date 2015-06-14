package org.tinymediamanager.thirdparty;

//Copyright (c) 2011, Maxim Veksler
//http://stackoverflow.com/questions/5034321/understanding-jdk-1-6-on-ubuntu-linux-ipv6-output-handling
//All rights reserved.
//
//Redistribution and use in source and binary forms, with or without
//modification, are permitted provided that the following conditions are met:
//    * Redistributions of source code must retain the above copyright
//      notice, this list of conditions and the following disclaimer.
//    * Redistributions in binary form must reproduce the above copyright
//      notice, this list of conditions and the following disclaimer in the
//      documentation and/or other materials provided with the distribution.
//    * Neither the name of the <organization> nor the
//      names of its contributors may be used to endorse or promote products
//      derived from this software without specific prior written permission.
//
//THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
//ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
//WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
//DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
//DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
//(INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
//LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
//ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
//(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
//SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Hostname and IP address info, based on JDK6 NetworkInterface
 * 
 * @author Maxim Veksler <maxim@vekslers.org>
 */
public class NetworkUtil {
  private static Logger log = LoggerFactory.getLogger(NetworkUtil.class);

  public static void main(String[] args) {
    System.out.println("MAC: " + NetworkUtil.getMachineMac());
    System.out.println("HOSTNAME: " + NetworkUtil.getMachineHostname());
    System.out.println("IP: " + NetworkUtil.getMachineIPAddress());
    System.out.println("HOSTNAME ipv6: " + NetworkUtil.getMachineIPv6Hostname());
    System.out.println("IP ipv6: " + NetworkUtil.getMachineIPv6Address());
  }

  /**
   * Get the MAC address of the remote IP (if on local LAN).
   * 
   * @param hostnameOrIP
   *          The target IP or Hostname (if you have DNS configured).
   * 
   * @return MAC address if IP in local LAN, null if not.
   */
  public static String getRemoteHostMac(String hostnameOrIP) {
    try {
      InetAddress address = InetAddress.getByName(hostnameOrIP);
      NetworkInterface networkInterface = NetworkInterface.getByInetAddress(address);
      return obtainMACFromAddress(networkInterface);
    }
    catch (Exception e) {
      if (log.isDebugEnabled()) {
        log.debug("Failed to obtain MAC address for address " + hostnameOrIP, e);
      }
    }

    // Means we had a failure.
    return null;
  }

  /**
   * Get the machine address of the machine we are currently running on.
   * 
   * @return something like 08-00-27-DC-4A-9E or null if can't obtain mac
   */
  public static String getMachineMac() {
    try {
      return obtainMACFromAddress(getNonLoopbackNetworkInterface());
    }
    catch (Exception e) {
      if (log.isDebugEnabled()) {
        log.debug("Failed to obtain MAC address for localhost", e);
      }
    }

    return null;
  }

  /**
   * Get machine hostname, based on IPv4 configurations.
   * 
   * @return String representing FQDN or null if can't find hostname
   */
  public static String getMachineHostname() {
    try {
      NetworkInterface networkInterface = getNonLoopbackNetworkInterface();
      Inet4Address address = getInet4Address(networkInterface);
      if (address != null)
        return address.getCanonicalHostName();
    }
    catch (Exception e) {
      if (log.isDebugEnabled()) {
        log.debug("Failed to obtain MachineHostname", e);
      }
    }

    return null;
  }

  /**
   * Get machine hostname, based on IPv6 configurations.
   * 
   * @return String representing FQDN or null if can't find hostname
   */
  public static String getMachineIPv6Hostname() {
    try {
      NetworkInterface networkInterface = getNonLoopbackNetworkInterface();
      Inet6Address address = getInet6Address(networkInterface);
      if (address != null)
        return address.getCanonicalHostName();
    }
    catch (Exception e) {
      if (log.isDebugEnabled()) {
        log.debug("Failed to obtain IPv6Hostname", e);
      }
    }

    return null;
  }

  /**
   * Get machine IP, based on IPv4 configurations.
   * 
   * @return String representing IP or null if can't find properly configured interface
   */
  public static String getMachineIPAddress() {
    try {
      NetworkInterface networkInterface = getNonLoopbackNetworkInterface();
      Inet4Address address = getInet4Address(networkInterface);
      if (address != null)
        return address.getHostAddress();
    }
    catch (Exception e) {
      if (log.isDebugEnabled()) {
        log.debug("Failed to obtain MachineIPAddress", e);
      }
    }

    return null;
  }

  /**
   * Get machine IP, based on IPv6 configurations.
   * 
   * @return String representing IP or null if can't find properly configured interface
   */
  public static String getMachineIPv6Address() {
    try {
      NetworkInterface networkInterface = getNonLoopbackNetworkInterface();
      Inet6Address address = getInet6Address(networkInterface);
      if (address != null)
        return address.getHostAddress();
    }
    catch (Exception e) {
      if (log.isDebugEnabled()) {
        log.debug("Failed to obtain MachineIPv6Address", e);
      }
    }

    return null;
  }

  /*
   * ######################## Helper private functions
   */

  private static String obtainMACFromAddress(NetworkInterface networkInterface) throws SocketException {
    if (networkInterface != null) {
      byte[] mac = networkInterface.getHardwareAddress();
      if (mac == null)
        throw new Error("Failed to obtain mac address from interface: " + networkInterface.getDisplayName());

      StringBuilder stringBuilder = new StringBuilder(17);
      /*
       * Extract each array of mac address and convert it to hexa with the following format 08-00-27-DC-4A-9E.
       */
      for (int i = 0; i < mac.length; i++) {
        stringBuilder.append(String.format("%02X%s", mac[i], (i < mac.length - 1) ? "-" : ""));
      }

      return stringBuilder.toString();
    }

    return null;
  }

  private static Inet4Address getInet4Address(NetworkInterface networkInterface) {
    if (networkInterface != null) {
      Enumeration<InetAddress> NICAddresses = networkInterface.getInetAddresses();
      while (NICAddresses.hasMoreElements()) {
        InetAddress address = NICAddresses.nextElement();

        if (address instanceof Inet4Address)
          return (Inet4Address) address;
      }
    }

    return null;
  }

  private static Inet6Address getInet6Address(NetworkInterface networkInterface) {
    if (networkInterface != null) {
      Enumeration<InetAddress> NICAddresses = networkInterface.getInetAddresses();
      while (NICAddresses.hasMoreElements()) {
        InetAddress address = NICAddresses.nextElement();

        if (address instanceof Inet6Address)
          return (Inet6Address) address;
      }
    }

    return null;
  }

  private static NetworkInterface getNonLoopbackNetworkInterface() throws SocketException {
    // We need to iterate over all NIC's machine has because stupid ubuntu does not assign
    // MAC address to default loopback interface...
    Enumeration<NetworkInterface> b = NetworkInterface.getNetworkInterfaces();
    while (b.hasMoreElements()) {
      NetworkInterface networkInterface = b.nextElement();
      Enumeration<InetAddress> inetAddresses = networkInterface.getInetAddresses();
      while (inetAddresses.hasMoreElements()) {
        InetAddress address = inetAddresses.nextElement();
        if (!address.isLoopbackAddress())
          return networkInterface;
      }
    }

    // Means we haven't found any non loopback interfaces. Bummer, return empty handed.
    return null;
  }

}