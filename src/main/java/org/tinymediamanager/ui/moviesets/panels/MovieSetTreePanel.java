package org.tinymediamanager.ui.moviesets.panels;

import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.AbstractAction;
import javax.swing.DefaultListSelectionModel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JToggleButton;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.TableColumnModelEvent;
import javax.swing.event.TableColumnModelListener;
import javax.swing.tree.DefaultMutableTreeNode;

import org.tinymediamanager.core.AbstractSettings;
import org.tinymediamanager.core.movie.MovieList;
import org.tinymediamanager.core.movie.MovieModuleManager;
import org.tinymediamanager.core.movie.entities.Movie;
import org.tinymediamanager.core.movie.entities.MovieSet;
import org.tinymediamanager.ui.ITmmTabItem;
import org.tinymediamanager.ui.ITmmUIFilter;
import org.tinymediamanager.ui.ITmmUIModule;
import org.tinymediamanager.ui.TablePopupListener;
import org.tinymediamanager.ui.UTF8Control;
import org.tinymediamanager.ui.components.tree.ITmmTreeFilter;
import org.tinymediamanager.ui.components.tree.TmmTreeNode;
import org.tinymediamanager.ui.components.tree.TmmTreeTextFilter;
import org.tinymediamanager.ui.components.treetable.TmmTreeTable;
import org.tinymediamanager.ui.moviesets.MovieSetSelectionModel;
import org.tinymediamanager.ui.moviesets.MovieSetTableFormat;
import org.tinymediamanager.ui.moviesets.MovieSetTreeDataProvider;
import org.tinymediamanager.ui.moviesets.MovieSetUIModule;
import org.tinymediamanager.ui.moviesets.actions.MovieSetEditAction;

import net.miginfocom.swing.MigLayout;

public class MovieSetTreePanel extends JPanel implements ITmmTabItem {
  private static final long            serialVersionUID = 5889203009864512935L;
  /** @wbp.nls.resourceBundle messages */
  private static final ResourceBundle  BUNDLE           = ResourceBundle.getBundle("messages", new UTF8Control()); //$NON-NLS-1$

  private TmmTreeTable                 tree;

  private final MovieList              movieList        = MovieList.getInstance();
  private final MovieSetSelectionModel selectionModel;

  public MovieSetTreePanel(MovieSetSelectionModel movieSetSelectionModel) {
    selectionModel = movieSetSelectionModel;
    setLayout(new MigLayout("", "[300lp:300lp,grow][fill]", "[][400lp,grow]"));

    final TmmTreeTextFilter<TmmTreeNode> searchField = new TmmTreeTextFilter<>();
    add(searchField, "cell 0 0,growx");

    final JToggleButton btnFilter = new JToggleButton("Filter");
    btnFilter.setToolTipText(BUNDLE.getString("movieextendedsearch.options")); //$NON-NLS-1$
    btnFilter.addActionListener(e -> MovieSetUIModule.getInstance().setFilterMenuVisible(btnFilter.isSelected()));
    add(btnFilter, "cell 1 0");

    tree = new TmmTreeTable(new MovieSetTreeDataProvider(), new MovieSetTableFormat()) {
      @Override
      public void storeFilters() {
        if (MovieModuleManager.SETTINGS.isStoreUiFilters()) {
          List<AbstractSettings.UIFilters> filterValues = new ArrayList<>();
          for (ITmmTreeFilter<TmmTreeNode> filter : treeFilters) {
            if (filter instanceof ITmmUIFilter) {
              ITmmUIFilter uiFilter = (ITmmUIFilter) filter;
              if (uiFilter.getFilterState() != ITmmUIFilter.FilterState.INACTIVE) {
                AbstractSettings.UIFilters uiFilters = new AbstractSettings.UIFilters();
                uiFilters.id = uiFilter.getId();
                uiFilters.state = uiFilter.getFilterState();
                uiFilters.filterValue = uiFilter.getFilterValueAsString();
                filterValues.add(uiFilters);
              }
            }
          }
          MovieModuleManager.SETTINGS.setMovieSetUiFilters(filterValues);
          MovieModuleManager.SETTINGS.saveSettings();
        }
      }
    };

    // restore hidden columns
    tree.readHiddenColumns(MovieModuleManager.SETTINGS.getMovieSetTableHiddenColumns());
    tree.getColumnModel().addColumnModelListener(new TableColumnModelListener() {
      @Override
      public void columnAdded(TableColumnModelEvent e) {
        writeSettings();
      }

      @Override
      public void columnRemoved(TableColumnModelEvent e) {
        writeSettings();
      }

      @Override
      public void columnMoved(TableColumnModelEvent e) {
      }

      @Override
      public void columnMarginChanged(ChangeEvent e) {

      }

      @Override
      public void columnSelectionChanged(ListSelectionEvent e) {
      }

      private void writeSettings() {
        tree.writeHiddenColumns(cols -> {
          MovieModuleManager.SETTINGS.setMovieSetTableHiddenColumns(cols);
          MovieModuleManager.SETTINGS.saveSettings();
        });
      }
    });

    tree.addFilter(searchField);
    JScrollPane scrollPane = new JScrollPane(tree);
    tree.configureScrollPane(scrollPane, new int[] { 0 });
    add(scrollPane, "cell 0 1 2 1,grow");
    tree.adjustColumnPreferredWidths(3);
    movieSetSelectionModel.setTreeTable(tree);

    tree.setRootVisible(false);

    tree.getModel().addTableModelListener(arg0 -> {
      // lblMovieCountFiltered.setText(String.valueOf(movieTableModel.getRowCount()));
      // select first movie set if nothing is selected
      ListSelectionModel selectionModel1 = tree.getSelectionModel();
      if (selectionModel1.isSelectionEmpty() && tree.getModel().getRowCount() > 0) {
        selectionModel1.setSelectionInterval(0, 0);
      }
    });

    tree.getSelectionModel().addListSelectionListener(arg0 -> {
      if (arg0.getValueIsAdjusting() || !(arg0.getSource() instanceof DefaultListSelectionModel)) {
        return;
      }

      int index = ((DefaultListSelectionModel) arg0.getSource()).getMinSelectionIndex();

      DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getValueAt(index, 0);
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
    });

    // selecting first movie set at startup
    if (movieList.getMovieSetList() != null && movieList.getMovieSetList().size() > 0) {
      SwingUtilities.invokeLater(() -> {
        ListSelectionModel selectionModel1 = tree.getSelectionModel();
        if (selectionModel1.isSelectionEmpty() && tree.getModel().getRowCount() > 0) {
          selectionModel1.setSelectionInterval(0, 0);
        }
      });
    }

    // add double click listener
    MouseListener mouseListener = new MouseAdapter() {
      @Override
      public void mousePressed(MouseEvent e) {
        if (e.getClickCount() == 2 && !e.isConsumed() && e.getButton() == MouseEvent.BUTTON1) {
          new MovieSetEditAction().actionPerformed(new ActionEvent(e, 0, ""));
        }
      }
    };
    tree.addMouseListener(mouseListener);
  }

  @Override
  public ITmmUIModule getUIModule() {
    return MovieSetUIModule.getInstance();
  }

  public TmmTreeTable getTreeTable() {
    return tree;
  }

  public void setPopupMenu(JPopupMenu popupMenu) {
    // add the tree menu entries on the bottom
    popupMenu.addSeparator();
    popupMenu.add(new MovieSetTreePanel.ExpandAllAction());
    popupMenu.add(new MovieSetTreePanel.CollapseAllAction());

    tree.addMouseListener(new TablePopupListener(popupMenu, tree));
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
