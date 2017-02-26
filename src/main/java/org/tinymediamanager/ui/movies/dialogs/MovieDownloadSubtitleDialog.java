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
package org.tinymediamanager.ui.movies.dialogs;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.border.EmptyBorder;

import org.japura.gui.CheckComboBox;
import org.japura.gui.model.ListCheckModel;
import org.tinymediamanager.core.movie.MovieList;
import org.tinymediamanager.core.movie.MovieModuleManager;
import org.tinymediamanager.scraper.MediaScraper;
import org.tinymediamanager.scraper.entities.MediaLanguages;
import org.tinymediamanager.ui.EqualsLayout;
import org.tinymediamanager.ui.IconManager;
import org.tinymediamanager.ui.UTF8Control;
import org.tinymediamanager.ui.components.MediaScraperCheckComboBox;
import org.tinymediamanager.ui.dialogs.TmmDialog;

import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.FormSpecs;
import com.jgoodies.forms.layout.RowSpec;

/**
 * The Class MovieDownloadSubtitleDialog. Download subtitles via file hash
 * 
 * @author Manuel Laggner
 */
public class MovieDownloadSubtitleDialog extends TmmDialog {
  private static final long           serialVersionUID = 3826984454317879241L;
  /** @wbp.nls.resourceBundle messages */
  private static final ResourceBundle BUNDLE           = ResourceBundle.getBundle("messages", new UTF8Control()); //$NON-NLS-1$

  private MediaScraperCheckComboBox   cbSubtitleScraper;
  private JComboBox<MediaLanguages>   cbLanguage;
  private boolean                     startDownload    = false;

  public MovieDownloadSubtitleDialog(String title) {
    super(title, "downloadSubtitle");
    setMinimumSize(new Dimension(getWidth(), getHeight()));

    JPanel panelCenter = new JPanel();
    getContentPane().add(panelCenter, BorderLayout.CENTER);
    panelCenter.setLayout(
        new FormLayout(new ColumnSpec[] { FormSpecs.RELATED_GAP_COLSPEC, ColumnSpec.decode("200dlu:grow"), FormSpecs.RELATED_GAP_COLSPEC, },
            new RowSpec[] { FormSpecs.RELATED_GAP_ROWSPEC, RowSpec.decode("75dlu"), FormSpecs.RELATED_GAP_ROWSPEC, }));

    JPanel panelScraper = new JPanel();
    panelCenter.add(panelScraper, "2, 2, default, fill");
    panelScraper.setLayout(new FormLayout(
        new ColumnSpec[] { FormSpecs.RELATED_GAP_COLSPEC, FormSpecs.DEFAULT_COLSPEC, FormSpecs.RELATED_GAP_COLSPEC, ColumnSpec.decode("default:grow"),
            FormSpecs.RELATED_GAP_COLSPEC, },
        new RowSpec[] { FormSpecs.LABEL_COMPONENT_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC, FormSpecs.LABEL_COMPONENT_GAP_ROWSPEC,
            FormSpecs.DEFAULT_ROWSPEC, FormSpecs.PARAGRAPH_GAP_ROWSPEC, RowSpec.decode("default:grow"), FormSpecs.RELATED_GAP_ROWSPEC, }));

    JLabel lblScraper = new JLabel(BUNDLE.getString("scraper")); //$NON-NLS-1$
    panelScraper.add(lblScraper, "2, 2, right, default");

    cbSubtitleScraper = new MediaScraperCheckComboBox();
    cbSubtitleScraper.setTextFor(CheckComboBox.NONE, BUNDLE.getString("scraper.selected.none")); //$NON-NLS-1$
    cbSubtitleScraper.setTextFor(CheckComboBox.MULTIPLE, BUNDLE.getString("scraper.selected.multiple")); //$NON-NLS-1$
    cbSubtitleScraper.setTextFor(CheckComboBox.ALL, BUNDLE.getString("scraper.selected.all")); //$NON-NLS-1$
    panelScraper.add(cbSubtitleScraper, "4, 2");

    JLabel lblLanguage = new JLabel(BUNDLE.getString("metatag.language")); //$NON-NLS-1$
    panelScraper.add(lblLanguage, "2, 4, right, default");

    cbLanguage = new JComboBox(MediaLanguages.values());
    panelScraper.add(cbLanguage, "4, 4");

    JTextArea taHint = new JTextArea();
    taHint.setLineWrap(true);
    taHint.setWrapStyleWord(true);
    taHint.setText(BUNDLE.getString("movie.download.subtitles.hint")); //$NON-NLS-1$
    taHint.setOpaque(false);
    panelScraper.add(taHint, "2, 6, 3, 1, fill, fill");

    JPanel panelButtons = new JPanel();
    panelButtons.setLayout(new EqualsLayout(5));
    panelButtons.setBorder(new EmptyBorder(4, 4, 4, 4));
    getContentPane().add(panelButtons, BorderLayout.SOUTH);

    JButton btnStart = new JButton(BUNDLE.getString("scraper.start")); //$NON-NLS-1$
    btnStart.setIcon(IconManager.APPLY);
    btnStart.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        startDownload = true;
        setVisible(false);
      }
    });
    panelButtons.add(btnStart);

    JButton btnCancel = new JButton(BUNDLE.getString("Button.cancel")); //$NON-NLS-1$
    btnCancel.setIcon(IconManager.CANCEL);
    btnCancel.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        startDownload = false;
        setVisible(false);
      }
    });
    panelButtons.add(btnCancel);

    // set data

    // scraper
    ListCheckModel model = cbSubtitleScraper.getModel();
    for (MediaScraper subtitleScraper : MovieList.getInstance().getAvailableSubtitleScrapers()) {
      model.addElement(subtitleScraper);

      if (MovieModuleManager.MOVIE_SETTINGS.getMovieSubtitleScrapers().contains(subtitleScraper.getId())) {
        model.addCheck(subtitleScraper);
      }
    }

    cbLanguage.setSelectedItem(MovieModuleManager.MOVIE_SETTINGS.getSubtitleScraperLanguage());
  }

  /**
   * Get the selected scrapers
   * 
   * @return the selected subtitle scrapers
   */
  public List<MediaScraper> getSubtitleScrapers() {
    List<MediaScraper> scrapers = new ArrayList<>();

    // artwork scrapers
    ListCheckModel model = cbSubtitleScraper.getModel();
    for (Object checked : model.getCheckeds()) {
      if (checked != null && checked instanceof MediaScraper) {
        scrapers.add((MediaScraper) checked);
      }
    }

    return scrapers;
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
