/*
 * Copyright 2012 - 2018 Manuel Laggner
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
package org.tinymediamanager.scraper.exceptions;

import org.tinymediamanager.scraper.entities.MediaType;

/**
 * The UnsupportedMediaTypeException is used to indicate that the given media type cannot be used for the chosen meta data provider
 * 
 * @author Manuel Laggner
 * @since 1.0
 */
public class UnsupportedMediaTypeException extends Exception {
  private static final long serialVersionUID = 2860692702692312793L;
  private MediaType         type;

  public UnsupportedMediaTypeException(MediaType type) {
    this.type = type;
  }

  @Override
  public String toString() {
    return "The media type " + type.name() + " is not supported by this meta data provider";
  }
}
