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
package org.tinymediamanager.ui.moviesets;

import java.awt.CardLayout;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import org.apache.commons.lang3.StringUtils;
import org.tinymediamanager.core.movie.Movie;
import org.tinymediamanager.core.movie.MovieList;
import org.tinymediamanager.core.movie.MovieSet;
import org.tinymediamanager.ui.movies.MovieInformationPanel;
import org.tinymediamanager.ui.movies.MovieSelectionModel;

import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

/**
 * The Class MovieSetTreePanel.
 */
public class MovieSetPanel extends JPanel {

  /** The Constant serialVersionUID. */
  private static final long      serialVersionUID     = 1L;

  /** The split pane horizontal. */
  private JSplitPane             splitPaneHorizontal;

  /** The movie selection model. */
  private MovieSelectionModel    movieSelectionModel;

  /** The movieset selection model. */
  private MovieSetSelectionModel movieSetSelectionModel;

  /** The movie list. */
  private MovieList              movieList            = MovieList.getInstance();

  /** The action add movie set. */
  private final Action           actionAddMovieSet    = new AddMovieSetAction();

  /** The tree. */
  private JTree                  tree;

  /** The tree model. */
  private MovieSetTreeModel      treeModel;

  /** The action remove movie set. */
  private final Action           actionRemoveMovieSet = new RemoveMovieSetAction();
  private final Action           actionSearchMovieSet = new SearchMovieSetAction();

  /**
   * Instantiates a new movie set panel.
   */
  public MovieSetPanel() {
    super();

    movieSelectionModel = new MovieSelectionModel();
    movieSetSelectionModel = new MovieSetSelectionModel();
    treeModel = new MovieSetTreeModel(movieList.getMovieSetList());

    setLayout(new FormLayout(
        new ColumnSpec[] { FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("default:grow"), FormFactory.RELATED_GAP_COLSPEC, }, new RowSpec[] {
            FormFactory.RELATED_GAP_ROWSPEC, RowSpec.decode("fill:default:grow"), FormFactory.DEFAULT_ROWSPEC, }));

    splitPaneHorizontal = new JSplitPane();
    splitPaneHorizontal.setContinuousLayout(true);
    add(splitPaneHorizontal, "2, 2, fill, fill");

    JPanel panelMovieSetList = new JPanel();
    splitPaneHorizontal.setLeftComponent(panelMovieSetList);
    panelMovieSetList.setLayout(new FormLayout(new ColumnSpec[] { FormFactory.LABEL_COMPONENT_GAP_COLSPEC, ColumnSpec.decode("250px:grow"), },
        new RowSpec[] { FormFactory.LINE_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC,
            RowSpec.decode("fill:322px:grow"), }));

    JToolBar toolBar = new JToolBar();
    panelMovieSetList.add(toolBar, "2, 2");

    JButton btnAddMovieSet = new JButton("");
    btnAddMovieSet.setAction(actionAddMovieSet);
    toolBar.add(btnAddMovieSet);

    JButton btnRemoveMovieSet = new JButton("New button");
    btnRemoveMovieSet.setAction(actionRemoveMovieSet);
    toolBar.add(btnRemoveMovieSet);

    JButton btnSearchMovieSet = new JButton("New button");
    btnSearchMovieSet.setAction(actionSearchMovieSet);
    toolBar.add(btnSearchMovieSet);

    JScrollPane scrollPane = new JScrollPane();
    panelMovieSetList.add(scrollPane, "2, 4, fill, fill");

    tree = new JTree(treeModel);
    tree.setRootVisible(false);
    tree.setShowsRootHandles(true);
    scrollPane.setViewportView(tree);

    final JPanel panelRight = new JPanel();
    splitPaneHorizontal.setRightComponent(panelRight);
    panelRight.setLayout(new CardLayout(0, 0));

    JPanel panelSet = new MovieSetInformationPanel(movieSetSelectionModel);
    panelRight.add(panelSet, "movieSet");

    JPanel panelMovie = new MovieInformationPanel(movieSelectionModel);
    panelRight.add(panelMovie, "movie");

    tree.addTreeSelectionListener(new TreeSelectionListener() {
      @Override
      public void valueChanged(TreeSelectionEvent e) {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
        if (node != null) {
          if (node.getUserObject() instanceof MovieSet) {
            MovieSet movieSet = (MovieSet) node.getUserObject();
            movieSetSelectionModel.setSelectedMovieSet(movieSet);
            CardLayout cl = (CardLayout) (panelRight.getLayout());
            cl.show(panelRight, "movieSet");
          }
          if (node.getUserObject() instanceof Movie) {
            Movie movie = (Movie) node.getUserObject();
            movieSelectionModel.setSelectedMovie(movie);
            CardLayout cl = (CardLayout) (panelRight.getLayout());
            cl.show(panelRight, "movie");
          }
        }
      }
    });
  }

  /**
   * The Class AddMovieSetAction.
   */
  private class AddMovieSetAction extends AbstractAction {

    /**
     * Instantiates a new adds the movie set action.
     */
    public AddMovieSetAction() {
      putValue(LARGE_ICON_KEY, new ImageIcon(getClass().getResource("/org/tinymediamanager/ui/images/Add.png")));
      putValue(SHORT_DESCRIPTION, "Add a movie set");
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent e) {
      String name = JOptionPane.showInputDialog(null, "Name of the movie set : ", "", 1);
      if (StringUtils.isNotEmpty(name)) {
        MovieSet movieSet = new MovieSet(name);
        movieSet.saveToDb();
        movieList.addMovieSet(movieSet);
        treeModel.addMovieSet(movieSet);
      }
    }
  }

  /**
   * The Class RemoveMovieSetAction.
   */
  private class RemoveMovieSetAction extends AbstractAction {

    /**
     * Instantiates a new removes the movie set action.
     */
    public RemoveMovieSetAction() {
      putValue(LARGE_ICON_KEY, new ImageIcon(getClass().getResource("/org/tinymediamanager/ui/images/Remove.png")));
      putValue(SHORT_DESCRIPTION, "Remove selected movie set");
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent e) {
      TreePath[] paths = tree.getSelectionPaths();
      tree.clearSelection();
      if (paths != null) {
        for (TreePath path : paths) {
          if (path.getPathCount() > 1) {

            // DefaultMutableTreeNode node = (DefaultMutableTreeNode)
            // path.getLastPathComponent();
            // if (node.getUserObject() instanceof MovieSet) {
            // MovieSet movieSet = (MovieSet) node.getUserObject();
            // System.out.println(movieSet.getName());
            // }
            // if (node.getUserObject() instanceof Movie) {
            // Movie movie = (Movie) node.getUserObject();
            // System.out.println(movie.getName());
            // }
            //
            treeModel.remove(path);

            // tree.addSelectionPath(path.getParentPath());
          }
        }
      }
    }
  }

  private class SearchMovieSetAction extends AbstractAction {
    public SearchMovieSetAction() {
      putValue(NAME, "search");
      putValue(SHORT_DESCRIPTION, "Some short description");
    }

    public void actionPerformed(ActionEvent e) {
      TreePath[] paths = tree.getSelectionPaths();
      tree.clearSelection();

      // filter out all movie sets from the selection
      if (paths != null) {
        for (TreePath path : paths) {
          if (path.getPathCount() > 1) {

            DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
            if (node.getUserObject() instanceof MovieSet) {
              MovieSet movieSet = (MovieSet) node.getUserObject();

              // display movie set chooser
              MovieSetChooser chooser = new MovieSetChooser(movieSet);
              chooser.setVisible(true);
            }
          }
        }
      }

    }
  }
}
