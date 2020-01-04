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
package org.tinymediamanager.core;

import java.util.List;

/**
 * The interface ITmmModule. For use as a connector to a tmm module
 * 
 * @author Manuel Laggner
 */
public interface ITmmModule {

  String getModuleTitle();

  boolean isEnabled();

  void startUp() throws Exception;

  void shutDown() throws Exception;

  void initializeDatabase() throws Exception;

  void saveSettings() throws Exception;

  List<String> getStartupMessages();
}
