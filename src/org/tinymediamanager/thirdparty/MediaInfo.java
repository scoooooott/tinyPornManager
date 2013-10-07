/*
 * Copyright 2012 - 2013 Manuel Laggner
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
import java.io.File;
import java.io.IOException;
import java.text.Normalizer;
import java.text.Normalizer.Form;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.jna.NativeLibrary;
import com.sun.jna.Platform;
import com.sun.jna.Pointer;
import com.sun.jna.WString;

/**
 * The Class MediaInfo.
 * 
 * @author Myron Boyle
 */
public class MediaInfo implements Closeable {

  /** The Constant LOGGER. */
  private static final Logger LOGGER = LoggerFactory.getLogger(MediaInfo.class);

  static {
    try {
      // libmediainfo for linux depends on libzen
      if (Platform.isLinux()) {
        // We need to load dependencies first, because we know where our native
        // libs are (e.g. Java Web Start Cache).
        // If we do not, the system will look for dependencies, but only in the
        // library path.
        NativeLibrary.getInstance("zen");
      }
    }
    catch (Throwable e) {
      LOGGER.error("Failed to preload libzen");
    }
  }

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
    return handle != null ? true : false;
  }

  /**
   * Instantiates a new media info.
   */
  public MediaInfo() {
    try {
      handle = MediaInfoLibrary.INSTANCE.New();
    }
    catch (LinkageError e) {
      // throw new MediaInfoException(e);
      LOGGER.error("Failed to load mediainfo", e);
      handle = null;
    }
  }

  /**
   * Open.
   * 
   * @param file
   *          the file
   * @return true, if successful
   */
  public synchronized boolean open(File file) {
    if (file != null && isLoaded()) {
      String filename = file.getAbsolutePath();
      if (SystemUtils.IS_OS_MAC_OSX) {
        filename = Normalizer.normalize(filename, Form.NFC);
      }
      return file.isFile() && MediaInfoLibrary.INSTANCE.Open(handle, new WString(filename)) > 0;
    }
    else {
      return false;
    }
  }

  /**
   * Inform.
   * 
   * @return the string
   */
  public synchronized String inform() {
    if (isLoaded()) {
      return MediaInfoLibrary.INSTANCE.Inform(handle).toString();
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
   * Option.
   * 
   * @param option
   *          the option
   * @param value
   *          the value
   * @return the string
   */
  public synchronized String option(String option, String value) {
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
  public synchronized String get(StreamKind streamKind, int streamNumber, String parameter, InfoKind infoKind, InfoKind searchKind) {
    if (isLoaded()) {
      return MediaInfoLibrary.INSTANCE.Get(handle, streamKind.ordinal(), streamNumber, new WString(parameter), infoKind.ordinal(),
          searchKind.ordinal()).toString();
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
   * @return the string
   */
  public String get(StreamKind streamKind, int streamNumber, int parameterIndex) {
    return get(streamKind, streamNumber, parameterIndex, InfoKind.Text);
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
  public synchronized String get(StreamKind streamKind, int streamNumber, int parameterIndex, InfoKind infoKind) {
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
  public synchronized int streamCount(StreamKind streamKind) {
    if (isLoaded()) {
      return MediaInfoLibrary.INSTANCE.Count_Get(handle, streamKind.ordinal(), -1);
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
  public synchronized int parameterCount(StreamKind streamKind, int streamNumber) {
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
    Map<StreamKind, List<Map<String, String>>> mediaInfo = new EnumMap<StreamKind, List<Map<String, String>>>(StreamKind.class);

    for (StreamKind streamKind : StreamKind.values()) {
      int streamCount = streamCount(streamKind);

      if (streamCount > 0) {
        List<Map<String, String>> streamInfoList = new ArrayList<Map<String, String>>(streamCount);

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
    Map<String, String> streamInfo = new LinkedHashMap<String, String>();

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
  public synchronized void close() {
    if (isLoaded()) {
      MediaInfoLibrary.INSTANCE.Close(handle);
    }
  }

  /**
   * Dispose.
   */
  public synchronized void dispose() {
    if (handle == null)
      return;

    // delete handle
    MediaInfoLibrary.INSTANCE.Delete(handle);
    handle = null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#finalize()
   */
  @Override
  protected void finalize() {
    dispose();
  }

  /**
   * The Enum StreamKind.
   * 
   * @author Manuel Laggner
   */
  public enum StreamKind {

    /** The General. */
    General,
    /** The Video. */
    Video,
    /** The Audio. */
    Audio,
    /** The Text. */
    Text,
    /** The Chapters. */
    Chapters,
    /** The Image. */
    Image,
    /** The Menu. */
    Menu;
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
    Domain;
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
    catch (LinkageError e) {
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
  public static Map<StreamKind, List<Map<String, String>>> snapshot(File file) throws IOException {
    MediaInfo mi = new MediaInfo();
    try {
      if (mi.open(file)) {
        return mi.snapshot();
      }
      else {
        throw new IOException("Failed to open file: " + file);
      }
    }
    finally {
      mi.close();
    }
  }
}
