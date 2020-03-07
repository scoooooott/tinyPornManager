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
package org.tinymediamanager.core;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.core.entities.MediaEntity;
import org.tinymediamanager.core.jmte.HtmlEncoder;

import com.floreysoft.jmte.Engine;
import com.floreysoft.jmte.NamedRenderer;
import com.floreysoft.jmte.RenderFormatInfo;
import com.floreysoft.jmte.encoder.XMLEncoder;

public abstract class MediaEntityExporter {
  private static final Logger   LOGGER             = LoggerFactory.getLogger(MediaEntityExporter.class);
  protected static final String TEMPLATE_DIRECTORY = "templates";

  protected Engine              engine;
  protected Properties          properties;
  protected String              fileExtension;
  protected String              listTemplate       = "";
  protected String              detailTemplate     = "";
  protected Path                templateDir;
  protected boolean             cancel             = false;

  public enum TemplateType {
    MOVIE,
    TV_SHOW
  }

  protected MediaEntityExporter(Path templatePath, TemplateType type) throws Exception {
    templateDir = templatePath;

    // check if template exists and is valid
    if (!Files.isDirectory(templateDir)) {
      throw new Exception("illegal template path");
    }

    Path configFile = templateDir.resolve("template.conf");
    if (!Files.exists(configFile)) {
      throw new Exception("illegal template config");
    }

    // load settings from template
    properties = new Properties();
    BufferedInputStream stream = new BufferedInputStream(new FileInputStream(configFile.toFile()));
    properties.load(stream);
    stream.close();

    // check needed settings
    String typeInConfig = properties.getProperty("type");
    if (!typeInConfig.equalsIgnoreCase(type.name())) {
      throw new Exception("illegal template");
    }

    String listTemplateFile = properties.getProperty("list");
    if (StringUtils.isBlank(listTemplateFile)) {
      throw new Exception("illegal template");
    }

    // get other settings
    String detailTemplateFile = properties.getProperty("detail");
    fileExtension = StringUtils.isBlank(properties.getProperty("extension")) 
        ? "html" 
        : properties.getProperty("extension").toLowerCase();

    // set up engine
    engine = Engine.createEngine();

    if ("html".equals(fileExtension)) {
      engine.setEncoder(new HtmlEncoder()); // special char replacement
    }
    if ("xml".equals(fileExtension)) {
      engine.setEncoder(new XMLEncoder()); // special char replacement
    }

    // load list template from File
    listTemplate = Utils.readFileToString(templateDir.resolve(listTemplateFile));
    if (StringUtils.isNotBlank(detailTemplateFile)) {
      detailTemplate = Utils.readFileToString(templateDir.resolve(detailTemplateFile));
    }
  }

  public abstract <T extends MediaEntity> void export(List<T> entitiesToExport, Path pathToExport) throws Exception;

  /**
   * cancel the export
   */
  public void cancel() {
    this.cancel = true;
  }

  /**
   * Find templates for the given type.
   * 
   * @param type
   *          the template type
   * @return the list of all found template types
   */
  public static List<ExportTemplate> findTemplates(TemplateType type) {
    List<ExportTemplate> templatesFound = new ArrayList<>();

    // search in template folder for templates
    Path root = Paths.get(TEMPLATE_DIRECTORY);
    if (!Files.isDirectory(root)) {
      return templatesFound;
    }

    // search ever subdir
    try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(root)) {
      for (Path path : directoryStream) {
        if (Files.isDirectory(path)) {

          // get type of template
          Path config = path.resolve("template.conf");
          if (!Files.exists(config)) {
            continue;
          }

          // load settings from template
          Properties properties = new Properties();
          try {
            BufferedInputStream stream = new BufferedInputStream(new FileInputStream(config.toFile()));
            properties.load(stream);
            stream.close();
          }
          catch (Exception e) {
            LOGGER.warn("error in config: " + path + " | " + e.getMessage());
            continue;
          }

          // get template type
          String typeInConfig = properties.getProperty("type");
          if (StringUtils.isBlank(typeInConfig)) {
            continue;
          }

          if (typeInConfig.equalsIgnoreCase(type.name())) {
            ExportTemplate template = new ExportTemplate();
            template.setName(properties.getProperty("name"));
            template.setType(type);
            template.setPath(path.toAbsolutePath().toString());
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
      }
    }
    catch (IOException ignored) {
    }

    return templatesFound;
  }

  /**
   * this renderer is used to copy artwork into the exported template
   *
   * @author Manuel Laggner
   */
  protected abstract static class ArtworkCopyRenderer implements NamedRenderer {
    protected Path pathToExport;

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

    /**
     * parse the parameters out of the parameters string
     *
     * @param parameters
     *          the parameters as string
     * @return a map containing all parameters
     */
    protected Map<String, Object> parseParameters(String parameters) {
      Map<String, Object> parameterMap = new HashMap<>();

      // defaults
      parameterMap.put("thumb", Boolean.FALSE);
      parameterMap.put("destination", "images");

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
          case "type":
            try {
              MediaFileType type = MediaFileType.valueOf(value.toUpperCase(Locale.ROOT));
              parameterMap.put(key, type);
            }
            catch (Exception e) {
              // do not let the exporter crash
            }
            break;

          case "destination":
          case "default":
            parameterMap.put(key, value);
            break;

          case "thumb":
          case "escape":
            parameterMap.put(key, Boolean.parseBoolean(value));
            break;

          case "width":
            try {
              parameterMap.put(key, Integer.parseInt(value));
            }
            catch (Exception ignored) {
              // do not let the exporter crash
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
