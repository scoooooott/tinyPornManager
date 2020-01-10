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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.SwingUtilities;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.table.TableModel;
import javax.swing.tree.AbstractLayoutCache;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

/**
 * Responsible for translating TreeModel events to appropriate TableModelEvents.
 *
 * @author Manuel Laggner
 */
final class TmmTreeTableEventBroadcaster implements TableModelListener, TreeModelListener, TreeExpansionListener, TreeWillExpandListener {
  private TmmTreeTableModel        model;

  private TableModelEvent          pendingExpansionEvent = null;

  private static final int         NODES_CHANGED         = 0;
  private static final int         NODES_INSERTED        = 1;
  private static final int         NODES_REMOVED         = 2;
  private static final int         STRUCTURE_CHANGED     = 3;

  private List<TableModelListener> tableListeners        = new ArrayList<>();
  private List<TreeModelListener>  treeListeners         = new ArrayList<>();

  public TmmTreeTableEventBroadcaster(TmmTreeTableModel model) {
    this.model = model;
  }

  private TmmTreeTableModel getModel() {
    return model;
  }

  private AbstractLayoutCache getLayout() {
    return getModel().getLayout();
  }

  private TmmTreeTableTreePathSupport getTreePathSupport() {
    return getModel().getTreePathSupport();
  }

  private TreeModel getTreeModel() {
    return getModel().getTreeModel();
  }

  private TableModel getTableModel() {
    return getModel().getTableModel();
  }

  public synchronized void addTableModelListener(TableModelListener l) {
    tableListeners.add(l);
  }

  public synchronized void addTreeModelListener(TreeModelListener l) {
    treeListeners.add(l);
  }

  public synchronized void removeTableModelListener(TableModelListener l) {
    tableListeners.remove(l);
  }

  public synchronized void removeTreeModelListener(TreeModelListener l) {
    treeListeners.remove(l);
  }

  private void fireTableChange(TableModelEvent e, TableModelListener[] listeners) {
    if (e == null) {
      return;
    }
    assert (e.getSource() == getModel());
    for (TableModelListener listener : listeners) {
      listener.tableChanged(e);
    }
  }

  private void fireTableChange(TableModelEvent e) {
    if (e == null) {
      return;
    }
    fireTableChange(e, getTableModelListeners());
  }

  private void fireTableChange(TableModelEvent[] e) {
    if (e == null || e.length == 0) {
      return;
    }

    TableModelListener[] listeners = getTableModelListeners();

    for (TableModelEvent anE : e) {
      fireTableChange(anE, listeners);
    }
  }

  private TableModelListener[] getTableModelListeners() {
    TableModelListener[] listeners;
    synchronized (this) {
      listeners = new TableModelListener[tableListeners.size()];

      listeners = tableListeners.toArray(listeners);
    }
    return listeners;
  }

  private synchronized void fireTreeChange(TreeModelEvent e, int type) {
    if (e == null) {
      return;
    }
    assert (e.getSource() == getModel());

    TreeModelListener[] listeners;
    synchronized (this) {
      listeners = new TreeModelListener[treeListeners.size()];
      listeners = treeListeners.toArray(listeners);
    }

    // Now refire it to any listeners
    for (TreeModelListener listener : listeners) {
      switch (type) {
        case NODES_CHANGED:
          listener.treeNodesChanged(e);
          break;
        case NODES_INSERTED:
          listener.treeNodesInserted(e);
          break;
        case NODES_REMOVED:
          listener.treeNodesRemoved(e);
          break;
        case STRUCTURE_CHANGED:
          listener.treeStructureChanged(e);
          break;
        default:
          assert false;
      }
    }
  }

  @Override
  public void tableChanged(final TableModelEvent e) {
    assert (e.getType() == TableModelEvent.UPDATE) : "Table model should only fire updates, never structural changes";

    if (SwingUtilities.isEventDispatchThread()) {
      fireTableChange(translateEvent(e));
    }
    else {
      SwingUtilities.invokeLater(() -> tableChanged(e));
    }
  }

  @Override
  public void treeNodesChanged(TreeModelEvent e) {
    assert SwingUtilities.isEventDispatchThread();

    fireTreeChange(translateEvent(e), NODES_CHANGED);

    TableModelEvent[] events = translateEvent(e, NODES_CHANGED);
    getLayout().treeNodesChanged(e);
    fireTableChange(events);
  }

  @Override
  public void treeNodesInserted(TreeModelEvent e) {
    assert SwingUtilities.isEventDispatchThread();

    fireTreeChange(translateEvent(e), NODES_INSERTED);

    TableModelEvent[] events = translateEvent(e, NODES_INSERTED);
    getLayout().treeNodesInserted(e);
    fireTableChange(events);
  }

  @Override
  public void treeNodesRemoved(TreeModelEvent e) {
    assert SwingUtilities.isEventDispatchThread();

    fireTreeChange(translateEvent(e), NODES_REMOVED);

    TableModelEvent[] events = translateEvent(e, NODES_REMOVED);
    getLayout().treeNodesRemoved(e);
    fireTableChange(events);
  }

  @Override
  public void treeStructureChanged(TreeModelEvent e) {
    assert SwingUtilities.isEventDispatchThread();

    getTreePathSupport().treeStructureChanged(e);
    fireTreeChange(translateEvent(e), STRUCTURE_CHANGED);

    if (!getLayout().isExpanded(e.getTreePath())) {
      treeNodesChanged(e);
      return;
    }

    getTreePathSupport().clear();
    fireTableChange(new TableModelEvent(getModel()));
  }

  @Override
  public void treeWillCollapse(TreeExpansionEvent event) {
    assert SwingUtilities.isEventDispatchThread();

    pendingExpansionEvent = translateEvent(event, false);
  }

  @Override
  public void treeWillExpand(TreeExpansionEvent event) {
    assert SwingUtilities.isEventDispatchThread();

    pendingExpansionEvent = translateEvent(event, true);
  }

  @Override
  public void treeCollapsed(TreeExpansionEvent event) {
    assert SwingUtilities.isEventDispatchThread();

    if (event != null) {
      TreePath path = event.getPath();

      // Tell the layout about the change
      if (path != null && getTreePathSupport().isVisible(path)) {
        getLayout().setExpandedState(path, false);
      }
    }

    int row;
    if (event != null) {
      TreePath path = event.getPath();
      row = getLayout().getRowForPath(path);
    }
    else {
      row = -1;
    }
    TableModelEvent evt;
    if (row == -1) {
      evt = new TableModelEvent(getModel());
    }
    else {
      evt = new TableModelEvent(getModel(), row, row, 0, TableModelEvent.UPDATE);
    }
    fireTableChange(new TableModelEvent[] { evt, pendingExpansionEvent });
    pendingExpansionEvent = null;
  }

  @Override
  public void treeExpanded(TreeExpansionEvent event) {
    assert SwingUtilities.isEventDispatchThread();

    if (event != null) {
      updateExpandedDescendants(event.getPath());
    }

    int row;
    if (event != null) {
      TreePath path = event.getPath();
      row = getLayout().getRowForPath(path);
    }
    else {
      row = -1;
    }
    TableModelEvent evt;
    if (row == -1) {
      evt = new TableModelEvent(getModel());
    }
    else {
      evt = new TableModelEvent(getModel(), row, row, 0, TableModelEvent.UPDATE);
    }
    fireTableChange(new TableModelEvent[] { evt, pendingExpansionEvent });
    pendingExpansionEvent = null;
  }

  private void updateExpandedDescendants(TreePath path) {
    getLayout().setExpandedState(path, true);

    TreePath[] descendants = getTreePathSupport().getExpandedDescendants(path);

    if (descendants.length > 0) {
      for (TreePath descendant : descendants) {
        getLayout().setExpandedState(descendant, true);
      }
    }
  }

  private TableModelEvent translateEvent(TableModelEvent e) {
    return new TableModelEvent(getModel(), e.getFirstRow(), e.getLastRow(), e.getColumn() + 1, e.getType());
  }

  private TreeModelEvent translateEvent(TreeModelEvent e) {
    return new TreeModelEvent(getModel(), e.getPath(), e.getChildIndices(), e.getChildren());
  }

  /**
   * Translates a TreeModelEvent into one or more contiguous TableModelEvents
   */
  private TableModelEvent[] translateEvent(TreeModelEvent e, int type) {
    TreePath path = e.getTreePath();

    // If the node is not expanded, we simply fire a change event for the parent
    boolean inClosedNode = !getLayout().isExpanded(path);
    if (inClosedNode) {
      int row = getLayout().getRowForPath(path);
      // If the node is closed, no expensive checks are needed - just fire a change on the parent node in case it needs to update its display
      if (row != -1) {
        switch (type) {
          case NODES_CHANGED:
          case NODES_INSERTED:
          case NODES_REMOVED:
            return new TableModelEvent[] { new TableModelEvent(getModel(), row, row, 0, TableModelEvent.UPDATE) };
          default:
            assert false : "Unknown event type " + type;
        }
      }
      return new TableModelEvent[0];
    }

    int[] rowIndices = computeRowIndices(e);
    boolean discontiguous = isDiscontiguous(rowIndices);

    int[][] blocks;
    if (discontiguous) {
      blocks = getContiguousIndexBlocks(rowIndices, type == NODES_REMOVED);
    }
    else {
      blocks = new int[][] { rowIndices };
    }

    TableModelEvent[] result = new TableModelEvent[blocks.length];
    for (int i = 0; i < blocks.length; i++) {

      int[] currBlock = blocks[i];
      switch (type) {
        case NODES_CHANGED:
          result[i] = createTableChangeEvent(e, currBlock);
          break;
        case NODES_INSERTED:
          result[i] = createTableInsertionEvent(e, currBlock);
          break;
        case NODES_REMOVED:
          result[i] = createTableDeletionEvent(e, currBlock);
          break;
        default:
          assert false : "Unknown event type: " + type;
      }
    }
    return result;
  }

  /**
   * Translates tree expansion event into an appropriate TableModelEvent indicating the number of rows added/removed at the appropriate index
   */
  private TableModelEvent translateEvent(TreeExpansionEvent e, boolean expand) {
    TreePath path = e.getPath();

    int firstRow = getLayout().getRowForPath(path) + 1;
    if (firstRow == -1) {
      return null;
    }

    // Get all the expanded descendants of the path that was expanded/collapsed
    TreePath[] paths = getTreePathSupport().getExpandedDescendants(path);

    // Start with the number of children of whatever was expanded/collapsed
    int count = getTreeModel().getChildCount(path.getLastPathComponent());

    if (count == 0) {
      return null;
    }

    // Iterate any of the expanded children, adding in their child counts
    for (TreePath path1 : paths) {
      count += getTreeModel().getChildCount(path1.getLastPathComponent());
    }

    int lastRow = firstRow + count - 1;
    return new TableModelEvent(getModel(), firstRow, lastRow, TableModelEvent.ALL_COLUMNS, expand ? TableModelEvent.INSERT : TableModelEvent.DELETE);
  }

  private TableModelEvent createTableChangeEvent(TreeModelEvent e, int[] indices) {
    TableModelEvent result;
    TreePath path = e.getTreePath();
    int row = getLayout().getRowForPath(path);

    int first = null == indices ? row : indices[0];
    int last = null == indices ? row : indices[indices.length - 1];
    result = new TableModelEvent(getModel(), first, last, 0, TableModelEvent.UPDATE);

    return result;
  }

  private TableModelEvent createTableInsertionEvent(TreeModelEvent e, int[] indices) {
    TableModelEvent result;
    TreePath path = e.getTreePath();
    int row = getLayout().getRowForPath(path);

    boolean realInsert = getLayout().isExpanded(path);

    if (realInsert) {
      if (indices.length == 1) {
        int affectedRow = indices[0];
        result = new TableModelEvent(getModel(), affectedRow, affectedRow, TableModelEvent.ALL_COLUMNS, TableModelEvent.INSERT);

      }
      else {
        int lowest = indices[0];
        int highest = indices[indices.length - 1];
        result = new TableModelEvent(getModel(), lowest, highest, TableModelEvent.ALL_COLUMNS, TableModelEvent.INSERT);

      }
    }
    else {
      result = new TableModelEvent(getModel(), row, row, TableModelEvent.ALL_COLUMNS); // TODO - specify only the tree column
    }
    return result;
  }

  private TableModelEvent createTableDeletionEvent(TreeModelEvent e, int[] indices) {
    TableModelEvent result;

    int firstRow = indices[0];
    int lastRow = indices[indices.length - 1];

    result = new TableModelEvent(getModel(), firstRow, lastRow, TableModelEvent.ALL_COLUMNS, TableModelEvent.DELETE);
    return result;
  }

  private static boolean isDiscontiguous(int[] indices) {
    if (indices == null || indices.length <= 1) {
      return false;
    }
    Arrays.sort(indices);
    int lastVal = indices[0];
    for (int i = 1; i < indices.length; i++) {
      if (indices[i] != lastVal + 1) {
        return true;
      }
      else {
        lastVal++;
      }
    }
    return false;
  }

  private static int[][] getContiguousIndexBlocks(int[] indices, final boolean reverseOrder) {
    // Quick checks
    if (indices.length == 0) {
      return new int[][] { {} };
    }
    if (indices.length == 1) {
      return new int[][] { indices };
    }

    // Sort the indices as requested
    if (reverseOrder) {
      inverseSort(indices);
    }
    else {
      Arrays.sort(indices);
    }

    final List<int[]> blocks = new ArrayList<>();
    int startIndex = 0;

    // Iterate the indices
    for (int i = 1; i < indices.length; i++) {
      // See if we've hit a discontinuity
      int lastVal = indices[i - 1];
      boolean newBlock = reverseOrder ? indices[i] != lastVal - 1 : indices[i] != lastVal + 1;

      if (newBlock) {
        // new block detected
        // copy the last contiguous block and add it to the result array
        int[] block = new int[i - startIndex];
        System.arraycopy(indices, startIndex, block, 0, block.length);
        blocks.add(block);
        startIndex = i;
      }
    }

    // add last block to the result array
    int[] block = new int[indices.length - startIndex];
    System.arraycopy(indices, startIndex, block, 0, block.length);
    blocks.add(block);

    return blocks.toArray(new int[][] {});
  }

  private static void inverseSort(int[] array) {
    for (int i = 0; i < array.length; i++) {
      array[i] *= -1;
    }
    Arrays.sort(array);
    for (int i = 0; i < array.length; i++) {
      array[i] *= -1;
    }
  }

  private int[] computeRowIndices(TreeModelEvent e) {
    int[] rowIndices;
    int parentRow = getLayout().getRowForPath(e.getTreePath());
    if (e.getChildren() != null) {
      rowIndices = new int[e.getChildren().length];
      for (int i = 0; i < e.getChildren().length; i++) {
        TreePath childPath = e.getTreePath().pathByAddingChild(e.getChildren()[i]);
        int index = getLayout().getRowForPath(childPath);
        rowIndices[i] = index < 0 ? parentRow + e.getChildIndices()[i] + 1 : index;
      }
    }
    else {
      rowIndices = null;
    }
    return rowIndices;
  }
}
