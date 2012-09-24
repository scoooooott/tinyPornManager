package org.tinymediamanager;

import java.awt.EventQueue;

import javax.swing.UIManager;

import org.tinymediamanager.ui.MainWindow;

public class TinyMediaManager {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					// Get the native look and feel class name
					String nativeLF = UIManager.getSystemLookAndFeelClassName();

					// Install the look and feel
					UIManager.setLookAndFeel(nativeLF);

					// proxy settings
					if (Globals.settings.getProxyHost() != null
							&& !Globals.settings.getProxyHost().isEmpty()) {
						System.setProperty("proxyPort",
								Globals.settings.getProxyPort());
						System.setProperty("proxyHost",
								Globals.settings.getProxyHost());
						System.setProperty("http.proxyUser",
								Globals.settings.getProxyUsername());
						System.setProperty("http.proxyPassword",
								Globals.settings.getProxyPassword());
					}

					MainWindow window = new MainWindow();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});

	}

}
