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

package org.tinymediamanager.ui.movies.filters;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.JComponent;

import org.tinymediamanager.ui.components.combobox.TmmCheckComboBox;

/**
 * the class {@link AbstractCheckComboBoxMovieUIFilter<E>} is used as a helper class to avoid much boilerplate code in filters with a
 * {@link TmmCheckComboBox}
 * 
 * @param <E>
 *          the type for the {@link TmmCheckComboBox}
 *
 * @author Manuel Laggner
 */
abstract class AbstractCheckComboBoxMovieUIFilter<E> extends AbstractMovieUIFilter {

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
    checkComboBox.removeActionListener(filterComponentActionListener);

    List<E> selectedItems = checkComboBox.getSelectedItems();

    checkComboBox.setItems(values);

    if (!selectedItems.isEmpty()) {
      checkComboBox.setSelectedItems(selectedItems);
    }

    // re-add the itemlistener
    checkComboBox.addActionListener(filterComponentActionListener);
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
