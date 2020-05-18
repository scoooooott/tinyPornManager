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
package org.tinymediamanager.ui.movies.panels;

import java.awt.Cursor;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.beans.PropertyChangeListener;
import java.util.Comparator;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.swing.DefaultListSelectionModel;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.event.ListSelectionListener;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.Globals;
import org.tinymediamanager.core.Message;
import org.tinymediamanager.core.Message.MessageLevel;
import org.tinymediamanager.core.MessageManager;
import org.tinymediamanager.core.UTF8Control;
import org.tinymediamanager.core.entities.MediaTrailer;
import org.tinymediamanager.core.movie.MovieHelpers;
import org.tinymediamanager.core.tvshow.TvShowHelpers;
import org.tinymediamanager.scraper.util.UrlUtil;
import org.tinymediamanager.ui.IconManager;
import org.tinymediamanager.ui.TableColumnResizer;
import org.tinymediamanager.ui.TmmUIHelper;
import org.tinymediamanager.ui.components.table.TmmTable;
import org.tinymediamanager.ui.movies.MovieSelectionModel;
import org.tinymediamanager.ui.tvshows.TvShowSelectionModel;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.ObservableElementList;
import ca.odell.glazedlists.gui.AdvancedTableFormat;
import ca.odell.glazedlists.swing.DefaultEventTableModel;
import ca.odell.glazedlists.swing.GlazedListsSwing;
import net.miginfocom.swing.MigLayout;

/**
 * The Class MovieTrailerPanel.
 *
 * @author Manuel Laggner
 */
public class TrailerPanel extends JPanel {
  private static final long           serialVersionUID = 2506465845096043845L;
  /**
   * @wbp.nls.resourceBundle messages
   */
  private static final ResourceBundle BUNDLE           = ResourceBundle.getBundle("messages", new UTF8Control());
  private static final Logger         LOGGER           = LoggerFactory.getLogger(TrailerPanel.class);

  private MovieSelectionModel         movieSelectionModel;
  private TvShowSelectionModel        tvShowSelectionModel;
  private TmmTable                    table;
  private EventList<MediaTrailer>     trailerEventList = null;

  /**
   * Instantiates a new movie details panel.
   *
   * @param model
   *          the model
   */
  public TrailerPanel(MovieSelectionModel model) {
    this.movieSelectionModel = model;

    createLayout();

    // install the propertychangelistener
    PropertyChangeListener propertyChangeListener = propertyChangeEvent -> {
      String property = propertyChangeEvent.getPropertyName();
      Object source = propertyChangeEvent.getSource();

      if (source.getClass() != MovieSelectionModel.class) {
        return;
      }

      // react on selection of a movie and change of a trailer
      if ("selectedMovie".equals(property) || "trailer".equals(property)) {
        // this does sometimes not work. simply wrap it
        try {
          trailerEventList.getReadWriteLock().writeLock().lock();
          trailerEventList.clear();
          trailerEventList.addAll(movieSelectionModel.getSelectedMovie().getTrailer());
        }
        catch (Exception ignored) {
          // ignored
        }
        finally {
          trailerEventList.getReadWriteLock().writeLock().unlock();
        }

        TableColumnResizer.adjustColumnPreferredWidths(table, 7);
      }
    };

    movieSelectionModel.addPropertyChangeListener(propertyChangeListener);

  }

  public TrailerPanel(TvShowSelectionModel model) {
    this.tvShowSelectionModel = model;

    createLayout();

    // install the propertychangelistener
    PropertyChangeListener propertyChangeListener = propertyChangeEvent -> {
      String property = propertyChangeEvent.getPropertyName();
      Object source = propertyChangeEvent.getSource();

      if (source.getClass() != TvShowSelectionModel.class) {
        return;
      }

      // react on selection of a movie and change of a trailer
      if ("selectedTvShow".equals(property) || "trailer".equals(property)) {
        // this does sometimes not work. simply wrap it
        try {
          trailerEventList.getReadWriteLock().writeLock().lock();
          trailerEventList.clear();
          trailerEventList.addAll(tvShowSelectionModel.getSelectedTvShow().getTrailer());
        }
        catch (Exception ignored) {
          // ignored
        }
        finally {
          trailerEventList.getReadWriteLock().writeLock().unlock();
        }

        TableColumnResizer.adjustColumnPreferredWidths(table, 7);
      }
    };

    tvShowSelectionModel.addPropertyChangeListener(propertyChangeListener);

  }

  private void createLayout() {
    trailerEventList = GlazedListsSwing.swingThreadProxyList(
        new ObservableElementList<>(GlazedLists.threadSafeList(new BasicEventList<>()), GlazedLists.beanConnector(MediaTrailer.class)));
    DefaultEventTableModel<MediaTrailer> trailerTableModel = new DefaultEventTableModel<>(GlazedListsSwing.swingThreadProxyList(trailerEventList),
        new TrailerTableFormat());
    setLayout(new MigLayout("", "[400lp,grow]", "[250lp,grow]"));
    table = new TmmTable(trailerTableModel);
    table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
    table.setSelectionModel(new NullSelectionModel());

    JScrollPane scrollPane = new JScrollPane(table);
    table.configureScrollPane(scrollPane);
    add(scrollPane, "cell 0 0,grow");
    scrollPane.setViewportView(table);

    LinkListener linkListener = new LinkListener();
    table.addMouseListener(linkListener);
    table.addMouseMotionListener(linkListener);
  }

  private class TrailerTableFormat implements AdvancedTableFormat<MediaTrailer> {
    public TrailerTableFormat() {
    }

    @Override
    public int getColumnCount() {
      return 7;
    }

    @Override
    public String getColumnName(int column) {
      switch (column) {
        case 0:
        case 1:
          return "";

        case 2:
          return BUNDLE.getString("metatag.nfo");

        case 3:
          return BUNDLE.getString("metatag.name");

        case 4:
          return BUNDLE.getString("metatag.source");

        case 5:
          return BUNDLE.getString("metatag.quality");

        case 6:
          return BUNDLE.getString("metatag.format");
      }

      throw new IllegalStateException();
    }

    @Override
    public Object getColumnValue(MediaTrailer trailer, int column) {
      if (trailer == null) {
        return null;
      }

      switch (column) {
        case 0:
          if (StringUtils.isNotBlank(trailer.getUrl()) && trailer.getUrl().toLowerCase(Locale.ROOT).startsWith("http")) {
            return IconManager.DOWNLOAD;
          }
          return null;

        case 1:
          return IconManager.PLAY;

        case 2:
          return trailer.getInNfo();

        case 3:
          return trailer.getName();

        case 4:
          return trailer.getProvider();

        case 5:
          return trailer.getQuality();

        case 6:
          String ext = UrlUtil.getExtension(trailer.getUrl()).toLowerCase(Locale.ROOT);
          if (!Globals.settings.getVideoFileType().contains("." + ext)) {
            // .php redirection scripts et all
            ext = "";
          }
          return ext;
      }

      throw new IllegalStateException();
    }

    @SuppressWarnings("rawtypes")
    @Override
    public Class getColumnClass(int column) {
      switch (column) {
        case 0:
        case 1:
          return ImageIcon.class;

        case 2:
          return Boolean.class;

        case 3:
        case 4:
        case 5:
        case 6:
          return String.class;
      }

      throw new IllegalStateException();
    }

    @SuppressWarnings("rawtypes")
    @Override
    public Comparator getColumnComparator(int arg0) {
      return null;
    }
  }

  private class LinkListener implements MouseListener, MouseMotionListener {
    @Override
    public void mouseClicked(MouseEvent e) {
      JTable table = (JTable) e.getSource();
      int row = table.rowAtPoint(new Point(e.getX(), e.getY()));
      int col = table.columnAtPoint(new Point(e.getX(), e.getY()));

      // click on the download button
      if (col == 0) {
        row = table.convertRowIndexToModel(row);
        MediaTrailer trailer = trailerEventList.get(row);

        if (StringUtils.isNotBlank(trailer.getUrl()) && trailer.getUrl().toLowerCase(Locale.ROOT).startsWith("http")) {
          if (movieSelectionModel != null) {
            MovieHelpers.downloadTrailer(movieSelectionModel.getSelectedMovie(), trailer);
          }
          if (tvShowSelectionModel != null) {
            TvShowHelpers.downloadTrailer(tvShowSelectionModel.getSelectedTvShow(), trailer);
          }
        }
      }

      // click on the play button
      if (col == 1) {
        // try to open the browser
        row = table.convertRowIndexToModel(row);
        MediaTrailer trailer = trailerEventList.get(row);
        String url = trailer.getUrl();
        try {
          TmmUIHelper.browseUrl(url);
        }
        catch (Exception ex) {
          LOGGER.error(ex.getMessage());
          MessageManager.instance
              .pushMessage(new Message(MessageLevel.ERROR, url, "message.erroropenurl", new String[] { ":", ex.getLocalizedMessage() }));
        }
      }
    }

    @Override
    public void mouseEntered(MouseEvent e) {
      JTable table = (JTable) e.getSource();
      int col = table.columnAtPoint(new Point(e.getX(), e.getY()));
      if (col == 0 || col == 1) {
        table.setCursor(new Cursor(Cursor.HAND_CURSOR));
      }
    }

    @Override
    public void mouseExited(MouseEvent e) {
      JTable table = (JTable) e.getSource();
      int col = table.columnAtPoint(new Point(e.getX(), e.getY()));
      if (col != 0 && col != 1) {
        table.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
      }
    }

    @Override
    public void mouseMoved(MouseEvent e) {
      JTable table = (JTable) e.getSource();
      int col = table.columnAtPoint(new Point(e.getX(), e.getY()));
      if (col != 0 && col != 1 && table.getCursor().getType() == Cursor.HAND_CURSOR) {
        table.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
      }
      if ((col == 0 || col == 1) && table.getCursor().getType() == Cursor.DEFAULT_CURSOR) {
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

  private class NullSelectionModel extends DefaultListSelectionModel {
    private static final long serialVersionUID = -1956483331520197616L;

    @Override
    public boolean isSelectionEmpty() {
      return true;
    }

    @Override
    public boolean isSelectedIndex(int index) {
      return false;
    }

    @Override
    public int getMinSelectionIndex() {
      return -1;
    }

    @Override
    public int getMaxSelectionIndex() {
      return -1;
    }

    @Override
    public int getLeadSelectionIndex() {
      return -1;
    }

    @Override
    public int getAnchorSelectionIndex() {
      return -1;
    }

    @Override
    public void setSelectionInterval(int index0, int index1) {
    }

    @Override
    public void setLeadSelectionIndex(int index) {
    }

    @Override
    public void setAnchorSelectionIndex(int index) {
    }

    @Override
    public void addSelectionInterval(int index0, int index1) {
    }

    @Override
    public void insertIndexInterval(int index, int length, boolean before) {
    }

    @Override
    public void clearSelection() {
    }

    @Override
    public void removeSelectionInterval(int index0, int index1) {
    }

    @Override
    public void removeIndexInterval(int index0, int index1) {
    }

    @Override
    public void setSelectionMode(int selectionMode) {
    }

    @Override
    public int getSelectionMode() {
      return SINGLE_SELECTION;
    }

    @Override
    public void addListSelectionListener(ListSelectionListener lsl) {
    }

    @Override
    public void removeListSelectionListener(ListSelectionListener lsl) {
    }

    @Override
    public void setValueIsAdjusting(boolean valueIsAdjusting) {
    }

    @Override
    public boolean getValueIsAdjusting() {
      return false;
    }
  }
}
