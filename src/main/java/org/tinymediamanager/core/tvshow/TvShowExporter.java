/*
 * Copyright 2012 - 2015 Manuel Laggner
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

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.core.MediaEntityExporter;
import org.tinymediamanager.core.MediaFileType;
import org.tinymediamanager.core.Utils;
import org.tinymediamanager.core.entities.MediaEntity;
import org.tinymediamanager.core.entities.MediaFile;
import org.tinymediamanager.core.tvshow.entities.TvShow;
import org.tinymediamanager.core.tvshow.entities.TvShowEpisode;

import com.floreysoft.jmte.NamedRenderer;
import com.floreysoft.jmte.RenderFormatInfo;

/**
 * The class TvShowExporter. To export TV shows via templates
 * 
 * @author Manuel Laggner
 */
public class TvShowExporter extends MediaEntityExporter {
  private final static Logger LOGGER = LoggerFactory.getLogger(TvShowExporter.class);

  public TvShowExporter(Path pathToTemplate) throws Exception {
    super(pathToTemplate, TemplateType.TV_SHOW);
  }

  /**
   * exports movie list according to template file.
   * 
   * @param tvShowsToExport
   *          list of movies
   * @param pathToExport
   *          the path to export
   * @throws Exception
   *           the exception
   */
  @Override
  public <T extends MediaEntity> void export(List<T> tvShowsToExport, Path exportDir) throws Exception {
    LOGGER.info("preparing tv show export; using " + properties.getProperty("name"));

    // register own renderers
    engine.registerNamedRenderer(new NamedDateRenderer());
    engine.registerNamedRenderer(new TvShowFilenameRenderer());

    // prepare export destination
    if (Files.notExists(exportDir)) {
      try {
        Files.createDirectories(exportDir);
      }
      catch (Exception e) {
        throw new Exception("error creating export directory");
      }
    }

    // prepare listfile
    Path listExportFile = null;
    if (fileExtension.equalsIgnoreCase("html")) {
      listExportFile = exportDir.resolve("index.html");
    }
    if (fileExtension.equalsIgnoreCase("xml")) {
      listExportFile = exportDir.resolve("tvshows.xml");
    }
    if (fileExtension.equalsIgnoreCase("csv")) {
      listExportFile = exportDir.resolve("tvshows.csv");
    }
    if (listExportFile == null) {
      throw new Exception("error creating tv show list file");
    }

    // load episode template
    String episodeTemplateFile = properties.getProperty("episode");
    String episodeTemplate = "";
    if (StringUtils.isNotBlank(episodeTemplateFile)) {
      episodeTemplate = Utils.readFileToString(templateDir.resolve(episodeTemplateFile));
    }

    // create the list
    LOGGER.info("generating tv show list");
    Utils.deleteFileSafely(listExportFile);

    Map<String, Object> root = new HashMap<String, Object>();
    root.put("tvShows", new ArrayList<T>(tvShowsToExport));
    String output = engine.transform(listTemplate, root);
    Utils.writeStringToFile(listExportFile, output);
    LOGGER.info("movie list generated: " + listExportFile);

    if (StringUtils.isNotBlank(detailTemplate)) {
      for (MediaEntity me : tvShowsToExport) {
        TvShow show = (TvShow) me;
        // create a TV show dir
        Path showDir = exportDir.resolve(TvShowRenamer.createDestination("$N ($Y)", show, new ArrayList<TvShowEpisode>()));
        if (Files.isDirectory(showDir)) {
          Utils.deleteDirectoryRecursive(showDir);
        }
        Files.createDirectory(showDir);

        Path detailsExportFile = showDir.resolve("tvshow." + fileExtension);
        root = new HashMap<String, Object>();
        root.put("tvShow", show);

        output = engine.transform(detailTemplate, root);
        Utils.writeStringToFile(detailsExportFile, output);

        if (StringUtils.isNotBlank(episodeTemplate)) {
          for (TvShowEpisode episode : show.getEpisodes()) {
            List<MediaFile> mfs = episode.getMediaFiles(MediaFileType.VIDEO);
            if (!mfs.isEmpty()) {
              Path seasonDir = showDir.resolve(TvShowRenamer.generateSeasonDir("", episode));
              if (!Files.isDirectory(seasonDir)) {
                Files.createDirectory(seasonDir);
              }

              String episodeFileName = FilenameUtils.getBaseName(TvShowRenamer.generateFilename(show, mfs.get(0))) + "." + fileExtension;
              Path episodeExportFile = seasonDir.resolve(episodeFileName);
              root = new HashMap<String, Object>();
              root.put("episode", episode);
              output = engine.transform(episodeTemplate, root);
              Utils.writeStringToFile(episodeExportFile, output);
            }
          }
        }
      }
    }

    // copy all non .jtme/template.conf files to destination dir
    try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(templateDir)) {
      for (Path path : directoryStream) {
        if (Files.isRegularFile(path)) {
          if (path.getFileName().endsWith(".jmte") || path.getFileName().endsWith("template.conf")) {
            continue;
          }
          Files.copy(path, exportDir, StandardCopyOption.REPLACE_EXISTING);
        }
        else if (Files.isDirectory(path)) {
          Utils.copyDirectoryRecursive(path, exportDir);
        }
      }
    }
    catch (IOException ex) {
    }
  }

  /*******************************************************************************
   * helper classes
   *******************************************************************************/
  public static class TvShowFilenameRenderer implements NamedRenderer {
    @Override
    public RenderFormatInfo getFormatInfo() {
      return null;
    }

    @Override
    public String getName() {
      return "filename";
    }

    @Override
    public Class<?>[] getSupportedClasses() {
      return new Class[] { TvShow.class };
    }

    @Override
    public String render(Object o, String pattern, Locale locale) {
      if (o instanceof TvShow) {
        TvShow show = (TvShow) o;
        return TvShowRenamer.generateTvShowDir(show);
      }
      return null;
    }
  }
}
