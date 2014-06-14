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
package org.tinymediamanager.ui.moviesets;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.util.ResourceBundle;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeCellRenderer;

import org.apache.commons.lang3.StringUtils;
import org.tinymediamanager.core.movie.entities.Movie;
import org.tinymediamanager.core.movie.entities.MovieSet;
import org.tinymediamanager.ui.IconManager;
import org.tinymediamanager.ui.TmmFontHelper;
import org.tinymediamanager.ui.UTF8Control;

import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

/**
 * The Class MovieSetTreeCellRenderer.
 * 
 * @author Manuel Laggner
 */
public class MovieSetTreeCellRenderer implements TreeCellRenderer {
  private static final ResourceBundle BUNDLE             = ResourceBundle.getBundle("messages", new UTF8Control()); //$NON-NLS-1$
  private static final Color          EVEN_ROW_COLOR     = new Color(241, 245, 250);

  private JPanel                      movieSetPanel      = new JPanel();
  private JPanel                      moviePanel         = new JPanel();
  private JLabel                      movieSetTitle      = new JLabel();
  private JLabel                      movieTitle         = new JLabel();
  private JLabel                      movieSetInfo       = new JLabel();
  private JLabel                      movieSetImageLabel = new JLabel();
  private JLabel                      movieNfoLabel      = new JLabel();
  private JLabel                      movieImageLabel    = new JLabel();

  private DefaultTreeCellRenderer     defaultRenderer    = new DefaultTreeCellRenderer();

  public MovieSetTreeCellRenderer() {
    movieSetPanel.setLayout(new FormLayout(new ColumnSpec[] { ColumnSpec.decode("min:grow"), FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
        ColumnSpec.decode("center:20px"), ColumnSpec.decode("center:20px") }, new RowSpec[] { FormFactory.DEFAULT_ROWSPEC,
        FormFactory.DEFAULT_ROWSPEC, }));

    TmmFontHelper.changeFont(movieSetTitle, Font.BOLD);
    movieSetTitle.setHorizontalAlignment(JLabel.LEFT);
    movieSetTitle.setMinimumSize(new Dimension(0, 0));
    movieSetPanel.add(movieSetTitle, "1, 1");

    movieSetPanel.add(movieSetImageLabel, "4, 1, 1, 2");

    TmmFontHelper.changeFont(movieSetInfo, 0.816);
    movieSetInfo.setHorizontalAlignment(JLabel.LEFT);
    movieSetInfo.setMinimumSize(new Dimension(0, 0));
    movieSetPanel.add(movieSetInfo, "1, 2");

    moviePanel.setLayout(new FormLayout(new ColumnSpec[] { ColumnSpec.decode("min:grow"), FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
        ColumnSpec.decode("center:20px"), ColumnSpec.decode("center:20px") }, new RowSpec[] { FormFactory.DEFAULT_ROWSPEC }));
    movieTitle.setMinimumSize(new Dimension(0, 0));
    moviePanel.add(movieTitle, "1, 1");
    moviePanel.add(movieNfoLabel, "3, 1");
    moviePanel.add(movieImageLabel, "4, 1");
  }

  @Override
  public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
    Component returnValue = null;

    // paint movie set node
    if (value != null && value instanceof MovieSetTreeNode) {
      Object userObject = ((MovieSetTreeNode) value).getUserObject();
      if (userObject instanceof MovieSet) {
        MovieSet movieSet = (MovieSet) userObject;

        if (StringUtils.isNotBlank(movieSet.getTitle())) {
          movieSetTitle.setText(movieSet.getTitle());
        }
        else {
          movieSetTitle.setText(BUNDLE.getString("tmm.unknowntitle")); //$NON-NLS-1$
        }
        movieSetInfo.setText(movieSet.getMovies().size() + " Movies");
        movieSetImageLabel.setIcon(movieSet.getHasImages() ? IconManager.CHECKMARK : IconManager.CROSS);

        movieSetPanel.setEnabled(tree.isEnabled());
        movieSetPanel.invalidate();
        returnValue = movieSetPanel;
      }
    }

    // paint movie node
    if (value != null && value instanceof MovieTreeNode) {
      Object userObject = ((MovieTreeNode) value).getUserObject();
      if (userObject instanceof Movie) {
        Movie movie = (Movie) userObject;

        if (StringUtils.isNotBlank(movie.getTitle())) {
          movieTitle.setText(movie.getTitle());
        }
        else {
          movieTitle.setText(BUNDLE.getString("tmm.unknowntitle")); //$NON-NLS-1$
        }
        movieNfoLabel.setIcon(movie.getHasNfoFile() ? IconManager.CHECKMARK : IconManager.CROSS);
        movieImageLabel.setIcon(movie.getHasImages() ? IconManager.CHECKMARK : IconManager.CROSS);

        moviePanel.setEnabled(tree.isEnabled());
        moviePanel.invalidate();
        returnValue = moviePanel;
      }
    }

    if (returnValue == null) {
      returnValue = defaultRenderer.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
    }

    // paint background
    if (selected) {
      returnValue.setBackground(defaultRenderer.getBackgroundSelectionColor());
    }
    else {
      returnValue.setBackground(row % 2 == 0 ? EVEN_ROW_COLOR : Color.WHITE);
      // rendererPanel.setBackground(defaultRenderer.getBackgroundNonSelectionColor());
    }

    return returnValue;
  }
}
