package org.tinymediamanager.thirdparty;

import java.io.FileInputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;
import org.tinymediamanager.thirdparty.MediaInfo.StreamKind;

public class MediaInfoXMLParser {
  private final List<MiFile> files                   = new ArrayList<>();

  private static Pattern     DURATION_HOUR_PATTERN   = Pattern.compile("(\\d*?) h");
  private static Pattern     DURATION_MINUTE_PATTERN = Pattern.compile("(\\d*?) min");
  private static Pattern     DURATION_SECOND_PATTERN = Pattern.compile("(\\d*?) s");

  public static MediaInfoXMLParser parseXML(Path path) throws Exception {
    return new MediaInfoXMLParser(Jsoup.parse(new FileInputStream(path.toFile()), "UTF-8", "", Parser.xmlParser()));
  }

  private MediaInfoXMLParser(Document document) throws Exception {
    // first check if there is a valid root object (old and new format)
    Elements rootElements = document.select("MediaInfo");
    if (rootElements.isEmpty()) {
      throw new InvalidXmlException("Invalid/unparseable XML");
    }

    Element root = rootElements.get(0);

    // get all files from that iso
    Elements fileElements = root.select("media"); // old style
    if (fileElements.isEmpty()) {
      fileElements = root.select("file"); // new style
    }

    // process every file in the ISO
    for (Element file : fileElements) {
      MiFile miFile = new MiFile();

      // filename is in <media> tag in the newer format
      if (StringUtils.isNotBlank(file.attr("ref"))) {
        miFile.filename = file.attr("ref");
      }

      // process every track in the file
      for (Element track : file.select("track")) {
        MiTrack miTrack = new MiTrack();
        miTrack.type = track.attr("type");

        // all tags in that track
        miTrack.elements.addAll(track.children());

        miFile.tracks.add(miTrack);
      }

      miFile.createSnapshot();

      files.add(miFile);
    }

  }

  /**
   * find file with longest stream
   */
  public MiFile getMainFile() {
    long longest = 0L;

    MiFile mainFile = null;
    for (MiFile f : files) {
      if (f.duration > longest) {
        mainFile = f;
        longest = f.duration;
      }
    }

    // prevent NPE
    if (mainFile == null) {
      mainFile = new MiFile();
    }

    return mainFile;
  }

  /**
   * File record (1:N)
   */
  public static class MiFile {
    private final List<MiTrack>                             tracks;

    public final Map<StreamKind, List<Map<String, String>>> snapshot;

    private long                                            filesize   = 0L;
    private long                                            streamsize = 0L;
    private int                                             duration   = 0;
    private String                                          filename   = "";
    private Set<String>                                     languages  = new HashSet<>();

    private MiFile() {
      tracks = new ArrayList<>();
      snapshot = new EnumMap<>(StreamKind.class);
    }

    public Set<String> getLanguages() {
      return languages;
    }

    /**
     * Returns the filesize or accumulated streamsize
     * 
     * @return the filesize or accumulated streamsize
     */
    public long getFilesize() {
      return filesize > streamsize ? filesize : streamsize;
    }

    /**
     * Returns the (accumulated) duration
     * 
     * @return the (accumulated) duration
     */
    public int getDuration() {
      return duration;
    }

    // kinda same snapshot map like from originating MediaInfo (TagNames differ!)
    private void createSnapshot() {
      filesize = 0L;
      streamsize = 0L;
      duration = 0;

      StreamKind currentKind = null;
      List<Map<String, String>> streamInfoList = new ArrayList<>(tracks.size());
      Map<String, String> generalStreamInfo = null;

      for (MiTrack track : tracks) {
        if (StreamKind.valueOf(track.type) != currentKind) {
          // reset map for each type
          streamInfoList = new ArrayList<>(tracks.size());
        }

        Map<String, String> streamInfo = new LinkedHashMap<>();
        currentKind = StreamKind.valueOf(track.type);

        // remember the general stream info, to enhance it afterwards if needed
        if (currentKind == StreamKind.General) {
          generalStreamInfo = streamInfo;
        }

        for (Element elem : track.elements) {
          int i = 0;
          String ename = elem.tagName();
          String key = getMappedKey(ename);
          while (streamInfo.containsKey(key)) {
            // change key for duplicates
            // 1 = keyname
            // 2 = keyname/String
            // 3 = keyname/String1
            // 4 = keyname/String2
            // [...]
            key = ename + "/String";
            if (i > 0) {
              key += i;
            }
            i++;
          }

          String value = elem.ownText();

          // Width and Height sometimes comes with the string "pixels"
          if (key.equals("Width") || key.equals("Height")) {
            value = value.replace("pixels", "");
          }

          // accumulate filesizes & duration
          if (key.equals("FileSize")) {
            try {
              filesize += Long.parseLong(value); // should be only once in General
            }
            catch (NumberFormatException ignored) {
            }
          }
          if (key.equals("Stream_size")) {
            try {
              streamsize += Long.parseLong(value);
            }
            catch (NumberFormatException ignored) {
            }
          }
          if (key.equals("Complete_name") && StreamKind.General == currentKind) {
            this.filename = value;
          }
          if (key.equals("Duration") && StreamKind.General == currentKind) {
            try {
              // parse the duration value as a number
              // comes in different favors
              // a) <Duration>5184.000</Duration> // 5184 secs
              // b) <Duration>888000</Duration> // 888 secs
              Double d = Double.parseDouble(value.replace(".", ""));
              duration += d.intValue() / 1000;
            }
            catch (NumberFormatException e) {
              // parse the duration value as text
              // a) <Duration>1 h 26 min</Duration>
              // b) <Duration>14 min 48 s 0 ms</Duration>
              try {
                int hours = 0;
                int minutes = 0;
                int seconds = 0;

                Matcher matcher = DURATION_HOUR_PATTERN.matcher(value);
                if (matcher.find()) {
                  hours = Integer.parseInt(matcher.group(1));
                }
                matcher = DURATION_MINUTE_PATTERN.matcher(value);
                if (matcher.find()) {
                  minutes = Integer.parseInt(matcher.group(1));
                }
                matcher = DURATION_SECOND_PATTERN.matcher(value);
                if (matcher.find()) {
                  seconds = Integer.parseInt(matcher.group(1));
                }

                duration += hours * 3600 + minutes * 60 + seconds;
              }
              catch (NumberFormatException ignored) {
              }
            }
          }
          // push it twice; orginating key AND possible new mapped key; to maintain the StringX ordering!
          streamInfo.put(key, value);
          if (!key.equals(getMappedKey(key))) {
            streamInfo.put(getMappedKey(key), value);
          }
        }
        streamInfoList.add(streamInfo);
        snapshot.put(StreamKind.valueOf(track.type), streamInfoList);
      } // end tracks

      // we rely on some infos in the general stream info; add that if it was not available in the XML
      if (generalStreamInfo != null) {
        if (generalStreamInfo.get("VideoCount") == null && snapshot.get(StreamKind.Video) != null) {
          generalStreamInfo.put("VideoCount", String.valueOf(snapshot.get(StreamKind.Video).size()));
        }
        if (generalStreamInfo.get("AudioCount") == null && snapshot.get(StreamKind.Audio) != null) {
          generalStreamInfo.put("AudioCount", String.valueOf(snapshot.get(StreamKind.Audio).size()));
        }
        if (generalStreamInfo.get("TextCount") == null && snapshot.get(StreamKind.Text) != null) {
          generalStreamInfo.put("TextCount", String.valueOf(snapshot.get(StreamKind.Text).size()));
        }
      }

      // if (!filename.isEmpty()) {
      // // we have a filename (DVD structure or plain file)
      // Path p = Paths.get(".",filename);
      // if (p.getNameCount() == 0) {
      // // we just have a root directory like v: - create fake video name...
      // p = p.resolve("/iso/dummy.vob");
      // }
      // MediaFile mf = new MediaFile(p);
      // if (mf.getType() != MediaFileType.VIDEO && !mf.getExtension().equalsIgnoreCase("mpls")) {
      // // MI seems to write/parse MPLS into xml...? tread that as valid.
      // // all other are invalid files - ignore duration et all
      // snapshot.clear();
      // duration = 0;
      // }
      // }
    }

    /**
     * Provides a simple mapping from our parsed XML tags to our already used internal MI naming.<br>
     * So we should be able to use our default gatherMI() method... since the snapshot is now correct ;)
     * 
     * @param key
     *          the original key to get the mapped key for
     * @return the mapped key
     */
    private String getMappedKey(String key) {
      String k = key;
      switch (key) {
        case "Codec/String": // second codec entry
          k = "CodecID/Hint";
          break;
        case "Count_of_audio_streams":
          k = "AudioCount";
          break;
        case "Count_of_text_streams":
          k = "TextCount"; // assumption!
          break;
        case "Count_of_video_streams":
          k = "VideoCount"; // assumption!
          break;
        case "Count_of_menu_streams":
          k = "MenuCount"; // assumption!
          break;
        case "Channel_s_":
        case "Channels":
          k = "Channel(s)";
          break;
        case "Bit_rate":
          k = "BitRate";
          break;
        case "File_size":
          k = "FileSize";
          break;
        case "Overall_bit_rate":
          k = "OverallBitRate";
          break;
        case "Count_of_stream_of_this_kind":
          k = "StreamCount";
          break;
        case "Codec_Extensions_usually_used":
          k = "Codec/Extensions";
          break;
        case "Scan_type":
          k = "ScanType";
          break;
        default:
          break;
      }
      return k;
    }
  }

  /**
   * Track record of every file (1:N)
   */
  public static class MiTrack {
    public String              type;
    public final List<Element> elements = new ArrayList<>();
  }

  public static class InvalidXmlException extends Exception {
    public InvalidXmlException(String message) {
      super(message);
    }
  }
}
