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

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import org.tinymediamanager.core.tvshow.TvShowList;
import org.tinymediamanager.core.tvshow.TvShowModuleManager;
import org.tinymediamanager.scraper.MediaScraper;
import org.tinymediamanager.scraper.entities.MediaLanguages;
import org.tinymediamanager.ui.IconManager;
import org.tinymediamanager.ui.components.ReadOnlyTextArea;
import org.tinymediamanager.ui.components.TmmLabel;
import org.tinymediamanager.ui.components.combobox.MediaScraperCheckComboBox;
import org.tinymediamanager.ui.dialogs.TmmDialog;

import net.miginfocom.swing.MigLayout;

/**
 * The Class TvShowDownloadSubtitleDialog. Download subtitles via file hash
 * 
 * @author Manuel Laggner
 */
public class TvShowDownloadSubtitleDialog extends TmmDialog {
  private static final long         serialVersionUID = 3826984454317879241L;

  private final TvShowList          tvShowList       = TvShowList.getInstance();

  private MediaScraperCheckComboBox cbSubtitleScraper;
  private JComboBox<MediaLanguages> cbLanguage;
  private boolean                   startDownload    = false;

  public TvShowDownloadSubtitleDialog(String title) {
    super(title, "downloadSubtitle");

    {
      JPanel panelCenter = new JPanel();
      getContentPane().add(panelCenter, BorderLayout.CENTER);
      panelCenter.setLayout(new MigLayout("", "[][300lp]", "[][][20lp:n][]"));

      JLabel lblScraper = new TmmLabel(BUNDLE.getString("scraper"));
      panelCenter.add(lblScraper, "cell 0 0");

      cbSubtitleScraper = new MediaScraperCheckComboBox(tvShowList.getAvailableSubtitleScrapers());
      panelCenter.add(cbSubtitleScraper, "cell 1 0,growx");

      JLabel lblLanguage = new TmmLabel(BUNDLE.getString("metatag.language"));
      panelCenter.add(lblLanguage, "cell 0 1");

      cbLanguage = new JComboBox(MediaLanguages.valuesSorted());
      panelCenter.add(cbLanguage, "cell 1 1,growx");

      cbLanguage.setSelectedItem(TvShowModuleManager.SETTINGS.getSubtitleScraperLanguage());

      JTextArea taHint = new ReadOnlyTextArea(BUNDLE.getString("tvshow.download.subtitles.hint"));
      taHint.setOpaque(false);
      panelCenter.add(taHint, "cell 0 3 2 1,growx");
    }
    {
      JButton btnCancel = new JButton(BUNDLE.getString("Button.cancel"));
      btnCancel.setIcon(IconManager.CANCEL_INV);
      btnCancel.addActionListener(e -> {
        startDownload = false;
        setVisible(false);
      });
      addButton(btnCancel);

      JButton btnStart = new JButton(BUNDLE.getString("scraper.start"));
      btnStart.setIcon(IconManager.APPLY_INV);
      btnStart.addActionListener(e -> {
        startDownload = true;
        setVisible(false);
      });
      addDefaultButton(btnStart);
    }
    // set data

    // Subtitle scraper
    List<MediaScraper> selectedSubtitleScrapers = new ArrayList<>();
    for (MediaScraper subtitleScraper : tvShowList.getAvailableSubtitleScrapers()) {
      if (TvShowModuleManager.SETTINGS.getSubtitleScrapers().contains(subtitleScraper.getId())) {
        selectedSubtitleScrapers.add(subtitleScraper);
      }
    }
    if (!selectedSubtitleScrapers.isEmpty()) {
      cbSubtitleScraper.setSelectedItems(selectedSubtitleScrapers);
    }
  }

  /**
   * Get the selected scrapers
   *
   * @return the selected subtitle scrapers
   */
  public List<MediaScraper> getSubtitleScrapers() {
    // scrapers
    return new ArrayList<>(cbSubtitleScraper.getSelectedItems());
  }

  /**
   * Get the selected Language
   *
   * @return the selected language
   */
  public MediaLanguages getLanguage() {
    return (MediaLanguages) cbLanguage.getSelectedItem();
  }

  /**
   * Should start download.
   * 
   * @return true, if successful
   */
  public boolean shouldStartDownload() {
    return startDownload;
  }
}
