package org.tinymediamanager.scraper.xbmc;

import java.io.File;
import java.io.IOException;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.tinymediamanager.scraper.MediaScraper;
import org.tinymediamanager.scraper.ScraperType;

public class XbmcScraper extends MediaScraper {
  private Map<String, ScraperFunction> functions = new TreeMap<String, ScraperFunction>();
  private String                       thumb;
  private String                       scraperXml;
  private String                       language;
  private String                       provider;
  private File                         folder;
  private String                       settingsPath;

  /**
   * instantiates a new scraper and parse info from addon.xml
   * 
   * @param scraperFolder
   */
  public XbmcScraper(File scraperFolder) {
    super(null, null, null); // dummy constructor

    try {
      File info = new File(scraperFolder, "addon.xml");
      Document doc = Jsoup.parse(info, "UTF-8", "");

      Elements addon = doc.getElementsByTag("addon");

      this.setFolder(scraperFolder);
      this.setId(addon.attr("id"));
      this.setName(addon.attr("name"));
      this.setVersion(addon.attr("version"));
      this.provider = addon.attr("provider-name");

      for (Element el : doc.getElementsByAttribute("point")) {
        String point = el.attr("point");
        if (point.equals("xbmc.addon.metadata")) {
          Elements desc = el.getElementsByAttributeValue("lang", Locale.getDefault().getLanguage());
          if (desc.size() == 0) {
            // fallback EN
            desc = el.getElementsByAttributeValue("lang", "en");
          }
          for (Element d : desc) {
            if (d.nodeName().equals("summary")) {
              this.setThumb(d.text());
            }
            else {
              this.setDescription(d.text());
            }
          }
        }
        else if (point.contains("metadata.scraper")) {
          this.scraperXml = el.attr("library");
          this.language = el.attr("language");

          // more here http://wiki.xbmc.org/index.php?title=addon.xml#.3Cextension.3E
          if (point.equals("xbmc.metadata.scraper.movies")) {
            this.setType(ScraperType.MOVIE);
          }
          else if (point.equals("xbmc.metadata.scraper.tvshows")) {
            this.setType(ScraperType.TV_SHOW);
          }
          else if (point.equals("xbmc.metadata.scraper.albums")) {
            this.setType(ScraperType.ALBUM);
          }
          else if (point.equals("xbmc.metadata.scraper.artists")) {
            this.setType(ScraperType.ARTIST);
          }
          else if (point.equals("xbmc.metadata.scraper.musicvideos")) {
            this.setType(ScraperType.MUSICVIDEO);
          }
          else if (point.equals("xbmc.metadata.scraper.library")) {
            this.setType(ScraperType.LIBRARY);
          }
        }
      }

      File settings = new File(scraperFolder, "resources/settings.xml");
      if (settings.exists()) {
        this.settingsPath = settings.getAbsolutePath();
      }
    }
    catch (IOException e) {
      e.printStackTrace();
    }
  }

  public String getSettingsPath() {
    return settingsPath;
  }

  public void setSettingsPath(String settingsPath) {
    this.settingsPath = settingsPath;
  }

  /**
   * no dupes!
   */
  public void addFunction(ScraperFunction func) {
    if (!functions.containsKey(func.getName())) {
      functions.put(func.getName(), func);
    }
  }

  public ScraperFunction getFunction(String name) {
    return functions.get(name);
  }

  public ScraperFunction[] getFunctions() {
    return functions.values().toArray(new ScraperFunction[functions.size()]);
  }

  public String getThumb() {
    return thumb;
  }

  public void setThumb(String thumb) {
    this.thumb = thumb;
  }

  public boolean containsFunction(String functionName) {
    return functions.containsKey(functionName);
  }

  public String getProvider() {
    return provider;
  }

  public void setProvider(String provider) {
    this.provider = provider;
  }

  public File getFolder() {
    return folder;
  }

  public void setFolder(File folder) {
    this.folder = folder;
  }

  public String getScraperXml() {
    return scraperXml;
  }

  public void setScraperXml(String scraperXml) {
    this.scraperXml = scraperXml;
  }

  public String getLanguage() {
    return language;
  }

  public void setLanguage(String language) {
    this.language = language;
  }

  /**
   * <p>
   * Uses <code>ReflectionToStringBuilder</code> to generate a <code>toString</code> for the specified object.
   * </p>
   * 
   * @return the String result
   * @see ReflectionToStringBuilder#toString(Object)
   */
  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
  }
}
