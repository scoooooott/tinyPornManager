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

import java.util.Comparator;
import java.util.ResourceBundle;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import org.tinymediamanager.core.MediaFile;
import org.tinymediamanager.core.MediaFileType;
import org.tinymediamanager.ui.TableColumnAdjuster;
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

  /** The Constant serialVersionUID. */
  private static final long                 serialVersionUID    = -4929581173434859034L;

  /** The Constant BUNDLE. */
  private static final ResourceBundle       BUNDLE              = ResourceBundle.getBundle("messages", new UTF8Control()); //$NON-NLS-1$

  /** The scroll pane files. */
  private JScrollPane                       scrollPaneFiles;

  /** The table files. */
  private JTable                            tableFiles;

  /** The table column adjuster. */
  private TableColumnAdjuster               tableColumnAdjuster = null;

  /** The media file event list. */
  private EventList<MediaFile>              mediaFileEventList;

  /** The media file table model. */
  private DefaultEventTableModel<MediaFile> mediaFileTableModel = null;

  public MediaFilesPanel(EventList<MediaFile> mediaFiles) {
    this.mediaFileEventList = mediaFiles;
    setLayout(new FormLayout(new ColumnSpec[] { ColumnSpec.decode("default:grow"), }, new RowSpec[] { RowSpec.decode("default:grow"), }));

    mediaFileTableModel = new DefaultEventTableModel<MediaFile>(GlazedListsSwing.swingThreadProxyList(mediaFileEventList), new MediaTableFormat());
    // tableFiles = new JTable(mediaFileTableModel);
    tableFiles = new ZebraJTable(mediaFileTableModel);
    tableFiles.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

    // scrollPaneFiles = new JScrollPane();
    scrollPaneFiles = ZebraJTable.createStripedJScrollPane(tableFiles);
    add(scrollPaneFiles, "1, 1, fill, fill");

    scrollPaneFiles.setViewportView(tableFiles);

    // adjust table
    tableColumnAdjuster = new TableColumnAdjuster(tableFiles);
    tableColumnAdjuster.setColumnDataIncluded(true);
    tableColumnAdjuster.setColumnHeaderIncluded(true);
    tableColumnAdjuster.setOnlyAdjustLarger(false);
    // tableColumnAdjuster.setDynamicAdjustment(true);
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
      return 6;
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
          return BUNDLE.getString("metatag.filename"); //$NON-NLS-1$

        case 1:
          return BUNDLE.getString("metatag.size"); //$NON-NLS-1$

        case 2:
          return BUNDLE.getString("metatag.mediafiletype"); //$NON-NLS-1$

        case 3:
          return BUNDLE.getString("metatag.codec"); //$NON-NLS-1$

        case 4:
          return BUNDLE.getString("metatag.resolution"); //$NON-NLS-1$

        case 5:
          return BUNDLE.getString("metatag.runtime"); //$NON-NLS-1$

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
          return mediaFile.getFilename();

        case 1:
          return mediaFile.getFilesizeInMegabytes();

        case 2:
          return getMediaFileTypeLocalized(mediaFile.getType());

        case 3:
          return mediaFile.getVideoCodec();

        case 4:
          return mediaFile.getVideoResolution();

        case 5:
          return mediaFile.getDurationHM();
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
        case 1:
        case 2:
        case 3:
        case 4:
        case 5:
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
