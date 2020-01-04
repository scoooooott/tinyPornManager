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
package org.tinymediamanager.ui.components.treetable;

import java.awt.Component;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.border.Border;
import javax.swing.event.TreeModelEvent;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;
import javax.swing.tree.AbstractLayoutCache;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import org.apache.commons.lang3.StringUtils;
import org.tinymediamanager.core.AbstractSettings;
import org.tinymediamanager.ui.ITmmUIFilter;
import org.tinymediamanager.ui.components.table.TmmTable;
import org.tinymediamanager.ui.components.table.TmmTableFormat;
import org.tinymediamanager.ui.components.tree.ITmmTreeFilter;
import org.tinymediamanager.ui.components.tree.TmmTreeDataProvider;
import org.tinymediamanager.ui.components.tree.TmmTreeModel;
import org.tinymediamanager.ui.components.tree.TmmTreeNode;
import org.tinymediamanager.ui.tvshows.TvShowTreeCellRenderer;

/**
 * The class TmmTreeTable provides a combination of a tree and a table
 * 
 * @author Manuel Laggner
 */
public class TmmTreeTable extends TmmTable {

  protected TmmTreeTableRenderDataProvider   renderDataProvider = null;
  protected int                              selectedRow        = -1;
  protected Boolean                          cachedRootVisible  = true;
  protected Set<ITmmTreeFilter<TmmTreeNode>> treeFilters;
  protected ITmmTreeTableModel               treeTableModel;
  protected PropertyChangeListener           filterChangeListener;

  private int[]                              lastEditPosition;

  public TmmTreeTable(TmmTreeDataProvider<? extends TmmTreeNode> dataProvider, TmmTableFormat tableFormat) {
    treeFilters = new CopyOnWriteArraySet<>();
    treeTableModel = new TmmTreeTableModel(new TmmTreeModelConnector<>(dataProvider), tableFormat);
    ((TmmTreeModel) treeTableModel.getTreeModel()).getDataProvider().setTreeFilters(treeFilters);
    filterChangeListener = evt -> updateFiltering();
    setModel(treeTableModel);
    initTreeTable();
  }

  @Override
  public void addColumn(TableColumn aColumn) {
    if (aColumn.getIdentifier() == null && getModel() instanceof TmmTreeTableModel) {
      // disable grid in header
      aColumn.setHeaderRenderer(new BottomBorderHeaderRenderer());

      TmmTreeTableModel tableModel = ((TmmTreeTableModel) getModel());
      tableModel.setUpColumn(aColumn);
    }
    super.addColumn(aColumn);
  }

  protected void initTreeTable() {
    getColumnModel().getColumn(0).setCellRenderer(new TvShowTreeCellRenderer());
    getSelectionModel().addListSelectionListener(e -> {
      if (getSelectedRowCount() == 1) {
        selectedRow = getSelectedRow();
      }
      else {
        selectedRow = -1;
      }
    });

    setTableHeader(createTableHeader());
    getTableHeader().setReorderingAllowed(false);
    getTableHeader().setOpaque(false);
    setOpaque(false);
    // turn off grid painting as we'll handle this manually in order to paint grid lines over the entire viewport.
    setShowGrid(false);
  }

  @Override
  public TableCellRenderer getCellRenderer(int row, int column) {
    int c = convertColumnIndexToModel(column);
    TableCellRenderer result;
    if (c == 0) {
      TableColumn tableColumn = getColumnModel().getColumn(column);
      TableCellRenderer renderer = tableColumn.getCellRenderer();
      if (renderer == null) {
        result = getDefaultRenderer(Object.class);
      }
      else {
        result = renderer;
      }
    }
    else {
      result = super.getCellRenderer(row, column);
    }
    return result;
  }

  /**
   * Get the RenderDataProvider which is providing text, icons and tooltips for items in the tree column. The default property for this value is null,
   * in which case standard JTable/JTree object -> icon/string conventions are used
   */
  public TmmTreeTableRenderDataProvider getRenderDataProvider() {
    return renderDataProvider;
  }

  /**
   * Set the RenderDataProvider which will provide text, icons and tooltips for items in the tree column. The default is null. If null, the data
   * displayed will be generated in the standard JTable/JTree way - calling <code>toString()</code> on objects in the tree model and using the look
   * and feel's default tree folder and tree leaf icons.
   */
  public void setRenderDataProvider(TmmTreeTableRenderDataProvider provider) {
    if (provider != renderDataProvider) {
      TmmTreeTableRenderDataProvider old = renderDataProvider;
      renderDataProvider = provider;
      firePropertyChange("renderDataProvider", old, provider);
    }
  }

  /**
   * Get the TreePathSupport object which manages path expansion for this Treetable
   */
  TmmTreeTableTreePathSupport getTreePathSupport() {
    TmmTreeTableModel mdl = getTreeTableModel();
    if (mdl != null) {
      return mdl.getTreePathSupport();
    }
    else {
      return null;
    }
  }

  public TmmTreeTableModel getTreeTableModel() {
    TableModel mdl = getModel();
    if (mdl instanceof TmmTreeTableModel) {
      return (TmmTreeTableModel) getModel();
    }
    else {
      return null;
    }
  }

  public void expandRow(int row) {
    expandPath(treeTableModel.getLayout().getPathForRow(row));
  }

  public void collapseRow(int row) {
    collapsePath(treeTableModel.getLayout().getPathForRow(row));
  }

  public void expandPath(TreePath path) {
    getTreePathSupport().expandPath(path);
  }

  public boolean isExpanded(TreePath path) {
    return getTreePathSupport().isExpanded(path);
  }

  public void collapsePath(TreePath path) {
    getTreePathSupport().collapsePath(path);
  }

  boolean isTreeColumnIndex(int column) {
    int columnIndex = convertColumnIndexToModel(column);
    return columnIndex == 0;
  }

  public final AbstractLayoutCache getLayoutCache() {
    TmmTreeTableModel model = getTreeTableModel();
    if (model != null) {
      return model.getLayout();
    }
    else {
      return null;
    }
  }

  public void setRootVisible(boolean val) {
    if (getTreeTableModel() == null) {
      cachedRootVisible = val;
    }
    if (val != isRootVisible()) {
      AbstractLayoutCache layoutCache = getLayoutCache();
      if (layoutCache != null) {
        layoutCache.setRootVisible(val);
        if (layoutCache.getRowCount() > 0) {
          TreePath rootPath = layoutCache.getPathForRow(0);
          if (null != rootPath)
            layoutCache.treeStructureChanged(new TreeModelEvent(this, rootPath));
        }
        firePropertyChange("rootVisible", !val, val); // NOI18N
      }
    }
  }

  public boolean isRootVisible() {
    if (getLayoutCache() == null) {
      return cachedRootVisible;
    }
    else {
      return getLayoutCache().isRootVisible();
    }
  }

  @Override
  public boolean editCellAt(int row, int column, EventObject e) {
    // If it was on column 0, it may be a request to expand a tree node - check for that first.
    boolean isTreeColumn = isTreeColumnIndex(column);
    if (isTreeColumn && e instanceof MouseEvent) {
      MouseEvent me = (MouseEvent) e;
      AbstractLayoutCache layoutCache = getLayoutCache();
      if (layoutCache != null) {
        TreePath path = layoutCache.getPathForRow(convertRowIndexToModel(row));
        if (path != null && !getTreeTableModel().isLeaf(path.getLastPathComponent())) {
          int handleWidth = TmmTreeTableCellRenderer.getExpansionHandleWidth();
          Insets ins = getInsets();
          int nd = path.getPathCount() - (isRootVisible() ? 1 : 2);
          if (nd < 0) {
            nd = 0;
          }
          int handleStart = ins.left + (nd * TmmTreeTableCellRenderer.getNestingWidth());
          int handleEnd = ins.left + handleStart + handleWidth;
          // Translate 'x' to position of column if non-0:
          int columnStart = getCellRect(row, column, false).x;
          handleStart += columnStart;
          handleEnd += columnStart;

          TableColumn tableColumn = getColumnModel().getColumn(column);
          TableCellEditor columnCellEditor = tableColumn.getCellEditor();
          if ((me.getX() > ins.left && me.getX() >= handleStart && me.getX() <= handleEnd) || (me.getClickCount() > 1 && columnCellEditor == null)) {

            boolean expanded = layoutCache.isExpanded(path);
            if (!expanded) {
              getTreePathSupport().expandPath(path);

              Object ourObject = path.getLastPathComponent();
              int cCount = getTreeTableModel().getChildCount(ourObject);
              if (cCount > 0) {
                int lastRow = row;
                for (int i = 0; i < cCount; i++) {
                  Object child = getTreeTableModel().getChild(ourObject, i);
                  TreePath childPath = path.pathByAddingChild(child);
                  int childRow = layoutCache.getRowForPath(childPath);
                  childRow = convertRowIndexToView(childRow);
                  if (childRow > lastRow) {
                    lastRow = childRow;
                  }
                }
                int firstRow = row;
                Rectangle rectLast = getCellRect(lastRow, 0, true);
                Rectangle rectFirst = getCellRect(firstRow, 0, true);
                Rectangle rectFull = new Rectangle(rectFirst.x, rectFirst.y, rectLast.x + rectLast.width - rectFirst.x,
                    rectLast.y + rectLast.height - rectFirst.y);
                scrollRectToVisible(rectFull);
              }

            }
            else {
              getTreePathSupport().collapsePath(path);
            }
            return false;
          }
        }
        // It may be a request to check/uncheck a check-box
        if (checkAt(row, column, me)) {
          return false;
        }
      }
    }

    boolean res = false;
    if (!isTreeColumn || e instanceof MouseEvent && row >= 0 && isEditEvent(row, column, (MouseEvent) e)) {
      res = super.editCellAt(row, column, e);
    }
    if (res && isTreeColumn && row >= 0 && null != getEditorComponent()) {
      configureTreeCellEditor(getEditorComponent(), row, column);
    }
    if (e == null && !res && isTreeColumn) {
      // Handle SPACE
      checkAt(row, column, null);
    }
    return res;
  }

  private boolean isEditEvent(int row, int column, MouseEvent me) {
    if (me.getClickCount() > 1) {
      return true;
    }
    boolean noModifiers = me.getModifiersEx() == MouseEvent.BUTTON1_DOWN_MASK;
    if (lastEditPosition != null && selectedRow == row && noModifiers && lastEditPosition[0] == row && lastEditPosition[1] == column) {

      int handleWidth = TmmTreeTableCellRenderer.getExpansionHandleWidth();
      Insets ins = getInsets();
      AbstractLayoutCache layoutCache = getLayoutCache();
      if (layoutCache != null) {
        TreePath path = layoutCache.getPathForRow(convertRowIndexToModel(row));
        int nd = path.getPathCount() - (isRootVisible() ? 1 : 2);
        if (nd < 0) {
          nd = 0;
        }
        int handleStart = ins.left + (nd * TmmTreeTableCellRenderer.getNestingWidth());
        int handleEnd = ins.left + handleStart + handleWidth;
        // Translate 'x' to position of column if non-0:
        int columnStart = getCellRect(row, column, false).x;
        handleStart += columnStart;
        handleEnd += columnStart;
        if (me.getX() >= handleEnd) {
          lastEditPosition = null;
          return true;
        }
      }
    }
    lastEditPosition = new int[] { row, column };
    return false;
  }

  protected final boolean checkAt(int row, int column, MouseEvent me) {
    TmmTreeTableRenderDataProvider render = getRenderDataProvider();
    TableCellRenderer tcr = getDefaultRenderer(Object.class);
    if (render instanceof TmmTreeTableCheckRenderDataProvider && tcr instanceof TmmTreeTableCellRenderer) {
      TmmTreeTableCheckRenderDataProvider crender = (TmmTreeTableCheckRenderDataProvider) render;
      TmmTreeTableCellRenderer ocr = (TmmTreeTableCellRenderer) tcr;
      Object value = getValueAt(row, column);
      if (value != null && crender.isCheckable(value) && crender.isCheckEnabled(value)) {
        boolean chBoxPosition = false;
        if (me == null) {
          chBoxPosition = true;
        }
        else {
          int handleWidth = TmmTreeTableCellRenderer.getExpansionHandleWidth();
          int chWidth = ocr.getTheCheckBoxWidth();
          Insets ins = getInsets();
          AbstractLayoutCache layoutCache = getLayoutCache();
          if (layoutCache != null) {
            TreePath path = layoutCache.getPathForRow(convertRowIndexToModel(row));
            int nd = path.getPathCount() - (isRootVisible() ? 1 : 2);
            if (nd < 0) {
              nd = 0;
            }
            int chStart = ins.left + (nd * TmmTreeTableCellRenderer.getNestingWidth()) + handleWidth;
            int chEnd = chStart + chWidth;

            chBoxPosition = (me.getX() > ins.left && me.getX() >= chStart && me.getX() <= chEnd);
          }
        }
        if (chBoxPosition) {
          Boolean selected = crender.isSelected(value);
          if (selected == null || Boolean.TRUE.equals(selected)) {
            crender.setSelected(value, Boolean.FALSE);
          }
          else {
            crender.setSelected(value, Boolean.TRUE);
          }
          Rectangle r = getCellRect(row, column, true);
          repaint(r.x, r.y, r.width, r.height);
          return true;
        }
      }
    }
    return false;
  }

  protected void configureTreeCellEditor(Component editor, int row, int column) {
    if (!(editor instanceof JComponent)) {
      return;
    }
    TreeCellEditorBorder b = new TreeCellEditorBorder();

    AbstractLayoutCache layoutCache = getLayoutCache();
    if (layoutCache != null) {
      TreePath path = layoutCache.getPathForRow(convertRowIndexToModel(row));
      Object o = getValueAt(row, column);
      TmmTreeTableRenderDataProvider rdp = getRenderDataProvider();
      TableCellRenderer tcr = getDefaultRenderer(Object.class);
      if (rdp instanceof TmmTreeTableCheckRenderDataProvider && tcr instanceof TmmTreeTableCellRenderer) {
        TmmTreeTableCheckRenderDataProvider crender = (TmmTreeTableCheckRenderDataProvider) rdp;
        TmmTreeTableCellRenderer ocr = (TmmTreeTableCellRenderer) tcr;
        Object value = getValueAt(row, column);
        if (value != null && crender.isCheckable(value) && crender.isCheckEnabled(value)) {
          b.checkWidth = ocr.getTheCheckBoxWidth();
          b.checkBox = ocr.setUpCheckBox(crender, value, ocr.createCheckBox());
        }
      }
      b.icon = rdp.getIcon(o);
      b.nestingDepth = Math.max(0, path.getPathCount() - (isRootVisible() ? 1 : 2));
      b.isLeaf = getTreeTableModel().isLeaf(o);
      b.isExpanded = layoutCache.isExpanded(path);

      ((JComponent) editor).setBorder(b);
    }
  }

  @Override
  public void addNotify() {
    super.addNotify();
    calcRowHeight();
  }

  private void calcRowHeight() {
    // Users of themes can set an explicit row height, so check for it

    int rHeight = 20;
    // Derive a row height to accommodate the font and expand icon
    Font f = getFont();
    FontMetrics fm = getFontMetrics(f);
    int h = Math.max(fm.getHeight() + fm.getMaxDescent(), TmmTreeTableCellRenderer.getExpansionHandleHeight());
    rHeight = Math.max(rHeight, h) + 2;

    setRowHeight(rHeight);
  }

  /**
   * Returns all set tree nodes filter.
   *
   * @return a list of all set tree nodes filters
   */
  public List<ITmmTreeFilter<TmmTreeNode>> getFilters() {
    return new ArrayList<>(treeFilters);
  }

  /**
   * Removes any applied tree nodes filter.
   */
  public void clearFilter() {
    // remove our filter listener
    for (ITmmTreeFilter<TmmTreeNode> filter : treeFilters) {
      filter.removePropertyChangeListener(filterChangeListener);
    }

    treeFilters.clear();
  }

  /**
   * add a new filter to this tree
   *
   * @param newFilter
   *          the new filter to be added
   */
  public void addFilter(ITmmTreeFilter<TmmTreeNode> newFilter) {
    // add our filter listener
    newFilter.addPropertyChangeListener(ITmmTreeFilter.TREE_FILTER_CHANGED, filterChangeListener);

    treeFilters.add(newFilter);
  }

  /**
   * removes the given filter from this tree
   *
   * @param filter
   *          the filter to be removed
   */
  public void removeFilter(ITmmTreeFilter filter) {
    // remove our filter listener
    filter.removePropertyChangeListener(filterChangeListener);

    treeFilters.remove(filter);
  }

  /**
   * Updates nodes sorting and filtering for all loaded nodes.
   */
  @SuppressWarnings("unchecked")
  void updateFiltering() {
    final TreeModel model = treeTableModel.getTreeModel();
    if (model instanceof TmmTreeModel) {
      ((TmmTreeModel) model).updateSortingAndFiltering();
    }

    storeFilters();
    firePropertyChange("filterChanged", null, treeFilters);
  }

  public void setFilterValues(List<AbstractSettings.UIFilters> values) {
    boolean fireFilterChanged = false;

    for (AbstractSettings.UIFilters uiFilters : values) {
      if (StringUtils.isBlank(uiFilters.id) || uiFilters.state == ITmmUIFilter.FilterState.INACTIVE) {
        continue;
      }

      for (ITmmTreeFilter filter : treeFilters) {
        if (filter instanceof ITmmUIFilter) {
          ITmmUIFilter uiFilter = (ITmmUIFilter) filter;
          if (uiFilter.getId().equals(uiFilters.id)) {
            uiFilter.setFilterState(uiFilters.state);
            uiFilter.setFilterValue(uiFilters.filterValue);
            fireFilterChanged = true;
          }
        }
      }
    }

    if (fireFilterChanged) {
      updateFiltering();
    }
  }

  /**
   * to be overridden to provide storing of filters
   */
  public void storeFilters() {
  }

  /**
   * provide table cell tooltips via our table model
   *
   * @param e
   *          the mouse event
   * @return the tooltip or null
   */
  public String getToolTipText(MouseEvent e) {
    if (!(getModel() instanceof TmmTreeTableModel)) {
      return null;
    }

    Point p = e.getPoint();
    int rowIndex = rowAtPoint(p);
    int colIndex = columnAtPoint(p);
    int realColumnIndex = convertColumnIndexToModel(colIndex) - 1; // first column is the tree

    if (colIndex == 0) {
      // tree
      return super.getToolTipText(e);
    }
    else if (colIndex > 0) {
      // table
      TmmTreeTableModel treeTableModel = ((TmmTreeTableModel) getModel());
      ConnectorTableModel tableModel = treeTableModel.getTableModel();

      return tableModel.getTooltipAt(rowIndex, realColumnIndex);
    }

    return null;
  }

  private static class TreeCellEditorBorder implements Border {
    private Insets    insets        = new Insets(0, 0, 0, 0);
    private boolean   isLeaf;
    private boolean   isExpanded;
    private Icon      icon;
    private int       nestingDepth;
    private final int ICON_TEXT_GAP = new JLabel().getIconTextGap();
    private int       checkWidth;
    private JCheckBox checkBox;

    @Override
    public Insets getBorderInsets(Component c) {
      insets.left = (nestingDepth * TmmTreeTableCellRenderer.getNestingWidth()) + TmmTreeTableCellRenderer.getExpansionHandleWidth() + 1;
      insets.left += checkWidth + ((icon != null) ? icon.getIconWidth() + ICON_TEXT_GAP : 0);
      insets.top = 1;
      insets.right = 1;
      insets.bottom = 1;
      return insets;
    }

    @Override
    public boolean isBorderOpaque() {
      return false;
    }

    @Override
    public void paintBorder(Component c, java.awt.Graphics g, int x, int y, int width, int height) {
      int iconY;
      int iconX = nestingDepth * TmmTreeTableCellRenderer.getNestingWidth();
      if (!isLeaf) {
        Icon expIcon = isExpanded ? TmmTreeTableCellRenderer.getExpandedIcon() : TmmTreeTableCellRenderer.getCollapsedIcon();
        if (expIcon.getIconHeight() < height) {
          iconY = (height / 2) - (expIcon.getIconHeight() / 2);
        }
        else {
          iconY = 0;
        }
        expIcon.paintIcon(c, g, iconX, iconY);
      }
      iconX += TmmTreeTableCellRenderer.getExpansionHandleWidth() + 1;

      if (null != checkBox) {
        java.awt.Graphics chbg = g.create(iconX, y, checkWidth, height);
        checkBox.paint(chbg);
        chbg.dispose();
      }
      iconX += checkWidth;

      if (null != icon) {
        if (icon.getIconHeight() < height) {
          iconY = (height / 2) - (icon.getIconHeight() / 2);
        }
        else {
          iconY = 0;
        }
        icon.paintIcon(c, g, iconX, iconY);
      }
    }
  }

  private class TmmTreeModelConnector<E extends TmmTreeNode> extends TmmTreeModel {

    /**
     * Create a new instance of the TmmTreeModel for the given TmmTree and data provider
     *
     * @param dataProvider
     *          the data provider to create the model for
     */
    public TmmTreeModelConnector(final TmmTreeDataProvider<E> dataProvider) {
      super(null, dataProvider);
    }

    @Override
    public void updateSortingAndFiltering(TmmTreeNode parent) {
      // store selected rows
      int[] selectedRows = getSelectedRows();

      // Updating root node children
      boolean structureChanged = performFilteringAndSortingRecursively(parent);
      if (structureChanged) {
        nodeStructureChanged(getRoot());

        // Restoring tree state including all selections and expansions
        clearSelection();
        for (int row : selectedRows) {
          getSelectionModel().addSelectionInterval(row, row);
        }
      }
    }
  }

}
