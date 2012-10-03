package org.tinymediamanager.ui;

import javax.swing.ImageIcon;

import org.jdesktop.beansbinding.Converter;

public class ImageIconConverter extends Converter {

	private final static ImageIcon checkIcon = new ImageIcon(
			MoviePanel.class.getResource("images/Checkmark.png"));
	private final static ImageIcon crossIcon = new ImageIcon(
			MoviePanel.class.getResource("images/Cross.png"));

	@Override
	public Object convertForward(Object arg0) {
		if (arg0 instanceof Boolean && arg0 == Boolean.TRUE) {
			return checkIcon;
		}

		return crossIcon;
	}

	@Override
	public Object convertReverse(Object arg0) {
		// TODO Auto-generated method stub
		return null;
	}

}
