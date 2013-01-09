package org.tinymediamanager.ui.moviesets;

import java.util.ArrayList;
import java.util.List;

import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import org.tinymediamanager.core.movie.Movie;
import org.tinymediamanager.core.movie.MovieSet;

public class MovieSetTreeModel implements TreeModel {

  private DefaultMutableTreeNode  root      = new DefaultMutableTreeNode("MovieSets");

  private List<TreeModelListener> listeners = new ArrayList<TreeModelListener>();

  public MovieSetTreeModel(List<MovieSet> movieSets) {
    // build initial tree
    for (MovieSet movieSet : movieSets) {
      DefaultMutableTreeNode setNode = new MovieSetTreeNode(movieSet);
      for (Movie movie : movieSet.getMovies()) {
        DefaultMutableTreeNode movieNode = new MovieSetTreeNode(movie);
        setNode.add(movieNode);
      }
      root.add(setNode);
    }
  }

  @Override
  public Object getChild(Object parent, int index) {
    return ((TreeNode) parent).getChildAt(index);
  }

  @Override
  public Object getRoot() {
    return root;
  }

  public int getChildCount(Object parent) {
    return ((TreeNode) parent).getChildCount();
  }

  public boolean isLeaf(Object node) {
    return getChildCount(node) == 0;
  }

  public int getIndexOfChild(Object parent, Object child) {
    return ((TreeNode) parent).getIndex((TreeNode) child);
  }

  public void addTreeModelListener(TreeModelListener listener) {
    listeners.add(listener);
  }

  public void removeTreeModelListener(TreeModelListener listener) {
    listeners.remove(listener);
  }

  @Override
  public void valueForPathChanged(TreePath path, Object newValue) {
    // foo
  }

  // Fügt dem parent-Knoten (durch den TreePath gegeben) den
  // Child-Knoten hinzu
  public void addMovieSet(MutableTreeNode child) {
    // Den Knoten einbauen
    int index = root.getChildCount();
    root.add(child);

    // Die Listener unterrichten
    TreeModelEvent event = new TreeModelEvent(this, // Quelle des Events
        root.getPath(), // Pfad zum Vater des veränderten Knoten
        new int[] { index }, // Index des veränderten Knotens
        new Object[] { child }); // Der neue Knoten

    for (TreeModelListener listener : listeners)
      listener.treeNodesInserted(event);
  }

}
