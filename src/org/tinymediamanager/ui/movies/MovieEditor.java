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
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.ArrayList;
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
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JTextPane;
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
import org.tinymediamanager.scraper.Certification;
import org.tinymediamanager.scraper.MediaGenres;
import org.tinymediamanager.scraper.Trailer;
import org.tinymediamanager.ui.AutocompleteComboBox;
import org.tinymediamanager.ui.ImageChooser;
import org.tinymediamanager.ui.ImageChooser.ImageType;
import org.tinymediamanager.ui.ImageLabel;
import org.tinymediamanager.ui.TableColumnAdjuster;

import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

// TODO: Auto-generated Javadoc
/**
 * The Class MovieEditor.
 */
public class MovieEditor extends JDialog {

  /** The Constant serialVersionUID. */
  private static final long serialVersionUID  = 1L;

  /** The content panel. */
  private final JPanel      contentPanel      = new JPanel();

  /** The movie to edit. */
  private Movie             movieToEdit;

  /** The movielist. */
  private MovieList         movieList         = MovieList.getInstance();

  /** The tf title. */
  private JTextField        tfTitle;

  /** The tf original title. */
  private JTextField        tfOriginalTitle;

  /** The tf year. */
  private JSpinner          spYear;

  /** The tp plot. */
  private JTextPane         tpPlot;

  /** The tf director. */
  private JTextField        tfDirector;

  /** The table. */
  private JTable            tableActors;

  /** The lbl movie path. */
  private JLabel            lblMoviePath;

  /** The lbl poster. */
  private ImageLabel        lblPoster;

  /** The lbl fanart. */
  private ImageLabel        lblFanart;

  /** The cast. */
  private List<MovieCast>   cast              = ObservableCollections.observableList(new ArrayList<MovieCast>());

  /** The genres. */
  private List<MediaGenres> genres            = ObservableCollections.observableList(new ArrayList<MediaGenres>());

  /** The trailers. */
  private List<Trailer>     trailers          = ObservableCollections.observableList(new ArrayList<Trailer>());

  /** The tags. */
  private List<String>      tags              = ObservableCollections.observableList(new ArrayList<String>());

  /** The action ok. */
  private final Action      actionOK          = new SwingAction();

  /** The action cancel. */
  private final Action      actionCancel      = new SwingAction_1();

  /** The action add actor. */
  private final Action      actionAddActor    = new SwingAction_4();

  /** The action remove actor. */
  private final Action      actionRemoveActor = new SwingAction_5();

  /** The tf writer. */
  private JTextField        tfWriter;

  /** The sp runtime. */
  private JSpinner          spRuntime;

  /** The tf production companies. */
  private JTextField        tfProductionCompanies;

  /** The list genres. */
  private JList             listGenres;

  /** The action add genre. */
  private final Action      actionAddGenre    = new SwingAction_2();

  /** The action remove genre. */
  private final Action      actionRemoveGenre = new SwingAction_3();

  /** The cb genres. */
  private JComboBox         cbGenres;

  /** The sp rating. */
  private JSpinner          spRating;

  /** The cb certification. */
  private JComboBox         cbCertification;

  /** The tf imdb id. */
  private JTextField        tfImdbId;

  /** The tf tmdb id. */
  private JTextField        tfTmdbId;

  /** The lbl imdb id. */
  private JLabel            lblImdbId;

  /** The lbl tmdb id. */
  private JLabel            lblTmdbId;

  /** The lbl watched. */
  private JLabel            lblWatched;

  /** The cb watched. */
  private JCheckBox         cbWatched;

  /** The tf tagline. */
  private JTextField        tfTagline;

  /** The table trailer. */
  private JTable            tableTrailer;

  /** The action. */
  private final Action      action            = new SwingAction_6();

  /** The action_1. */
  private final Action      action_1          = new SwingAction_7();
  private JComboBox         cbTags;
  private JList             listTags;
  private final Action      action_2          = new SwingAction_8();
  private final Action      action_3          = new SwingAction_9();

  /**
   * Create the dialog.
   * 
   * @param movie
   *          the movie
   */
  public MovieEditor(Movie movie) {
    setModal(true);
    setIconImage(Globals.logo);
    setTitle("Edit Movie");
    movieToEdit = movie;
    setBounds(5, 5, 944, 740);
    getContentPane().setLayout(new BorderLayout());
    contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
    getContentPane().add(contentPanel, BorderLayout.CENTER);
    contentPanel.setLayout(new FormLayout(new ColumnSpec[] { FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("max(40dlu;default)"),
        FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("50px:grow"), FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("50px"),
        FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("100px:grow"), FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("default:grow"),
        FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("100px:grow"), FormFactory.UNRELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC,
        FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("250px:grow"), }, new RowSpec[] { FormFactory.RELATED_GAP_ROWSPEC,
        FormFactory.DEFAULT_ROWSPEC, FormFactory.NARROW_LINE_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.NARROW_LINE_GAP_ROWSPEC,
        FormFactory.DEFAULT_ROWSPEC, FormFactory.NARROW_LINE_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.NARROW_LINE_GAP_ROWSPEC,
        FormFactory.DEFAULT_ROWSPEC, FormFactory.NARROW_LINE_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.NARROW_LINE_GAP_ROWSPEC,
        FormFactory.DEFAULT_ROWSPEC, FormFactory.NARROW_LINE_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.NARROW_LINE_GAP_ROWSPEC,
        RowSpec.decode("top:max(150px;default)"), FormFactory.NARROW_LINE_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
        FormFactory.NARROW_LINE_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.NARROW_LINE_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
        FormFactory.NARROW_LINE_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.NARROW_LINE_GAP_ROWSPEC, RowSpec.decode("fill:default"),
        FormFactory.NARROW_LINE_GAP_ROWSPEC, RowSpec.decode("fill:30px:grow"), FormFactory.NARROW_LINE_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
        FormFactory.NARROW_LINE_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
        FormFactory.NARROW_LINE_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
        FormFactory.RELATED_GAP_ROWSPEC, RowSpec.decode("default:grow"), }));
    {
      lblMoviePath = new JLabel("");
      contentPanel.add(lblMoviePath, "2, 2, 13, 1");
    }
    {
      JLabel lblTitle = new JLabel("Title");
      contentPanel.add(lblTitle, "2, 4, right, default");
    }
    {
      tfTitle = new JTextField();
      contentPanel.add(tfTitle, "4, 4, 9, 1, fill, default");
      tfTitle.setColumns(10);
    }
    {
      // JLabel lblPoster = new JLabel("");
      lblPoster = new ImageLabel();
      lblPoster.addMouseListener(new MouseAdapter() {
        @Override
        public void mouseClicked(MouseEvent e) {
          ImageChooser dialog = new ImageChooser(movieToEdit.getImdbId(), movieToEdit.getTmdbId(), ImageType.POSTER, lblPoster);
          dialog.setVisible(true);
        }
      });
      lblPoster.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
      contentPanel.add(lblPoster, "14, 4, 3, 15, fill, fill");
    }
    {
      JLabel lblOriginalTitle = new JLabel("Originaltitle");
      contentPanel.add(lblOriginalTitle, "2, 6, right, default");
    }
    {
      tfOriginalTitle = new JTextField();
      contentPanel.add(tfOriginalTitle, "4, 6, 9, 1, fill, top");
      tfOriginalTitle.setColumns(10);
    }
    {
      JLabel lblTagline = new JLabel("Tagline");
      contentPanel.add(lblTagline, "2, 8, right, default");
    }
    {
      tfTagline = new JTextField();
      contentPanel.add(tfTagline, "4, 8, 9, 1, fill, default");
      tfTagline.setColumns(10);
    }
    {
      JLabel lblYear = new JLabel("Year");
      contentPanel.add(lblYear, "2, 10, right, default");
    }
    {
      spYear = new JSpinner();
      contentPanel.add(spYear, "4, 10, fill, top");
    }
    {
      JLabel lblRuntime = new JLabel("Runtime");
      contentPanel.add(lblRuntime, "8, 10, right, default");
    }
    {
      spRuntime = new JSpinner();
      contentPanel.add(spRuntime, "10, 10, fill, default");
    }
    {
      JLabel lblMin = new JLabel("min");
      contentPanel.add(lblMin, "12, 10");
    }
    {
      JLabel lblRating = new JLabel("Rating");
      contentPanel.add(lblRating, "2, 12, right, default");
    }
    {
      spRating = new JSpinner();
      contentPanel.add(spRating, "4, 12");
    }
    {
      JLabel lblCertification = new JLabel("Certification");
      contentPanel.add(lblCertification, "8, 12, right, default");
    }
    {
      cbCertification = new JComboBox();
      for (Certification cert : Certification.getCertificationsforCountry(Globals.settings.getCertificationCountry())) {
        cbCertification.addItem(cert);
      }
      contentPanel.add(cbCertification, "10, 12, 3, 1, fill, default");
    }
    {
      lblImdbId = new JLabel("IMDB Id");
      contentPanel.add(lblImdbId, "2, 14, right, default");
    }
    {
      tfImdbId = new JTextField();
      lblImdbId.setLabelFor(tfImdbId);
      contentPanel.add(tfImdbId, "4, 14, 3, 1, fill, default");
      tfImdbId.setColumns(10);
    }
    {
      lblTmdbId = new JLabel("TMDB Id");
      contentPanel.add(lblTmdbId, "8, 14, right, default");
    }
    {
      tfTmdbId = new JTextField();
      lblTmdbId.setLabelFor(tfTmdbId);
      contentPanel.add(tfTmdbId, "10, 14, 3, 1, fill, default");
      tfTmdbId.setColumns(10);
    }
    {
      lblWatched = new JLabel("Watched");
      contentPanel.add(lblWatched, "2, 16, right, default");
    }
    {
      cbWatched = new JCheckBox("");
      lblWatched.setLabelFor(cbWatched);
      contentPanel.add(cbWatched, "4, 16");
    }
    {
      JLabel lblPlot = new JLabel("Plot");
      contentPanel.add(lblPlot, "2, 18, right, top");
    }
    {
      JScrollPane scrollPane = new JScrollPane();
      contentPanel.add(scrollPane, "4, 18, 9, 1, fill, fill");
      {
        tpPlot = new JTextPane();
        scrollPane.setViewportView(tpPlot);
      }
    }
    {
      JLabel lblDirector = new JLabel("Director");
      contentPanel.add(lblDirector, "2, 20, right, default");
    }
    {
      tfDirector = new JTextField();
      contentPanel.add(tfDirector, "4, 20, 9, 1, fill, top");
      tfDirector.setColumns(10);
    }
    {
      // JLabel lblFanart = new JLabel("");
      lblFanart = new ImageLabel();
      lblFanart.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
      lblFanart.addMouseListener(new MouseAdapter() {
        @Override
        public void mouseClicked(MouseEvent e) {
          ImageChooser dialog = new ImageChooser(movieToEdit.getImdbId(), movieToEdit.getTmdbId(), ImageType.FANART, lblFanart);
          dialog.setVisible(true);
        }
      });
      contentPanel.add(lblFanart, "14, 20, 3, 11, fill, fill");
    }
    lblFanart.setImagePath(movie.getFanart());
    {
      JLabel lblWriter = new JLabel("Writer");
      contentPanel.add(lblWriter, "2, 22, right, default");
    }
    {
      tfWriter = new JTextField();
      contentPanel.add(tfWriter, "4, 22, 9, 1, fill, top");
      tfWriter.setColumns(10);
    }
    {
      JLabel lblCompany = new JLabel("Production");
      contentPanel.add(lblCompany, "2, 24, right, default");
    }
    {
      tfProductionCompanies = new JTextField();
      contentPanel.add(tfProductionCompanies, "4, 24, 9, 1, fill, default");
      tfProductionCompanies.setColumns(10);
    }
    {
      JLabel lblActors = new JLabel("Actors");
      contentPanel.add(lblActors, "2, 26, right, default");
    }
    {
      JScrollPane scrollPane = new JScrollPane();
      contentPanel.add(scrollPane, "4, 26, 5, 7, fill, fill");
      {
        tableActors = new JTable();
        scrollPane.setViewportView(tableActors);
      }
    }
    {
      JLabel lblGenres = new JLabel("Genres");
      contentPanel.add(lblGenres, "10, 26, right, default");
    }
    {
      JButton btnAddActor = new JButton("Add Actor");
      btnAddActor.setMargin(new Insets(2, 2, 2, 2));
      btnAddActor.setAction(actionAddActor);
      btnAddActor.setIcon(new ImageIcon(MovieEditor.class.getResource("/org/tinymediamanager/ui/images/Add.png")));
      contentPanel.add(btnAddActor, "2, 28, right, top");
    }
    {
      JScrollPane scrollPane = new JScrollPane();
      contentPanel.add(scrollPane, "12, 26, 1, 5, fill, fill");
      {
        listGenres = new JList();
        scrollPane.setViewportView(listGenres);
      }
    }
    {
      JButton btnAddGenre = new JButton("");
      btnAddGenre.setAction(actionAddGenre);
      btnAddGenre.setIcon(new ImageIcon(MovieEditor.class.getResource("/org/tinymediamanager/ui/images/Add.png")));
      btnAddGenre.setMargin(new Insets(2, 2, 2, 2));
      contentPanel.add(btnAddGenre, "10, 28, right, top");
    }
    {
      JButton btnRemoveActor = new JButton("Remove Actor");
      btnRemoveActor.setMargin(new Insets(2, 2, 2, 2));
      btnRemoveActor.setAction(actionRemoveActor);
      btnRemoveActor.setIcon(new ImageIcon(MovieEditor.class.getResource("/org/tinymediamanager/ui/images/Remove.png")));
      contentPanel.add(btnRemoveActor, "2, 30, right, top");
    }
    {
      JPanel buttonPane = new JPanel();
      getContentPane().add(buttonPane, BorderLayout.SOUTH);
      buttonPane.setLayout(new FormLayout(new ColumnSpec[] { ColumnSpec.decode("200px:grow"), ColumnSpec.decode("100px"),
          FormFactory.LABEL_COMPONENT_GAP_COLSPEC, ColumnSpec.decode("100px"), FormFactory.RELATED_GAP_COLSPEC, }, new RowSpec[] {
          FormFactory.LINE_GAP_ROWSPEC, RowSpec.decode("25px"), FormFactory.RELATED_GAP_ROWSPEC, }));
      {
        JButton okButton = new JButton("OK");
        okButton.setAction(actionOK);
        okButton.setActionCommand("OK");
        buttonPane.add(okButton, "2, 2, fill, top");
        getRootPane().setDefaultButton(okButton);
      }
      {
        JButton cancelButton = new JButton("Cancel");
        cancelButton.setAction(actionCancel);
        cancelButton.setActionCommand("Cancel");
        buttonPane.add(cancelButton, "4, 2, fill, top");
      }
    }
    {
      JButton btnRemoveGenre = new JButton("");
      btnRemoveGenre.setAction(actionRemoveGenre);
      btnRemoveGenre.setMargin(new Insets(2, 2, 2, 2));
      btnRemoveGenre.setIcon(new ImageIcon(MovieEditor.class.getResource("/org/tinymediamanager/ui/images/Remove.png")));
      contentPanel.add(btnRemoveGenre, "10, 30, right, top");
    }
    {
      cbGenres = new JComboBox(MediaGenres.values());
      contentPanel.add(cbGenres, "12, 32");
    }

    {
      lblMoviePath.setText(movie.getPath() + File.separator + movie.getMovieFiles().get(0));
      tfTitle.setText(movie.getName());
      tfOriginalTitle.setText(movie.getOriginalName());
      tfTagline.setText(movie.getTagline());
      tfImdbId.setText(movie.getImdbId());
      tfTmdbId.setText(String.valueOf(movie.getTmdbId()));
      tpPlot.setText(movie.getOverview());
      tfDirector.setText(movie.getDirector());
      tfWriter.setText(movie.getWriter());
      lblPoster.setImagePath(movie.getPoster());
      tfProductionCompanies.setText(movie.getProductionCompany());
      spRuntime.setValue(new Integer(movie.getRuntime()));
      cbWatched.setSelected(movie.isWatched());

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

      for (Trailer trailer : movie.getTrailers()) {
        trailers.add(trailer);
      }

      for (String tag : movieToEdit.getTags()) {
        tags.add(tag);
      }

      cbCertification.setSelectedItem(movie.getCertification());
    }
    {
      JLabel lblTrailer = new JLabel("Trailer");
      contentPanel.add(lblTrailer, "2, 34, right, top");
    }
    {
      JScrollPane scrollPane = new JScrollPane();
      contentPanel.add(scrollPane, "4, 34, 9, 7, fill, fill");
      {
        tableTrailer = new JTable();
        scrollPane.setViewportView(tableTrailer);
      }
      {
        JLabel lblTags = new JLabel("Tags");
        contentPanel.add(lblTags, "14, 34");
      }
      {
        JScrollPane scrollPane_1 = new JScrollPane();
        contentPanel.add(scrollPane_1, "16, 34, 1, 5, fill, fill");
        {
          listTags = new JList();
          scrollPane_1.setViewportView(listTags);
        }
      }
      {
        JButton btnAddTrailer = new JButton("");
        btnAddTrailer.setAction(action);
        btnAddTrailer.setIcon(new ImageIcon(MovieEditor.class.getResource("/org/tinymediamanager/ui/images/Add.png")));
        btnAddTrailer.setMargin(new Insets(2, 2, 2, 2));
        contentPanel.add(btnAddTrailer, "2, 36, right, top");
      }
      {
        JButton btnAddTag = new JButton("");
        btnAddTag.setAction(action_2);
        btnAddTag.setIcon(new ImageIcon(MovieEditor.class.getResource("/org/tinymediamanager/ui/images/Add.png")));
        btnAddTag.setMargin(new Insets(2, 2, 2, 2));
        contentPanel.add(btnAddTag, "14, 36, right, top");
      }
      {
        JButton btnRemoveTrailer = new JButton("");
        btnRemoveTrailer.setAction(action_1);
        btnRemoveTrailer.setIcon(new ImageIcon(MovieEditor.class.getResource("/org/tinymediamanager/ui/images/Remove.png")));
        btnRemoveTrailer.setMargin(new Insets(2, 2, 2, 2));
        contentPanel.add(btnRemoveTrailer, "2, 38, right, top");
      }
    }
    initDataBindings();

    // adjust table columns
    TableColumnAdjuster tableColumnAdjuster = new TableColumnAdjuster(tableTrailer);
    {
      JButton btnRemoveTag = new JButton("");
      btnRemoveTag.setAction(action_3);
      btnRemoveTag.setIcon(new ImageIcon(MovieEditor.class.getResource("/org/tinymediamanager/ui/images/Remove.png")));
      btnRemoveTag.setMargin(new Insets(2, 2, 2, 2));
      contentPanel.add(btnRemoveTag, "14, 38, right, top");
    }
    {
      cbTags = new AutocompleteComboBox(movieList.getTagsInMovies().toArray());
      cbTags.setEditable(true);
      contentPanel.add(cbTags, "16, 40, fill, top");
    }
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
          Trailer changedTrailer = trailers.get(row);
          // if flag inNFO was changed, change all other trailers flags
          if (changedTrailer.getInNfo()) {
            for (Trailer trailer : trailers) {
              if (trailer != changedTrailer) {
                trailer.setInNfo(Boolean.FALSE);
              }
            }
          }
        }
      }
    });
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
      movieToEdit.setTagline(tfTagline.getText());
      movieToEdit.setYear(String.valueOf(spYear.getValue()));
      movieToEdit.setRuntime((Integer) spRuntime.getValue());
      movieToEdit.setImdbId(tfImdbId.getText());
      movieToEdit.setWatched(cbWatched.isSelected());
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

      movieToEdit.setDirector(tfDirector.getText());
      movieToEdit.setWriter(tfWriter.getText());
      movieToEdit.setProductionCompany(tfProductionCompanies.getText());

      movieToEdit.removeAllActors();
      for (MovieCast actor : cast) {
        movieToEdit.addToCast(actor);
      }

      movieToEdit.removeAllGenres();
      for (MediaGenres genre : genres) {
        movieToEdit.addGenre(genre);
      }

      movieToEdit.removeAllTrailers();
      for (Trailer trailer : trailers) {
        movieToEdit.addTrailer(trailer);
      }

      movieToEdit.clearTags();
      for (String tag : tags) {
        movieToEdit.addToTags(tag);
      }

      movieToEdit.saveToDb();
      movieToEdit.writeNFO();

      setVisible(false);
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
      Trailer trailer = new Trailer();
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
    JTableBinding<Trailer, List<Trailer>, JTable> jTableBinding_1 = SwingBindings.createJTableBinding(UpdateStrategy.READ, trailers, tableTrailer);
    //
    BeanProperty<Trailer, Boolean> trailerBeanProperty = BeanProperty.create("inNfo");
    jTableBinding_1.addColumnBinding(trailerBeanProperty).setColumnName("NFO").setColumnClass(Boolean.class);
    //
    BeanProperty<Trailer, String> trailerBeanProperty_1 = BeanProperty.create("name");
    jTableBinding_1.addColumnBinding(trailerBeanProperty_1).setColumnName("Name");
    //
    BeanProperty<Trailer, String> trailerBeanProperty_2 = BeanProperty.create("provider");
    jTableBinding_1.addColumnBinding(trailerBeanProperty_2).setColumnName("Source");
    //
    BeanProperty<Trailer, String> trailerBeanProperty_3 = BeanProperty.create("quality");
    jTableBinding_1.addColumnBinding(trailerBeanProperty_3).setColumnName("Quality");
    //
    BeanProperty<Trailer, String> trailerBeanProperty_4 = BeanProperty.create("url");
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
}
