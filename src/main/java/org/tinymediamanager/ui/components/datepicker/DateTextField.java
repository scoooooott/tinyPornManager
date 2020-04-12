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

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import javax.swing.JFormattedTextField;
import javax.swing.UIManager;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.core.TmmDateFormat;

/**
 * The class DateTextField is a special JTextField for editing dates
 *
 * @author Manuel Laggner
 */
class DateTextField extends JFormattedTextField implements CaretListener, FocusListener, ActionListener {
  private static final long   serialVersionUID = -8901842591101625304L;
  private static final Logger LOGGER           = LoggerFactory.getLogger(DateTextField.class);

  private Date                date;
  private SimpleDateFormat    dateFormatter;
  private String              datePattern;
  private String              maskPattern;

  private Color               positiveColor;
  private Color               negativeColor;

  private int                 hours;
  private int                 minutes;
  private int                 seconds;
  private int                 millis;

  private Calendar            calendar;

  public DateTextField() {
    this(null);
  }

  public DateTextField(String datePattern) {
    setDateFormatString(datePattern);
    maskPattern = createMaskFromDatePattern(this.datePattern);

    setColumns(this.datePattern.length());

    setToolTipText(this.datePattern);
    addCaretListener(this);
    addFocusListener(this);
    addActionListener(this);

    positiveColor = new Color(0, 150, 0);
    negativeColor = Color.RED;

    calendar = Calendar.getInstance();
  }

  public Date getDate() {
    try {
      calendar.setTime(dateFormatter.parse(getText()));
      calendar.set(Calendar.HOUR_OF_DAY, hours);
      calendar.set(Calendar.MINUTE, minutes);
      calendar.set(Calendar.SECOND, seconds);
      calendar.set(Calendar.MILLISECOND, millis);
      date = calendar.getTime();
    }
    catch (ParseException e) {
      date = null;
    }
    return date;
  }

  public void setDate(Date date) {
    setDate(date, true);
  }

  private void setDate(Date date, boolean firePropertyChange) {
    Date oldDate = this.date;
    this.date = date;

    if (date == null) {
      setText("");
    }
    else {
      calendar.setTime(date);
      hours = calendar.get(Calendar.HOUR_OF_DAY);
      minutes = calendar.get(Calendar.MINUTE);
      seconds = calendar.get(Calendar.SECOND);
      millis = calendar.get(Calendar.MILLISECOND);

      String formattedDate = dateFormatter.format(date);
      try {
        setText(formattedDate);
      }
      catch (RuntimeException e) {
        LOGGER.warn("Could not set text: {}", e);
      }
    }
    setForeground(UIManager.getColor("FormattedTextField.foreground"));

    if (firePropertyChange) {
      firePropertyChange("date", oldDate, date);
    }
  }

  private void setDateFormatString(String dateFormatString) {
    try {
      dateFormatter = new SimpleDateFormat();
      dateFormatter.applyPattern(dateFormatString);
    }
    catch (RuntimeException e) {
      dateFormatter = (SimpleDateFormat) TmmDateFormat.MEDIUM_DATE_FORMAT;
      dateFormatter.setLenient(false);
    }
    this.datePattern = dateFormatter.toPattern();
    setToolTipText(this.datePattern);
    setDate(date, false);
  }

  /**
   * After any user input, the value of the text field is verified. Depending on being a valid date, the value is colored green or red.
   * 
   * @param event
   *          the caret event
   */
  @Override
  public void caretUpdate(CaretEvent event) {
    String text = getText().trim();
    String emptyMask = maskPattern.replace('#', ' ');

    if (text.length() == 0 || text.equals(emptyMask)) {
      setForeground(UIManager.getColor("FormattedTextField.foreground"));
      return;
    }

    try {
      // check valid date
      dateFormatter.parse(getText());
      setForeground(positiveColor);
    }
    catch (Exception e) {
      setForeground(negativeColor);
    }
  }

  @Override
  public void focusLost(FocusEvent focusEvent) {
    checkText();
  }

  private void checkText() {
    try {
      Date date = dateFormatter.parse(getText());
      setDate(date, true);
    }
    catch (Exception e) {
      // ignore
    }
  }

  @Override
  public void focusGained(FocusEvent e) {
  }

  private String createMaskFromDatePattern(String datePattern) {
    String symbols = "GyMdkHmsSEDFwWahKzZ";
    StringBuilder mask = new StringBuilder();
    for (int i = 0; i < datePattern.length(); i++) {
      char ch = datePattern.charAt(i);
      boolean symbolFound = false;
      for (int n = 0; n < symbols.length(); n++) {
        if (symbols.charAt(n) == ch) {
          mask.append("#");
          symbolFound = true;
          break;
        }
      }
      if (!symbolFound) {
        mask.append(ch);
      }
    }
    return mask.toString();
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    checkText();
  }

  @Override
  public void setEnabled(boolean enabled) {
    super.setEnabled(enabled);
    if (!enabled) {
      super.setBackground(UIManager.getColor("TextField.inactiveBackground"));
    }
  }
}
