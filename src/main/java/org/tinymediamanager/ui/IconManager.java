/*
 * Copyright 2012 - 2015 Manuel Laggner
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
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.swing.ImageIcon;
import javax.swing.UIManager;

public class IconManager {
  private final static Map<URI, ImageIcon> ICON_CACHE             = new HashMap<>();
  public final static ImageIcon            EMPTY_IMAGE            = new ImageIcon(IconManager.class.getResource("images/empty.png"));

  // toolbar icons
  public final static ImageIcon            TOOLBAR_ABOUT          = loadImage("icn_about.png");
  public final static ImageIcon            TOOLBAR_ABOUT_HOVER    = loadImage("icn_about_hover.png");
  public final static ImageIcon            TOOLBAR_DONATE         = loadImage("icn_donate.png");
  public final static ImageIcon            TOOLBAR_DONATE_HOVER   = loadImage("icn_donate_hover.png");
  public final static ImageIcon            TOOLBAR_EDIT           = loadImage("icn_edit.png");
  public final static ImageIcon            TOOLBAR_EDIT_HOVER     = loadImage("icn_edit_hover.png");
  public final static ImageIcon            TOOLBAR_EXPORT         = loadImage("icn_export.png");
  public final static ImageIcon            TOOLBAR_EXPORT_HOVER   = loadImage("icn_export_hover.png");
  public final static ImageIcon            TOOLBAR_LOGO           = loadImage("icn_logo_toolbar.png");
  public final static ImageIcon            TOOLBAR_REFRESH        = loadImage("icn_refresh.png");
  public final static ImageIcon            TOOLBAR_REFRESH_HOVER  = loadImage("icn_refresh_hover.png");
  public final static ImageIcon            TOOLBAR_SEARCH         = loadImage("icn_search.png");
  public final static ImageIcon            TOOLBAR_SEARCH_HOVER   = loadImage("icn_search_hover.png");
  public final static ImageIcon            TOOLBAR_SETTINGS       = loadImage("icn_settings.png");
  public final static ImageIcon            TOOLBAR_SETTINGS_HOVER = loadImage("icn_settings_hover.png");
  public final static ImageIcon            TOOLBAR_TOOLS          = loadImage("icn_tools.png");
  public final static ImageIcon            TOOLBAR_TOOLS_HOVER    = loadImage("icn_tools_hover.png");

  // packaged icons
  public final static ImageIcon            CHECK_ALL              = loadImage("check-all.png");
  public final static ImageIcon            CHECKMARK              = loadImage("checkmark.png");
  public final static ImageIcon            CLAPBOARD              = loadImage("clapboard.png");
  public final static ImageIcon            COPY                   = loadImage("copy.png");
  public final static ImageIcon            CROSS                  = loadImage("cross.png");
  public final static ImageIcon            DATE_PICKER            = loadImage("datepicker.png");
  public final static ImageIcon            DELETE                 = loadImage("delete.png");
  public final static ImageIcon            DOT_AVAILABLE          = loadImage("dot_available.png");
  public final static ImageIcon            DOT_UNAVAILABLE        = loadImage("dot_unavailable.png");
  public final static ImageIcon            DOWNLOAD               = loadImage("download.png");
  public final static ImageIcon            DOWNLOAD_DISABLED      = loadImage("download-disabled.png");
  public final static ImageIcon            ERROR                  = loadImage("error.png");
  public final static ImageIcon            EXPORT                 = loadImage("export.png");
  public final static ImageIcon            FILE_OPEN              = loadImage("file-open.png");
  public final static ImageIcon            FILTER                 = loadImage("filter.png");
  public final static ImageIcon            HINT                   = loadImage("hint.png");
  public final static ImageIcon            IMAGE                  = loadImage("image.png");
  public final static ImageIcon            INFO                   = loadImage("info.png");

  public final static ImageIcon            NEW                    = loadImage("new.png");
  public final static ImageIcon            PLAY_SMALL             = loadImage("play-small.png");
  public final static ImageIcon            PLAY                   = loadImage("play.png");
  public final static ImageIcon            PROCESS_STOP           = loadImage("process-stop.png");
  public final static ImageIcon            REFRESH                = loadImage("refresh.png");
  public final static ImageIcon            REGISTER               = loadImage("register.png");
  public final static ImageIcon            SEARCH                 = loadImage("icn_searchfield.png");
  public final static ImageIcon            SETTINGS_SMALL         = loadImage("settings-small.png");
  public final static ImageIcon            STAR_FILLED            = loadImage("star-filled.png");
  public final static ImageIcon            STAR_EMPTY             = loadImage("star-empty.png");
  public final static ImageIcon            SUBTITLE               = loadImage("subtitle.png");
  public final static ImageIcon            SYNC                   = loadImage("sync.png");
  public final static ImageIcon            UNCHECK_ALL            = loadImage("uncheck-all.png");
  public final static ImageIcon            UNWATCHED              = loadImage("unwatched.png");

  // Material icons light (button usage)
  public final static ImageIcon            ADD_INV                = createMaterialFontIcon('\uE145', 16, true);
  public final static ImageIcon            ARROW_UP_INV           = createMaterialFontIcon('\uE5D8', 16, true);
  public final static ImageIcon            ARROW_DOWN_INV         = createMaterialFontIcon('\uE5DB', 16, true);
  public final static ImageIcon            APPLY_INV              = createMaterialFontIcon('\uE876', 16, true);
  public final static ImageIcon            CANCEL_INV             = createMaterialFontIcon('\uE14C', 16, true);
  public final static ImageIcon            REMOVE_INV             = createMaterialFontIcon('\uE15B', 16, true);

  // Material icons normal
  public final static ImageIcon            ADD                    = createMaterialFontIcon('\uE145', 16);
  public final static ImageIcon            BUG                    = createMaterialFontIcon('\uE868', 16);
  public final static ImageIcon            CANCEL                 = createMaterialFontIcon('\uE14C', 16);
  public final static ImageIcon            CONFIGURE              = createMaterialFontIcon('\uE869', 16);
  public final static ImageIcon            FEEDBACK               = createMaterialFontIcon('\uE158', 16);
  public final static ImageIcon            EDIT                   = createMaterialFontIcon('\uE150', 16);
  public final static ImageIcon            REMOVE                 = createMaterialFontIcon('\uE15B', 16);

  // Material icons - column headers
  public final static ImageIcon            DATE_ADDED             = createMaterialFontIcon('\uE02E', 24);
  public final static ImageIcon            EPISODES               = createFontIcon('E', 20);
  public final static ImageIcon            IMAGES                 = createMaterialFontIcon('\uE410', 24);
  public final static ImageIcon            MOVIE                  = createMaterialFontIcon('\uE54D', 24);
  public final static ImageIcon            NFO                    = createMaterialFontIcon('\uE873', 24);
  public final static ImageIcon            RATING                 = createMaterialFontIcon('\uE838', 24);
  public final static ImageIcon            SEASONS                = createFontIcon('S', 20);
  public final static ImageIcon            SUBTITLES              = createMaterialFontIcon('\uE24C', 24);
  public final static ImageIcon            TRAILER                = createMaterialFontIcon('\uE02C', 24);
  public final static ImageIcon            WATCHED                = createMaterialFontIcon('\uE037', 24);

  // OBSOLETE ICONS (will be dropped when legacy code is removed)
  public final static ImageIcon            EXIT                   = loadImage("exit.png");

  private static ImageIcon loadImage(String name) {
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

  /**
   * create a image off the material icon font
   *
   * @param iconId
   *          the icon id
   * @param size
   *          the desired font size
   * @return
   */
  public static ImageIcon createMaterialFontIcon(char iconId, int size) {
    return createMaterialFontIcon(iconId, size, UIManager.getColor("Label.foreground"));
  }

  /**
   * create a image off the material icon font
   *
   * @param iconId
   *          the icon id
   * @param size
   *          the desired font size
   * @param inverse
   *          inverse color
   * @return
   */
  public static ImageIcon createMaterialFontIcon(char iconId, int size, boolean inverse) {
    if (inverse) {
      return createMaterialFontIcon(iconId, size, UIManager.getColor("Label.background"));
    }
    else {
      return createMaterialFontIcon(iconId, size, UIManager.getColor("Label.foreground"));
    }
  }

  /**
   * create a image off the material icon font
   *
   * @param iconId
   *          the icon id
   * @param size
   *          the desired font size
   * @param color
   *          the color to create the icon in
   * @return
   */
  public static ImageIcon createMaterialFontIcon(char iconId, int size, Color color) {
    Font materialFont = new Font("Material Icons", Font.PLAIN, size);
    return createFontIcon(materialFont, iconId, size, color);
  }

  private static ImageIcon createFontIcon(char iconId, int size) {
    return createFontIcon(iconId, size, UIManager.getColor("Label.foreground"));
  }

  private static ImageIcon createFontIcon(char iconId, int size, Color color) {
    Font defaultfont = (Font) UIManager.get("Label.font");
    if (defaultfont == null) {
      return null;
    }
    Font font = defaultfont.deriveFont(Font.BOLD, (float) size);
    return createFontIcon(font, iconId, size, color);
  }

  private static ImageIcon createFontIcon(Font font, char iconId, int size, Color color) {
    try {
      // calculate icon size
      BufferedImage tmp = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
      Graphics2D g2 = GraphicsEnvironment.getLocalGraphicsEnvironment().createGraphics(tmp);
      g2.setFont(font);
      int iconWidth = g2.getFontMetrics().charWidth(iconId);
      int iconHeight = g2.getFontMetrics().getHeight();
      g2.dispose();

      // and draw it
      BufferedImage buffer = new BufferedImage(iconWidth, iconHeight, BufferedImage.TYPE_INT_ARGB);
      g2 = (Graphics2D) buffer.getGraphics();
      g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

      g2.setFont(font);
      g2.setColor(color);

      int sy = size;
      g2.drawString(String.valueOf(iconId), 0, sy);
      g2.dispose();

      g2.drawImage(buffer, 0, 0, null);
      g2.dispose();
      return new ImageIcon(buffer);
    }
    catch (Exception ignored) {
    }

    return EMPTY_IMAGE;
  }
}
