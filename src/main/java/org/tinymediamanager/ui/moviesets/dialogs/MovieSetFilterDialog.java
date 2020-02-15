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

package org.tinymediamanager.ui.moviesets.dialogs;

import java.awt.BorderLayout;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

import javax.swing.Box;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;

import org.tinymediamanager.Globals;
import org.tinymediamanager.core.UTF8Control;
import org.tinymediamanager.ui.IconManager;
import org.tinymediamanager.ui.TmmWindowSaver;
import org.tinymediamanager.ui.components.MainTabbedPane;
import org.tinymediamanager.ui.components.TmmLabel;
import org.tinymediamanager.ui.components.tree.TmmTreeNode;
import org.tinymediamanager.ui.components.treetable.TmmTreeTable;
import org.tinymediamanager.ui.dialogs.TmmDialog;
import org.tinymediamanager.ui.moviesets.filters.IMovieSetUIFilter;
import org.tinymediamanager.ui.moviesets.filters.MovieSetDatasourceFilter;
import org.tinymediamanager.ui.moviesets.filters.MovieSetNewMoviesFilter;
import org.tinymediamanager.ui.moviesets.filters.MovieSetWithMoreThanOneMovieFilter;

import net.miginfocom.swing.MigLayout;

public class MovieSetFilterDialog extends TmmDialog {
  private static final long                         serialVersionUID = 5003714573168481816L;
  /** @wbp.nls.resourceBundle messages */
  protected static final ResourceBundle             BUNDLE           = ResourceBundle.getBundle("messages", new UTF8Control());

  private static final float                        FONT_SIZE        = Math.round(Globals.settings.getFontSize() * 0.916);

  private final TmmTreeTable                        treeTable;

  // map for storing which filter is in which panel
  private final Map<JPanel, Set<IMovieSetUIFilter>> filterMap;

  private JTabbedPane                               tabbedPane;

  public MovieSetFilterDialog(TmmTreeTable treeTable) {
    super(BUNDLE.getString("movieextendedsearch.options"), "movieSetFilter");
    setModalityType(ModalityType.MODELESS);

    this.treeTable = treeTable;

    this.filterMap = new HashMap<>();
    this.treeTable.addPropertyChangeListener("filterChanged", evt -> filterChanged());

    {
      tabbedPane = new MainTabbedPane() {
        private static final long serialVersionUID = 9041548865608767661L;

        @Override
        public void updateUI() {
          putClientProperty("leftBorder", "half");
          putClientProperty("rightBorder", "half");
          putClientProperty("bottomBorder", Boolean.FALSE);
          super.updateUI();
        }
      };
      getContentPane().add(tabbedPane, BorderLayout.CENTER);

      {
        // panel Main
        JPanel panelMain = new JPanel(new MigLayout("", "[][][100lp:n,grow]", "[]"));
        JScrollPane scrollPaneMain = new JScrollPane(panelMain);
        scrollPaneMain.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        tabbedPane.addTab(BUNDLE.getString("metatag.details"), scrollPaneMain);

        panelMain.add(new TmmLabel(BUNDLE.getString("movieextendedsearch.filterby")), "cell 0 0 3 1, growx, aligny top, wrap");

        addFilter(new MovieSetNewMoviesFilter(), panelMain);
        addFilter(new MovieSetWithMoreThanOneMovieFilter(), panelMain);
        addFilter(new MovieSetDatasourceFilter(), panelMain);
      }
    }
  }

  /**
   * add a new filter to the panel and selection model
   *
   * @param filter
   *          the filter to be added
   * @param panel
   *          the panel to add the filter to
   */
  private void addFilter(IMovieSetUIFilter<TmmTreeNode> filter, JPanel panel) {
    panel.add(filter.getCheckBox(), "");
    panel.add(filter.getLabel(), "right");

    if (filter.getFilterComponent() != null) {
      panel.add(filter.getFilterComponent(), "wmin 100, grow, wrap");
    }
    else {
      panel.add(Box.createGlue(), "wrap");
    }

    Set<IMovieSetUIFilter> filters = filterMap.computeIfAbsent(panel, k -> new HashSet<>());
    filters.add(filter);

    treeTable.addFilter(filter);
  }

  /**
   * re-calculate if the active filter icon should be displayed
   */
  private void filterChanged() {
    for (Map.Entry<JPanel, Set<IMovieSetUIFilter>> entry : filterMap.entrySet()) {
      boolean active = false;
      for (IMovieSetUIFilter filter : entry.getValue()) {
        switch (filter.getFilterState()) {
          case ACTIVE:
          case ACTIVE_NEGATIVE:
            active = true;
            break;

          default:
            break;
        }

        if (active) {
          break;
        }
      }

      for (int i = 0; i < tabbedPane.getTabCount(); i++) {
        if (SwingUtilities.isDescendingFrom(entry.getKey(), tabbedPane.getComponentAt(i))) {
          if (active) {
            tabbedPane.setIconAt(i, IconManager.FILTER_ACTIVE);
          }
          else {
            tabbedPane.setIconAt(i, null);
          }

          break;
        }
      }
    }
  }

  @Override
  protected void initBottomPanel() {
    // no bottom line needed
  }

  @Override
  public void dispose() {
    // do not dispose (singleton), but save the size/position
    TmmWindowSaver.getInstance().saveSettings(this);
  }
}
