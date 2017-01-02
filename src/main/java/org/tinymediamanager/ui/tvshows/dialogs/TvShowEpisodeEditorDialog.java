/*
 * Copyright 2012 - 2016 Manuel Laggner
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

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.nio.file.Path;
import java.util.ArrayList;
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
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SpinnerDateModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingWorker;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.apache.commons.lang3.LocaleUtils;
import org.apache.commons.lang3.StringUtils;
import org.jdesktop.beansbinding.AutoBinding.UpdateStrategy;
import org.jdesktop.beansbinding.BeanProperty;
import org.jdesktop.observablecollections.ObservableCollections;
import org.jdesktop.swingbinding.JListBinding;
import org.jdesktop.swingbinding.JTableBinding;
import org.jdesktop.swingbinding.SwingBindings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.core.MediaFileType;
import org.tinymediamanager.core.MediaSource;
import org.tinymediamanager.core.Utils;
import org.tinymediamanager.core.entities.MediaFile;
import org.tinymediamanager.core.tvshow.TvShowList;
import org.tinymediamanager.core.tvshow.TvShowModuleManager;
import org.tinymediamanager.core.tvshow.entities.TvShowActor;
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
import org.tinymediamanager.ui.TmmFontHelper;
import org.tinymediamanager.ui.TmmUIHelper;
import org.tinymediamanager.ui.UTF8Control;
import org.tinymediamanager.ui.components.AutocompleteComboBox;
import org.tinymediamanager.ui.components.ImageLabel;
import org.tinymediamanager.ui.components.MediaScraperComboBox;
import org.tinymediamanager.ui.components.datepicker.DatePicker;
import org.tinymediamanager.ui.dialogs.TmmDialog;
import org.tinymediamanager.ui.panels.MediaFileEditorPanel;

import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.FormSpecs;
import com.jgoodies.forms.layout.RowSpec;

import ca.odell.glazedlists.swing.AutoCompleteSupport;

/**
 * The Class TvShowEpisodeScrapeDialog.
 * 
 * @author Manuel Laggner
 */
public class TvShowEpisodeEditorDialog extends TmmDialog implements ActionListener {
  private static final long                                     serialVersionUID = 7702248909791283043L;
  /**
   * @wbp.nls.resourceBundle messages
   */
  private static final ResourceBundle                           BUNDLE           = ResourceBundle.getBundle("messages", new UTF8Control());           //$NON-NLS-1$
  private static final Logger                                   LOGGER           = LoggerFactory.getLogger(TvShowEpisodeEditorDialog.class);
  private static final Date                                     INITIAL_DATE     = new Date(0);

  private TvShowList                                            tvShowList       = TvShowList.getInstance();
  private TvShowEpisode                                         episodeToEdit;
  private List<TvShowActor>                                     cast             = ObservableCollections.observableList(new ArrayList<TvShowActor>());
  private List<String>                                          tags             = ObservableCollections.observableList(new ArrayList<String>());
  private List<MediaFile>                                       mediaFiles       = new ArrayList<>();
  private boolean                                               continueQueue    = true;
  private int                                                   voteCount        = 0;

  private JTextField                                            tfTitle;
  private JLabel                                                lblFilename;
  private JSpinner                                              spEpisode;
  private JSpinner                                              spSeason;
  private JSpinner                                              spRating;
  private JSpinner                                              spDvdSeason;
  private JSpinner                                              spDvdEpisode;
  private JCheckBox                                             cbDvdOrder;
  private JSpinner                                              spDisplaySeason;
  private JSpinner                                              spDisplayEpisode;
  private DatePicker                                            dpFirstAired;
  private JSpinner                                              spDateAdded;
  private JCheckBox                                             chckbxWatched;
  private ImageLabel                                            lblThumb;
  private JTextArea                                             taPlot;
  private JTextField                                            tfDirector;
  private JTextField                                            tfWriter;
  private JTable                                                tableGuests;
  private AutocompleteComboBox<String>                          cbTags;
  private AutoCompleteSupport<String>                           cbTagsAutoCompleteSupport;
  private JList<String>                                         listTags;
  private JComboBox<MediaSource>                                cbMediaSource;
  private MediaFileEditorPanel                                  mediaFilesPanel;
  private MediaScraperComboBox                                  cbScraper;

  private JTableBinding<TvShowActor, List<TvShowActor>, JTable> jTableBinding;
  private JListBinding<String, List<String>, JList>             jListBinding;

  /**
   * Instantiates a new tv show episode scrape dialog.
   * 
   * @param episode
   *          the episode
   * @param inQueue
   *          the in queue
   */
  public TvShowEpisodeEditorDialog(TvShowEpisode episode, boolean inQueue) {
    super(BUNDLE.getString("tvshowepisode.scrape"), "tvShowEpisodeScraper"); //$NON-NLS-1$
    setBounds(5, 5, 964, 632);

    for (MediaFile mf : episode.getMediaFiles()) {
      mediaFiles.add(new MediaFile(mf));
    }

    this.episodeToEdit = episode;
    getContentPane().setLayout(new BorderLayout());

    {
      JPanel panelFilename = new JPanel();
      getContentPane().add(panelFilename, BorderLayout.NORTH);
      panelFilename.setLayout(new FormLayout(
          new ColumnSpec[] { FormFactory.RELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC, FormFactory.DEFAULT_COLSPEC,
              FormFactory.RELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC, FormFactory.RELATED_GAP_COLSPEC, },
          new RowSpec[] { FormFactory.LINE_GAP_ROWSPEC, RowSpec.decode("15px"), FormFactory.RELATED_GAP_ROWSPEC, }));

      JLabel lblFilenameT = new JLabel(BUNDLE.getString("metatag.path")); //$NON-NLS-1$
      panelFilename.add(lblFilenameT, "2, 2, left, top");

      lblFilename = new JLabel("");
      TmmFontHelper.changeFont(lblFilename, 1.166, Font.BOLD);
      panelFilename.add(lblFilename, "5, 2, left, top");
    }

    JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.NORTH);
    getContentPane().add(tabbedPane, BorderLayout.CENTER);

    /**
     * DetailsPanel
     */
    {
      JPanel detailsPanel = new JPanel();
      tabbedPane.addTab(BUNDLE.getString("metatag.details"), detailsPanel); //$NON-NLS-1$
      detailsPanel.setLayout(new FormLayout(
          new ColumnSpec[] { FormSpecs.LABEL_COMPONENT_GAP_COLSPEC, FormSpecs.DEFAULT_COLSPEC, FormSpecs.RELATED_GAP_COLSPEC,
              ColumnSpec.decode("40dlu:grow"), FormSpecs.RELATED_GAP_COLSPEC, ColumnSpec.decode("7dlu:grow"), FormSpecs.RELATED_GAP_COLSPEC,
              FormSpecs.DEFAULT_COLSPEC, FormSpecs.RELATED_GAP_COLSPEC, ColumnSpec.decode("20dlu"), FormSpecs.RELATED_GAP_COLSPEC,
              ColumnSpec.decode("30dlu:grow"), FormSpecs.RELATED_GAP_COLSPEC, FormSpecs.DEFAULT_COLSPEC, FormSpecs.RELATED_GAP_COLSPEC,
              FormSpecs.DEFAULT_COLSPEC, FormSpecs.RELATED_GAP_COLSPEC, ColumnSpec.decode("7dlu:grow"), FormSpecs.RELATED_GAP_COLSPEC,
              FormSpecs.DEFAULT_COLSPEC, FormSpecs.RELATED_GAP_COLSPEC, ColumnSpec.decode("100dlu:grow"), FormSpecs.LABEL_COMPONENT_GAP_COLSPEC, },
          new RowSpec[] { FormSpecs.LINE_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC, FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC,
              FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC, FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC,
              FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC, FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC,
              FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC, FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC,
              FormSpecs.RELATED_GAP_ROWSPEC, RowSpec.decode("35dlu:grow"), FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC,
              FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC, FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC,
              FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC, FormSpecs.RELATED_GAP_ROWSPEC, RowSpec.decode("default:grow"),
              FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC, FormSpecs.RELATED_GAP_ROWSPEC, }));

      JLabel lblTitle = new JLabel(BUNDLE.getString("metatag.title")); //$NON-NLS-1$
      detailsPanel.add(lblTitle, "2, 4, right, default");

      tfTitle = new JTextField();
      detailsPanel.add(tfTitle, "4, 4, 19, 1");
      tfTitle.setColumns(10);

      JLabel lblSeason = new JLabel(BUNDLE.getString("metatag.season")); //$NON-NLS-1$
      detailsPanel.add(lblSeason, "2, 6, right, default");

      spSeason = new JSpinner();
      detailsPanel.add(spSeason, "4, 6");

      JLabel lblEpisode = new JLabel(BUNDLE.getString("metatag.episode")); //$NON-NLS-1$
      detailsPanel.add(lblEpisode, "8, 6, right, default");

      spEpisode = new JSpinner();
      detailsPanel.add(spEpisode, "10, 6");

      JLabel lblDvdSeason = new JLabel(BUNDLE.getString("metatag.dvdseason")); //$NON-NLS-1$
      detailsPanel.add(lblDvdSeason, "2, 8, right, default");

      spDvdSeason = new JSpinner();
      detailsPanel.add(spDvdSeason, "4, 8");

      JLabel lblDvdEpisode = new JLabel(BUNDLE.getString("metatag.dvdepisode")); //$NON-NLS-1$
      detailsPanel.add(lblDvdEpisode, "8, 8, right, default");

      spDvdEpisode = new JSpinner();
      detailsPanel.add(spDvdEpisode, "10, 8");

      JLabel lblDvdOrder = new JLabel(BUNDLE.getString("metatag.dvdorder")); //$NON-NLS-1$
      detailsPanel.add(lblDvdOrder, "14, 8, right, default");

      cbDvdOrder = new JCheckBox("");
      detailsPanel.add(cbDvdOrder, "16, 8");
      cbDvdOrder.setSelected(episodeToEdit.isDvdOrder());

      JLabel lblDisplaySeason = new JLabel(BUNDLE.getString("metatag.displayseason")); //$NON-NLS-1$
      detailsPanel.add(lblDisplaySeason, "2, 10, right, default");

      spDisplaySeason = new JSpinner();
      detailsPanel.add(spDisplaySeason, "4, 10");

      JLabel lblDisplayEpisode = new JLabel(BUNDLE.getString("metatag.displayepisode")); //$NON-NLS-1$
      detailsPanel.add(lblDisplayEpisode, "8, 10, right, default");

      spDisplayEpisode = new JSpinner();
      detailsPanel.add(spDisplayEpisode, "10, 10");

      JLabel lblRating = new JLabel(BUNDLE.getString("metatag.rating")); //$NON-NLS-1$
      detailsPanel.add(lblRating, "2, 12, right, default");

      spRating = new JSpinner();
      detailsPanel.add(spRating, "4, 12");

      JLabel lblFirstAired = new JLabel(BUNDLE.getString("metatag.aired")); //$NON-NLS-1$
      detailsPanel.add(lblFirstAired, "8, 12, right, default");

      dpFirstAired = new DatePicker(episode.getFirstAired());
      detailsPanel.add(dpFirstAired, "10, 12, 3, 1, fill, default");

      JLabel lblWatched = new JLabel(BUNDLE.getString("metatag.watched")); //$NON-NLS-1$
      detailsPanel.add(lblWatched, "2, 14, right, default");

      chckbxWatched = new JCheckBox("");
      detailsPanel.add(chckbxWatched, "4, 14");

      JLabel lblDateAdded = new JLabel(BUNDLE.getString("metatag.dateadded")); //$NON-NLS-1$
      detailsPanel.add(lblDateAdded, "8, 14, right, default");

      spDateAdded = new JSpinner(new SpinnerDateModel());
      detailsPanel.add(spDateAdded, "10, 14, 3, 1, fill, default");

      JLabel lblMediasource = new JLabel(BUNDLE.getString("metatag.source")); //$NON-NLS-1$
      detailsPanel.add(lblMediasource, "2, 16, right, default");

      cbMediaSource = new JComboBox(MediaSource.values());
      detailsPanel.add(cbMediaSource, "4, 16, 4, 1, fill, default");

      JLabel lblPlot = new JLabel(BUNDLE.getString("metatag.plot")); //$NON-NLS-1$
      detailsPanel.add(lblPlot, "2, 18, right, top");

      JScrollPane scrollPane = new JScrollPane();
      detailsPanel.add(scrollPane, "4, 18, 13, 1, fill, fill");

      taPlot = new JTextArea();
      taPlot.setLineWrap(true);
      taPlot.setWrapStyleWord(true);
      scrollPane.setViewportView(taPlot);

      lblThumb = new ImageLabel();
      lblThumb.setAlternativeText(BUNDLE.getString("image.notfound.thumb")); //$NON-NLS-1$
      lblThumb.addMouseListener(new MouseAdapter() {
        @Override
        public void mouseClicked(MouseEvent e) {
          Path file = TmmUIHelper.selectFile(BUNDLE.getString("image.choose")); //$NON-NLS-1$
          if (file != null && Utils.isRegularFile(file)) {
            String fileName = file.toAbsolutePath().toString();
            lblThumb.setImageUrl("file:/" + fileName);
          }
        }
      });
      lblThumb.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
      detailsPanel.add(lblThumb, "20, 6, 3, 13");

      JLabel lblDirector = new JLabel(BUNDLE.getString("metatag.director")); //$NON-NLS-1$
      detailsPanel.add(lblDirector, "2, 20, right, default");

      tfDirector = new JTextField();
      tfDirector.setText((String) null);
      tfDirector.setColumns(10);
      detailsPanel.add(tfDirector, "4, 20, 13, 1, fill, default");

      JLabel lblWriter = new JLabel(BUNDLE.getString("metatag.writer")); //$NON-NLS-1$
      detailsPanel.add(lblWriter, "2, 22, right, default");

      tfWriter = new JTextField();
      tfWriter.setText((String) null);
      tfWriter.setColumns(10);
      detailsPanel.add(tfWriter, "4, 22, 13, 1, fill, default");

      JLabel lblGuests = new JLabel(BUNDLE.getString("metatag.guests")); //$NON-NLS-1$
      detailsPanel.add(lblGuests, "2, 24, right, top");

      JScrollPane scrollPaneGuests = new JScrollPane();
      detailsPanel.add(scrollPaneGuests, "4, 24, 13, 7, fill, fill");

      tableGuests = new JTable();
      tableGuests.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
      scrollPaneGuests.setViewportView(tableGuests);

      JLabel lblTags = new JLabel(BUNDLE.getString("metatag.tags")); //$NON-NLS-1$
      detailsPanel.add(lblTags, "20, 24, default, top");

      JScrollPane scrollPaneTags = new JScrollPane();
      detailsPanel.add(scrollPaneTags, "22, 24, 1, 5, fill, fill");

      listTags = new JList();
      scrollPaneTags.setViewportView(listTags);

      JButton btnAddActor = new JButton("");
      btnAddActor.setMargin(new Insets(2, 2, 2, 2));
      btnAddActor.setAction(new AddActorAction());
      btnAddActor.setIcon(IconManager.LIST_ADD);
      detailsPanel.add(btnAddActor, "2, 26, right, top");

      JButton btnAddTag = new JButton("");
      btnAddTag.setMargin(new Insets(2, 2, 2, 2));
      btnAddTag.setAction(new AddTagAction());
      btnAddTag.setIcon(IconManager.LIST_ADD);
      detailsPanel.add(btnAddTag, "20, 26, right, top");

      JButton btnRemoveActor = new JButton("");
      btnRemoveActor.setMargin(new Insets(2, 2, 2, 2));
      btnRemoveActor.setAction(new RemoveActorAction());
      btnRemoveActor.setIcon(IconManager.LIST_REMOVE);
      detailsPanel.add(btnRemoveActor, "2, 28, right, top");

      JButton btnRemoveTag = new JButton("");
      btnRemoveTag.setMargin(new Insets(2, 2, 2, 2));
      btnRemoveTag.setAction(new RemoveTagAction());
      btnRemoveTag.setIcon(IconManager.LIST_REMOVE);
      detailsPanel.add(btnRemoveTag, "20, 28, right, top");

      cbTags = new AutocompleteComboBox<String>(tvShowList.getTagsInEpisodes());
      cbTags.setEditable(true);
      cbTagsAutoCompleteSupport = cbTags.getAutoCompleteSupport();
      InputMap im = cbTags.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
      Object enterAction = im.get(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0));
      cbTags.getActionMap().put(enterAction, new AddTagAction());
      detailsPanel.add(cbTags, "22, 30, fill, default");
    }

    /**
     * Media Files panel
     */
    {
      mediaFilesPanel = new MediaFileEditorPanel(mediaFiles);
      tabbedPane.addTab(BUNDLE.getString("metatag.mediafiles"), null, mediaFilesPanel, null); //$NON-NLS-1$
    }

    {
      JPanel bottomPanel = new JPanel();
      getContentPane().add(bottomPanel, BorderLayout.SOUTH);

      bottomPanel.setLayout(new FormLayout(
          new ColumnSpec[] { FormFactory.LABEL_COMPONENT_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC, FormFactory.RELATED_GAP_COLSPEC,
              FormFactory.DEFAULT_COLSPEC, FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("default:grow"), FormFactory.RELATED_GAP_COLSPEC,
              FormFactory.DEFAULT_COLSPEC, FormFactory.RELATED_GAP_COLSPEC, },
          new RowSpec[] { FormFactory.LINE_GAP_ROWSPEC, RowSpec.decode("25px"), FormFactory.RELATED_GAP_ROWSPEC, }));

      cbScraper = new MediaScraperComboBox(tvShowList.getAvailableMediaScrapers());
      MediaScraper defaultScraper = tvShowList.getDefaultMediaScraper();
      cbScraper.setSelectedItem(defaultScraper);
      bottomPanel.add(cbScraper, "2, 2, fill, default");

      JButton btnScrape = new JButton(BUNDLE.getString("Button.scrape")); //$NON-NLS-1$
      btnScrape.setPreferredSize(new Dimension(100, 23));
      btnScrape.setMaximumSize(new Dimension(0, 0));
      btnScrape.setMinimumSize(new Dimension(100, 23));
      btnScrape.setActionCommand("Scrape");
      btnScrape.addActionListener(this);
      bottomPanel.add(btnScrape, "4, 2, left, fill");

      JButton btnSearch = new JButton(BUNDLE.getString("tvshowepisodechooser.search")); //$NON-NLS-1$
      btnSearch.setActionCommand("Search");
      btnSearch.addActionListener(this);
      btnSearch.setIcon(IconManager.SEARCH);
      bottomPanel.add(btnSearch, "6, 2, left, fill");
      {
        JPanel buttonPane = new JPanel();
        bottomPanel.add(buttonPane, "8, 2, fill, fill");
        EqualsLayout layout = new EqualsLayout(5);
        layout.setMinWidth(100);
        buttonPane.setLayout(layout);
        JButton okButton = new JButton(BUNDLE.getString("Button.ok")); //$NON-NLS-1$
        okButton.setToolTipText(BUNDLE.getString("tvshow.change"));
        okButton.setIcon(IconManager.APPLY);
        buttonPane.add(okButton);
        okButton.setActionCommand("OK");
        okButton.addActionListener(this);

        JButton cancelButton = new JButton(BUNDLE.getString("Button.cancel")); //$NON-NLS-1$
        cancelButton.setToolTipText(BUNDLE.getString("edit.discard"));
        cancelButton.setIcon(IconManager.CANCEL);
        buttonPane.add(cancelButton);
        cancelButton.setActionCommand("Cancel");
        cancelButton.addActionListener(this);

        if (inQueue) {
          JButton abortButton = new JButton(BUNDLE.getString("Button.abortqueue")); //$NON-NLS-1$
          abortButton.setToolTipText(BUNDLE.getString("tvshow.edit.abortqueue.desc")); //$NON-NLS-1$
          abortButton.setIcon(IconManager.PROCESS_STOP);
          buttonPane.add(abortButton);
          abortButton.setActionCommand("Abort");
          abortButton.addActionListener(this);
        }
      }
    }

    initDataBindings();

    // fill data
    {
      MediaFile mediaFile = episodeToEdit.getMediaFiles().get(0);
      lblFilename.setText(mediaFile.getFileAsPath().toString());
      tfTitle.setText(episodeToEdit.getTitle());

      spSeason.setModel(new SpinnerNumberModel(episodeToEdit.getAiredSeason(), -1, Integer.MAX_VALUE, 1));
      spEpisode.setModel(new SpinnerNumberModel(episodeToEdit.getAiredEpisode(), -1, Integer.MAX_VALUE, 1));
      spDvdSeason.setModel(new SpinnerNumberModel(episodeToEdit.getDvdSeason(), -1, Integer.MAX_VALUE, 1));
      spDvdEpisode.setModel(new SpinnerNumberModel(episodeToEdit.getDvdEpisode(), -1, Integer.MAX_VALUE, 1));
      spDisplaySeason.setModel(new SpinnerNumberModel(episodeToEdit.getDisplaySeason(), -1, Integer.MAX_VALUE, 1));
      spDisplayEpisode.setModel(new SpinnerNumberModel(episodeToEdit.getDisplayEpisode(), -1, Integer.MAX_VALUE, 1));
      spDateAdded.setValue(episodeToEdit.getDateAdded());

      lblThumb.setImagePath(episodeToEdit.getArtworkFilename(MediaFileType.THUMB));
      spRating.setModel(new SpinnerNumberModel(episodeToEdit.getRating(), 0.0, 10.0, 0.1));
      spRating.addChangeListener(new ChangeListener() {
        @Override
        public void stateChanged(ChangeEvent e) {
          voteCount = 1;
        }
      });
      voteCount = episodeToEdit.getVotes();
      chckbxWatched.setSelected(episodeToEdit.isWatched());
      taPlot.setText(episodeToEdit.getPlot());
      taPlot.setCaretPosition(0);
      tfDirector.setText(episodeToEdit.getDirector());
      tfWriter.setText(episodeToEdit.getWriter());
      cbMediaSource.setSelectedItem(episodeToEdit.getMediaSource());

      for (TvShowActor origCast : episodeToEdit.getGuests()) {
        TvShowActor actor = new TvShowActor();
        actor.setName(origCast.getName());
        actor.setCharacter(origCast.getCharacter());
        actor.setThumbUrl(origCast.getThumbUrl());
        cast.add(actor);
      }

      for (String tag : episodeToEdit.getTags()) {
        tags.add(tag);
      }
    }

    // adjust table columns
    tableGuests.getColumnModel().getColumn(0).setHeaderValue(BUNDLE.getString("metatag.name")); //$NON-NLS-1$
    tableGuests.getColumnModel().getColumn(1).setHeaderValue(BUNDLE.getString("metatag.role")); //$NON-NLS-1$

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

  @Override
  public void actionPerformed(ActionEvent e) {
    // assign scraped data
    if ("OK".equals(e.getActionCommand())) {
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
      episodeToEdit.setDirector(tfDirector.getText());
      episodeToEdit.setWriter(tfWriter.getText());
      episodeToEdit.setActors(cast);

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

    // cancel
    if ("Cancel".equals(e.getActionCommand())) {
      setVisible(false);
    }

    // Abort queue
    if ("Abort".equals(e.getActionCommand())) {
      continueQueue = false;
      setVisible(false);
    }

    // scrape
    if ("Scrape".equals(e.getActionCommand())) {
      MediaScraper scraper = (MediaScraper) cbScraper.getSelectedItem();
      ScrapeTask task = new ScrapeTask(scraper);
      task.execute();
    }

    // search
    if ("Search".equals(e.getActionCommand())) {
      MediaScraper scraper = (MediaScraper) cbScraper.getSelectedItem();
      TvShowEpisodeChooserDialog dialog = new TvShowEpisodeChooserDialog(episodeToEdit, scraper);
      dialog.setLocationRelativeTo(this);
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

  private class ScrapeTask extends SwingWorker<Void, Void> {
    MediaScraper mediaScraper;

    public ScrapeTask(MediaScraper mediaScraper) {
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
          spRating.setValue(new Double(metadata.getRating()));
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
          List<TvShowActor> actors = new ArrayList<>();
          String director = "";
          String writer = "";
          for (MediaCastMember member : metadata.getCastMembers()) {
            switch (member.getType()) {
              case ACTOR:
                TvShowActor actor = new TvShowActor();
                actor.setName(member.getName());
                actor.setCharacter(member.getCharacter());
                actor.setThumbUrl(member.getImageUrl());
                actors.add(actor);
                break;

              case DIRECTOR:
                if (!StringUtils.isEmpty(director)) {
                  director += ", ";
                }
                director += member.getName();
                break;

              case WRITER:
                if (!StringUtils.isEmpty(writer)) {
                  writer += ", ";
                }
                writer += member.getName();
                break;

              default:
                break;
            }
          }
          cast.clear();
          cast.addAll(actors);
          tfDirector.setText(director);
          tfWriter.setText(writer);

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
    jTableBinding = SwingBindings.createJTableBinding(UpdateStrategy.READ, cast, tableGuests);
    //
    BeanProperty<TvShowActor, String> movieCastBeanProperty = BeanProperty.create("name");
    jTableBinding.addColumnBinding(movieCastBeanProperty);
    //
    BeanProperty<TvShowActor, String> movieCastBeanProperty_1 = BeanProperty.create("character");
    jTableBinding.addColumnBinding(movieCastBeanProperty_1);
    //
    jTableBinding.bind();
    //
    jListBinding = SwingBindings.createJListBinding(UpdateStrategy.READ, tags, listTags);
    jListBinding.bind();
    //
  }

  @Override
  public void dispose() {
    super.dispose();
    jTableBinding.unbind();
    jListBinding.unbind();
    mediaFilesPanel.unbindBindings();
    dpFirstAired.cleanup();
  }

  @Override
  public void pack() {
    // do not let it pack - it looks weird
  }

  private class AddTagAction extends AbstractAction {
    private static final long serialVersionUID = 5968029647764173330L;

    public AddTagAction() {
      putValue(SHORT_DESCRIPTION, BUNDLE.getString("tag.add")); //$NON-NLS-1$
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

  private class AddActorAction extends AbstractAction {
    private static final long serialVersionUID = -5879601617842300526L;

    public AddActorAction() {
      putValue(SHORT_DESCRIPTION, BUNDLE.getString("cast.actor.add")); //$NON-NLS-1$
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      TvShowActor actor = new TvShowActor(BUNDLE.getString("cast.actor.unknown"), BUNDLE.getString("cast.role.unknown")); //$NON-NLS-1$
      cast.add(0, actor);
    }
  }

  private class RemoveActorAction extends AbstractAction {
    private static final long serialVersionUID = 6970920169867315771L;

    public RemoveActorAction() {
      putValue(SHORT_DESCRIPTION, BUNDLE.getString("cast.actor.remove")); //$NON-NLS-1$
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      int row = tableGuests.getSelectedRow();
      if (row > -1) {
        row = tableGuests.convertRowIndexToModel(row);
        cast.remove(row);
      }
    }
  }
}
