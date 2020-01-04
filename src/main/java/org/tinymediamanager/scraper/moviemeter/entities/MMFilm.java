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
package org.tinymediamanager.scraper.moviemeter.entities;

import java.util.Collections;
import java.util.List;

public class MMFilm {
  public int              id                = 0;
  public String           url               = "";
  public int              year              = 0;
  public String           imdb              = "";
  public String           title             = "";
  public String           display_title     = "";
  public String           alternative_title = "";
  public String           plot              = "";
  public int              duration          = 0;
  public int              votes_count       = 0;
  public double           average           = 0;
  public MMPoster         posters           = new MMPoster();
  public List<String>     countries         = Collections.emptyList();
  public List<String>     genres            = Collections.emptyList();
  public List<MMActor>    actors            = Collections.emptyList();
  public List<MMDirector> directors         = Collections.emptyList();
}
