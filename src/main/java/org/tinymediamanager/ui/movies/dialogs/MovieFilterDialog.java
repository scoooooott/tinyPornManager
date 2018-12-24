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

package org.tinymediamanager.ui.movies.dialogs;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.util.ResourceBundle;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Box;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.ScrollPaneConstants;

import org.tinymediamanager.Globals;
import org.tinymediamanager.ui.TmmWindowSaver;
import org.tinymediamanager.ui.UTF8Control;
import org.tinymediamanager.ui.components.TmmLabel;
import org.tinymediamanager.ui.dialogs.TmmDialog;
import org.tinymediamanager.ui.movies.IMovieUIFilter;
import org.tinymediamanager.ui.movies.MovieExtendedComparator;
import org.tinymediamanager.ui.movies.MovieExtendedComparator.SortColumn;
import org.tinymediamanager.ui.movies.MovieExtendedComparator.SortOrder;
import org.tinymediamanager.ui.movies.MovieSelectionModel;
import org.tinymediamanager.ui.movies.filters.MovieAudioCodecFilter;
import org.tinymediamanager.ui.movies.filters.MovieCastFilter;
import org.tinymediamanager.ui.movies.filters.MovieCertificationFilter;
import org.tinymediamanager.ui.movies.filters.MovieCountryFilter;
import org.tinymediamanager.ui.movies.filters.MovieDatasourceFilter;
import org.tinymediamanager.ui.movies.filters.MovieDuplicateFilter;
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
import org.tinymediamanager.ui.movies.filters.MovieVideoExtrasFilter;
import org.tinymediamanager.ui.movies.filters.MovieVideoFormatFilter;
import org.tinymediamanager.ui.movies.filters.MovieWatchedFilter;
import org.tinymediamanager.ui.movies.filters.MovieYearFilter;

import net.miginfocom.swing.MigLayout;

public class MovieFilterDialog extends TmmDialog {
  private static final long             serialVersionUID = 2298540526428945319L;
  /** @wbp.nls.resourceBundle messages */
  protected static final ResourceBundle BUNDLE           = ResourceBundle.getBundle("messages", new UTF8Control()); //$NON-NLS-1$

  private static final float            FONT_SIZE        = Math.round(Globals.settings.getFontSize() * 0.916);

  private final MovieSelectionModel     selectionModel;
  private final JPanel                  panelFilter;
  private final JComboBox<SortColumn>   cbSortColumn;
  private final JComboBox<SortOrder>    cbSortOrder;

  public MovieFilterDialog(MovieSelectionModel selectionModel) {
    super(BUNDLE.getString("movieextendedsearch.options"), "movieFilter");
    setModalityType(ModalityType.MODELESS);

    this.selectionModel = selectionModel;

    {
      JPanel panelContent = new JPanel();
      getContentPane().add(panelContent, BorderLayout.CENTER);
      panelContent.setLayout(new MigLayout("", "[][10lp][100lp,grow]", "[][400lp:n,grow][][][]"));

      JLabel lblFilterBy = new TmmLabel(BUNDLE.getString("movieextendedsearch.filterby")); //$NON-NLS-1$
      setComponentFont(lblFilterBy);
      panelContent.add(lblFilterBy, "cell 0 0,growx,aligny top");

      panelFilter = new JPanel(new MigLayout("", "[][][100lp:n,grow]", "[]"));

      JScrollPane scrollPane = new JScrollPane(panelFilter);
      scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
      panelContent.add(scrollPane, "cell 0 1 3 1,grow");

      addFilter(new MovieNewMoviesFilter());
      addFilter(new MovieDuplicateFilter());
      addFilter(new MovieWatchedFilter());
      addFilter(new MovieGenreFilter());
      addFilter(new MovieCertificationFilter());
      addFilter(new MovieYearFilter());
      addFilter(new MovieCastFilter());
      addFilter(new MovieCountryFilter());
      addFilter(new MovieLanguageFilter());
      addFilter(new MovieProductionCompanyFilter());
      addFilter(new MovieTagFilter());
      addFilter(new MovieInMovieSetFilter());
      addFilter(new MovieVideoFormatFilter());
      addFilter(new MovieVideoCodecFilter());
      addFilter(new MovieFrameRateFilter());
      addFilter(new MovieVideo3DFilter());
      addFilter(new MovieAudioCodecFilter());
      addFilter(new MovieDatasourceFilter());
      addFilter(new MovieMediaSourceFilter());
      addFilter(new MovieVideoExtrasFilter());
      addFilter(new MovieMissingMetadataFilter());
      addFilter(new MovieMissingArtworkFilter());
      addFilter(new MovieMissingSubtitlesFilter());

      JSeparator separator = new JSeparator();
      panelContent.add(separator, "cell 0 2 3 1,growx,aligny top");

      JLabel lblSortBy = new TmmLabel(BUNDLE.getString("movieextendedsearch.sortby")); //$NON-NLS-1$
      setComponentFont(lblSortBy);
      panelContent.add(lblSortBy, "cell 0 3,growx,aligny top");

      cbSortColumn = new JComboBox();
      for (MovieExtendedComparator.SortColumn column : MovieExtendedComparator.SortColumn.values()) {
        cbSortColumn.addItem(column);
      }
      setComponentFont(cbSortColumn);
      Action actionSort = new SortAction();
      cbSortColumn.setAction(actionSort);
      panelContent.add(cbSortColumn, "cell 0 4,growx,aligny top");

      cbSortOrder = new JComboBox();
      for (MovieExtendedComparator.SortOrder order : MovieExtendedComparator.SortOrder.values()) {
        cbSortOrder.addItem(order);
      }
      setComponentFont(cbSortOrder);
      cbSortOrder.setAction(actionSort);
      panelContent.add(cbSortOrder, "cell 2 4,growx,aligny top");
    }
  }

  /**
   * add a new filter to the panel and selection model
   *
   * @param filter
   *          the filter to be added
   */
  private void addFilter(IMovieUIFilter filter) {
    panelFilter.add(filter.getCheckBox(), "");
    panelFilter.add(filter.getLabel(), "right");

    if (filter.getFilterComponent() != null) {
      panelFilter.add(filter.getFilterComponent(), "wmin 100, grow, wrap");
    }
    else {
      panelFilter.add(Box.createGlue(), "wrap");
    }

    selectionModel.addFilter(filter);
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

  private void setComponentFont(JComponent comp) {
    comp.setFont(comp.getFont().deriveFont(FONT_SIZE));
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
