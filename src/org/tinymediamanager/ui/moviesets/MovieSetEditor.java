/*
 * Copyright 2012 Manuel Laggner
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
package org.tinymediamanager.ui.moviesets;

import java.awt.BorderLayout;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JTextPane;

import org.tinymediamanager.Globals;
import org.tinymediamanager.core.movie.MovieSet;
import org.tinymediamanager.ui.ImageLabel;

import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

/**
 * The Class MovieSetEditor.
 */
public class MovieSetEditor extends JDialog {

  /** The movie set to edit. */
  private MovieSet   movieSetToEdit;
  private JTextField tfName;
  private JTable     tableMovies;

  private ImageLabel lblPoster;

  private JTextPane  tpOverview;

  /**
   * Instantiates a new movie set editor.
   * 
   * @param movieSet
   *          the movie set
   */
  public MovieSetEditor(MovieSet movieSet) {
    setModal(true);
    setIconImage(Globals.logo);
    setTitle("Edit Movieset");
    setName("movieSetEditor");
    setBounds(5, 5, 700, 500);

    movieSetToEdit = movieSet;

    getContentPane().setLayout(new BorderLayout());

    JPanel panelContent = new JPanel();
    panelContent.setLayout(new FormLayout(new ColumnSpec[] { FormFactory.DEFAULT_COLSPEC, FormFactory.RELATED_GAP_COLSPEC,
        ColumnSpec.decode("default:grow"), FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("250px:grow"), }, new RowSpec[] {
        FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC, RowSpec.decode("default:grow"), FormFactory.RELATED_GAP_ROWSPEC,
        RowSpec.decode("default:grow"), }));
    getContentPane().add(panelContent, BorderLayout.CENTER);

    JLabel lblName = new JLabel("Name");
    panelContent.add(lblName, "1, 1, right, default");

    tfName = new JTextField();
    panelContent.add(tfName, "3, 1, fill, default");
    tfName.setColumns(10);

    lblPoster = new ImageLabel();
    panelContent.add(lblPoster, "5, 1");

    JLabel lblOverview = new JLabel("Overview");
    panelContent.add(lblOverview, "1, 3, right, top");

    JScrollPane scrollPaneOverview = new JScrollPane();
    panelContent.add(scrollPaneOverview, "3, 3, fill, fill");

    tpOverview = new JTextPane();
    scrollPaneOverview.setViewportView(tpOverview);

    JLabel lblMovies = new JLabel("Movies");
    panelContent.add(lblMovies, "1, 5, right, top");

    JScrollPane scrollPaneMovies = new JScrollPane();
    panelContent.add(scrollPaneMovies, "3, 5, fill, fill");

    tableMovies = new JTable();
    scrollPaneMovies.setViewportView(tableMovies);

    /**
     * Button pane
     */
    {
      JPanel buttonPane = new JPanel();
      getContentPane().add(buttonPane, BorderLayout.SOUTH);
      buttonPane.setLayout(new FormLayout(new ColumnSpec[] { ColumnSpec.decode("200px:grow"), ColumnSpec.decode("100px"),
          FormFactory.LABEL_COMPONENT_GAP_COLSPEC, ColumnSpec.decode("100px"), FormFactory.RELATED_GAP_COLSPEC, }, new RowSpec[] {
          FormFactory.LINE_GAP_ROWSPEC, RowSpec.decode("25px"), FormFactory.RELATED_GAP_ROWSPEC, }));
      {
        JButton okButton = new JButton("OK");
        buttonPane.add(okButton, "2, 2, fill, top");
        getRootPane().setDefaultButton(okButton);
      }
      {
        JButton cancelButton = new JButton("Cancel");
        buttonPane.add(cancelButton, "4, 2, fill, top");
      }
    }

    {
      tfName.setText(movieSetToEdit.getName());
      tpOverview.setText(movieSetToEdit.getOverview());
      lblPoster.setImageUrl(movieSetToEdit.getPosterUrl());
    }

  }
}
