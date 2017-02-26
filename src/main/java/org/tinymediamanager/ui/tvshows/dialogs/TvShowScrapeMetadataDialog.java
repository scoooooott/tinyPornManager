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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ResourceBundle;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;

import org.japura.gui.CheckComboBox;
import org.japura.gui.model.ListCheckModel;
import org.tinymediamanager.Globals;
import org.tinymediamanager.core.tvshow.TvShowList;
import org.tinymediamanager.core.tvshow.TvShowModuleManager;
import org.tinymediamanager.core.tvshow.TvShowScraperMetadataConfig;
import org.tinymediamanager.core.tvshow.TvShowSearchAndScrapeOptions;
import org.tinymediamanager.scraper.MediaScraper;
import org.tinymediamanager.ui.EqualsLayout;
import org.tinymediamanager.ui.IconManager;
import org.tinymediamanager.ui.UTF8Control;
import org.tinymediamanager.ui.components.MediaScraperCheckComboBox;
import org.tinymediamanager.ui.components.MediaScraperComboBox;
import org.tinymediamanager.ui.dialogs.TmmDialog;
import org.tinymediamanager.ui.tvshows.TvShowScraperMetadataPanel;

import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.FormSpecs;
import com.jgoodies.forms.layout.RowSpec;

/**
 * The Class TvShowScrapeMetadataDialog.
 * 
 * @author Manuel Laggner
 */
public class TvShowScrapeMetadataDialog extends TmmDialog {
  private static final long            serialVersionUID            = 6120530120703772160L;
  /**
   * @wbp.nls.resourceBundle messages
   */
  private static final ResourceBundle  BUNDLE                      = ResourceBundle.getBundle("messages", new UTF8Control()); //$NON-NLS-1$

  private TvShowSearchAndScrapeOptions tvShowSearchAndScrapeConfig = new TvShowSearchAndScrapeOptions();
  private boolean                      startScrape                 = true;

  /** UI components */
  private MediaScraperComboBox         cbMetadataScraper;
  private CheckComboBox                cbArtworkScraper;

  public TvShowScrapeMetadataDialog(String title) {
    super(title, "updateMetadata");
    setBounds(5, 5, 533, 280);

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
    panelScraper.setLayout(new FormLayout(
        new ColumnSpec[] { FormSpecs.RELATED_GAP_COLSPEC, FormSpecs.DEFAULT_COLSPEC, FormSpecs.RELATED_GAP_COLSPEC, ColumnSpec.decode("default:grow"),
            FormSpecs.RELATED_GAP_COLSPEC, },
        new RowSpec[] { FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC, FormSpecs.LABEL_COMPONENT_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC,
            FormSpecs.PARAGRAPH_GAP_ROWSPEC, }));

    JLabel lblMetadataScraperT = new JLabel(BUNDLE.getString("scraper.metadata")); //$NON-NLS-1$
    panelScraper.add(lblMetadataScraperT, "2, 2, right, default");

    cbMetadataScraper = new MediaScraperComboBox(TvShowList.getInstance().getAvailableMediaScrapers());
    panelScraper.add(cbMetadataScraper, "4, 2");

    JLabel lblArtworkScraper = new JLabel(BUNDLE.getString("scraper.artwork")); //$NON-NLS-1$
    panelScraper.add(lblArtworkScraper, "2, 4, right, default");

    cbArtworkScraper = new MediaScraperCheckComboBox();
    cbArtworkScraper.setTextFor(CheckComboBox.NONE, BUNDLE.getString("scraper.selected.none")); //$NON-NLS-1$
    cbArtworkScraper.setTextFor(CheckComboBox.MULTIPLE, BUNDLE.getString("scraper.selected.multiple")); //$NON-NLS-1$
    cbArtworkScraper.setTextFor(CheckComboBox.ALL, BUNDLE.getString("scraper.selected.all")); //$NON-NLS-1$
    panelScraper.add(cbArtworkScraper, "4, 4");

    {
      JPanel panelCenter = new JPanel();
      panelContent.add(panelCenter, BorderLayout.CENTER);
      panelCenter.setLayout(new FormLayout(new ColumnSpec[] { FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("default:grow"), },
          new RowSpec[] { FormFactory.RELATED_GAP_ROWSPEC, RowSpec.decode("default:grow"), FormFactory.RELATED_GAP_ROWSPEC, }));

      JPanel panelScraperMetadataSetting = new TvShowScraperMetadataPanel(this.tvShowSearchAndScrapeConfig.getScraperMetadataConfig());
      panelScraperMetadataSetting.setBorder(new TitledBorder(new LineBorder(new Color(184, 207, 229)), BUNDLE.getString("scraper.metadata.select"),
          TitledBorder.LEADING, TitledBorder.TOP, null, null)); // $NON-NLS-1$,
      panelCenter.add(panelScraperMetadataSetting, "2, 2, fill, default");
    }

    JPanel panelButtons = new JPanel();
    panelButtons.setLayout(new EqualsLayout(5));
    panelButtons.setBorder(new EmptyBorder(4, 4, 4, 4));
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
    MediaScraper defaultScraper = TvShowList.getInstance().getDefaultMediaScraper();
    cbMetadataScraper.setSelectedItem(defaultScraper);

    // artwork provider
    ListCheckModel model = cbArtworkScraper.getModel();
    for (MediaScraper artworkScraper : TvShowList.getInstance().getAvailableArtworkScrapers()) {
      model.addElement(artworkScraper);

      if (TvShowModuleManager.SETTINGS.getTvShowArtworkScrapers().contains(artworkScraper.getId())) {
        model.addCheck(artworkScraper);
      }
    }
  }

  /**
   * Pass the tv show search and scrape config to the caller.
   * 
   * @return the tv show search and scrape config
   */
  public TvShowSearchAndScrapeOptions getTvShowSearchAndScrapeConfig() {
    // metadata provider
    tvShowSearchAndScrapeConfig.setMetadataScraper((MediaScraper) cbMetadataScraper.getSelectedItem());

    // artwork scrapers
    ListCheckModel model = cbArtworkScraper.getModel();
    for (Object checked : model.getCheckeds()) {
      if (checked != null && checked instanceof MediaScraper) {
        tvShowSearchAndScrapeConfig.addArtworkScraper((MediaScraper) checked);
      }
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
