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
package org.tinymediamanager.core.movie;

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

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.core.MediaEntityExporter;
import org.tinymediamanager.core.Utils;
import org.tinymediamanager.core.entities.MediaEntity;
import org.tinymediamanager.core.movie.entities.Movie;

import com.floreysoft.jmte.NamedRenderer;
import com.floreysoft.jmte.RenderFormatInfo;

/**
 * This class exports a list of movies to various formats according to templates.
 * 
 * @author Myron Boyle / Manuel Laggner
 */
public class MovieExporter extends MediaEntityExporter {
  private final static Logger LOGGER = LoggerFactory.getLogger(MovieExporter.class);

  public MovieExporter(Path pathToTemplate) throws Exception {
    super(pathToTemplate, TemplateType.MOVIE);
  }

  /**
   * exports movie list according to template file.
   * 
   * @param moviesToExport
   *          list of movies
   * @param pathToExport
   *          the path to export
   * @throws Exception
   *           the exception
   */
  @Override
  public <T extends MediaEntity> void export(List<T> moviesToExport, Path exportDir) throws Exception {
    LOGGER.info("preparing movie export; using " + properties.getProperty("name"));

    // register own renderers
    engine.registerNamedRenderer(new NamedDateRenderer());
    engine.registerNamedRenderer(new MovieFilenameRenderer());

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
      listExportFile = exportDir.resolve("movielist.xml");
    }
    if (fileExtension.equalsIgnoreCase("csv")) {
      listExportFile = exportDir.resolve("movielist.csv");
    }
    if (listExportFile == null) {
      throw new Exception("error creating movie list file");
    }

    // create list
    LOGGER.info("generating movie list");
    Utils.deleteFileSafely(listExportFile);

    Map<String, Object> root = new HashMap<String, Object>();
    root.put("movies", new ArrayList<T>(moviesToExport));

    String output = engine.transform(listTemplate, root);

    Utils.writeStringToFile(listExportFile, output);
    LOGGER.info("movie list generated: " + listExportFile);

    // create details for
    if (StringUtils.isNotBlank(detailTemplate)) {
      Path detailsDir = exportDir.resolve("movies");
      if (Files.isDirectory(detailsDir)) {
        Utils.deleteDirectoryRecursive(detailsDir);
      }
      Files.createDirectory(detailsDir);

      for (MediaEntity me : moviesToExport) {
        Movie movie = (Movie) me;
        LOGGER.debug("processing movie " + movie.getTitle());
        // get preferred movie name like set up in movie renamer
        String detailFilename = MovieRenamer.createDestinationForFilename(MovieModuleManager.MOVIE_SETTINGS.getMovieRenamerFilename(), movie);
        if (StringUtils.isBlank(detailFilename)) {
          detailFilename = movie.getVideoBasenameWithoutStacking();
          // FilenameUtils.getBaseName(Utils.cleanStackingMarkers(movie.getMediaFiles(MediaFileType.VIDEO).get(0).getFilename()));
        }
        Path detailsExportFile = detailsDir.resolve(detailFilename + "." + fileExtension);

        root = new HashMap<String, Object>();
        root.put("movie", movie);

        output = engine.transform(detailTemplate, root);
        Utils.writeStringToFile(detailsExportFile, output);

      }

      LOGGER.info("movie detail pages generated: " + exportDir);
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
  public static class MovieFilenameRenderer implements NamedRenderer {
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
    public String render(Object o, String pattern, Locale locale) {
      if (o instanceof Movie) {
        Movie movie = (Movie) o;
        String filename = MovieRenamer.createDestinationForFilename(MovieModuleManager.MOVIE_SETTINGS.getMovieRenamerFilename(), movie);
        if (StringUtils.isNotBlank(filename)) {
          return filename;
        }
        return movie.getVideoBasenameWithoutStacking();
        // FilenameUtils.getBaseName(Utils.cleanStackingMarkers(movie.getMediaFiles(MediaFileType.VIDEO).get(0).getFilename()));
      }
      return null;
    }
  }
}
