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
import java.util.Enumeration;
import java.util.List;

import javax.swing.SwingUtilities;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.tree.AbstractLayoutCache;
import javax.swing.tree.ExpandVetoException;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

/**
 * Manages expanded/collapsed paths for the TreeTable.
 *
 * @author Manuel Laggner
 */
final class TmmTreeTableTreePathSupport {
  private List<TreeExpansionListener>  expansionListeners  = new ArrayList<>();
  private List<TreeWillExpandListener> willExpandListeners = new ArrayList<>();
  private AbstractLayoutCache          layout;

  TmmTreeTableTreePathSupport(AbstractLayoutCache layout) {
    this.layout = layout;
  }

  void clear() {
  }

  void expandPath(TreePath path) {
    assert SwingUtilities.isEventDispatchThread();
    if (layout.isExpanded(path)) {
      return;
    }

    TreePath parentPath = path.getParentPath();
    if (parentPath != null) {
      expandPath(parentPath);
    }

    TreeExpansionEvent treeExpansionEvent = new TreeExpansionEvent(this, path);
    try {
      fireTreeWillExpand(treeExpansionEvent, true);
      layout.setExpandedState(path, true);
      fireTreeExpansion(treeExpansionEvent, true);
    }
    catch (Exception ignored) {
    }
  }

  public void collapsePath(TreePath path) {
    assert SwingUtilities.isEventDispatchThread();
    if (!layout.isExpanded(path)) {
      return;
    }

    TreeExpansionEvent treeExpansionEvent = new TreeExpansionEvent(this, path);
    try {
      fireTreeWillExpand(treeExpansionEvent, false);
      layout.setExpandedState(path, false);
      fireTreeExpansion(treeExpansionEvent, false);
    }
    catch (Exception ignored) {
    }
  }

  private void fireTreeExpansion(TreeExpansionEvent e, boolean expanded) {
    for (TreeExpansionListener listener : new ArrayList<>(expansionListeners)) {
      if (expanded) {
        listener.treeExpanded(e);
      }
      else {
        listener.treeCollapsed(e);
      }
    }
  }

  private void fireTreeWillExpand(TreeExpansionEvent e, boolean expanded) throws ExpandVetoException {
    for (TreeWillExpandListener listener : new ArrayList<>(willExpandListeners)) {
      if (expanded) {
        listener.treeWillExpand(e);
      }
      else {
        listener.treeWillCollapse(e);
      }
    }
  }

  boolean isExpanded(TreePath path) {
    assert SwingUtilities.isEventDispatchThread();
    if (path == null) {
      return false;
    }

    if (!layout.isRootVisible() && path.getParentPath() == null) {
      return true; // Invisible root is always expanded
    }

    // Is this node expanded?
    boolean nodeExpanded = layout.isExpanded(path);
    if (!nodeExpanded) {
      return false;
    }

    // It is, make sure its parent is also expanded.
    TreePath parentPath = path.getParentPath();

    if (parentPath != null) {
      return isExpanded(parentPath);
    }

    return true;
  }

  boolean isVisible(TreePath path) {
    if (path != null) {
      TreePath parentPath = path.getParentPath();

      if (parentPath != null) {
        return isExpanded(parentPath);
      }
      // Root.
      return true;
    }
    return false;
  }

  TreePath[] getExpandedDescendants(TreePath parent) {
    assert SwingUtilities.isEventDispatchThread();
    TreePath[] result = new TreePath[0];
    if (isExpanded(parent)) {
      TreePath path;
      List<TreePath> results = null;

      Enumeration<TreePath> tpe = layout.getVisiblePathsFrom(parent);
      if (tpe != null) {
        while (tpe.hasMoreElements()) {
          path = tpe.nextElement();
          // Add the path if it is expanded, a descendant of parent, and it is visible (all parents expanded). This is rather expensive!
          if (path != parent && layout.isExpanded(path) && parent.isDescendant(path)) {

            if (results == null) {
              results = new ArrayList<>();
            }
            results.add(path);
          }
        }
        if (results != null) {
          result = results.toArray(result);
        }
      }
    }
    return result;
  }

  synchronized void addTreeExpansionListener(TreeExpansionListener listener) {
    expansionListeners.add(listener);
  }

  synchronized void removeTreeExpansionListener(TreeExpansionListener listener) {
    expansionListeners.remove(listener);
  }

  synchronized void addTreeWillExpandListener(TreeWillExpandListener listener) {
    willExpandListeners.add(listener);
  }

  synchronized void removeTreeWillExpandListener(TreeWillExpandListener listener) {
    willExpandListeners.remove(listener);
  }

  void treeStructureChanged(TreeModelEvent event) {
    TreePath path = event.getTreePath();
    TreeModel model = layout.getModel();
    if ((path == null) && (model != null)) {
      Object root = model.getRoot();
      if (root != null) {
        path = new TreePath(root);
      }
    }

    TreePath[] expandedDescendants = getExpandedDescendants(path);

    layout.treeStructureChanged(event);

    for (TreePath tp : expandedDescendants) {
      layout.setExpandedState(tp, true);
    }
  }
}
