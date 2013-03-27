/*
 * Copyright 2012 Manuel Laggner
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
package org.tinymediamanager.core.movie;

/**
 * The Enum MovieFanartNaming.
 * 
 * @author Manuel Laggner
 */
public enum MovieFanartNaming {

  /** [filename]-fanart.png */
  FILENAME_FANART_PNG,
  /** [filename]-fanart.jpg */
  FILENAME_FANART_JPG,
  /** [filename]-fanart.tbn */
  @Deprecated
  FILENAME_FANART_TBN,

  /** [moviename]-fanart.png */
  MOVIENAME_FANART_PNG,
  /** [moviename]-fanart.jpg */
  MOVIENAME_FANART_JPG,
  /** [moviename]-fanart.tbn */
  @Deprecated
  MOVIENAME_FANART_TBN,

  /** fanart.png */
  FANART_PNG,
  /** fanart.jpg */
  FANART_JPG,
  /** fanart.tbn */
  @Deprecated
  FANART_TBN;
}
