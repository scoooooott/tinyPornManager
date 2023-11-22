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
package xyz.ifnotnull.tmm.scraper.pornhub;

import org.tinymediamanager.scraper.interfaces.IMediaProvider;
import org.tinymediamanager.scraper.spi.IAddonProvider;

import java.util.ArrayList;
import java.util.List;

public class PornhubAddonProvider implements IAddonProvider {

  @Override
  public List<Class<? extends IMediaProvider>> getAddonClasses() {
    List<Class<? extends IMediaProvider>> addons = new ArrayList<>();

    addons.add(PornhubMovieMetadataProvider.class);
    addons.add(PornhubMovieArtworkProvider.class);
    return addons;
  }
}
