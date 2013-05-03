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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map.Entry;
import java.util.ResourceBundle;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingWorker;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.Globals;
import org.tinymediamanager.core.MediaFile;
import org.tinymediamanager.core.tvshow.TvShowEpisode;
import org.tinymediamanager.core.tvshow.TvShowList;
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
  private static final ResourceBundle BUNDLE           = ResourceBundle.getBundle("messages", new UTF8Control()); //$NON-NLS-1$

  /** The static LOGGER. */
  private static final Logger         LOGGER           = LoggerFactory.getLogger(TvShowChooserDialog.class);

  /** The episode to scrape. */
  private TvShowEpisode               episodeToScrape;

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

  /** The lbl fanart. */
  private ImageLabel                  lblFanart;

  /** The ta plot. */
  private JTextArea                   taPlot;

  /** The tv show list. */
  private TvShowList                  tvShowList;

  /**
   * Instantiates a new tv show episode scrape dialog.
   * 
   * @param episodeToScrape
   *          the episode to scrape
   * @param inQueue
   *          the in queue
   */
  public TvShowEpisodeEditorDialog(TvShowEpisode episode, boolean inQueue) {
    setTitle(BUNDLE.getString("tvshowepisode.scrape")); //$NON-NLS-1$
    setName("tvShowEpisodeScraper");
    TmmWindowSaver.loadSettings(this);
    setBounds(5, 5, 800, 400);
    setIconImage(Globals.logo);
    setModal(true);

    tvShowList = TvShowList.getInstance();
    this.episodeToScrape = episode;
    getContentPane().setLayout(new BorderLayout());
    {
      JPanel contentPanel = new JPanel();
      getContentPane().add(contentPanel, BorderLayout.CENTER);
      contentPanel.setLayout(new FormLayout(new ColumnSpec[] { FormFactory.LABEL_COMPONENT_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC,
          FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("75px"), FormFactory.RELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC,
          FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("150px:grow"), FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("300px:grow"),
          FormFactory.LABEL_COMPONENT_GAP_COLSPEC, }, new RowSpec[] { FormFactory.LINE_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
          FormFactory.NARROW_LINE_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.NARROW_LINE_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
          FormFactory.NARROW_LINE_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.NARROW_LINE_GAP_ROWSPEC, RowSpec.decode("default:grow(2)"),
          FormFactory.RELATED_GAP_ROWSPEC, RowSpec.decode("default:grow"), }));

      lblFilename = new JLabel("");
      contentPanel.add(lblFilename, "2, 2, 3, 1, left, bottom");

      JLabel lblTitle = new JLabel("Title");
      contentPanel.add(lblTitle, "2, 4, right, default");

      tfTitle = new JTextField();
      contentPanel.add(tfTitle, "4, 4, 7, 1");
      tfTitle.setColumns(10);

      JLabel lblSeason = new JLabel("Season");
      contentPanel.add(lblSeason, "2, 6, right, default");

      spSeason = new JSpinner();
      contentPanel.add(spSeason, "4, 6");

      JLabel lblEpisode = new JLabel("Episode");
      contentPanel.add(lblEpisode, "2, 8, right, default");

      spEpisode = new JSpinner();
      contentPanel.add(spEpisode, "4, 8");

      JLabel lblPlot = new JLabel("Plot");
      contentPanel.add(lblPlot, "2, 10, right, top");

      JScrollPane scrollPane = new JScrollPane();
      contentPanel.add(scrollPane, "4, 10, 5, 1, fill, fill");

      taPlot = new JTextArea();
      taPlot.setLineWrap(true);
      taPlot.setWrapStyleWord(true);
      scrollPane.setViewportView(taPlot);

      lblFanart = new ImageLabel();
      contentPanel.add(lblFanart, "10, 6, 1, 5");

    }

    {
      JPanel bottomPanel = new JPanel();
      getContentPane().add(bottomPanel, BorderLayout.SOUTH);

      bottomPanel.setLayout(new FormLayout(new ColumnSpec[] { FormFactory.LABEL_COMPONENT_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC,
          FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("default:grow"), FormFactory.DEFAULT_COLSPEC, }, new RowSpec[] {
          FormFactory.LINE_GAP_ROWSPEC, RowSpec.decode("25px"), }));

      JComboBox cbScraper = new JComboBox();
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
        buttonPane.add(okButton, "1, 1, fill, top");
        okButton.setActionCommand("OK");
        okButton.addActionListener(this);

        JButton cancelButton = new JButton(BUNDLE.getString("Button.cancel")); //$NON-NLS-1$
        buttonPane.add(cancelButton, "3, 1, fill, top");
        cancelButton.setActionCommand("Cancel");
        cancelButton.addActionListener(this);

        if (inQueue) {
          JButton abortButton = new JButton(BUNDLE.getString("Button.abortqueue")); //$NON-NLS-1$
          buttonPane.add(abortButton, "5, 1, fill, top");
          abortButton.setActionCommand("Abort");
          abortButton.addActionListener(this);
        }
      }
    }

    // fill data
    {
      MediaFile mediaFile = episodeToScrape.getMediaFiles().get(0);
      lblFilename.setText(mediaFile.getPath());
      tfTitle.setText(episodeToScrape.getTitle());
      spSeason.setValue(episodeToScrape.getSeason());
      spEpisode.setValue(episodeToScrape.getEpisode());
      lblFanart.setImagePath(episodeToScrape.getFanart());
    }

  }

  /**
   * Shows the dialog and returns whether the work on the queue should be continued.
   * 
   * @return true, if successful
   */
  public boolean showDialog() {
    setVisible(true);
    setLocationRelativeTo(MainWindow.getActiveInstance());
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
      episodeToScrape.setTitle(tfTitle.getText());
      episodeToScrape.setSeason((Integer) spSeason.getValue());
      episodeToScrape.setEpisode((Integer) spEpisode.getValue());
      episodeToScrape.setPlot(taPlot.getText());

      if (!StringUtils.isEmpty(lblFanart.getImageUrl()) && !lblFanart.getImageUrl().equals(episodeToScrape.getFanartUrl())) {
        episodeToScrape.setFanartUrl(lblFanart.getImageUrl());
        episodeToScrape.writeFanartImage();
      }

      episodeToScrape.saveToDb();

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
      for (Entry<String, Object> entry : episodeToScrape.getTvShow().getIds().entrySet()) {
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
            if (ma.getType() == MediaArtworkType.BACKGROUND) {
              lblFanart.setImageUrl(ma.getDefaultUrl());
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
}
