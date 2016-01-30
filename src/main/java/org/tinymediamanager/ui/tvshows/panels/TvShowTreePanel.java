package org.tinymediamanager.ui.tvshows.panels;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseListener;
import java.util.ResourceBundle;

import javax.swing.AbstractAction;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.JTree;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import org.apache.commons.lang3.StringUtils;
import org.tinymediamanager.core.tvshow.TvShowList;
import org.tinymediamanager.core.tvshow.entities.TvShow;
import org.tinymediamanager.core.tvshow.entities.TvShowEpisode;
import org.tinymediamanager.core.tvshow.entities.TvShowSeason;
import org.tinymediamanager.ui.ITmmTabItem;
import org.tinymediamanager.ui.ITmmUIModule;
import org.tinymediamanager.ui.IconManager;
import org.tinymediamanager.ui.PopupListener;
import org.tinymediamanager.ui.TmmUIHelper;
import org.tinymediamanager.ui.UTF8Control;
import org.tinymediamanager.ui.components.EnhancedTextField;
import org.tinymediamanager.ui.components.TmmTree;
import org.tinymediamanager.ui.tvshows.TvShowExtendedMatcher.SearchOptions;
import org.tinymediamanager.ui.tvshows.TvShowRootTreeNode;
import org.tinymediamanager.ui.tvshows.TvShowSelectionModel;
import org.tinymediamanager.ui.tvshows.TvShowTreeCellRenderer;
import org.tinymediamanager.ui.tvshows.TvShowTreeModel;
import org.tinymediamanager.ui.tvshows.TvShowUIModule;

import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

public class TvShowTreePanel extends JPanel implements ITmmTabItem {
  private static final long           serialVersionUID = 5889203009864512935L;
  /** @wbp.nls.resourceBundle messages */
  private static final ResourceBundle BUNDLE           = ResourceBundle.getBundle("messages", new UTF8Control()); //$NON-NLS-1$

  private JTree                       tree;

  private TvShowTreeModel             treeModel;
  private TvShowSelectionModel        tvShowSelectionModel;
  private TvShowList                  tvShowList       = TvShowList.getInstance();

  public TvShowTreePanel(TvShowSelectionModel selectionModel) {
    this.tvShowSelectionModel = selectionModel;
    treeModel = new TvShowTreeModel(tvShowList.getTvShows());

    setLayout(new FormLayout(
        new ColumnSpec[] { ColumnSpec.decode("10dlu"), ColumnSpec.decode("default:grow"), FormFactory.RELATED_GAP_COLSPEC,
            FormFactory.DEFAULT_COLSPEC, FormFactory.RELATED_GAP_COLSPEC, },
        new RowSpec[] { FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC, RowSpec.decode("3px:grow"), FormFactory.DEFAULT_ROWSPEC, }));

    final JTextField searchField = EnhancedTextField.createSearchTextField();
    searchField.setColumns(12);
    searchField.getDocument().addDocumentListener(new DocumentListener() {
      @Override
      public void insertUpdate(final DocumentEvent e) {
        applyFilter();
      }

      @Override
      public void removeUpdate(final DocumentEvent e) {
        applyFilter();
      }

      @Override
      public void changedUpdate(final DocumentEvent e) {
        applyFilter();
      }

      public void applyFilter() {
        TvShowTreeModel filteredModel = (TvShowTreeModel) tree.getModel();
        if (StringUtils.isNotBlank(searchField.getText())) {
          filteredModel.setFilter(SearchOptions.TEXT, searchField.getText());
        }
        else {
          filteredModel.removeFilter(SearchOptions.TEXT);
        }

        filteredModel.filter(tree);
      }
    });

    add(searchField, "2, 1, fill, fill");

    final JToggleButton btnFilter = new JToggleButton("Filter");
    btnFilter.setToolTipText(BUNDLE.getString("movieextendedsearch.options")); //$NON-NLS-1$
    btnFilter.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        TvShowUIModule.getInstance().setFilterMenuVisible(btnFilter.isSelected());
      }
    });
    add(btnFilter, "4, 1, default, bottom");

    JScrollPane scrollPane = new JScrollPane();
    scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
    add(scrollPane, "1, 3, 5, 1, fill, fill");

    tree = new TmmTree(treeModel);
    tvShowSelectionModel.setTree(tree);

    tree.setRootVisible(false);
    tree.setShowsRootHandles(true);
    tree.setCellRenderer(new TvShowTreeCellRenderer());
    tree.setRowHeight(0);
    scrollPane.setViewportView(tree);

    tree.addTreeSelectionListener(new TreeSelectionListener() {
      @Override
      public void valueChanged(TreeSelectionEvent e) {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
        if (node != null) {
          // click on a tv show
          if (node.getUserObject() instanceof TvShow) {
            TvShow tvShow = (TvShow) node.getUserObject();
            TvShowUIModule.getInstance().setSelectedTvShow(tvShow);
          }

          // click on a season
          if (node.getUserObject() instanceof TvShowSeason) {
            TvShowSeason tvShowSeason = (TvShowSeason) node.getUserObject();
            TvShowUIModule.getInstance().setSelectedTvShowSeason(tvShowSeason);
          }

          // click on an episode
          if (node.getUserObject() instanceof TvShowEpisode) {
            TvShowEpisode tvShowEpisode = (TvShowEpisode) node.getUserObject();
            TvShowUIModule.getInstance().setSelectedTvShowEpisode(tvShowEpisode);
          }
        }
        else {
          // check if there is at least one tv show in the model
          TvShowRootTreeNode root = (TvShowRootTreeNode) tree.getModel().getRoot();
          if (root.getChildCount() == 0) {
            // sets an inital show
            tvShowSelectionModel.setSelectedTvShow(null);
          }
        }
      }
    });

    scrollPane.setColumnHeaderView(buildHeader());

    // selecting first TV show at startup
    if (tvShowList.getTvShows() != null && tvShowList.getTvShows().size() > 0) {
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

    int nfoColumnWidth = TmmUIHelper.getColumnWidthForIcon(IconManager.NFO);
    int imageColumnWidth = TmmUIHelper.getColumnWidthForIcon(IconManager.IMAGES);
    int subtitleColumnWidth = TmmUIHelper.getColumnWidthForIcon(IconManager.SUBTITLES);

    panelHeader.setLayout(new FormLayout(
        new ColumnSpec[] { FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("default:grow"), FormFactory.RELATED_GAP_COLSPEC,
            ColumnSpec.decode("center:31px"), ColumnSpec.decode("center:30px"), ColumnSpec.decode("center:" + nfoColumnWidth + "px"),
            ColumnSpec.decode("center:" + imageColumnWidth + "px"), ColumnSpec.decode("center:" + subtitleColumnWidth + "px") },
        new RowSpec[] { FormFactory.DEFAULT_ROWSPEC, }));

    JLabel lblTvShowsColumn = new JLabel(BUNDLE.getString("metatag.tvshow")); //$NON-NLS-1$
    lblTvShowsColumn.setHorizontalAlignment(JLabel.CENTER);
    panelHeader.add(lblTvShowsColumn, "2, 1");

    JLabel lblSeasonColumn = new JLabel("S");
    lblSeasonColumn.setHorizontalAlignment(JLabel.CENTER);
    panelHeader.add(lblSeasonColumn, "4, 1");

    JLabel lblEpisodesColumn = new JLabel("E");
    lblEpisodesColumn.setHorizontalAlignment(JLabel.CENTER);
    panelHeader.add(lblEpisodesColumn, "5, 1");

    JLabel lblNfoColumn = new JLabel("");
    lblNfoColumn.setHorizontalAlignment(JLabel.CENTER);
    lblNfoColumn.setIcon(IconManager.NFO);
    lblNfoColumn.setToolTipText(BUNDLE.getString("metatag.nfo"));//$NON-NLS-1$
    panelHeader.add(lblNfoColumn, "6, 1");

    JLabel lblImageColumn = new JLabel("");
    lblImageColumn.setHorizontalAlignment(JLabel.CENTER);
    lblImageColumn.setIcon(IconManager.IMAGES);
    lblImageColumn.setToolTipText(BUNDLE.getString("metatag.images"));//$NON-NLS-1$
    panelHeader.add(lblImageColumn, "7, 1");

    JLabel lblSubtitleColumn = new JLabel("");
    lblSubtitleColumn.setHorizontalAlignment(JLabel.CENTER);
    lblSubtitleColumn.setIcon(IconManager.SUBTITLES);
    lblSubtitleColumn.setToolTipText(BUNDLE.getString("metatag.subtitles"));//$NON-NLS-1$
    panelHeader.add(lblSubtitleColumn, "8, 1");

    return panelHeader;
  }

  @Override
  public ITmmUIModule getUIModule() {
    return TvShowUIModule.getInstance();
  }

  public JTree getTree() {
    return tree;
  }

  public void setPopupMenu(JPopupMenu popupMenu) {
    // add the tree menu entries on the bottom
    popupMenu.addSeparator();
    popupMenu.add(new ExpandAllAction());
    popupMenu.add(new CollapseAllAction());

    MouseListener popupListener = new PopupListener(popupMenu, tree);
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
