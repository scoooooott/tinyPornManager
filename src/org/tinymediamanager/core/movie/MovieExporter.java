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

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.core.MediaEntityExporter;
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

  public MovieExporter(String pathToTemplate) throws Exception {
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
  public <T extends MediaEntity> void export(List<T> moviesToExport, String pathToExport) throws Exception {
    LOGGER.info("preparing movie export; using " + properties.getProperty("name"));

    // register own renderers
    engine.registerNamedRenderer(new NamedDateRenderer());
    engine.registerNamedRenderer(new MovieFilenameRenderer());

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
      listExportFile = new File(exportDir, "movielist.xml");
    }
    if (fileExtension.equalsIgnoreCase("csv")) {
      listExportFile = new File(exportDir, "movielist.csv");
    }
    if (listExportFile == null) {
      throw new Exception("error creating movie list file");
    }

    // create list
    LOGGER.info("generating movie list");
    FileUtils.deleteQuietly(listExportFile);

    Map<String, Object> root = new HashMap<String, Object>();
    root.put("movies", new ArrayList<T>(moviesToExport));

    String output = engine.transform(listTemplate, root);

    FileUtils.writeStringToFile(listExportFile, output, "UTF-8");
    LOGGER.info("movie list generated: " + listExportFile.getAbsolutePath());

    // create details for
    if (StringUtils.isNotBlank(detailTemplate)) {
      File detailsDir = new File(exportDir, "movies");
      if (detailsDir.exists()) {
        FileUtils.deleteQuietly(detailsDir);
      }
      detailsDir.mkdirs();

      String renamerTemplate = "";
      if (StringUtils.isNotBlank(MovieModuleManager.MOVIE_SETTINGS.getMovieRenamerFilename())) {
        renamerTemplate = MovieModuleManager.MOVIE_SETTINGS.getMovieRenamerFilename();
      }
      else {
        // default
        renamerTemplate = "$T ($Y) $V $A";
      }

      for (MediaEntity me : moviesToExport) {
        Movie movie = (Movie) me;
        LOGGER.debug("processing movie " + movie.getTitle());
        // get preferred movie name like set up in movie renamer
        File detailsExportFile = new File(detailsDir, MovieRenamer.createDestinationForFilename(renamerTemplate, movie) + "." + fileExtension);

        root = new HashMap<String, Object>();
        root.put("movie", movie);

        output = engine.transform(detailTemplate, root);
        FileUtils.writeStringToFile(detailsExportFile, output, "UTF-8");

      }

      LOGGER.info("movie detail pages generated: " + exportDir.getAbsolutePath());
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
        return MovieRenamer.createDestinationForFilename(MovieModuleManager.MOVIE_SETTINGS.getMovieRenamerFilename(), movie);
      }
      return null;
    }
  }
}
