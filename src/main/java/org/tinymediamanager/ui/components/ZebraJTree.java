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
package org.tinymediamanager.ui.components;

import java.awt.Color;
import java.awt.Rectangle;

import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;

/**
 * The Class ZebraTree.
 * 
 * @author Manuel Laggner
 */
public class ZebraJTree extends JTree {

  /** The Constant serialVersionUID. */
  private static final long       serialVersionUID = -8401898893090686850L;

  /** The row colors. */
  public java.awt.Color           rowColors[]      = new java.awt.Color[2];

  /** The draw stripes. */
  private boolean                 drawStripes      = false;

  /** The default renderer. */
  private DefaultTreeCellRenderer defaultRenderer  = new DefaultTreeCellRenderer();

  /**
   * Instantiates a new zebra j tree.
   */
  public ZebraJTree() {
  }

  /**
   * Instantiates a new zebra j tree.
   * 
   * @param value
   *          the value
   */
  public ZebraJTree(java.util.Hashtable<?, ?> value) {
    super(value);
  }

  /**
   * Instantiates a new zebra j tree.
   * 
   * @param value
   *          the value
   */
  public ZebraJTree(Object[] value) {
    super(value);
  }

  /**
   * Instantiates a new zebra j tree.
   * 
   * @param newModel
   *          the new model
   */
  public ZebraJTree(javax.swing.tree.TreeModel newModel) {
    super(newModel);
  }

  /**
   * Instantiates a new zebra j tree.
   * 
   * @param root
   *          the root
   */
  public ZebraJTree(javax.swing.tree.TreeNode root) {
    super(root);
  }

  /**
   * Instantiates a new zebra j tree.
   * 
   * @param root
   *          the root
   * @param asksAllowsChildren
   *          the asks allows children
   */
  public ZebraJTree(javax.swing.tree.TreeNode root, boolean asksAllowsChildren) {
    super(root, asksAllowsChildren);
  }

  /**
   * Instantiates a new zebra j tree.
   * 
   * @param value
   *          the value
   */
  public ZebraJTree(java.util.Vector<?> value) {
    super(value);
  }

  /**
   * Add zebra stripes to the background.
   * 
   * @param g
   *          the g
   */
  public void paintComponent(java.awt.Graphics g) {
    if (!(drawStripes = isOpaque())) {
      super.paintComponent(g);
      return;
    }

    // Paint zebra background stripes
    updateZebraColors();
    final java.awt.Insets insets = getInsets();
    final int w = getWidth() - insets.left - insets.right;
    final int h = getHeight() - insets.top - insets.bottom;
    final int x = insets.left;
    int y = insets.top;
    int nRows = 0;
    int startRow = 0;
    int rowHeight = getRowHeight();
    if (rowHeight > 0) {
      nRows = h / rowHeight;
    }
    else {
      // Paint non-uniform height rows first
      final int nItems = getRowCount();
      rowHeight = 17; // A default for empty trees
      for (int i = 0; i < nItems; i++, y += rowHeight) {
        Rectangle rect = getRowBounds(i);
        rowHeight = rect != null ? rect.height : rowHeight;
        g.setColor(getSelectionModel().isRowSelected(i) ? defaultRenderer.getBackgroundSelectionColor() : rowColors[i & 1]);
        g.fillRect(x, y, w, rowHeight);
      }
      // Use last row height for remainder of tree area
      nRows = nItems + (insets.top + h - y) / rowHeight;
      startRow = nItems;
    }

    for (int i = startRow; i < nRows; i++, y += rowHeight) {
      g.setColor(getSelectionModel().isRowSelected(i) ? defaultRenderer.getBackgroundSelectionColor() : rowColors[i & 1]);
      g.fillRect(x, y, w, rowHeight);
    }

    final int remainder = insets.top + h - y;
    if (remainder > 0) {
      g.setColor(rowColors[nRows & 1]);
      g.fillRect(x, y, w, remainder);
    }

    // Paint component
    setOpaque(false);
    super.paintComponent(g);
    setOpaque(true);
  }

  /**
   * Wrap cell renderer and editor to add zebra background stripes.
   * 
   * @author Manuel Laggner
   */
  private class RendererEditorWrapper implements javax.swing.tree.TreeCellRenderer, javax.swing.tree.TreeCellEditor {

    /** The ren. */
    public javax.swing.tree.TreeCellRenderer ren = null;

    /** The ed. */
    public javax.swing.tree.TreeCellEditor   ed  = null;

    /*
     * (non-Javadoc)
     * 
     * @see javax.swing.tree.TreeCellRenderer#getTreeCellRendererComponent(javax.swing.JTree, java.lang.Object, boolean, boolean, boolean, int,
     * boolean)
     */
    public java.awt.Component getTreeCellRendererComponent(javax.swing.JTree tree, Object value, boolean selected, boolean expanded, boolean leaf,
        int row, boolean hasFocus) {
      final java.awt.Component c = ren.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
      if (selected || !drawStripes)
        return c;
      if (!(c instanceof javax.swing.tree.DefaultTreeCellRenderer))
        c.setBackground(rowColors[row & 1]);
      else
        ((javax.swing.tree.DefaultTreeCellRenderer) c).setBackgroundNonSelectionColor(rowColors[row & 1]);
      return c;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.swing.tree.TreeCellEditor#getTreeCellEditorComponent(javax.swing.JTree, java.lang.Object, boolean, boolean, boolean, int)
     */
    public java.awt.Component getTreeCellEditorComponent(javax.swing.JTree tree, Object value, boolean selected, boolean expanded, boolean leaf,
        int row) {
      final java.awt.Component c = ed.getTreeCellEditorComponent(tree, value, selected, expanded, leaf, row);
      if (!selected && drawStripes)
        c.setBackground(rowColors[row & 1]);
      return c;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.swing.CellEditor#addCellEditorListener(javax.swing.event.CellEditorListener)
     */
    public void addCellEditorListener(javax.swing.event.CellEditorListener l) {
      ed.addCellEditorListener(l);
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.swing.CellEditor#cancelCellEditing()
     */
    public void cancelCellEditing() {
      ed.cancelCellEditing();
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.swing.CellEditor#getCellEditorValue()
     */
    public Object getCellEditorValue() {
      return ed.getCellEditorValue();
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.swing.CellEditor#isCellEditable(java.util.EventObject)
     */
    public boolean isCellEditable(java.util.EventObject anEvent) {
      return ed.isCellEditable(anEvent);
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.swing.CellEditor#removeCellEditorListener(javax.swing.event.CellEditorListener)
     */
    public void removeCellEditorListener(javax.swing.event.CellEditorListener l) {
      ed.removeCellEditorListener(l);
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.swing.CellEditor#shouldSelectCell(java.util.EventObject)
     */
    public boolean shouldSelectCell(java.util.EventObject anEvent) {
      return ed.shouldSelectCell(anEvent);
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.swing.CellEditor#stopCellEditing()
     */
    public boolean stopCellEditing() {
      return ed.stopCellEditing();
    }
  }

  /** The wrapper. */
  private RendererEditorWrapper wrapper = null;

  /**
   * Return the wrapped cell renderer.
   * 
   * @return the cell renderer
   */
  public javax.swing.tree.TreeCellRenderer getCellRenderer() {
    final javax.swing.tree.TreeCellRenderer ren = super.getCellRenderer();
    if (ren == null)
      return null;
    if (wrapper == null)
      wrapper = new RendererEditorWrapper();
    wrapper.ren = ren;
    return wrapper;
  }

  /**
   * Return the wrapped cell editor.
   * 
   * @return the cell editor
   */
  public javax.swing.tree.TreeCellEditor getCellEditor() {
    final javax.swing.tree.TreeCellEditor ed = super.getCellEditor();
    if (ed == null)
      return null;
    if (wrapper == null)
      wrapper = new RendererEditorWrapper();
    wrapper.ed = ed;
    return wrapper;
  }

  /**
   * Compute zebra background stripe colors.
   * 
   */
  private void updateZebraColors() {
    if ((rowColors[0] = getBackground()) == null) {
      rowColors[0] = rowColors[1] = java.awt.Color.white;
      return;
    }

    java.awt.Color sel = javax.swing.UIManager.getColor("Tree.selectionBackground");
    if (sel == null) {
      sel = java.awt.SystemColor.textHighlight;
    }

    if (sel == null) {
      rowColors[1] = rowColors[0];
      return;
    }

    // final float[] bgHSB = java.awt.Color.RGBtoHSB(rowColors[0].getRed(), rowColors[0].getGreen(), rowColors[0].getBlue(), null);
    // final float[] selHSB = java.awt.Color.RGBtoHSB(sel.getRed(), sel.getGreen(), sel.getBlue(), null);

    // rowColors[1] = java.awt.Color.getHSBColor((selHSB[1] == 0.0 || selHSB[2] == 0.0) ? bgHSB[0] : selHSB[0], 0.1f * selHSB[1] + 0.9f * bgHSB[1],
    // bgHSB[2] + ((bgHSB[2] < 0.5f) ? 0.05f : -0.05f));
    rowColors[1] = new Color(241, 245, 250);
  }

}
