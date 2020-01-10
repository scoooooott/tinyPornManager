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

package org.tinymediamanager.core.movie.filenaming;

import org.apache.commons.lang3.StringUtils;
import org.tinymediamanager.core.IFileNaming;

/**
 * The Enum MovieDiscartNaming.
 * 
 * @author Manuel Laggner
 */
public enum MovieDiscartNaming implements IFileNaming {

  /** [filename]-disc.* */
  FILENAME_DISC {
    @Override
    public String getFilename(String basename, String extension) {
      return StringUtils.isNotBlank(basename) ? basename + "-disc." + extension : "";
    }
  },

  /** disc.* */
  DISC {
    @Override
    public String getFilename(String basename, String extension) {
      return "disc." + extension;
    }
  },

  /** [filename]-discart.* */
  FILENAME_DISCART {
    @Override
    public String getFilename(String basename, String extension) {
      return StringUtils.isNotBlank(basename) ? basename + "-discart." + extension : "";
    }
  },

  /** discart.* */
  DISCART {
    @Override
    public String getFilename(String basename, String extension) {
      return "discart." + extension;
    }
  }
}
