package tim.prune.waypoint;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.event.ListDataEvent;

public class GotoMenu extends JMenu {
	private static final long serialVersionUID = -6420392093436734652L;

	private GotoManager _manager;
	private JMenuItem _startItem;

	private void setupMenu() {
		removeAll();
		JMenuItem gridItem = new JMenuItem("Go To Grid");
		gridItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				_manager.openDialog();
			}
		});
		add(gridItem);
		add(_startItem);
		addSeparator();
		for (int x = 0; x < _manager.getSize(); x++) {
			if ((_manager.getUserSize() != 0) && (x == _manager.getUserSize())) {
				addSeparator();
			}
			JMenuItem item = new JMenuItem((String)_manager.getElementAt(x));
			final int tx = x;
			item.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					_manager.gotoWaypoint(tx);
				}
			});
			add(item);
		}
	}

	public GotoMenu(GotoManager inManager) {
		super("Go To");
		_manager = inManager;
		_startItem = new JMenuItem("Go To Start View");
		_startItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				_manager.gotoStartView();
			}
		});
		_startItem.setEnabled(false);
		setupMenu();
		_manager.addListDataListener(new GotoManager.GotoListener() {
			@Override
			public void contentsChanged(ListDataEvent arg0) {
				setupMenu();
			}
		});
	}

	public void enableStartView() {
		_startItem.setEnabled(true);
	}

	public void disableStartView() {
		_startItem.setEnabled(false);
	}
}
