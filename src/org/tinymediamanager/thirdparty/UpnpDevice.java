/* 
 *              weupnp - Trivial upnp java library 
 *
 * Copyright (C) 2008 Alessandro Bahgat Shehata, Daniele Castagna
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 * 
 * Alessandro Bahgat Shehata - ale dot bahgat at gmail dot com
 * Daniele Castagna - daniele dot castagna at gmail dot com
 * 
 */
package org.tinymediamanager.thirdparty;

import java.io.IOException;
import java.net.InetAddress;
import java.net.URL;
import java.net.URLConnection;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

public class UpnpDevice {

  /**
   * Receive timeout when requesting data from device
   */
  private static final int HTTP_RECEIVE_TIMEOUT = 7000;

  private String           st;
  private String           location;
  private String           serviceType;
  private String           serviceTypeCIF;
  private String           urlBase;
  private String           controlURL;
  private String           controlURLCIF;
  private String           eventSubURL;
  private String           eventSubURLCIF;
  private String           sCPDURL;
  private String           sCPDURLCIF;
  private String           deviceType;
  private String           deviceTypeCIF;

  // description data

  /**
   * The friendly (human readable) name associated with this device
   */
  private String           friendlyName;

  /**
   * The device manufacturer name
   */
  private String           manufacturer;

  /**
   * The model description as a string
   */
  private String           modelDescription;

  /**
   * The URL that can be used to access the IGD interface
   */
  private String           presentationURL;

  /**
   * The address used to reach this machine from the GatewayDevice
   */
  private InetAddress      localAddress;

  /**
   * The model number (used by the manufacturer to identify the product)
   */
  private String           modelNumber;

  /**
   * The model name
   */
  private String           modelName;

  /**
   * Creates a new instance of GatewayDevice
   */
  public UpnpDevice() {
  }

  /**
   * Retrieves the properties and description of the UpnpDevice.
   * <p/>
   * Connects to the device's {@link #location} and parses the response to populate the fields of this class
   * 
   * @throws SAXException
   *           if an error occurs while parsing the request
   * @throws IOException
   *           on communication errors
   * @see org.bitlet.weupnp.GatewayDeviceHandler
   */
  public void loadDescription() throws SAXException, IOException {

    URLConnection urlConn = new URL(getLocation()).openConnection();
    urlConn.setReadTimeout(HTTP_RECEIVE_TIMEOUT);

    XMLReader parser = XMLReaderFactory.createXMLReader();
    parser.setContentHandler(new UpnpDeviceHandler(this));
    parser.parse(new InputSource(urlConn.getInputStream()));

    /* fix urls */
    String ipConDescURL;
    if (urlBase != null && urlBase.trim().length() > 0) {
      ipConDescURL = urlBase;
    }
    else {
      ipConDescURL = location;
    }

    int lastSlashIndex = ipConDescURL.indexOf('/', 7);
    if (lastSlashIndex > 0) {
      ipConDescURL = ipConDescURL.substring(0, lastSlashIndex);
    }

    sCPDURL = copyOrCatUrl(ipConDescURL, sCPDURL);
    controlURL = copyOrCatUrl(ipConDescURL, controlURL);
    controlURLCIF = copyOrCatUrl(ipConDescURL, controlURLCIF);
    presentationURL = copyOrCatUrl(ipConDescURL, presentationURL);
  }

  /**
   * <p>
   * Uses <code>ReflectionToStringBuilder</code> to generate a <code>toString</code> for the specified object.
   * </p>
   * 
   * @return the String result
   * @see ReflectionToStringBuilder#toString(Object)
   */
  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
  }

  /**
   * Gets the local address to connect the gateway through
   * 
   * @return the {@link #localAddress}
   */
  public InetAddress getLocalAddress() {
    return localAddress;
  }

  /**
   * Sets the {@link #localAddress}
   * 
   * @param localAddress
   *          the address to set
   */
  public void setLocalAddress(InetAddress localAddress) {
    this.localAddress = localAddress;
  }

  public String getSt() {
    return st;
  }

  public void setSt(String st) {
    this.st = st;
  }

  public String getLocation() {
    return location;
  }

  public void setLocation(String location) {
    this.location = location;
  }

  public String getServiceType() {
    return serviceType;
  }

  public void setServiceType(String serviceType) {
    this.serviceType = serviceType;
  }

  public String getServiceTypeCIF() {
    return serviceTypeCIF;
  }

  public void setServiceTypeCIF(String serviceTypeCIF) {
    this.serviceTypeCIF = serviceTypeCIF;
  }

  public String getControlURL() {
    return controlURL;
  }

  public void setControlURL(String controlURL) {
    this.controlURL = controlURL;
  }

  public String getControlURLCIF() {
    return controlURLCIF;
  }

  public void setControlURLCIF(String controlURLCIF) {
    this.controlURLCIF = controlURLCIF;
  }

  public String getEventSubURL() {
    return eventSubURL;
  }

  public void setEventSubURL(String eventSubURL) {
    this.eventSubURL = eventSubURL;
  }

  public String getEventSubURLCIF() {
    return eventSubURLCIF;
  }

  public void setEventSubURLCIF(String eventSubURLCIF) {
    this.eventSubURLCIF = eventSubURLCIF;
  }

  public String getSCPDURL() {
    return sCPDURL;
  }

  public void setSCPDURL(String sCPDURL) {
    this.sCPDURL = sCPDURL;
  }

  public String getSCPDURLCIF() {
    return sCPDURLCIF;
  }

  public void setSCPDURLCIF(String sCPDURLCIF) {
    this.sCPDURLCIF = sCPDURLCIF;
  }

  public String getDeviceType() {
    return deviceType;
  }

  public void setDeviceType(String deviceType) {
    this.deviceType = deviceType;
  }

  public String getDeviceTypeCIF() {
    return deviceTypeCIF;
  }

  public void setDeviceTypeCIF(String deviceTypeCIF) {
    this.deviceTypeCIF = deviceTypeCIF;
  }

  public String getURLBase() {
    return urlBase;
  }

  public void setURLBase(String uRLBase) {
    this.urlBase = uRLBase;
  }

  public String getFriendlyName() {
    return friendlyName;
  }

  public void setFriendlyName(String friendlyName) {
    this.friendlyName = friendlyName;
  }

  public String getManufacturer() {
    return manufacturer;
  }

  public void setManufacturer(String manufacturer) {
    this.manufacturer = manufacturer;
  }

  public String getModelDescription() {
    return modelDescription;
  }

  public void setModelDescription(String modelDescription) {
    this.modelDescription = modelDescription;
  }

  public String getPresentationURL() {
    return presentationURL;
  }

  public void setPresentationURL(String presentationURL) {
    this.presentationURL = presentationURL;
  }

  public String getModelName() {
    return modelName;
  }

  public void setModelName(String modelName) {
    this.modelName = modelName;
  }

  public String getModelNumber() {
    return modelNumber;
  }

  public void setModelNumber(String modelNumber) {
    this.modelNumber = modelNumber;
  }

  // private methods
  private String copyOrCatUrl(String dst, String src) {
    if (src != null) {
      if (src.startsWith("http://")) {
        dst = src;
      }
      else {
        if (!src.startsWith("/")) {
          dst += "/";
        }
        dst += src;
      }
    }
    return dst;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((localAddress == null) ? 0 : localAddress.hashCode());
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
    UpnpDevice other = (UpnpDevice) obj;
    if (localAddress == null) {
      if (other.localAddress != null)
        return false;
    }
    else if (!localAddress.equals(other.localAddress))
      return false;
    return true;
  }

}
