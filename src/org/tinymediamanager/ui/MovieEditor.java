package org.tinymediamanager.ui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;

import org.jdesktop.beansbinding.AutoBinding.UpdateStrategy;
import org.jdesktop.beansbinding.BeanProperty;
import org.jdesktop.observablecollections.ObservableCollections;
import org.jdesktop.swingbinding.JTableBinding;
import org.jdesktop.swingbinding.SwingBindings;
import org.tinymediamanager.core.movie.Movie;
import org.tinymediamanager.core.movie.MovieCast;

import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

public class MovieEditor extends JDialog {

	private final JPanel contentPanel = new JPanel();
	private Movie movieToEdit;
	private JTextField tfTitle;
	private JTextField tfOriginalTitle;
	private JTextField tfYear;
	private JTextPane tpPlot;
	private JTextField tfDirector;
	private JTable table;
	private JLabel lblMoviePath;
	private ImageLabel lblPoster;
	private String posterUrl;
	private ImageLabel lblFanart;
	private String fanartUrl;

	private List<MovieCast> cast = ObservableCollections
			.observableList(new ArrayList<MovieCast>());
	private final Action actionOK = new SwingAction();
	private final Action actionCancel = new SwingAction_1();

	/**
	 * Create the dialog.
	 */
	public MovieEditor(Movie movie) {
		setModal(true);
		setTitle("Change Movie");
		movieToEdit = movie;
		setBounds(100, 100, 944, 543);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel
				.setLayout(new FormLayout(new ColumnSpec[] {
						FormFactory.RELATED_GAP_COLSPEC,
						ColumnSpec.decode("max(40dlu;default)"),
						FormFactory.RELATED_GAP_COLSPEC,
						ColumnSpec.decode("400px:grow"),
						FormFactory.RELATED_GAP_COLSPEC,
						ColumnSpec.decode("50dlu"),
						FormFactory.RELATED_GAP_COLSPEC,
						ColumnSpec.decode("250px:grow"), }, new RowSpec[] {
						FormFactory.RELATED_GAP_ROWSPEC,
						FormFactory.DEFAULT_ROWSPEC,
						FormFactory.RELATED_GAP_ROWSPEC,
						FormFactory.DEFAULT_ROWSPEC,
						FormFactory.RELATED_GAP_ROWSPEC,
						FormFactory.DEFAULT_ROWSPEC,
						FormFactory.RELATED_GAP_ROWSPEC,
						FormFactory.DEFAULT_ROWSPEC,
						FormFactory.RELATED_GAP_ROWSPEC,
						RowSpec.decode("fill:max(150px;default)"),
						FormFactory.RELATED_GAP_ROWSPEC,
						FormFactory.DEFAULT_ROWSPEC,
						FormFactory.RELATED_GAP_ROWSPEC,
						FormFactory.DEFAULT_ROWSPEC,
						FormFactory.RELATED_GAP_ROWSPEC,
						FormFactory.DEFAULT_ROWSPEC,
						FormFactory.RELATED_GAP_ROWSPEC,
						RowSpec.decode("75px"),
						FormFactory.RELATED_GAP_ROWSPEC,
						FormFactory.DEFAULT_ROWSPEC, }));
		{
			lblMoviePath = new JLabel("");
			contentPanel.add(lblMoviePath, "2, 2, 7, 1");
		}
		{
			JLabel lblTitle = new JLabel("Title");
			contentPanel.add(lblTitle, "2, 4, right, default");
		}
		{
			tfTitle = new JTextField();
			contentPanel.add(tfTitle, "4, 4, fill, default");
			tfTitle.setColumns(10);
		}
		{
			// JLabel lblPoster = new JLabel("");
			lblPoster = new ImageLabel();
			contentPanel.add(lblPoster, "8, 4, 1, 7, fill, fill");
		}
		{
			JLabel lblOriginalTitle = new JLabel("Originaltitle");
			contentPanel.add(lblOriginalTitle, "2, 6, right, default");
		}
		{
			tfOriginalTitle = new JTextField();
			contentPanel.add(tfOriginalTitle, "4, 6, fill, top");
			tfOriginalTitle.setColumns(10);
		}
		{
			JLabel lblYear = new JLabel("Year");
			contentPanel.add(lblYear, "2, 8, right, default");
		}
		{
			tfYear = new JTextField();
			contentPanel.add(tfYear, "4, 8, fill, top");
			tfYear.setColumns(10);
		}
		{
			JLabel lblPlot = new JLabel("Plot");
			contentPanel.add(lblPlot, "2, 10, right, top");
		}
		{
			JScrollPane scrollPane = new JScrollPane();
			contentPanel.add(scrollPane, "4, 10, fill, fill");
			{
				tpPlot = new JTextPane();
				scrollPane.setViewportView(tpPlot);
			}
		}
		{
			JLabel lblDirector = new JLabel("Director");
			contentPanel.add(lblDirector, "2, 12, right, default");
		}
		{
			tfDirector = new JTextField();
			contentPanel.add(tfDirector, "4, 12, fill, top");
			tfDirector.setColumns(10);
		}
		{
			JButton btnChp = new JButton("CHP");
			contentPanel.add(btnChp, "8, 12, left, default");
		}
		{
			JLabel lblActors = new JLabel("Actors");
			contentPanel.add(lblActors, "2, 14, right, default");
		}
		{
			JScrollPane scrollPane = new JScrollPane();
			contentPanel.add(scrollPane, "4, 14, 1, 5, fill, fill");
			{
				table = new JTable();
				scrollPane.setViewportView(table);
				table.setBorder(UIManager.getBorder("TextField.border"));
			}
		}
		{
			JButton btnAddActor = new JButton("Add Actor");
			contentPanel.add(btnAddActor, "6, 14");
		}
		{
			// JLabel lblFanart = new JLabel("");
			lblFanart = new ImageLabel();
			contentPanel.add(lblFanart, "8, 14, 1, 5, fill, fill");
		}
		{
			JButton btnRemoveActor = new JButton("Remove Actor");
			contentPanel.add(btnRemoveActor, "6, 16");
		}
		{
			JButton btnChf = new JButton("CHF");
			contentPanel.add(btnChf, "8, 20, left, default");
		}
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				JButton okButton = new JButton("OK");
				okButton.setAction(actionOK);
				okButton.setActionCommand("OK");
				buttonPane.add(okButton);
				getRootPane().setDefaultButton(okButton);
			}
			{
				JButton cancelButton = new JButton("Cancel");
				cancelButton.setAction(actionCancel);
				cancelButton.setActionCommand("Cancel");
				buttonPane.add(cancelButton);
			}
		}

		{
			lblMoviePath.setText(movie.getPath() + File.separator
					+ movie.getMovieFiles().get(0));
			tfTitle.setText(movie.getName());
			tfOriginalTitle.setText(movie.getOriginalName());
			tfYear.setText(movie.getYear());
			tpPlot.setText(movie.getOverview());
			tfDirector.setText(movie.getDirector());
			lblPoster.setImagePath(movie.getPoster());
			lblFanart.setImagePath(movie.getFanart());

			for (MovieCast origCast : movie.getActors()) {
				MovieCast actor = new MovieCast();
				actor.setName(origCast.getName());
				actor.setType(origCast.getType());
				actor.setCharacter(origCast.getCharacter());
				cast.add(actor);
			}
		}
		initDataBindings();
	}

	protected void initDataBindings() {
		JTableBinding<MovieCast, List<MovieCast>, JTable> jTableBinding = SwingBindings
				.createJTableBinding(UpdateStrategy.READ, cast, table);
		//
		BeanProperty<MovieCast, String> movieCastBeanProperty = BeanProperty
				.create("name");
		jTableBinding.addColumnBinding(movieCastBeanProperty)
				.setColumnName("Name").setEditable(false);
		//
		BeanProperty<MovieCast, String> movieCastBeanProperty_1 = BeanProperty
				.create("character");
		jTableBinding.addColumnBinding(movieCastBeanProperty_1)
				.setColumnName("Role").setEditable(false);
		//
		jTableBinding.setEditable(false);
		jTableBinding.bind();
	}

	private class SwingAction extends AbstractAction {
		public SwingAction() {
			putValue(NAME, "OK");
			putValue(SHORT_DESCRIPTION, "Change movie");
		}

		public void actionPerformed(ActionEvent e) {
			movieToEdit.setName(tfTitle.getText());
			movieToEdit.setOriginalName(tfOriginalTitle.getText());
			movieToEdit.setYear(tfYear.getText());
			setVisible(false);
		}
	}

	private class SwingAction_1 extends AbstractAction {
		public SwingAction_1() {
			putValue(NAME, "CANCEL");
			putValue(SHORT_DESCRIPTION, "Discard changes");
		}

		public void actionPerformed(ActionEvent e) {
			setVisible(false);
		}
	}
}
