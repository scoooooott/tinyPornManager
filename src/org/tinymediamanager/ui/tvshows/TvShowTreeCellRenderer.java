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

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.util.ResourceBundle;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeCellRenderer;

import org.apache.commons.lang3.StringUtils;
import org.tinymediamanager.core.tvshow.TvShow;
import org.tinymediamanager.core.tvshow.TvShowEpisode;
import org.tinymediamanager.core.tvshow.TvShowSeason;
import org.tinymediamanager.ui.ImageIconConverter;
import org.tinymediamanager.ui.UTF8Control;

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

  private static final ResourceBundle BUNDLE                     = ResourceBundle.getBundle("messages", new UTF8Control()); //$NON-NLS-1$
  private static final Color          EVEN_ROW_COLOR             = new Color(241, 245, 250);

  private JPanel                      tvShowPanel                = new JPanel();
  private JLabel                      tvShowTitle                = new JLabel();
  private JLabel                      tvShowInfo                 = new JLabel();
  private JLabel                      tvShowNfoLabel             = new JLabel();
  private JLabel                      tvShowImageLabel           = new JLabel();

  private JPanel                      tvShowSeasonPanel          = new JPanel();
  private JLabel                      tvShowSeasonTitle          = new JLabel();

  private JPanel                      tvShowEpisodePanel         = new JPanel();
  private JLabel                      tvShowEpisodeTitle         = new JLabel();
  private JLabel                      tvShowEpisodeNfoLabel      = new JLabel();
  private JLabel                      tvShowEpisodeImageLabel    = new JLabel();
  private JLabel                      tvShowEpisodeSubtitleLabel = new JLabel();

  private DefaultTreeCellRenderer     defaultRenderer            = new DefaultTreeCellRenderer();

  /**
   * Instantiates a new tv show tree cell renderer.
   */
  public TvShowTreeCellRenderer() {
    // tvShowPanel.setLayout(new BoxLayout(tvShowPanel, BoxLayout.Y_AXIS));
    tvShowPanel.setLayout(new FormLayout(new ColumnSpec[] { ColumnSpec.decode("min:grow"), FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
        ColumnSpec.decode("center:20px"), ColumnSpec.decode("center:20px"), ColumnSpec.decode("center:20px") }, new RowSpec[] {
        FormFactory.DEFAULT_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, }));

    tvShowTitle.setFont(new Font("Dialog", Font.BOLD, 12));
    tvShowTitle.setHorizontalAlignment(JLabel.LEFT);
    tvShowTitle.setMinimumSize(new Dimension(0, 0));
    tvShowPanel.add(tvShowTitle, "1, 1");

    tvShowPanel.add(tvShowNfoLabel, "3, 1, 1, 2");
    tvShowPanel.add(tvShowImageLabel, "4, 1, 1, 2");

    tvShowInfo.setFont(new Font("Dialog", Font.PLAIN, 10));
    tvShowInfo.setHorizontalAlignment(JLabel.LEFT);
    tvShowInfo.setMinimumSize(new Dimension(0, 0));
    tvShowPanel.add(tvShowInfo, "1, 2");

    tvShowSeasonPanel.setLayout(new BoxLayout(tvShowSeasonPanel, BoxLayout.Y_AXIS));
    tvShowSeasonPanel.add(tvShowSeasonTitle);

    tvShowEpisodePanel.setLayout(new FormLayout(new ColumnSpec[] { ColumnSpec.decode("min:grow"), FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
        ColumnSpec.decode("center:20px"), ColumnSpec.decode("center:20px"), ColumnSpec.decode("center:20px") },
        new RowSpec[] { FormFactory.DEFAULT_ROWSPEC }));
    tvShowEpisodeTitle.setMinimumSize(new Dimension(0, 0));
    tvShowEpisodePanel.add(tvShowEpisodeTitle, "1, 1");
    tvShowEpisodePanel.add(tvShowEpisodeNfoLabel, "3, 1");
    tvShowEpisodePanel.add(tvShowEpisodeImageLabel, "4, 1");
    tvShowEpisodePanel.add(tvShowEpisodeSubtitleLabel, "5, 1");
  }

  /*
   * (non-Javadoc)
   * 
   * @see javax.swing.tree.TreeCellRenderer#getTreeCellRendererComponent(javax.swing.JTree, java.lang.Object, boolean, boolean, boolean, int, boolean)
   */
  @Override
  public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
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
        tvShowInfo.setText(tvShow.getSeasons().size()
            + " " + BUNDLE.getString("metatag.seasons") + " - " + tvShow.getEpisodes().size() + " " + BUNDLE.getString("metatag.episodes")); //$NON-NLS-1$
        tvShowNfoLabel.setIcon(tvShow.getHasNfoFile() ? ImageIconConverter.checkIcon : ImageIconConverter.crossIcon);
        tvShowImageLabel.setIcon(tvShow.getHasImages() ? ImageIconConverter.checkIcon : ImageIconConverter.crossIcon);

        tvShowPanel.setEnabled(tree.isEnabled());
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
        tvShowEpisodePanel.setEnabled(tree.isEnabled());

        tvShowEpisodeNfoLabel.setIcon(episode.getHasNfoFile() ? ImageIconConverter.checkIcon : ImageIconConverter.crossIcon);
        tvShowEpisodeImageLabel.setIcon(episode.getHasImages() ? ImageIconConverter.checkIcon : ImageIconConverter.crossIcon);
        tvShowEpisodeSubtitleLabel.setIcon(episode.hasSubtitles() ? ImageIconConverter.checkIcon : ImageIconConverter.crossIcon);
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
      returnValue.setBackground(row % 2 == 0 ? EVEN_ROW_COLOR : Color.WHITE);
      // rendererPanel.setBackground(defaultRenderer.getBackgroundNonSelectionColor());
    }

    return returnValue;
  }
}
