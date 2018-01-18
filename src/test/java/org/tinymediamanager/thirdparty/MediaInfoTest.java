package org.tinymediamanager.thirdparty;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

import java.nio.file.Paths;
import java.util.Date;

import org.junit.BeforeClass;
import org.junit.Test;
import org.tinymediamanager.BasicTest;
import org.tinymediamanager.core.entities.MediaFile;
import org.tinymediamanager.core.entities.MediaFileAudioStream;
import org.tinymediamanager.core.entities.MediaFileSubtitle;

public class MediaInfoTest extends BasicTest {

  @BeforeClass
  public static void setUp() throws Exception {
    MediaInfoUtils.loadMediaInfo();

  }

  @Test
  public void testIsoXml() {
    // DVD ISO - old format
    try {
      MediaFile mf = new MediaFile(Paths.get("src/test/resources/testmovies/MediainfoXML/MediaInfo.0.7.99.iso"));
      mf.gatherMediaInformation();

      assertThat(mf.getVideoWidth()).isEqualTo(720);
      assertThat(mf.getVideoHeight()).isEqualTo(576);
      assertThat(mf.getVideoCodec()).isEqualTo("MPEG");
      assertThat(mf.getDuration()).isEqualTo(5160);

      assertThat(mf.getAudioStreams().size()).isEqualTo(8);
      // first audio stream is AC-3 english/5.1
      MediaFileAudioStream audioStream = mf.getAudioStreams().get(0);
      assertThat(audioStream.getChannelsAsInt()).isEqualTo(6);
      assertThat(audioStream.getCodec()).isEqualTo("AC3");
      assertThat(audioStream.getLanguage()).isEqualTo("eng");

      assertThat(mf.getSubtitles().size()).isEqualTo(32);
      MediaFileSubtitle subtitle = mf.getSubtitles().get(0);
      assertThat(subtitle.getLanguage()).isEqualTo("eng");
      assertThat(subtitle.getCodec()).isEqualTo("RLE");
    }
    catch (Exception e) {
      fail(e.getMessage());
    }

    // DVD ISO - new format
    try {
      MediaFile mf = new MediaFile(Paths.get("src/test/resources/testmovies/MediainfoXML/MediaInfo.17.10.iso"));
      mf.gatherMediaInformation();

      assertThat(mf.getVideoWidth()).isEqualTo(720);
      assertThat(mf.getVideoHeight()).isEqualTo(576);
      assertThat(mf.getVideoCodec()).isEqualTo("MPEG");
      assertThat(mf.getDuration()).isEqualTo(5184);

      assertThat(mf.getAudioStreams().size()).isEqualTo(8);
      // first audio stream is AC-3 english/5.1
      MediaFileAudioStream audioStream = mf.getAudioStreams().get(0);
      assertThat(audioStream.getChannelsAsInt()).isEqualTo(6);
      assertThat(audioStream.getCodec()).isEqualTo("AC3");
      assertThat(audioStream.getLanguage()).isEqualTo("eng");

      assertThat(mf.getSubtitles().size()).isEqualTo(32);
      MediaFileSubtitle subtitle = mf.getSubtitles().get(0);
      assertThat(subtitle.getLanguage()).isEqualTo("eng");
      assertThat(subtitle.getCodec()).isEqualTo("RLE");
    }
    catch (Exception e) {
      fail(e.getMessage());
    }

    // BD ISO
    try {
      MediaFile mf = new MediaFile(Paths.get("src/test/resources/testmovies/MediainfoXML/MediaInfo-BD.iso"));
      mf.gatherMediaInformation();

      assertThat(mf.getVideoWidth()).isEqualTo(1920);
      assertThat(mf.getVideoHeight()).isEqualTo(1080);
      assertThat(mf.getVideoCodec()).isEqualTo("h264");
      assertThat(mf.getDuration()).isEqualTo(888);

      assertThat(mf.getAudioStreams().size()).isEqualTo(1);
      // first audio stream is AC-3 english/5.1
      MediaFileAudioStream audioStream = mf.getAudioStreams().get(0);
      assertThat(audioStream.getChannelsAsInt()).isEqualTo(6);
      assertThat(audioStream.getCodec()).isEqualTo("AC3");
      assertThat(audioStream.getLanguage()).isEqualTo("eng");

      assertThat(mf.getSubtitles().size()).isEqualTo(10);
      MediaFileSubtitle subtitle = mf.getSubtitles().get(0);
      assertThat(subtitle.getLanguage()).isEqualTo("deu");
      assertThat(subtitle.getCodec()).isEqualTo("PGS");
    }
    catch (Exception e) {
      fail(e.getMessage());
    }

    // BD-MPLS ISO
    try {
      MediaFile mf = new MediaFile(Paths.get("src/test/resources/testmovies/MediainfoXML/MediaInfo-BD-mpls.iso"));
      mf.gatherMediaInformation();

      assertThat(mf.getVideoWidth()).isEqualTo(1920);
      assertThat(mf.getVideoHeight()).isEqualTo(1080);
      assertThat(mf.getVideoCodec()).isEqualTo("h264");
      assertThat(mf.getDuration()).isEqualTo(5624);

      assertThat(mf.getAudioStreams().size()).isEqualTo(4);
      // first audio stream is AC-3 english/5.1
      MediaFileAudioStream audioStream = mf.getAudioStreams().get(0);
      assertThat(audioStream.getChannelsAsInt()).isEqualTo(8);
      assertThat(audioStream.getCodec()).isEqualTo("DTSHD");
      assertThat(audioStream.getLanguage()).isEqualTo("eng");

      assertThat(mf.getSubtitles().size()).isEqualTo(7);
      MediaFileSubtitle subtitle = mf.getSubtitles().get(0);
      assertThat(subtitle.getLanguage()).isEqualTo("eng");
      assertThat(subtitle.getCodec()).isEqualTo("PGS");
    }
    catch (Exception e) {
      fail(e.getMessage());
    }

    // BD ISO without size in xml
    try {
      MediaFile mf = new MediaFile(Paths.get("src/test/resources/testmovies/MediainfoXML/MediaInfo-BD-nosize.iso"));
      mf.gatherMediaInformation();

      assertThat(mf.getVideoWidth()).isEqualTo(1920);
      assertThat(mf.getVideoHeight()).isEqualTo(1080);
      assertThat(mf.getVideoCodec()).isEqualTo("h264");
      assertThat(mf.getDuration()).isEqualTo(6626);

      assertThat(mf.getAudioStreams().size()).isEqualTo(1);
      // first audio stream is AC-3 english/5.1
      MediaFileAudioStream audioStream = mf.getAudioStreams().get(0);
      assertThat(audioStream.getChannelsAsInt()).isEqualTo(6);
      assertThat(audioStream.getCodec()).isEqualTo("DTSHD");
      assertThat(audioStream.getLanguage()).isEqualTo("deu");

      assertThat(mf.getSubtitles().size()).isEqualTo(3);
      MediaFileSubtitle subtitle = mf.getSubtitles().get(0);
      assertThat(subtitle.getLanguage()).isEqualTo("deu");
      assertThat(subtitle.getCodec()).isEqualTo("PGS");
    }
    catch (Exception e) {
      fail(e.getMessage());
    }

    // CD ISO
    try {
      MediaFile mf = new MediaFile(Paths.get("src/test/resources/testmovies/MediainfoXML/MediaInfo-CD.iso"));
      mf.gatherMediaInformation();

      assertThat(mf.getVideoWidth()).isEqualTo(720);
      assertThat(mf.getVideoHeight()).isEqualTo(576);
      assertThat(mf.getVideoCodec()).isEqualTo("MPEG-2");
      assertThat(mf.getDuration()).isEqualTo(6627);

      assertThat(mf.getAudioStreams().size()).isEqualTo(1);
      // first audio stream is AC-3 english/5.1
      MediaFileAudioStream audioStream = mf.getAudioStreams().get(0);
      assertThat(audioStream.getChannelsAsInt()).isEqualTo(6);
      assertThat(audioStream.getCodec()).isEqualTo("AC3");
      assertThat(audioStream.getLanguage()).isEqualTo("deu");

      assertThat(mf.getSubtitles().size()).isEqualTo(0);
    }
    catch (Exception e) {
      fail(e.getMessage());
    }

    // CD ISO without language information
    try {
      MediaFile mf = new MediaFile(Paths.get("src/test/resources/testmovies/MediainfoXML/MediaInfo-CD-nolang.iso"));
      mf.gatherMediaInformation();

      assertThat(mf.getVideoWidth()).isEqualTo(720);
      assertThat(mf.getVideoHeight()).isEqualTo(480);
      assertThat(mf.getVideoCodec()).isEqualTo("MPEG-2");
      assertThat(mf.getDuration()).isEqualTo(120);

      assertThat(mf.getAudioStreams().size()).isEqualTo(1);
      // first audio stream is AC-3 english/5.1
      MediaFileAudioStream audioStream = mf.getAudioStreams().get(0);
      assertThat(audioStream.getChannelsAsInt()).isEqualTo(2);
      assertThat(audioStream.getCodec()).isEqualTo("MPEG1 Audio");
      assertThat(audioStream.getLanguage()).isEmpty();

      assertThat(mf.getSubtitles().size()).isEqualTo(0);
    }
    catch (Exception e) {
      fail(e.getMessage());
    }

    // MKV ISO
    try {
      MediaFile mf = new MediaFile(Paths.get("src/test/resources/testmovies/MediainfoXML/MediaInfo-MKV.iso"));
      mf.gatherMediaInformation();

      assertThat(mf.getVideoWidth()).isEqualTo(720);
      assertThat(mf.getVideoHeight()).isEqualTo(302);
      assertThat(mf.getVideoCodec()).isEqualTo("h264");
      assertThat(mf.getDuration()).isEqualTo(6412);

      assertThat(mf.getAudioStreams().size()).isEqualTo(1);
      // first audio stream is AC-3 english/5.1
      MediaFileAudioStream audioStream = mf.getAudioStreams().get(0);
      assertThat(audioStream.getChannelsAsInt()).isEqualTo(6);
      assertThat(audioStream.getCodec()).isEqualTo("AC3");
      assertThat(audioStream.getLanguage()).isEqualTo("deu");

      assertThat(mf.getSubtitles().size()).isEqualTo(0);
    }
    catch (Exception e) {
      fail(e.getMessage());
    }
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

  @Test
  public void displayInfoOutputFormats() {
    // since version 17.10
    System.out.println(MediaInfo.staticOption("Info_OutputFormats"));
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

    // MediaFile mf = new MediaFile(Paths.get("src/test/resources/testmovies/MediainfoXML/MediaInfo-BD-mpls.iso"));
    MediaFile mf = new MediaFile(Paths.get("src/test/resources/testmovies/MediainfoXML/MediaInfo.17.10.iso"));
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
    System.out.println("FPS: " + mf.getFrameRate());
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
