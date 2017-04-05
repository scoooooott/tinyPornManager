/*
 * Copyright 2012 - 2017 Manuel Laggner
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

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.tinymediamanager.core.Message;
import org.tinymediamanager.ui.EqualsLayout;
import org.tinymediamanager.ui.MainWindow;
import org.tinymediamanager.ui.TmmUIMessageCollector;
import org.tinymediamanager.ui.TmmWindowSaver;
import org.tinymediamanager.ui.UTF8Control;
import org.tinymediamanager.ui.panels.MessagePanel;

import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.FormSpecs;
import com.jgoodies.forms.layout.RowSpec;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.event.ListEvent;
import ca.odell.glazedlists.event.ListEventListener;

/**
 * The class MessageHistoryDialog is used to display a history of all messages in a window
 * 
 * @author Manuel Laggner
 */
public class MessageHistoryDialog extends TmmDialog implements ListEventListener<Message> {
  private static final long           serialVersionUID = -5054005564554148578L;
  /**
   * @wbp.nls.resourceBundle messages
   */
  private static final ResourceBundle BUNDLE           = ResourceBundle.getBundle("messages", new UTF8Control()); //$NON-NLS-1$
  private static MessageHistoryDialog instance;
  private Map<Message, JPanel>        messageMap;
  private JPanel                      messagesPanel;
  private JScrollPane                 scrollPane;

  private MessageHistoryDialog() {
    super(MainWindow.getActiveInstance(), BUNDLE.getString("summarywindow.title"), "messageSummary"); //$NON-NLS-1$
    setModal(false);
    setModalityType(ModalityType.MODELESS);

    messageMap = new HashMap<>();

    getContentPane().setLayout(
        new FormLayout(new ColumnSpec[] { FormSpecs.RELATED_GAP_COLSPEC, ColumnSpec.decode("300dlu:grow"), FormSpecs.RELATED_GAP_COLSPEC, },
            new RowSpec[] { FormSpecs.RELATED_GAP_ROWSPEC, RowSpec.decode("150dlu:grow"), FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC,
                FormSpecs.RELATED_GAP_ROWSPEC, }));

    scrollPane = new JScrollPane();
    getContentPane().add(scrollPane, "2, 2, fill, fill");

    messagesPanel = new JPanel();
    messagesPanel.setBackground(Color.WHITE);
    messagesPanel.setLayout(new BoxLayout(messagesPanel, BoxLayout.PAGE_AXIS));
    scrollPane.setViewportView(messagesPanel);
    scrollPane.getVerticalScrollBar().setUnitIncrement(16);

    final JPanel panelButtons = new JPanel();
    EqualsLayout layout = new EqualsLayout(5);
    layout.setMinWidth(100);
    panelButtons.setLayout(layout);
    getContentPane().add(panelButtons, "2, 4, fill, fill");

    JButton btnClose = new JButton(BUNDLE.getString("Button.close")); //$NON-NLS-1$
    panelButtons.add(btnClose);
    btnClose.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent arg0) {
        setVisible(false);
      }
    });

    TmmUIMessageCollector.instance.getMessages().addListEventListener(this);
    updatePanel();
  }

  public static MessageHistoryDialog getInstance() {
    if (instance == null) {
      instance = new MessageHistoryDialog();
    }
    return instance;
  }

  @Override
  public void setVisible(boolean visible) {
    TmmUIMessageCollector.instance.resetNewMessageCount();
    if (visible) {
      TmmWindowSaver.getInstance().loadSettings(this);
      pack();
      setLocationRelativeTo(MainWindow.getActiveInstance());
      super.setVisible(true);
    }
    else {
      super.setVisible(false);
    }
  }

  @Override
  public void listChanged(ListEvent<Message> listChanges) {
    updatePanel();
  }

  private void updatePanel() {
    EventList<Message> list = TmmUIMessageCollector.instance.getMessages();
    list.getReadWriteLock().readLock().lock();
    try {
      for (int i = 0; i < list.size(); i++) {
        Message message = list.get(i);
        if (!messageMap.containsKey(message)) {
          MessagePanel panel = new MessagePanel(message);
          messageMap.put(message, panel);
          messagesPanel.add(panel);
        }
      }
      messagesPanel.revalidate();
      messagesPanel.repaint();
    }
    finally {
      list.getReadWriteLock().readLock().unlock();
    }
  }
}
