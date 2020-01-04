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

package org.tinymediamanager.core.tvshow.filenaming;

import java.io.File;

import org.apache.commons.lang3.StringUtils;
import org.tinymediamanager.core.tvshow.ITvShowSeasonFileNaming;
import org.tinymediamanager.core.tvshow.TvShowHelpers;
import org.tinymediamanager.core.tvshow.TvShowModuleManager;
import org.tinymediamanager.core.tvshow.entities.TvShow;

/**
 * The Enum TvShowSeasonBannerNaming.
 * 
 * @author Manuel Laggner
 */
public enum TvShowSeasonBannerNaming implements ITvShowSeasonFileNaming {
  /** seasonXX-banner.* */
  SEASON_BANNER {
    @Override
    public String getFilename(TvShow tvShow, int season, String extension) {
      if (season == 0 && TvShowModuleManager.SETTINGS.isSpecialSeason()) {
        return "season-specials-banner." + extension;
      }
      else if (season > -1) {
        return String.format("season%02d-banner.%s", season, extension);
      }
      else {
        return "";
      }
    }
  },

  /** season_folder/seasonXX-banner.* */
  SEASON_FOLDER {
    @Override
    public String getFilename(TvShow tvShow, int season, String extension) {
      String seasonFoldername = TvShowHelpers.detectSeasonFolder(tvShow, season);

      // check whether the season folder name exists or not; do not create it just for the artwork!
      if (StringUtils.isBlank(seasonFoldername)) {
        // no season folder name in the templates found - fall back to the the show base filename style
        return SEASON_BANNER.getFilename(tvShow, season, extension);
      }

      String filename = seasonFoldername + File.separator;

      if (season == 0 && TvShowModuleManager.SETTINGS.isSpecialSeason()) {
        filename += "season-specials-banner";
      }
      else if (season > -1) {
        filename += String.format("season%02d-banner", season);
      }
      else {
        return "";
      }
      return filename + "." + extension;
    }
  }
}
