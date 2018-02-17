package org.tinymediamanager.thirdparty;

import java.util.List;

import org.fourthline.cling.model.meta.Device;
import org.junit.Test;
import org.tinymediamanager.thirdparty.upnp.Upnp;

public class ITUpnpTest {

  @Test
  public void detectKodi() throws InterruptedException {
    Upnp u = Upnp.getInstance();
    List<Device> kodis = u.getKodiDevices();
    for (Device device : kodis) {
      System.out.println("Found: " + device.getDetails().getPresentationURI().getHost() + ":" + device.getDetails().getPresentationURI().getPort());
    }
  }
}
