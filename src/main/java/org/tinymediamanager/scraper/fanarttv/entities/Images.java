/*
 * Copyright 2012 - 2016 Manuel Laggner
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
package org.tinymediamanager.scraper.fanarttv.entities;

import java.util.ArrayList;
import java.util.List;

/**
 * The entity Images. Holds all images for one movie/tv show
 */
public class Images {
  public String      name;
  public String      tmdbId;
  public String      imdbId;

  // movies
  public List<Image> hdmovielogo     = new ArrayList<Image>();    //hd clearlogo in HP
  public List<Image> moviedisc       = new ArrayList<Image>();
  public List<Image> movielogo       = new ArrayList<Image>();
  public List<Image> movieposter     = new ArrayList<Image>();
  public List<Image> hdmovieclearart = new ArrayList<Image>();
  public List<Image> movieart        = new ArrayList<Image>();
  public List<Image> moviebackground = new ArrayList<Image>();
  public List<Image> moviebanner     = new ArrayList<Image>();
  public List<Image> moviethumb      = new ArrayList<Image>();

  // tv shows
  public List<Image> clearlogo       = new ArrayList<Image>();
  public List<Image> hdtvlogo        = new ArrayList<Image>(); // hd clearlogo in HP
  public List<Image> clearart        = new ArrayList<Image>();
  public List<Image> showbackground  = new ArrayList<Image>();
  public List<Image> tvthumb         = new ArrayList<Image>();
  public List<Image> hdclearart      = new ArrayList<Image>();
  public List<Image> tvbanner        = new ArrayList<Image>();
  public List<Image> tvposter        = new ArrayList<Image>();
  public List<Image> seasonposter    = new ArrayList<Image>();
  public List<Image> seasonthumb     = new ArrayList<Image>();
  public List<Image> seasonbanner    = new ArrayList<Image>();
  public List<Image> characterart    = new ArrayList<Image>();
}
