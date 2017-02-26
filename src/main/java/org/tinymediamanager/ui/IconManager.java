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
package org.tinymediamanager.ui;

import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.swing.ImageIcon;

public class IconManager {
  private final static Map<URI, ImageIcon> ICON_CACHE        = new HashMap<>();
  public final static ImageIcon            EMPTY_IMAGE       = new ImageIcon(IconManager.class.getResource("images/empty.png"));

  public final static ImageIcon            APPLY             = loadImage("apply.png");
  public final static ImageIcon            ARROW_UP          = loadImage("arrow-up.png");
  public final static ImageIcon            ARROW_DOWN        = loadImage("arrow-down.png");
  public final static ImageIcon            BUG               = loadImage("bug.png");
  public final static ImageIcon            CANCEL            = loadImage("cancel.png");
  public final static ImageIcon            CHECK_ALL         = loadImage("check-all.png");
  public final static ImageIcon            CHECKMARK         = loadImage("checkmark.png");
  public final static ImageIcon            CLAPBOARD         = loadImage("clapboard.png");
  public final static ImageIcon            COPY              = loadImage("copy.png");
  public final static ImageIcon            CROSS             = loadImage("cross.png");
  public final static ImageIcon            DATE_PICKER       = loadImage("datepicker.png");
  public final static ImageIcon            DELETE            = loadImage("delete.png");
  public final static ImageIcon            DOWNLOAD          = loadImage("download.png");
  public final static ImageIcon            DOWNLOAD_DISABLED = loadImage("download-disabled.png");
  public final static ImageIcon            EDIT              = loadImage("edit.png");
  public final static ImageIcon            ERROR             = loadImage("error.png");
  public final static ImageIcon            EXIT              = loadImage("exit.png");
  public final static ImageIcon            EXPORT            = loadImage("export.png");
  public final static ImageIcon            FEEDBACK          = loadImage("feedback.png");
  public final static ImageIcon            FILE_OPEN         = loadImage("file-open.png");
  public final static ImageIcon            FILTER            = loadImage("filter.png");
  public final static ImageIcon            FILTER_ACTIVE     = loadImage("filter_active.png");
  public final static ImageIcon            HINT              = loadImage("hint.png");
  public final static ImageIcon            IMAGE             = loadImage("image.png");
  public final static ImageIcon            INFO              = loadImage("info.png");
  public final static ImageIcon            LIST_ADD          = loadImage("list-add.png");
  public final static ImageIcon            LIST_REMOVE       = loadImage("list-remove.png");
  public final static ImageIcon            LOADING           = loadImage("loading.gif");
  public final static ImageIcon            NEW               = loadImage("new.png");
  public final static ImageIcon            PLAY_SMALL        = loadImage("play-small.png");
  public final static ImageIcon            PLAY              = loadImage("play.png");
  public final static ImageIcon            PROCESS_STOP      = loadImage("process-stop.png");
  public final static ImageIcon            REFRESH           = loadImage("refresh.png");
  public final static ImageIcon            REGISTER          = loadImage("register.png");
  public final static ImageIcon            SEARCH            = loadImage("search.png");
  public final static ImageIcon            SETTINGS_SMALL    = loadImage("settings-small.png");
  public final static ImageIcon            STAR_FILLED       = loadImage("star-filled.png");
  public final static ImageIcon            STAR_EMPTY        = loadImage("star-empty.png");
  public final static ImageIcon            SUBTITLE          = loadImage("subtitle.png");
  public final static ImageIcon            SYNC              = loadImage("sync.png");
  public final static ImageIcon            UNCHECK_ALL       = loadImage("uncheck-all.png");
  public final static ImageIcon            UNWATCHED         = loadImage("unwatched.png");

  private static ImageIcon loadImage(String name) {
    URL file = IconManager.class.getResource("/images/ui/" + name);
    if (file != null) {
      return new ImageIcon(file);
    }

    return EMPTY_IMAGE;
  }

  /**
   * loads an image from the given url
   * 
   * @param url
   *          the url pointing to the image
   * @return the image or an empty image (1x1 px transparent) if it is not loadable
   */
  public static ImageIcon loadImageFromURL(URL url) {
    URI uri = null;

    if (url == null) {
      return EMPTY_IMAGE;
    }

    try {
      uri = url.toURI();
      if (uri == null) {
        return EMPTY_IMAGE;
      }
    }
    catch (Exception e) {
      return EMPTY_IMAGE;
    }

    // read cache
    ImageIcon icon = ICON_CACHE.get(uri);

    if (icon == null) {
      try {
        icon = new ImageIcon(url);
      }
      catch (Exception ignored) {
      }
      finally {
        if (icon == null) {
          icon = EMPTY_IMAGE;
        }
      }
      ICON_CACHE.put(uri, icon);
    }

    return icon;
  }
}
