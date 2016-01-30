package org.tinymediamanager.ui.components;

import java.awt.Frame;
import java.awt.Graphics;
import java.awt.GraphicsConfiguration;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.UIResource;

import org.tinymediamanager.ui.MainWindow;

import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;
import com.jtattoo.plaf.BaseRootPaneUI;
import com.jtattoo.plaf.BaseTitleButton;
import com.jtattoo.plaf.DecorationHelper;
import com.jtattoo.plaf.JTattooUtilities;

/**
 * 
 * @author manuel
 *
 *         // logic taken from the laf JTattoo - BaseTitlePane.java
 */
public class TmmWindowDecorationPanel extends JPanel {
  private static final long      serialVersionUID = 2723090799541794214L;

  private Action                 closeAction;
  private Action                 iconifyAction;
  private Action                 restoreAction;
  private Action                 maximizeAction;
  private JButton                iconifyButton;
  private JButton                maxButton;
  private JButton                closeButton;
  private Icon                   iconifyIcon;
  private Icon                   maximizeIcon;
  private Icon                   minimizeIcon;
  private Icon                   closeIcon;
  private Window                 window;
  private WindowListener         windowListener;
  private PropertyChangeListener propertyChangeListener;

  // This flag is used to avoid a bug with OSX and java 1.7. The call to setExtendedState
  // with both flags ICONIFY and MAXIMIZED_BOTH throws an illegal state exception, so we
  // have to switch off the MAXIMIZED_BOTH flag in the iconify() method. If frame is deiconified
  // we use the wasMaximized flag to restore the maximized state.
  private int                    state;
  // This flag indicates a maximize error. This occurs on multiscreen environments where the first
  // screen does not have the same resolution as the second screen. In this case we only simulate the
  // maximize/restore behaviour. It's not a perfect simulation (frame border will stay visible,
  // and we have to restore the bounds if look and feel changes in maximized state)
  private boolean                wasMaximized;
  private boolean                wasMaximizeError = false;

  public TmmWindowDecorationPanel() {
    setOpaque(false);
    state = -1;
    wasMaximized = false;

    iconifyIcon = UIManager.getIcon("InternalFrame.iconifyIcon");
    maximizeIcon = UIManager.getIcon("InternalFrame.maximizeIcon");
    minimizeIcon = UIManager.getIcon("InternalFrame.minimizeIcon");
    closeIcon = UIManager.getIcon("InternalFrame.closeIcon");

    createActions();
    createButtons();
    setLayout(new FormLayout(new ColumnSpec[] { ColumnSpec.decode("center:12dlu"), }, new RowSpec[] { RowSpec.decode("12dlu"),
        FormFactory.GLUE_ROWSPEC, RowSpec.decode("12dlu"), FormFactory.GLUE_ROWSPEC, RowSpec.decode("12dlu"), FormFactory.NARROW_LINE_GAP_ROWSPEC, }));

    add(closeButton, "1, 1, center, center");
    add(maxButton, "1, 3, center, center");
    add(iconifyButton, "1, 5, center, center");
  }

  public void iconify() {
    Frame frame = getFrame();
    if (frame != null) {
      if (JTattooUtilities.isMac() && JTattooUtilities.getJavaVersion() >= 1.7) {
        // Workaround to avoid a bug within OSX and Java 1.7
        DecorationHelper.setExtendedState(frame, state & ~BaseRootPaneUI.MAXIMIZED_BOTH | Frame.ICONIFIED);
      }
      else {
        DecorationHelper.setExtendedState(frame, state | Frame.ICONIFIED);
      }
    }
  }

  public void maximize() {
    Frame frame = getFrame();
    if (frame != null) {
      validateMaximizedBounds();
      PropertyChangeListener[] pcl = frame.getPropertyChangeListeners();
      for (int i = 0; i < pcl.length; i++) {
        pcl[i].propertyChange(new PropertyChangeEvent(this, "windowMaximize", Boolean.FALSE, Boolean.FALSE));
      }
      DecorationHelper.setExtendedState(frame, state | BaseRootPaneUI.MAXIMIZED_BOTH);
      for (int i = 0; i < pcl.length; i++) {
        pcl[i].propertyChange(new PropertyChangeEvent(this, "windowMaximized", Boolean.FALSE, Boolean.FALSE));
      }

    }
  }

  public void restore() {
    Frame frame = getFrame();
    if (frame != null) {
      wasMaximizeError = false;
      PropertyChangeListener[] pcl = frame.getPropertyChangeListeners();
      for (int i = 0; i < pcl.length; i++) {
        pcl[i].propertyChange(new PropertyChangeEvent(this, "windowRestore", Boolean.FALSE, Boolean.FALSE));
      }
      if ((state & Frame.ICONIFIED) != 0) {
        DecorationHelper.setExtendedState(frame, state & ~Frame.ICONIFIED);
      }
      else {
        DecorationHelper.setExtendedState(frame, state & ~BaseRootPaneUI.MAXIMIZED_BOTH);
      }
      for (int i = 0; i < pcl.length; i++) {
        pcl[i].propertyChange(new PropertyChangeEvent(this, "windowRestored", Boolean.FALSE, Boolean.FALSE));
      }
    }
  }

  public void close() {
    if (window != null) {
      window.dispatchEvent(new WindowEvent(window, WindowEvent.WINDOW_CLOSING));
    }
  }

  private Frame getFrame() {
    return MainWindow.getActiveInstance();
  }

  private void validateMaximizedBounds() {
    Frame frame = getFrame();
    if (frame != null && !wasMaximizeError) {
      GraphicsConfiguration gc = frame.getGraphicsConfiguration();
      Insets screenInsets = Toolkit.getDefaultToolkit().getScreenInsets(gc);
      Rectangle maxBounds = gc.getBounds();
      maxBounds.x = Math.max(0, screenInsets.left);
      maxBounds.y = Math.max(0, screenInsets.top);
      maxBounds.width -= (screenInsets.left + screenInsets.right);
      maxBounds.height -= (screenInsets.top + screenInsets.bottom);
      frame.setMaximizedBounds(maxBounds);
    }
  }

  private void createActions() {
    closeAction = new CloseAction();
    iconifyAction = new IconifyAction();
    restoreAction = new RestoreAction();
    maximizeAction = new MaximizeAction();
  }

  public void createButtons() {
    closeButton = new BaseTitleButton(closeAction, "Close", closeIcon, 1.0f);
    closeButton.setBorder(new EmptyBorder(0, 0, 0, 0));
    maxButton = new BaseTitleButton(restoreAction, "Maximize", maximizeIcon, 1.0f);
    maxButton.setBorder(new EmptyBorder(0, 0, 0, 0));
    iconifyButton = new BaseTitleButton(iconifyAction, "Iconify", iconifyIcon, 1.0f);
    iconifyButton.setBorder(new EmptyBorder(0, 0, 0, 0));
  }

  @Override
  public void addNotify() {
    super.addNotify();
    uninstallListeners();
    window = SwingUtilities.getWindowAncestor(this);
    if (window != null) {
      if (window instanceof Frame) {
        setState(DecorationHelper.getExtendedState((Frame) window));
      }
      else {
        setState(0);
      }
      setActive(JTattooUtilities.isWindowActive(window));
      installListeners();
    }
  }

  @Override
  public void removeNotify() {
    super.removeNotify();
    uninstallListeners();
    window = null;
  }

  private void installListeners() {
    if (window != null) {
      windowListener = createWindowListener();
      window.addWindowListener(windowListener);
      propertyChangeListener = createWindowPropertyChangeListener();
      window.addPropertyChangeListener(propertyChangeListener);
    }
  }

  private void uninstallListeners() {
    if (window != null) {
      window.removeWindowListener(windowListener);
      window.removePropertyChangeListener(propertyChangeListener);
    }
  }

  private WindowListener createWindowListener() {
    return new WindowHandler();
  }

  private PropertyChangeListener createWindowPropertyChangeListener() {
    return new PropertyChangeHandler();
  }

  private void setActive(boolean flag) {
    if (getWindowDecorationStyle() == BaseRootPaneUI.FRAME) {
      Boolean active = flag ? Boolean.TRUE : Boolean.FALSE;
      iconifyButton.putClientProperty("paintActive", active);
      closeButton.putClientProperty("paintActive", active);
      maxButton.putClientProperty("paintActive", active);
    }
    getRootPane().repaint();
  }

  private int getWindowDecorationStyle() {
    return DecorationHelper.getWindowDecorationStyle(getRootPane());
  }

  private void setState(int state) {
    setState(state, false);
  }

  private void setState(int state, boolean updateRegardless) {
    if (window != null && getWindowDecorationStyle() == BaseRootPaneUI.FRAME) {
      if (this.state == state && !updateRegardless) {
        return;
      }

      final Frame frame = getFrame();
      if (frame != null) {

        if (((state & BaseRootPaneUI.MAXIMIZED_BOTH) != 0)
            && (getRootPane().getBorder() == null || (getRootPane().getBorder() instanceof UIResource)) && frame.isShowing()) {
          getRootPane().setBorder(null);
        }

        if (frame.isResizable()) {
          if ((state & BaseRootPaneUI.MAXIMIZED_BOTH) != 0) {
            updateMaxButton(restoreAction, minimizeIcon);
            maximizeAction.setEnabled(false);
            restoreAction.setEnabled(true);
          }
          else {
            updateMaxButton(maximizeAction, maximizeIcon);
            maximizeAction.setEnabled(true);
            restoreAction.setEnabled(false);
          }
          if (maxButton.getParent() == null || iconifyButton.getParent() == null) {
            add(maxButton);
            add(iconifyButton);
            revalidate();
            repaint();
          }
          maxButton.setText(null);
        }
        else {
          maximizeAction.setEnabled(false);
          restoreAction.setEnabled(false);
          if (maxButton.getParent() != null) {
            remove(maxButton);
            revalidate();
            repaint();
          }
        }
        // BUGFIX
        // When programatically maximize a frame via setExtendedState in a multiscreen environment the width
        // and height may not be set correctly. We fix this issue here.
        if ((state & BaseRootPaneUI.MAXIMIZED_BOTH) != 0) {
          validateMaximizedBounds();
          getRootPane().setBorder(null);
          SwingUtilities.invokeLater(new Runnable() {
            public void run() {
              GraphicsConfiguration gc = frame.getGraphicsConfiguration();
              Insets screenInsets = Toolkit.getDefaultToolkit().getScreenInsets(gc);
              Rectangle maxBounds = gc.getBounds();
              maxBounds.width -= (screenInsets.left + screenInsets.right);
              maxBounds.height -= (screenInsets.top + screenInsets.bottom);
              if ((frame.getWidth() != maxBounds.width) || (frame.getHeight() != maxBounds.height)) {
                restore();
                wasMaximizeError = true;
                frame.setMaximizedBounds(null);
                maximize();
              }
            }
          });
        }
      }
      else {
        // Not contained in a Frame
        maximizeAction.setEnabled(false);
        restoreAction.setEnabled(false);
        iconifyAction.setEnabled(false);
        remove(maxButton);
        remove(iconifyButton);
        revalidate();
        repaint();
      }
      closeAction.setEnabled(true);
      this.state = state;
    }
  }

  private void updateMaxButton(Action action, Icon icon) {
    maxButton.setAction(action);
    maxButton.setIcon(icon);
  }

  @Override
  public void paintComponent(Graphics g) {
    super.paintComponent(g);
    if (getFrame() != null) {
      setState(DecorationHelper.getExtendedState(getFrame()));
    }

  }

  /*****************************************************************************************
   * helper classes
   ****************************************************************************************/
  private class CloseAction extends AbstractAction {
    private static final long serialVersionUID = 3463634837846413651L;

    public CloseAction() {
      super(UIManager.getString("MetalTitlePane.closeTitle"));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      close();
    }
  }

  private class IconifyAction extends AbstractAction {
    private static final long serialVersionUID = 7682300139513319195L;

    public IconifyAction() {
      super(UIManager.getString("MetalTitlePane.iconifyTitle"));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      iconify();
    }
  }

  private class RestoreAction extends AbstractAction {
    private static final long serialVersionUID = 1080414069482732579L;

    public RestoreAction() {
      super(UIManager.getString("MetalTitlePane.restoreTitle"));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      restore();
    }
  }

  private class MaximizeAction extends AbstractAction {
    private static final long serialVersionUID = -1988007147862446193L;

    public MaximizeAction() {
      super(UIManager.getString("MetalTitlePane.maximizeTitle"));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      maximize();
    }
  }

  private class PropertyChangeHandler implements PropertyChangeListener {
    @Override
    public void propertyChange(PropertyChangeEvent pce) {
      String name = pce.getPropertyName();
      // Frame.state isn't currently bound.
      if ("resizable".equals(name) || "state".equals(name)) {
        Frame frame = getFrame();
        if (frame != null) {
          setState(DecorationHelper.getExtendedState(frame), true);
        }
        if ("resizable".equals(name)) {
          getRootPane().repaint();
        }
      }
      else if ("title".equals(name)) {
        repaint();
      }
      else if ("componentOrientation".equals(name)) {
        revalidate();
        repaint();
      }

      if ("windowRestored".equals(name)) {
        wasMaximized = false;
      }
      else if ("windowMaximized".equals(name)) {
        wasMaximized = true;
      }
    }
  }

  private class WindowHandler extends WindowAdapter {
    @Override
    public void windowDeiconified(WindowEvent e) {
      if (JTattooUtilities.isMac() && JTattooUtilities.getJavaVersion() >= 1.7 && wasMaximized) {
        SwingUtilities.invokeLater(new Runnable() {

          @Override
          public void run() {
            maximize();
          }
        });
      }
    }

    @Override
    public void windowActivated(WindowEvent ev) {
      setActive(true);
    }

    @Override
    public void windowDeactivated(WindowEvent ev) {
      setActive(false);
    }
  }
}
