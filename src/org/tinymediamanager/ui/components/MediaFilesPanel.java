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
package org.tinymediamanager.ui.components;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Comparator;
import java.util.ResourceBundle;

import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.core.MediaFile;
import org.tinymediamanager.core.MediaFileType;
import org.tinymediamanager.core.Message;
import org.tinymediamanager.core.Message.MessageLevel;
import org.tinymediamanager.core.MessageManager;
import org.tinymediamanager.ui.TableColumnAdjuster;
import org.tinymediamanager.ui.TmmUIHelper;
import org.tinymediamanager.ui.UTF8Control;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.gui.AdvancedTableFormat;
import ca.odell.glazedlists.swing.DefaultEventTableModel;
import ca.odell.glazedlists.swing.GlazedListsSwing;

import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

/**
 * The Class MediaFilesPanel.
 * 
 * @author Manuel Laggner
 */
public class MediaFilesPanel extends JPanel {
  private static final long                 serialVersionUID    = -4929581173434859034L;
  private static final Logger               LOGGER              = LoggerFactory.getLogger(MediaFilesPanel.class);
  private static final ResourceBundle       BUNDLE              = ResourceBundle.getBundle("messages", new UTF8Control());              //$NON-NLS-1$
  private static final ImageIcon            PLAY_ICON           = new ImageIcon(
                                                                    MediaFilesPanel.class
                                                                        .getResource("/org/tinymediamanager/ui/images/Play_small.png"));

  private JScrollPane                       scrollPaneFiles;
  private JTable                            tableFiles;

  private TableColumnAdjuster               tableColumnAdjuster = null;
  private EventList<MediaFile>              mediaFileEventList;
  private DefaultEventTableModel<MediaFile> mediaFileTableModel = null;

  public MediaFilesPanel(EventList<MediaFile> mediaFiles) {
    this.mediaFileEventList = mediaFiles;
    setLayout(new FormLayout(new ColumnSpec[] { ColumnSpec.decode("default:grow"), }, new RowSpec[] { RowSpec.decode("default:grow"), }));

    mediaFileTableModel = new DefaultEventTableModel<MediaFile>(GlazedListsSwing.swingThreadProxyList(mediaFileEventList), new MediaTableFormat());
    tableFiles = new ZebraJTable(mediaFileTableModel);
    tableFiles.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

    tableFiles.addMouseListener(new MouseListener() {
      @Override
      public void mouseReleased(MouseEvent arg0) {
      }

      @Override
      public void mousePressed(MouseEvent arg0) {
      }

      @Override
      public void mouseExited(MouseEvent arg0) {
      }

      @Override
      public void mouseEntered(MouseEvent arg0) {
      }

      @Override
      public void mouseClicked(MouseEvent arg0) {
        int col = tableFiles.columnAtPoint(arg0.getPoint());
        if (col == 0) {
          int row = tableFiles.rowAtPoint(arg0.getPoint());
          row = tableFiles.convertRowIndexToModel(row);
          MediaFile mf = mediaFileEventList.get(row);
          if (mf.getType() == MediaFileType.VIDEO || mf.getType() == MediaFileType.TRAILER) {
            try {
              TmmUIHelper.openFile(mf.getFile());
            }
            catch (Exception e) {
              LOGGER.error("open file", e);
              MessageManager.instance.pushMessage(new Message(MessageLevel.ERROR, mf, "message.erroropenfile", new String[] { ":",
                  e.getLocalizedMessage() }));
            }
          }
        }
      }
    });

    scrollPaneFiles = ZebraJTable.createStripedJScrollPane(tableFiles);
    add(scrollPaneFiles, "1, 1, fill, fill");

    scrollPaneFiles.setViewportView(tableFiles);

    // adjust table
    tableColumnAdjuster = new TableColumnAdjuster(tableFiles);
    tableColumnAdjuster.setColumnDataIncluded(true);
    tableColumnAdjuster.setColumnHeaderIncluded(true);
    tableColumnAdjuster.setOnlyAdjustLarger(false);

  }

  /**
   * Adjust columns.
   */
  public void adjustColumns() {
    tableColumnAdjuster.adjustColumns();
  }

  /**
   * The Class MediaTableFormat.
   * 
   * @author Manuel Laggner
   */
  private static class MediaTableFormat implements AdvancedTableFormat<MediaFile> {

    /*
     * (non-Javadoc)
     * 
     * @see ca.odell.glazedlists.gui.TableFormat#getColumnCount()
     */
    @Override
    public int getColumnCount() {
      return 8;
    }

    /*
     * (non-Javadoc)
     * 
     * @see ca.odell.glazedlists.gui.TableFormat#getColumnName(int)
     */
    @Override
    public String getColumnName(int column) {
      switch (column) {
        case 0:
          return "";

        case 1:
          return BUNDLE.getString("metatag.filename"); //$NON-NLS-1$

        case 2:
          return BUNDLE.getString("metatag.size"); //$NON-NLS-1$

        case 3:
          return BUNDLE.getString("metatag.mediafiletype"); //$NON-NLS-1$

        case 4:
          return BUNDLE.getString("metatag.codec"); //$NON-NLS-1$

        case 5:
          return BUNDLE.getString("metatag.resolution"); //$NON-NLS-1$

        case 6:
          return BUNDLE.getString("metatag.runtime"); //$NON-NLS-1$

        case 7:
          return BUNDLE.getString("metatag.subtitle"); //$NON-NLS-1$
      }

      throw new IllegalStateException();
    }

    /*
     * (non-Javadoc)
     * 
     * @see ca.odell.glazedlists.gui.TableFormat#getColumnValue(java.lang.Object, int)
     */
    @Override
    public Object getColumnValue(MediaFile mediaFile, int column) {
      switch (column) {
        case 0:
          if (mediaFile.getType() == MediaFileType.VIDEO || mediaFile.getType() == MediaFileType.TRAILER) {
            return MediaFilesPanel.PLAY_ICON;
          }
          return null;

        case 1:
          return mediaFile.getFilename();

        case 2:
          return mediaFile.getFilesizeInMegabytes();

        case 3:
          return getMediaFileTypeLocalized(mediaFile.getType());

        case 4:
          return mediaFile.getCombinedCodecs();

        case 5:
          return mediaFile.getVideoResolution();

        case 6:
          return mediaFile.getDurationHM();

        case 7:
          return mediaFile.getSubtitlesAsString();
      }

      throw new IllegalStateException();
    }

    /*
     * (non-Javadoc)
     * 
     * @see ca.odell.glazedlists.gui.AdvancedTableFormat#getColumnClass(int)
     */
    @SuppressWarnings("rawtypes")
    @Override
    public Class getColumnClass(int column) {
      switch (column) {
        case 0:
          return ImageIcon.class;

        case 1:
        case 2:
        case 3:
        case 4:
        case 5:
        case 6:
        case 7:
          return String.class;
      }

      throw new IllegalStateException();
    }

    /*
     * (non-Javadoc)
     * 
     * @see ca.odell.glazedlists.gui.AdvancedTableFormat#getColumnComparator(int)
     */
    @SuppressWarnings("rawtypes")
    @Override
    public Comparator getColumnComparator(int arg0) {
      return null;
    }

    private String getMediaFileTypeLocalized(MediaFileType type) {
      String prop = "mediafiletype." + type.name().toLowerCase();
      return BUNDLE.getString(prop);
    }
  }
}
