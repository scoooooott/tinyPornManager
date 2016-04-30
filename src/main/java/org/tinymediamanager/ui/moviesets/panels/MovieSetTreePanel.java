package org.tinymediamanager.ui.moviesets.panels;

import java.awt.event.ActionEvent;
import java.awt.event.MouseListener;
import java.util.ResourceBundle;

import javax.swing.AbstractAction;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import org.tinymediamanager.core.movie.MovieList;
import org.tinymediamanager.core.movie.entities.Movie;
import org.tinymediamanager.core.movie.entities.MovieSet;
import org.tinymediamanager.ui.ITmmTabItem;
import org.tinymediamanager.ui.ITmmUIModule;
import org.tinymediamanager.ui.IconManager;
import org.tinymediamanager.ui.TmmUIHelper;
import org.tinymediamanager.ui.TreePopupListener;
import org.tinymediamanager.ui.UTF8Control;
import org.tinymediamanager.ui.components.TmmTree;
import org.tinymediamanager.ui.moviesets.MovieSetRootTreeNode;
import org.tinymediamanager.ui.moviesets.MovieSetSelectionModel;
import org.tinymediamanager.ui.moviesets.MovieSetTreeCellRenderer;
import org.tinymediamanager.ui.moviesets.MovieSetTreeModel;
import org.tinymediamanager.ui.moviesets.MovieSetUIModule;
import org.tinymediamanager.ui.moviesets.actions.MovieSetAddAction;
import org.tinymediamanager.ui.moviesets.actions.MovieSetRemoveAction;

import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

public class MovieSetTreePanel extends JPanel implements ITmmTabItem {
  private static final long           serialVersionUID = 5889203009864512935L;
  private static final ResourceBundle BUNDLE           = ResourceBundle.getBundle("messages", new UTF8Control()); //$NON-NLS-1$

  private JTree                       tree;

  private MovieSetTreeModel           treeModel;
  private MovieList                   movieList        = MovieList.getInstance();

  public MovieSetTreePanel(MovieSetSelectionModel movieSetSelectionModel) {
    treeModel = new MovieSetTreeModel(movieList.getMovieSetList());

    setLayout(new FormLayout(
        new ColumnSpec[] { ColumnSpec.decode("10dlu"), ColumnSpec.decode("default:grow"), FormFactory.RELATED_GAP_COLSPEC,
            FormFactory.DEFAULT_COLSPEC, FormFactory.RELATED_GAP_COLSPEC, },
        new RowSpec[] { FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC, RowSpec.decode("3px:grow"), FormFactory.DEFAULT_ROWSPEC, }));

    // final JTextField textField = new JSearchTextField();
    // textField.setColumns(12);
    // textField.getDocument().addDocumentListener(new DocumentListener() {
    // @Override
    // public void insertUpdate(final DocumentEvent e) {
    // applyFilter();
    // }
    //
    // @Override
    // public void removeUpdate(final DocumentEvent e) {
    // applyFilter();
    // }
    //
    // @Override
    // public void changedUpdate(final DocumentEvent e) {
    // applyFilter();
    // }
    //
    // public void applyFilter() {
    // MovieSetTreeModel filteredModel = (MovieSetTreeModel) tree.getModel();
    // if (StringUtils.isNotBlank(textField.getText())) {
    // filteredModel.setFilter(SearchOptions.TEXT, textField.getText());
    // }
    // else {
    // filteredModel.removeFilter(SearchOptions.TEXT);
    // }
    //
    // filteredModel.filter(tree);
    // }
    // });

    // add(textField, "2, 1, fill, fill");

    JToolBar toolBar = new JToolBar();
    toolBar.setRollover(true);
    toolBar.setFloatable(false);
    toolBar.setOpaque(false);
    toolBar.add(new MovieSetAddAction(false));
    toolBar.add(new MovieSetRemoveAction(false));
    add(toolBar, "2, 1, fill, fill");

    JScrollPane scrollPane = new JScrollPane();
    scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
    add(scrollPane, "1, 3, 5, 1, fill, fill");

    tree = new TmmTree(treeModel);
    movieSetSelectionModel.setTree(tree);

    tree.setRootVisible(false);
    tree.setShowsRootHandles(true);
    tree.setCellRenderer(new MovieSetTreeCellRenderer());
    tree.setRowHeight(0);
    scrollPane.setViewportView(tree);

    tree.addTreeSelectionListener(new TreeSelectionListener() {
      @Override
      public void valueChanged(TreeSelectionEvent e) {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
        if (node != null) {
          // click on a movie set
          if (node.getUserObject() instanceof MovieSet) {
            MovieSet movieSet = (MovieSet) node.getUserObject();
            MovieSetUIModule.getInstance().setSelectedMovieSet(movieSet);
          }

          // click on a movie
          if (node.getUserObject() instanceof Movie) {
            Movie movie = (Movie) node.getUserObject();
            MovieSetUIModule.getInstance().setSelectedMovie(movie);
          }
        }
        else {
          // check if there is at least one movie set in the model
          MovieSetRootTreeNode root = (MovieSetRootTreeNode) tree.getModel().getRoot();
          if (root.getChildCount() == 0) {
            // sets an inital show
            MovieSetUIModule.getInstance().setSelectedMovieSet(null);
          }
        }
      }
    });

    scrollPane.setColumnHeaderView(buildHeader());

    // selecting first movie set at startup
    if (movieList.getMovieSetList() != null && movieList.getMovieSetList().size() > 0) {
      SwingUtilities.invokeLater(new Runnable() {
        @Override
        public void run() {
          DefaultMutableTreeNode firstLeaf = (DefaultMutableTreeNode) ((DefaultMutableTreeNode) tree.getModel().getRoot()).getFirstChild();
          tree.setSelectionPath(new TreePath(((DefaultMutableTreeNode) firstLeaf.getParent()).getPath()));
          tree.setSelectionPath(new TreePath(firstLeaf.getPath()));
        }
      });
    }
  }

  /**
   * 
   */
  private JPanel buildHeader() {
    JPanel panelHeader = new JPanel();

    int movieColumnWidth = TmmUIHelper.getColumnWidthForIcon(IconManager.MOVIE);
    int nfoColumnWidth = TmmUIHelper.getColumnWidthForIcon(IconManager.NFO);
    int imageColumnWidth = TmmUIHelper.getColumnWidthForIcon(IconManager.IMAGES);

    panelHeader
        .setLayout(
            new FormLayout(
                new ColumnSpec[] { ColumnSpec.decode("min:grow"), FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
                    ColumnSpec.decode("center:" + movieColumnWidth + "px"), ColumnSpec.decode("center:" + nfoColumnWidth + "px"),
                    ColumnSpec.decode("center:" + imageColumnWidth + "px"), ColumnSpec.decode("1px") },
                new RowSpec[] { FormFactory.DEFAULT_ROWSPEC }));

    JLabel lblMovieSetsColumn = new JLabel(BUNDLE.getString("metatag.movieset")); //$NON-NLS-1$
    lblMovieSetsColumn.setHorizontalAlignment(JLabel.CENTER);
    panelHeader.add(lblMovieSetsColumn, "1, 1");

    JLabel lblMovieCountColumn = new JLabel("");
    lblMovieCountColumn.setHorizontalAlignment(JLabel.CENTER);
    lblMovieCountColumn.setIcon(IconManager.MOVIE);
    lblMovieCountColumn.getInsets().left = 10;
    lblMovieCountColumn.setToolTipText(BUNDLE.getString("movieextendedsearch.movieset"));//$NON-NLS-1$
    panelHeader.add(lblMovieCountColumn, "3, 1");

    JLabel lblNfoColumn = new JLabel("");
    lblNfoColumn.setHorizontalAlignment(JLabel.CENTER);
    lblNfoColumn.setIcon(IconManager.NFO);
    lblNfoColumn.setToolTipText(BUNDLE.getString("metatag.nfo"));//$NON-NLS-1$
    panelHeader.add(lblNfoColumn, "4, 1");

    JLabel lblImageColumn = new JLabel("");
    lblImageColumn.setHorizontalAlignment(JLabel.CENTER);
    lblImageColumn.setIcon(IconManager.IMAGES);
    lblImageColumn.setToolTipText(BUNDLE.getString("metatag.images"));//$NON-NLS-1$
    panelHeader.add(lblImageColumn, "5, 1");

    return panelHeader;
  }

  @Override
  public ITmmUIModule getUIModule() {
    return MovieSetUIModule.getInstance();
  }

  public JTree getTree() {
    return tree;
  }

  public void setPopupMenu(JPopupMenu popupMenu) {
    // add the tree menu entries on the bottom
    popupMenu.addSeparator();
    popupMenu.add(new ExpandAllAction());
    popupMenu.add(new CollapseAllAction());

    MouseListener popupListener = new TreePopupListener(popupMenu, tree);
    tree.addMouseListener(popupListener);
  }

  /**************************************************************************
   * local helper classes
   **************************************************************************/
  public class CollapseAllAction extends AbstractAction {
    private static final long serialVersionUID = -1444530142931061317L;

    public CollapseAllAction() {
      putValue(NAME, BUNDLE.getString("tree.collapseall")); //$NON-NLS-1$
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      for (int i = tree.getRowCount() - 1; i >= 0; i--) {
        tree.collapseRow(i);
      }
    }
  }

  public class ExpandAllAction extends AbstractAction {
    private static final long serialVersionUID = 6191727607109012198L;

    public ExpandAllAction() {
      putValue(NAME, BUNDLE.getString("tree.expandall")); //$NON-NLS-1$
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      int i = 0;
      do {
        tree.expandRow(i++);
      } while (i < tree.getRowCount());
    }
  }
}
