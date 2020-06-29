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
package org.tinymediamanager.ui.components.toolbar;

import java.awt.BorderLayout;
import java.awt.event.KeyEvent;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ResourceBundle;

import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

import org.apache.commons.lang3.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.Globals;
import org.tinymediamanager.TinyMediaManager;
import org.tinymediamanager.core.Message;
import org.tinymediamanager.core.Message.MessageLevel;
import org.tinymediamanager.core.MessageManager;
import org.tinymediamanager.core.UTF8Control;
import org.tinymediamanager.core.Utils;
import org.tinymediamanager.core.WolDevice;
import org.tinymediamanager.thirdparty.KodiRPC;
import org.tinymediamanager.ui.ITmmUIModule;
import org.tinymediamanager.ui.IconManager;
import org.tinymediamanager.ui.MainWindow;
import org.tinymediamanager.ui.TmmUIHelper;
import org.tinymediamanager.ui.actions.AboutAction;
import org.tinymediamanager.ui.actions.BugReportAction;
import org.tinymediamanager.ui.actions.ClearHttpCacheAction;
import org.tinymediamanager.ui.actions.ClearImageCacheAction;
import org.tinymediamanager.ui.actions.CreateDesktopFileAction;
import org.tinymediamanager.ui.actions.DocsAction;
import org.tinymediamanager.ui.actions.ExportLogAction;
import org.tinymediamanager.ui.actions.FaqAction;
import org.tinymediamanager.ui.actions.FeedbackAction;
import org.tinymediamanager.ui.actions.ForumAction;
import org.tinymediamanager.ui.actions.HomepageAction;
import org.tinymediamanager.ui.actions.LaunchUpdaterAction;
import org.tinymediamanager.ui.actions.RebuildImageCacheAction;
import org.tinymediamanager.ui.actions.SettingsAction;
import org.tinymediamanager.ui.actions.ShowChangelogAction;
import org.tinymediamanager.ui.dialogs.FullLogDialog;
import org.tinymediamanager.ui.dialogs.LogDialog;
import org.tinymediamanager.ui.dialogs.MessageHistoryDialog;
import org.tinymediamanager.ui.thirdparty.KodiRPCMenu;

import net.miginfocom.swing.MigLayout;

/**
 * The Class ToolbarPanel.
 *
 * @author Manuel Laggner
 */
public class ToolbarPanel extends JPanel {
  private static final long           serialVersionUID = 7969400170662870244L;
  private static final ResourceBundle BUNDLE           = ResourceBundle.getBundle("messages", new UTF8Control());
  private static final Logger         LOGGER           = LoggerFactory.getLogger(ToolbarPanel.class);            // $NON-NLS-1$

  private ToolbarButton               btnSearch;
  private ToolbarButton               btnEdit;
  private ToolbarButton               btnUpdate;
  private ToolbarButton               btnRename;

  private ToolbarMenu                 menuUpdate;
  private ToolbarMenu                 menuSearch;
  private ToolbarMenu                 menuEdit;
  private ToolbarMenu                 menuRename;

  public ToolbarPanel() {
    putClientProperty("class", "toolbarPanel");
    setLayout(new BorderLayout());

    JPanel panelCenter = new JPanel();
    add(panelCenter, BorderLayout.CENTER);
    panelCenter.setOpaque(false);
    panelCenter.setLayout(new MigLayout("insets 0", "[5lp:n][]20lp[]20lp[]20lp[]20lp[][grow][]15lp[]15lp[]15lp[][5lp:n]", "[]1lp[]5lp"));

    panelCenter.add(new JLabel(IconManager.TOOLBAR_LOGO), "cell 1 0 1 2,alignx center");

    btnUpdate = new ToolbarButton(IconManager.TOOLBAR_REFRESH, IconManager.TOOLBAR_REFRESH_HOVER);
    panelCenter.add(btnUpdate, "cell 2 0,alignx center");

    btnSearch = new ToolbarButton(IconManager.TOOLBAR_SEARCH, IconManager.TOOLBAR_SEARCH_HOVER);
    panelCenter.add(btnSearch, "cell 3 0,alignx center");

    btnEdit = new ToolbarButton(IconManager.TOOLBAR_EDIT, IconManager.TOOLBAR_EDIT_HOVER);
    panelCenter.add(btnEdit, "cell 4 0,alignx center");

    btnRename = new ToolbarButton(IconManager.TOOLBAR_RENAME, IconManager.TOOLBAR_RENAME_HOVER);
    panelCenter.add(btnRename, "cell 5 0,alignx center");

    JButton btnSettings = new ToolbarButton(IconManager.TOOLBAR_SETTINGS, IconManager.TOOLBAR_SETTINGS_HOVER);
    Action settingsAction = new SettingsAction();
    btnSettings.setAction(settingsAction);
    panelCenter.add(btnSettings, "cell 8 0,alignx center,aligny bottom");

    JPopupMenu toolsPopupMenu = buildToolsMenu();
    JButton btnTools = new ToolbarButton(IconManager.TOOLBAR_TOOLS, IconManager.TOOLBAR_TOOLS_HOVER, toolsPopupMenu);
    panelCenter.add(btnTools, "cell 9 0,alignx center,aligny bottom");

    JPopupMenu infoPopupMenu = buildInfoMenu();
    JButton btnInfo = new ToolbarButton(IconManager.TOOLBAR_ABOUT, IconManager.TOOLBAR_ABOUT_HOVER, infoPopupMenu);
    panelCenter.add(btnInfo, "cell 10 0,alignx center,aligny bottom");

    menuUpdate = new ToolbarMenu(BUNDLE.getString("Toolbar.update"));
    panelCenter.add(menuUpdate, "cell 2 1,alignx center");

    menuSearch = new ToolbarMenu(BUNDLE.getString("Toolbar.search"));
    panelCenter.add(menuSearch, "cell 3 1,alignx center");

    menuEdit = new ToolbarMenu(BUNDLE.getString("Toolbar.edit"));
    panelCenter.add(menuEdit, "cell 4 1,alignx center");

    menuRename = new ToolbarMenu(BUNDLE.getString("Toolbar.rename"));
    panelCenter.add(menuRename, "cell 5 1,alignx center");

    JLabel lblSettings = new ToolbarLabel(BUNDLE.getString("Toolbar.settings"), settingsAction);
    panelCenter.add(lblSettings, "cell 8 1,alignx center");

    ToolbarMenu lblTools = new ToolbarMenu(BUNDLE.getString("Toolbar.tools"), toolsPopupMenu);
    panelCenter.add(lblTools, "cell 9 1,alignx center");

    ToolbarMenu menuHelp = new ToolbarMenu(BUNDLE.getString("Toolbar.help"), infoPopupMenu);
    panelCenter.add(menuHelp, "cell 10 1,alignx center");

    JPanel panelEast = new JPanel();
    add(panelEast, BorderLayout.EAST);
    panelEast.setOpaque(false);
    panelEast.setLayout(new MigLayout("insets 0", "[]", "[grow]"));
  }

  public void setUIModule(ITmmUIModule module) {
    btnUpdate.setAction(module.getUpdateAction());
    btnUpdate.setIcons(module.getSearchButtonIcon(), module.getSearchButtonHoverIcon());
    menuUpdate.setPopupMenu(module.getUpdateMenu());

    btnSearch.setAction(module.getSearchAction());
    menuSearch.setPopupMenu(module.getSearchMenu());

    btnEdit.setAction(module.getEditAction());
    menuEdit.setPopupMenu(module.getEditMenu());

    btnRename.setAction(module.getRenameAction());
    menuRename.setPopupMenu(module.getRenameMenu());
  }

  private JPopupMenu buildToolsMenu() {
    JPopupMenu menu = new JPopupMenu();

    menu.add(new ClearImageCacheAction());
    menu.add(new RebuildImageCacheAction());
    menu.add(new ClearHttpCacheAction());

    menu.addSeparator();

    JMenuItem tmmLogs = new JMenuItem(BUNDLE.getString("tmm.errorlogs"));
    menu.add(tmmLogs);
    tmmLogs.addActionListener(arg0 -> {
      JDialog logDialog = new LogDialog();
      logDialog.setLocationRelativeTo(MainWindow.getActiveInstance());
      logDialog.setVisible(true);
    });

    JMenuItem tmmMessages = new JMenuItem(BUNDLE.getString("tmm.messages"));
    tmmMessages.setMnemonic(KeyEvent.VK_L);
    menu.add(tmmMessages);
    tmmMessages.addActionListener(arg0 -> {
      JDialog messageDialog = MessageHistoryDialog.getInstance();
      messageDialog.setVisible(true);
    });
    JMenuItem tmmFolder = new JMenuItem(BUNDLE.getString("tmm.gotoinstalldir"));
    menu.add(tmmFolder);
    tmmFolder.addActionListener(arg0 -> {
      Path path = Paths.get(System.getProperty("user.dir"));
      try {
        // check whether this location exists
        if (Files.exists(path)) {
          TmmUIHelper.openFile(path);
        }
      }
      catch (Exception ex) {
        LOGGER.error("open filemanager", ex);
        MessageManager.instance
            .pushMessage(new Message(MessageLevel.ERROR, path, "message.erroropenfolder", new String[] { ":", ex.getLocalizedMessage() }));
      }
    });

    menu.addSeparator();

    final JMenu menuWakeOnLan = new JMenu(BUNDLE.getString("tmm.wakeonlan"));
    menuWakeOnLan.setMnemonic(KeyEvent.VK_W);
    menuWakeOnLan.addMenuListener(new MenuListener() {
      @Override
      public void menuCanceled(MenuEvent arg0) {
      }

      @Override
      public void menuDeselected(MenuEvent arg0) {
      }

      @Override
      public void menuSelected(MenuEvent arg0) {
        menuWakeOnLan.removeAll();
        for (final WolDevice device : Globals.settings.getWolDevices()) {
          JMenuItem item = new JMenuItem(device.getName());
          item.addActionListener(arg01 -> Utils.sendWakeOnLanPacket(device.getMacAddress()));
          menuWakeOnLan.add(item);
        }
      }
    });
    menu.add(menuWakeOnLan);

    if (Boolean.parseBoolean(System.getProperty("tmm.noupdate")) != true) {
      menu.addSeparator();
      menu.add(new LaunchUpdaterAction());
    }

    // debug menu
    if (Globals.isDebug()) {
      final JMenu debugMenu = new JMenu("Debug");

      JMenuItem trace = new JMenuItem("set Console Logger to TRACE");
      trace.addActionListener(arg0 -> {
        System.setProperty("tmm.consoleloglevel", "TRACE");
        TinyMediaManager.setConsoleLogLevel();
        MessageManager.instance.pushMessage(new Message("Trace levels set!", "Test"));
        LOGGER.trace("if you see that, we're now on TRACE logging level ;)");
      });
      debugMenu.add(trace);

      JMenuItem traceLogs = new JMenuItem("Show all logs from this session");
      debugMenu.add(traceLogs);
      traceLogs.addActionListener(arg0 -> {
        JDialog logDialog = new FullLogDialog();
        logDialog.setLocationRelativeTo(MainWindow.getActiveInstance());
        logDialog.setVisible(true);
      });

      menu.addSeparator();
      menu.add(debugMenu);
    }

    final JMenu kodiRPCMenu = KodiRPCMenu.KodiMenuTop();
    menu.add(kodiRPCMenu);

    // activate/deactivate menu items based on some status
    menu.addPopupMenuListener(new PopupMenuListener() {
      @Override
      public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
        if (!Globals.settings.getWolDevices().isEmpty()) {
          menuWakeOnLan.setEnabled(true);
        }
        else {
          menuWakeOnLan.setEnabled(false);
        }

        kodiRPCMenu.setText(KodiRPC.getInstance().getVersion());
        if (KodiRPC.getInstance().isConnected()) {
          kodiRPCMenu.setEnabled(true);
        }
        else {
          kodiRPCMenu.setEnabled(false);
        }
      }

      @Override
      public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
      }

      @Override
      public void popupMenuCanceled(PopupMenuEvent e) {
      }
    });

    menu.addSeparator();
    menu.add(new BugReportAction());
    menu.add(new ExportLogAction());

    if (SystemUtils.IS_OS_LINUX) {
      menu.addSeparator();
      menu.add(new CreateDesktopFileAction());
    }

    return menu;
  }

  private JPopupMenu buildInfoMenu() {
    JPopupMenu menu = new JPopupMenu();

    menu.add(new FaqAction());
    menu.add(new DocsAction());
    menu.add(new ForumAction());
    menu.add(new ShowChangelogAction());
    menu.addSeparator();

    menu.add(new BugReportAction());
    menu.add(new FeedbackAction());

    menu.addSeparator();
    menu.add(new HomepageAction());
    menu.add(new AboutAction());

    return menu;
  }
}
