/*
 * Copyright 2012 - 2019 Manuel Laggner
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

package org.tinymediamanager.core.tvshow;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.core.MediaCertification;
import org.tinymediamanager.core.tvshow.entities.TvShow;
import org.tinymediamanager.core.tvshow.entities.TvShowEpisode;

/**
 * a collection of various helpers for the TV show module
 *
 * @author Manuel Laggner
 */
public class TvShowHelpers {
  private static final Logger LOGGER = LoggerFactory.getLogger(TvShow.class);

  private TvShowHelpers() {
    // hide constructor for utility classes
  }

  /**
   * Parses a given certification string for the localized country setup in setting.
   *
   * @param name
   *          certification string like "USA:R / UK:15 / Sweden:15"
   * @return the localized certification if found, else *ANY* language cert found
   */
  // <certification>USA:R / UK:15 / Sweden:15 / Spain:18 / South Korea:15 /
  // Singapore:NC-16 / Portugal:M/16 / Philippines:R-18 / Norway:15 / New
  // Zealand:M / Netherlands:16 / Malaysia:U / Malaysia:18PL / Ireland:18 /
  // Iceland:16 / Hungary:18 / Germany:16 / Finland:K-15 / Canada:18A /
  // Canada:18+ / Brazil:16 / Australia:M / Argentina:16</certification>

  public static MediaCertification parseCertificationStringForTvShowSetupCountry(String name) {
    MediaCertification cert = MediaCertification.UNKNOWN;
    name = name.trim();
    if (name.contains("/")) {
      // multiple countries
      String[] countries = name.split("/");
      // first try to find by setup CertLanguage
      for (String c : countries) {
        c = c.trim();
        if (c.contains(":")) {
          String[] cs = c.split(":");
          cert = MediaCertification.getCertification(TvShowModuleManager.SETTINGS.getCertificationCountry(), cs[1]);
          if (cert != MediaCertification.UNKNOWN) {
            return cert;
          }
        }
        else {
          cert = MediaCertification.getCertification(TvShowModuleManager.SETTINGS.getCertificationCountry(), c);
          if (cert != MediaCertification.UNKNOWN) {
            return cert;
          }
        }
      }
      // still not found localized cert? parse the name to find *ANY*
      // certificate
      for (String c : countries) {
        c = c.trim();
        if (c.contains(":")) {
          String[] cs = c.split(":");
          cert = MediaCertification.findCertification(cs[1]);
          if (cert != MediaCertification.UNKNOWN) {
            return cert;
          }
        }
        else {
          cert = MediaCertification.findCertification(c);
          if (cert != MediaCertification.UNKNOWN) {
            return cert;
          }
        }
      }
    }
    else {
      // no slash, so only one country
      if (name.contains(":")) {
        String[] cs = name.split(":");
        cert = MediaCertification.getCertification(TvShowModuleManager.SETTINGS.getCertificationCountry(), cs[1].trim());
        if (cert == MediaCertification.UNKNOWN) {
          cert = MediaCertification.findCertification(cs[1].trim());
        }
      }
      else {
        // no country? try to find only by name
        cert = MediaCertification.getCertification(TvShowModuleManager.SETTINGS.getCertificationCountry(), name.trim());
      }
    }
    // still not found localized cert? parse the name to find *ANY* certificate
    if (cert == MediaCertification.UNKNOWN) {
      cert = MediaCertification.findCertification(name);
    }
    return cert;
  }

  /**
   * try to detect the TV show folder by comparing the paths of the media files
   * 
   * @param tvShow
   *          the TV show to analyze
   * @param season
   *          the season for what we would like to have the season folder
   * @return the path to the season folder relative to the TV show folder or the default season folder name from the renamer settings
   */
  public static String detectSeasonFolder(TvShow tvShow, int season) {
    List<String> subPaths = new ArrayList<>();

    Path tvShowPath = tvShow.getPathNIO();
    List<TvShowEpisode> episodes = tvShow.getEpisodesForSeason(season);

    try {
      // compare all episodes for the given season
      for (TvShowEpisode episode : episodes) {
        Path videoFilePath = episode.getMainVideoFile().getFileAsPath().getParent();

        // split up the relative path into its path junks
        Path relativePath = tvShowPath.relativize(videoFilePath);
        int subfolders = relativePath.getNameCount();

        for (int i = 1; i <= subfolders; i++) {
          subPaths.add(relativePath.subpath(0, i).toString());
        }
      }
    }
    catch (Exception e) {
      LOGGER.debug("could not extract season folder: {}", e.getMessage());
    }

    if (subPaths.isEmpty()) {
      return "";
    }

    // group them
    Map<String, Long> subPathCounts = subPaths.stream().collect(Collectors.groupingBy(s -> s, Collectors.counting()));

    // take the highest count
    Map.Entry<String, Long> entry = subPathCounts.entrySet().stream().max(Comparator.comparing(Map.Entry::getValue)).get();

    // if there are at least 80% of all episodes having this subfolder, take it
    if (entry.getValue() >= 0.8 * episodes.size()) {
      return entry.getKey();
    }

    // just fake an episode here, since the real foldername can only be generated out of the episode
    // create a dummy episode to inject the season number
    TvShowEpisode episode = new TvShowEpisode();
    episode.setSeason(season);

    return TvShowRenamer.getSeasonFoldername(tvShow, episode);
  }
}
