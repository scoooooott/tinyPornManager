package org.tinymediamanager.ui.movies.actions;

import org.tinymediamanager.core.movie.entities.Movie;
import org.tinymediamanager.ui.IconManager;
import org.tinymediamanager.ui.MainWindow;
import org.tinymediamanager.ui.UTF8Control;
import org.tinymediamanager.ui.actions.TmmAction;
import org.tinymediamanager.ui.movies.MovieUIModule;
import org.tinymediamanager.ui.movies.dialogs.MovieCleanUpDialog;
import javax.swing.JOptionPane;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class MovieCleanUpFilesAction extends TmmAction {

    private static final long           serialVersionUID = -2029243504238273721L;
    private static final ResourceBundle BUNDLE           = ResourceBundle.getBundle("messages", new UTF8Control()); //$NON-NLS-1$

    public MovieCleanUpFilesAction() {

        putValue(NAME, BUNDLE.getString("movie.cleanupfiles")); //$NON-NLS-1$
        putValue(SHORT_DESCRIPTION, BUNDLE.getString("movie.cleanupfiles.desc")); //$NON-NLS-1$
        putValue(SMALL_ICON, IconManager.DELETE);
        putValue(LARGE_ICON_KEY, IconManager.DELETE);

    }

    @Override
    protected void processAction(ActionEvent e) {

        List<Movie> selectedMovies = new ArrayList<>(MovieUIModule.getInstance().getSelectionModel().getSelectedMovies());

        if (selectedMovies.isEmpty()) {
            JOptionPane.showMessageDialog(MainWindow.getActiveInstance(), BUNDLE.getString("tmm.nothingselected")); //$NON-NLS-1$
            return;
        }

        MovieCleanUpDialog dialog = new MovieCleanUpDialog(selectedMovies);
        dialog.setVisible(true);
    }


}
