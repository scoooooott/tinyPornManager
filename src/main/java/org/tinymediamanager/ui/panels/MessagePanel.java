package org.tinymediamanager.ui.panels;

import java.awt.Dimension;
import java.awt.Font;
import java.text.DateFormat;
import java.util.ResourceBundle;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;

import org.tinymediamanager.core.Message;
import org.tinymediamanager.core.Utils;
import org.tinymediamanager.core.entities.MediaEntity;
import org.tinymediamanager.core.entities.MediaFile;
import org.tinymediamanager.ui.IconManager;
import org.tinymediamanager.ui.TmmFontHelper;
import org.tinymediamanager.ui.UTF8Control;
import org.tinymediamanager.ui.components.ReadOnlyTextArea;

import net.miginfocom.swing.MigLayout;

public class MessagePanel extends JPanel {
  private static final long           serialVersionUID = -7224510527137312686L;
  /** @wbp.nls.resourceBundle messages */
  private static final ResourceBundle BUNDLE           = ResourceBundle.getBundle("messages", new UTF8Control()); //$NON-NLS-1$

  private JLabel                      lblTitle;
  private JTextArea                   taMessage;
  private JLabel                      lblIcon;
  private JLabel                      lblDate;

  public MessagePanel(Message message) {
    setOpaque(false);
    initComponents();
    // init data
    DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.DEFAULT);
    lblDate.setText(dateFormat.format(message.getMessageDate()));

    String text = "";
    if (message.getMessageSender() instanceof MediaEntity) {
      // mediaEntity title: eg. Movie title
      MediaEntity me = (MediaEntity) message.getMessageSender();
      text = me.getTitle();
    }
    else if (message.getMessageSender() instanceof MediaFile) {
      // mediaFile: filename
      MediaFile mf = (MediaFile) message.getMessageSender();
      text = mf.getFilename();
    }
    else {
      try {
        text = Utils.replacePlaceholders(BUNDLE.getString(message.getMessageSender().toString()), message.getSenderParams());
      }
      catch (Exception e) {
        text = String.valueOf(message.getMessageSender());
      }
    }
    lblTitle.setText(text);

    text = "";
    try {
      // try to get a localized version
      text = Utils.replacePlaceholders(BUNDLE.getString(message.getMessageId()), message.getIdParams());
    }
    catch (Exception e) {
      // simply take the id
      text = message.getMessageId();
    }
    taMessage.setText(text);

    switch (message.getMessageLevel()) {
      case ERROR:
      case WARN:
        lblIcon.setIcon(IconManager.ERROR);
        break;

      default:
        lblIcon.setIcon(null);
        break;
    }
  }

  private void initComponents() {
    setLayout(new MigLayout("", "[450lp,grow]", "[][]"));

    lblDate = new JLabel("");
    add(lblDate, "cell 0 0,aligny top");
    JPanel innerPanel = new RoundedPanel() {
      private static final long serialVersionUID = -6407635030887890673L;

      {
        arcs = new Dimension(10, 10);
        drawShadow = false;
      }
    };
    add(innerPanel, "cell 0 1,growx");
    innerPanel.setLayout(new MigLayout("", "[1px][350lp,grow]", "[1px][1px][]"));

    lblIcon = new JLabel("");
    lblIcon.setHorizontalAlignment(SwingConstants.CENTER);
    innerPanel.add(lblIcon, "cell 0 1,alignx center,aligny center");

    lblTitle = new JLabel();
    TmmFontHelper.changeFont(lblTitle, Font.BOLD);

    innerPanel.add(lblTitle, "cell 1 0,growx");

    taMessage = new ReadOnlyTextArea();
    innerPanel.add(taMessage, "cell 1 2,grow");
  }
}
