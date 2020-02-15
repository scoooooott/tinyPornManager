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
package org.tinymediamanager.ui.dialogs;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.util.ResourceBundle;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreePath;

import org.tinymediamanager.core.TmmModuleManager;
import org.tinymediamanager.core.UTF8Control;
import org.tinymediamanager.ui.EqualsLayout;
import org.tinymediamanager.ui.IconManager;
import org.tinymediamanager.ui.components.TmmSplitPane;
import org.tinymediamanager.ui.components.tree.TmmTree;
import org.tinymediamanager.ui.components.tree.TmmTreeNode;
import org.tinymediamanager.ui.components.tree.TmmTreeTextFilter;
import org.tinymediamanager.ui.settings.TmmSettingsDataProvider;
import org.tinymediamanager.ui.settings.TmmSettingsNode;

import net.miginfocom.swing.MigLayout;

/**
 * The class SettingsDialog. For displaying all settings in a dialog
 * 
 * @author Manuel Laggner
 */
public class SettingsDialog extends TmmDialog {
  private static final long              serialVersionUID = 2435834806519338339L;
  /** @wbp.nls.resourceBundle messages */
  private static final ResourceBundle    BUNDLE           = ResourceBundle.getBundle("messages", new UTF8Control());
  private static JDialog                 instance;

  private TmmTree<TmmTreeNode>           tree;
  private JSplitPane                     splitPane;
  private TmmTreeTextFilter<TmmTreeNode> tfFilter;

  /**
   * Get the single instance of the settings dialog
   * 
   * @return the settings dialog
   */
  public static JDialog getInstance() {
    if (instance == null) {
      instance = new SettingsDialog();
    }
    return instance;
  }

  private SettingsDialog() {
    super(BUNDLE.getString("tmm.settings"), "settings");

    initComponents();

    tree.addFilter(tfFilter);
    tree.setRootVisible(false);
    tree.setShowsRootHandles(true);
    tree.setRowHeight(0);
    tree.setCellRenderer(new SettingsTreeCellRenderer());

    tree.addTreeSelectionListener(e -> {
      DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
      if (node != null) {
        // click on a settings node
        if (node.getUserObject() instanceof TmmSettingsNode) {
          TmmSettingsNode tmmSettingsNode = (TmmSettingsNode) node.getUserObject();
          if (tmmSettingsNode.getComponent() != null) {
            JScrollPane scrollPane = new JScrollPane(tmmSettingsNode.getComponent());
            scrollPane.getVerticalScrollBar().setUnitIncrement(16);
            splitPane.setRightComponent(scrollPane);
            revalidate();
          }
        }
      }
    });

    // expand tree nodes
    for (int i = 0; i < tree.getRowCount(); i++) {
      tree.expandRow(i);
    }

    // select first node on creation
    SwingUtilities.invokeLater(() -> {
      DefaultMutableTreeNode firstLeaf = (DefaultMutableTreeNode) ((DefaultMutableTreeNode) tree.getModel().getRoot()).getFirstChild();
      tree.setSelectionPath(new TreePath(((DefaultMutableTreeNode) firstLeaf.getParent()).getPath()));
      tree.setSelectionPath(new TreePath(firstLeaf.getPath()));
      tree.requestFocus();
    });
  }

  private void initComponents() {
    {
      JPanel contentPanel = new JPanel();
      contentPanel.setLayout(new MigLayout("", "[600lp:1000lp,grow]", "[600lp,grow]"));
      getContentPane().add(contentPanel, BorderLayout.CENTER);

      splitPane = new TmmSplitPane();
      contentPanel.add(splitPane, "cell 0 0, grow");

      JPanel panelLeft = new JPanel();
      splitPane.setLeftComponent(panelLeft);
      panelLeft.setLayout(new MigLayout("", "[200lp:200lp,grow]", "[][400lp,grow]"));
      {
        tfFilter = new TmmTreeTextFilter<>();
        panelLeft.add(tfFilter, "cell 0 0,grow");
        tfFilter.setColumns(10);
      }

      JScrollPane scrollPaneLeft = new JScrollPane();
      panelLeft.add(scrollPaneLeft, "cell 0 1,grow");

      tree = new TmmTree<>(new TmmSettingsDataProvider());
      scrollPaneLeft.setViewportView(tree);
      scrollPaneLeft.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    }
    {

    }
    {
      JPanel southPanel = new JPanel();
      getContentPane().add(southPanel, BorderLayout.SOUTH);
      southPanel.setLayout(new MigLayout("insets n 0 0 0, gap rel 0", "[grow][]", "[shrink 0][]"));
      {
        JSeparator separator = new JSeparator();
        southPanel.add(separator, "cell 0 0 2 1,growx");
      }

      JPanel panelButtons = new JPanel();
      EqualsLayout layout = new EqualsLayout(5);
      layout.setMinWidth(100);
      panelButtons.setLayout(layout);
      panelButtons.setBorder(new EmptyBorder(4, 4, 4, 4));
      southPanel.add(panelButtons, "cell 1 1,alignx left,aligny top");

      JButton okButton = new JButton(BUNDLE.getString("Button.close"));
      panelButtons.add(okButton);
      okButton.setAction(new CloseAction());
      getRootPane().setDefaultButton(okButton);
    }
  }

  private class CloseAction extends AbstractAction {
    private static final long serialVersionUID = 2386371884117941373L;

    CloseAction() {
      putValue(NAME, BUNDLE.getString("Button.close"));
      putValue(SMALL_ICON, IconManager.APPLY_INV);
      putValue(LARGE_ICON_KEY, IconManager.APPLY_INV);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      setVisible(false);
    }
  }

  @Override
  public void setVisible(boolean visible) {
    if (!visible) {
      TmmModuleManager.getInstance().saveSettings();
    }
    super.setVisible(visible);
  }

  private class SettingsTreeCellRenderer implements TreeCellRenderer {

    private JLabel label;

    SettingsTreeCellRenderer() {
      label = new JLabel() {
        @Override
        public void updateUI() {
          super.updateUI();
          Object obj = UIManager.get("Tree.nodeBorder");
          if (obj instanceof Border) {
            setBorder((Border) obj);
          }
        }
      };
    }

    @Override
    public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row,
        boolean hasFocus) {
      if (selected) {
        label.setForeground(UIManager.getColor("Tree.selectionForeground"));
      }
      else {
        label.setForeground(UIManager.getColor("Tree.textForeground"));
      }
      label.setText(value.toString());
      label.invalidate();
      return label;
    }
  }
}
