package org.tinymediamanager.core.movie;

import static org.assertj.core.api.Assertions.assertThat;
import static org.tinymediamanager.core.movie.MovieEdition.EXTENDED_EDITION;
import static org.tinymediamanager.core.movie.MovieEdition.UNCUT;

import org.junit.Test;

/**
 * @author Manuel Laggner
 */
public class MovieEditionTest {

  @Test
  public void testMovieEditionRegexp() {
    assertThat(MovieEdition.getMovieEditionFromString("Lord.Of.The.Rings.Extended.Edition")).isEqualTo(EXTENDED_EDITION);
    assertThat(MovieEdition.getMovieEditionFromString("12 Monkeys [Uncut] [3D]")).isEqualTo(UNCUT);
    // TODO need some more real examples
  }
}
