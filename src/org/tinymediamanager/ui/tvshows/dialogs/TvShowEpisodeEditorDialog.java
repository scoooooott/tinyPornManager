/*
 * Copyright 2012 - 2015 Manuel Laggner
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
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map.Entry;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpinnerDateModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingWorker;

import org.apache.commons.lang3.StringUtils;
import org.jdesktop.beansbinding.AutoBinding.UpdateStrategy;
import org.jdesktop.beansbinding.BeanProperty;
import org.jdesktop.observablecollections.ObservableCollections;
import org.jdesktop.swingbinding.JListBinding;
import org.jdesktop.swingbinding.JTableBinding;
import org.jdesktop.swingbinding.SwingBindings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.Globals;
import org.tinymediamanager.core.entities.MediaFile;
import org.tinymediamanager.core.tvshow.TvShowList;
import org.tinymediamanager.core.tvshow.TvShowScrapers;
import org.tinymediamanager.core.tvshow.entities.TvShowActor;
import org.tinymediamanager.core.tvshow.entities.TvShowEpisode;
import org.tinymediamanager.scraper.ITvShowMetadataProvider;
import org.tinymediamanager.scraper.MediaArtwork;
import org.tinymediamanager.scraper.MediaArtwork.MediaArtworkType;
import org.tinymediamanager.scraper.MediaEpisode;
import org.tinymediamanager.scraper.MediaMetadata;
import org.tinymediamanager.scraper.MediaScrapeOptions;
import org.tinymediamanager.scraper.MediaType;
import org.tinymediamanager.ui.EqualsLayout;
import org.tinymediamanager.ui.IconManager;
import org.tinymediamanager.ui.TmmUIHelper;
import org.tinymediamanager.ui.UTF8Control;
import org.tinymediamanager.ui.components.AutocompleteComboBox;
import org.tinymediamanager.ui.components.ImageLabel;
import org.tinymediamanager.ui.dialogs.TmmDialog;

import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

/**
 * The Class TvShowEpisodeScrapeDialog.
 * 
 * @author Manuel Laggner
 */
public class TvShowEpisodeEditorDialog extends TmmDialog implements ActionListener {
  private static final long                                     serialVersionUID = 7702248909791283043L;
  /** @wbp.nls.resourceBundle messages */
  private static final ResourceBundle                           BUNDLE           = ResourceBundle.getBundle("messages", new UTF8Control());           //$NON-NLS-1$
  private static final Logger                                   LOGGER           = LoggerFactory.getLogger(TvShowChooserDialog.class);
  private static final Date                                     INITIAL_DATE     = new Date(0);

  private TvShowList                                            tvShowList       = TvShowList.getInstance();
  private TvShowEpisode                                         episodeToEdit;
  private List<TvShowActor>                                     cast             = ObservableCollections.observableList(new ArrayList<TvShowActor>());
  private List<String>                                          tags             = ObservableCollections.observableList(new ArrayList<String>());
  private boolean                                               continueQueue    = true;

  private JTextField                                            tfTitle;
  private JLabel                                                lblFilename;
  private JSpinner                                              spEpisode;
  private JSpinner                                              spSeason;
  private JSpinner                                              spRating;
  private JSpinner                                              spDvdSeason;
  private JSpinner                                              spDvdEpisode;
  private JCheckBox                                             cbDvdOrder;
  private JSpinner                                              spFirstAired;
  private JSpinner                                              spDateAdded;
  private JCheckBox                                             chckbxWatched;
  private ImageLabel                                            lblThumb;
  private JTextArea                                             taPlot;
  private JTextField                                            tfDirector;
  private JTextField                                            tfWriter;
  private JTable                                                tableGuests;
  private JComboBox                                             cbTags;
  private JList                                                 listTags;

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

    this.episodeToEdit = episode;
    getContentPane().setLayout(new BorderLayout());
    {
      JPanel contentPanel = new JPanel();
      getContentPane().add(contentPanel, BorderLayout.CENTER);
      contentPanel.setLayout(new FormLayout(new ColumnSpec[] { FormFactory.LABEL_COMPONENT_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC,
          FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("100px"), FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("default:grow"),
          FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("120px"), FormFactory.RELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC,
          FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("300px:grow"), FormFactory.LABEL_COMPONENT_GAP_COLSPEC, }, new RowSpec[] {
          FormFactory.LINE_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
          FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
          FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
          FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC, RowSpec.decode("default:grow"),
          FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
          FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
          FormFactory.RELATED_GAP_ROWSPEC, RowSpec.decode("default:grow"), FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
          FormFactory.RELATED_GAP_ROWSPEC, }));

      JLabel lblFilenameT = new JLabel(BUNDLE.getString("metatag.path")); //$NON-NLS-1$
      contentPanel.add(lblFilenameT, "2, 2, right, default");

      lblFilename = new JLabel("");
      contentPanel.add(lblFilename, "4, 2, 9, 1, left, bottom");

      JLabel lblTitle = new JLabel(BUNDLE.getString("metatag.title")); //$NON-NLS-1$
      contentPanel.add(lblTitle, "2, 4, right, default");

      tfTitle = new JTextField();
      contentPanel.add(tfTitle, "4, 4, 9, 1");
      tfTitle.setColumns(10);

      JLabel lblSeason = new JLabel(BUNDLE.getString("metatag.season")); //$NON-NLS-1$
      contentPanel.add(lblSeason, "2, 6, right, default");

      spSeason = new JSpinner();
      contentPanel.add(spSeason, "4, 6");

      JLabel lblEpisode = new JLabel(BUNDLE.getString("metatag.episode")); //$NON-NLS-1$
      contentPanel.add(lblEpisode, "6, 6, right, default");

      spEpisode = new JSpinner();
      contentPanel.add(spEpisode, "8, 6");

      JLabel lblDvdSeason = new JLabel(BUNDLE.getString("metatag.dvdseason")); //$NON-NLS-1$
      contentPanel.add(lblDvdSeason, "2, 8, right, default");

      spDvdSeason = new JSpinner();
      contentPanel.add(spDvdSeason, "4, 8");

      JLabel lblDvdEpisode = new JLabel(BUNDLE.getString("metatag.dvdepisode")); //$NON-NLS-1$
      contentPanel.add(lblDvdEpisode, "6, 8, right, default");

      spDvdEpisode = new JSpinner();
      contentPanel.add(spDvdEpisode, "8, 8");

      JLabel lblDvdOrder = new JLabel(BUNDLE.getString("metatag.dvdorder")); //$NON-NLS-1$
      contentPanel.add(lblDvdOrder, "2, 10, right, default");

      cbDvdOrder = new JCheckBox("");
      contentPanel.add(cbDvdOrder, "4, 10");

      JLabel lblRating = new JLabel(BUNDLE.getString("metatag.rating")); //$NON-NLS-1$
      contentPanel.add(lblRating, "2, 12, right, default");

      spRating = new JSpinner();
      contentPanel.add(spRating, "4, 12");

      JLabel lblFirstAired = new JLabel(BUNDLE.getString("metatag.aired")); //$NON-NLS-1$
      contentPanel.add(lblFirstAired, "6, 12, right, default");

      spFirstAired = new JSpinner(new SpinnerDateModel());
      contentPanel.add(spFirstAired, "8, 12");

      JLabel lblWatched = new JLabel(BUNDLE.getString("metatag.watched")); //$NON-NLS-1$
      contentPanel.add(lblWatched, "2, 14, right, default");

      chckbxWatched = new JCheckBox("");
      contentPanel.add(chckbxWatched, "4, 14");

      JLabel lblDateAdded = new JLabel(BUNDLE.getString("metatag.dateadded")); //$NON-NLS-1$
      contentPanel.add(lblDateAdded, "6, 14, right, default");

      spDateAdded = new JSpinner(new SpinnerDateModel());
      contentPanel.add(spDateAdded, "8, 14");

      JLabel lblPlot = new JLabel(BUNDLE.getString("metatag.plot")); //$NON-NLS-1$
      contentPanel.add(lblPlot, "2, 16, right, top");

      JScrollPane scrollPane = new JScrollPane();
      contentPanel.add(scrollPane, "4, 16, 5, 1, fill, fill");

      taPlot = new JTextArea();
      taPlot.setLineWrap(true);
      taPlot.setWrapStyleWord(true);
      scrollPane.setViewportView(taPlot);

      lblThumb = new ImageLabel();
      lblThumb.setAlternativeText(BUNDLE.getString("image.notfound.thumb")); //$NON-NLS-1$
      lblThumb.addMouseListener(new MouseAdapter() {
        @Override
        public void mouseClicked(MouseEvent e) {
          File file = TmmUIHelper.selectFile(BUNDLE.getString("image.choose")); //$NON-NLS-1$
          if (file != null && file.exists() && file.isFile()) {
            String fileName = file.getPath();
            lblThumb.setImageUrl("file:/" + fileName);
          }
        }
      });
      lblThumb.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
      contentPanel.add(lblThumb, "10, 6, 3, 15");

      JLabel lblDirector = new JLabel(BUNDLE.getString("metatag.director")); //$NON-NLS-1$
      contentPanel.add(lblDirector, "2, 18, right, default");

      tfDirector = new JTextField();
      tfDirector.setText((String) null);
      tfDirector.setColumns(10);
      contentPanel.add(tfDirector, "4, 18, 5, 1, fill, default");

      JLabel lblWriter = new JLabel(BUNDLE.getString("metatag.writer")); //$NON-NLS-1$
      contentPanel.add(lblWriter, "2, 20, right, default");

      tfWriter = new JTextField();
      tfWriter.setText((String) null);
      tfWriter.setColumns(10);
      contentPanel.add(tfWriter, "4, 20, 5, 1, fill, default");

      JLabel lblGuests = new JLabel(BUNDLE.getString("metatag.guests")); //$NON-NLS-1$
      contentPanel.add(lblGuests, "2, 22, right, top");

      JScrollPane scrollPaneGuests = new JScrollPane();
      contentPanel.add(scrollPaneGuests, "4, 22, 5, 7, fill, fill");

      tableGuests = new JTable();
      scrollPaneGuests.setViewportView(tableGuests);

      JLabel lblTags = new JLabel(BUNDLE.getString("metatag.tags")); //$NON-NLS-1$
      contentPanel.add(lblTags, "10, 22, default, top");

      JScrollPane scrollPaneTags = new JScrollPane();
      contentPanel.add(scrollPaneTags, "12, 22, 1, 5, fill, fill");

      listTags = new JList();
      scrollPaneTags.setViewportView(listTags);

      JButton btnAddActor = new JButton("");
      btnAddActor.setMargin(new Insets(2, 2, 2, 2));
      btnAddActor.setAction(new AddActorAction());
      btnAddActor.setIcon(IconManager.LIST_ADD);
      contentPanel.add(btnAddActor, "2, 24, right, top");

      JButton btnAddTag = new JButton("");
      btnAddTag.setMargin(new Insets(2, 2, 2, 2));
      btnAddTag.setAction(new AddTagAction());
      btnAddTag.setIcon(IconManager.LIST_ADD);
      contentPanel.add(btnAddTag, "10, 24, right, top");

      JButton btnRemoveActor = new JButton("");
      btnRemoveActor.setMargin(new Insets(2, 2, 2, 2));
      btnRemoveActor.setAction(new RemoveActorAction());
      btnRemoveActor.setIcon(IconManager.LIST_REMOVE);
      contentPanel.add(btnRemoveActor, "2, 26, right, top");

      JButton btnRemoveTag = new JButton("");
      btnRemoveTag.setMargin(new Insets(2, 2, 2, 2));
      btnRemoveTag.setAction(new RemoveTagAction());
      btnRemoveTag.setIcon(IconManager.LIST_REMOVE);
      contentPanel.add(btnRemoveTag, "10, 26, right, top");

      cbTags = new AutocompleteComboBox(tvShowList.getTagsInEpisodes().toArray());
      cbTags.setEditable(true);
      contentPanel.add(cbTags, "12, 28, fill, default");
    }

    {
      JPanel bottomPanel = new JPanel();
      getContentPane().add(bottomPanel, BorderLayout.SOUTH);

      bottomPanel.setLayout(new FormLayout(new ColumnSpec[] { FormFactory.LABEL_COMPONENT_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC,
          FormFactory.RELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC, FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("default:grow"),
          FormFactory.RELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC, FormFactory.RELATED_GAP_COLSPEC, }, new RowSpec[] {
          FormFactory.LINE_GAP_ROWSPEC, RowSpec.decode("25px"), FormFactory.RELATED_GAP_ROWSPEC, }));

      JComboBox cbScraper = new JComboBox(TvShowScrapers.values());
      cbScraper.setSelectedItem(Globals.settings.getTvShowSettings().getTvShowScraper());
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
      lblFilename.setText(mediaFile.getPath() + File.separator + mediaFile.getFilename());
      tfTitle.setText(episodeToEdit.getTitle());

      spSeason.setModel(new SpinnerNumberModel(episodeToEdit.getAiredSeason(), -1, Integer.MAX_VALUE, 1));
      spEpisode.setModel(new SpinnerNumberModel(episodeToEdit.getAiredEpisode(), -1, Integer.MAX_VALUE, 1));
      spDvdSeason.setModel(new SpinnerNumberModel(episodeToEdit.getDvdSeason(), -1, Integer.MAX_VALUE, 1));
      spDvdEpisode.setModel(new SpinnerNumberModel(episodeToEdit.getDvdEpisode(), -1, Integer.MAX_VALUE, 1));
      cbDvdOrder.setSelected(episodeToEdit.isDvdOrder());

      SimpleDateFormat dateFormat = (SimpleDateFormat) DateFormat.getDateInstance(DateFormat.MEDIUM);
      // spDateAdded.setEditor(new JSpinner.DateEditor(spDateAdded, dateFormat.toPattern()));
      spFirstAired.setEditor(new JSpinner.DateEditor(spFirstAired, dateFormat.toPattern()));

      spDateAdded.setValue(episodeToEdit.getDateAdded());
      if (episodeToEdit.getFirstAired() != null) {
        spFirstAired.setValue(episodeToEdit.getFirstAired());
      }
      else {
        spFirstAired.setValue(INITIAL_DATE);
      }
      lblThumb.setImagePath(episodeToEdit.getThumb());
      spRating.setModel(new SpinnerNumberModel(episodeToEdit.getRating(), 0.0, 10.0, 0.1));
      chckbxWatched.setSelected(episodeToEdit.isWatched());
      taPlot.setText(episodeToEdit.getPlot());
      taPlot.setCaretPosition(0);
      tfDirector.setText(episodeToEdit.getDirector());
      tfWriter.setText(episodeToEdit.getWriter());

      for (TvShowActor origCast : episodeToEdit.getGuests()) {
        TvShowActor actor = new TvShowActor();
        actor.setName(origCast.getName());
        actor.setCharacter(origCast.getCharacter());
        actor.setThumb(origCast.getThumb());
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

      episodeToEdit.setPlot(taPlot.getText());

      double tempRating = (Double) spRating.getValue();
      float rating = (float) tempRating;
      if (episodeToEdit.getRating() != rating) {
        episodeToEdit.setRating(rating);
        episodeToEdit.setVotes(1);
      }

      episodeToEdit.setDateAdded((Date) spDateAdded.getValue());

      Date firstAiredDate = (Date) spFirstAired.getValue();
      if (!firstAiredDate.equals(INITIAL_DATE)) {
        episodeToEdit.setFirstAired(firstAiredDate);
      }

      episodeToEdit.setWatched(chckbxWatched.isSelected());
      episodeToEdit.setDirector(tfDirector.getText());
      episodeToEdit.setWriter(tfWriter.getText());
      episodeToEdit.setActors(cast);

      if (StringUtils.isNotEmpty(lblThumb.getImageUrl()) && !lblThumb.getImageUrl().equals(episodeToEdit.getThumbUrl())) {
        episodeToEdit.setThumbUrl(lblThumb.getImageUrl());
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
      ScrapeTask task = new ScrapeTask(TvShowList.getInstance().getMetadataProvider());
      task.execute();
    }

    // search
    if ("Search".equals(e.getActionCommand())) {
      TvShowEpisodeChooserDialog dialog = new TvShowEpisodeChooserDialog(episodeToEdit, TvShowList.getInstance().getMetadataProvider());
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
    ITvShowMetadataProvider mp;

    public ScrapeTask(ITvShowMetadataProvider mp) {
      this.mp = mp;
    }

    @Override
    protected Void doInBackground() throws Exception {
      MediaScrapeOptions options = new MediaScrapeOptions();
      options.setLanguage(Globals.settings.getTvShowSettings().getScraperLanguage());
      options.setCountry(Globals.settings.getTvShowSettings().getCertificationCountry());
      for (Entry<String, Object> entry : episodeToEdit.getTvShow().getIds().entrySet()) {
        options.setId(entry.getKey(), entry.getValue().toString());
      }

      options.setType(MediaType.TV_EPISODE);
      options.setId(MediaMetadata.SEASON_NR, spSeason.getValue().toString());
      options.setId(MediaMetadata.EPISODE_NR, spEpisode.getValue().toString());

      try {
        MediaMetadata metadata = mp.getEpisodeMetadata(options);

        // if nothing has been found -> open the search box
        if (metadata == null || StringUtils.isBlank(metadata.getStringValue(MediaMetadata.TITLE))) {
          // message
          JOptionPane.showMessageDialog(TvShowEpisodeEditorDialog.this, BUNDLE.getString("message.scrape.tvshowepisodefailed")); //$NON-NLS-1$    
        }
        else {
          tfTitle.setText(metadata.getStringValue(MediaMetadata.TITLE));
          taPlot.setText(metadata.getStringValue(MediaMetadata.PLOT));
          spFirstAired.setValue(parseFirstAired(metadata.getStringValue(MediaMetadata.RELEASE_DATE)));

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

      return null;
    }

    private Date parseFirstAired(String aired) {
      try {
        Pattern date = Pattern.compile("([0-9]{2})[_\\.-]([0-9]{2})[_\\.-]([0-9]{4})");
        Matcher m = date.matcher(aired);
        if (m.find()) {
          return new SimpleDateFormat("dd-MM-yyyy").parse(m.group(1) + "-" + m.group(2) + "-" + m.group(3));
        }
        else {
          date = Pattern.compile("([0-9]{4})[_\\.-]([0-9]{2})[_\\.-]([0-9]{2})");
          m = date.matcher(aired);
          if (m.find()) {
            return new SimpleDateFormat("yyyy-MM-dd").parse(m.group(1) + "-" + m.group(2) + "-" + m.group(3));
          }
          else {
            return INITIAL_DATE;
          }
        }
      }
      catch (Exception e) {
        return INITIAL_DATE;
      }
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

  private class RemoveTagAction extends AbstractAction {
    private static final long serialVersionUID = -4799506776650330500L;

    public RemoveTagAction() {
      putValue(SHORT_DESCRIPTION, BUNDLE.getString("tag.remove")); //$NON-NLS-1$
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      String tag = (String) listTags.getSelectedValue();
      tags.remove(tag);
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
