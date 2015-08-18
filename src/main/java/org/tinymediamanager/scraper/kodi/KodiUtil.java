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
package org.tinymediamanager.scraper.kodi;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
   * tries to detect the Kodi installation folder
   * 
   * @return File or NULL
   */
  public static File detectKodiFolder() {
    String[] appFolder = { "Kodi", "kodi", "xbmc", "XMBC" };
    String[] installFolder = { System.getenv("ProgramFiles(x86)"), System.getenv("ProgramFiles"), System.getenv("ProgramData"), "/usr/share/",
        "/usr/lib/", "/Applications/XBMC.app/Contents/Resources" };

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
  private static File detectKodiUserdataFolder() {
    // http://wiki.xbmc.org/?title=Userdata
    String[] appFolder = { "Kodi", ".kodi", "kodi", "XMBC", ".xbmc", "xbmc" };
    String[] userFolder = { System.getenv("APPDATA"), System.getProperty("user.home"),
        "/Users/" + System.getProperty("user.name") + "/Library/Application Support" };

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
    Map<String, KodiScraper> tmp = new LinkedHashMap<String, KodiScraper>(); // tmp
                                                                             // sorted
                                                                             // map
                                                                             // for
                                                                             // version
                                                                             // comparison

    // detect manually added addons
    File addons = new File("kodi_scraper");
    if (addons != null && addons.exists()) {
      foundAddonFiles.addAll(FileUtils.listFiles(addons, fileFilter, dirFilter));
    }

    // detect addons from Kodi user data folder
    addons = new File(detectKodiUserdataFolder(), "addons");
    if (addons != null && addons.exists()) {
      foundAddonFiles.addAll(FileUtils.listFiles(addons, fileFilter, dirFilter));
    }

    // detect addons from Kodi install folder
    addons = new File(detectKodiFolder(), "addons");
    if (addons != null && addons.exists()) {
      foundAddonFiles.addAll(FileUtils.listFiles(addons, fileFilter, dirFilter));
    }

    for (File f : foundAddonFiles) {
      KodiScraper x = new KodiScraper(f.getParentFile()); // parent = folder
      if (StringUtils.isBlank(x.id)) {
        continue;
      }
      if ("metadata.local".equals(x.id)) {
        continue; // local Kodi scraper
      }

      if (!tmp.containsKey(x.id)) {
        tmp.put(x.id, x);
      }
      else {
        // ok, scraper ID already added, now check for higher version.
        KodiScraper old = tmp.get(x.id);
        if (StrgUtils.compareVersion(x.version, old.version) > 0) {
          // ok, new scraper has a higher version, replace this...
          LOGGER.debug("replacing " + x.id + " v" + old.version + " with v" + x.version);
          tmp.remove(x.id);
          tmp.put(x.id, x);
        }
        else {
          LOGGER.debug("not adding " + x.addonFolder.getAbsolutePath() + " - ID already imported, or version lower");
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

    List<KodiScraper> scrapers = new ArrayList<KodiScraper>();

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

    scrapers = getKodiAddons(dirFilter, fileFilter);

    if (scrapers.size() == 0) {
      LOGGER.debug("Meh - could not find any scrapers...");
    }
    else {
      for (KodiScraper sc : scrapers) {
        LOGGER.debug("Found scraper: " + sc.addonFolder + File.separator + sc.scraperXml);
      }
    }

    List<AbstractKodiMetadataProvider> metadataProviders = new ArrayList<>();
    for (KodiScraper scraper : scrapers) {
      if(scraper.type == null){
        continue;
      }
      try {
        switch (scraper.type) {
          case MOVIE:
            metadataProviders.add(new KodiMovieMetadataProvider(scraper));
            break;

          case TV_SHOW:
            // metadataProviders.add(new KodiTvShowMetadataProvider(scraper));
            break;

          default:
            break;
        }
      }
      catch (Exception e) {
        LOGGER.error("could not load scraper " + scraper.id, e);
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

    List<KodiScraper> common = new ArrayList<KodiScraper>();

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

    if (common.size() == 0) {
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
    ArrayList<File> common = new ArrayList<File>();

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

    if (common.size() == 0) {
      LOGGER.debug("Meh - could not find any common function...");
    }

    return common;
  }
}
