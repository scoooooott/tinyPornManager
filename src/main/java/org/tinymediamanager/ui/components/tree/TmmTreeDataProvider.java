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
package org.tinymediamanager.ui.components.tree;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.tinymediamanager.core.AbstractModelObject;

/**
 * The class TmmTreeDataProvider is the base class for all data providers used with the TmmTree
 * 
 * @author Manuel Laggner
 *
 * @param <E>
 */
public abstract class TmmTreeDataProvider<E extends TmmTreeNode> extends AbstractModelObject {
  public static final String               NODE_INSERTED          = "nodeInserted";
  public static final String               NODE_CHANGED           = "nodeChanged";
  public static final String               NODE_REMOVED           = "nodeRemoved";
  public static final String               NODE_STRUCTURE_CHANGED = "nodeStructureChanged";

  protected Set<ITmmTreeFilter<E>>         treeFilters;
  protected Comparator<E>                  treeComparator         = null;
  protected final ReadWriteLock            readWriteLock          = new ReentrantReadWriteLock();
  protected final Map<Object, TmmTreeNode> nodeMap                = new HashMap<>();

  /**
   * Get all tree filters assigned to this data provider
   * 
   * @return a list of all set filters
   */
  public Set<ITmmTreeFilter<E>> getTreeFilters() {
    return treeFilters;
  }

  /**
   * Set all given tree filters for this data provider
   * 
   * @param treeFilters
   *          the tree filters to be set
   */
  public void setTreeFilters(final Set<ITmmTreeFilter<E>> treeFilters) {
    this.treeFilters = treeFilters;
  }

  /**
   * Get the comparator for this tree
   * 
   * @return the comparator
   */
  public Comparator<E> getTreeComparator() {
    return treeComparator;
  }

  /**
   * Set the comparator for this tree
   * 
   * @param treeComparator
   *          set this comparator for the tree
   */
  public void setTreeComparator(Comparator<E> treeComparator) {
    this.treeComparator = treeComparator;
  }

  protected TmmTreeNode getNodeFromCache(Object obj) {
    readWriteLock.readLock().lock();
    TmmTreeNode node = nodeMap.get(obj);
    readWriteLock.readLock().unlock();
    return node;
  }

  protected void putNodeToCache(Object obj, TmmTreeNode node) {
    readWriteLock.writeLock().lock();
    nodeMap.put(obj, node);
    readWriteLock.writeLock().unlock();
  }

  protected TmmTreeNode removeNodeFromCache(Object obj) {
    readWriteLock.writeLock().lock();
    TmmTreeNode node = nodeMap.remove(obj);
    readWriteLock.writeLock().unlock();
    return node;
  }

  /**
   * Get the root node
   * 
   * @return the root node
   */
  abstract public E getRoot();

  /**
   * Get the parent node for the given node
   * 
   * @param node
   *          the node to get the parent node for
   * @return the parent node
   */
  abstract public E getParent(E node);

  /**
   * Get all children for the given node
   * 
   * @param node
   *          the node to get all children for
   * @return a list of all children
   */
  abstract public List<E> getChildren(E node);

  /**
   * Is the given node a leaf?
   * 
   * @param node
   *          the node
   * @return true if the node is a leaf; false otherwise
   */
  abstract public boolean isLeaf(final E node);
}
