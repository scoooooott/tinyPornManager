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
package org.tinymediamanager.core.tvshow;

import static org.tinymediamanager.core.tvshow.TvShowSettings.DEFAULT_RENAMER_FILE_PATTERN;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.file.DirectoryStream;
import java.nio.file.FileAlreadyExistsException;
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
import org.tinymediamanager.core.ImageUtils;
import org.tinymediamanager.core.MediaEntityExporter;
import org.tinymediamanager.core.MediaFileType;
import org.tinymediamanager.core.Utils;
import org.tinymediamanager.core.entities.MediaEntity;
import org.tinymediamanager.core.entities.MediaFile;
import org.tinymediamanager.core.jmte.NamedDateRenderer;
import org.tinymediamanager.core.jmte.NamedFirstCharacterRenderer;
import org.tinymediamanager.core.jmte.NamedNumberRenderer;
import org.tinymediamanager.core.jmte.NamedUpperCaseRenderer;
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
  private static final Logger LOGGER = LoggerFactory.getLogger(TvShowExporter.class);

  public TvShowExporter(Path pathToTemplate) throws Exception {
    super(pathToTemplate, TemplateType.TV_SHOW);
  }

  /**
   * exports movie list according to template file.
   * 
   * @param tvShowsToExport
   *          list of movies
   * @param exportDir
   *          the path to export
   * @throws Exception
   *           the exception
   */
  @Override
  public <T extends MediaEntity> void export(List<T> tvShowsToExport, Path exportDir) throws Exception {
    LOGGER.info("preparing tv show export; using {}", properties.getProperty("name"));

    if (cancel) {
      return;
    }

    // register own renderers
    engine.registerNamedRenderer(new NamedDateRenderer());
    engine.registerNamedRenderer(new NamedNumberRenderer());
    engine.registerNamedRenderer(new NamedUpperCaseRenderer());
    engine.registerNamedRenderer(new NamedFirstCharacterRenderer());
    engine.registerNamedRenderer(new TvShowFilenameRenderer());
    engine.registerNamedRenderer(new TvShowArtworkCopyRenderer(exportDir));

    // prepare export destination
    if (!Files.exists(exportDir)) {
      Files.createDirectories(exportDir);
    }

    // prepare listfile
    Path listExportFile = exportDir.resolve("tvshows." + fileExtension);
    if (listExportFile == null) {
      throw new FileNotFoundException("error creating tv show list file");
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

    Map<String, Object> root = new HashMap<>();
    root.put("tvShows", new ArrayList<>(tvShowsToExport));
    String output = engine.transform(listTemplate, root);
    Utils.writeStringToFile(listExportFile, output);
    LOGGER.info("TvShow list generated: {}", listExportFile);

    if (StringUtils.isNotBlank(detailTemplate)) {
      for (T me : tvShowsToExport) {
        if (cancel) {
          return;
        }

        TvShow show = (TvShow) me;
        // create a TV show dir
        Path showDir = exportDir.resolve(getFilename(show));
        try {
          Files.createDirectory(showDir);
        }
        catch (FileAlreadyExistsException e) {
          LOGGER.debug("Folder already exists...");
        }

        Path detailsExportFile = showDir.resolve("tvshow." + fileExtension);
        root = new HashMap<>();
        root.put("tvShow", show);

        output = engine.transform(detailTemplate, root);
        Utils.writeStringToFile(detailsExportFile, output);

        if (StringUtils.isNotBlank(episodeTemplate)) {
          for (TvShowEpisode episode : show.getEpisodes()) {
            if (cancel) {
              return;
            }

            List<MediaFile> mfs = episode.getMediaFiles(MediaFileType.VIDEO);
            if (!mfs.isEmpty()) {
              Path seasonDir = showDir.resolve(TvShowRenamer.getSeasonFoldername("", episode.getTvShow(), episode));
              if (!Files.isDirectory(seasonDir)) {
                Files.createDirectory(seasonDir);
              }

              String episodeFileName = getFilename(episode) + "." + fileExtension;
              Path episodeExportFile = seasonDir.resolve(episodeFileName);
              root = new HashMap<>();
              root.put("episode", episode);
              output = engine.transform(episodeTemplate, root);
              Utils.writeStringToFile(episodeExportFile, output);
            }
          }
        }
      }
    }

    if (cancel) {
      return;
    }

    // copy all non .jtme/template.conf files to destination dir
    try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(templateDir)) {
      for (Path path : directoryStream) {
        if (Utils.isRegularFile(path)) {
          if (path.getFileName().toString().endsWith(".jmte") || path.getFileName().toString().endsWith("template.conf")) {
            continue;
          }
          Files.copy(path, exportDir.resolve(path.getFileName()), StandardCopyOption.REPLACE_EXISTING);
        }
        else if (Files.isDirectory(path)) {
          Utils.copyDirectoryRecursive(path, exportDir.resolve(path.getFileName()));
        }
      }
    }
    catch (IOException ex) {
      LOGGER.error("could not copy resources: ", ex);
    }
  }

  private static String getFilename(MediaEntity entity) {
    if (entity instanceof TvShow) {
      return TvShowRenamer.createDestination("${showTitle} (${showYear})", (TvShow) entity);
    }
    if (entity instanceof TvShowEpisode) {
      TvShowEpisode episode = (TvShowEpisode) entity;
      List<MediaFile> mfs = episode.getMediaFiles(MediaFileType.VIDEO);
      return FilenameUtils
          .getBaseName(TvShowRenamer.generateEpisodeFilenames(DEFAULT_RENAMER_FILE_PATTERN, episode.getTvShow(), mfs.get(0)).get(0).getFilename());
    }
    return "";
  }

  /*******************************************************************************
   * helper classes
   *******************************************************************************/
  private static class TvShowFilenameRenderer implements NamedRenderer {
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
    public String render(Object o, String pattern, Locale locale, Map<String, Object> model) {
      Map<String, Object> parameters = new HashMap<>();
      if (pattern != null) {
        parameters = parseParameters(pattern);
      }
      if (o instanceof TvShow) {
        TvShow show = (TvShow) o;
        String filename = getFilename(show);
        if (parameters.get("escape") == Boolean.TRUE) {
          try {
            filename = URLEncoder.encode(filename, "UTF-8").replace("+", "%20");
          }
          catch (Exception ignored) {
          }
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

      String[] details = parameters.split(",");
      for (String detail : details) {
        String key = "";
        String value = "";
        try {
          String[] d = detail.split("=");
          key = d[0].trim();
          value = d[1].trim();
        }
        catch (Exception ignored) {
        }

        if (StringUtils.isAnyBlank(key, value)) {
          continue;
        }

        switch (key.toLowerCase(Locale.ROOT)) {
          case "escape":
            parameterMap.put(key, Boolean.parseBoolean(value));
            break;

          default:
            break;
        }
      }

      return parameterMap;

    }
  }

  /**
   * this renderer is used to copy artwork into the exported template
   * 
   * @author Manuel Laggner
   */
  private static class TvShowArtworkCopyRenderer extends ArtworkCopyRenderer {

    public TvShowArtworkCopyRenderer(Path pathToExport) {
      super(pathToExport);
    }

    @Override
    public Class<?>[] getSupportedClasses() {
      return new Class[] { TvShow.class, TvShowEpisode.class };
    }

    @Override
    public String render(Object o, String pattern, Locale locale, Map<String, Object> model) {
      if (o instanceof TvShow || o instanceof TvShowEpisode) {
        MediaEntity entity = (MediaEntity) o;
        Map<String, Object> parameters = new HashMap<>();
        if (pattern != null) {
          parameters = parseParameters(pattern);
        }

        MediaFile mf = entity.getArtworkMap().get(parameters.get("type"));
        if (mf == null || !mf.isGraphic()) {
          if (StringUtils.isNotBlank((String) parameters.get("default"))) {
            return (String) parameters.get("default");
          }
          return ""; // pass an emtpy string to prevent tvShow.toString() gets triggered by jmte
        }

        String filename = getFilename(entity) + "-" + mf.getType();

        Path imageDir;
        if (StringUtils.isNotBlank((String) parameters.get("destination"))) {
          imageDir = pathToExport.resolve((String) parameters.get("destination"));
        }
        else {
          imageDir = pathToExport;
        }

        try {
          // create the image dir
          if (!Files.exists(imageDir)) {
            Files.createDirectory(imageDir);
          }

          // we need to rescale the image; scale factor is fixed to
          if (parameters.get("thumb") == Boolean.TRUE) {
            filename += ".thumb." + FilenameUtils.getExtension(mf.getFilename());
            int width = 150;
            if (parameters.get("width") != null) {
              width = (int) parameters.get("width");
            }
            InputStream is = ImageUtils.scaleImage(mf.getFileAsPath(), width);
            Files.copy(is, imageDir.resolve(filename), StandardCopyOption.REPLACE_EXISTING);
          }
          else {
            filename += "." + FilenameUtils.getExtension(mf.getFilename());
            Files.copy(mf.getFileAsPath(), imageDir.resolve(filename), StandardCopyOption.REPLACE_EXISTING);
          }
        }
        catch (Exception e) {
          LOGGER.error("could not copy artwork file: ", e);
          if (StringUtils.isNotBlank((String) parameters.get("default"))) {
            return (String) parameters.get("default");
          }
          return ""; // pass an emtpy string to prevent tvShow.toString() gets triggered by jmte
        }

        if (parameters.get("escape") == Boolean.TRUE) {
          try {
            filename = URLEncoder.encode(filename, "UTF-8").replace("+", "%20");
          }
          catch (Exception ignored) {
          }
        }

        return filename;
      }
      return ""; // pass an emtpy string to prevent obj.toString() gets triggered by jmte
    }
  }
}
