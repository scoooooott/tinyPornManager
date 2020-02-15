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
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Comparator;
import java.util.List;
import java.util.Map.Entry;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JViewport;
import javax.swing.SwingWorker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.core.MediaFileType;
import org.tinymediamanager.core.Message;
import org.tinymediamanager.core.MessageManager;
import org.tinymediamanager.core.tvshow.TvShowEpisodeAndSeasonParser;
import org.tinymediamanager.core.tvshow.TvShowEpisodeSearchAndScrapeOptions;
import org.tinymediamanager.core.tvshow.TvShowModuleManager;
import org.tinymediamanager.core.tvshow.entities.TvShowEpisode;
import org.tinymediamanager.scraper.MediaMetadata;
import org.tinymediamanager.scraper.MediaScraper;
import org.tinymediamanager.scraper.exceptions.MissingIdException;
import org.tinymediamanager.scraper.exceptions.ScrapeException;
import org.tinymediamanager.scraper.interfaces.ITvShowMetadataProvider;
import org.tinymediamanager.ui.IconManager;
import org.tinymediamanager.ui.TmmFontHelper;
import org.tinymediamanager.ui.components.EnhancedTextField;
import org.tinymediamanager.ui.components.ReadOnlyTextArea;
import org.tinymediamanager.ui.components.TmmSplitPane;
import org.tinymediamanager.ui.components.table.TmmTable;
import org.tinymediamanager.ui.dialogs.TmmDialog;
import org.tinymediamanager.ui.tvshows.TvShowEpisodeChooserModel;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.FilterList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.ObservableElementList;
import ca.odell.glazedlists.SortedList;
import ca.odell.glazedlists.TextFilterator;
import ca.odell.glazedlists.gui.TableFormat;
import ca.odell.glazedlists.matchers.MatcherEditor;
import ca.odell.glazedlists.swing.AdvancedTableModel;
import ca.odell.glazedlists.swing.DefaultEventSelectionModel;
import ca.odell.glazedlists.swing.GlazedListsSwing;
import ca.odell.glazedlists.swing.TextComponentMatcherEditor;
import net.miginfocom.swing.MigLayout;

/**
 * The TvShowEpisodeChooserDialog is used for searching a special episode
 * 
 * @author Manuel Laggner
 */
public class TvShowEpisodeChooserDialog extends TmmDialog implements ActionListener {
  private static final long                                serialVersionUID = 3317576458848699068L;
  private static final Logger                              LOGGER           = LoggerFactory.getLogger(TvShowEpisodeChooserDialog.class);

  private TvShowEpisode                                    episode;
  private MediaScraper                                     mediaScraper;
  private MediaMetadata                                    metadata;
  private ObservableElementList<TvShowEpisodeChooserModel> episodeEventList;
  private final List<TvShowEpisodeChooserModel>            selectedEpisodes;
  private final SortedList<TvShowEpisodeChooserModel>      sortedEpisodes;

  private JLabel                                           lblPath;
  private JTable                                           table;
  private JTextArea                                        taPlot;
  private JTextField                                       textField;

  public TvShowEpisodeChooserDialog(TvShowEpisode ep, MediaScraper mediaScraper) {
    super(BUNDLE.getString("tvshowepisode.choose"), "episodeChooser");

    this.episode = ep;
    this.mediaScraper = mediaScraper;
    this.metadata = new MediaMetadata(mediaScraper.getId());
    episodeEventList = new ObservableElementList<>(GlazedLists.threadSafeList(new BasicEventList<>()),
        GlazedLists.beanConnector(TvShowEpisodeChooserModel.class));
    sortedEpisodes = new SortedList<>(GlazedListsSwing.swingThreadProxyList(episodeEventList), new EpisodeComparator());

    {
      final JPanel panelPath = new JPanel();
      panelPath.setLayout(new MigLayout("", "[grow]", "[]"));
      {
        lblPath = new JLabel("");
        TmmFontHelper.changeFont(lblPath, 1.16667, Font.BOLD);
        panelPath.add(lblPath, "cell 0 0, wmin 0");
      }

      setTopIformationPanel(panelPath);
    }

    JPanel contentPanel = new JPanel();
    getContentPane().add(contentPanel, BorderLayout.CENTER);
    contentPanel.setLayout(new MigLayout("", "[700lp,grow]", "[500lp,grow]"));

    {
      JSplitPane splitPane = new TmmSplitPane();
      contentPanel.add(splitPane, "cell 0 0,grow");

      JPanel panelLeft = new JPanel();
      panelLeft.setLayout(new MigLayout("", "[300lp,grow]", "[][400lp,grow]"));

      textField = EnhancedTextField.createSearchTextField();
      panelLeft.add(textField, "cell 0 0, growx");
      textField.setColumns(10);

      JScrollPane scrollPane = new JScrollPane();
      panelLeft.add(scrollPane, "cell 0 1,grow");
      splitPane.setLeftComponent(panelLeft);

      MatcherEditor<TvShowEpisodeChooserModel> textMatcherEditor = new TextComponentMatcherEditor<>(textField,
          new TvShowEpisodeChooserModelFilterator());
      FilterList<TvShowEpisodeChooserModel> textFilteredEpisodes = new FilterList<>(sortedEpisodes, textMatcherEditor);
      AdvancedTableModel<TvShowEpisodeChooserModel> episodeTableModel = GlazedListsSwing.eventTableModelWithThreadProxyList(textFilteredEpisodes,
          new EpisodeTableFormat());
      DefaultEventSelectionModel<TvShowEpisodeChooserModel> selectionModel = new DefaultEventSelectionModel<>(textFilteredEpisodes);
      selectedEpisodes = selectionModel.getSelected();

      selectionModel.addListSelectionListener(e -> {
        if (e.getValueIsAdjusting()) {
          return;
        }
        // display first selected episode
        if (!selectedEpisodes.isEmpty()) {
          TvShowEpisodeChooserModel episode = selectedEpisodes.get(0);
          taPlot.setText(episode.getOverview());
        }
        else {
          taPlot.setText("");
        }
        taPlot.setCaretPosition(0);
      });

      table = new TmmTable(episodeTableModel);
      table.setSelectionModel(selectionModel);
      scrollPane.setViewportView(table);

      JPanel panelRight = new JPanel();
      panelRight.setLayout(new MigLayout("", "[400lp,grow]", "[400lp,grow]"));

      JScrollPane scrollPane_1 = new JScrollPane();
      panelRight.add(scrollPane_1, "cell 0 0,grow");
      splitPane.setRightComponent(panelRight);

      taPlot = new ReadOnlyTextArea();
      scrollPane_1.setViewportView(taPlot);
      splitPane.setDividerLocation(300);
    }
    {

      JButton cancelButton = new JButton(BUNDLE.getString("Button.cancel"));
      cancelButton.setToolTipText(BUNDLE.getString("edit.discard"));
      cancelButton.setIcon(IconManager.CANCEL_INV);
      cancelButton.setActionCommand("Cancel");
      cancelButton.addActionListener(this);
      addButton(cancelButton);

      final JButton okButton = new JButton(BUNDLE.getString("Button.ok"));
      okButton.setToolTipText(BUNDLE.getString("tvshow.change"));
      okButton.setIcon(IconManager.APPLY_INV);
      okButton.setActionCommand("OK");
      okButton.addActionListener(this);
      addDefaultButton(okButton);

      table.addMouseListener(new MouseAdapter() {
        @Override
        public void mouseClicked(MouseEvent e) {
          if (e.getClickCount() >= 2 && !e.isConsumed() && e.getButton() == MouseEvent.BUTTON1) {
            actionPerformed(new ActionEvent(okButton, ActionEvent.ACTION_PERFORMED, "OK"));
          }
        }
      });
    }

    // column widths
    table.getColumnModel().getColumn(0).setMaxWidth(50);
    table.getColumnModel().getColumn(1).setMaxWidth(50);

    SearchTask task = new SearchTask();
    task.execute();

    lblPath.setText(episode.getPathNIO().resolve(episode.getMediaFiles(MediaFileType.VIDEO).get(0).getFilename()).toString());
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    // assign episode
    if ("OK".equals(e.getActionCommand())) {
      if (!selectedEpisodes.isEmpty()) {
        TvShowEpisodeChooserModel episode = selectedEpisodes.get(0);
        if (episode != TvShowEpisodeChooserModel.emptyResult) {
          metadata = episode.getMediaMetadata();
        }

        setVisible(false);
      }
    }

    // cancel
    if ("Cancel".equals(e.getActionCommand())) {
      setVisible(false);
    }
  }

  public MediaMetadata getMetadata() {
    return metadata;
  }

  private class SearchTask extends SwingWorker<Void, Void> {
    @Override
    public Void doInBackground() {
      TvShowEpisodeSearchAndScrapeOptions options = new TvShowEpisodeSearchAndScrapeOptions();
      options.setLanguage(TvShowModuleManager.SETTINGS.getScraperLanguage());

      for (Entry<String, Object> entry : episode.getTvShow().getIds().entrySet()) {
        options.setId(entry.getKey(), entry.getValue().toString());
      }

      try {
        for (MediaMetadata episode : ((ITvShowMetadataProvider) mediaScraper.getMediaProvider()).getEpisodeList(options)) {
          episodeEventList.add(new TvShowEpisodeChooserModel(mediaScraper, episode));
        }
      }
      catch (ScrapeException e) {
        LOGGER.error("searchMovieFallback", e);
        MessageManager.instance.pushMessage(
            new Message(Message.MessageLevel.ERROR, episode, "message.scrape.episodelistfailed", new String[] { ":", e.getLocalizedMessage() }));
      }
      catch (MissingIdException e) {
        LOGGER.warn("missing id for scrape");
        MessageManager.instance.pushMessage(new Message(Message.MessageLevel.ERROR, episode, "scraper.error.missingid"));
      }
      return null;
    }

    @Override
    protected void done() {
      if (textField.getText().isEmpty()) {
        int index = -1;
        // search for a match and preselect it

        // with file name
        for (int i = 0; i < sortedEpisodes.size(); i++) {
          TvShowEpisodeChooserModel model = sortedEpisodes.get(i);
          if (equals(TvShowEpisodeAndSeasonParser.cleanEpisodeTitle(episode.getVideoBasenameWithoutStacking(), episode.getTvShow().getTitle()),
              model.getTitle())) {
            index = i;
            break;
          }
        }

        // with ep title
        if (index == 0) {
          for (int i = 0; i < sortedEpisodes.size(); i++) {
            TvShowEpisodeChooserModel model = sortedEpisodes.get(i);
            if (equals(TvShowEpisodeAndSeasonParser.cleanEpisodeTitle(episode.getTitle(), episode.getTvShow().getTitle()), model.getTitle())) {
              index = i;
              break;
            }
          }
        }

        if (index > -1) {
          // preselect the entry
          table.getSelectionModel().setSelectionInterval(index, index);
          // and scroll it to the top
          scrollToVisible(index, 0);
          // Rectangle rect = table.getCellRect(index, 0, true);
          // table.scrollRectToVisible(rect);
        }
      }
    }

    private boolean equals(String title1, String title2) {
      String cleaned1 = title1.replaceAll("[!?,._-]", " ").replaceAll("\\s+", " ").trim();
      String cleaned2 = title2.replaceAll("[!?,._-]", " ").replaceAll("\\s+", " ").trim();
      return cleaned1.equalsIgnoreCase(cleaned2);
    }

    private void scrollToVisible(int rowIndex, int vColIndex) {
      if (!(table.getParent() instanceof JViewport)) {
        return;
      }

      if (table.getRowCount() < 1) {
        return;
      }

      // view dimension
      Dimension viewportExtentSize = ((JViewport) table.getParent()).getExtentSize();
      Dimension cellDimension = new Dimension(0, 0);

      Rectangle rect = table.getCellRect(rowIndex, vColIndex, true);
      Rectangle rectOne;
      if (rowIndex + 1 < table.getRowCount()) {
        if (vColIndex + 1 < table.getColumnCount()) {
          vColIndex++;
        }
        rectOne = table.getCellRect(rowIndex + 1, vColIndex, true);
        cellDimension.width = rectOne.x - rect.x;
        cellDimension.height = rectOne.y - rect.y;
      }

      rect.setLocation(rect.x + viewportExtentSize.width - cellDimension.width, rect.y + viewportExtentSize.height - cellDimension.height);
      table.scrollRectToVisible(rect);
    }
  }

  private class TvShowEpisodeChooserModelFilterator implements TextFilterator<TvShowEpisodeChooserModel> {
    @Override
    public void getFilterStrings(List<String> baseList, TvShowEpisodeChooserModel model) {
      baseList.add(model.getTitle());
      baseList.add(model.getOverview());
    }
  }

  private class EpisodeComparator implements Comparator<TvShowEpisodeChooserModel> {
    @Override
    public int compare(TvShowEpisodeChooserModel o1, TvShowEpisodeChooserModel o2) {
      if (o1.getSeason() < o2.getSeason()) {
        return -1;
      }

      if (o1.getSeason() > o2.getSeason()) {
        return 1;
      }

      if (o1.getEpisode() < o2.getEpisode()) {
        return -1;
      }

      if (o1.getEpisode() > o2.getEpisode()) {
        return 1;
      }

      return 0;
    }
  }

  private class EpisodeTableFormat implements TableFormat<TvShowEpisodeChooserModel> {
    @Override
    public int getColumnCount() {
      return 3;
    }

    @Override
    public String getColumnName(int column) {
      switch (column) {
        case 0:
          return BUNDLE.getString("metatag.season");

        case 1:
          return BUNDLE.getString("metatag.episode");

        case 2:
          return BUNDLE.getString("metatag.title");
      }
      return null;
    }

    @Override
    public Object getColumnValue(TvShowEpisodeChooserModel baseObject, int column) {
      switch (column) {
        case 0:
          return baseObject.getSeason();

        case 1:
          return baseObject.getEpisode();

        case 2:
          return baseObject.getTitle();
      }
      return null;
    }

  }
}
