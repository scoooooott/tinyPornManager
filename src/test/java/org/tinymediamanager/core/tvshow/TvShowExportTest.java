package org.tinymediamanager.core.tvshow;

import java.nio.file.Paths;

import org.junit.Test;
import org.tinymediamanager.core.TmmModuleManager;
import org.tinymediamanager.core.movie.MovieModuleManager;

public class TvShowExportTest {

  @Test
  public void testList() throws Exception {
    TmmModuleManager.getInstance().startUp();
    TvShowModuleManager.getInstance().startUp();
    TvShowList list = TvShowList.getInstance();

    TvShowExporter exporter = new TvShowExporter(Paths.get("templates", "TvShowDetailExampleHtml"));
    exporter.export(list.getTvShows(), Paths.get("target", "export", "TvShowDetailExampleHtml"));

    MovieModuleManager.getInstance().shutDown();
  }
}
