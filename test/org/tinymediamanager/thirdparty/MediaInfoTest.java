package org.tinymediamanager.thirdparty;

import java.io.File;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.tinymediamanager.core.MediaFile;

import com.sun.jna.Platform;

public class MediaInfoTest {

  private MediaInfo mi = null;

  @Before
  public void setUp() throws Exception {
    // set native dir (needs to be absolute)
    String path = MediaInfoTest.class.getClassLoader().getResource(".").getPath() + "native/";
    if (Platform.isWindows()) {
      path += "windows-";
    }
    else if (Platform.isLinux()) {
      path += "linux-";
    }
    else if (Platform.isMac()) {
      path += "mac-";
    }
    path += System.getProperty("os.arch");
    System.setProperty("jna.library.path", path);
    System.setProperty("jna.nosys", "true");
    System.out.println("Try to load mediainfo from: " + path);

    System.out.println(MediaInfo.version());

    mi = new MediaInfo();
  }

  @After
  public void tearDown() throws Exception {
    mi.close();
  }

  @Test
  public void displayVersion() {
    System.out.println(MediaInfo.staticOption("Info_Version"));
  }

  /**
   * displays all known parameters you could fetch
   */
  @Test
  public void displayInfoParameters() {
    System.out.println(MediaInfo.staticOption("Info_Parameters"));
  }

  @Test
  public void displayInfoCapacities() {
    System.out.println(MediaInfo.staticOption("Info_Capacities"));
  }

  /**
   * displays all supported codecs
   */
  @Test
  public void displayInfoCodecs() {
    System.out.println(MediaInfo.staticOption("Info_Codecs"));
  }

  @Test
  public void mediaFile() {
    MediaFile mf = new MediaFile();
    mf.setPath("/path/to/mediafile/");
    mf.setFilename("movie.avi");

    mf.gatherMediaInformation();

    System.out.println("res: " + mf.getVideoResolution());
    System.out.println("chan: " + mf.getAudioChannels());
    System.out.println("codec: " + mf.getAudioCodec());
    System.out.println("cont: " + mf.getContainerFormat());
    System.out.println("vid: " + mf.getExactVideoFormat());
    System.out.println("codec: " + mf.getVideoCodec());
    System.out.println("def: " + mf.getVideoDefinitionCategory());
    System.out.println("form: " + mf.getVideoFormat());
    System.out.println("ws?: " + mf.isWidescreen());
    System.out.println("ar: " + mf.getAspectRatio());
    System.out.println("width: " + mf.getVideoWidth());
    System.out.println("height: " + mf.getVideoHeight());
    // Map<String, String> i = getMediaInfo().snapshot(StreamKind.Video, 0);
  }

  /**
   * mediainfo direct example
   */
  @Test
  public void testDirect() throws Exception {
    String FileName = "Example.ogg";
    String To_Display = "";

    // Info about the library

    MediaInfo MI = new MediaInfo();

    To_Display += "\r\n\r\nOpen\r\n";
    if (MI.open(new File(FileName)))
      To_Display += "is OK\r\n";
    else
      To_Display += "has a problem\r\n";

    To_Display += "\r\n\r\nInform with Complete=false\r\n";
    MI.option("Complete", "");
    To_Display += MI.inform();

    To_Display += "\r\n\r\nInform with Complete=true\r\n";
    MI.option("Complete", "1");
    To_Display += MI.inform();

    To_Display += "\r\n\r\nCustom Inform\r\n";
    MI.option("Inform", "General;Example : FileSize=%FileSize%");
    To_Display += MI.inform();

    To_Display += "\r\n\r\nGetI with Stream=General and Parameter=2\r\n";
    To_Display += MI.get(MediaInfo.StreamKind.General, 0, 2, MediaInfo.InfoKind.Text);

    To_Display += "\r\n\r\nCount_Get with StreamKind=Stream_Audio\r\n";
    To_Display += MI.parameterCount(MediaInfo.StreamKind.Audio, -1);

    To_Display += "\r\n\r\nGet with Stream=General and Parameter=\"AudioCount\"\r\n";
    To_Display += MI.get(MediaInfo.StreamKind.General, 0, "AudioCount", MediaInfo.InfoKind.Text, MediaInfo.InfoKind.Name);

    To_Display += "\r\n\r\nGet with Stream=Audio and Parameter=\"StreamCount\"\r\n";
    To_Display += MI.get(MediaInfo.StreamKind.Audio, 0, "StreamCount", MediaInfo.InfoKind.Text, MediaInfo.InfoKind.Name);

    To_Display += "\r\n\r\nGet with Stream=General and Parameter=\"FileSize\"\r\n";
    To_Display += MI.get(MediaInfo.StreamKind.General, 0, "FileSize", MediaInfo.InfoKind.Text, MediaInfo.InfoKind.Name);

    To_Display += "\r\n\r\nClose\r\n";
    MI.close();

    System.out.println(To_Display);
  }
}
