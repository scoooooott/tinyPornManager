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
package org.tinymediamanager.ui.tvshows.dialogs;

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
import java.nio.file.Path;
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
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JLayer;
import javax.swing.JList;
import javax.swing.JOptionPane;
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
import javax.swing.SwingWorker;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.apache.commons.lang3.StringUtils;
import org.jdesktop.beansbinding.AutoBinding.UpdateStrategy;
import org.jdesktop.beansbinding.BindingGroup;
import org.jdesktop.observablecollections.ObservableCollections;
import org.jdesktop.swingbinding.JListBinding;
import org.jdesktop.swingbinding.SwingBindings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.core.MediaFileType;
import org.tinymediamanager.core.MediaSource;
import org.tinymediamanager.core.Message;
import org.tinymediamanager.core.Message.MessageLevel;
import org.tinymediamanager.core.MessageManager;
import org.tinymediamanager.core.TmmProperties;
import org.tinymediamanager.core.Utils;
import org.tinymediamanager.core.entities.MediaFile;
import org.tinymediamanager.core.entities.MediaRating;
import org.tinymediamanager.core.entities.Person;
import org.tinymediamanager.core.tvshow.TvShowEpisodeSearchAndScrapeOptions;
import org.tinymediamanager.core.tvshow.TvShowList;
import org.tinymediamanager.core.tvshow.TvShowModuleManager;
import org.tinymediamanager.core.tvshow.entities.TvShowEpisode;
import org.tinymediamanager.scraper.MediaMetadata;
import org.tinymediamanager.scraper.MediaScraper;
import org.tinymediamanager.scraper.entities.MediaArtwork;
import org.tinymediamanager.scraper.entities.MediaArtwork.MediaArtworkType;
import org.tinymediamanager.scraper.exceptions.MissingIdException;
import org.tinymediamanager.scraper.exceptions.NothingFoundException;
import org.tinymediamanager.scraper.exceptions.ScrapeException;
import org.tinymediamanager.scraper.interfaces.ITvShowMetadataProvider;
import org.tinymediamanager.ui.IconManager;
import org.tinymediamanager.ui.ShadowLayerUI;
import org.tinymediamanager.ui.TmmUIHelper;
import org.tinymediamanager.ui.UIConstants;
import org.tinymediamanager.ui.components.FlatButton;
import org.tinymediamanager.ui.components.ImageLabel;
import org.tinymediamanager.ui.components.LinkLabel;
import org.tinymediamanager.ui.components.MainTabbedPane;
import org.tinymediamanager.ui.components.MediaRatingTable;
import org.tinymediamanager.ui.components.PersonTable;
import org.tinymediamanager.ui.components.TmmLabel;
import org.tinymediamanager.ui.components.combobox.AutocompleteComboBox;
import org.tinymediamanager.ui.components.combobox.MediaScraperComboBox;
import org.tinymediamanager.ui.components.datepicker.DatePicker;
import org.tinymediamanager.ui.components.table.TmmTable;
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
 * The Class TvShowEpisodeScrapeDialog.
 * 
 * @author Manuel Laggner
 */
public class TvShowEpisodeEditorDialog extends TmmDialog {
  private static final long                       serialVersionUID    = 7702248909791283043L;
  private static final Logger                     LOGGER              = LoggerFactory.getLogger(TvShowEpisodeEditorDialog.class);
  private static final Insets                     BUTTON_MARGIN       = UIConstants.SMALL_BUTTON_MARGIN;
  private static final String                     ORIGINAL_IMAGE_SIZE = "originalImageSize";
  private static final String                     SPACER              = "        ";
  private static final String                     DIALOG_ID           = "tvShowEpisodeEditor";

  private TvShowList                              tvShowList          = TvShowList.getInstance();
  private TvShowEpisode                           episodeToEdit;
  private List<String>                            tags                = ObservableCollections.observableList(new ArrayList<>());
  private List<MediaFile>                         mediaFiles          = new ArrayList<>();
  private boolean                                 continueQueue       = true;
  private boolean                                 navigateBack        = false;
  private int                                     queueIndex;
  private int                                     queueSize;

  private EventList<MediaRatingTable.MediaRating> ratings;
  private EventList<Person>                       guests;
  private EventList<Person>                       directors;
  private EventList<Person>                       writers;

  private JTextField                              tfTitle;
  private JSpinner                                spEpisode;
  private JSpinner                                spSeason;
  private JSpinner                                spRating;
  private JSpinner                                spDvdSeason;
  private JSpinner                                spDvdEpisode;
  private JCheckBox                               cbDvdOrder;
  private JSpinner                                spDisplaySeason;
  private JSpinner                                spDisplayEpisode;
  private DatePicker                              dpFirstAired;
  private JSpinner                                spDateAdded;
  private JCheckBox                               chckbxWatched;
  private ImageLabel                              lblThumb;
  private JTextArea                               taPlot;
  private AutocompleteComboBox<String>            cbTags;
  private AutoCompleteSupport<String>             cbTagsAutoCompleteSupport;
  private JList<String>                           listTags;
  private AutocompleteComboBox<MediaSource>       cbMediaSource;
  private MediaFileEditorPanel                    mediaFilesPanel;
  private MediaScraperComboBox                    cbScraper;

  private TmmTable                                tableRatings;
  private TmmTable                                tableGuests;
  private TmmTable                                tableDirectors;
  private TmmTable                                tableWriters;
  private JTextField                              tfOriginalTitle;
  private JTextField                              tfThumb;
  private JTextField                              tfNote;

  /**
   * Instantiates a new TV show episode scrape dialog.
   * 
   * @param episode
   *          the episode
   * @param queueIndex
   *          the actual index in the queue
   * @param queueSize
   *          the queue size
   */
  public TvShowEpisodeEditorDialog(TvShowEpisode episode, int queueIndex, int queueSize) {
    super(BUNDLE.getString("tvshowepisode.edit") + "  < " + episode.getMainVideoFile().getFilename() + " >", DIALOG_ID);

    // creation of lists
    guests = new ObservableElementList<>(GlazedLists.threadSafeList(new BasicEventList<>()), GlazedLists.beanConnector(Person.class));
    directors = new ObservableElementList<>(GlazedLists.threadSafeList(new BasicEventList<>()), GlazedLists.beanConnector(Person.class));
    writers = new ObservableElementList<>(GlazedLists.threadSafeList(new BasicEventList<>()), GlazedLists.beanConnector(Person.class));

    for (MediaFile mf : episode.getMediaFiles()) {
      mediaFiles.add(new MediaFile(mf));
    }

    this.episodeToEdit = episode;
    this.queueIndex = queueIndex;
    this.queueSize = queueSize;
    this.ratings = GlazedLists.threadSafeList(MediaRatingTable.convertRatingMapToEventList(episode.getRatings(), false));
    MediaRating userMediaRating = episodeToEdit.getRating(MediaRating.USER);

    initComponents();
    bindingGroup = initDataBindings();

    // fill data
    {
      tfTitle.setText(episodeToEdit.getTitle());
      tfOriginalTitle.setText(episodeToEdit.getOriginalTitle());
      cbDvdOrder.setSelected(episodeToEdit.isDvdOrder());
      spSeason.setModel(new SpinnerNumberModel(episodeToEdit.getAiredSeason(), -1, 9999, 1));
      spEpisode.setModel(new SpinnerNumberModel(episodeToEdit.getAiredEpisode(), -1, 9999, 1));
      spDvdSeason.setModel(new SpinnerNumberModel(episodeToEdit.getDvdSeason(), -1, 9999, 1));
      spDvdEpisode.setModel(new SpinnerNumberModel(episodeToEdit.getDvdEpisode(), -1, 9999, 1));
      spDisplaySeason.setModel(new SpinnerNumberModel(episodeToEdit.getDisplaySeason(), -1, 9999, 1));
      spDisplayEpisode.setModel(new SpinnerNumberModel(episodeToEdit.getDisplayEpisode(), -1, 9999, 1));
      spDateAdded.setValue(episodeToEdit.getDateAdded());
      spRating.setModel(new SpinnerNumberModel(userMediaRating.getRating(), 0.0, 10.0, 0.1));

      lblThumb.setImagePath(episodeToEdit.getArtworkFilename(MediaFileType.THUMB));
      tfThumb.setText(episodeToEdit.getArtworkUrl(MediaFileType.THUMB));
      chckbxWatched.setSelected(episodeToEdit.isWatched());
      taPlot.setText(episodeToEdit.getPlot());
      taPlot.setCaretPosition(0);
      cbMediaSource.setSelectedItem(episodeToEdit.getMediaSource());
      tfNote.setText(episodeToEdit.getNote());

      for (Person origCast : episodeToEdit.getGuests()) {
        guests.add(new Person(origCast));
      }
      for (Person director : episodeToEdit.getDirectors()) {
        directors.add(new Person(director));
      }
      for (Person writer : episodeToEdit.getWriters()) {
        writers.add(new Person(writer));
      }

      tags.addAll(episodeToEdit.getTags());
    }
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
      JPanel detailsPanel = new JPanel();
      tabbedPane.addTab(BUNDLE.getString("metatag.details"), detailsPanel);
      detailsPanel.setLayout(new MigLayout("", "[][20lp:75lp,grow][50lp:75lp][][60lp:75lp][50lp:75lp][20lp:n][][25lp:n][200lp:250lp,grow]",
          "[][][][][][][100lp:125lp,grow][][][][100lp][pref:pref:pref][]"));

      {
        JLabel lblTitle = new TmmLabel(BUNDLE.getString("metatag.title"));
        detailsPanel.add(lblTitle, "cell 0 0,alignx right");

        tfTitle = new JTextField();
        detailsPanel.add(tfTitle, "flowx,cell 1 0 7 1,growx");

        final JButton btnPlay = new JButton(IconManager.PLAY_INV);
        btnPlay.setFocusable(false);
        btnPlay.addActionListener(e -> {
          MediaFile mf = episodeToEdit.getMainVideoFile();
          try {
            TmmUIHelper.openFile(mf.getFileAsPath());
          }
          catch (Exception ex) {
            LOGGER.error("open file - {}", e);
            MessageManager.instance
                .pushMessage(new Message(MessageLevel.ERROR, mf, "message.erroropenfile", new String[] { ":", ex.getLocalizedMessage() }));
          }
        });
        detailsPanel.add(btnPlay, "cell 1 0 7 1");
      }
      {
        JLabel lblOriginalTitleT = new TmmLabel(BUNDLE.getString("metatag.originaltitle"));
        detailsPanel.add(lblOriginalTitleT, "cell 0 1,alignx trailing");

        tfOriginalTitle = new JTextField();
        detailsPanel.add(tfOriginalTitle, "cell 1 1 7 1,growx");
      }
      {
        JLabel lblSeason = new TmmLabel(BUNDLE.getString("metatag.season"));
        detailsPanel.add(lblSeason, "cell 0 2,alignx right");

        spSeason = new JSpinner();
        detailsPanel.add(spSeason, "cell 1 2,growx");

        JLabel lblEpisode = new TmmLabel(BUNDLE.getString("metatag.episode"));
        detailsPanel.add(lblEpisode, "cell 3 2,alignx right");

        spEpisode = new JSpinner();
        detailsPanel.add(spEpisode, "cell 4 2,growx");
      }
      {
        JLabel lblFirstAired = new TmmLabel(BUNDLE.getString("metatag.aired"));
        detailsPanel.add(lblFirstAired, "cell 0 3,alignx right");
      }

      dpFirstAired = new DatePicker(episodeToEdit.getFirstAired());
      detailsPanel.add(dpFirstAired, "cell 1 3 2 1,growx");
      {
        JLabel lblDvdSeason = new TmmLabel(BUNDLE.getString("metatag.dvdseason"));
        detailsPanel.add(lblDvdSeason, "cell 0 4,alignx right,aligny center");

        spDvdSeason = new JSpinner();
        detailsPanel.add(spDvdSeason, "cell 1 4,growx");

        JLabel lblDvdEpisode = new TmmLabel(BUNDLE.getString("metatag.dvdepisode"));
        detailsPanel.add(lblDvdEpisode, "cell 3 4,alignx right");

        spDvdEpisode = new JSpinner();
        detailsPanel.add(spDvdEpisode, "cell 4 4,growx");

        JLabel lblDvdOrder = new TmmLabel(BUNDLE.getString("metatag.dvdorder"));
        detailsPanel.add(lblDvdOrder, "flowx,cell 6 4 2 1");

        cbDvdOrder = new JCheckBox("");
        detailsPanel.add(cbDvdOrder, "cell 6 4 2 1");
      }
      {
        JLabel lblDisplaySeason = new TmmLabel(BUNDLE.getString("metatag.displayseason"));
        detailsPanel.add(lblDisplaySeason, "cell 0 5,alignx right");

        spDisplaySeason = new JSpinner();
        detailsPanel.add(spDisplaySeason, "cell 1 5,growx");

        JLabel lblDisplayEpisode = new TmmLabel(BUNDLE.getString("metatag.displayepisode"));
        detailsPanel.add(lblDisplayEpisode, "cell 3 5,alignx right");

        spDisplayEpisode = new JSpinner();
        detailsPanel.add(spDisplayEpisode, "cell 4 5,growx");
      }
      {
        JLabel lblPlot = new TmmLabel(BUNDLE.getString("metatag.plot"));
        detailsPanel.add(lblPlot, "cell 0 6,alignx right,aligny top");

        JScrollPane scrollPane = new JScrollPane();
        detailsPanel.add(scrollPane, "cell 1 6 7 1,grow");

        taPlot = new JTextArea();
        taPlot.setLineWrap(true);
        taPlot.setWrapStyleWord(true);
        scrollPane.setViewportView(taPlot);
      }
      {
        detailsPanel.add(new TmmLabel(BUNDLE.getString("mediafiletype.thumb")), "cell 9 0");

        LinkLabel lblThumbSize = new LinkLabel();
        detailsPanel.add(lblThumbSize, "cell 9 0");

        JButton btnDeleteThumb = new FlatButton(SPACER, IconManager.DELETE_GRAY);
        btnDeleteThumb.setToolTipText(BUNDLE.getString("Button.deleteartwork.desc"));
        btnDeleteThumb.addActionListener(e -> {
          lblThumb.clearImage();
          tfThumb.setText("");
        });
        detailsPanel.add(btnDeleteThumb, "cell 9 0");

        lblThumb = new ImageLabel();
        lblThumb.addMouseListener(new MouseAdapter() {
          @Override
          public void mouseClicked(MouseEvent e) {
            String path = TmmProperties.getInstance().getProperty(DIALOG_ID + ".path");
            Path file = TmmUIHelper.selectFile(BUNDLE.getString("image.choose"), path,
                new FileNameExtensionFilter("Image files", ".jpg", ".jpeg", ".png", ".bmp", ".gif", ".tbn"));
            if (file != null && Utils.isRegularFile(file)) {
              String fileName = file.toAbsolutePath().toString();
              TmmProperties.getInstance().putProperty(DIALOG_ID + ".path", file.getParent().toString());
              lblThumb.setImageUrl("file:/" + fileName);
              tfThumb.setText(lblThumb.getImageUrl());
            }
          }
        });
        lblThumb.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        detailsPanel.add(lblThumb, "cell 9 1 1 6,grow");
        lblThumb.addPropertyChangeListener(ORIGINAL_IMAGE_SIZE, e -> setImageSizeAndCreateLink(lblThumbSize, lblThumb, MediaFileType.THUMB));
      }
      {
        JLabel lblRating = new TmmLabel(BUNDLE.getString("metatag.userrating"));
        detailsPanel.add(lblRating, "cell 0 9,alignx right");

        spRating = new JSpinner();
        detailsPanel.add(spRating, "cell 1 9,growx");
      }
      {
        JLabel lblRatingsT = new TmmLabel(BUNDLE.getString("metatag.ratings"));
        detailsPanel.add(lblRatingsT, "flowy,cell 0 10,alignx right,aligny top");

        JScrollPane scrollPaneRatings = new JScrollPane();
        detailsPanel.add(scrollPaneRatings, "cell 1 10 5 2,grow");

        tableRatings = new MediaRatingTable(ratings);
        tableRatings.configureScrollPane(scrollPaneRatings);
        scrollPaneRatings.setViewportView(tableRatings);

        JButton btnAddRating = new JButton(new AddRatingAction());
        btnAddRating.setMargin(BUTTON_MARGIN);
        detailsPanel.add(btnAddRating, "cell 0 10,alignx right,aligny top");

        JButton btnRemoveRating = new JButton(new RemoveRatingAction());
        btnRemoveRating.setMargin(BUTTON_MARGIN);
        detailsPanel.add(btnRemoveRating, "cell 0 10,alignx right,aligny top");
      }
      {
        JLabel lblNoteT = new TmmLabel(BUNDLE.getString("metatag.note"));
        detailsPanel.add(lblNoteT, "cell 0 12,alignx trailing");

        tfNote = new JTextField();
        detailsPanel.add(tfNote, "cell 1 12 7 1,growx");
      }
    }

    /**********************************************************************************
     * Detail 2 panel
     **********************************************************************************/
    {
      JPanel details2Panel = new JPanel();
      tabbedPane.addTab(BUNDLE.getString("metatag.details2"), details2Panel);
      details2Panel.setLayout(new MigLayout("", "[][][100lp:150lp][20lp:50lp,grow]", "[][][][20lp:n][][20lp:n][100lp:150lp,grow][][grow 200]"));
      {
        JLabel lblDateAdded = new TmmLabel(BUNDLE.getString("metatag.dateadded"));
        details2Panel.add(lblDateAdded, "cell 0 0,alignx right");

        spDateAdded = new JSpinner(new SpinnerDateModel());
        details2Panel.add(spDateAdded, "cell 1 0,growx");
      }
      {
        JLabel lblWatched = new TmmLabel(BUNDLE.getString("metatag.watched"));
        details2Panel.add(lblWatched, "cell 0 1,alignx right");

        chckbxWatched = new JCheckBox("");
        details2Panel.add(chckbxWatched, "cell 1 1");
      }
      {
        JLabel lblMediasource = new TmmLabel(BUNDLE.getString("metatag.source"));
        details2Panel.add(lblMediasource, "cell 0 2,alignx right");

        cbMediaSource = new AutocompleteComboBox(MediaSource.values());
        details2Panel.add(cbMediaSource, "cell 1 2,growx");
      }

      {
        JLabel lblThumbT = new TmmLabel(BUNDLE.getString("mediafiletype.thumb"));
        details2Panel.add(lblThumbT, "cell 0 4,alignx right");

        tfThumb = new JTextField();
        details2Panel.add(tfThumb, "cell 1 4 3 1,growx");
        tfThumb.setColumns(10);
      }
      {
        JLabel lblTags = new TmmLabel(BUNDLE.getString("metatag.tags"));
        details2Panel.add(lblTags, "flowy,cell 0 6,alignx right,aligny top");

        JScrollPane scrollPaneTags = new JScrollPane();
        details2Panel.add(scrollPaneTags, "cell 1 6 2 1,grow");

        listTags = new JList();
        scrollPaneTags.setViewportView(listTags);

        JButton btnAddTag = new JButton(new AddTagAction());
        btnAddTag.setMargin(BUTTON_MARGIN);
        details2Panel.add(btnAddTag, "cell 0 6 1 3,alignx right,aligny top");

        JButton btnRemoveTag = new JButton(new RemoveTagAction());
        btnRemoveTag.setMargin(BUTTON_MARGIN);
        details2Panel.add(btnRemoveTag, "cell 0 6 1 3,alignx right,aligny top");

        JButton btnMoveTagUp = new JButton(new MoveTagUpAction());
        btnMoveTagUp.setMargin(BUTTON_MARGIN);
        details2Panel.add(btnMoveTagUp, "cell 0 6 1 3,alignx right,aligny top");

        JButton btnMoveTagDown = new JButton(new MoveTagDownAction());
        btnMoveTagDown.setMargin(BUTTON_MARGIN);
        details2Panel.add(btnMoveTagDown, "cell 0 6 1 3,alignx right,aligny top");

        cbTags = new AutocompleteComboBox<>(tvShowList.getTagsInEpisodes());
        cbTags.setEditable(true);
        cbTagsAutoCompleteSupport = cbTags.getAutoCompleteSupport();

        InputMap im = cbTags.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        Object enterAction = im.get(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0));
        cbTags.getActionMap().put(enterAction, new AddTagAction());
        details2Panel.add(cbTags, "cell 1 7 2 1,growx");

      }
    }

    /**********************************************************************************
     * CrewPanel
     **********************************************************************************/
    {
      JPanel crewPanel = new JPanel();
      tabbedPane.addTab(BUNDLE.getString("movie.edit.castandcrew"), null, crewPanel, null);
      crewPanel.setLayout(new MigLayout("", "[][150lp:300lp,grow][20lp:n][][150lp:300lp,grow]", "[100lp:250lp][20lp:n][100lp:200lp]"));
      {
        JLabel lblGuests = new TmmLabel(BUNDLE.getString("metatag.guests"));
        crewPanel.add(lblGuests, "flowy,cell 0 0,alignx right,aligny top");

        JScrollPane scrollPane = new JScrollPane();
        crewPanel.add(scrollPane, "cell 1 0,grow");

        tableGuests = new PersonTable(guests, true);
        tableGuests.configureScrollPane(scrollPane);
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
        JButton btnAddGuest = new JButton(new AddGuestAction());
        btnAddGuest.setMargin(BUTTON_MARGIN);
        crewPanel.add(btnAddGuest, "cell 0 0,alignx right");
      }
      {
        JButton btnRemoveGuest = new JButton(new RemoveGuestAction());
        btnRemoveGuest.setMargin(BUTTON_MARGIN);
        crewPanel.add(btnRemoveGuest, "cell 0 0,alignx right");
      }
      {
        JButton btnMoveGuestUp = new JButton(new MoveGuestUpAction());
        btnMoveGuestUp.setMargin(BUTTON_MARGIN);
        crewPanel.add(btnMoveGuestUp, "cell 0 0,alignx right");
      }
      {
        JButton btnMoveGuestDown = new JButton(new MoveGuestDownAction());
        btnMoveGuestDown.setMargin(BUTTON_MARGIN);
        crewPanel.add(btnMoveGuestDown, "cell 0 0,alignx right,aligny top");
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
     * Media Files panel
     *********************************************************************************/
    {
      mediaFilesPanel = new MediaFileEditorPanel(mediaFiles);
      tabbedPane.addTab(BUNDLE.getString("metatag.mediafiles"), null, mediaFilesPanel, null); // $NON-NLS-1$
    }

    /**********************************************************************************
     * bottom panel
     *********************************************************************************/
    {
      JPanel scrapePanel = new JPanel();
      scrapePanel.setOpaque(false);

      cbScraper = new MediaScraperComboBox(tvShowList.getAvailableMediaScrapers());
      MediaScraper defaultScraper = tvShowList.getDefaultMediaScraper();
      scrapePanel.setLayout(new MigLayout("", "[][][][grow]", "[]"));
      cbScraper.setSelectedItem(defaultScraper);
      scrapePanel.add(cbScraper, "cell 0 0");

      JButton btnScrape = new JButton(new ScrapeAction());
      scrapePanel.add(btnScrape, "cell 1 0");

      JButton btnSearch = new JButton(new SearchAction());
      scrapePanel.add(btnSearch, "cell 2 0");

      setBottomInformationPanel(scrapePanel);
    }
    {
      if (queueSize > 1) {
        JButton abortButton = new JButton(new AbortQueueAction());
        addButton(abortButton);
        if (queueIndex > 0) {
          JButton backButton = new JButton(new NavigateBackAction());
          addButton(backButton);
        }
      }

      JButton cancelButton = new JButton(new DiscardAction());
      addButton(cancelButton);

      JButton okButton = new JButton(new ChangeEpisodeAction());
      addDefaultButton(okButton);
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

  private class ScrapeAction extends AbstractAction {
    private static final long serialVersionUID = -4799506776650330500L;

    ScrapeAction() {
      putValue(NAME, BUNDLE.getString("Button.scrape"));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      MediaScraper scraper = (MediaScraper) cbScraper.getSelectedItem();
      ScrapeTask task = new ScrapeTask(scraper);
      task.execute();
    }
  }

  private class SearchAction extends AbstractAction {
    private static final long serialVersionUID = -4799506776650330500L;

    SearchAction() {
      putValue(NAME, BUNDLE.getString("tvshowepisodechooser.search"));
      putValue(SMALL_ICON, IconManager.SEARCH_INV);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      MediaScraper scraper = (MediaScraper) cbScraper.getSelectedItem();
      TvShowEpisodeChooserDialog dialog = new TvShowEpisodeChooserDialog(episodeToEdit, scraper);
      dialog.setLocationRelativeTo(TvShowEpisodeEditorDialog.this);
      dialog.setVisible(true);
      MediaMetadata metadata = dialog.getMetadata();
      if (metadata != null && metadata.getSeasonNumber() > -1 && metadata.getEpisodeNumber() > 0) {
        tfTitle.setText(metadata.getTitle());
        tfOriginalTitle.setText(metadata.getOriginalTitle());
        taPlot.setText(metadata.getPlot());
        spEpisode.setValue(metadata.getEpisodeNumber());
        spSeason.setValue(metadata.getSeasonNumber());
        spDvdEpisode.setValue(metadata.getDvdEpisodeNumber());
        spDvdSeason.setValue(metadata.getDvdSeasonNumber());

        guests.clear();
        writers.clear();
        directors.clear();

        // force copy constructors here
        for (Person member : metadata.getCastMembers()) {
          switch (member.getType()) {
            case ACTOR:
              guests.add(new Person(member));
              break;

            case DIRECTOR:
              directors.add(new Person(member));
              break;

            case WRITER:
              writers.add(new Person(member));
              break;

            default:
              break;
          }
        }

        MediaArtwork ma = metadata.getMediaArt(MediaArtworkType.THUMB).stream().findFirst().orElse(null);
        if (ma != null) {
          tfThumb.setText(ma.getDefaultUrl());
          lblThumb.setImageUrl(ma.getDefaultUrl());
        }
      }
    }
  }

  private class ChangeEpisodeAction extends AbstractAction {
    private static final long serialVersionUID = -4799506776650330500L;

    ChangeEpisodeAction() {
      putValue(NAME, BUNDLE.getString("Button.ok"));
      putValue(SHORT_DESCRIPTION, BUNDLE.getString("tvshow.change"));
      putValue(SMALL_ICON, IconManager.APPLY_INV);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      episodeToEdit.setTitle(tfTitle.getText());
      episodeToEdit.setOriginalTitle(tfOriginalTitle.getText());
      episodeToEdit.setDvdOrder(cbDvdOrder.isSelected());
      episodeToEdit.setAiredSeason((Integer) spSeason.getValue());
      episodeToEdit.setAiredEpisode((Integer) spEpisode.getValue());
      episodeToEdit.setDvdSeason((Integer) spDvdSeason.getValue());
      episodeToEdit.setDvdEpisode((Integer) spDvdEpisode.getValue());
      episodeToEdit.setDisplaySeason((Integer) spDisplaySeason.getValue());
      episodeToEdit.setDisplayEpisode((Integer) spDisplayEpisode.getValue());
      episodeToEdit.setPlot(taPlot.getText());
      episodeToEdit.setNote(tfNote.getText());

      Object mediaSource = cbMediaSource.getSelectedItem();
      if (mediaSource instanceof MediaSource) {
        episodeToEdit.setMediaSource((MediaSource) mediaSource);
      }
      else if (mediaSource instanceof String) {
        episodeToEdit.setMediaSource(MediaSource.getMediaSource((String) mediaSource));
      }
      else {
        episodeToEdit.setMediaSource(MediaSource.UNKNOWN);
      }

      // sync media files with the media file editor and fire the mediaFiles event
      MediaFileEditorPanel.syncMediaFiles(mediaFiles, episodeToEdit.getMediaFiles());
      episodeToEdit.fireEventForChangedMediaInformation();

      // user rating
      Map<String, MediaRating> ratings = new HashMap<>();

      if ((double) spRating.getValue() > 0) {
        ratings.put(MediaRating.USER, new MediaRating(MediaRating.USER, (double) spRating.getValue(), 1, 10));
      }

      // other ratings
      for (MediaRatingTable.MediaRating mediaRating : TvShowEpisodeEditorDialog.this.ratings) {
        if (StringUtils.isNotBlank(mediaRating.key) && mediaRating.value > 0 && mediaRating.votes > 0) {
          MediaRating rating = new MediaRating(mediaRating.key, mediaRating.value, mediaRating.votes, mediaRating.maxValue);
          ratings.put(mediaRating.key, rating);
        }
      }
      episodeToEdit.setRatings(ratings);

      episodeToEdit.setDateAdded((Date) spDateAdded.getValue());
      episodeToEdit.setFirstAired(dpFirstAired.getDate());

      episodeToEdit.setWatched(chckbxWatched.isSelected());
      episodeToEdit.setActors(guests);
      episodeToEdit.setDirectors(directors);
      episodeToEdit.setWriters(writers);

      // process artwork
      processArtwork(MediaFileType.THUMB, lblThumb, tfThumb);

      episodeToEdit.setTags(tags);
      episodeToEdit.writeNFO();
      episodeToEdit.saveToDb();

      setVisible(false);
    }
  }

  private void processArtwork(MediaFileType type, ImageLabel imageLabel, JTextField textField) {
    if (StringUtils.isAllBlank(imageLabel.getImagePath(), imageLabel.getImageUrl())
        && StringUtils.isNotBlank(episodeToEdit.getArtworkFilename(type))) {
      // artwork has been explicitly deleted
      episodeToEdit.deleteMediaFiles(type);
    }

    if (StringUtils.isNotEmpty(textField.getText()) && !textField.getText().equals(episodeToEdit.getArtworkUrl(type))) {
      // artwork url and textfield do not match -> redownload
      episodeToEdit.setArtworkUrl(textField.getText(), type);
      episodeToEdit.downloadArtwork(type);
    }
    else if (StringUtils.isEmpty(textField.getText())) {
      // remove the artwork url
      episodeToEdit.removeArtworkUrl(type);
    }
  }

  private class DiscardAction extends AbstractAction {
    private static final long serialVersionUID = -5581329896797961536L;

    DiscardAction() {
      putValue(NAME, BUNDLE.getString("Button.cancel"));
      putValue(SHORT_DESCRIPTION, BUNDLE.getString("edit.discard"));
      putValue(SMALL_ICON, IconManager.CANCEL_INV);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      setVisible(false);
    }
  }

  private class AbortQueueAction extends AbstractAction {
    private static final long serialVersionUID = -7652218354710642510L;

    AbortQueueAction() {
      putValue(NAME, BUNDLE.getString("Button.abortqueue"));
      putValue(SHORT_DESCRIPTION, BUNDLE.getString("tvshow.edit.abortqueue.desc"));
      putValue(SMALL_ICON, IconManager.STOP_INV);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      continueQueue = false;
      setVisible(false);
    }
  }

  private class ScrapeTask extends SwingWorker<Void, Void> {
    private MediaScraper  mediaScraper;
    private MediaMetadata metadata = null;

    ScrapeTask(MediaScraper mediaScraper) {
      this.mediaScraper = mediaScraper;
    }

    @Override
    protected Void doInBackground() {
      setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
      TvShowEpisodeSearchAndScrapeOptions options = new TvShowEpisodeSearchAndScrapeOptions();
      options.setLanguage(TvShowModuleManager.SETTINGS.getScraperLanguage());

      for (Entry<String, Object> entry : episodeToEdit.getTvShow().getIds().entrySet()) {
        options.setId(entry.getKey(), entry.getValue().toString());
      }

      options.setId(MediaMetadata.SEASON_NR, spSeason.getValue().toString());
      options.setId(MediaMetadata.EPISODE_NR, spEpisode.getValue().toString());

      options.setId(MediaMetadata.SEASON_NR_DVD, spDvdSeason.getValue().toString());
      options.setId(MediaMetadata.EPISODE_NR_DVD, spDvdEpisode.getValue().toString());
      try {
        LOGGER.info("=====================================================");
        LOGGER.info("Scraper metadata with scraper: {}", mediaScraper.getMediaProvider().getProviderInfo().getId());
        LOGGER.info(options.toString());
        LOGGER.info("=====================================================");
        metadata = ((ITvShowMetadataProvider) mediaScraper.getMediaProvider()).getMetadata(options);
      }
      catch (ScrapeException e) {
        LOGGER.error("getMetadata", e);
        MessageManager.instance.pushMessage(new Message(Message.MessageLevel.ERROR, TvShowEpisodeEditorDialog.this.episodeToEdit,
            "message.scrape.metadataepisodefailed", new String[] { ":", e.getLocalizedMessage() }));
      }
      catch (MissingIdException e) {
        LOGGER.warn("missing id for scrape");
        MessageManager.instance
            .pushMessage(new Message(Message.MessageLevel.ERROR, TvShowEpisodeEditorDialog.this.episodeToEdit, "scraper.error.missingid"));
      }
      catch (NothingFoundException ignored) {
        LOGGER.debug("nothing found");
      }

      setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
      return null;
    }

    @Override
    protected void done() {
      super.done();

      // if nothing has been found -> open the search box
      if (metadata == null || StringUtils.isBlank(metadata.getTitle())) {
        // message
        JOptionPane.showMessageDialog(TvShowEpisodeEditorDialog.this, BUNDLE.getString("message.scrape.tvshowepisodefailed"));
      }
      else {
        tfTitle.setText(metadata.getTitle());
        tfOriginalTitle.setText(metadata.getOriginalTitle());
        taPlot.setText(metadata.getPlot());
        dpFirstAired.setDate(metadata.getReleaseDate());

        // set aired or dvd ep/season
        spSeason.setValue(metadata.getSeasonNumber());
        spEpisode.setValue(metadata.getEpisodeNumber());
        spDvdSeason.setValue(metadata.getDvdSeasonNumber());
        spDvdEpisode.setValue(metadata.getDvdEpisodeNumber());
        spDisplayEpisode.setValue(metadata.getDisplayEpisodeNumber());
        spDisplaySeason.setValue(metadata.getDisplaySeasonNumber());

        ratings.clear();
        ratings.addAll(MediaRatingTable.convertRatingMapToEventList(metadata.getRatings()));

        tags.clear();
        tags.addAll(metadata.getTags());

        // cast
        guests.clear();
        directors.clear();
        writers.clear();

        // force copy constructors here
        for (Person member : metadata.getCastMembers()) {
          switch (member.getType()) {
            case ACTOR:
              guests.add(new Person(member));
              break;

            case DIRECTOR:
              directors.add(new Person(member));
              break;

            case WRITER:
              writers.add(new Person(member));
              break;

            default:
              break;
          }
        }

        // artwork
        MediaArtwork ma = metadata.getMediaArt(MediaArtworkType.THUMB).stream().findFirst().orElse(null);
        if (ma != null) {
          lblThumb.setImageUrl(ma.getDefaultUrl());
          tfThumb.setText(ma.getDefaultUrl());
        }
      }
    }
  }

  protected BindingGroup initDataBindings() {
    JListBinding<String, List<String>, JList> jListBinding = SwingBindings.createJListBinding(UpdateStrategy.READ, tags, listTags);
    jListBinding.bind();
    //
    BindingGroup bindingGroup = new BindingGroup();
    //
    bindingGroup.addBinding(jListBinding);
    //
    return bindingGroup;
  }

  @Override
  public void dispose() {
    super.dispose();

    mediaFilesPanel.unbindBindings();
    dpFirstAired.cleanup();
  }

  private void setImageSizeAndCreateLink(LinkLabel lblSize, ImageLabel imageLabel, MediaFileType type) {
    createLinkForImage(lblSize, imageLabel);

    // image has been deleted
    if (imageLabel.getOriginalImageSize().width == 0 && imageLabel.getOriginalImageSize().height == 0) {
      lblSize.setText("");
      return;
    }

    Dimension dimension = episodeToEdit.getArtworkDimension(type);
    if (dimension.width == 0 && dimension.height == 0) {
      lblSize.setText(imageLabel.getOriginalImageSize().width + "x" + imageLabel.getOriginalImageSize().height);
    }
    else {
      lblSize.setText(dimension.width + "x" + dimension.height);
    }
  }

  private class AddRatingAction extends AbstractAction {
    private static final long serialVersionUID = 2903255414533349267L;

    AddRatingAction() {
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
        ratings.add(mediaRating);
      }
    }
  }

  private class RemoveRatingAction extends AbstractAction {
    private static final long serialVersionUID = -7079821950827356996L;

    RemoveRatingAction() {
      putValue(SHORT_DESCRIPTION, BUNDLE.getString("rating.remove"));
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

  private class AddTagAction extends AbstractAction {
    private static final long serialVersionUID = 5968029647764173330L;

    AddTagAction() {
      putValue(SHORT_DESCRIPTION, BUNDLE.getString("tag.add"));
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

  private class RemoveTagAction extends AbstractAction {
    private static final long serialVersionUID = -4799506776650330500L;

    RemoveTagAction() {
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

  private class MoveTagUpAction extends AbstractAction {
    private static final long serialVersionUID = -6855661707692602266L;

    MoveTagUpAction() {
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

    MoveTagDownAction() {
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

  private class AddGuestAction extends AbstractAction {
    private static final long serialVersionUID = -5879601617842300526L;

    AddGuestAction() {
      putValue(SHORT_DESCRIPTION, BUNDLE.getString("cast.guest.add"));
      putValue(SMALL_ICON, IconManager.ADD_INV);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      Person actor = new Person(Person.Type.ACTOR, BUNDLE.getString("cast.actor.unknown"), BUNDLE.getString("cast.role.unknown"));
      PersonEditorDialog dialog = new PersonEditorDialog(SwingUtilities.getWindowAncestor(tableGuests), BUNDLE.getString("cast.guest.add"), actor);
      dialog.setVisible(true);

      if (StringUtils.isNotBlank(actor.getName()) && !actor.getName().equals(BUNDLE.getString("cast.actor.unknown"))) {
        guests.add(0, actor);
      }
    }
  }

  private class RemoveGuestAction extends AbstractAction {
    private static final long serialVersionUID = 6970920169867315771L;

    RemoveGuestAction() {
      putValue(SHORT_DESCRIPTION, BUNDLE.getString("cast.guest.remove"));
      putValue(SMALL_ICON, IconManager.REMOVE_INV);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      int row = tableGuests.getSelectedRow();
      if (row > -1) {
        row = tableGuests.convertRowIndexToModel(row);
        guests.remove(row);
      }
    }
  }

  private class MoveGuestUpAction extends AbstractAction {
    private static final long serialVersionUID = 5775423424097844658L;

    MoveGuestUpAction() {
      putValue(SHORT_DESCRIPTION, BUNDLE.getString("cast.guest.moveup"));
      putValue(SMALL_ICON, IconManager.ARROW_UP_INV);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      int row = tableGuests.getSelectedRow();
      if (row > 0) {
        Collections.rotate(guests.subList(row - 1, row + 1), 1);
        tableGuests.getSelectionModel().setSelectionInterval(row - 1, row - 1);
      }
    }
  }

  private class MoveGuestDownAction extends AbstractAction {
    private static final long serialVersionUID = -6564146895819191932L;

    MoveGuestDownAction() {
      putValue(SHORT_DESCRIPTION, BUNDLE.getString("cast.guest.movedown"));
      putValue(SMALL_ICON, IconManager.ARROW_DOWN_INV);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      int row = tableGuests.getSelectedRow();
      if (row < guests.size() - 1) {
        Collections.rotate(guests.subList(row, row + 2), -1);
        tableGuests.getSelectionModel().setSelectionInterval(row + 1, row + 1);
      }
    }
  }

  private class AddDirectorAction extends AbstractAction {
    private static final long serialVersionUID = -8929331442958057771L;

    AddDirectorAction() {
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

    RemoveDirectorAction() {
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

    MoveDirectorUpAction() {
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

    MoveDirectorDownAction() {
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

    AddWriterAction() {
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

    RemoveWriterAction() {
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

    MoveWriterUpAction() {
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

    MoveWriterDownAction() {
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

  public boolean isContinueQueue() {
    return continueQueue;
  }

  public boolean isNavigateBack() {
    return navigateBack;
  }
}
