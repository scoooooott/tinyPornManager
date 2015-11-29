package org.tinymediamanager.core.tvshow;

import static org.assertj.core.api.Assertions.*;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.tinymediamanager.core.MediaFileType;
import org.tinymediamanager.core.Settings;
import org.tinymediamanager.core.TmmModuleManager;
import org.tinymediamanager.core.movie.MovieModuleManager;
import org.tinymediamanager.core.tvshow.entities.TvShow;
import org.tinymediamanager.core.tvshow.entities.TvShowEpisode;
import org.tinymediamanager.core.tvshow.tasks.TvShowUpdateDatasourceTask;

public class TvShowUpdateDatasourceTaskTest {
  private static String DATA_PATH         = "target/data";
  private static String DATASOURCE_SOURCE = "target/test-classes/testtvshows";
  private static String DATASOURCE_DEST   = "target/tvshowtest";

  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
    FileUtils.deleteQuietly(new File(DATA_PATH));
    // create a default config file for config access
    Settings.getInstance(DATA_PATH);
    TmmModuleManager.getInstance().startUp();
    MovieModuleManager.getInstance().startUp();
    TvShowModuleManager.getInstance().startUp();
  }

  @AfterClass
  public static void tearDownAfterClass() throws Exception {
    TvShowModuleManager.getInstance().shutDown();
    MovieModuleManager.getInstance().shutDown();
    TmmModuleManager.getInstance().shutDown();
  }

  @Test
  public void testUds() throws Exception {
    File destDir = new File(DATASOURCE_DEST);
    FileUtils.deleteQuietly(destDir);

    // just a copy; we might have another test which uses these files
    FileUtils.copyDirectory(new File(DATASOURCE_SOURCE), destDir);
    TvShowModuleManager.TV_SHOW_SETTINGS.addTvShowDataSources(DATASOURCE_DEST);

    TvShowUpdateDatasourceTask task = new TvShowUpdateDatasourceTask();
    task.run();

    TvShowList tvShowList = TvShowList.getInstance();
    assertThat(tvShowList.getTvShows().size()).isEqualTo(3);

    // Breaking Bad
    TvShow show = tvShowList.getTvShowByPath(new File(DATASOURCE_DEST + "/Breaking Bad"));
    assertThat(show).isNotNull();
    assertThat(show.getTitle()).isEqualTo("Breaking Bad");

    for (TvShowEpisode ep : show.getEpisodes()) {
      System.out.println(ep.getPath() + File.separator + ep.getMediaFiles(MediaFileType.VIDEO).get(0).getFilename() + " : " + ep.getSeason() + " "
          + ep.getEpisode());
    }

    assertThat(show.getEpisodes().size()).isEqualTo(62);
  }

}
