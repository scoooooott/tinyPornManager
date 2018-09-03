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
package org.tinymediamanager.ui.tvshows.dialogs;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;

import org.tinymediamanager.core.MediaSource;
import org.tinymediamanager.core.threading.TmmTask;
import org.tinymediamanager.core.threading.TmmTaskManager;
import org.tinymediamanager.core.tvshow.TvShowList;
import org.tinymediamanager.core.tvshow.TvShowModuleManager;
import org.tinymediamanager.core.tvshow.entities.TvShow;
import org.tinymediamanager.core.tvshow.entities.TvShowEpisode;
import org.tinymediamanager.scraper.entities.MediaGenres;
import org.tinymediamanager.scraper.trakttv.SyncTraktTvTask;
import org.tinymediamanager.ui.IconManager;
import org.tinymediamanager.ui.components.TmmLabel;
import org.tinymediamanager.ui.components.combobox.AutocompleteComboBox;
import org.tinymediamanager.ui.dialogs.TmmDialog;

import net.miginfocom.swing.MigLayout;

/**
 * The Class TvShowBulkEditorDialog.
 * 
 * @author Manuel Laggner
 */
public class TvShowBulkEditorDialog extends TmmDialog {
  private static final long      serialVersionUID = 3527478264068979388L;

  private TvShowList             tvShowList       = TvShowList.getInstance();
  private List<TvShow>           tvShowsToEdit;
  private List<TvShowEpisode>    tvShowEpisodesToEdit;
  private boolean                episodesChanged  = false;
  private boolean                tvShowsChanged   = false;

  /** UI components */
  private JComboBox<MediaGenres> cbGenres;
  private JComboBox<String>      cbTags;
  private JComboBox<String>      cbTagsEpisode;
  private JCheckBox              chckbxWatched;
  private JSpinner               spSeason;

  /**
   * Instantiates a new movie batch editor.
   * 
   * @param tvShows
   *          the tv shows
   * @param episodes
   *          the episodes
   */
  public TvShowBulkEditorDialog(final List<TvShow> tvShows, final List<TvShowEpisode> episodes) {
    super(BUNDLE.getString("tvshow.bulkedit"), "movieBatchEditor"); //$NON-NLS-1$

    tvShowsToEdit = tvShows;
    tvShowEpisodesToEdit = episodes;

    initComponents();

  }

  private void initComponents() {
    JPanel panelContent = new JPanel();
    getContentPane().add(panelContent, BorderLayout.CENTER);
    panelContent.setLayout(new MigLayout("", "[20lp:n][][100lp,grow][]", "[][][][20lp:n][][][][][][]"));

    JLabel lblTvShowT = new TmmLabel(BUNDLE.getString("metatag.tvshow")); //$NON-NLS-1$
    panelContent.add(lblTvShowT, "cell 0 0 2 1");

    JLabel lblGenres = new TmmLabel(BUNDLE.getString("metatag.genre")); //$NON-NLS-1$
    panelContent.add(lblGenres, "cell 1 1,alignx right");

    cbGenres = new AutocompleteComboBox(MediaGenres.values());
    panelContent.add(cbGenres, "cell 2 1, growx, wmin 0");
    cbGenres.setEditable(true);
    JButton btnAddGenre = new JButton("");
    panelContent.add(btnAddGenre, "flowx,cell 3 1");
    btnAddGenre.setIcon(IconManager.ADD_INV);
    btnAddGenre.setMargin(new Insets(2, 2, 2, 2));
    btnAddGenre.addActionListener(e -> {
      tvShowsChanged = true;
      setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
      MediaGenres genre = null;
      Object item = cbGenres.getSelectedItem();

      // genre
      if (item instanceof MediaGenres) {
        genre = (MediaGenres) item;
      }

      // newly created genre?
      if (item instanceof String) {
        genre = MediaGenres.getGenre((String) item);
      }

      if (genre != null) {
        for (TvShow tvShow : tvShowsToEdit) {
          tvShow.addGenre(genre);
        }
      }
      setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    });

    JLabel lblTags = new TmmLabel("Tag");
    panelContent.add(lblTags, "cell 1 2,alignx right");

    cbTags = new AutocompleteComboBox<>(tvShowList.getTagsInTvShows());
    panelContent.add(cbTags, "cell 2 2, growx, wmin 0");
    cbTags.setEditable(true);

    JButton btnAddTag = new JButton("");
    panelContent.add(btnAddTag, "flowx,cell 3 2");
    btnAddTag.setIcon(IconManager.ADD_INV);
    btnAddTag.setMargin(new Insets(2, 2, 2, 2));
    btnAddTag.addActionListener(e -> {
      tvShowsChanged = true;
      setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
      String tag = (String) cbTags.getSelectedItem();
      for (TvShow tvShow : tvShowsToEdit) {
        tvShow.addToTags(tag);
      }
      setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    });

    JLabel lblEpisodeT = new TmmLabel(BUNDLE.getString("metatag.episode")); //$NON-NLS-1$
    panelContent.add(lblEpisodeT, "cell 0 4 2 1");

    JLabel lblWatched = new TmmLabel(BUNDLE.getString("metatag.watched")); //$NON-NLS-1$
    panelContent.add(lblWatched, "cell 1 5,alignx right");

    chckbxWatched = new JCheckBox("");
    panelContent.add(chckbxWatched, "cell 2 5");

    JButton btnWatched = new JButton("");
    panelContent.add(btnWatched, "cell 3 5");
    btnWatched.setMargin(new Insets(2, 2, 2, 2));
    btnWatched.setIcon(IconManager.APPLY_INV);
    btnWatched.addActionListener(e -> {
      episodesChanged = true;
      setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
      for (TvShowEpisode episode : tvShowEpisodesToEdit) {
        episode.setWatched(chckbxWatched.isSelected());
      }
      setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    });

    JLabel lblSeason = new TmmLabel(BUNDLE.getString("metatag.season")); //$NON-NLS-1$
    panelContent.add(lblSeason, "cell 1 6,alignx right");

    spSeason = new JSpinner();
    panelContent.add(spSeason, "cell 2 6");
    spSeason.setPreferredSize(new Dimension(40, 20));
    spSeason.setMinimumSize(new Dimension(40, 20));

    JButton btnSeason = new JButton("");
    panelContent.add(btnSeason, "cell 3 6");
    btnSeason.setIcon(IconManager.APPLY_INV);
    btnSeason.setMargin(new Insets(2, 2, 2, 2));
    btnSeason.addActionListener(arg0 -> {
      episodesChanged = true;
      setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
      for (TvShowEpisode episode : tvShowEpisodesToEdit) {
        Integer season = (Integer) spSeason.getValue();
        episode.setSeason(season);
      }
      setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    });

    JLabel lblDvdOrder = new TmmLabel(BUNDLE.getString("metatag.dvdorder")); //$NON-NLS-1$
    panelContent.add(lblDvdOrder, "cell 1 7,alignx right");

    final JCheckBox cbDvdOrder = new JCheckBox("");
    panelContent.add(cbDvdOrder, "cell 2 7");

    JButton btnDvdOrder = new JButton("");
    panelContent.add(btnDvdOrder, "cell 3 7");
    btnDvdOrder.setIcon(IconManager.APPLY_INV);
    btnDvdOrder.setMargin(new Insets(2, 2, 2, 2));
    btnDvdOrder.addActionListener(arg0 -> {
      episodesChanged = true;
      setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
      for (TvShowEpisode episode : tvShowEpisodesToEdit) {
        episode.setDvdOrder(cbDvdOrder.isSelected());
      }
      setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    });

    JLabel lblTagsEpisode = new TmmLabel("Tag");
    panelContent.add(lblTagsEpisode, "cell 1 8,alignx right");

    cbTagsEpisode = new AutocompleteComboBox(tvShowList.getTagsInEpisodes().toArray());
    panelContent.add(cbTagsEpisode, "cell 2 8,growx, wmin 0");
    cbTagsEpisode.setEditable(true);

    JButton btnAddTagEpisode = new JButton("");
    panelContent.add(btnAddTagEpisode, "flowx,cell 3 8");
    btnAddTagEpisode.setIcon(IconManager.ADD_INV);
    btnAddTagEpisode.setMargin(new Insets(2, 2, 2, 2));
    btnAddTagEpisode.addActionListener(e -> {
      episodesChanged = true;
      setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
      String tag = (String) cbTagsEpisode.getSelectedItem();
      for (TvShowEpisode episode : tvShowEpisodesToEdit) {
        episode.addToTags(tag);
      }
      setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    });

    JLabel lblMediasourceEpisode = new TmmLabel(BUNDLE.getString("metatag.source")); //$NON-NLS-1$
    panelContent.add(lblMediasourceEpisode, "cell 1 9,alignx right");

    final JComboBox<MediaSource> cbMediaSourceEpisode = new JComboBox(MediaSource.values());
    panelContent.add(cbMediaSourceEpisode, "cell 2 9,growx, wmin 0");

    JButton btnMediaSourceEpisode = new JButton("");
    panelContent.add(btnMediaSourceEpisode, "cell 3 9");
    btnMediaSourceEpisode.setMargin(new Insets(2, 2, 2, 2));
    btnMediaSourceEpisode.setIcon(IconManager.APPLY_INV);
    btnMediaSourceEpisode.addActionListener(e -> {
      episodesChanged = true;
      Object obj = cbMediaSourceEpisode.getSelectedItem();
      if (obj instanceof MediaSource) {
        MediaSource mediaSource = (MediaSource) obj;
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        for (TvShowEpisode episode : tvShowEpisodesToEdit) {
          episode.setMediaSource(mediaSource);
        }
        setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
      }
    });

    JButton btnRemoveGenre = new JButton("");
    panelContent.add(btnRemoveGenre, "cell 3 1");
    btnRemoveGenre.setIcon(IconManager.REMOVE_INV);
    btnRemoveGenre.setMargin(new Insets(2, 2, 2, 2));

    JButton btnRemoveTag = new JButton("");
    panelContent.add(btnRemoveTag, "cell 3 2");
    btnRemoveTag.setIcon(IconManager.REMOVE_INV);
    btnRemoveTag.setMargin(new Insets(2, 2, 2, 2));

    JButton btnRemoveTagEpisode = new JButton("");
    panelContent.add(btnRemoveTagEpisode, "cell 3 8");
    btnRemoveTagEpisode.setIcon(IconManager.REMOVE_INV);
    btnRemoveTagEpisode.setMargin(new Insets(2, 2, 2, 2));

    btnRemoveTagEpisode.addActionListener(e -> {
      episodesChanged = true;
      setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
      String tag = (String) cbTagsEpisode.getSelectedItem();
      for (TvShowEpisode episode : tvShowEpisodesToEdit) {
        episode.removeFromTags(tag);
      }
      setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    });
    btnRemoveTag.addActionListener(e -> {
      tvShowsChanged = true;
      setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
      String tag = (String) cbTags.getSelectedItem();
      for (TvShow tvShow : tvShowsToEdit) {
        tvShow.removeFromTags(tag);
      }
      setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    });
    btnRemoveGenre.addActionListener(e -> {
      tvShowsChanged = true;
      setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
      MediaGenres genre = (MediaGenres) cbGenres.getSelectedItem();
      for (TvShow tvShow : tvShowsToEdit) {
        tvShow.removeGenre(genre);
      }
      setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    });

    {
      JButton btnClose = new JButton(BUNDLE.getString("Button.close")); //$NON-NLS-1$
      btnClose.setIcon(IconManager.APPLY_INV);
      btnClose.addActionListener(arg0 -> {
        // rewrite tv show if anything changed
        if (tvShowsChanged) {
          setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
          for (TvShow tvShow : tvShowsToEdit) {
            tvShow.writeNFO();
            tvShow.saveToDb();
          }
          setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        }

        // rewrite episodes if anything changed
        if (episodesChanged) {
          setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
          for (TvShowEpisode episode : tvShowEpisodesToEdit) {
            episode.writeNFO();
            episode.saveToDb();
          }
          setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        }

        if (TvShowModuleManager.SETTINGS.getSyncTrakt()) {
          Set<TvShow> tvShows1 = new HashSet<>();
          for (TvShowEpisode episode : tvShowEpisodesToEdit) {
            tvShows1.add(episode.getTvShow());
          }
          tvShows1.addAll(tvShowsToEdit);
          TmmTask task = new SyncTraktTvTask(null, new ArrayList<>(tvShows1));
          TmmTaskManager.getInstance().addUnnamedTask(task);
        }

        setVisible(false);
      });
      addDefaultButton(btnClose);

      // add window listener to write changes (if the window close button "X" is pressed)
      addWindowListener(new WindowAdapter() {
        @Override
        public void windowClosing(WindowEvent e) {
          // rewrite tv show if anything changed
          if (tvShowsChanged) {
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            for (TvShow tvShow : tvShowsToEdit) {
              tvShow.writeNFO();
              tvShow.saveToDb();
            }
            setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
          }

          // rewrite episodes if anything changed
          if (episodesChanged) {
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            for (TvShowEpisode episode : tvShowEpisodesToEdit) {
              episode.writeNFO();
              episode.saveToDb();
            }
            setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
          }
        }
      });
    }
  }
}
