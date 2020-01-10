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
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseListener;
import java.text.DateFormatSymbols;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import javax.swing.JButton;
import javax.swing.JPanel;

import org.tinymediamanager.ui.UIConstants;

/**
 * The class DayPanel is used to display a panel for day choosing
 * 
 * @author Manuel Laggner
 */
class DayPanel extends JPanel implements ActionListener {
  private static final long serialVersionUID = -4247612348953136350L;

  private int               day;
  private Calendar          calendar;
  private Calendar          today;
  private Locale            locale;

  private JButton[]         days;
  private JButton           selectedDay;

  private Color             transparentBackgroundColor;
  private Color             selectedColor;
  private Color             sundayForeground;
  private Color             weekdayForeground;
  private Color             decorationBackgroundColor;

  DayPanel() {
    setBackground(Color.blue);

    locale = Locale.getDefault();
    days = new JButton[49];
    selectedDay = null;
    calendar = Calendar.getInstance(locale);
    today = (Calendar) calendar.clone();

    setLayout(new BorderLayout());

    JPanel dayPanel = new JPanel();
    dayPanel.setLayout(new GridLayout(7, 7));

    sundayForeground = new Color(164, 0, 0);
    weekdayForeground = UIConstants.LINK_COLOR;
    decorationBackgroundColor = new Color(210, 228, 238);
    selectedColor = new Color(160, 160, 160);
    transparentBackgroundColor = new Color(255, 255, 255, 0);

    for (int row = 0; row < 7; row++) {
      for (int column = 0; column < 7; column++) {
        int index = column + (7 * row);

        if (row == 0) {
          days[index] = new DecoratorButton();
        }
        else {
          days[index] = new JButton();
          days[index].setBorderPainted(false);
          days[index].addActionListener(this);
        }

        days[index].setMargin(new Insets(0, 0, 0, 0));
        days[index].setFocusPainted(false);
        dayPanel.add(days[index]);
      }
    }

    init();

    setDay(Calendar.getInstance().get(Calendar.DAY_OF_MONTH));
    add(dayPanel, BorderLayout.CENTER);

    updateUI();
  }

  /**
   * Initializes the locale specific names for the days of the week.
   */
  protected void init() {
    Date date = calendar.getTime();
    calendar = Calendar.getInstance(locale);
    calendar.setTime(date);

    drawDayNames();
    drawDays();
  }

  /**
   * Draws the day names of the day columns
   */
  private void drawDayNames() {
    int firstDayOfWeek = calendar.getFirstDayOfWeek();
    DateFormatSymbols dateFormatSymbols = new DateFormatSymbols(locale);
    String[] dayNames = dateFormatSymbols.getShortWeekdays();

    int day = firstDayOfWeek;

    for (int i = 0; i < 7; i++) {
      days[i].setText(dayNames[day]);

      if (day == 1) {
        days[i].setForeground(sundayForeground);
      }
      else {
        days[i].setForeground(weekdayForeground);
      }

      if (day < 7) {
        day++;
      }
      else {
        day -= 6;
      }
    }
  }

  /**
   * Draws the day buttons
   */
  private void drawDays() {
    Calendar tmpCalendar = (Calendar) calendar.clone();
    tmpCalendar.set(Calendar.HOUR_OF_DAY, 0);
    tmpCalendar.set(Calendar.MINUTE, 0);
    tmpCalendar.set(Calendar.SECOND, 0);
    tmpCalendar.set(Calendar.MILLISECOND, 0);

    int firstDayOfWeek = tmpCalendar.getFirstDayOfWeek();
    tmpCalendar.set(Calendar.DAY_OF_MONTH, 1);

    int firstDay = tmpCalendar.get(Calendar.DAY_OF_WEEK) - firstDayOfWeek;

    if (firstDay < 0) {
      firstDay += 7;
    }

    // draw last days of previous month
    tmpCalendar.add(Calendar.MONTH, -1);
    int lastDayOfPreviousMonth = tmpCalendar.getActualMaximum(Calendar.DAY_OF_MONTH);
    tmpCalendar.add(Calendar.MONTH, 1);

    int i;
    for (i = 0; i < firstDay; i++) {
      days[i + 7].setEnabled(false);
      days[i + 7].setText(Integer.toString(lastDayOfPreviousMonth - firstDay + i + 1));
      days[i + 7].setVisible(true);
    }

    tmpCalendar.add(Calendar.MONTH, 1);
    Date firstDayInNextMonth = tmpCalendar.getTime();
    tmpCalendar.add(Calendar.MONTH, -1);

    Date day = tmpCalendar.getTime();
    int n = 0;
    Color foregroundColor = getForeground();

    while (day.before(firstDayInNextMonth)) {
      days[i + n + 7].setText(Integer.toString(n + 1));
      days[i + n + 7].setVisible(true);

      if ((tmpCalendar.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR))
          && (tmpCalendar.get(Calendar.YEAR) == today.get(Calendar.YEAR))) {
        days[i + n + 7].setForeground(sundayForeground);
      }
      else {
        days[i + n + 7].setForeground(foregroundColor);
      }

      if ((n + 1) == this.day) {
        days[i + n + 7].setBackground(selectedColor);
        days[i + n + 7].setBorderPainted(true);
        selectedDay = days[i + n + 7];
      }
      else {
        days[i + n + 7].setBackground(transparentBackgroundColor);
        days[i + n + 7].setBorderPainted(false);
      }

      days[i + n + 7].setEnabled(true);

      n++;
      tmpCalendar.add(Calendar.DATE, 1);
      day = tmpCalendar.getTime();
    }

    // fill up the last row with the days from the next month
    int actualDays = n;
    while ((n + i) % 7 != 0) {
      days[i + n + 7].setText(Integer.toString(n + 1 - actualDays));
      days[i + n + 7].setEnabled(false);
      days[i + n + 7].setVisible(true);
      n++;
    }

    // and hide the last line if it has not been started
    for (int k = n + i + 7; k < 49; k++) {
      days[k].setVisible(false);
      days[k].setText("");
    }
  }

  @Override
  public Locale getLocale() {
    return locale;
  }

  @Override
  public void setLocale(Locale locale) {
    this.locale = locale;
    super.setLocale(locale);
    init();
  }

  /**
   * Set the selected day
   * 
   * @param newDay
   *          the day to select
   */
  public void setDay(int newDay) {
    if (newDay < 1) {
      newDay = 1;
    }
    Calendar tmpCalendar = (Calendar) calendar.clone();
    tmpCalendar.set(Calendar.DAY_OF_MONTH, 1);
    tmpCalendar.add(Calendar.MONTH, 1);
    tmpCalendar.add(Calendar.DATE, -1);

    int maxDaysInMonth = tmpCalendar.get(Calendar.DATE);

    if (newDay > maxDaysInMonth) {
      newDay = maxDaysInMonth;
    }

    day = newDay;

    if (selectedDay != null) {
      selectedDay.setBackground(transparentBackgroundColor);
      selectedDay.setBorderPainted(false);
      selectedDay.repaint();
    }

    for (int i = 7; i < 49; i++) {
      if (days[i].getText().equals(Integer.toString(day))) {
        selectedDay = days[i];
        selectedDay.setBackground(selectedColor);
        selectedDay.setBorderPainted(true);
        break;
      }
    }

    firePropertyChange("day", 0, day);
  }

  /**
   * Returns the selected day.
   *
   * @return the day value
   */
  public int getDay() {
    return day;
  }

  /**
   * Sets a specific month. This is needed for correct graphical representation of the days.
   *
   * @param month
   *          the new month
   */
  void setMonth(int month) {
    calendar.set(Calendar.MONTH, month);
    int maxDays = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);

    if (day > maxDays) {
      day = maxDays;
    }

    drawDays();
  }

  /**
   * Sets a specific year. This is needed for correct graphical representation of the days.
   *
   * @param year
   *          the new year
   */
  public void setYear(int year) {
    calendar.set(Calendar.YEAR, year);
    drawDays();
  }

  /**
   * Sets a specific calendar. This is needed for correct graphical representation of the days.
   *
   * @param calendar
   *          the new calendar
   */
  public void setCalendar(Calendar calendar) {
    this.calendar = calendar;
    drawDays();
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    JButton button = (JButton) e.getSource();
    String buttonText = button.getText();
    int day = Integer.parseInt(buttonText);
    setDay(day);
  }

  private class DecoratorButton extends JButton {
    private static final long serialVersionUID = -5306477668406547496L;

    DecoratorButton() {
      setBackground(decorationBackgroundColor);
      setBorderPainted(false);
      setFocusable(false);
    }

    @Override
    public void addMouseListener(MouseListener l) {
    }
  }
}
