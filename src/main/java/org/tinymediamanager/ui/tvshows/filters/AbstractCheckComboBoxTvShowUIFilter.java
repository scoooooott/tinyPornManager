package org.tinymediamanager.ui.tvshows.filters;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.JComponent;

import org.tinymediamanager.ui.components.combobox.TmmCheckComboBox;

/**
 * the class {@link AbstractCheckComboBoxTvShowUIFilter <E>} is used as a helper class to avoid much boilerplate code in filters with a
 * {@link TmmCheckComboBox}
 * 
 * @param <E>
 *          the type for the {@link TmmCheckComboBox}
 *
 * @author Manuel Laggner
 */
abstract class AbstractCheckComboBoxTvShowUIFilter<E> extends AbstractTvShowUIFilter {

  protected TmmCheckComboBox<E> checkComboBox;

  @Override
  protected JComponent createFilterComponent() {
    checkComboBox = new TmmCheckComboBox<>();
    return checkComboBox;
  }

  @Override
  public String getFilterValueAsString() {
    try {
      List<String> values = new ArrayList<>();

      for (E type : checkComboBox.getSelectedItems()) {
        values.add(parseTypeToString(type));
      }

      return objectMapper.writeValueAsString(values);
    }
    catch (Exception e) {
      return null;
    }
  }

  @Override
  public void setFilterValue(Object value) {
    List<E> selectedItems = new ArrayList<>();

    try {
      List<String> values = objectMapper.readValue((String) value, objectMapper.getTypeFactory().constructCollectionType(List.class, String.class));

      for (String valueAsString : values) {
        selectedItems.add(parseStringToType(valueAsString));
      }

    }
    catch (Exception ignored) {
    }

    checkComboBox.setSelectedItems(selectedItems);
  }

  /**
   * set the given values into the {@link TmmCheckComboBox}
   *
   * @param values
   *          a list of the values to be set
   */
  protected void setValues(List<E> values) {
    // remove the listener to not firing unnecessary events
    checkComboBox.removeActionListener(actionListener);

    List<E> selectedItems = checkComboBox.getSelectedItems();

    checkComboBox.setItems(values);

    if (!selectedItems.isEmpty()) {
      checkComboBox.setSelectedItems(selectedItems);
    }

    // re-add the itemlistener
    checkComboBox.addActionListener(actionListener);
  }

  /**
   * set the given values into the {@link TmmCheckComboBox}
   *
   * @param values
   *          a list of the values to be set
   */
  protected void setValues(E... values) {
    setValues(Arrays.asList(values));
  }

  /**
   * parse a given instance of the type E into a {@link String}
   * 
   * @param type
   *          the instance of E
   * @return a string containing the {@link String} value of the instance of E
   * @throws Exception
   *           any exception occurred
   */
  protected abstract String parseTypeToString(E type) throws Exception;

  /**
   * parse a given {@link String} into an instance of the type E
   * 
   * @param string
   *          the {@link String} to parse
   * @return an instance of the type E
   * @throws Exception
   *           any exception occurred
   */
  protected abstract E parseStringToType(String string) throws Exception;
}
