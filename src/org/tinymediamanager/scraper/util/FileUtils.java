package org.tinymediamanager.scraper.util;

import java.io.File;

import org.apache.log4j.Logger;

/**
 * Common file operations, in addition to the Commons FileUtils
 * 
 * @author seans
 */
public class FileUtils {
    private static Logger log = Logger.getLogger(FileUtils.class);
    
    public static void deleteQuietly(File f) {
        if (f==null) return;
        if (!f.delete()) {
            log.warn("Failed to delete file: " + f);
        }
    }
    
    public static void mkdirsQuietly(File f) {
        if (f==null || f.exists()) return;
        if (!f.mkdirs()) {
            log.warn("Failed to mkdirs() on " + f);
        }
    }

    public static void mkdirQuietly(File f) {
        if (f==null || f.exists()) return;
        if (!f.mkdir()) {
            log.warn("Failed to mkdir() on " + f);
        }
    }
    
    public static void setLastModifiedQuietly(File f, long time) {
        if (f==null) return;
        if (!f.setLastModified(time)) {
            log.warn("Failed to setLastModified("+time+") on " + f);
        }
    }
}

