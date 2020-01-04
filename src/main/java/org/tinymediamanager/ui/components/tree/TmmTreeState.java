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

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * The class TmmTreeState is used to store the state of our tree
 * 
 * @author Manuel Laggner
 */
class TmmTreeState {
  protected Map<String, TmmNodeState> states = new LinkedHashMap<>();

  public TmmTreeState() {
  }

  public TmmTreeState(final Map<String, TmmNodeState> states) {
    if (states != null) {
      setStates(states);
    }
  }

  public Map<String, TmmNodeState> getStates() {
    return states;
  }

  public void setStates(final Map<String, TmmNodeState> states) {
    this.states = states;
  }

  public void addState(final String nodeId, final boolean expanded, final boolean selected) {
    states.put(nodeId, new TmmNodeState(expanded, selected));
  }

  public boolean isExpanded(final String nodeId) {
    final TmmNodeState state = states.get(nodeId);
    return state != null && state.isExpanded();
  }

  public boolean isSelected(final String nodeId) {
    final TmmNodeState state = states.get(nodeId);
    return state != null && state.isSelected();
  }
}
