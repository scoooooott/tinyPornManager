/*
 * Copyright 2012 - 2018 Manuel Laggner
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
import java.awt.Toolkit;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.swing.ImageIcon;
import javax.swing.UIManager;

import org.tinymediamanager.Globals;

public class IconManager {
  private final static Map<URI, ImageIcon> ICON_CACHE                  = new HashMap<>();
  private final static int                 DEFAULT_FONT_SIZE           = Globals.settings.getFontSize();
  private final static Color               ICON_COLOR                  = new Color(45, 121, 162);

  public final static ImageIcon            EMPTY_IMAGE                 = new ImageIcon(IconManager.class.getResource("images/empty.png"));

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
  public final static ImageIcon            DOT_AVAILABLE               = loadImage("dot_available.png");
  public final static ImageIcon            DOT_UNAVAILABLE             = loadImage("dot_unavailable.png");
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

  // font awesome icons normal
  public final static ImageIcon            CONFIGURE                   = createFontAwesomeIcon('\uF0AD');
  public final static ImageIcon            ERROR                       = createFontAwesomeIcon('\uF071');
  public final static ImageIcon            FILTER_ACTIVE               = createFontAwesomeIcon('\uF0B0', Color.RED);
  public final static ImageIcon            NEW                         = createTextIcon("new", 14);
  public final static ImageIcon            SEARCH_GREY                 = createFontAwesomeIcon('\uF002');
  public final static ImageIcon            PLAY_LARGE                  = createFontAwesomeIcon('\uF144', 28);

  // font awesome icons light (button usage)
  public final static ImageIcon            ADD_INV                     = createFontAwesomeIcon('\uF067', true);
  public final static ImageIcon            ARROW_UP_INV                = createFontAwesomeIcon('\uF077', true);
  public final static ImageIcon            ARROW_DOWN_INV              = createFontAwesomeIcon('\uF078', true);
  public final static ImageIcon            APPLY_INV                   = createFontAwesomeIcon('\uF00C', true);
  public final static ImageIcon            BACK_INV                    = createFontAwesomeIcon('\uF053', true);
  public final static ImageIcon            CANCEL_INV                  = createFontAwesomeIcon('\uF00D', true);
  public final static ImageIcon            CHECK_ALL                   = createFontAwesomeIcon('\uF14A', true);
  public final static ImageIcon            CLEAR_ALL                   = createFontAwesomeIcon('\uF0C8', true);
  public final static ImageIcon            COPY_INV                    = createFontAwesomeIcon('\uF24D', true);
  public final static ImageIcon            DATE_PICKER                 = createFontAwesomeIcon('\uF073', true);
  public final static ImageIcon            FILTER_INV                  = createFontAwesomeIcon('\uF0B0', true);
  public final static ImageIcon            FILE_OPEN_INV               = createFontAwesomeIcon('\uF07C', true);
  public final static ImageIcon            IMAGE_INV                   = createFontAwesomeIcon('\uF03E', true);
  public final static ImageIcon            PLAY_INV                    = createFontAwesomeIcon('\uF04B', true);
  public final static ImageIcon            REMOVE_INV                  = createFontAwesomeIcon('\uF068', true);
  public final static ImageIcon            SEARCH_INV                  = createFontAwesomeIcon('\uF002', true);
  public final static ImageIcon            STOP_INV                    = createFontAwesomeIcon('\uF04D', true);

  // font awesome icons - column headers
  public final static ImageIcon            COUNT                       = createFontAwesomeIcon('\uF292', 16);
  public final static ImageIcon            DATE_ADDED                  = createFontAwesomeIcon('\uF271', 16);
  public final static ImageIcon            EPISODES                    = createTextIcon("E", 18);
  public final static ImageIcon            FILE_SIZE                   = createFontAwesomeIcon('\uF0C7', 16);
  public final static ImageIcon            IMAGES                      = createFontAwesomeIcon('\uF302', 16);
  public final static ImageIcon            IDCARD                      = createFontAwesomeIcon('\uF2C2', 16);
  public final static ImageIcon            NFO                         = createFontAwesomeIcon('\uF15C', 16);
  public final static ImageIcon            RATING                      = createFontAwesomeIcon('\uF005', 16);
  public final static ImageIcon            SEASONS                     = createTextIcon("S", 18);
  public final static ImageIcon            SUBTITLES                   = createFontAwesomeIcon('\uF086', 16);
  public final static ImageIcon            TRAILER                     = createFontAwesomeIcon('\uF008', 16);
  public final static ImageIcon            VIDEO_FORMAT                = createFontAwesomeIcon('\uF320', 16);
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

  /**
   * create a image off the font awesome icon font in the default size 14pt for 12pt base font size.
   *
   * @param iconId
   *          the icon id
   * @return the generated icon
   */
  public static ImageIcon createFontAwesomeIcon(char iconId) {
    return createFontAwesomeIcon(iconId, calculateFontSize(14 / 12.0f), UIManager.getColor("Label.foreground"));
  }

  private static int calculateFontSize(float scaleFactor) {
    return (int) Math.floor(DEFAULT_FONT_SIZE * scaleFactor);
    // return Math.round(DEFAULT_FONT_SIZE * scaleFactor);
  }

  /**
   * create a image off the font awesome icon font in given size (scaling to the base font size of 12pt applied!)
   *
   * @param iconId
   *          the icon id
   * @param size
   *          the desired font size
   * @return the generated icon
   */
  public static ImageIcon createFontAwesomeIcon(char iconId, int size) {
    return createFontAwesomeIcon(iconId, calculateFontSize(size / 12.0f), UIManager.getColor("Label.foreground"));
  }

  /**
   * create a image off the awesome icon font with the given scaling factor
   *
   * @param iconId
   *          the icon id
   * @param scaleFactor
   *          the scale factor to apply
   * @return the generated icon
   */
  public static ImageIcon createFontAwesomeIcon(char iconId, float scaleFactor) {
    return createFontAwesomeIcon(iconId, calculateFontSize(scaleFactor), UIManager.getColor("Label.foreground"));
  }

  /**
   * create a image off the awesome icon font in given size (scaling to the base font size of 12pt applied!)
   *
   * @param iconId
   *          the icon id
   * @param size
   *          the desired font size
   * @param inverse
   *          inverse color
   * @return the generated icon
   */
  public static ImageIcon createFontAwesomeIcon(char iconId, int size, boolean inverse) {
    if (inverse) {
      return createFontAwesomeIcon(iconId, calculateFontSize(size / 12.0f), UIManager.getColor("Label.background"));
    }
    else {
      return createFontAwesomeIcon(iconId, calculateFontSize(size / 12.0f), UIManager.getColor("Label.foreground"));
    }
  }

  /**
   * create a image off the awesome icon font in the default size 14pt for 12pt base font size.
   *
   * @param iconId
   *          the icon id
   * @param inverse
   *          inverse color
   * @return the generated icon
   */
  public static ImageIcon createFontAwesomeIcon(char iconId, boolean inverse) {
    return createFontAwesomeIcon(iconId, 14, inverse);
  }

  /**
   * create a image off the awesome icon font size 14pt for 12pt base font size.
   *
   * @param iconId
   *          the icon id
   * @param color
   *          the color to create the icon in
   * @return the generated icon
   */
  public static ImageIcon createFontAwesomeIcon(char iconId, Color color) {
    return createFontAwesomeIcon(iconId, 14, color);
  }

  /**
   * create a image off the awesome icon font
   *
   * @param iconId
   *          the icon id
   * @param size
   *          the desired font size
   * @param color
   *          the color to create the icon in
   * @return the generated icon
   */
  public static ImageIcon createFontAwesomeIcon(char iconId, int size, Color color) {
    Font font = new Font("Font Awesome 5 Pro Regular", Font.PLAIN, size);
    return createFontIcon(font, String.valueOf(iconId), color);
  }

  private static ImageIcon createTextIcon(String text, int size) {
    return createTextIcon(text, size, UIManager.getColor("Label.foreground"));
  }

  private static ImageIcon createTextIcon(String text, int size, Color color) {
    Font defaultfont = (Font) UIManager.get("Label.font");
    if (defaultfont == null) {
      return null;
    }
    Font font = defaultfont.deriveFont(Font.BOLD, (float) size);
    return createFontIcon(font, text, color);
  }

  private static ImageIcon createFontIcon(Font font, String text, Color color) {
    try {
      // calculate icon size
      BufferedImage tmp = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
      Graphics2D g2 = GraphicsEnvironment.getLocalGraphicsEnvironment().createGraphics(tmp);
      g2.setFont(font);

      // get the visual bounds of the string (this is more realiable than the string bounds)
      Rectangle2D defaultBounds = g2.getFontMetrics().getStringBounds("M", g2);
      Rectangle2D bounds = font.createGlyphVector(g2.getFontRenderContext(), text).getVisualBounds();
      int iconWidth = (int) Math.ceil(bounds.getWidth()) + 2; // +2 to avoid clipping problems
      int iconHeight = (int) Math.ceil(bounds.getHeight()) + 2; // +2 to avoid clipping problems

      if (iconHeight < defaultBounds.getHeight()) {
        iconHeight = (int) Math.ceil(defaultBounds.getHeight());
      }

      g2.dispose();

      // if width is less than height, increase the width to be at least a square
      if (iconWidth < iconHeight) {
        iconWidth = iconHeight;
      }

      // and draw it
      BufferedImage buffer = new BufferedImage(iconWidth, iconHeight, BufferedImage.TYPE_INT_ARGB);
      g2 = (Graphics2D) buffer.getGraphics();
      // g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
      // g2.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
      Map<?, ?> desktopHints = (Map<?, ?>) Toolkit.getDefaultToolkit().getDesktopProperty("awt.font.desktophints");
      if (desktopHints != null) {
        g2.setRenderingHints(desktopHints);
      }

      g2.setFont(font);
      g2.setColor(color);

      // draw the glyhps centered
      int y = (int) Math.floor(bounds.getY() - (defaultBounds.getHeight() - bounds.getHeight()) / 2);
      g2.drawString(text, (int) ((iconWidth - Math.ceil(bounds.getWidth())) / 2), -y);
      g2.dispose();
      return new ImageIcon(buffer);
    }
    catch (Exception ignored) {
    }

    return EMPTY_IMAGE;
  }
}
