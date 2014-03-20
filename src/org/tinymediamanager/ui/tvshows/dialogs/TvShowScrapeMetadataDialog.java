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
package org.tinymediamanager.ui.tvshows.dialogs;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ResourceBundle;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;

import org.tinymediamanager.Globals;
import org.tinymediamanager.core.tvshow.TvShowArtworkScrapers;
import org.tinymediamanager.core.tvshow.TvShowScraperMetadataConfig;
import org.tinymediamanager.core.tvshow.TvShowScrapers;
import org.tinymediamanager.core.tvshow.TvShowSearchAndScrapeOptions;
import org.tinymediamanager.ui.EqualsLayout;
import org.tinymediamanager.ui.IconManager;
import org.tinymediamanager.ui.MainWindow;
import org.tinymediamanager.ui.TmmWindowSaver;
import org.tinymediamanager.ui.UTF8Control;
import org.tinymediamanager.ui.tvshows.TvShowScraperMetadataPanel;

import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

/**
 * The Class TvShowScrapeMetadataDialog.
 * 
 * @author Manuel Laggner
 */
public class TvShowScrapeMetadataDialog extends JDialog {
  private static final long            serialVersionUID            = 6120530120703772160L;
  private static final ResourceBundle  BUNDLE                      = ResourceBundle.getBundle("messages", new UTF8Control()); //$NON-NLS-1$

  private TvShowSearchAndScrapeOptions tvShowSearchAndScrapeConfig = new TvShowSearchAndScrapeOptions();
  private boolean                      startScrape                 = true;

  /** UI components */
  private JComboBox                    cbMetadataScraper;
  private JCheckBox                    chckbxTheTVDb;

  /**
   * Instantiates a new movie scrape metadata.
   * 
   * @param title
   *          the title
   */
  public TvShowScrapeMetadataDialog(String title) {
    setTitle(title);
    setName("updateMetadata");
    setBounds(5, 5, 533, 257);
    TmmWindowSaver.loadSettings(this);
    setIconImage(MainWindow.LOGO);
    setModal(true);

    // copy the values
    TvShowScraperMetadataConfig settings = Globals.settings.getTvShowScraperMetadataConfig();

    TvShowScraperMetadataConfig scraperMetadataConfig = new TvShowScraperMetadataConfig();
    scraperMetadataConfig.setTitle(settings.isTitle());
    scraperMetadataConfig.setPlot(settings.isPlot());
    scraperMetadataConfig.setAired(settings.isAired());
    scraperMetadataConfig.setRating(settings.isRating());
    scraperMetadataConfig.setRuntime(settings.isRuntime());
    scraperMetadataConfig.setYear(settings.isYear());
    scraperMetadataConfig.setCertification(settings.isCertification());
    scraperMetadataConfig.setCast(settings.isCast());
    scraperMetadataConfig.setGenres(settings.isGenres());
    scraperMetadataConfig.setArtwork(settings.isArtwork());
    scraperMetadataConfig.setEpisodes(settings.isEpisodes());
    scraperMetadataConfig.setStatus(settings.isStatus());

    tvShowSearchAndScrapeConfig.setScraperMetadataConfig(scraperMetadataConfig);

    JPanel panelContent = new JPanel();
    getContentPane().add(panelContent, BorderLayout.CENTER);
    panelContent.setLayout(new BorderLayout(0, 0));

    JPanel panelScraper = new JPanel();
    panelContent.add(panelScraper, BorderLayout.NORTH);
    panelScraper.setLayout(new FormLayout(new ColumnSpec[] { FormFactory.RELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC,
        FormFactory.RELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC, FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("default:grow"), },
        new RowSpec[] { FormFactory.DEFAULT_ROWSPEC, FormFactory.NARROW_LINE_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, }));

    JLabel lblMetadataScraperT = new JLabel(BUNDLE.getString("scraper.metadata")); //$NON-NLS-1$
    panelScraper.add(lblMetadataScraperT, "2, 1, right, default");

    cbMetadataScraper = new JComboBox(TvShowScrapers.values());
    panelScraper.add(cbMetadataScraper, "4, 1, fill, default");

    JLabel lblArtworkScraper = new JLabel(BUNDLE.getString("scraper.artwork")); //$NON-NLS-1$
    panelScraper.add(lblArtworkScraper, "2, 3, right, default");

    chckbxTheTVDb = new JCheckBox("The TV DB");
    panelScraper.add(chckbxTheTVDb, "4, 3");

    {
      JPanel panelCenter = new JPanel();
      panelContent.add(panelCenter, BorderLayout.CENTER);
      panelCenter.setLayout(new FormLayout(new ColumnSpec[] { FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("default:grow"), }, new RowSpec[] {
          FormFactory.RELATED_GAP_ROWSPEC, RowSpec.decode("default:grow"), FormFactory.RELATED_GAP_ROWSPEC, }));

      JPanel panelScraperMetadataSetting = new TvShowScraperMetadataPanel(this.tvShowSearchAndScrapeConfig.getScraperMetadataConfig());
      panelScraperMetadataSetting.setBorder(new TitledBorder(new LineBorder(new Color(184, 207, 229)), BUNDLE.getString("scraper.metadata.select"),
          TitledBorder.LEADING, TitledBorder.TOP, null, null)); //$NON-NLS-1$,
      panelCenter.add(panelScraperMetadataSetting, "2, 2, fill, default");
    }

    JPanel panelButtons = new JPanel();
    panelButtons.setLayout(new EqualsLayout(5));
    panelContent.add(panelButtons, BorderLayout.SOUTH);

    JButton btnStart = new JButton(BUNDLE.getString("scraper.start")); //$NON-NLS-1$
    btnStart.setIcon(IconManager.APPLY);
    btnStart.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        startScrape = true;
        setVisible(false);
      }
    });
    panelButtons.add(btnStart);

    JButton btnCancel = new JButton(BUNDLE.getString("Button.cancel")); //$NON-NLS-1$
    btnCancel.setIcon(IconManager.CANCEL);
    btnCancel.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        startScrape = false;
        setVisible(false);
      }
    });
    panelButtons.add(btnCancel);

    // set data

    // metadataprovider
    TvShowScrapers defaultScraper = Globals.settings.getTvShowSettings().getTvShowScraper();
    cbMetadataScraper.setSelectedItem(defaultScraper);

    // artwork provider
    chckbxTheTVDb.setSelected(true);
    chckbxTheTVDb.setEnabled(false);
  }

  /**
   * Pass the tv show search and scrape config to the caller.
   * 
   * @return the tv show search and scrape config
   */
  public TvShowSearchAndScrapeOptions getTvShowSearchAndScrapeConfig() {
    // metadata provider
    tvShowSearchAndScrapeConfig.setMetadataScraper((TvShowScrapers) cbMetadataScraper.getSelectedItem());

    // artwork provider
    if (chckbxTheTVDb.isSelected()) {
      tvShowSearchAndScrapeConfig.addArtworkScraper(TvShowArtworkScrapers.TVDB);
    }

    return tvShowSearchAndScrapeConfig;
  }

  /**
   * Should start scrape.
   * 
   * @return true, if successful
   */
  public boolean shouldStartScrape() {
    return startScrape;
  }
}
