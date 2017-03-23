/*
 * Copyright 2012 - 2017 Manuel Laggner
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
import java.io.InputStream;
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
    LOGGER.info("preparing tv show export; using " + properties.getProperty("name"));

    // register own renderers
    engine.registerNamedRenderer(new NamedDateRenderer());
    engine.registerNamedRenderer(new TvShowFilenameRenderer());
    engine.registerNamedRenderer(new ArtworkCopyRenderer(exportDir));

    // prepare export destination
    if (!Files.exists(exportDir)) {
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

    Map<String, Object> root = new HashMap<>();
    root.put("tvShows", new ArrayList<>(tvShowsToExport));
    String output = engine.transform(listTemplate, root);
    Utils.writeStringToFile(listExportFile, output);
    LOGGER.info("movie list generated: " + listExportFile);

    if (StringUtils.isNotBlank(detailTemplate)) {
      for (MediaEntity me : tvShowsToExport) {
        TvShow show = (TvShow) me;
        // create a TV show dir
        Path showDir = exportDir.resolve(getFilename(show));
        // nah - to dangerous if you choose some root folder!
        // if (Files.isDirectory(showDir)) {
        // Utils.deleteDirectoryRecursive(showDir);
        // }
        Files.createDirectory(showDir);

        Path detailsExportFile = showDir.resolve("tvshow." + fileExtension);
        root = new HashMap<>();
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

  /**
   * this renderer is used to copy artwork into the exported template
   * 
   * @author Manuel Laggner
   */
  private class ArtworkCopyRenderer implements NamedRenderer {
    private Path pathToExport;

    public ArtworkCopyRenderer(Path pathToExport) {
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
        Map<String, Object> parameters = new HashMap<String, Object>();
        if (pattern != null) {
          parameters = parseParameters(pattern);
        }

        MediaFile mf = entity.getArtworkMap().get(parameters.get("type"));
        if (mf == null || !mf.isGraphic()) {
          return null;
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
            InputStream is = ImageCache.scaleImage(mf.getFileAsPath(), width);
            Files.copy(is, imageDir.resolve(filename), StandardCopyOption.REPLACE_EXISTING);
          }
          else {
            filename += "." + FilenameUtils.getExtension(mf.getFilename());
            Files.copy(mf.getFileAsPath(), imageDir.resolve(filename), StandardCopyOption.REPLACE_EXISTING);
          }
        }
        catch (Exception e) {
          LOGGER.error("could not copy artwork file: ", e);
          return "";
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

        switch (key.toLowerCase(Locale.ROOT)) {
          case "type":
            MediaFileType type = MediaFileType.valueOf(value.toUpperCase(Locale.ROOT));
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
