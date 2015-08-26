package tim.prune.drawing.tool.property;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Point;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.AbstractButton;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JToolBar;
import javax.swing.JWindow;

import tim.prune.DrawApp;

public abstract class PopupPropertyTool extends PropertyTool implements ActionListener {

	private AbstractButton toolButton;
	private WindowHider windowHider;
	
	public PopupPropertyTool(DrawApp app, JWindow toolPopupWindow) {
		super(app, toolPopupWindow);
		toolButton = buildToolButton();
	}
	
	@Override
	public void addTools(JToolBar toolbar) {
		toolbar.add(toolButton);
	}
	
	public AbstractButton getToolButton() {
		return toolButton;
	}
	
	protected AbstractButton buildToolButton() {
		JButton toolButton = new JButton();
		toolButton.setBorderPainted(false);
		toolButton.addActionListener(this);
		return toolButton;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		JWindow toolPopupWindow = getToolPopupWindow();
		if( toolPopupWindow.isVisible() ) {
			hidePopup();
		} else {
			showPopup(getPopupContent());
		}
	}
	
	protected abstract JComponent getPopupContent();

	protected void showPopup(JComponent content) {
		JComponent toolbarItem = getToolButton();
		Point screenLoc = toolbarItem.getLocationOnScreen();
		screenLoc.y += toolbarItem.getHeight();
		
		final JWindow window = getToolPopupWindow();
		if( windowHider != null ) windowHider.dispose();
		windowHider = new WindowHider(window);
		
		Container contentPane = window.getContentPane();
		contentPane.removeAll();
		
		contentPane.setLayout(new BorderLayout());
		contentPane.add(content, BorderLayout.CENTER);
		
		window.setLocation(screenLoc);
		window.pack();
		window.setMinimumSize(new Dimension(100, 0));
		window.setVisible(true);
	}
	
	protected void hidePopup() {
		getToolPopupWindow().setVisible(false);
		if( windowHider != null ) {
			windowHider.dispose();
		}
		windowHider = null;
	}
	
	private static final class WindowHider implements MouseListener {
		private final Window window;
		private long requestTime;
		private boolean entered;
		
		public WindowHider(Window window) {
			this.window = window;
			window.addMouseListener(this);
		}
		
		public void dispose() {
			window.removeMouseListener(this);
			requestTime = -1;
		}
		
		@Override
		public void mouseReleased(MouseEvent e) {
		}
		
		@Override
		public void mousePressed(MouseEvent e) {
		}
		
		@Override
		public void mouseExited(MouseEvent e) {
			if( entered && !window.contains(e.getX(), e.getY())) {
				final long time = System.currentTimeMillis();
				requestTime = time;
				EventQueue.invokeLater(new Runnable() {
					private long timeToClose = System.currentTimeMillis() + 1000;
					@Override
					public void run() {
						if( requestTime == time && window.isVisible() ) {
							if( System.currentTimeMillis() >= timeToClose ) {
								window.setVisible(false);
							} else {
								EventQueue.invokeLater(this);
							}
						}
					}
				});
			}
		}
		@Override
		public void mouseEntered(MouseEvent e) {
			requestTime = -1;
			entered = true;				
		}
		@Override
		public void mouseClicked(MouseEvent e) {
		}
	}
}
