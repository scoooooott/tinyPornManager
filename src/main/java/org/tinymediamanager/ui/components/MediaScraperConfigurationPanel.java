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
package org.tinymediamanager.ui.components;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.ResourceBundle;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.apache.commons.lang3.StringUtils;
import org.tinymediamanager.scraper.config.IConfigureableMediaProvider;
import org.tinymediamanager.ui.IconManager;
import org.tinymediamanager.ui.UTF8Control;

/**
 * The class MediaScraperConfigurationPanel is used to display configurable scraper options
 * 
 * @author Manuel Laggner
 */
public class MediaScraperConfigurationPanel extends JPanel {
  private static final long           serialVersionUID = -4120483383064864579L;
  /**
   * @wbp.nls.resourceBundle messages
   */
  private static final ResourceBundle BUNDLE           = ResourceBundle.getBundle("messages", new UTF8Control()); //$NON-NLS-1$

  private IConfigureableMediaProvider mediaProvider;
  private boolean                     dirty            = false;

  private JPanel                      configPanel;

  public MediaScraperConfigurationPanel(IConfigureableMediaProvider mediaProvider) {
    this.mediaProvider = mediaProvider;

    setLayout(new BorderLayout());

    JPanel panelHead = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 0));
    add(panelHead, BorderLayout.NORTH);

    JLabel lblScraperOptions = new JLabel(BUNDLE.getString("Settings.scraper.options")); //$NON-NLS-1$
    panelHead.add(lblScraperOptions);

    configPanel = createConfigPanel();
    add(configPanel, BorderLayout.CENTER);

    // add a listener to determine when to save the settings
    addAncestorListener(new AncestorListener() {
      @Override
      public void ancestorRemoved(AncestorEvent event) {
        // check if anything has been changed
        if (dirty) {
          saveSettings();
        }
      }

      @Override
      public void ancestorMoved(AncestorEvent event) {
      }

      @Override
      public void ancestorAdded(AncestorEvent event) {
      }
    });
  }

  private JPanel createConfigPanel() {
    JPanel panel = new JPanel();
    GridBagLayout gridBagLayout = new GridBagLayout();
    gridBagLayout.columnWidths = new int[] { 0 };
    gridBagLayout.rowHeights = new int[] { 0 };
    gridBagLayout.columnWeights = new double[] { Double.MIN_VALUE };
    gridBagLayout.rowWeights = new double[] { Double.MIN_VALUE };
    panel.setLayout(gridBagLayout);

    GridBagConstraints constraints = new GridBagConstraints();
    constraints.gridy = 0;

    // build up the panel for being displayed in the popup
    for (Entry<String, Object> entry : mediaProvider.getProviderSettings().entrySet()) {
      constraints.anchor = GridBagConstraints.LINE_START;
      constraints.ipadx = 20;

      // label
      JLabel label = new JLabel(entry.getKey());
      constraints.gridx = 0;
      panel.add(label, constraints);

      JComponent comp;
      if (entry.getValue() instanceof Boolean) {
        JCheckBox checkbox = new JCheckBox();
        checkbox.setSelected((Boolean) entry.getValue());
        checkbox.addActionListener(new ActionListener() {
          @Override
          public void actionPerformed(ActionEvent e) {
            dirty = true;
          }
        });
        comp = checkbox;
      }
      else {
        JTextField tf = new JTextField(String.valueOf(entry.getValue()));
        tf.getDocument().addDocumentListener(new DocumentListener() {
          @Override
          public void removeUpdate(DocumentEvent e) {
            dirty = true;
          }

          @Override
          public void insertUpdate(DocumentEvent e) {
            dirty = true;
          }

          @Override
          public void changedUpdate(DocumentEvent e) {
            dirty = true;
          }
        });
        comp = tf;
      }
      comp.putClientProperty(entry.getKey(), entry.getKey());
      constraints.ipadx = 0;
      constraints.gridx = 1;
      panel.add(comp, constraints);

      // add a hint if a long text has been found
      try {
        String desc = BUNDLE.getString("scraper." + mediaProvider.getProviderInfo().getId() + "." + entry.getKey() + ".desc"); //$NON-NLS-1$
        if (StringUtils.isNotBlank(desc)) {
          JLabel lblHint = new JLabel(IconManager.HINT);
          lblHint.setToolTipText(desc);
          constraints.gridx = 2;
          panel.add(lblHint, constraints);
        }
      }
      catch (Exception ignored) {
      }
      constraints.gridy++;
    }
    return panel;
  }

  private void saveSettings() {
    Map<String, Object> newConfig = new HashMap<>();
    Map<String, Object> config = mediaProvider.getProviderSettings();
    // transfer the items from the components to the config
    for (Entry<String, Object> entry : config.entrySet()) {
      for (Component comp : configPanel.getComponents()) {
        // get the right component for this setting
        if (!(comp instanceof JComponent)) {
          continue;
        }

        Object param = ((JComponent) comp).getClientProperty(entry.getKey());
        if (param == null || !entry.getKey().equals(param)) {
          continue;
        }

        // parse the value and write it back to the new config
        try {
          if (comp instanceof JCheckBox) {
            newConfig.put(entry.getKey(), ((JCheckBox) comp).isSelected());
          }
          else {
            Method method = param.getClass().getMethod("valueOf", String.class);
            if (method != null) {
              newConfig.put(entry.getKey(), method.invoke(null, ((JTextField) comp).getText()));
            }
          }
        }
        catch (Exception ignored) {
        }
      }
    }

    mediaProvider.setProviderSettings(newConfig);
  }
}
