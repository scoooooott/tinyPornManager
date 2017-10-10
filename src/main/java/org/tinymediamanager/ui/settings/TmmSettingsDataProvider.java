/*
 * Copyright 2012 - 2017 Manuel Laggner
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

package org.tinymediamanager.ui.settings;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import org.tinymediamanager.ui.UTF8Control;
import org.tinymediamanager.ui.components.tree.TmmTreeDataProvider;
import org.tinymediamanager.ui.components.tree.TmmTreeNode;
import org.tinymediamanager.ui.movies.MovieUIModule;
import org.tinymediamanager.ui.tvshows.TvShowUIModule;

/**
 * This data provider manages the settings view
 *
 * @author Manuel Laggner
 */
public class TmmSettingsDataProvider extends TmmTreeDataProvider<TmmTreeNode> {
  private static final ResourceBundle BUNDLE = ResourceBundle.getBundle("messages", new UTF8Control()); //$NON-NLS-1$
  private TmmTreeNode                 root;

  public TmmSettingsDataProvider() {
    TmmSettingsNode rootSettingsNode = new TmmSettingsNode("", null);
    root = new TmmTreeNode(rootSettingsNode, this);

    // build up the settings structure
    TmmSettingsNode generalSettingsNode = new TmmSettingsNode(BUNDLE.getString("Settings.general"), new UiSettingsPanel()); //$NON-NLS-1$
    generalSettingsNode.addChild(new TmmSettingsNode(BUNDLE.getString("Settings.filetypes"), new FileTypesSettingsPanel())); //$NON-NLS-1$
    generalSettingsNode.addChild(new TmmSettingsNode(BUNDLE.getString("Settings.sorting"), new SortTitleSettingsPanel())); //$NON-NLS-1$
    generalSettingsNode.addChild(new TmmSettingsNode(BUNDLE.getString("Settings.externaldevices"), new ExternalDevicesSettingsPanel())); //$NON-NLS-1$
    generalSettingsNode.addChild(new TmmSettingsNode(BUNDLE.getString("Settings.externalservices"), new ExternalServicesSettingsPanel())); //$NON-NLS-1$
    generalSettingsNode.addChild(new TmmSettingsNode(BUNDLE.getString("Settings.system"), new SystemSettingsPanel())); //$NON-NLS-1$
    generalSettingsNode.addChild(new TmmSettingsNode(BUNDLE.getString("Settings.misc"), new GeneralSettingsPanel())); //$NON-NLS-1$
    rootSettingsNode.addChild(generalSettingsNode);

    // movie settings
    rootSettingsNode.addChild(MovieUIModule.getInstance().getSettingsNode());

    // TV show settings
    rootSettingsNode.addChild(TvShowUIModule.getInstance().getSettingsNode());
  }

  @Override
  public TmmTreeNode getRoot() {
    return root;
  }

  @Override
  public TmmTreeNode getParent(TmmTreeNode node) {
    if (node == root) {
      return null;
    }

    TmmSettingsNode settingsNode = (TmmSettingsNode) node.getUserObject();
    if (settingsNode == null || settingsNode.getParent() == null) {
      return root;
    }

    return getNodeFromCache(settingsNode.getParent());
  }

  @Override
  public List<TmmTreeNode> getChildren(TmmTreeNode parent) {
    List<TmmTreeNode> children = new ArrayList<>();

    if (parent.getUserObject() instanceof TmmSettingsNode) {
      TmmSettingsNode settingsNode = (TmmSettingsNode) parent.getUserObject();
      if (settingsNode != null) {
        for (TmmSettingsNode child : settingsNode.getChildren()) {
          TmmTreeNode node = new TmmTreeNode(child, this);
          putNodeToCache(child, node);
          children.add(node);
        }
      }
    }

    return children;
  }

  @Override
  public boolean isLeaf(TmmTreeNode node) {
    if (node == root) {
      return false;
    }

    if (node.getUserObject() instanceof TmmSettingsNode && ((TmmSettingsNode) node.getUserObject()).getChildren().isEmpty()) {
      return true;
    }

    return false;
  }
}
