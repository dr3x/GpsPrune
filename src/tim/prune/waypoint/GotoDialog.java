package tim.prune.waypoint;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import com.bbn.openmap.LatLonPoint;
import com.bbn.openmap.proj.coords.DMSLatLonPoint;
import com.bbn.openmap.proj.coords.MGRSPoint;
import com.bbn.openmap.proj.coords.UTMPoint;

public class GotoDialog extends JDialog {
	private static final long serialVersionUID = 8072416207974096510L;
	private GotoManager _manager = null;
	private JTextField _coordEntry = null;
	private JComboBox _gotoList = null;
	private JButton _delete = null;
	private JLabel _utmText = null;
	private JLabel _decText = null;
	private JLabel _degText = null;
	private JCheckBox _startView = null;
	private JTextField _nameField = null;
	private JButton _restore = null;
	private JButton _save = null;

	private String _restorePoint = "";
	private String _restoreName = "";
	private boolean _restoreStartView;

	private class NameDocListener implements DocumentListener {
		public void changedUpdate(DocumentEvent e) {
			updated(e);
		}
		public void removeUpdate(DocumentEvent e) {
			updated(e);
		}
		public void insertUpdate(DocumentEvent e) {
			updated(e);
		}
		public void updated(DocumentEvent e) {
			checkSave();
		}
	};

	private class MGRSDocListener extends NameDocListener {
		public void updated(DocumentEvent e) {
			super.updated(e);
			MGRSPoint p = null;
			try {
				p = new MGRSPoint(_coordEntry.getText());
			} catch (NumberFormatException ex) { }
			if (p != null) {
				LatLonPoint llp = p.toLatLonPoint();
				UTMPoint utmPoint = new UTMPoint(llp);
				DMSLatLonPoint dmsPoint = new DMSLatLonPoint(llp);
				_utmText.setText(utmPoint.toString());
				_decText.setText(llp.toString());
				_degText.setText(dmsPoint.toString());
			}
		}
	};

	public GotoDialog(GotoManager inManager, JFrame inFrame) {
		super(inFrame, "Go To Grid Coordinate");
		_manager = inManager;
		setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

		JPanel coordPanel = new JPanel();
		coordPanel.setLayout(new BoxLayout(coordPanel, BoxLayout.Y_AXIS));
		coordPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		JPanel coordTop = new JPanel();
		coordTop.setLayout(new BoxLayout(coordTop, BoxLayout.X_AXIS));
		_coordEntry = new JTextField("");
		_gotoList = new JComboBox(_manager);
		coordTop.add(_gotoList);
		coordTop.add(Box.createRigidArea(new Dimension(10, 0)));
		_delete = new JButton("Delete");
		_delete.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				_manager.deleteSelected();
			}
		});
		_delete.setEnabled(false);
		coordTop.add(_delete);
		coordPanel.add(coordTop);
		JPanel clickPanel = new JPanel(new FlowLayout());
		clickPanel.add(new JLabel("Click map to update coordinates."));
		coordPanel.add(clickPanel);
		JPanel coordInfo = new JPanel(new GridLayout(4, 2));
		coordInfo.add(new JLabel("MGRS:"));
		coordInfo.add(_coordEntry);
		coordInfo.add(new JLabel("UTM:"));
		_utmText = new JLabel();
		coordInfo.add(_utmText);
		coordInfo.add(new JLabel("Lat/Lon Decimal:"));
		_decText = new JLabel();
		coordInfo.add(_decText);
		coordInfo.add(new JLabel("Lat/Lon Deg/Min/Sec:"));
		_degText = new JLabel();
		// Do this to help get a properly sized dialog.
		_degText.setText("XX\u00B0 XX' XXX.XXX\" S, XX\u00B0 XX' XXX.XXX\" W");
		coordInfo.add(_degText);
		coordPanel.add(coordInfo);
		add(coordPanel);

		JPanel namePanel = new JPanel(new GridLayout(2, 2));
		namePanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
		namePanel.add(new JLabel("Location Name:"));
		_nameField = new JTextField();
		namePanel.add(_nameField);
		_startView = new JCheckBox("Start View");
		_startView.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				checkSave();
			}
		});
		namePanel.add(_startView);
		_save = new JButton("Save in My Go To List");
		_save.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				MGRSPoint p = new MGRSPoint(_coordEntry.getText());
				_manager.save(_nameField.getText(), p);
				setPoint(p);
				setRestorePoint();
				disableSave();
			}
		});
		namePanel.add(_save);
		add(namePanel);

		JPanel bottomButtons = new JPanel();
		bottomButtons.setLayout(new BoxLayout(bottomButtons, BoxLayout.LINE_AXIS));
		bottomButtons.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		JButton cancel = new JButton("Close");
		cancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				clearDialog();
				setVisible(false);
			}
		});
		bottomButtons.add(cancel);
		bottomButtons.add(Box.createHorizontalGlue());
		JButton clear = new JButton("Clear");
		clear.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				clearDialog();
			}
		});
		bottomButtons.add(clear);
		bottomButtons.add(Box.createRigidArea(new Dimension(10, 0)));
		_restore = new JButton("Restore");
		_restore.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setPoint(new MGRSPoint(_restorePoint));
				_nameField.setText(_restoreName);
				_startView.setSelected(_restoreStartView);
				disableSave();
			}
		});
		bottomButtons.add(_restore);
		bottomButtons.add(Box.createRigidArea(new Dimension(10, 0)));
		JButton go = new JButton("Go");
		go.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				MGRSPoint p = null;
				try {
					p = new MGRSPoint(_coordEntry.getText());
				} catch (NumberFormatException ex) { }
				if (p != null) {
					_manager.gotoCoordEntry(p);
					setPoint(p);
				}
			}
		});
		bottomButtons.add(go);
		add(bottomButtons);
		disableSave();
		pack();
		_nameField.getDocument().addDocumentListener(new NameDocListener());
		_coordEntry.getDocument().addDocumentListener(new MGRSDocListener());
	}

	public void clearDialog() {
		_coordEntry.setText("");
		_utmText.setText("");
		_decText.setText("");
		_degText.setText("");
		_nameField.setText("");
		_startView.setSelected(false);
		_delete.setEnabled(false);
		_manager.clearSelected();
		// Allow a restore.
		_restore.setEnabled(true);
	}

	public void setPoint(MGRSPoint inPoint) {
		LatLonPoint llp = inPoint.toLatLonPoint();
		UTMPoint utmPoint = new UTMPoint(llp);
		DMSLatLonPoint dmsPoint = new DMSLatLonPoint(llp);
		_coordEntry.setText(inPoint.toString());
		_utmText.setText(utmPoint.toString());
		_decText.setText(llp.toString());
		_degText.setText(dmsPoint.toString());
	}

	public boolean isStartView() {
		return _startView.isSelected();
	}
	
	public void enableDelete() {
		_delete.setEnabled(true);
	}

	public void disableDelete() {
		_delete.setEnabled(false);
	}

	public void startViewSelected(boolean inSelected) {
		_startView.setSelected(inSelected);
	}

	public void setName(String inName) {
		_nameField.setText(inName);
	}

	public void enableSave() {
		String nName = _nameField.getText()==null?"":_nameField.getText();
		if (!nName.equals("")) {
			_save.setEnabled(true);
		}
		_restore.setEnabled(true);
	}

	public void disableSave() {
		_save.setEnabled(false);
		_restore.setEnabled(false);
	}

	private void checkSave() {
		String nName = _nameField.getText()==null?"":_nameField.getText();
		String cPoint = _coordEntry.getText()==null?"":_coordEntry.getText();
		if ((_restoreStartView != _startView.isSelected()) ||
				!_restoreName.equals(nName) || 
				!_restorePoint.equals(cPoint)) {
			enableSave();
		} else {
			disableSave();
		}
	}

	public void setRestorePoint() {
		_restorePoint = _coordEntry.getText()==null?"":_coordEntry.getText();
		_restoreName = _nameField.getText()==null?"":_nameField.getText();
		_restoreStartView = _startView.isSelected();
		disableSave();
	}
}
