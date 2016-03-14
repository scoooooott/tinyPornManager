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
package org.tinymediamanager.ui;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;

/**
 * The interface ITmmUIFilter is a generic interface for all types of UI filters inside the tinyMediaManager UI
 * 
 * @author Manuel Laggner
 */
public interface ITmmUIFilter {
  /**
   * Get the JCheckBox for enabling/disabling the filter
   * 
   * @return the JCheckBox to enable/disable the filter
   */
  public JCheckBox getCheckBox();

  /**
   * Get the JLabel for the filter name
   * 
   * @return the JLabel with the filter name
   */
  public JLabel getLabel();

  /**
   * Get the filter component for extended filtering or null if it is not needed
   * 
   * @return the component for extended filtering (e.g. JComboBox for JTextfield) or null
   */
  public JComponent getFilterComponent();
}
