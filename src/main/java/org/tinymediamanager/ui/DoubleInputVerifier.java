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

package org.tinymediamanager.ui;

import java.awt.Color;

import javax.swing.InputVerifier;
import javax.swing.JComponent;
import javax.swing.JTextField;

/**
 * the class IntegerInputVerifier validates whether the given component contains a parseable double or not
 * 
 * @author Manuel Laggner
 */
public class DoubleInputVerifier extends InputVerifier {

  private final Color jTextFieldDefaultColor;

  public DoubleInputVerifier() {
    JTextField tf = new JTextField();
    jTextFieldDefaultColor = tf.getBackground();
  }

  @Override
  public boolean verify(JComponent input) {
    try {
      if (input instanceof JTextField) {
        Double.parseDouble(((JTextField) input).getText());
        input.setBackground(jTextFieldDefaultColor);
        return true;
      }
    }
    catch (Exception ignored) {
    }

    input.setBackground(Color.PINK);
    return false;
  }
}
