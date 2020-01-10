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
package org.tinymediamanager.ui.components.datepicker;

import java.awt.BorderLayout;
import java.awt.Insets;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.MenuElement;
import javax.swing.MenuSelectionManager;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.tinymediamanager.ui.IconManager;

/**
 * The class DatePicker is used to provide a HTML like date picker
 * 
 * @author Manuel Laggner
 */
public class DatePicker extends JPanel implements PropertyChangeListener {
  private static final long serialVersionUID = -2806415143740677890L;

  private DateTextField     dateEditor;
  private JButton           calendarButton;
  private CalendarPanel     calendarPanel;
  protected JPopupMenu      popup;

  private boolean           dateSelected;

  private ChangeListener    changeListener;

  /**
   * Creates a new DatePicker with no date set
   */
  public DatePicker() {
    this(null, null);
  }

  /**
   * Creates a new DatePicker with the given date set
   * 
   * @param date
   *          the date or null
   */
  public DatePicker(Date date) {
    this(date, null);
  }

  /**
   * Creates a new DatePicker with the given date set and the given date format
   * 
   * @param date
   *          the date or null
   * @param dateFormat
   *          the date format string or null (then MEDIUM SimpleDateFormat format is used)
   */
  public DatePicker(Date date, String dateFormat) {
    setLayout(new BorderLayout());

    dateEditor = new DateTextField(dateFormat);
    dateEditor.addPropertyChangeListener("date", this);

    calendarPanel = new CalendarPanel(date);
    calendarPanel.addPropertyChangeListener("day", this);

    setDate(date);

    calendarButton = new JButton(IconManager.DATE_PICKER);
    calendarButton.setFocusable(false);
    calendarButton.setMargin(new Insets(0, 0, 0, 0));
    calendarButton.addActionListener(e -> {
      int x = -dateEditor.getWidth();
      int y = calendarButton.getHeight();

      Calendar calendar = Calendar.getInstance();
      Date date1 = dateEditor.getDate();
      if (date1 != null) {
        calendar.setTime(date1);
      }
      calendarPanel.setCalendar(calendar);
      popup.show(calendarButton, x, y);
      dateSelected = false;
    });

    add(calendarButton, BorderLayout.EAST);
    add(this.dateEditor, BorderLayout.CENTER);

    popup = new JPopupMenu() {
      private static final long serialVersionUID = 3478677561327475762L;

      @Override
      public void setVisible(boolean visible) {
        Boolean isCanceled = (Boolean) getClientProperty("JPopupMenu.firePopupMenuCanceled");
        if (visible || (!visible && dateSelected) || ((isCanceled != null) && !visible && isCanceled.booleanValue())) {
          super.setVisible(visible);
        }
      }
    };

    popup.setLightWeightPopupEnabled(true);
    popup.add(calendarPanel);

    changeListener = new ChangeListener() {
      boolean hasListened = false;

      @Override
      public void stateChanged(ChangeEvent e) {
        if (hasListened) {
          hasListened = false;
          return;
        }
        if (popup.isVisible() && DatePicker.this.calendarPanel.monthComboBox.hasFocus()) {
          MenuElement[] menuElements = MenuSelectionManager.defaultManager().getSelectedPath();
          MenuElement[] newMenuElements = new MenuElement[menuElements.length + 1];
          newMenuElements[0] = popup;
          System.arraycopy(menuElements, 0, newMenuElements, 1, menuElements.length);
          hasListened = true;
          MenuSelectionManager.defaultManager().setSelectedPath(newMenuElements);
        }
      }
    };
    MenuSelectionManager.defaultManager().addChangeListener(changeListener);
  }

  @Override
  public void propertyChange(PropertyChangeEvent evt) {
    if (evt.getPropertyName().equals("day") && popup.isVisible()) {
      dateSelected = true;
      popup.setVisible(false);
      if ((Integer) evt.getNewValue() > 0) {
        setDate(calendarPanel.getCalendar().getTime());
      }
      else {
        setDate(null);
      }
    }
    else if (evt.getPropertyName().equals("date")) {
      if (evt.getSource() == dateEditor) {
        firePropertyChange("date", evt.getOldValue(), evt.getNewValue());
      }
      else {
        setDate((Date) evt.getNewValue());
      }
    }
  }

  @Override
  public void updateUI() {
    super.updateUI();
    setEnabled(isEnabled());

    if (calendarPanel != null) {
      SwingUtilities.updateComponentTreeUI(popup);
    }
  }

  @Override
  public void setLocale(Locale l) {
    super.setLocale(l);
    dateEditor.setLocale(l);
    calendarPanel.setLocale(l);
  }

  /**
   * Returns the date. If the JDateChooser is started with a null date and no date was set by the user, null is returned.
   * 
   * @return the current date
   */
  public Date getDate() {
    return dateEditor.getDate();
  }

  /**
   * Sets the date. Fires the property change "date" if date != null.
   * 
   * @param date
   *          the new date.
   */
  public void setDate(Date date) {
    dateEditor.setDate(date);
    if (getParent() != null) {
      getParent().invalidate();
    }
  }

  /**
   * Returns the calendar. If the JDateChooser is started with a null date (or null calendar) and no date was set by the user, null is returned.
   * 
   * @return the current calendar
   */
  public Calendar getCalendar() {
    Date date = getDate();
    if (date == null) {
      return null;
    }
    Calendar calendar = Calendar.getInstance();
    calendar.setTime(date);
    return calendar;
  }

  /**
   * Sets the calendar. Value null will set the null date on the date editor.
   * 
   * @param calendar
   *          the calendar.
   */
  public void setCalendar(Calendar calendar) {
    if (calendar == null) {
      dateEditor.setDate(null);
    }
    else {
      dateEditor.setDate(calendar.getTime());
    }
  }

  /**
   * Enable or disable the JDateChooser.
   * 
   * @param enabled
   *          the new enabled value
   */
  @Override
  public void setEnabled(boolean enabled) {
    super.setEnabled(enabled);
    if (dateEditor != null) {
      dateEditor.setEnabled(enabled);
      calendarButton.setEnabled(enabled);
    }
  }

  public void cleanup() {
    MenuSelectionManager.defaultManager().removeChangeListener(changeListener);
    changeListener = null;
  }

  @Override
  public boolean requestFocusInWindow() {
    if (dateEditor != null) {
      return dateEditor.requestFocusInWindow();
    }
    return super.requestFocusInWindow();
  }
}
