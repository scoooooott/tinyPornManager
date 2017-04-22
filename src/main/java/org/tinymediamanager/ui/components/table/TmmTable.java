/*
 * Copyright 2012 - 2017 Manuel Laggner
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
package org.tinymediamanager.ui.components.table;

import java.awt.Color;
import java.awt.Component;
import java.awt.ComponentOrientation;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.function.Consumer;

import javax.swing.BorderFactory;
import javax.swing.CellRendererPane;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JViewport;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

import org.apache.commons.lang3.StringUtils;
import org.tinymediamanager.ui.IconManager;
import org.tinymediamanager.ui.UTF8Control;

/**
 * The Class TmmTable. It's being used to draw the tables like our designer designed it
 *
 * @author Manuel Laggner
 */
public class TmmTable extends JTable {
  private static final long             serialVersionUID  = 6150939811851709115L;
  private static final ResourceBundle   BUNDLE            = ResourceBundle.getBundle("messages", new UTF8Control());

  protected static final Color          TABLE_GRID_COLOR  = new Color(211, 211, 211);
  protected static final Color          TABLE_GRID_COLOR2 = new Color(248, 248, 248);

  private static final CellRendererPane CELL_RENDER_PANE  = new CellRendererPane();

  public TmmTable() {
    super();
    init();
  }

  public TmmTable(TableModel dm) {
    setModel(dm);
    init();
  }

  /**
   * Overridden to use TmmTableColumnModel as TableColumnModel.
   *
   * @see javax.swing.JTable#createDefaultColumnModel()
   */
  @Override
  protected TableColumnModel createDefaultColumnModel() {
    return new TmmTableColumnModel();
  }

  @Override
  public void addColumn(TableColumn aColumn) {
    if (aColumn.getIdentifier() == null && getModel() instanceof TmmTableModel) {
      // disable grid in header
      aColumn.setHeaderRenderer(new BottomBorderHeaderRenderer());

      TmmTableModel tableModel = ((TmmTableModel) getModel());
      tableModel.setUpColumn(aColumn);
    }
    super.addColumn(aColumn);
  }

  private void init() {
    setTableHeader(createTableHeader());
    getTableHeader().setReorderingAllowed(false);
    getTableHeader().setOpaque(false);
    setOpaque(false);
    setRowHeight(22);
    setGridColor(TABLE_GRID_COLOR);
    setIntercellSpacing(new Dimension(0, 0));
    // turn off grid painting as we'll handle this manually in order to paint grid lines over the entire viewport.
    setShowGrid(false);
  }

  public void writeHiddenColumns(Consumer<List<String>> setting) {
    List<String> hiddenColumns = new ArrayList<>();
    if (getColumnModel() instanceof TmmTableColumnModel) {
      List<TableColumn> cols = ((TmmTableColumnModel) getColumnModel()).getHiddenColumns();
      for (TableColumn col : cols) {
        if (col.getIdentifier() instanceof String && StringUtils.isNotBlank((String) col.getIdentifier())) {
          hiddenColumns.add((String) col.getIdentifier());
        }
      }
    }
    setting.accept(hiddenColumns);
  }

  public void readHiddenColumns(List<String> hiddenColumns) {
    if (getColumnModel() instanceof TmmTableColumnModel) {
      ((TmmTableColumnModel) getColumnModel()).setHiddenColumns(hiddenColumns);
    }
  }

  /**
   * Set the preferred width of all columns according to its contents If a column is marked as non resizeable, the max-width is set
   *
   * @param margin
   *          the margin left and right
   */
  public void adjustColumnPreferredWidths(int margin) {
    // strategy - get max width for cells in header and column and
    // make that the preferred width
    TableColumnModel columnModel = getColumnModel();
    for (int col = 0; col < getColumnCount(); col++) {

      int maxwidth = 0;
      // header
      TableCellRenderer rend = columnModel.getColumn(col).getHeaderRenderer();
      Object value = columnModel.getColumn(col).getHeaderValue();
      if (rend == null) {
        rend = getTableHeader().getDefaultRenderer();
      }
      Component comp = rend.getTableCellRendererComponent(this, value, false, false, -1, col);
      maxwidth = Math.max(comp.getPreferredSize().width + 2 * margin, maxwidth);

      // rows
      for (int row = 0; row < getRowCount(); row++) {
        rend = getCellRenderer(row, col);
        value = getValueAt(row, col);
        comp = rend.getTableCellRendererComponent(this, value, false, false, row, col);
        maxwidth = Math.max(comp.getPreferredSize().width + margin, maxwidth);
      }

      TableColumn column = columnModel.getColumn(col);
      column.setPreferredWidth(maxwidth);
      if (!column.getResizable()) {
        column.setMinWidth(maxwidth);
        column.setMaxWidth(maxwidth);
      }
    }
  }

  protected JTableHeader createTableHeader() {
    return new JTableHeader(getColumnModel()) {
      private static final long serialVersionUID = 1652463935117013248L;

      @Override
      protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        // if this JTableHeader is parented in a JViewport, then paint the
        // table header background to the right of the last column if necessary.
        JViewport viewport = (JViewport) table.getParent();
        if (viewport != null && table.getWidth() < viewport.getWidth()) {
          int x = table.getWidth();
          int width = viewport.getWidth() - table.getWidth();
          paintHeader(g, getTable(), x, width);
        }
      }
    };
  }

  public void setNewFontSize(float size) {
    setFont(getFont().deriveFont(size));
    FontMetrics fm = getFontMetrics(getFont());
    setRowHeight(fm.getHeight() + 4);
  }

  protected static void paintHeader(Graphics g, JTable table, int x, int width) {
    TableCellRenderer renderer = new BottomBorderHeaderRenderer();
    Component component = renderer.getTableCellRendererComponent(table, "", false, false, -1, 2);

    component.setBounds(0, 0, width, table.getTableHeader().getHeight());

    ((JComponent) component).setOpaque(false);
    CELL_RENDER_PANE.paintComponent(g, component, null, x, 0, width, table.getTableHeader().getHeight(), true);
  }

  @Override
  public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
    Component component = super.prepareRenderer(renderer, row, column);
    // if the renderer is a JComponent and the given row isn't part of a
    // selection, make the renderer non-opaque so that striped rows show through.
    if (component instanceof JComponent) {
      ((JComponent) component).setOpaque(getSelectionModel().isSelectedIndex(row));
    }
    return component;
  }

  /**
   * Overridden to install special button into the upper right hand corner.
   *
   * @see javax.swing.JTable#configureEnclosingScrollPane()
   */
  @Override
  protected void configureEnclosingScrollPane() {
    super.configureEnclosingScrollPane();

    Container p = getParent();
    if (p instanceof JViewport) {
      Container parent = p.getParent();
      if (parent instanceof JScrollPane) {
        JScrollPane scrollPane = (JScrollPane) parent;
        // Make certain we are the viewPort's view and not, for
        // example, the rowHeaderView of the scrollPane -
        // an implementor of fixed columns might do this.
        JViewport viewport = scrollPane.getViewport();
        if (viewport == null || viewport.getView() != this) {
          return;
        }

        final JButton b = new JButton(IconManager.CONFIGURE);
        b.setOpaque(false);
        b.putClientProperty("flatButton", Boolean.TRUE);
        b.setToolTipText(BUNDLE.getString("Button.selectvisiblecolumns"));
        b.setBorder(BorderFactory.createEmptyBorder());
        b.updateUI();
        // b.getAccessibleContext().setAccessibleName(selectVisibleColumnsLabel);
        // b.getAccessibleContext().setAccessibleDescription(selectVisibleColumnsLabel);
        b.addActionListener(evt -> TmmTableColumnSelectionPopup.showColumnSelectionPopup(b, TmmTable.this));
        b.addMouseListener(new MouseAdapter() {
          @Override
          public void mouseClicked(MouseEvent me) {
            TmmTableColumnSelectionPopup.showColumnSelectionPopup(b, TmmTable.this);
          }
        });
        b.setFocusable(false);
        scrollPane.setCorner(JScrollPane.UPPER_RIGHT_CORNER, b);
      }
    }
  }

  public void configureScrollPane(JScrollPane scrollPane) {
    int[] columnsWithoutRightVerticalGrid = {};
    configureScrollPane(scrollPane, columnsWithoutRightVerticalGrid);
  }

  public void configureScrollPane(JScrollPane scrollPane, int[] columnsWithoutRightVerticalGrid) {
    if (!(scrollPane.getViewport() instanceof TmmViewport)) {
      scrollPane.setViewport(new TmmViewport(this, columnsWithoutRightVerticalGrid));
      scrollPane.getViewport().setView(this);
    }
    scrollPane.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, TABLE_GRID_COLOR));
    scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
  }

  protected static class BottomBorderHeaderRenderer extends DefaultTableCellRenderer {
    private static final long serialVersionUID = 7963585655106103415L;

    public BottomBorderHeaderRenderer() {
      setHorizontalAlignment(SwingConstants.CENTER);
      setOpaque(true);

      // This call is needed because DefaultTableCellRenderer calls setBorder()
      // in its constructor, which is executed after updateUI()
      setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, TABLE_GRID_COLOR));
    }

    @Override
    public void updateUI() {
      super.updateUI();
      setBorder(BorderFactory.createEmptyBorder());
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean selected, boolean focused, int row, int column) {
      JTableHeader h = table != null ? table.getTableHeader() : null;

      if (h != null) {
        setEnabled(h.isEnabled());
        setComponentOrientation(h.getComponentOrientation());

        setForeground(h.getForeground());
        setBackground(h.getBackground());
        setFont(h.getFont());
      }
      else {
        /* Use sensible values instead of random leftover values from the last call */
        setEnabled(true);
        setComponentOrientation(ComponentOrientation.UNKNOWN);

        setForeground(UIManager.getColor("TableHeader.foreground"));
        setBackground(UIManager.getColor("TableHeader.background"));
        setFont(UIManager.getFont("TableHeader.font"));
      }

      if (value instanceof ImageIcon) {
        setIcon((ImageIcon) value);
        setText("");
      }
      else {
        setText((value == null) ? "" : value.toString());
        setIcon(null);
        setHorizontalAlignment(JLabel.CENTER);
      }

      return this;
    }
  }
}
