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

import java.awt.Color;

import ch.swingfx.color.ColorUtil;
import ch.swingfx.twinkle.style.AbstractNotificationStyle;
import ch.swingfx.twinkle.style.background.ColorBackground;
import ch.swingfx.twinkle.style.closebutton.RoundCloseButton;
import ch.swingfx.twinkle.style.overlay.BorderOverlay;
import ch.swingfx.twinkle.style.overlay.NullOverlay;
import ch.swingfx.twinkle.style.overlay.OverlayPaintMode;
import ch.swingfx.twinkle.window.NotificationWindowTypes;

/**
 * The class TmmNotificationStyle to display notifications our style
 * 
 * @author Manuel Laggner
 */
public class TmmNotificationStyle extends AbstractNotificationStyle {
  public TmmNotificationStyle() {
    super();
    withNotificationWindowCreator(NotificationWindowTypes.DEFAULT);
    withTitleFontColor(Color.BLACK);
    withMessageFontColor(Color.BLACK);
    withAlpha(0.85f);
    withWidth(320);
    withBackground(new ColorBackground(new Color(245, 245, 245)));
    withWindowCornerRadius(8);
    withOverlay(new BorderOverlay(1, Color.BLACK, OverlayPaintMode.ALWAYS, new NullOverlay()));
    withCloseButton(new RoundCloseButton(ColorUtil.withAlpha(Color.WHITE, 0.8f), Color.BLACK).withPosition(290, 5));
  }
}
