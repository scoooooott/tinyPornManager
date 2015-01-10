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

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.ResourceBundle;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.SwingWorker;

import org.jdesktop.beansbinding.AutoBinding;
import org.jdesktop.beansbinding.AutoBinding.UpdateStrategy;
import org.jdesktop.beansbinding.BeanProperty;
import org.jdesktop.beansbinding.Bindings;
import org.jdesktop.observablecollections.ObservableCollections;
import org.jdesktop.swingbinding.JTableBinding;
import org.jdesktop.swingbinding.SwingBindings;
import org.tinymediamanager.Globals;
import org.tinymediamanager.core.tvshow.entities.TvShowEpisode;
import org.tinymediamanager.scraper.ITvShowMetadataProvider;
import org.tinymediamanager.scraper.MediaEpisode;
import org.tinymediamanager.scraper.MediaScrapeOptions;
import org.tinymediamanager.ui.EqualsLayout;
import org.tinymediamanager.ui.IconManager;
import org.tinymediamanager.ui.UTF8Control;
import org.tinymediamanager.ui.dialogs.TmmDialog;
import org.tinymediamanager.ui.tvshows.TvShowEpisodeChooserModel;

import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

/**
 * The TvShowEpisodeChooserDialog is used for searching a special episode
 * 
 * @author Manuel Laggner
 */
public class TvShowEpisodeChooserDialog extends TmmDialog implements ActionListener {
  private static final long               serialVersionUID = 3317576458848699068L;
  /** @wbp.nls.resourceBundle messages */
  private static final ResourceBundle     BUNDLE           = ResourceBundle.getBundle("messages", new UTF8Control());                         //$NON-NLS-1$

  private TvShowEpisode                   episode;
  private ITvShowMetadataProvider         metadataProvider;
  private MediaEpisode                    metadata;
  private List<TvShowEpisodeChooserModel> episodesFound    = ObservableCollections.observableList(new ArrayList<TvShowEpisodeChooserModel>());
  private JTable                          table;
  private JTextArea                       taPlot;

  public TvShowEpisodeChooserDialog(TvShowEpisode ep, ITvShowMetadataProvider mp) {
    super(BUNDLE.getString("tvshowepisode.choose"), "episodeChooser"); //$NON-NLS-1$
    setBounds(5, 5, 600, 400);

    this.episode = ep;
    this.metadataProvider = mp;
    this.metadata = new MediaEpisode(mp.getProviderInfo().getId());
    getContentPane().setLayout(
        new FormLayout(new ColumnSpec[] { FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("590px:grow"), FormFactory.RELATED_GAP_COLSPEC, },
            new RowSpec[] { FormFactory.RELATED_GAP_ROWSPEC, RowSpec.decode("fill:405px:grow"), FormFactory.RELATED_GAP_ROWSPEC,
                RowSpec.decode("fill:37px"), FormFactory.RELATED_GAP_ROWSPEC, }));
    {

      JSplitPane splitPane = new JSplitPane();
      getContentPane().add(splitPane, "2, 2, fill, fill");

      JScrollPane scrollPane = new JScrollPane();
      scrollPane.setMinimumSize(new Dimension(200, 23));
      splitPane.setLeftComponent(scrollPane);

      table = new JTable();
      scrollPane.setViewportView(table);

      JScrollPane scrollPane_1 = new JScrollPane();
      splitPane.setRightComponent(scrollPane_1);

      taPlot = new JTextArea();
      taPlot.setEditable(false);
      taPlot.setWrapStyleWord(true);
      taPlot.setLineWrap(true);
      scrollPane_1.setViewportView(taPlot);
      splitPane.setDividerLocation(300);

    }
    JPanel bottomPanel = new JPanel();
    getContentPane().add(bottomPanel, "2, 4, fill, top");

    bottomPanel.setLayout(new FormLayout(new ColumnSpec[] { FormFactory.LABEL_COMPONENT_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC,
        FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("default:grow"), FormFactory.DEFAULT_COLSPEC, FormFactory.RELATED_GAP_COLSPEC, },
        new RowSpec[] { FormFactory.LINE_GAP_ROWSPEC, RowSpec.decode("25px"), FormFactory.RELATED_GAP_ROWSPEC, }));

    JPanel buttonPane = new JPanel();
    bottomPanel.add(buttonPane, "5, 2, fill, fill");
    EqualsLayout layout = new EqualsLayout(5);
    layout.setMinWidth(100);
    buttonPane.setLayout(layout);
    final JButton okButton = new JButton(BUNDLE.getString("Button.ok")); //$NON-NLS-1$
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

    initDataBindings();

    // column titles
    table.getColumnModel().getColumn(0).setHeaderValue(BUNDLE.getString("metatag.season")); //$NON-NLS-1$
    table.getColumnModel().getColumn(0).setMaxWidth(50);
    table.getColumnModel().getColumn(1).setHeaderValue(BUNDLE.getString("metatag.episode")); //$NON-NLS-1$
    table.getColumnModel().getColumn(1).setMaxWidth(50);
    table.getColumnModel().getColumn(2).setHeaderValue(BUNDLE.getString("metatag.title")); //$NON-NLS-1$

    SearchTask task = new SearchTask();
    task.execute();

    MouseListener mouseListener = new MouseListener() {
      @Override
      public void mouseReleased(MouseEvent e) {
      }

      @Override
      public void mousePressed(MouseEvent e) {
      }

      @Override
      public void mouseExited(MouseEvent e) {
      }

      @Override
      public void mouseEntered(MouseEvent e) {
      }

      @Override
      public void mouseClicked(MouseEvent e) {
        if (e.getClickCount() >= 2) {
          actionPerformed(new ActionEvent(okButton, ActionEvent.ACTION_PERFORMED, "OK"));
        }
      }
    };
    table.addMouseListener(mouseListener);
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    // assign episode
    if ("OK".equals(e.getActionCommand())) {
      int row = table.getSelectedRow();
      row = table.convertRowIndexToModel(row);
      TvShowEpisodeChooserModel episode = episodesFound.get(row);
      if (episode != TvShowEpisodeChooserModel.emptyResult) {
        metadata = episode.getMediaEpisode();
      }

      setVisible(false);
    }

    // cancel
    if ("Cancel".equals(e.getActionCommand())) {
      setVisible(false);
    }
  }

  public MediaEpisode getMetadata() {
    return metadata;
  }

  private class SearchTask extends SwingWorker<Void, Void> {
    @Override
    public Void doInBackground() {
      MediaScrapeOptions options = new MediaScrapeOptions();
      options.setLanguage(Globals.settings.getTvShowSettings().getScraperLanguage());
      options.setCountry(Globals.settings.getTvShowSettings().getCertificationCountry());
      for (Entry<String, Object> entry : episode.getTvShow().getIds().entrySet()) {
        options.setId(entry.getKey(), entry.getValue().toString());
      }

      try {
        for (MediaEpisode episode : metadataProvider.getEpisodeList(options)) {
          episodesFound.add(new TvShowEpisodeChooserModel(metadataProvider, episode));
        }
      }
      catch (Exception e) {
      }

      return null;
    }
  }

  protected void initDataBindings() {
    JTableBinding<TvShowEpisodeChooserModel, List<TvShowEpisodeChooserModel>, JTable> jTableBinding = SwingBindings.createJTableBinding(
        UpdateStrategy.READ, episodesFound, table);
    //
    BeanProperty<TvShowEpisodeChooserModel, String> tvShowChooserModelBeanProperty = BeanProperty.create("season");
    jTableBinding.addColumnBinding(tvShowChooserModelBeanProperty).setEditable(false);
    //
    BeanProperty<TvShowEpisodeChooserModel, String> tvShowChooserModelBeanProperty_1 = BeanProperty.create("episode");
    jTableBinding.addColumnBinding(tvShowChooserModelBeanProperty_1).setEditable(false);
    //
    BeanProperty<TvShowEpisodeChooserModel, String> tvShowChooserModelBeanProperty_2 = BeanProperty.create("title");
    jTableBinding.addColumnBinding(tvShowChooserModelBeanProperty_2).setEditable(false);
    //
    jTableBinding.bind();
    //
    BeanProperty<JTable, String> jTableBeanProperty = BeanProperty.create("selectedElement.overview");
    BeanProperty<JTextArea, String> jTextAreaBeanProperty = BeanProperty.create("text");
    AutoBinding<JTable, String, JTextArea, String> autoBinding = Bindings.createAutoBinding(UpdateStrategy.READ, table, jTableBeanProperty, taPlot,
        jTextAreaBeanProperty);
    autoBinding.bind();
  }
}
