/*
 * Copyright 2012 - 2020 Manuel Laggner
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
import java.text.MessageFormat;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.apache.commons.lang3.StringUtils;
import org.tinymediamanager.core.MediaCertification;
import org.tinymediamanager.core.MediaSource;
import org.tinymediamanager.core.UTF8Control;
import org.tinymediamanager.core.entities.MediaGenres;
import org.tinymediamanager.core.movie.MovieEdition;
import org.tinymediamanager.core.movie.MovieList;
import org.tinymediamanager.core.movie.MovieModuleManager;
import org.tinymediamanager.core.movie.entities.Movie;
import org.tinymediamanager.core.movie.entities.MovieSet;
import org.tinymediamanager.core.threading.TmmTask;
import org.tinymediamanager.core.threading.TmmTaskManager;
import org.tinymediamanager.scraper.util.ListUtils;
import org.tinymediamanager.thirdparty.trakttv.SyncTraktTvTask;
import org.tinymediamanager.ui.IconManager;
import org.tinymediamanager.ui.components.TmmLabel;
import org.tinymediamanager.ui.components.combobox.AutocompleteComboBox;
import org.tinymediamanager.ui.dialogs.TmmDialog;
import org.tinymediamanager.ui.moviesets.actions.MovieSetAddAction;

import net.miginfocom.swing.MigLayout;

/**
 * The class MovieBulkEditor is used for bulk editing movies
 * 
 * @author Manuel Laggner
 */
public class MovieBulkEditorDialog extends TmmDialog {
  private static final long           serialVersionUID = -8515248604267310279L;
  /** @wbp.nls.resourceBundle messages */
  private static final ResourceBundle BUNDLE           = ResourceBundle.getBundle("messages", new UTF8Control());

  private MovieList                   movieList        = MovieList.getInstance();
  private List<Movie>                 moviesToEdit;
  private boolean                     changed          = false;

  private JComboBox                   cbMovieSet;

  /**
   * Instantiates a new movie batch editor.
   * 
   * @param movies
   *          the movies
   */
  public MovieBulkEditorDialog(final List<Movie> movies) {
    super(BUNDLE.getString("movie.edit"), "movieBulkEditor");

    {
      JPanel panelContent = new JPanel();
      getContentPane().add(panelContent, BorderLayout.CENTER);
      panelContent.setLayout(new MigLayout("", "[20lp:n][200lp:350lp,grow][][][]", "[][][][][][][][][][][][][][]"));

      {
        JLabel lblGenresT = new TmmLabel(BUNDLE.getString("metatag.genre"));
        panelContent.add(lblGenresT, "cell 0 0,alignx right");

        JComboBox cbGenres = new AutocompleteComboBox(MediaGenres.values());
        cbGenres.setEditable(true);
        panelContent.add(cbGenres, "cell 1 0, growx, wmin 0");

        JButton btnAddGenre = new JButton();
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

          if (genre != null) {
            for (Movie movie : moviesToEdit) {
              movie.addGenre(genre);
            }
          }
          setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        });
        panelContent.add(btnAddGenre, "cell 2 0");

        JButton btnRemoveGenre = new JButton();
        btnRemoveGenre.setIcon(IconManager.REMOVE_INV);
        btnRemoveGenre.setMargin(new Insets(2, 2, 2, 2));
        btnRemoveGenre.addActionListener(e -> {
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

          if (genre != null) {
            for (Movie movie : moviesToEdit) {
              movie.removeGenre(genre);
            }
          }
          setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        });
        panelContent.add(btnRemoveGenre, "cell 3 0");

        JButton btnRemoveAllGenres = new JButton();
        btnRemoveAllGenres.setIcon(IconManager.DELETE);
        btnRemoveAllGenres.setMargin(new Insets(2, 2, 2, 2));
        btnRemoveAllGenres.addActionListener(e -> {
          if (isDeleteConfirmed(BUNDLE.getString("metatag.genre"))) {
            changed = true;
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            for (Movie movie : moviesToEdit) {
              movie.removeAllGenres();
            }
            setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
          }
        });
        panelContent.add(btnRemoveAllGenres, "cell 4 0");
      }
      {
        JLabel lblTagsT = new TmmLabel(BUNDLE.getString("metatag.tags"));
        panelContent.add(lblTagsT, "cell 0 1,alignx right");

        JComboBox cbTags = new AutocompleteComboBox(ListUtils.asSortedList(movieList.getTagsInMovies()));
        cbTags.setEditable(true);
        panelContent.add(cbTags, "cell 1 1, growx, wmin 0");

        JButton btnAddTag = new JButton();
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

        JButton btnRemoveTag = new JButton();
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

        JButton btnRemoveAllTags = new JButton();
        btnRemoveAllTags.setIcon(IconManager.DELETE);
        btnRemoveAllTags.setMargin(new Insets(2, 2, 2, 2));
        btnRemoveAllTags.addActionListener(e -> {
          if (isDeleteConfirmed(BUNDLE.getString("metatag.tags"))) {
            changed = true;
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            for (Movie movie : moviesToEdit) {
              movie.removeAllTags();
            }
            setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
          }
        });
        panelContent.add(btnRemoveAllTags, "cell 4 1");
      }
      {
        JLabel lblEditionT = new TmmLabel(BUNDLE.getString("metatag.edition"));
        panelContent.add(lblEditionT, "cell 0 2,alignx right");

        JComboBox cbEdition = new AutocompleteComboBox(MovieEdition.values());
        panelContent.add(cbEdition, "cell 1 2,growx");

        JButton btnMovieEdition = new JButton();
        btnMovieEdition.setIcon(IconManager.APPLY_INV);
        btnMovieEdition.setMargin(new Insets(2, 2, 2, 2));
        btnMovieEdition.addActionListener(e -> {
          changed = true;
          setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
          MovieEdition edition = null;
          Object item = cbEdition.getSelectedItem();

          // edition
          if (item instanceof MovieEdition) {
            edition = (MovieEdition) item;
          }

          // newly created edition?
          if (item instanceof String) {
            edition = MovieEdition.getMovieEdition((String) item);
          }

          if (edition != null) {
            for (Movie movie : moviesToEdit) {
              movie.setEdition(edition);
            }
          }
          setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        });
        panelContent.add(btnMovieEdition, "cell 2 2");
      }
      {
        JLabel lblCertificationT = new TmmLabel(BUNDLE.getString("metatag.certification"));
        panelContent.add(lblCertificationT, "cell 0 3,alignx right");

        final JComboBox cbCertification = new JComboBox();
        for (MediaCertification cert : MediaCertification.getCertificationsforCountry(MovieModuleManager.SETTINGS.getCertificationCountry())) {
          cbCertification.addItem(cert);
        }
        panelContent.add(cbCertification, "cell 1 3,growx");

        JButton btnCertification = new JButton();
        btnCertification.setMargin(new Insets(2, 2, 2, 2));
        btnCertification.setIcon(IconManager.APPLY_INV);
        btnCertification.addActionListener(e -> {
          changed = true;
          setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
          MediaCertification cert = (MediaCertification) cbCertification.getSelectedItem();
          for (Movie movie : moviesToEdit) {
            movie.setCertification(cert);

          }
          setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        });
        panelContent.add(btnCertification, "cell 2 3");
      }
      {
        JLabel lblMovieSetT = new TmmLabel(BUNDLE.getString("metatag.movieset"));
        panelContent.add(lblMovieSetT, "cell 0 4,alignx right");

        cbMovieSet = new JComboBox();
        panelContent.add(cbMovieSet, "cell 1 4,growx,wmin 0");

        JButton btnSetMovieSet = new JButton();
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
        panelContent.add(btnSetMovieSet, "cell 2 4");

        JButton btnNewMovieset = new JButton();
        btnNewMovieset.setMargin(new Insets(2, 2, 2, 2));
        btnNewMovieset.setAction(new MovieSetAddAction());
        panelContent.add(btnNewMovieset, "cell 3 4 2 1,growx");
      }
      {
        JLabel lblWatchedT = new TmmLabel(BUNDLE.getString("metatag.watched"));
        panelContent.add(lblWatchedT, "cell 0 5,alignx right");

        JCheckBox chckbxWatched = new JCheckBox("");
        panelContent.add(chckbxWatched, "cell 1 5,aligny top");

        JButton btnWatched = new JButton();
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
        panelContent.add(btnWatched, "cell 2 5");
      }
      {
        JLabel lblVideo3DT = new TmmLabel(BUNDLE.getString("metatag.3d"));
        panelContent.add(lblVideo3DT, "cell 0 6,alignx right");

        final JCheckBox chckbxVideo3D = new JCheckBox("");
        panelContent.add(chckbxVideo3D, "cell 1 6");

        JButton btnVideo3D = new JButton();
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
        panelContent.add(btnVideo3D, "cell 2 6");
      }
      {
        JLabel lblMediasourceT = new TmmLabel(BUNDLE.getString("metatag.source"));
        panelContent.add(lblMediasourceT, "cell 0 7,alignx right");

        final JComboBox cbMediaSource = new JComboBox(MediaSource.values());
        panelContent.add(cbMediaSource, "cell 1 7,growx");

        JButton btnMediaSource = new JButton();
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
        panelContent.add(btnMediaSource, "cell 2 7");
      }
      {
        JLabel lblLanguageT = new TmmLabel(BUNDLE.getString("metatag.language"));
        panelContent.add(lblLanguageT, "cell 0 8,alignx right");

        JTextField tfLanguage = new JTextField();
        panelContent.add(tfLanguage, "cell 1 8,growx");

        JButton btnLanguage = new JButton();
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
        panelContent.add(btnLanguage, "cell 2 8");
      }
      {
        JLabel lblCountryT = new TmmLabel(BUNDLE.getString("metatag.country"));
        panelContent.add(lblCountryT, "cell 0 9,alignx trailing");

        JTextField tfCountry = new JTextField();
        panelContent.add(tfCountry, "cell 1 9,growx");

        JButton btnCountry = new JButton();
        btnCountry.setMargin(new Insets(2, 2, 2, 2));
        btnCountry.setIcon(IconManager.APPLY_INV);
        btnCountry.addActionListener(e -> {
          changed = true;
          setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
          for (Movie movie : moviesToEdit) {
            movie.setCountry(tfCountry.getText());
          }
          setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        });
        panelContent.add(btnCountry, "cell 2 9");
      }
      {
        JLabel lblNoteT = new TmmLabel(BUNDLE.getString("metatag.note"));
        panelContent.add(lblNoteT, "cell 0 10,alignx trailing");

        JTextField tfNote = new JTextField();
        panelContent.add(tfNote, "cell 1 10,growx");

        JButton btnNote = new JButton();
        btnNote.setMargin(new Insets(2, 2, 2, 2));
        btnNote.setIcon(IconManager.APPLY_INV);
        btnNote.addActionListener(e -> {
          changed = true;
          setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
          for (Movie movie : moviesToEdit) {
            movie.setNote(tfNote.getText());
          }
          setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        });
        panelContent.add(btnNote, "cell 2 10");
      }
      {
        JLabel lblSorttitleT = new TmmLabel(BUNDLE.getString("metatag.sorttitle"));
        panelContent.add(lblSorttitleT, "flowx,cell 0 11,alignx right");

        JLabel lblSorttitleInfo = new JLabel(IconManager.HINT);
        lblSorttitleInfo.setToolTipText(BUNDLE.getString("edit.setsorttitle.desc"));
        panelContent.add(lblSorttitleInfo, "cell 0 11");

        JButton btnSetSorttitle = new JButton(BUNDLE.getString("edit.setsorttitle"));
        btnSetSorttitle.addActionListener(e -> {
          changed = true;
          setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
          for (Movie movie : moviesToEdit) {
            movie.setSortTitle(movie.getTitleSortable());
          }
          setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        });
        panelContent.add(btnSetSorttitle, "cell 1 11");

        JButton btnClearSorttitle = new JButton(BUNDLE.getString("edit.clearsorttitle"));
        btnClearSorttitle.addActionListener(e -> {
          changed = true;
          setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
          for (Movie movie : moviesToEdit) {
            movie.setSortTitle("");
          }
          setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        });
        panelContent.add(btnClearSorttitle, "cell 1 13");
      }
      {
        JLabel lblSpokenLanguages = new TmmLabel(BUNDLE.getString("metatag.spokenlanguages"));
        panelContent.add(lblSpokenLanguages, "cell 0 12,alignx right");

        JButton btnFirstAudioStream = new JButton(BUNDLE.getString("edit.audio.first"));
        btnFirstAudioStream.setToolTipText(BUNDLE.getString("edit.audio.first.desc"));
        btnFirstAudioStream.addActionListener(e -> {
          changed = true;
          setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
          for (Movie movie : moviesToEdit) {
            movie.setSpokenLanguages(movie.getMediaInfoAudioLanguageList().stream().findFirst().orElse(""));
          }
          setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        });
        panelContent.add(btnFirstAudioStream, "flowx,cell 1 12");

        JButton btnBestAudioStream = new JButton(BUNDLE.getString("edit.audio.best"));
        btnBestAudioStream.setToolTipText(BUNDLE.getString("edit.audio.best.desc"));
        btnBestAudioStream.addActionListener(e -> {
          changed = true;
          setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
          for (Movie movie : moviesToEdit) {
            movie.setSpokenLanguages(movie.getMediaInfoAudioLanguage());
          }
          setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        });
        panelContent.add(btnBestAudioStream, "cell 1 12");

        JButton btnAllAudioStreams = new JButton(BUNDLE.getString("edit.audio.all"));
        btnAllAudioStreams.setToolTipText(BUNDLE.getString("edit.audio.all.desc"));
        btnAllAudioStreams.addActionListener(e -> {
          changed = true;
          setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
          for (Movie movie : moviesToEdit) {
            movie.setSpokenLanguages(String.join(", ", movie.getMediaInfoAudioLanguageList()));
          }
          setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        });
        panelContent.add(btnAllAudioStreams, "cell 1 12");
      }
    }

    {
      JButton btnClose = new JButton(BUNDLE.getString("Button.close"));
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

  private boolean isDeleteConfirmed(String attribute) {
    Object[] options = { BUNDLE.getString("Button.yes"), BUNDLE.getString("Button.no") };
    int dialogResult = JOptionPane.showOptionDialog(null, MessageFormat.format(BUNDLE.getString("message.bulkedit.delete"), attribute),
        BUNDLE.getString("message.bulkedit.warning"), JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, null);
    if (dialogResult == JOptionPane.YES_OPTION) {
      return true;
    }
    else {
      return false;
    }
  }
}
