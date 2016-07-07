package org.tinymediamanager.ui.components.table;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.function.Function;

import javax.swing.ImageIcon;
import javax.swing.table.TableCellRenderer;

import org.apache.commons.lang3.StringUtils;
import org.tinymediamanager.ui.IconManager;

import ca.odell.glazedlists.gui.AdvancedTableFormat;

/**
 * The abstract TmmTableFormat is a convenience wrapper for the @see com.glazedlists.AdvancedTableFormat
 *
 * @author Manuel Laggner
 */
public abstract class TmmTableFormat<E> implements AdvancedTableFormat<E> {

  protected List<Column> columns = new ArrayList<>();

  protected void addColumn(Column column) {
    columns.add(column);
  }

  @Override
  public Class getColumnClass(int i) {
    return columns.get(i).columnClass;
  }

  @Override
  public Comparator getColumnComparator(int i) {
    return columns.get(i).columnComparator;
  }

  @Override
  public int getColumnCount() {
    return columns.size();
  }

  @Override
  public String getColumnName(int i) {
    return columns.get(i).columnTitle;
  }

  @Override
  public Object getColumnValue(E e, int i) {
    return columns.get(i).columnValue.apply(e);
  }

  public String getColumnIdentifier(int i) {
    return columns.get(i).columnIdentifier;
  }

  public TableCellRenderer getCellRenderer(int i) {
    return columns.get(i).cellRenderer;
  }

  public ImageIcon getHeaderIcon(int i) {
    return columns.get(i).headerIcon;
  }

  public boolean getColumnResizeable(int i) {
    return columns.get(i).columnResizeable;
  }

  protected class Column {
    private String            columnTitle;
    private String            columnIdentifier;
    private Function<E, ?>    columnValue;
    private Class             columnClass;
    private Comparator<?>     columnComparator = null;
    private TableCellRenderer cellRenderer     = null;
    private ImageIcon         headerIcon       = null;
    private boolean           columnResizeable = true;

    public Column(String title, String identifier, Function<E, ?> value, Class clazz) {
      columnTitle = title;
      columnIdentifier = identifier;
      columnValue = value;
      columnClass = clazz;
    }

    public void setColumnComparator(Comparator comparator) {
      columnComparator = comparator;
    }

    public void setCellRenderer(TableCellRenderer renderer) {
      cellRenderer = renderer;
    }

    public void setHeaderIcon(ImageIcon icon) {
      headerIcon = icon;
    }

    public void setColumnResizeable(boolean resizeable) {
      columnResizeable = resizeable;
    }
  }

  protected ImageIcon getCheckIcon(boolean bool) {
    if (bool) {
      return IconManager.DOT_AVAILABLE;
    }
    return IconManager.DOT_UNAVAILABLE;
  }

  public class StringComparator implements Comparator<String> {
    @Override
    public int compare(String arg0, String arg1) {
      if (StringUtils.isEmpty(arg0)) {
        return -1;
      }
      if (StringUtils.isEmpty(arg1)) {
        return 1;
      }
      return arg0.toLowerCase().compareTo(arg1.toLowerCase());
    }
  }

  public class FloatComparator implements Comparator<Float> {
    @Override
    public int compare(Float arg0, Float arg1) {
      return arg0.compareTo(arg1);
    }
  }

  public class ImageComparator implements Comparator<ImageIcon> {
    @Override
    public int compare(ImageIcon arg0, ImageIcon arg1) {
      if (arg0 == arg1) {
        return 0;
      }
      if (arg0 == IconManager.DOT_AVAILABLE) {
        return 1;
      }
      return -1;
    }
  }

  public class DateComparator implements Comparator<Date> {
    @Override
    public int compare(Date arg0, Date arg1) {
      return arg0.compareTo(arg1);
    }
  }
}
