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

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.core.MediaEntityExporter;
import org.tinymediamanager.core.MediaFileType;
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

  public TvShowExporter(String pathToTemplate) throws Exception {
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
  public <T extends MediaEntity> void export(List<T> tvShowsToExport, String pathToExport) throws Exception {
    LOGGER.info("preparing tv show export; using " + properties.getProperty("name"));

    // register own renderers
    engine.registerNamedRenderer(new NamedDateRenderer());
    engine.registerNamedRenderer(new TvShowFilenameRenderer());

    // prepare export destination
    File exportDir = new File(pathToExport);
    if (!exportDir.exists()) {
      if (!exportDir.mkdirs()) {
        throw new Exception("error creating export directory");
      }
    }

    // prepare listfile
    File listExportFile = null;
    if (fileExtension.equalsIgnoreCase("html")) {
      listExportFile = new File(exportDir, "index.html");
    }
    if (fileExtension.equalsIgnoreCase("xml")) {
      listExportFile = new File(exportDir, "tvshows.xml");
    }
    if (fileExtension.equalsIgnoreCase("csv")) {
      listExportFile = new File(exportDir, "tvshows.csv");
    }
    if (listExportFile == null) {
      throw new Exception("error creating tv show list file");
    }

    // load episode template
    String episodeTemplateFile = properties.getProperty("episode");
    String episodeTemplate = "";
    if (StringUtils.isNotBlank(episodeTemplateFile)) {
      episodeTemplate = FileUtils.readFileToString(new File(templateDir, episodeTemplateFile), "UTF-8");
    }

    // create the list
    LOGGER.info("generating tv show list");
    FileUtils.deleteQuietly(listExportFile);

    Map<String, Object> root = new HashMap<String, Object>();
    root.put("tvShows", new ArrayList<T>(tvShowsToExport));
    String output = engine.transform(listTemplate, root);
    FileUtils.writeStringToFile(listExportFile, output, "UTF-8");
    LOGGER.info("movie list generated: " + listExportFile.getAbsolutePath());

    if (StringUtils.isNotBlank(detailTemplate)) {
      for (MediaEntity me : tvShowsToExport) {
        TvShow show = (TvShow) me;
        // create a TV show dir
        File showDir = new File(pathToExport + File.separator + TvShowRenamer.createDestination("$T ($Y)", show, new ArrayList<TvShowEpisode>()));
        if (showDir.exists()) {
          FileUtils.deleteQuietly(showDir);
        }
        showDir.mkdirs();

        File detailsExportFile = new File(showDir, "tvshow." + fileExtension);
        root = new HashMap<String, Object>();
        root.put("tvShow", show);

        output = engine.transform(detailTemplate, root);
        FileUtils.writeStringToFile(detailsExportFile, output, "UTF-8");

        if (StringUtils.isNotBlank(episodeTemplate)) {
          for (TvShowEpisode episode : show.getEpisodes()) {
            List<MediaFile> mfs = episode.getMediaFiles(MediaFileType.VIDEO);
            if (!mfs.isEmpty()) {
              File seasonDir = new File(showDir, TvShowRenamer.generateSeasonDir("", episode));
              if (!showDir.exists()) {
                seasonDir.mkdirs();
              }

              String episodeFileName = FilenameUtils.getBaseName(TvShowRenamer.generateFilename(show, mfs.get(0))) + "." + fileExtension;
              File episodeExportFile = new File(seasonDir, episodeFileName);
              root = new HashMap<String, Object>();
              root.put("episode", episode);
              output = engine.transform(episodeTemplate, root);
              FileUtils.writeStringToFile(episodeExportFile, output, "UTF-8");
            }
          }
        }
      }
    }

    // copy all non .jtme/template.conf files to destination dir
    File[] templateContent = templateDir.listFiles();
    for (File fileInTemplateDir : templateContent) {
      if (fileInTemplateDir.getName().endsWith(".jmte")) {
        continue;
      }
      if (fileInTemplateDir.getName().endsWith("template.conf")) {
        continue;
      }
      if (fileInTemplateDir.isFile()) {
        FileUtils.copyFileToDirectory(fileInTemplateDir, exportDir);
      }
      if (fileInTemplateDir.isDirectory()) {
        FileUtils.copyDirectoryToDirectory(fileInTemplateDir, exportDir);
      }
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
