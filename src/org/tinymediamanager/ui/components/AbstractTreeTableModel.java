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
package org.tinymediamanager.ui.components;

import javax.swing.event.EventListenerList;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreePath;

/**
 * The Class AbstractTreeTableModel.
 * 
 * @author Manuel Laggner
 */
public abstract class AbstractTreeTableModel implements TreeTableModel {

  /** The root. */
  protected Object            root;

  /** The listener list. */
  protected EventListenerList listenerList      = new EventListenerList();

  /** The Constant CHANGED. */
  private static final int    CHANGED           = 0;

  /** The Constant INSERTED. */
  private static final int    INSERTED          = 1;

  /** The Constant REMOVED. */
  private static final int    REMOVED           = 2;

  /** The Constant STRUCTURE_CHANGED. */
  private static final int    STRUCTURE_CHANGED = 3;

  /**
   * abstract tree table model.
   */
  public AbstractTreeTableModel() {
  }

  /*
   * (non-Javadoc)
   * 
   * @see javax.swing.tree.TreeModel#getRoot()
   */
  public Object getRoot() {
    return root;
  }

  /*
   * (non-Javadoc)
   * 
   * @see javax.swing.tree.TreeModel#isLeaf(java.lang.Object)
   */
  public boolean isLeaf(Object node) {
    return getChildCount(node) == 0;
  }

  /*
   * (non-Javadoc)
   * 
   * @see javax.swing.tree.TreeModel#valueForPathChanged(javax.swing.tree.TreePath, java.lang.Object)
   */
  public void valueForPathChanged(TreePath path, Object newValue) {
  }

  /**
   * Die Methode wird normalerweise nicht aufgerufen.
   * 
   * @param parent
   *          the parent
   * @param child
   *          the child
   * @return the index of child
   */
  public int getIndexOfChild(Object parent, Object child) {
    return 0;
  }

  /*
   * (non-Javadoc)
   * 
   * @see javax.swing.tree.TreeModel#addTreeModelListener(javax.swing.event.TreeModelListener)
   */
  public void addTreeModelListener(TreeModelListener l) {
    listenerList.add(TreeModelListener.class, l);
  }

  /*
   * (non-Javadoc)
   * 
   * @see javax.swing.tree.TreeModel#removeTreeModelListener(javax.swing.event.TreeModelListener)
   */
  public void removeTreeModelListener(TreeModelListener l) {
    listenerList.remove(TreeModelListener.class, l);
  }

  /**
   * Fire tree node.
   * 
   * @param changeType
   *          the change type
   * @param source
   *          the source
   * @param path
   *          the path
   * @param childIndices
   *          the child indices
   * @param children
   *          the children
   */
  private void fireTreeNode(int changeType, Object source, Object[] path, int[] childIndices, Object[] children) {
    Object[] listeners = listenerList.getListenerList();
    TreeModelEvent e = new TreeModelEvent(source, path, childIndices, children);
    for (int i = listeners.length - 2; i >= 0; i -= 2) {
      if (listeners[i] == TreeModelListener.class) {

        switch (changeType) {
          case CHANGED:
            ((TreeModelListener) listeners[i + 1]).treeNodesChanged(e);
            break;
          case INSERTED:
            ((TreeModelListener) listeners[i + 1]).treeNodesInserted(e);
            break;
          case REMOVED:
            ((TreeModelListener) listeners[i + 1]).treeNodesRemoved(e);
            break;
          case STRUCTURE_CHANGED:
            ((TreeModelListener) listeners[i + 1]).treeStructureChanged(e);
            break;
          default:
            break;
        }

      }
    }
  }

  /**
   * Fire tree nodes changed.
   * 
   * @param source
   *          the source
   * @param path
   *          the path
   * @param childIndices
   *          the child indices
   * @param children
   *          the children
   */
  protected void fireTreeNodesChanged(Object source, Object[] path, int[] childIndices, Object[] children) {
    fireTreeNode(CHANGED, source, path, childIndices, children);
  }

  /**
   * Fire tree nodes inserted.
   * 
   * @param source
   *          the source
   * @param path
   *          the path
   * @param childIndices
   *          the child indices
   * @param children
   *          the children
   */
  protected void fireTreeNodesInserted(Object source, Object[] path, int[] childIndices, Object[] children) {
    fireTreeNode(INSERTED, source, path, childIndices, children);
  }

  /**
   * Fire tree nodes removed.
   * 
   * @param source
   *          the source
   * @param path
   *          the path
   * @param childIndices
   *          the child indices
   * @param children
   *          the children
   */
  protected void fireTreeNodesRemoved(Object source, Object[] path, int[] childIndices, Object[] children) {
    fireTreeNode(REMOVED, source, path, childIndices, children);
  }

  /**
   * Fire tree structure changed.
   * 
   * @param source
   *          the source
   * @param path
   *          the path
   * @param childIndices
   *          the child indices
   * @param children
   *          the children
   */
  protected void fireTreeStructureChanged(Object source, Object[] path, int[] childIndices, Object[] children) {
    fireTreeNode(STRUCTURE_CHANGED, source, path, childIndices, children);
  }

}
