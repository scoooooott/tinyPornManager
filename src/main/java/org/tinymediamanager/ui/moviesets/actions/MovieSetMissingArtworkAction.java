package org.tinymediamanager.ui.moviesets.actions;

import org.tinymediamanager.core.movie.entities.MovieSet;
import org.tinymediamanager.core.movie.tasks.MovieSetMissingArtworkDownloadTask;
import org.tinymediamanager.core.threading.TmmTaskManager;
import org.tinymediamanager.ui.IconManager;
import org.tinymediamanager.ui.MainWindow;
import org.tinymediamanager.ui.UTF8Control;
import org.tinymediamanager.ui.actions.TmmAction;
import org.tinymediamanager.ui.moviesets.MovieSetUIModule;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class MovieSetMissingArtworkAction extends TmmAction {

    private final static ResourceBundle BUNDLE = ResourceBundle.getBundle("messages", new UTF8Control());

    public MovieSetMissingArtworkAction() {
        putValue(NAME, BUNDLE.getString("movieset.downloadmissingartwork"));
        putValue(SMALL_ICON, IconManager.IMAGE);
        putValue(LARGE_ICON_KEY, IconManager.IMAGE);
    }


    @Override
    protected void processAction(ActionEvent e) {

        List<MovieSet> selectedMovieSets = new ArrayList<>(MovieSetUIModule.getInstance().getSelectionModel().getSelectedMovieSets());

        if (selectedMovieSets.isEmpty()) {
            JOptionPane.showMessageDialog(MainWindow.getActiveInstance(), BUNDLE.getString("tmm.nothingselected")); //$NON-NLS-1$
            return;
        }

        MovieSetMissingArtworkDownloadTask task = new MovieSetMissingArtworkDownloadTask(selectedMovieSets);
        TmmTaskManager.getInstance().addDownloadTask(task);

    }
}
