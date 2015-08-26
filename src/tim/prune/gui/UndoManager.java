package tim.prune.gui;

import java.awt.FlowLayout;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Stack;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import tim.prune.App;
import tim.prune.I18nManager;
import tim.prune.undo.UndoOperation;

/**
 * Class to manage the selection of actions to undo or redo
 */
abstract public class UndoManager
{
	private App _app;
	private JDialog _dialog;
	private JList _actionList;

	abstract protected Stack<UndoOperation> getStack();
	abstract protected void doActions(int inNumUndos);
	abstract protected String getDescriptionPrefix();

	/**
	 * Constructor
	 * @param inApp App object
	 * @param inFrame parent frame
	 */
	public UndoManager(App inApp, JFrame inFrame, String title, String pretext)
	{
		_app = inApp;
		_dialog = new JDialog(inFrame, title, true);
		_dialog.setLocationRelativeTo(inFrame);
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BorderLayout(3, 3));
		mainPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		Stack<UndoOperation> undoStack = getStack();
		mainPanel.add(new JLabel(pretext), BorderLayout.NORTH);

		String[] undoActions = new String[undoStack.size()];
		for (int i=0; i<undoStack.size(); i++)
		{
			undoActions[i] = getDescriptionPrefix() + undoStack.elementAt(undoStack.size()-1-i).getDescription();
		}
		_actionList = new JList(undoActions);
		_actionList.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
		_actionList.setSelectedIndex(0);
		_actionList.addListSelectionListener(new ListSelectionListener()
			{
				public void valueChanged(ListSelectionEvent e)
				{
					if (_actionList.getMinSelectionIndex() > 0)
					{
						_actionList.setSelectionInterval(0, _actionList.getMaxSelectionIndex());
					}
				}
			});
		mainPanel.add(new JScrollPane(_actionList), BorderLayout.CENTER);
		// Buttons
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
		JButton okButton = new JButton(I18nManager.getText("button.ok"));
		okButton.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					doActions(_actionList.getMaxSelectionIndex() + 1);
					_dialog.dispose();
					_app.getCanvas().repaint();
				}
			});
		buttonPanel.add(okButton);
		JButton cancelButton = new JButton(I18nManager.getText("button.cancel"));
		cancelButton.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					_dialog.dispose();
				}
			});
		buttonPanel.add(cancelButton);
		mainPanel.add(buttonPanel, BorderLayout.SOUTH);
		_dialog.getContentPane().add(mainPanel);
		_dialog.pack();
		_dialog.setVisible(true);
	}

}
