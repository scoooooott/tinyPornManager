package org.tinymediamanager.ui.dialogs;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ResourceBundle;

import javax.swing.JDialog;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.Timer;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.Globals;
import org.tinymediamanager.ui.TmmUILogAppender.LogOutput;
import org.tinymediamanager.ui.TmmUILogCollector;
import org.tinymediamanager.ui.UTF8Control;

import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

public class LogDialog extends JDialog implements ActionListener {
  private static final long           serialVersionUID = -5054005564554148578L;
  private static final ResourceBundle BUNDLE           = ResourceBundle.getBundle("messages", new UTF8Control()); //$NON-NLS-1$
  private static final Logger         LOGGER           = LoggerFactory.getLogger(LogDialog.class);
  private static final int            REFRESH_PERIOD   = 1000;

  private JTextArea                   taLogs;

  private int                         logByteCount     = 0;
  private final Timer                 timerRefresh;

  public LogDialog() {
    setIconImage(Globals.logo);
    setTitle(BUNDLE.getString("logwindow.title")); //$NON-NLS-1$
    setBounds(25, 25, 600, 250);

    timerRefresh = new Timer(REFRESH_PERIOD, this);
    timerRefresh.setInitialDelay(0);

    getContentPane().setLayout(
        new FormLayout(new ColumnSpec[] { FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("default:grow"), }, new RowSpec[] {
            FormFactory.RELATED_GAP_ROWSPEC, RowSpec.decode("default:grow"), }));

    JScrollPane scrollPane = new JScrollPane();
    getContentPane().add(scrollPane, "2, 2, fill, fill");

    taLogs = new JTextArea();
    scrollPane.setViewportView(taLogs);
    taLogs.setEditable(false);
    taLogs.setWrapStyleWord(true);
    taLogs.setLineWrap(true);

    taLogs.setText(TmmUILogCollector.instance.getLogOutput().getContent());
    timerRefresh.start();
  }

  @Override
  public void actionPerformed(ActionEvent ae) {
    if (ae.getSource() == timerRefresh) {
      updateApplicationLog();
    }
  }

  private void updateApplicationLog() {
    final boolean append = (logByteCount > 0) ? true : false;
    final LogOutput logOutput = TmmUILogCollector.instance.getLogOutput(this.logByteCount);
    logByteCount = logOutput.getByteCount();
    final String content = logOutput.getContent();

    if (content.length() > 0) {
      if (append) {
        final Document doc = taLogs.getDocument();
        try {
          doc.insertString(doc.getLength(), content, null);
        }
        catch (BadLocationException ble) {
          LOGGER.error("bad location: ", ble);
        }
      }
      else {
        taLogs.setText(content);
      }
      // scroll to the end of the textarea
      taLogs.setCaretPosition(taLogs.getText().length());
    }
  }
}
