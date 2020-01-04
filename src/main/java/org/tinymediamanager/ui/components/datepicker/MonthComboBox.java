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

import java.text.DateFormatSymbols;
import java.util.Calendar;
import java.util.Locale;

import javax.swing.JComboBox;

/**
 * The class MonthComboBox is used to display a ComboBox for choosing a month
 * 
 * @author Manuel Laggner
 */
class MonthComboBox extends JComboBox<String> {
  private static final long serialVersionUID = 5078860132234256877L;

  MonthComboBox() {
    DateFormatSymbols dateFormatSymbols = new DateFormatSymbols(Locale.getDefault());
    String[] monthNames = dateFormatSymbols.getMonths();

    for (int i = 0; i < 12; i++) {
      addItem(monthNames[i]);
    }

    setSelectedIndex(Calendar.getInstance().get(Calendar.MONTH));
  }
}
