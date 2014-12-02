package ro.bogdani.simplefilebackup.model;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import javax.swing.SwingWorker;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import ro.bogdani.simplefilebackup.BackupProcess;
import ro.bogdani.simplefilebackup.BackupProcess.State;

public class DiskTreeModel implements TreeModel {

	final DiskNode root = new DiskNode(null);

	final StatusModel status;

	final Map<DiskNode, NodeState> selectedNodes = new HashMap<DiskNode, NodeState>();

	private enum NodeState {
		SELECTED, PARTIAL_SELECTED
	}

	public DiskTreeModel(StatusModel status) {
		this.status = status;
	}

	public Object getChild(Object parent, int index) {
		return ((DiskNode) parent).getChild(index);
	}

	public int getChildCount(Object parent) {
		return (int) ((DiskNode) parent).countChildren();
	}

	public int getIndexOfChild(Object parent, Object child) {
		return ((DiskNode) parent).getIndexOfChild((DiskNode) child);
	}

	public Object getRoot() {
		return root;
	}

	public boolean isLeaf(Object node) {
		return ((DiskNode) node).isLeaf();
	}

	public void toggleSelected(final DiskNode node, final BackupProcess backupProcess) {
		SelectionWorker worker = new SelectionWorker(node, backupProcess);
		worker.execute();
	}

	private void unSelectNode(DiskNode node) {
		selectedNodes.remove(node);
	}

	private void unSelectChildren(final SelectionWorker selectionWorker, final DiskNode node) throws IOException {
		if (node.isLeaf()) {
			return;
		}
		try {
			Stream<Path> childPaths = Files.list(node.getPath());
			Iterator<Path> itChildPaths = childPaths.iterator();
			while (itChildPaths.hasNext()) {
				Path childPath = itChildPaths.next();
				DiskNode childNode = new DiskNode(childPath);
				unSelectNode(childNode);
				unSelectChildren(selectionWorker, childNode);
			}
			childPaths.close();
			selectionWorker.firePropertyChange("currentPath", null, new NodeChangeEvent(node.getPath().toAbsolutePath(), NodeChange.UNSELECTED));
		} catch (AccessDeniedException ade) {
			return;
		}
	}

	private void selectNode(DiskNode node) {
		selectedNodes.put(node, NodeState.SELECTED);
	}

	private void partialSelectNode(DiskNode node) {
		selectedNodes.put(node, NodeState.PARTIAL_SELECTED);
	}

	private void selectChildren(SelectionWorker selectionWorker, final DiskNode node) throws IOException {
		if (node.isLeaf()) {
			return;
		}
		try {
			Stream<Path> childPaths = Files.list(node.getPath());
			Iterator<Path> itChildPaths = childPaths.iterator();
			while (itChildPaths.hasNext()) {
				Path childPath = itChildPaths.next();
				DiskNode childNode = new DiskNode(childPath);
				selectNode(childNode);
				selectChildren(selectionWorker, childNode);
			}			
			childPaths.close();
			selectionWorker.firePropertyChange("currentPath", null, new NodeChangeEvent(node.getPath().toAbsolutePath(), NodeChange.SELECTED));
		} catch (AccessDeniedException ade) {
			return;
		}
	}

	private void calculateParentsState(DiskNode node) throws IOException {
		Path parentPath = node.getPath().getParent();
		if (parentPath != null) {
			boolean hasSelectedChildren = false;
			boolean hasPartialSelectedChildren = false;
			boolean hasUnselectedChildren = false;

			Stream<Path> childPaths = Files.list(parentPath);
			Iterator<Path> itChildPaths = childPaths.iterator();
			while (itChildPaths.hasNext()) {
				Path childPath = itChildPaths.next();
				DiskNode childNode = new DiskNode(childPath);
				if (isSelected(childNode)) {
					hasSelectedChildren = true;
				} else if (isPartialSelected(childNode)) {
					hasPartialSelectedChildren = true;
				} else if (isNotSelected(childNode)) {
					hasUnselectedChildren = true;
				}
			}
			childPaths.close();

			DiskNode parentNode = new DiskNode(parentPath);
			if (hasSelectedChildren) {
				if (hasUnselectedChildren || hasPartialSelectedChildren) {
					partialSelectNode(parentNode);
				} else {
					selectNode(parentNode);
				}
			} else if (hasPartialSelectedChildren) {
				partialSelectNode(parentNode);
			} else {
				unSelectNode(parentNode);
			}

			calculateParentsState(parentNode);
		}
	}

	public boolean isPartialSelected(DiskNode node) {
		return NodeState.PARTIAL_SELECTED.equals(selectedNodes.get(node));
	}

	public boolean isSelected(DiskNode node) {
		return NodeState.SELECTED.equals(selectedNodes.get(node));
	}

	public boolean isNotSelected(DiskNode node) {
		return selectedNodes.get(node) == null;
	}

	public List<Path> getUniqueSelectedPaths() {
		List<Path> selectedPaths = new ArrayList<Path>();
		for (DiskNode node : selectedNodes.keySet()) {
			if (selectedNodes.get(node).equals(NodeState.SELECTED)
					&& node.isEmpty()) {
				selectedPaths.add(node.getPath().toAbsolutePath());
			}
		}
		return selectedPaths;
	}

	public void addTreeModelListener(TreeModelListener arg0) {
	}

	public void removeTreeModelListener(TreeModelListener arg0) {
	}

	public void valueForPathChanged(TreePath path, Object newValue) {
	}

	private enum NodeChange {
		SELECTED, UNSELECTED
	}
	
	private class NodeChangeEvent {
		final String path;
		final NodeChange change;
		
		public NodeChangeEvent(Path path, NodeChange change) {
			this.path = path.toAbsolutePath().toString();
			this.change = change;			
		}
	}
	
	private class SelectionWorker extends SwingWorker<Void, NodeChangeEvent> {

		final DiskNode node;
		final BackupProcess backupProcess;
		
		SelectionWorker(DiskNode node, BackupProcess backupProcess) {
			this.node = node;
			this.backupProcess = backupProcess;

			addPropertyChangeListener(new PropertyChangeListener() {
				public void propertyChange(PropertyChangeEvent evt) {
					if ("currentPath".equals(evt.getPropertyName())) {
						SelectionWorker.this.publish(new NodeChangeEvent[] { (NodeChangeEvent) evt.getNewValue() });
					}
				}
			});
		}
		
		@Override
		protected void process(List<NodeChangeEvent> chunks) {
			if (isDone()) {
				return;
			}
			NodeChangeEvent nodeChangeEvent = chunks.get(0);
			switch (nodeChangeEvent.change) {
			case SELECTED:
				status.setStatus("Adding... " + nodeChangeEvent.path);
				break;
			case UNSELECTED:	
				status.setStatus("Removing... " + nodeChangeEvent.path);
				break;
			default:
				break;
			}
			backupProcess.stateChange(State.CHOOSING_FILES);
		}

		@Override
		protected Void doInBackground() throws Exception {
			backupProcess.stateChange(State.CHOOSING_FILES);
			try {
				if (isSelected(node)) {
					unSelectNode(node);
					unSelectChildren(this, node);
				} else {
					// if the node is not selected or partial selected then make it selected
					selectNode(node);
					selectChildren(this, node);
				}
				calculateParentsState(node);
			} catch (IOException e) {
				e.printStackTrace();
			}
			return null;
		}

		@Override
		protected void done() {
			status.setDefaultStatus();
			backupProcess.stateChange(State.READY);
		}
	};
}
