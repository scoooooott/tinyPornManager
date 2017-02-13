package org.tinymediamanager.thirdparty.upnp;

import java.io.IOException;

import org.fourthline.cling.binding.LocalServiceBindingException;
import org.fourthline.cling.binding.annotations.AnnotationLocalServiceBinder;
import org.fourthline.cling.model.DefaultServiceManager;
import org.fourthline.cling.model.ValidationException;
import org.fourthline.cling.model.meta.DeviceDetails;
import org.fourthline.cling.model.meta.DeviceIdentity;
import org.fourthline.cling.model.meta.LocalDevice;
import org.fourthline.cling.model.meta.LocalService;
import org.fourthline.cling.model.meta.ManufacturerDetails;
import org.fourthline.cling.model.meta.ModelDetails;
import org.fourthline.cling.model.types.DeviceType;
import org.fourthline.cling.model.types.UDADeviceType;
import org.fourthline.cling.model.types.UDN;
import org.fourthline.cling.support.connectionmanager.ConnectionManagerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.ReleaseInfo;
import org.tinymediamanager.thirdparty.NetworkUtil;

public class MediaServer {
  private static final Logger LOGGER = LoggerFactory.getLogger(MediaServer.class);

  @SuppressWarnings({ "rawtypes", "unchecked" })
  public static LocalDevice createDevice() throws ValidationException, LocalServiceBindingException, IOException {
    DeviceIdentity identity = new DeviceIdentity(UDN.uniqueSystemIdentifier("tinyMediaManager"));
    DeviceType type = new UDADeviceType("MediaServer", 1);
    String hostname = NetworkUtil.getMachineHostname();
    if (hostname == null) {
      hostname = Upnp.IP;
    }
    DeviceDetails details = new DeviceDetails("tinyMediaManager (" + hostname + ")",
        new ManufacturerDetails("tinyMediaManager", "http://www.tinymediamanager.org/"),
        new ModelDetails("tinyMediaManager", "tinyMediaManager - Media Server", ReleaseInfo.getVersion()));

    LOGGER.info("Hello, i'm " + identity.getUdn().getIdentifierString());

    // Content Directory Service
    LocalService cds = new AnnotationLocalServiceBinder().read(ContentDirectoryService.class);
    cds.setManager(new DefaultServiceManager<ContentDirectoryService>(cds, ContentDirectoryService.class));

    // Connection Manager Service
    LocalService<ConnectionManagerService> cms = new AnnotationLocalServiceBinder().read(ConnectionManagerService.class);
    cms.setManager(new DefaultServiceManager<>(cms, ConnectionManagerService.class));

    return new LocalDevice(identity, type, details, new LocalService[] { cds, cms });
  }
}
