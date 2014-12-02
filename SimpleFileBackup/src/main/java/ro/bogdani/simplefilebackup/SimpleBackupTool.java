package ro.bogdani.simplefilebackup;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.nio.file.Path;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.SwingWorker;

import ro.bogdani.simplefilebackup.components.JMultistateCheckBoxTree;
import ro.bogdani.simplefilebackup.model.DiskTreeModel;
import ro.bogdani.simplefilebackup.model.StatusModel;
import ro.bogdani.simplefilebackup.model.StatusModelChangeListner;

@SuppressWarnings("serial")
public class SimpleBackupTool extends JFrame implements BackupProcess {

	final JTree treeDisk;
	final JButton btnBackup;
	final JLabel statusBar;
	
	StatusModel statusModel = new StatusModel();
	
    public SimpleBackupTool() {
        super();        
                
        Container container = this.getContentPane();
        container.setLayout(new BorderLayout());
        treeDisk = new JMultistateCheckBoxTree(new DiskTreeModel(statusModel), this);
        container.add(new JScrollPane(treeDisk), BorderLayout.CENTER);
        
        JPanel southPanel = new JPanel();
        southPanel.setLayout(new BorderLayout());        
        container.add(southPanel, BorderLayout.SOUTH);
        
        btnBackup = new JButton("Backup");
        southPanel.add(btnBackup, BorderLayout.NORTH);
        btnBackup.addMouseListener(new MouseAdapter() {
        	@Override
        	public void mouseClicked(MouseEvent e) {
        		stateChange(State.BACKUP);
        		new SwingWorker<Void, Void>() {
					@Override
					protected Void doInBackground() throws Exception {
		        		List<Path> selectedFiles = ((DiskTreeModel) treeDisk.getModel()).getUniqueSelectedPaths();
		        		for (Path file : selectedFiles) {
		        			System.out.println(file.toAbsolutePath());
		        		}
		        		return null;
					}
					
					protected void done() {
						stateChange(State.READY);
					};
				}.execute();
        	}
		});
        statusBar = new JLabel();
        statusBar.setEnabled(false);
        southPanel.add(statusBar, BorderLayout.SOUTH);
        statusModel.setStatusModelChangeListner(new StatusModelChangeListner() {
			public void change(String newStatus) {				
				statusBar.setText(newStatus);
				statusBar.repaint();
			}
		});
        statusModel.setDefaultStatus();
        
        pack();
        setSize(500, 800);
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
    }


	public void stateChange(State state) {
		switch (state) {
		case READY:
			btnBackup.setEnabled(true);
			treeDisk.repaint();
			treeDisk.setEnabled(true);
			treeDisk.setCursor(Cursor.getDefaultCursor());
			break;
		case CHOOSING_FILES:
		case BACKUP:
			btnBackup.setEnabled(false);
			treeDisk.setEnabled(false);
			treeDisk.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
			treeDisk.repaint();
			break;
		default:
			break;
		}		
	}
	
	public static void main(String args[]) {
		SimpleBackupTool m = new SimpleBackupTool();
		m.setVisible(true);
	}
}