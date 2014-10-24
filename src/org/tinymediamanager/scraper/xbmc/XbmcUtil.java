package org.tinymediamanager.scraper.xbmc;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.lang3.StringUtils;

public class XbmcUtil {

  /**
   * tries to detect the XBMC/Kodi installation folder
   * 
   * @return File or NULL
   */
  public static File detectXbmcFolder() {
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
   * tries to detect the XBMC/Kodi userdata folder
   * 
   * @return File or NULL
   */
  public static File detectXbmcUserdataFolder() {
    // http://wiki.xbmc.org/?title=Userdata
    String[] appFolder = { "Kodi", "XMBC", "kodi", ".xbmc", "xbmc", ".kodi" };
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

  public static ArrayList<XbmcScraper> getXbmcAddons(IOFileFilter dirFilter, IOFileFilter fileFilter) {
    ArrayList<XbmcScraper> scrapers = new ArrayList<XbmcScraper>();

    File addons = new File("xbmc_scraper");
    System.out.println("searchig for scrapers in: " + addons);
    if (addons != null && addons.exists()) {
      Collection<File> files = FileUtils.listFiles(addons, fileFilter, dirFilter);
      for (File f : files) {
        XbmcScraper x = new XbmcScraper(f.getParentFile()); // parent = folder
        if (!scrapers.contains(x)) {
          scrapers.add(x);
        }
      }
    }

    addons = new File(detectXbmcUserdataFolder(), "addons");
    System.out.println("searchig for scrapers in: " + addons);
    if (addons != null && addons.exists()) {
      Collection<File> files = FileUtils.listFiles(addons, fileFilter, dirFilter);
      for (File f : files) {
        XbmcScraper x = new XbmcScraper(f.getParentFile()); // parent = folder
        if (!scrapers.contains(x)) {
          scrapers.add(x);
        }
      }
    }

    addons = new File(detectXbmcFolder(), "addons");
    System.out.println("searchig for scrapers in: " + addons);
    if (addons != null && addons.exists()) {
      Collection<File> files = FileUtils.listFiles(addons, fileFilter, dirFilter);
      for (File f : files) {
        XbmcScraper x = new XbmcScraper(f.getParentFile()); // parent = folder
        if (!scrapers.contains(x)) {
          scrapers.add(x);
        }
      }
    }
    return scrapers;
  }

  /**
   * returns a list of all found scraper addons.xml
   * 
   * @return
   */
  public static ArrayList<XbmcScraper> getAllScrapers() {
    ArrayList<XbmcScraper> scrapers = new ArrayList<XbmcScraper>();

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

    scrapers = getXbmcAddons(dirFilter, fileFilter);

    if (scrapers.size() == 0) {
      System.out.println("Meh - could not find any scrapers...");
    }

    return scrapers;
  }

  /**
   * returns a list of all found common addons.xml
   * 
   * @return
   */
  public static ArrayList<XbmcScraper> getAllCommon() {
    ArrayList<XbmcScraper> common = new ArrayList<XbmcScraper>();

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
        // return pathname.getName().endsWith("xml") && !pathname.getName().equals("addon.xml");
      }

      @Override
      public boolean accept(File arg0, String arg1) {
        return false;
      }
    };

    common = getXbmcAddons(dirFilter, fileFilter);

    if (common.size() == 0) {
      System.out.println("Meh - could not find any common folders...");
    }

    return common;
  }

  /**
   * returns a list of all found common addons.xml
   * 
   * @return
   */
  public static ArrayList<File> getAllCommonXMLs() {
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

    for (XbmcScraper sc : getAllCommon()) {
      Collection<File> files = FileUtils.listFiles(sc.getFolder(), fileFilter, dirFilter);
      for (File f : files) {
        if (!common.contains(f)) {
          System.out.println("Found common: " + f);
          common.add(f);
        }
        else {
          System.out.println("Skipped common: " + f);
        }
      }
    }

    if (common.size() == 0) {
      System.out.println("Meh - could not find any common function...");
    }

    return common;
  }

}
