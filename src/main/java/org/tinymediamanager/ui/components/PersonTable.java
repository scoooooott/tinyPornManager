/*
 * Copyright 2012 - 2017 Manuel Laggner
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

import java.awt.Cursor;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ResourceBundle;

import javax.swing.ImageIcon;
import javax.swing.JTable;
import javax.swing.SwingUtilities;

import org.apache.commons.lang3.StringUtils;
import org.tinymediamanager.core.entities.Person;
import org.tinymediamanager.ui.IconManager;
import org.tinymediamanager.ui.UTF8Control;
import org.tinymediamanager.ui.components.table.TmmTableFormat;
import org.tinymediamanager.ui.components.table.TmmTableModel;
import org.tinymediamanager.ui.dialogs.PersonEditorDialog;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.swing.DefaultEventTableModel;
import ca.odell.glazedlists.swing.GlazedListsSwing;

/**
 * This class is used to display Persons in a table
 *
 * @author Manuel Laggner
 */
public class PersonTable extends org.tinymediamanager.ui.components.table.TmmTable {
  /** @wbp.nls.resourceBundle messages */
  private static final ResourceBundle BUNDLE = ResourceBundle.getBundle("messages", new UTF8Control());

  /**
   * create a PersonTable for display only
   * 
   * @param personEventList
   *          the EventList containing the Persons
   */
  public PersonTable(EventList<Person> personEventList) {
    this(personEventList, false);
  }

  /**
   * create a PersonTable for either displaying or editing
   * 
   * @param personEventList
   *          the EventList containing the Persons
   * @param edit
   *          editable or not
   */
  public PersonTable(EventList<Person> personEventList, boolean edit) {
    super();

    DefaultEventTableModel<Person> castTableModel = new TmmTableModel<>(GlazedListsSwing.swingThreadProxyList(personEventList),
        new PersonTableFormat(edit));
    setModel(castTableModel);
    // init();

    adjustColumnPreferredWidths(3);
    if (edit) {
      PersonTableButtonListener listener = new PersonTableButtonListener(this, personEventList, BUNDLE.getString("cast.edit"));
      addMouseListener(listener);
      addMouseMotionListener(listener);
    }
  }

  /**
   * helper class for the table model
   */
  private static class PersonTableFormat extends TmmTableFormat<Person> {
    private PersonTableFormat(boolean edit) {
      /*
       * name
       */
      Column col = new Column(BUNDLE.getString("metatag.name"), "name", Person::getName, String.class);
      col.setColumnResizeable(true);
      addColumn(col);

      /*
       * role
       */
      col = new Column(BUNDLE.getString("metatag.role"), "role", Person::getRole, String.class);
      col.setColumnResizeable(true);
      addColumn(col);

      /*
       * image
       */
      col = new Column(BUNDLE.getString("image.url"), "imageUrl", person -> {
        if (StringUtils.isNotBlank(person.getThumbUrl())) {
          return IconManager.DOT_AVAILABLE;
        }
        return IconManager.DOT_UNAVAILABLE;
      }, ImageIcon.class);
      col.setColumnResizeable(false);
      col.setHeaderIcon(IconManager.IMAGES);
      addColumn(col);

      /*
       * edit
       */
      if (edit) {
        col = new Column(BUNDLE.getString("Button.edit"), "edit", person -> IconManager.EDIT, ImageIcon.class);
        col.setColumnResizeable(false);
        col.setHeaderIcon(IconManager.EDIT);
        addColumn(col);
      }
    }
  }

  /**
   * helper class for listening to the edit button
   */
  private static class PersonTableButtonListener implements MouseListener, MouseMotionListener {
    private final JTable            personTable;
    private final EventList<Person> personEventList;
    private final String            windowTitle;

    private PersonTableButtonListener(JTable personTable, EventList<Person> personEventList, String windowTitle) {
      this.personTable = personTable;
      this.personEventList = personEventList;
      this.windowTitle = windowTitle;
    }

    @Override
    public void mouseClicked(MouseEvent arg0) {
      int col = personTable.columnAtPoint(arg0.getPoint());
      if (isEditorColumn(col)) {
        int row = personTable.rowAtPoint(arg0.getPoint());
        row = personTable.convertRowIndexToModel(row);
        Person person = personEventList.get(row);
        if (person != null) {
          PersonEditorDialog dialog = new PersonEditorDialog(SwingUtilities.getWindowAncestor(personTable), windowTitle, person);
          dialog.setVisible(true);
        }
      }
    }

    @Override
    public void mouseEntered(MouseEvent e) {
      JTable table = (JTable) e.getSource();
      int col = table.columnAtPoint(new Point(e.getX(), e.getY()));
      if (isEditorColumn(col)) {
        table.setCursor(new Cursor(Cursor.HAND_CURSOR));
      }
    }

    @Override
    public void mouseExited(MouseEvent e) {
      JTable table = (JTable) e.getSource();
      int col = table.columnAtPoint(new Point(e.getX(), e.getY()));
      if (!isEditorColumn(col)) {
        table.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
      }
    }

    @Override
    public void mouseMoved(MouseEvent e) {
      JTable table = (JTable) e.getSource();
      int col = table.columnAtPoint(new Point(e.getX(), e.getY()));
      isEditorColumn(col);
      if (!isEditorColumn(col) && table.getCursor().getType() == Cursor.HAND_CURSOR) {
        table.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
      }
      if (isEditorColumn(col) && table.getCursor().getType() == Cursor.DEFAULT_CURSOR) {
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

    /**
     * check whether this column is the edit column
     *
     * @param column
     *          the column index
     * @return true/false
     */
    private boolean isEditorColumn(int column) {
      if (column < 0) {
        return false;
      }
      return "edit".equals(personTable.getColumnModel().getColumn(column).getIdentifier());
    }
  }
}
