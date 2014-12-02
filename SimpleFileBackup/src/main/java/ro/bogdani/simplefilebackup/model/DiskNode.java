package ro.bogdani.simplefilebackup.model;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

public class DiskNode {

	Path path;

	public DiskNode(Path path) {
		this.path = path;
	}

	public boolean isRoot() {
		return path == null;
	}

	public long countChildren() {
		if (isRoot()) {
			return getChildren().size();
		} else {
			Stream<Path> files = null;
			try {
				files = Files.list(path);
				if (files == null) {
					return 0;
				} else {
					return files.count();
				}
			} catch (IOException ioe) {
				return 0;
			} finally {
				if (files != null) {
					files.close();
				}
			}
		}
	}

	public DiskNode getChild(int index) {
		return new DiskNode(getChildren().get(index));
	}
	
	public int getIndexOfChild(DiskNode child) {
		return getChildren().indexOf(child.getPath());
	}
	
	public Path getPath() {
		return path;
	}

	private List<Path> getChildren() {
		List<Path> childPaths = new ArrayList<Path>();
		Iterator<Path> pathIterator = null;
		if (isRoot()) {
			FileSystem fileSystem = FileSystems.getDefault();
			pathIterator = fileSystem.getRootDirectories().iterator();			
		} else {
			try {
				pathIterator = Files.list(path).iterator();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		while (pathIterator.hasNext()) {
			childPaths.add(pathIterator.next());
		}
		
		return childPaths;
	}
	
	public boolean isLeaf() {
		if (isRoot()) {
			return false;
		} else {
			return !Files.isDirectory(path, LinkOption.NOFOLLOW_LINKS);
		}
	}	
	
	public boolean isEmpty() {
		if (isLeaf()) {
			return true;
		}
		if (isRoot()) {
			return false;
		}
	    DirectoryStream<Path> dirStream;
		try {			
			dirStream = Files.newDirectoryStream(path);
		} catch (IOException e) {
			return false;
		}
        return !dirStream.iterator().hasNext();	    
	}	

	@Override
	public String toString() {
		if (path == null) {
			return "My Drives";
		}
		Path fileName = path.getFileName();
		if (fileName == null) {
			// this is called for disk roots (e.g. C:\, D:\ etc.)
			return path.toString();
		} else {
			return fileName.toString();
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((path == null) ? 0 : path.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		DiskNode other = (DiskNode) obj;
		if (path == null) {
			if (other.path != null)
				return false;
		} else if (!path.equals(other.path))
			return false;
		return true;
	}
}
