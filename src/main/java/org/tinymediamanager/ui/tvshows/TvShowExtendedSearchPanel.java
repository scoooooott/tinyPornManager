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
package org.tinymediamanager.ui.tvshows;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
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
import org.japura.gui.CheckComboBox;
import org.japura.gui.event.ListCheckListener;
import org.japura.gui.event.ListEvent;
import org.tinymediamanager.Globals;
import org.tinymediamanager.core.MediaSource;
import org.tinymediamanager.core.entities.MediaFile;
import org.tinymediamanager.core.tvshow.TvShowList;
import org.tinymediamanager.core.tvshow.TvShowModuleManager;
import org.tinymediamanager.core.tvshow.TvShowSettings;
import org.tinymediamanager.scraper.entities.MediaGenres;
import org.tinymediamanager.ui.SmallCheckBoxUI;
import org.tinymediamanager.ui.SmallTextFieldBorder;
import org.tinymediamanager.ui.UTF8Control;
import org.tinymediamanager.ui.components.SmallCheckComboBox;
import org.tinymediamanager.ui.components.SmallComboBox;
import org.tinymediamanager.ui.movies.MovieExtendedComparator.WatchedFlag;
import org.tinymediamanager.ui.panels.RoundedPanel;
import org.tinymediamanager.ui.tvshows.TvShowExtendedMatcher.SearchOptions;

import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.FormSpecs;
import com.jgoodies.forms.layout.RowSpec;
import com.jtattoo.plaf.AbstractLookAndFeel;

/**
 * @author Manuel Laggner
 * 
 */
public class TvShowExtendedSearchPanel extends RoundedPanel {
  private static final long            serialVersionUID = 5003714573168481816L;
  /**
   * @wbp.nls.resourceBundle messages
   */
  private static final ResourceBundle  BUNDLE           = ResourceBundle.getBundle("messages", new UTF8Control());               //$NON-NLS-1$
  private static final float           FONT_SIZE        = Math.round(Globals.settings.getFontSize() * 0.916);
  private static final SmallCheckBoxUI CHECKBOX_UI      = AbstractLookAndFeel.getTheme() != null ? new SmallCheckBoxUI() : null; // hint for WBPro

  private TvShowTreeModel              tvShowTreeModel;
  private JTree                        tree;
  private TvShowList                   tvShowList       = TvShowList.getInstance();

  /** UI components */
  private JCheckBox                    cbFilterDatasource;
  private CheckComboBox                cbDatasource;
  private JCheckBox                    cbFilterCast;
  private JTextField                   tfCastMember;
  private JCheckBox                    cbFilterMissingMetadata;
  private JCheckBox                    cbFilterMissingArtwork;
  private JCheckBox                    cbFilterMissingSubtitles;
  private JCheckBox                    cbFilterNewEpisodes;
  private JCheckBox                    cbFilterWatched;
  private JComboBox                    cbWatched;
  private JCheckBox                    cbFilterGenres;
  private JComboBox                    cbGenres;
  private JCheckBox                    cbFilterTag;
  private CheckComboBox                cbTag;
  private JCheckBox                    cbFilterVideoCodec;
  private JComboBox                    cbVideoCodec;
  private JCheckBox                    cbFilterAudioCodec;
  private JComboBox                    cbAudioCodec;
  private JCheckBox                    cbFilterVideoFormat;
  private JComboBox                    cbVideoFormat;

  private final Action                 actionFilter     = new FilterAction();
  private final ListCheckListener      listCheckListener;
  private JCheckBox                    cbFilterMediaSource;
  private JLabel                       lblMediaSource;
  private JComboBox                    cbMediaSource;

  public TvShowExtendedSearchPanel(TvShowTreeModel model, JTree tree) {
    super();
    setOpaque(false);
    shadowAlpha = 100;
    arcs = new Dimension(10, 10);

    this.tvShowTreeModel = model;
    this.tree = tree;

    // add a dummy mouse listener to prevent clicking through
    addMouseListener(new MouseAdapter() {
    });

    listCheckListener = new ListCheckListener() {
      @Override
      public void removeCheck(ListEvent event) {
        actionFilter.actionPerformed(new ActionEvent(event.getSource(), 1, "checked"));
      }

      @Override
      public void addCheck(ListEvent event) {
        actionFilter.actionPerformed(new ActionEvent(event.getSource(), 1, "checked"));
      }
    };

    setLayout(new FormLayout(
        new ColumnSpec[] { FormSpecs.RELATED_GAP_COLSPEC, FormSpecs.DEFAULT_COLSPEC, FormSpecs.RELATED_GAP_COLSPEC, ColumnSpec.decode("default:grow"),
            FormSpecs.RELATED_GAP_COLSPEC, ColumnSpec.decode("default:grow"), FormSpecs.UNRELATED_GAP_COLSPEC, },
        new RowSpec[] { FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC, FormSpecs.LABEL_COMPONENT_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC,
            FormSpecs.DEFAULT_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC,
            FormSpecs.DEFAULT_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC,
            FormSpecs.DEFAULT_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC, FormSpecs.UNRELATED_GAP_ROWSPEC, }));

    JLabel lblFilterBy = new JLabel(BUNDLE.getString("movieextendedsearch.filterby")); //$NON-NLS-1$
    setComponentFont(lblFilterBy);
    add(lblFilterBy, "2, 2, 3, 1");

    cbFilterNewEpisodes = new JCheckBox("");
    cbFilterNewEpisodes.setAction(actionFilter);
    add(cbFilterNewEpisodes, "2, 4");

    JLabel lblNewEpisodes = new JLabel(BUNDLE.getString("movieextendedsearch.newepisodes")); //$NON-NLS-1$
    setComponentFont(lblNewEpisodes);
    add(lblNewEpisodes, "4, 4, right, default");

    cbFilterWatched = new JCheckBox("");
    cbFilterWatched.setAction(actionFilter);
    cbFilterWatched.setUI(CHECKBOX_UI); // $hide$
    add(cbFilterWatched, "2, 5");

    JLabel lblWatched = new JLabel(BUNDLE.getString("metatag.watched")); //$NON-NLS-1$
    setComponentFont(lblWatched);
    add(lblWatched, "4, 5, right, default");

    cbWatched = new SmallComboBox(WatchedFlag.values());
    setComponentFont(cbWatched);
    cbWatched.setAction(actionFilter);
    add(cbWatched, "6, 5, fill, default");

    cbFilterGenres = new JCheckBox("");
    cbFilterGenres.setAction(actionFilter);
    cbFilterGenres.setUI(CHECKBOX_UI); // $hide$
    add(cbFilterGenres, "2, 6");

    JLabel lblGenres = new JLabel(BUNDLE.getString("metatag.genre")); //$NON-NLS-1$
    setComponentFont(lblGenres);
    add(lblGenres, "4, 6, right, default");

    cbGenres = new SmallComboBox(MediaGenres.values());
    setComponentFont(cbGenres);
    cbGenres.setAction(actionFilter);
    add(cbGenres, "6, 6, fill, default");

    cbFilterCast = new JCheckBox("");
    cbFilterCast.setAction(actionFilter);
    cbFilterCast.setUI(CHECKBOX_UI); // $hide$
    add(cbFilterCast, "2, 7");

    JLabel lblCastMember = new JLabel(BUNDLE.getString("movieextendedsearch.cast")); //$NON-NLS-1$
    setComponentFont(lblCastMember);
    add(lblCastMember, "4, 7, right, default");

    tfCastMember = new JTextField();
    setComponentFont(tfCastMember);
    tfCastMember.setBorder(new SmallTextFieldBorder());
    add(tfCastMember, "6, 7, fill, default");
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
    add(cbFilterTag, "2, 8");

    JLabel lblTag = new JLabel(BUNDLE.getString("movieextendedsearch.tag")); //$NON-NLS-1$
    setComponentFont(lblTag);
    add(lblTag, "4, 8, right, default");

    cbTag = new SmallCheckComboBox();
    cbTag.setTextFor(CheckComboBox.NONE, BUNDLE.getString("movieextendedsearch.tags.selected.none")); //$NON-NLS-1$
    cbTag.setTextFor(CheckComboBox.MULTIPLE, BUNDLE.getString("movieextendedsearch.tags.selected.multiple")); //$NON-NLS-1$
    cbTag.setTextFor(CheckComboBox.ALL, BUNDLE.getString("movieextendedsearch.tags.selected.all")); //$NON-NLS-1$
    cbTag.getModel().addListCheckListener(listCheckListener);
    add(cbTag, "6, 8, fill, default");

    cbFilterVideoFormat = new JCheckBox("");
    cbFilterVideoFormat.setUI(CHECKBOX_UI); // $hide$
    cbFilterVideoFormat.setAction(actionFilter);
    add(cbFilterVideoFormat, "2, 9");

    JLabel lblVideoFormat = new JLabel(BUNDLE.getString("metatag.resolution")); //$NON-NLS-1$
    setComponentFont(lblVideoFormat);
    add(lblVideoFormat, "4, 9, right, default");

    cbVideoFormat = new SmallComboBox(getVideoFormats());
    setComponentFont(cbVideoFormat);
    cbVideoFormat.setAction(actionFilter);
    add(cbVideoFormat, "6, 9, fill, default");

    cbFilterVideoCodec = new JCheckBox("");
    cbFilterVideoCodec.setAction(actionFilter);
    cbFilterVideoCodec.setUI(CHECKBOX_UI); // $hide$
    add(cbFilterVideoCodec, "2, 10");

    JLabel lblVideoCodec = new JLabel(BUNDLE.getString("metatag.videocodec")); //$NON-NLS-1$
    setComponentFont(lblVideoCodec);
    add(lblVideoCodec, "4, 10, right, default");

    cbVideoCodec = new SmallComboBox();
    setComponentFont(cbVideoCodec);
    cbVideoCodec.setAction(actionFilter);
    add(cbVideoCodec, "6, 10, fill, default");

    cbFilterAudioCodec = new JCheckBox("");
    cbFilterAudioCodec.setAction(actionFilter);
    cbFilterAudioCodec.setUI(CHECKBOX_UI); // $hide$
    add(cbFilterAudioCodec, "2, 11");

    JLabel lblAudioCodec = new JLabel(BUNDLE.getString("metatag.audiocodec")); //$NON-NLS-1$
    setComponentFont(lblAudioCodec);
    add(lblAudioCodec, "4, 11, right, default");

    cbAudioCodec = new SmallComboBox();
    setComponentFont(cbAudioCodec);
    cbAudioCodec.setAction(actionFilter);
    add(cbAudioCodec, "6, 11, fill, default");

    cbFilterDatasource = new JCheckBox("");
    cbFilterDatasource.setAction(actionFilter);
    cbFilterDatasource.setUI(CHECKBOX_UI); // $hide$
    add(cbFilterDatasource, "2, 12");

    JLabel lblDatasource = new JLabel(BUNDLE.getString("metatag.datasource")); //$NON-NLS-1$
    setComponentFont(lblDatasource);
    add(lblDatasource, "4, 12, right, default");

    cbDatasource = new SmallCheckComboBox();
    cbDatasource.setTextFor(CheckComboBox.NONE, BUNDLE.getString("checkcombobox.selected.none")); //$NON-NLS-1$
    cbDatasource.setTextFor(CheckComboBox.MULTIPLE, BUNDLE.getString("checkcombobox.selected.multiple")); //$NON-NLS-1$
    cbDatasource.setTextFor(CheckComboBox.ALL, BUNDLE.getString("checkcombobox.selected.all")); //$NON-NLS-1$
    cbDatasource.getModel().addListCheckListener(listCheckListener);
    add(cbDatasource, "6, 12, fill, default");

    cbFilterMediaSource = new JCheckBox("");
    cbFilterMediaSource.setAction(actionFilter);
    cbFilterMediaSource.setUI(CHECKBOX_UI); // $hide$
    add(cbFilterMediaSource, "2, 13");

    lblMediaSource = new JLabel(BUNDLE.getString("metatag.source")); //$NON-NLS-1$
    setComponentFont(lblMediaSource);
    add(lblMediaSource, "4, 13, right, default");

    cbMediaSource = new SmallComboBox(MediaSource.values());
    setComponentFont(cbMediaSource);
    cbMediaSource.setAction(actionFilter);
    add(cbMediaSource, "6, 13, fill, default");

    cbFilterMissingMetadata = new JCheckBox("");
    cbFilterMissingMetadata.setAction(actionFilter);
    cbFilterMissingMetadata.setUI(CHECKBOX_UI); // $hide$
    add(cbFilterMissingMetadata, "2, 14");

    JLabel lblMissingMetadata = new JLabel(BUNDLE.getString("movieextendedsearch.missingmetadata")); //$NON-NLS-1$
    setComponentFont(lblMissingMetadata);
    add(lblMissingMetadata, "4, 14, right, default");

    cbFilterMissingArtwork = new JCheckBox("");
    cbFilterMissingArtwork.setAction(actionFilter);
    cbFilterMissingArtwork.setUI(CHECKBOX_UI); // $hide$
    add(cbFilterMissingArtwork, "2, 15");

    JLabel lblMissingArtwork = new JLabel(BUNDLE.getString("movieextendedsearch.missingartwork")); //$NON-NLS-1$
    setComponentFont(lblMissingArtwork);
    add(lblMissingArtwork, "4, 15, right, default");

    cbFilterMissingSubtitles = new JCheckBox("");
    cbFilterMissingSubtitles.setAction(actionFilter);
    cbFilterMissingSubtitles.setUI(CHECKBOX_UI); // $hide$
    add(cbFilterMissingSubtitles, "2, 16");

    JLabel lblMissingSubtitles = new JLabel(BUNDLE.getString("movieextendedsearch.missingsubtitles")); //$NON-NLS-1$
    setComponentFont(lblMissingSubtitles);
    add(lblMissingSubtitles, "4, 16, right, default");
    cbFilterNewEpisodes.setUI(CHECKBOX_UI); // $hide$

    PropertyChangeListener propertyChangeListener = new PropertyChangeListener() {
      @Override
      public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getSource() instanceof TvShowSettings && "tvShowDataSource".equals(evt.getPropertyName())) {
          buildAndInstallDatasourceArray();
        }
        if (evt.getSource() instanceof TvShowList && "tag".equals(evt.getPropertyName())) {
          buildAndInstallTagsArray();
        }
        if (evt.getSource() instanceof TvShowList && ("audioCodec".equals(evt.getPropertyName()) || "videoCodec".equals(evt.getPropertyName()))) {
          buildAndInstallCodecArray();
        }
      }
    };
    tvShowList.addPropertyChangeListener(propertyChangeListener);
    TvShowModuleManager.SETTINGS.addPropertyChangeListener(propertyChangeListener);

    buildAndInstallDatasourceArray();
    buildAndInstallTagsArray();
    buildAndInstallCodecArray();
  }

  private void buildAndInstallDatasourceArray() {
    // remember old value and remove listener
    List<Object> oldValues = cbDatasource.getModel().getCheckeds();
    cbDatasource.getModel().removeListCheckListener(listCheckListener);

    // build up the new checkbox
    cbDatasource.getModel().clear();
    List<String> datasources = new ArrayList<>(TvShowModuleManager.SETTINGS.getTvShowDataSource());

    for (String datasource : datasources) {
      cbDatasource.getModel().addElement(datasource);
    }

    // re-set the value and readd action listener
    if (oldValues != null) {
      for (Object obj : oldValues) {
        cbDatasource.getModel().setCheck(obj);
      }
    }
    cbDatasource.getModel().addListCheckListener(listCheckListener);
  }

  private void buildAndInstallTagsArray() {
    // remember old value and remove listener
    List<Object> oldValues = cbTag.getModel().getCheckeds();
    cbTag.getModel().removeListCheckListener(listCheckListener);

    // build up the new checkbox
    cbTag.getModel().clear();
    Set<String> tags = new TreeSet<>(tvShowList.getTagsInTvShows());
    tags.addAll(tvShowList.getTagsInEpisodes());

    for (String tag : tags) {
      cbTag.getModel().addElement(tag);
    }

    // re-set the value and readd action listener
    if (oldValues != null) {
      for (Object obj : oldValues) {
        cbTag.getModel().setCheck(obj);
      }
    }
    cbTag.getModel().addListCheckListener(listCheckListener);
  }

  private void buildAndInstallCodecArray() {
    cbVideoCodec.removeAllItems();
    List<String> codecs = new ArrayList<>(tvShowList.getVideoCodecsInEpisodes());
    Collections.sort(codecs);
    for (String codec : codecs) {
      cbVideoCodec.addItem(codec);
    }

    cbAudioCodec.removeAllItems();
    codecs = new ArrayList<>(tvShowList.getAudioCodecsInEpisodes());
    Collections.sort(codecs);
    for (String codec : codecs) {
      cbAudioCodec.addItem(codec);
    }
  }

  private class FilterAction extends AbstractAction {
    private static final long serialVersionUID = 2680577442970097443L;

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
        List<Object> tags = cbTag.getModel().getCheckeds();
        tvShowTreeModel.setFilter(SearchOptions.TAG, tags);
      }
      else {
        tvShowTreeModel.removeFilter(SearchOptions.TAG);
      }

      // filter by datasource
      if (cbFilterDatasource.isSelected()) {
        List<Object> datasources = cbDatasource.getModel().getCheckeds();
        tvShowTreeModel.setFilter(SearchOptions.DATASOURCE, datasources);
      }
      else {
        tvShowTreeModel.removeFilter(SearchOptions.DATASOURCE);
      }

      // filter by media source
      if (cbFilterMediaSource.isSelected()) {
        MediaSource mediaSource = (MediaSource) cbMediaSource.getSelectedItem();
        tvShowTreeModel.setFilter(SearchOptions.MEDIA_SOURCE, mediaSource);
      }
      else {
        tvShowTreeModel.removeFilter(SearchOptions.MEDIA_SOURCE);

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

      // filter by video codec
      if (cbFilterVideoCodec.isSelected()) {
        String videoCodec = (String) cbVideoCodec.getSelectedItem();
        if (StringUtils.isNotBlank(videoCodec)) {
          tvShowTreeModel.setFilter(SearchOptions.VIDEO_CODEC, videoCodec);
        }
      }
      else {
        tvShowTreeModel.removeFilter(SearchOptions.VIDEO_CODEC);
      }

      // filter by audio codec
      if (cbFilterAudioCodec.isSelected()) {
        String audioCodec = (String) cbAudioCodec.getSelectedItem();
        if (StringUtils.isNotBlank(audioCodec)) {
          tvShowTreeModel.setFilter(SearchOptions.AUDIO_CODEC, audioCodec);
        }
      }
      else {
        tvShowTreeModel.removeFilter(SearchOptions.AUDIO_CODEC);
      }

      // filer by video format
      if (cbFilterVideoFormat.isSelected()) {
        String videoFormat = (String) cbVideoFormat.getSelectedItem();
        if (StringUtils.isNotBlank(videoFormat)) {
          tvShowTreeModel.setFilter(SearchOptions.VIDEO_FORMAT, videoFormat);
        }
      }
      else {
        tvShowTreeModel.removeFilter(SearchOptions.VIDEO_FORMAT);
      }

      // apply the filter
      tvShowTreeModel.filter(tree);
    }
  }

  private void setComponentFont(JComponent comp) {
    comp.setFont(comp.getFont().deriveFont(FONT_SIZE));
  }

  private String[] getVideoFormats() {
    return new String[] { MediaFile.VIDEO_FORMAT_480P, MediaFile.VIDEO_FORMAT_540P, MediaFile.VIDEO_FORMAT_576P, MediaFile.VIDEO_FORMAT_720P,
        MediaFile.VIDEO_FORMAT_1080P, MediaFile.VIDEO_FORMAT_4K, MediaFile.VIDEO_FORMAT_SD, MediaFile.VIDEO_FORMAT_HD }; // MediaFile.VIDEO_FORMAT_8K,
  }
}
