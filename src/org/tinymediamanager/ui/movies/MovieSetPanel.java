/*
 * Copyright 2013 Manuel Laggner
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
package org.tinymediamanager.ui.movies;

import java.awt.CardLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;

import org.tinymediamanager.core.movie.Movie;
import org.tinymediamanager.core.movie.MovieList;
import org.tinymediamanager.core.movie.MovieSet;

import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

/**
 * The Class MovieSetTreePanel.
 */
public class MovieSetPanel extends JPanel {

  /** The split pane horizontal. */
  private JSplitPane splitPaneHorizontal;

  public MovieSetPanel() {
    super();
    setLayout(new FormLayout(
        new ColumnSpec[] { FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("default:grow"), FormFactory.RELATED_GAP_COLSPEC, }, new RowSpec[] {
            FormFactory.RELATED_GAP_ROWSPEC, RowSpec.decode("fill:default:grow"), FormFactory.DEFAULT_ROWSPEC, }));

    splitPaneHorizontal = new JSplitPane();
    splitPaneHorizontal.setContinuousLayout(true);
    add(splitPaneHorizontal, "2, 2, fill, fill");

    JPanel panelMovieSetList = new JPanel();
    splitPaneHorizontal.setLeftComponent(panelMovieSetList);
    panelMovieSetList.setLayout(new FormLayout(new ColumnSpec[] { FormFactory.LABEL_COMPONENT_GAP_COLSPEC, ColumnSpec.decode("74px:grow"), },
        new RowSpec[] { FormFactory.LINE_GAP_ROWSPEC, RowSpec.decode("fill:322px:grow"), }));

    JScrollPane scrollPane = new JScrollPane();
    panelMovieSetList.add(scrollPane, "2, 2, fill, fill");

    // build tree
    MovieList movieList = MovieList.getInstance();
    DefaultMutableTreeNode root = new DefaultMutableTreeNode("MovieSets");
    for (MovieSet movieSet : movieList.getMovieSetList()) {
      DefaultMutableTreeNode setNode = new MovieSetTreeNode(movieSet);
      for (Movie movie : movieSet.getMovies()) {
        DefaultMutableTreeNode movieNode = new MovieSetTreeNode(movie);
        setNode.add(movieNode);
      }
      root.add(setNode);
    }

    final JTree tree = new JTree(root);
    tree.setRootVisible(false);
    tree.setShowsRootHandles(true);
    scrollPane.setViewportView(tree);

    final JPanel panelRight = new JPanel();
    splitPaneHorizontal.setRightComponent(panelRight);
    panelRight.setLayout(new CardLayout(0, 0));

    JPanel panelSet = new JPanel();
    panelRight.add(panelSet, "movieSet");

    JLabel lblNewLabel = new JLabel("Set");
    panelSet.add(lblNewLabel);

    JPanel panelMovie = new JPanel();
    panelRight.add(panelMovie, "movie");

    JLabel lblNewLabel_1 = new JLabel("Movie");
    panelMovie.add(lblNewLabel_1);

    tree.addTreeSelectionListener(new TreeSelectionListener() {
      @Override
      public void valueChanged(TreeSelectionEvent e) {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();

        if (node.getUserObject() instanceof MovieSet) {
          MovieSet movieSet = (MovieSet) node.getUserObject();
          CardLayout cl = (CardLayout) (panelRight.getLayout());
          cl.show(panelRight, "movieSet");
        }
        if (node.getUserObject() instanceof Movie) {
          Movie movie = (Movie) node.getUserObject();
          CardLayout cl = (CardLayout) (panelRight.getLayout());
          cl.show(panelRight, "movie");
        }
      }
    });
  }
}
