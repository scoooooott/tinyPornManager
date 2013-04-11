package org.tinymediamanager.ui.movies;

import java.net.URL;

import javax.swing.ImageIcon;

import org.netbeans.swing.etable.ETable;
import org.netbeans.swing.etable.ETableColumn;
import org.netbeans.swing.etable.ETableColumnModel;
import org.netbeans.swing.etable.TableColumnSelector;
import org.netbeans.swing.etable.TableColumnSelector.TreeNode;
import org.tinymediamanager.ui.BorderCellRenderer;
import org.tinymediamanager.ui.IconRenderer;
import org.tinymediamanager.ui.MainWindow;

public class MovieTableColumnModel extends ETableColumnModel implements TableColumnSelector.TreeNode {

  private Column[] columns;

  public MovieTableColumnModel(ETable table) {
    Column movieName = new Column(0, table);
    movieName.setHidingAllowed(false);
    movieName.setHeaderValue(table.getModel().getColumnName(0));
    movieName.setCellRenderer(new BorderCellRenderer());
    addColumn(movieName);

    Column year = new Column(1, table);
    year.setHeaderValue(table.getModel().getColumnName(1));
    year.setPreferredWidth(35);
    year.setMinWidth(35);
    year.setMaxWidth(50);
    addColumn(year);

    Column nfo = new Column(2, table);
    nfo.setHeaderRenderer(new IconRenderer("NFO"));
    nfo.setMaxWidth(20);
    URL imageURL = MainWindow.class.getResource("images/File.png");
    if (imageURL != null) {
      nfo.setHeaderValue(new ImageIcon(imageURL));
    }
    addColumn(nfo);

    Column image = new Column(3, table);
    image.setHeaderRenderer(new IconRenderer("Images"));
    image.setMaxWidth(20);
    imageURL = null;
    imageURL = MainWindow.class.getResource("images/Image.png");
    if (imageURL != null) {
      image.setHeaderValue(new ImageIcon(imageURL));
    }
    addColumn(image);

    Column trailer = new Column(4, table);
    trailer.setHeaderRenderer(new IconRenderer("Trailer"));
    trailer.setMaxWidth(20);
    imageURL = null;
    imageURL = MainWindow.class.getResource("images/ClapBoard.png");
    if (imageURL != null) {
      trailer.setHeaderValue(new ImageIcon(imageURL));
    }
    addColumn(trailer);

    Column subtitle = new Column(5, table);
    subtitle.setHeaderRenderer(new IconRenderer("Subtitles"));
    subtitle.setMaxWidth(20);
    imageURL = null;
    imageURL = MainWindow.class.getResource("images/subtitle.png");
    if (imageURL != null) {
      subtitle.setHeaderValue(new ImageIcon(imageURL));
    }
    addColumn(subtitle);

    this.columns = new Column[6];
    columns[0] = movieName;
    columns[1] = year;
    columns[2] = nfo;
    columns[3] = image;
    columns[4] = trailer;
    columns[5] = subtitle;
  }

  @Override
  public String getText() {
    return "";
  }

  @Override
  public boolean isLeaf() {
    return false;
  }

  @Override
  public TreeNode[] getChildren() {
    return columns;

  }

  public class Column extends ETableColumn implements TableColumnSelector.TreeNode {

    private static final long serialVersionUID = 7685487733470143317L;
    private boolean           hidingAllowed    = true;
    private boolean           sortingAllowed   = true;

    public Column(int modelindex, ETable table) {
      super(modelindex, table);
    }

    @Override
    public boolean isHidingAllowed() {
      return hidingAllowed;
    }

    public void setHidingAllowed(boolean hidingAllowed) {
      this.hidingAllowed = hidingAllowed;
    }

    @Override
    public boolean isSortingAllowed() {
      return sortingAllowed;
    }

    public void setSortingAllowed(boolean sortingAllowed) {
      this.sortingAllowed = sortingAllowed;
    }

    @Override
    public TreeNode[] getChildren() {
      return null;
    }

    @Override
    public String getText() {
      return (String) getHeaderValue();
    }

    @Override
    public boolean isLeaf() {
      return true;
    }
  }
}
