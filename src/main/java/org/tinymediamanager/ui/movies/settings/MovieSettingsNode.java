/*
 * Copyright 2012 - 2018 Manuel Laggner
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

package org.tinymediamanager.ui.movies.settings;

import java.util.ResourceBundle;

import org.tinymediamanager.ui.UTF8Control;
import org.tinymediamanager.ui.settings.TmmSettingsNode;

/**
 * the class {@link MovieSettingsNode} provides all settings pages
 * 
 * @author Manuel Laggner
 */
public class MovieSettingsNode extends TmmSettingsNode {
  private final static ResourceBundle BUNDLE = ResourceBundle.getBundle("messages", new UTF8Control()); //$NON-NLS-1$

  public MovieSettingsNode() {
    super(BUNDLE.getString("Settings.movies"), new MovieSettingsPanel()); //$NON-NLS-1$

    addChild(new TmmSettingsNode(BUNDLE.getString("Settings.datasourceandnfo"), new MovieDatasourceSettingsPanel())); //$NON-NLS-1$

    TmmSettingsNode scraperSettingsNode = new TmmSettingsNode(BUNDLE.getString("Settings.scraper"), new MovieScraperSettingsPanel()); //$NON-NLS-1$
    scraperSettingsNode.addChild(new TmmSettingsNode(BUNDLE.getString("Settings.scraper.options"), new MovieScraperOptionsSettingsPanel()));//$NON-NLS-1$
    scraperSettingsNode.addChild(new TmmSettingsNode(BUNDLE.getString("Settings.nfo"), new MovieScraperNfoSettingsPanel()));//$NON-NLS-1$
    addChild(scraperSettingsNode);

    TmmSettingsNode imageSettingsNode = new TmmSettingsNode(BUNDLE.getString("Settings.images"), new MovieImageSettingsPanel());//$NON-NLS-1$
    imageSettingsNode.addChild(new TmmSettingsNode(BUNDLE.getString("Settings.artwork.naming"), new MovieImageTypeSettingsPanel()));//$NON-NLS-1$
    imageSettingsNode.addChild(new TmmSettingsNode(BUNDLE.getString("Settings.extraartwork"), new MovieImageExtraPanel()));//$NON-NLS-1$
    addChild(imageSettingsNode);

    addChild(new TmmSettingsNode(BUNDLE.getString("Settings.trailer"), new MovieTrailerSettingsPanel()));//$NON-NLS-1$
    addChild(new TmmSettingsNode(BUNDLE.getString("Settings.subtitle"), new MovieSubtitleSettingsPanel()));//$NON-NLS-1$
    addChild(new TmmSettingsNode(BUNDLE.getString("Settings.renamer"), new MovieRenamerSettingsPanel()));//$NON-NLS-1$
  }
}
