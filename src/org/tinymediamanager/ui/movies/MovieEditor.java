/*
 * Copyright 2012 Manuel Laggner
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
package org.tinymediamanager.ui.movies;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.SpinnerDateModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.EmptyBorder;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import org.apache.commons.lang3.StringUtils;
import org.jdesktop.beansbinding.AutoBinding.UpdateStrategy;
import org.jdesktop.beansbinding.BeanProperty;
import org.jdesktop.observablecollections.ObservableCollections;
import org.jdesktop.swingbinding.JComboBoxBinding;
import org.jdesktop.swingbinding.JListBinding;
import org.jdesktop.swingbinding.JTableBinding;
import org.jdesktop.swingbinding.SwingBindings;
import org.tinymediamanager.Globals;
import org.tinymediamanager.core.movie.Movie;
import org.tinymediamanager.core.movie.MovieCast;
import org.tinymediamanager.core.movie.MovieList;
import org.tinymediamanager.core.movie.MovieSet;
import org.tinymediamanager.scraper.Certification;
import org.tinymediamanager.scraper.MediaGenres;
import org.tinymediamanager.scraper.MediaTrailer;
import org.tinymediamanager.ui.AutocompleteComboBox;
import org.tinymediamanager.ui.EqualsLayout;
import org.tinymediamanager.ui.ImageLabel;
import org.tinymediamanager.ui.TableColumnAdjuster;
import org.tinymediamanager.ui.TmmWindowSaver;
import org.tinymediamanager.ui.movies.MovieImageChooser.ImageType;

import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

/**
 * The Class MovieEditor.
 */
public class MovieEditor extends JDialog {

  /** The Constant serialVersionUID. */
  private static final long  serialVersionUID     = 1L;

  /** The details1 panel. */
  private final JPanel       details1Panel        = new JPanel();

  /** The details2 panel. */
  private final JPanel       details2Panel        = new JPanel();

  /** The movie to edit. */
  private Movie              movieToEdit;

  /** The movielist. */
  private MovieList          movieList            = MovieList.getInstance();

  /** The tf title. */
  private JTextField         tfTitle;

  /** The tf original title. */
  private JTextField         tfOriginalTitle;

  /** The tf year. */
  private JSpinner           spYear;

  /** The tp plot. */
  private JTextPane          tpPlot;

  /** The tf director. */
  private JTextField         tfDirector;

  /** The table. */
  private JTable             tableActors;

  /** The lbl movie path. */
  private JLabel             lblMoviePath;

  /** The lbl poster. */
  private ImageLabel         lblPoster;

  /** The lbl fanart. */
  private ImageLabel         lblFanart;

  /** The cast. */
  private List<MovieCast>    cast                 = ObservableCollections.observableList(new ArrayList<MovieCast>());

  /** The genres. */
  private List<MediaGenres>  genres               = ObservableCollections.observableList(new ArrayList<MediaGenres>());

  /** The trailers. */
  private List<MediaTrailer> trailers             = ObservableCollections.observableList(new ArrayList<MediaTrailer>());

  /** The tags. */
  private List<String>       tags                 = ObservableCollections.observableList(new ArrayList<String>());

  /** The action ok. */
  private final Action       actionOK             = new SwingAction();

  /** The action cancel. */
  private final Action       actionCancel         = new SwingAction_1();

  /** The action add actor. */
  private final Action       actionAddActor       = new SwingAction_4();

  /** The action remove actor. */
  private final Action       actionRemoveActor    = new SwingAction_5();

  /** The tf writer. */
  private JTextField         tfWriter;

  /** The sp runtime. */
  private JSpinner           spRuntime;

  /** The tf production companies. */
  private JTextPane          tfProductionCompanies;

  /** The list genres. */
  private JList              listGenres;

  /** The action add genre. */
  private final Action       actionAddGenre       = new SwingAction_2();

  /** The action remove genre. */
  private final Action       actionRemoveGenre    = new SwingAction_3();

  /** The cb genres. */
  private JComboBox          cbGenres;

  /** The sp rating. */
  private JSpinner           spRating;

  /** The cb certification. */
  private JComboBox          cbCertification;

  /** The tf imdb id. */
  private JTextField         tfImdbId;

  /** The tf tmdb id. */
  private JTextField         tfTmdbId;

  /** The lbl imdb id. */
  private JLabel             lblImdbId;

  /** The lbl tmdb id. */
  private JLabel             lblTmdbId;

  /** The lbl watched. */
  private JLabel             lblWatched;

  /** The cb watched. */
  private JCheckBox          cbWatched;

  /** The tf tagline. */
  private JTextPane          tpTagline;

  /** The table trailer. */
  private JTable             tableTrailer;

  /** The action. */
  private final Action       action               = new SwingAction_6();

  /** The action_1. */
  private final Action       action_1             = new SwingAction_7();

  /** The cb tags. */
  private JComboBox          cbTags;

  /** The list tags. */
  private JList              listTags;

  /** The action_2. */
  private final Action       action_2             = new SwingAction_8();

  /** The action_3. */
  private final Action       action_3             = new SwingAction_9();

  private final Action       actionToggleMovieSet = new ToggleMovieSetAction();

  /** The sp date added. */
  private JSpinner           spDateAdded;

  /** The extrathumbs. */
  private List<String>       extrathumbs          = new ArrayList<String>();

  /** The cb movie set. */
  private JComboBox          cbMovieSet;

  /** The tf sorttitle. */
  private JTextField         tfSorttitle;

  /** The tf spoken languages. */
  private JTextField         tfSpokenLanguages;

  /** The continue queue. */
  private boolean            continueQueue        = true;

  /** The abort action. */
  private final Action       abortAction          = new SwingAction_10();

  /**
   * Create the dialog.
   * 
   * @param movie
   *          the movie
   */
  public MovieEditor(Movie movie, boolean inQueue) {
    setModal(true);
    setIconImage(Globals.logo);
    setTitle("Edit Movie");
    setName("movieEditor");
    setBounds(5, 5, 950, 700);
    TmmWindowSaver.loadSettings(this);

    movieToEdit = movie;
    getContentPane().setLayout(new BorderLayout());
    {
      JPanel panelPath = new JPanel();
      getContentPane().add(panelPath, BorderLayout.NORTH);
      panelPath.setLayout(new FormLayout(new ColumnSpec[] { FormFactory.RELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC,
          FormFactory.DEFAULT_COLSPEC, FormFactory.RELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC, }, new RowSpec[] { FormFactory.LINE_GAP_ROWSPEC,
          RowSpec.decode("15px"), FormFactory.RELATED_GAP_ROWSPEC, }));

      JLabel lblMoviePathT = new JLabel("Path");
      panelPath.add(lblMoviePathT, "2, 2, left, top");

      lblMoviePath = new JLabel("");
      lblMoviePath.setFont(new Font("Dialog", Font.BOLD, 14));
      panelPath.add(lblMoviePath, "5, 2, left, top");
    }

    JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.NORTH);
    tabbedPane.addTab("Details", details1Panel);
    getContentPane().add(tabbedPane, BorderLayout.CENTER);

    details1Panel.setBorder(new EmptyBorder(5, 5, 5, 5));
    details1Panel.setLayout(new FormLayout(new ColumnSpec[] { FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("max(40dlu;default)"),
        FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("50px:grow"), FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("50px"),
        FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("100px:grow"), FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("default:grow"),
        FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("50px"), FormFactory.UNRELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC,
        FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("250px:grow"), FormFactory.RELATED_GAP_COLSPEC, }, new RowSpec[] {
        FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
        FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
        FormFactory.RELATED_GAP_ROWSPEC, RowSpec.decode("50px"), FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
        FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
        FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
        FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC, RowSpec.decode("top:max(75px;default)"),
        FormFactory.RELATED_GAP_ROWSPEC, RowSpec.decode("75px:grow"), FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
        FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC, RowSpec.decode("50px"),
        FormFactory.NARROW_LINE_GAP_ROWSPEC, RowSpec.decode("fill:30px:grow(2)"), }));

    {
      JLabel lblTitle = new JLabel("Title");
      details1Panel.add(lblTitle, "2, 4, right, default");
    }
    {
      tfTitle = new JTextField();
      details1Panel.add(tfTitle, "4, 4, 9, 1, fill, default");
      tfTitle.setColumns(10);
    }
    {
      // JLabel lblPoster = new JLabel("");
      lblPoster = new ImageLabel();
      lblPoster.addMouseListener(new MouseAdapter() {
        @Override
        public void mouseClicked(MouseEvent e) {
          MovieImageChooser dialog = new MovieImageChooser(movieToEdit.getImdbId(), movieToEdit.getTmdbId(), ImageType.POSTER, lblPoster, null);
          dialog.setVisible(true);
        }
      });
      lblPoster.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
      details1Panel.add(lblPoster, "14, 4, 3, 21, fill, fill");
    }
    {
      JLabel lblOriginalTitle = new JLabel("Originaltitle");
      details1Panel.add(lblOriginalTitle, "2, 6, right, default");
    }
    {
      tfOriginalTitle = new JTextField();
      details1Panel.add(tfOriginalTitle, "4, 6, 9, 1, fill, top");
      tfOriginalTitle.setColumns(10);
    }
    {
      JLabel lblSorttitle = new JLabel("Sorttitle");
      details1Panel.add(lblSorttitle, "2, 8, right, default");
    }
    {
      tfSorttitle = new JTextField();
      details1Panel.add(tfSorttitle, "4, 8, 9, 1, fill, default");
      tfSorttitle.setColumns(10);
    }
    {
      JLabel lblTagline = new JLabel("Tagline");
      details1Panel.add(lblTagline, "2, 10, right, top");
    }
    {
      JScrollPane scrollPaneTagline = new JScrollPane();
      tpTagline = new JTextPane();
      scrollPaneTagline.setViewportView(tpTagline);
      details1Panel.add(scrollPaneTagline, "4, 10, 9, 1, fill, fill");
    }
    {
      JLabel lblYear = new JLabel("Year");
      details1Panel.add(lblYear, "2, 12, right, default");
    }
    {
      spYear = new JSpinner();
      details1Panel.add(spYear, "4, 12, fill, top");
    }
    {
      JLabel lblRuntime = new JLabel("Runtime");
      details1Panel.add(lblRuntime, "8, 12, right, default");
    }
    {
      spRuntime = new JSpinner();
      details1Panel.add(spRuntime, "10, 12, fill, default");
    }
    {
      JLabel lblMin = new JLabel("min");
      details1Panel.add(lblMin, "12, 12");
    }
    {
      JLabel lblRating = new JLabel("Rating");
      details1Panel.add(lblRating, "2, 14, right, default");
    }
    {
      spRating = new JSpinner();
      details1Panel.add(spRating, "4, 14");
    }
    {
      JLabel lblCertification = new JLabel("Certification");
      details1Panel.add(lblCertification, "8, 14, right, default");
    }
    {
      cbCertification = new JComboBox();
      for (Certification cert : Certification.getCertificationsforCountry(Globals.settings.getCertificationCountry())) {
        cbCertification.addItem(cert);
      }
      details1Panel.add(cbCertification, "10, 14, 3, 1, fill, default");
    }
    {
      JLabel lblMovieSet = new JLabel("Movie set");
      details1Panel.add(lblMovieSet, "2, 16, right, default");
    }
    {
      cbMovieSet = new JComboBox();
      cbMovieSet.setAction(actionToggleMovieSet);
      details1Panel.add(cbMovieSet, "4, 16, 9, 1, fill, default");
    }
    {
      lblImdbId = new JLabel("IMDB Id");
      details1Panel.add(lblImdbId, "2, 18, right, default");
    }
    {
      tfImdbId = new JTextField();
      lblImdbId.setLabelFor(tfImdbId);
      details1Panel.add(tfImdbId, "4, 18, 3, 1, fill, default");
      tfImdbId.setColumns(10);
    }
    {
      lblTmdbId = new JLabel("TMDB Id");
      details1Panel.add(lblTmdbId, "8, 18, right, default");
    }
    {
      tfTmdbId = new JTextField();
      lblTmdbId.setLabelFor(tfTmdbId);
      details1Panel.add(tfTmdbId, "10, 18, 3, 1, fill, default");
      tfTmdbId.setColumns(10);
    }
    {
      lblWatched = new JLabel("Watched");
      details1Panel.add(lblWatched, "2, 20, right, default");
    }
    {
      cbWatched = new JCheckBox("");
      lblWatched.setLabelFor(cbWatched);
      details1Panel.add(cbWatched, "4, 20");
    }
    {
      JLabel lblDateAdded = new JLabel("Date added");
      details1Panel.add(lblDateAdded, "8, 20, right, default");
    }
    {
      spDateAdded = new JSpinner(new SpinnerDateModel());
      // JSpinner.DateEditor timeEditor = new JSpinner.DateEditor(spDateAdded,
      // "dd.MM.yyyy HH:mm:ss");
      // spDateAdded.setEditor(timeEditor);
      details1Panel.add(spDateAdded, "10, 20, 3, 1");
    }
    {
      JLabel lblSpokenLanguages = new JLabel("Spoken languages");
      details1Panel.add(lblSpokenLanguages, "2, 22, right, default");
    }
    {
      tfSpokenLanguages = new JTextField();
      details1Panel.add(tfSpokenLanguages, "4, 22, 9, 1, fill, default");
      tfSpokenLanguages.setColumns(10);
    }
    {
      JLabel lblPlot = new JLabel("Plot");
      details1Panel.add(lblPlot, "2, 24, right, top");
    }
    {
      JScrollPane scrollPanePlot = new JScrollPane();
      details1Panel.add(scrollPanePlot, "4, 24, 9, 3, fill, fill");
      {
        tpPlot = new JTextPane();
        scrollPanePlot.setViewportView(tpPlot);
      }
    }
    {
      JLabel lblDirector = new JLabel("Director");
      details1Panel.add(lblDirector, "2, 28, right, default");
    }
    {
      tfDirector = new JTextField();
      details1Panel.add(tfDirector, "4, 28, 9, 1, fill, top");
      tfDirector.setColumns(10);
    }
    {
      // JLabel lblFanart = new JLabel("");
      lblFanart = new ImageLabel();
      lblFanart.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
      lblFanart.addMouseListener(new MouseAdapter() {
        @Override
        public void mouseClicked(MouseEvent e) {
          MovieImageChooser dialog = new MovieImageChooser(movieToEdit.getImdbId(), movieToEdit.getTmdbId(), ImageType.FANART, lblFanart, extrathumbs);
          dialog.setVisible(true);
        }
      });
      details1Panel.add(lblFanart, "14, 26, 3, 7, fill, fill");
    }
    lblFanart.setImagePath(movie.getFanart());
    {
      JLabel lblWriter = new JLabel("Writer");
      details1Panel.add(lblWriter, "2, 30, right, default");
    }
    {
      tfWriter = new JTextField();
      details1Panel.add(tfWriter, "4, 30, 9, 1, fill, top");
      tfWriter.setColumns(10);
    }
    {
      JLabel lblCompany = new JLabel("Production");
      details1Panel.add(lblCompany, "2, 32, right, top");
    }
    {
      JScrollPane scrollPaneProduction = new JScrollPane();
      details1Panel.add(scrollPaneProduction, "4, 32, 9, 1, fill, fill");
      tfProductionCompanies = new JTextPane();
      scrollPaneProduction.setViewportView(tfProductionCompanies);
    }

    /**
     * DetailsPanel 2
     */
    tabbedPane.addTab("Details 2", details2Panel);
    details2Panel.setBorder(new EmptyBorder(5, 5, 5, 5));
    details2Panel.setLayout(new FormLayout(new ColumnSpec[] { FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("max(40dlu;default)"),
        FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("50px:grow"), FormFactory.RELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC,
        FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("100px:grow"), FormFactory.RELATED_GAP_COLSPEC, }, new RowSpec[] {
        FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
        FormFactory.RELATED_GAP_ROWSPEC, RowSpec.decode("fill:30px:grow"), FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
        FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
        FormFactory.RELATED_GAP_ROWSPEC, RowSpec.decode("default:grow"), FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
        FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC, RowSpec.decode("default:grow"),
        FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC, RowSpec.decode("default:grow(2)"), }));
    {
      JLabel lblActors = new JLabel("Actors");
      details2Panel.add(lblActors, "2, 2, right, default");
    }
    {
      JScrollPane scrollPane = new JScrollPane();
      details2Panel.add(scrollPane, "4, 2, 1, 7");
      {
        tableActors = new JTable();
        scrollPane.setViewportView(tableActors);
      }
    }
    {
      JLabel lblGenres = new JLabel("Genres");
      details2Panel.add(lblGenres, "6, 2");
    }
    {
      JButton btnAddActor = new JButton("Add Actor");
      btnAddActor.setMargin(new Insets(2, 2, 2, 2));
      btnAddActor.setAction(actionAddActor);
      btnAddActor.setIcon(new ImageIcon(MovieEditor.class.getResource("/org/tinymediamanager/ui/images/Add.png")));
      details2Panel.add(btnAddActor, "2, 4, right, top");
    }
    {
      JScrollPane scrollPaneGenres = new JScrollPane();
      details2Panel.add(scrollPaneGenres, "8, 2, 1, 5");
      {
        listGenres = new JList();
        scrollPaneGenres.setViewportView(listGenres);
      }
    }
    {
      JButton btnAddGenre = new JButton("");
      btnAddGenre.setAction(actionAddGenre);
      btnAddGenre.setIcon(new ImageIcon(MovieEditor.class.getResource("/org/tinymediamanager/ui/images/Add.png")));
      btnAddGenre.setMargin(new Insets(2, 2, 2, 2));
      details2Panel.add(btnAddGenre, "6, 4, right, top");
    }
    {
      JButton btnRemoveActor = new JButton("Remove Actor");
      btnRemoveActor.setMargin(new Insets(2, 2, 2, 2));
      btnRemoveActor.setAction(actionRemoveActor);
      btnRemoveActor.setIcon(new ImageIcon(MovieEditor.class.getResource("/org/tinymediamanager/ui/images/Remove.png")));
      details2Panel.add(btnRemoveActor, "2,6, right, top");
    }

    {
      JButton btnRemoveGenre = new JButton("");
      btnRemoveGenre.setAction(actionRemoveGenre);
      btnRemoveGenre.setMargin(new Insets(2, 2, 2, 2));
      btnRemoveGenre.setIcon(new ImageIcon(MovieEditor.class.getResource("/org/tinymediamanager/ui/images/Remove.png")));
      details2Panel.add(btnRemoveGenre, "6, 6, right, top");
    }
    {
      cbGenres = new JComboBox(MediaGenres.values());
      details2Panel.add(cbGenres, "8,8");
    }

    {
      JLabel lblTrailer = new JLabel("Trailer");
      details2Panel.add(lblTrailer, "2, 10, right, default");
    }
    {
      JScrollPane scrollPaneTrailer = new JScrollPane();
      details2Panel.add(scrollPaneTrailer, "4, 10, 5, 5");
      tableTrailer = new JTable();
      scrollPaneTrailer.setViewportView(tableTrailer);
    }
    {
      JButton btnAddTrailer = new JButton("");
      btnAddTrailer.setAction(action);
      btnAddTrailer.setIcon(new ImageIcon(MovieEditor.class.getResource("/org/tinymediamanager/ui/images/Add.png")));
      btnAddTrailer.setMargin(new Insets(2, 2, 2, 2));
      details2Panel.add(btnAddTrailer, "2, 12, right, top");
    }
    {
      JButton btnRemoveTrailer = new JButton("");
      btnRemoveTrailer.setAction(action_1);
      btnRemoveTrailer.setIcon(new ImageIcon(MovieEditor.class.getResource("/org/tinymediamanager/ui/images/Remove.png")));
      btnRemoveTrailer.setMargin(new Insets(2, 2, 2, 2));
      details2Panel.add(btnRemoveTrailer, "2, 14, right, top");
    }
    {
      JLabel lblTags = new JLabel("Tags");
      details2Panel.add(lblTags, "2, 16, right, default");
    }
    {
      JScrollPane scrollPaneTags = new JScrollPane();
      details2Panel.add(scrollPaneTags, "4, 16, 1, 5");
      listTags = new JList();
      scrollPaneTags.setViewportView(listTags);
    }
    {
      JButton btnAddTag = new JButton("");
      btnAddTag.setAction(action_2);
      btnAddTag.setIcon(new ImageIcon(MovieEditor.class.getResource("/org/tinymediamanager/ui/images/Add.png")));
      btnAddTag.setMargin(new Insets(2, 2, 2, 2));
      details2Panel.add(btnAddTag, "2, 18, right, top");
    }
    {
      JButton btnRemoveTag = new JButton("");
      btnRemoveTag.setAction(action_3);
      btnRemoveTag.setIcon(new ImageIcon(MovieEditor.class.getResource("/org/tinymediamanager/ui/images/Remove.png")));
      btnRemoveTag.setMargin(new Insets(2, 2, 2, 2));
      details2Panel.add(btnRemoveTag, "2, 20, right, top");
    }
    {
      cbTags = new AutocompleteComboBox(movieList.getTagsInMovies().toArray());
      cbTags.setEditable(true);
      details2Panel.add(cbTags, "4, 22");
    }

    /**
     * Button pane
     */
    {
      JPanel buttonPane = new JPanel();
      getContentPane().add(buttonPane, BorderLayout.SOUTH);
      EqualsLayout layout = new EqualsLayout(5);
      layout.setMinWidth(100);
      buttonPane.setLayout(layout);
      {
        JButton okButton = new JButton("OK");
        buttonPane.add(okButton, "2, 1, fill, top");
        okButton.setAction(actionOK);
        okButton.setActionCommand("OK");
        getRootPane().setDefaultButton(okButton);
      }
      {
        JButton cancelButton = new JButton("Cancel");
        buttonPane.add(cancelButton, "4, 1, fill, top");
        cancelButton.setAction(actionCancel);
        cancelButton.setActionCommand("Cancel");
      }
      if (inQueue) {
        JButton btnAbort = new JButton("Abort queue");
        btnAbort.setAction(abortAction);
        buttonPane.add(btnAbort, "6, 1, fill, top");
      }

    }
    initDataBindings();

    {
      lblMoviePath.setText(movie.getPath());
      tfTitle.setText(movie.getName());
      tfOriginalTitle.setText(movie.getOriginalName());
      tfSorttitle.setText(movie.getSortTitle());
      tpTagline.setText(movie.getTagline());
      tfImdbId.setText(movie.getImdbId());
      tfTmdbId.setText(String.valueOf(movie.getTmdbId()));
      tpPlot.setText(movie.getOverview());
      tfDirector.setText(movie.getDirector());
      tfWriter.setText(movie.getWriter());
      lblPoster.setImagePath(movie.getPoster());
      tfProductionCompanies.setText(movie.getProductionCompany());
      spRuntime.setValue(Integer.valueOf(movie.getRuntime()));
      cbWatched.setSelected(movie.isWatched());
      spDateAdded.setValue(movie.getDateAdded());
      tfSpokenLanguages.setText(movie.getSpokenLanguages());

      int year = 0;
      try {
        year = Integer.valueOf(movie.getYear());
      }
      catch (Exception e) {
      }
      spYear.setValue(year);

      spYear.setEditor(new JSpinner.NumberEditor(spYear, "#"));
      spRating.setModel(new SpinnerNumberModel(movie.getRating(), 0.0, 10.0, 0.1));

      for (MovieCast origCast : movie.getActors()) {
        MovieCast actor = new MovieCast();
        actor.setName(origCast.getName());
        actor.setType(origCast.getType());
        actor.setCharacter(origCast.getCharacter());
        actor.setThumb(origCast.getThumb());
        cast.add(actor);
      }

      for (MediaGenres genre : movie.getGenres()) {
        genres.add(genre);
      }

      for (MediaTrailer trailer : movie.getTrailers()) {
        trailers.add(trailer);
      }

      for (String tag : movieToEdit.getTags()) {
        tags.add(tag);
      }

      extrathumbs.addAll(movieToEdit.getExtraThumbs());

      cbCertification.setSelectedItem(movie.getCertification());

      cbMovieSet.addItem("");
      for (MovieSet movieSet : movieList.getMovieSetList()) {
        cbMovieSet.addItem(movieSet);
        if (movieToEdit.getMovieSet() == movieSet) {
          cbMovieSet.setSelectedItem(movieSet);
        }
      }

      toggleSorttitle();
    }

    // adjust table columns
    TableColumnAdjuster tableColumnAdjuster = new TableColumnAdjuster(tableTrailer);
    tableColumnAdjuster.setColumnDataIncluded(true);
    tableColumnAdjuster.setColumnHeaderIncluded(true);
    tableColumnAdjuster.adjustColumns();

    // implement listener to simulate button group
    tableTrailer.getModel().addTableModelListener(new TableModelListener() {
      @Override
      public void tableChanged(TableModelEvent arg0) {
        // click on the checkbox
        if (arg0.getColumn() == 0) {
          int row = arg0.getFirstRow();
          MediaTrailer changedTrailer = trailers.get(row);
          // if flag inNFO was changed, change all other trailers flags
          if (changedTrailer.getInNfo()) {
            for (MediaTrailer trailer : trailers) {
              if (trailer != changedTrailer) {
                trailer.setInNfo(Boolean.FALSE);
              }
            }
          }
        }
      }
    });
  }

  private void toggleSorttitle() {
    Object obj = cbMovieSet.getSelectedItem();
    if (obj instanceof String) {
      tfSorttitle.setEnabled(true);
    }
    else {
      tfSorttitle.setEnabled(false);
    }
  }

  /**
   * The Class SwingAction.
   */
  private class SwingAction extends AbstractAction {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /**
     * Instantiates a new swing action.
     */
    public SwingAction() {
      putValue(NAME, "Ok");
      putValue(SHORT_DESCRIPTION, "Change movie");
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent e) {
      movieToEdit.setName(tfTitle.getText());
      movieToEdit.setOriginalName(tfOriginalTitle.getText());
      movieToEdit.setTagline(tpTagline.getText());
      movieToEdit.setYear(String.valueOf(spYear.getValue()));
      movieToEdit.setRuntime((Integer) spRuntime.getValue());
      movieToEdit.setImdbId(tfImdbId.getText());
      movieToEdit.setWatched(cbWatched.isSelected());
      movieToEdit.setSpokenLanguages(tfSpokenLanguages.getText());
      try {
        movieToEdit.setTmdbId(Integer.parseInt(tfTmdbId.getText()));
      }
      catch (NumberFormatException ex) {
        JOptionPane.showMessageDialog(null, "wrong format: TMDB Id");
        return;
      }

      Object certification = cbCertification.getSelectedItem();
      if (certification instanceof Certification) {
        movieToEdit.setCertification((Certification) certification);
      }

      if (!StringUtils.isEmpty(lblPoster.getImageUrl()) && lblPoster.getImageUrl() != movieToEdit.getPosterUrl()) {
        movieToEdit.setPosterUrl(lblPoster.getImageUrl());
        movieToEdit.writeImages(true, false);
      }

      if (!StringUtils.isEmpty(lblFanart.getImageUrl()) && lblFanart.getImageUrl() != movieToEdit.getFanartUrl()) {
        movieToEdit.setFanartUrl(lblFanart.getImageUrl());
        movieToEdit.writeImages(false, true);
      }

      // set extrathumbs
      if (extrathumbs.size() != movieToEdit.getExtraThumbs().size() || !extrathumbs.containsAll(movieToEdit.getExtraThumbs())
          || !movieToEdit.getExtraThumbs().containsAll(extrathumbs)) {
        movieToEdit.downloadExtraThumbs(extrathumbs);
      }

      movieToEdit.setDirector(tfDirector.getText());
      movieToEdit.setWriter(tfWriter.getText());
      movieToEdit.setProductionCompany(tfProductionCompanies.getText());

      // two way sync of actors
      // movieToEdit.removeAllActors();
      // for (MovieCast actor : cast) {
      // movieToEdit.addToCast(actor);
      // }
      movieToEdit.setActors(cast);

      // two way sync of genres (so selection won't get killed
      // movieToEdit.removeAllGenres();
      // for (MediaGenres genre : genres) {
      // movieToEdit.addGenre(genre);
      // }
      movieToEdit.setGenres(genres);

      movieToEdit.removeAllTrailers();
      for (MediaTrailer trailer : trailers) {
        movieToEdit.addTrailer(trailer);
      }

      // two way sync of tags
      // movieToEdit.clearTags();
      // for (String tag : tags) {
      // movieToEdit.addToTags(tag);
      // }
      movieToEdit.setTags(tags);

      movieToEdit.setDateAdded((Date) spDateAdded.getValue());

      // movie set
      Object obj = cbMovieSet.getSelectedItem();
      if (obj instanceof String) {
        movieToEdit.removeFromMovieSet();
        movieToEdit.setSortTitle(tfSorttitle.getText());
      }
      if (obj instanceof MovieSet) {
        MovieSet movieSet = (MovieSet) obj;

        if (movieToEdit.getMovieSet() != movieSet) {
          movieToEdit.removeFromMovieSet();
          movieToEdit.setMovieSet(movieSet);
          movieSet.addMovie(movieToEdit);
        }

        movieToEdit.setSortTitleFromMovieSet();
      }

      double tempRating = (Double) spRating.getValue();
      float rating = (float) tempRating;
      if (movieToEdit.getRating() != rating) {
        movieToEdit.setRating(rating);
        movieToEdit.setVotes(1);
      }

      movieToEdit.saveToDb();
      movieToEdit.writeNFO();
      setVisible(false);
      dispose();
    }
  }

  /**
   * The Class SwingAction_1.
   */
  private class SwingAction_1 extends AbstractAction {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /**
     * Instantiates a new swing action_1.
     */
    public SwingAction_1() {
      putValue(NAME, "Cancel");
      putValue(SHORT_DESCRIPTION, "Discard changes");
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent e) {
      setVisible(false);
      dispose();
    }
  }

  /**
   * The Class SwingAction_4.
   */
  private class SwingAction_4 extends AbstractAction {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /**
     * Instantiates a new swing action_4.
     */
    public SwingAction_4() {
      putValue(SHORT_DESCRIPTION, "Add a new Actor");
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent e) {
      MovieCast actor = new MovieCast("unknown actor", "unknown role");
      cast.add(0, actor);
    }
  }

  /**
   * The Class SwingAction_5.
   */
  private class SwingAction_5 extends AbstractAction {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /**
     * Instantiates a new swing action_5.
     */
    public SwingAction_5() {
      putValue(SHORT_DESCRIPTION, "Remove actor");
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent e) {
      int row = tableActors.getSelectedRow();
      row = tableActors.convertRowIndexToModel(row);
      cast.remove(row);
    }
  }

  /**
   * The Class SwingAction_2.
   */
  private class SwingAction_2 extends AbstractAction {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /**
     * Instantiates a new swing action_2.
     */
    public SwingAction_2() {
      // putValue(NAME, "SwingAction_2");
      putValue(SHORT_DESCRIPTION, "Add genre");
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent e) {
      MediaGenres newGenre = (MediaGenres) cbGenres.getSelectedItem();
      // add genre if it is not already in the list
      if (!genres.contains(newGenre)) {
        genres.add(newGenre);
      }
    }
  }

  /**
   * The Class SwingAction_3.
   */
  private class SwingAction_3 extends AbstractAction {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /**
     * Instantiates a new swing action_3.
     */
    public SwingAction_3() {
      // putValue(NAME, "SwingAction_3");
      putValue(SHORT_DESCRIPTION, "Remove genre");
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent e) {
      MediaGenres newGenre = (MediaGenres) listGenres.getSelectedValue();
      // remove genre
      if (newGenre != null) {
        genres.remove(newGenre);
      }
    }
  }

  /**
   * The Class SwingAction_6.
   */
  private class SwingAction_6 extends AbstractAction {

    /**
     * Instantiates a new swing action_6.
     */
    public SwingAction_6() {
      // putValue(NAME, "SwingAction_6");
      putValue(SHORT_DESCRIPTION, "Add a trailer");
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent e) {
      MediaTrailer trailer = new MediaTrailer();
      trailer.setName("unknown");
      trailer.setProvider("unknown");
      trailer.setQuality("unknown");
      trailer.setUrl("http://");
      trailers.add(0, trailer);
    }
  }

  /**
   * The Class SwingAction_7.
   */
  private class SwingAction_7 extends AbstractAction {

    /**
     * Instantiates a new swing action_7.
     */
    public SwingAction_7() {
      // putValue(NAME, "SwingAction_7");
      putValue(SHORT_DESCRIPTION, "Remove selected trailer");
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent e) {
      int row = tableTrailer.getSelectedRow();
      row = tableTrailer.convertRowIndexToModel(row);
      trailers.remove(row);
    }
  }

  protected void initDataBindings() {
    JTableBinding<MovieCast, List<MovieCast>, JTable> jTableBinding = SwingBindings.createJTableBinding(UpdateStrategy.READ, cast, tableActors);
    //
    BeanProperty<MovieCast, String> movieCastBeanProperty = BeanProperty.create("name");
    jTableBinding.addColumnBinding(movieCastBeanProperty).setColumnName("Name");
    //
    BeanProperty<MovieCast, String> movieCastBeanProperty_1 = BeanProperty.create("character");
    jTableBinding.addColumnBinding(movieCastBeanProperty_1).setColumnName("Role");
    //
    jTableBinding.bind();
    //
    JListBinding<MediaGenres, List<MediaGenres>, JList> jListBinding = SwingBindings.createJListBinding(UpdateStrategy.READ, genres, listGenres);
    jListBinding.bind();
    //
    JTableBinding<MediaTrailer, List<MediaTrailer>, JTable> jTableBinding_1 = SwingBindings.createJTableBinding(UpdateStrategy.READ, trailers,
        tableTrailer);
    //
    BeanProperty<MediaTrailer, Boolean> trailerBeanProperty = BeanProperty.create("inNfo");
    jTableBinding_1.addColumnBinding(trailerBeanProperty).setColumnName("NFO").setColumnClass(Boolean.class);
    //
    BeanProperty<MediaTrailer, String> trailerBeanProperty_1 = BeanProperty.create("name");
    jTableBinding_1.addColumnBinding(trailerBeanProperty_1).setColumnName("Name");
    //
    BeanProperty<MediaTrailer, String> trailerBeanProperty_2 = BeanProperty.create("provider");
    jTableBinding_1.addColumnBinding(trailerBeanProperty_2).setColumnName("Source");
    //
    BeanProperty<MediaTrailer, String> trailerBeanProperty_3 = BeanProperty.create("quality");
    jTableBinding_1.addColumnBinding(trailerBeanProperty_3).setColumnName("Quality");
    //
    BeanProperty<MediaTrailer, String> trailerBeanProperty_4 = BeanProperty.create("url");
    jTableBinding_1.addColumnBinding(trailerBeanProperty_4).setColumnName("Url");
    //
    jTableBinding_1.bind();
    //
    BeanProperty<MovieList, List<String>> movieListBeanProperty = BeanProperty.create("tagsInMovies");
    JComboBoxBinding<String, MovieList, JComboBox> jComboBinding = SwingBindings.createJComboBoxBinding(UpdateStrategy.READ, movieList,
        movieListBeanProperty, cbTags);
    jComboBinding.bind();
    //
    JListBinding<String, List<String>, JList> jListBinding_1 = SwingBindings.createJListBinding(UpdateStrategy.READ, tags, listTags);
    jListBinding_1.bind();
  }

  /**
   * Shows the dialog and returns whether the work on the queue should be
   * continued.
   * 
   * @return true, if successful
   */
  public boolean showDialog() {
    setVisible(true);
    return continueQueue;
  }

  private class SwingAction_8 extends AbstractAction {
    public SwingAction_8() {
      // putValue(NAME, "SwingAction_8");
      putValue(SHORT_DESCRIPTION, "add tag");
    }

    public void actionPerformed(ActionEvent e) {
      String newTag = (String) cbTags.getSelectedItem();
      boolean tagFound = false;

      // search if this tag already has been added
      for (String tag : tags) {
        if (tag.equals(newTag)) {
          tagFound = true;
          break;
        }
      }

      // add tag
      if (!tagFound) {
        tags.add(newTag);
      }
    }
  }

  private class SwingAction_9 extends AbstractAction {
    public SwingAction_9() {
      // putValue(NAME, "SwingAction_9");
      putValue(SHORT_DESCRIPTION, "Remove selected tag");
    }

    public void actionPerformed(ActionEvent e) {
      String tag = (String) listTags.getSelectedValue();
      tags.remove(tag);
    }
  }

  private class ToggleMovieSetAction extends AbstractAction {
    public ToggleMovieSetAction() {
    }

    public void actionPerformed(ActionEvent e) {
      toggleSorttitle();
    }
  }

  private class SwingAction_10 extends AbstractAction {
    public SwingAction_10() {
      putValue(NAME, "Abort queue");
      putValue(SHORT_DESCRIPTION, "Abort editing all selected movies");
    }

    public void actionPerformed(ActionEvent e) {
      continueQueue = false;
      setVisible(false);
      dispose();
    }
  }
}
