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

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Rectangle;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.tree.AbstractLayoutCache;
import javax.swing.tree.TreePath;

import org.tinymediamanager.ui.components.tree.TmmTreeNode;

/**
 * An TreeTable aware TableCellRenderer which knows how to paint expansion handles and indent child nodes an appropriate amount.
 *
 * @author Manuel Laggner
 */
public class TmmTreeTableCellRenderer extends DefaultTableCellRenderer {

  private static int                                expansionHandleWidth  = 0;
  private static int                                expansionHandleHeight = 0;
  private boolean                                   expanded              = false;
  private boolean                                   leaf                  = true;
  private boolean                                   showHandle            = true;
  private int                                       nestingDepth          = 0;
  private int                                       labelTextGap          = 0;
  private final JCheckBox                           theCheckBox;
  private JCheckBox                                 checkBox;
  private Reference<TmmTreeTableRenderDataProvider> lastRendererRef       = new WeakReference<>(null);
  private Reference<Object>                         lastRenderedValueRef  = new WeakReference<>(null);
  private static final Border                       expansionBorder       = new ExpansionHandleBorder();

  public TmmTreeTableCellRenderer() {
    theCheckBox = createCheckBox();
  }

  JCheckBox createCheckBox() {
    JCheckBox cb = new JCheckBox();
    cb.setSize(cb.getPreferredSize());
    cb.setBorderPainted(false);
    cb.setOpaque(false);
    return cb;
  }

  @Override
  public final void setBorder(Border b) {
    b = new RestrictedInsetsBorder(b);
    if (b == expansionBorder) {
      super.setBorder(b);
    }
    else {
      super.setBorder(BorderFactory.createCompoundBorder(b, expansionBorder));
    }
  }

  static Icon getExpandedIcon() {
    return UIManager.getIcon("Tree.expandedIcon");
  }

  static Icon getCollapsedIcon() {
    return UIManager.getIcon("Tree.collapsedIcon");
  }

  static int getNestingWidth() {
    return getExpansionHandleWidth();
  }

  static int getExpansionHandleWidth() {
    if (expansionHandleWidth == 0) {
      expansionHandleWidth = getExpandedIcon().getIconWidth();
    }
    return expansionHandleWidth;
  }

  static int getExpansionHandleHeight() {
    if (expansionHandleHeight == 0) {
      expansionHandleHeight = getExpandedIcon().getIconHeight();
    }
    return expansionHandleHeight;
  }

  private void setNestingDepth(int i) {
    nestingDepth = i;
  }

  private void setExpanded(boolean val) {
    expanded = val;
  }

  private void setLeaf(boolean val) {
    leaf = val;
  }

  private void setShowHandle(boolean val) {
    showHandle = val;
  }

  private void setCheckBox(JCheckBox checkBox) {
    this.checkBox = checkBox;
  }

  private boolean isLeaf() {
    return leaf;
  }

  private boolean isExpanded() {
    return expanded;
  }

  private boolean isShowHandle() {
    return showHandle;
  }

  private void setLabelTextGap(int labelTextGap) {
    this.labelTextGap = labelTextGap;
  }

  private int getLabelTextGap() {
    return labelTextGap;
  }

  private int getNestingDepth() {
    return nestingDepth;
  }

  private JCheckBox getCheckBox() {
    return checkBox;
  }

  protected JCheckBox setUpCheckBox(TmmTreeTableCheckRenderDataProvider crendata, Object value, JCheckBox cb) {
    Boolean chSelected = crendata.isSelected(value);
    cb.setEnabled(true);
    cb.setSelected(!Boolean.FALSE.equals(chSelected));

    cb.getModel().setArmed(chSelected == null);
    cb.getModel().setPressed(chSelected == null);
    cb.setEnabled(crendata.isCheckEnabled(value));
    cb.setBackground(getBackground());
    return cb;
  }

  int getTheCheckBoxWidth() {
    return theCheckBox.getSize().width;
  }

  @Override
  public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
    setForeground(null);
    setBackground(null);
    setToolTipText(null);
    setLabelTextGap(0);
    super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

    // put the node text to the tooltip too
    if (value instanceof TmmTreeNode) {
      setToolTipText(value.toString());
    }

    TmmTreeTable tbl = (TmmTreeTable) table;
    if (tbl.isTreeColumnIndex(column)) {
      AbstractLayoutCache layout = tbl.getLayoutCache();
      row = tbl.convertRowIndexToModel(row);
      boolean isleaf = tbl.getTreeTableModel().isLeaf(value);
      setLeaf(isleaf);
      setShowHandle(true);
      TreePath path = layout.getPathForRow(row);
      boolean isExpanded = layout.isExpanded(path);
      setExpanded(isExpanded);
      int nd = path == null ? 0 : path.getPathCount() - (tbl.isRootVisible() ? 1 : 2);
      if (nd < 0) {
        nd = 0;
      }
      setNestingDepth(nd);
      TmmTreeTableRenderDataProvider rendata = tbl.getRenderDataProvider();
      Icon icon = null;
      if (rendata != null && value != null) {
        String displayName = rendata.getDisplayName(value);
        if (displayName != null) {
          setText(displayName);
        }
        lastRendererRef = new WeakReference<>(rendata);
        lastRenderedValueRef = new WeakReference<>(value);
        Color fg = rendata.getForeground(value);
        if (fg != null && !isSelected) {
          setForeground(fg);
        }
        icon = rendata.getIcon(value);

        JCheckBox cb = null;
        if (rendata instanceof TmmTreeTableCheckRenderDataProvider) {
          TmmTreeTableCheckRenderDataProvider crendata = (TmmTreeTableCheckRenderDataProvider) rendata;
          if (crendata.isCheckable(value)) {
            cb = setUpCheckBox(crendata, value, theCheckBox);
          }
        }
        setCheckBox(cb);
      }
      else {
        setCheckBox(null);
      }

      setIcon(icon);
      if (icon == null || icon.getIconWidth() == 0) {
        setLabelTextGap(getIconTextGap());
      }
    }
    else {
      setCheckBox(null);
      setIcon(null);
      setShowHandle(false);
      lastRendererRef = new WeakReference<>(null);
      lastRenderedValueRef = new WeakReference<>(null);
    }

    return this;
  }

  @Override
  public String getToolTipText() {
    TmmTreeTableRenderDataProvider rendata = lastRendererRef.get();
    Object value = lastRenderedValueRef.get();
    if (rendata != null && value != null) {
      String toolT = rendata.getTooltipText(value);
      if (toolT != null && (toolT = toolT.trim()).length() > 0) {
        return toolT;
      }
    }
    return super.getToolTipText();
  }

  private static class ExpansionHandleBorder implements Border {
    private Insets insets = new Insets(0, 0, 0, 0);

    @Override
    public Insets getBorderInsets(Component c) {
      TmmTreeTableCellRenderer ren = (TmmTreeTableCellRenderer) ((JComponent) c).getClientProperty(TmmTreeTableCellRenderer.class);
      if (ren == null) {
        ren = (TmmTreeTableCellRenderer) c;
      }
      if (ren.isShowHandle()) {
        insets.left = getExpansionHandleWidth() + (ren.getNestingDepth() * getNestingWidth()) + ren.getLabelTextGap();
        // Defensively adjust all the insets fields
        insets.top = 1;
        insets.right = 1;
        insets.bottom = 1;
      }
      else {
        // Defensively adjust all the insets fields
        insets.left = 1;
        insets.top = 1;
        insets.right = 1;
        insets.bottom = 1;
      }
      if (ren.getCheckBox() != null) {
        insets.left += ren.getCheckBox().getSize().width;
      }
      return insets;
    }

    @Override
    public boolean isBorderOpaque() {
      return false;
    }

    @Override
    public void paintBorder(Component c, java.awt.Graphics g, int x, int y, int width, int height) {
      TmmTreeTableCellRenderer ren = (TmmTreeTableCellRenderer) ((JComponent) c).getClientProperty(TmmTreeTableCellRenderer.class);
      if (ren == null) {
        ren = (TmmTreeTableCellRenderer) c;
      }
      if (ren.isShowHandle() && !ren.isLeaf()) {
        Icon icon = ren.isExpanded() ? getExpandedIcon() : getCollapsedIcon();
        int iconY;
        int iconX = ren.getNestingDepth() * getNestingWidth();
        if (icon.getIconHeight() < height) {
          iconY = (height / 2) - (icon.getIconHeight() / 2);
        }
        else {
          iconY = 0;
        }

        icon.paintIcon(c, g, iconX, iconY);

      }
      JCheckBox chBox = ren.getCheckBox();
      if (chBox != null) {
        int chBoxX = getExpansionHandleWidth() + ren.getNestingDepth() * getNestingWidth();
        Rectangle bounds = chBox.getBounds();
        int chBoxY;
        if (bounds.getHeight() < height) {
          chBoxY = (height / 2) - (((int) bounds.getHeight()) / 2);
        }
        else {
          chBoxY = 0;
        }
        Dimension chDim = chBox.getSize();
        java.awt.Graphics gch = g.create(chBoxX, chBoxY, chDim.width, chDim.height);
        chBox.paint(gch);
      }
    }
  }

  private static class RestrictedInsetsBorder implements Border {
    private final Border delegate;

    public RestrictedInsetsBorder(Border delegate) {
      this.delegate = delegate;
    }

    @Override
    public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
      delegate.paintBorder(c, g, x, y, width, height);
    }

    @Override
    public Insets getBorderInsets(Component c) {
      Insets insets = delegate.getBorderInsets(c);
      if (insets.top > 1 || insets.left > 1 || insets.bottom > 1 || insets.right > 1) {
        insets = new Insets(Math.min(insets.top, 1), Math.min(insets.left, 1), Math.min(insets.bottom, 1), Math.min(insets.right, 1));
      }
      return insets;
    }

    @Override
    public boolean isBorderOpaque() {
      return delegate.isBorderOpaque();
    }
  }
}
