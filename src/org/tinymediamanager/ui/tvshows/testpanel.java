/*
 * Copyright 2012 - 2013 Manuel Laggner
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
package org.tinymediamanager.ui.tvshows;

import javax.swing.JLabel;
import javax.swing.JPanel;

import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

/**
 * @author Manuel Laggner
 * 
 */
public class testpanel extends JPanel {
  public testpanel() {
    setLayout(new FormLayout(new ColumnSpec[] { ColumnSpec.decode("default:grow"), FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
        ColumnSpec.decode("20px"), FormFactory.LABEL_COMPONENT_GAP_COLSPEC, ColumnSpec.decode("20px"), }, new RowSpec[] {
        FormFactory.DEFAULT_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, }));

    JLabel lblNewLabel = new JLabel("New label");
    add(lblNewLabel, "1, 1");

    JLabel lblNewLabel_1 = new JLabel("New label");
    add(lblNewLabel_1, "3, 1, 1, 2");

    JLabel lblNewLabel_3 = new JLabel("New label");
    add(lblNewLabel_3, "5, 1, 1, 2");

    JLabel lblNewLabel_2 = new JLabel("New label");
    add(lblNewLabel_2, "1, 2");
  }

}
