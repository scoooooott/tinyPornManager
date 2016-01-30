/*
 * Copyright 2012 - 2014 Manuel Laggner
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

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Component;
import java.awt.Composite;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.border.AbstractBorder;
import javax.swing.plaf.UIResource;
import javax.swing.tree.DefaultTreeCellRenderer;

import com.jgoodies.forms.layout.FormLayout;

/**
 * The Class TmmTree.
 * 
 * @author Manuel Laggner
 */
public class TmmTree extends JTree {
  private static final long       serialVersionUID = 6080373740630696814L;
  private static Color            BACKGROUND_COLOR = new Color(235, 235, 235);

  private RendererEditorWrapper   wrapper          = null;
  private DefaultTreeCellRenderer defaultRenderer  = new DefaultTreeCellRenderer();

  public TmmTree() {
  }

  public TmmTree(java.util.Hashtable<?, ?> value) {
    super(value);
  }

  public TmmTree(Object[] value) {
    super(value);
  }

  public TmmTree(javax.swing.tree.TreeModel newModel) {
    super(newModel);
  }

  public TmmTree(javax.swing.tree.TreeNode root) {
    super(root);
  }

  public TmmTree(javax.swing.tree.TreeNode root, boolean asksAllowsChildren) {
    super(root, asksAllowsChildren);
  }

  public TmmTree(java.util.Vector<?> value) {
    super(value);
  }

  @Override
  public void paintComponent(java.awt.Graphics g) {
    final Insets insets = getInsets();
    final int w = getWidth() - insets.left - insets.right;
    final int h = getHeight() - insets.top - insets.bottom;
    final int x = insets.left;
    int y = insets.top;

    // paint rows
    int nRows = 0;
    int startRow = 0;
    int rowHeight = getRowHeight();
    if (rowHeight > 0) {
      nRows = h / rowHeight;
    }
    else {
      final int nItems = getRowCount();
      rowHeight = 17; // A default for empty trees
      for (int i = 0; i < nItems; i++, y += rowHeight) {
        Rectangle rect = getRowBounds(i);
        rowHeight = rect != null ? rect.height : rowHeight;
        g.setColor(getSelectionModel().isRowSelected(i) ? defaultRenderer.getBackgroundSelectionColor() : BACKGROUND_COLOR);
        g.fillRect(x, y, w, rowHeight);
      }
      // Use last row height for remainder of tree area
      nRows = nItems + (insets.top + h - y) / rowHeight;
      startRow = nItems;
    }

    for (int i = startRow; i < nRows; i++, y += rowHeight) {
      g.setColor(getSelectionModel().isRowSelected(i) ? defaultRenderer.getBackgroundSelectionColor() : BACKGROUND_COLOR);
      g.fillRect(x, y, w, rowHeight);
    }

    final int remainder = insets.top + h - y;
    if (remainder > 0) {
      g.setColor(BACKGROUND_COLOR);
      g.fillRect(x, y, w, remainder);
    }

    setOpaque(false);
    super.paintComponent(g);
    setOpaque(true);
  }

  @Override
  public javax.swing.tree.TreeCellRenderer getCellRenderer() {
    final javax.swing.tree.TreeCellRenderer ren = super.getCellRenderer();
    if (ren == null)
      return null;
    if (wrapper == null)
      wrapper = new RendererEditorWrapper();
    wrapper.renderer = ren;
    return wrapper;
  }

  @Override
  public javax.swing.tree.TreeCellEditor getCellEditor() {
    final javax.swing.tree.TreeCellEditor ed = super.getCellEditor();
    if (ed == null)
      return null;
    if (wrapper == null)
      wrapper = new RendererEditorWrapper();
    wrapper.editor = ed;
    return wrapper;
  }

  /*
   * helper classes
   */
  public static class BottomBorderBorder extends AbstractBorder implements UIResource {
    private static final long  serialVersionUID = -1431631265848685069L;
    public static final Color  COLOR            = new Color(211, 211, 211);
    private static final Color COLOR2           = new Color(248, 248, 248);

    @Override
    public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
      Graphics2D g2d = (Graphics2D) g;

      g.setColor(COLOR);
      g.drawLine(g.getClipBounds().x, height - 2, g.getClipBounds().width, height - 2);
      g.setColor(COLOR2);

      Composite savedComposite = g2d.getComposite();
      g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
      g.drawLine(g.getClipBounds().x, height - 1, g.getClipBounds().width, height - 1);

      g2d.setComposite(savedComposite);
    }
  }

  private class RendererEditorWrapper implements javax.swing.tree.TreeCellRenderer, javax.swing.tree.TreeCellEditor {
    public javax.swing.tree.TreeCellRenderer renderer = null;
    public javax.swing.tree.TreeCellEditor   editor   = null;

    @Override
    public java.awt.Component getTreeCellRendererComponent(javax.swing.JTree tree, Object value, boolean selected, boolean expanded, boolean leaf,
        int row, boolean hasFocus) {
      final java.awt.Component c = renderer.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
      if (selected)
        return c;
      if (!(c instanceof javax.swing.tree.DefaultTreeCellRenderer))
        c.setBackground(BACKGROUND_COLOR);
      else
        ((javax.swing.tree.DefaultTreeCellRenderer) c).setBackgroundNonSelectionColor(BACKGROUND_COLOR);
      return c;
    }

    @Override
    public java.awt.Component getTreeCellEditorComponent(javax.swing.JTree tree, Object value, boolean selected, boolean expanded, boolean leaf,
        int row) {
      final java.awt.Component c = editor.getTreeCellEditorComponent(tree, value, selected, expanded, leaf, row);
      if (!selected)
        c.setBackground(BACKGROUND_COLOR);
      return c;
    }

    @Override
    public void addCellEditorListener(javax.swing.event.CellEditorListener l) {
      editor.addCellEditorListener(l);
    }

    @Override
    public void cancelCellEditing() {
      editor.cancelCellEditing();
    }

    @Override
    public Object getCellEditorValue() {
      return editor.getCellEditorValue();
    }

    @Override
    public boolean isCellEditable(java.util.EventObject anEvent) {
      return editor.isCellEditable(anEvent);
    }

    @Override
    public void removeCellEditorListener(javax.swing.event.CellEditorListener l) {
      editor.removeCellEditorListener(l);
    }

    @Override
    public boolean shouldSelectCell(java.util.EventObject anEvent) {
      return editor.shouldSelectCell(anEvent);
    }

    @Override
    public boolean stopCellEditing() {
      return editor.stopCellEditing();
    }
  }

  public static class VerticalBorderPanel extends JPanel {
    private static final long serialVersionUID = -3832845181622979771L;
    private final Color       gridColor        = new Color(217, 217, 217);
    private List<Integer>     columnsWithoutBorder;

    public VerticalBorderPanel(int[] columnsWithoutBorder) {
      this.columnsWithoutBorder = new ArrayList<Integer>();
      for (int index = 0; index < columnsWithoutBorder.length; index++) {
        this.columnsWithoutBorder.add(columnsWithoutBorder[index]);
      }
    }

    @Override
    protected void paintComponent(Graphics g) {
      super.paintComponent(g);
      LayoutManager layout = getLayout();
      if (!(layout instanceof FormLayout)) {
        return;
      }
      FormLayout formLayout = (FormLayout) layout;
      FormLayout.LayoutInfo layoutInfo = formLayout.getLayoutInfo(this);
      int height = layoutInfo.getHeight();

      g.setColor(this.gridColor);

      for (int row = 0; row <= layoutInfo.rowOrigins.length - 1; row++) {
        for (int col = 0; col <= layoutInfo.columnOrigins.length - 1; col++) {
          if (columnsWithoutBorder.contains(col)) {
            continue;
          }

          int x = layoutInfo.columnOrigins[col];
          int y = layoutInfo.rowOrigins[row];
          g.drawLine(x, y, x, y + height);

        }
      }
    }
  }
}
