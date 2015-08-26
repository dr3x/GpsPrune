package tim.prune.gui;

import java.awt.Toolkit;
import java.awt.event.KeyEvent;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;

import tim.prune.DataSubscriber;
import tim.prune.GenericFunction;
import tim.prune.I18nManager;

public abstract class MenuManager implements DataSubscriber {

	/** Array of key events */
	private static final int[] KEY_EVENTS = {
		KeyEvent.VK_A, KeyEvent.VK_B, KeyEvent.VK_C, KeyEvent.VK_D, KeyEvent.VK_E,
		KeyEvent.VK_F, KeyEvent.VK_G, KeyEvent.VK_H, KeyEvent.VK_I, KeyEvent.VK_J,
		KeyEvent.VK_K, KeyEvent.VK_L, KeyEvent.VK_M, KeyEvent.VK_N, KeyEvent.VK_O,
		KeyEvent.VK_P, KeyEvent.VK_Q, KeyEvent.VK_R, KeyEvent.VK_S, KeyEvent.VK_T,
		KeyEvent.VK_U, KeyEvent.VK_V, KeyEvent.VK_W, KeyEvent.VK_X, KeyEvent.VK_Y, KeyEvent.VK_Z};
	
	public abstract JMenuBar createMenuBar();
	
	public abstract JToolBar createToolBar();
	
	public abstract void informFileLoaded();
	
	

	/**
	 * Convenience method for making a menu item using a function
	 * @param inFunction function
	 * @param inEnabled flag to specify initial enabled state
	 * @return menu item using localized name of function
	 */
	protected static JMenuItem makeMenuItem(GenericFunction inFunction, boolean inEnabled)
	{
		JMenuItem item = makeMenuItem(inFunction);
		item.setEnabled(inEnabled);
		return item;
	}

	/**
	 * Convenience method for making a menu item using a function
	 * @param inFunction function
	 * @return menu item using localized name of function
	 */
	protected static JMenuItem makeMenuItem(GenericFunction inFunction)
	{
		JMenuItem item = new JMenuItem(I18nManager.getText(inFunction.getNameKey()));
		item.addActionListener(new FunctionLauncher(inFunction));
		return item;
	}

	/**
	 * Set the alt key for the given menu
	 * @param inMenu menu to set
	 * @param inKey key to lookup to get language-sensitive altkey
	 */
	protected static void setAltKey(JMenu inMenu, String inKey)
	{
		// Lookup the key in the properties
		String altKey = I18nManager.getText(inKey);
		if (altKey != null && altKey.length() == 1)
		{
			int code = altKey.charAt(0) - 'A';
			if (code >= 0 && code < 26)
			{
				// Found a valid code between A and Z
				inMenu.setMnemonic(KEY_EVENTS[code]);
			}
		}
	}

	/**
	 * Set the shortcut key for the given menu item
	 * @param inMenuItem menu item to set
	 * @param inKey key to lookup to get language-sensitive shortcut
	 */
	protected static void setShortcut(JMenuItem inMenuItem, String inKey)
	{
		// Lookup the key in the properties
		String altKey = I18nManager.getText(inKey);
		if (altKey != null && altKey.length() == 1)
		{
			int code = altKey.charAt(0) - 'A';
			if (code >= 0 && code < 26)
			{
				// Found a valid code between A and Z
				inMenuItem.setAccelerator(KeyStroke.getKeyStroke(KEY_EVENTS[code],
					Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
				// use platform-specific key mask so Ctrl on Linux/Win, Clover on Mac
			}
		}
	}
}
