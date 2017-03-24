package org.tinymediamanager.core.tvshow;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.tinymediamanager.BasicTest;
import org.tinymediamanager.core.Settings;
import org.tinymediamanager.core.TmmModuleManager;
import org.tinymediamanager.core.Utils;
import org.tinymediamanager.core.tvshow.entities.TvShow;
import org.tinymediamanager.core.tvshow.entities.TvShowSeason;
import org.tinymediamanager.core.tvshow.tasks.TvShowUpdateDatasourceTask2;

public class TvShowUpdateDatasourceTaskTest extends BasicTest {
  private static final String FOLDER                   = getSettingsFolder();
  private static final int    NUMBER_OF_EXPECTED_SHOWS = 9;

  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
    // MediaInfoUtils.loadMediaInfo(); // unneeded here for UDS. does not work on buildserver
    deleteSettingsFolder();
    Settings.getInstance(FOLDER);
  }

  @Before
  public void setUpBeforeTest() throws Exception {
    TmmModuleManager.getInstance().startUp();
    TvShowModuleManager.getInstance().startUp();
    Utils.copyDirectoryRecursive(Paths.get("target/test-classes/testtvshows"), Paths.get(FOLDER, "testtvshows"));
    TvShowModuleManager.SETTINGS.addTvShowDataSources(FOLDER + "/testtvshows");
  }

  @After
  public void tearDownAfterTest() throws Exception {
    TvShowModuleManager.getInstance().shutDown();
    TmmModuleManager.getInstance().shutDown();
  }

  @Test
  public void udsNew() throws Exception {
    TvShowUpdateDatasourceTask2 task = new TvShowUpdateDatasourceTask2();
    task.run();
    check();
  }

  // @Test
  // public void udsOld() throws Exception {
  // TvShowUpdateDatasourceTask task = new TvShowUpdateDatasourceTask();
  // task.run();
  // check();
  // }

  private void check() {
    // do some checks before shutting down the database
    TvShowList tvShowList = TvShowList.getInstance();
    assertThat(tvShowList.getTvShows().size()).isEqualTo(NUMBER_OF_EXPECTED_SHOWS);
    Comparator<TvShowSeason> seasonComparator = new Comparator<TvShowSeason>() {
      @Override
      public int compare(TvShowSeason o1, TvShowSeason o2) {
        return o1.getSeason() - o2.getSeason();
      }
    };

    ///////////////////////////////////////////////////////////////////////////////////////
    // Breaking Bad
    ///////////////////////////////////////////////////////////////////////////////////////
    TvShow show = tvShowList.getTvShowByPath(new File(FOLDER + "/testtvshows/Breaking Bad"));
    assertThat(show).isNotNull();
    assertThat(show.getTitle()).isEqualTo("Breaking Bad");
    assertThat(show.getEpisodes().size()).isEqualTo(62);
    assertThat(show.getSeasons().size()).isEqualTo(5);

    List<TvShowSeason> seasons = show.getSeasons();
    // Collections.sort(seasons, seasonComparator);
    Object[] a = seasons.toArray();
    Arrays.sort(a);
    for (int i = 0; i < a.length; i++) {
      seasons.set(i, (TvShowSeason) a[i]);
    }

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
    show = tvShowList.getTvShowByPath(new File(FOLDER + "/testtvshows/Firefly"));
    assertThat(show).isNotNull();
    assertThat(show.getTitle()).isEqualTo("Firefly");
    assertThat(show.getEpisodes().size()).isEqualTo(14);
    assertThat(show.getSeasons().size()).isEqualTo(1);

    seasons = show.getSeasons();
    // Collections.sort(seasons, seasonComparator);
    a = seasons.toArray();
    Arrays.sort(a);
    for (int i = 0; i < a.length; i++) {
      seasons.set(i, (TvShowSeason) a[i]);
    }

    assertThat(seasons.get(0).getSeason()).isEqualTo(1);
    assertThat(seasons.get(0).getEpisodes().size()).isEqualTo(14);

    ///////////////////////////////////////////////////////////////////////////////////////
    // Futurama
    ///////////////////////////////////////////////////////////////////////////////////////
    show = tvShowList.getTvShowByPath(new File(FOLDER + "/testtvshows/Futurama (1999)"));
    assertThat(show).isNotNull();
    assertThat(show.getTitle()).isEqualTo("Futurama");
    assertThat(show.getEpisodes().size()).isEqualTo(44);
    assertThat(show.getSeasons().size()).isEqualTo(3);

    seasons = show.getSeasons();
    // Collections.sort(seasons, seasonComparator);
    a = seasons.toArray();
    Arrays.sort(a);
    for (int i = 0; i < a.length; i++) {
      seasons.set(i, (TvShowSeason) a[i]);
    }

    assertThat(seasons.get(0).getSeason()).isEqualTo(1);
    assertThat(seasons.get(0).getEpisodes().size()).isEqualTo(9);
    assertThat(seasons.get(1).getSeason()).isEqualTo(2);
    assertThat(seasons.get(1).getEpisodes().size()).isEqualTo(20);
    assertThat(seasons.get(2).getSeason()).isEqualTo(3);
    assertThat(seasons.get(2).getEpisodes().size()).isEqualTo(15);
  }
}
