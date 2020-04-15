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

package org.tinymediamanager.ui.thirdparty;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

import org.tinymediamanager.thirdparty.KodiRPC;
import org.tinymediamanager.thirdparty.SplitUri;
import org.tinymediamanager.ui.movies.actions.MovieKodiRefreshNfoAction;
import org.tinymediamanager.ui.tvshows.actions.TvShowKodiRefreshNfoAction;

public class KodiRPCMenu {

  /**
   * Adds Kodi RPC menu structure in right-click popup
   * 
   * @return
   */
  public static JMenu KodiMenuRightClickMovies() {
    String version = KodiRPC.getInstance().getVersion();
    JMenu m = new JMenu(version);
    m.add(new MovieKodiRefreshNfoAction());
    return m;
  }

  /**
   * Adds Kodi RPC menu structure in right-click popup
   * 
   * @return
   */
  public static JMenu KodiMenuRightClickTvShows() {
    String version = KodiRPC.getInstance().getVersion();
    JMenu m = new JMenu(version);
    m.add(new TvShowKodiRefreshNfoAction());
    return m;
  }

  /**
   * Adds Kodi RPC menu structure in top bar
   * 
   * @return
   */
  public static JMenu KodiMenuTop() {
    String version = KodiRPC.getInstance().getVersion();
    JMenu m = new JMenu(version);
    m.add(Application());
    m.add(System());
    m.add(VideoDatasources());
    m.add(AudioDatasources());
    return m;
  }

  private static JMenu Application() {
    JMenu m = new JMenu("Application");

    JMenuItem i = new JMenuItem("Quit");
    i.addActionListener(e -> KodiRPC.getInstance().ApplicationQuit());
    m.add(i);

    i = new JMenuItem("Mute / UnMute");
    i.addActionListener(e -> KodiRPC.getInstance().ApplicationMute());
    m.add(i);

    m.add(Volume());

    return m;
  }

  private static JMenu VideoDatasources() {
    JMenu m = new JMenu("Video Library");

    JMenuItem cleanLibraryMenuItem = new JMenuItem("Clean video library");
    cleanLibraryMenuItem.addActionListener(new CleanVideoLibraryListener());
    m.add(cleanLibraryMenuItem);

    JMenu m2 = new JMenu("Scan for new content");
    JMenuItem i = new JMenuItem("Scan ALL Datasources");
    i.addActionListener(new VideoDatasourceScanListener(null));
    i.setEnabled(false);
    m2.add(i);
    if (!KodiRPC.getInstance().getVideoDataSources().isEmpty()) {
      i.setEnabled(true);
      for (SplitUri ds : KodiRPC.getInstance().getVideoDataSources()) {
        i = new JMenuItem("Scan " + ds.label + "  (" + ds.type + ")");
        if ("UPNP".equals(ds.type)) {
          // cannot "scan" UPNP - always directly fetched and not in library
          i.setEnabled(false);
        }
        else {
          i.addActionListener(new VideoDatasourceScanListener(ds.file));
        }
        m2.add(i);
      }
    }
    m.add(m2);
    return m;
  }

  private static JMenu AudioDatasources() {
    JMenu m = new JMenu("Audio Library");

    JMenuItem cleanLibraryMenuItem = new JMenuItem("Clean audio library");
    cleanLibraryMenuItem.addActionListener(new CleanAudioLibraryListener());
    m.add(cleanLibraryMenuItem);

    JMenu m2 = new JMenu("Scan for new content");
    JMenuItem i = new JMenuItem("Scan ALL Datasources");
    i.addActionListener(new AudioDatasourceScanListener(null));
    i.setEnabled(false);
    m2.add(i);
    if (!KodiRPC.getInstance().getAudioDataSources().isEmpty()) {
      i.setEnabled(true);
      for (SplitUri ds : KodiRPC.getInstance().getAudioDataSources()) {
        i = new JMenuItem("Scan " + ds.label + "  (" + ds.type + ")");
        if ("UPNP".equals(ds.type)) {
          // cannot "scan" UPNP - always directly fetched and not in library
          i.setEnabled(false);
        }
        else {
          i.addActionListener(new AudioDatasourceScanListener(ds.file));
        }
        m2.add(i);
      }
    }
    m.add(m2);

    return m;
  }

  private static JMenu Volume() {
    JMenu m = new JMenu("Volume");

    JMenuItem i = new JMenuItem("100%");
    i.addActionListener(new ApplicationVolumeListener(100));
    m.add(i);
    i = new JMenuItem(" 90%");
    i.addActionListener(new ApplicationVolumeListener(90));
    m.add(i);
    i = new JMenuItem(" 80%");
    i.addActionListener(new ApplicationVolumeListener(80));
    m.add(i);
    i = new JMenuItem(" 70%");
    i.addActionListener(new ApplicationVolumeListener(70));
    m.add(i);
    i = new JMenuItem(" 60%");
    i.addActionListener(new ApplicationVolumeListener(60));
    m.add(i);
    i = new JMenuItem(" 50%");
    i.addActionListener(new ApplicationVolumeListener(50));
    m.add(i);
    i = new JMenuItem(" 40%");
    i.addActionListener(new ApplicationVolumeListener(40));
    m.add(i);
    i = new JMenuItem(" 30%");
    i.addActionListener(new ApplicationVolumeListener(30));
    m.add(i);
    i = new JMenuItem(" 20%");
    i.addActionListener(new ApplicationVolumeListener(20));
    m.add(i);
    i = new JMenuItem(" 10%");
    i.addActionListener(new ApplicationVolumeListener(10));
    m.add(i);

    return m;
  }

  private static class ApplicationVolumeListener implements ActionListener {
    private int vol;

    public ApplicationVolumeListener(int vol) {
      this.vol = vol;
    }

    public void actionPerformed(ActionEvent e) {
      KodiRPC.getInstance().ApplicationVolume(vol);
    }
  }

  private static class CleanAudioLibraryListener implements ActionListener {
    public void actionPerformed(ActionEvent e) {
      KodiRPC.getInstance().LibraryAudioClean();
    }
  }

  private static class CleanVideoLibraryListener implements ActionListener {
    public void actionPerformed(ActionEvent e) {
      KodiRPC.getInstance().LibraryVideoClean();
    }
  }

  private static class VideoDatasourceScanListener implements ActionListener {
    private String datasource;

    public VideoDatasourceScanListener(String datasource) {
      this.datasource = datasource;
    }

    public void actionPerformed(ActionEvent e) {
      KodiRPC.getInstance().LibraryVideoScan(datasource);
    }
  }

  private static class AudioDatasourceScanListener implements ActionListener {
    private String datasource;

    public AudioDatasourceScanListener(String datasource) {
      this.datasource = datasource;
    }

    public void actionPerformed(ActionEvent e) {
      KodiRPC.getInstance().LibraryAudioScan(datasource);
    }
  }

  private static JMenu System() {
    JMenu m = new JMenu("System");

    JMenuItem i = new JMenuItem("Hibernate");
    i.addActionListener(e -> KodiRPC.getInstance().SystemHibernate());
    m.add(i);

    i = new JMenuItem("Reboot");
    i.addActionListener(e -> KodiRPC.getInstance().SystemReboot());
    m.add(i);

    i = new JMenuItem("Shutdown");
    i.addActionListener(e -> KodiRPC.getInstance().SystemShutdown());
    m.add(i);

    i = new JMenuItem("Suspend");
    i.addActionListener(e -> KodiRPC.getInstance().SystemSuspend());
    m.add(i);

    return m;
  }

}
