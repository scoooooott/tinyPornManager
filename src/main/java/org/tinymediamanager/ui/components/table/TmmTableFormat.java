package org.tinymediamanager.ui.components.table;

import ca.odell.glazedlists.gui.AdvancedTableFormat;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;

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

  protected void addColumn(String columnTitle, Function<E, Object> value) {
    Column column = new Column();
    column.columnTitle = columnTitle;
    column.columnValue = value;

    // get the return type of the method apply (from lambda function)
    for (Method method : value.getClass().getMethods()) {
      if ("apply".equals(method.getName())) {
        column.columnClass = method.getReturnType();
      }
    }

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

  public String getColumnIdentifier(int i){
    return columns.get(i).columnIdentifier;
  }

  public class ColumnBuilder {
    private Column column = new Column();

    public ColumnBuilder setTitle(String title) {
      column.columnTitle = title;
      return this;
    }

    public ColumnBuilder setIdentifier(String identifier){
      column.columnIdentifier = identifier;
      return this;
    }

    public ColumnBuilder setColumnValue(Function<E, ?> value, Class clazz) {
      column.columnValue = value;
      column.columnClass = clazz;
      return this;
    }

    public ColumnBuilder setColumnComparator(Comparator comparator){
      column.columnComparator = comparator;
      return this;
    }

    public Column build(){
      return column;
    }
  }

  protected class Column {
    String columnTitle;
    String columnIdentifier;
    Function<E, ?> columnValue;
    Class columnClass;
    Comparator<?> columnComparator;
  }
}
