/*
 * Copyright 2012 - 2014 Manuel Laggner
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

package org.tinymediamanager.core;

/**
 * various MediaFileTypes
 * 
 * @author Myron Boyle
 */
public enum MediaFileType {

  // ordering of list = ordering of type in GUI ;)

  // @formatter:off
  VIDEO, 
  VIDEO_EXTRA, // bonus/extra videos
  TRAILER, 
  SAMPLE, // sample != trailer
  AUDIO, 
  SUBTITLE, 
  NFO, 
  POSTER, 
  FANART, 
  BANNER,
  CLEARART,
  DISCART,
  LOGO,
  THUMB,   
  SEASON_POSTER,
  EXTRAFANART, 
  EXTRATHUMB,  
  GRAPHIC,
  TEXT, // various text infos, like BDinfo.txt or others...
  UNKNOWN;
  // @formatter:on
}
