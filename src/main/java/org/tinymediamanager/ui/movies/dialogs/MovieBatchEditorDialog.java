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
package org.tinymediamanager.ui.movies.dialogs;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Insets;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.apache.commons.lang3.StringUtils;
import org.tinymediamanager.core.MediaSource;
import org.tinymediamanager.core.movie.MovieList;
import org.tinymediamanager.core.movie.MovieModuleManager;
import org.tinymediamanager.core.movie.entities.Movie;
import org.tinymediamanager.core.movie.entities.MovieSet;
import org.tinymediamanager.core.threading.TmmTask;
import org.tinymediamanager.core.threading.TmmTaskManager;
import org.tinymediamanager.scraper.entities.Certification;
import org.tinymediamanager.scraper.entities.MediaGenres;
import org.tinymediamanager.scraper.trakttv.SyncTraktTvTask;
import org.tinymediamanager.ui.IconManager;
import org.tinymediamanager.ui.UTF8Control;
import org.tinymediamanager.ui.components.combobox.AutocompleteComboBox;
import org.tinymediamanager.ui.dialogs.TmmDialog;
import org.tinymediamanager.ui.moviesets.actions.MovieSetAddAction;

import net.miginfocom.swing.MigLayout;

/**
 * The Class MovieBatchEditor.
 * 
 * @author Manuel Laggner
 */
public class MovieBatchEditorDialog extends TmmDialog {
  private static final long           serialVersionUID = -8515248604267310279L;
  /** @wbp.nls.resourceBundle messages */
  private static final ResourceBundle BUNDLE           = ResourceBundle.getBundle("messages", new UTF8Control()); //$NON-NLS-1$

  private MovieList                   movieList        = MovieList.getInstance();
  private List<Movie>                 moviesToEdit;
  private boolean                     changed          = false;

  private JComboBox                   cbGenres;
  private JComboBox                   cbTags;
  private JComboBox                   cbMovieSet;
  private JCheckBox                   chckbxWatched;
  private JTextField                  tfLanguage;

  /**
   * Instantiates a new movie batch editor.
   * 
   * @param movies
   *          the movies
   */
  public MovieBatchEditorDialog(final List<Movie> movies) {
    super(BUNDLE.getString("movie.edit"), "movieBatchEditor"); //$NON-NLS-1$

    {
      JPanel panelContent = new JPanel();
      getContentPane().add(panelContent, BorderLayout.CENTER);
      panelContent.setLayout(new MigLayout("", "[][100lp][][]", "[][][][][][][][][][]"));

      JLabel lblGenres = new JLabel(BUNDLE.getString("metatag.genre")); //$NON-NLS-1$
      panelContent.add(lblGenres, "cell 0 0,alignx right");

      // cbGenres = new JComboBox(MediaGenres2.values());
      cbGenres = new AutocompleteComboBox(MediaGenres.values());
      cbGenres.setEditable(true);
      panelContent.add(cbGenres, "cell 1 0,growx");

      JButton btnAddGenre = new JButton("");
      btnAddGenre.setIcon(IconManager.ADD_INV);
      btnAddGenre.setMargin(new Insets(2, 2, 2, 2));
      btnAddGenre.addActionListener(e -> {
        changed = true;
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
        // MediaGenres2 genre = (MediaGenres2) cbGenres.getSelectedItem();
        if (genre != null) {
          for (Movie movie : moviesToEdit) {
            movie.addGenre(genre);
          }
        }
        setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
      });
      panelContent.add(btnAddGenre, "cell 2 0");

      JButton btnRemoveGenre = new JButton("");
      btnRemoveGenre.setIcon(IconManager.REMOVE_INV);
      btnRemoveGenre.setMargin(new Insets(2, 2, 2, 2));
      btnRemoveGenre.addActionListener(e -> {
        changed = true;
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        MediaGenres genre = (MediaGenres) cbGenres.getSelectedItem();
        for (Movie movie : moviesToEdit) {
          movie.removeGenre(genre);
        }
        setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
      });
      panelContent.add(btnRemoveGenre, "cell 3 0");

      JLabel lblTags = new JLabel(BUNDLE.getString("metatag.tags")); //$NON-NLS-1$
      panelContent.add(lblTags, "cell 0 1,alignx right");

      cbTags = new AutocompleteComboBox(movieList.getTagsInMovies().toArray());
      cbTags.setEditable(true);
      panelContent.add(cbTags, "cell 1 1,growx");

      JButton btnAddTag = new JButton("");
      btnAddTag.setIcon(IconManager.ADD_INV);
      btnAddTag.setMargin(new Insets(2, 2, 2, 2));
      btnAddTag.addActionListener(e -> {
        changed = true;
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        String tag = (String) cbTags.getSelectedItem();
        if (StringUtils.isBlank(tag)) {
          return;
        }

        for (Movie movie : moviesToEdit) {
          movie.addToTags(tag);
        }
        setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
      });
      panelContent.add(btnAddTag, "cell 2 1");

      JButton btnRemoveTag = new JButton("");
      btnRemoveTag.setIcon(IconManager.REMOVE_INV);
      btnRemoveTag.setMargin(new Insets(2, 2, 2, 2));
      btnRemoveTag.addActionListener(e -> {
        changed = true;
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        String tag = (String) cbTags.getSelectedItem();
        for (Movie movie : moviesToEdit) {
          movie.removeFromTags(tag);
        }
        setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
      });
      panelContent.add(btnRemoveTag, "cell 3 1");

      JLabel lblCertification = new JLabel(BUNDLE.getString("metatag.certification")); //$NON-NLS-1$
      panelContent.add(lblCertification, "cell 0 2,alignx right");

      final JComboBox cbCertification = new JComboBox();
      for (Certification cert : Certification.getCertificationsforCountry(MovieModuleManager.SETTINGS.getCertificationCountry())) {
        cbCertification.addItem(cert);
      }
      panelContent.add(cbCertification, "cell 1 2,growx");

      JButton btnCertification = new JButton("");
      btnCertification.setMargin(new Insets(2, 2, 2, 2));
      btnCertification.setIcon(IconManager.APPLY_INV);
      btnCertification.addActionListener(e -> {
        changed = true;
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        Certification cert = (Certification) cbCertification.getSelectedItem();
        for (Movie movie : moviesToEdit) {
          movie.setCertification(cert);
          ;
        }
        setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
      });
      panelContent.add(btnCertification, "cell 2 2");

      JLabel lblMovieSet = new JLabel(BUNDLE.getString("metatag.movieset")); //$NON-NLS-1$
      panelContent.add(lblMovieSet, "cell 0 3,alignx right");

      cbMovieSet = new JComboBox();
      panelContent.add(cbMovieSet, "cell 1 3,growx");

      JButton btnSetMovieSet = new JButton("");
      btnSetMovieSet.setMargin(new Insets(2, 2, 2, 2));
      btnSetMovieSet.setIcon(IconManager.APPLY_INV);
      btnSetMovieSet.addActionListener(e -> {
        changed = true;
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        // movie set
        Object obj = cbMovieSet.getSelectedItem();
        for (Movie movie : moviesToEdit) {
          if (obj instanceof String) {
            movie.removeFromMovieSet();
          }
          if (obj instanceof MovieSet) {
            MovieSet movieSet = (MovieSet) obj;

            if (movie.getMovieSet() != movieSet) {
              movie.removeFromMovieSet();
              movie.setMovieSet(movieSet);
              movieSet.insertMovie(movie);
            }
          }
        }
        setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
      });
      panelContent.add(btnSetMovieSet, "cell 2 3");

      JButton btnNewMovieset = new JButton("");
      btnNewMovieset.setMargin(new Insets(2, 2, 2, 2));
      btnNewMovieset.setAction(new MovieSetAddAction());
      panelContent.add(btnNewMovieset, "cell 3 3,growx");

      JLabel lblWatched = new JLabel(BUNDLE.getString("metatag.watched")); //$NON-NLS-1$
      panelContent.add(lblWatched, "cell 0 4,alignx right");

      chckbxWatched = new JCheckBox("");
      panelContent.add(chckbxWatched, "cell 1 4,aligny top");

      JButton btnWatched = new JButton("");
      btnWatched.setMargin(new Insets(2, 2, 2, 2));
      btnWatched.setIcon(IconManager.APPLY_INV);
      btnWatched.addActionListener(e -> {
        changed = true;
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        for (Movie movie : moviesToEdit) {
          movie.setWatched(chckbxWatched.isSelected());
        }
        setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
      });
      panelContent.add(btnWatched, "cell 2 4");

      JLabel lblVideo3D = new JLabel(BUNDLE.getString("metatag.3d")); //$NON-NLS-1$
      panelContent.add(lblVideo3D, "cell 0 5,alignx right");

      final JCheckBox chckbxVideo3D = new JCheckBox("");
      panelContent.add(chckbxVideo3D, "cell 1 5");

      JButton btnVideo3D = new JButton("");
      btnVideo3D.setMargin(new Insets(2, 2, 2, 2));
      btnVideo3D.setIcon(IconManager.APPLY_INV);
      btnVideo3D.addActionListener(e -> {
        changed = true;
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        for (Movie movie : moviesToEdit) {
          movie.setVideoIn3D(chckbxVideo3D.isSelected());
        }
        setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
      });
      panelContent.add(btnVideo3D, "cell 2 5");

      JLabel lblMediasource = new JLabel(BUNDLE.getString("metatag.source")); //$NON-NLS-1$
      panelContent.add(lblMediasource, "cell 0 6,alignx right");

      final JComboBox cbMediaSource = new JComboBox(MediaSource.values());
      panelContent.add(cbMediaSource, "cell 1 6,growx");

      JButton btnMediaSource = new JButton("");
      btnMediaSource.setMargin(new Insets(2, 2, 2, 2));
      btnMediaSource.setIcon(IconManager.APPLY_INV);
      btnMediaSource.addActionListener(e -> {
        changed = true;
        Object obj = cbMediaSource.getSelectedItem();
        if (obj instanceof MediaSource) {
          MediaSource mediaSource = (MediaSource) obj;
          setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
          for (Movie movie : moviesToEdit) {
            movie.setMediaSource(mediaSource);
          }
          setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        }
      });
      panelContent.add(btnMediaSource, "cell 2 6");

      JLabel lblLanguage = new JLabel(BUNDLE.getString("metatag.language")); //$NON-NLS-1$
      panelContent.add(lblLanguage, "cell 0 7,alignx right");

      tfLanguage = new JTextField();
      panelContent.add(tfLanguage, "cell 1 7,growx");
      tfLanguage.setColumns(10);

      JButton btnLanguage = new JButton("");
      btnLanguage.setMargin(new Insets(2, 2, 2, 2));
      btnLanguage.setIcon(IconManager.APPLY_INV);
      btnLanguage.addActionListener(e -> {
        changed = true;
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        for (Movie movie : moviesToEdit) {
          movie.setSpokenLanguages(tfLanguage.getText());
        }
        setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
      });
      panelContent.add(btnLanguage, "cell 2 7");
      {

        JLabel lblSorttitleT = new JLabel(BUNDLE.getString("metatag.sorttitle")); //$NON-NLS-1$
        panelContent.add(lblSorttitleT, "flowx,cell 0 8,alignx right");

        JButton btnSetSorttitle = new JButton(BUNDLE.getString("edit.setsorttitle")); //$NON-NLS-1$
        btnSetSorttitle.addActionListener(e -> {
          changed = true;
          setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
          for (Movie movie : moviesToEdit) {
            movie.setSortTitle(movie.getTitleSortable());
          }
          setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        });
        panelContent.add(btnSetSorttitle, "cell 1 8");

        JButton btnClearSorttitle = new JButton(BUNDLE.getString("edit.clearsorttitle")); //$NON-NLS-1$
        btnClearSorttitle.addActionListener(e -> {
          changed = true;
          setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
          for (Movie movie : moviesToEdit) {
            movie.setSortTitle("");
          }
          setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        });
        panelContent.add(btnClearSorttitle, "cell 1 9");
      }

      JLabel lblSorttitleInfo = new JLabel(IconManager.HINT);
      lblSorttitleInfo.setToolTipText(BUNDLE.getString("edit.setsorttitle.desc")); //$NON-NLS-1$
      panelContent.add(lblSorttitleInfo, "cell 0 8");
    }

    {
      JButton btnClose = new JButton(BUNDLE.getString("Button.close")); //$NON-NLS-1$
      btnClose.setIcon(IconManager.APPLY_INV);
      btnClose.addActionListener(arg0 -> {
        // rewrite movies, if anything changed
        if (changed) {
          setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
          for (Movie movie : moviesToEdit) {
            movie.writeNFO();
            movie.saveToDb();
          }
          setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        }
        setVisible(false);
      });
      addDefaultButton(btnClose);

      // add window listener to write changes (if the window close button "X" is pressed)
      addWindowListener(new WindowAdapter() {
        @Override
        public void windowClosing(WindowEvent e) {
          // rewrite movies, if anything changed
          if (changed) {
            for (Movie movie : moviesToEdit) {
              movie.writeNFO();
              movie.saveToDb();
            }
            // if configured - sync with trakt.tv
            if (MovieModuleManager.SETTINGS.getSyncTrakt()) {
              TmmTask task = new SyncTraktTvTask(moviesToEdit, null);
              TmmTaskManager.getInstance().addUnnamedTask(task);
            }
          }
        }
      });
    }

    {
      setMovieSets();
      moviesToEdit = movies;

      PropertyChangeListener listener = evt -> {
        if ("addedMovieSet".equals(evt.getPropertyName())) {
          setMovieSets();
        }
      };
      movieList.addPropertyChangeListener(listener);
    }
  }

  private void setMovieSets() {
    MovieSet selectedMovieSet = null;

    Object obj = cbMovieSet.getSelectedItem();
    if (obj instanceof MovieSet) {
      selectedMovieSet = (MovieSet) obj;
    }

    cbMovieSet.removeAllItems();

    cbMovieSet.addItem("");

    for (MovieSet movieSet : movieList.getSortedMovieSetList()) {
      cbMovieSet.addItem(movieSet);
    }

    if (selectedMovieSet != null) {
      cbMovieSet.setSelectedItem(selectedMovieSet);
    }
  }
}
