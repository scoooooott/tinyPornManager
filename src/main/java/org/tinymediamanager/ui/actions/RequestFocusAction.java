package org.tinymediamanager.ui.actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JComponent;

/**
 * Requests the focus in window for the given JComponent.
 * 
 * @author Henrik Niehaus
 *
 */
public class RequestFocusAction extends AbstractAction {

  private static final long serialVersionUID = 1L;
  private JComponent        target;

  public RequestFocusAction(JComponent target) {
    this.target = target;
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    target.requestFocusInWindow();
  }
}