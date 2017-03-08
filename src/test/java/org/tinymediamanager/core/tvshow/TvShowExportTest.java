package org.tinymediamanager.core.tvshow;

import java.nio.file.Paths;

import org.junit.Test;
import org.tinymediamanager.BasicTest;
import org.tinymediamanager.core.TmmModuleManager;

public class TvShowExportTest extends BasicTest {

  @Test
  public void testList() throws Exception {
    TmmModuleManager.getInstance().startUp();
    TvShowModuleManager.getInstance().startUp();
    TvShowList list = TvShowList.getInstance();

    TvShowExporter exporter = new TvShowExporter(Paths.get("templates", "TvShowDetailExampleHtml"));
    exporter.export(list.getTvShows(), Paths.get(getSettingsFolder(), "TvShowDetailExampleHtml"));

    TvShowModuleManager.getInstance().shutDown();
    TmmModuleManager.getInstance().shutDown();
  }
}
