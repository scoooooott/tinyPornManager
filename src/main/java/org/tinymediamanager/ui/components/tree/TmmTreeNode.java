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

import javax.swing.tree.DefaultMutableTreeNode;

/**
 * The default tree node for the TmmTree
 * 
 * @author Manuel Laggner
 */
public class TmmTreeNode extends DefaultMutableTreeNode {
  private static final long     serialVersionUID = 6426644541629397542L;

  protected TmmTreeDataProvider dataProvider;

  private final String          id;

  public TmmTreeNode(Object userObject, TmmTreeDataProvider dataProvider) {
    super.setUserObject(userObject);
    this.dataProvider = dataProvider;
    this.id = "" + userObject.hashCode();
  }

  public String getId() {
    return id;
  }

  public TmmTreeDataProvider getDataProvider() {
    return dataProvider;
  }

  @Override
  public String toString() {
    return userObject.toString();
  }
}
