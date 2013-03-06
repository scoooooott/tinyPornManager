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
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import org.tinymediamanager.Globals;

import ca.odell.glazedlists.ObservableElementList;

import com.floreysoft.jmte.Engine;
import com.floreysoft.jmte.NamedRenderer;
import com.floreysoft.jmte.RenderFormatInfo;
import com.floreysoft.jmte.encoder.XMLEncoder;
import com.floreysoft.jmte.message.ParseException;

/**
 * This class exports a list of movies to various formats according to
 * templates.
 * 
 * @author Myron Boyle
 */
public class MovieExporter {

  /** The Constant LOGGER. */
  private final static Logger LOGGER             = Logger.getLogger(MovieExporter.class);

  private static final String TEMPLATE_DIRECTORY = "templates";

  /**
   * exports movie list according to template file
   * 
   * @param movies
   *          list of movies
   * @param template
   *          filename of template
   * @throws Exception
   */
  public static void export(ObservableElementList<Movie> movies, String template) throws Exception {
    LOGGER.info("preparing movie export; using " + template);

    Engine engine = Engine.createCachingEngine();
    engine.registerNamedRenderer(new MovieExporter.NamedDateRenderer()); // our custom date renderer
    if (template.toLowerCase().contains(".html") || template.toLowerCase().contains(".xml")) {
      engine.setEncoder(new XMLEncoder()); // special char replacement
    }

    String temp = FileUtils.readFileToString(new File(TEMPLATE_DIRECTORY, template), "UTF-8");

    if (template.toLowerCase().startsWith("list")) {
      LOGGER.info("generating movie list");
      File f = new File(TEMPLATE_DIRECTORY, FilenameUtils.getBaseName(template));
      FileUtils.deleteQuietly(f);

      Map<String, Object> root = new HashMap<String, Object>();
      root.put("movies", new ArrayList<Movie>(movies));

      String output = engine.transform(temp, root);

      FileUtils.writeStringToFile(f, output, "UTF-8");
      LOGGER.info("movie list generated: " + f.getAbsolutePath());
    }
    else if (template.toLowerCase().startsWith("detail")) {
      LOGGER.info("generating movie detail pages");
      File dir = new File(TEMPLATE_DIRECTORY, FilenameUtils.getBaseName(template));
      FileUtils.deleteDirectory(dir);
      dir.mkdirs();

      // TODO: HTML pages per movie could be perfectly multithreaded ;)
      for (Movie movie : movies) {
        LOGGER.debug("processing movie " + movie.getName());
        // get preferred movie name like set up in movie renamer
        File f = new File(dir, MovieRenamer.createDestination(Globals.settings.getMovieRenamerFilename(), movie) + "."
            + FilenameUtils.getExtension(FilenameUtils.getBaseName(template)));

        Map<String, Object> root = new HashMap<String, Object>();
        root.put("movie", movie);

        String output = engine.transform(temp, root);
        FileUtils.writeStringToFile(f, output, "UTF-8");
      }
      LOGGER.info("movie detail pages generated: " + dir.getAbsolutePath());
    }
    else {
      LOGGER.warn("invalid template name - must start with 'list' or ' detail'");
    }
  }

  public static class NamedDateRenderer implements NamedRenderer {

    private static final String DEFAULT_PATTERN = "dd.MM.yyyy HH:mm:ss Z";

    //private final String        regexPatternDescription = "Was weiß ich denn?";

    private Date convert(Object o, DateFormat dateFormat) {
      if (o instanceof Date) {
        return (Date) o;
      }
      else if (o instanceof Number) {
        long longValue = ((Number) o).longValue();
        return new Date(longValue);
      }
      else if (o instanceof String) {
        try {
          try {
            return dateFormat.parse((String) o);
          }
          catch (java.text.ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
          }
        }
        catch (ParseException e) {
        }
      }
      return null;
    }

    @Override
    public String getName() {
      return "date";
    }

    @Override
    public Class<?>[] getSupportedClasses() {
      return new Class[] { Date.class, String.class, Integer.class, Long.class };
    }

    @Override
    public String render(Object o, String pattern, Locale locale) {
      String patternToUse = pattern != null ? pattern : DEFAULT_PATTERN;
      try {
        DateFormat dateFormat = new SimpleDateFormat(patternToUse);
        Date value = convert(o, dateFormat);
        if (value != null) {
          String format = dateFormat.format(value);
          return format;
        }
      }
      catch (IllegalArgumentException iae) {
      }
      catch (NullPointerException npe) {
      }
      return null;
    }

    @Override
    public RenderFormatInfo getFormatInfo() {
      return null;
    }
  }

}
