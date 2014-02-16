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
package org.tinymediamanager.ui;

import javax.swing.Action;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;

/**
 * Interface ITmmUIModule to access UI Modules easily
 * 
 * @author Manuel Laggner
 */
public interface ITmmUIModule {

  public String getModuleId();

  public JPanel getTabPanel();

  public String getTabTitle();

  public JPanel getDetailPanel();

  public Action getSearchAction();

  public JPopupMenu getSearchMenu();

  public Action getEditAction();

  public JPopupMenu getEditMenu();

  public Action getUpdateAction();

  public JPopupMenu getUpdateMenu();

  public Action getExportAction();

  public JPopupMenu getExportMenu();

  public JPanel getSettingsPanel();
}
