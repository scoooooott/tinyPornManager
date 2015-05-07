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
package org.tinymediamanager.ui.actions;

import java.awt.event.ActionEvent;
import java.util.ResourceBundle;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;

import org.apache.commons.lang3.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.core.Message;
import org.tinymediamanager.core.Message.MessageLevel;
import org.tinymediamanager.core.MessageManager;
import org.tinymediamanager.ui.TmmUIHelper;
import org.tinymediamanager.ui.UTF8Control;

/**
 * The DonateAction to redirect to the donate page
 * 
 * @author Manuel Laggner
 */
public class DonateAction extends AbstractAction {
  private static final long           serialVersionUID = 1668251251156765161L;
  private static final ResourceBundle BUNDLE           = ResourceBundle.getBundle("messages", new UTF8Control()); //$NON-NLS-1$
  private static final Logger         LOGGER           = LoggerFactory.getLogger(DonateAction.class);

  public DonateAction() {
    putValue(SHORT_DESCRIPTION, BUNDLE.getString("tmm.donate")); //$NON-NLS-1$
    putValue(SMALL_ICON, new ImageIcon(getClass().getResource("/org/tinymediamanager/ui/images/btn_donate_SM.gif")));
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    String url = StringEscapeUtils
        .unescapeHtml4("https://www.paypal.com/cgi-bin/webscr?cmd=_donations&amp;business=manuel%2elaggner%40gmail%2ecom&amp;lc=GB&amp;item_name=tinyMediaManager&amp;currency_code=EUR&amp;bn=PP%2dDonationsBF%3abtn_donate_LG%2egif%3aNonHosted");
    // .unescapeHtml4("https://www.paypal.com/cgi-bin/webscr?cmd=_donations&amp;business=myron%40tinymediamanager%2eorg&amp;lc=GB&amp;item_name=tinyMediaManager&amp;currency_code=EUR&amp;bn=PP%2dDonationsBF%3abtn_donate_LG%2egif%3aNonHosted");
    try {
      TmmUIHelper.browseUrl(url);
    }
    catch (Exception e1) {
      LOGGER.error("Donate", e1);
      MessageManager.instance
          .pushMessage(new Message(MessageLevel.ERROR, url, "message.erroropenurl", new String[] { ":", e1.getLocalizedMessage() }));
    }
  }
}
