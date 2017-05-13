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
import static org.tinymediamanager.core.entities.Person.Type.DIRECTOR;
import static org.tinymediamanager.core.entities.Person.Type.WRITER;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.nio.file.Path;
import java.util.ArrayList;
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

import org.apache.commons.lang3.LocaleUtils;
import org.apache.commons.lang3.StringUtils;
import org.jdesktop.beansbinding.AutoBinding.UpdateStrategy;
import org.jdesktop.observablecollections.ObservableCollections;
import org.jdesktop.swingbinding.JListBinding;
import org.jdesktop.swingbinding.SwingBindings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.core.MediaFileType;
import org.tinymediamanager.core.MediaSource;
import org.tinymediamanager.core.TmmProperties;
import org.tinymediamanager.core.Utils;
import org.tinymediamanager.core.entities.MediaFile;
import org.tinymediamanager.core.entities.Person;
import org.tinymediamanager.core.tvshow.TvShowList;
import org.tinymediamanager.core.tvshow.TvShowModuleManager;
import org.tinymediamanager.core.tvshow.entities.TvShowEpisode;
import org.tinymediamanager.scraper.MediaMetadata;
import org.tinymediamanager.scraper.MediaScrapeOptions;
import org.tinymediamanager.scraper.MediaScraper;
import org.tinymediamanager.scraper.entities.MediaArtwork;
import org.tinymediamanager.scraper.entities.MediaArtwork.MediaArtworkType;
import org.tinymediamanager.scraper.entities.MediaCastMember;
import org.tinymediamanager.scraper.entities.MediaEpisode;
import org.tinymediamanager.scraper.entities.MediaType;
import org.tinymediamanager.scraper.mediaprovider.ITvShowMetadataProvider;
import org.tinymediamanager.ui.EqualsLayout;
import org.tinymediamanager.ui.IconManager;
import org.tinymediamanager.ui.TmmUIHelper;
import org.tinymediamanager.ui.UTF8Control;
import org.tinymediamanager.ui.components.ImageLabel;
import org.tinymediamanager.ui.components.MainTabbedPane;
import org.tinymediamanager.ui.components.PersonTable;
import org.tinymediamanager.ui.components.combobox.AutocompleteComboBox;
import org.tinymediamanager.ui.components.combobox.MediaScraperComboBox;
import org.tinymediamanager.ui.components.datepicker.DatePicker;
import org.tinymediamanager.ui.components.table.TmmTable;
import org.tinymediamanager.ui.dialogs.PersonEditorDialog;
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
  private static final long            serialVersionUID = 7702248909791283043L;
  /** @wbp.nls.resourceBundle messages */
  private static final ResourceBundle  BUNDLE           = ResourceBundle.getBundle("messages", new UTF8Control());      //$NON-NLS-1$
  private static final Logger          LOGGER           = LoggerFactory.getLogger(TvShowEpisodeEditorDialog.class);
  private static final Insets          BUTTON_MARGIN    = new Insets(2, 2, 2, 2);

  private static final String          DIALOG_ID        = "tvShowEpisodeEditor";

  private TvShowList                   tvShowList       = TvShowList.getInstance();
  private TvShowEpisode                episodeToEdit;
  private List<String>                 tags             = ObservableCollections.observableList(new ArrayList<String>());
  private List<MediaFile>              mediaFiles       = new ArrayList<>();
  private boolean                      continueQueue    = true;
  private int                          voteCount        = 0;
  private boolean                      inQueue;

  private EventList<Person>            guests;
  private EventList<Person>            directors;
  private EventList<Person>            writers;

  private JTextField                   tfTitle;
  private JSpinner                     spEpisode;
  private JSpinner                     spSeason;
  private JSpinner                     spRating;
  private JSpinner                     spDvdSeason;
  private JSpinner                     spDvdEpisode;
  private JCheckBox                    cbDvdOrder;
  private JSpinner                     spDisplaySeason;
  private JSpinner                     spDisplayEpisode;
  private DatePicker                   dpFirstAired;
  private JSpinner                     spDateAdded;
  private JCheckBox                    chckbxWatched;
  private ImageLabel                   lblThumb;
  private JTextArea                    taPlot;
  private AutocompleteComboBox<String> cbTags;
  private AutoCompleteSupport<String>  cbTagsAutoCompleteSupport;
  private JList<String>                listTags;
  private JComboBox<MediaSource>       cbMediaSource;
  private MediaFileEditorPanel         mediaFilesPanel;
  private MediaScraperComboBox         cbScraper;

  private TmmTable                     tableGuests;
  private TmmTable                     tableDirectors;
  private TmmTable                     tableWriters;

  /**
   * Instantiates a new TV show episode scrape dialog.
   * 
   * @param episode
   *          the episode
   * @param inQueue
   *          the in queue
   */
  public TvShowEpisodeEditorDialog(TvShowEpisode episode, boolean inQueue) {
    super(BUNDLE.getString("tvshow.edit") + "  < " + episode.getFirstVideoFile().getFilename() + " >", DIALOG_ID); //$NON-NLS-1$
    setBounds(5, 5, 964, 632);

    // creation of lists
    guests = new ObservableElementList<>(GlazedLists.threadSafeList(new BasicEventList<>()), GlazedLists.beanConnector(Person.class));
    directors = new ObservableElementList<>(GlazedLists.threadSafeList(new BasicEventList<>()), GlazedLists.beanConnector(Person.class));
    writers = new ObservableElementList<>(GlazedLists.threadSafeList(new BasicEventList<>()), GlazedLists.beanConnector(Person.class));

    for (MediaFile mf : episode.getMediaFiles()) {
      mediaFiles.add(new MediaFile(mf));
    }

    this.episodeToEdit = episode;
    this.inQueue = inQueue;

    initComponents();
    initDataBindings();

    // fill data
    {
      tfTitle.setText(episodeToEdit.getTitle());
      cbDvdOrder.setSelected(episodeToEdit.isDvdOrder());
      spSeason.setModel(new SpinnerNumberModel(episodeToEdit.getAiredSeason(), -1, 9999, 1));
      spEpisode.setModel(new SpinnerNumberModel(episodeToEdit.getAiredEpisode(), -1, 9999, 1));
      spDvdSeason.setModel(new SpinnerNumberModel(episodeToEdit.getDvdSeason(), -1, 9999, 1));
      spDvdEpisode.setModel(new SpinnerNumberModel(episodeToEdit.getDvdEpisode(), -1, 9999, 1));
      spDisplaySeason.setModel(new SpinnerNumberModel(episodeToEdit.getDisplaySeason(), -1, 9999, 1));
      spDisplayEpisode.setModel(new SpinnerNumberModel(episodeToEdit.getDisplayEpisode(), -1, 9999, 1));
      spDateAdded.setValue(episodeToEdit.getDateAdded());

      lblThumb.setImagePath(episodeToEdit.getArtworkFilename(MediaFileType.THUMB));
      spRating.setModel(new SpinnerNumberModel(episodeToEdit.getRating(), 0.0, 10.0, 0.1));
      spRating.addChangeListener(e -> voteCount = 1);
      voteCount = episodeToEdit.getVotes();
      chckbxWatched.setSelected(episodeToEdit.isWatched());
      taPlot.setText(episodeToEdit.getPlot());
      taPlot.setCaretPosition(0);
      // tfDirector.setText(episodeToEdit.getDirector());
      // tfWriter.setText(episodeToEdit.getWriter());
      cbMediaSource.setSelectedItem(episodeToEdit.getMediaSource());

      for (Person origCast : episodeToEdit.getGuests()) {
        Person actor = new Person(ACTOR, origCast.getName(), origCast.getRole());
        actor.setThumbUrl(origCast.getThumbUrl());
        guests.add(actor);
      }

      for (String tag : episodeToEdit.getTags()) {
        tags.add(tag);
      }
    }
  }

  private void initComponents() {
    getContentPane().setLayout(new BorderLayout());
    JPanel rootPanel = new JPanel();
    rootPanel.setLayout(new BorderLayout());
    rootPanel.putClientProperty("class", "rootPanel");
    getContentPane().add(rootPanel, BorderLayout.CENTER);

    JTabbedPane tabbedPane = new MainTabbedPane() {
      private static final long serialVersionUID = 71548865608767532L;

      @Override
      public void updateUI() {
        putClientProperty("bottomBorder", Boolean.FALSE);
        super.updateUI();
      }
    };
    rootPanel.add(tabbedPane, BorderLayout.CENTER);

    /**********************************************************************************
     * DetailsPanel 1
     **********************************************************************************/
    {
      JPanel detailsPanel = new JPanel();
      tabbedPane.addTab(BUNDLE.getString("metatag.details"), detailsPanel);
      detailsPanel.setLayout(new MigLayout("", "[][][50lp:75lp][][][50lp:75lp][][][25lp:n][200lp:250lp,grow]", "[][][][][][][][75p:150lp][][]"));

      {
        JLabel lblTitle = new JLabel(BUNDLE.getString("metatag.title")); //$NON-NLS-1$
        detailsPanel.add(lblTitle, "cell 0 0,alignx right");

        tfTitle = new JTextField();
        detailsPanel.add(tfTitle, "cell 1 0 9 1,growx");
      }
      {
        JLabel lblSeason = new JLabel(BUNDLE.getString("metatag.season")); //$NON-NLS-1$
        detailsPanel.add(lblSeason, "cell 0 1,alignx right");

        spSeason = new JSpinner();
        detailsPanel.add(spSeason, "cell 1 1,growx");

        JLabel lblEpisode = new JLabel(BUNDLE.getString("metatag.episode")); //$NON-NLS-1$
        detailsPanel.add(lblEpisode, "cell 3 1,alignx right");

        spEpisode = new JSpinner();
        detailsPanel.add(spEpisode, "cell 4 1,growx");
      }
      {
        JLabel lblDvdSeason = new JLabel(BUNDLE.getString("metatag.dvdseason")); //$NON-NLS-1$
        detailsPanel.add(lblDvdSeason, "cell 0 2,alignx right,aligny center");

        spDvdSeason = new JSpinner();
        detailsPanel.add(spDvdSeason, "cell 1 2,growx");

        JLabel lblDvdEpisode = new JLabel(BUNDLE.getString("metatag.dvdepisode")); //$NON-NLS-1$
        detailsPanel.add(lblDvdEpisode, "cell 3 2,alignx right");

        spDvdEpisode = new JSpinner();
        detailsPanel.add(spDvdEpisode, "cell 4 2,growx");

        JLabel lblDvdOrder = new JLabel(BUNDLE.getString("metatag.dvdorder")); //$NON-NLS-1$
        detailsPanel.add(lblDvdOrder, "cell 6 2,alignx right");

        cbDvdOrder = new JCheckBox("");
        detailsPanel.add(cbDvdOrder, "cell 7 2");
      }
      {
        JLabel lblDisplaySeason = new JLabel(BUNDLE.getString("metatag.displayseason")); //$NON-NLS-1$
        detailsPanel.add(lblDisplaySeason, "cell 0 3,alignx right");

        spDisplaySeason = new JSpinner();
        detailsPanel.add(spDisplaySeason, "cell 1 3,growx");

        JLabel lblDisplayEpisode = new JLabel(BUNDLE.getString("metatag.displayepisode")); //$NON-NLS-1$
        detailsPanel.add(lblDisplayEpisode, "cell 3 3,alignx right");

        spDisplayEpisode = new JSpinner();
        detailsPanel.add(spDisplayEpisode, "cell 4 3,growx");
      }
      {
        JLabel lblRating = new JLabel(BUNDLE.getString("metatag.rating")); //$NON-NLS-1$
        detailsPanel.add(lblRating, "cell 0 4,alignx right");

        spRating = new JSpinner();
        detailsPanel.add(spRating, "cell 1 4,growx");
      }
      {
        JLabel lblFirstAired = new JLabel(BUNDLE.getString("metatag.aired")); //$NON-NLS-1$
        detailsPanel.add(lblFirstAired, "cell 3 4,alignx right");

        dpFirstAired = new DatePicker(episodeToEdit.getFirstAired());
        detailsPanel.add(dpFirstAired, "cell 4 4 2 1,growx");
      }
      {
        JLabel lblWatched = new JLabel(BUNDLE.getString("metatag.watched")); //$NON-NLS-1$
        detailsPanel.add(lblWatched, "cell 0 5,alignx right");

        chckbxWatched = new JCheckBox("");
        detailsPanel.add(chckbxWatched, "cell 1 5");
      }
      {
        JLabel lblDateAdded = new JLabel(BUNDLE.getString("metatag.dateadded")); //$NON-NLS-1$
        detailsPanel.add(lblDateAdded, "cell 3 5,alignx right");

        spDateAdded = new JSpinner(new SpinnerDateModel());
        detailsPanel.add(spDateAdded, "cell 4 5 2 1,growx");
      }
      {
        JLabel lblMediasource = new JLabel(BUNDLE.getString("metatag.source")); //$NON-NLS-1$
        detailsPanel.add(lblMediasource, "cell 0 6,alignx right");

        cbMediaSource = new JComboBox(MediaSource.values());
        detailsPanel.add(cbMediaSource, "cell 1 6 2 1,growx");
      }
      {
        JLabel lblPlot = new JLabel(BUNDLE.getString("metatag.plot")); //$NON-NLS-1$
        detailsPanel.add(lblPlot, "cell 0 7,alignx right,aligny top");

        JScrollPane scrollPane = new JScrollPane();
        detailsPanel.add(scrollPane, "cell 1 7 7 1,grow");

        taPlot = new JTextArea();
        taPlot.setLineWrap(true);
        taPlot.setWrapStyleWord(true);
        scrollPane.setViewportView(taPlot);
      }
      {
        lblThumb = new ImageLabel();
        lblThumb.addMouseListener(new MouseAdapter() {
          @Override
          public void mouseClicked(MouseEvent e) {
            String path = TmmProperties.getInstance().getProperty(DIALOG_ID + ".path");
            Path file = TmmUIHelper.selectFile(BUNDLE.getString("image.choose"), path); //$NON-NLS-1$
            if (file != null && Utils.isRegularFile(file)) {
              String fileName = file.toAbsolutePath().toString();
              lblThumb.setImageUrl("file:/" + fileName);
              TmmProperties.getInstance().putProperty(DIALOG_ID + ".path", fileName);
            }
          }
        });
        lblThumb.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        detailsPanel.add(lblThumb, "cell 9 1 1 7,grow");
      }
      {
        JLabel lblTags = new JLabel(BUNDLE.getString("metatag.tags")); //$NON-NLS-1$
        detailsPanel.add(lblTags, "flowy,cell 0 8,alignx right,aligny top");

        JScrollPane scrollPaneTags = new JScrollPane();
        detailsPanel.add(scrollPaneTags, "cell 1 8 3 1,grow");

        listTags = new JList();
        scrollPaneTags.setViewportView(listTags);

        cbTags = new AutocompleteComboBox<>(tvShowList.getTagsInEpisodes());
        cbTags.setEditable(true);
        cbTagsAutoCompleteSupport = cbTags.getAutoCompleteSupport();
        InputMap im = cbTags.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        Object enterAction = im.get(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0));
        cbTags.getActionMap().put(enterAction, new AddTagAction());
        detailsPanel.add(cbTags, "cell 1 9 3 1,growx");

        {
          JButton btnAddTag = new JButton(new AddTagAction());
          btnAddTag.setMargin(BUTTON_MARGIN);
          detailsPanel.add(btnAddTag, "cell 0 8,alignx right,aligny top");
        }
        {
          JButton btnRemoveTag = new JButton(new RemoveTagAction());
          btnRemoveTag.setMargin(BUTTON_MARGIN);
          detailsPanel.add(btnRemoveTag, "cell 0 8,alignx right,aligny top");
        }
        {
          JButton btnMoveTagUp = new JButton(new MoveTagUpAction());
          btnMoveTagUp.setMargin(BUTTON_MARGIN);
          detailsPanel.add(btnMoveTagUp, "cell 0 8,alignx right,aligny top");
        }
        {
          JButton btnMoveTagDown = new JButton(new MoveTagDownAction());
          btnMoveTagDown.setMargin(BUTTON_MARGIN);
          detailsPanel.add(btnMoveTagDown, "cell 0 8,alignx right,aligny top");
        }
      }
    }

    /**********************************************************************************
     * CrewPanel
     **********************************************************************************/
    {
      JPanel crewPanel = new JPanel();
      tabbedPane.addTab(BUNDLE.getString("movie.edit.castandcrew"), null, crewPanel, null); //$NON-NLS-1$
      crewPanel.setLayout(new MigLayout("", "[][150lp:300lp,grow][20lp:n][][150lp:300lp,grow]", "[100lp:250lp][20lp:n][100lp:200lp]"));
      {
        JLabel lblGuests = new JLabel(BUNDLE.getString("metatag.guests")); //$NON-NLS-1$
        crewPanel.add(lblGuests, "flowy,cell 0 0,alignx right,aligny top");

        JScrollPane scrollPane = new JScrollPane();
        crewPanel.add(scrollPane, "cell 1 0,grow");

        tableGuests = new PersonTable(guests, true);
        tableGuests.configureScrollPane(scrollPane);
      }
      {
        JLabel lblDirectorsT = new JLabel(BUNDLE.getString("metatag.directors")); //$NON-NLS-1$
        crewPanel.add(lblDirectorsT, "flowy,cell 0 2,alignx right,aligny top");

        JScrollPane scrollPane = new JScrollPane();
        crewPanel.add(scrollPane, "cell 1 2,grow");

        tableDirectors = new PersonTable(directors, true);
        tableDirectors.configureScrollPane(scrollPane);
      }
      {
        JLabel lblWritersT = new JLabel(BUNDLE.getString("metatag.writers")); //$NON-NLS-1$
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
      tabbedPane.addTab(BUNDLE.getString("metatag.mediafiles"), null, mediaFilesPanel, null); //$NON-NLS-1$
    }

    /**********************************************************************************
     * bottom panel
     *********************************************************************************/
    {
      JPanel bottomPanel = new JPanel();
      rootPanel.add(bottomPanel, BorderLayout.SOUTH);
      bottomPanel.setOpaque(false);

      cbScraper = new MediaScraperComboBox(tvShowList.getAvailableMediaScrapers());
      MediaScraper defaultScraper = tvShowList.getDefaultMediaScraper();
      bottomPanel.setLayout(new MigLayout("", "[][][][grow][]", "[]"));
      cbScraper.setSelectedItem(defaultScraper);
      bottomPanel.add(cbScraper, "cell 0 0");

      JButton btnScrape = new JButton(new ScrapeAction());
      bottomPanel.add(btnScrape, "cell 1 0");

      JButton btnSearch = new JButton(new SearchAction());
      bottomPanel.add(btnSearch, "cell 2 0");
      {
        JPanel buttonPane = new JPanel();
        buttonPane.setOpaque(false);
        bottomPanel.add(buttonPane, "cell 4 0");
        EqualsLayout layout = new EqualsLayout(5);
        layout.setMinWidth(100);
        buttonPane.setLayout(layout);

        JButton okButton = new JButton(new ChangeEpisodeAction());
        buttonPane.add(okButton);
        getRootPane().setDefaultButton(okButton);

        JButton cancelButton = new JButton(new DiscardAction());
        buttonPane.add(cancelButton);

        if (inQueue) {
          JButton abortButton = new JButton(new AbortQueueAction());
          buttonPane.add(abortButton);
        }
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

  private class ScrapeAction extends AbstractAction {
    private static final long serialVersionUID = -4799506776650330500L;

    ScrapeAction() {
      putValue(NAME, BUNDLE.getString("Button.scrape")); //$NON-NLS-1$
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
      putValue(NAME, BUNDLE.getString("tvshowepisodechooser.search")); //$NON-NLS-1$
      putValue(SMALL_ICON, IconManager.SEARCH);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      MediaScraper scraper = (MediaScraper) cbScraper.getSelectedItem();
      TvShowEpisodeChooserDialog dialog = new TvShowEpisodeChooserDialog(episodeToEdit, scraper);
      dialog.setLocationRelativeTo(TvShowEpisodeEditorDialog.this);
      dialog.setVisible(true);
      MediaEpisode metadata = dialog.getMetadata();
      if (metadata != null && StringUtils.isNotBlank(metadata.title)) {
        tfTitle.setText(metadata.title);
        taPlot.setText(metadata.plot);
        spEpisode.setValue(metadata.episode);
        spSeason.setValue(metadata.season);
        for (MediaArtwork ma : metadata.artwork) {
          if (ma.getType() == MediaArtworkType.THUMB) {
            lblThumb.setImageUrl(ma.getDefaultUrl());
            break;
          }
        }
      }
    }
  }

  private class ChangeEpisodeAction extends AbstractAction {
    private static final long serialVersionUID = -4799506776650330500L;

    ChangeEpisodeAction() {
      putValue(NAME, BUNDLE.getString("Button.ok")); //$NON-NLS-1$
      putValue(SHORT_DESCRIPTION, BUNDLE.getString("tvshow.change"));//$NON-NLS-1$
      putValue(SMALL_ICON, IconManager.APPLY_INV);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      episodeToEdit.setTitle(tfTitle.getText());
      episodeToEdit.setDvdOrder(cbDvdOrder.isSelected());
      episodeToEdit.setAiredSeason((Integer) spSeason.getValue());
      episodeToEdit.setAiredEpisode((Integer) spEpisode.getValue());
      episodeToEdit.setDvdSeason((Integer) spDvdSeason.getValue());
      episodeToEdit.setDvdEpisode((Integer) spDvdEpisode.getValue());
      episodeToEdit.setDisplaySeason((Integer) spDisplaySeason.getValue());
      episodeToEdit.setDisplayEpisode((Integer) spDisplayEpisode.getValue());
      episodeToEdit.setMediaSource((MediaSource) cbMediaSource.getSelectedItem());
      episodeToEdit.setPlot(taPlot.getText());

      // sync media files with the media file editor and fire the mediaFiles event
      MediaFileEditorPanel.syncMediaFiles(mediaFiles, episodeToEdit.getMediaFiles());
      episodeToEdit.fireEventForChangedMediaInformation();

      double tempRating = (Double) spRating.getValue();
      float rating = (float) tempRating;
      if (episodeToEdit.getRating() != rating) {
        episodeToEdit.setRating(rating);
        episodeToEdit.setVotes(voteCount);
      }

      episodeToEdit.setDateAdded((Date) spDateAdded.getValue());
      episodeToEdit.setFirstAired(dpFirstAired.getDate());

      episodeToEdit.setWatched(chckbxWatched.isSelected());
      episodeToEdit.setActors(guests);
      episodeToEdit.setDirectors(directors);
      episodeToEdit.setWriters(writers);

      if (StringUtils.isNotEmpty(lblThumb.getImageUrl()) && (!lblThumb.getImageUrl().equals(episodeToEdit.getArtworkUrl(MediaFileType.THUMB))
          || StringUtils.isBlank(episodeToEdit.getArtworkUrl(MediaFileType.THUMB)))) {
        episodeToEdit.setArtworkUrl(lblThumb.getImageUrl(), MediaFileType.THUMB);
        episodeToEdit.writeThumbImage();
      }

      episodeToEdit.setTags(tags);
      episodeToEdit.writeNFO();
      episodeToEdit.saveToDb();

      setVisible(false);
    }
  }

  private class DiscardAction extends AbstractAction {
    private static final long serialVersionUID = -5581329896797961536L;

    DiscardAction() {
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

  private class AbortQueueAction extends AbstractAction {
    private static final long serialVersionUID = -7652218354710642510L;

    AbortQueueAction() {
      putValue(NAME, BUNDLE.getString("Button.abortqueue")); //$NON-NLS-1$
      putValue(SHORT_DESCRIPTION, BUNDLE.getString("tvshow.edit.abortqueue.desc")); //$NON-NLS-1$
      putValue(SMALL_ICON, IconManager.PROCESS_STOP);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      continueQueue = false;
      setVisible(false);
    }
  }

  private class ScrapeTask extends SwingWorker<Void, Void> {
    MediaScraper mediaScraper;

    ScrapeTask(MediaScraper mediaScraper) {
      this.mediaScraper = mediaScraper;
    }

    @Override
    protected Void doInBackground() throws Exception {
      setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
      MediaScrapeOptions options = new MediaScrapeOptions(MediaType.TV_EPISODE);
      options.setLanguage(LocaleUtils.toLocale(TvShowModuleManager.SETTINGS.getScraperLanguage().name()));
      options.setCountry(TvShowModuleManager.SETTINGS.getCertificationCountry());
      for (Entry<String, Object> entry : episodeToEdit.getTvShow().getIds().entrySet()) {
        options.setId(entry.getKey(), entry.getValue().toString());
      }

      options.setId(MediaMetadata.SEASON_NR, spSeason.getValue().toString());
      options.setId(MediaMetadata.EPISODE_NR, spEpisode.getValue().toString());

      options.setId(MediaMetadata.SEASON_NR_DVD, spDvdSeason.getValue().toString());
      options.setId(MediaMetadata.EPISODE_NR_DVD, spDvdEpisode.getValue().toString());
      try {
        LOGGER.info("=====================================================");
        LOGGER.info("Scraper metadata with scraper: " + mediaScraper.getMediaProvider().getProviderInfo().getId() + ", "
            + mediaScraper.getMediaProvider().getProviderInfo().getVersion());
        LOGGER.info(options.toString());
        LOGGER.info("=====================================================");
        MediaMetadata metadata = ((ITvShowMetadataProvider) mediaScraper.getMediaProvider()).getMetadata(options);

        // if nothing has been found -> open the search box
        if (metadata == null || StringUtils.isBlank(metadata.getTitle())) {
          // message
          JOptionPane.showMessageDialog(TvShowEpisodeEditorDialog.this, BUNDLE.getString("message.scrape.tvshowepisodefailed")); //$NON-NLS-1$
        }
        else {
          tfTitle.setText(metadata.getTitle());
          taPlot.setText(metadata.getPlot());
          dpFirstAired.setDate(metadata.getReleaseDate());
          spRating.setValue(metadata.getRating());
          // buffer votes not visible
          voteCount = metadata.getVoteCount();

          // set aired or dvd ep/season
          spSeason.setValue(metadata.getSeasonNumber());
          spEpisode.setValue(metadata.getEpisodeNumber());
          spDvdSeason.setValue(metadata.getDvdSeasonNumber());
          spDvdEpisode.setValue(metadata.getDvdEpisodeNumber());
          spDisplayEpisode.setValue(metadata.getDisplayEpisodeNumber());
          spDisplaySeason.setValue(metadata.getDisplaySeasonNumber());

          // cast
          guests.clear();
          directors.clear();
          writers.clear();

          for (MediaCastMember member : metadata.getCastMembers()) {
            switch (member.getType()) {
              case ACTOR:
                Person actor = new Person(ACTOR, member.getName(), member.getCharacter());
                actor.setThumbUrl(member.getImageUrl());
                guests.add(actor);
                break;

              case DIRECTOR:
                Person director = new Person(DIRECTOR, member.getName(), member.getPart());
                director.setThumbUrl(member.getImageUrl());
                directors.add(director);
                break;

              case WRITER:
                Person writer = new Person(WRITER, member.getName(), member.getPart());
                writer.setThumbUrl(member.getImageUrl());
                writers.add(writer);
                break;

              default:
                break;
            }
          }

          // artwork
          for (MediaArtwork ma : metadata.getFanart()) {
            if (ma.getType() == MediaArtworkType.THUMB) {
              lblThumb.setImageUrl(ma.getDefaultUrl());
              break;
            }
          }
        }
      }
      catch (Exception e) {
        LOGGER.warn("Error getting metadata " + e.getMessage());
      }

      setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
      return null;
    }
  }

  protected void initDataBindings() {
    JListBinding<String, List<String>, JList> jListBinding = SwingBindings.createJListBinding(UpdateStrategy.READ, tags, listTags);
    bindings.add(jListBinding);
    jListBinding.bind();
    //
  }

  @Override
  public void dispose() {
    super.dispose();

    mediaFilesPanel.unbindBindings();
    dpFirstAired.cleanup();
  }

  @Override
  public void pack() {
    // do not let it pack - it looks weird
  }

  private class AddTagAction extends AbstractAction {
    private static final long serialVersionUID = 5968029647764173330L;

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

  private class RemoveTagAction extends AbstractAction {
    private static final long serialVersionUID = -4799506776650330500L;

    RemoveTagAction() {
      putValue(SHORT_DESCRIPTION, BUNDLE.getString("tag.remove")); //$NON-NLS-1$
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

  private class AddGuestAction extends AbstractAction {
    private static final long serialVersionUID = -5879601617842300526L;

    AddGuestAction() {
      putValue(SHORT_DESCRIPTION, BUNDLE.getString("cast.guest.add")); //$NON-NLS-1$
      putValue(SMALL_ICON, IconManager.ADD_INV);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      Person actor = new Person(Person.Type.ACTOR, BUNDLE.getString("cast.actor.unknown"), BUNDLE.getString("cast.role.unknown")); //$NON-NLS-1$
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
      putValue(SHORT_DESCRIPTION, BUNDLE.getString("cast.guest.remove")); //$NON-NLS-1$
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
      putValue(SHORT_DESCRIPTION, BUNDLE.getString("cast.guest.moveup")); //$NON-NLS-1$
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
      putValue(SHORT_DESCRIPTION, BUNDLE.getString("cast.guest.movedown")); //$NON-NLS-1$
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
      putValue(SHORT_DESCRIPTION, BUNDLE.getString("cast.director.add")); //$NON-NLS-1$
      putValue(SMALL_ICON, IconManager.ADD_INV);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      Person person = new Person(Person.Type.DIRECTOR, BUNDLE.getString("director.name.unknown"), "Director"); //$NON-NLS-1$
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
      putValue(SHORT_DESCRIPTION, BUNDLE.getString("cast.director.remove")); //$NON-NLS-1$
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
      putValue(SHORT_DESCRIPTION, BUNDLE.getString("movie.edit.movedirectorup")); //$NON-NLS-1$
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
      putValue(SHORT_DESCRIPTION, BUNDLE.getString("movie.edit.movedirectordown")); //$NON-NLS-1$
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
      putValue(SHORT_DESCRIPTION, BUNDLE.getString("cast.writer.add")); //$NON-NLS-1$
      putValue(SMALL_ICON, IconManager.ADD_INV);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      Person person = new Person(Person.Type.DIRECTOR, BUNDLE.getString("writer.name.unknown"), "Writer"); //$NON-NLS-1$
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
      putValue(SHORT_DESCRIPTION, BUNDLE.getString("cast.writer.remove")); //$NON-NLS-1$
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
      putValue(SHORT_DESCRIPTION, BUNDLE.getString("movie.edit.movewriterup")); //$NON-NLS-1$
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
      putValue(SHORT_DESCRIPTION, BUNDLE.getString("movie.edit.movewriterdown")); //$NON-NLS-1$
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
}
