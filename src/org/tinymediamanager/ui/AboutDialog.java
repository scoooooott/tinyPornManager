package org.tinymediamanager.ui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

public class AboutDialog extends JDialog {

  private final JPanel contentPanel = new JPanel();
  private final Action action       = new SwingAction();

  /**
   * Create the dialog.
   */
  public AboutDialog() {
    setType(Type.UTILITY);
    setResizable(false);
    setModal(true);
    setBounds(100, 100, 450, 300);
    getContentPane().setLayout(new BorderLayout());
    contentPanel.setLayout(new FlowLayout());
    contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
    getContentPane().add(contentPanel, BorderLayout.CENTER);
    {
      JPanel buttonPane = new JPanel();
      getContentPane().add(buttonPane, BorderLayout.SOUTH);
      buttonPane.setLayout(new FormLayout(new ColumnSpec[] { ColumnSpec.decode("default:grow"), FormFactory.BUTTON_COLSPEC, }, new RowSpec[] { FormFactory.LINE_GAP_ROWSPEC,
          RowSpec.decode("23px"), }));
      {
        JButton okButton = new JButton();
        okButton.setAction(action);
        buttonPane.add(okButton, "2, 2, fill, top");
        getRootPane().setDefaultButton(okButton);
      }
    }
  }

  private class SwingAction extends AbstractAction {
    public SwingAction() {
      putValue(NAME, "Ok");
    }

    public void actionPerformed(ActionEvent e) {
      setVisible(false);
    }
  }
}
