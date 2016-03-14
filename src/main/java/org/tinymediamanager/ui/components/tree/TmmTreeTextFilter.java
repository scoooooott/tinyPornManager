/*
 * Copyright 2012 - 2015 Manuel Laggner
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

import java.util.Enumeration;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.apache.commons.lang3.StringUtils;
import org.tinymediamanager.ui.IconManager;
import org.tinymediamanager.ui.UTF8Control;
import org.tinymediamanager.ui.components.EnhancedTextField;

/**
 * The class TmmTreeTextFilter provides a textual filter for the TmmTree
 * 
 * @author Manuel Laggner
 *
 * @param <E>
 */
public class TmmTreeTextFilter<E extends TmmTreeNode> extends EnhancedTextField implements ITmmTreeFilter<E> {
  private static final long           serialVersionUID = 8492300503787395800L;
  private static final ResourceBundle BUNDLE           = ResourceBundle.getBundle("messages", new UTF8Control()); //$NON-NLS-1$

  protected String                    filterText       = "";

  public TmmTreeTextFilter() {
    super(BUNDLE.getString("tmm.searchfield"), IconManager.SEARCH); //$NON-NLS-1$
    initDocumentListener();
  }

  protected void initDocumentListener() {
    getDocument().addDocumentListener(new DocumentListener() {
      @Override
      public void insertUpdate(final DocumentEvent e) {
        updateFilter();
      }

      @Override
      public void removeUpdate(final DocumentEvent e) {
        updateFilter();
      }

      @Override
      public void changedUpdate(final DocumentEvent e) {
        updateFilter();
      }

      private void updateFilter() {
        String oldValue = filterText;
        filterText = getText();
        firePropertyChange(ITmmTreeFilter.TREE_FILTER_CHANGED, oldValue, filterText);
      }
    });
  }

  @SuppressWarnings("unchecked")
  @Override
  public boolean accept(E node) {
    if (StringUtils.isBlank(filterText)) {
      return true;
    }

    Pattern pattern = Pattern.compile("(?i)" + Pattern.quote(filterText));

    // first: filter on the node
    Matcher matcher = pattern.matcher(node.toString());
    if (matcher.find()) {
      return true;
    }

    // second: parse all children too
    for (Enumeration<E> e = node.children(); e.hasMoreElements();) {
      if (accept(e.nextElement())) {
        return true;
      }
    }

    return false;
  }
}
