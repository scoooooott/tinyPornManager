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
import java.io.InputStream;
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
import org.tinymediamanager.core.ImageCache;
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
    engine.registerNamedRenderer(new ArtworkCopyRenderer(pathToExport));

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
    Utils.deleteFileSafely(listExportFile);

    Map<String, Object> root = new HashMap<String, Object>();
    root.put("tvShows", new ArrayList<T>(tvShowsToExport));
    String output = engine.transform(listTemplate, root);
    FileUtils.writeStringToFile(listExportFile, output, "UTF-8");
    LOGGER.info("movie list generated: " + listExportFile.getAbsolutePath());

    if (StringUtils.isNotBlank(detailTemplate)) {
      for (MediaEntity me : tvShowsToExport) {
        TvShow show = (TvShow) me;
        // create a TV show dir
        File showDir = new File(pathToExport, getFilename(show));
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
            if (mfs.isEmpty()) {
              continue;
            }

            File seasonDir = new File(showDir, TvShowRenamer.generateSeasonDir("", episode));
            if (!showDir.exists()) {
              seasonDir.mkdirs();
            }

            String episodeFileName = getFilename(episode) + "." + fileExtension;
            File episodeExportFile = new File(seasonDir, episodeFileName);
            root = new HashMap<String, Object>();
            root.put("episode", episode);
            output = engine.transform(episodeTemplate, root);
            FileUtils.writeStringToFile(episodeExportFile, output, "UTF-8");
          }
        }
      }
    }

    // copy all non .jtme/template.conf files to destination dir
    File[] templateContent = templateDir.listFiles();
    if (templateContent != null) {
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
  }

  private static String getFilename(MediaEntity entity) {
    if (entity instanceof TvShow) {
      return TvShowRenamer.createDestination("$N ($Y)", (TvShow) entity, new ArrayList<TvShowEpisode>());
    }
    if (entity instanceof TvShowEpisode) {
      TvShowEpisode episode = (TvShowEpisode) entity;
      List<MediaFile> mfs = episode.getMediaFiles(MediaFileType.VIDEO);
      return FilenameUtils.getBaseName(TvShowRenamer.generateFilename(episode.getTvShow(), mfs.get(0)));
    }
    return "";
  }

  /*******************************************************************************
   * helper classes
   *******************************************************************************/
  private class TvShowFilenameRenderer implements NamedRenderer {
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

  /**
   * this renderer is used to copy artwork into the exported template
   * 
   * @author Manuel Laggner
   */
  private class ArtworkCopyRenderer implements NamedRenderer {
    private String pathToExport;

    public ArtworkCopyRenderer(String pathToExport) {
      this.pathToExport = pathToExport;
    }

    @Override
    public RenderFormatInfo getFormatInfo() {
      return null;
    }

    @Override
    public String getName() {
      return "copyArtwork";
    }

    @Override
    public Class<?>[] getSupportedClasses() {
      return new Class[] { TvShow.class, TvShowEpisode.class };
    }

    @Override
    public String render(Object o, String pattern, Locale locale) {
      if (o instanceof TvShow || o instanceof TvShowEpisode) {
        MediaEntity entity = (MediaEntity) o;
        Map<String, Object> parameters = parseParameters(pattern);

        MediaFile mf = entity.getArtworkMap().get(parameters.get("type"));
        if (mf == null || !mf.isGraphic()) {
          return null;
        }

        String filename = parameters.get("destination") + File.separator + getFilename(entity) + "-" + mf.getType();
        try {
          // we need to rescale the image; scale factor is fixed to
          if (parameters.get("thumb") == Boolean.TRUE) {
            filename += ".thumb." + FilenameUtils.getExtension(mf.getFilename());
            int width = 150;
            if (parameters.get("width") != null) {
              width = (int) parameters.get("width");
            }
            InputStream is = ImageCache.scaleImage(mf.getFile(), width);
            FileUtils.copyInputStreamToFile(is, new File(pathToExport, filename));
          }
          else {
            filename += "." + FilenameUtils.getExtension(mf.getFilename());
            FileUtils.copyFile(mf.getFile(), new File(pathToExport, filename));
          }
        }
        catch (Exception e) {
          LOGGER.warn("could not copy artwork file: " + e.getMessage());
          return null;
        }

        return filename;
      }
      return null;
    }

    /**
     * parse the parameters out of the parameters string
     * 
     * @param parameters
     *          the parameters as string
     * @return a map containing all parameters
     */
    private Map<String, Object> parseParameters(String parameters) {
      Map<String, Object> parameterMap = new HashMap<>();

      // defaults
      parameterMap.put("thumb", Boolean.FALSE);
      parameterMap.put("destination", "images");

      String[] details = parameters.split(",");
      for (int x = 0; x < details.length; x++) {
        String key = "";
        String value = "";
        try {
          String[] d = details[x].split("=");
          key = d[0].trim();
          value = d[1].trim();
        }
        catch (Exception e) {
        }

        if (StringUtils.isAnyBlank(key, value)) {
          continue;
        }

        switch (key.toLowerCase()) {
          case "type":
            MediaFileType type = MediaFileType.valueOf(value.toUpperCase());
            if (type != null) {
              parameterMap.put(key, type);
            }
            break;

          case "destination":
            parameterMap.put("destination", value);
            break;

          case "thumb":
            parameterMap.put(key, Boolean.parseBoolean(value));
            break;

          case "width":
            try {
              parameterMap.put(key, Integer.parseInt(value));
            }
            catch (Exception e) {
            }
            break;

          default:
            break;
        }
      }

      return parameterMap;
    }
  }
}
