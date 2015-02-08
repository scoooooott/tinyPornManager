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
package org.tinymediamanager.ui.dialogs;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.JList;
import javax.swing.JScrollPane;

import org.jdesktop.beansbinding.AutoBinding.UpdateStrategy;
import org.jdesktop.swingbinding.JListBinding;
import org.jdesktop.swingbinding.SwingBindings;
import org.tinymediamanager.ui.UTF8Control;

import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

/**
 * The MessageSummaryDialog - to display the all messages occured while a main task
 * 
 * @author Manuel Laggner
 */
public class MessageSummaryDialog extends TmmDialog {
  private static final long           serialVersionUID = -8163687483097098568L;
  /** @wbp.nls.resourceBundle messages */
  private static final ResourceBundle BUNDLE           = ResourceBundle.getBundle("messages", new UTF8Control()); //$NON-NLS-1$

  private List<String>                messageList      = new ArrayList<String>();
  private JList                       listMessages;

  public MessageSummaryDialog(List<String> messages) {
    super(BUNDLE.getString("summarywindow.title"), "messageSummary"); //$NON-NLS-1$
    setBounds(5, 5, 1000, 590);

    messageList.addAll(messages);
    getContentPane().setLayout(
        new FormLayout(new ColumnSpec[] { FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("684px:grow"), FormFactory.RELATED_GAP_COLSPEC, },
            new RowSpec[] { FormFactory.RELATED_GAP_ROWSPEC, RowSpec.decode("fill:265px:grow"), FormFactory.RELATED_GAP_ROWSPEC, }));

    JScrollPane scrollPane = new JScrollPane();
    getContentPane().add(scrollPane, "2, 2, fill, fill");

    listMessages = new JList();
    scrollPane.setViewportView(listMessages);
    initDataBindings();
  }

  protected void initDataBindings() {
    JListBinding<String, List<String>, JList> jListBinding = SwingBindings.createJListBinding(UpdateStrategy.READ, messageList, listMessages);
    jListBinding.bind();
  }

  @Override
  public void pack() {
    // do not let it pack - it looks weird
  }
}
