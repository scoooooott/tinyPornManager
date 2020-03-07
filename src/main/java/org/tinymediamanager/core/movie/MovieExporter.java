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
package org.tinymediamanager.core.movie;

import static org.tinymediamanager.core.movie.MovieSettings.DEFAULT_RENAMER_FILE_PATTERN;

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
import org.tinymediamanager.core.Utils;
import org.tinymediamanager.core.entities.MediaEntity;
import org.tinymediamanager.core.entities.MediaFile;
import org.tinymediamanager.core.jmte.NamedDateRenderer;
import org.tinymediamanager.core.jmte.NamedFirstCharacterRenderer;
import org.tinymediamanager.core.jmte.NamedNumberRenderer;
import org.tinymediamanager.core.jmte.NamedUpperCaseRenderer;
import org.tinymediamanager.core.movie.entities.Movie;

import com.floreysoft.jmte.NamedRenderer;
import com.floreysoft.jmte.RenderFormatInfo;

/**
 * This class exports a list of movies to various formats according to templates.
 * 
 * @author Myron Boyle / Manuel Laggner
 */
public class MovieExporter extends MediaEntityExporter {
  private static final Logger LOGGER = LoggerFactory.getLogger(MovieExporter.class);

  public MovieExporter(Path pathToTemplate) throws Exception {
    super(pathToTemplate, TemplateType.MOVIE);
  }

  /**
   * exports movie list according to template file.
   * 
   * @param moviesToExport
   *          list of movies
   * @param exportDir
   *          the path to export
   * @throws Exception
   *           the exception
   */
  @Override
  public <T extends MediaEntity> void export(List<T> moviesToExport, Path exportDir) throws Exception {
    LOGGER.info("preparing movie export; using {}", properties.getProperty("name"));

    if (cancel) {
      return;
    }

    // register own renderers
    engine.registerNamedRenderer(new NamedDateRenderer());
    engine.registerNamedRenderer(new NamedNumberRenderer());
    engine.registerNamedRenderer(new NamedUpperCaseRenderer());
    engine.registerNamedRenderer(new NamedFirstCharacterRenderer());
    engine.registerNamedRenderer(new MovieFilenameRenderer());
    engine.registerNamedRenderer(new MovieArtworkCopyRenderer(exportDir));

    // prepare export destination
    if (!Files.exists(exportDir)) {
      Files.createDirectories(exportDir);
    }

    // prepare listfile
    Path listExportFile = exportDir.resolve("movielist." + fileExtension);
    if (listExportFile == null) {
      throw new FileNotFoundException("error creating movie list file");
    }

    // create list
    LOGGER.info("generating movie list");
    Utils.deleteFileSafely(listExportFile);

    Map<String, Object> root = new HashMap<>();
    root.put("movies", new ArrayList<>(moviesToExport));

    String output = engine.transform(listTemplate, root);

    Utils.writeStringToFile(listExportFile, output);
    LOGGER.info("movie list generated: {}", listExportFile);

    // create details for
    if (StringUtils.isNotBlank(detailTemplate)) {
      Path detailsDir = exportDir.resolve("movies");
      try {
        Files.createDirectory(detailsDir);
      }
      catch (FileAlreadyExistsException e) {
        LOGGER.debug("Folder already exists...");
      }

      for (T me : moviesToExport) {
        if (cancel) {
          return;
        }

        Movie movie = (Movie) me;
        LOGGER.debug("processing movie {}", movie.getTitle());
        // get preferred movie name like set up in movie renamer
        String detailFilename = MovieRenamer.createDestinationForFilename(MovieModuleManager.SETTINGS.getRenamerFilename(), movie);
        if (StringUtils.isBlank(detailFilename)) {
          detailFilename = movie.getVideoBasenameWithoutStacking();
        }
        Path detailsExportFile = detailsDir.resolve(detailFilename + "." + fileExtension);

        root = new HashMap<>();
        root.put("movie", movie);

        output = engine.transform(detailTemplate, root);
        Utils.writeStringToFile(detailsExportFile, output);

      }

      LOGGER.info("movie detail pages generated: {}", exportDir);
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

  private static String getMovieFilename(Movie movie) {
    String filename = MovieRenamer.createDestinationForFilename(MovieModuleManager.SETTINGS.getRenamerFilename(), movie);
    if (StringUtils.isNotBlank(filename)) {
      return filename;
    }

    // fallback (no renamer settings)
    filename = MovieRenamer.createDestinationForFilename(DEFAULT_RENAMER_FILE_PATTERN, movie);
    if (StringUtils.isNotBlank(filename)) {
      return filename;
    }

    // fallback (should no happen, but could)
    return movie.getDbId().toString();
  }

  /*******************************************************************************
   * helper classes
   *******************************************************************************/
  private static class MovieFilenameRenderer implements NamedRenderer {
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
      return new Class[] { Movie.class };
    }

    @Override
    public String render(Object o, String pattern, Locale locale, Map<String, Object> model) {
      if (o instanceof Movie) {
        Movie movie = (Movie) o;

        Map<String, Object> parameters = new HashMap<>();
        if (pattern != null) {
          parameters = parseParameters(pattern);
        }

        String filename = getMovieFilename(movie);
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
  private static class MovieArtworkCopyRenderer extends ArtworkCopyRenderer {

    public MovieArtworkCopyRenderer(Path pathToExport) {
      super(pathToExport);
    }

    @Override
    public Class<?>[] getSupportedClasses() {
      return new Class[] { Movie.class };
    }

    @Override
    public String render(Object o, String pattern, Locale locale, Map<String, Object> model) {
      if (o instanceof Movie) {
        Movie movie = (Movie) o;
        Map<String, Object> parameters = parseParameters(pattern);

        MediaFile mf = movie.getArtworkMap().get(parameters.get("type"));
        if (mf == null || !mf.isGraphic()) {
          if (StringUtils.isNotBlank((String) parameters.get("default"))) {
            return (String) parameters.get("default");
          }
          return ""; // pass an emtpy string to prevent movie.toString() gets triggered by jmte
        }

        String filename = getMovieFilename(movie) + "-" + mf.getType();

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
          return ""; // pass an emtpy string to prevent movie.toString() gets triggered by jmte
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
