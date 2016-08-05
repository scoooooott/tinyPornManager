package org.tinymediamanager.core.tvshow;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.tinymediamanager.core.Settings;
import org.tinymediamanager.core.TmmModuleManager;
import org.tinymediamanager.core.tvshow.entities.TvShow;
import org.tinymediamanager.core.tvshow.entities.TvShowSeason;
import org.tinymediamanager.core.tvshow.tasks.TvShowUpdateDatasourceTask;
import org.tinymediamanager.core.tvshow.tasks.TvShowUpdateDatasourceTask2;

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
    TvShowModuleManager.getInstance().startUp();
  }

  @AfterClass
  public static void tearDownAfterClass() throws Exception {
    TvShowModuleManager.getInstance().shutDown();
    TmmModuleManager.getInstance().shutDown();
  }

  @Test
  public void udsNew() throws Exception {
    // just a copy; we might have another test which uses these files
    // FileUtils.copyDirectory(new File(DATASOURCE_SOURCE), new File(DATASOURCE_DEST));
    TvShowModuleManager.TV_SHOW_SETTINGS.addTvShowDataSources(DATASOURCE_SOURCE);
    TvShowUpdateDatasourceTask2 task = new TvShowUpdateDatasourceTask2();
    task.run();
  }

  @Test
  public void testUds() throws Exception {
    Comparator<TvShowSeason> seasonComparator = new Comparator<TvShowSeason>() {
      @Override
      public int compare(TvShowSeason o1, TvShowSeason o2) {
        return o1.getSeason() - o2.getSeason();
      }
    };
    File destDir = new File(DATASOURCE_DEST);
    FileUtils.deleteQuietly(destDir);

    // just a copy; we might have another test which uses these files
    FileUtils.copyDirectory(new File(DATASOURCE_SOURCE), destDir);
    TvShowModuleManager.TV_SHOW_SETTINGS.addTvShowDataSources(DATASOURCE_DEST);

    TvShowUpdateDatasourceTask task = new TvShowUpdateDatasourceTask();
    task.run();

    TvShowList tvShowList = TvShowList.getInstance();
    assertThat(tvShowList.getTvShows().size()).isEqualTo(3);

    ///////////////////////////////////////////////////////////////////////////////////////
    // Breaking Bad
    ///////////////////////////////////////////////////////////////////////////////////////
    TvShow show = tvShowList.getTvShowByPath(new File(DATASOURCE_DEST + "/Breaking Bad"));
    assertThat(show).isNotNull();
    assertThat(show.getTitle()).isEqualTo("Breaking Bad");
    assertThat(show.getEpisodes().size()).isEqualTo(62);
    assertThat(show.getSeasons().size()).isEqualTo(5);

    List<TvShowSeason> seasons = show.getSeasons();
    Collections.sort(seasons, seasonComparator);
    assertThat(seasons.get(0).getSeason()).isEqualTo(1);
    assertThat(seasons.get(0).getEpisodes().size()).isEqualTo(7);
    assertThat(seasons.get(1).getSeason()).isEqualTo(2);
    assertThat(seasons.get(1).getEpisodes().size()).isEqualTo(13);
    assertThat(seasons.get(2).getSeason()).isEqualTo(3);
    assertThat(seasons.get(2).getEpisodes().size()).isEqualTo(13);
    assertThat(seasons.get(3).getSeason()).isEqualTo(4);
    assertThat(seasons.get(3).getEpisodes().size()).isEqualTo(13);
    assertThat(seasons.get(4).getSeason()).isEqualTo(5);
    assertThat(seasons.get(4).getEpisodes().size()).isEqualTo(16);

    ///////////////////////////////////////////////////////////////////////////////////////
    // Firefly
    ///////////////////////////////////////////////////////////////////////////////////////
    show = tvShowList.getTvShowByPath(new File(DATASOURCE_DEST + "/Firefly"));
    assertThat(show).isNotNull();
    assertThat(show.getTitle()).isEqualTo("Firefly");
    assertThat(show.getEpisodes().size()).isEqualTo(14);
    assertThat(show.getSeasons().size()).isEqualTo(1);

    seasons = show.getSeasons();
    Collections.sort(seasons, seasonComparator);
    assertThat(seasons.get(0).getSeason()).isEqualTo(1);
    assertThat(seasons.get(0).getEpisodes().size()).isEqualTo(14);

    ///////////////////////////////////////////////////////////////////////////////////////
    // Futurama
    ///////////////////////////////////////////////////////////////////////////////////////
    show = tvShowList.getTvShowByPath(new File(DATASOURCE_DEST + "/Futurama (1999)"));
    assertThat(show).isNotNull();
    assertThat(show.getTitle()).isEqualTo("Futurama");
    assertThat(show.getEpisodes().size()).isEqualTo(44);
    assertThat(show.getSeasons().size()).isEqualTo(3);

    seasons = show.getSeasons();
    Collections.sort(seasons, seasonComparator);
    assertThat(seasons.get(0).getSeason()).isEqualTo(1);
    assertThat(seasons.get(0).getEpisodes().size()).isEqualTo(9);
    assertThat(seasons.get(1).getSeason()).isEqualTo(2);
    assertThat(seasons.get(1).getEpisodes().size()).isEqualTo(20);
    assertThat(seasons.get(2).getSeason()).isEqualTo(3);
    assertThat(seasons.get(2).getEpisodes().size()).isEqualTo(15);
  }
}
