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

import java.io.Closeable;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.jna.Pointer;
import com.sun.jna.WString;

/**
 * The Class MediaInfo.<br>
 * http://sourceforge.net/p/mediainfo/code/HEAD/tree/MediaInfoLib/tags/v0.7.73/Source/MediaInfoDLL/MediaInfoDLL.JNA.java
 * 
 * @author Myron Boyle
 */
public class MediaInfo implements Closeable {

  private static final Logger LOGGER = LoggerFactory.getLogger(MediaInfo.class);

  /**
   * the internal pointer handle of mediainfo<br>
   * .
   */
  private Pointer             handle;

  /**
   * checks if the internal handle is null.
   * 
   * @return true/false
   */
  public Boolean isLoaded() {
    return handle != null;
  }

  /**
   * Instantiates a new media info.
   */
  public MediaInfo() {
  }

  /**
   * Open.
   * 
   * @param file
   *          the file
   * @return true, if successful
   */
  public boolean open(Path file) throws MediaInfoException {
    // create handle
    try {
      if (handle == null) {
        handle = MediaInfoLibrary.INSTANCE.New();
      }
    }
    catch (LinkageError e) {
      return false;
    }

    if (file != null && isLoaded()) {
      return MediaInfoLibrary.INSTANCE.Open(handle, new WString(file.toAbsolutePath().toString())) > 0;
    }
    else {
      return false;
    }
  }

  /**
   * STREAM handling
   * 
   * @param length
   * @param offset
   * @return
   */
  public boolean openBufferInit(long length, long offset) {
    try {
      if (handle == null) {
        handle = MediaInfoLibrary.INSTANCE.New();
      }
    }
    catch (LinkageError e) {
      return false;
    }
    if (isLoaded()) {
      return MediaInfoLibrary.INSTANCE.Open_Buffer_Init(handle, length, offset) > 0;
    }
    else {
      return false;
    }
  }

  /**
   * Open a stream and collect information about it (technical information and tags) (By buffer, Continue)
   * 
   * @param buffer
   *          pointer to the stream
   * @param size
   *          Count of bytes to read
   * @return a bitfield<br>
   *         bit 0: Is Accepted (format is known)<br>
   *         bit 1: Is Filled (main data is collected)<br>
   *         bit 2: Is Updated (some data have beed updated, example: duration for a real time MPEG-TS stream)<br>
   *         bit 3: Is Finalized (No more data is needed, will not use further data)<br>
   *         bit 4-15: Reserved<br>
   *         bit 16-31: User defined
   */
  public int openBufferContinue(byte[] buffer, int size) {
    return MediaInfoLibrary.INSTANCE.Open_Buffer_Continue(handle, buffer, size);
  }

  public long openBufferContinueGoToGet() {
    return MediaInfoLibrary.INSTANCE.Open_Buffer_Continue_GoTo_Get(handle);
  }

  public int openBufferFinalize() {
    return MediaInfoLibrary.INSTANCE.Open_Buffer_Finalize(handle);
  }

  /**
   * Inform.
   * 
   * @return the string
   */
  public String inform() {
    if (isLoaded()) {
      return MediaInfoLibrary.INSTANCE.Inform(handle, 0).toString();
    }
    else {
      return "";
    }
  }

  /**
   * Option.
   * 
   * @param option
   *          the option
   * @return the string
   */
  public String option(String option) {
    return option(option, "");
  }

  /**
   * Get a piece of information about a file (parameter is a string).
   *
   * @param StreamKind
   *          Kind of Stream (general, video, audio...)
   * @param StreamNumber
   *          Stream number in Kind of Stream (first, second...)
   * @param parameter
   *          Parameter you are looking for in the Stream (Codec, width, bitrate...), in string format ("Codec", "Width"...)
   * @return a string about information you search, an empty string if there is a problem
   */
  public String Get(StreamKind StreamKind, int StreamNumber, String parameter) {
    return Get(StreamKind, StreamNumber, parameter, InfoKind.Text, InfoKind.Name);
  }

  /**
   * Get a piece of information about a file (parameter is a string).
   *
   * @param StreamKind
   *          Kind of Stream (general, video, audio...)
   * @param StreamNumber
   *          Stream number in Kind of Stream (first, second...)
   * @param parameter
   *          Parameter you are looking for in the Stream (Codec, width, bitrate...), in string format ("Codec", "Width"...)
   * @param infoKind
   *          Kind of information you want about the parameter (the text, the measure, the help...)
   */
  public String Get(StreamKind StreamKind, int StreamNumber, String parameter, InfoKind infoKind) {
    return Get(StreamKind, StreamNumber, parameter, infoKind, InfoKind.Name);
  }

  /**
   * Get a piece of information about a file (parameter is a string).
   *
   * @param StreamKind
   *          Kind of Stream (general, video, audio...)
   * @param StreamNumber
   *          Stream number in Kind of Stream (first, second...)
   * @param parameter
   *          Parameter you are looking for in the Stream (Codec, width, bitrate...), in string format ("Codec", "Width"...)
   * @param infoKind
   *          Kind of information you want about the parameter (the text, the measure, the help...)
   * @param searchKind
   *          Where to look for the parameter
   * @return a string about information you search, an empty string if there is a problem
   */
  public String Get(StreamKind StreamKind, int StreamNumber, String parameter, InfoKind infoKind, InfoKind searchKind) {
    if (isLoaded()) {
      return MediaInfoLibrary.INSTANCE
          .Get(handle, StreamKind.ordinal(), StreamNumber, new WString(parameter), infoKind.ordinal(), searchKind.ordinal()).toString();
    }
    else {
      return "";
    }
  }

  /**
   * Get a piece of information about a file (parameter is an integer).
   *
   * @param StreamKind
   *          Kind of Stream (general, video, audio...)
   * @param StreamNumber
   *          Stream number in Kind of Stream (first, second...)
   * @param parameterIndex
   *          Parameter you are looking for in the Stream (Codec, width, bitrate...), in integer format (first parameter, second parameter...)
   * @return a string about information you search, an empty string if there is a problem
   */
  public String get(StreamKind StreamKind, int StreamNumber, int parameterIndex) {
    return Get(StreamKind, StreamNumber, parameterIndex, InfoKind.Text);
  }

  /**
   * Get a piece of information about a file (parameter is an integer).
   * 
   * @param StreamKind
   *          Kind of Stream (general, video, audio...)
   * @param StreamNumber
   *          Stream number in Kind of Stream (first, second...)
   * @param parameterIndex
   *          Parameter you are looking for in the Stream (Codec, width, bitrate...), in integer format (first parameter, second parameter...)
   * @param infoKind
   *          Kind of information you want about the parameter (the text, the measure, the help...)
   * @return a string about information you search, an empty string if there is a problem
   */
  public String Get(StreamKind StreamKind, int StreamNumber, int parameterIndex, InfoKind infoKind) {
    if (isLoaded()) {
      return MediaInfoLibrary.INSTANCE.GetI(handle, StreamKind.ordinal(), StreamNumber, parameterIndex, infoKind.ordinal()).toString();
    }
    else {
      return "";
    }
  }

  /**
   * Option.
   * 
   * @param option
   *          the option
   * @param value
   *          the value
   * @return the string
   */
  public String option(String option, String value) {
    if (isLoaded()) {
      return MediaInfoLibrary.INSTANCE.Option(handle, new WString(option), new WString(value)).toString();
    }
    else {
      return "";
    }
  }

  /**
   * Gets the.
   * 
   * @param streamKind
   *          the stream kind
   * @param streamNumber
   *          the stream number
   * @param parameter
   *          the parameter
   * @return the string
   */
  public String get(StreamKind streamKind, int streamNumber, String parameter) {
    return get(streamKind, streamNumber, parameter, InfoKind.Text, InfoKind.Name);
  }

  /**
   * Gets the.
   * 
   * @param streamKind
   *          the stream kind
   * @param streamNumber
   *          the stream number
   * @param parameter
   *          the parameter
   * @param infoKind
   *          the info kind
   * @return the string
   */
  public String get(StreamKind streamKind, int streamNumber, String parameter, InfoKind infoKind) {
    return get(streamKind, streamNumber, parameter, infoKind, InfoKind.Name);
  }

  /**
   * Gets the.
   * 
   * @param streamKind
   *          the stream kind
   * @param streamNumber
   *          the stream number
   * @param parameter
   *          the parameter
   * @param infoKind
   *          the info kind
   * @param searchKind
   *          the search kind
   * @return the string
   */
  public String get(StreamKind streamKind, int streamNumber, String parameter, InfoKind infoKind, InfoKind searchKind) {
    if (isLoaded()) {
      return MediaInfoLibrary.INSTANCE
          .Get(handle, streamKind.ordinal(), streamNumber, new WString(parameter), infoKind.ordinal(), searchKind.ordinal()).toString();
    }
    else {
      return "";
    }
  }

  /**
   * Gets the.
   * 
   * @param streamKind
   *          the stream kind
   * @param streamNumber
   *          the stream number
   * @param parameterIndex
   *          the parameter index
   * @param infoKind
   *          the info kind
   * @return the string
   */
  public String get(StreamKind streamKind, int streamNumber, int parameterIndex, InfoKind infoKind) {
    if (isLoaded()) {
      return MediaInfoLibrary.INSTANCE.GetI(handle, streamKind.ordinal(), streamNumber, parameterIndex, infoKind.ordinal()).toString();
    }
    else {
      return "";
    }
  }

  /**
   * Stream count.
   * 
   * @param streamKind
   *          the stream kind
   * @return the int
   */
  public int streamCount(StreamKind streamKind) {
    if (isLoaded()) {
      try {
        // We should use NativeLong for -1, but it fails on 64-bit
        // int Count_Get(Pointer Handle, int StreamKind, NativeLong StreamNumber);
        // return MediaInfoDLL_Internal.INSTANCE.Count_Get(Handle, StreamKind.ordinal(), -1);
        // so we use slower Get() with a character string
        String StreamCount = get(streamKind, 0, "StreamCount");
        if (StreamCount == null || StreamCount.length() == 0) {
          return 0;
        }
        return Integer.parseInt(StreamCount);
      }
      catch (Exception e) {
        return 0;
      }
    }
    else {
      return 0;
    }
  }

  /**
   * Parameter count.
   * 
   * @param streamKind
   *          the stream kind
   * @param streamNumber
   *          the stream number
   * @return the int
   */
  public int parameterCount(StreamKind streamKind, int streamNumber) {
    if (isLoaded()) {
      return MediaInfoLibrary.INSTANCE.Count_Get(handle, streamKind.ordinal(), streamNumber);
    }
    else {
      return 0;
    }
  }

  /**
   * Snapshot.
   * 
   * @return the map
   */
  public Map<StreamKind, List<Map<String, String>>> snapshot() {
    Map<StreamKind, List<Map<String, String>>> mediaInfo = new EnumMap<>(StreamKind.class);

    for (StreamKind streamKind : StreamKind.values()) {
      int streamCount = streamCount(streamKind);

      if (streamCount > 0) {
        List<Map<String, String>> streamInfoList = new ArrayList<>(streamCount);

        for (int i = 0; i < streamCount; i++) {
          streamInfoList.add(snapshot(streamKind, i));
        }

        mediaInfo.put(streamKind, streamInfoList);
      }
    }

    return mediaInfo;
  }

  /**
   * Snapshot.
   * 
   * @param streamKind
   *          the stream kind
   * @param streamNumber
   *          the stream number
   * @return the map
   */
  public Map<String, String> snapshot(StreamKind streamKind, int streamNumber) {
    Map<String, String> streamInfo = new LinkedHashMap<>();

    for (int i = 0, count = parameterCount(streamKind, streamNumber); i < count; i++) {
      String value = get(streamKind, streamNumber, i, InfoKind.Text);

      if (value.length() > 0) {
        streamInfo.put(get(streamKind, streamNumber, i, InfoKind.Name), value);
      }
    }

    return streamInfo;
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.io.Closeable#close()
   */
  @Override
  public void close() {
    if (isLoaded()) {
      MediaInfoLibrary.INSTANCE.Close(handle);
    }
  }

  /**
   * Dispose.
   */
  public void dispose() {
    if (!isLoaded()) {
      throw new IllegalStateException();
    }

    // delete handle
    MediaInfoLibrary.INSTANCE.Delete(handle);
    handle = null;
  }

  @Override
  protected void finalize() {
    if (isLoaded()) {
      dispose();
    }
  }

  /**
   * The Enum StreamKind.
   * 
   * @author Manuel Laggner
   */
  public enum StreamKind {
    General,
    Video,
    Audio,
    Text,
    Other,
    Image,
    Menu,
    @Deprecated
    Chapters // replaced by 'other'
  }

  /**
   * The Enum InfoKind.
   * 
   * @author Manuel Laggner
   */
  public enum InfoKind {
    /**
     * Unique name of parameter.
     */
    Name,

    /**
     * Value of parameter.
     */
    Text,

    /**
     * Unique name of measure unit of parameter.
     */
    Measure,

    /** The Options. */
    Options,

    /**
     * Translated name of parameter.
     */
    Name_Text,

    /**
     * Translated name of measure unit.
     */
    Measure_Text,

    /**
     * More information about the parameter.
     */
    Info,

    /**
     * How this parameter is supported, could be N (No), B (Beta), R (Read only), W (Read/Write).
     */
    HowTo,

    /**
     * Domain of this piece of information.
     */
    Domain
  }

  public enum Status {
    None(0x00),
    Accepted(0x01),
    Filled(0x02),
    Updated(0x04),
    Finalized(0x08);

    private int value;

    Status(int value) {
      this.value = value;
    }

    public int getValue(int value) {
      return value;
    }
  }

  /**
   * Version.
   * 
   * @return the string
   */
  public static String version() {
    return staticOption("Info_Version");
  }

  /**
   * Parameters.
   * 
   * @return the string
   */
  public static String parameters() {
    return staticOption("Info_Parameters");
  }

  /**
   * Codecs.
   * 
   * @return the string
   */
  public static String codecs() {
    return staticOption("Info_Codecs");
  }

  /**
   * Capacities.
   * 
   * @return the string
   */
  public static String capacities() {
    return staticOption("Info_Capacities");
  }

  /**
   * Static option.
   * 
   * @param option
   *          the option
   * @return the string
   */
  public static String staticOption(String option) {
    return staticOption(option, "");
  }

  /**
   * Static option.
   * 
   * @param option
   *          the option
   * @param value
   *          the value
   * @return the string
   */
  public static String staticOption(String option, String value) {
    try {
      return MediaInfoLibrary.INSTANCE.Option(null, new WString(option), new WString(value)).toString();
    }
    catch (Error e) {
      // throw new MediaInfoException(e);
      LOGGER.error("Failed to load mediainfo", e);
      return "";
    }
  }

  /**
   * Helper for easy usage.
   * 
   * @param file
   *          the file
   * @return the map
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   */
  public static Map<StreamKind, List<Map<String, String>>> snapshot(Path file) throws IOException {
    try (MediaInfo mi = new MediaInfo()) {
      if (mi.open(file)) {
        return mi.snapshot();
      }
      else {
        throw new IOException("Failed to open file: " + file);
      }
    }
  }
}
