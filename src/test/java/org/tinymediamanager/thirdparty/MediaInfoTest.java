package org.tinymediamanager.thirdparty;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Paths;
import java.util.Date;
import java.util.Locale;

import org.apache.commons.lang3.StringUtils;
import org.junit.BeforeClass;
import org.junit.Test;
import org.tinymediamanager.BasicTest;
import org.tinymediamanager.core.MediaFileHelper;
import org.tinymediamanager.core.MediaFileType;
import org.tinymediamanager.core.entities.MediaFile;
import org.tinymediamanager.core.entities.MediaFileAudioStream;
import org.tinymediamanager.core.entities.MediaFileSubtitle;
import org.tinymediamanager.scraper.util.LanguageUtils;

public class MediaInfoTest extends BasicTest {

  @BeforeClass
  public static void setUp() throws Exception {
    MediaInfoUtils.loadMediaInfo();

  }

  @Test
  public void testAudiofiles() {
    MediaFile mf = new MediaFile(Paths.get("src/test/resources/samples/AAC-HE_LC_6ch.mka"));
    mf.gatherMediaInformation();
    assertThat(mf.getAudioCodec()).isEqualTo("AAC");
    assertThat(mf.getAudioChannels()).isEqualTo("6ch");

    mf = new MediaFile(Paths.get("src/test/resources/samples/AAC-HE_LC_8ch.mka"));
    mf.gatherMediaInformation();
    assertThat(mf.getAudioCodec()).isEqualTo("AAC");
    assertThat(mf.getAudioChannels()).isEqualTo("8ch");

    mf = new MediaFile(Paths.get("src/test/resources/samples/DTS-X.mka"));
    mf.gatherMediaInformation();
    assertThat(mf.getAudioCodec()).isEqualTo("DTS-X");
    assertThat(mf.getAudioChannels()).isEqualTo("8ch");
    assertThat(mf.getAudioLanguage()).isEqualTo("eng");

    mf = new MediaFile(Paths.get("src/test/resources/samples/DTS-ES_Discrete.ts"));
    mf.gatherMediaInformation();
    assertThat(mf.getAudioCodec()).isEqualTo("DTS-ES");
    assertThat(mf.getAudioChannels()).isEqualTo("7ch");

    mf = new MediaFile(Paths.get("src/test/resources/samples/DTSHD-HRA.mka"));
    mf.gatherMediaInformation();
    assertThat(mf.getAudioCodec()).isEqualTo("DTSHD-HRA");
    assertThat(mf.getAudioChannels()).isEqualTo("8ch");
    assertThat(mf.getAudioLanguage()).isEqualTo("eng");

    mf = new MediaFile(Paths.get("src/test/resources/samples/DTSHD-MA.mka"));
    mf.gatherMediaInformation();
    assertThat(mf.getAudioCodec()).isEqualTo("DTSHD-MA");
    assertThat(mf.getAudioChannels()).isEqualTo("8ch");
    assertThat(mf.getAudioLanguage()).isEqualTo("eng");

    mf = new MediaFile(Paths.get("src/test/resources/samples/DTS.mka"));
    mf.gatherMediaInformation();
    assertThat(mf.getAudioCodec()).isEqualTo("DTS");
    assertThat(mf.getAudioChannels()).isEqualTo("6ch");

    mf = new MediaFile(Paths.get("src/test/resources/samples/TrueHD.mka"));
    mf.gatherMediaInformation();
    assertThat(mf.getAudioCodec()).isEqualTo("TrueHD");
    assertThat(mf.getAudioChannels()).isEqualTo("8ch");
    assertThat(mf.getAudioLanguage()).isEqualTo("eng");

    mf = new MediaFile(Paths.get("src/test/resources/samples/TrueHD-Atmos.mka"));
    mf.gatherMediaInformation();
    assertThat(mf.getAudioCodec()).isEqualTo("Atmos");
    assertThat(mf.getAudioChannels()).isEqualTo("8ch");

    mf = new MediaFile(Paths.get("src/test/resources/samples/AC-3.mka"));
    mf.gatherMediaInformation();
    assertThat(mf.getAudioCodec()).isEqualTo("AC3");
    assertThat(mf.getAudioChannels()).isEqualTo("6ch");

    mf = new MediaFile(Paths.get("src/test/resources/samples/PCM.mka"));
    mf.gatherMediaInformation();
    assertThat(mf.getAudioCodec()).isEqualTo("PCM");
    assertThat(mf.getAudioChannels()).isEqualTo("6ch");

    mf = new MediaFile(Paths.get("src/test/resources/samples/E-AC3.ac3"));
    mf.gatherMediaInformation();
    assertThat(mf.getAudioCodec()).isEqualTo("EAC3");
    assertThat(mf.getAudioChannels()).isEqualTo("6ch");
  }

  // @Test
  public void testVideofiles() {
    MediaFile mf = new MediaFile(Paths.get("src/test/resources/samples/3D-FSBS.mkv"));
    mf.gatherMediaInformation();
    assertThat(mf.getVideo3DFormat()).isEqualTo(MediaFileHelper.VIDEO_3D_SBS);

    mf = new MediaFile(Paths.get("src/test/resources/samples/3D-HSBS.mkv"));
    mf.gatherMediaInformation();
    assertThat(mf.getVideo3DFormat()).isEqualTo(MediaFileHelper.VIDEO_3D_HSBS);

    mf = new MediaFile(Paths.get("src/test/resources/samples/3D-FTAB.mkv"));
    mf.gatherMediaInformation();
    assertThat(mf.getVideo3DFormat()).isEqualTo(MediaFileHelper.VIDEO_3D_TAB);

    mf = new MediaFile(Paths.get("src/test/resources/samples/3D-HTAB.mkv"));
    mf.gatherMediaInformation();
    assertThat(mf.getVideo3DFormat()).isEqualTo(MediaFileHelper.VIDEO_3D_HTAB);
  }

  @Test
  public void testIsoXml() {
    // DVD ISO - old format
    MediaFile mf = new MediaFile(Paths.get("src/test/resources/testmovies/MediainfoXML/MediaInfo.0.7.99.iso"));
    mf.gatherMediaInformation();

    assertThat(mf.getVideoWidth()).isEqualTo(720);
    assertThat(mf.getVideoHeight()).isEqualTo(576);
    assertThat(mf.getVideoCodec()).isEqualTo("MPEG");
    assertThat(mf.getDuration()).isEqualTo(5160);

    assertThat(mf.getAudioStreams().size()).isEqualTo(8);
    // first audio stream is AC-3 english/5.1
    MediaFileAudioStream audioStream = mf.getAudioStreams().get(0);
    assertThat(audioStream.getAudioChannels()).isEqualTo(6);
    assertThat(audioStream.getCodec()).isEqualTo("AC3");
    assertThat(audioStream.getLanguage()).isEqualTo("eng");

    assertThat(mf.getSubtitles().size()).isEqualTo(32);
    MediaFileSubtitle subtitle = mf.getSubtitles().get(0);
    assertThat(subtitle.getLanguage()).isEqualTo("eng");
    assertThat(subtitle.getCodec()).isEqualTo("RLE");

    // DVD ISO - new format
    mf = new MediaFile(Paths.get("src/test/resources/testmovies/MediainfoXML/MediaInfo.17.10.iso"));
    mf.gatherMediaInformation();

    assertThat(mf.getVideoWidth()).isEqualTo(720);
    assertThat(mf.getVideoHeight()).isEqualTo(576);
    assertThat(mf.getVideoCodec()).isEqualTo("MPEG-2");
    assertThat(mf.getDuration()).isEqualTo(5184);

    assertThat(mf.getAudioStreams().size()).isEqualTo(8);
    // first audio stream is AC-3 english/5.1
    audioStream = mf.getAudioStreams().get(0);
    assertThat(audioStream.getAudioChannels()).isEqualTo(6);
    assertThat(audioStream.getCodec()).isEqualTo("AC3");
    assertThat(audioStream.getLanguage()).isEqualTo("eng");

    assertThat(mf.getSubtitles().size()).isEqualTo(32);
    subtitle = mf.getSubtitles().get(0);
    assertThat(subtitle.getLanguage()).isEqualTo("eng");
    assertThat(subtitle.getCodec()).isEqualTo("RLE");

    // BD ISO
    mf = new MediaFile(Paths.get("src/test/resources/testmovies/MediainfoXML/MediaInfo-BD.iso"));
    mf.gatherMediaInformation();

    assertThat(mf.getVideoWidth()).isEqualTo(1920);
    assertThat(mf.getVideoHeight()).isEqualTo(1080);
    assertThat(mf.getVideoCodec()).isEqualTo("h264");
    assertThat(mf.getDuration()).isEqualTo(888);

    assertThat(mf.getAudioStreams().size()).isEqualTo(1);
    // first audio stream is AC-3 english/5.1
    audioStream = mf.getAudioStreams().get(0);
    assertThat(audioStream.getAudioChannels()).isEqualTo(6);
    assertThat(audioStream.getCodec()).isEqualTo("AC3");
    assertThat(audioStream.getLanguage()).isEqualTo("eng");

    assertThat(mf.getSubtitles().size()).isEqualTo(10);
    subtitle = mf.getSubtitles().get(0);
    assertThat(subtitle.getLanguage()).isEqualTo("deu");
    assertThat(subtitle.getCodec()).isEqualTo("PGS");

    // BD-MPLS ISO
    mf = new MediaFile(Paths.get("src/test/resources/testmovies/MediainfoXML/MediaInfo-BD-mpls.iso"));
    mf.gatherMediaInformation();

    assertThat(mf.getVideoWidth()).isEqualTo(1920);
    assertThat(mf.getVideoHeight()).isEqualTo(1080);
    assertThat(mf.getVideoCodec()).isEqualTo("h264");
    assertThat(mf.getDuration()).isEqualTo(5624);

    assertThat(mf.getAudioStreams().size()).isEqualTo(4);
    // first audio stream is AC-3 english/5.1
    audioStream = mf.getAudioStreams().get(0);
    assertThat(audioStream.getAudioChannels()).isEqualTo(8);
    assertThat(audioStream.getCodec()).isEqualTo("DTSHD-MA");
    assertThat(audioStream.getLanguage()).isEqualTo("eng");

    assertThat(mf.getSubtitles().size()).isEqualTo(7);
    subtitle = mf.getSubtitles().get(0);
    assertThat(subtitle.getLanguage()).isEqualTo("eng");
    assertThat(subtitle.getCodec()).isEqualTo("PGS");

    // BD ISO without size in xml
    mf = new MediaFile(Paths.get("src/test/resources/testmovies/MediainfoXML/MediaInfo-BD-nosize.iso"));
    mf.gatherMediaInformation();

    assertThat(mf.getVideoWidth()).isEqualTo(1920);
    assertThat(mf.getVideoHeight()).isEqualTo(1080);
    assertThat(mf.getVideoCodec()).isEqualTo("h264");
    assertThat(mf.getDuration()).isEqualTo(6626);

    assertThat(mf.getAudioStreams().size()).isEqualTo(1);
    // first audio stream is AC-3 english/5.1
    audioStream = mf.getAudioStreams().get(0);
    assertThat(audioStream.getAudioChannels()).isEqualTo(6);
    assertThat(audioStream.getCodec()).isEqualTo("DTSHD-MA");
    assertThat(audioStream.getLanguage()).isEqualTo("deu");

    assertThat(mf.getSubtitles().size()).isEqualTo(3);
    subtitle = mf.getSubtitles().get(0);
    assertThat(subtitle.getLanguage()).isEqualTo("deu");
    assertThat(subtitle.getCodec()).isEqualTo("PGS");

    // CD ISO
    mf = new MediaFile(Paths.get("src/test/resources/testmovies/MediainfoXML/MediaInfo-CD.iso"));
    mf.gatherMediaInformation();

    assertThat(mf.getVideoWidth()).isEqualTo(720);
    assertThat(mf.getVideoHeight()).isEqualTo(576);
    assertThat(mf.getVideoCodec()).isEqualTo("MPEG-2");
    assertThat(mf.getDuration()).isEqualTo(6627);

    assertThat(mf.getAudioStreams().size()).isEqualTo(1);
    // first audio stream is AC-3 english/5.1
    audioStream = mf.getAudioStreams().get(0);
    assertThat(audioStream.getAudioChannels()).isEqualTo(6);
    assertThat(audioStream.getCodec()).isEqualTo("AC3");
    assertThat(audioStream.getLanguage()).isEqualTo("deu");

    assertThat(mf.getSubtitles().size()).isEqualTo(0);

    // CD ISO without language information
    mf = new MediaFile(Paths.get("src/test/resources/testmovies/MediainfoXML/MediaInfo-CD-nolang.iso"));
    mf.gatherMediaInformation();

    assertThat(mf.getVideoWidth()).isEqualTo(720);
    assertThat(mf.getVideoHeight()).isEqualTo(480);
    assertThat(mf.getVideoCodec()).isEqualTo("MPEG-2");
    assertThat(mf.getDuration()).isEqualTo(120);

    assertThat(mf.getAudioStreams().size()).isEqualTo(1);
    // first audio stream is AC-3 english/5.1
    audioStream = mf.getAudioStreams().get(0);
    assertThat(audioStream.getAudioChannels()).isEqualTo(2);
    assertThat(audioStream.getCodec()).isEqualTo("MPEG Audio");
    assertThat(audioStream.getLanguage()).isEmpty();

    assertThat(mf.getSubtitles().size()).isEqualTo(0);

    // MKV FILE
    mf = new MediaFile(Paths.get("src/test/resources/testmovies/MediainfoXML/MediaInfoMKV.mkv"));
    mf.gatherMediaInformation();

    assertThat(mf.getVideoWidth()).isEqualTo(720);
    assertThat(mf.getVideoHeight()).isEqualTo(302);
    assertThat(mf.getVideoCodec()).isEqualTo("h264");
    assertThat(mf.getDuration()).isEqualTo(6412);

    assertThat(mf.getAudioStreams().size()).isEqualTo(1);
    // first audio stream is AC-3 english/5.1
    audioStream = mf.getAudioStreams().get(0);
    assertThat(audioStream.getAudioChannels()).isEqualTo(6);
    assertThat(audioStream.getCodec()).isEqualTo("AC3");
    assertThat(audioStream.getLanguage()).isEqualTo("deu");

    assertThat(mf.getSubtitles().size()).isEqualTo(0);
  }

  // @Test
  // public void displayVersion() {
  // System.out.println(MediaInfo.staticOption("Info_Version"));
  // }
  //
  // /**
  // * displays all known parameters you could fetch
  // */
  // @Test
  // public void displayInfoParameters() {
  // System.out.println(MediaInfo.staticOption("Info_Parameters"));
  // }
  //
  // @Test
  // public void displayInfoCapacities() {
  // System.out.println(MediaInfo.staticOption("Info_Capacities"));
  // }
  //
  // @Test
  // public void displayInfoOutputFormats() {
  // // since version 17.10
  // System.out.println(MediaInfo.staticOption("Info_OutputFormats"));
  // }
  //
  // /**
  // * displays all supported codecs
  // */
  // @Test
  // public void displayInfoCodecs() {
  // System.out.println(MediaInfo.staticOption("Info_Codecs"));
  // }

  @Test
  public void mediaFile() {
    setTraceLogging();

    MediaFile mf = new MediaFile(Paths.get("src/test/resources/samples/h265.mp4"));
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
    System.out.println("hdr?: " + mf.getHdrFormat());

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
    String FileName = "src/test/resources/samples/DTS-X.mka";
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

  @Test
  public void testSubtitleLanguageDetection() throws Exception {
    compareSubtitle("en.srt", "eng");
    compareSubtitle("eng_sdh.srt", "eng");
    compareSubtitle("moviename.en.srt", "eng");
    compareSubtitle("moviename.en.forced.srt", "eng");
    compareSubtitle("moviename.eng.srt", "eng");
    compareSubtitle("moviename.german.srt", "deu");
    compareSubtitle("moviename.Deutsch.srt", "deu");
    compareSubtitle("moviename.eng_hi.srt", "hin"); // yeah not eng since the string ends with a valid language code :/
    compareSubtitle("moviename.eng_sdh.srt", "eng");
    compareSubtitle("movie.name.year.GERMAN.dTV.XViD.srt", "deu");
    compareSubtitle("movietitle.year.NLPS.XviD.DTS.3CD-WAF.German.waf.com.cn.hk.srt", "deu");
  }

  /**
   * Try to parse the language out of the filename. This happens (like in Kodi) to chop the filename into different chunks and search in the chunks
   * for possible language tags.<br />
   * To make this work flawless we need to chop out the "main" filename part (movie/episode video filename) and look into the rest. For this we need
   * to pass the basename of the main video file to this method too.<br />
   * This is only usable for audio and subtitle files
   *
   * @param mediaFile
   *          the {@link MediaFile} to work with
   * @param commonPart
   *          the common part of the filename which is shared with the video file
   */
  private static void gatherLanguageInformation(MediaFile mediaFile, String commonPart) {
    // FIXME: if finished, this should move to MediaFileHelper
    if (mediaFile.getType() != MediaFileType.SUBTITLE && mediaFile.getType() != MediaFileType.AUDIO) {
      return;
    }

    String filename = mediaFile.getFilename();
    String path = mediaFile.getPath();

    String shortname = mediaFile.getBasename().toLowerCase(Locale.ROOT);

    shortname = shortname.replace(commonPart, "");
    shortname = shortname.replaceAll("\\p{Punct}*forced", "");

    // split the shortname into chunks and search from the end to the beginning for the language
    String[] chunks = shortname.split("[ \\._\\-]");

    String language = "";
    for (int i = chunks.length - 1; i >= 0; i--) {
      language = LanguageUtils.parseLanguageFromString(chunks[i]);
      if (StringUtils.isNotBlank(language)) {
        break;
      }
    }

    if (mediaFile.getType() == MediaFileType.SUBTITLE) {
      MediaFileSubtitle sub = mediaFile.getSubtitles().get(0);
      if (StringUtils.isBlank(sub.getLanguage())) {
        sub.setLanguage(language);
      }
    }
    else if (mediaFile.getType() == MediaFileType.AUDIO) {
      MediaFileAudioStream audio = mediaFile.getAudioStreams().get(0);
      if (StringUtils.isBlank(audio.getLanguage())) {
        audio.setLanguage(language);
      }
    }
  }

  private void compareSubtitle(String filename, String expectedLanguage) throws Exception {
    MediaFile mf = new MediaFile(Paths.get("target/test-classes/subtitles/" + filename));
    mf.gatherMediaInformation();
    gatherLanguageInformation(mf, "moviename");
    assertThat(mf.getType()).isEqualTo(MediaFileType.SUBTITLE);
    assertThat(mf.getSubtitles()).isNotEmpty();
    assertThat(mf.getSubtitles().get(0).getLanguage()).isEqualTo(expectedLanguage);
  }

  // @Test
  // public void listDirectForAll() {
  // MediaInfo mi = new MediaInfo();
  //
  // DirectoryStream<Path> stream = null;
  // try {
  // stream = Files.newDirectoryStream(Paths.get("src/test/resources/samples"));
  // for (Path path : stream) {
  // if (mi.open(path)) {
  //
  // // https://github.com/MediaArea/MediaInfoLib/blob/master/Source/Resource/Text/Stream/Audio.csv
  // // Format;;;N YTY;;;Format used;;
  // // Format/String;;;Y NT;;;Format used + additional features
  // // Format/Info;;;Y NT;;;Info about the format;;
  // // Format_Commercial;;;N YT;;;Commercial name used by vendor for theses setings or Format field if there is no difference;;
  // // Format_Profile;;;Y YTY;;;Profile of the Format (old XML: 'Profile@Level' format; MIXML: 'Profile' only)
  // // Format_AdditionalFeatures;;;N YTY;;;Format features needed for fully supporting the content
  //
  // String ret = path + "\n";
  // ret += mi.get(MediaInfo.StreamKind.Audio, 0, "Format") + "\n";
  // ret += mi.get(MediaInfo.StreamKind.Audio, 0, "Format/String") + "\n";
  // ret += mi.get(MediaInfo.StreamKind.Audio, 0, "Format/Info") + "\n";
  // ret += mi.get(MediaInfo.StreamKind.Audio, 0, "Format_Commercial") + "\n";
  // ret += mi.get(MediaInfo.StreamKind.Audio, 0, "Format_Profile") + "\n";
  // ret += mi.get(MediaInfo.StreamKind.Audio, 0, "Format_AdditionalFeatures") + "\n";
  //
  // System.out.println(ret);
  // mi.close();
  // }
  // }
  // }
  // catch (Exception e) {
  // // TODO: handle exception
  // }
  // }
}
