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

package org.tinymediamanager.ui.movies.dialogs;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Box;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTabbedPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;

import org.tinymediamanager.core.UTF8Control;
import org.tinymediamanager.ui.IconManager;
import org.tinymediamanager.ui.TmmWindowSaver;
import org.tinymediamanager.ui.components.MainTabbedPane;
import org.tinymediamanager.ui.components.TmmLabel;
import org.tinymediamanager.ui.dialogs.TmmDialog;
import org.tinymediamanager.ui.movies.MovieExtendedComparator;
import org.tinymediamanager.ui.movies.MovieExtendedComparator.SortColumn;
import org.tinymediamanager.ui.movies.MovieExtendedComparator.SortOrder;
import org.tinymediamanager.ui.movies.MovieSelectionModel;
import org.tinymediamanager.ui.movies.filters.IMovieUIFilter;
import org.tinymediamanager.ui.movies.filters.MovieAspectRatioFilter;
import org.tinymediamanager.ui.movies.filters.MovieAudioChannelFilter;
import org.tinymediamanager.ui.movies.filters.MovieAudioCodecFilter;
import org.tinymediamanager.ui.movies.filters.MovieCastFilter;
import org.tinymediamanager.ui.movies.filters.MovieCertificationFilter;
import org.tinymediamanager.ui.movies.filters.MovieCountryFilter;
import org.tinymediamanager.ui.movies.filters.MovieDatasourceFilter;
import org.tinymediamanager.ui.movies.filters.MovieDuplicateFilter;
import org.tinymediamanager.ui.movies.filters.MovieEditionFilter;
import org.tinymediamanager.ui.movies.filters.MovieFrameRateFilter;
import org.tinymediamanager.ui.movies.filters.MovieGenreFilter;
import org.tinymediamanager.ui.movies.filters.MovieInMovieSetFilter;
import org.tinymediamanager.ui.movies.filters.MovieLanguageFilter;
import org.tinymediamanager.ui.movies.filters.MovieMediaSourceFilter;
import org.tinymediamanager.ui.movies.filters.MovieMissingArtworkFilter;
import org.tinymediamanager.ui.movies.filters.MovieMissingMetadataFilter;
import org.tinymediamanager.ui.movies.filters.MovieMissingSubtitlesFilter;
import org.tinymediamanager.ui.movies.filters.MovieNewMoviesFilter;
import org.tinymediamanager.ui.movies.filters.MovieProductionCompanyFilter;
import org.tinymediamanager.ui.movies.filters.MovieTagFilter;
import org.tinymediamanager.ui.movies.filters.MovieVideo3DFilter;
import org.tinymediamanager.ui.movies.filters.MovieVideoCodecFilter;
import org.tinymediamanager.ui.movies.filters.MovieVideoContainerFilter;
import org.tinymediamanager.ui.movies.filters.MovieVideoExtrasFilter;
import org.tinymediamanager.ui.movies.filters.MovieVideoFormatFilter;
import org.tinymediamanager.ui.movies.filters.MovieWatchedFilter;
import org.tinymediamanager.ui.movies.filters.MovieYearFilter;

import net.miginfocom.swing.MigLayout;

public class MovieFilterDialog extends TmmDialog {
  private static final long                      serialVersionUID = 2298540526428945319L;
  /** @wbp.nls.resourceBundle messages */
  protected static final ResourceBundle          BUNDLE           = ResourceBundle.getBundle("messages", new UTF8Control());

  private final MovieSelectionModel              selectionModel;
  private final JComboBox<SortColumn>            cbSortColumn;
  private final JComboBox<SortOrder>             cbSortOrder;

  // map for storing which filter is in which panel
  private final Map<JPanel, Set<IMovieUIFilter>> filterMap;

  private JTabbedPane                            tabbedPane;

  public MovieFilterDialog(MovieSelectionModel selectionModel) {
    super(BUNDLE.getString("movieextendedsearch.options"), "movieFilter");
    setModalityType(ModalityType.MODELESS);

    this.selectionModel = selectionModel;
    this.filterMap = new HashMap<>();
    this.selectionModel.addPropertyChangeListener("filterChanged", evt -> filterChanged());

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

        addFilter(new MovieNewMoviesFilter(), panelMain);
        addFilter(new MovieDuplicateFilter(), panelMain);
        addFilter(new MovieWatchedFilter(), panelMain);
        addFilter(new MovieGenreFilter(), panelMain);
        addFilter(new MovieCertificationFilter(), panelMain);
        addFilter(new MovieYearFilter(), panelMain);
        addFilter(new MovieCastFilter(), panelMain);
        addFilter(new MovieCountryFilter(), panelMain);
        addFilter(new MovieLanguageFilter(), panelMain);
        addFilter(new MovieProductionCompanyFilter(), panelMain);
        addFilter(new MovieTagFilter(), panelMain);
        addFilter(new MovieEditionFilter(), panelMain);
        addFilter(new MovieInMovieSetFilter(), panelMain);
      }

      {
        // panel media data
        JPanel panelMediaData = new JPanel(new MigLayout("", "[][][100lp:n,grow]", "[]"));
        JScrollPane scrollPaneMediaData = new JScrollPane(panelMediaData);
        scrollPaneMediaData.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        tabbedPane.addTab(BUNDLE.getString("metatag.mediainformation"), scrollPaneMediaData);
        panelMediaData.add(new TmmLabel(BUNDLE.getString("movieextendedsearch.filterby")), "cell 0 0 3 1, growx, aligny top, wrap");

        addFilter(new MovieVideoFormatFilter(), panelMediaData);
        addFilter(new MovieVideoCodecFilter(), panelMediaData);
        addFilter(new MovieAspectRatioFilter(), panelMediaData);
        addFilter(new MovieFrameRateFilter(), panelMediaData);
        addFilter(new MovieVideo3DFilter(), panelMediaData);
        addFilter(new MovieVideoContainerFilter(), panelMediaData);
        addFilter(new MovieAudioCodecFilter(), panelMediaData);
        addFilter(new MovieAudioChannelFilter(), panelMediaData);
        addFilter(new MovieDatasourceFilter(), panelMediaData);
        addFilter(new MovieMediaSourceFilter(), panelMediaData);
        addFilter(new MovieVideoExtrasFilter(), panelMediaData);
        addFilter(new MovieMissingMetadataFilter(), panelMediaData);
        addFilter(new MovieMissingArtworkFilter(), panelMediaData);
        addFilter(new MovieMissingSubtitlesFilter(), panelMediaData);
      }

      {
        // panel sort
        JPanel panelSort = new JPanel();
        panelSort.setLayout(new MigLayout("insets n 0 n 0", "[5lp!][10lp][150lp,grow][5lp!]", "[]"));

        JSeparator separator = new JSeparator();
        panelSort.add(separator, "cell 0 1 4 1,growx,aligny top");

        JLabel lblSortBy = new TmmLabel(BUNDLE.getString("movieextendedsearch.sortby"));
        panelSort.add(lblSortBy, "cell 1 2,growx,aligny top");

        cbSortColumn = new JComboBox();
        for (MovieExtendedComparator.SortColumn column : MovieExtendedComparator.SortColumn.values()) {
          cbSortColumn.addItem(column);
        }

        Action actionSort = new SortAction();
        cbSortColumn.setAction(actionSort);
        panelSort.add(cbSortColumn, "cell 1 3,growx,aligny top");

        cbSortOrder = new JComboBox();
        for (MovieExtendedComparator.SortOrder order : MovieExtendedComparator.SortOrder.values()) {
          cbSortOrder.addItem(order);
        }
        cbSortOrder.setAction(actionSort);
        panelSort.add(cbSortOrder, "cell 2 3,growx,aligny top");

        getContentPane().add(panelSort, BorderLayout.SOUTH);
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
  private void addFilter(IMovieUIFilter filter, JPanel panel) {
    panel.add(filter.getCheckBox(), "");
    panel.add(filter.getLabel(), "right");

    if (filter.getFilterComponent() != null) {
      panel.add(filter.getFilterComponent(), "wmin 100, grow, wrap");
    }
    else {
      panel.add(Box.createGlue(), "wrap");
    }

    Set<IMovieUIFilter> filters = filterMap.computeIfAbsent(panel, k -> new HashSet<>());
    filters.add(filter);

    selectionModel.addFilter(filter);
  }

  /**
   * re-calculate if the active filter icon should be displayed
   */
  private void filterChanged() {
    for (Map.Entry<JPanel, Set<IMovieUIFilter>> entry : filterMap.entrySet()) {
      boolean active = false;
      for (IMovieUIFilter filter : entry.getValue()) {
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

  private class SortAction extends AbstractAction {
    private static final long serialVersionUID = -4057379119252539003L;

    @Override
    public void actionPerformed(ActionEvent e) {
      MovieExtendedComparator.SortColumn column = (MovieExtendedComparator.SortColumn) cbSortColumn.getSelectedItem();
      MovieExtendedComparator.SortOrder order = (MovieExtendedComparator.SortOrder) cbSortOrder.getSelectedItem();
      boolean ascending = order == MovieExtendedComparator.SortOrder.ASCENDING;

      // sort
      selectionModel.sortMovies(column, ascending);
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
