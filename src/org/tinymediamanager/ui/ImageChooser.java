package org.tinymediamanager.ui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;

import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

public class ImageChooser extends JDialog {

	public enum ImageType {
		POSTER, FANART
	}

	private final JPanel contentPanel = new JPanel();
	private JProgressBar progressBar;
	private JLabel lblProgressAction;

	private String imageUrl;
	private ImageType type;

	private final Action actionOK = new SwingAction();
	private final Action actionCancel = new SwingAction_1();

	/**
	 * Create the dialog.
	 */
	public ImageChooser(String imdbId, String tmdbId, String imageUrl,
			ImageType type) {
		this.imageUrl = imageUrl;
		this.type = type;

		setBounds(100, 100, 450, 300);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(new FormLayout(new ColumnSpec[] {},
				new RowSpec[] {}));
		{
			JPanel buttonPane = new JPanel();
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			buttonPane.setLayout(new FormLayout(new ColumnSpec[] {
					FormFactory.RELATED_GAP_COLSPEC,
					ColumnSpec.decode("100px"),
					FormFactory.RELATED_GAP_COLSPEC,
					ColumnSpec.decode("default:grow"),
					FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("47px"),
					FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
					ColumnSpec.decode("65px"), }, new RowSpec[] {
					FormFactory.LINE_GAP_ROWSPEC, RowSpec.decode("23px"), }));
			{
				progressBar = new JProgressBar();
				buttonPane.add(progressBar, "2, 2");
			}
			{
				lblProgressAction = new JLabel("");
				buttonPane.add(lblProgressAction, "4, 2");
			}
			{
				JButton okButton = new JButton("OK");
				okButton.setAction(actionOK);
				okButton.setActionCommand("OK");
				buttonPane.add(okButton, "6, 2, left, top");
				getRootPane().setDefaultButton(okButton);
			}
			{
				JButton cancelButton = new JButton("Cancel");
				cancelButton.setAction(actionCancel);
				cancelButton.setActionCommand("Cancel");
				buttonPane.add(cancelButton, "8, 2, left, top");
			}
		}
	}

	private class SwingAction extends AbstractAction {
		public SwingAction() {
			putValue(NAME, "OK");
			putValue(SHORT_DESCRIPTION, "Set selected image");
		}

		public void actionPerformed(ActionEvent e) {
			setVisible(true);
		}
	}

	private void startProgressBar(String description) {
		lblProgressAction.setText(description);
		progressBar.setVisible(true);
		progressBar.setIndeterminate(true);
	}

	private void stopProgressBar() {
		lblProgressAction.setText("");
		progressBar.setVisible(false);
		progressBar.setIndeterminate(false);
	}

	private void addImage(ImageLabel image, String description) {

	}

	private class SwingAction_1 extends AbstractAction {
		public SwingAction_1() {
			putValue(NAME, "Cancel");
			putValue(SHORT_DESCRIPTION, "Cancel");
		}

		public void actionPerformed(ActionEvent e) {
			setVisible(false);
		}
	}

	private class DownloadTask extends SwingWorker<Void, Void> {

		private String imdbId;
		private String tmdbId;

		public DownloadTask(String imdbId, String tmdbId) {
			this.imdbId = imdbId;
			this.tmdbId = tmdbId;
		}

		@Override
		public Void doInBackground() {
			startProgressBar("Downloading images");

			return null;
		}

		/*
		 * Executed in event dispatching thread
		 */
		@Override
		public void done() {
			stopProgressBar();
		}
	}
}
