/*
 * Copyright 2012 - 2013 Manuel Laggner
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
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Rectangle;

import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.table.TableCellRenderer;
import javax.swing.tree.TreeModel;

/**
 * @author Manuel Laggner
 * 
 */
public class TreeTableCellRenderer extends JTree implements TableCellRenderer {

  private static final long  serialVersionUID = -1105742558256514707L;

  /** The Constant EVEN_ROW_COLOR. */
  private static final Color EVEN_ROW_COLOR   = new Color(241, 245, 250);

  /** last rendered row */
  // protected int visibleRow;

  protected Rectangle        cellLocation;

  private TreeTable          treeTable;

  public TreeTableCellRenderer(TreeTable treeTable, TreeModel model) {
    super(model);
    this.treeTable = treeTable;

    // set the row height of the JTable
    // has do be done by hand, because the tree is null
    // when super(model) calls setRowHeight
    // setRowHeight(getRowHeight());
    setRowHeight(0);
  }

  // /**
  // * same row height for tree and table
  // */
  // public void setRowHeight(int rowHeight) {
  // if (rowHeight > 0) {
  // super.setRowHeight(rowHeight);
  // if (treeTable != null && treeTable.getRowHeight() != rowHeight) {
  // treeTable.setRowHeight(getRowHeight());
  // }
  // }
  // }

  /**
   * same height for tree and table
   */
  public void setBounds(int x, int y, int w, int h) {
    super.setBounds(x, 0, w, treeTable.getHeight());
  }

  /**
   * insets for the folders
   */
  public void paint(Graphics g) {
    // g.translate(0, -visibleRow * getRowHeight());
    g.translate(0, -cellLocation.y);

    super.paint(g);
  }

  /**
   * gets the renderer
   */
  public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
    if (isSelected)
      setBackground(table.getSelectionBackground());
    else
      setBackground(row % 2 == 0 ? EVEN_ROW_COLOR : Color.WHITE);

    cellLocation = getRowBounds(row);
    table.setRowHeight(row, cellLocation.height);
    // visibleRow = row;
    // cellLocation = table.getCellRect(row, column, false);
    return this;
  }

}
