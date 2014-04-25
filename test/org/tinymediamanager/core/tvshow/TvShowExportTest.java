package org.tinymediamanager.core.tvshow;

import java.io.File;

import org.junit.Test;
import org.tinymediamanager.core.TmmModuleManager;
import org.tinymediamanager.core.movie.MovieModuleManager;

public class TvShowExportTest {

  @Test
  public void testList() throws Exception {
    TmmModuleManager.getInstance().startUp();
    TvShowModuleManager.getInstance().startUp();
    TvShowList list = TvShowList.getInstance();
    // TvShowExporter exporter = new TvShowExporter("templates" + File.separator + "TvShowListExampleHtml");
    // exporter.export(list.getTvShows(), "export" + File.separator + "TvShowListExampleHtml");

    TvShowExporter exporter = new TvShowExporter("templates" + File.separator + "TvShowDetailExampleHtml");
    exporter.export(list.getTvShows(), "export" + File.separator + "TvShowDetailExampleHtml");

    MovieModuleManager.getInstance().shutDown();
  }
}
