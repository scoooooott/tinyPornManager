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
package org.tinymediamanager.scraper.util;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.scraper.mediaprovider.IMediaProvider;
import org.tinymediamanager.scraper.mediaprovider.IMediaSubtitleProvider;

import net.xeoh.plugins.base.Plugin;
import net.xeoh.plugins.base.impl.PluginManagerFactory;
import net.xeoh.plugins.base.options.addpluginsfrom.OptionReportAfter;
import net.xeoh.plugins.base.util.JSPFProperties;
import net.xeoh.plugins.base.util.PluginManagerUtil;
import net.xeoh.plugins.base.util.uri.ClassURI;

/**
 * This class manages loading of external plugins. It is intended to be accessed via TmmModuleManager to ensure controlled access (i.e. caching)
 * 
 * @author Manuel Laggner
 */
public class PluginManager {
  private final static Logger                        LOGGER = LoggerFactory.getLogger(PluginManager.class);
  private static net.xeoh.plugins.base.PluginManager pm;
  private static PluginManagerUtil                   pmu;
  private static PluginManager                       instance;

  public PluginManager() {
  }

  public synchronized static PluginManager getInstance() {
    if (instance == null) {
      JSPFProperties props = new JSPFProperties();
      props.setProperty(PluginManager.class, "cache.enabled", "true");
      props.setProperty(PluginManager.class, "cache.mode", "weak"); // optional
      props.setProperty(PluginManager.class, "cache.file", "jspf.cache");

      instance = new PluginManager();
      pm = PluginManagerFactory.createPluginManager(props);
      pmu = new PluginManagerUtil(pm);

      StopWatch stopWatch = new StopWatch();
      stopWatch.start();

      LOGGER.debug("loading classpath plugins...");
      // pm.addPluginsFrom(ClassURI.CLASSPATH); // sloooow

      // if (ReleaseInfo.getVersion().equals("SVN")) {
      // since we do not have them as dependencies, load all from classpath (we have dependent projects)
      pm.addPluginsFrom(ClassURI.CLASSPATH("org.tinymediamanager.scraper.**")); // 4 secs
      // }
      stopWatch.stop();
      LOGGER.debug("Done loading classpath plugins - took " + stopWatch);

      // dedicated folder just for plugins
      stopWatch.reset();
      stopWatch.start();
      LOGGER.debug("loading external plugins...");
      if (LOGGER.isTraceEnabled()) {
        pm.addPluginsFrom(new File("plugins/").toURI(), new OptionReportAfter());
      }
      else {
        pm.addPluginsFrom(new File("plugins/").toURI());
      }
      stopWatch.stop();
      LOGGER.debug("Done loading external plugins - took " + stopWatch);
    }
    return instance;
  }

  /**
   * get all plugins for the desired interface
   * 
   * @param iface
   *          the interface to search for plugins
   * @return all found plugins
   */
  public <T extends IMediaProvider> List<T> getPluginsForInterface(Class<T> iface) {
    List<T> plugins = new ArrayList<>();

    // get the right plugins
    for (T mp : pmu.getPlugins(iface)) {
      plugins.add(mp);
    }

    return plugins;
  }

  /**
   * All plugins implementing the IMediaSubtitleProvider
   */
  public List<IMediaSubtitleProvider> getSubtitlePlugins() {
    List<IMediaSubtitleProvider> plugins = new ArrayList<>();
    for (Plugin p : pmu.getPlugins(IMediaSubtitleProvider.class)) {
      plugins.add((IMediaSubtitleProvider) p);
    }
    return plugins;
  }
}
