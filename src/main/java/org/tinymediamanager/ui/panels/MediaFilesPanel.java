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
package org.tinymediamanager.ui.panels;

import java.awt.Cursor;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.core.MediaFileType;
import org.tinymediamanager.core.Message;
import org.tinymediamanager.core.Message.MessageLevel;
import org.tinymediamanager.core.MessageManager;
import org.tinymediamanager.core.TmmDateFormat;
import org.tinymediamanager.core.UTF8Control;
import org.tinymediamanager.core.entities.MediaEntity;
import org.tinymediamanager.core.entities.MediaFile;
import org.tinymediamanager.ui.IconManager;
import org.tinymediamanager.ui.MainWindow;
import org.tinymediamanager.ui.TableColumnResizer;
import org.tinymediamanager.ui.TmmUIHelper;
import org.tinymediamanager.ui.components.table.TmmTable;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.gui.AdvancedTableFormat;
import ca.odell.glazedlists.swing.DefaultEventTableModel;
import ca.odell.glazedlists.swing.GlazedListsSwing;
import net.miginfocom.swing.MigLayout;

/**
 * The Class MediaFilesPanel.
 * 
 * @author Manuel Laggner
 */
public abstract class MediaFilesPanel extends JPanel {
  private static final long           serialVersionUID = -4929581173434859034L;
  private static final Logger         LOGGER           = LoggerFactory.getLogger(MediaFilesPanel.class);
  private static final ResourceBundle BUNDLE           = ResourceBundle.getBundle("messages", new UTF8Control());

  private TmmTable                    tableFiles;

  private EventList<MediaFile>        mediaFileEventList;

  public MediaFilesPanel(EventList<MediaFile> mediaFiles) {
    this.mediaFileEventList = mediaFiles;

    initComponents();
  }

  private void initComponents() {
    setLayout(new MigLayout("insets 0", "[450lp,grow]", "[300lp,grow]"));
    {
      tableFiles = new TmmTable(new DefaultEventTableModel<>(GlazedListsSwing.swingThreadProxyList(mediaFileEventList), new MediaTableFormat()));
      tableFiles.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

      LinkListener linkListener = new LinkListener();
      tableFiles.addMouseListener(linkListener);
      tableFiles.addMouseMotionListener(linkListener);

      JScrollPane scrollPaneFiles = new JScrollPane(tableFiles);
      tableFiles.configureScrollPane(scrollPaneFiles);
      add(scrollPaneFiles, "cell 0 0,grow");

      scrollPaneFiles.setViewportView(tableFiles);
    }
  }

  public void adjustColumns() {
    TableColumnResizer.adjustColumnPreferredWidths(tableFiles, 6);
  }

  /**
   * get the actual media entity holding this list of media files
   *
   * @return the media entity
   */
  abstract public MediaEntity getMediaEntity();

  private static class MediaTableFormat implements AdvancedTableFormat<MediaFile> {
    @Override
    public int getColumnCount() {
      return 10;
    }

    @Override
    public String getColumnName(int column) {
      switch (column) {
        case 0:
          return "";

        case 1:
          return BUNDLE.getString("metatag.filename");

        case 2:
          return BUNDLE.getString("metatag.size");

        case 3:
          return BUNDLE.getString("metatag.mediafiletype");

        case 4:
          return BUNDLE.getString("metatag.codec");

        case 5:
          return BUNDLE.getString("metatag.resolution");

        case 6:
          return BUNDLE.getString("metatag.runtime");

        case 7:
          return BUNDLE.getString("metatag.subtitle");

        case 8:
          return BUNDLE.getString("metatag.filecreationdate");

        case 9:
          return BUNDLE.getString("metatag.filelastmodifieddate");
      }

      throw new IllegalStateException();
    }

    @Override
    public Object getColumnValue(MediaFile mediaFile, int column) {
      switch (column) {
        case 0:
          if (mediaFile.isVideo()) {
            return IconManager.PLAY;
          }
          if (mediaFile.isGraphic()) {
            return IconManager.SEARCH;
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
          return mediaFile.getDurationHMS();

        case 7:
          return mediaFile.getSubtitlesAsString();

        case 8:
          return formatDate(mediaFile.getDateCreated());

        case 9:
          return formatDate(mediaFile.getDateLastModified());

      }

      throw new IllegalStateException();
    }

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
        case 8:
        case 9:
          return String.class;
      }

      throw new IllegalStateException();
    }

    @SuppressWarnings("rawtypes")
    @Override
    public Comparator getColumnComparator(int arg0) {
      return null;
    }

    private String getMediaFileTypeLocalized(MediaFileType type) {
      String prop = "mediafiletype." + type.name().toLowerCase(Locale.ROOT);
      return BUNDLE.getString(prop);
    }

    private String formatDate(Date date) {
      if (date == null) {
        return "";
      }

      return TmmDateFormat.MEDIUM_DATE_SHORT_TIME_FORMAT.format(date);
    }
  }

  private class LinkListener implements MouseListener, MouseMotionListener {
    @Override
    public void mouseClicked(MouseEvent arg0) {
      int col = tableFiles.columnAtPoint(arg0.getPoint());
      if (col == 0) {
        int row = tableFiles.rowAtPoint(arg0.getPoint());
        row = tableFiles.convertRowIndexToModel(row);
        MediaFile mf = mediaFileEventList.get(row);
        // open the video file in the desired player
        if (mf.isVideo()) {
          try {
            TmmUIHelper.openFile(mf.getFileAsPath());
          }
          catch (Exception e) {
            LOGGER.error("open file", e);
            MessageManager.instance
                .pushMessage(new Message(MessageLevel.ERROR, mf, "message.erroropenfile", new String[] { ":", e.getLocalizedMessage() }));
          }
        }
        // open the graphic in the lightbox
        if (mf.isGraphic()) {
          MainWindow.getActiveInstance().createLightbox(mf.getFileAsPath().toString(), "");
        }
      }
    }

    @Override
    public void mouseEntered(MouseEvent e) {
      JTable table = (JTable) e.getSource();
      int col = table.columnAtPoint(new Point(e.getX(), e.getY()));
      if (col == 0) {
        table.setCursor(new Cursor(Cursor.HAND_CURSOR));
      }
    }

    @Override
    public void mouseExited(MouseEvent e) {
      JTable table = (JTable) e.getSource();
      int col = table.columnAtPoint(new Point(e.getX(), e.getY()));
      if (col != 0) {
        table.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
      }
    }

    @Override
    public void mouseMoved(MouseEvent e) {
      JTable table = (JTable) e.getSource();
      int col = table.columnAtPoint(new Point(e.getX(), e.getY()));
      if (col != 0 && table.getCursor().getType() == Cursor.HAND_CURSOR) {
        table.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
      }
      if (col == 0 && table.getCursor().getType() == Cursor.DEFAULT_CURSOR) {
        table.setCursor(new Cursor(Cursor.HAND_CURSOR));
      }
    }

    @Override
    public void mousePressed(MouseEvent e) {
    }

    @Override
    public void mouseReleased(MouseEvent e) {
    }

    @Override
    public void mouseDragged(MouseEvent arg0) {
    }
  }
}
