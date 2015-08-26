package tim.prune.gui.progress;

import java.awt.BorderLayout;
import java.awt.Container;

import javax.swing.JDialog;
import javax.swing.JProgressBar;

public class ProgressMonitor {

	private final JProgressBar progressBar;
	private final JDialog progressDialog;
	
	public ProgressMonitor() {
		progressDialog = new JDialog();
		progressDialog.setAlwaysOnTop(true);
		progressDialog.setSize(450, 50);
		progressDialog.setLocationRelativeTo(null);
		
		Container contentPane = progressDialog.getContentPane();
		contentPane.setLayout(new BorderLayout());
		progressBar = new JProgressBar();
		progressBar.setIndeterminate(true);
		progressBar.setStringPainted(true);
		contentPane.add(progressBar);
	}
	
	public void start( String task, int totalWork ) {
		if( totalWork <= 0 ) {
			progressBar.setIndeterminate(true);
		} else {
			progressBar.setMaximum(totalWork);
		}
		progressDialog.setTitle(task == null ? "" : task);
		progressDialog.setVisible(true);
	}
	
	public void update( String message, int workInc ) {
		if( workInc > 0 ) {
			progressBar.setValue(progressBar.getValue()+workInc);
		}
		progressBar.setString(message == null ? "" : message);
	}
	
	public void finish() {
		progressDialog.setVisible(false);
		progressDialog.dispose();
	}
}
