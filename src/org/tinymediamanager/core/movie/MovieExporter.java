/*
 * Copyright 2012 - 2013 Manuel Laggner
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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.Globals;
import org.tinymediamanager.core.ExportTemplate;

import com.floreysoft.jmte.Engine;
import com.floreysoft.jmte.NamedRenderer;
import com.floreysoft.jmte.RenderFormatInfo;
import com.floreysoft.jmte.encoder.Encoder;
import com.floreysoft.jmte.encoder.XMLEncoder;
import com.floreysoft.jmte.message.ParseException;

/**
 * This class exports a list of movies to various formats according to templates.
 * 
 * @author Myron Boyle / Manuel Laggner
 */
public class MovieExporter {

  public enum TemplateType {

    /** The movie. */
    MOVIE,

    /** The tv show. */
    TV_SHOW
  }

  /** The Constant LOGGER. */
  private final static Logger LOGGER             = LoggerFactory.getLogger(MovieExporter.class);

  /** The Constant TEMPLATE_DIRECTORY. */
  private static final String TEMPLATE_DIRECTORY = "templates";

  /**
   * Find templates for the given type.
   * 
   * @return the list
   */
  public static List<ExportTemplate> findTemplates() {
    List<ExportTemplate> templatesFound = new ArrayList<ExportTemplate>();

    // search in template folder for templates
    File root = new File(TEMPLATE_DIRECTORY);
    if (!root.exists() || !root.isDirectory()) {
      return templatesFound;
    }

    // search ever subdir
    File[] templateDirs = root.listFiles();
    for (File dir : templateDirs) {
      if (!dir.isDirectory()) {
        continue;
      }

      // get type of template
      File config = new File(dir, "template.conf");
      if (!config.exists()) {
        continue;
      }

      // load settings from template
      Properties properties = new Properties();
      try {
        BufferedInputStream stream = new BufferedInputStream(new FileInputStream(config));
        properties.load(stream);
        stream.close();
      }
      catch (Exception e) {
        LOGGER.warn("error in config: " + dir.getAbsolutePath() + " | " + e.getMessage());
        continue;
      }

      // get template type
      String typeInConfig = properties.getProperty("type");
      if (StringUtils.isBlank(typeInConfig)) {
        continue;
      }

      if (typeInConfig.equalsIgnoreCase(TemplateType.MOVIE.name())) {
        ExportTemplate template = new ExportTemplate();
        template.setName(properties.getProperty("name"));
        template.setType(TemplateType.MOVIE);
        template.setPath(dir.getAbsolutePath());
        template.setUrl(properties.getProperty("url"));
        template.setDescription(properties.getProperty("description"));
        if (StringUtils.isNotBlank(properties.getProperty("detail"))) {
          template.setDetail(true);
        }
        else {
          template.setDetail(false);
        }

        templatesFound.add(template);
      }
    }

    return templatesFound;
  }

  /**
   * exports movie list according to template file.
   * 
   * @param movies
   *          list of movies
   * @param pathToTemplate
   *          the path to template
   * @param pathToExport
   *          the path to export
   * @throws Exception
   *           the exception
   */
  public static void export(List<Movie> movies, String pathToTemplate, String pathToExport) throws Exception {
    LOGGER.info("preparing movie export; using " + pathToTemplate);

    // check if template exists and is valid
    File templateDir = new File(pathToTemplate);
    if (!templateDir.exists() || !templateDir.isDirectory()) {
      throw new Exception("illegal template");
    }

    File file = new File(pathToTemplate, "template.conf");
    if (!file.exists() || !file.isFile()) {
      throw new Exception("illegal template");
    }

    // load settings from template
    Properties properties = new Properties();
    BufferedInputStream stream = new BufferedInputStream(new FileInputStream(file));
    properties.load(stream);
    stream.close();

    // check needed settings
    String listTemplateFile = properties.getProperty("list");
    if (StringUtils.isBlank(listTemplateFile)) {
      throw new Exception("illegal template");
    }

    // get other settings
    String detailTemplateFile = properties.getProperty("detail");
    String fileExtension = properties.getProperty("extension");
    if (StringUtils.isBlank(fileExtension)) {
      fileExtension = "html";
    }

    // set up engine
    Engine engine = Engine.createCachingEngine();

    // register own renderers
    engine.registerNamedRenderer(new MovieExporter.NamedDateRenderer());
    engine.registerNamedRenderer(new MovieExporter.MovieFilenameRenderer());

    if (fileExtension.equalsIgnoreCase("html")) {
      engine.setEncoder(new HtmlEncoder()); // special char replacement
    }
    if (fileExtension.equalsIgnoreCase("xml")) {
      engine.setEncoder(new XMLEncoder()); // special char replacement
    }

    // load list template from File
    String listTemplate = FileUtils.readFileToString(new File(pathToTemplate, listTemplateFile), "UTF-8");

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
      throw new Exception("error creating movielist file");
    }

    // create list
    LOGGER.info("generating movie list");
    FileUtils.deleteQuietly(listExportFile);

    Map<String, Object> root = new HashMap<String, Object>();
    root.put("movies", new ArrayList<Movie>(movies));

    String output = engine.transform(listTemplate, root);

    FileUtils.writeStringToFile(listExportFile, output, "UTF-8");
    LOGGER.info("movie list generated: " + listExportFile.getAbsolutePath());

    // create details for
    if (StringUtils.isNotBlank(detailTemplateFile)) {
      String detailTemplate = FileUtils.readFileToString(new File(pathToTemplate, detailTemplateFile), "UTF-8");

      File detailsDir = new File(exportDir, "movies");
      if (detailsDir.exists()) {
        FileUtils.deleteQuietly(detailsDir);
      }
      detailsDir.mkdirs();

      for (Movie movie : movies) {
        LOGGER.debug("processing movie " + movie.getTitle());
        // get preferred movie name like set up in movie renamer
        File detailsExportFile = new File(detailsDir, MovieRenamer.createDestination(Globals.settings.getMovieSettings().getMovieRenamerFilename(),
            movie) + "." + fileExtension);

        root = new HashMap<String, Object>();
        root.put("movie", movie);

        output = engine.transform(detailTemplate, root);
        FileUtils.writeStringToFile(detailsExportFile, output, "UTF-8");

      }

      LOGGER.info("movie detail pages generated: " + exportDir.getAbsolutePath());
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
        FileUtils.copyDirectory(fileInTemplateDir, exportDir);
      }
    }
  }

  /**
   * The Class NamedDateRenderer.
   * 
   * @author Manuel Laggner
   */
  public static class NamedDateRenderer implements NamedRenderer {

    /** The Constant DEFAULT_PATTERN. */
    private static final String DEFAULT_PATTERN = "dd.MM.yyyy HH:mm:ss Z";

    // private final String regexPatternDescription = "Was weiß ich denn?";

    /**
     * Convert.
     * 
     * @param o
     *          the o
     * @param dateFormat
     *          the date format
     * @return the date
     */
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
            LOGGER.warn("cannot convert date format", e);
          }
        }
        catch (ParseException e) {
        }
      }
      return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.floreysoft.jmte.NamedRenderer#getName()
     */
    @Override
    public String getName() {
      return "date";
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.floreysoft.jmte.NamedRenderer#getSupportedClasses()
     */
    @Override
    public Class<?>[] getSupportedClasses() {
      return new Class[] { Date.class, String.class, Integer.class, Long.class };
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.floreysoft.jmte.NamedRenderer#render(java.lang.Object, java.lang.String, java.util.Locale)
     */
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

    /*
     * (non-Javadoc)
     * 
     * @see com.floreysoft.jmte.NamedRenderer#getFormatInfo()
     */
    @Override
    public RenderFormatInfo getFormatInfo() {
      return null;
    }
  }

  /**
   * The Class MovieFilenameRenderer.
   * 
   * @author Manuel Laggner
   */
  public static class MovieFilenameRenderer implements NamedRenderer {

    /*
     * (non-Javadoc)
     * 
     * @see com.floreysoft.jmte.NamedRenderer#getFormatInfo()
     */
    @Override
    public RenderFormatInfo getFormatInfo() {
      return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.floreysoft.jmte.NamedRenderer#getName()
     */
    @Override
    public String getName() {
      return "filename";
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.floreysoft.jmte.NamedRenderer#getSupportedClasses()
     */
    @Override
    public Class<?>[] getSupportedClasses() {
      return new Class[] { Movie.class };
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.floreysoft.jmte.NamedRenderer#render(java.lang.Object, java.lang.String, java.util.Locale)
     */
    @Override
    public String render(Object o, String pattern, Locale locale) {
      if (o instanceof Movie) {
        Movie movie = (Movie) o;
        return MovieRenamer.createDestination(Globals.settings.getMovieSettings().getMovieRenamerFilename(), movie);
      }
      return null;
    }

  }

  /**
   * The Class HtmlEncoder.
   * 
   * @author Manuel Laggner
   */
  public static class HtmlEncoder implements Encoder {
    /*
     * (non-Javadoc)
     * 
     * @see com.floreysoft.jmte.encoder.Encoder#encode(java.lang.String)
     */
    @Override
    public String encode(String arg0) {
      return StringEscapeUtils.escapeHtml4(arg0);
    }

  }
}
