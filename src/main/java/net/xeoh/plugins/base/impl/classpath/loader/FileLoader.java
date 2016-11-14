/*
 * InternalClasspathLoader.java
 * 
 * Copyright (c) 2009, Ralf Biedert All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modification, are
 * permitted provided that the following conditions are met:
 * 
 * Redistributions of source code must retain the above copyright notice, this list of
 * conditions and the following disclaimer. Redistributions in binary form must reproduce the
 * above copyright notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 * 
 * Neither the name of the author nor the names of its contributors may be used to endorse or
 * promote products derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS
 * OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR
 * TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * 
 */
package net.xeoh.plugins.base.impl.classpath.loader;

import java.io.File;
import java.net.URI;
import java.util.Collection;

import net.xeoh.plugins.base.Plugin;
import net.xeoh.plugins.base.impl.PluginManagerImpl;
import net.xeoh.plugins.base.impl.classpath.ClassPathManager;
import net.xeoh.plugins.base.impl.classpath.locator.AbstractClassPathLocation;
import net.xeoh.plugins.base.impl.classpath.locator.ClassPathLocator;
import net.xeoh.plugins.base.impl.classpath.locator.locations.JARClasspathLocation;
import net.xeoh.plugins.base.options.AddPluginsFromOption;

/**
 * @author rb
 *
 */
public class FileLoader extends AbstractLoader {

  /**
   * @param pluginManager
   */
  public FileLoader(PluginManagerImpl pluginManager) {
    super(pluginManager);
  }

  @Override
  public boolean handlesURI(URI uri) {
    if (uri != null && "file".equals(uri.getScheme()))
      return true;
    return false;
  }

  @Override
  public void loadFrom(URI url, AddPluginsFromOption[] options) {
    // If not caught by the previous handler, handle files normally.
    if (url.getScheme().equals("file")) {

      // Get the actual file from the given path (toPath() strips out the hostname from smb shares)
      String file = url.toString().replace("file:", "");
      if (file.startsWith("/") && file.substring(0, 4).contains(":")) {
        file = file.substring(1);
      }

      // patch to enable loading of plugins in a dir containing a +
      /*
       * // Try to decode the path properly try { file = URLDecoder.decode(file, "utf8"); } catch (final UnsupportedEncodingException e) {
       * e.printStackTrace(); }
       */

      // Now load from the given file ...
      this.logger.fine("More specifically, trying to add from " + file);
      final File root = new File(file);

      // ... if it exists ...
      if (!root.exists()) {
        this.logger.warning("Supplied path does not exist. Unable to add plugins from there.");
        return;
      }

      // Here we go
      locateAllPluginsAt(root);
      return;
    }
  }

  /**
   * Given a top level directory, we locate all classpath locations and load all plugins we find.
   *
   * @param root
   *          The top level to start from.
   */
  void locateAllPluginsAt(File root) {
    final ClassPathManager manager = this.pluginManager.getClassPathManager();
    final ClassPathLocator locator = manager.getLocator();

    final Collection<AbstractClassPathLocation> locations = locator.findBelow(root.toURI());
    for (AbstractClassPathLocation location : locations) {
      manager.registerLocation(location);

      Collection<String> subclasses = null;

      // Check if it has a list of plugins
      if (location instanceof JARClasspathLocation) {
        final JARClasspathLocation jarLocation = (JARClasspathLocation) location;
        subclasses = jarLocation.getPredefinedPluginList();
      }

      // Add all found files ... if we have no predefined list
      if (subclasses == null)
        subclasses = manager.findSubclassesFor(location, Plugin.class);

      // Try to load them
      for (String string : subclasses) {
        tryToLoadClassAsPlugin(location, string);
      }
    }
  }
}