/*
 * Copyright 2012 Manuel Laggner
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
import java.io.FileWriter;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import org.tinymediamanager.Globals;

import ca.odell.glazedlists.ObservableElementList;
import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.Template;

/**
 * The Class MovieRenamer.
 */
public class MovieExporter {

  /** The Constant LOGGER. */
  private final static Logger LOGGER             = Logger.getLogger(MovieExporter.class);

  private static final String TEMPLATE_DIRECTORY = "templates";

  public static void export(ObservableElementList<Movie> movies, String template) throws Exception {
    LOGGER.info("preparing movie export; using " + template);
    String extension = ".html";
    if (template.toLowerCase().contains("csv")) {
      extension = ".xls";
    }

    Configuration cfg = new Configuration();
    cfg.setDirectoryForTemplateLoading(new File(TEMPLATE_DIRECTORY));
    cfg.setObjectWrapper(new DefaultObjectWrapper());

    Template temp = cfg.getTemplate(template, "UTF-8");

    if (template.toLowerCase().startsWith("list")) {
      LOGGER.info("generating movie list");
      File out = new File(TEMPLATE_DIRECTORY, FilenameUtils.getBaseName(template) + extension);
      FileUtils.deleteQuietly(out);

      // Map root = new HashMap(); // since we only put movies in there, use type args
      Map<String, ObservableElementList<Movie>> root = new HashMap<String, ObservableElementList<Movie>>();
      root.put("movies", movies);

      FileWriter fw = new FileWriter(out);
      temp.process(root, fw);
      fw.flush();
      LOGGER.info("movie list generated: " + out.getAbsolutePath());
    }
    else if (template.toLowerCase().startsWith("detail")) {
      LOGGER.info("generating movie detail pages");
      File out = new File(TEMPLATE_DIRECTORY, FilenameUtils.getBaseName(template));
      FileUtils.deleteDirectory(out);
      out.mkdirs();

      // TODO: HTML pages per movie could be perfectly multithreaded ;)
      for (Movie movie : movies) {

        LOGGER.debug("processing movie " + movie.getName());
        // get preferred movie name like set up in movie renamer
        File fname = new File(out, MovieRenamer.createDestination(Globals.settings.getMovieRenamerFilename(), movie) + extension);
        FileWriter fw = new FileWriter(fname);
        Map<String, Movie> hm = new HashMap<String, Movie>();
        hm.put("movie", movie);
        temp.process(hm, fw);
        fw.flush();

      }
      LOGGER.info("movie detail pages generated: " + out.getAbsolutePath());
    }
    else {
      LOGGER.warn("invalid template name - must start with 'list' or ' detail'");
    }

  }
}
