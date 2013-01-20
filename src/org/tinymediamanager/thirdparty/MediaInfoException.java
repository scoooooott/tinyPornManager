package org.tinymediamanager.thirdparty;

import com.sun.jna.Platform;

public class MediaInfoException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  public MediaInfoException(LinkageError e) {
    this(String.format("Unable to load %d-bit native library 'mediainfo'", Platform.is64Bit() ? 64 : 32), e);
  }

  public MediaInfoException(String msg, Throwable e) {
    super(msg, e);
  }

}
