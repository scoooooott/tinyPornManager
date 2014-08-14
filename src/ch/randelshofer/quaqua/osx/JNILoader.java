/*
 * @(#)JNILoader.java  1.0  2013-03-21
 * 
 * Copyright (c) 2013 Werner Randelshofer, Switzerland.
 * All rights reserved.
 * 
 * You may not use, copy or modify this file, except in compliance with the 
 * license agreement you entered into with Werner Randelshofer. 
 * For details see accompanying license terms.
 */
package ch.randelshofer.quaqua.osx;

import java.io.File;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@code JNILoader}.
 *
 * @author Werner Randelshofer
 * @version 1.0 2013-03-21 Created.
 */
public class JNILoader {
  private static final Logger LOGGER = LoggerFactory.getLogger(JNILoader.class);

  public static void loadLibrary(String libName) {
    // Try to load the native library from the native dir
    try {
      String nativepath = new File(".").getCanonicalPath();
      nativepath += "/native/mac-" + System.getProperty("os.arch") + "/lib" + libName + ".jnilib";
      System.load(nativepath);
    }
    catch (Error e) {
      // could not load it from the native dir - try to load it per lib name 
      LOGGER.warn(e.getMessage());
      System.loadLibrary(libName);
    }
    catch (IOException e) {
      LOGGER.warn(e.getMessage());
      System.loadLibrary(libName);
    }
    /*
     * } catch (Error e) { JFrame f=new JFrame(); JTextArea l=new JTextArea(); StringWriter w=new StringWriter(); e.printStackTrace(new
     * PrintWriter(w)); l.setText(w.toString()); f.add(new JScrollPane(l)); f.pack(); f.show(); throw e; }
     */
  }
}