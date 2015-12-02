package org.tinymediamanager.ui.panels;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.text.DateFormat;
import java.util.ResourceBundle;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTextPane;
import javax.swing.ListCellRenderer;
import javax.swing.SwingConstants;

import org.tinymediamanager.core.Message;
import org.tinymediamanager.core.Utils;
import org.tinymediamanager.core.entities.MediaEntity;
import org.tinymediamanager.core.entities.MediaFile;
import org.tinymediamanager.ui.IconManager;
import org.tinymediamanager.ui.TmmFontHelper;
import org.tinymediamanager.ui.UTF8Control;

import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.FormSpecs;
import com.jgoodies.forms.layout.RowSpec;

public class MessagePanel extends JPanel implements ListCellRenderer<Message> {
  private static final long           serialVersionUID = -7224510527137312686L;
  /**
   * @wbp.nls.resourceBundle messages
   */
  private static final ResourceBundle BUNDLE           = ResourceBundle.getBundle("messages", new UTF8Control()); //$NON-NLS-1$
  private JLabel                      lblTitle;
  private JTextPane                   tpMessage;
  private JLabel                      lblIcon;
  private JLabel                      lblDate;
  private DateFormat                  dateFormat;

  public MessagePanel() {
    initComponents();
    dateFormat = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.DEFAULT);
  }

  private void initComponents() {
    setOpaque(false);
    setLayout(new FormLayout(
        new ColumnSpec[] { FormSpecs.LABEL_COMPONENT_GAP_COLSPEC, ColumnSpec.decode("default:grow"), FormSpecs.LABEL_COMPONENT_GAP_COLSPEC, },
        new RowSpec[] { FormSpecs.DEFAULT_ROWSPEC, RowSpec.decode("top:default"), FormSpecs.LINE_GAP_ROWSPEC, }));

    lblDate = new JLabel("");
    add(lblDate, "2, 1");
    final JPanel innerPanel = new RoundedPanel() {
      private static final long serialVersionUID = -6407635030887890673L;

      {
        arcs = new Dimension(10, 10);
        shady = false;
      }
    };
    add(innerPanel, "2, 2, fill, default");
    // innerPanel.setBorder(BorderFactory.createLineBorder(getForeground()));
    innerPanel.setLayout(new FormLayout(
        new ColumnSpec[] { FormSpecs.RELATED_GAP_COLSPEC, ColumnSpec.decode("25dlu"), FormSpecs.RELATED_GAP_COLSPEC,
            ColumnSpec.decode("default:grow"), FormSpecs.RELATED_GAP_COLSPEC, },
        new RowSpec[] { FormSpecs.LABEL_COMPONENT_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC, FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC,
            FormSpecs.LABEL_COMPONENT_GAP_ROWSPEC, }));

    lblIcon = new JLabel("");
    lblIcon.setHorizontalAlignment(SwingConstants.CENTER);
    innerPanel.add(lblIcon, "2, 2, 1, 3, center, center");

    lblTitle = new JLabel();
    TmmFontHelper.changeFont(lblTitle, Font.BOLD);

    innerPanel.add(lblTitle, "4, 2, fill, default");

    tpMessage = new JTextPane();
    tpMessage.setOpaque(false);
    innerPanel.add(tpMessage, "4, 4, fill, top");
  }

  @Override
  public Component getListCellRendererComponent(JList<? extends Message> list, Message value, int index, boolean isSelected, boolean cellHasFocus) {
    lblDate.setText(dateFormat.format(value.getMessageDate()));

    String text = "";
    if (value.getMessageSender() instanceof MediaEntity) {
      // mediaEntity title: eg. Movie title
      MediaEntity me = (MediaEntity) value.getMessageSender();
      text = me.getTitle();
    }
    else if (value.getMessageSender() instanceof MediaFile) {
      // mediaFile: filename
      MediaFile mf = (MediaFile) value.getMessageSender();
      text = mf.getFilename();
    }
    else {
      try {
        text = Utils.replacePlaceholders(BUNDLE.getString(value.getMessageSender().toString()), value.getSenderParams());
      }
      catch (Exception e) {
        text = String.valueOf(value.getMessageSender());
      }
    }
    lblTitle.setText(text);

    text = "";
    try {
      // try to get a localized version
      text = Utils.replacePlaceholders(BUNDLE.getString(value.getMessageId()), value.getIdParams());
    }
    catch (Exception e) {
      // simply take the id
      text = value.getMessageId();
    }
    tpMessage.setText(text);

    switch (value.getMessageLevel()) {
      case ERROR:
      case WARN:
        lblIcon.setIcon(IconManager.ERROR);
        break;

      default:
        lblIcon.setIcon(null);
        break;
    }
    return this;
  }
}
