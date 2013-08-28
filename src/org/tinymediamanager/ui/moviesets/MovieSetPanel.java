/*
 * Copyright 2012 - 2013 Manuel Laggner
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
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.util.ResourceBundle;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
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
import org.jdesktop.beansbinding.AutoBinding;
import org.jdesktop.beansbinding.AutoBinding.UpdateStrategy;
import org.jdesktop.beansbinding.BeanProperty;
import org.jdesktop.beansbinding.Bindings;
import org.tinymediamanager.core.movie.Movie;
import org.tinymediamanager.core.movie.MovieList;
import org.tinymediamanager.core.movie.MovieSet;
import org.tinymediamanager.ui.TreeUI;
import org.tinymediamanager.ui.UTF8Control;
import org.tinymediamanager.ui.components.ZebraJTree;
import org.tinymediamanager.ui.movies.MovieInformationPanel;
import org.tinymediamanager.ui.movies.MovieSelectionModel;
import org.tinymediamanager.ui.movies.MovieSetTreeCellRenderer;
import org.tinymediamanager.ui.movies.dialogs.MovieEditorDialog;
import org.tinymediamanager.ui.moviesets.dialogs.MovieSetChooserDialog;
import org.tinymediamanager.ui.moviesets.dialogs.MovieSetEditorDialog;
import org.tinymediamanager.ui.tvshows.TvShowPanel;

import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;
import com.jtattoo.plaf.AbstractLookAndFeel;
import com.jtattoo.plaf.JTattooUtilities;

/**
 * The Class MovieSetTreePanel.
 * 
 * @author Manuel Laggner
 */
public class MovieSetPanel extends JPanel {
  private static final long           serialVersionUID     = -7095093579735941697L;
  private static final ResourceBundle BUNDLE               = ResourceBundle.getBundle("messages", new UTF8Control()); //$NON-NLS-1$

  private MovieSelectionModel         movieSelectionModel;
  private MovieSetSelectionModel      movieSetSelectionModel;
  private MovieList                   movieList            = MovieList.getInstance();
  private MovieSetTreeModel           treeModel;
  private int                         width                = 0;

  /**
   * UI elements
   */
  private JSplitPane                  splitPaneHorizontal;
  private JTree                       tree;
  private JLabel                      lblMovieSetCount;

  private final Action                actionAddMovieSet    = new AddMovieSetAction();
  private final Action                actionRemoveMovieSet = new RemoveMovieSetAction();
  private final Action                actionSearchMovieSet = new SearchMovieSetAction();
  private final Action                actionEditMovieSet   = new EditMovieSetAction();

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
    toolBar.setRollover(true);
    toolBar.setFloatable(false);
    toolBar.setOpaque(false);
    panelMovieSetList.add(toolBar, "2, 2");

    JButton btnAddMovieSet = new JButton("");
    btnAddMovieSet.setAction(actionAddMovieSet);
    toolBar.add(btnAddMovieSet);

    JButton btnRemoveMovieSet = new JButton("");
    btnRemoveMovieSet.setAction(actionRemoveMovieSet);
    toolBar.add(btnRemoveMovieSet);

    JButton btnSearchMovieSet = new JButton("");
    btnSearchMovieSet.setAction(actionSearchMovieSet);
    toolBar.add(btnSearchMovieSet);

    JButton btnEditMovieSet = new JButton("");
    btnEditMovieSet.setAction(actionEditMovieSet);
    toolBar.add(btnEditMovieSet);

    JScrollPane scrollPane = new JScrollPane();
    panelMovieSetList.add(scrollPane, "2, 4, fill, fill");

    // tree = new JTree(treeModel);
    tree = new ZebraJTree(treeModel) {
      private static final long serialVersionUID = 1L;

      @Override
      public void paintComponent(Graphics g) {
        width = this.getWidth();
        super.paintComponent(g);
      }
    };

    TreeUI ui = new TreeUI() {
      @Override
      protected void paintRow(Graphics g, Rectangle clipBounds, Insets insets, Rectangle bounds, TreePath path, int row, boolean isExpanded,
          boolean hasBeenExpanded, boolean isLeaf) {
        bounds.width = width - bounds.x;
        super.paintRow(g, clipBounds, insets, bounds, path, row, isExpanded, hasBeenExpanded, isLeaf);
      }

    };
    tree.setUI(ui);

    tree.setRootVisible(false);
    tree.setShowsRootHandles(true);
    tree.setCellRenderer(new MovieSetTreeCellRenderer());
    scrollPane.setViewportView(tree);

    JPanel panelHeader = new JPanel() {
      private static final long serialVersionUID = -6646766582759138262L;

      @Override
      public void paintComponent(Graphics g) {
        super.paintComponent(g);
        JTattooUtilities.fillHorGradient(g, AbstractLookAndFeel.getTheme().getColHeaderColors(), 0, 0, getWidth(), getHeight());
      }
    };
    scrollPane.setColumnHeaderView(panelHeader);
    panelHeader.setLayout(new FormLayout(new ColumnSpec[] { FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("default:grow"),
        FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("center:20px"), ColumnSpec.decode("center:20px"), },
        new RowSpec[] { FormFactory.DEFAULT_ROWSPEC, }));

    JLabel lblMovieSetColumn = new JLabel("Movie set");
    lblMovieSetColumn.setHorizontalAlignment(JLabel.CENTER);
    panelHeader.add(lblMovieSetColumn, "2, 1");

    JLabel lblNfoColumn = new JLabel("");
    lblNfoColumn.setHorizontalAlignment(JLabel.CENTER);
    lblNfoColumn.setIcon(new ImageIcon(TvShowPanel.class.getResource("/org/tinymediamanager/ui/images/Info.png")));
    panelHeader.add(lblNfoColumn, "4, 1");

    JLabel lblImageColumn = new JLabel("");
    lblImageColumn.setHorizontalAlignment(JLabel.CENTER);
    lblImageColumn.setIcon(new ImageIcon(TvShowPanel.class.getResource("/org/tinymediamanager/ui/images/Image.png")));
    panelHeader.add(lblImageColumn, "5, 1");

    final JPanel panelRight = new JPanel();
    splitPaneHorizontal.setRightComponent(panelRight);
    panelRight.setLayout(new CardLayout(0, 0));

    JPanel panelSet = new MovieSetInformationPanel(movieSetSelectionModel);
    panelRight.add(panelSet, "movieSet");

    JPanel panelMovie = new MovieInformationPanel(movieSelectionModel);
    panelRight.add(panelMovie, "movie");

    JPanel panelMovieSetCount = new JPanel();
    add(panelMovieSetCount, "2, 3, left, fill");

    JLabel lblMovieSets = new JLabel("Moviesets:");
    panelMovieSetCount.add(lblMovieSets);

    lblMovieSetCount = new JLabel("0");
    panelMovieSetCount.add(lblMovieSetCount);

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
        else {
          movieSetSelectionModel.setSelectedMovieSet(null);
        }
      }
    });
    // further initializations
    init();
    initDataBindings();
  }

  private void init() {
    // build menu
    buildMenu();

  }

  private void buildMenu() {
    // TODO popup menu for moviesets
    // // popup menu
    // JPopupMenu popupMenu = new JPopupMenu();

  }

  /**
   * The Class AddMovieSetAction.
   * 
   * @author Manuel Laggner
   */
  private class AddMovieSetAction extends AbstractAction {
    private static final long serialVersionUID = 819724436270051906L;

    /**
     * Instantiates a new adds the movie set action.
     */
    public AddMovieSetAction() {
      putValue(LARGE_ICON_KEY, new ImageIcon(getClass().getResource("/org/tinymediamanager/ui/images/Add.png")));
      putValue(SHORT_DESCRIPTION, BUNDLE.getString("movieset.add.desc")); //$NON-NLS-1$
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    @Override
    public void actionPerformed(ActionEvent e) {
      String name = JOptionPane.showInputDialog(null, BUNDLE.getString("movieset.title"), "", 1); //$NON-NLS-1$
      if (StringUtils.isNotEmpty(name)) {
        MovieSet movieSet = new MovieSet(name);
        movieSet.saveToDb();
        movieList.addMovieSet(movieSet);
      }
    }
  }

  /**
   * The Class RemoveMovieSetAction.
   * 
   * @author Manuel Laggner
   */
  private class RemoveMovieSetAction extends AbstractAction {
    private static final long serialVersionUID = -9030996266835702009L;

    /**
     * Instantiates a new removes the movie set action.
     */
    public RemoveMovieSetAction() {
      putValue(LARGE_ICON_KEY, new ImageIcon(getClass().getResource("/org/tinymediamanager/ui/images/Remove.png")));
      putValue(SHORT_DESCRIPTION, BUNDLE.getString("movieset.remove.desc")); //$NON-NLS-1$
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    @Override
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
              movieList.removeMovieSet(movieSet);
            }
          }
        }
      }
    }
  }

  /**
   * The Class SearchMovieSetAction.
   * 
   * @author Manuel Laggner
   */
  private class SearchMovieSetAction extends AbstractAction {
    private static final long serialVersionUID = -2260581786599155278L;

    /**
     * Instantiates a new search movie set action.
     */
    public SearchMovieSetAction() {
      putValue(LARGE_ICON_KEY, new ImageIcon(getClass().getResource("/org/tinymediamanager/ui/images/Search.png")));
      putValue(SHORT_DESCRIPTION, BUNDLE.getString("movieset.search")); //$NON-NLS-1$
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    @Override
    public void actionPerformed(ActionEvent e) {
      TreePath[] paths = tree.getSelectionPaths();
      // tree.clearSelection();

      // filter out all movie sets from the selection
      if (paths != null) {
        for (TreePath path : paths) {
          if (path.getPathCount() > 1) {

            DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
            if (node.getUserObject() instanceof MovieSet) {
              MovieSet movieSet = (MovieSet) node.getUserObject();

              // display movie set chooser
              MovieSetChooserDialog chooser = new MovieSetChooserDialog(movieSet, paths.length > 1 ? true : false);
              if (!chooser.showDialog()) {
                break;
              }
            }
          }
        }
      }
    }
  }

  /**
   * Inits the data bindings.
   */
  protected void initDataBindings() {
    BeanProperty<MovieList, Integer> movieListBeanProperty = BeanProperty.create("movieSetCount");
    BeanProperty<JLabel, String> jLabelBeanProperty = BeanProperty.create("text");
    AutoBinding<MovieList, Integer, JLabel, String> autoBinding = Bindings.createAutoBinding(UpdateStrategy.READ, movieList, movieListBeanProperty,
        lblMovieSetCount, jLabelBeanProperty);
    autoBinding.bind();
  }

  /**
   * The Class EditMovieSetAction.
   * 
   * @author Manuel Laggner
   */
  private class EditMovieSetAction extends AbstractAction {
    private static final long serialVersionUID = 1848573591741154631L;

    /**
     * Instantiates a new edits the movie set action.
     */
    public EditMovieSetAction() {
      putValue(LARGE_ICON_KEY, new ImageIcon(getClass().getResource("/org/tinymediamanager/ui/images/Pencil.png")));
      putValue(SHORT_DESCRIPTION, BUNDLE.getString("movieset.edit")); //$NON-NLS-1$
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    @Override
    public void actionPerformed(ActionEvent e) {
      TreePath[] paths = tree.getSelectionPaths();
      // tree.clearSelection();

      // filter out all movie sets from the selection
      if (paths != null) {
        for (TreePath path : paths) {
          if (path.getPathCount() > 1) {

            DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
            if (node.getUserObject() instanceof MovieSet) {
              MovieSet movieSet = (MovieSet) node.getUserObject();

              // display movie set editor
              MovieSetEditorDialog editor = new MovieSetEditorDialog(movieSet, paths.length > 1 ? true : false);
              if (!editor.showDialog()) {
                break;
              }
            }
            if (node.getUserObject() instanceof Movie) {
              Movie movie = (Movie) node.getUserObject();

              // display movie editor
              MovieEditorDialog editor = new MovieEditorDialog(movie, paths.length > 1 ? true : false);
              if (!editor.showDialog()) {
                break;
              }
            }
          }
        }
      }
    }
  }
}
