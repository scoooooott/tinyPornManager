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

import org.japura.gui.CheckComboBox;
import org.japura.gui.renderer.CheckListRenderer;
import org.tinymediamanager.ui.SmallCheckBoxUI;
import org.tinymediamanager.ui.TmmFontHelper;

/**
 * The class SmallCheckListRenderer is used to display a small CheckComboBox in the filter panels
 * 
 * @author Manuel Laggner
 */
public class SmallCheckComboBox extends CheckComboBox {
  private static final long serialVersionUID = 7854706583856410194L;

  public SmallCheckComboBox() {
    super();
    setRenderer(new SmallCheckListRenderer());
    TmmFontHelper.changeFont(getComboBox(), 0.916);
  }

  private class SmallCheckListRenderer extends CheckListRenderer {
    private static final long serialVersionUID = -7316047303991759659L;

    public SmallCheckListRenderer() {
      super();
      TmmFontHelper.changeFont(this, 0.916);
      setUI(new SmallCheckBoxUI());
    }
  }
}
