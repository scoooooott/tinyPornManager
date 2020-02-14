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
package org.tinymediamanager.ui.movies.dialogs;

import java.awt.BorderLayout;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.apache.commons.lang3.StringUtils;
import org.tinymediamanager.core.MediaSource;
import org.tinymediamanager.core.movie.MovieList;
import org.tinymediamanager.core.movie.MovieModuleManager;
import org.tinymediamanager.ui.IconManager;
import org.tinymediamanager.ui.components.TmmLabel;
import org.tinymediamanager.ui.dialogs.TmmDialog;

import net.miginfocom.swing.MigLayout;

/**
 * The class MovieCreateOfflineDialog.
 * 
 * @author Manuel Laggner
 */
public class MovieCreateOfflineDialog extends TmmDialog {
  private static final long serialVersionUID = -8515248604267318279L;

  private MovieList         movieList        = MovieList.getInstance();

  public MovieCreateOfflineDialog() {
    super(BUNDLE.getString("movie.createoffline"), "movieCreateOffline");

    JPanel panelContent = new JPanel();
    getContentPane().add(panelContent, BorderLayout.CENTER);
    panelContent.setLayout(new MigLayout("", "[][][]", "[][][]"));

    JLabel lblTitle = new TmmLabel(BUNDLE.getString("metatag.title"));
    panelContent.add(lblTitle, "cell 0 0,alignx right");

    final JTextField tfMovieName = new JTextField();
    panelContent.add(tfMovieName, "cell 1 0,growx");
    tfMovieName.setColumns(10);

    JLabel lblMediaSource = new TmmLabel(BUNDLE.getString("metatag.source"));
    panelContent.add(lblMediaSource, "cell 0 1,alignx right");

    final JComboBox<MediaSource> cbMediaSource = new JComboBox();
    cbMediaSource.addItem(MediaSource.UNKNOWN);
    cbMediaSource.addItem(MediaSource.DVD);
    cbMediaSource.addItem(MediaSource.BLURAY);
    cbMediaSource.addItem(MediaSource.HDDVD);
    cbMediaSource.addItem(MediaSource.VHS);
    panelContent.add(cbMediaSource, "cell 1 1,growx");

    JLabel lblDatasource = new TmmLabel(BUNDLE.getString("metatag.datasource"));
    panelContent.add(lblDatasource, "cell 0 2,alignx right");

    final JComboBox<String> cbDatasource = new JComboBox();
    panelContent.add(cbDatasource, "cell 1 2,growx");

    JButton btnAdd = new JButton(IconManager.ADD_INV);
    btnAdd.addActionListener(e -> {
      String title = tfMovieName.getText();
      String datasource = (String) cbDatasource.getSelectedItem();
      MediaSource mediaSource = (MediaSource) cbMediaSource.getSelectedItem();
      if (StringUtils.isNoneBlank(title, datasource)) {
        movieList.addOfflineMovie(title, datasource, mediaSource);
        // message
        String text = BUNDLE.getString("movie.createoffline.created").replaceAll("\\{\\}", title);
        JOptionPane.showMessageDialog(MovieCreateOfflineDialog.this, text); // $NON-NLS-1$
      }
    });
    panelContent.add(btnAdd, "cell 2 0");

    {
      JButton btnClose = new JButton(BUNDLE.getString("Button.close"));
      btnClose.addActionListener(e -> setVisible(false));
      addButton(btnClose);
    }

    {
      // initializations
      for (String datasource : MovieModuleManager.SETTINGS.getMovieDataSource()) {
        cbDatasource.addItem(datasource);
      }
    }
  }
}
