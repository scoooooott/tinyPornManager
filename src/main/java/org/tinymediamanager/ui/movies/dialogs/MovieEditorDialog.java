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
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map.Entry;
import java.util.ResourceBundle;

import javax.swing.AbstractAction;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.KeyStroke;
import javax.swing.SpinnerDateModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.EmptyBorder;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import org.apache.commons.lang3.StringUtils;
import org.jdesktop.beansbinding.AutoBinding.UpdateStrategy;
import org.jdesktop.beansbinding.BeanProperty;
import org.jdesktop.observablecollections.ObservableCollections;
import org.jdesktop.swingbinding.JListBinding;
import org.jdesktop.swingbinding.JTableBinding;
import org.jdesktop.swingbinding.SwingBindings;
import org.tinymediamanager.core.MediaFileType;
import org.tinymediamanager.core.MediaSource;
import org.tinymediamanager.core.entities.MediaFile;
import org.tinymediamanager.core.movie.MovieEdition;
import org.tinymediamanager.core.movie.MovieList;
import org.tinymediamanager.core.movie.MovieModuleManager;
import org.tinymediamanager.core.movie.entities.Movie;
import org.tinymediamanager.core.movie.entities.MovieActor;
import org.tinymediamanager.core.movie.entities.MovieProducer;
import org.tinymediamanager.core.movie.entities.MovieSet;
import org.tinymediamanager.core.movie.entities.MovieTrailer;
import org.tinymediamanager.core.threading.TmmTask;
import org.tinymediamanager.core.threading.TmmTaskManager;
import org.tinymediamanager.scraper.ScraperType;
import org.tinymediamanager.scraper.entities.Certification;
import org.tinymediamanager.scraper.entities.MediaGenres;
import org.tinymediamanager.scraper.entities.MediaType;
import org.tinymediamanager.scraper.trakttv.SyncTraktTvTask;
import org.tinymediamanager.ui.EqualsLayout;
import org.tinymediamanager.ui.IconManager;
import org.tinymediamanager.ui.MainWindow;
import org.tinymediamanager.ui.TmmFontHelper;
import org.tinymediamanager.ui.UTF8Control;
import org.tinymediamanager.ui.components.AutocompleteComboBox;
import org.tinymediamanager.ui.components.ImageLabel;
import org.tinymediamanager.ui.components.MediaIdTable;
import org.tinymediamanager.ui.components.MediaIdTable.MediaId;
import org.tinymediamanager.ui.components.datepicker.DatePicker;
import org.tinymediamanager.ui.components.datepicker.YearSpinner;
import org.tinymediamanager.ui.dialogs.ImageChooserDialog;
import org.tinymediamanager.ui.dialogs.ImageChooserDialog.ImageType;
import org.tinymediamanager.ui.dialogs.TmmDialog;
import org.tinymediamanager.ui.panels.MediaFileEditorPanel;

import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.FormSpecs;
import com.jgoodies.forms.layout.RowSpec;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.swing.AutoCompleteSupport;

/**
 * The Class MovieEditor.
 * 
 * @author Manuel Laggner
 */
public class MovieEditorDialog extends TmmDialog {
  private static final long                                         serialVersionUID = -286251957529920347L;
  /** @wbp.nls.resourceBundle messages */
  private static final ResourceBundle                               BUNDLE           = ResourceBundle.getBundle("messages", new UTF8Control());      //$NON-NLS-1$

  private Movie                                                     movieToEdit;
  private MovieList                                                 movieList        = MovieList.getInstance();
  private List<MovieActor>                                          cast             = ObservableCollections
      .observableList(new ArrayList<MovieActor>());
  private List<MovieProducer>                                       producers        = ObservableCollections
      .observableList(new ArrayList<MovieProducer>());
  private List<MediaGenres>                                         genres           = ObservableCollections
      .observableList(new ArrayList<MediaGenres>());
  private List<MovieTrailer>                                        trailers         = ObservableCollections
      .observableList(new ArrayList<MovieTrailer>());
  private List<String>                                              tags             = ObservableCollections.observableList(new ArrayList<String>());
  private EventList<MediaId>                                        ids              = new BasicEventList<>();
  private List<MediaFile>                                           mediaFiles       = new ArrayList<>();
  private List<String>                                              extrathumbs      = new ArrayList<>();
  private List<String>                                              extrafanarts     = new ArrayList<>();
  private boolean                                                   continueQueue    = true;

  private final JPanel                                              details1Panel    = new JPanel();
  private final JPanel                                              details2Panel    = new JPanel();
  private JTextField                                                tfTitle;
  private JTextField                                                tfOriginalTitle;
  private YearSpinner                                               spYear;
  private JTextPane                                                 tpPlot;
  private JTextField                                                tfDirector;
  private JTable                                                    tableActors;
  private JLabel                                                    lblMoviePath;
  private ImageLabel                                                lblPoster;
  private ImageLabel                                                lblFanart;
  private JTextField                                                tfWriter;
  private JSpinner                                                  spRuntime;
  private JTextPane                                                 tfProductionCompanies;
  private JList<MediaGenres>                                        listGenres;
  private AutocompleteComboBox<MediaGenres>                         cbGenres;
  private AutoCompleteSupport<MediaGenres>                          cbGenresAutoCompleteSupport;
  private JSpinner                                                  spRating;
  private JComboBox                                                 cbCertification;
  private JCheckBox                                                 cbWatched;
  private JTextPane                                                 tpTagline;
  private JTable                                                    tableTrailer;
  private JTable                                                    tableProducers;
  private AutocompleteComboBox<String>                              cbTags;
  private AutoCompleteSupport<String>                               cbTagsAutoCompleteSupport;
  private JList<String>                                             listTags;
  private JSpinner                                                  spDateAdded;
  private JComboBox                                                 cbMovieSet;
  private JTextField                                                tfSorttitle;
  private JTextField                                                tfSpokenLanguages;
  private JTextField                                                tfCountry;
  private DatePicker                                                dpReleaseDate;
  private JSpinner                                                  spTop250;
  private JComboBox                                                 cbSource;
  private JCheckBox                                                 chckbxVideo3D;
  private JTable                                                    tableIds;
  private MediaFileEditorPanel                                      mediaFilesPanel;
  private JComboBox                                                 cbEdition;

  private ImageLabel                                                lblLogo;
  private ImageLabel                                                lblClearlogo;
  private ImageLabel                                                lblBanner;
  private ImageLabel                                                lblClearart;
  private ImageLabel                                                lblThumb;
  private ImageLabel                                                lblDisc;

  private JTableBinding<MovieActor, List<MovieActor>, JTable>       jTableBinding;
  private JListBinding<MediaGenres, List<MediaGenres>, JList>       jListBinding;
  private JTableBinding<MovieTrailer, List<MovieTrailer>, JTable>   jTableBinding_1;
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
    super(BUNDLE.getString("movie.edit"), "movieEditor"); //$NON-NLS-1$
    setBounds(5, 5, 950, 650);

    movieToEdit = movie;
    ids = MediaIdTable.convertIdMapToEventList(movieToEdit.getIds());
    for (MediaFile mf : movie.getMediaFiles()) {
      mediaFiles.add(new MediaFile(mf));
    }

    getContentPane().setLayout(new BorderLayout());
    {
      JPanel panelPath = new JPanel();
      getContentPane().add(panelPath, BorderLayout.NORTH);
      panelPath.setLayout(new FormLayout(
          new ColumnSpec[] { FormFactory.RELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC, FormFactory.DEFAULT_COLSPEC,
              FormFactory.RELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC, FormFactory.RELATED_GAP_COLSPEC, },
          new RowSpec[] { FormFactory.LINE_GAP_ROWSPEC, RowSpec.decode("15px"), FormFactory.RELATED_GAP_ROWSPEC, }));

      JLabel lblMoviePathT = new JLabel(BUNDLE.getString("metatag.path")); //$NON-NLS-1$
      panelPath.add(lblMoviePathT, "2, 2, left, top");

      lblMoviePath = new JLabel("");
      TmmFontHelper.changeFont(lblMoviePath, 1.166, Font.BOLD);
      panelPath.add(lblMoviePath, "5, 2, left, top");
    }

    JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.NORTH);
    getContentPane().add(tabbedPane, BorderLayout.CENTER);

    /**
     * DetailsPanel 1
     */
    {
      details1Panel.setBorder(new EmptyBorder(5, 5, 5, 5));
      details1Panel.setLayout(new FormLayout(
          new ColumnSpec[] { FormSpecs.RELATED_GAP_COLSPEC, ColumnSpec.decode("max(40dlu;default)"), FormSpecs.RELATED_GAP_COLSPEC,
              FormSpecs.DEFAULT_COLSPEC, FormSpecs.RELATED_GAP_COLSPEC, ColumnSpec.decode("7dlu:grow"), FormSpecs.RELATED_GAP_COLSPEC,
              FormSpecs.DEFAULT_COLSPEC, FormSpecs.RELATED_GAP_COLSPEC, ColumnSpec.decode("25dlu:grow"), FormSpecs.RELATED_GAP_COLSPEC,
              ColumnSpec.decode("24dlu"), FormSpecs.RELATED_GAP_COLSPEC, ColumnSpec.decode("7dlu:grow"), FormSpecs.RELATED_GAP_COLSPEC,
              FormSpecs.DEFAULT_COLSPEC, FormSpecs.RELATED_GAP_COLSPEC, FormSpecs.DEFAULT_COLSPEC, FormSpecs.RELATED_GAP_COLSPEC,
              FormSpecs.UNRELATED_GAP_COLSPEC, FormSpecs.RELATED_GAP_COLSPEC, FormSpecs.DEFAULT_COLSPEC, FormSpecs.RELATED_GAP_COLSPEC,
              ColumnSpec.decode("100dlu:grow(2)"), FormSpecs.RELATED_GAP_COLSPEC, },
          new RowSpec[] { FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC, FormSpecs.LABEL_COMPONENT_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC,
              FormSpecs.LABEL_COMPONENT_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC, FormSpecs.LABEL_COMPONENT_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC,
              FormSpecs.LABEL_COMPONENT_GAP_ROWSPEC, RowSpec.decode("50px:grow"), FormSpecs.LABEL_COMPONENT_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC,
              FormSpecs.LABEL_COMPONENT_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC, FormSpecs.LABEL_COMPONENT_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC,
              FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC, FormSpecs.RELATED_GAP_ROWSPEC, RowSpec.decode("15dlu"),
              FormSpecs.LABEL_COMPONENT_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC, FormSpecs.LABEL_COMPONENT_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC,
              FormSpecs.LABEL_COMPONENT_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC, FormSpecs.LABEL_COMPONENT_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC,
              FormSpecs.LABEL_COMPONENT_GAP_ROWSPEC, RowSpec.decode("fill:50dlu:grow"), FormSpecs.LABEL_COMPONENT_GAP_ROWSPEC,
              FormSpecs.DEFAULT_ROWSPEC, FormSpecs.LABEL_COMPONENT_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC, FormSpecs.LABEL_COMPONENT_GAP_ROWSPEC,
              RowSpec.decode("50px"), FormSpecs.LABEL_COMPONENT_GAP_ROWSPEC, RowSpec.decode("fill:default:grow"), }));

      {
        JLabel lblTitle = new JLabel(BUNDLE.getString("metatag.title")); //$NON-NLS-1$
        details1Panel.add(lblTitle, "2, 4, right, default");
      }
      {
        tfTitle = new JTextField();
        details1Panel.add(tfTitle, "4, 4, 15, 1, fill, default");
        tfTitle.setColumns(10);
      }
      {
        // JLabel lblPoster = new JLabel("");
        lblPoster = new ImageLabel();
        lblPoster.setAlternativeText(BUNDLE.getString("image.notfound.poster")); //$NON-NLS-1$
        lblPoster.addMouseListener(new MouseAdapter() {
          @Override
          public void mouseClicked(MouseEvent e) {
            ImageChooserDialog dialog = new ImageChooserDialog(movieToEdit.getIds(), ImageType.POSTER, movieList.getDefaultArtworkScrapers(),
                lblPoster, null, null, MediaType.MOVIE);
            dialog.setLocationRelativeTo(MainWindow.getActiveInstance());
            dialog.setVisible(true);
          }
        });
        lblPoster.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        details1Panel.add(lblPoster, "22, 4, 3, 23, fill, fill");
      }
      {
        JLabel lblOriginalTitle = new JLabel(BUNDLE.getString("metatag.originaltitle")); //$NON-NLS-1$
        details1Panel.add(lblOriginalTitle, "2, 6, right, default");
      }
      {
        tfOriginalTitle = new JTextField();
        details1Panel.add(tfOriginalTitle, "4, 6, 15, 1, fill, top");
        tfOriginalTitle.setColumns(10);
      }
      {
        JLabel lblSorttitle = new JLabel(BUNDLE.getString("metatag.sorttitle")); //$NON-NLS-1$
        details1Panel.add(lblSorttitle, "2, 8, right, default");
      }
      {
        tfSorttitle = new JTextField();
        details1Panel.add(tfSorttitle, "4, 8, 15, 1, fill, default");
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
        details1Panel.add(scrollPaneTagline, "4, 10, 15, 1, fill, fill");
      }
      {
        JLabel lblYear = new JLabel(BUNDLE.getString("metatag.year")); //$NON-NLS-1$
        details1Panel.add(lblYear, "2, 12, right, default");
      }
      {
        spYear = new YearSpinner();
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
        JLabel lblRating = new JLabel(BUNDLE.getString("metatag.rating")); //$NON-NLS-1$
        details1Panel.add(lblRating, "16, 12, right, default");
      }
      {
        spRating = new JSpinner();
        details1Panel.add(spRating, "18, 12");
      }

      spRating.setModel(new SpinnerNumberModel(movie.getRating(), 0.0, 10.0, 0.1));
      {
        JLabel lblReleaseDate = new JLabel(BUNDLE.getString("metatag.releasedate")); //$NON-NLS-1$
        details1Panel.add(lblReleaseDate, "2, 14, right, default");
      }
      {
        dpReleaseDate = new DatePicker(movie.getReleaseDate());
        details1Panel.add(dpReleaseDate, "4, 14");
      }
      {
        JLabel lblCertification = new JLabel(BUNDLE.getString("metatag.certification")); //$NON-NLS-1$
        details1Panel.add(lblCertification, "8, 14, right, default");
      }
      cbCertification = new JComboBox();
      details1Panel.add(cbCertification, "10, 14, 3, 1, fill, default");
      {
        JLabel lblTop = new JLabel(BUNDLE.getString("metatag.top250")); //$NON-NLS-1$
        details1Panel.add(lblTop, "16, 14, right, default");
      }
      {
        spTop250 = new JSpinner();
        details1Panel.add(spTop250, "18, 14");
      }
      spTop250.setValue(movie.getTop250());
      {
        JLabel lblIds = new JLabel("Ids");
        details1Panel.add(lblIds, "2, 16, right, bottom");
      }
      {
        JScrollPane scrollPaneIds = new JScrollPane();
        details1Panel.add(scrollPaneIds, "4, 16, 9, 5, fill, fill");
        {
          tableIds = new MediaIdTable(ids, ScraperType.MOVIE);
          scrollPaneIds.setViewportView(tableIds);
        }
      }
      {
        JButton btnAddId = new JButton("");
        btnAddId.setAction(new AddIdAction());
        btnAddId.setIcon(IconManager.LIST_ADD);
        btnAddId.setMargin(new Insets(2, 2, 2, 2));
        details1Panel.add(btnAddId, "2, 18, right, top");
      }
      {
        JButton btnRemoveId = new JButton("");
        btnRemoveId.setAction(new RemoveIdAction());
        btnRemoveId.setIcon(IconManager.LIST_REMOVE);
        btnRemoveId.setMargin(new Insets(2, 2, 2, 2));
        details1Panel.add(btnRemoveId, "2, 20, right, top");
      }
      {
        JLabel lblSpokenLanguages = new JLabel(BUNDLE.getString("metatag.spokenlanguages")); //$NON-NLS-1$
        details1Panel.add(lblSpokenLanguages, "2, 22, right, default");
      }
      {
        tfSpokenLanguages = new JTextField();
        details1Panel.add(tfSpokenLanguages, "4, 22, fill, default");
        tfSpokenLanguages.setColumns(10);
      }
      {
        JLabel lblCountry = new JLabel(BUNDLE.getString("metatag.country")); //$NON-NLS-1$
        details1Panel.add(lblCountry, "8, 22, right, default");
      }
      {
        tfCountry = new JTextField();
        details1Panel.add(tfCountry, "10, 22, 3, 1, fill, default");
        tfCountry.setColumns(10);
      }
      {
        JLabel lblMovieSet = new JLabel(BUNDLE.getString("metatag.movieset")); //$NON-NLS-1$
        details1Panel.add(lblMovieSet, "2, 24, right, default");
      }
      {
        cbMovieSet = new JComboBox();
        cbMovieSet.addItem("");
        details1Panel.add(cbMovieSet, "4, 24, 9, 1, fill, default");
      }
      {
        JLabel lblDateAdded = new JLabel(BUNDLE.getString("metatag.dateadded")); //$NON-NLS-1$
        details1Panel.add(lblDateAdded, "2, 26, right, default");
      }
      {
        spDateAdded = new JSpinner(new SpinnerDateModel());
        // JSpinner.DateEditor timeEditor = new JSpinner.DateEditor(spDateAdded,
        // "dd.MM.yyyy HH:mm:ss");
        // spDateAdded.setEditor(timeEditor);
        details1Panel.add(spDateAdded, "4, 26");
      }
      spDateAdded.setValue(movie.getDateAdded());
      JLabel lblWatched = new JLabel(BUNDLE.getString("metatag.watched")); //$NON-NLS-1$
      details1Panel.add(lblWatched, "8, 26, right, default");
      {
        cbWatched = new JCheckBox("");
        details1Panel.add(cbWatched, "10, 26");
      }
      cbWatched.setSelected(movie.isWatched());
      lblWatched.setLabelFor(cbWatched);
      {
        JLabel lblSourceT = new JLabel(BUNDLE.getString("metatag.source")); //$NON-NLS-1$
        details1Panel.add(lblSourceT, "2, 28, right, default");
      }
      {
        cbSource = new JComboBox(MediaSource.values());
        details1Panel.add(cbSource, "4, 28, fill, default");
      }
      cbSource.setSelectedItem(movie.getMediaSource());
      {
        final JLabel lblEditionT = new JLabel(BUNDLE.getString("metatag.edition")); //$NON-NLS-1$
        details1Panel.add(lblEditionT, "8, 28, right, default");
      }
      {
        cbEdition = new JComboBox(MovieEdition.values());
        details1Panel.add(cbEdition, "10, 28, 3, 1, fill, default");
      }
      {
        JLabel lblVideod = new JLabel(BUNDLE.getString("metatag.3d")); //$NON-NLS-1$
        details1Panel.add(lblVideod, "16, 28, right, default");
      }
      {
        chckbxVideo3D = new JCheckBox("");
        details1Panel.add(chckbxVideo3D, "18, 28");
      }
      chckbxVideo3D.setSelected(movie.isVideoIn3D());
      {
        JLabel lblPlot = new JLabel(BUNDLE.getString("metatag.plot")); //$NON-NLS-1$
        details1Panel.add(lblPlot, "2, 30, right, top");
      }
      {
        JScrollPane scrollPanePlot = new JScrollPane();
        details1Panel.add(scrollPanePlot, "4, 30, 15, 1, fill, fill");
        {
          tpPlot = new JTextPane();
          scrollPanePlot.setViewportView(tpPlot);
        }
      }
      {
        lblFanart = new ImageLabel();
        lblFanart.setAlternativeText(BUNDLE.getString("image.notfound.fanart")); //$NON-NLS-1$
        lblFanart.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        lblFanart.addMouseListener(new MouseAdapter() {
          @Override
          public void mouseClicked(MouseEvent e) {
            ImageChooserDialog dialog = new ImageChooserDialog(movieToEdit.getIds(), ImageType.FANART, movieList.getDefaultArtworkScrapers(),
                lblFanart, extrathumbs, extrafanarts, MediaType.MOVIE);
            dialog.setLocationRelativeTo(MainWindow.getActiveInstance());
            dialog.setVisible(true);
          }
        });
        details1Panel.add(lblFanart, "22, 30, 3, 5, fill, fill");
      }
      lblFanart.setImagePath(movie.getArtworkFilename(MediaFileType.FANART));
      {
        JLabel lblDirector = new JLabel(BUNDLE.getString("metatag.director")); //$NON-NLS-1$
        details1Panel.add(lblDirector, "2, 32, right, default");
      }
      {
        tfDirector = new JTextField();
        details1Panel.add(tfDirector, "4, 32, 15, 1, fill, top");
        tfDirector.setColumns(10);
      }
      {
        JLabel lblWriter = new JLabel(BUNDLE.getString("metatag.writer")); //$NON-NLS-1$
        details1Panel.add(lblWriter, "2, 34, right, default");
      }
      {
        tfWriter = new JTextField();
        details1Panel.add(tfWriter, "4, 34, 15, 1, fill, top");
        tfWriter.setColumns(10);
      }
      {
        JLabel lblCompany = new JLabel(BUNDLE.getString("metatag.production")); //$NON-NLS-1$
        details1Panel.add(lblCompany, "2, 36, right, top");
      }
      {
        JScrollPane scrollPaneProduction = new JScrollPane();
        details1Panel.add(scrollPaneProduction, "4, 36, 15, 1, fill, fill");
        tfProductionCompanies = new JTextPane();
        scrollPaneProduction.setViewportView(tfProductionCompanies);
      }
      tabbedPane.addTab(BUNDLE.getString("metatag.details"), details1Panel); //$NON-NLS-1$
    }

    /**
     * DetailsPanel 2
     */
    {
      tabbedPane.addTab(BUNDLE.getString("metatag.details2"), details2Panel); //$NON-NLS-1$
      details2Panel.setBorder(new EmptyBorder(5, 5, 5, 5));
      details2Panel.setLayout(new FormLayout(
          new ColumnSpec[] { FormSpecs.RELATED_GAP_COLSPEC, ColumnSpec.decode("max(40dlu;default)"), FormSpecs.RELATED_GAP_COLSPEC,
              ColumnSpec.decode("50px:grow"), FormSpecs.RELATED_GAP_COLSPEC, FormSpecs.DEFAULT_COLSPEC, FormSpecs.RELATED_GAP_COLSPEC,
              ColumnSpec.decode("100px:grow"), FormSpecs.RELATED_GAP_COLSPEC, },
          new RowSpec[] { FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC, FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC,
              FormSpecs.RELATED_GAP_ROWSPEC, RowSpec.decode("fill:default"), FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC,
              FormSpecs.RELATED_GAP_ROWSPEC, RowSpec.decode("default:grow"), FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC,
              FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC, FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC,
              FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC, FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC,
              FormSpecs.RELATED_GAP_ROWSPEC, RowSpec.decode("default:grow"), FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC,
              FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC, FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC,
              FormSpecs.RELATED_GAP_ROWSPEC, RowSpec.decode("default:grow"), FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC,
              FormSpecs.RELATED_GAP_ROWSPEC, RowSpec.decode("default:grow(2)"), }));
      {
        JLabel lblActors = new JLabel(BUNDLE.getString("metatag.actors")); //$NON-NLS-1$
        details2Panel.add(lblActors, "2, 2, right, default");
      }
      {
        JScrollPane scrollPane = new JScrollPane();
        details2Panel.add(scrollPane, "4, 2, 1, 11");
        tableActors = new JTable();
        tableActors.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
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
        tableProducers.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
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
        details2Panel.add(scrollPaneGenres, "4, 14, 1, 9");
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
        details2Panel.add(scrollPaneTags, "8, 14, 1, 9");
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
        cbGenres = new AutocompleteComboBox(MediaGenres.values());
        cbGenresAutoCompleteSupport = cbGenres.getAutoCompleteSupport();
        InputMap im = cbGenres.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        Object enterAction = im.get(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0));
        cbGenres.getActionMap().put(enterAction, new AddGenreAction());
      }
      {
        JButton btnRemoveTag = new JButton("");
        btnRemoveTag.setAction(new RemoveTagAction());
        btnRemoveTag.setIcon(IconManager.LIST_REMOVE);
        btnRemoveTag.setMargin(new Insets(2, 2, 2, 2));
        details2Panel.add(btnRemoveTag, "6, 18, right, top");
      }
      {
        JButton btnMoveGenreUp = new JButton((String) null);
        btnMoveGenreUp.setMargin(new Insets(2, 2, 2, 2));
        btnMoveGenreUp.setAction(new MoveGenreUpAction());
        btnMoveGenreUp.setIcon(IconManager.ARROW_UP);
        details2Panel.add(btnMoveGenreUp, "2, 20, right, top");
      }
      {
        JButton btnMoveTagUp = new JButton((String) null);
        btnMoveTagUp.setMargin(new Insets(2, 2, 2, 2));
        btnMoveTagUp.setAction(new MoveTagUpAction());
        btnMoveTagUp.setIcon(IconManager.ARROW_UP);
        details2Panel.add(btnMoveTagUp, "6, 20, right, top");
      }
      {
        JButton btnMoveGenreDown = new JButton((String) null);
        btnMoveGenreDown.setMargin(new Insets(2, 2, 2, 2));
        btnMoveGenreDown.setAction(new MoveGenreDownAction());
        btnMoveGenreDown.setIcon(IconManager.ARROW_DOWN);
        details2Panel.add(btnMoveGenreDown, "2, 22, right, top");
      }
      {
        JButton btnMoveTagDown = new JButton((String) null);
        btnMoveTagDown.setMargin(new Insets(2, 2, 2, 2));
        btnMoveTagDown.setAction(new MoveTagDownAction());
        btnMoveTagDown.setIcon(IconManager.ARROW_DOWN);
        details2Panel.add(btnMoveTagDown, "6, 22, right, top");
      }
      details2Panel.add(cbGenres, "4, 24");

      {
        cbTags = new AutocompleteComboBox<String>(movieList.getTagsInMovies());
        cbTagsAutoCompleteSupport = cbTags.getAutoCompleteSupport();
        InputMap im = cbTags.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        Object enterAction = im.get(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0));
        cbTags.getActionMap().put(enterAction, new AddTagAction());
        details2Panel.add(cbTags, "8, 24");
      }

      {
        JLabel lblTrailer = new JLabel(BUNDLE.getString("metatag.trailer")); //$NON-NLS-1$
        details2Panel.add(lblTrailer, "2, 26, right, default");
      }
      {
        JScrollPane scrollPaneTrailer = new JScrollPane();
        details2Panel.add(scrollPaneTrailer, "4, 26, 5, 5");
        tableTrailer = new JTable();
        tableTrailer.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
        scrollPaneTrailer.setViewportView(tableTrailer);
      }
      {
        JButton btnAddTrailer = new JButton("");
        btnAddTrailer.setAction(new AddTrailerAction());
        btnAddTrailer.setIcon(IconManager.LIST_ADD);
        btnAddTrailer.setMargin(new Insets(2, 2, 2, 2));
        details2Panel.add(btnAddTrailer, "2, 28, right, top");
      }
      {
        JButton btnRemoveTrailer = new JButton("");
        btnRemoveTrailer.setAction(new RemoveTrailerAction());
        btnRemoveTrailer.setIcon(IconManager.LIST_REMOVE);
        btnRemoveTrailer.setMargin(new Insets(2, 2, 2, 2));
        details2Panel.add(btnRemoveTrailer, "2, 30, right, top");
      }
    }

    /**
     * extra artwork pane
     */
    {
      JPanel artworkPanel = new JPanel();
      tabbedPane.addTab(BUNDLE.getString("metatag.extraartwork"), null, artworkPanel, null); //$NON-NLS-1$
      artworkPanel.setLayout(new FormLayout(
          new ColumnSpec[] { FormSpecs.RELATED_GAP_COLSPEC, ColumnSpec.decode("250px:grow"), FormSpecs.RELATED_GAP_COLSPEC,
              ColumnSpec.decode("250px:grow"), FormSpecs.RELATED_GAP_COLSPEC, ColumnSpec.decode("150px:grow"), FormSpecs.RELATED_GAP_COLSPEC, },
          new RowSpec[] { FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC, FormSpecs.RELATED_GAP_ROWSPEC, RowSpec.decode("50px:grow(2)"),
              FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC, FormSpecs.RELATED_GAP_ROWSPEC, RowSpec.decode("50px:grow(2)"),
              FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC, FormSpecs.RELATED_GAP_ROWSPEC, RowSpec.decode("200px:grow(2)"),
              FormSpecs.RELATED_GAP_ROWSPEC, RowSpec.decode("default:grow"), }));
      {
        JLabel lblLogoT = new JLabel(BUNDLE.getString("mediafiletype.logo")); //$NON-NLS-1$
        artworkPanel.add(lblLogoT, "2, 2");
      }
      {
        lblLogo = new ImageLabel();
        lblLogo.setAlternativeText(BUNDLE.getString("image.notfound.logo")); //$NON-NLS-1$
        lblLogo.addMouseListener(new MouseAdapter() {
          @Override
          public void mouseClicked(MouseEvent e) {
            ImageChooserDialog dialog = new ImageChooserDialog(movieToEdit.getIds(), ImageType.LOGO, movieList.getDefaultArtworkScrapers(), lblLogo,
                null, null, MediaType.MOVIE);
            dialog.setLocationRelativeTo(MainWindow.getActiveInstance());
            dialog.setVisible(true);
          }
        });
        lblLogo.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        artworkPanel.add(lblLogo, "2, 4, fill, fill");
      }
      {
        final JLabel lblClearlogoT = new JLabel(BUNDLE.getString("mediafiletype.clearlogo")); //$NON-NLS-1$
        artworkPanel.add(lblClearlogoT, "4, 2");
      }
      {
        lblClearlogo = new ImageLabel();
        lblClearlogo.setAlternativeText(BUNDLE.getString("image.notfound.clearlogo")); //$NON-NLS-1$
        lblClearlogo.addMouseListener(new MouseAdapter() {
          @Override
          public void mouseClicked(MouseEvent e) {
            ImageChooserDialog dialog = new ImageChooserDialog(movieToEdit.getIds(), ImageType.CLEARLOGO, movieList.getDefaultArtworkScrapers(),
                lblClearlogo, null, null, MediaType.MOVIE);
            dialog.setLocationRelativeTo(MainWindow.getActiveInstance());
            dialog.setVisible(true);
          }
        });
        lblClearlogo.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        artworkPanel.add(lblClearlogo, "4, 4, fill, fill");
      }
      {
        JLabel lblBannerT = new JLabel(BUNDLE.getString("mediafiletype.banner")); //$NON-NLS-1$
        artworkPanel.add(lblBannerT, "2, 6");
      }
      {
        lblBanner = new ImageLabel();
        lblBanner.setAlternativeText(BUNDLE.getString("image.notfound.banner")); //$NON-NLS-1$
        lblBanner.addMouseListener(new MouseAdapter() {
          @Override
          public void mouseClicked(MouseEvent e) {
            ImageChooserDialog dialog = new ImageChooserDialog(movieToEdit.getIds(), ImageType.BANNER, movieList.getDefaultArtworkScrapers(),
                lblBanner, null, null, MediaType.MOVIE);
            dialog.setLocationRelativeTo(MainWindow.getActiveInstance());
            dialog.setVisible(true);
          }
        });
        lblBanner.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        artworkPanel.add(lblBanner, "2, 8, 3, 1, fill, fill");
      }

      {
        JLabel lblClearartT = new JLabel("ClearArt");
        artworkPanel.add(lblClearartT, "2, 10");
      }
      {
        lblClearart = new ImageLabel();
        lblClearart.setAlternativeText(BUNDLE.getString("image.notfound.clearart")); //$NON-NLS-1$
        lblClearart.addMouseListener(new MouseAdapter() {
          @Override
          public void mouseClicked(MouseEvent e) {
            ImageChooserDialog dialog = new ImageChooserDialog(movieToEdit.getIds(), ImageType.CLEARART, movieList.getDefaultArtworkScrapers(),
                lblClearart, null, null, MediaType.MOVIE);
            dialog.setLocationRelativeTo(MainWindow.getActiveInstance());
            dialog.setVisible(true);
          }
        });
        lblClearart.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        artworkPanel.add(lblClearart, "2, 12, fill, fill");
      }
      {
        JLabel lblThumbT = new JLabel("Thumb");
        artworkPanel.add(lblThumbT, "4, 10");
      }
      {
        lblThumb = new ImageLabel();
        lblThumb.setAlternativeText(BUNDLE.getString("image.notfound.thumb")); //$NON-NLS-1$
        lblThumb.addMouseListener(new MouseAdapter() {
          @Override
          public void mouseClicked(MouseEvent e) {
            ImageChooserDialog dialog = new ImageChooserDialog(movieToEdit.getIds(), ImageType.THUMB, movieList.getDefaultArtworkScrapers(), lblThumb,
                null, null, MediaType.MOVIE);
            dialog.setLocationRelativeTo(MainWindow.getActiveInstance());
            dialog.setVisible(true);
          }
        });
        lblThumb.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        artworkPanel.add(lblThumb, "4, 12, fill, fill");
      }
      {
        JLabel lblDiscT = new JLabel("Disc");
        artworkPanel.add(lblDiscT, "6, 10");
      }
      {
        lblDisc = new ImageLabel();
        lblDisc.setAlternativeText(BUNDLE.getString("image.notfound.disc")); //$NON-NLS-1$
        lblDisc.addMouseListener(new MouseAdapter() {
          @Override
          public void mouseClicked(MouseEvent e) {
            ImageChooserDialog dialog = new ImageChooserDialog(movieToEdit.getIds(), ImageType.DISC, movieList.getDefaultArtworkScrapers(), lblDisc,
                null, null, MediaType.MOVIE);
            dialog.setLocationRelativeTo(MainWindow.getActiveInstance());
            dialog.setVisible(true);
          }
        });
        lblDisc.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        artworkPanel.add(lblDisc, "6, 12, fill, fill");
      }
    }

    /**
     * Media Files
     */
    {
      mediaFilesPanel = new MediaFileEditorPanel(mediaFiles);
      tabbedPane.addTab(BUNDLE.getString("metatag.mediafiles"), null, mediaFilesPanel, null); //$NON-NLS-1$
    }

    /**
     * Button pane
     */
    {
      JPanel bottomPane = new JPanel();
      getContentPane().add(bottomPane, BorderLayout.SOUTH);
      bottomPane.setLayout(
          new FormLayout(new ColumnSpec[] { ColumnSpec.decode("371px:grow"), FormFactory.DEFAULT_COLSPEC, FormFactory.RELATED_GAP_COLSPEC, },
              new RowSpec[] { FormFactory.LINE_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.LINE_GAP_ROWSPEC, }));

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
      int year = 0;
      try {
        year = Integer.parseInt(movieToEdit.getYear());
      }
      catch (Exception ignored) {
      }

      SimpleDateFormat dateFormat = (SimpleDateFormat) DateFormat.getDateInstance(DateFormat.MEDIUM);

      for (Certification cert : Certification.getCertificationsforCountry(MovieModuleManager.MOVIE_SETTINGS.getCertificationCountry())) {
        cbCertification.addItem(cert);
      }

      tfTitle.setText(movieToEdit.getTitle());
      tfOriginalTitle.setText(movieToEdit.getOriginalTitle());
      tfSorttitle.setText(movieToEdit.getSortTitle());
      tpTagline.setText(movieToEdit.getTagline());
      tpPlot.setText(movieToEdit.getPlot());
      tpPlot.setCaretPosition(0);
      tfDirector.setText(movieToEdit.getDirector());
      tfWriter.setText(movieToEdit.getWriter());
      lblPoster.setImagePath(movieToEdit.getArtworkFilename(MediaFileType.POSTER));
      tfProductionCompanies.setText(movieToEdit.getProductionCompany());
      spRuntime.setValue(movieToEdit.getRuntime());
      cbEdition.setSelectedItem(movieToEdit.getEdition());

      tfSpokenLanguages.setText(movieToEdit.getSpokenLanguages());
      tfCountry.setText(movieToEdit.getCountry());
      spYear.setValue(year);
      cbCertification.setSelectedItem(movieToEdit.getCertification());

      lblMoviePath.setText(movieToEdit.getPath());
      lblLogo.setImagePath(movieToEdit.getArtworkFilename(MediaFileType.LOGO));
      lblClearlogo.setImagePath(movieToEdit.getArtworkFilename(MediaFileType.CLEARLOGO));
      lblClearart.setImagePath(movieToEdit.getArtworkFilename(MediaFileType.CLEARART));
      lblThumb.setImagePath(movieToEdit.getArtworkFilename(MediaFileType.THUMB));
      lblDisc.setImagePath(movieToEdit.getArtworkFilename(MediaFileType.DISCART));
      lblBanner.setImagePath(movieToEdit.getArtworkFilename(MediaFileType.BANNER));

      for (MovieActor origCast : movieToEdit.getActors()) {
        MovieActor actor = new MovieActor();
        actor.setName(origCast.getName());
        actor.setCharacter(origCast.getCharacter());
        actor.setThumbUrl(origCast.getThumbUrl());
        cast.add(actor);
      }

      for (MovieProducer origProducer : movieToEdit.getProducers()) {
        MovieProducer producer = new MovieProducer();
        producer.setName(origProducer.getName());
        producer.setRole(origProducer.getRole());
        producer.setThumbUrl(origProducer.getThumbUrl());
        producers.add(producer);
      }

      for (MediaGenres genre : movieToEdit.getGenres()) {
        genres.add(genre);
      }

      for (MovieTrailer trailer : movieToEdit.getTrailer()) {
        trailers.add(trailer);
      }

      for (String tag : movieToEdit.getTags()) {
        if (StringUtils.isNotBlank(tag)) {
          tags.add(tag);
        }
      }

      extrathumbs.addAll(movieToEdit.getExtraThumbs());
      extrafanarts.addAll(movieToEdit.getExtraFanarts());
      for (MovieSet movieSet : movieList.getSortedMovieSetList()) {
        cbMovieSet.addItem(movieSet);
        if (movieToEdit.getMovieSet() == movieSet) {
          cbMovieSet.setSelectedItem(movieSet);
        }
      }
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
          MovieTrailer changedTrailer = trailers.get(row);
          // if flag inNFO was changed, change all other trailers flags
          if (changedTrailer.getInNfo()) {
            for (MovieTrailer trailer : trailers) {
              if (trailer != changedTrailer) {
                trailer.setInNfo(Boolean.FALSE);
              }
            }
          }
        }
      }
    });
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
      movieToEdit.setReleaseDate(dpReleaseDate.getDate());
      movieToEdit.setRuntime((Integer) spRuntime.getValue());
      movieToEdit.setTop250((Integer) spTop250.getValue());
      movieToEdit.setWatched(cbWatched.isSelected());
      movieToEdit.setSpokenLanguages(tfSpokenLanguages.getText());
      movieToEdit.setCountry(tfCountry.getText());
      movieToEdit.setMediaSource((MediaSource) cbSource.getSelectedItem());
      movieToEdit.setVideoIn3D(chckbxVideo3D.isSelected());
      movieToEdit.setEdition((MovieEdition) cbEdition.getSelectedItem());

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
        movieToEdit.getIds().remove(id);
      }

      Object certification = cbCertification.getSelectedItem();
      if (certification instanceof Certification) {
        movieToEdit.setCertification((Certification) certification);
      }

      // sync media files with the media file editor and fire the mediaFiles event
      MediaFileEditorPanel.syncMediaFiles(mediaFiles, movieToEdit.getMediaFiles());
      movieToEdit.fireEventForChangedMediaInformation();

      if (!StringUtils.isEmpty(lblPoster.getImageUrl()) && !lblPoster.getImageUrl().equals(movieToEdit.getArtworkUrl(MediaFileType.POSTER))) {
        movieToEdit.setArtworkUrl(lblPoster.getImageUrl(), MediaFileType.POSTER);
        movieToEdit.downloadArtwork(MediaFileType.POSTER);
      }

      if (!StringUtils.isEmpty(lblFanart.getImageUrl()) && !lblFanart.getImageUrl().equals(movieToEdit.getArtworkUrl(MediaFileType.FANART))) {
        movieToEdit.setArtworkUrl(lblFanart.getImageUrl(), MediaFileType.FANART);
        movieToEdit.downloadArtwork(MediaFileType.FANART);
      }

      if (!StringUtils.isEmpty(lblLogo.getImageUrl()) && !lblLogo.getImageUrl().equals(movieToEdit.getArtworkUrl(MediaFileType.LOGO))) {
        movieToEdit.setArtworkUrl(lblLogo.getImageUrl(), MediaFileType.LOGO);
        movieToEdit.downloadArtwork(MediaFileType.LOGO);
      }

      if (!StringUtils.isEmpty(lblClearlogo.getImageUrl())
          && !lblClearlogo.getImageUrl().equals(movieToEdit.getArtworkUrl(MediaFileType.CLEARLOGO))) {
        movieToEdit.setArtworkUrl(lblClearlogo.getImageUrl(), MediaFileType.CLEARLOGO);
        movieToEdit.downloadArtwork(MediaFileType.CLEARLOGO);
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
      for (MovieTrailer trailer : trailers) {
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

      double tempRating = (Double) spRating.getValue();
      float rating = (float) tempRating;
      if (movieToEdit.getRating() != rating) {
        movieToEdit.setRating(rating);
        movieToEdit.setVotes(1);
      }

      movieToEdit.writeNFO();
      movieToEdit.saveToDb();

      // if configured - sync with trakt.tv
      if (MovieModuleManager.MOVIE_SETTINGS.getSyncTrakt()) {
        TmmTask task = new SyncTraktTvTask(Arrays.asList(movieToEdit), null);
        TmmTaskManager.getInstance().addUnnamedTask(task);
      }

      setVisible(false);
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
    }
  }

  private class AddIdAction extends AbstractAction {
    private static final long serialVersionUID = 2903255414553349267L;

    public AddIdAction() {
      putValue(SHORT_DESCRIPTION, BUNDLE.getString("id.add")); //$NON-NLS-1$
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      MediaId Id = new MediaId(); // $NON-NLS-1$
      ids.add(Id);
    }
  }

  private class RemoveIdAction extends AbstractAction {
    private static final long serialVersionUID = -7079826950827356996L;

    public RemoveIdAction() {
      putValue(SHORT_DESCRIPTION, BUNDLE.getString("id.remove")); //$NON-NLS-1$
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
      putValue(SHORT_DESCRIPTION, BUNDLE.getString("genre.remove")); //$NON-NLS-1$
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      List<MediaGenres> selectedGenres = (List<MediaGenres>) listGenres.getSelectedValuesList();
      for (MediaGenres genre : selectedGenres) {
        genres.remove(genre);
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
      MovieTrailer trailer = new MovieTrailer();
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
      putValue(SHORT_DESCRIPTION, BUNDLE.getString("tag.remove")); //$NON-NLS-1$
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
      putValue(NAME, BUNDLE.getString("Button.abortqueue")); //$NON-NLS-1$
      putValue(SHORT_DESCRIPTION, BUNDLE.getString("movie.edit.abortqueue.desc")); //$NON-NLS-1$
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      continueQueue = false;
      setVisible(false);
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

  private class MoveGenreUpAction extends AbstractAction {
    private static final long serialVersionUID = -6855661707692602266L;

    public MoveGenreUpAction() {
      putValue(SHORT_DESCRIPTION, BUNDLE.getString("movie.edit.movegenreup")); //$NON-NLS-1$
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
      putValue(SHORT_DESCRIPTION, BUNDLE.getString("movie.edit.movegenredown")); //$NON-NLS-1$
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
      putValue(SHORT_DESCRIPTION, BUNDLE.getString("movie.edit.movetagup")); //$NON-NLS-1$
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
      putValue(SHORT_DESCRIPTION, BUNDLE.getString("movie.edit.movetagdown")); //$NON-NLS-1$
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

  @Override
  public void dispose() {
    super.dispose();
    jTableBinding.unbind();
    jListBinding.unbind();
    jTableBinding_1.unbind();
    jListBinding_1.unbind();
    jTableBinding_2.unbind();
    mediaFilesPanel.unbindBindings();
    dpReleaseDate.cleanup();
  }

  @Override
  public void pack() {
    // do not pack - it would look weird
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
    BeanProperty<MovieTrailer, Boolean> trailerBeanProperty = BeanProperty.create("inNfo");
    jTableBinding_1.addColumnBinding(trailerBeanProperty).setColumnClass(Boolean.class);
    //
    BeanProperty<MovieTrailer, String> trailerBeanProperty_1 = BeanProperty.create("name");
    jTableBinding_1.addColumnBinding(trailerBeanProperty_1);
    //
    BeanProperty<MovieTrailer, String> trailerBeanProperty_2 = BeanProperty.create("provider");
    jTableBinding_1.addColumnBinding(trailerBeanProperty_2);
    //
    BeanProperty<MovieTrailer, String> trailerBeanProperty_3 = BeanProperty.create("quality");
    jTableBinding_1.addColumnBinding(trailerBeanProperty_3);
    //
    BeanProperty<MovieTrailer, String> trailerBeanProperty_4 = BeanProperty.create("url");
    jTableBinding_1.addColumnBinding(trailerBeanProperty_4);
    //
    jTableBinding_1.bind();
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
}
