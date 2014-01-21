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
import java.util.Set;
import java.util.TreeSet;

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
import org.tinymediamanager.core.tvshow.TvShowList;
import org.tinymediamanager.core.tvshow.TvShowSettings;
import org.tinymediamanager.scraper.MediaGenres;
import org.tinymediamanager.ui.SmallCheckBoxUI;
import org.tinymediamanager.ui.SmallTextFieldBorder;
import org.tinymediamanager.ui.UTF8Control;
import org.tinymediamanager.ui.components.RoundedPanel;
import org.tinymediamanager.ui.components.SmallComboBox;
import org.tinymediamanager.ui.movies.MovieExtendedComparator.WatchedFlag;
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
  private TvShowList                   tvShowList       = TvShowList.getInstance();

  /** UI components */
  private JCheckBox                    cbFilterDatasource;
  private JComboBox                    cbDatasource;
  private JCheckBox                    cbFilterCast;
  private JTextField                   tfCastMember;
  private JCheckBox                    cbFilterMissingMetadata;
  private JCheckBox                    cbFilterMissingArtwork;
  private JCheckBox                    cbFilterMissingSubtitles;

  private final Action                 actionFilter     = new FilterAction();
  private JCheckBox                    cbFilterNewEpisodes;
  private JLabel                       lblNewEpisodes;
  private JCheckBox                    cbFilterWatched;
  private JLabel                       lblWatched;
  private JComboBox                    cbWatched;
  private JCheckBox                    cbFilterGenres;
  private JLabel                       lblGenres;
  private JComboBox                    cbGenres;
  private JCheckBox                    cbFilterTag;
  private JLabel                       lblTag;
  private JComboBox                    cbTag;

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
            FormFactory.DEFAULT_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.UNRELATED_GAP_ROWSPEC, }));

    JLabel lblFilterBy = new JLabel(BUNDLE.getString("movieextendedsearch.filterby")); //$NON-NLS-1$
    setComponentFont(lblFilterBy);
    add(lblFilterBy, "2, 2, 3, 1");

    cbFilterWatched = new JCheckBox("");
    cbFilterWatched.setAction(actionFilter);
    cbFilterWatched.setUI(CHECKBOX_UI); // $hide$
    add(cbFilterWatched, "2, 4");

    lblWatched = new JLabel(BUNDLE.getString("metatag.watched")); //$NON-NLS-1$
    setComponentFont(lblWatched);
    add(lblWatched, "4, 4, right, default");

    cbWatched = new SmallComboBox(WatchedFlag.values());
    setComponentFont(cbWatched);
    cbWatched.setAction(actionFilter);
    add(cbWatched, "6, 4, fill, default");

    cbFilterGenres = new JCheckBox("");
    cbFilterGenres.setAction(actionFilter);
    cbFilterGenres.setUI(CHECKBOX_UI); // $hide$
    add(cbFilterGenres, "2, 5");

    lblGenres = new JLabel(BUNDLE.getString("metatag.genre")); //$NON-NLS-1$
    setComponentFont(lblGenres);
    add(lblGenres, "4, 5, right, default");

    cbGenres = new SmallComboBox(MediaGenres.values());
    setComponentFont(cbGenres);
    cbGenres.setAction(actionFilter);
    add(cbGenres, "6, 5, fill, default");

    cbFilterCast = new JCheckBox("");
    cbFilterCast.setAction(actionFilter);
    cbFilterCast.setUI(CHECKBOX_UI); // $hide$
    add(cbFilterCast, "2, 6");

    JLabel lblCastMember = new JLabel(BUNDLE.getString("movieextendedsearch.cast")); //$NON-NLS-1$
    setComponentFont(lblCastMember);
    add(lblCastMember, "4, 6, right, default");

    tfCastMember = new JTextField();
    setComponentFont(tfCastMember);
    tfCastMember.setBorder(new SmallTextFieldBorder());
    add(tfCastMember, "6, 6, fill, default");
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

    cbFilterTag = new JCheckBox("");
    cbFilterTag.setAction(actionFilter);
    cbFilterTag.setUI(CHECKBOX_UI); // $hide$
    add(cbFilterTag, "2, 7");

    lblTag = new JLabel(BUNDLE.getString("movieextendedsearch.tag")); //$NON-NLS-1$
    setComponentFont(lblTag);
    add(lblTag, "4, 7, right, default");

    cbTag = new SmallComboBox();
    setComponentFont(cbTag);
    cbTag.setAction(actionFilter);
    add(cbTag, "6, 7, fill, default");

    cbFilterDatasource = new JCheckBox("");
    cbFilterDatasource.setAction(actionFilter);
    cbFilterDatasource.setUI(CHECKBOX_UI); // $hide$
    add(cbFilterDatasource, "2, 8");

    JLabel lblDatasource = new JLabel(BUNDLE.getString("metatag.datasource")); //$NON-NLS-1$
    setComponentFont(lblDatasource);
    add(lblDatasource, "4, 8, right, default");

    cbDatasource = new SmallComboBox();
    setComponentFont(cbDatasource);
    cbDatasource.setAction(actionFilter);
    add(cbDatasource, "6, 8, fill, default");

    cbFilterMissingMetadata = new JCheckBox("");
    cbFilterMissingMetadata.setAction(actionFilter);
    cbFilterMissingMetadata.setUI(CHECKBOX_UI); // $hide$
    add(cbFilterMissingMetadata, "2, 9");

    JLabel lblMissingMetadata = new JLabel(BUNDLE.getString("movieextendedsearch.missingmetadata")); //$NON-NLS-1$
    setComponentFont(lblMissingMetadata);
    add(lblMissingMetadata, "4, 9, right, default");

    cbFilterMissingArtwork = new JCheckBox("");
    cbFilterMissingArtwork.setAction(actionFilter);
    cbFilterMissingArtwork.setUI(CHECKBOX_UI); // $hide$
    add(cbFilterMissingArtwork, "2, 10");

    JLabel lblMissingArtwork = new JLabel(BUNDLE.getString("movieextendedsearch.missingartwork")); //$NON-NLS-1$
    setComponentFont(lblMissingArtwork);
    add(lblMissingArtwork, "4, 10, right, default");

    cbFilterMissingSubtitles = new JCheckBox("");
    cbFilterMissingSubtitles.setAction(actionFilter);
    cbFilterMissingSubtitles.setUI(CHECKBOX_UI); // $hide$
    add(cbFilterMissingSubtitles, "2, 11");

    JLabel lblMissingSubtitles = new JLabel(BUNDLE.getString("movieextendedsearch.missingsubtitles")); //$NON-NLS-1$
    setComponentFont(lblMissingSubtitles);
    add(lblMissingSubtitles, "4, 11, right, default");

    cbFilterNewEpisodes = new JCheckBox("");
    cbFilterNewEpisodes.setAction(actionFilter);
    cbFilterNewEpisodes.setUI(CHECKBOX_UI); // $hide$
    add(cbFilterNewEpisodes, "2, 12");

    lblNewEpisodes = new JLabel(BUNDLE.getString("movieextendedsearch.newepisodes")); //$NON-NLS-1$
    setComponentFont(lblNewEpisodes);
    add(lblNewEpisodes, "4, 12, right, default");

    PropertyChangeListener propertyChangeListener = new PropertyChangeListener() {
      @Override
      public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getSource() instanceof TvShowSettings && "tvShowDataSource".equals(evt.getPropertyName())) {
          buildAndInstallDatasourceArray();
        }
        if (evt.getSource() instanceof TvShowList && "tag".equals(evt.getPropertyName())) {
          buildAndInstallTagsArray();
        }
      }
    };
    tvShowList.addPropertyChangeListener(propertyChangeListener);
    Settings.getInstance().getTvShowSettings().addPropertyChangeListener(propertyChangeListener);

    buildAndInstallDatasourceArray();
    buildAndInstallTagsArray();
  }

  private void buildAndInstallDatasourceArray() {
    cbDatasource.removeAllItems();
    List<String> datasources = new ArrayList<String>(Settings.getInstance().getTvShowSettings().getTvShowDataSource());
    Collections.sort(datasources);
    for (String datasource : datasources) {
      cbDatasource.addItem(datasource);
    }
  }

  private void buildAndInstallTagsArray() {
    cbTag.removeAllItems();
    Set<String> tags = new TreeSet<String>(tvShowList.getTagsInTvShows());
    tags.addAll(tvShowList.getTagsInEpisodes());
    for (String tag : tags) {
      cbTag.addItem(tag);
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
      // filter by watched flag
      if (cbFilterWatched.isSelected()) {
        if (cbWatched.getSelectedItem() == WatchedFlag.WATCHED) {
          tvShowTreeModel.setFilter(SearchOptions.WATCHED, true);
        }
        else {
          tvShowTreeModel.setFilter(SearchOptions.WATCHED, false);
        }
      }
      else {
        tvShowTreeModel.removeFilter(SearchOptions.WATCHED);
      }

      // filter by genre
      if (cbFilterGenres.isSelected()) {
        MediaGenres genre = (MediaGenres) cbGenres.getSelectedItem();
        tvShowTreeModel.setFilter(SearchOptions.GENRE, genre);
      }
      else {
        tvShowTreeModel.removeFilter(SearchOptions.GENRE);
      }

      // filter by tag
      if (cbFilterTag.isSelected()) {
        String tag = (String) cbTag.getSelectedItem();
        tvShowTreeModel.setFilter(SearchOptions.TAG, tag);
      }
      else {
        tvShowTreeModel.removeFilter(SearchOptions.TAG);
      }

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

      // filter by missing subtitles
      if (cbFilterMissingSubtitles.isSelected()) {
        tvShowTreeModel.setFilter(SearchOptions.MISSING_SUBTITLES, Boolean.TRUE);
      }
      else {
        tvShowTreeModel.removeFilter(SearchOptions.MISSING_SUBTITLES);
      }

      // filter by new episodes
      if (cbFilterNewEpisodes.isSelected()) {
        tvShowTreeModel.setFilter(SearchOptions.NEW_EPISODES, Boolean.TRUE);
      }
      else {
        tvShowTreeModel.removeFilter(SearchOptions.NEW_EPISODES);
      }

      // apply the filter
      tvShowTreeModel.filter(tree);
    }
  }

  private void setComponentFont(JComponent comp) {
    comp.setFont(comp.getFont().deriveFont(FONT_SIZE));
  }
}
