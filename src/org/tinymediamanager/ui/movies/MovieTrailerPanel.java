/*
 * Copyright 2012 Manuel Laggner
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
package org.tinymediamanager.ui.movies;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.URI;
import java.util.Comparator;
import java.util.ResourceBundle;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;

import org.apache.log4j.Logger;
import org.tinymediamanager.core.movie.Movie;
import org.tinymediamanager.scraper.MediaTrailer;
import org.tinymediamanager.ui.MyTable;
import org.tinymediamanager.ui.TableColumnAdjuster;
import org.tinymediamanager.ui.UTF8Control;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.gui.AdvancedTableFormat;
import ca.odell.glazedlists.swing.EventTableModel;

import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

/**
 * The Class MovieTrailerPanel.
 * 
 * @author Manuel Laggner
 */
public class MovieTrailerPanel extends JPanel {

  /** The Constant BUNDLE. */
  private static final ResourceBundle   BUNDLE              = ResourceBundle.getBundle("messages", new UTF8Control());       //$NON-NLS-1$

  /** The Constant serialVersionUID. */
  private static final long             serialVersionUID    = 1L;

  /** The logger. */
  private static Logger                 LOGGER              = Logger.getLogger(MovieTrailerPanel.class);

  /** The movie selection model. */
  private MovieSelectionModel           movieSelectionModel;

  /** The table. */
  private JTable                        table;

  /** The table column adjuster. */
  private TableColumnAdjuster           tableColumnAdjuster = null;

  /** The trailer event list. */
  private EventList<MediaTrailer>       trailerEventList    = GlazedLists.threadSafeList(new BasicEventList<MediaTrailer>());

  /** The trailer table model. */
  private EventTableModel<MediaTrailer> trailerTableModel   = null;

  /**
   * Instantiates a new movie details panel.
   * 
   * @param model
   *          the model
   */
  public MovieTrailerPanel(MovieSelectionModel model) {
    this.movieSelectionModel = model;
    setLayout(new FormLayout(new ColumnSpec[] { FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("default:grow"), }, new RowSpec[] {
        FormFactory.RELATED_GAP_ROWSPEC, RowSpec.decode("default:grow"), }));

    trailerTableModel = new EventTableModel<MediaTrailer>(trailerEventList, new TrailerTableFormat());
    table = new MyTable(trailerTableModel);
    table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
    table.setSelectionModel(new NullSelectionModel());

    JScrollPane scrollPane = MyTable.createStripedJScrollPane(table);
    add(scrollPane, "2, 2, fill, fill");
    scrollPane.setViewportView(table);

    // make the url clickable
    URLRenderer renderer = new URLRenderer(table);
    table.getColumnModel().getColumn(4).setCellRenderer(renderer);
    table.addMouseListener(renderer);
    table.addMouseMotionListener(renderer);

    tableColumnAdjuster = new TableColumnAdjuster(table);
    tableColumnAdjuster.setColumnDataIncluded(true);
    tableColumnAdjuster.setColumnHeaderIncluded(true);

    // install the propertychangelistener
    PropertyChangeListener propertyChangeListener = new PropertyChangeListener() {
      public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
        String property = propertyChangeEvent.getPropertyName();
        Object source = propertyChangeEvent.getSource();
        // react on selection of a movie and change of a trailer
        if ((source.getClass() == MovieSelectionModel.class && "selectedMovie".equals(property))
            || (source.getClass() == Movie.class && "trailer".equals(property))) {
          trailerEventList.clear();
          trailerEventList.addAll(movieSelectionModel.getSelectedMovie().getTrailers());
          tableColumnAdjuster.adjustColumns();
        }
      }
    };

    movieSelectionModel.addPropertyChangeListener(propertyChangeListener);

  }

  /**
   * The Class TrailerTableFormat.
   * 
   * @author Manuel Laggner
   */
  private static class TrailerTableFormat implements AdvancedTableFormat<MediaTrailer> {

    /**
     * Instantiates a new trailer table format.
     */
    public TrailerTableFormat() {
    }

    /*
     * (non-Javadoc)
     * 
     * @see ca.odell.glazedlists.gui.TableFormat#getColumnCount()
     */
    @Override
    public int getColumnCount() {
      return 5;
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
          return BUNDLE.getString("metatag.nfo"); //$NON-NLS-1$

        case 1:
          return BUNDLE.getString("metatag.name"); //$NON-NLS-1$

        case 2:
          return BUNDLE.getString("metatag.source"); //$NON-NLS-1$

        case 3:
          return BUNDLE.getString("metatag.quality"); //$NON-NLS-1$

        case 4:
          return BUNDLE.getString("metatag.url"); //$NON-NLS-1$
      }

      throw new IllegalStateException();
    }

    /*
     * (non-Javadoc)
     * 
     * @see ca.odell.glazedlists.gui.TableFormat#getColumnValue(java.lang.Object, int)
     */
    @Override
    public Object getColumnValue(MediaTrailer trailer, int column) {
      if (trailer == null) {
        return null;
      }

      switch (column) {
        case 0:
          return trailer.getInNfo();

        case 1:
          return trailer.getName();

        case 2:
          return trailer.getProvider();

        case 3:
          return trailer.getQuality();

        case 4:
          return trailer.getUrl();
      }

      throw new IllegalStateException();
    }

    /*
     * (non-Javadoc)
     * 
     * @see ca.odell.glazedlists.gui.AdvancedTableFormat#getColumnClass(int)
     */
    @Override
    public Class getColumnClass(int column) {
      switch (column) {
        case 0:
          return Boolean.class;

        case 1:
        case 2:
        case 3:
        case 4:
          return String.class;
      }

      throw new IllegalStateException();
    }

    /*
     * (non-Javadoc)
     * 
     * @see ca.odell.glazedlists.gui.AdvancedTableFormat#getColumnComparator(int)
     */
    @Override
    public Comparator getColumnComparator(int arg0) {
      // TODO Auto-generated method stub
      return null;
    }
  }

  /**
   * The Class URLRenderer.
   * 
   * @author Manuel Laggner
   */
  private static class URLRenderer extends DefaultTableCellRenderer implements MouseListener, MouseMotionListener {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /**
     * Instantiates a new uRL renderer.
     * 
     * @param table
     *          the table
     */
    public URLRenderer(JTable table) {
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.swing.table.DefaultTableCellRenderer#getTableCellRendererComponent (javax.swing.JTable, java.lang.Object, boolean, boolean, int,
     * int)
     */
    @Override
    public Component getTableCellRendererComponent(JTable table, final Object value, boolean arg2, boolean arg3, int arg4, int arg5) {
      final JLabel lab = new JLabel("<html><font color=\"#0000CF\"><u>" + value + "</u></font></html>");
      return lab;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
     */
    @Override
    public void mouseClicked(MouseEvent e) {
      JTable table = (JTable) e.getSource();
      int row = table.rowAtPoint(new Point(e.getX(), e.getY()));
      int col = table.columnAtPoint(new Point(e.getX(), e.getY()));

      if (col == 4) {
        // try to open the browser
        try {
          Desktop.getDesktop().browse(new URI((String) table.getModel().getValueAt(row, col)));
        }
        catch (Exception ex) {
          LOGGER.warn(ex.getMessage());
        }
      }
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.awt.event.MouseListener#mouseEntered(java.awt.event.MouseEvent)
     */
    @Override
    public void mouseEntered(MouseEvent e) {
      JTable table = (JTable) e.getSource();
      int col = table.columnAtPoint(new Point(e.getX(), e.getY()));
      if (col == 4) {
        table.setCursor(new Cursor(Cursor.HAND_CURSOR));
      }
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.awt.event.MouseListener#mouseExited(java.awt.event.MouseEvent)
     */
    @Override
    public void mouseExited(MouseEvent e) {
      JTable table = (JTable) e.getSource();
      int col = table.columnAtPoint(new Point(e.getX(), e.getY()));
      if (col != 4) {
        table.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
      }
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.awt.event.MouseMotionListener#mouseMoved(java.awt.event.MouseEvent)
     */
    @Override
    public void mouseMoved(MouseEvent e) {
      JTable table = (JTable) e.getSource();
      int col = table.columnAtPoint(new Point(e.getX(), e.getY()));
      if (col != 4 && table.getCursor().getType() == Cursor.HAND_CURSOR) {
        table.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
      }
      if (col == 4 && table.getCursor().getType() == Cursor.DEFAULT_CURSOR) {
        table.setCursor(new Cursor(Cursor.HAND_CURSOR));
      }
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent)
     */
    @Override
    public void mousePressed(MouseEvent e) {
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
     */
    @Override
    public void mouseReleased(MouseEvent e) {
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.awt.event.MouseMotionListener#mouseDragged(java.awt.event.MouseEvent )
     */
    @Override
    public void mouseDragged(MouseEvent arg0) {
      // TODO Auto-generated method stub

    }

  }

  /**
   * The Class NullSelectionModel.
   * 
   * @author Manuel Laggner
   */
  private static class NullSelectionModel implements ListSelectionModel {

    /*
     * (non-Javadoc)
     * 
     * @see javax.swing.ListSelectionModel#isSelectionEmpty()
     */
    public boolean isSelectionEmpty() {
      return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.swing.ListSelectionModel#isSelectedIndex(int)
     */
    public boolean isSelectedIndex(int index) {
      return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.swing.ListSelectionModel#getMinSelectionIndex()
     */
    public int getMinSelectionIndex() {
      return -1;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.swing.ListSelectionModel#getMaxSelectionIndex()
     */
    public int getMaxSelectionIndex() {
      return -1;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.swing.ListSelectionModel#getLeadSelectionIndex()
     */
    public int getLeadSelectionIndex() {
      return -1;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.swing.ListSelectionModel#getAnchorSelectionIndex()
     */
    public int getAnchorSelectionIndex() {
      return -1;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.swing.ListSelectionModel#setSelectionInterval(int, int)
     */
    public void setSelectionInterval(int index0, int index1) {
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.swing.ListSelectionModel#setLeadSelectionIndex(int)
     */
    public void setLeadSelectionIndex(int index) {
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.swing.ListSelectionModel#setAnchorSelectionIndex(int)
     */
    public void setAnchorSelectionIndex(int index) {
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.swing.ListSelectionModel#addSelectionInterval(int, int)
     */
    public void addSelectionInterval(int index0, int index1) {
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.swing.ListSelectionModel#insertIndexInterval(int, int, boolean)
     */
    public void insertIndexInterval(int index, int length, boolean before) {
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.swing.ListSelectionModel#clearSelection()
     */
    public void clearSelection() {
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.swing.ListSelectionModel#removeSelectionInterval(int, int)
     */
    public void removeSelectionInterval(int index0, int index1) {
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.swing.ListSelectionModel#removeIndexInterval(int, int)
     */
    public void removeIndexInterval(int index0, int index1) {
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.swing.ListSelectionModel#setSelectionMode(int)
     */
    public void setSelectionMode(int selectionMode) {
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.swing.ListSelectionModel#getSelectionMode()
     */
    public int getSelectionMode() {
      return SINGLE_SELECTION;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.swing.ListSelectionModel#addListSelectionListener(javax.swing.event .ListSelectionListener)
     */
    public void addListSelectionListener(ListSelectionListener lsl) {
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.swing.ListSelectionModel#removeListSelectionListener(javax.swing .event.ListSelectionListener)
     */
    public void removeListSelectionListener(ListSelectionListener lsl) {
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.swing.ListSelectionModel#setValueIsAdjusting(boolean)
     */
    public void setValueIsAdjusting(boolean valueIsAdjusting) {
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.swing.ListSelectionModel#getValueIsAdjusting()
     */
    public boolean getValueIsAdjusting() {
      return false;
    }
  }
}
