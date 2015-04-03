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
package org.tinymediamanager.ui.movies.dialogs;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.FlowLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.apache.commons.lang3.StringUtils;
import org.tinymediamanager.core.movie.MovieList;
import org.tinymediamanager.core.movie.MovieMediaSource;
import org.tinymediamanager.core.movie.MovieModuleManager;
import org.tinymediamanager.core.movie.entities.Movie;
import org.tinymediamanager.core.movie.entities.MovieSet;
import org.tinymediamanager.core.threading.TmmTask;
import org.tinymediamanager.core.threading.TmmTaskManager;
import org.tinymediamanager.scraper.Certification;
import org.tinymediamanager.scraper.MediaGenres;
import org.tinymediamanager.scraper.trakttv.SyncTraktTvTask;
import org.tinymediamanager.ui.IconManager;
import org.tinymediamanager.ui.UTF8Control;
import org.tinymediamanager.ui.components.AutocompleteComboBox;
import org.tinymediamanager.ui.dialogs.TmmDialog;
import org.tinymediamanager.ui.moviesets.actions.MovieSetAddAction;

import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

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

  /**
   * Instantiates a new movie batch editor.
   * 
   * @param movies
   *          the movies
   */
  public MovieBatchEditorDialog(final List<Movie> movies) {
    super(BUNDLE.getString("movie.edit"), "movieBatchEditor"); //$NON-NLS-1$
    setBounds(5, 5, 350, 230);
    getContentPane().setLayout(new BorderLayout(0, 0));

    {
      JPanel panelContent = new JPanel();
      getContentPane().add(panelContent, BorderLayout.CENTER);
      panelContent.setLayout(new FormLayout(new ColumnSpec[] { FormFactory.RELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC,
          FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("default:grow"), FormFactory.RELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC,
          FormFactory.RELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC, FormFactory.RELATED_GAP_COLSPEC, }, new RowSpec[] {
          FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
          FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
          FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
          FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC, }));

      JLabel lblGenres = new JLabel(BUNDLE.getString("metatag.genre")); //$NON-NLS-1$
      panelContent.add(lblGenres, "2, 2, right, default");

      // cbGenres = new JComboBox(MediaGenres2.values());
      cbGenres = new AutocompleteComboBox(MediaGenres.values());
      cbGenres.setEditable(true);
      panelContent.add(cbGenres, "4, 2, fill, default");

      JButton btnAddGenre = new JButton("");
      btnAddGenre.setIcon(IconManager.LIST_ADD);
      btnAddGenre.setMargin(new Insets(2, 2, 2, 2));
      btnAddGenre.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
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
        }
      });
      panelContent.add(btnAddGenre, "6, 2");

      JButton btnRemoveGenre = new JButton("");
      btnRemoveGenre.setIcon(IconManager.LIST_REMOVE);
      btnRemoveGenre.setMargin(new Insets(2, 2, 2, 2));
      btnRemoveGenre.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
          changed = true;
          setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
          MediaGenres genre = (MediaGenres) cbGenres.getSelectedItem();
          for (Movie movie : moviesToEdit) {
            movie.removeGenre(genre);
          }
          setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        }
      });
      panelContent.add(btnRemoveGenre, "8, 2");

      JLabel lblTags = new JLabel(BUNDLE.getString("metatag.tags")); //$NON-NLS-1$
      panelContent.add(lblTags, "2, 4, right, default");

      cbTags = new AutocompleteComboBox(movieList.getTagsInMovies().toArray());
      cbTags.setEditable(true);
      panelContent.add(cbTags, "4, 4, fill, default");

      JButton btnAddTag = new JButton("");
      btnAddTag.setIcon(IconManager.LIST_ADD);
      btnAddTag.setMargin(new Insets(2, 2, 2, 2));
      btnAddTag.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
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
        }
      });
      panelContent.add(btnAddTag, "6, 4");

      JButton btnRemoveTag = new JButton("");
      btnRemoveTag.setIcon(IconManager.LIST_REMOVE);
      btnRemoveTag.setMargin(new Insets(2, 2, 2, 2));
      btnRemoveTag.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
          changed = true;
          setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
          String tag = (String) cbTags.getSelectedItem();
          for (Movie movie : moviesToEdit) {
            movie.removeFromTags(tag);
          }
          setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        }
      });
      panelContent.add(btnRemoveTag, "8, 4");

      JLabel lblCertification = new JLabel(BUNDLE.getString("metatag.certification")); //$NON-NLS-1$
      panelContent.add(lblCertification, "2, 6, right, default");

      final JComboBox cbCertification = new JComboBox();
      for (Certification cert : Certification.getCertificationsforCountry(MovieModuleManager.MOVIE_SETTINGS.getCertificationCountry())) {
        cbCertification.addItem(cert);
      }
      panelContent.add(cbCertification, "4, 6, fill, default");

      JButton btnCertification = new JButton("");
      btnCertification.setMargin(new Insets(2, 2, 2, 2));
      btnCertification.setIcon(IconManager.APPLY);
      btnCertification.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
          changed = true;
          setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
          Certification cert = (Certification) cbCertification.getSelectedItem();
          for (Movie movie : moviesToEdit) {
            movie.setCertification(cert);
            ;
          }
          setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        }
      });
      panelContent.add(btnCertification, "6, 6");

      JLabel lblMovieSet = new JLabel(BUNDLE.getString("metatag.movieset")); //$NON-NLS-1$
      panelContent.add(lblMovieSet, "2, 8, right, default");

      cbMovieSet = new JComboBox();
      panelContent.add(cbMovieSet, "4, 8, fill, default");

      JButton btnSetMovieSet = new JButton("");
      btnSetMovieSet.setMargin(new Insets(2, 2, 2, 2));
      btnSetMovieSet.setIcon(IconManager.APPLY);
      btnSetMovieSet.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
          changed = true;
          setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
          // movie set
          Object obj = cbMovieSet.getSelectedItem();
          for (Movie movie : moviesToEdit) {
            if (obj instanceof String) {
              movie.removeFromMovieSet();
              movie.setSortTitle("");
            }
            if (obj instanceof MovieSet) {
              MovieSet movieSet = (MovieSet) obj;

              if (movie.getMovieSet() != movieSet) {
                movie.removeFromMovieSet();
                movie.setMovieSet(movieSet);
                // movieSet.addMovie(movie);
                movieSet.insertMovie(movie);
              }

              // movie.setSortTitleFromMovieSet();
              // movie.saveToDb();
              movieSet.updateMovieSorttitle();
            }
          }
          setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        }
      });
      panelContent.add(btnSetMovieSet, "6, 8");

      JButton btnNewMovieset = new JButton("");
      btnNewMovieset.setMargin(new Insets(2, 2, 2, 2));
      btnNewMovieset.setAction(new MovieSetAddAction(false));
      panelContent.add(btnNewMovieset, "8, 8");

      JLabel lblWatched = new JLabel(BUNDLE.getString("metatag.watched")); //$NON-NLS-1$
      panelContent.add(lblWatched, "2, 10, right, default");

      chckbxWatched = new JCheckBox("");
      panelContent.add(chckbxWatched, "4, 10");

      JButton btnWatched = new JButton("");
      btnWatched.setMargin(new Insets(2, 2, 2, 2));
      btnWatched.setIcon(IconManager.APPLY);
      btnWatched.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
          changed = true;
          setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
          for (Movie movie : moviesToEdit) {
            movie.setWatched(chckbxWatched.isSelected());
          }
          setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        }
      });
      panelContent.add(btnWatched, "6, 10");

      JLabel lblVideo3D = new JLabel(BUNDLE.getString("metatag.3d")); //$NON-NLS-1$
      panelContent.add(lblVideo3D, "2, 12, right, default");

      final JCheckBox chckbxVideo3D = new JCheckBox("");
      panelContent.add(chckbxVideo3D, "4, 12");

      JButton btnVideo3D = new JButton("");
      btnVideo3D.setMargin(new Insets(2, 2, 2, 2));
      btnVideo3D.setIcon(IconManager.APPLY);
      btnVideo3D.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
          changed = true;
          setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
          for (Movie movie : moviesToEdit) {
            movie.setVideoIn3D(chckbxVideo3D.isSelected());
          }
          setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        }
      });
      panelContent.add(btnVideo3D, "6, 12");

      JLabel lblMediasource = new JLabel(BUNDLE.getString("metatag.source")); //$NON-NLS-1$
      panelContent.add(lblMediasource, "2, 14, right, default");

      final JComboBox cbMediaSource = new JComboBox(MovieMediaSource.values());
      panelContent.add(cbMediaSource, "4, 14, fill, default");

      JButton btnMediaSource = new JButton("");
      btnMediaSource.setMargin(new Insets(2, 2, 2, 2));
      btnMediaSource.setIcon(IconManager.APPLY);
      btnMediaSource.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
          changed = true;
          Object obj = cbMediaSource.getSelectedItem();
          if (obj instanceof MovieMediaSource) {
            MovieMediaSource mediaSource = (MovieMediaSource) obj;
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            for (Movie movie : moviesToEdit) {
              movie.setMediaSource(mediaSource);
            }
            setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
          }
        }
      });
      panelContent.add(btnMediaSource, "6, 14");
    }

    {
      JPanel panelButtons = new JPanel();
      FlowLayout flowLayout = (FlowLayout) panelButtons.getLayout();
      flowLayout.setAlignment(FlowLayout.RIGHT);
      getContentPane().add(panelButtons, BorderLayout.SOUTH);

      JButton btnClose = new JButton(BUNDLE.getString("Button.close")); //$NON-NLS-1$
      btnClose.setIcon(IconManager.APPLY);
      btnClose.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent arg0) {
          // rewrite movies, if anything changed
          if (changed) {
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            for (Movie movie : moviesToEdit) {
              movie.saveToDb();
            }
            setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
          }
          setVisible(false);
        }
      });
      panelButtons.add(btnClose);

      // add window listener to write changes (if the window close button "X" is
      // pressed)
      addWindowListener(new WindowAdapter() {
        @Override
        public void windowClosing(WindowEvent e) {
          // rewrite movies, if anything changed
          if (changed) {
            for (Movie movie : moviesToEdit) {
              movie.saveToDb();
            }
            // if configured - sync with trakt.tv
            if (MovieModuleManager.MOVIE_SETTINGS.getSyncTrakt()) {
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

      PropertyChangeListener listener = new PropertyChangeListener() {
        @Override
        public void propertyChange(PropertyChangeEvent evt) {
          if ("addedMovieSet".equals(evt.getPropertyName())) {
            setMovieSets();
          }
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
