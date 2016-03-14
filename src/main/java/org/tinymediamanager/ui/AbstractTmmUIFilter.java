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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.JTextComponent;

/**
 * An abstract implementation for easier usage of the ITmmUIFilter
 * 
 * @author Manuel Laggner
 */
public abstract class AbstractTmmUIFilter implements ITmmUIFilter {
  protected final JCheckBox  checkBox;
  protected final JLabel     label;
  protected final JComponent filterComponent;

  public AbstractTmmUIFilter() {
    this.checkBox = new JCheckBox();
    this.label = createLabel();
    this.filterComponent = createFilterComponent();

    this.checkBox.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        filterChanged();
      }
    });

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
      ((AbstractButton) this.filterComponent).addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
          filterChanged();
        }
      });
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

  protected abstract void filterChanged();
}
