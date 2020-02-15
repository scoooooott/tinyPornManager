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

import java.awt.Cursor;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Enumeration;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.apache.commons.lang3.StringUtils;
import org.tinymediamanager.core.UTF8Control;
import org.tinymediamanager.ui.IconManager;
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
  private static final ResourceBundle BUNDLE           = ResourceBundle.getBundle("messages", new UTF8Control());

  protected String                    filterText       = "";

  public TmmTreeTextFilter() {
    super(BUNDLE.getString("tmm.searchfield"), IconManager.SEARCH_GREY);
    lblIcon.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    lblIcon.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent e) {
        if (StringUtils.isNotBlank(getText())) {
          setText("");
        }
      }
    });

    initDocumentListener();
  }

  protected void initDocumentListener() {
    getDocument().addDocumentListener(new DocumentListener() {
      @Override
      public void insertUpdate(final DocumentEvent e) {
        changeIcon();
        updateFilter();
      }

      @Override
      public void removeUpdate(final DocumentEvent e) {
        changeIcon();
        updateFilter();
      }

      @Override
      public void changedUpdate(final DocumentEvent e) {
        changeIcon();
        updateFilter();
      }

      private void changeIcon() {
        if (StringUtils.isBlank(getText())) {
          lblIcon.setIcon(IconManager.SEARCH_GREY);
        }
        else {
          lblIcon.setIcon(IconManager.CLEAR_GREY);
        }
      }

      private void updateFilter() {
        String oldValue = filterText;
        filterText = getText();
        firePropertyChange(ITmmTreeFilter.TREE_FILTER_CHANGED, oldValue, filterText);
      }
    });
  }

  @Override
  public boolean isActive() {
    return StringUtils.isNotBlank(filterText);
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
    for (Enumeration<? extends javax.swing.tree.TreeNode> e = node.children(); e.hasMoreElements();) {
      if (accept((E) e.nextElement())) {
        return true;
      }
    }

    // third: check the parent(s)
    if (checkParent(node.getDataProvider().getParent(node), pattern)) {
      return true;
    }

    return false;
  }

  protected boolean checkParent(TmmTreeNode node, Pattern pattern) {
    if (node == null) {
      return false;
    }

    Matcher matcher = pattern.matcher(node.toString());
    if (matcher.find()) {
      return true;
    }

    return checkParent(node.getDataProvider().getParent(node), pattern);
  }
}
