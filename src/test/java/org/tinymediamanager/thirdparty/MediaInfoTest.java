package org.tinymediamanager.thirdparty;

import java.nio.file.Paths;
import java.util.Date;

import org.junit.BeforeClass;
import org.junit.Test;
import org.tinymediamanager.BasicTest;
import org.tinymediamanager.core.entities.MediaFile;

public class MediaInfoTest extends BasicTest {

  @BeforeClass
  public static void setUp() throws Exception {
    MediaInfoUtils.loadMediaInfo();

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
    setTraceLogging();

    MediaFile mf = new MediaFile(Paths.get("src/test/resources/testmovies/MediainfoXML/MediaInfo-BD-mpls.iso"));
    mf.gatherMediaInformation();

    System.out.println("----------------------");
    System.out.println("filesize: " + mf.getFilesize());
    System.out.println("filedate: " + new Date(mf.getFiledate()));
    System.out.println("container: " + mf.getContainerFormat());
    System.out.println("runtime: " + mf.getDurationHHMMSS());

    System.out.println("----------------------");
    System.out.println("vres: " + mf.getVideoResolution());
    System.out.println("vwidth: " + mf.getVideoWidth());
    System.out.println("vheight: " + mf.getVideoHeight());
    System.out.println("vformat: " + mf.getVideoFormat());
    System.out.println("vid: " + mf.getExactVideoFormat());
    System.out.println("vcodec: " + mf.getVideoCodec());
    System.out.println("vdef: " + mf.getVideoDefinitionCategory());
    System.out.println("var: " + mf.getAspectRatio());
    System.out.println("ws?: " + mf.isWidescreen());

    System.out.println("----------------------");
    System.out.println("acodec: " + mf.getAudioCodec());
    System.out.println("alang: " + mf.getAudioLanguage());
    System.out.println("achan: " + mf.getAudioChannels());

    System.out.println("----------------------");
    System.out.println("subs: " + mf.getSubtitlesAsString());
  }

  /**
   * mediainfo direct example
   */
  @Test
  public void testDirect() throws Exception {
    String FileName = "";
    String To_Display = "";

    // Info about the library

    MediaInfo MI = new MediaInfo();

    To_Display += "\r\n\r\nOpen\r\n";
    if (MI.open(Paths.get(FileName)))
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

    To_Display += "\r\n\r\nGet with Stream=General and Parameter=\"File_Modified_Date_Local\"\r\n";
    To_Display += MI.get(MediaInfo.StreamKind.General, 0, "File_Modified_Date_Local", MediaInfo.InfoKind.Text, MediaInfo.InfoKind.Name);

    To_Display += "\r\n\r\nClose\r\n";
    MI.close();

    System.out.println(To_Display);
  }
}
