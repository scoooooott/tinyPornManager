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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ResourceBundle;

import javax.swing.AbstractButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.SwingPropertyChangeSupport;
import javax.swing.text.JTextComponent;

/**
 * An abstract implementation for easier usage of the ITmmUIFilter
 * 
 * @author Manuel Laggner
 */
public abstract class AbstractTmmUIFilter<E> implements ITmmUIFilter<E> {
  protected static final ResourceBundle BUNDLE                = ResourceBundle.getBundle("messages", new UTF8Control()); //$NON-NLS-1$

  protected final JCheckBox             checkBox;
  protected final JLabel                label;
  protected final JComponent            filterComponent;

  protected final PropertyChangeSupport propertyChangeSupport = new SwingPropertyChangeSupport(this, true);

  public AbstractTmmUIFilter() {
    this.checkBox = new JCheckBox();
    this.label = createLabel();
    this.filterComponent = createFilterComponent();

    this.checkBox.addActionListener(e -> filterChanged());

    if (this.filterComponent != null && this.filterComponent instanceof JTextComponent) {
      ((JTextComponent) this.filterComponent).getDocument().addDocumentListener(new DocumentListener() {
        @Override
        public void removeUpdate(DocumentEvent e) {
          filterChanged();
        }

        @Override
        public void insertUpdate(DocumentEvent e) {
          filterChanged();
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
          filterChanged();
        }
      });
    }
    else if (this.filterComponent != null && this.filterComponent instanceof AbstractButton) {
      ((AbstractButton) this.filterComponent).addActionListener(e -> filterChanged());
    }
    else if (this.filterComponent != null && this.filterComponent instanceof JComboBox) {
      ((JComboBox<?>) this.filterComponent).addActionListener(e -> filterChanged());
    }
    else if (this.filterComponent != null && this.filterComponent instanceof JSpinner) {
      ((JSpinner) this.filterComponent).addChangeListener(e -> filterChanged());
    }
  }

  @Override
  public JCheckBox getCheckBox() {
    return checkBox;
  }

  @Override
  public JLabel getLabel() {
    return label;
  }

  @Override
  public JComponent getFilterComponent() {
    return filterComponent;
  }

  protected abstract JLabel createLabel();

  protected abstract JComponent createFilterComponent();

  /**
   * is this filter active?
   * 
   * @return true or false
   */
  public boolean isActive() {
    return checkBox.isSelected();
  }

  @Override
  public void setActive(boolean active) {
    checkBox.setSelected(active);
  }

  /**
   * delegate the filter changed event to our listeners
   */
  protected void filterChanged() {
    firePropertyChange(ITmmUIFilter.FILTER_CHANGED, false, true);
  }

  /**
   * Adds the property change listener.
   * 
   * @param listener
   *          the listener
   */
  @Override
  public void addPropertyChangeListener(PropertyChangeListener listener) {
    propertyChangeSupport.addPropertyChangeListener(listener);
  }

  /**
   * Adds the property change listener.
   * 
   * @param propertyName
   *          the property name
   * @param listener
   *          the listener
   */
  @Override
  public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
    propertyChangeSupport.addPropertyChangeListener(propertyName, listener);
  }

  /**
   * Removes the property change listener.
   * 
   * @param listener
   *          the listener
   */
  @Override
  public void removePropertyChangeListener(PropertyChangeListener listener) {
    propertyChangeSupport.removePropertyChangeListener(listener);
  }

  /**
   * Removes the property change listener.
   * 
   * @param propertyName
   *          the property name
   * @param listener
   *          the listener
   */
  @Override
  public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
    propertyChangeSupport.removePropertyChangeListener(propertyName, listener);
  }

  /**
   * Fire property change.
   * 
   * @param propertyName
   *          the property name
   * @param oldValue
   *          the old value
   * @param newValue
   *          the new value
   */
  protected void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
    propertyChangeSupport.firePropertyChange(propertyName, oldValue, newValue);
  }

  /**
   * Fire property change.
   * 
   * @param evt
   *          the evt
   */
  protected void firePropertyChange(PropertyChangeEvent evt) {
    propertyChangeSupport.firePropertyChange(evt);
  }
}
