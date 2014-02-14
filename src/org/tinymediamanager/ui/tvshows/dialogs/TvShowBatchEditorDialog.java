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
package org.tinymediamanager.ui.tvshows.dialogs;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.border.TitledBorder;

import org.tinymediamanager.Globals;
import org.tinymediamanager.core.tvshow.TvShow;
import org.tinymediamanager.core.tvshow.TvShowEpisode;
import org.tinymediamanager.core.tvshow.TvShowList;
import org.tinymediamanager.scraper.MediaGenres;
import org.tinymediamanager.ui.IconManager;
import org.tinymediamanager.ui.TmmWindowSaver;
import org.tinymediamanager.ui.UTF8Control;
import org.tinymediamanager.ui.components.AutocompleteComboBox;

import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

/**
 * The Class TvShowBatchEditorDialog.
 * 
 * @author Manuel Laggner
 */
public class TvShowBatchEditorDialog extends JDialog {

  /** The Constant serialVersionUID. */
  private static final long           serialVersionUID = 3527478264068979388L;

  /** The Constant BUNDLE. */
  private static final ResourceBundle BUNDLE           = ResourceBundle.getBundle("messages", new UTF8Control()); //$NON-NLS-1$

  /** The tv show list. */
  private TvShowList                  tvShowList       = TvShowList.getInstance();

  /** The tv shows to edit. */
  private List<TvShow>                tvShowsToEdit;

  /** The tv show episodes to edit. */
  private List<TvShowEpisode>         tvShowEpisodesToEdit;

  /** The episodes changed. */
  private boolean                     episodesChanged  = false;

  /** The tv shows changed. */
  private boolean                     tvShowsChanged   = false;

  /** The cb genres. */
  private JComboBox                   cbGenres;

  /** The cb tags. */
  private JComboBox                   cbTags;

  /** The chckbx watched. */
  private JCheckBox                   chckbxWatched;

  /** The sp season. */
  private JSpinner                    spSeason;

  /**
   * Instantiates a new movie batch editor.
   * 
   * @param tvShows
   *          the tv shows
   * @param episodes
   *          the episodes
   */
  public TvShowBatchEditorDialog(final List<TvShow> tvShows, final List<TvShowEpisode> episodes) {
    setModal(true);
    setIconImage(Globals.logo);
    setTitle(BUNDLE.getString("tvshow.bulkedit")); //$NON-NLS-1$
    setName("movieBatchEditor");
    setBounds(5, 5, 350, 286);
    TmmWindowSaver.loadSettings(this);
    getContentPane().setLayout(new BorderLayout(0, 0));

    tvShowsToEdit = tvShows;
    tvShowEpisodesToEdit = episodes;

    JPanel panelTvShows = new JPanel();
    panelTvShows.setBorder(new TitledBorder(null, BUNDLE.getString("metatag.tvshow"), TitledBorder.LEADING, TitledBorder.TOP, null, null)); //$NON-NLS-1$
    getContentPane().add(panelTvShows, BorderLayout.NORTH);
    panelTvShows.setLayout(new FormLayout(new ColumnSpec[] { FormFactory.RELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC,
        FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("default:grow"), FormFactory.RELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC,
        FormFactory.RELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC, FormFactory.RELATED_GAP_COLSPEC, }, new RowSpec[] {
        FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.NARROW_LINE_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
        FormFactory.RELATED_GAP_ROWSPEC, }));

    JLabel lblGenres = new JLabel(BUNDLE.getString("metatag.genre"));//$NON-NLS-1$
    panelTvShows.add(lblGenres, "2, 2, right, default");

    cbGenres = new AutocompleteComboBox(MediaGenres.values());
    panelTvShows.add(cbGenres, "4, 2");
    cbGenres.setEditable(true);

    JButton btnAddGenre = new JButton("");
    panelTvShows.add(btnAddGenre, "6, 2");
    btnAddGenre.setIcon(IconManager.LIST_ADD);
    btnAddGenre.setMargin(new Insets(2, 2, 2, 2));

    JButton btnRemoveGenre = new JButton("");
    panelTvShows.add(btnRemoveGenre, "8, 2");
    btnRemoveGenre.setIcon(IconManager.LIST_REMOVE);
    btnRemoveGenre.setMargin(new Insets(2, 2, 2, 2));

    JLabel lblTags = new JLabel("Tag");
    panelTvShows.add(lblTags, "2, 4, right, default");

    cbTags = new AutocompleteComboBox(tvShowList.getTagsInTvShows().toArray());
    panelTvShows.add(cbTags, "4, 4");
    cbTags.setEditable(true);

    JButton btnAddTag = new JButton("");
    panelTvShows.add(btnAddTag, "6, 4");
    btnAddTag.setIcon(IconManager.LIST_ADD);
    btnAddTag.setMargin(new Insets(2, 2, 2, 2));

    JButton btnRemoveTag = new JButton("");
    panelTvShows.add(btnRemoveTag, "8, 4");
    btnRemoveTag.setIcon(IconManager.LIST_REMOVE);
    btnRemoveTag.setMargin(new Insets(2, 2, 2, 2));
    btnRemoveTag.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        tvShowsChanged = true;
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        String tag = (String) cbTags.getSelectedItem();
        for (TvShow tvShow : tvShowsToEdit) {
          tvShow.removeFromTags(tag);
        }
        setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
      }
    });
    btnAddTag.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        tvShowsChanged = true;
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        String tag = (String) cbTags.getSelectedItem();
        for (TvShow tvShow : tvShowsToEdit) {
          tvShow.addToTags(tag);
        }
        setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
      }
    });
    btnRemoveGenre.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        tvShowsChanged = true;
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        MediaGenres genre = (MediaGenres) cbGenres.getSelectedItem();
        for (TvShow tvShow : tvShowsToEdit) {
          tvShow.removeGenre(genre);
        }
        setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
      }
    });
    btnAddGenre.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
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
      }
    });

    {
      JPanel panelTvShowEpisodes = new JPanel();
      panelTvShowEpisodes.setBorder(new TitledBorder(null, BUNDLE.getString("metatag.episode"), TitledBorder.LEADING, TitledBorder.TOP, null, null));//$NON-NLS-1$
      getContentPane().add(panelTvShowEpisodes, BorderLayout.CENTER);
      panelTvShowEpisodes.setLayout(new FormLayout(new ColumnSpec[] { FormFactory.RELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC,
          FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("default:grow"), FormFactory.RELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC,
          FormFactory.RELATED_GAP_COLSPEC, }, new RowSpec[] { FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
          FormFactory.NARROW_LINE_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC, }));

      JLabel lblWatched = new JLabel(BUNDLE.getString("metatag.watched")); //$NON-NLS-1$
      panelTvShowEpisodes.add(lblWatched, "2, 2, right, default");

      chckbxWatched = new JCheckBox("");
      panelTvShowEpisodes.add(chckbxWatched, "4, 2");

      JButton btnWatched = new JButton("");
      btnWatched.setMargin(new Insets(2, 2, 2, 2));
      btnWatched.setIcon(IconManager.APPLY);
      btnWatched.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
          episodesChanged = true;
          setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
          for (TvShowEpisode episode : tvShowEpisodesToEdit) {
            episode.setWatched(chckbxWatched.isSelected());
          }
          setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        }
      });
      panelTvShowEpisodes.add(btnWatched, "6, 2");

      JLabel lblSeason = new JLabel(BUNDLE.getString("metatag.season"));//$NON-NLS-1$
      panelTvShowEpisodes.add(lblSeason, "2, 4, right, default");

      spSeason = new JSpinner();
      spSeason.setPreferredSize(new Dimension(40, 20));
      spSeason.setMinimumSize(new Dimension(40, 20));
      panelTvShowEpisodes.add(spSeason, "4, 4, left, default");

      JButton btnSeason = new JButton("");
      btnSeason.setIcon(IconManager.APPLY);
      btnSeason.setMargin(new Insets(2, 2, 2, 2));
      btnSeason.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent arg0) {
          episodesChanged = true;
          setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
          for (TvShowEpisode episode : tvShowEpisodesToEdit) {
            Integer season = (Integer) spSeason.getValue();
            episode.setSeason(season);
          }
          setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        }
      });
      panelTvShowEpisodes.add(btnSeason, "6, 4");
    }

    {
      JPanel panelButtons = new JPanel();
      FlowLayout flowLayout = (FlowLayout) panelButtons.getLayout();
      flowLayout.setAlignment(FlowLayout.RIGHT);
      getContentPane().add(panelButtons, BorderLayout.SOUTH);

      JButton btnClose = new JButton(BUNDLE.getString("Button.close")); //$NON-NLS-1$
      btnClose.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent arg0) {
          // rewrite tv show if anything changed
          if (tvShowsChanged) {
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            for (TvShow tvShow : tvShowsToEdit) {
              tvShow.saveToDb();
              tvShow.writeNFO();
            }
            setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
          }

          // rewrite episodes if anything changed
          if (episodesChanged) {
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            for (TvShowEpisode episode : tvShowEpisodesToEdit) {
              episode.saveToDb();
              episode.writeNFO();
            }
            setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
          }

          setVisible(false);
          dispose();
        }
      });
      panelButtons.add(btnClose);

      // add window listener to write changes (if the window close button "X" is pressed)
      addWindowListener(new WindowAdapter() {
        @Override
        public void windowClosing(WindowEvent e) {
          // rewrite tv show if anything changed
          if (tvShowsChanged) {
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            for (TvShow tvShow : tvShowsToEdit) {
              tvShow.saveToDb();
              tvShow.writeNFO();
            }
            setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
          }

          // rewrite episodes if anything changed
          if (episodesChanged) {
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            for (TvShowEpisode episode : tvShowEpisodesToEdit) {
              episode.saveToDb();
              episode.writeNFO();
            }
            setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
          }
        }
      });
    }

  }
}
