/*
 * Copyright 2012 - 2019 Manuel Laggner
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

import static org.tinymediamanager.ui.plaf.TmmIcons.EMPTY_IMAGE;
import static org.tinymediamanager.ui.plaf.TmmIcons.createFontAwesomeIcon;
import static org.tinymediamanager.ui.plaf.TmmIcons.createTextIcon;

import java.awt.Color;
import java.awt.Font;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.swing.ImageIcon;
import javax.swing.UIManager;

import org.tinymediamanager.Globals;

public class IconManager {
  public static final Font                 FONT_AWESOME                = Font.decode("Font Awesome 5 Pro Regular");
  private final static Map<URI, ImageIcon> ICON_CACHE                  = new HashMap<>();
  private final static int                 DEFAULT_FONT_SIZE           = Globals.settings.getFontSize();
  private final static Color               ICON_COLOR                  = UIManager.getColor("Focus.color");


  // toolbar icons
  public final static ImageIcon            TOOLBAR_ABOUT               = loadImage("icn_about.png");
  public final static ImageIcon            TOOLBAR_ABOUT_HOVER         = loadImage("icn_about_hover.png");
  public final static ImageIcon            TOOLBAR_ADD_MOVIE_SET       = loadImage("icn_add_collection.png");
  public final static ImageIcon            TOOLBAR_ADD_MOVIE_SET_HOVER = loadImage("icn_add_collection_hover.png");
  public final static ImageIcon            TOOLBAR_DONATE              = loadImage("icn_donate.png");
  public final static ImageIcon            TOOLBAR_DONATE_HOVER        = loadImage("icn_donate_hover.png");
  public final static ImageIcon            TOOLBAR_EDIT                = loadImage("icn_edit.png");
  public final static ImageIcon            TOOLBAR_EDIT_HOVER          = loadImage("icn_edit_hover.png");
  public final static ImageIcon            TOOLBAR_EXPORT              = loadImage("icn_export.png");
  public final static ImageIcon            TOOLBAR_EXPORT_HOVER        = loadImage("icn_export_hover.png");
  public final static ImageIcon            TOOLBAR_LOGO                = loadImage("icn_logo_toolbar.png");
  public final static ImageIcon            TOOLBAR_REFRESH             = loadImage("icn_refresh.png");
  public final static ImageIcon            TOOLBAR_REFRESH_HOVER       = loadImage("icn_refresh_hover.png");
  public final static ImageIcon            TOOLBAR_RENAME              = loadImage("icn_rename.png");
  public final static ImageIcon            TOOLBAR_RENAME_HOVER        = loadImage("icn_rename_hover.png");
  public final static ImageIcon            TOOLBAR_SEARCH              = loadImage("icn_search.png");
  public final static ImageIcon            TOOLBAR_SEARCH_HOVER        = loadImage("icn_search_hover.png");
  public final static ImageIcon            TOOLBAR_SETTINGS            = loadImage("icn_settings.png");
  public final static ImageIcon            TOOLBAR_SETTINGS_HOVER      = loadImage("icn_settings_hover.png");
  public final static ImageIcon            TOOLBAR_TOOLS               = loadImage("icn_tools.png");
  public final static ImageIcon            TOOLBAR_TOOLS_HOVER         = loadImage("icn_tools_hover.png");

  // packaged icons
  public final static ImageIcon            STAR_FILLED                 = loadImage("star-filled.png");
  public final static ImageIcon            STAR_EMPTY                  = loadImage("star-empty.png");
  public final static ImageIcon            UNWATCHED                   = loadImage("unwatched.png");

  // font awesome icons for actions in the popup menu
  public final static ImageIcon            ADD                         = createFontAwesomeIcon('\uF067', ICON_COLOR);
  public final static ImageIcon            BUG                         = createFontAwesomeIcon('\uF188', ICON_COLOR);
  public final static ImageIcon            DELETE                      = createFontAwesomeIcon('\uF00D', ICON_COLOR);
  public final static ImageIcon            DELETE_FOREVER              = createFontAwesomeIcon('\uF2ED', ICON_COLOR);
  public final static ImageIcon            DOWNLOAD                    = createFontAwesomeIcon('\uF019', ICON_COLOR);
  public final static ImageIcon            FEEDBACK                    = createFontAwesomeIcon('\uF0E0', ICON_COLOR);
  public final static ImageIcon            EDIT                        = createFontAwesomeIcon('\uF044', ICON_COLOR);
  public final static ImageIcon            EXPORT                      = createFontAwesomeIcon('\uF14D', ICON_COLOR);
  public final static ImageIcon            HINT                        = createFontAwesomeIcon('\uF05A', ICON_COLOR);
  public final static ImageIcon            IMAGE                       = createFontAwesomeIcon('\uF03E', ICON_COLOR);
  public final static ImageIcon            MEDIAINFO                   = createFontAwesomeIcon('\uF129', ICON_COLOR);
  public final static ImageIcon            PLAY                        = createFontAwesomeIcon('\uF04B', ICON_COLOR);
  public final static ImageIcon            REFRESH                     = createFontAwesomeIcon('\uF01E', ICON_COLOR);
  public final static ImageIcon            REMOVE                      = createFontAwesomeIcon('\uF068', ICON_COLOR);
  public final static ImageIcon            SEARCH                      = createFontAwesomeIcon('\uF002', ICON_COLOR);
  public final static ImageIcon            SUBTITLE                    = createFontAwesomeIcon('\uF086', ICON_COLOR);
  public final static ImageIcon            SYNC                        = createFontAwesomeIcon('\uF021', ICON_COLOR);

  // font awesome icons for the table/tree
  public final static ImageIcon            TABLE_OK                    = createFontAwesomeIcon('\uF00C', new Color(31, 187, 0));
  public final static ImageIcon            TABLE_PROBLEM               = createFontAwesomeIcon('\uF071', new Color(204, 120, 50));
  public final static ImageIcon            TABLE_NOT_OK                = createFontAwesomeIcon('\uF00D', new Color(204, 2, 2));

  // font awesome icons normal
  public final static ImageIcon            CANCEL                      = createFontAwesomeIcon('\uF057');
  public final static ImageIcon            CARET_UP                    = createFontAwesomeIcon('\uF0D8');
  public final static ImageIcon            CARET_DOWN                  = createFontAwesomeIcon('\uF0D7');
  public final static ImageIcon            CLEAR_GREY                  = createFontAwesomeIcon('\uF057');
  public final static ImageIcon            COLLAPSED                   = createFontAwesomeIcon('\uF329');
  public final static ImageIcon            CONFIGURE                   = createFontAwesomeIcon('\uF0AD');
  public final static ImageIcon            ERROR                       = createFontAwesomeIcon('\uF057');
  public final static ImageIcon            EXPANDED                    = createFontAwesomeIcon('\uF32B');
  public final static ImageIcon            WARN                        = createFontAwesomeIcon('\uF071');
  public final static ImageIcon            WARN_INTENSIFIED            = createFontAwesomeIcon('\uF071', Color.RED);
  public final static ImageIcon            INFO                        = createFontAwesomeIcon('\uF05A');
  public final static ImageIcon            FILTER_ACTIVE               = createFontAwesomeIcon('\uF672', new Color(255, 119, 0));
  public final static ImageIcon            NEW                         = createTextIcon("new", DEFAULT_FONT_SIZE, new Color(31, 187, 0));
  public final static ImageIcon            SEARCH_GREY                 = createFontAwesomeIcon('\uF002');
  public final static ImageIcon            STOP                        = createFontAwesomeIcon('\uF28D');
  public final static ImageIcon            PLAY_LARGE                  = createFontAwesomeIcon('\uF144', 28);

  // font awesome icons light (button usage)
  public final static ImageIcon            ADD_INV                     = createFontAwesomeIcon('\uF067', UIManager.getColor("Button.foreground"));
  public final static ImageIcon            ARROW_UP_INV                = createFontAwesomeIcon('\uF077', UIManager.getColor("Button.foreground"));
  public final static ImageIcon            ARROW_DOWN_INV              = createFontAwesomeIcon('\uF078', UIManager.getColor("Button.foreground"));
  public final static ImageIcon            APPLY_INV                   = createFontAwesomeIcon('\uF058', UIManager.getColor("Button.foreground"));
  public final static ImageIcon            BACK_INV                    = createFontAwesomeIcon('\uF137', UIManager.getColor("Button.foreground"));
  public final static ImageIcon            CANCEL_INV                  = createFontAwesomeIcon('\uF057', UIManager.getColor("Button.foreground"));
  public final static ImageIcon            CHECK_ALL                   = createFontAwesomeIcon('\uF14A', UIManager.getColor("Button.foreground"));
  public final static ImageIcon            CLEAR_ALL                   = createFontAwesomeIcon('\uF0C8', UIManager.getColor("Button.foreground"));
  public final static ImageIcon            COPY_INV                    = createFontAwesomeIcon('\uF24D', UIManager.getColor("Button.foreground"));
  public final static ImageIcon            DATE_PICKER                 = createFontAwesomeIcon('\uF073', UIManager.getColor("Button.foreground"));
  public final static ImageIcon            DELETE_INV                  = createFontAwesomeIcon('\uF2ED', UIManager.getColor("Button.foreground"));
  public final static ImageIcon            FILTER_INV                  = createFontAwesomeIcon('\uF0B0', UIManager.getColor("Button.foreground"));
  public final static ImageIcon            FILE_OPEN_INV               = createFontAwesomeIcon('\uF07C', UIManager.getColor("Button.foreground"));
  public final static ImageIcon            IMAGE_INV                   = createFontAwesomeIcon('\uF03E', UIManager.getColor("Button.foreground"));
  public final static ImageIcon            PLAY_INV                    = createFontAwesomeIcon('\uF04B', UIManager.getColor("Button.foreground"));
  public final static ImageIcon            REMOVE_INV                  = createFontAwesomeIcon('\uF068', UIManager.getColor("Button.foreground"));
  public final static ImageIcon            SEARCH_INV                  = createFontAwesomeIcon('\uF002', UIManager.getColor("Button.foreground"));
  public final static ImageIcon            STOP_INV                    = createFontAwesomeIcon('\uF28D', UIManager.getColor("Button.foreground"));

  // font awesome icons - column headers
  public final static ImageIcon            AUDIO                       = createFontAwesomeIcon('\uF028', 16);
  public final static ImageIcon            CERTIFICATION               = createFontAwesomeIcon('\uF29A', 16);
  public final static ImageIcon            COUNT                       = createFontAwesomeIcon('\uF292', 16);
  public final static ImageIcon            DATE_ADDED                  = createFontAwesomeIcon('\uF271', 16);
  public final static ImageIcon            EDITION                     = createFontAwesomeIcon('\uF51F', 16);
  public final static ImageIcon            EDIT_HEADER                 = createFontAwesomeIcon('\uF044', 16);
  public final static ImageIcon            EPISODES                    = createTextIcon("E", 18);
  public final static ImageIcon            FILE_SIZE                   = createFontAwesomeIcon('\uF0C7', 16);
  public final static ImageIcon            IMAGES                      = createFontAwesomeIcon('\uF302', 16);
  public final static ImageIcon            IDCARD                      = createFontAwesomeIcon('\uF2C2', 16);
  public final static ImageIcon            NFO                         = createFontAwesomeIcon('\uF15C', 16);
  public final static ImageIcon            RATING                      = createFontAwesomeIcon('\uF005', 16);
  public final static ImageIcon            SEASONS                     = createTextIcon("S", 18);
  public final static ImageIcon            SOURCE                      = createFontAwesomeIcon('\uF601', 16);
  public final static ImageIcon            SUBTITLES                   = createFontAwesomeIcon('\uF086', 16);
  public final static ImageIcon            TRAILER                     = createFontAwesomeIcon('\uF008', 16);
  public final static ImageIcon            VIDEO_3D                    = createFontAwesomeIcon('\uF1B2', 16);
  public final static ImageIcon            VIDEO_FORMAT                = createFontAwesomeIcon('\uF320', 16);
  public final static ImageIcon            VOTES                       = createFontAwesomeIcon('\uF164', 16);
  public final static ImageIcon            WATCHED                     = createFontAwesomeIcon('\uF04B', 16);

  public static ImageIcon loadImage(String name) {
    URL file = IconManager.class.getResource("images/interface/" + name);
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
