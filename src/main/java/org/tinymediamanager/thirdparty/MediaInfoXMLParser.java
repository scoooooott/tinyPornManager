/*
 * Copyright 2012 - 2020 Manuel Laggner
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.tinymediamanager.thirdparty;

import java.io.FileInputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.core.Settings;
import org.tinymediamanager.thirdparty.MediaInfo.StreamKind;

public class MediaInfoXMLParser {

  private static final Logger  LOGGER                  = LoggerFactory.getLogger(MediaInfoXMLParser.class);
  private static final Pattern DURATION_HOUR_PATTERN   = Pattern.compile("(\\d*?) h");
  private static final Pattern DURATION_MINUTE_PATTERN = Pattern.compile("(\\d*?) min");
  private static final Pattern DURATION_SECOND_PATTERN = Pattern.compile("(\\d*?) s");
  private Path                 file                    = null;

  public MediaInfoXMLParser(Path file) {
    this.file = file;
  }

  public List<MediaInfoFile> parseXML() throws Exception {
    List<MediaInfoFile> files = new ArrayList<>();

    Document document = Jsoup.parse(new FileInputStream(file.toFile()), "UTF-8", "", Parser.xmlParser());
    // first check if there is a valid root object (old and new format)
    Elements rootElements = document.select("MediaInfo");
    if (rootElements.isEmpty()) {
      throw new InvalidXmlException("Invalid/unparseable XML");
    }

    Element root = rootElements.get(0);

    // get root element
    Elements fileElements = root.select("media"); // new style
    if (fileElements.isEmpty()) {
      fileElements = root.select("file"); // old style
    }

    // process every file in the ISO
    for (Element fileInXML : fileElements) {
      LOGGER.trace("XML: ------------------");

      MediaInfoFile miFile = new MediaInfoFile("/tmp/dummy.bdmv");
      if (StringUtils.isNotBlank(fileInXML.attr("ref")) && fileInXML.attr("ref").length() > 5) {
        miFile.setFilename(fileInXML.attr("ref"));
      }

      List<MiTrack> tracks = new ArrayList<MiTrack>();
      // and add all tracks
      for (Element track : fileInXML.select("track")) {
        MiTrack miTrack = new MiTrack();
        miTrack.type = track.attr("type");
        // all tags in that track
        miTrack.elements.addAll(track.children());
        tracks.add(miTrack);
      }

      // do the magic - create same weird map as MediaInfoLib will do, so we can parse with our impl...
      StreamKind currentKind = null;
      List<Map<String, String>> streamInfoList = new ArrayList<>(tracks.size());
      Map<String, String> generalStreamInfo = null;

      long kindFilesize = 0L;
      long kindStreamsize = 0L;
      int kindDuration = 0;

      int duration = 0;
      long filesize = 0;
      for (MiTrack track : tracks) {
        if (StreamKind.valueOf(track.type) != currentKind) {
          // use highest duration per kind as duration
          if (kindDuration > duration) {
            duration = kindDuration;
          }
          if (kindFilesize > filesize) {
            filesize = kindFilesize;
          }
          if (kindStreamsize > filesize) {
            filesize = kindStreamsize; // reuse filesize
          }
          // reset map for each type
          streamInfoList = new ArrayList<>(tracks.size());
          kindFilesize = 0L;
          kindStreamsize = 0L;
          kindDuration = 0;
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
          if (!ename.equals(key)) {
            LOGGER.trace("Key '{}' was repaced with '{}'", ename, key);
          }
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
          if (value.isEmpty()) {
            continue;
          }
          LOGGER.trace("XML: Kind:{}  {}={}", currentKind, key, value);

          // Width and Height sometimes comes with the string "pixels"
          if (key.equals("Width") || key.equals("Height")) {
            value = value.replace("pixels", "").trim();
          }

          // accumulate filesizes & duration /for multiple tracks)
          // but only per streamKind (audio & video tracks will have same duration ;)
          if (key.equals("FileSize")) {
            try {
              // accumulate filemsize for same type of tracks
              kindFilesize += parseSize(value); // should be only once in General
              // and overwrite current value with accumulated (since we can only have one value/kind)
              value = String.valueOf(kindFilesize);
            }
            catch (NumberFormatException ignored) {
              LOGGER.trace("could not parse filesize", ignored);
            }
          }
          if (key.equals("Stream_size")) {
            try {
              // accumulate streamsize for same type of tracks
              kindStreamsize += parseSize(value);
              // and overwrite current value with accumulated (since we can only have one value/kind)
              value = String.valueOf(kindStreamsize);
            }
            catch (NumberFormatException ignored) {
              LOGGER.trace("could not parse streamsize", ignored);
            }
          }
          if (key.equals("Complete_name") && StreamKind.General == currentKind && value.length() > 5) {
            miFile.setFilename(value);
          }
          if (key.equals("Duration") && StreamKind.General == currentKind) {
            try {
              // parse the duration value as a number
              // comes in different favors
              // a) <Duration>5184.000</Duration> // 5184 secs
              // b) <Duration>888000</Duration> // 888 secs
              Double d = Double.parseDouble(value.replace(".", ""));
              // accumulate duration for same type of tracks
              kindDuration += d.intValue() / 1000;
              // and overwrite current value with accumulated (since we can only have one value/kind)
              value = String.valueOf(kindDuration);
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

                // accumulate duration for same type of tracks
                kindDuration += hours * 3600 + minutes * 60 + seconds;
                // and overwrite current value with accumulated (since we can only have one value/kind)
                value = String.valueOf(kindDuration);
              }
              catch (NumberFormatException ignored) {
              }
            }
          }

          // push it twice; originating key AND possible new mapped key; to maintain the StringX ordering!
          streamInfo.put(key, value);
          if (!key.equals(getMappedKey(key))) {
            LOGGER.trace("Duplicate Key '{}'", key);
            streamInfo.put(getMappedKey(key), value);
          }
        }
        streamInfoList.add(streamInfo);
        miFile.getSnapshot().put(StreamKind.valueOf(track.type), streamInfoList);
      } // end tracks

      miFile.setDuration(duration);
      miFile.setFilesize(filesize);

      // we rely on some infos in the general stream info; add that if it was not available in the XML
      if (generalStreamInfo != null) {
        if (generalStreamInfo.get("VideoCount") == null && miFile.getSnapshot().get(StreamKind.Video) != null) {
          generalStreamInfo.put("VideoCount", String.valueOf(miFile.getSnapshot().get(StreamKind.Video).size()));
        }
        if (generalStreamInfo.get("AudioCount") == null && miFile.getSnapshot().get(StreamKind.Audio) != null) {
          generalStreamInfo.put("AudioCount", String.valueOf(miFile.getSnapshot().get(StreamKind.Audio).size()));
        }
        if (generalStreamInfo.get("TextCount") == null && miFile.getSnapshot().get(StreamKind.Text) != null) {
          generalStreamInfo.put("TextCount", String.valueOf(miFile.getSnapshot().get(StreamKind.Text).size()));
        }
      }

      if (!miFile.getFilename().isEmpty()) {
        // we have a filename (DVD structure or plain file)
        Path p = Paths.get(miFile.getFilename());
        if (p.getNameCount() == 0) {
          // we just have a root directory like v: - create fake video name...
          p = p.resolve("/iso/dummy.vob");
        }
      }

      String ext = FilenameUtils.getExtension(miFile.getFilename()).toLowerCase(Locale.ROOT);
      if (Settings.getInstance().getAllSupportedFileTypes().contains("." + ext) || "mpls".equalsIgnoreCase(ext)) {
        miFile.setFilename(Paths.get(miFile.getFilename()).getFileName().toString()); // so we have it w/o path
        files.add(miFile);
      }
    } // for every MI file entry

    return files;
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

  private long parseSize(String size) {
    // <File_size>22.0 MiB (5%)</File_size>
    long s = 0L;

    // replace everything after bracket
    size = size.replaceAll("\\(.*$", "").trim();

    int factor = 1;
    if (size.toLowerCase(Locale.ROOT).endsWith("kib")) {
      factor = 1024;
    }
    if (size.toLowerCase(Locale.ROOT).endsWith("mib")) {
      factor = 1024 * 1024;
    }
    if (size.toLowerCase(Locale.ROOT).endsWith("gib")) {
      factor = 1024 * 1024 * 1024;
    }
    // remove everything after first whitespace
    size = size.replaceAll("\\s.*$", "");

    Double d = Double.parseDouble(size);
    s = d.longValue(); // bytes
    s = s * factor;
    return s;
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
