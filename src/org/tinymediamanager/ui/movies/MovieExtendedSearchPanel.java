package org.tinymediamanager.ui.movies;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.tinymediamanager.scraper.MediaGenres;
import org.tinymediamanager.ui.CollapsiblePanel;
import org.tinymediamanager.ui.movies.MovieExtendedComparator.SortColumn;
import org.tinymediamanager.ui.movies.MovieExtendedComparator.SortOrder;

import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

/**
 * The Class MovieExtendedSearchPanel.
 */
@SuppressWarnings("serial")
public class MovieExtendedSearchPanel extends CollapsiblePanel {

  /** The cb search watched. */
  private JCheckBox           cbFilterWatched;

  /** The action search. */
  private final Action        actionSearch = new SearchAction();

  private final Action        actionSort   = new SortAction();

  /** The movie selection model. */
  private MovieSelectionModel movieSelectionModel;
  private JLabel              lblGenre;
  private JComboBox           cbGenre;
  private JComboBox           cbSortColumn;
  private JComboBox           cbSortOrder;
  private JLabel              lblFilterBy;
  private JLabel              lblWatchedFlag;
  private JComboBox           cbWatched;
  private JCheckBox           cbFilterGenre;
  private JLabel              lblSortBy;
  private JCheckBox           cbFilterCast;
  private JLabel              lblCastMember;
  private JTextField          tfCastMember;
  private JLabel              lblSpacer;

  /**
   * Instantiates a new movie extended search panel.
   * 
   * @param model
   *          the model
   */
  public MovieExtendedSearchPanel(MovieSelectionModel model) {
    super("Extended filter and sort options");
    this.movieSelectionModel = model;

    JPanel panel = new JPanel();
    panel.setLayout(new FormLayout(new ColumnSpec[] { FormFactory.RELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC, FormFactory.RELATED_GAP_COLSPEC,
        ColumnSpec.decode("default:grow"), FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("default:grow"), }, new RowSpec[] { FormFactory.DEFAULT_ROWSPEC,
        FormFactory.NARROW_LINE_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.NARROW_LINE_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.NARROW_LINE_GAP_ROWSPEC,
        FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.NARROW_LINE_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
        FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, }));

    lblFilterBy = new JLabel("filter by");
    panel.add(lblFilterBy, "2, 1, 3, 1");

    cbFilterWatched = new JCheckBox("");
    panel.add(cbFilterWatched, "2, 3");

    lblWatchedFlag = new JLabel("Watched flag");
    panel.add(lblWatchedFlag, "4, 3, right, default");

    cbWatched = new JComboBox();
    panel.add(cbWatched, "6, 3, fill, default");

    cbFilterGenre = new JCheckBox("");
    panel.add(cbFilterGenre, "2, 5");

    lblGenre = new JLabel("Genre");
    panel.add(lblGenre, "4, 5, right, default");

    cbGenre = new JComboBox(MediaGenres.values());
    panel.add(cbGenre, "6, 5, fill, default");

    cbFilterCast = new JCheckBox("");
    panel.add(cbFilterCast, "2, 7");

    lblCastMember = new JLabel("Cast member");
    panel.add(lblCastMember, "4, 7, right, default");

    tfCastMember = new JTextField();
    panel.add(tfCastMember, "6, 7, fill, default");
    tfCastMember.setColumns(10);

    lblSortBy = new JLabel("sort by");
    panel.add(lblSortBy, "2, 9, 3, 1");

    cbSortColumn = new JComboBox(SortColumn.values());
    panel.add(cbSortColumn, "4, 11, fill, default");

    cbSortOrder = new JComboBox(SortOrder.values());
    panel.add(cbSortOrder, "6, 11, fill, default");

    add(panel);
    toggleVisibility(false);
  }

  /**
   * The Class SearchAction.
   */
  private class SearchAction extends AbstractAction {

    /**
     * Instantiates a new search action.
     */
    public SearchAction() {
      putValue(NAME, "Filter");
      putValue(SHORT_DESCRIPTION, "Search using the given options");
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent e) {
      // HashMap<SearchOptions, Object> searchOptions = new
      // HashMap<SearchOptions, Object>();
      // // watched Flag
      // if (cbSearchNotWatched.isSelected() ^ cbFilterWatched.isSelected()) {
      // if (cbSearchNotWatched.isSelected()) {
      // searchOptions.put(SearchOptions.WATCHED, false);
      // } else {
      // searchOptions.put(SearchOptions.WATCHED, true);
      // }
      // }
      //
      // // genre
      // MediaGenres genre = (MediaGenres) cbGenre.getSelectedItem();
      // if (genre != null && genre != MediaGenres.EMPTY) {
      // searchOptions.put(SearchOptions.GENRE, genre);
      // }
      //
      // // apply the filter
      // movieSelectionModel.filterMovies(searchOptions);
    }
  }

  private class SortAction extends AbstractAction {
    public SortAction() {
      putValue(NAME, "Sort");
      putValue(SHORT_DESCRIPTION, "Sort movielist");
    }

    public void actionPerformed(ActionEvent e) {
      SortColumn column = (SortColumn) cbSortColumn.getSelectedItem();
      SortOrder order = (SortOrder) cbSortOrder.getSelectedItem();
      boolean ascending = order == SortOrder.ASCENDING ? true : false;

      // sort
      movieSelectionModel.sortMovies(column, ascending);
    }
  }
}
