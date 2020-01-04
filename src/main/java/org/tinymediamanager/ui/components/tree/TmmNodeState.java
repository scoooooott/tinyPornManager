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

/**
 * The class TmmNodeState is used to store the node-states of our tree
 * 
 * @author Manuel Laggner
 */
class TmmNodeState {
  protected boolean expanded;
  protected boolean selected;

  public TmmNodeState() {
    this.expanded = false;
    this.selected = false;
  }

  public TmmNodeState(final boolean expanded, final boolean selected) {
    this.expanded = expanded;
    this.selected = selected;
  }

  public boolean isExpanded() {
    return expanded;
  }

  public void setExpanded(final boolean expanded) {
    this.expanded = expanded;
  }

  public boolean isSelected() {
    return selected;
  }

  public void setSelected(final boolean selected) {
    this.selected = selected;
  }
}
