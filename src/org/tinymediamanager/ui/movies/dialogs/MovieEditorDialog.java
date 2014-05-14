/*
 * Copyright 2012 - 2014 Manuel Laggner
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
import java.awt.Font;
import java.awt.Frame;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.AbstractAction;
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
import org.tinymediamanager.core.MediaFileType;
import org.tinymediamanager.core.movie.MovieList;
import org.tinymediamanager.core.movie.entities.Movie;
import org.tinymediamanager.core.movie.entities.MovieActor;
import org.tinymediamanager.core.movie.entities.MovieProducer;
import org.tinymediamanager.core.movie.entities.MovieSet;
import org.tinymediamanager.scraper.Certification;
import org.tinymediamanager.scraper.MediaGenres;
import org.tinymediamanager.scraper.MediaTrailer;
import org.tinymediamanager.scraper.MediaType;
import org.tinymediamanager.ui.EqualsLayout;
import org.tinymediamanager.ui.IconManager;
import org.tinymediamanager.ui.MainWindow;
import org.tinymediamanager.ui.TmmWindowSaver;
import org.tinymediamanager.ui.UTF8Control;
import org.tinymediamanager.ui.components.AutocompleteComboBox;
import org.tinymediamanager.ui.components.ImageLabel;
import org.tinymediamanager.ui.dialogs.ImageChooserDialog;
import org.tinymediamanager.ui.dialogs.ImageChooserDialog.ImageType;

import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

/**
 * The Class MovieEditor.
 * 
 * @author Manuel Laggner
 */
public class MovieEditorDialog extends JDialog {
  private static final long                                         serialVersionUID = -286251957529920347L;
  private static final ResourceBundle                               BUNDLE           = ResourceBundle.getBundle("messages", new UTF8Control());      //$NON-NLS-1$
  private static final Date                                         INITIAL_DATE     = new Date(0);

  private Movie                                                     movieToEdit;
  private MovieList                                                 movieList        = MovieList.getInstance();
  private List<MovieActor>                                          cast             = ObservableCollections
                                                                                         .observableList(new ArrayList<MovieActor>());
  private List<MovieProducer>                                       producers        = ObservableCollections
                                                                                         .observableList(new ArrayList<MovieProducer>());
  private List<MediaGenres>                                         genres           = ObservableCollections
                                                                                         .observableList(new ArrayList<MediaGenres>());
  private List<MediaTrailer>                                        trailers         = ObservableCollections
                                                                                         .observableList(new ArrayList<MediaTrailer>());
  private List<String>                                              tags             = ObservableCollections.observableList(new ArrayList<String>());
  private List<String>                                              extrathumbs      = new ArrayList<String>();
  private List<String>                                              extrafanarts     = new ArrayList<String>();
  private boolean                                                   continueQueue    = true;

  private final JPanel                                              details1Panel    = new JPanel();
  private final JPanel                                              details2Panel    = new JPanel();
  private JTextField                                                tfTitle;
  private JTextField                                                tfOriginalTitle;
  private JSpinner                                                  spYear;
  private JTextPane                                                 tpPlot;
  private JTextField                                                tfDirector;
  private JTable                                                    tableActors;
  private JLabel                                                    lblMoviePath;
  private ImageLabel                                                lblPoster;
  private ImageLabel                                                lblFanart;
  private JTextField                                                tfWriter;
  private JSpinner                                                  spRuntime;
  private JTextPane                                                 tfProductionCompanies;
  private JList                                                     listGenres;
  private JComboBox                                                 cbGenres;
  private JSpinner                                                  spRating;
  private JComboBox                                                 cbCertification;
  private JTextField                                                tfImdbId;
  private JTextField                                                tfTmdbId;
  private JCheckBox                                                 cbWatched;
  private JTextPane                                                 tpTagline;
  private JTable                                                    tableTrailer;
  private JTable                                                    tableProducers;
  private JComboBox                                                 cbTags;
  private JList                                                     listTags;
  private JSpinner                                                  spDateAdded;
  private JComboBox                                                 cbMovieSet;
  private JTextField                                                tfSorttitle;
  private JTextField                                                tfSpokenLanguages;
  private JTextField                                                tfCountry;
  private JSpinner                                                  spReleaseDate;
  private JSpinner                                                  spTop250;

  private ImageLabel                                                lblLogo;
  private ImageLabel                                                lblBanner;
  private ImageLabel                                                lblClearart;
  private ImageLabel                                                lblThumb;
  private ImageLabel                                                lblDisc;

  private JTableBinding<MovieActor, List<MovieActor>, JTable>       jTableBinding;
  private JListBinding<MediaGenres, List<MediaGenres>, JList>       jListBinding;
  private JTableBinding<MediaTrailer, List<MediaTrailer>, JTable>   jTableBinding_1;
  private JComboBoxBinding<String, MovieList, JComboBox>            jComboBinding;
  private JListBinding<String, List<String>, JList>                 jListBinding_1;
  private JTableBinding<MovieProducer, List<MovieProducer>, JTable> jTableBinding_2;

  /**
   * Create the dialog.
   * 
   * @param movie
   *          the movie
   * @param inQueue
   *          the in queue
   */
  public MovieEditorDialog(Movie movie, boolean inQueue) {
    super((Frame) null, BUNDLE.getString("movie.edit"), true); //$NON-NLS-1$
    // setModal(true);
    setIconImage(MainWindow.LOGO);
    //    setTitle(BUNDLE.getString("movie.edit")); //$NON-NLS-1$
    setName("movieEditor");
    setBounds(5, 5, 950, 700);
    TmmWindowSaver.loadSettings(this);

    movieToEdit = movie;
    getContentPane().setLayout(new BorderLayout());
    {
      JPanel panelPath = new JPanel();
      getContentPane().add(panelPath, BorderLayout.NORTH);
      panelPath.setLayout(new FormLayout(new ColumnSpec[] { FormFactory.RELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC,
          FormFactory.DEFAULT_COLSPEC, FormFactory.RELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC, FormFactory.RELATED_GAP_COLSPEC, },
          new RowSpec[] { FormFactory.LINE_GAP_ROWSPEC, RowSpec.decode("15px"), FormFactory.RELATED_GAP_ROWSPEC, }));

      JLabel lblMoviePathT = new JLabel(BUNDLE.getString("metatag.path")); //$NON-NLS-1$
      panelPath.add(lblMoviePathT, "2, 2, left, top");

      lblMoviePath = new JLabel("");
      lblMoviePath.setFont(new Font("Dialog", Font.BOLD, 14));
      panelPath.add(lblMoviePath, "5, 2, left, top");
    }

    JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.NORTH);
    tabbedPane.addTab(BUNDLE.getString("metatag.details"), details1Panel); //$NON-NLS-1$
    getContentPane().add(tabbedPane, BorderLayout.CENTER);

    details1Panel.setBorder(new EmptyBorder(5, 5, 5, 5));
    details1Panel.setLayout(new FormLayout(new ColumnSpec[] { FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("max(40dlu;default)"),
        FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("50px:grow"), FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("50px"),
        FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("100px:grow"), FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("default:grow"),
        FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("50px"), FormFactory.UNRELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC,
        FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("250px:grow"), FormFactory.RELATED_GAP_COLSPEC, }, new RowSpec[] {
        FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.NARROW_LINE_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
        FormFactory.NARROW_LINE_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.NARROW_LINE_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
        FormFactory.NARROW_LINE_GAP_ROWSPEC, RowSpec.decode("50px:grow"), FormFactory.NARROW_LINE_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
        FormFactory.NARROW_LINE_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.NARROW_LINE_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
        FormFactory.NARROW_LINE_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.NARROW_LINE_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
        FormFactory.NARROW_LINE_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.NARROW_LINE_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
        FormFactory.NARROW_LINE_GAP_ROWSPEC, RowSpec.decode("top:max(50px;default)"), FormFactory.NARROW_LINE_GAP_ROWSPEC,
        RowSpec.decode("50px:grow(2)"), FormFactory.NARROW_LINE_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.NARROW_LINE_GAP_ROWSPEC,
        FormFactory.DEFAULT_ROWSPEC, FormFactory.NARROW_LINE_GAP_ROWSPEC, RowSpec.decode("50px"), FormFactory.NARROW_LINE_GAP_ROWSPEC,
        RowSpec.decode("fill:30px:grow(2)"), }));

    {
      JLabel lblTitle = new JLabel(BUNDLE.getString("metatag.title")); //$NON-NLS-1$
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
      lblPoster.setAlternativeText(BUNDLE.getString("image.notfound.poster")); //$NON-NLS-1$
      lblPoster.addMouseListener(new MouseAdapter() {
        @Override
        public void mouseClicked(MouseEvent e) {
          ImageChooserDialog dialog = new ImageChooserDialog(movieToEdit.getIds(), ImageType.POSTER, movieList.getArtworkProviders(), lblPoster,
              null, null, MediaType.MOVIE);
          dialog.setLocationRelativeTo(MainWindow.getActiveInstance());
          dialog.setVisible(true);
        }
      });
      lblPoster.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
      details1Panel.add(lblPoster, "14, 4, 3, 23, fill, fill");
    }
    {
      JLabel lblOriginalTitle = new JLabel(BUNDLE.getString("metatag.originaltitle")); //$NON-NLS-1$
      details1Panel.add(lblOriginalTitle, "2, 6, right, default");
    }
    {
      tfOriginalTitle = new JTextField();
      details1Panel.add(tfOriginalTitle, "4, 6, 9, 1, fill, top");
      tfOriginalTitle.setColumns(10);
    }
    {
      JLabel lblSorttitle = new JLabel(BUNDLE.getString("metatag.sorttitle")); //$NON-NLS-1$
      details1Panel.add(lblSorttitle, "2, 8, right, default");
    }
    {
      tfSorttitle = new JTextField();
      details1Panel.add(tfSorttitle, "4, 8, 9, 1, fill, default");
      tfSorttitle.setColumns(10);
    }
    {
      JLabel lblTagline = new JLabel(BUNDLE.getString("metatag.tagline")); //$NON-NLS-1$
      details1Panel.add(lblTagline, "2, 10, right, top");
    }
    {
      JScrollPane scrollPaneTagline = new JScrollPane();
      tpTagline = new JTextPane();
      scrollPaneTagline.setViewportView(tpTagline);
      details1Panel.add(scrollPaneTagline, "4, 10, 9, 1, fill, fill");
    }
    {
      JLabel lblYear = new JLabel(BUNDLE.getString("metatag.year")); //$NON-NLS-1$
      details1Panel.add(lblYear, "2, 12, right, default");
    }
    {
      spYear = new JSpinner();
      details1Panel.add(spYear, "4, 12, fill, top");
    }
    {
      JLabel lblRuntime = new JLabel(BUNDLE.getString("metatag.runtime")); //$NON-NLS-1$
      details1Panel.add(lblRuntime, "8, 12, right, default");
    }
    {
      spRuntime = new JSpinner();
      details1Panel.add(spRuntime, "10, 12, fill, default");
    }
    {
      JLabel lblMin = new JLabel(BUNDLE.getString("metatag.minutes")); //$NON-NLS-1$
      details1Panel.add(lblMin, "12, 12");
    }
    {
      JLabel lblReleaseDate = new JLabel(BUNDLE.getString("metatag.releasedate")); //$NON-NLS-1$
      details1Panel.add(lblReleaseDate, "2, 14, right, default");
    }
    {
      spReleaseDate = new JSpinner(new SpinnerDateModel());
      details1Panel.add(spReleaseDate, "4, 14");
    }
    {
      JLabel lblCertification = new JLabel(BUNDLE.getString("metatag.certification")); //$NON-NLS-1$
      details1Panel.add(lblCertification, "8, 14, right, default");
    }

    {
      cbCertification = new JComboBox();
      details1Panel.add(cbCertification, "10, 14, 3, 1, fill, default");
      for (Certification cert : Certification.getCertificationsforCountry(Globals.settings.getMovieSettings().getCertificationCountry())) {
        cbCertification.addItem(cert);
      }
    }
    {
      JLabel lblRating = new JLabel(BUNDLE.getString("metatag.rating")); //$NON-NLS-1$
      details1Panel.add(lblRating, "2, 16, right, default");
    }
    {
      spRating = new JSpinner();
      details1Panel.add(spRating, "4, 16");
    }
    {
      JLabel lblTop = new JLabel(BUNDLE.getString("metatag.top250")); //$NON-NLS-1$
      details1Panel.add(lblTop, "8, 16, right, default");
    }
    {
      spTop250 = new JSpinner();
      details1Panel.add(spTop250, "10, 16");
    }
    {
      JLabel lblMovieSet = new JLabel(BUNDLE.getString("metatag.movieset")); //$NON-NLS-1$
      details1Panel.add(lblMovieSet, "2, 18, right, default");
    }
    {
      cbMovieSet = new JComboBox();
      cbMovieSet.setAction(new ToggleMovieSetAction()); // $hide$
      details1Panel.add(cbMovieSet, "4, 18, 9, 1, fill, default");
    }
    {
      JLabel lblImdbId = new JLabel(BUNDLE.getString("metatag.imdb")); //$NON-NLS-1$
      details1Panel.add(lblImdbId, "2, 20, right, default");
      tfImdbId = new JTextField();
      lblImdbId.setLabelFor(tfImdbId);
      details1Panel.add(tfImdbId, "4, 20, 3, 1, fill, default");
      tfImdbId.setColumns(10);
    }
    {
      JLabel lblTmdbId = new JLabel(BUNDLE.getString("metatag.tmdb")); //$NON-NLS-1$
      details1Panel.add(lblTmdbId, "8, 20, right, default");
      tfTmdbId = new JTextField();
      lblTmdbId.setLabelFor(tfTmdbId);
      details1Panel.add(tfTmdbId, "10, 20, 3, 1, fill, default");
      tfTmdbId.setColumns(10);
    }
    {
      JLabel lblWatched = new JLabel(BUNDLE.getString("metatag.watched")); //$NON-NLS-1$
      details1Panel.add(lblWatched, "2, 22, right, default");
      cbWatched = new JCheckBox("");
      lblWatched.setLabelFor(cbWatched);
      details1Panel.add(cbWatched, "4, 22");
    }
    {
      JLabel lblDateAdded = new JLabel(BUNDLE.getString("metatag.dateadded")); //$NON-NLS-1$
      details1Panel.add(lblDateAdded, "8, 22, right, default");
    }
    {
      spDateAdded = new JSpinner(new SpinnerDateModel());
      // JSpinner.DateEditor timeEditor = new JSpinner.DateEditor(spDateAdded,
      // "dd.MM.yyyy HH:mm:ss");
      // spDateAdded.setEditor(timeEditor);
      details1Panel.add(spDateAdded, "10, 22, 3, 1");
    }
    {
      JLabel lblSpokenLanguages = new JLabel(BUNDLE.getString("metatag.spokenlanguages")); //$NON-NLS-1$
      details1Panel.add(lblSpokenLanguages, "2, 24, right, default");
    }
    {
      tfSpokenLanguages = new JTextField();
      details1Panel.add(tfSpokenLanguages, "4, 24, 3, 1, fill, default");
      tfSpokenLanguages.setColumns(10);
    }
    {
      JLabel lblCountry = new JLabel(BUNDLE.getString("metatag.country")); //$NON-NLS-1$
      details1Panel.add(lblCountry, "8, 24, right, default");
    }
    {
      tfCountry = new JTextField();
      details1Panel.add(tfCountry, "10, 24, 3, 1, fill, default");
      tfCountry.setColumns(10);
    }
    {
      JLabel lblPlot = new JLabel(BUNDLE.getString("metatag.plot")); //$NON-NLS-1$
      details1Panel.add(lblPlot, "2, 26, right, top");
    }
    {
      JScrollPane scrollPanePlot = new JScrollPane();
      details1Panel.add(scrollPanePlot, "4, 26, 9, 3, fill, fill");
      {
        tpPlot = new JTextPane();
        scrollPanePlot.setViewportView(tpPlot);
      }
    }
    {
      JLabel lblDirector = new JLabel(BUNDLE.getString("metatag.director")); //$NON-NLS-1$
      details1Panel.add(lblDirector, "2, 30, right, default");
    }
    {
      tfDirector = new JTextField();
      details1Panel.add(tfDirector, "4, 30, 9, 1, fill, top");
      tfDirector.setColumns(10);
    }
    {
      lblFanart = new ImageLabel();
      lblFanart.setAlternativeText(BUNDLE.getString("image.notfound.fanart")); //$NON-NLS-1$
      lblFanart.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
      lblFanart.addMouseListener(new MouseAdapter() {
        @Override
        public void mouseClicked(MouseEvent e) {
          ImageChooserDialog dialog = new ImageChooserDialog(movieToEdit.getIds(), ImageType.FANART, movieList.getArtworkProviders(), lblFanart,
              extrathumbs, extrafanarts, MediaType.MOVIE);
          dialog.setLocationRelativeTo(MainWindow.getActiveInstance());
          dialog.setVisible(true);
        }
      });
      details1Panel.add(lblFanart, "14, 28, 3, 7, fill, fill");
    }
    {
      JLabel lblWriter = new JLabel(BUNDLE.getString("metatag.writer")); //$NON-NLS-1$
      details1Panel.add(lblWriter, "2, 32, right, default");
    }
    {
      tfWriter = new JTextField();
      details1Panel.add(tfWriter, "4, 32, 9, 1, fill, top");
      tfWriter.setColumns(10);
    }
    {
      JLabel lblCompany = new JLabel(BUNDLE.getString("metatag.production")); //$NON-NLS-1$
      details1Panel.add(lblCompany, "2, 34, right, top");
    }
    {
      JScrollPane scrollPaneProduction = new JScrollPane();
      details1Panel.add(scrollPaneProduction, "4, 34, 9, 1, fill, fill");
      tfProductionCompanies = new JTextPane();
      scrollPaneProduction.setViewportView(tfProductionCompanies);
    }

    /**
     * DetailsPanel 2
     */
    tabbedPane.addTab(BUNDLE.getString("metatag.details2"), details2Panel); //$NON-NLS-1$
    details2Panel.setBorder(new EmptyBorder(5, 5, 5, 5));
    details2Panel.setLayout(new FormLayout(new ColumnSpec[] { FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("max(40dlu;default)"),
        FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("50px:grow"), FormFactory.RELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC,
        FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("100px:grow"), FormFactory.RELATED_GAP_COLSPEC, }, new RowSpec[] {
        FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
        FormFactory.RELATED_GAP_ROWSPEC, RowSpec.decode("fill:default"), FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
        FormFactory.RELATED_GAP_ROWSPEC, RowSpec.decode("default:grow"), FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
        FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
        FormFactory.RELATED_GAP_ROWSPEC, RowSpec.decode("default:grow"), FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
        FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
        FormFactory.RELATED_GAP_ROWSPEC, RowSpec.decode("default:grow"), FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
        FormFactory.RELATED_GAP_ROWSPEC, RowSpec.decode("default:grow(2)"), }));
    {
      JLabel lblActors = new JLabel(BUNDLE.getString("metatag.actors")); //$NON-NLS-1$
      details2Panel.add(lblActors, "2, 2, right, default");
    }
    {
      JScrollPane scrollPane = new JScrollPane();
      details2Panel.add(scrollPane, "4, 2, 1, 11");
      tableActors = new JTable();
      scrollPane.setViewportView(tableActors);
    }
    {
      JLabel lblProducers = new JLabel(BUNDLE.getString("metatag.producers")); //$NON-NLS-1$
      details2Panel.add(lblProducers, "6, 2, right, default");
    }
    {
      JScrollPane scrollPane = new JScrollPane();
      details2Panel.add(scrollPane, "8, 2, 1, 11");
      tableProducers = new JTable();
      scrollPane.setViewportView(tableProducers);
    }
    {
      JButton btnAddActor = new JButton(BUNDLE.getString("cast.actor.add")); //$NON-NLS-1$
      btnAddActor.setMargin(new Insets(2, 2, 2, 2));
      btnAddActor.setAction(new AddActorAction());
      btnAddActor.setIcon(IconManager.LIST_ADD);
      details2Panel.add(btnAddActor, "2, 4, right, top");
    }
    {
      JButton btnAddProducer = new JButton((String) null);
      btnAddProducer.setMargin(new Insets(2, 2, 2, 2));
      btnAddProducer.setAction(new AddProducerAction());
      btnAddProducer.setIcon(IconManager.LIST_ADD);
      details2Panel.add(btnAddProducer, "6, 4, right, top");
    }
    {
      JButton btnRemoveActor = new JButton(BUNDLE.getString("cast.actor.remove")); //$NON-NLS-1$
      btnRemoveActor.setMargin(new Insets(2, 2, 2, 2));
      btnRemoveActor.setAction(new RemoveActorAction());
      btnRemoveActor.setIcon(IconManager.LIST_REMOVE);
      details2Panel.add(btnRemoveActor, "2,6, right, top");
    }
    {
      JButton btnRemoveProducer = new JButton((String) null);
      btnRemoveProducer.setMargin(new Insets(2, 2, 2, 2));
      btnRemoveProducer.setAction(new RemoveProducerAction());
      btnRemoveProducer.setIcon(IconManager.LIST_REMOVE);
      details2Panel.add(btnRemoveProducer, "6, 6, right, top");
    }
    {
      JButton btnMoveActorUp = new JButton((String) null);
      btnMoveActorUp.setMargin(new Insets(2, 2, 2, 2));
      btnMoveActorUp.setAction(new MoveActorUpAction());
      btnMoveActorUp.setIcon(IconManager.ARROW_UP);
      details2Panel.add(btnMoveActorUp, "2, 8, right, top");
    }
    {
      JButton btnMoveProducerUp = new JButton((String) null);
      btnMoveProducerUp.setMargin(new Insets(2, 2, 2, 2));
      btnMoveProducerUp.setAction(new MoveProducerUpAction());
      btnMoveProducerUp.setIcon(IconManager.ARROW_UP);
      details2Panel.add(btnMoveProducerUp, "6, 8, right, top");
    }
    {
      JButton btnMoveActorDown = new JButton((String) null);
      btnMoveActorDown.setMargin(new Insets(2, 2, 2, 2));
      btnMoveActorDown.setAction(new MoveActorDownAction());
      btnMoveActorDown.setIcon(IconManager.ARROW_DOWN);
      details2Panel.add(btnMoveActorDown, "2, 10, right, top");
    }
    {
      JButton btnMoveProducerDown = new JButton((String) null);
      btnMoveProducerDown.setMargin(new Insets(2, 2, 2, 2));
      btnMoveProducerDown.setAction(new MoveProducerDownAction());
      btnMoveProducerDown.setIcon(IconManager.ARROW_DOWN);
      details2Panel.add(btnMoveProducerDown, "6, 10, right, top");
    }
    {
      JLabel lblGenres = new JLabel(BUNDLE.getString("metatag.genre")); //$NON-NLS-1$
      details2Panel.add(lblGenres, "2, 14, right, default");
    }
    {
      JScrollPane scrollPaneGenres = new JScrollPane();
      details2Panel.add(scrollPaneGenres, "4, 14, 1, 5");
      {
        listGenres = new JList();
        scrollPaneGenres.setViewportView(listGenres);
      }
    }
    {
      JLabel lblTags = new JLabel(BUNDLE.getString("metatag.tags")); //$NON-NLS-1$
      details2Panel.add(lblTags, "6, 14, right, default");
    }
    {
      JScrollPane scrollPaneTags = new JScrollPane();
      details2Panel.add(scrollPaneTags, "8, 14, 1, 5");
      listTags = new JList();
      scrollPaneTags.setViewportView(listTags);
    }
    {
      JButton btnAddGenre = new JButton("");
      btnAddGenre.setAction(new AddGenreAction());
      btnAddGenre.setIcon(IconManager.LIST_ADD);
      btnAddGenre.setMargin(new Insets(2, 2, 2, 2));
      details2Panel.add(btnAddGenre, "2, 16, right, top");
    }
    {
      JButton btnAddTag = new JButton("");
      btnAddTag.setAction(new AddTagAction());
      btnAddTag.setIcon(IconManager.LIST_ADD);
      btnAddTag.setMargin(new Insets(2, 2, 2, 2));
      details2Panel.add(btnAddTag, "6, 16, right, top");
    }

    {
      JButton btnRemoveGenre = new JButton("");
      btnRemoveGenre.setAction(new RemoveGenreAction());
      btnRemoveGenre.setMargin(new Insets(2, 2, 2, 2));
      btnRemoveGenre.setIcon(IconManager.LIST_REMOVE);
      details2Panel.add(btnRemoveGenre, "2, 18, right, top");
    }
    {
      JButton btnRemoveTag = new JButton("");
      btnRemoveTag.setAction(new RemoveTagAction());
      btnRemoveTag.setIcon(IconManager.LIST_REMOVE);
      btnRemoveTag.setMargin(new Insets(2, 2, 2, 2));
      details2Panel.add(btnRemoveTag, "6, 18, right, top");
    }
    {
      // cbGenres = new JComboBox(MediaGenres2.values());
      cbGenres = new AutocompleteComboBox(MediaGenres.values());
      cbGenres.setEditable(true);
      details2Panel.add(cbGenres, "4, 20");
    }
    {
      cbTags = new AutocompleteComboBox(movieList.getTagsInMovies().toArray());
      cbTags.setEditable(true);
      details2Panel.add(cbTags, "8, 20");
    }

    {
      JLabel lblTrailer = new JLabel(BUNDLE.getString("metatag.trailer")); //$NON-NLS-1$
      details2Panel.add(lblTrailer, "2, 22, right, default");
    }
    {
      JScrollPane scrollPaneTrailer = new JScrollPane();
      details2Panel.add(scrollPaneTrailer, "4, 22, 5, 5");
      tableTrailer = new JTable();
      scrollPaneTrailer.setViewportView(tableTrailer);
    }
    {
      JButton btnAddTrailer = new JButton("");
      btnAddTrailer.setAction(new AddTrailerAction());
      btnAddTrailer.setIcon(IconManager.LIST_ADD);
      btnAddTrailer.setMargin(new Insets(2, 2, 2, 2));
      details2Panel.add(btnAddTrailer, "2, 24, right, top");
    }
    {
      JButton btnRemoveTrailer = new JButton("");
      btnRemoveTrailer.setAction(new RemoveTrailerAction());
      btnRemoveTrailer.setIcon(IconManager.LIST_REMOVE);
      btnRemoveTrailer.setMargin(new Insets(2, 2, 2, 2));
      details2Panel.add(btnRemoveTrailer, "2, 26, right, top");
    }

    /**
     * extra artwork pane
     */
    {
      JPanel artworkPanel = new JPanel();
      tabbedPane.addTab(BUNDLE.getString("metatag.extraartwork"), null, artworkPanel, null); //$NON-NLS-1$
      artworkPanel.setLayout(new FormLayout(new ColumnSpec[] { FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("250px:grow"),
          FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("250px:grow"), FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("150px:grow"),
          FormFactory.RELATED_GAP_COLSPEC, }, new RowSpec[] { FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
          FormFactory.RELATED_GAP_ROWSPEC, RowSpec.decode("50px:grow(2)"), FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
          FormFactory.RELATED_GAP_ROWSPEC, RowSpec.decode("200px:grow(2)"), FormFactory.RELATED_GAP_ROWSPEC, RowSpec.decode("default:grow"), }));
      {
        JLabel lblLogoT = new JLabel("Logo");
        artworkPanel.add(lblLogoT, "2, 2");
      }
      {
        lblLogo = new ImageLabel();
        lblLogo.setAlternativeText(BUNDLE.getString("image.notfound.logo")); //$NON-NLS-1$
        lblLogo.addMouseListener(new MouseAdapter() {
          @Override
          public void mouseClicked(MouseEvent e) {
            ImageChooserDialog dialog = new ImageChooserDialog(movieToEdit.getIds(), ImageType.LOGO, movieList.getArtworkProviders(), lblLogo, null,
                null, MediaType.MOVIE);
            dialog.setLocationRelativeTo(MainWindow.getActiveInstance());
            dialog.setVisible(true);
          }
        });
        {
          JLabel lblBannerT = new JLabel("Banner");
          artworkPanel.add(lblBannerT, "4, 2");
        }
        lblLogo.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        artworkPanel.add(lblLogo, "2, 4, fill, fill");
      }
      {
        lblBanner = new ImageLabel();
        lblBanner.setAlternativeText(BUNDLE.getString("image.notfound.banner")); //$NON-NLS-1$
        lblBanner.addMouseListener(new MouseAdapter() {
          @Override
          public void mouseClicked(MouseEvent e) {
            ImageChooserDialog dialog = new ImageChooserDialog(movieToEdit.getIds(), ImageType.BANNER, movieList.getArtworkProviders(), lblBanner,
                null, null, MediaType.MOVIE);
            dialog.setLocationRelativeTo(MainWindow.getActiveInstance());
            dialog.setVisible(true);
          }
        });
        lblBanner.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        artworkPanel.add(lblBanner, "4, 4, 3, 1, fill, fill");
      }
      lblBanner.setImagePath(movie.getArtworkFilename(MediaFileType.BANNER));
      {
        JLabel lblClearartT = new JLabel("ClearArt");
        artworkPanel.add(lblClearartT, "2, 6");
      }
      {
        JLabel lblThumbT = new JLabel("Thumb");
        artworkPanel.add(lblThumbT, "4, 6");
      }
      {
        lblClearart = new ImageLabel();
        lblClearart.setAlternativeText(BUNDLE.getString("image.notfound.clearart")); //$NON-NLS-1$
        lblClearart.addMouseListener(new MouseAdapter() {
          @Override
          public void mouseClicked(MouseEvent e) {
            ImageChooserDialog dialog = new ImageChooserDialog(movieToEdit.getIds(), ImageType.CLEARART, movieList.getArtworkProviders(),
                lblClearart, null, null, MediaType.MOVIE);
            dialog.setLocationRelativeTo(MainWindow.getActiveInstance());
            dialog.setVisible(true);
          }
        });
        {
          JLabel lblDiscT = new JLabel("Disc");
          artworkPanel.add(lblDiscT, "6, 6");
        }
        lblClearart.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        artworkPanel.add(lblClearart, "2, 8, fill, fill");
      }
      {
        lblThumb = new ImageLabel();
        lblThumb.setAlternativeText(BUNDLE.getString("image.notfound.thumb")); //$NON-NLS-1$
        lblThumb.addMouseListener(new MouseAdapter() {
          @Override
          public void mouseClicked(MouseEvent e) {
            ImageChooserDialog dialog = new ImageChooserDialog(movieToEdit.getIds(), ImageType.THUMB, movieList.getArtworkProviders(), lblThumb,
                null, null, MediaType.MOVIE);
            dialog.setLocationRelativeTo(MainWindow.getActiveInstance());
            dialog.setVisible(true);
          }
        });
        lblThumb.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        artworkPanel.add(lblThumb, "4, 8, fill, fill");
      }
      {
        lblDisc = new ImageLabel();
        lblDisc.setAlternativeText(BUNDLE.getString("image.notfound.disc")); //$NON-NLS-1$
        lblDisc.addMouseListener(new MouseAdapter() {
          @Override
          public void mouseClicked(MouseEvent e) {
            ImageChooserDialog dialog = new ImageChooserDialog(movieToEdit.getIds(), ImageType.DISC, movieList.getArtworkProviders(), lblDisc, null,
                null, MediaType.MOVIE);
            dialog.setLocationRelativeTo(MainWindow.getActiveInstance());
            dialog.setVisible(true);
          }
        });
        lblDisc.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        artworkPanel.add(lblDisc, "6, 8, fill, fill");
      }
      lblDisc.setImagePath(movie.getArtworkFilename(MediaFileType.DISCART));
    }

    /**
     * Button pane
     */
    {
      JPanel bottomPane = new JPanel();
      getContentPane().add(bottomPane, BorderLayout.SOUTH);
      bottomPane.setLayout(new FormLayout(new ColumnSpec[] { ColumnSpec.decode("371px:grow"), FormFactory.DEFAULT_COLSPEC,
          FormFactory.RELATED_GAP_COLSPEC, }, new RowSpec[] { FormFactory.LINE_GAP_ROWSPEC, RowSpec.decode("25px"),
          FormFactory.NARROW_LINE_GAP_ROWSPEC, }));

      JPanel buttonPane = new JPanel();
      bottomPane.add(buttonPane, "2, 2, left, top");
      EqualsLayout layout = new EqualsLayout(5);
      layout.setMinWidth(100);
      buttonPane.setLayout(layout);
      {
        JButton okButton = new JButton(BUNDLE.getString("Button.ok")); //$NON-NLS-1$
        buttonPane.add(okButton, "2, 1, fill, top");
        okButton.setAction(new ChangeMovieAction());
        okButton.setActionCommand("OK");
        getRootPane().setDefaultButton(okButton);
      }
      {
        JButton cancelButton = new JButton(BUNDLE.getString("Button.cancel")); //$NON-NLS-1$
        buttonPane.add(cancelButton, "4, 1, fill, top");
        cancelButton.setAction(new DiscardAction());
        cancelButton.setActionCommand("Cancel");
      }
      if (inQueue) {
        JButton btnAbort = new JButton(BUNDLE.getString("Button.abortqueue")); //$NON-NLS-1$
        btnAbort.setAction(new AbortQueueAction());
        buttonPane.add(btnAbort, "6, 1, fill, top");
      }

    }
    initDataBindings();

    {
      lblMoviePath.setText(movie.getPath());
      tfTitle.setText(movie.getTitle());
      tfOriginalTitle.setText(movie.getOriginalTitle());
      tfSorttitle.setText(movie.getSortTitle());
      tpTagline.setText(movie.getTagline());
      tfImdbId.setText(movie.getImdbId());
      tfTmdbId.setText(String.valueOf(movie.getTmdbId()));
      tpPlot.setText(movie.getPlot());
      tfDirector.setText(movie.getDirector());
      tfWriter.setText(movie.getWriter());
      lblFanart.setImagePath(movie.getArtworkFilename(MediaFileType.FANART));
      lblPoster.setImagePath(movie.getArtworkFilename(MediaFileType.POSTER));
      lblLogo.setImagePath(movie.getArtworkFilename(MediaFileType.LOGO));
      lblClearart.setImagePath(movie.getArtworkFilename(MediaFileType.CLEARART));
      lblThumb.setImagePath(movie.getArtworkFilename(MediaFileType.THUMB));
      tfProductionCompanies.setText(movie.getProductionCompany());
      spRuntime.setValue(Integer.valueOf(movie.getRuntime()));
      spTop250.setValue(Integer.valueOf(movie.getTop250()));
      cbWatched.setSelected(movie.isWatched());
      spDateAdded.setValue(movie.getDateAdded());
      cbCertification.setSelectedItem(movie.getCertification());
      if (movie.getReleaseDate() != null) {
        spReleaseDate.setValue(movie.getReleaseDate());
      }
      else {
        spReleaseDate.setValue(INITIAL_DATE);
      }
      tfSpokenLanguages.setText(movie.getSpokenLanguages());
      tfCountry.setText(movie.getCountry());

      int year = 0;
      try {
        year = Integer.valueOf(movie.getYear());
      }
      catch (Exception e) {
      }
      spYear.setModel(new SpinnerNumberModel(year, 0, 3000, 1));
      spYear.setEditor(new JSpinner.NumberEditor(spYear, "#"));

      spRating.setModel(new SpinnerNumberModel(movie.getRating(), 0.0, 10.0, 0.1));
      SimpleDateFormat dateFormat = (SimpleDateFormat) DateFormat.getDateInstance(DateFormat.MEDIUM);
      spReleaseDate.setEditor(new JSpinner.DateEditor(spReleaseDate, dateFormat.toPattern()));

      for (MovieActor origCast : movie.getActors()) {
        MovieActor actor = new MovieActor();
        actor.setName(origCast.getName());
        actor.setCharacter(origCast.getCharacter());
        actor.setThumbUrl(origCast.getThumbUrl());
        actor.setThumbPath(origCast.getThumbPath());
        cast.add(actor);
      }

      for (MovieProducer origProducer : movie.getProducers()) {
        MovieProducer producer = new MovieProducer();
        producer.setName(origProducer.getName());
        producer.setRole(origProducer.getRole());
        producer.setThumbUrl(origProducer.getThumbUrl());
        producer.setThumbPath(origProducer.getThumbPath());
        producers.add(producer);
      }

      for (MediaGenres genre : movie.getGenres()) {
        genres.add(genre);
      }

      for (MediaTrailer trailer : movie.getTrailers()) {
        trailers.add(trailer);
      }

      for (String tag : movieToEdit.getTags()) {
        if (StringUtils.isNotBlank(tag)) {
          tags.add(tag);
        }
      }

      extrathumbs.addAll(movieToEdit.getExtraThumbs());
      extrafanarts.addAll(movieToEdit.getExtraFanarts());

      cbMovieSet.addItem("");
      for (MovieSet movieSet : movieList.getSortedMovieSetList()) {
        cbMovieSet.addItem(movieSet);
        if (movieToEdit.getMovieSet() == movieSet) {
          cbMovieSet.setSelectedItem(movieSet);
        }
      }

      toggleSorttitle();
    }
    // adjust columnn titles - we have to do it this way - thx to windowbuilder pro
    tableActors.getColumnModel().getColumn(0).setHeaderValue(BUNDLE.getString("metatag.name")); //$NON-NLS-1$
    tableActors.getColumnModel().getColumn(1).setHeaderValue(BUNDLE.getString("metatag.role")); //$NON-NLS-1$
    tableProducers.getColumnModel().getColumn(0).setHeaderValue(BUNDLE.getString("metatag.name")); //$NON-NLS-1$
    tableProducers.getColumnModel().getColumn(1).setHeaderValue(BUNDLE.getString("metatag.role")); //$NON-NLS-1$

    tableTrailer.getColumnModel().getColumn(0).setHeaderValue(BUNDLE.getString("metatag.nfo")); //$NON-NLS-1$
    tableTrailer.getColumnModel().getColumn(1).setHeaderValue(BUNDLE.getString("metatag.name")); //$NON-NLS-1$
    tableTrailer.getColumnModel().getColumn(2).setHeaderValue(BUNDLE.getString("metatag.source")); //$NON-NLS-1$
    tableTrailer.getColumnModel().getColumn(3).setHeaderValue(BUNDLE.getString("metatag.quality")); //$NON-NLS-1$
    tableTrailer.getColumnModel().getColumn(4).setHeaderValue(BUNDLE.getString("metatag.url")); //$NON-NLS-1$

    // adjust table columns
    tableTrailer.getColumnModel().getColumn(0).setMaxWidth(55);

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

  /**
   * Toggle sorttitle.
   */
  private void toggleSorttitle() {
    Object obj = cbMovieSet.getSelectedItem();
    if (obj instanceof String) {
      tfSorttitle.setEnabled(true);
    }
    else {
      tfSorttitle.setEnabled(false);
    }
  }

  private class ChangeMovieAction extends AbstractAction {
    private static final long serialVersionUID = -3767744690599233490L;

    public ChangeMovieAction() {
      putValue(NAME, BUNDLE.getString("Button.ok")); //$NON-NLS-1$
      putValue(SHORT_DESCRIPTION, BUNDLE.getString("movie.change")); //$NON-NLS-1$
      putValue(SMALL_ICON, IconManager.APPLY);
      putValue(LARGE_ICON_KEY, IconManager.APPLY);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      movieToEdit.setTitle(tfTitle.getText());
      movieToEdit.setOriginalTitle(tfOriginalTitle.getText());
      movieToEdit.setTagline(tpTagline.getText());
      movieToEdit.setPlot(tpPlot.getText());
      movieToEdit.setYear(spYear.getValue().equals(0) ? "" : String.valueOf(spYear.getValue())); // set empty on 0

      Date releaseDate = (Date) spReleaseDate.getValue();
      if (!releaseDate.equals(INITIAL_DATE)) {
        movieToEdit.setReleaseDate(releaseDate);
      }
      movieToEdit.setRuntime((Integer) spRuntime.getValue());
      movieToEdit.setTop250((Integer) spTop250.getValue());
      movieToEdit.setImdbId(tfImdbId.getText());
      movieToEdit.setWatched(cbWatched.isSelected());
      movieToEdit.setSpokenLanguages(tfSpokenLanguages.getText());
      movieToEdit.setCountry(tfCountry.getText());

      if (StringUtils.isNotBlank(tfTmdbId.getText())) {
        try {
          movieToEdit.setTmdbId(Integer.parseInt(tfTmdbId.getText()));
        }
        catch (NumberFormatException ex) {
          JOptionPane.showMessageDialog(null, BUNDLE.getString("tmdb.wrongformat")); //$NON-NLS-1$
          return;
        }
      }
      else {
        movieToEdit.setTmdbId(0);
      }

      Object certification = cbCertification.getSelectedItem();
      if (certification instanceof Certification) {
        movieToEdit.setCertification((Certification) certification);
      }

      if (!StringUtils.isEmpty(lblPoster.getImageUrl()) && !lblPoster.getImageUrl().equals(movieToEdit.getPosterUrl())) {
        movieToEdit.setPosterUrl(lblPoster.getImageUrl());
        movieToEdit.downloadArtwork(MediaFileType.POSTER);
      }

      if (!StringUtils.isEmpty(lblFanart.getImageUrl()) && !lblFanart.getImageUrl().equals(movieToEdit.getFanartUrl())) {
        movieToEdit.setFanartUrl(lblFanart.getImageUrl());
        movieToEdit.downloadArtwork(MediaFileType.FANART);
      }

      if (!StringUtils.isEmpty(lblLogo.getImageUrl()) && !lblLogo.getImageUrl().equals(movieToEdit.getArtworkUrl(MediaFileType.LOGO))) {
        movieToEdit.setArtworkUrl(lblLogo.getImageUrl(), MediaFileType.LOGO);
        movieToEdit.downloadArtwork(MediaFileType.LOGO);
      }

      if (!StringUtils.isEmpty(lblBanner.getImageUrl()) && !lblBanner.getImageUrl().equals(movieToEdit.getArtworkUrl(MediaFileType.BANNER))) {
        movieToEdit.setArtworkUrl(lblBanner.getImageUrl(), MediaFileType.BANNER);
        movieToEdit.downloadArtwork(MediaFileType.BANNER);
      }

      if (!StringUtils.isEmpty(lblClearart.getImageUrl()) && !lblClearart.getImageUrl().equals(movieToEdit.getArtworkUrl(MediaFileType.CLEARART))) {
        movieToEdit.setArtworkUrl(lblClearart.getImageUrl(), MediaFileType.CLEARART);
        movieToEdit.downloadArtwork(MediaFileType.CLEARART);
      }

      if (!StringUtils.isEmpty(lblThumb.getImageUrl()) && !lblThumb.getImageUrl().equals(movieToEdit.getArtworkUrl(MediaFileType.THUMB))) {
        movieToEdit.setArtworkUrl(lblThumb.getImageUrl(), MediaFileType.THUMB);
        movieToEdit.downloadArtwork(MediaFileType.THUMB);
      }

      if (!StringUtils.isEmpty(lblDisc.getImageUrl()) && !lblDisc.getImageUrl().equals(movieToEdit.getArtworkUrl(MediaFileType.DISCART))) {
        movieToEdit.setArtworkUrl(lblDisc.getImageUrl(), MediaFileType.DISCART);
        movieToEdit.downloadArtwork(MediaFileType.DISCART);
      }

      // set extrathumbs
      if (extrathumbs.size() != movieToEdit.getExtraThumbs().size() || !extrathumbs.containsAll(movieToEdit.getExtraThumbs())
          || !movieToEdit.getExtraThumbs().containsAll(extrathumbs)) {
        // movieToEdit.downloadExtraThumbs(extrathumbs);
        movieToEdit.setExtraThumbs(extrathumbs);
        movieToEdit.downloadArtwork(MediaFileType.EXTRATHUMB);
      }

      // set extrafanarts
      if (extrafanarts.size() != movieToEdit.getExtraFanarts().size() || !extrafanarts.containsAll(movieToEdit.getExtraFanarts())
          || !movieToEdit.getExtraFanarts().containsAll(extrafanarts)) {
        // movieToEdit.downloadExtraFanarts(extrafanarts);
        movieToEdit.setExtraFanarts(extrafanarts);
        movieToEdit.downloadArtwork(MediaFileType.EXTRAFANART);
      }

      movieToEdit.setDirector(tfDirector.getText());
      movieToEdit.setWriter(tfWriter.getText());
      movieToEdit.setProductionCompany(tfProductionCompanies.getText());
      movieToEdit.setActors(cast);
      movieToEdit.setProducers(producers);
      movieToEdit.setGenres(genres);

      movieToEdit.removeAllTrailers();
      for (MediaTrailer trailer : trailers) {
        movieToEdit.addTrailer(trailer);
      }

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
          // movieSet.addMovie(movieToEdit);
          movieSet.insertMovie(movieToEdit);
        }

        // movieToEdit.setSortTitleFromMovieSet();
        movieSet.updateMovieSorttitle();
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

  private class DiscardAction extends AbstractAction {
    private static final long serialVersionUID = -5581329896797961536L;

    public DiscardAction() {
      putValue(NAME, BUNDLE.getString("Button.cancel")); //$NON-NLS-1$
      putValue(SHORT_DESCRIPTION, BUNDLE.getString("edit.discard")); //$NON-NLS-1$
      putValue(SMALL_ICON, IconManager.CANCEL);
      putValue(LARGE_ICON_KEY, IconManager.CANCEL);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      setVisible(false);
      dispose();
    }
  }

  private class AddActorAction extends AbstractAction {
    private static final long serialVersionUID = 2903255414153349267L;

    public AddActorAction() {
      putValue(SHORT_DESCRIPTION, BUNDLE.getString("cast.actor.add")); //$NON-NLS-1$
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      MovieActor actor = new MovieActor(BUNDLE.getString("cast.actor.unknown"), BUNDLE.getString("cast.role.unknown")); //$NON-NLS-1$
      cast.add(0, actor);
    }
  }

  private class RemoveActorAction extends AbstractAction {
    private static final long serialVersionUID = -7079826970827356996L;

    public RemoveActorAction() {
      putValue(SHORT_DESCRIPTION, BUNDLE.getString("cast.actor.remove")); //$NON-NLS-1$
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      int row = tableActors.getSelectedRow();
      if (row > -1) {
        row = tableActors.convertRowIndexToModel(row);
        cast.remove(row);
      }
    }
  }

  private class AddProducerAction extends AbstractAction {
    private static final long serialVersionUID = -8834531637996987853L;

    public AddProducerAction() {
      putValue(SHORT_DESCRIPTION, BUNDLE.getString("cast.producer.add")); //$NON-NLS-1$
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      MovieProducer producer = new MovieProducer(BUNDLE.getString("producer.name.unknown"), BUNDLE.getString("producer.role.unknown")); //$NON-NLS-1$
      producers.add(0, producer);
    }
  }

  private class RemoveProducerAction extends AbstractAction {
    private static final long serialVersionUID = -3907776089614305086L;

    public RemoveProducerAction() {
      putValue(SHORT_DESCRIPTION, BUNDLE.getString("cast.producer.remove")); //$NON-NLS-1$
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      int row = tableProducers.getSelectedRow();
      if (row > -1) {
        row = tableProducers.convertRowIndexToModel(row);
        producers.remove(row);
      }
    }
  }

  private class AddGenreAction extends AbstractAction {
    private static final long serialVersionUID = 176474809593575743L;

    public AddGenreAction() {
      putValue(SHORT_DESCRIPTION, BUNDLE.getString("genre.add")); //$NON-NLS-1$
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      MediaGenres newGenre = null;
      Object item = cbGenres.getSelectedItem();

      // genre
      if (item instanceof MediaGenres) {
        newGenre = (MediaGenres) item;
      }

      // newly created genre?
      if (item instanceof String) {
        newGenre = MediaGenres.getGenre((String) item);
      }

      // add genre if it is not already in the list
      if (newGenre != null && !genres.contains(newGenre)) {
        genres.add(newGenre);
      }
    }
  }

  private class RemoveGenreAction extends AbstractAction {
    private static final long serialVersionUID = 2733654945906747720L;

    public RemoveGenreAction() {
      putValue(SHORT_DESCRIPTION, BUNDLE.getString("genre.remove")); //$NON-NLS-1$
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      MediaGenres newGenre = (MediaGenres) listGenres.getSelectedValue();
      // remove genre
      if (newGenre != null) {
        genres.remove(newGenre);
      }
    }
  }

  private class AddTrailerAction extends AbstractAction {
    private static final long serialVersionUID = -4446154040952056823L;

    public AddTrailerAction() {
      putValue(SHORT_DESCRIPTION, BUNDLE.getString("trailer.add")); //$NON-NLS-1$
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      MediaTrailer trailer = new MediaTrailer();
      trailer.setName("unknown");
      trailer.setProvider("unknown");
      trailer.setQuality("unknown");
      trailer.setUrl("http://");
      trailers.add(0, trailer);
    }
  }

  private class RemoveTrailerAction extends AbstractAction {
    private static final long serialVersionUID = -6956921050689930101L;

    public RemoveTrailerAction() {
      putValue(SHORT_DESCRIPTION, BUNDLE.getString("trailer.remove")); //$NON-NLS-1$
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      int row = tableTrailer.getSelectedRow();
      if (row > -1) {
        row = tableTrailer.convertRowIndexToModel(row);
        trailers.remove(row);
      }
    }
  }

  /**
   * Shows the dialog and returns whether the work on the queue should be continued.
   * 
   * @return true, if successful
   */
  public boolean showDialog() {
    setLocationRelativeTo(MainWindow.getActiveInstance());
    setVisible(true);
    return continueQueue;
  }

  private class AddTagAction extends AbstractAction {
    private static final long serialVersionUID = 9160043031922897785L;

    public AddTagAction() {
      putValue(SHORT_DESCRIPTION, BUNDLE.getString("tag.add")); //$NON-NLS-1$
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      String newTag = (String) cbTags.getSelectedItem();
      boolean tagFound = false;

      // do not continue with empty tags
      if (StringUtils.isBlank(newTag)) {
        return;
      }

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

  private class RemoveTagAction extends AbstractAction {
    private static final long serialVersionUID = -1580945350962234235L;

    public RemoveTagAction() {
      putValue(SHORT_DESCRIPTION, BUNDLE.getString("tag.remove")); //$NON-NLS-1$
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      String tag = (String) listTags.getSelectedValue();
      tags.remove(tag);
    }
  }

  private class ToggleMovieSetAction extends AbstractAction {
    private static final long serialVersionUID = 5666621763248388091L;

    public ToggleMovieSetAction() {
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      toggleSorttitle();
    }
  }

  private class AbortQueueAction extends AbstractAction {
    private static final long serialVersionUID = -7652218354710642510L;

    public AbortQueueAction() {
      putValue(NAME, BUNDLE.getString("Button.abortqueue")); //$NON-NLS-1$
      putValue(SHORT_DESCRIPTION, BUNDLE.getString("movie.edit.abortqueue.desc")); //$NON-NLS-1$
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      continueQueue = false;
      setVisible(false);
      dispose();
    }
  }

  private class MoveActorUpAction extends AbstractAction {
    private static final long serialVersionUID = 5775423424097844658L;

    public MoveActorUpAction() {
      putValue(SHORT_DESCRIPTION, BUNDLE.getString("movie.edit.moveactorup")); //$NON-NLS-1$
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      int row = tableActors.getSelectedRow();
      if (row > 0) {
        Collections.rotate(cast.subList(row - 1, row + 1), 1);
        tableActors.getSelectionModel().setSelectionInterval(row - 1, row - 1);
      }
    }
  }

  private class MoveActorDownAction extends AbstractAction {
    private static final long serialVersionUID = -6564146895819191932L;

    public MoveActorDownAction() {
      putValue(SHORT_DESCRIPTION, BUNDLE.getString("movie.edit.moveactordown")); //$NON-NLS-1$
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      int row = tableActors.getSelectedRow();
      if (row < cast.size() - 1) {
        Collections.rotate(cast.subList(row, row + 2), -1);
        tableActors.getSelectionModel().setSelectionInterval(row + 1, row + 1);
      }
    }
  }

  private class MoveProducerUpAction extends AbstractAction {
    private static final long serialVersionUID = -6855661707692602266L;

    public MoveProducerUpAction() {
      putValue(SHORT_DESCRIPTION, BUNDLE.getString("movie.edit.moveproducerup")); //$NON-NLS-1$
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      int row = tableProducers.getSelectedRow();
      if (row > 0) {
        Collections.rotate(producers.subList(row - 1, row + 1), 1);
        tableProducers.getSelectionModel().setSelectionInterval(row - 1, row - 1);
      }
    }
  }

  private class MoveProducerDownAction extends AbstractAction {
    private static final long serialVersionUID = -1135108943010008069L;

    public MoveProducerDownAction() {
      putValue(SHORT_DESCRIPTION, BUNDLE.getString("movie.edit.moveproducerdown")); //$NON-NLS-1$
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      int row = tableProducers.getSelectedRow();
      if (row < producers.size() - 1) {
        Collections.rotate(producers.subList(row, row + 2), -1);
        tableProducers.getSelectionModel().setSelectionInterval(row + 1, row + 1);
      }
    }
  }

  protected void initDataBindings() {
    jTableBinding = SwingBindings.createJTableBinding(UpdateStrategy.READ, cast, tableActors);
    //
    BeanProperty<MovieActor, String> movieCastBeanProperty = BeanProperty.create("name");
    jTableBinding.addColumnBinding(movieCastBeanProperty);
    //
    BeanProperty<MovieActor, String> movieCastBeanProperty_1 = BeanProperty.create("character");
    jTableBinding.addColumnBinding(movieCastBeanProperty_1);
    //
    jTableBinding.bind();
    //
    jListBinding = SwingBindings.createJListBinding(UpdateStrategy.READ, genres, listGenres);
    jListBinding.bind();
    //
    jTableBinding_1 = SwingBindings.createJTableBinding(UpdateStrategy.READ, trailers, tableTrailer);
    //
    BeanProperty<MediaTrailer, Boolean> trailerBeanProperty = BeanProperty.create("inNfo");
    jTableBinding_1.addColumnBinding(trailerBeanProperty).setColumnClass(Boolean.class);
    //
    BeanProperty<MediaTrailer, String> trailerBeanProperty_1 = BeanProperty.create("name");
    jTableBinding_1.addColumnBinding(trailerBeanProperty_1);
    //
    BeanProperty<MediaTrailer, String> trailerBeanProperty_2 = BeanProperty.create("provider");
    jTableBinding_1.addColumnBinding(trailerBeanProperty_2);
    //
    BeanProperty<MediaTrailer, String> trailerBeanProperty_3 = BeanProperty.create("quality");
    jTableBinding_1.addColumnBinding(trailerBeanProperty_3);
    //
    BeanProperty<MediaTrailer, String> trailerBeanProperty_4 = BeanProperty.create("url");
    jTableBinding_1.addColumnBinding(trailerBeanProperty_4);
    //
    jTableBinding_1.bind();
    //
    BeanProperty<MovieList, List<String>> movieListBeanProperty = BeanProperty.create("tagsInMovies");
    jComboBinding = SwingBindings.createJComboBoxBinding(UpdateStrategy.READ, movieList, movieListBeanProperty, cbTags);
    jComboBinding.bind();
    //
    jListBinding_1 = SwingBindings.createJListBinding(UpdateStrategy.READ, tags, listTags);
    jListBinding_1.bind();
    //
    jTableBinding_2 = SwingBindings.createJTableBinding(UpdateStrategy.READ, producers, tableProducers);
    //
    BeanProperty<MovieProducer, String> movieProducerBeanProperty = BeanProperty.create("name");
    jTableBinding_2.addColumnBinding(movieProducerBeanProperty);
    //
    BeanProperty<MovieProducer, String> movieProducerBeanProperty_1 = BeanProperty.create("role");
    jTableBinding_2.addColumnBinding(movieProducerBeanProperty_1);
    //
    jTableBinding_2.bind();

  }

  @Override
  public void dispose() {
    super.dispose();
    jTableBinding.unbind();
    jListBinding.unbind();
    jTableBinding_1.unbind();
    jComboBinding.unbind();
    jListBinding_1.unbind();
    jTableBinding_2.unbind();
  }
}
