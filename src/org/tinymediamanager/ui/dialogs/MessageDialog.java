/*
 * Copyright 2012 - 2013 Manuel Laggner
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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ResourceBundle;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;

import org.tinymediamanager.ui.EqualsLayout;
import org.tinymediamanager.ui.IconManager;
import org.tinymediamanager.ui.MainWindow;
import org.tinymediamanager.ui.UTF8Control;

import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

/**
 * The class MessageDialog. To display messages nicely
 * 
 * @author Manuel Laggner
 */
public class MessageDialog extends JDialog {
  private static final long           serialVersionUID = -9035402766767310658L;
  private static final ResourceBundle BUNDLE           = ResourceBundle.getBundle("messages", new UTF8Control()); //$NON-NLS-1$

  private JLabel                      lblImage;
  private JLabel                      lblText;
  private JLabel                      lblDescription;
  private JScrollPane                 scrollPane;
  private JTextPane                   textPane;

  public MessageDialog(Window owner, String title) {
    super(owner, title);
    setIconImages(owner.getIconImages());
    setMinimumSize(new Dimension(300, 150));
    setResizable(false);
    setModal(true);
    getContentPane().setLayout(new BorderLayout(0, 0));
    {
      JPanel panelContent = new JPanel();
      getContentPane().add(panelContent, BorderLayout.CENTER);
      panelContent.setLayout(new FormLayout(new ColumnSpec[] { FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("default"),
          FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("default:grow"), FormFactory.RELATED_GAP_COLSPEC, }, new RowSpec[] {
          FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
          FormFactory.RELATED_GAP_ROWSPEC, RowSpec.decode("default:grow"), FormFactory.RELATED_GAP_ROWSPEC, }));
      {
        lblImage = new JLabel("");
        lblImage.setVisible(false);
        panelContent.add(lblImage, "2, 2, 1, 3");
      }
      {
        lblText = new JLabel("");
        lblText.setVisible(false);
        panelContent.add(lblText, "4, 2, fill, fill");
      }
      {
        lblDescription = new JLabel("");
        lblDescription.setVisible(false);
        panelContent.add(lblDescription, "4, 4, fill, fill");
      }
      {
        scrollPane = new JScrollPane();
        scrollPane.setVisible(false);
        scrollPane.setPreferredSize(new Dimension(600, 200));
        panelContent.add(scrollPane, "2, 6, 3, 1, fill, fill");
        {
          textPane = new JTextPane();
          textPane.setVisible(false);
          textPane.setEditable(false);
          scrollPane.setViewportView(textPane);
        }
      }
    }
    {
      JPanel panelBottom = new JPanel();
      getContentPane().add(panelBottom, BorderLayout.SOUTH);
      panelBottom.setLayout(new FormLayout(new ColumnSpec[] { FormFactory.RELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC,
          FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("default:grow"), FormFactory.RELATED_GAP_COLSPEC, }, new RowSpec[] {
          FormFactory.RELATED_GAP_ROWSPEC, RowSpec.decode("default:grow"), FormFactory.RELATED_GAP_ROWSPEC, }));
      {
        JPanel panelButtons = new JPanel();
        EqualsLayout layout = new EqualsLayout(5);
        layout.setMinWidth(100);
        panelButtons.setLayout(layout);
        panelBottom.add(panelButtons, "4, 2, right, fill");
        {
          JButton btnClose = new JButton(BUNDLE.getString("Button.close")); //$NON-NLS-1$
          btnClose.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
              setVisible(false);
              dispose();
            }
          });
          panelButtons.add(btnClose);
        }
      }
    }
  }

  public void setImage(Icon icon) {
    lblImage.setIcon(icon);
    lblImage.setVisible(true);
  }

  public void setText(String text) {
    lblText.setText(toHTML(text));
    lblText.setVisible(true);
  }

  public void setDescription(String description) {
    lblDescription.setText(toHTML(description));
    lblDescription.setVisible(true);
  }

  public void setDetails(String details) {
    textPane.setText(details);
    textPane.setVisible(true);
    textPane.setCaretPosition(0);
    scrollPane.setVisible(true);
  }

  public static void showExceptionWindow(Throwable ex) {
    MessageDialog dialog = new MessageDialog(MainWindow.getActiveInstance(), BUNDLE.getString("tmm.problemdetected")); //$NON-NLS-1$

    dialog.setImage(IconManager.ERROR);
    String msg = ex.getLocalizedMessage();
    dialog.setText(msg != null ? msg : "");
    dialog.setDescription(BUNDLE.getString("tmm.uicrash")); //$NON-NLS-1$
    dialog.setDetails(stackStraceAsString(ex));

    dialog.setResizable(true);
    dialog.pack();
    dialog.setLocationRelativeTo(MainWindow.getActiveInstance());
    dialog.setVisible(true);
  }

  private static String stackStraceAsString(Throwable ex) {
    StringWriter sw = new StringWriter();
    ex.printStackTrace(new PrintWriter(sw));
    return sw.toString();
  }

  public static final String toHTML(String s) {
    s = s == null ? "" : s.replaceAll("\n", "<br>");
    String tmp = s.trim().toLowerCase();

    StringBuilder sb = new StringBuilder(s);
    if (!tmp.startsWith("<html>"))
      sb.insert(0, "<html>");
    if (!tmp.endsWith("</html>")) {
      sb.append("</html>");
    }
    return sb.toString();
  }
}
