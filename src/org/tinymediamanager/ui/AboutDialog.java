package org.tinymediamanager.ui;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import org.tinymediamanager.ReleaseInfo;

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
    setTitle("About");
    setResizable(false);
    setModal(true);
    setBounds(100, 100, 450, 223);
    getContentPane().setLayout(new BorderLayout());
    contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
    getContentPane().add(contentPanel, BorderLayout.CENTER);
    contentPanel.setLayout(new FormLayout(new ColumnSpec[] { FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("center:89px"), FormFactory.RELATED_GAP_COLSPEC,
        ColumnSpec.decode("default:grow"), }, new RowSpec[] { FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC,
        FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC, RowSpec.decode("max(50px;min)"),
        FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, }));
    {
      JLabel lblLogo = new JLabel("");
      lblLogo.setIcon(new ImageIcon(AboutDialog.class.getResource("/org/tinymediamanager/ui/images/tmm96.png")));
      contentPanel.add(lblLogo, "2, 2, 1, 7, default, top");
    }
    {
      JLabel lblTinymediamanager = new JLabel("tinyMediaManager");
      lblTinymediamanager.setFont(new Font("Dialog", Font.BOLD, 18));
      contentPanel.add(lblTinymediamanager, "4, 2, center, default");
    }
    {
      JLabel lblByManuel = new JLabel("©2012 by Manuel Laggner");
      contentPanel.add(lblByManuel, "4, 4, center, default");
    }
    {
      JLabel lblVersion = new JLabel(ReleaseInfo.getVersion());
      contentPanel.add(lblVersion, "4, 8, default, top");

    }
    {
      JLabel lblHomepage = new JLabel("Homepage");
      contentPanel.add(lblHomepage, "2, 10");
    }
    {
      LinkLabel lblNewLabel = new LinkLabel("http://code.google.com/p/tinymediamanager/");
      contentPanel.add(lblNewLabel, "4, 10");
    }
    {
      JPanel buttonPane = new JPanel();
      getContentPane().add(buttonPane, BorderLayout.SOUTH);
      buttonPane.setLayout(new FormLayout(new ColumnSpec[] { ColumnSpec.decode("default:grow"), FormFactory.BUTTON_COLSPEC, FormFactory.RELATED_GAP_COLSPEC, }, new RowSpec[] {
          FormFactory.LINE_GAP_ROWSPEC, RowSpec.decode("23px"), FormFactory.RELATED_GAP_ROWSPEC, }));
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
