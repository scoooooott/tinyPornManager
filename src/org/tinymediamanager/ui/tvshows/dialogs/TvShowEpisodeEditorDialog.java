/*
 * Copyright 2012 - 2013 Manuel Laggner
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
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map.Entry;
import java.util.ResourceBundle;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
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
import org.jdesktop.swingbinding.JTableBinding;
import org.jdesktop.swingbinding.SwingBindings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.Globals;
import org.tinymediamanager.core.MediaFile;
import org.tinymediamanager.core.tvshow.TvShowActor;
import org.tinymediamanager.core.tvshow.TvShowEpisode;
import org.tinymediamanager.core.tvshow.TvShowList;
import org.tinymediamanager.core.tvshow.TvShowScrapers;
import org.tinymediamanager.scraper.IMediaMetadataProvider;
import org.tinymediamanager.scraper.MediaArtwork;
import org.tinymediamanager.scraper.MediaArtwork.MediaArtworkType;
import org.tinymediamanager.scraper.MediaMetadata;
import org.tinymediamanager.scraper.MediaScrapeOptions;
import org.tinymediamanager.scraper.MediaType;
import org.tinymediamanager.ui.EqualsLayout;
import org.tinymediamanager.ui.MainWindow;
import org.tinymediamanager.ui.TmmWindowSaver;
import org.tinymediamanager.ui.UTF8Control;
import org.tinymediamanager.ui.components.ImageLabel;

import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

/**
 * The Class TvShowEpisodeScrapeDialog.
 * 
 * @author Manuel Laggner
 */
public class TvShowEpisodeEditorDialog extends JDialog implements ActionListener {

  /** The Constant serialVersionUID. */
  private static final long           serialVersionUID = 7702248909791283043L;

  /** The Constant BUNDLE. */
  private static final ResourceBundle BUNDLE           = ResourceBundle.getBundle("messages", new UTF8Control());           //$NON-NLS-1$

  /** The static LOGGER. */
  private static final Logger         LOGGER           = LoggerFactory.getLogger(TvShowChooserDialog.class);

  /** The cast. */
  private List<TvShowActor>           cast             = ObservableCollections.observableList(new ArrayList<TvShowActor>());

  /** The episode to edit. */
  private TvShowEpisode               episodeToEdit;

  /** The continue queue. */
  private boolean                     continueQueue    = true;

  /** The tf title. */
  private JTextField                  tfTitle;

  /** The lbl filename. */
  private JLabel                      lblFilename;

  /** The sp episode. */
  private JSpinner                    spEpisode;

  /** The sp season. */
  private JSpinner                    spSeason;

  /** The sp rating. */
  private JSpinner                    spRating;

  /** The sp first aired. */
  private JSpinner                    spFirstAired;

  /** The sp date added. */
  private JSpinner                    spDateAdded;

  /** The chckbx watched. */
  private JCheckBox                   chckbxWatched;

  /** The lbl thumb. */
  private ImageLabel                  lblThumb;

  /** The ta plot. */
  private JTextArea                   taPlot;

  /** The tf director. */
  private JTextField                  tfDirector;

  /** The tf writer. */
  private JTextField                  tfWriter;

  /** The table guests. */
  private JTable                      tableGuests;

  /**
   * Instantiates a new tv show episode scrape dialog.
   * 
   * @param episode
   *          the episode
   * @param inQueue
   *          the in queue
   */
  public TvShowEpisodeEditorDialog(TvShowEpisode episode, boolean inQueue) {
    setTitle(BUNDLE.getString("tvshowepisode.scrape")); //$NON-NLS-1$
    setName("tvShowEpisodeScraper");
    TmmWindowSaver.loadSettings(this);
    setBounds(5, 5, 800, 500);
    setIconImage(Globals.logo);
    setModal(true);

    this.episodeToEdit = episode;
    getContentPane().setLayout(new BorderLayout());
    {
      JPanel contentPanel = new JPanel();
      getContentPane().add(contentPanel, BorderLayout.CENTER);
      contentPanel.setLayout(new FormLayout(new ColumnSpec[] { FormFactory.LABEL_COMPONENT_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC,
          FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("100px"), FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("default:grow"),
          FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("default:grow"), FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("100px"),
          FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("300px:grow"), FormFactory.LABEL_COMPONENT_GAP_COLSPEC, }, new RowSpec[] {
          FormFactory.LINE_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
          FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
          FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC, RowSpec.decode("default:grow"),
          FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
          FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
          FormFactory.RELATED_GAP_ROWSPEC, RowSpec.decode("default:grow"), FormFactory.RELATED_GAP_ROWSPEC, }));

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
      contentPanel.add(lblEpisode, "8, 6, right, default");

      spEpisode = new JSpinner();
      contentPanel.add(spEpisode, "10, 6");

      JLabel lblRating = new JLabel(BUNDLE.getString("metatag.rating")); //$NON-NLS-1$
      contentPanel.add(lblRating, "2, 8, right, default");

      spRating = new JSpinner();
      contentPanel.add(spRating, "4, 8");

      JLabel lblFirstAired = new JLabel(BUNDLE.getString("metatag.aired")); //$NON-NLS-1$
      contentPanel.add(lblFirstAired, "8, 8, right, default");

      spFirstAired = new JSpinner(new SpinnerDateModel());
      contentPanel.add(spFirstAired, "10, 8");

      JLabel lblWatched = new JLabel(BUNDLE.getString("metatag.watched")); //$NON-NLS-1$
      contentPanel.add(lblWatched, "2, 10, right, default");

      chckbxWatched = new JCheckBox("");
      contentPanel.add(chckbxWatched, "4, 10");

      JLabel lblDateAdded = new JLabel(BUNDLE.getString("metatag.dateadded")); //$NON-NLS-1$
      contentPanel.add(lblDateAdded, "8, 10, right, default");

      spDateAdded = new JSpinner(new SpinnerDateModel());
      contentPanel.add(spDateAdded, "10, 10");

      JLabel lblPlot = new JLabel(BUNDLE.getString("metatag.plot")); //$NON-NLS-1$
      contentPanel.add(lblPlot, "2, 12, right, top");

      JScrollPane scrollPane = new JScrollPane();
      contentPanel.add(scrollPane, "4, 12, 7, 1, fill, fill");

      taPlot = new JTextArea();
      taPlot.setLineWrap(true);
      taPlot.setWrapStyleWord(true);
      scrollPane.setViewportView(taPlot);

      lblThumb = new ImageLabel();
      lblThumb.setAlternativeText(BUNDLE.getString("image.notfound.thumb")); //$NON-NLS-1$
      contentPanel.add(lblThumb, "12, 6, 1, 11");

      JLabel lblDirector = new JLabel(BUNDLE.getString("metatag.director")); //$NON-NLS-1$
      contentPanel.add(lblDirector, "2, 14, right, default");

      tfDirector = new JTextField();
      tfDirector.setText((String) null);
      tfDirector.setColumns(10);
      contentPanel.add(tfDirector, "4, 14, 7, 1, fill, default");

      JLabel lblWriter = new JLabel(BUNDLE.getString("metatag.writer")); //$NON-NLS-1$
      contentPanel.add(lblWriter, "2, 16, right, default");

      tfWriter = new JTextField();
      tfWriter.setText((String) null);
      tfWriter.setColumns(10);
      contentPanel.add(tfWriter, "4, 16, 7, 1, fill, default");

      JLabel lblGuests = new JLabel(BUNDLE.getString("metatag.guests")); //$NON-NLS-1$
      contentPanel.add(lblGuests, "2, 18, right, top");

      JScrollPane scrollPaneGuests = new JScrollPane();
      contentPanel.add(scrollPaneGuests, "4, 18, 7, 5, fill, fill");

      tableGuests = new JTable();
      scrollPaneGuests.setViewportView(tableGuests);

      JButton btnAddActor = new JButton("");
      btnAddActor.setIcon(new ImageIcon(TvShowEpisodeEditorDialog.class.getResource("/org/tinymediamanager/ui/images/Add.png")));
      btnAddActor.setMargin(new Insets(2, 2, 2, 2));
      contentPanel.add(btnAddActor, "2, 20, right, top");

      JButton btnRemoveActor = new JButton("");
      btnRemoveActor.setIcon(new ImageIcon(TvShowEpisodeEditorDialog.class.getResource("/org/tinymediamanager/ui/images/Remove.png")));
      btnRemoveActor.setMargin(new Insets(2, 2, 2, 2));
      contentPanel.add(btnRemoveActor, "2, 22, right, top");

    }

    {
      JPanel bottomPanel = new JPanel();
      getContentPane().add(bottomPanel, BorderLayout.SOUTH);

      bottomPanel.setLayout(new FormLayout(new ColumnSpec[] { FormFactory.LABEL_COMPONENT_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC,
          FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("default:grow"), FormFactory.DEFAULT_COLSPEC, }, new RowSpec[] {
          FormFactory.LINE_GAP_ROWSPEC, RowSpec.decode("25px"), }));

      JComboBox cbScraper = new JComboBox(TvShowScrapers.values());
      cbScraper.setSelectedItem(Globals.settings.getTvShowSettings().getTvShowScraper());
      bottomPanel.add(cbScraper, "2, 2, fill, default");

      JButton btnScrape = new JButton(BUNDLE.getString("Button.scrape")); //$NON-NLS-1$
      btnScrape.setPreferredSize(new Dimension(100, 23));
      btnScrape.setMaximumSize(new Dimension(0, 0));
      btnScrape.setMinimumSize(new Dimension(100, 23));
      btnScrape.setActionCommand("Scrape");
      btnScrape.addActionListener(this);
      bottomPanel.add(btnScrape, "4, 2, left, default");
      {
        JPanel buttonPane = new JPanel();
        bottomPanel.add(buttonPane, "5, 2, fill, fill");
        EqualsLayout layout = new EqualsLayout(5);
        layout.setMinWidth(100);
        buttonPane.setLayout(layout);
        JButton okButton = new JButton(BUNDLE.getString("Button.ok")); //$NON-NLS-1$
        okButton.setToolTipText(BUNDLE.getString("tvshow.change"));
        buttonPane.add(okButton);
        okButton.setActionCommand("OK");
        okButton.addActionListener(this);

        JButton cancelButton = new JButton(BUNDLE.getString("Button.cancel")); //$NON-NLS-1$
        cancelButton.setToolTipText(BUNDLE.getString("edit.discard"));
        buttonPane.add(cancelButton);
        cancelButton.setActionCommand("Cancel");
        cancelButton.addActionListener(this);

        if (inQueue) {
          JButton abortButton = new JButton(BUNDLE.getString("Button.abortqueue")); //$NON-NLS-1$
          abortButton.setToolTipText(BUNDLE.getString("tvshow.edit.abortqueue.desc")); //$NON-NLS-1$
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

      spSeason.setModel(new SpinnerNumberModel(episodeToEdit.getSeason(), Integer.MIN_VALUE, Integer.MAX_VALUE, 1));
      spEpisode.setModel(new SpinnerNumberModel(episodeToEdit.getEpisode(), Integer.MIN_VALUE, Integer.MAX_VALUE, 1));

      SimpleDateFormat dateFormat = (SimpleDateFormat) DateFormat.getDateInstance(DateFormat.MEDIUM);
      // spDateAdded.setEditor(new JSpinner.DateEditor(spDateAdded, dateFormat.toPattern()));
      spFirstAired.setEditor(new JSpinner.DateEditor(spFirstAired, dateFormat.toPattern()));

      spDateAdded.setValue(episodeToEdit.getDateAdded());
      if (episodeToEdit.getFirstAired() != null) {
        spFirstAired.setValue(episodeToEdit.getFirstAired());
      }
      else {
        spFirstAired.setValue(new Date(0));
      }
      lblThumb.setImagePath(episodeToEdit.getThumb());
      spRating.setModel(new SpinnerNumberModel(episodeToEdit.getRating(), 0.0, 10.0, 0.1));
      chckbxWatched.setSelected(episodeToEdit.isWatched());
      taPlot.setText(episodeToEdit.getPlot());
      taPlot.setCaretPosition(0);
      tfDirector.setText(episodeToEdit.getDirector());
      tfWriter.setText(episodeToEdit.getWriter());

      for (TvShowActor origCast : episodeToEdit.getActors()) {
        TvShowActor actor = new TvShowActor();
        actor.setName(origCast.getName());
        actor.setCharacter(origCast.getCharacter());
        actor.setThumb(origCast.getThumb());
        cast.add(actor);
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
    setLocationRelativeTo(MainWindow.getActiveInstance());
    setVisible(true);
    return continueQueue;
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
   */
  /**
   * Action performed.
   * 
   * @param e
   *          the e
   */
  public void actionPerformed(ActionEvent e) {
    // assign scraped data
    if ("OK".equals(e.getActionCommand())) {
      episodeToEdit.setTitle(tfTitle.getText());
      episodeToEdit.setSeason((Integer) spSeason.getValue());
      episodeToEdit.setEpisode((Integer) spEpisode.getValue());
      episodeToEdit.setPlot(taPlot.getText());

      double tempRating = (Double) spRating.getValue();
      float rating = (float) tempRating;
      if (episodeToEdit.getRating() != rating) {
        episodeToEdit.setRating(rating);
        episodeToEdit.setVotes(1);
      }

      episodeToEdit.setDateAdded((Date) spDateAdded.getValue());
      episodeToEdit.setFirstAired((Date) spFirstAired.getValue());
      episodeToEdit.setWatched(chckbxWatched.isSelected());
      episodeToEdit.setDirector(tfDirector.getText());
      episodeToEdit.setWriter(tfWriter.getText());
      episodeToEdit.setActors(cast);

      if (StringUtils.isNotEmpty(lblThumb.getImageUrl()) && !lblThumb.getImageUrl().equals(episodeToEdit.getThumbUrl())) {
        episodeToEdit.setThumbUrl(lblThumb.getImageUrl());
        episodeToEdit.writeThumbImage();
      }

      episodeToEdit.saveToDb();

      this.setVisible(false);
      dispose();
    }

    // cancel
    if ("Cancel".equals(e.getActionCommand())) {
      this.setVisible(false);
      dispose();
    }

    // Abort queue
    if ("Abort".equals(e.getActionCommand())) {
      continueQueue = false;
      this.setVisible(false);
      dispose();
    }

    // scrape
    if ("Scrape".equals(e.getActionCommand())) {
      ScrapeTask task = new ScrapeTask(TvShowList.getInstance().getMetadataProvider());
      task.execute();
    }
  }

  /**
   * The Class ScrapeTask.
   * 
   * @author Manuel Laggner
   */
  private class ScrapeTask extends SwingWorker<Void, Void> {

    /** The mp. */
    IMediaMetadataProvider mp;

    /**
     * Instantiates a new scrape task.
     * 
     * @param mp
     *          the mp
     */
    public ScrapeTask(IMediaMetadataProvider mp) {
      this.mp = mp;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.swing.SwingWorker#doInBackground()
     */
    @Override
    protected Void doInBackground() throws Exception {
      MediaScrapeOptions options = new MediaScrapeOptions();
      for (Entry<String, Object> entry : episodeToEdit.getTvShow().getIds().entrySet()) {
        options.setId(entry.getKey(), entry.getValue().toString());
      }

      options.setType(MediaType.TV_EPISODE);
      options.setId("seasonNr", spSeason.getValue().toString());
      options.setId("episodeNr", spEpisode.getValue().toString());

      try {
        MediaMetadata metadata = mp.getMetadata(options);
        if (StringUtils.isNotBlank(metadata.getTitle())) {
          tfTitle.setText(metadata.getTitle());
          taPlot.setText(metadata.getPlot());
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

  }

  protected void initDataBindings() {
    JTableBinding<TvShowActor, List<TvShowActor>, JTable> jTableBinding = SwingBindings.createJTableBinding(UpdateStrategy.READ, cast, tableGuests);
    //
    BeanProperty<TvShowActor, String> movieCastBeanProperty = BeanProperty.create("name");
    jTableBinding.addColumnBinding(movieCastBeanProperty);
    //
    BeanProperty<TvShowActor, String> movieCastBeanProperty_1 = BeanProperty.create("character");
    jTableBinding.addColumnBinding(movieCastBeanProperty_1);
    //
    jTableBinding.bind();
  }
}
