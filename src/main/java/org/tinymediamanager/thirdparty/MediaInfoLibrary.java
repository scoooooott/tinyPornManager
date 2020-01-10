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

import static java.util.Collections.singletonMap;

import com.sun.jna.FunctionMapper;
import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Platform;
import com.sun.jna.Pointer;
import com.sun.jna.WString;

/**
 * The Interface MediaInfoLibrary.
 * 
 * @author Myron Boyle
 */
interface MediaInfoLibrary extends Library {

  // libmediainfo for linux depends on libzen, so we need to load dependencies first, because we know where our native libs are (e.g. Java Web Start
  // Cache).
  // if we do not, the system will look for dependencies, but only in the library path
  Library          LIB_ZEN  = Platform.isLinux() ? Native.load("zen", Library.class) : null;

  MediaInfoLibrary INSTANCE = Native.load("mediainfo", MediaInfoLibrary.class,
      singletonMap(OPTION_FUNCTION_MAPPER, (FunctionMapper) (lib, method) -> {
        // MediaInfo_New(), MediaInfo_Open() ...
        return "MediaInfo_" + method.getName();
      }));

  /**
   * Create a new handle.
   * 
   * @return handle
   */
  Pointer New();

  /**
   * Open a file and collect information about it (technical information and tags).
   * 
   * @param handle
   *          the handle
   * @param file
   *          full name of the file to open
   * @return 1 if file was opened, 0 if file was not not opened
   */
  int Open(Pointer handle, WString file);

  int Open_Buffer_Init(Pointer handle, long length, long offset);

  int Open_Buffer_Continue(Pointer handle, byte[] buffer, int size);

  long Open_Buffer_Continue_GoTo_Get(Pointer handle);

  int Open_Buffer_Finalize(Pointer handle);

  /**
   * Configure or get information about MediaInfo.
   * 
   * @param handle
   *          the handle
   * @param option
   *          The name of option
   * @param value
   *          The value of option
   * @return Depends on the option: by default "" (nothing) means No, other means Yes
   */
  WString Option(Pointer handle, WString option, WString value);

  /**
   * Get all details about a file.
   * 
   * @param handle
   *          the handle
   * @param reserved
   *          reserved
   * @return All details about a file in one string
   */
  WString Inform(Pointer handle, int reserved);

  /**
   * Get a piece of information about a file (parameter is a string).
   * 
   * @param handle
   *          the handle
   * @param streamKind
   *          Kind of stream (general, video, audio...)
   * @param streamNumber
   *          Stream number in Kind of stream (first, second...)
   * @param parameter
   *          Parameter you are looking for in the stream (Codec, width, bitrate...), in string format ("Codec", "Width"...)
   * @param infoKind
   *          Kind of information you want about the parameter (the text, the measure, the help...)
   * @param searchKind
   *          Where to look for the parameter
   * @return a string about information you search, an empty string if there is a problem
   */
  WString Get(Pointer handle, int streamKind, int streamNumber, WString parameter, int infoKind, int searchKind);

  /**
   * Get a piece of information about a file (parameter is an integer).
   * 
   * @param handle
   *          the handle
   * @param streamKind
   *          Kind of stream (general, video, audio...)
   * @param streamNumber
   *          Stream number in Kind of stream (first, second...)
   * @param parameterIndex
   *          the parameter index
   * @param infoKind
   *          Kind of information you want about the parameter (the text, the measure, the help...)
   * @return a string about information you search, an empty string if there is a problem
   */
  WString GetI(Pointer handle, int streamKind, int streamNumber, int parameterIndex, int infoKind);

  /**
   * Count of streams of a stream kind (StreamNumber not filled), or count of piece of information in this stream.
   * 
   * @param handle
   *          the handle
   * @param streamKind
   *          Kind of stream (general, video, audio...)
   * @param streamNumber
   *          Stream number in this kind of stream (first, second...)
   * @return number of streams of the given stream kind
   */
  int Count_Get(Pointer handle, int streamKind, int streamNumber);

  /**
   * Close a file opened before with Open().
   * 
   * @param handle
   *          the handle
   */
  void Close(Pointer handle);

  /**
   * Dispose of a handle created with New().
   * 
   * @param handle
   *          the handle
   */
  void Delete(Pointer handle);

}
