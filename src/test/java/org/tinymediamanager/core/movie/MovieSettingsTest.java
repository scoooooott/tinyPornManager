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

package org.tinymediamanager.core.movie;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertTrue;

import java.nio.file.Paths;

import org.assertj.core.api.Assertions;
import org.junit.BeforeClass;
import org.junit.Test;
import org.tinymediamanager.BasicTest;
import org.tinymediamanager.core.Settings;
import org.tinymediamanager.core.Utils;

public class MovieSettingsTest extends BasicTest {

  @BeforeClass
  public static void beforeClass() {
    deleteSettingsFolder();
    Settings.getInstance(getSettingsFolder());
  }

  @Test
  public void testMovieSettings() {
    try {
      MovieSettings settings = MovieSettings.getInstance(getSettingsFolder());
      assertThat(settings).isNotNull();
      settings.setAsciiReplacement(true);

      // let the dirty flag set by the async propertychange listener
      Thread.sleep(100);

      settings.saveSettings();

      // cannot re-instantiate settings - need to check plain file
      String config = Utils.readFileToString(Paths.get(getSettingsFolder(), MovieSettings.getInstance().getConfigFilename()));
      assertTrue(config.contains("\"asciiReplacement\" : true"));
    }
    catch (Exception e) {
      Assertions.fail(e.getMessage());
    }
  }
}
