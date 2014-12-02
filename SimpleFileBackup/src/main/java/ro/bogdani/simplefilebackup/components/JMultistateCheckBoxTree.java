package ro.bogdani.simplefilebackup.components;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JPanel;
import javax.swing.JTextPane;
import javax.swing.JTree;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.tree.ExpandVetoException;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreePath;

import ro.bogdani.simplefilebackup.BackupProcess;
import ro.bogdani.simplefilebackup.model.DiskNode;
import ro.bogdani.simplefilebackup.model.DiskTreeModel;

@SuppressWarnings("serial")
public class JMultistateCheckBoxTree extends JTree {

	final JMultistateCheckBoxTree self;
	final DiskTreeModel model;

	public JMultistateCheckBoxTree(DiskTreeModel model, final BackupProcess backupProcess) {
		
		super(model);
		
		this.self = this;		
		this.model = model;

		setCellRenderer(new NodeRenderer());

		this.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent mouseEvent) {
				TreePath tp = self.getPathForLocation(mouseEvent.getX(),
						mouseEvent.getY());
				if (tp == null) {
					return;
				}

				DiskNode node = (DiskNode) self.getLastSelectedPathComponent();
				JMultistateCheckBoxTree.this.model.toggleSelected(node, backupProcess);		
			}
		});
		
		// TODO use for lazy loading?
		this.addTreeWillExpandListener(new TreeWillExpandListener() {
			public void treeWillExpand(TreeExpansionEvent event)
					throws ExpandVetoException {
			}
			
			public void treeWillCollapse(TreeExpansionEvent event)
					throws ExpandVetoException {
			}
		});
	}

	private class NodeRenderer extends JPanel implements
			TreeCellRenderer {

		JMultistateCheckBox checkBox;
		JTextPane text;

		public NodeRenderer() {
			this.setLayout(new BorderLayout());
			checkBox = new JMultistateCheckBox();
			add(checkBox, BorderLayout.WEST);
			text = new JTextPane();
			add(text, BorderLayout.EAST);
			setOpaque(false);
		}

		public Component getTreeCellRendererComponent(JTree tree, Object value,
				boolean selected, boolean expanded, boolean leaf, int row,
				boolean hasFocus) {
			if (value instanceof DiskNode) {
				DiskNode node = (DiskNode) value;
				if (node.isRoot()) {
					checkBox.setEnabled(false);
				} else
				if (model != null) {
					checkBox.setEnabled(true);
					if (model.isSelected(node)) {
						checkBox.setSelected(true);
					} else
					if (model.isPartialSelected(node)) {
						checkBox.setPartialSelected();
					} else {
						checkBox.setSelected(false);
					}
				}
				text.setText(node.toString());
			}
			return this;
		}
	}
}