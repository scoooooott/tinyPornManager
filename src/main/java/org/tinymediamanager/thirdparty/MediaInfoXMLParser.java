package org.tinymediamanager.thirdparty;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.tinymediamanager.core.MediaFileType;
import org.tinymediamanager.core.entities.MediaFile;
import org.tinymediamanager.thirdparty.MediaInfo.StreamKind;
import org.w3c.dom.Element;

@XmlRootElement(name = "Mediainfo")
public class MediaInfoXMLParser {

  @XmlElement(name = "File")
  public List<MiFile> files;

  private long        filesize = 0L;
  private int         duration = 0;

  /**
   * init mediafile snapshots
   */
  public void snapshot() {
    filesize = 0L;
    duration = 0;
    for (MiFile file : files) {
      file.snapshot();
      filesize += file.getFilesize();
      duration += file.getDuration();
    }
  }

  public long getFilesize() {
    return filesize;
  }

  public int getDuration() {
    return duration;
  }

  /**
   * find file with biggest stream
   */
  public MiFile getBiggestFile() {
    long biggest = 0L;
    MiFile biggestFile = null;
    for (MiFile f : files) {
      if (f.getFilesize() > biggest) {
        biggest = f.getFilesize();
        biggestFile = f;
      }
    }
    return biggestFile;
  }

  /**
   * File record (1:N)
   */
  public static class MiFile {
    @XmlElement(name = "track")
    public List<MiTrack>                              tracks;

    @XmlTransient
    public Map<StreamKind, List<Map<String, String>>> snapshot;

    private long                                      filesize   = 0L;
    private long                                      streamsize = 0L;
    private int                                       duration   = 0;
    private String                                    filename   = "";

    /**
     * Returns the filesize or accumulated streamsize
     * 
     * @return
     */
    public long getFilesize() {
      return filesize > streamsize ? filesize : streamsize;
    }

    /**
     * Returns the (accumulated) duration
     * 
     * @return
     */
    public int getDuration() {
      return duration;
    }

    // kinda same snapshot map like from originating MediaInfo (TagNames differ!)
    public void snapshot() {
      filesize = 0L;
      streamsize = 0L;
      duration = 0;
      if (snapshot == null) {
        snapshot = new EnumMap<>(StreamKind.class);
        StreamKind currentKind = null;
        List<Map<String, String>> streamInfoList = new ArrayList<>(tracks.size());
        for (MiTrack track : tracks) {
          if (StreamKind.valueOf(track.type) != currentKind) {
            // reset map for each type
            streamInfoList = new ArrayList<>(tracks.size());
          }
          currentKind = StreamKind.valueOf(track.type);
          Map<String, String> streamInfo = new LinkedHashMap<>();
          for (Element elem : track.elements) {
            int i = 0;
            String ename = elem.getNodeName();
            String key = getMappedKey(elem.getNodeName());
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

            // accumulate filesizes & duration
            if (key.equals("FileSize")) {
              filesize += Long.parseLong(elem.getTextContent()); // should be only once in General
            }
            if (key.equals("Stream_size")) {
              streamsize += Long.parseLong(elem.getTextContent());
            }
            if (key.equals("Complete_name") && StreamKind.General == currentKind) {
              this.filename = elem.getTextContent();
            }
            if (key.equals("Duration") && StreamKind.General == currentKind) {
              try {
                Double d = Double.parseDouble(elem.getTextContent());
                duration += d.intValue() / 1000;
              }
              catch (NumberFormatException ignore) {
              }
            }
            // ppush it twice; orginating key AND possible new mapped key; to maintain the StringX ordering!
            streamInfo.put(key, elem.getTextContent());
            if (!key.equals(getMappedKey(key))) {
              streamInfo.put(getMappedKey(key), elem.getTextContent());
            }
          }
          streamInfoList.add(streamInfo);
          snapshot.put(StreamKind.valueOf(track.type), streamInfoList);
        } // end tracks
      } // end file

      if (!filename.isEmpty()) {
        // we have a filename (DVD structure or plain file)
        Path p = Paths.get(filename);
        if (p.getNameCount() == 0) {
          // we just have a root directory like v: - create fake video name...
          p = p.resolve("/iso/dummy.vob");
        }
        MediaFile mf = new MediaFile(p);
        if (mf.getType() != MediaFileType.VIDEO) {
          // invalid file - ignore duration et all
          snapshot = new EnumMap<>(StreamKind.class);
          duration = 0;
        }
      }
    }

    /**
     * Provides a simple mapping from our parsed XML tags to our already used internal MI naming.<br>
     * So we should be able to use our default gatherMI() method... since the snapshot is now correct ;)
     * 
     * @param key
     * @return
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
  static class MiTrack {
    @XmlAttribute
    public String        type;
    @XmlAnyElement
    public List<Element> elements;
  }
}
