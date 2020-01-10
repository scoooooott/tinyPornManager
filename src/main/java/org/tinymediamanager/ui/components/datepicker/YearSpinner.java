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

import java.util.Calendar;

import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

/**
 * The class YearSpinner is used to display a spinner for choosing a year
 *
 * @author Manuel Laggner
 */
public class YearSpinner extends JSpinner {
  private static final long serialVersionUID = 2648810220491090064L;

  public YearSpinner() {
    Calendar calendar = Calendar.getInstance();
    setModel(new SpinnerNumberModel(calendar.get(Calendar.YEAR), 0, calendar.getMaximum(Calendar.YEAR), 1));
    setEditor(new JSpinner.NumberEditor(this, "#"));
  }
}
