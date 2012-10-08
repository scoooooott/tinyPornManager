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
package org.tinymediamanager.scraper.util;

import java.io.File;

import org.apache.log4j.Logger;

/**
 * Common file operations, in addition to the Commons FileUtils.
 *
 * @author seans
 */
public class FileUtils {
    
    /** The log. */
    private static Logger log = Logger.getLogger(FileUtils.class);
    
    /**
     * Delete quietly.
     *
     * @param f the f
     */
    public static void deleteQuietly(File f) {
        if (f==null) return;
        if (!f.delete()) {
            log.warn("Failed to delete file: " + f);
        }
    }
    
    /**
     * Mkdirs quietly.
     *
     * @param f the f
     */
    public static void mkdirsQuietly(File f) {
        if (f==null || f.exists()) return;
        if (!f.mkdirs()) {
            log.warn("Failed to mkdirs() on " + f);
        }
    }

    /**
     * Mkdir quietly.
     *
     * @param f the f
     */
    public static void mkdirQuietly(File f) {
        if (f==null || f.exists()) return;
        if (!f.mkdir()) {
            log.warn("Failed to mkdir() on " + f);
        }
    }
    
    /**
     * Sets the last modified quietly.
     *
     * @param f the f
     * @param time the time
     */
    public static void setLastModifiedQuietly(File f, long time) {
        if (f==null) return;
        if (!f.setLastModified(time)) {
            log.warn("Failed to setLastModified("+time+") on " + f);
        }
    }
}

