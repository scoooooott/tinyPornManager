package org.tinymediamanager.ui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;

import org.tinymediamanager.core.movie.Movie;

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
	private JTextField tfDirector;
	private JTable table;

	/**
	 * Create the dialog.
	 */
	public MovieEditor(Movie movie) {
		movieToEdit = movie;
		setBounds(100, 100, 746, 640);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel
				.setLayout(new FormLayout(new ColumnSpec[] {
						FormFactory.RELATED_GAP_COLSPEC,
						ColumnSpec.decode("max(40dlu;default)"),
						FormFactory.RELATED_GAP_COLSPEC,
						ColumnSpec.decode("max(200px;default):grow"),
						FormFactory.RELATED_GAP_COLSPEC,
						FormFactory.DEFAULT_COLSPEC,
						FormFactory.RELATED_GAP_COLSPEC,
						ColumnSpec.decode("max(200px;default)"), },
						new RowSpec[] { FormFactory.RELATED_GAP_ROWSPEC,
								FormFactory.DEFAULT_ROWSPEC,
								FormFactory.RELATED_GAP_ROWSPEC,
								FormFactory.DEFAULT_ROWSPEC,
								FormFactory.RELATED_GAP_ROWSPEC,
								FormFactory.DEFAULT_ROWSPEC,
								FormFactory.RELATED_GAP_ROWSPEC,
								RowSpec.decode("max(75px;default)"),
								FormFactory.RELATED_GAP_ROWSPEC,
								FormFactory.DEFAULT_ROWSPEC,
								FormFactory.RELATED_GAP_ROWSPEC,
								FormFactory.DEFAULT_ROWSPEC,
								FormFactory.RELATED_GAP_ROWSPEC,
								FormFactory.DEFAULT_ROWSPEC,
								FormFactory.RELATED_GAP_ROWSPEC,
								FormFactory.DEFAULT_ROWSPEC, }));
		{
			JLabel lblTitle = new JLabel("Title");
			contentPanel.add(lblTitle, "2, 2, right, default");
		}
		{
			tfTitle = new JTextField();
			contentPanel.add(tfTitle, "4, 2, fill, default");
			tfTitle.setColumns(10);
		}
		{
			JLabel lblPoster = new JLabel("");
			contentPanel.add(lblPoster, "8, 2, 1, 7, right, default");
		}
		{
			JLabel lblOriginalTitle = new JLabel("Originaltitle");
			contentPanel.add(lblOriginalTitle, "2, 4, right, default");
		}
		{
			tfOriginalTitle = new JTextField();
			contentPanel.add(tfOriginalTitle, "4, 4, fill, top");
			tfOriginalTitle.setColumns(10);
		}
		{
			JLabel lblYear = new JLabel("Year");
			contentPanel.add(lblYear, "2, 6, right, default");
		}
		{
			tfYear = new JTextField();
			contentPanel.add(tfYear, "4, 6, fill, top");
			tfYear.setColumns(10);
		}
		{
			JLabel lblPlot = new JLabel("Plot");
			contentPanel.add(lblPlot, "2, 8, right, top");
		}
		{
			JTextPane tpPlot = new JTextPane();
			tpPlot.setBorder(UIManager.getBorder("TextField.border"));
			contentPanel.add(tpPlot, "4, 8, fill, fill");
		}
		{
			JLabel lblDirector = new JLabel("Director");
			contentPanel.add(lblDirector, "2, 10, right, default");
		}
		{
			tfDirector = new JTextField();
			contentPanel.add(tfDirector, "4, 10, fill, top");
			tfDirector.setColumns(10);
		}
		{
			JLabel lblActors = new JLabel("Actors");
			contentPanel.add(lblActors, "2, 12");
		}
		{
			table = new JTable();
			table.setBorder(UIManager.getBorder("TextField.border"));
			contentPanel.add(table, "4, 12, 1, 5, fill, fill");
		}
		{
			JButton btnAddActor = new JButton("Add Actor");
			contentPanel.add(btnAddActor, "6, 12");
		}
		{
			JLabel lblFanart = new JLabel("");
			contentPanel.add(lblFanart, "8, 12, 1, 5, fill, fill");
		}
		{
			JButton btnRemoveActor = new JButton("Remove Actor");
			contentPanel.add(btnRemoveActor, "6, 14");
		}
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				JButton okButton = new JButton("OK");
				okButton.setActionCommand("OK");
				buttonPane.add(okButton);
				getRootPane().setDefaultButton(okButton);
			}
			{
				JButton cancelButton = new JButton("Cancel");
				cancelButton.setActionCommand("Cancel");
				buttonPane.add(cancelButton);
			}
		}
	}

}
