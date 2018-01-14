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
package org.tinymediamanager.ui.tvshows.dialogs;

import static org.tinymediamanager.core.entities.Person.Type.ACTOR;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.AbstractAction;
import javax.swing.DefaultComboBoxModel;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JLayer;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SpinnerDateModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import org.apache.commons.lang3.StringUtils;
import org.jdesktop.beansbinding.AutoBinding.UpdateStrategy;
import org.jdesktop.observablecollections.ObservableCollections;
import org.jdesktop.swingbinding.JListBinding;
import org.jdesktop.swingbinding.SwingBindings;
import org.tinymediamanager.core.AbstractModelObject;
import org.tinymediamanager.core.MediaFileType;
import org.tinymediamanager.core.entities.MediaFile;
import org.tinymediamanager.core.entities.Person;
import org.tinymediamanager.core.entities.Rating;
import org.tinymediamanager.core.threading.TmmTask;
import org.tinymediamanager.core.threading.TmmTaskManager;
import org.tinymediamanager.core.tvshow.TvShowList;
import org.tinymediamanager.core.tvshow.TvShowModuleManager;
import org.tinymediamanager.core.tvshow.entities.TvShow;
import org.tinymediamanager.core.tvshow.entities.TvShowEpisode;
import org.tinymediamanager.scraper.ScraperType;
import org.tinymediamanager.scraper.entities.Certification;
import org.tinymediamanager.scraper.entities.MediaAiredStatus;
import org.tinymediamanager.scraper.entities.MediaGenres;
import org.tinymediamanager.scraper.entities.MediaType;
import org.tinymediamanager.scraper.trakttv.SyncTraktTvTask;
import org.tinymediamanager.ui.IconManager;
import org.tinymediamanager.ui.LeftDotTableCellRenderer;
import org.tinymediamanager.ui.MainWindow;
import org.tinymediamanager.ui.ShadowLayerUI;
import org.tinymediamanager.ui.TableColumnResizer;
import org.tinymediamanager.ui.TableSpinnerEditor;
import org.tinymediamanager.ui.UIConstants;
import org.tinymediamanager.ui.components.ImageLabel;
import org.tinymediamanager.ui.components.MainTabbedPane;
import org.tinymediamanager.ui.components.MediaIdTable;
import org.tinymediamanager.ui.components.MediaIdTable.MediaId;
import org.tinymediamanager.ui.components.MediaRatingTable;
import org.tinymediamanager.ui.components.MediaRatingTable.MediaRating;
import org.tinymediamanager.ui.components.PersonTable;
import org.tinymediamanager.ui.components.TmmLabel;
import org.tinymediamanager.ui.components.combobox.AutocompleteComboBox;
import org.tinymediamanager.ui.components.datepicker.DatePicker;
import org.tinymediamanager.ui.components.datepicker.YearSpinner;
import org.tinymediamanager.ui.components.table.TmmTable;
import org.tinymediamanager.ui.components.table.TmmTableFormat;
import org.tinymediamanager.ui.components.table.TmmTableModel;
import org.tinymediamanager.ui.dialogs.IdEditorDialog;
import org.tinymediamanager.ui.dialogs.ImageChooserDialog;
import org.tinymediamanager.ui.dialogs.ImageChooserDialog.ImageType;
import org.tinymediamanager.ui.dialogs.PersonEditorDialog;
import org.tinymediamanager.ui.dialogs.RatingEditorDialog;
import org.tinymediamanager.ui.dialogs.TmmDialog;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.ObservableElementList;
import ca.odell.glazedlists.gui.WritableTableFormat;
import ca.odell.glazedlists.swing.AutoCompleteSupport;
import ca.odell.glazedlists.swing.DefaultEventTableModel;
import ca.odell.glazedlists.swing.GlazedListsSwing;
import net.miginfocom.swing.MigLayout;

/**
 * The Class TvShowEditor.
 * 
 * @author Manuel Laggner
 */
public class TvShowEditorDialog extends TmmDialog {
  private static final long                 serialVersionUID = 3270218410302989845L;
  private static final Insets               BUTTON_MARGIN    = UIConstants.SMALL_BUTTON_MARGIN;

  private TvShow                            tvShowToEdit;
  private TvShowList                        tvShowList       = TvShowList.getInstance();
  private EventList<Person>                 actors;
  private List<MediaGenres>                 genres           = ObservableCollections.observableList(new ArrayList<MediaGenres>());
  private EventList<MediaId>                ids              = new BasicEventList<>();
  private EventList<MediaRating>            ratings          = new BasicEventList<>();
  private List<String>                      tags             = ObservableCollections.observableList(new ArrayList<String>());
  private EventList<EpisodeEditorContainer> episodes;
  private Rating                            userRating;
  private boolean                           continueQueue    = true;
  private boolean                           inQueue;

  /**
   * UI elements
   */
  private JTextField                        tfTitle;
  private YearSpinner                       spYear;
  private JTextArea                         taPlot;
  private TmmTable                          tableActors;
  private ImageLabel                        lblPoster;
  private ImageLabel                        lblFanart;
  private ImageLabel                        lblBanner;
  private JSpinner                          spRuntime;
  private JTextField                        tfStudio;
  private JList<MediaGenres>                listGenres;
  private AutocompleteComboBox<MediaGenres> cbGenres;
  private AutoCompleteSupport<MediaGenres>  cbGenresAutoCompleteSupport;
  private JSpinner                          spRating;
  private JComboBox                         cbCertification;
  private JComboBox<MediaAiredStatus>       cbStatus;

  private AutocompleteComboBox<String>      cbTags;
  private AutoCompleteSupport<String>       cbTagsAutoCompleteSupport;
  private JList<String>                     listTags;
  private JSpinner                          spDateAdded;
  private DatePicker                        dpPremiered;
  private TmmTable                          tableEpisodes;
  private JTextField                        tfSorttitle;

  private JTextField                        tfPoster;
  private JTextField                        tfFanart;
  private JTextField                        tfLogo;
  private JTextField                        tfClearLogo;
  private JTextField                        tfBanner;
  private JTextField                        tfClearArt;
  private JTextField                        tfThumb;

  private ImageLabel                        lblLogo;
  private ImageLabel                        lblClearlogo;
  private ImageLabel                        lblClearart;
  private ImageLabel                        lblThumb;

  private TmmTable                          tableIds;
  private TmmTable                          tableRatings;
  private JTextField                        tfOriginalTitle;

  /**
   * Instantiates a new tv show editor dialog.
   * 
   * @param tvShow
   *          the tv show
   * @param inQueue
   *          the in queue
   */
  public TvShowEditorDialog(TvShow tvShow, boolean inQueue) {
    super(BUNDLE.getString("tvshow.edit") + "  < " + tvShow.getPathNIO() + " >", "tvShowEditor"); //$NON-NLS-1$

    this.tvShowToEdit = tvShow;
    this.inQueue = inQueue;
    ids = MediaIdTable.convertIdMapToEventList(tvShowToEdit.getIds());
    ratings = MediaRatingTable.convertRatingMapToEventList(tvShowToEdit.getRatings(), false);
    userRating = tvShowToEdit.getRating(Rating.USER);

    // creation of lists
    actors = new ObservableElementList<>(GlazedLists.threadSafeList(new BasicEventList<>()), GlazedLists.beanConnector(Person.class));
    episodes = new ObservableElementList<>(GlazedLists.threadSafeList(new BasicEventList<>()),
        GlazedLists.beanConnector(EpisodeEditorContainer.class));

    initComponents();
    initDataBindings();

    {
      tfTitle.setText(tvShow.getTitle());
      tfOriginalTitle.setText(tvShow.getOriginalTitle());
      tfSorttitle.setText(tvShow.getSortTitle());
      taPlot.setText(tvShow.getPlot());
      lblPoster.setImagePath(tvShow.getArtworkFilename(MediaFileType.POSTER));
      lblFanart.setImagePath(tvShow.getArtworkFilename(MediaFileType.FANART));
      lblLogo.setImagePath(tvShow.getArtworkFilename(MediaFileType.LOGO));
      lblClearlogo.setImagePath(tvShow.getArtworkFilename(MediaFileType.CLEARLOGO));
      lblClearart.setImagePath(tvShow.getArtworkFilename(MediaFileType.CLEARART));
      lblThumb.setImagePath(tvShow.getArtworkFilename(MediaFileType.THUMB));
      lblBanner.setImagePath(tvShow.getArtworkFilename(MediaFileType.BANNER));

      tfPoster.setText(tvShow.getArtworkUrl(MediaFileType.POSTER));
      tfFanart.setText(tvShow.getArtworkUrl(MediaFileType.FANART));
      tfLogo.setText(tvShow.getArtworkUrl(MediaFileType.LOGO));
      tfClearLogo.setText(tvShow.getArtworkUrl(MediaFileType.CLEARLOGO));
      tfClearArt.setText(tvShow.getArtworkUrl(MediaFileType.CLEARART));
      tfThumb.setText(tvShow.getArtworkUrl(MediaFileType.THUMB));
      tfBanner.setText(tvShow.getArtworkUrl(MediaFileType.BANNER));

      tfStudio.setText(tvShow.getProductionCompany());
      cbStatus.setSelectedItem(tvShow.getStatus());
      spRuntime.setValue(tvShow.getRuntime());
      int year = tvShow.getYear();
      spYear.setValue(year);
      spDateAdded.setValue(tvShow.getDateAdded());
      spRating.setModel(new SpinnerNumberModel(userRating.getRating(), 0.0, 10.0, 0.1));

      cbCertification.setSelectedItem(tvShow.getCertification());

      for (Person origCast : tvShow.getActors()) {
        actors.add(new Person(origCast));
      }
      lblBanner.setImagePath(tvShow.getArtworkFilename(MediaFileType.BANNER));
      lblFanart.setImagePath(tvShow.getArtworkFilename(MediaFileType.FANART));

      genres.addAll(tvShow.getGenres());
      tags.addAll(tvShowToEdit.getTags());

      for (Certification cert : Certification.getCertificationsforCountry(TvShowModuleManager.SETTINGS.getCertificationCountry())) {
        cbCertification.addItem(cert);
      }

      List<TvShowEpisode> epl = new ArrayList<>(tvShowToEdit.getEpisodes());
      // custom sort per filename (just this time)
      // for unknown EPs (-1/-1) this is extremely useful to sort like on filesystem
      // and for already renamed ones, it makes no difference
      epl.sort(Comparator.comparing(s -> s.getMediaFiles(MediaFileType.VIDEO).get(0).getFile()));

      for (TvShowEpisode episode : epl) {
        EpisodeEditorContainer container = new EpisodeEditorContainer();
        container.tvShowEpisode = episode;
        container.dvdOrder = episode.isDvdOrder();
        container.season = episode.getSeason();
        container.episode = episode.getEpisode();
        episodes.add(container);
      }

      if (((DefaultComboBoxModel) cbCertification.getModel()).getIndexOf(tvShow.getCertification()) == -1) {
        cbCertification.addItem(tvShow.getCertification());
      }

    }

    // adjust columnn titles - we have to do it this way - thx to windowbuilder pro
    tableEpisodes.getColumnModel().getColumn(1).setCellRenderer(new LeftDotTableCellRenderer());
    tableEpisodes.getColumnModel().getColumn(2).setCellEditor(new TableSpinnerEditor());
    tableEpisodes.getColumnModel().getColumn(3).setCellEditor(new TableSpinnerEditor());

    // adjust table columns
    TableColumnResizer.adjustColumnPreferredWidths(tableActors, 6);
    TableColumnResizer.adjustColumnPreferredWidths(tableEpisodes, 6);
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
      details1Panel.setLayout(
          new MigLayout("", "[][grow][50lp:75lp][][60lp:75lp][100lp:n][][25lp:n][200lp:250lp,grow]", "[][][][100lp:175lp][][][][][][75lp:100lp]"));

      {
        JLabel lblTitle = new TmmLabel(BUNDLE.getString("metatag.title")); //$NON-NLS-1$
        details1Panel.add(lblTitle, "cell 0 0,alignx right");

        tfTitle = new JTextField();
        details1Panel.add(tfTitle, "cell 1 0 6 1,growx");
      }
      {
        lblPoster = new ImageLabel();
        lblPoster.addMouseListener(new MouseAdapter() {
          @Override
          public void mouseClicked(MouseEvent e) {
            ImageChooserDialog dialog = new ImageChooserDialog(tvShowToEdit.getIds(), ImageType.POSTER, tvShowList.getAvailableArtworkScrapers(),
                lblPoster, null, null, MediaType.TV_SHOW);
            dialog.setLocationRelativeTo(MainWindow.getActiveInstance());
            dialog.setVisible(true);
            updateArtworkUrl(lblPoster, tfPoster);
          }
        });
        lblPoster.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        details1Panel.add(lblPoster, "cell 8 0 1 6,grow");
      }
      {
        JLabel lblOriginalTitleT = new TmmLabel(BUNDLE.getString("metatag.originaltitle")); //$NON-NLS-1$
        details1Panel.add(lblOriginalTitleT, "cell 0 1,alignx right");

        tfOriginalTitle = new JTextField();
        details1Panel.add(tfOriginalTitle, "cell 1 1 6 1,growx");
      }
      {
        JLabel lblSortTitle = new TmmLabel(BUNDLE.getString("metatag.sorttitle")); //$NON-NLS-1$
        details1Panel.add(lblSortTitle, "cell 0 2,alignx right");

        tfSorttitle = new JTextField();
        details1Panel.add(tfSorttitle, "cell 1 2 6 1,growx");
      }
      {
        JLabel lblPlot = new TmmLabel(BUNDLE.getString("metatag.plot")); //$NON-NLS-1$
        details1Panel.add(lblPlot, "cell 0 3,alignx right,aligny top");

        JScrollPane scrollPanePlot = new JScrollPane();
        details1Panel.add(scrollPanePlot, "cell 1 3 6 1,grow");

        taPlot = new JTextArea();
        taPlot.setLineWrap(true);
        taPlot.setWrapStyleWord(true);
        taPlot.setForeground(UIManager.getColor("TextField.foreground")); //$NON-NLS-1$
        scrollPanePlot.setViewportView(taPlot);
      }
      {
        JLabel lblYear = new TmmLabel(BUNDLE.getString("metatag.year")); //$NON-NLS-1$
        details1Panel.add(lblYear, "cell 0 4,alignx right");

        spYear = new YearSpinner();
        details1Panel.add(spYear, "cell 1 4,growx");
      }
      {
        JLabel lblpremiered = new TmmLabel(BUNDLE.getString("metatag.premiered")); //$NON-NLS-1$
        details1Panel.add(lblpremiered, "cell 3 4,alignx right");

        dpPremiered = new DatePicker(tvShowToEdit.getFirstAired());
        details1Panel.add(dpPremiered, "cell 4 4 2 1,growx");
      }
      JLabel lblStudio = new TmmLabel(BUNDLE.getString("metatag.studio")); //$NON-NLS-1$
      details1Panel.add(lblStudio, "cell 0 5,alignx right");

      {

        tfStudio = new JTextField();
        details1Panel.add(tfStudio, "cell 1 5 6 1,growx");
      }
      {
        JLabel lblRuntime = new TmmLabel(BUNDLE.getString("metatag.runtime")); //$NON-NLS-1$
        details1Panel.add(lblRuntime, "cell 0 6,alignx right");

        spRuntime = new JSpinner();
        details1Panel.add(spRuntime, "flowx,cell 1 6,growx");

        JLabel lblMin = new TmmLabel(BUNDLE.getString("metatag.minutes")); //$NON-NLS-1$
        details1Panel.add(lblMin, "cell 1 6");
      }
      {
        JLabel lblStatus = new TmmLabel(BUNDLE.getString("metatag.status")); //$NON-NLS-1$
        details1Panel.add(lblStatus, "cell 3 6,alignx right");

        cbStatus = new JComboBox(MediaAiredStatus.values());
        details1Panel.add(cbStatus, "cell 4 6,growx");
      }
      {
        JLabel lblCertification = new TmmLabel(BUNDLE.getString("metatag.certification")); //$NON-NLS-1$
        details1Panel.add(lblCertification, "cell 0 7,alignx right");

        cbCertification = new JComboBox();
        details1Panel.add(cbCertification, "cell 1 7,growx");
      }
      {
        JLabel lblRating = new TmmLabel(BUNDLE.getString("metatag.userrating")); //$NON-NLS-1$
        details1Panel.add(lblRating, "cell 0 8,alignx right");

        spRating = new JSpinner();
        details1Panel.add(spRating, "cell 1 8,growx");
      }
      {
        JLabel lblRatingsT = new TmmLabel(BUNDLE.getString("metatag.ratings")); //$NON-NLS-1$
        details1Panel.add(lblRatingsT, "flowy,cell 0 9,alignx right,aligny top");

        JScrollPane scrollPaneRatings = new JScrollPane();
        details1Panel.add(scrollPaneRatings, "cell 1 9 4 1,growx");

        tableRatings = new MediaRatingTable(ratings);
        tableRatings.configureScrollPane(scrollPaneRatings);
        scrollPaneRatings.setViewportView(tableRatings);
      }
      {
        lblFanart = new ImageLabel();
        lblFanart.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        lblFanart.addMouseListener(new MouseAdapter() {
          @Override
          public void mouseClicked(MouseEvent e) {
            ImageChooserDialog dialog = new ImageChooserDialog(tvShowToEdit.getIds(), ImageType.FANART, tvShowList.getAvailableArtworkScrapers(),
                lblFanart, null, null, MediaType.TV_SHOW);
            dialog.setLocationRelativeTo(MainWindow.getActiveInstance());
            dialog.setVisible(true);
            updateArtworkUrl(lblFanart, tfFanart);
          }
        });
        details1Panel.add(lblFanart, "cell 8 7 1 3,grow");
      }

      JButton btnAddRating = new JButton(new AddRatingAction());
      btnAddRating.setMargin(BUTTON_MARGIN);
      details1Panel.add(btnAddRating, "cell 0 9,alignx right,aligny top");

      JButton btnRemoveRating = new JButton(new RemoveRatingAction());
      btnRemoveRating.setMargin(BUTTON_MARGIN);
      details1Panel.add(btnRemoveRating, "cell 0 9,alignx right,aligny top");
    }

    /**********************************************************************************
     * DetailsPanel 2
     **********************************************************************************/
    {
      JPanel details2Panel = new JPanel();
      tabbedPane.addTab(BUNDLE.getString("metatag.details2"), details2Panel); //$NON-NLS-1$

      details2Panel.setLayout(
          new MigLayout("", "[][150lp:400lp,grow][20lp:n][][150lp:300lp,grow]", "[][:150lp:200lp,grow][20lp:n][100lp:150lp,grow][][100lp,grow 200]"));
      {
        JLabel lblActors = new TmmLabel(BUNDLE.getString("metatag.actors")); //$NON-NLS-1$
        details2Panel.add(lblActors, "flowy,cell 0 0 1 2,alignx right,aligny top");

        JScrollPane scrollPaneActors = new JScrollPane();
        details2Panel.add(scrollPaneActors, "cell 1 0 1 2,grow");
        tableActors = new PersonTable(actors, true);
        tableActors.configureScrollPane(scrollPaneActors);

        JButton btnAddActor = new JButton(new AddActorAction());
        btnAddActor.setMargin(BUTTON_MARGIN);
        details2Panel.add(btnAddActor, "cell 0 0,alignx right");

        JButton btnRemoveActor = new JButton(new RemoveActorAction()); // $NON-NLS-1$
        btnRemoveActor.setMargin(BUTTON_MARGIN);
        details2Panel.add(btnRemoveActor, "cell 0 0,alignx right,aligny top");

        JButton btnMoveActorUp = new JButton(new MoveActorUpAction());
        btnMoveActorUp.setMargin(BUTTON_MARGIN);
        details2Panel.add(btnMoveActorUp, "cell 0 0,alignx right");

        JButton btnMoveActorDown = new JButton(new MoveActorDownAction());
        btnMoveActorDown.setMargin(BUTTON_MARGIN);
        details2Panel.add(btnMoveActorDown, "cell 0 0,alignx right,aligny top");
      }
      {
        JLabel lblDateAdded = new TmmLabel(BUNDLE.getString("metatag.dateadded")); //$NON-NLS-1$
        details2Panel.add(lblDateAdded, "cell 3 0,alignx right");

        spDateAdded = new JSpinner(new SpinnerDateModel());
        details2Panel.add(spDateAdded, "cell 4 0");
      }
      {
        JLabel lblIds = new TmmLabel("Ids");
        details2Panel.add(lblIds, "flowy,cell 3 1,alignx right,aligny top");

        JScrollPane scrollPaneIds = new JScrollPane();
        details2Panel.add(scrollPaneIds, "cell 4 1,grow");

        tableIds = new MediaIdTable(ids, ScraperType.TV_SHOW);
        tableIds.configureScrollPane(scrollPaneIds);

        JButton btnAddId = new JButton(new AddIdAction());
        btnAddId.setMargin(BUTTON_MARGIN);
        details2Panel.add(btnAddId, "cell 3 1,alignx right");

        JButton btnRemoveId = new JButton(new RemoveIdAction());
        btnRemoveId.setMargin(BUTTON_MARGIN);
        details2Panel.add(btnRemoveId, "cell 3 1,alignx right,aligny top");
      }
      {
        JLabel lblGenres = new TmmLabel(BUNDLE.getString("metatag.genre")); //$NON-NLS-1$
        details2Panel.add(lblGenres, "flowy,cell 0 3,alignx right,aligny top");

        JScrollPane scrollPaneGenres = new JScrollPane();
        details2Panel.add(scrollPaneGenres, "cell 1 3,grow");

        listGenres = new JList<>();
        scrollPaneGenres.setViewportView(listGenres);

        JButton btnAddGenre = new JButton(new AddGenreAction());
        btnAddGenre.setMargin(BUTTON_MARGIN);
        details2Panel.add(btnAddGenre, "cell 0 3,alignx right");

        JButton btnRemoveGenre = new JButton(new RemoveGenreAction());
        btnRemoveGenre.setMargin(BUTTON_MARGIN);
        details2Panel.add(btnRemoveGenre, "cell 0 3,alignx right,aligny top");

        JButton btnMoveGenreUp = new JButton(new MoveGenreUpAction());
        btnMoveGenreUp.setMargin(BUTTON_MARGIN);
        details2Panel.add(btnMoveGenreUp, "cell 0 3,alignx right,aligny top");

        JButton btnMoveGenreDown = new JButton(new MoveGenreDownAction());
        btnMoveGenreDown.setMargin(BUTTON_MARGIN);
        details2Panel.add(btnMoveGenreDown, "cell 0 3,alignx right,aligny top");

        cbGenres = new AutocompleteComboBox(MediaGenres.values());
        cbGenresAutoCompleteSupport = cbGenres.getAutoCompleteSupport();
        InputMap im = cbGenres.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        Object enterAction = im.get(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0));
        cbGenres.getActionMap().put(enterAction, new AddGenreAction());
        details2Panel.add(cbGenres, "cell 1 4,growx");
      }
      {
        JLabel lblTags = new TmmLabel(BUNDLE.getString("metatag.tags")); //$NON-NLS-1$
        details2Panel.add(lblTags, "flowy,cell 3 3,alignx right,aligny top");

        JScrollPane scrollPaneTags = new JScrollPane();
        details2Panel.add(scrollPaneTags, "cell 4 3,grow");
        listTags = new JList<>();
        scrollPaneTags.setViewportView(listTags);

        JButton btnAddTag = new JButton(new AddTagAction());
        btnAddTag.setMargin(BUTTON_MARGIN);
        details2Panel.add(btnAddTag, "cell 3 3,alignx right");

        JButton btnRemoveTag = new JButton(new RemoveTagAction());
        btnRemoveTag.setMargin(BUTTON_MARGIN);
        details2Panel.add(btnRemoveTag, "cell 3 3,alignx right,aligny top");

        JButton btnMoveTagUp = new JButton(new MoveTagUpAction());
        btnMoveTagUp.setMargin(BUTTON_MARGIN);
        details2Panel.add(btnMoveTagUp, "cell 3 3,alignx right,aligny top");

        JButton btnMoveTagDown = new JButton(new MoveTagDownAction());
        btnMoveTagDown.setMargin(BUTTON_MARGIN);
        details2Panel.add(btnMoveTagDown, "cell 3 3,alignx right,aligny top");

        cbTags = new AutocompleteComboBox<>(tvShowList.getTagsInTvShows());
        cbTagsAutoCompleteSupport = cbTags.getAutoCompleteSupport();
        InputMap im = cbTags.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        Object enterAction = im.get(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0));
        cbTags.getActionMap().put(enterAction, new AddTagAction());
        details2Panel.add(cbTags, "cell 4 4,growx");
      }
    }

    /**********************************************************************************
     * artwork urls
     **********************************************************************************/
    {
      JPanel artworkPanel = new JPanel();
      tabbedPane.addTab(BUNDLE.getString("edit.artwork"), null, artworkPanel, null);
      artworkPanel.setLayout(new MigLayout("", "[][grow]", "[][][][][][][]"));
      {
        JLabel lblPosterT = new TmmLabel(BUNDLE.getString("mediafiletype.poster")); //$NON-NLS-1$
        artworkPanel.add(lblPosterT, "cell 0 0,alignx right");
      }
      {
        tfPoster = new JTextField();
        artworkPanel.add(tfPoster, "cell 1 0,growx");

        JLabel lblFanartT = new TmmLabel(BUNDLE.getString("mediafiletype.fanart")); //$NON-NLS-1$
        artworkPanel.add(lblFanartT, "cell 0 1,alignx right");

        tfFanart = new JTextField();
        artworkPanel.add(tfFanart, "cell 1 1,growx");
      }
      {
        JLabel lblLogoT = new TmmLabel(BUNDLE.getString("mediafiletype.logo")); //$NON-NLS-1$
        artworkPanel.add(lblLogoT, "cell 0 2,alignx right");

        tfLogo = new JTextField();
        artworkPanel.add(tfLogo, "cell 1 2,growx");
      }
      {
        JLabel lblClearLogoT = new TmmLabel(BUNDLE.getString("mediafiletype.clearlogo")); //$NON-NLS-1$
        artworkPanel.add(lblClearLogoT, "cell 0 3,alignx right");

        tfClearLogo = new JTextField();
        artworkPanel.add(tfClearLogo, "cell 1 3,growx");
      }
      {
        JLabel lblBannerT = new TmmLabel(BUNDLE.getString("mediafiletype.banner")); //$NON-NLS-1$
        artworkPanel.add(lblBannerT, "cell 0 4,alignx right");

        tfBanner = new JTextField();
        artworkPanel.add(tfBanner, "cell 1 4,growx");
      }
      {
        JLabel lblClearArtT = new TmmLabel(BUNDLE.getString("mediafiletype.clearart")); //$NON-NLS-1$
        artworkPanel.add(lblClearArtT, "cell 0 5,alignx right");

        tfClearArt = new JTextField();
        artworkPanel.add(tfClearArt, "cell 1 5,growx");
      }
      {
        JLabel lblThumbT = new TmmLabel(BUNDLE.getString("mediafiletype.thumb")); //$NON-NLS-1$
        artworkPanel.add(lblThumbT, "cell 0 6,alignx right");

        tfThumb = new JTextField();
        artworkPanel.add(tfThumb, "cell 1 6,growx");
      }
    }

    /**********************************************************************************
     * local artwork pane
     **********************************************************************************/
    {
      JPanel artworkPanel = new JPanel();
      tabbedPane.addTab(BUNDLE.getString("metatag.extraartwork"), null, artworkPanel, null);
      artworkPanel.setLayout(new MigLayout("", "[200lp:300lp,grow][20lp:n][200lp:300lp,grow]",
          "[][100lp:125lp,grow][20lp:n][][100lp:125lp,grow][20lp:n][][100lp:150lp,grow]"));
      {
        JLabel lblLogoT = new TmmLabel(BUNDLE.getString("mediafiletype.logo")); //$NON-NLS-1$
        artworkPanel.add(lblLogoT, "cell 0 0");

        lblLogo = new ImageLabel();
        lblLogo.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        lblLogo.addMouseListener(new MouseAdapter() {
          @Override
          public void mouseClicked(MouseEvent e) {
            ImageChooserDialog dialog = new ImageChooserDialog(tvShowToEdit.getIds(), ImageType.LOGO, tvShowList.getAvailableArtworkScrapers(),
                lblLogo, null, null, MediaType.TV_SHOW);
            dialog.setLocationRelativeTo(MainWindow.getActiveInstance());
            dialog.setVisible(true);
            updateArtworkUrl(lblLogo, tfLogo);
          }
        });
        artworkPanel.add(lblLogo, "cell 0 1,grow");
      }
      {
        JLabel lblClearlogoT = new TmmLabel(BUNDLE.getString("mediafiletype.clearlogo")); //$NON-NLS-1$
        artworkPanel.add(lblClearlogoT, "cell 2 0");

        lblClearlogo = new ImageLabel();
        lblClearlogo.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        lblClearlogo.addMouseListener(new MouseAdapter() {
          @Override
          public void mouseClicked(MouseEvent e) {
            ImageChooserDialog dialog = new ImageChooserDialog(tvShowToEdit.getIds(), ImageType.CLEARLOGO, tvShowList.getAvailableArtworkScrapers(),
                lblClearlogo, null, null, MediaType.TV_SHOW);
            dialog.setLocationRelativeTo(MainWindow.getActiveInstance());
            dialog.setVisible(true);
            updateArtworkUrl(lblClearlogo, tfClearLogo);
          }
        });
        artworkPanel.add(lblClearlogo, "cell 2 1,grow");
      }
      {
        JLabel lblBannerT = new TmmLabel(BUNDLE.getString("mediafiletype.banner")); //$NON-NLS-1$
        artworkPanel.add(lblBannerT, "cell 0 3");

        lblBanner = new ImageLabel();
        lblBanner.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        lblBanner.addMouseListener(new MouseAdapter() {
          @Override
          public void mouseClicked(MouseEvent e) {
            ImageChooserDialog dialog = new ImageChooserDialog(tvShowToEdit.getIds(), ImageType.BANNER, tvShowList.getAvailableArtworkScrapers(),
                lblBanner, null, null, MediaType.TV_SHOW);
            dialog.setLocationRelativeTo(MainWindow.getActiveInstance());
            dialog.setVisible(true);
            updateArtworkUrl(lblBanner, tfBanner);
          }
        });
        artworkPanel.add(lblBanner, "cell 0 4 3 1,grow");
      }
      {

        lblClearart = new ImageLabel();
        lblClearart.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        lblClearart.addMouseListener(new MouseAdapter() {
          @Override
          public void mouseClicked(MouseEvent e) {
            ImageChooserDialog dialog = new ImageChooserDialog(tvShowToEdit.getIds(), ImageType.CLEARART, tvShowList.getAvailableArtworkScrapers(),
                lblClearart, null, null, MediaType.TV_SHOW);
            dialog.setLocationRelativeTo(MainWindow.getActiveInstance());
            dialog.setVisible(true);
            updateArtworkUrl(lblClearart, tfClearArt);
          }
        });

        JLabel lblClearartT = new TmmLabel(BUNDLE.getString("mediafiletype.clearart")); //$NON-NLS-1$
        artworkPanel.add(lblClearartT, "cell 0 6");
        JLabel lblThumbT = new TmmLabel(BUNDLE.getString("mediafiletype.thumb")); //$NON-NLS-1$
        artworkPanel.add(lblThumbT, "cell 2 6");
        artworkPanel.add(lblClearart, "cell 0 7,grow");
      }
      {

        lblThumb = new ImageLabel();
        lblThumb.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        lblThumb.addMouseListener(new MouseAdapter() {
          @Override
          public void mouseClicked(MouseEvent e) {
            ImageChooserDialog dialog = new ImageChooserDialog(tvShowToEdit.getIds(), ImageType.THUMB, tvShowList.getAvailableArtworkScrapers(),
                lblThumb, null, null, MediaType.TV_SHOW);
            dialog.setLocationRelativeTo(MainWindow.getActiveInstance());
            dialog.setVisible(true);
            updateArtworkUrl(lblThumb, tfThumb);
          }
        });

        artworkPanel.add(lblThumb, "cell 2 7,grow");
      }
    }

    /**********************************************************************************
     * episode pane
     **********************************************************************************/
    {
      JPanel episodesPanel = new JPanel();

      tabbedPane.addTab(BUNDLE.getString("metatag.episodes"), episodesPanel);
      episodesPanel.setLayout(new MigLayout("", "[][grow]", "[][100px:n,grow]"));
      {
        JButton btnCloneEpisode = new JButton(new CloneEpisodeAction());
        btnCloneEpisode.setMargin(BUTTON_MARGIN);
        episodesPanel.add(btnCloneEpisode, "cell 0 0");
      }
      {
        JScrollPane scrollPaneEpisodes = new JScrollPane();
        episodesPanel.add(scrollPaneEpisodes, "cell 1 0 1 2,grow");

        DefaultEventTableModel<EpisodeEditorContainer> episodeTableModel = new TmmTableModel<>(GlazedListsSwing.swingThreadProxyList(episodes),
            new EpisodeTableFormat());
        tableEpisodes = new TmmTable(episodeTableModel);
        tableEpisodes.configureScrollPane(scrollPaneEpisodes);
      }
      {
        JButton btnRemoveEpisode = new JButton(new RemoveEpisodeAction());
        btnRemoveEpisode.setMargin(BUTTON_MARGIN);
        episodesPanel.add(btnRemoveEpisode, "cell 0 1,aligny top");
      }
    }

    /**********************************************************************************
     * button pane
     **********************************************************************************/
    {
      if (inQueue) {
        JButton btnAbort = new JButton(new AbortAction());
        addButton(btnAbort);
      }

      JButton cancelButton = new JButton(new CancelAction());
      addButton(cancelButton);

      JButton okButton = new JButton(new OKAction());
      addDefaultButton(okButton);
    }
  }

  private void updateArtworkUrl(ImageLabel imageLabel, JTextField textField) {
    if (StringUtils.isNotBlank(imageLabel.getImageUrl())) {
      textField.setText(imageLabel.getImageUrl());
    }
  }

  private class OKAction extends AbstractAction {
    private static final long serialVersionUID = 6699599213348390696L;

    OKAction() {
      putValue(NAME, BUNDLE.getString("Button.ok")); //$NON-NLS-1$
      putValue(SHORT_DESCRIPTION, BUNDLE.getString("tvshow.change")); //$NON-NLS-1$
      putValue(SMALL_ICON, IconManager.APPLY_INV);
      putValue(LARGE_ICON_KEY, IconManager.APPLY_INV);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      tvShowToEdit.setTitle(tfTitle.getText());
      tvShowToEdit.setOriginalTitle(tfOriginalTitle.getText());
      tvShowToEdit.setSortTitle(tfSorttitle.getText());
      tvShowToEdit.setYear((Integer) spYear.getValue());
      tvShowToEdit.setPlot(taPlot.getText());
      tvShowToEdit.setRuntime((Integer) spRuntime.getValue());

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
          tvShowToEdit.setId(id.key, value);
        }
        catch (NumberFormatException ex) {
          // okay, we set it as a String
          tvShowToEdit.setId(id.key, id.value);
        }
      }
      // second round -> remove deleted ids
      List<String> removeIds = new ArrayList<>();
      for (Entry<String, Object> entry : tvShowToEdit.getIds().entrySet()) {
        MediaId id = new MediaId(entry.getKey());
        if (!ids.contains(id)) {
          removeIds.add(entry.getKey());
        }
      }
      for (String id : removeIds) {
        tvShowToEdit.getIds().remove(id);
      }

      Object certification = cbCertification.getSelectedItem();
      if (certification instanceof Certification) {
        tvShowToEdit.setCertification((Certification) certification);
      }

      if (!StringUtils.isEmpty(tfPoster.getText()) && !tfPoster.getText().equals(tvShowToEdit.getArtworkUrl(MediaFileType.POSTER))) {
        tvShowToEdit.setArtworkUrl(tfPoster.getText(), MediaFileType.POSTER);
        tvShowToEdit.downloadArtwork(MediaFileType.POSTER);
      }

      if (!StringUtils.isEmpty(tfFanart.getText()) && !tfFanart.getText().equals(tvShowToEdit.getArtworkUrl(MediaFileType.FANART))) {
        tvShowToEdit.setArtworkUrl(tfFanart.getText(), MediaFileType.FANART);
        tvShowToEdit.downloadArtwork(MediaFileType.FANART);
      }

      if (!StringUtils.isEmpty(tfBanner.getText()) && !tfBanner.getText().equals(tvShowToEdit.getArtworkUrl(MediaFileType.BANNER))) {
        tvShowToEdit.setArtworkUrl(tfBanner.getText(), MediaFileType.BANNER);
        tvShowToEdit.downloadArtwork(MediaFileType.BANNER);
      }

      if (!StringUtils.isEmpty(tfLogo.getText()) && !tfLogo.getText().equals(tvShowToEdit.getArtworkUrl(MediaFileType.LOGO))) {
        tvShowToEdit.setArtworkUrl(tfLogo.getText(), MediaFileType.LOGO);
        tvShowToEdit.downloadArtwork(MediaFileType.LOGO);
      }

      if (!StringUtils.isEmpty(tfClearLogo.getText()) && !tfClearLogo.getText().equals(tvShowToEdit.getArtworkUrl(MediaFileType.CLEARLOGO))) {
        tvShowToEdit.setArtworkUrl(tfClearLogo.getText(), MediaFileType.CLEARLOGO);
        tvShowToEdit.downloadArtwork(MediaFileType.CLEARLOGO);
      }

      if (!StringUtils.isEmpty(tfClearArt.getText()) && !tfClearArt.getText().equals(tvShowToEdit.getArtworkUrl(MediaFileType.CLEARART))) {
        tvShowToEdit.setArtworkUrl(tfClearArt.getText(), MediaFileType.CLEARART);
        tvShowToEdit.downloadArtwork(MediaFileType.CLEARART);
      }

      if (!StringUtils.isEmpty(tfThumb.getText()) && !tfThumb.getText().equals(tvShowToEdit.getArtworkUrl(MediaFileType.THUMB))) {
        tvShowToEdit.setArtworkUrl(tfThumb.getText(), MediaFileType.THUMB);
        tvShowToEdit.downloadArtwork(MediaFileType.THUMB);
      }

      tvShowToEdit.setProductionCompany(tfStudio.getText());
      tvShowToEdit.setActors(actors);
      tvShowToEdit.setGenres(genres);

      tvShowToEdit.setTags(tags);
      tvShowToEdit.setDateAdded((Date) spDateAdded.getValue());
      tvShowToEdit.setFirstAired(dpPremiered.getDate());

      tvShowToEdit.setStatus((MediaAiredStatus) cbStatus.getSelectedItem());

      // user rating
      Map<String, Rating> ratings = new HashMap<>();

      if ((double) spRating.getValue() > 0) {
        Rating userRating = new Rating(Rating.USER, (double) spRating.getValue(), 1, 10);
        ratings.put(Rating.USER, userRating);
      }

      // other ratings
      for (MediaRating mediaRating : TvShowEditorDialog.this.ratings) {
        if (StringUtils.isNotBlank(mediaRating.key) && mediaRating.value > 0 && mediaRating.votes > 0) {
          Rating rating = new Rating(mediaRating.key, mediaRating.value, mediaRating.votes, mediaRating.maxValue);
          ratings.put(mediaRating.key, rating);
        }
      }
      tvShowToEdit.setRatings(ratings);

      // adapt episodes according to the episode table (in a 2 way sync)
      // remove episodes
      for (int i = tvShowToEdit.getEpisodeCount() - 1; i >= 0; i--) {
        boolean found = false;
        TvShowEpisode episode = tvShowToEdit.getEpisodes().get(i);
        for (EpisodeEditorContainer container : episodes) {
          if (container.tvShowEpisode == episode) {
            found = true;
            break;
          }
        }

        if (!found) {
          tvShowToEdit.removeEpisode(episode);
        }
      }

      // add episodes
      for (EpisodeEditorContainer container : episodes) {
        boolean found = false;
        boolean shouldStore = false;

        if (container.dvdOrder != container.tvShowEpisode.isDvdOrder()) {
          container.tvShowEpisode.setDvdOrder(container.dvdOrder);
          shouldStore = true;
        }

        if (container.episode != container.tvShowEpisode.getEpisode()) {
          if (container.dvdOrder) {
            container.tvShowEpisode.setDvdEpisode(container.episode);
          }
          else {
            container.tvShowEpisode.setAiredEpisode(container.episode);
          }

          shouldStore = true;
        }

        if (container.season != container.tvShowEpisode.getSeason()) {
          if (container.dvdOrder) {
            container.tvShowEpisode.setDvdSeason(container.season);
          }
          else {
            container.tvShowEpisode.setAiredSeason(container.season);
          }
          shouldStore = true;
        }

        for (TvShowEpisode episode : tvShowToEdit.getEpisodes()) {
          if (container.tvShowEpisode == episode) {
            found = true;
            break;
          }
        }

        if (!found) {
          container.tvShowEpisode.writeNFO();
          container.tvShowEpisode.saveToDb();
          tvShowToEdit.addEpisode(container.tvShowEpisode);
        }
        else if (shouldStore) {
          container.tvShowEpisode.writeNFO();
          container.tvShowEpisode.saveToDb();
        }
      }

      tvShowToEdit.writeNFO();
      tvShowToEdit.saveToDb();

      if (TvShowModuleManager.SETTINGS.getSyncTrakt()) {
        TmmTask task = new SyncTraktTvTask(null, Arrays.asList(tvShowToEdit));
        TmmTaskManager.getInstance().addUnnamedTask(task);
      }

      setVisible(false);
    }
  }

  private class CancelAction extends AbstractAction {
    private static final long serialVersionUID = -4617793684152607277L;

    CancelAction() {
      putValue(NAME, BUNDLE.getString("Button.cancel")); //$NON-NLS-1$
      putValue(SHORT_DESCRIPTION, BUNDLE.getString("edit.discard")); //$NON-NLS-1$
      putValue(SMALL_ICON, IconManager.CANCEL_INV);
      putValue(LARGE_ICON_KEY, IconManager.CANCEL_INV);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      setVisible(false);
    }
  }

  private class AddRatingAction extends AbstractAction {
    private static final long serialVersionUID = 2903255414533349267L;

    public AddRatingAction() {
      putValue(SHORT_DESCRIPTION, BUNDLE.getString("rating.add")); //$NON-NLS-1$
      putValue(SMALL_ICON, IconManager.ADD_INV);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      MediaRating mediaRating = new MediaRating("");
      // default values
      mediaRating.maxValue = 10;
      mediaRating.votes = 1;

      RatingEditorDialog dialog = new RatingEditorDialog(SwingUtilities.getWindowAncestor(tableActors), BUNDLE.getString("rating.add"), mediaRating);
      dialog.setVisible(true);

      if (StringUtils.isNotBlank(mediaRating.key) && mediaRating.value > 0 && mediaRating.maxValue > 0 && mediaRating.votes > 0) {
        ratings.add(mediaRating);
      }
    }
  }

  private class RemoveRatingAction extends AbstractAction {
    private static final long serialVersionUID = -7079821950827356996L;

    public RemoveRatingAction() {
      putValue(SHORT_DESCRIPTION, BUNDLE.getString("rating.remove")); //$NON-NLS-1$
      putValue(SMALL_ICON, IconManager.REMOVE_INV);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      int row = tableRatings.getSelectedRow();
      if (row > -1) {
        row = tableRatings.convertRowIndexToModel(row);
        ratings.remove(row);
      }
    }
  }

  private class AddActorAction extends AbstractAction {
    private static final long serialVersionUID = -5879601617842300526L;

    AddActorAction() {
      putValue(SHORT_DESCRIPTION, BUNDLE.getString("cast.actor.add")); //$NON-NLS-1$
      putValue(SMALL_ICON, IconManager.ADD_INV);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      Person actor = new Person(ACTOR, BUNDLE.getString("cast.actor.unknown"), BUNDLE.getString("cast.role.unknown")); //$NON-NLS-1$
      PersonEditorDialog dialog = new PersonEditorDialog(SwingUtilities.getWindowAncestor(tableActors), BUNDLE.getString("cast.actor.add"), actor);
      dialog.setVisible(true);

      if (StringUtils.isNotBlank(actor.getName()) && !actor.getName().equals(BUNDLE.getString("cast.actor.unknown"))) {
        actors.add(0, actor);
      }
    }
  }

  private class RemoveActorAction extends AbstractAction {
    private static final long serialVersionUID = 6970920169867315771L;

    RemoveActorAction() {
      putValue(SHORT_DESCRIPTION, BUNDLE.getString("cast.actor.remove")); //$NON-NLS-1$
      putValue(SMALL_ICON, IconManager.REMOVE_INV);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      int row = tableActors.getSelectedRow();
      if (row > -1) {
        row = tableActors.convertRowIndexToModel(row);
        actors.remove(row);
      }
    }
  }

  private class MoveActorUpAction extends AbstractAction {
    private static final long serialVersionUID = 5775423424097844658L;

    MoveActorUpAction() {
      putValue(SHORT_DESCRIPTION, BUNDLE.getString("movie.edit.moveactorup")); //$NON-NLS-1$
      putValue(SMALL_ICON, IconManager.ARROW_UP_INV);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      int row = tableActors.getSelectedRow();
      if (row > 0) {
        Collections.rotate(actors.subList(row - 1, row + 1), 1);
        tableActors.getSelectionModel().setSelectionInterval(row - 1, row - 1);
      }
    }
  }

  private class MoveActorDownAction extends AbstractAction {
    private static final long serialVersionUID = -6564146895819191932L;

    MoveActorDownAction() {
      putValue(SHORT_DESCRIPTION, BUNDLE.getString("movie.edit.moveactordown")); //$NON-NLS-1$
      putValue(SMALL_ICON, IconManager.ARROW_DOWN_INV);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      int row = tableActors.getSelectedRow();
      if (row < actors.size() - 1) {
        Collections.rotate(actors.subList(row, row + 2), -1);
        tableActors.getSelectionModel().setSelectionInterval(row + 1, row + 1);
      }
    }
  }

  private class AddGenreAction extends AbstractAction {
    private static final long serialVersionUID = 6666302391216952247L;

    AddGenreAction() {
      putValue(SHORT_DESCRIPTION, BUNDLE.getString("genre.add")); //$NON-NLS-1$
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
    private static final long serialVersionUID = -5459615776560234688L;

    RemoveGenreAction() {
      putValue(SHORT_DESCRIPTION, BUNDLE.getString("genre.remove")); //$NON-NLS-1$
      putValue(SMALL_ICON, IconManager.REMOVE_INV);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      for (MediaGenres genre : listGenres.getSelectedValuesList()) {
        genres.remove(genre);
      }
    }
  }

  private class MoveGenreUpAction extends AbstractAction {
    private static final long serialVersionUID = -6855661707692602266L;

    MoveGenreUpAction() {
      putValue(SHORT_DESCRIPTION, BUNDLE.getString("movie.edit.movegenreup")); //$NON-NLS-1$
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

    MoveGenreDownAction() {
      putValue(SHORT_DESCRIPTION, BUNDLE.getString("movie.edit.movegenredown")); //$NON-NLS-1$
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

  /**
   * Shows the dialog and returns whether the work on the queue should be continued.
   * 
   * @return true, if successful
   */
  public boolean showDialog() {
    setVisible(true);
    return continueQueue;
  }

  private class AddTagAction extends AbstractAction {
    private static final long serialVersionUID = 9160043031922897785L;

    AddTagAction() {
      putValue(SHORT_DESCRIPTION, BUNDLE.getString("tag.add")); //$NON-NLS-1$
      putValue(SMALL_ICON, IconManager.ADD_INV);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      String newTag = (String) cbTags.getSelectedItem();
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

  private class AddIdAction extends AbstractAction {
    private static final long serialVersionUID = 2903255414553349267L;

    AddIdAction() {
      putValue(SHORT_DESCRIPTION, BUNDLE.getString("id.add")); //$NON-NLS-1$
      putValue(SMALL_ICON, IconManager.ADD_INV);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      MediaId mediaId = new MediaId();
      IdEditorDialog dialog = new IdEditorDialog(SwingUtilities.getWindowAncestor(tableIds), BUNDLE.getString("id.add"), mediaId);
      dialog.setVisible(true);

      if (StringUtils.isNoneBlank(mediaId.key, mediaId.value)) {
        ids.add(mediaId);
      }
    }
  }

  private class RemoveIdAction extends AbstractAction {
    private static final long serialVersionUID = -7079826950827356996L;

    RemoveIdAction() {
      putValue(SHORT_DESCRIPTION, BUNDLE.getString("id.remove")); //$NON-NLS-1$
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

  private class RemoveTagAction extends AbstractAction {
    private static final long serialVersionUID = -1580945350962234235L;

    RemoveTagAction() {
      putValue(SHORT_DESCRIPTION, BUNDLE.getString("tag.remove")); //$NON-NLS-1$
      putValue(SMALL_ICON, IconManager.REMOVE_INV);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      for (String tag : listTags.getSelectedValuesList()) {
        tags.remove(tag);
      }
    }
  }

  private class MoveTagUpAction extends AbstractAction {
    private static final long serialVersionUID = -6855661707692602266L;

    MoveTagUpAction() {
      putValue(SHORT_DESCRIPTION, BUNDLE.getString("movie.edit.movetagup")); //$NON-NLS-1$
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

    MoveTagDownAction() {
      putValue(SHORT_DESCRIPTION, BUNDLE.getString("movie.edit.movetagdown")); //$NON-NLS-1$
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

  private class AbortAction extends AbstractAction {
    private static final long serialVersionUID = -7652218354710642510L;

    AbortAction() {
      putValue(NAME, BUNDLE.getString("Button.abortqueue")); //$NON-NLS-1$
      putValue(SHORT_DESCRIPTION, BUNDLE.getString("tvshow.edit.abortqueue.desc")); //$NON-NLS-1$
      putValue(SMALL_ICON, IconManager.STOP_INV);
      putValue(LARGE_ICON_KEY, IconManager.STOP_INV);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      continueQueue = false;
      setVisible(false);
    }
  }

  private class EpisodeEditorContainer extends AbstractModelObject {
    TvShowEpisode tvShowEpisode;
    int           season;
    int           episode;
    boolean       dvdOrder = false;

    public String getEpisodeTitle() {
      return tvShowEpisode.getTitle();
    }

    public String getMediaFilename() {
      List<MediaFile> mfs = tvShowEpisode.getMediaFiles(MediaFileType.VIDEO);
      if (mfs != null && mfs.size() > 0) {
        return mfs.get(0).getFile().toString();
      }
      else {
        return "";
      }
    }

    public int getEpisode() {
      return episode;
    }

    public void setEpisode(int newValue) {
      int oldValue = this.episode;
      this.episode = newValue;
      firePropertyChange("episode", oldValue, newValue);
    }

    public int getSeason() {
      return season;
    }

    public void setSeason(int newValue) {
      int oldValue = this.season;
      this.season = newValue;
      firePropertyChange("season", oldValue, newValue);
    }

    public boolean isDvdOrder() {
      return this.dvdOrder;
    }

    public void setDvdOrder(boolean newValue) {
      boolean oldValue = this.dvdOrder;
      this.dvdOrder = newValue;
      firePropertyChange("dvdOrder", oldValue, newValue);
    }
  }

  protected void initDataBindings() {
    JListBinding<MediaGenres, List<MediaGenres>, JList> jListBinding = SwingBindings.createJListBinding(UpdateStrategy.READ, genres, listGenres);
    bindings.add(jListBinding);
    jListBinding.bind();
    //
    JListBinding<String, List<String>, JList> jListBinding_1 = SwingBindings.createJListBinding(UpdateStrategy.READ, tags, listTags);
    bindings.add(jListBinding_1);
    jListBinding_1.bind();
  }

  @Override
  public void dispose() {
    super.dispose();

    dpPremiered.cleanup();
  }

  private class CloneEpisodeAction extends AbstractAction {
    private static final long serialVersionUID = -3255090541823134232L;

    CloneEpisodeAction() {
      putValue(SHORT_DESCRIPTION, BUNDLE.getString("tvshowepisode.clone")); //$NON-NLS-1$
      putValue(SMALL_ICON, IconManager.COPY_INV);
      putValue(LARGE_ICON_KEY, IconManager.COPY_INV);
    }

    @Override
    public void actionPerformed(ActionEvent arg0) {
      int row = tableEpisodes.getSelectedRow();
      if (row > -1) {
        row = tableEpisodes.convertRowIndexToModel(row);
        EpisodeEditorContainer origContainer = episodes.get(row);
        EpisodeEditorContainer newContainer = new EpisodeEditorContainer();
        newContainer.tvShowEpisode = new TvShowEpisode(origContainer.tvShowEpisode);
        newContainer.tvShowEpisode.setTitle(origContainer.tvShowEpisode.getTitle() + " (clone)");
        newContainer.episode = -1;
        newContainer.season = newContainer.tvShowEpisode.getSeason();
        episodes.add(row + 1, newContainer);
      }
    }
  }

  private class RemoveEpisodeAction extends AbstractAction {
    private static final long serialVersionUID = -8233854057648972649L;

    RemoveEpisodeAction() {
      putValue(SHORT_DESCRIPTION, BUNDLE.getString("tvshowepisode.remove")); //$NON-NLS-1$
      putValue(SMALL_ICON, IconManager.REMOVE_INV);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      int row = tableEpisodes.getSelectedRow();
      if (row > -1) {
        row = tableEpisodes.convertRowIndexToModel(row);
        episodes.remove(row);
      }
    }
  }

  private static class EpisodeTableFormat extends TmmTableFormat<EpisodeEditorContainer> implements WritableTableFormat<EpisodeEditorContainer> {
    EpisodeTableFormat() {
      /*
       * title
       */
      Column col = new Column(BUNDLE.getString("metatag.title"), "name", EpisodeEditorContainer::getEpisodeTitle, String.class);
      col.setColumnResizeable(true);
      addColumn(col);

      /*
       * MF name
       */
      col = new Column(BUNDLE.getString("metatag.filename"), "filename", EpisodeEditorContainer::getMediaFilename, String.class);
      col.setColumnResizeable(true);
      addColumn(col);

      /*
       * season
       */
      col = new Column(BUNDLE.getString("metatag.season"), "season", EpisodeEditorContainer::getSeason, Integer.class);
      col.setColumnResizeable(false);
      addColumn(col);

      /*
       * episode
       */
      col = new Column(BUNDLE.getString("metatag.episode"), "episode", EpisodeEditorContainer::getEpisode, Integer.class);
      col.setColumnResizeable(false);
      addColumn(col);

      /*
       * DVD order
       */
      col = new Column(BUNDLE.getString("metatag.dvdorder"), "name", EpisodeEditorContainer::isDvdOrder, Boolean.class);
      col.setColumnResizeable(false);
      addColumn(col);
    }

    @Override
    public boolean isEditable(EpisodeEditorContainer episodeEditorContainer, int i) {
      switch (i) {
        case 2:
        case 3:
        case 4:
          return true;

        default:
          return false;
      }
    }

    @Override
    public EpisodeEditorContainer setColumnValue(EpisodeEditorContainer episodeEditorContainer, Object o, int i) {
      switch (i) {
        case 2:
          episodeEditorContainer.setSeason((Integer) o);
          break;

        case 3:
          episodeEditorContainer.setEpisode((Integer) o);
          break;

        case 4:
          episodeEditorContainer.setDvdOrder((Boolean) o);
          break;

        default:
          break;
      }
      return episodeEditorContainer;
    }
  }
}
