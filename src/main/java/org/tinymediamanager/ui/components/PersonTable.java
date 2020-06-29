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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.core.Message;
import org.tinymediamanager.core.MessageManager;
import org.tinymediamanager.core.UTF8Control;
import org.tinymediamanager.core.entities.Person;
import org.tinymediamanager.ui.IconManager;
import org.tinymediamanager.ui.TmmUIHelper;
import org.tinymediamanager.ui.components.table.TmmTable;
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
public class PersonTable extends TmmTable {
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

    DefaultEventTableModel<Person> personTableModel = new TmmTableModel<>(GlazedListsSwing.swingThreadProxyList(personEventList),
        new PersonTableFormat(edit));
    setModel(personTableModel);
    // init();

    adjustColumnPreferredWidths(3);
    PersonTableButtonListener listener = new PersonTableButtonListener(this, personEventList, BUNDLE.getString("cast.edit"));
    addMouseListener(listener);
    addMouseMotionListener(listener);
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
          return IconManager.TABLE_OK;
        }
        return IconManager.TABLE_NOT_OK;
      }, ImageIcon.class);
      col.setColumnResizeable(false);
      col.setHeaderIcon(IconManager.IMAGES);
      addColumn(col);

      /*
       * profile
       */
      col = new Column(BUNDLE.getString("profile.url"), "profileUrl", person -> {
        if (StringUtils.isNotBlank(person.getProfileUrl())) {
          return IconManager.TABLE_OK;
        }
        return IconManager.TABLE_NOT_OK;
      }, ImageIcon.class);
      col.setColumnResizeable(false);
      col.setHeaderIcon(IconManager.IDCARD);
      addColumn(col);

      /*
       * edit
       */
      if (edit) {
        col = new Column(BUNDLE.getString("Button.edit"), "edit", person -> IconManager.EDIT, ImageIcon.class);
        col.setColumnResizeable(false);
        col.setHeaderIcon(IconManager.EDIT_HEADER);
        addColumn(col);
      }
    }
  }

  /**
   * helper class for listening to the edit button
   */
  private static class PersonTableButtonListener implements MouseListener, MouseMotionListener {
    private static final Logger     LOGGER = LoggerFactory.getLogger(PersonTableButtonListener.class);
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
      int row = personTable.rowAtPoint(arg0.getPoint());
      int col = personTable.columnAtPoint(arg0.getPoint());

      if (isLinkColumn(row, col)) {
        row = personTable.convertRowIndexToModel(row);
        Person person = personEventList.get(row);

        if (person != null) {
          if (isEditorColumn(col)) {
            PersonEditorDialog dialog = new PersonEditorDialog(SwingUtilities.getWindowAncestor(personTable), windowTitle, person);
            dialog.setVisible(true);
          }
          else if (isProfileColumn(row, col)) {
            try {
              TmmUIHelper.browseUrl(person.getProfileUrl());
            }
            catch (Exception e1) {
              LOGGER.error("Opening actor profile", e1);
              MessageManager.instance.pushMessage(new Message(Message.MessageLevel.ERROR, person.getProfileUrl(), "message.erroropenurl",
                  new String[] { ":", e1.getLocalizedMessage() }));
            }
          }
        }
      }
    }

    @Override
    public void mouseEntered(MouseEvent e) {
      JTable table = (JTable) e.getSource();

      Point point = new Point(e.getX(), e.getY());
      int row = table.rowAtPoint(point);
      int col = table.columnAtPoint(point);

      if (isLinkColumn(row, col)) {
        table.setCursor(new Cursor(Cursor.HAND_CURSOR));
      }
    }

    @Override
    public void mouseExited(MouseEvent e) {
      JTable table = (JTable) e.getSource();

      Point point = new Point(e.getX(), e.getY());
      int row = table.rowAtPoint(point);
      int col = table.columnAtPoint(point);

      if (!isLinkColumn(row, col)) {
        table.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
      }
    }

    @Override
    public void mouseMoved(MouseEvent e) {
      JTable table = (JTable) e.getSource();

      Point point = new Point(e.getX(), e.getY());
      int row = table.rowAtPoint(point);
      int col = table.columnAtPoint(point);

      if (!isLinkColumn(row, col) && table.getCursor().getType() == Cursor.HAND_CURSOR) {
        table.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
      }
      if (isLinkColumn(row, col) && table.getCursor().getType() == Cursor.DEFAULT_CURSOR) {
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
     * check whether this column is the profile column
     * 
     * @param column
     *          the column
     * @return true/false
     */
    private boolean isLinkColumn(int row, int column) {
      return isEditorColumn(column) || isProfileColumn(row, column);
    }

    /**
     * check whether this column is the profile column
     *
     * @param row
     *          the row index
     * 
     * @param column
     *          the column index
     * @return true/false
     */
    private boolean isProfileColumn(int row, int column) {
      if (column < 0 || row < 0) {
        return false;
      }

      if (!"profileUrl".equals(personTable.getColumnModel().getColumn(column).getIdentifier())) {
        return false;
      }

      // check if that person has a profile url
      row = personTable.convertRowIndexToModel(row);
      Person person = personEventList.get(row);

      return StringUtils.isNotBlank(person.getProfileUrl());
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
