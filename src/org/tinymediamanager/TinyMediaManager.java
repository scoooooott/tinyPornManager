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
					Globals.settings.setProxy();

					MainWindow window = new MainWindow();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});

	}

}
