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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ResourceBundle;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.apache.commons.lang3.StringUtils;
import org.tinymediamanager.core.MediaSource;
import org.tinymediamanager.core.movie.MovieList;
import org.tinymediamanager.core.movie.MovieModuleManager;
import org.tinymediamanager.ui.EqualsLayout;
import org.tinymediamanager.ui.IconManager;
import org.tinymediamanager.ui.UTF8Control;
import org.tinymediamanager.ui.dialogs.TmmDialog;

import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.FormSpecs;
import com.jgoodies.forms.layout.RowSpec;

/**
 * The class MovieCreateOfflineDialog.
 * 
 * @author Manuel Laggner
 */
public class MovieCreateOfflineDialog extends TmmDialog {
  private static final long           serialVersionUID = -8515248604267318279L;
  /** @wbp.nls.resourceBundle messages */
  private static final ResourceBundle BUNDLE           = ResourceBundle.getBundle("messages", new UTF8Control()); //$NON-NLS-1$

  private MovieList                   movieList        = MovieList.getInstance();

  public MovieCreateOfflineDialog() {
    super(BUNDLE.getString("movie.createoffline"), "movieCreateOffline"); //$NON-NLS-1$
    setBounds(5, 5, 350, 230);
    getContentPane().setLayout(new BorderLayout(0, 0));

    JPanel panelContent = new JPanel();
    getContentPane().add(panelContent, BorderLayout.NORTH);
    panelContent.setLayout(new FormLayout(
        new ColumnSpec[] { FormSpecs.RELATED_GAP_COLSPEC, FormSpecs.DEFAULT_COLSPEC, FormSpecs.RELATED_GAP_COLSPEC, ColumnSpec.decode("100dlu:grow"),
            FormSpecs.RELATED_GAP_COLSPEC, FormSpecs.DEFAULT_COLSPEC, FormSpecs.RELATED_GAP_COLSPEC, },
        new RowSpec[] { FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC, FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC,
            FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC, FormSpecs.PARAGRAPH_GAP_ROWSPEC, }));

    JLabel lblTitle = new JLabel(BUNDLE.getString("metatag.title")); //$NON-NLS-1$
    panelContent.add(lblTitle, "2, 2, right, default");

    final JTextField tfMovieName = new JTextField();
    panelContent.add(tfMovieName, "4, 2, fill, default");
    tfMovieName.setColumns(10);

    JLabel lblMediaSource = new JLabel(BUNDLE.getString("metatag.source")); //$NON-NLS-1$
    panelContent.add(lblMediaSource, "2, 4, right, default");

    final JComboBox<MediaSource> cbMediaSource = new JComboBox();
    cbMediaSource.addItem(MediaSource.UNKNOWN);
    cbMediaSource.addItem(MediaSource.DVD);
    cbMediaSource.addItem(MediaSource.BLURAY);
    cbMediaSource.addItem(MediaSource.HDDVD);
    cbMediaSource.addItem(MediaSource.VHS);
    panelContent.add(cbMediaSource, "4, 4, fill, default");

    JLabel lblDatasource = new JLabel(BUNDLE.getString("metatag.datasource")); //$NON-NLS-1$
    panelContent.add(lblDatasource, "2, 6, right, default");

    final JComboBox<String> cbDatasource = new JComboBox();
    panelContent.add(cbDatasource, "4, 6, fill, default");

    JButton btnAdd = new JButton(IconManager.LIST_ADD);
    btnAdd.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        String title = tfMovieName.getText();
        String datasource = (String) cbDatasource.getSelectedItem();
        MediaSource mediaSource = (MediaSource) cbMediaSource.getSelectedItem();
        if (StringUtils.isNoneBlank(title, datasource)) {
          movieList.addOfflineMovie(title, datasource, mediaSource);
        }
      }
    });
    panelContent.add(btnAdd, "6, 2, right, default");

    {
      JPanel panelBottom = new JPanel();
      getContentPane().add(panelBottom, BorderLayout.SOUTH);
      panelBottom.setLayout(
          new FormLayout(new ColumnSpec[] { FormSpecs.RELATED_GAP_COLSPEC, ColumnSpec.decode("default:grow"), FormSpecs.RELATED_GAP_COLSPEC, },
              new RowSpec[] { FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC, FormSpecs.RELATED_GAP_ROWSPEC, }));

      JPanel panelButtons = new JPanel();
      EqualsLayout layout = new EqualsLayout(5);
      layout.setMinWidth(100);
      panelButtons.setLayout(layout);
      panelBottom.add(panelButtons, "2, 2, right, fill");

      JButton btnClose = new JButton(BUNDLE.getString("Button.close")); //$NON-NLS-1$
      btnClose.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
          setVisible(false);
        }
      });
      panelButtons.add(btnClose);
    }

    {
      // initializations
      for (String datasource : MovieModuleManager.MOVIE_SETTINGS.getMovieDataSource()) {
        cbDatasource.addItem(datasource);
      }
    }
  }
}
