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
package org.tinymediamanager.scraper.kodi;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.scraper.util.StrgUtils;

/**
 * This class has some common Kodi utils for the scraper
 *
 * @author Manuel Laggner, Myron Boyle
 */
class KodiUtil {
  private static final Logger                     LOGGER     = LoggerFactory.getLogger(KodiUtil.class);
  // prescan directory for ALL common XMLs
  static final ArrayList<File>                    commonXmls = KodiUtil.getAllCommonXMLs();
  static final List<AbstractKodiMetadataProvider> scrapers   = KodiUtil.getAllScrapers();

  /**
   * Strips out unknown XML header values which might break validators<br>
   * like &lt;?xml ... asdf=false ... ?&gt;
   *
   * @param xml
   * @return
   */
  public static String fixXmlHeader(String xml) {
    String ret = xml;
    Pattern head = Pattern.compile(".*(<\\?xml(.*?)\\?>).*", Pattern.DOTALL); // just the header line <?xml ... ?>
    Matcher headm = head.matcher(xml);
    if (headm.matches()) {
      String xmlHeaderOrig = headm.group(1);
      String xmlHeaderNew = headm.group(1);
      Pattern p = Pattern.compile("(\\w+)=[\"\']?[\\w.-]+[\"\']?"); // key="value" with optional apostrophe
      Matcher m = p.matcher(xmlHeaderNew);
      while (m.find()) {
        String known = m.group(1).toLowerCase(Locale.ROOT);
        switch (known) {
          case "version":
          case "encoding":
          case "standalone":
            // valid, do nothing
            break;

          default:
            // replace unknown
            xmlHeaderNew = xmlHeaderNew.replace(m.group(), "");
            break;
        }
        xmlHeaderNew = xmlHeaderNew.replaceAll(";", ""); // replace semicolons - should not be there...
      }
      if (!xmlHeaderNew.equals(xmlHeaderOrig)) {
        xmlHeaderNew = xmlHeaderNew.replaceAll("  ", " ");
        LOGGER.warn("Fixing invalid XML header! " + xmlHeaderOrig + " -> " + xmlHeaderNew);
        ret = ret.replace(xmlHeaderOrig, xmlHeaderNew);
      }
    }
    return ret.trim();
  }

  /**
   * tries to fix invalid XML structure
   *
   * @param xml
   * @return
   */
  public static String fixXmlAttributes(String xml) {
    String ret = xml;
    Pattern attr = Pattern.compile("=\"(.*?)\""); // name="value"
    Matcher m = attr.matcher(xml);
    while (m.find()) {
      String g = m.group(1);
      if (g.contains("<") || g.contains(">")) {
        LOGGER.warn("Fixing invalid XML entities: {}", g);
        String fixed = m.group(1).replaceAll("<", "&lt;").replaceAll(">", "&gt;");
        ret = ret.replace(g, fixed); // use string replace
      }
    }
    return ret;
  }

  /**
   * fixes document.write("<sc" + "ript") XML validation
   *
   * @param xml
   * @return
   */
  public static String fixScripts(String xml) {
    String ret = xml;
    ret = ret.replace("<sc\" \\+ \"ript", "<script");
    ret = ret.replace("</\" \\+ \"script", "</script");
    return ret;
  }

  /**
   * tries to detect the Kodi installation folder
   *
   * @return File or NULL
   */
  public static File detectKodiFolder() {
    String[] appFolder = { "Kodi", "kodi", "xbmc", "XMBC" };
    String[] installFolder = { System.getenv("ProgramFiles(x86)"), System.getenv("ProgramFiles"), System.getenv("ProgramData"), "/usr/share/",
        "/usr/lib/", "/Applications/Kodi.app/Contents/Resources", "/Applications/XBMC.app/Contents/Resources" };

    for (String i : installFolder) {
      if (StringUtils.isEmpty(i)) {
        continue;
      }
      for (String a : appFolder) {
        File path = new File(i, a);
        if (path.exists()) {
          return path;
        }
      }
    }

    return null;
  }

  /**
   * tries to detect the Kodi userdata folder
   *
   * @return File or NULL
   */
  public static File detectKodiUserFolder() {
    // http://wiki.xbmc.org/?title=Userdata
    String[] appFolder = { "Kodi", ".kodi", "kodi", "XMBC", ".xbmc", "xbmc" };
    String[] userFolder = { System.getenv("APPDATA"), System.getProperty("user.home"),
        System.getProperty("user.home") + "/Library/Application Support" };

    for (String u : userFolder) {
      if (StringUtils.isEmpty(u)) {
        continue;
      }
      for (String a : appFolder) {
        File path = new File(u, a);
        if (path.exists()) {
          return path;
        }
      }
    }

    return null;
  }

  /**
   * gets a Kodi scraper object, selected by the filters<br>
   *
   * @param dirFilter
   *          the directory filter for addon search
   * @param fileFilter
   *          the file filter for addon search
   * @return a list of all found addons
   */
  private static List<KodiScraper> getKodiAddons(IOFileFilter dirFilter, IOFileFilter fileFilter) {
    List<KodiScraper> scrapers = new ArrayList<>();
    List<File> foundAddonFiles = new ArrayList<>();
    Map<String, KodiScraper> tmp = new LinkedHashMap<>(); // tmp
    // sorted
    // map
    // for
    // version
    // comparison

    // check if we are in a unit test
    File addons = new File("./target/test-classes/kodi_scraper");
    if (addons != null && addons.exists()) {
      foundAddonFiles.addAll(FileUtils.listFiles(addons, fileFilter, dirFilter));
    }
    else {
      // detect manually added addons
      addons = new File("kodi_scraper");
      if (addons != null && addons.exists()) {
        foundAddonFiles.addAll(FileUtils.listFiles(addons, fileFilter, dirFilter));
      }

      // detect addons from Kodi user data folder
      addons = new File(detectKodiUserFolder(), "addons");
      if (addons != null && addons.exists()) {
        foundAddonFiles.addAll(FileUtils.listFiles(addons, fileFilter, dirFilter));
      }

      // detect addons from Kodi install folder
      addons = new File(detectKodiFolder(), "addons");
      if (addons != null && addons.exists()) {
        foundAddonFiles.addAll(FileUtils.listFiles(addons, fileFilter, dirFilter));
      }
    }

    for (File f : foundAddonFiles) {
      KodiScraper x = new KodiScraper(f.getParentFile()); // parent = folder
      if (StringUtils.isBlank(x.getProviderInfo().getId())) {
        continue;
      }
      if ("metadata.local".equals(x.getProviderInfo().getId())) {
        continue; // local Kodi scraper
      }

      if (!tmp.containsKey(x.getProviderInfo().getId())) {
        tmp.put(x.getProviderInfo().getId(), x);
      }
      else {
        // ok, scraper ID already added, now check for higher version.
        KodiScraper old = tmp.get(x.getProviderInfo().getId());
        if (StrgUtils.compareVersion(x.getProviderInfo().getVersion(), old.getProviderInfo().getVersion()) > 0) {
          // ok, new scraper has a higher version, replace this...
          LOGGER.debug(
              "replacing " + x.getProviderInfo().getId() + " v" + old.getProviderInfo().getVersion() + " with v" + x.getProviderInfo().getVersion());
          tmp.remove(x.getProviderInfo().getId());
          tmp.put(x.getProviderInfo().getId(), x);
        }
        else {
          LOGGER.debug("not adding {} - ID already imported, or version lower", x.addonFolder.getAbsolutePath());
        }
      }
    }

    // tmp to scraper list
    scrapers.addAll(tmp.values());

    return scrapers;
  }

  /**
   * returns a list of all found scrapers
   *
   * @return
   */
  private static List<AbstractKodiMetadataProvider> getAllScrapers() {
    LOGGER.debug("searching for Kodi scrapers");

    List<KodiScraper> localScrapers;

    IOFileFilter dirFilter = new IOFileFilter() {
      @Override
      public boolean accept(File arg0, String arg1) {
        return false;
      }

      @Override
      public boolean accept(File arg0) {
        return arg0.getName().startsWith("metadata") && !arg0.getName().contains("common");
      }
    };
    IOFileFilter fileFilter = new IOFileFilter() {
      @Override
      public boolean accept(File pathname) {
        return pathname.getName().equals("addon.xml");
      }

      @Override
      public boolean accept(File arg0, String arg1) {
        return false;
      }
    };

    localScrapers = getKodiAddons(dirFilter, fileFilter);

    if (localScrapers.isEmpty()) {
      LOGGER.debug("Meh - could not find any scrapers...");
    }
    else {
      for (KodiScraper sc : localScrapers) {
        LOGGER.debug("Found scraper: " + sc.addonFolder + File.separator + sc.scraperXml);
      }
    }

    List<AbstractKodiMetadataProvider> metadataProviders = new ArrayList<>();
    for (KodiScraper scraper : localScrapers) {
      if (scraper.type == null) {
        continue;
      }
      try {
        switch (scraper.type) {
          case MOVIE:
            metadataProviders.add(new KodiMovieMetadataProvider(scraper));
            break;

          case TV_SHOW:
            metadataProviders.add(new KodiTvShowMetadataProvider(scraper));
            break;

          default:
            break;
        }
      }
      catch (Exception e) {
        LOGGER.error("could not load scraper " + scraper.getProviderInfo().getId(), e);
      }
    }
    return metadataProviders;
  }

  /**
   * returns a list of all found common addons.xml
   *
   * @return
   */
  private static List<KodiScraper> getAllCommon() {
    LOGGER.debug("searching for Kodi commons");

    List<KodiScraper> common = new ArrayList<>();

    IOFileFilter dirFilter = new IOFileFilter() {
      @Override
      public boolean accept(File arg0, String arg1) {
        return false;
      }

      @Override
      public boolean accept(File arg0) {
        // all common metadata folders for additional inclusion
        return arg0.getName().startsWith("metadata") && arg0.getName().contains("common");
      }
    };
    IOFileFilter fileFilter = new IOFileFilter() {
      @Override
      public boolean accept(File pathname) {
        // all XML files in scraper folder - but not the addon.xml itself
        return pathname.getName().equals("addon.xml");
        // return pathname.getName().endsWith("xml") &&
        // !pathname.getName().equals("addon.xml");
      }

      @Override
      public boolean accept(File arg0, String arg1) {
        return false;
      }
    };

    common = getKodiAddons(dirFilter, fileFilter);

    if (common.isEmpty()) {
      LOGGER.debug("Meh - could not find any common folders...");
    }

    return common;
  }

  /**
   * returns a list of all found common xmls, but not addon.xml
   *
   * @return
   */
  private static ArrayList<File> getAllCommonXMLs() {
    ArrayList<File> common = new ArrayList<>();

    IOFileFilter dirFilter = new IOFileFilter() {
      @Override
      public boolean accept(File arg0, String arg1) {
        return false;
      }

      @Override
      public boolean accept(File arg0) {
        // all common metadata folders for additional inclusion
        return arg0.getName().startsWith("metadata") && arg0.getName().contains("common");
      }
    };
    IOFileFilter fileFilter = new IOFileFilter() {
      @Override
      public boolean accept(File pathname) {
        // all XML files in scraper folder - but not the addon.xml itself
        return pathname.getName().endsWith("xml") && !pathname.getName().equals("addon.xml");
      }

      @Override
      public boolean accept(File arg0, String arg1) {
        return false;
      }
    };

    for (KodiScraper sc : getAllCommon()) {
      Collection<File> files = FileUtils.listFiles(sc.getFolder(), fileFilter, dirFilter);
      for (File f : files) {
        if (!common.contains(f)) {
          // FIXME: check, if same directory NAME exists (dupe check in other
          // dir)
          LOGGER.debug("Found common: " + f);
          common.add(f);
        }
        else {
          LOGGER.debug("Skipped common: " + f);
        }
      }
    }

    if (common.isEmpty()) {
      LOGGER.debug("Meh - could not find any common function...");
    }

    return common;
  }
}
