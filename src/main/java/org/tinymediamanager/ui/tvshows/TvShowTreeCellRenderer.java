/*
 * Copyright 2012 - 2015 Manuel Laggner
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

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.util.ResourceBundle;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeCellRenderer;

import org.apache.commons.lang3.StringUtils;
import org.tinymediamanager.core.tvshow.entities.TvShow;
import org.tinymediamanager.core.tvshow.entities.TvShowEpisode;
import org.tinymediamanager.core.tvshow.entities.TvShowSeason;
import org.tinymediamanager.ui.IconManager;
import org.tinymediamanager.ui.TmmFontHelper;
import org.tinymediamanager.ui.TmmUIHelper;
import org.tinymediamanager.ui.UTF8Control;
import org.tinymediamanager.ui.components.TmmTree.BottomBorderBorder;
import org.tinymediamanager.ui.components.TmmTree.VerticalBorderPanel;
import org.tinymediamanager.ui.tvshows.TvShowTreeDataProvider.TvShowEpisodeTreeNode;
import org.tinymediamanager.ui.tvshows.TvShowTreeDataProvider.TvShowSeasonTreeNode;
import org.tinymediamanager.ui.tvshows.TvShowTreeDataProvider.TvShowTreeNode;

import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

/**
 * The Class TvShowTreeCellRenderer.
 * 
 * @author Manuel Laggner
 */
public class TvShowTreeCellRenderer implements TreeCellRenderer {
  private static final ResourceBundle BUNDLE                     = ResourceBundle.getBundle("messages", new UTF8Control());   //$NON-NLS-1$

  private JPanel                      tvShowPanel                = new VerticalBorderPanel(new int[] { 0, 1, 2, 3 });
  private JLabel                      tvShowTitle                = new JLabel();
  private JLabel                      tvShowSeasons              = new JLabel();
  private JLabel                      tvShowEpisodes             = new JLabel();
  private JLabel                      tvShowNfoLabel             = new JLabel();
  private JLabel                      tvShowImageLabel           = new JLabel();

  private JPanel                      tvShowSeasonPanel          = new VerticalBorderPanel(new int[] { 0, 1, 2, 3, 4, 5, 6 });
  private JLabel                      tvShowSeasonTitle          = new JLabel();
  private JLabel                      tvShowSeasonEpisodes       = new JLabel();

  private JPanel                      tvShowEpisodePanel         = new VerticalBorderPanel(new int[] { 0, 1 });
  private JLabel                      tvShowEpisodeTitle         = new JLabel();
  private JLabel                      tvShowEpisodeNfoLabel      = new JLabel();
  private JLabel                      tvShowEpisodeImageLabel    = new JLabel();
  private JLabel                      tvShowEpisodeSubtitleLabel = new JLabel();

  private DefaultTreeCellRenderer     defaultRenderer            = new DefaultTreeCellRenderer();
  private final Color                 defaultColor               = defaultRenderer.getTextSelectionColor();

  /**
   * Instantiates a new tv show tree cell renderer.
   */
  public TvShowTreeCellRenderer() {
    int nfoColumnWidth = TmmUIHelper.getColumnWidthForIcon(IconManager.NFO);
    int imageColumnWidth = TmmUIHelper.getColumnWidthForIcon(IconManager.IMAGES);
    int subtitleColumnWidth = TmmUIHelper.getColumnWidthForIcon(IconManager.SUBTITLES);

    tvShowPanel.setLayout(new FormLayout(new ColumnSpec[] { ColumnSpec.decode("min:grow"), FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
        ColumnSpec.decode("center:30px"), ColumnSpec.decode("center:30px"), ColumnSpec.decode("center:" + nfoColumnWidth + "px"),
        ColumnSpec.decode("center:" + imageColumnWidth + "px"), ColumnSpec.decode("center:" + subtitleColumnWidth + "px"), ColumnSpec.decode("1px") },
        new RowSpec[] { FormFactory.DEFAULT_ROWSPEC }));

    tvShowPanel.setBorder(new BottomBorderBorder());
    TmmFontHelper.changeFont(tvShowTitle, Font.BOLD);
    tvShowTitle.setHorizontalAlignment(JLabel.LEFT);
    tvShowTitle.setMinimumSize(new Dimension(0, 0));
    tvShowTitle.setBorder(new EmptyBorder(5, 0, 5, 0));
    tvShowTitle.setForeground(defaultColor);
    tvShowTitle.setHorizontalTextPosition(SwingConstants.LEADING);
    tvShowPanel.add(tvShowTitle, "1, 1");

    tvShowPanel.add(tvShowSeasons, "3, 1");
    TmmFontHelper.changeFont(tvShowSeasons, 0.916);
    tvShowSeasons.setForeground(defaultColor);
    tvShowPanel.add(tvShowEpisodes, "4, 1");
    TmmFontHelper.changeFont(tvShowEpisodes, 0.916);
    tvShowEpisodes.setForeground(defaultColor);
    tvShowPanel.add(tvShowNfoLabel, "5, 1");
    tvShowPanel.add(tvShowImageLabel, "6, 1");

    tvShowSeasonPanel.setLayout(new FormLayout(new ColumnSpec[] { ColumnSpec.decode("min:grow"), FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
        ColumnSpec.decode("center:30px"), ColumnSpec.decode("center:30px"), ColumnSpec.decode("center:" + nfoColumnWidth + "px"),
        ColumnSpec.decode("center:" + imageColumnWidth + "px"), ColumnSpec.decode("center:" + subtitleColumnWidth + "px"), ColumnSpec.decode("1px") },
        new RowSpec[] { FormFactory.DEFAULT_ROWSPEC }));
    tvShowSeasonPanel.add(tvShowSeasonTitle, "1, 1");
    tvShowSeasonTitle.setBorder(new EmptyBorder(5, 0, 5, 0));
    tvShowSeasonTitle.setHorizontalTextPosition(SwingConstants.LEADING);
    tvShowSeasonTitle.setForeground(defaultColor);
    tvShowSeasonPanel.setBorder(new BottomBorderBorder());
    tvShowSeasonPanel.add(tvShowSeasonEpisodes, "4 , 1");
    TmmFontHelper.changeFont(tvShowSeasonEpisodes, 0.916);

    tvShowEpisodePanel
        .setLayout(new FormLayout(
            new ColumnSpec[] { ColumnSpec.decode("min:grow"), FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
                ColumnSpec.decode("center:" + nfoColumnWidth + "px"), ColumnSpec.decode("center:" + imageColumnWidth + "px"),
                ColumnSpec.decode("center:" + subtitleColumnWidth + "px"), ColumnSpec.decode("1px") },
            new RowSpec[] { FormFactory.DEFAULT_ROWSPEC, }));
    tvShowEpisodeTitle.setMinimumSize(new Dimension(0, 0));
    tvShowEpisodeTitle.setBorder(new EmptyBorder(5, 0, 5, 0));
    tvShowEpisodeTitle.setHorizontalTextPosition(SwingConstants.LEADING);
    tvShowEpisodeTitle.setForeground(defaultColor);
    tvShowEpisodePanel.add(tvShowEpisodeTitle, "1, 1");
    tvShowEpisodePanel.add(tvShowEpisodeNfoLabel, "3, 1");
    tvShowEpisodePanel.add(tvShowEpisodeImageLabel, "4, 1");
    tvShowEpisodePanel.add(tvShowEpisodeSubtitleLabel, "5, 1");
    tvShowEpisodePanel.setBorder(new BottomBorderBorder());
  }

  @Override
  public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row,
      boolean hasFocus) {
    Component returnValue = null;
    // paint tv show node
    if (value != null && value instanceof TvShowTreeNode) {
      Object userObject = ((TvShowTreeNode) value).getUserObject();
      if (userObject instanceof TvShow) {
        TvShow tvShow = (TvShow) userObject;

        if (StringUtils.isBlank(tvShow.getYear()) || "0".equals(tvShow.getYear())) {
          tvShowTitle.setText(tvShow.getTitleSortable());
        }
        else {
          tvShowTitle.setText(tvShow.getTitleSortable() + " (" + tvShow.getYear() + ")");
        }
        if (StringUtils.isBlank(tvShowTitle.getText())) {
          tvShowTitle.setText(BUNDLE.getString("tmm.unknowntitle")); //$NON-NLS-1$
        }

        if (tvShow.isNewlyAdded() || tvShow.hasNewlyAddedEpisodes()) {
          tvShowTitle.setIcon(IconManager.NEW);
        }
        else {
          tvShowTitle.setIcon(null);
        }

        tvShowSeasons.setText("" + tvShow.getSeasons().size());
        tvShowEpisodes.setText("" + tvShow.getEpisodes().size());
        tvShowNfoLabel.setIcon(tvShow.getHasNfoFile() ? IconManager.DOT_AVAILABLE : IconManager.DOT_UNAVAILABLE);
        tvShowImageLabel.setIcon(tvShow.getHasImages() ? IconManager.DOT_AVAILABLE : IconManager.DOT_UNAVAILABLE);

        tvShowPanel.setEnabled(tree.isEnabled());
        tvShowPanel.invalidate();
        returnValue = tvShowPanel;
      }
    }

    // paint tv show season node
    if (value != null && value instanceof TvShowSeasonTreeNode) {
      Object userObject = ((TvShowSeasonTreeNode) value).getUserObject();
      if (userObject instanceof TvShowSeason) {
        TvShowSeason season = (TvShowSeason) userObject;
        tvShowSeasonTitle.setText(BUNDLE.getString("metatag.season") + " " + season.getSeason());//$NON-NLS-1$
        tvShowSeasonPanel.setEnabled(tree.isEnabled());

        if (season.isNewlyAdded()) {
          tvShowSeasonTitle.setIcon(IconManager.NEW);
        }
        else {
          tvShowSeasonTitle.setIcon(null);
        }

        tvShowSeasonEpisodes.setText("" + season.getEpisodes().size());

        tvShowSeasonPanel.invalidate();
        returnValue = tvShowSeasonPanel;
      }
    }

    // paint tv show episode node
    if (value != null && value instanceof TvShowEpisodeTreeNode) {
      Object userObject = ((TvShowEpisodeTreeNode) value).getUserObject();
      if (userObject instanceof TvShowEpisode) {
        TvShowEpisode episode = (TvShowEpisode) userObject;
        if (episode.getEpisode() > 0) {
          tvShowEpisodeTitle.setText(episode.getEpisode() + ". " + episode.getTitle());
        }
        else {
          tvShowEpisodeTitle.setText(episode.getTitle());
        }
        if (StringUtils.isBlank(tvShowTitle.getText())) {
          tvShowEpisodeTitle.setText(BUNDLE.getString("tmm.unknowntitle")); //$NON-NLS-1$
        }

        if (episode.isNewlyAdded()) {
          tvShowEpisodeTitle.setIcon(IconManager.NEW);
        }
        else {
          tvShowEpisodeTitle.setIcon(null);
        }

        tvShowEpisodePanel.setEnabled(tree.isEnabled());

        tvShowEpisodeNfoLabel.setIcon(episode.getHasNfoFile() ? IconManager.DOT_AVAILABLE : IconManager.DOT_UNAVAILABLE);
        tvShowEpisodeImageLabel.setIcon(episode.getHasImages() ? IconManager.DOT_AVAILABLE : IconManager.DOT_UNAVAILABLE);
        tvShowEpisodeSubtitleLabel.setIcon(episode.hasSubtitles() ? IconManager.DOT_AVAILABLE : IconManager.DOT_UNAVAILABLE);

        tvShowEpisodePanel.invalidate();
        returnValue = tvShowEpisodePanel;
      }
    }

    if (returnValue == null) {
      returnValue = defaultRenderer.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
    }

    // paint background
    if (selected) {
      returnValue.setBackground(defaultRenderer.getBackgroundSelectionColor());
    }
    else {
      returnValue.setBackground(defaultRenderer.getBackgroundNonSelectionColor());
    }

    return returnValue;
  }
}
