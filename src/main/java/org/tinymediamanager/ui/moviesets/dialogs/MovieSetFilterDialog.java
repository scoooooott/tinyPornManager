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

package org.tinymediamanager.ui.moviesets.dialogs;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.ResourceBundle;

import javax.swing.Box;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;

import org.tinymediamanager.Globals;
import org.tinymediamanager.ui.TmmWindowSaver;
import org.tinymediamanager.ui.UTF8Control;
import org.tinymediamanager.ui.components.TmmLabel;
import org.tinymediamanager.ui.components.tree.TmmTreeNode;
import org.tinymediamanager.ui.components.treetable.TmmTreeTable;
import org.tinymediamanager.ui.dialogs.TmmDialog;
import org.tinymediamanager.ui.moviesets.IMovieSetUIFilter;
import org.tinymediamanager.ui.moviesets.filters.MovieSetDatasourceFilter;
import org.tinymediamanager.ui.moviesets.filters.MovieSetNewMoviesFilter;
import org.tinymediamanager.ui.moviesets.filters.MovieSetWithMoreThanOneMovieFilter;

import net.miginfocom.swing.MigLayout;

public class MovieSetFilterDialog extends TmmDialog {
  private static final long             serialVersionUID = 5003714573168481816L;
  /** @wbp.nls.resourceBundle messages */
  protected static final ResourceBundle BUNDLE           = ResourceBundle.getBundle("messages", new UTF8Control()); //$NON-NLS-1$

  private static final float            FONT_SIZE        = Math.round(Globals.settings.getFontSize() * 0.916);

  private final TmmTreeTable            treeTable;
  private final JPanel                  panelFilter;

  public MovieSetFilterDialog(TmmTreeTable treeTable) {
    super(BUNDLE.getString("movieextendedsearch.options"), "movieSetFilter");
    setModalityType(ModalityType.MODELESS);

    this.treeTable = treeTable;

    {
      JPanel panelContent = new JPanel();
      getContentPane().add(panelContent, BorderLayout.CENTER);
      panelContent.setLayout(new MigLayout("", "[][10lp][]", "[][50lp:n,grow][][][]"));

      JLabel lblFilterBy = new TmmLabel(BUNDLE.getString("movieextendedsearch.filterby")); //$NON-NLS-1$
      setComponentFont(lblFilterBy);
      panelContent.add(lblFilterBy, "cell 0 0,growx,aligny top");

      panelFilter = new JPanel();
      GridBagLayout gbl_panelFilter = new GridBagLayout();
      gbl_panelFilter.columnWidths = new int[] { 0 };
      gbl_panelFilter.rowHeights = new int[] { 0 };
      gbl_panelFilter.columnWeights = new double[] { Double.MIN_VALUE };
      gbl_panelFilter.rowWeights = new double[] { Double.MIN_VALUE };
      panelFilter.setLayout(gbl_panelFilter);

      JScrollPane scrollPane = new JScrollPane(panelFilter);
      scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
      panelContent.add(scrollPane, "cell 0 1 3 1,grow");

      addFilter(new MovieSetNewMoviesFilter());
      addFilter(new MovieSetWithMoreThanOneMovieFilter());
      addFilter(new MovieSetDatasourceFilter());
    }
  }

  /**
   * add a new filter to the panel and selection model
   *
   * @param filter
   *          the filter to be added
   */
  private void addFilter(IMovieSetUIFilter<TmmTreeNode> filter) {
    GridBagConstraints gbc = new GridBagConstraints();

    gbc.gridx = 0;
    gbc.gridy = GridBagConstraints.RELATIVE;
    gbc.weighty = 1;
    gbc.ipadx = 20;
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

    // small spacer for the scroll pane
    gbc.gridx = 3;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.anchor = GridBagConstraints.LINE_START;
    panelFilter.add(Box.createGlue(), gbc);

    treeTable.addFilter(filter);
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
