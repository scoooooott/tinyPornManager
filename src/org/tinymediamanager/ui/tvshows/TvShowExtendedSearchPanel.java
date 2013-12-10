/*
 * Copyright 2012 - 2013 Manuel Laggner
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
package org.tinymediamanager.ui.tvshows;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.apache.commons.lang3.StringUtils;
import org.tinymediamanager.core.Settings;
import org.tinymediamanager.core.tvshow.TvShowSettings;
import org.tinymediamanager.ui.SmallCheckBoxUI;
import org.tinymediamanager.ui.SmallTextFieldBorder;
import org.tinymediamanager.ui.UTF8Control;
import org.tinymediamanager.ui.components.RoundedPanel;
import org.tinymediamanager.ui.components.SmallComboBox;
import org.tinymediamanager.ui.tvshows.TvShowExtendedMatcher.SearchOptions;

import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;
import com.jtattoo.plaf.AbstractLookAndFeel;

/**
 * @author Manuel Laggner
 * 
 */
public class TvShowExtendedSearchPanel extends RoundedPanel {
  private static final long            serialVersionUID = 5003714573168481816L;
  private static final ResourceBundle  BUNDLE           = ResourceBundle.getBundle("messages", new UTF8Control());              //$NON-NLS-1$
  private static final float           FONT_SIZE        = 11f;
  private static final SmallCheckBoxUI CHECKBOX_UI      = AbstractLookAndFeel.getTheme() != null ? new SmallCheckBoxUI() : null; // hint for WBPro

  private TvShowTreeModel              tvShowTreeModel;
  private JTree                        tree;

  /** UI components */
  private JCheckBox                    cbFilterDatasource;
  private JComboBox                    cbDatasource;
  private JCheckBox                    cbFilterCast;
  private JTextField                   tfCastMember;
  private JCheckBox                    cbFilterMissingMetadata;
  private JCheckBox                    cbFilterMissingArtwork;

  private final Action                 actionFilter     = new FilterAction();

  public TvShowExtendedSearchPanel(TvShowTreeModel model, JTree tree) {
    super();
    setOpaque(false);
    shadowAlpha = 100;
    arcs = new Dimension(10, 10);

    this.tvShowTreeModel = model;
    this.tree = tree;

    setLayout(new FormLayout(new ColumnSpec[] { FormFactory.RELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC, FormFactory.RELATED_GAP_COLSPEC,
        ColumnSpec.decode("default:grow"), FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("default:grow"), FormFactory.UNRELATED_GAP_COLSPEC, },
        new RowSpec[] { FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.NARROW_LINE_GAP_ROWSPEC,
            FormFactory.DEFAULT_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
            FormFactory.DEFAULT_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
            FormFactory.DEFAULT_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.NARROW_LINE_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
            FormFactory.NARROW_LINE_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.NARROW_LINE_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
            FormFactory.UNRELATED_GAP_ROWSPEC, }));

    JLabel lblFilterBy = new JLabel(BUNDLE.getString("movieextendedsearch.filterby")); //$NON-NLS-1$
    setComponentFont(lblFilterBy);
    add(lblFilterBy, "2, 2, 3, 1");

    cbFilterDatasource = new JCheckBox("");
    cbFilterDatasource.setAction(actionFilter);
    cbFilterDatasource.setUI(CHECKBOX_UI); // $hide$
    add(cbFilterDatasource, "2, 4");

    JLabel lblDatasource = new JLabel(BUNDLE.getString("metatag.datasource")); //$NON-NLS-1$
    setComponentFont(lblDatasource);
    add(lblDatasource, "4, 4, right, default");

    cbDatasource = new SmallComboBox();
    setComponentFont(cbDatasource);
    cbDatasource.setAction(actionFilter);
    add(cbDatasource, "6, 4, fill, default");

    cbFilterCast = new JCheckBox("");
    cbFilterCast.setAction(actionFilter);
    add(cbFilterCast, "2, 5");
    cbFilterCast.setUI(CHECKBOX_UI); // $hide$

    JLabel lblCastMember = new JLabel(BUNDLE.getString("movieextendedsearch.cast")); //$NON-NLS-1$
    setComponentFont(lblCastMember);
    add(lblCastMember, "4, 5, right, default");

    tfCastMember = new JTextField();
    setComponentFont(tfCastMember);
    tfCastMember.setBorder(new SmallTextFieldBorder());
    add(tfCastMember, "6, 5, fill, default");
    tfCastMember.setColumns(10);
    tfCastMember.getDocument().addDocumentListener(new DocumentListener() {
      @Override
      public void changedUpdate(DocumentEvent e) {
        actionFilter.actionPerformed(null);
      }

      @Override
      public void insertUpdate(DocumentEvent e) {
        actionFilter.actionPerformed(null);
      }

      @Override
      public void removeUpdate(DocumentEvent e) {
        actionFilter.actionPerformed(null);
      }
    });

    cbFilterMissingMetadata = new JCheckBox("");
    cbFilterMissingMetadata.setAction(actionFilter);
    cbFilterMissingMetadata.setUI(CHECKBOX_UI); // $hide$
    add(cbFilterMissingMetadata, "2, 6");

    JLabel lblMissingMetadata = new JLabel(BUNDLE.getString("movieextendedsearch.missingmetadata")); //$NON-NLS-1$
    setComponentFont(lblMissingMetadata);
    add(lblMissingMetadata, "4, 6, right, default");

    cbFilterMissingArtwork = new JCheckBox("");
    cbFilterMissingArtwork.setAction(actionFilter);
    cbFilterMissingArtwork.setUI(CHECKBOX_UI); // $hide$
    add(cbFilterMissingArtwork, "2, 7");

    JLabel lblMissingArtwork = new JLabel(BUNDLE.getString("movieextendedsearch.missingartwork")); //$NON-NLS-1$
    setComponentFont(lblMissingArtwork);
    add(lblMissingArtwork, "4, 7, right, default");

    PropertyChangeListener propertyChangeListener = new PropertyChangeListener() {
      @Override
      public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getSource() instanceof TvShowSettings && "tvShowDataSource".equals(evt.getPropertyName())) {
          buildAndInstallDatasourceArray();
        }
      }
    };
    Settings.getInstance().getTvShowSettings().addPropertyChangeListener(propertyChangeListener);

    buildAndInstallDatasourceArray();
  }

  private void buildAndInstallDatasourceArray() {
    cbDatasource.removeAllItems();
    List<String> datasources = new ArrayList<String>(Settings.getInstance().getTvShowSettings().getTvShowDataSource());
    Collections.sort(datasources);
    for (String datasource : datasources) {
      cbDatasource.addItem(datasource);
    }
  }

  private class FilterAction extends AbstractAction {
    private static final long serialVersionUID = 2680577442970097443L;

    /*
     * (non-Javadoc)
     * 
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    @Override
    public void actionPerformed(ActionEvent e) {
      // filter by datasource
      if (cbFilterDatasource.isSelected()) {
        String datasource = (String) cbDatasource.getSelectedItem();
        if (StringUtils.isNotBlank(datasource)) {
          tvShowTreeModel.setFilter(SearchOptions.DATASOURCE, datasource);
        }
      }
      else {
        tvShowTreeModel.removeFilter(SearchOptions.DATASOURCE);
      }

      // filter by cast
      if (cbFilterCast.isSelected() && StringUtils.isNotBlank(tfCastMember.getText())) {
        tvShowTreeModel.setFilter(SearchOptions.CAST, tfCastMember.getText());
      }
      else {
        tvShowTreeModel.removeFilter(SearchOptions.CAST);
      }

      // filter by missing metadata
      if (cbFilterMissingMetadata.isSelected()) {
        tvShowTreeModel.setFilter(SearchOptions.MISSING_METADATA, Boolean.TRUE);
      }
      else {
        tvShowTreeModel.removeFilter(SearchOptions.MISSING_METADATA);
      }

      // filter by missing artwork
      if (cbFilterMissingArtwork.isSelected()) {
        tvShowTreeModel.setFilter(SearchOptions.MISSING_ARTWORK, Boolean.TRUE);
      }
      else {
        tvShowTreeModel.removeFilter(SearchOptions.MISSING_ARTWORK);
      }

      // apply the filter
      tvShowTreeModel.filter(tree);
    }
  }

  private void setComponentFont(JComponent comp) {
    comp.setFont(comp.getFont().deriveFont(FONT_SIZE));
  }
}
