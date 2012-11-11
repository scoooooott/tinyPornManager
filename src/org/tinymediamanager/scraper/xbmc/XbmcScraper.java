package org.tinymediamanager.scraper.xbmc;

import java.util.Map;
import java.util.TreeMap;

public class XbmcScraper {
  private Map<String, ScraperFunction> functions = new TreeMap<String, ScraperFunction>();
  private String                       name;
  private String                       thumb;
  private String                       content;
  private String                       id;
  private String                       description;
  private String                       settingsPath;

  public String getSettingsPath() {
    return settingsPath;
  }

  public void setSettingsPath(String settingsPath) {
    this.settingsPath = settingsPath;
  }

  public XbmcScraper(String id) {
    this.id = id;
  }

  public void addFunction(ScraperFunction func) {
    functions.put(func.getName(), func);
  }

  public ScraperFunction getFunction(String name) {
    return functions.get(name);
  }

  public ScraperFunction[] getFunctions() {
    return functions.values().toArray(new ScraperFunction[functions.size()]);
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getThumb() {
    return thumb;
  }

  public void setThumb(String thumb) {
    this.thumb = thumb;
  }

  public String getContent() {
    return content;
  }

  public void setContent(String content) {
    this.content = content;
  }

  public boolean containsFunction(String functionName) {
    return functions.containsKey(functionName);
  }

  public boolean isVideoScraper() {
    String c = getContent();
    if (c == null)
      return false;

    return c.contains("movies") || c.contains("tvshows");
  }

  public String getId() {
    return id;
  }

  public String getDescription() {
    return description;
  }

  public void setId(String id) {
    this.id = id;
  }

  public void setDescription(String description) {
    this.description = description;
  }
}
