package org.tinymediamanager.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingWorker;
import javax.swing.border.BevelBorder;
import javax.swing.border.SoftBevelBorder;

import org.tinymediamanager.scraper.MediaArt;
import org.tinymediamanager.scraper.MediaArtifactType;
import org.tinymediamanager.scraper.MediaMetadata;
import org.tinymediamanager.scraper.MediaMetadata.ArtworkSize;
import org.tinymediamanager.scraper.tmdb.TmdbMetadataProvider;

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
	private JPanel panelImages;

	private String imageUrl;
	private ImageType type;

	private ImageLabel markedImage = null;

	private final Action actionOK = new SwingAction();
	private final Action actionCancel = new SwingAction_1();

	/**
	 * Create the dialog.
	 */
	public ImageChooser(String imdbId, int tmdbId, String imageUrl,
			ImageType type) {
		setModal(true);
		this.imageUrl = imageUrl;
		this.type = type;

		setBounds(100, 100, 679, 452);
		getContentPane().setLayout(new BorderLayout());
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(new FormLayout(new ColumnSpec[] {
				FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
				ColumnSpec.decode("258px:grow"), }, new RowSpec[] {
				FormFactory.LINE_GAP_ROWSPEC,
				RowSpec.decode("fill:266px:grow"), }));
		{
			JScrollPane scrollPane = new JScrollPane();
			scrollPane
					.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
			contentPanel.add(scrollPane, "2, 2, fill, fill");
			{
				panelImages = new JPanel();
				scrollPane.setViewportView(panelImages);
				scrollPane.getVerticalScrollBar().setUnitIncrement(16);
				panelImages.setLayout(new WrapLayout(FlowLayout.LEFT));
			}
		}
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

		DownloadTask task = new DownloadTask(imdbId, tmdbId);
		task.execute();
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
		GridBagLayout gbl = new GridBagLayout();

		switch (type) {
		case FANART:
			gbl.columnWidths = new int[] { 300 };
			gbl.rowHeights = new int[] { 150 };
			break;

		case POSTER:
		default:
			gbl.columnWidths = new int[] { 150 };
			gbl.rowHeights = new int[] { 250 };
			break;

		}

		gbl.columnWeights = new double[] { Double.MIN_VALUE };
		gbl.rowWeights = new double[] { Double.MIN_VALUE };
		JPanel imagePanel = new JPanel();
		imagePanel.setLayout(gbl);

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.BOTH;
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.insets = new Insets(5, 5, 5, 5);

		image.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				ImageLabel image = (ImageLabel) arg0.getComponent();
				if (markedImage != null) {
					markedImage.setBorder(null);
					markedImage.setBackground(null);
				}
				image.setBorder(new SoftBevelBorder(BevelBorder.LOWERED, null,
						null, null, null));
				image.setBackground(Color.BLUE);
				markedImage = image;
			}
		});

		imagePanel.add(image, gbc);
		// imagePanel.add(new JLabel(description));

		panelImages.add(imagePanel);
		panelImages.validate();
		panelImages.getParent().validate();
		// contentPanel.add(image);

		// this.pack();
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
		private int tmdbId;

		public DownloadTask(String imdbId, int tmdbId) {
			this.imdbId = imdbId;
			this.tmdbId = tmdbId;
		}

		@Override
		public Void doInBackground() {
			startProgressBar("Downloading images");

			TmdbMetadataProvider tmdb = TmdbMetadataProvider.getInstance();
			try {
				MediaMetadata metadata = tmdb.getArtwork(tmdbId,
						ArtworkSize.SMALL);

				switch (type) {
				case POSTER:
					// poster
					List<MediaArt> art = metadata
							.getMediaArt(MediaArtifactType.POSTER);
					for (MediaArt poster : art) {
						ImageLabel image = new ImageLabel();
						image.setImageUrl(poster.getDownloadUrl());
						addImage(image, "");
					}
					break;

				case FANART:
					// fanart
					art = metadata.getMediaArt(MediaArtifactType.BACKGROUND);
					for (MediaArt poster : art) {
						ImageLabel image = new ImageLabel();
						image.setImageUrl(poster.getDownloadUrl());
						addImage(image, "");
					}
					break;
				}
			} catch (NumberFormatException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

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
