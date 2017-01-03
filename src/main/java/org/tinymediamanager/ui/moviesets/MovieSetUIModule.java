/*
 * Copyright 2012 - 2016 Manuel Laggner
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
package org.tinymediamanager.ui.moviesets;

import java.awt.CardLayout;
import java.util.ResourceBundle;

import javax.swing.Action;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTabbedPane;

import org.tinymediamanager.core.movie.entities.Movie;
import org.tinymediamanager.core.movie.entities.MovieSet;
import org.tinymediamanager.ui.ITmmUIModule;
import org.tinymediamanager.ui.UTF8Control;
import org.tinymediamanager.ui.components.MainTabbedPane;
import org.tinymediamanager.ui.movies.MovieSelectionModel;
import org.tinymediamanager.ui.movies.panels.MovieArtworkPanel;
import org.tinymediamanager.ui.movies.panels.MovieCastPanel;
import org.tinymediamanager.ui.movies.panels.MovieInformationPanel;
import org.tinymediamanager.ui.movies.panels.MovieMediaInformationPanel;
import org.tinymediamanager.ui.movies.panels.MovieTrailerPanel;
import org.tinymediamanager.ui.moviesets.actions.MovieSetEditAction;
import org.tinymediamanager.ui.moviesets.actions.MovieSetSearchAction;
import org.tinymediamanager.ui.moviesets.panels.MovieSetTreePanel;
import org.tinymediamanager.ui.settings.TmmSettingsNode;

public class MovieSetUIModule implements ITmmUIModule {
  private final static ResourceBundle  BUNDLE               = ResourceBundle.getBundle("messages", new UTF8Control()); //$NON-NLS-1$
  private final static String          ID                   = "movieSets";

  private static MovieSetUIModule      instance             = null;

  private final MovieSetSelectionModel selectionModel;
  private final MovieSelectionModel    movieSelectionModel;

  private final MovieSetTreePanel      listPanel;
  private final JPanel                 detailPanel;
  private final JTabbedPane            movieSetDetailPanel;
  private final JTabbedPane            movieDetailPanel;

  private final Action                 actionSearchMovieSet = new MovieSetSearchAction(false);
  private final Action                 actionEditMovieSet   = new MovieSetEditAction(false);

  private MovieSetUIModule() {
    selectionModel = new MovieSetSelectionModel();
    movieSelectionModel = new MovieSelectionModel();

    listPanel = new MovieSetTreePanel(selectionModel);

    detailPanel = new JPanel();
    detailPanel.setLayout(new CardLayout());

    // panel for movie sets
    movieSetDetailPanel = new MainTabbedPane();
    movieSetDetailPanel.addTab(BUNDLE.getString("metatag.details"), new MovieSetInformationPanel(selectionModel));//$NON-NLS-1$
    detailPanel.add(movieSetDetailPanel, "movieSet");

    // panel for movies
    movieDetailPanel = new MainTabbedPane();
    movieDetailPanel.addTab("Details", new MovieInformationPanel(movieSelectionModel));
    movieDetailPanel.addTab("Cast", new MovieCastPanel(movieSelectionModel));
    movieDetailPanel.addTab("Media files", new MovieMediaInformationPanel(movieSelectionModel));
    movieDetailPanel.addTab("Artwork", new MovieArtworkPanel(movieSelectionModel));
    movieDetailPanel.addTab("Trailer", new MovieTrailerPanel(movieSelectionModel));
    detailPanel.add(movieDetailPanel, "movie");
  }

  public static MovieSetUIModule getInstance() {
    if (instance == null) {
      instance = new MovieSetUIModule();
    }
    return instance;
  }

  @Override
  public String getModuleId() {
    return ID;
  }

  @Override
  public JPanel getTabPanel() {
    return listPanel;
  }

  @Override
  public String getTabTitle() {
    return BUNDLE.getString("tmm.moviesets"); //$NON-NLS-1$
  }

  @Override
  public JPanel getDetailPanel() {
    return detailPanel;
  }

  @Override
  public Action getSearchAction() {
    return actionSearchMovieSet;
  }

  @Override
  public JPopupMenu getSearchMenu() {
    return null;
  }

  @Override
  public Action getEditAction() {
    return actionEditMovieSet;
  }

  @Override
  public JPopupMenu getEditMenu() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Action getUpdateAction() {
    return null;
  }

  @Override
  public JPopupMenu getUpdateMenu() {
    return null;
  }

  @Override
  public Action getExportAction() {
    return null;
  }

  public MovieSetSelectionModel getSelectionModel() {
    return selectionModel;
  }

  @Override
  public TmmSettingsNode getSettingsNode() {
    return null;
  }

  public void setSelectedMovieSet(MovieSet movieSet) {
    selectionModel.setSelectedMovieSet(movieSet);
    CardLayout cl = (CardLayout) (detailPanel.getLayout());
    cl.show(detailPanel, "movieSet");
  }

  public void setSelectedMovie(Movie movie) {
    movieSelectionModel.setSelectedMovie(movie);
    CardLayout cl = (CardLayout) (detailPanel.getLayout());
    cl.show(detailPanel, "movie");
  }
}
