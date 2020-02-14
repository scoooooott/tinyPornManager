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

import static org.tinymediamanager.ui.TmmUIHelper.createLinkForImage;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.AbstractAction;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JLayer;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SpinnerDateModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import org.apache.commons.lang3.StringUtils;
import org.jdesktop.beansbinding.AutoBinding.UpdateStrategy;
import org.jdesktop.beansbinding.BeanProperty;
import org.jdesktop.beansbinding.BindingGroup;
import org.jdesktop.observablecollections.ObservableCollections;
import org.jdesktop.swingbinding.JListBinding;
import org.jdesktop.swingbinding.JTableBinding;
import org.jdesktop.swingbinding.SwingBindings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.core.MediaCertification;
import org.tinymediamanager.core.MediaFileType;
import org.tinymediamanager.core.MediaSource;
import org.tinymediamanager.core.Message;
import org.tinymediamanager.core.Message.MessageLevel;
import org.tinymediamanager.core.MessageManager;
import org.tinymediamanager.core.entities.MediaFile;
import org.tinymediamanager.core.entities.MediaGenres;
import org.tinymediamanager.core.entities.MediaRating;
import org.tinymediamanager.core.entities.MediaTrailer;
import org.tinymediamanager.core.entities.Person;
import org.tinymediamanager.core.movie.MovieEdition;
import org.tinymediamanager.core.movie.MovieList;
import org.tinymediamanager.core.movie.MovieModuleManager;
import org.tinymediamanager.core.movie.entities.Movie;
import org.tinymediamanager.core.movie.entities.MovieSet;
import org.tinymediamanager.core.threading.TmmTask;
import org.tinymediamanager.core.threading.TmmTaskManager;
import org.tinymediamanager.scraper.ScraperType;
import org.tinymediamanager.scraper.entities.MediaType;
import org.tinymediamanager.thirdparty.trakttv.SyncTraktTvTask;
import org.tinymediamanager.ui.IconManager;
import org.tinymediamanager.ui.MainWindow;
import org.tinymediamanager.ui.ShadowLayerUI;
import org.tinymediamanager.ui.TmmUIHelper;
import org.tinymediamanager.ui.UIConstants;
import org.tinymediamanager.ui.components.FlatButton;
import org.tinymediamanager.ui.components.ImageLabel;
import org.tinymediamanager.ui.components.LinkLabel;
import org.tinymediamanager.ui.components.MainTabbedPane;
import org.tinymediamanager.ui.components.MediaIdTable;
import org.tinymediamanager.ui.components.MediaIdTable.MediaId;
import org.tinymediamanager.ui.components.MediaRatingTable;
import org.tinymediamanager.ui.components.PersonTable;
import org.tinymediamanager.ui.components.TmmLabel;
import org.tinymediamanager.ui.components.combobox.AutocompleteComboBox;
import org.tinymediamanager.ui.components.datepicker.DatePicker;
import org.tinymediamanager.ui.components.datepicker.YearSpinner;
import org.tinymediamanager.ui.components.table.TmmTable;
import org.tinymediamanager.ui.dialogs.IdEditorDialog;
import org.tinymediamanager.ui.dialogs.ImageChooserDialog;
import org.tinymediamanager.ui.dialogs.ImageChooserDialog.ImageType;
import org.tinymediamanager.ui.dialogs.PersonEditorDialog;
import org.tinymediamanager.ui.dialogs.RatingEditorDialog;
import org.tinymediamanager.ui.dialogs.TmmDialog;
import org.tinymediamanager.ui.panels.MediaFileEditorPanel;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.ObservableElementList;
import ca.odell.glazedlists.swing.AutoCompleteSupport;
import net.miginfocom.swing.MigLayout;

/**
 * The Class MovieEditor.
 * 
 * @author Manuel Laggner
 */
public class MovieEditorDialog extends TmmDialog {
  private static final long                       serialVersionUID    = -286251957529920347L;
  private static final Logger                     LOGGER              = LoggerFactory.getLogger(MovieEditorDialog.class);
  private static final Insets                     BUTTON_MARGIN       = UIConstants.SMALL_BUTTON_MARGIN;
  private static final String                     ORIGINAL_IMAGE_SIZE = "originalImageSize";
  private static final String                     SPACER              = "        ";

  private Movie                                   movieToEdit;
  private MovieList                               movieList           = MovieList.getInstance();

  private List<MediaGenres>                       genres              = ObservableCollections.observableList(new ArrayList<>());
  private List<MediaTrailer>                      trailers            = ObservableCollections.observableList(new ArrayList<>());
  private List<String>                            tags                = ObservableCollections.observableList(new ArrayList<>());
  private EventList<MediaId>                      ids;
  private EventList<MediaRatingTable.MediaRating> mediaRatings;
  private List<MediaFile>                         mediaFiles          = new ArrayList<>();
  private List<String>                            extrathumbs         = null;
  private List<String>                            extrafanarts        = null;
  private MediaRating                             userMediaRating;
  private boolean                                 continueQueue       = true;
  private boolean                                 navigateBack        = false;
  private int                                     queueIndex;
  private int                                     queueSize;

  private EventList<Person>                       cast;
  private EventList<Person>                       producers;
  private EventList<Person>                       directors;
  private EventList<Person>                       writers;

  private JTextField                              tfTitle;
  private JTextField                              tfOriginalTitle;
  private YearSpinner                             spYear;
  private JTextArea                               taPlot;

  private ImageLabel                              lblPoster;
  private ImageLabel                              lblFanart;
  private JSpinner                                spRuntime;
  private JTextField                              tfProductionCompanies;
  private JList<MediaGenres>                      listGenres;
  private AutocompleteComboBox<MediaGenres>       cbGenres;
  private AutoCompleteSupport<MediaGenres>        cbGenresAutoCompleteSupport;
  private JSpinner                                spRating;
  private JComboBox<MediaCertification>           cbCertification;
  private JCheckBox                               cbWatched;
  private JTextField                              tfTagline;
  private JTextField                              tfNote;

  private JCheckBox                               chckbxVideo3D;

  private AutocompleteComboBox<String>            cbTags;
  private AutoCompleteSupport<String>             cbTagsAutoCompleteSupport;
  private JList<String>                           listTags;
  private JSpinner                                spDateAdded;
  private JComboBox                               cbMovieSet;
  private JTextField                              tfSorttitle;
  private JTextField                              tfSpokenLanguages;
  private JTextField                              tfCountry;
  private DatePicker                              dpReleaseDate;
  private JSpinner                                spTop250;
  private AutocompleteComboBox<MediaSource>       cbSource;
  private MediaFileEditorPanel                    mediaFilesPanel;
  private AutocompleteComboBox<MovieEdition>      cbEdition;

  private JTextField                              tfPoster;
  private JTextField                              tfFanart;
  private JTextField                              tfLogo;
  private JTextField                              tfClearLogo;
  private JTextField                              tfBanner;
  private JTextField                              tfClearArt;
  private JTextField                              tfThumb;
  private JTextField                              tfDisc;
  private JTextField                              tfKeyart;

  private ImageLabel                              lblLogo;
  private ImageLabel                              lblClearlogo;
  private ImageLabel                              lblBanner;
  private ImageLabel                              lblClearart;
  private ImageLabel                              lblThumb;
  private ImageLabel                              lblDisc;
  private ImageLabel                              lblKeyart;

  private TmmTable                                tableIds;
  private TmmTable                                tableRatings;
  private TmmTable                                tableTrailer;
  private TmmTable                                tableActors;
  private TmmTable                                tableProducers;
  private TmmTable                                tableDirectors;
  private TmmTable                                tableWriters;

  /**
   * Create the dialog.
   * 
   * @param movie
   *          the movie
   * @param queueIndex
   *          the actual index in the queue
   * @param queueSize
   *          the queue size
   */
  public MovieEditorDialog(Movie movie, int queueIndex, int queueSize) {
    super(BUNDLE.getString("movie.edit") + (queueSize > 1 ? " " + (queueIndex + 1) + "/" + queueSize : "") + "  < " + movie.getPathNIO() + " >",
        "movieEditor");

    // creation of lists
    cast = new ObservableElementList<>(GlazedLists.threadSafeList(new BasicEventList<>()), GlazedLists.beanConnector(Person.class));
    producers = new ObservableElementList<>(GlazedLists.threadSafeList(new BasicEventList<>()), GlazedLists.beanConnector(Person.class));
    directors = new ObservableElementList<>(GlazedLists.threadSafeList(new BasicEventList<>()), GlazedLists.beanConnector(Person.class));
    writers = new ObservableElementList<>(GlazedLists.threadSafeList(new BasicEventList<>()), GlazedLists.beanConnector(Person.class));

    this.movieToEdit = movie;
    this.queueIndex = queueIndex;
    this.queueSize = queueSize;
    this.ids = MediaIdTable.convertIdMapToEventList(movieToEdit.getIds());
    this.mediaRatings = MediaRatingTable.convertRatingMapToEventList(movieToEdit.getRatings(), false);
    this.userMediaRating = movieToEdit.getRating(MediaRating.USER);

    for (MediaFile mf : movie.getMediaFiles()) {
      mediaFiles.add(new MediaFile(mf));
    }

    initComponents();
    bindingGroup = initDataBindings();

    {
      int year = movieToEdit.getYear();

      List<MediaCertification> availableCertifications = MediaCertification
          .getCertificationsforCountry(MovieModuleManager.SETTINGS.getCertificationCountry());
      if (!availableCertifications.contains(movieToEdit.getCertification())) {
        availableCertifications.add(0, movieToEdit.getCertification());
      }
      for (MediaCertification cert : availableCertifications) {
        cbCertification.addItem(cert);
      }

      tfTitle.setText(movieToEdit.getTitle());
      tfOriginalTitle.setText(movieToEdit.getOriginalTitle());
      tfSorttitle.setText(movieToEdit.getSortTitle());
      spYear.setValue(year);
      spDateAdded.setValue(movieToEdit.getDateAdded());
      tfPoster.setText(movieToEdit.getArtworkUrl(MediaFileType.POSTER));
      tfFanart.setText(movieToEdit.getArtworkUrl(MediaFileType.FANART));
      tfLogo.setText(movieToEdit.getArtworkUrl(MediaFileType.LOGO));
      tfClearLogo.setText(movieToEdit.getArtworkUrl(MediaFileType.CLEARLOGO));
      tfClearArt.setText(movieToEdit.getArtworkUrl(MediaFileType.CLEARART));
      tfThumb.setText(movieToEdit.getArtworkUrl(MediaFileType.THUMB));
      tfDisc.setText(movieToEdit.getArtworkUrl(MediaFileType.DISC));
      tfBanner.setText(movieToEdit.getArtworkUrl(MediaFileType.BANNER));
      tfKeyart.setText(movieToEdit.getArtworkUrl(MediaFileType.KEYART));
      lblPoster.setImagePath(movieToEdit.getArtworkFilename(MediaFileType.POSTER));
      lblFanart.setImagePath(movieToEdit.getArtworkFilename(MediaFileType.FANART));
      lblLogo.setImagePath(movieToEdit.getArtworkFilename(MediaFileType.LOGO));
      lblClearlogo.setImagePath(movieToEdit.getArtworkFilename(MediaFileType.CLEARLOGO));
      lblClearart.setImagePath(movieToEdit.getArtworkFilename(MediaFileType.CLEARART));
      lblThumb.setImagePath(movieToEdit.getArtworkFilename(MediaFileType.THUMB));
      lblDisc.setImagePath(movieToEdit.getArtworkFilename(MediaFileType.DISC));
      lblBanner.setImagePath(movieToEdit.getArtworkFilename(MediaFileType.BANNER));
      lblKeyart.setImagePath(movieToEdit.getArtworkFilename(MediaFileType.KEYART));
      cbEdition.setSelectedItem(movieToEdit.getEdition());
      cbCertification.setSelectedItem(movieToEdit.getCertification());
      chckbxVideo3D.setSelected(movieToEdit.isVideoIn3D());
      cbSource.setSelectedItem(movieToEdit.getMediaSource());
      cbWatched.setSelected(movieToEdit.isWatched());
      tfTagline.setText(movieToEdit.getTagline());
      taPlot.setText(movieToEdit.getPlot());
      taPlot.setCaretPosition(0);
      spRuntime.setValue(movieToEdit.getRuntime());
      spTop250.setValue(movie.getTop250());
      tfProductionCompanies.setText(movieToEdit.getProductionCompany());
      tfSpokenLanguages.setText(movieToEdit.getSpokenLanguages());
      tfCountry.setText(movieToEdit.getCountry());
      spRating.setModel(new SpinnerNumberModel(userMediaRating.getRating(), 0.0, 10.0, 0.1));
      tfNote.setText(movieToEdit.getNote());

      for (Person origCast : movieToEdit.getActors()) {
        cast.add(new Person(origCast));
      }

      for (Person origProducer : movieToEdit.getProducers()) {
        producers.add(new Person(origProducer));
      }

      for (Person origDirector : movieToEdit.getDirectors()) {
        directors.add(new Person(origDirector));
      }

      for (Person origWriter : movieToEdit.getWriters()) {
        writers.add(new Person(origWriter));
      }

      genres.addAll(movieToEdit.getGenres());
      trailers.addAll(movieToEdit.getTrailer());

      for (String tag : movieToEdit.getTags()) {
        if (StringUtils.isNotBlank(tag)) {
          tags.add(tag);
        }
      }

      if (MovieModuleManager.SETTINGS.isImageExtraThumbs()) {
        extrathumbs = new ArrayList<>(movieToEdit.getExtraThumbs());
      }
      if (MovieModuleManager.SETTINGS.isImageExtraFanart()) {
        extrafanarts = new ArrayList<>(movieToEdit.getExtraFanarts());
      }
      for (MovieSet movieSet : movieList.getSortedMovieSetList()) {
        cbMovieSet.addItem(movieSet);
        if (movieToEdit.getMovieSet() == movieSet) {
          cbMovieSet.setSelectedItem(movieSet);
        }
      }
    }
    // adjust columnn titles - we have to do it this way - thx to windowbuilder pro
    tableTrailer.getColumnModel().getColumn(0).setHeaderValue(BUNDLE.getString("metatag.nfo"));
    tableTrailer.getColumnModel().getColumn(1).setHeaderValue(BUNDLE.getString("metatag.name"));
    tableTrailer.getColumnModel().getColumn(2).setHeaderValue(BUNDLE.getString("metatag.source"));
    tableTrailer.getColumnModel().getColumn(3).setHeaderValue(BUNDLE.getString("metatag.quality"));
    tableTrailer.getColumnModel().getColumn(4).setHeaderValue(BUNDLE.getString("metatag.url"));

    // adjust table columns
    tableTrailer.getColumnModel().getColumn(0).setMaxWidth(55);
    tableTrailer.adjustColumnPreferredWidths(5);

    tableRatings.adjustColumnPreferredWidths(5);

    // implement listener to simulate button group
    tableTrailer.getModel().addTableModelListener(arg0 -> {
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
    });
  }

  private void initComponents() {
    JTabbedPane tabbedPane = new MainTabbedPane() {
      private static final long serialVersionUID = 71548865608767532L;

      @Override
      public void updateUI() {
        putClientProperty("bottomBorder", Boolean.FALSE);
        super.updateUI();
      }
    };

    // to draw the shadow beneath window frame, encapsulate the panel
    JLayer<JComponent> rootLayer = new JLayer(tabbedPane, new ShadowLayerUI()); // removed <> because this leads WBP to crash
    getContentPane().add(rootLayer, BorderLayout.CENTER);

    /**********************************************************************************
     * DetailsPanel 1
     **********************************************************************************/
    {
      JPanel details1Panel = new JPanel();
      tabbedPane.addTab(BUNDLE.getString("metatag.details"), details1Panel);
      details1Panel.setLayout(new MigLayout("", "[][grow][50lp:75lp][][60lp:75lp][100lp:n][50lp:75lp,grow][25lp:n][200lp:250lp,grow]",
          "[][][][][100lp:175lp][][][][][][][75lp:100lp][]"));

      {
        JLabel lblTitle = new TmmLabel(BUNDLE.getString("metatag.title"));
        details1Panel.add(lblTitle, "cell 0 0,alignx right");

        tfTitle = new JTextField();
        details1Panel.add(tfTitle, "flowx,cell 1 0 6 1,growx,wmin 0");
      }
      {
        lblPoster = new ImageLabel();
        lblPoster.addMouseListener(new MouseAdapter() {
          @Override
          public void mouseClicked(MouseEvent e) {
            ImageChooserDialog dialog = new ImageChooserDialog(MovieEditorDialog.this, new HashMap<>(movieToEdit.getIds()), ImageType.POSTER,
                movieList.getDefaultArtworkScrapers(), lblPoster, null, null, MediaType.MOVIE);
            dialog.setLocationRelativeTo(MainWindow.getActiveInstance());
            dialog.setVisible(true);
            updateArtworkUrl(lblPoster, tfPoster);
          }
        });
        lblPoster.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        details1Panel.add(new TmmLabel(BUNDLE.getString("mediafiletype.poster")), "cell 8 0");

        LinkLabel lblPosterSize = new LinkLabel();
        details1Panel.add(lblPosterSize, "cell 8 0");

        JButton btnDeletePoster = new FlatButton(SPACER, IconManager.DELETE_GRAY);
        btnDeletePoster.setToolTipText(BUNDLE.getString("Button.deleteartwork.desc"));
        btnDeletePoster.addActionListener(e -> {
          lblPoster.clearImage();
          tfPoster.setText("");
        });
        details1Panel.add(btnDeletePoster, "cell 8 0");

        details1Panel.add(lblPoster, "cell 8 1 1 6,grow");
        lblPoster.addPropertyChangeListener(ORIGINAL_IMAGE_SIZE, e -> setImageSizeAndCreateLink(lblPosterSize, lblPoster, MediaFileType.POSTER));
      }
      {
        JLabel lblOriginalTitle = new TmmLabel(BUNDLE.getString("metatag.originaltitle"));
        details1Panel.add(lblOriginalTitle, "cell 0 1,alignx right");

        tfOriginalTitle = new JTextField();
        details1Panel.add(tfOriginalTitle, "cell 1 1 6 1,growx,wmin 0");
      }
      {
        JLabel lblSorttitle = new TmmLabel(BUNDLE.getString("metatag.sorttitle"));
        details1Panel.add(lblSorttitle, "cell 0 2,alignx right");

        tfSorttitle = new JTextField();
        details1Panel.add(tfSorttitle, "cell 1 2 6 1,growx,wmin 0");
      }
      {
        JLabel lblTagline = new TmmLabel(BUNDLE.getString("metatag.tagline"));
        details1Panel.add(lblTagline, "cell 0 3,alignx right,aligny top");

        tfTagline = new JTextField();
        details1Panel.add(tfTagline, "cell 1 3 6 1,growx,wmin 0");
      }
      {
        JLabel lblPlot = new TmmLabel(BUNDLE.getString("metatag.plot"));
        details1Panel.add(lblPlot, "cell 0 4,alignx right,aligny top");

        JScrollPane scrollPanePlot = new JScrollPane();
        details1Panel.add(scrollPanePlot, "cell 1 4 6 1,grow,wmin 0");

        taPlot = new JTextArea();
        taPlot.setLineWrap(true);
        taPlot.setWrapStyleWord(true);
        taPlot.setForeground(UIManager.getColor("TextField.foreground"));
        scrollPanePlot.setViewportView(taPlot);
      }

      {
        JLabel lblYear = new TmmLabel(BUNDLE.getString("metatag.year"));
        details1Panel.add(lblYear, "cell 0 5,alignx right");

        spYear = new YearSpinner();
        details1Panel.add(spYear, "cell 1 5,growx");
      }
      {
        JLabel lblReleaseDate = new TmmLabel(BUNDLE.getString("metatag.releasedate"));
        details1Panel.add(lblReleaseDate, "cell 3 5,alignx right");

        dpReleaseDate = new DatePicker(movieToEdit.getReleaseDate());
        details1Panel.add(dpReleaseDate, "cell 4 5 2 1,growx");
      }
      {
        JLabel lblCompany = new TmmLabel(BUNDLE.getString("metatag.production"));
        details1Panel.add(lblCompany, "cell 0 6,alignx right");

        tfProductionCompanies = new JTextField();
        details1Panel.add(tfProductionCompanies, "cell 1 6 6 1,growx,wmin 0");
      }
      {
        JLabel lblCountry = new TmmLabel(BUNDLE.getString("metatag.country"));
        details1Panel.add(lblCountry, "cell 0 7,alignx right");

        tfCountry = new JTextField();
        details1Panel.add(tfCountry, "cell 1 7 6 1,growx,wmin 0");
      }
      {
        JLabel lblSpokenLanguages = new TmmLabel(BUNDLE.getString("metatag.spokenlanguages"));
        details1Panel.add(lblSpokenLanguages, "cell 0 8,alignx right");

        tfSpokenLanguages = new JTextField();
        details1Panel.add(tfSpokenLanguages, "cell 1 8 6 1,growx,wmin 0");
      }

      {
        JLabel lblCertification = new TmmLabel(BUNDLE.getString("metatag.certification"));
        details1Panel.add(lblCertification, "cell 0 9,alignx right");

        cbCertification = new JComboBox();
        details1Panel.add(cbCertification, "cell 1 9,growx");
        cbCertification.setSelectedItem(movieToEdit.getCertification());
      }
      {
        JLabel lblRating = new TmmLabel(BUNDLE.getString("metatag.userrating"));
        details1Panel.add(lblRating, "cell 0 10,alignx right");

        spRating = new JSpinner();
        details1Panel.add(spRating, "cell 1 10,growx");
      }
      {
        JLabel lblRatingsT = new TmmLabel(BUNDLE.getString("metatag.ratings"));
        details1Panel.add(lblRatingsT, "flowy,cell 0 11,alignx right,aligny top");

        JScrollPane scrollPaneRatings = new JScrollPane();
        details1Panel.add(scrollPaneRatings, "cell 1 11 5 1,grow,wmin 0");

        tableRatings = new MediaRatingTable(mediaRatings);
        tableRatings.configureScrollPane(scrollPaneRatings);
        scrollPaneRatings.setViewportView(tableRatings);
      }
      {
        JLabel lblTop = new TmmLabel(BUNDLE.getString("metatag.top250"));
        details1Panel.add(lblTop, "cell 3 10,alignx right,aligny top");

        spTop250 = new JSpinner();
        details1Panel.add(spTop250, "cell 4 10,growx");
      }
      {
        lblFanart = new ImageLabel();
        lblFanart.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        lblFanart.addMouseListener(new MouseAdapter() {
          @Override
          public void mouseClicked(MouseEvent e) {
            ImageChooserDialog dialog = new ImageChooserDialog(MovieEditorDialog.this, new HashMap<>(movieToEdit.getIds()), ImageType.FANART,
                movieList.getDefaultArtworkScrapers(), lblFanart, extrathumbs, extrafanarts, MediaType.MOVIE);
            dialog.setLocationRelativeTo(MainWindow.getActiveInstance());
            dialog.setVisible(true);
            updateArtworkUrl(lblFanart, tfFanart);
          }
        });
        details1Panel.add(new TmmLabel(BUNDLE.getString("mediafiletype.fanart")), "cell 8 8");

        LinkLabel lblFanartSize = new LinkLabel();
        details1Panel.add(lblFanartSize, "cell 8 8");

        JButton btnDeleteFanart = new FlatButton(SPACER, IconManager.DELETE_GRAY);
        btnDeleteFanart.setToolTipText(BUNDLE.getString("Button.deleteartwork.desc"));
        btnDeleteFanart.addActionListener(e -> {
          lblFanart.clearImage();
          tfFanart.setText("");
        });
        details1Panel.add(btnDeleteFanart, "cell 8 8");

        details1Panel.add(lblFanart, "cell 8 9 1 4,grow");
        lblFanart.addPropertyChangeListener(ORIGINAL_IMAGE_SIZE, e -> setImageSizeAndCreateLink(lblFanartSize, lblFanart, MediaFileType.FANART));
      }

      JButton btnAddRating = new JButton(new AddRatingAction());
      btnAddRating.setMargin(BUTTON_MARGIN);
      details1Panel.add(btnAddRating, "cell 0 11,alignx right,aligny top");

      JButton btnRemoveRating = new JButton(new RemoveRatingAction());
      btnRemoveRating.setMargin(BUTTON_MARGIN);
      details1Panel.add(btnRemoveRating, "cell 0 11,alignx right,aligny top");
      {
        final JButton btnPlay = new JButton(IconManager.PLAY_INV);
        btnPlay.setFocusable(false);
        btnPlay.addActionListener(e -> {
          MediaFile mf = movieToEdit.getMainVideoFile();
          try {
            TmmUIHelper.openFile(mf.getFileAsPath());
          }
          catch (Exception ex) {
            LOGGER.error("open file - {}", e);
            MessageManager.instance
                .pushMessage(new Message(MessageLevel.ERROR, mf, "message.erroropenfile", new String[] { ":", ex.getLocalizedMessage() }));
          }
        });
        details1Panel.add(btnPlay, "cell 1 0 6 1");
      }
      {
        JLabel lblNoteT = new TmmLabel(BUNDLE.getString("metatag.note"));
        details1Panel.add(lblNoteT, "cell 0 12,alignx trailing");

        tfNote = new JTextField();
        details1Panel.add(tfNote, "cell 1 12 6 1,growx");
      }
    }

    /**********************************************************************************
     * DetailsPanel 2
     **********************************************************************************/
    {
      JPanel details2Panel = new JPanel();
      tabbedPane.addTab(BUNDLE.getString("metatag.details2"), details2Panel);

      details2Panel.setLayout(
          new MigLayout("", "[][][20lp:50lp][][50lp:100lp][20lp:n][][300lp:300lp]", "[][][][][][pref!][20lp:n][100lp:150lp,grow][][grow 200]"));
      {
        JLabel lblDateAdded = new TmmLabel(BUNDLE.getString("metatag.dateadded"));
        details2Panel.add(lblDateAdded, "cell 0 0,alignx right");

        spDateAdded = new JSpinner(new SpinnerDateModel());
        details2Panel.add(spDateAdded, "cell 1 0,growx");
      }
      {
        JLabel lblWatched = new TmmLabel(BUNDLE.getString("metatag.watched"));
        details2Panel.add(lblWatched, "flowx,cell 3 0");
      }
      {
        JLabel label = new TmmLabel(BUNDLE.getString("metatag.ids"));
        details2Panel.add(label, "flowy,cell 6 0 1 3,alignx right,aligny top");

        JScrollPane scrollPaneIds = new JScrollPane();
        details2Panel.add(scrollPaneIds, "cell 7 0 1 6,growx");

        tableIds = new MediaIdTable(ids);
        tableIds.configureScrollPane(scrollPaneIds);
        scrollPaneIds.setViewportView(tableIds);
      }
      {
        JLabel lblSourceT = new TmmLabel(BUNDLE.getString("metatag.source"));
        details2Panel.add(lblSourceT, "cell 0 1,alignx right");

        cbSource = new AutocompleteComboBox(MediaSource.values());
        details2Panel.add(cbSource, "cell 1 1,growx");
      }
      {
        JLabel label = new TmmLabel("3D");
        details2Panel.add(label, "flowx,cell 3 1");
      }
      {
        JLabel lblEditionT = new TmmLabel(BUNDLE.getString("metatag.edition"));
        details2Panel.add(lblEditionT, "cell 0 2,alignx right");

        cbEdition = new AutocompleteComboBox(MovieEdition.values());
        details2Panel.add(cbEdition, "cell 1 2 3 1");
      }
      {
        JLabel lblRuntime = new TmmLabel(BUNDLE.getString("metatag.runtime"));
        details2Panel.add(lblRuntime, "cell 0 3,alignx right");

        spRuntime = new JSpinner();
        details2Panel.add(spRuntime, "flowx,cell 1 3,growx");

        JLabel lblMin = new JLabel(BUNDLE.getString("metatag.minutes"));
        details2Panel.add(lblMin, "cell 1 3");
      }
      {
        JLabel lblMovieSet = new TmmLabel(BUNDLE.getString("metatag.movieset"));
        details2Panel.add(lblMovieSet, "cell 0 4,alignx right");

        cbMovieSet = new JComboBox();
        cbMovieSet.addItem("");
        details2Panel.add(cbMovieSet, "cell 1 4 4 1, growx, wmin 0");
      }
      {
        JLabel lblGenres = new TmmLabel(BUNDLE.getString("metatag.genre"));
        details2Panel.add(lblGenres, "flowy,cell 0 7,alignx right,aligny top");

        JScrollPane scrollPaneGenres = new JScrollPane();
        details2Panel.add(scrollPaneGenres, "cell 1 7 4 1,grow");

        listGenres = new JList();
        scrollPaneGenres.setViewportView(listGenres);

        cbGenres = new AutocompleteComboBox(MediaGenres.values());
        cbGenresAutoCompleteSupport = cbGenres.getAutoCompleteSupport();
        InputMap im = cbGenres.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        Object enterAction = im.get(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0));
        cbGenres.getActionMap().put(enterAction, new AddGenreAction());
        details2Panel.add(cbGenres, "cell 1 8 4 1, growx, wmin 0");
      }
      {
        JLabel lblTags = new TmmLabel(BUNDLE.getString("metatag.tags"));
        details2Panel.add(lblTags, "flowy,cell 6 7,alignx right,aligny top");

        JScrollPane scrollPaneTags = new JScrollPane();
        details2Panel.add(scrollPaneTags, "cell 7 7,grow");

        listTags = new JList();
        scrollPaneTags.setViewportView(listTags);

        cbTags = new AutocompleteComboBox<>(movieList.getTagsInMovies());
        cbTagsAutoCompleteSupport = cbTags.getAutoCompleteSupport();
        InputMap im = cbTags.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        Object enterAction = im.get(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0));
        cbTags.getActionMap().put(enterAction, new AddTagAction());
        details2Panel.add(cbTags, "cell 7 8, growx, wmin 0");
      }

      {
        JButton btnAddGenre = new JButton(new AddGenreAction());
        btnAddGenre.setMargin(BUTTON_MARGIN);
        details2Panel.add(btnAddGenre, "cell 0 7,alignx right,aligny top");
      }

      {
        JButton btnRemoveGenre = new JButton(new RemoveGenreAction());
        btnRemoveGenre.setMargin(BUTTON_MARGIN);
        details2Panel.add(btnRemoveGenre, "cell 0 7,alignx right,aligny top");
      }
      {
        JButton btnMoveGenreUp = new JButton(new MoveGenreUpAction());
        btnMoveGenreUp.setMargin(BUTTON_MARGIN);
        details2Panel.add(btnMoveGenreUp, "cell 0 7,alignx right,aligny top");
      }
      {
        JButton btnMoveGenreDown = new JButton(new MoveGenreDownAction());
        btnMoveGenreDown.setMargin(BUTTON_MARGIN);
        details2Panel.add(btnMoveGenreDown, "cell 0 7,alignx right,aligny top");
      }
      {
        JButton btnAddTag = new JButton(new AddTagAction());
        btnAddTag.setMargin(BUTTON_MARGIN);
        details2Panel.add(btnAddTag, "cell 6 7,alignx right,aligny top");
      }
      {
        JButton btnRemoveTag = new JButton(new RemoveTagAction());
        btnRemoveTag.setMargin(BUTTON_MARGIN);
        details2Panel.add(btnRemoveTag, "cell 6 7,alignx right,aligny top");
      }
      {
        JButton btnMoveTagUp = new JButton(new MoveTagUpAction());
        btnMoveTagUp.setMargin(BUTTON_MARGIN);
        details2Panel.add(btnMoveTagUp, "cell 6 7,alignx right,aligny top");
      }
      {
        JButton btnMoveTagDown = new JButton(new MoveTagDownAction());
        btnMoveTagDown.setMargin(BUTTON_MARGIN);
        details2Panel.add(btnMoveTagDown, "cell 6 7,alignx right,aligny top");
      }
      {
        JButton btnAddId = new JButton(new AddIdAction());
        btnAddId.setMargin(BUTTON_MARGIN);
        details2Panel.add(btnAddId, "cell 6 0 1 3,alignx right,aligny top");
      }
      {
        JButton btnRemoveId = new JButton(new RemoveIdAction());
        btnRemoveId.setMargin(BUTTON_MARGIN);
        details2Panel.add(btnRemoveId, "cell 6 0 1 3,alignx right,aligny top");
      }
      {
        cbWatched = new JCheckBox("");
        details2Panel.add(cbWatched, "cell 3 0");
      }
      {
        chckbxVideo3D = new JCheckBox("");
        details2Panel.add(chckbxVideo3D, "cell 3 1");
      }
    }

    /**********************************************************************************
     * CrewPanel
     **********************************************************************************/
    {
      JPanel crewPanel = new JPanel();
      tabbedPane.addTab(BUNDLE.getString("movie.edit.castandcrew"), null, crewPanel, null);
      crewPanel
          .setLayout(new MigLayout("", "[][150lp:300lp,grow][20lp:n][][150lp:300lp,grow]", "[100lp:250lp,grow][20lp:n][100lp:200lp,grow][grow]"));
      {
        JLabel lblActors = new TmmLabel(BUNDLE.getString("metatag.actors"));
        crewPanel.add(lblActors, "flowy,cell 0 0,alignx right,aligny top");

        JScrollPane scrollPane = new JScrollPane();
        crewPanel.add(scrollPane, "cell 1 0,grow");

        tableActors = new PersonTable(cast, true);
        tableActors.configureScrollPane(scrollPane);
      }
      {
        JLabel lblProducers = new TmmLabel(BUNDLE.getString("metatag.producers"));
        crewPanel.add(lblProducers, "flowy,cell 3 0,alignx right,aligny top");

        JScrollPane scrollPane = new JScrollPane();
        crewPanel.add(scrollPane, "cell 4 0,grow");

        tableProducers = new PersonTable(producers, true);
        tableProducers.configureScrollPane(scrollPane);
      }
      {
        JLabel lblDirectorsT = new TmmLabel(BUNDLE.getString("metatag.directors"));
        crewPanel.add(lblDirectorsT, "flowy,cell 0 2,alignx right,aligny top");

        JScrollPane scrollPane = new JScrollPane();
        crewPanel.add(scrollPane, "cell 1 2,grow");

        tableDirectors = new PersonTable(directors, true);
        tableDirectors.configureScrollPane(scrollPane);
      }
      {
        JLabel lblWritersT = new TmmLabel(BUNDLE.getString("metatag.writers"));
        crewPanel.add(lblWritersT, "flowy,cell 3 2,alignx right,aligny top");

        JScrollPane scrollPane = new JScrollPane();
        crewPanel.add(scrollPane, "cell 4 2,grow");

        tableWriters = new PersonTable(writers, true);
        tableWriters.configureScrollPane(scrollPane);
      }
      {
        JButton btnAddActor = new JButton(new AddActorAction());
        btnAddActor.setMargin(BUTTON_MARGIN);
        crewPanel.add(btnAddActor, "cell 0 0,alignx right");
      }
      {
        JButton btnRemoveActor = new JButton(new RemoveActorAction());
        btnRemoveActor.setMargin(BUTTON_MARGIN);
        crewPanel.add(btnRemoveActor, "cell 0 0,alignx right");
      }
      {
        JButton btnMoveActorUp = new JButton(new MoveActorUpAction());
        btnMoveActorUp.setMargin(BUTTON_MARGIN);
        crewPanel.add(btnMoveActorUp, "cell 0 0,alignx right");
      }
      {
        JButton btnMoveActorDown = new JButton(new MoveActorDownAction());
        btnMoveActorDown.setMargin(BUTTON_MARGIN);
        crewPanel.add(btnMoveActorDown, "cell 0 0,alignx right,aligny top");
      }
      {
        JButton btnAddProducer = new JButton(new AddProducerAction());
        btnAddProducer.setMargin(BUTTON_MARGIN);
        crewPanel.add(btnAddProducer, "cell 3 0,alignx right");
      }
      {
        JButton btnRemoveProducer = new JButton(new RemoveProducerAction());
        btnRemoveProducer.setMargin(BUTTON_MARGIN);
        crewPanel.add(btnRemoveProducer, "cell 3 0,alignx right");
      }
      {
        JButton btnMoveProducerUp = new JButton(new MoveProducerUpAction());
        btnMoveProducerUp.setMargin(BUTTON_MARGIN);
        crewPanel.add(btnMoveProducerUp, "cell 3 0,alignx right");
      }
      {
        JButton btnMoveProducerDown = new JButton(new MoveProducerDownAction());
        btnMoveProducerDown.setMargin(BUTTON_MARGIN);
        crewPanel.add(btnMoveProducerDown, "cell 3 0,alignx right,aligny top");
      }
      {
        JButton btnAddDirector = new JButton(new AddDirectorAction());
        btnAddDirector.setMargin(BUTTON_MARGIN);
        crewPanel.add(btnAddDirector, "cell 0 2,alignx right");
      }
      {
        JButton btnRemoveDirector = new JButton(new RemoveDirectorAction());
        btnRemoveDirector.setMargin(BUTTON_MARGIN);
        crewPanel.add(btnRemoveDirector, "cell 0 2,alignx right");
      }
      {
        JButton btnMoveDirectorUp = new JButton(new MoveDirectorUpAction());
        btnMoveDirectorUp.setMargin(BUTTON_MARGIN);
        crewPanel.add(btnMoveDirectorUp, "cell 0 2,alignx right");
      }
      {
        JButton btnMoveDirectorDown = new JButton(new MoveDirectorDownAction());
        btnMoveDirectorDown.setMargin(BUTTON_MARGIN);
        crewPanel.add(btnMoveDirectorDown, "cell 0 2,alignx right,aligny top");
      }
      {
        JButton btnAddWriter = new JButton(new AddWriterAction());
        btnAddWriter.setMargin(BUTTON_MARGIN);
        crewPanel.add(btnAddWriter, "cell 3 2,alignx right");
      }
      {
        JButton btnRemoveWriter = new JButton(new RemoveWriterAction());
        btnRemoveWriter.setMargin(BUTTON_MARGIN);
        crewPanel.add(btnRemoveWriter, "cell 3 2,alignx right");
      }
      {
        JButton btnMoveWriterUp = new JButton(new MoveWriterUpAction());
        btnMoveWriterUp.setMargin(BUTTON_MARGIN);
        crewPanel.add(btnMoveWriterUp, "cell 3 2,alignx right");
      }
      {
        JButton btnMoveWriterDown = new JButton(new MoveWriterDownAction());
        btnMoveWriterDown.setMargin(BUTTON_MARGIN);
        crewPanel.add(btnMoveWriterDown, "cell 3 2,alignx right,aligny top");
      }
    }

    /**********************************************************************************
     * local artwork
     **********************************************************************************/
    {
      JPanel artworkPanel = new JPanel();
      tabbedPane.addTab(BUNDLE.getString("metatag.extraartwork"), null, artworkPanel, null);
      artworkPanel.setLayout(new MigLayout("", "[200lp:300lp,grow][20lp:n][200lp:300lp,grow][20lp:n][100lp:200lp,grow]",
          "[][100lp:125lp,grow][20lp:n][][100lp:125lp,grow][20lp:n][][100lp:150lp,grow]"));
      {
        JLabel lblLogoT = new TmmLabel(BUNDLE.getString("mediafiletype.logo"));
        artworkPanel.add(lblLogoT, "cell 0 0");

        LinkLabel lblLogoSize = new LinkLabel();
        artworkPanel.add(lblLogoSize, "cell 0 0");

        JButton btnDeleteLogo = new FlatButton(SPACER, IconManager.DELETE_GRAY);
        btnDeleteLogo.setToolTipText(BUNDLE.getString("Button.deleteartwork.desc"));
        btnDeleteLogo.addActionListener(e -> {
          lblLogo.clearImage();
          tfLogo.setText("");
        });
        artworkPanel.add(btnDeleteLogo, "cell 0 0");

        lblLogo = new ImageLabel();
        lblLogo.addMouseListener(new MouseAdapter() {
          @Override
          public void mouseClicked(MouseEvent e) {
            ImageChooserDialog dialog = new ImageChooserDialog(MovieEditorDialog.this, new HashMap<>(movieToEdit.getIds()), ImageType.LOGO,
                movieList.getDefaultArtworkScrapers(), lblLogo, null, null, MediaType.MOVIE);
            dialog.setLocationRelativeTo(MainWindow.getActiveInstance());
            dialog.setVisible(true);
            updateArtworkUrl(lblLogo, tfLogo);

          }
        });
        lblLogo.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        artworkPanel.add(lblLogo, "cell 0 1,grow");
        lblLogo.addPropertyChangeListener(ORIGINAL_IMAGE_SIZE, e -> setImageSizeAndCreateLink(lblLogoSize, lblLogo, MediaFileType.LOGO));

      }
      {
        JLabel lblKeyartT = new TmmLabel(BUNDLE.getString("mediafiletype.keyart"));
        artworkPanel.add(lblKeyartT, "cell 4 0");

        LinkLabel lblKeyartSize = new LinkLabel();
        artworkPanel.add(lblKeyartSize, "cell 4 0");

        JButton btnDeleteKeyart = new FlatButton(SPACER, IconManager.DELETE_GRAY);
        btnDeleteKeyart.setToolTipText(BUNDLE.getString("Button.deleteartwork.desc"));
        btnDeleteKeyart.addActionListener(e -> {
          lblKeyart.clearImage();
          tfKeyart.setText("");
        });
        artworkPanel.add(btnDeleteKeyart, "cell 4 0");

        lblKeyart = new ImageLabel();
        lblKeyart.addMouseListener(new MouseAdapter() {
          @Override
          public void mouseClicked(MouseEvent e) {
            ImageChooserDialog dialog = new ImageChooserDialog(MovieEditorDialog.this, new HashMap<>(movieToEdit.getIds()), ImageType.KEYART,
                movieList.getDefaultArtworkScrapers(), lblKeyart, null, null, MediaType.MOVIE);
            dialog.setLocationRelativeTo(MainWindow.getActiveInstance());
            dialog.setVisible(true);
            updateArtworkUrl(lblKeyart, tfKeyart);
          }
        });
        lblKeyart.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        artworkPanel.add(lblKeyart, "cell 4 1 1 4,grow");
        lblKeyart.addPropertyChangeListener(ORIGINAL_IMAGE_SIZE, e -> setImageSizeAndCreateLink(lblKeyartSize, lblKeyart, MediaFileType.KEYART));
      }
      {
        JLabel lblClearlogoT = new TmmLabel(BUNDLE.getString("mediafiletype.clearlogo"));
        artworkPanel.add(lblClearlogoT, "cell 2 0");

        LinkLabel lblClearlogoSize = new LinkLabel();
        artworkPanel.add(lblClearlogoSize, "cell 2 0");

        JButton btnDeleteClearLogo = new FlatButton(SPACER, IconManager.DELETE_GRAY);
        btnDeleteClearLogo.setToolTipText(BUNDLE.getString("Button.deleteartwork.desc"));
        btnDeleteClearLogo.addActionListener(e -> {
          lblClearlogo.clearImage();
          tfClearLogo.setText("");
        });
        artworkPanel.add(btnDeleteClearLogo, "cell 2 0");

        lblClearlogo = new ImageLabel();
        lblClearlogo.addMouseListener(new MouseAdapter() {
          @Override
          public void mouseClicked(MouseEvent e) {
            ImageChooserDialog dialog = new ImageChooserDialog(MovieEditorDialog.this, new HashMap<>(movieToEdit.getIds()), ImageType.CLEARLOGO,
                movieList.getDefaultArtworkScrapers(), lblClearlogo, null, null, MediaType.MOVIE);
            dialog.setLocationRelativeTo(MainWindow.getActiveInstance());
            dialog.setVisible(true);
            updateArtworkUrl(lblClearlogo, tfClearLogo);
          }
        });
        lblClearlogo.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        artworkPanel.add(lblClearlogo, "cell 2 1,grow");
        lblClearlogo.addPropertyChangeListener(ORIGINAL_IMAGE_SIZE,
            e -> setImageSizeAndCreateLink(lblClearlogoSize, lblClearlogo, MediaFileType.CLEARLOGO));
      }
      {
        JLabel lblBannerT = new TmmLabel(BUNDLE.getString("mediafiletype.banner"));
        artworkPanel.add(lblBannerT, "cell 0 3");

        LinkLabel lblBannerSize = new LinkLabel();
        artworkPanel.add(lblBannerSize, "cell 0 3");

        JButton btnDeleteBanner = new FlatButton(SPACER, IconManager.DELETE_GRAY);
        btnDeleteBanner.setToolTipText(BUNDLE.getString("Button.deleteartwork.desc"));
        btnDeleteBanner.addActionListener(e -> {
          lblBanner.clearImage();
          tfBanner.setText("");
        });
        artworkPanel.add(btnDeleteBanner, "cell 0 3");

        lblBanner = new ImageLabel();
        lblBanner.addMouseListener(new MouseAdapter() {
          @Override
          public void mouseClicked(MouseEvent e) {
            ImageChooserDialog dialog = new ImageChooserDialog(MovieEditorDialog.this, new HashMap<>(movieToEdit.getIds()), ImageType.BANNER,
                movieList.getDefaultArtworkScrapers(), lblBanner, null, null, MediaType.MOVIE);
            dialog.setLocationRelativeTo(MainWindow.getActiveInstance());
            dialog.setVisible(true);
            updateArtworkUrl(lblBanner, tfBanner);
          }
        });
        lblBanner.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        artworkPanel.add(lblBanner, "cell 0 4 3 1,grow");
        lblBanner.addPropertyChangeListener(ORIGINAL_IMAGE_SIZE, e -> setImageSizeAndCreateLink(lblBannerSize, lblBanner, MediaFileType.BANNER));
      }
      {
        JLabel lblClearartT = new TmmLabel("ClearArt");
        artworkPanel.add(lblClearartT, "cell 0 6");

        LinkLabel lblClearartSize = new LinkLabel();
        artworkPanel.add(lblClearartSize, "cell 0 6");

        JButton btnDeleteClearart = new FlatButton(SPACER, IconManager.DELETE_GRAY);
        btnDeleteClearart.setToolTipText(BUNDLE.getString("Button.deleteartwork.desc"));
        btnDeleteClearart.addActionListener(e -> {
          lblClearart.clearImage();
          tfClearArt.setText("");
        });
        artworkPanel.add(btnDeleteClearart, "cell 0 6");

        lblClearart = new ImageLabel();
        lblClearart.addMouseListener(new MouseAdapter() {
          @Override
          public void mouseClicked(MouseEvent e) {
            ImageChooserDialog dialog = new ImageChooserDialog(MovieEditorDialog.this, new HashMap<>(movieToEdit.getIds()), ImageType.CLEARART,
                movieList.getDefaultArtworkScrapers(), lblClearart, null, null, MediaType.MOVIE);
            dialog.setLocationRelativeTo(MainWindow.getActiveInstance());
            dialog.setVisible(true);
            updateArtworkUrl(lblClearart, tfClearArt);
          }
        });
        lblClearart.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        artworkPanel.add(lblClearart, "cell 0 7,grow");
        lblClearart.addPropertyChangeListener(ORIGINAL_IMAGE_SIZE,
            e -> setImageSizeAndCreateLink(lblClearartSize, lblClearart, MediaFileType.CLEARART));

      }
      {
        JLabel lblThumbT = new TmmLabel("Thumb");
        artworkPanel.add(lblThumbT, "cell 2 6");

        LinkLabel lblThumbSize = new LinkLabel();
        artworkPanel.add(lblThumbSize, "cell 2 6");

        JButton btnDeleteThumb = new FlatButton(SPACER, IconManager.DELETE_GRAY);
        btnDeleteThumb.setToolTipText(BUNDLE.getString("Button.deleteartwork.desc"));
        btnDeleteThumb.addActionListener(e -> {
          lblThumb.clearImage();
          tfThumb.setText("");
        });
        artworkPanel.add(btnDeleteThumb, "cell 2 6");

        lblThumb = new ImageLabel();
        lblThumb.addMouseListener(new MouseAdapter() {
          @Override
          public void mouseClicked(MouseEvent e) {
            ImageChooserDialog dialog = new ImageChooserDialog(MovieEditorDialog.this, new HashMap<>(movieToEdit.getIds()), ImageType.THUMB,
                movieList.getDefaultArtworkScrapers(), lblThumb, null, null, MediaType.MOVIE);
            dialog.setLocationRelativeTo(MainWindow.getActiveInstance());
            dialog.setVisible(true);
            updateArtworkUrl(lblThumb, tfThumb);
          }
        });
        lblThumb.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        artworkPanel.add(lblThumb, "cell 2 7,grow");
        lblThumb.addPropertyChangeListener(ORIGINAL_IMAGE_SIZE, e -> setImageSizeAndCreateLink(lblThumbSize, lblThumb, MediaFileType.THUMB));
      }
      {
        JLabel lblDiscT = new TmmLabel("Disc");
        artworkPanel.add(lblDiscT, "cell 4 6");

        LinkLabel lblDiscSize = new LinkLabel();
        artworkPanel.add(lblDiscSize, "cell 4 6");

        JButton btnDeleteDisc = new FlatButton(SPACER, IconManager.DELETE_GRAY);
        btnDeleteDisc.setToolTipText(BUNDLE.getString("Button.deleteartwork.desc"));
        btnDeleteDisc.addActionListener(e -> {
          lblDisc.clearImage();
          tfDisc.setText("");
        });
        artworkPanel.add(btnDeleteDisc, "cell 4 6");

        lblDisc = new ImageLabel();
        lblDisc.addMouseListener(new MouseAdapter() {
          @Override
          public void mouseClicked(MouseEvent e) {
            ImageChooserDialog dialog = new ImageChooserDialog(MovieEditorDialog.this, new HashMap<>(movieToEdit.getIds()), ImageType.DISC,
                movieList.getDefaultArtworkScrapers(), lblDisc, null, null, MediaType.MOVIE);
            dialog.setLocationRelativeTo(MainWindow.getActiveInstance());
            dialog.setVisible(true);
            updateArtworkUrl(lblDisc, tfDisc);
          }
        });
        lblDisc.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        artworkPanel.add(lblDisc, "cell 4 7,grow");
        lblDisc.addPropertyChangeListener(ORIGINAL_IMAGE_SIZE, e -> setImageSizeAndCreateLink(lblDiscSize, lblDisc, MediaFileType.DISC));
      }
    }

    /**********************************************************************************
     * artwork and trailer urls
     **********************************************************************************/
    {
      JPanel artworkAndTrailerPanel = new JPanel();
      tabbedPane.addTab(BUNDLE.getString("edit.artworkandtrailer"), null, artworkAndTrailerPanel, null);
      artworkAndTrailerPanel.setLayout(new MigLayout("", "[][grow]", "[][][][][][][][][][20lp:n][250lp]"));
      {
        JLabel lblPosterT = new TmmLabel(BUNDLE.getString("mediafiletype.poster"));
        artworkAndTrailerPanel.add(lblPosterT, "cell 0 0,alignx right");
      }
      {
        tfPoster = new JTextField();
        artworkAndTrailerPanel.add(tfPoster, "cell 1 0,growx");

        JLabel lblFanartT = new TmmLabel(BUNDLE.getString("mediafiletype.fanart"));
        artworkAndTrailerPanel.add(lblFanartT, "cell 0 1,alignx right");

        tfFanart = new JTextField();
        artworkAndTrailerPanel.add(tfFanart, "cell 1 1,growx");
      }
      {
        JLabel lblLogoT = new TmmLabel(BUNDLE.getString("mediafiletype.logo"));
        artworkAndTrailerPanel.add(lblLogoT, "cell 0 2,alignx right");

        tfLogo = new JTextField();
        artworkAndTrailerPanel.add(tfLogo, "cell 1 2,growx");
      }
      {
        JLabel lblClearLogoT = new TmmLabel(BUNDLE.getString("mediafiletype.clearlogo"));
        artworkAndTrailerPanel.add(lblClearLogoT, "cell 0 3,alignx right");

        tfClearLogo = new JTextField();
        artworkAndTrailerPanel.add(tfClearLogo, "cell 1 3,growx");
      }
      {
        JLabel lblBannerT = new TmmLabel(BUNDLE.getString("mediafiletype.banner"));
        artworkAndTrailerPanel.add(lblBannerT, "cell 0 4,alignx right");

        tfBanner = new JTextField();
        artworkAndTrailerPanel.add(tfBanner, "cell 1 4,growx");
      }
      {
        JLabel lblClearArtT = new TmmLabel(BUNDLE.getString("mediafiletype.clearart"));
        artworkAndTrailerPanel.add(lblClearArtT, "cell 0 5,alignx right");

        tfClearArt = new JTextField();
        artworkAndTrailerPanel.add(tfClearArt, "cell 1 5,growx");
      }
      {
        JLabel lblThumbT = new TmmLabel(BUNDLE.getString("mediafiletype.thumb"));
        artworkAndTrailerPanel.add(lblThumbT, "cell 0 6,alignx right");

        tfThumb = new JTextField();
        artworkAndTrailerPanel.add(tfThumb, "cell 1 6,growx");
      }
      {
        JLabel lblDiscT = new TmmLabel(BUNDLE.getString("mediafiletype.disc"));
        artworkAndTrailerPanel.add(lblDiscT, "cell 0 7,alignx trailing");

        tfDisc = new JTextField();
        artworkAndTrailerPanel.add(tfDisc, "cell 1 7,growx");
      }
      {
        JLabel lblKeyartT = new TmmLabel(BUNDLE.getString("mediafiletype.keyart"));
        artworkAndTrailerPanel.add(lblKeyartT, "cell 0 8,alignx trailing");

        tfKeyart = new JTextField();
        artworkAndTrailerPanel.add(tfKeyart, "cell 1 8,growx");
      }

      {
        JLabel lblTrailer = new TmmLabel(BUNDLE.getString("metatag.trailer"));
        artworkAndTrailerPanel.add(lblTrailer, "flowy,cell 0 10,alignx right,aligny top");

        JButton btnAddTrailer = new JButton(new AddTrailerAction());
        btnAddTrailer.setMargin(BUTTON_MARGIN);
        artworkAndTrailerPanel.add(btnAddTrailer, "cell 0 10,alignx right,aligny top");

        JButton btnRemoveTrailer = new JButton(new RemoveTrailerAction());
        btnRemoveTrailer.setMargin(BUTTON_MARGIN);
        artworkAndTrailerPanel.add(btnRemoveTrailer, "cell 0 10,alignx right,aligny top");

        JScrollPane scrollPaneTrailer = new JScrollPane();
        artworkAndTrailerPanel.add(scrollPaneTrailer, "cell 1 10 7 1,grow");
        tableTrailer = new TmmTable();
        tableTrailer.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
        tableTrailer.configureScrollPane(scrollPaneTrailer);
        scrollPaneTrailer.setViewportView(tableTrailer);
      }
    }

    /**********************************************************************************
     * MediaFilesPanel
     **********************************************************************************/
    {
      mediaFilesPanel = new MediaFileEditorPanel(mediaFiles);
      tabbedPane.addTab(BUNDLE.getString("metatag.mediafiles"), null, mediaFilesPanel, null);
      mediaFilesPanel.setLayout(new MigLayout("", "[400lp:500lp,grow,fill]", "[300lp:400lp,grow,fill]"));
    }

    /**********************************************************************************
     * ButtonPanel
     **********************************************************************************/
    {
      if (queueSize > 1) {
        JButton btnAbort = new JButton(new AbortQueueAction());
        addButton(btnAbort);
        if (queueIndex > 0) {
          JButton backButton = new JButton(new NavigateBackAction());
          addButton(backButton);
        }
      }

      JButton cancelButton = new JButton(new DiscardAction());
      addButton(cancelButton);

      JButton okButton = new JButton(new ChangeMovieAction());
      addDefaultButton(okButton);
    }
  }

  private void updateArtworkUrl(ImageLabel imageLabel, JTextField textField) {
    if (StringUtils.isNotBlank(imageLabel.getImageUrl())) {
      textField.setText(imageLabel.getImageUrl());
    }
  }

  public boolean isContinueQueue() {
    return continueQueue;
  }

  public boolean isNavigateBack() {
    return navigateBack;
  }

  private class ChangeMovieAction extends AbstractAction {
    private static final long serialVersionUID = -3767744690599233490L;

    public ChangeMovieAction() {
      putValue(NAME, BUNDLE.getString("Button.ok"));
      putValue(SHORT_DESCRIPTION, BUNDLE.getString("movie.change"));
      putValue(SMALL_ICON, IconManager.APPLY_INV);
      putValue(LARGE_ICON_KEY, IconManager.APPLY_INV);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      movieToEdit.setTitle(tfTitle.getText());
      movieToEdit.setOriginalTitle(tfOriginalTitle.getText());
      movieToEdit.setTagline(tfTagline.getText());
      movieToEdit.setPlot(taPlot.getText());
      movieToEdit.setYear((Integer) spYear.getValue());
      movieToEdit.setReleaseDate(dpReleaseDate.getDate());
      movieToEdit.setRuntime((Integer) spRuntime.getValue());
      movieToEdit.setTop250((Integer) spTop250.getValue());
      movieToEdit.setWatched(cbWatched.isSelected());
      movieToEdit.setSpokenLanguages(tfSpokenLanguages.getText());
      movieToEdit.setCountry(tfCountry.getText());
      movieToEdit.setVideoIn3D(chckbxVideo3D.isSelected());
      movieToEdit.setNote(tfNote.getText());

      Object movieEdition = cbEdition.getSelectedItem();
      if (movieEdition instanceof MovieEdition) {
        movieToEdit.setEdition((MovieEdition) movieEdition);
      }
      else if (movieEdition instanceof String) {
        movieToEdit.setEdition(MovieEdition.getMovieEdition((String) movieEdition));
      }
      else {
        movieToEdit.setEdition(MovieEdition.NONE);
      }

      Object mediaSource = cbSource.getSelectedItem();
      if (mediaSource instanceof MediaSource) {
        movieToEdit.setMediaSource((MediaSource) mediaSource);
      }
      else if (mediaSource instanceof String) {
        movieToEdit.setMediaSource(MediaSource.getMediaSource((String) mediaSource));
      }
      else {
        movieToEdit.setMediaSource(MediaSource.UNKNOWN);
      }

      // sync of media ids
      // first round -> add existing ids
      for (MediaId id : ids) {
        // only process non empty ids
        // changed; if empty/0/null value gets set, it is removed in setter ;)
        // if (StringUtils.isAnyBlank(id.key, id.value)) {
        // continue;
        // }
        // first try to cast it into an Integer
        try {
          Integer value = Integer.parseInt(id.value);
          // cool, it is an Integer
          movieToEdit.setId(id.key, value);
        }
        catch (NumberFormatException ex) {
          // okay, we set it as a String
          movieToEdit.setId(id.key, id.value);
        }
      }
      // second round -> remove deleted ids
      List<String> removeIds = new ArrayList<>();
      for (Entry<String, Object> entry : movieToEdit.getIds().entrySet()) {
        MediaId id = new MediaId(entry.getKey());
        if (!ids.contains(id)) {
          removeIds.add(entry.getKey());
        }
      }
      for (String id : removeIds) {
        // set a null value causes to fire the right events
        movieToEdit.setId(id, null);
      }

      Object certification = cbCertification.getSelectedItem();
      if (certification instanceof MediaCertification) {
        movieToEdit.setCertification((MediaCertification) certification);
      }

      // sync media files with the media file editor and fire the mediaFiles event
      MediaFileEditorPanel.syncMediaFiles(mediaFiles, movieToEdit.getMediaFiles());
      movieToEdit.fireEventForChangedMediaInformation();

      // process artwork
      processArtwork(MediaFileType.POSTER, lblPoster, tfPoster);
      processArtwork(MediaFileType.FANART, lblFanart, tfFanart);
      processArtwork(MediaFileType.LOGO, lblLogo, tfLogo);
      processArtwork(MediaFileType.CLEARLOGO, lblClearlogo, tfClearLogo);
      processArtwork(MediaFileType.BANNER, lblBanner, tfBanner);
      processArtwork(MediaFileType.CLEARART, lblClearart, tfClearArt);
      processArtwork(MediaFileType.THUMB, lblThumb, tfThumb);
      processArtwork(MediaFileType.DISC, lblDisc, tfDisc);
      processArtwork(MediaFileType.KEYART, lblKeyart, tfKeyart);

      // set extrathumbs
      if (extrathumbs != null && (extrathumbs.size() != movieToEdit.getExtraThumbs().size() || !extrathumbs.containsAll(movieToEdit.getExtraThumbs())
          || !movieToEdit.getExtraThumbs().containsAll(extrathumbs))) {
        movieToEdit.setExtraThumbs(extrathumbs);
        movieToEdit.downloadArtwork(MediaFileType.EXTRATHUMB);
      }

      // set extrafanarts
      if (extrafanarts != null && (extrafanarts.size() != movieToEdit.getExtraFanarts().size()
          || !extrafanarts.containsAll(movieToEdit.getExtraFanarts()) || !movieToEdit.getExtraFanarts().containsAll(extrafanarts))) {
        movieToEdit.setExtraFanarts(extrafanarts);
        movieToEdit.downloadArtwork(MediaFileType.EXTRAFANART);
      }

      movieToEdit.setProductionCompany(tfProductionCompanies.getText());
      movieToEdit.setActors(cast);
      movieToEdit.setProducers(producers);
      movieToEdit.setDirectors(directors);
      movieToEdit.setWriters(writers);
      movieToEdit.setGenres(genres);

      movieToEdit.removeAllTrailers();
      for (MediaTrailer trailer : trailers) {
        movieToEdit.addTrailer(trailer);
      }

      movieToEdit.setTags(tags);
      movieToEdit.setDateAdded((Date) spDateAdded.getValue());
      movieToEdit.setSortTitle(tfSorttitle.getText());

      // movie set
      Object obj = cbMovieSet.getSelectedItem();
      if (obj instanceof String) {
        movieToEdit.removeFromMovieSet();
      }
      if (obj instanceof MovieSet) {
        MovieSet movieSet = (MovieSet) obj;

        if (movieToEdit.getMovieSet() != movieSet) {
          movieToEdit.removeFromMovieSet();
          movieToEdit.setMovieSet(movieSet);
          movieSet.insertMovie(movieToEdit);
        }
      }

      // user rating
      Map<String, MediaRating> newRatings = new HashMap<>();

      if ((double) spRating.getValue() > 0) {
        newRatings.put(MediaRating.USER, new MediaRating(MediaRating.USER, (double) spRating.getValue(), 1, 10));
      }

      // other ratings
      for (MediaRatingTable.MediaRating mediaRating : MovieEditorDialog.this.mediaRatings) {
        if (StringUtils.isNotBlank(mediaRating.key) && mediaRating.value > 0) {
          newRatings.put(mediaRating.key, new MediaRating(mediaRating.key, mediaRating.value, mediaRating.votes, mediaRating.maxValue));
        }
      }
      movieToEdit.setRatings(newRatings);

      movieToEdit.writeNFO();
      movieToEdit.saveToDb();

      // if configured - sync with trakt.tv
      if (MovieModuleManager.SETTINGS.getSyncTrakt()) {
        TmmTask task = new SyncTraktTvTask(Collections.singletonList(movieToEdit), null);
        TmmTaskManager.getInstance().addUnnamedTask(task);
      }

      setVisible(false);
    }
  }

  private void processArtwork(MediaFileType type, ImageLabel imageLabel, JTextField textField) {
    if (StringUtils.isAllBlank(imageLabel.getImagePath(), imageLabel.getImageUrl()) && StringUtils.isNotBlank(movieToEdit.getArtworkFilename(type))) {
      // artwork has been explicitly deleted
      movieToEdit.deleteMediaFiles(type);
    }

    if (StringUtils.isNotEmpty(textField.getText()) && !textField.getText().equals(movieToEdit.getArtworkUrl(type))) {
      // artwork url and textfield do not match -> redownload
      movieToEdit.setArtworkUrl(textField.getText(), type);
      movieToEdit.downloadArtwork(type);
    }
    else if (StringUtils.isEmpty(textField.getText())) {
      // remove the artwork url
      movieToEdit.removeArtworkUrl(type);
    }
  }

  private class DiscardAction extends AbstractAction {
    private static final long serialVersionUID = -5581329896797961536L;

    public DiscardAction() {
      putValue(NAME, BUNDLE.getString("Button.cancel"));
      putValue(SHORT_DESCRIPTION, BUNDLE.getString("edit.discard"));
      putValue(SMALL_ICON, IconManager.CANCEL_INV);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      setVisible(false);
    }
  }

  private class AddRatingAction extends AbstractAction {
    private static final long serialVersionUID = 2903255414533349267L;

    public AddRatingAction() {
      putValue(SHORT_DESCRIPTION, BUNDLE.getString("rating.add"));
      putValue(SMALL_ICON, IconManager.ADD_INV);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      MediaRatingTable.MediaRating mediaRating = new MediaRatingTable.MediaRating("");
      // default values
      mediaRating.maxValue = 10;
      mediaRating.votes = 1;

      RatingEditorDialog dialog = new RatingEditorDialog(SwingUtilities.getWindowAncestor(tableRatings), BUNDLE.getString("rating.add"), mediaRating);
      dialog.setVisible(true);

      if (StringUtils.isNotBlank(mediaRating.key) && mediaRating.value > 0 && mediaRating.maxValue > 0 && mediaRating.votes > 0) {
        mediaRatings.add(mediaRating);
      }
    }
  }

  private class RemoveRatingAction extends AbstractAction {
    private static final long serialVersionUID = -7079821950827356996L;

    public RemoveRatingAction() {
      putValue(SHORT_DESCRIPTION, BUNDLE.getString("rating.remove"));
      putValue(SMALL_ICON, IconManager.REMOVE_INV);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      int row = tableRatings.getSelectedRow();
      if (row > -1) {
        row = tableRatings.convertRowIndexToModel(row);
        mediaRatings.remove(row);
      }
    }
  }

  private class AddIdAction extends AbstractAction {
    private static final long serialVersionUID = 2903255414553349267L;

    public AddIdAction() {
      putValue(SHORT_DESCRIPTION, BUNDLE.getString("id.add"));
      putValue(SMALL_ICON, IconManager.ADD_INV);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      MediaId mediaId = new MediaId();
      IdEditorDialog dialog = new IdEditorDialog(SwingUtilities.getWindowAncestor(tableIds), BUNDLE.getString("id.add"), mediaId, ScraperType.MOVIE);
      dialog.setVisible(true);

      if (StringUtils.isNoneBlank(mediaId.key, mediaId.value)) {
        ids.add(mediaId);
      }
    }
  }

  private class RemoveIdAction extends AbstractAction {
    private static final long serialVersionUID = -7079826950827356996L;

    public RemoveIdAction() {
      putValue(SHORT_DESCRIPTION, BUNDLE.getString("id.remove"));
      putValue(SMALL_ICON, IconManager.REMOVE_INV);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      int row = tableIds.getSelectedRow();
      if (row > -1) {
        row = tableIds.convertRowIndexToModel(row);
        ids.remove(row);
      }
    }
  }

  private class AddActorAction extends AbstractAction {
    private static final long serialVersionUID = 2903255414153349267L;

    public AddActorAction() {
      putValue(SHORT_DESCRIPTION, BUNDLE.getString("cast.actor.add"));
      putValue(SMALL_ICON, IconManager.ADD_INV);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      Person actor = new Person(Person.Type.ACTOR, BUNDLE.getString("cast.actor.unknown"), BUNDLE.getString("cast.role.unknown"));
      PersonEditorDialog dialog = new PersonEditorDialog(SwingUtilities.getWindowAncestor(tableActors), BUNDLE.getString("cast.actor.add"), actor);
      dialog.setVisible(true);

      if (StringUtils.isNotBlank(actor.getName()) && !actor.getName().equals(BUNDLE.getString("cast.actor.unknown"))) {
        cast.add(0, actor);
      }
    }
  }

  private class RemoveActorAction extends AbstractAction {
    private static final long serialVersionUID = -7079826970827356996L;

    public RemoveActorAction() {
      putValue(SHORT_DESCRIPTION, BUNDLE.getString("cast.actor.remove"));
      putValue(SMALL_ICON, IconManager.REMOVE_INV);
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
      putValue(SHORT_DESCRIPTION, BUNDLE.getString("cast.producer.add"));
      putValue(SMALL_ICON, IconManager.ADD_INV);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      Person producer = new Person(Person.Type.PRODUCER, BUNDLE.getString("producer.name.unknown"), BUNDLE.getString("producer.role.unknown"));
      PersonEditorDialog dialog = new PersonEditorDialog(SwingUtilities.getWindowAncestor(tableProducers), BUNDLE.getString("cast.producer.add"),
          producer);
      dialog.setVisible(true);

      if (StringUtils.isNotBlank(producer.getName()) && !producer.getName().equals(BUNDLE.getString("producer.name.unknown"))) {
        producers.add(0, producer);
      }
    }
  }

  private class RemoveProducerAction extends AbstractAction {
    private static final long serialVersionUID = -3907776089614305086L;

    public RemoveProducerAction() {
      putValue(SHORT_DESCRIPTION, BUNDLE.getString("cast.producer.remove"));
      putValue(SMALL_ICON, IconManager.REMOVE_INV);
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
      putValue(SHORT_DESCRIPTION, BUNDLE.getString("genre.add"));
      putValue(SMALL_ICON, IconManager.ADD_INV);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      MediaGenres newGenre = null;
      Object item = cbGenres.getSelectedItem();

      // check, if text is selected (from auto completion), in this case we just
      // remove the selection
      Component editorComponent = cbGenres.getEditor().getEditorComponent();
      if (editorComponent instanceof JTextField) {
        JTextField tf = (JTextField) editorComponent;
        String selectedText = tf.getSelectedText();
        if (selectedText != null) {
          tf.setSelectionStart(0);
          tf.setSelectionEnd(0);
          tf.setCaretPosition(tf.getText().length());
          return;
        }
      }

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

        // set text combobox text input to ""
        if (editorComponent instanceof JTextField) {
          cbGenresAutoCompleteSupport.setFirstItem(null);
          cbGenres.setSelectedIndex(0);
          cbGenresAutoCompleteSupport.removeFirstItem();
        }
      }
    }
  }

  private class RemoveGenreAction extends AbstractAction {
    private static final long serialVersionUID = 2733654945906747720L;

    public RemoveGenreAction() {
      putValue(SHORT_DESCRIPTION, BUNDLE.getString("genre.remove"));
      putValue(SMALL_ICON, IconManager.REMOVE_INV);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      List<MediaGenres> selectedGenres = listGenres.getSelectedValuesList();
      for (MediaGenres genre : selectedGenres) {
        genres.remove(genre);
      }
    }
  }

  private class AddTrailerAction extends AbstractAction {
    private static final long serialVersionUID = -4446154040952056823L;

    public AddTrailerAction() {
      putValue(SHORT_DESCRIPTION, BUNDLE.getString("trailer.add"));
      putValue(SMALL_ICON, IconManager.ADD_INV);
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
      putValue(SHORT_DESCRIPTION, BUNDLE.getString("trailer.remove"));
      putValue(SMALL_ICON, IconManager.REMOVE_INV);
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
      putValue(SHORT_DESCRIPTION, BUNDLE.getString("tag.add"));
      putValue(SMALL_ICON, IconManager.ADD_INV);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      String newTag = (String) cbTags.getSelectedItem();

      // do not continue with empty tags
      if (StringUtils.isBlank(newTag)) {
        return;
      }

      // check, if text is selected (from auto completion), in this case we just
      // remove the selection
      Component editorComponent = cbTags.getEditor().getEditorComponent();
      if (editorComponent instanceof JTextField) {
        JTextField tf = (JTextField) editorComponent;
        String selectedText = tf.getSelectedText();
        if (selectedText != null) {
          tf.setSelectionStart(0);
          tf.setSelectionEnd(0);
          tf.setCaretPosition(tf.getText().length());
          return;
        }
      }

      // search if this tag already has been added
      boolean tagFound = false;
      for (String tag : tags) {
        if (tag.equals(newTag)) {
          tagFound = true;
          break;
        }
      }

      // add tag
      if (!tagFound) {
        tags.add(newTag);

        // set text combobox text input to ""
        if (editorComponent instanceof JTextField) {
          cbTagsAutoCompleteSupport.setFirstItem("");
          cbTags.setSelectedIndex(0);
          cbTagsAutoCompleteSupport.removeFirstItem();
        }
      }
    }
  }

  private class RemoveTagAction extends AbstractAction {
    private static final long serialVersionUID = -1580945350962234235L;

    public RemoveTagAction() {
      putValue(SHORT_DESCRIPTION, BUNDLE.getString("tag.remove"));
      putValue(SMALL_ICON, IconManager.REMOVE_INV);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      List<String> selectedTags = listTags.getSelectedValuesList();
      for (String tag : selectedTags) {
        tags.remove(tag);
      }
    }
  }

  private class AbortQueueAction extends AbstractAction {
    private static final long serialVersionUID = -7652218354710642510L;

    public AbortQueueAction() {
      putValue(NAME, BUNDLE.getString("Button.abortqueue"));
      putValue(SHORT_DESCRIPTION, BUNDLE.getString("movie.edit.abortqueue.desc"));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      continueQueue = false;
      setVisible(false);
    }
  }

  private class NavigateBackAction extends AbstractAction {
    private static final long serialVersionUID = -1652218154720642310L;

    public NavigateBackAction() {
      putValue(NAME, BUNDLE.getString("Button.back"));
      putValue(SMALL_ICON, IconManager.BACK_INV);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      navigateBack = true;
      setVisible(false);
    }
  }

  private class MoveActorUpAction extends AbstractAction {
    private static final long serialVersionUID = 5775423424097844658L;

    public MoveActorUpAction() {
      putValue(SHORT_DESCRIPTION, BUNDLE.getString("movie.edit.moveactorup"));
      putValue(SMALL_ICON, IconManager.ARROW_UP_INV);
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
      putValue(SHORT_DESCRIPTION, BUNDLE.getString("movie.edit.moveactordown"));
      putValue(SMALL_ICON, IconManager.ARROW_DOWN_INV);
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
      putValue(SHORT_DESCRIPTION, BUNDLE.getString("movie.edit.moveproducerup"));
      putValue(SMALL_ICON, IconManager.ARROW_UP_INV);
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
      putValue(SHORT_DESCRIPTION, BUNDLE.getString("movie.edit.moveproducerdown"));
      putValue(SMALL_ICON, IconManager.ARROW_DOWN_INV);
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

  private class MoveGenreUpAction extends AbstractAction {
    private static final long serialVersionUID = -6855661707692602266L;

    public MoveGenreUpAction() {
      putValue(SHORT_DESCRIPTION, BUNDLE.getString("movie.edit.movegenreup"));
      putValue(SMALL_ICON, IconManager.ARROW_UP_INV);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      int row = listGenres.getSelectedIndex();
      if (row > 0) {
        Collections.rotate(genres.subList(row - 1, row + 1), 1);
        listGenres.getSelectionModel().setSelectionInterval(row - 1, row - 1);
      }
    }
  }

  private class MoveGenreDownAction extends AbstractAction {
    private static final long serialVersionUID = -1135108943010008069L;

    public MoveGenreDownAction() {
      putValue(SHORT_DESCRIPTION, BUNDLE.getString("movie.edit.movegenredown"));
      putValue(SMALL_ICON, IconManager.ARROW_DOWN_INV);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      int row = listGenres.getSelectedIndex();
      if (row < genres.size() - 1) {
        Collections.rotate(genres.subList(row, row + 2), -1);
        listGenres.getSelectionModel().setSelectionInterval(row + 1, row + 1);
      }
    }
  }

  private class MoveTagUpAction extends AbstractAction {
    private static final long serialVersionUID = -6855661707692602266L;

    public MoveTagUpAction() {
      putValue(SHORT_DESCRIPTION, BUNDLE.getString("movie.edit.movetagup"));
      putValue(SMALL_ICON, IconManager.ARROW_UP_INV);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      int row = listTags.getSelectedIndex();
      if (row > 0) {
        Collections.rotate(tags.subList(row - 1, row + 1), 1);
        listTags.getSelectionModel().setSelectionInterval(row - 1, row - 1);
      }
    }
  }

  private class MoveTagDownAction extends AbstractAction {
    private static final long serialVersionUID = -1135108943010008069L;

    public MoveTagDownAction() {
      putValue(SHORT_DESCRIPTION, BUNDLE.getString("movie.edit.movetagdown"));
      putValue(SMALL_ICON, IconManager.ARROW_DOWN_INV);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      int row = listTags.getSelectedIndex();
      if (row < tags.size() - 1) {
        Collections.rotate(tags.subList(row, row + 2), -1);
        listTags.getSelectionModel().setSelectionInterval(row + 1, row + 1);
      }
    }
  }

  private class AddDirectorAction extends AbstractAction {
    private static final long serialVersionUID = -8929331442958057771L;

    public AddDirectorAction() {
      putValue(SHORT_DESCRIPTION, BUNDLE.getString("cast.director.add"));
      putValue(SMALL_ICON, IconManager.ADD_INV);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      Person person = new Person(Person.Type.DIRECTOR, BUNDLE.getString("director.name.unknown"), "Director");
      PersonEditorDialog dialog = new PersonEditorDialog(SwingUtilities.getWindowAncestor(tableDirectors), BUNDLE.getString("cast.director.add"),
          person);
      dialog.setVisible(true);

      if (StringUtils.isNotBlank(person.getName()) && !person.getName().equals(BUNDLE.getString("director.name.unknown"))) {
        directors.add(0, person);
      }
    }
  }

  private class RemoveDirectorAction extends AbstractAction {
    private static final long serialVersionUID = -7079826920821356196L;

    public RemoveDirectorAction() {
      putValue(SHORT_DESCRIPTION, BUNDLE.getString("cast.director.remove"));
      putValue(SMALL_ICON, IconManager.REMOVE_INV);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      int row = tableDirectors.getSelectedRow();
      if (row > -1) {
        row = tableDirectors.convertRowIndexToModel(row);
        directors.remove(row);
      }
    }
  }

  private class MoveDirectorUpAction extends AbstractAction {
    private static final long serialVersionUID = 5775423424097844658L;

    public MoveDirectorUpAction() {
      putValue(SHORT_DESCRIPTION, BUNDLE.getString("movie.edit.movedirectorup"));
      putValue(SMALL_ICON, IconManager.ARROW_UP_INV);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      int row = tableDirectors.getSelectedRow();
      if (row > 0) {
        Collections.rotate(directors.subList(row - 1, row + 1), 1);
        tableDirectors.getSelectionModel().setSelectionInterval(row - 1, row - 1);
      }
    }
  }

  private class MoveDirectorDownAction extends AbstractAction {
    private static final long serialVersionUID = -6564146895819191932L;

    public MoveDirectorDownAction() {
      putValue(SHORT_DESCRIPTION, BUNDLE.getString("movie.edit.movedirectordown"));
      putValue(SMALL_ICON, IconManager.ARROW_DOWN_INV);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      int row = tableDirectors.getSelectedRow();
      if (row < directors.size() - 1) {
        Collections.rotate(directors.subList(row, row + 2), -1);
        tableDirectors.getSelectionModel().setSelectionInterval(row + 1, row + 1);
      }
    }
  }

  private class AddWriterAction extends AbstractAction {
    private static final long serialVersionUID = -8929331442958057771L;

    public AddWriterAction() {
      putValue(SHORT_DESCRIPTION, BUNDLE.getString("cast.writer.add"));
      putValue(SMALL_ICON, IconManager.ADD_INV);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      Person person = new Person(Person.Type.DIRECTOR, BUNDLE.getString("writer.name.unknown"), "Writer");
      PersonEditorDialog dialog = new PersonEditorDialog(SwingUtilities.getWindowAncestor(tableWriters), BUNDLE.getString("cast.writer.add"), person);
      dialog.setVisible(true);

      if (StringUtils.isNotBlank(person.getName()) && !person.getName().equals(BUNDLE.getString("writer.name.unknown"))) {
        writers.add(0, person);
      }
    }
  }

  private class RemoveWriterAction extends AbstractAction {
    private static final long serialVersionUID = -7079826920821356196L;

    public RemoveWriterAction() {
      putValue(SHORT_DESCRIPTION, BUNDLE.getString("cast.writer.remove"));
      putValue(SMALL_ICON, IconManager.REMOVE_INV);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      int row = tableWriters.getSelectedRow();
      if (row > -1) {
        row = tableWriters.convertRowIndexToModel(row);
        writers.remove(row);
      }
    }
  }

  private class MoveWriterUpAction extends AbstractAction {
    private static final long serialVersionUID = 5775423424097844658L;

    public MoveWriterUpAction() {
      putValue(SHORT_DESCRIPTION, BUNDLE.getString("movie.edit.movewriterup"));
      putValue(SMALL_ICON, IconManager.ARROW_UP_INV);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      int row = tableWriters.getSelectedRow();
      if (row > 0) {
        Collections.rotate(writers.subList(row - 1, row + 1), 1);
        tableWriters.getSelectionModel().setSelectionInterval(row - 1, row - 1);
      }
    }
  }

  private class MoveWriterDownAction extends AbstractAction {
    private static final long serialVersionUID = -6564146895819191932L;

    public MoveWriterDownAction() {
      putValue(SHORT_DESCRIPTION, BUNDLE.getString("movie.edit.movewriterdown"));
      putValue(SMALL_ICON, IconManager.ARROW_DOWN_INV);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      int row = tableWriters.getSelectedRow();
      if (row < writers.size() - 1) {
        Collections.rotate(writers.subList(row, row + 2), -1);
        tableWriters.getSelectionModel().setSelectionInterval(row + 1, row + 1);
      }
    }
  }

  @Override
  public void dispose() {
    super.dispose();

    mediaFilesPanel.unbindBindings();
    dpReleaseDate.cleanup();
  }

  protected BindingGroup initDataBindings() {
    JListBinding<MediaGenres, List<MediaGenres>, JList> jListBinding = SwingBindings.createJListBinding(UpdateStrategy.READ, genres, listGenres);
    jListBinding.bind();
    //
    JTableBinding<MediaTrailer, List<MediaTrailer>, JTable> jTableBinding_1 = SwingBindings.createJTableBinding(UpdateStrategy.READ, trailers,
        tableTrailer);
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
    JListBinding<String, List<String>, JList> jListBinding_1 = SwingBindings.createJListBinding(UpdateStrategy.READ, tags, listTags);
    jListBinding_1.bind();
    //
    BindingGroup bindingGroup = new BindingGroup();
    //
    bindingGroup.addBinding(jListBinding);
    bindingGroup.addBinding(jTableBinding_1);
    bindingGroup.addBinding(jListBinding_1);
    return bindingGroup;
  }

  private void setImageSizeAndCreateLink(LinkLabel lblSize, ImageLabel imageLabel, MediaFileType type) {
    createLinkForImage(lblSize, imageLabel);

    // image has been deleted
    if (imageLabel.getOriginalImageSize().width == 0 && imageLabel.getOriginalImageSize().height == 0) {
      lblSize.setText("");
      return;
    }

    Dimension dimension = movieToEdit.getArtworkDimension(type);
    if (dimension.width == 0 && dimension.height == 0) {
      lblSize.setText(imageLabel.getOriginalImageSize().width + "x" + imageLabel.getOriginalImageSize().height);
    }
    else {
      lblSize.setText(dimension.width + "x" + dimension.height);
    }
  }
}
