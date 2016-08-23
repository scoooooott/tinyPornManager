/*
 * Copyright 2012 - 2016 Manuel Laggner
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
package org.tinymediamanager.ui.movies;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.util.ResourceBundle;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Box;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;

import org.tinymediamanager.Globals;
import org.tinymediamanager.ui.UTF8Control;
import org.tinymediamanager.ui.movies.MovieExtendedComparator.SortColumn;
import org.tinymediamanager.ui.movies.MovieExtendedComparator.SortOrder;
import org.tinymediamanager.ui.movies.filters.MovieAudioCodecFilter;
import org.tinymediamanager.ui.movies.filters.MovieCastFilter;
import org.tinymediamanager.ui.movies.filters.MovieCertificationFilter;
import org.tinymediamanager.ui.movies.filters.MovieDatasourceFilter;
import org.tinymediamanager.ui.movies.filters.MovieDuplicateFilter;
import org.tinymediamanager.ui.movies.filters.MovieGenreFilter;
import org.tinymediamanager.ui.movies.filters.MovieInMovieSetFilter;
import org.tinymediamanager.ui.movies.filters.MovieMediaSourceFilter;
import org.tinymediamanager.ui.movies.filters.MovieMissingArtworkFilter;
import org.tinymediamanager.ui.movies.filters.MovieMissingMetadataFilter;
import org.tinymediamanager.ui.movies.filters.MovieMissingSubtitlesFilter;
import org.tinymediamanager.ui.movies.filters.MovieNewMoviesFilter;
import org.tinymediamanager.ui.movies.filters.MovieTagFilter;
import org.tinymediamanager.ui.movies.filters.MovieVideo3DFilter;
import org.tinymediamanager.ui.movies.filters.MovieVideoCodecFilter;
import org.tinymediamanager.ui.movies.filters.MovieVideoFormatFilter;
import org.tinymediamanager.ui.movies.filters.MovieWatchedFilter;
import org.tinymediamanager.ui.movies.filters.MovieYearFilter;
import org.tinymediamanager.ui.panels.RoundedPanel;

import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.FormSpecs;
import com.jgoodies.forms.layout.RowSpec;

/**
 * The Class MovieExtendedSearchPanel.
 * 
 * @author Manuel Laggner
 */
public class MovieExtendedSearchPanel extends RoundedPanel {
  private static final long           serialVersionUID = -4170930017190753789L;
  /**
   * @wbp.nls.resourceBundle messages
   */
  private static final ResourceBundle BUNDLE           = ResourceBundle.getBundle("messages", new UTF8Control()); //$NON-NLS-1$
  private static final float          FONT_SIZE        = Math.round(Globals.settings.getFontSize() * 0.916);

  private MovieSelectionModel         movieSelectionModel;
  private JComboBox<SortColumn>       cbSortColumn;
  private JComboBox<SortOrder>        cbSortOrder;

  private final Action                actionSort       = new SortAction();
  private JPanel                      panelFilter;

  /**
   * Instantiates a new movie extended search
   * 
   * @param model
   *          the model
   */
  public MovieExtendedSearchPanel(MovieSelectionModel model) {
    super();
    setOpaque(false);
    arcs = new Dimension(10, 10);

    this.movieSelectionModel = model;

    // add a dummy mouse listener to prevent clicking through
    addMouseListener(new MouseAdapter() {
    });

    setLayout(new FormLayout(
        new ColumnSpec[] { FormSpecs.RELATED_GAP_COLSPEC, ColumnSpec.decode("default:grow"), FormSpecs.RELATED_GAP_COLSPEC,
            ColumnSpec.decode("default:grow"), FormSpecs.RELATED_GAP_COLSPEC, ColumnSpec.decode("default:grow"), FormSpecs.UNRELATED_GAP_COLSPEC, },
        new RowSpec[] { FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC, FormSpecs.LABEL_COMPONENT_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC,
            FormSpecs.DEFAULT_ROWSPEC, FormSpecs.LABEL_COMPONENT_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC, FormSpecs.LABEL_COMPONENT_GAP_ROWSPEC,
            FormSpecs.DEFAULT_ROWSPEC, FormSpecs.UNRELATED_GAP_ROWSPEC, }));

    JLabel lblFilterBy = new JLabel(BUNDLE.getString("movieextendedsearch.filterby")); //$NON-NLS-1$
    setComponentFont(lblFilterBy);
    add(lblFilterBy, "2, 2, 3, 1");

    panelFilter = new JPanel();
    GridBagLayout gbl_panelFilter = new GridBagLayout();
    gbl_panelFilter.columnWidths = new int[] { 0 };
    gbl_panelFilter.rowHeights = new int[] { 0 };
    gbl_panelFilter.columnWeights = new double[] { Double.MIN_VALUE };
    gbl_panelFilter.rowWeights = new double[] { Double.MIN_VALUE };
    panelFilter.setLayout(gbl_panelFilter);
    add(panelFilter, "2, 4, 5, 1, fill, fill");

    addFilter(new MovieNewMoviesFilter());
    addFilter(new MovieDuplicateFilter());
    addFilter(new MovieWatchedFilter());
    addFilter(new MovieGenreFilter());
    addFilter(new MovieCertificationFilter());
    addFilter(new MovieYearFilter());
    addFilter(new MovieCastFilter());
    addFilter(new MovieTagFilter());
    addFilter(new MovieInMovieSetFilter());
    addFilter(new MovieVideoFormatFilter());
    addFilter(new MovieVideoCodecFilter());
    addFilter(new MovieVideo3DFilter());
    addFilter(new MovieAudioCodecFilter());
    addFilter(new MovieDatasourceFilter());
    addFilter(new MovieMediaSourceFilter());
    addFilter(new MovieMissingMetadataFilter());
    addFilter(new MovieMissingArtworkFilter());
    addFilter(new MovieMissingSubtitlesFilter());

    JSeparator separator = new JSeparator();
    add(separator, "2, 5, 5, 1");

    JLabel lblSortBy = new JLabel(BUNDLE.getString("movieextendedsearch.sortby")); //$NON-NLS-1$
    setComponentFont(lblSortBy);
    add(lblSortBy, "2, 7, 3, 1");

    cbSortColumn = new JComboBox<>();
    for (SortColumn column : SortColumn.values()) {
      cbSortColumn.addItem(column);
    }
    setComponentFont(cbSortColumn);
    cbSortColumn.setAction(actionSort);
    add(cbSortColumn, "2, 9, 3, 1, fill, default");

    cbSortOrder = new JComboBox<>();
    for (SortOrder order : SortOrder.values()) {
      cbSortOrder.addItem(order);
    }
    setComponentFont(cbSortOrder);
    cbSortOrder.setAction(actionSort);
    add(cbSortOrder, "6, 9, fill, default");
  }

  /**
   * add a new filter to the panel
   * 
   * @param tvShowWatchedFilter
   */
  private void addFilter(IMovieUIFilter filter) {
    GridBagConstraints gbc = new GridBagConstraints();

    gbc.gridx = 0;
    gbc.ipadx = 2;
    gbc.fill = GridBagConstraints.NONE;
    gbc.anchor = GridBagConstraints.LINE_START;
    panelFilter.add(filter.getCheckBox(), gbc);

    gbc.gridx = 1;
    gbc.anchor = GridBagConstraints.LINE_END;
    panelFilter.add(filter.getLabel(), gbc);

    gbc.gridx = 2;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.anchor = GridBagConstraints.LINE_START;

    if (filter.getFilterComponent() != null) {
      panelFilter.add(filter.getFilterComponent(), gbc);
    }
    else {
      panelFilter.add(Box.createGlue(), gbc);
    }

    movieSelectionModel.addFilter(filter);
  }

  private class SortAction extends AbstractAction {
    private static final long serialVersionUID = -4057379119252539003L;

    @Override
    public void actionPerformed(ActionEvent e) {
      SortColumn column = (SortColumn) cbSortColumn.getSelectedItem();
      SortOrder order = (SortOrder) cbSortOrder.getSelectedItem();
      boolean ascending = order == SortOrder.ASCENDING ? true : false;

      // sort
      movieSelectionModel.sortMovies(column, ascending);
    }
  }

  private void setComponentFont(JComponent comp) {
    comp.setFont(comp.getFont().deriveFont(FONT_SIZE));
  }
}