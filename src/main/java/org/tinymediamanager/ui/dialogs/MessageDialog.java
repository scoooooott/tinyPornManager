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
import java.awt.Dimension;
import java.awt.Window;
import java.io.PrintWriter;
import java.io.StringWriter;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;

import org.tinymediamanager.ui.IconManager;
import org.tinymediamanager.ui.components.ReadOnlyTextPane;

import net.miginfocom.swing.MigLayout;

/**
 * The class MessageDialog. To display messages nicely
 * 
 * @author Manuel Laggner
 */
public class MessageDialog extends TmmDialog {
  private static final long serialVersionUID = -9035402766767310658L;

  private JLabel            lblImage;
  private JTextPane         tpText;
  private JTextPane         tpDescription;
  private JScrollPane       scrollPane;
  private JTextPane         textPane;

  public MessageDialog(Window owner, String title) {
    super(owner, title, "messageDialog");
    setModal(false);
    setModalExclusionType(ModalExclusionType.APPLICATION_EXCLUDE);

    {
      JPanel panelContent = new JPanel();
      panelContent.setLayout(new MigLayout("hidemode 1", "[][500lp,grow]", "[][][300lp,grow]"));
      getContentPane().add(panelContent, BorderLayout.CENTER);
      {
        lblImage = new JLabel("");
        lblImage.setVisible(false);
        panelContent.add(lblImage, "cell 0 0 1 2,grow");
      }
      {
        tpText = new ReadOnlyTextPane("");
        tpText.setVisible(false);
        panelContent.add(tpText, "cell 1 0,growx");
      }
      {
        tpDescription = new ReadOnlyTextPane("");
        tpDescription.setEditable(true);
        tpDescription.setVisible(false);
        panelContent.add(tpDescription, "cell 1 1,growx");
      }
      {
        scrollPane = new JScrollPane();
        scrollPane.setVisible(false);
        scrollPane.setPreferredSize(new Dimension(600, 200));
        panelContent.add(scrollPane, "cell 0 2 2 1,grow");
        {
          textPane = new JTextPane();
          textPane.setVisible(false);
          textPane.setEditable(false);
          scrollPane.setViewportView(textPane);
        }
      }
    }
    {
      JButton btnClose = new JButton(BUNDLE.getString("Button.close"));
      btnClose.addActionListener(arg0 -> setVisible(false));
      addDefaultButton(btnClose);
    }
  }

  public void setImage(Icon icon) {
    lblImage.setIcon(icon);
    lblImage.setVisible(true);
  }

  public void setText(String text) {
    tpText.setText(text);
    tpText.setVisible(true);
  }

  public void setDescription(String description) {
    tpDescription.setText(description);
    tpDescription.setVisible(true);
  }

  public void setDetails(String details) {
    textPane.setText(details);
    textPane.setVisible(true);
    textPane.setCaretPosition(0);
    scrollPane.setVisible(true);
  }

  public static void showExceptionWindow(Throwable ex) {
    MessageDialog dialog = new MessageDialog(null, BUNDLE.getString("tmm.problemdetected"));

    dialog.setImage(IconManager.ERROR);
    String msg = ex.getLocalizedMessage();
    dialog.setText(msg != null ? msg : "");
    dialog.setDescription(BUNDLE.getString("tmm.uicrash"));
    dialog.setDetails(stackStraceAsString(ex));
    dialog.setAlwaysOnTop(true);
    dialog.setVisible(true);
  }

  private static String stackStraceAsString(Throwable ex) {
    StringWriter sw = new StringWriter();
    ex.printStackTrace(new PrintWriter(sw));
    return sw.toString();
  }
}
