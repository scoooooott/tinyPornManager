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
import java.awt.GridLayout;
import java.awt.event.ItemEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.SpinnerNumberModel;

import org.tinymediamanager.core.UTF8Control;

/**
 * The class CalendarPanel is used to display a calendar like panel for date choosing
 * 
 * @author Manuel Laggner
 */
class CalendarPanel extends JPanel implements PropertyChangeListener {
  private static final long           serialVersionUID = -1214699062624370112L;
  private static final ResourceBundle BUNDLE           = ResourceBundle.getBundle("messages", new UTF8Control());

  private Calendar                    calendar;
  protected Locale                    locale;

  MonthComboBox                       monthComboBox;
  YearSpinner                         yearSpinner;
  DayPanel                            dayPanel;

  public CalendarPanel() {
    this(null);
  }

  public CalendarPanel(Date date) {
    setLayout(new BorderLayout());

    locale = Locale.getDefault();
    calendar = Calendar.getInstance(this.locale);

    JPanel monthYearPanel = new JPanel();
    monthYearPanel.setLayout(new BorderLayout());

    monthComboBox = new MonthComboBox();
    yearSpinner = new YearSpinner();

    monthYearPanel.add(monthComboBox, BorderLayout.WEST);
    monthYearPanel.add(yearSpinner, BorderLayout.CENTER);
    monthYearPanel.setBorder(BorderFactory.createEmptyBorder());

    dayPanel = new DayPanel();
    dayPanel.setMonth(monthComboBox.getSelectedIndex());
    dayPanel.setYear((int) yearSpinner.getValue());
    dayPanel.addPropertyChangeListener(this);
    dayPanel.setLocale(this.locale);

    monthComboBox.addItemListener(e -> {
      if (e.getStateChange() == ItemEvent.SELECTED) {
        int index = monthComboBox.getSelectedIndex();
        Calendar c = (Calendar) calendar.clone();
        c.set(Calendar.MONTH, index);
        setCalendar(c, false);
        dayPanel.setMonth(index);
      }
    });

    yearSpinner.addChangeListener(e -> {
      SpinnerNumberModel model = (SpinnerNumberModel) yearSpinner.getModel();
      int value = model.getNumber().intValue();
      Calendar c = (Calendar) calendar.clone();
      c.set(Calendar.YEAR, value);
      setCalendar(c, false);
      dayPanel.setYear(value);
    });
    add(monthYearPanel, BorderLayout.NORTH);
    add(dayPanel, BorderLayout.CENTER);

    JPanel specialButtonPanel = new JPanel();

    JButton todayButton = new JButton();
    todayButton.addActionListener(e -> setDate(new Date()));

    JButton noDateButton = new JButton();
    noDateButton.addActionListener(e -> firePropertyChange("day", 0, -1));

    specialButtonPanel.setLayout(new GridLayout(1, 2));
    todayButton.setText(BUNDLE.getString("Button.today"));
    specialButtonPanel.add(todayButton);

    noDateButton.setText(BUNDLE.getString("Button.nodate"));
    specialButtonPanel.add(noDateButton);
    add(specialButtonPanel, BorderLayout.SOUTH);

    if (date != null) {
      calendar.setTime(date);
    }

    setCalendar(calendar);
  }

  @Override
  public void propertyChange(PropertyChangeEvent evt) {
    if (calendar != null) {
      Calendar c = (Calendar) calendar.clone();

      if (evt.getPropertyName().equals("day")) {
        c.set(Calendar.DAY_OF_MONTH, (Integer) evt.getNewValue());
        setCalendar(c, false);
        firePropertyChange("day", evt.getOldValue(), evt.getNewValue());
      }
      else if (evt.getPropertyName().equals("date")) {
        c.setTime((Date) evt.getNewValue());
        setCalendar(c, true);
      }
    }
  }

  /**
   * Returns the calendar
   *
   * @return the value of the calendar
   */
  public Calendar getCalendar() {
    return calendar;
  }

  /**
   * Sets the calendar
   * 
   * @param calendar
   *          the new calendar
   */
  public void setCalendar(Calendar calendar) {
    setCalendar(calendar, true);
  }

  /**
   * Sets the calendar attribute of the JCalendar object
   * 
   * @param newCalendar
   *          the new calendar value
   * @param update
   *          the new calendar value
   */
  private void setCalendar(Calendar newCalendar, boolean update) {
    if (newCalendar == null) {
      // setDate(null); // WILL throw NPE
      return;
    }
    Calendar oldCalendar = calendar;
    calendar = newCalendar;

    if (update) {
      yearSpinner.setValue(newCalendar.get(Calendar.YEAR));
      monthComboBox.setSelectedIndex(newCalendar.get(Calendar.MONTH));
      dayPanel.setDay(newCalendar.get(Calendar.DATE));
    }

    firePropertyChange("calendar", oldCalendar, calendar);
  }

  /**
   * Returns a Date object.
   * 
   * @return a date object constructed from the calendar
   */
  public Date getDate() {
    return new Date(calendar.getTimeInMillis());
  }

  /**
   * Sets the date. Fires the property change "date".
   * 
   * @param date
   *          the new date.
   */
  public void setDate(Date date) {
    Date oldDate = calendar.getTime();
    calendar.setTime(date);
    int year = calendar.get(Calendar.YEAR);
    int month = calendar.get(Calendar.MONTH);
    int day = calendar.get(Calendar.DAY_OF_MONTH);

    yearSpinner.setValue(year);
    monthComboBox.setSelectedIndex(month);
    dayPanel.setCalendar(calendar);
    dayPanel.setDay(day);

    firePropertyChange("date", oldDate, date);
  }
}
