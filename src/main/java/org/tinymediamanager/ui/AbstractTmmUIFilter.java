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
package org.tinymediamanager.ui;

import java.awt.event.ActionListener;
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
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.SwingPropertyChangeSupport;
import javax.swing.text.JTextComponent;

import org.tinymediamanager.core.UTF8Control;
import org.tinymediamanager.ui.components.TriStateCheckBox;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * An abstract implementation for easier usage of the ITmmUIFilter
 * 
 * @author Manuel Laggner
 */
public abstract class AbstractTmmUIFilter<E> implements ITmmUIFilter<E> {
  protected static final ResourceBundle BUNDLE                = ResourceBundle.getBundle("messages", new UTF8Control());
  /**
   * an object mapper which can be used to transform filters via/to JSON
   */
  protected static ObjectMapper         objectMapper          = new ObjectMapper();

  protected final TriStateCheckBox      checkBox;
  protected final JLabel                label;
  protected final JComponent            filterComponent;
  protected final ActionListener        checkBoxActionListener;
  protected final ActionListener        filterComponentActionListener;
  protected final ChangeListener        changeListener;

  protected final PropertyChangeSupport propertyChangeSupport = new SwingPropertyChangeSupport(this, true);

  public AbstractTmmUIFilter() {
    this.checkBox = new TriStateCheckBox();
    this.checkBox.setToolTipText(BUNDLE.getString("filter.hint"));
    this.label = createLabel();
    this.filterComponent = createFilterComponent();

    // always fire the change event if the checkbox has been changed
    checkBoxActionListener = e -> filterChanged();
    this.checkBox.addActionListener(checkBoxActionListener);

    // the filter components only need to fire the change listener if the checkbox is active
    filterComponentActionListener = e -> {
      if (getFilterState() != FilterState.INACTIVE) {
        filterChanged();
      }
    };
    changeListener = e -> {
      if (getFilterState() != FilterState.INACTIVE) {
        filterChanged();
      }
    };

    if (this.filterComponent instanceof JTextComponent) {
      ((JTextComponent) this.filterComponent).getDocument().addDocumentListener(new DocumentListener() {
        @Override
        public void removeUpdate(DocumentEvent e) {
          if (getFilterState() != FilterState.INACTIVE) {
            filterChanged();
          }
        }

        @Override
        public void insertUpdate(DocumentEvent e) {
          if (getFilterState() != FilterState.INACTIVE) {
            filterChanged();
          }
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
          if (getFilterState() != FilterState.INACTIVE) {
            filterChanged();
          }
        }
      });
    }
    else if (this.filterComponent instanceof AbstractButton) {
      ((AbstractButton) this.filterComponent).addActionListener(filterComponentActionListener);
    }
    else if (this.filterComponent instanceof JComboBox) {
      ((JComboBox<?>) this.filterComponent).addActionListener(filterComponentActionListener);
    }
    else if (this.filterComponent instanceof JSpinner) {
      ((JSpinner) this.filterComponent).addChangeListener(changeListener);
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
   * get the filter state
   *
   * @return the filter state (ACTIVE, ACTIVE_NEGATIVE, INACTIVE)
   */
  @Override
  public FilterState getFilterState() {
    if (checkBox.isMixed()) {
      return FilterState.ACTIVE_NEGATIVE;
    }
    else if (!checkBox.isMixed() && checkBox.isSelected()) {
      return FilterState.ACTIVE;
    }
    return FilterState.INACTIVE;
  }

  /**
   * set the filter state (ACTIVE, ACTIVE_NEGATIVE, INACTIVE)
   *
   * @param state
   *          the state
   */
  @Override
  public void setFilterState(FilterState state) {
    switch (state) {
      case ACTIVE:
        checkBox.setSelected(true);
        break;

      case ACTIVE_NEGATIVE:
        checkBox.setMixed(true);
        break;

      case INACTIVE:
      default:
        checkBox.setSelected(false);
        break;
    }
  }

  /**
   * delegate the filter changed event to our listeners
   */
  protected void filterChanged() {
    SwingUtilities.invokeLater(() -> firePropertyChange(ITmmUIFilter.FILTER_CHANGED, false, true));
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
