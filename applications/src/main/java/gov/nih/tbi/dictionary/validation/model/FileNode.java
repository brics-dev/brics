package gov.nih.tbi.dictionary.validation.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Vector;

import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

public class FileNode implements MutableTreeNode, Comparable<FileNode> {
	public String fsStatus;


	private final String path;
	private boolean validated = false;
	private boolean included = true;
	private final String name;
	private FileType type;
	private String structureShortName; // Will always be null for DIR
	private int warnNum = 0, errorNum = 0, fileNum = 0;

	private FileNode parent;
	private Vector<FileNode> children = new Vector<FileNode>();

	// File Tracking
	private String crcHash;
	private Long fileSize;
	private int unknownErrorNum = 0;
	
	private FileNode association;
	
	private static final String[] typeStrings = {"UNKNOWN", "DIR", "TAB", "FORM STRUCTURE", "XML", "ASSOCIATED",
			"THUMBNAIL", "TRANSLATIONS", "TRIPLANAR"};



	

	public String getFsStatus() {
		return fsStatus;
	}

	public void setFsStatus(String fsStatus) {
		this.fsStatus = fsStatus;
	}

	public enum FileType {
		UNKNOWN, DIR, TAB, CSV, XML, ASSOCIATED, THUMBNAIL, TRANSLATION_RULE, TRIPLANAR
	}

	public FileNode(String conicalPath, String name, FileType type, FileNode parent) {

		this.path = conicalPath;
		this.name = name;
		this.type = type;
		this.parent = parent;
	}

	public FileNode getAssociation() {
		return association;
	}

	public void setAssociation(FileNode association) {
		this.association = association;
	}

	public String getConicalPath() {

		return path;
	}

	public boolean isValidated() {

		if (isDirectory()) {
			validated = false;
			for (FileNode child : children) {
				validated = validated || child.validated;
			}
		}
		return validated;
	}

	public void setValidated(boolean validated) {

		this.validated = validated;
	}

	public boolean isIncluded() {

		if (isDirectory()) {
			for (FileNode child : children) {
				included = included || child.included;
			}
		}
		return included;
	}

	public void exclude() {

		included = false;
		if (isDirectory()) {
			for (FileNode child : children) {
				child.exclude();
			}
		}
	}

	public void include() {

		included = true;
		if (isDirectory()) {
			for (FileNode child : children) {
				child.include();
			}
		}
	}

	public String getName() {

		return name;
	}

	public boolean isDirectory() {

		if (getType() == FileType.DIR) {
			return true;
		}
		return false;
	}

	public FileType getType() {

		return type;
	}

	public void setType(FileType type) {

		this.type = type;
	}

	public String getTypeDisplay() {

		return typeStrings[type.ordinal()];
	}

	/**
	 * Recursively grabs all the structure names contained in the folder
	 * 
	 * @return
	 */
	public Vector<String> getStructures() {

		Vector<String> structures = new Vector<String>();
		if (isDirectory()) {
			for (FileNode child : children) {
				structures.addAll(child.getStructures());
			}
		} else {
			if (structureShortName != null) {
				structures.add(structureShortName);
			}
		}
		return structures;
	}

	public String getStructureDisplay() {

		if (isDirectory()) {
			Vector<String> structures = getStructures();
			if (structures.size() == 1) {
				return structures.get(0);
			}
			if (structures.size() > 1) {
				return "MULTIPLE";
			}
		}
		return structureShortName;
	}

	public String getStructureName() {

		return structureShortName;
	}

	public void setStructureName(String name) {

		if (!isDirectory()) {
			structureShortName = name;
		}
	}

	public boolean isValid() {

		boolean valid;
		if (isDirectory()) {
			valid = true;
			for (FileNode child : children) {
				if (child.included) {
					valid = valid && child.isValid() && (child.type != FileType.UNKNOWN);
				}
			}
		} else {
			if (isValidated()) {
				valid = (errorNum == 0);
			} else {
				valid = (type == FileType.ASSOCIATED) || (type == FileType.THUMBNAIL) || (type == FileType.TRIPLANAR);
			}
		}
		return valid;
	}

	public boolean hasValidFormStructure() {
		if (this.isDirectory()) {
			for (FileNode child : this.children) {
				if (child.hasValidFormStructure()) {
					return true;
				}
			}
		}
 else {
			return this.included && this.errorNum == 0 && this.structureShortName != null;
		}
		return false;
	}

	public String getResultDisplay() {

		if (isValidated()) {
			if (isValid()) {
				return "PASSED";
			} else {
				return "FAILED";
			}
		}
		return "";
	}

	public void setFileNum(int fileNum) {

		this.fileNum = fileNum;
	}

	public int getFileNum() {

		if (isDirectory()) {
			return 0;
		}
		return fileNum;
	}

	public void setWarnNum(int warnNum) {

		this.warnNum = warnNum;
	}

	public int getWarnNum() {

		if (isDirectory()) {
			warnNum = 0;
			for (FileNode child : children) {
				if (child.isIncluded()) {
					warnNum += child.getWarnNum();
				}
			}

			warnNum += unknownErrorNum;
		}
		return warnNum;
	}

	public void setErrorNum(int errorsNum) {

		this.errorNum = errorsNum;
	}

	public int getErrorNum() {

		if (isDirectory()) {
			errorNum = 0;
			for (FileNode child : children) {
				if (child.isIncluded()) {
					errorNum += child.getErrorNum();
				}
			}
		}
		return errorNum;
	}

	public String getCrcHash() {

		return crcHash;
	}

	public void setCrcHash(String crcHash) {

		this.crcHash = crcHash;
	}

	public Long getFileSize() {

		return fileSize;
	}

	public void setFileSize(Long fileSize) {

		this.fileSize = fileSize;
	}

	public String getSummaryDisplay() {

		String summary = "";
		if (getFileNum() > 0) {
			if (getFileNum() == 1) {
				summary += "1 File";
			} else {
				summary += (getFileNum() + " Files");
			}
		}

		if (getWarnNum() > 0) {

			if (!summary.equals("")) {
				summary += " + ";
			}

			if (getWarnNum() == 1) {
				summary += "1 Warning";
			} else {
				summary += (getWarnNum() + " Warnings");
			}
		}

		if (getErrorNum() > 0) {

			if (!summary.equals("")) {
				summary += " + ";
			}

			if (getErrorNum() == 1) {
				summary += "1 Error";
			} else {
				summary += (getErrorNum() + " Errors");
			}
		}

		if (FileType.UNKNOWN.equals(type) && name.toLowerCase().endsWith(".csv")) {
			if (summary.equals("")) {
				summary = "Not a form structure";
			} else {
				summary = "Not a form structure; " + summary;
			}
		}
		return summary;
	}

	public int hashCode() {

		return path.hashCode() + type.hashCode();
	}

	public String toString() {

		return getName();
	}

	public void insert(MutableTreeNode node) {

		if (node instanceof FileNode) {
			FileNode file = (FileNode) node;
			file.setParent(this);
			children.add((FileNode) node);
		}
	}

	// MutableTreeNode Interface

	public void insert(MutableTreeNode node, int index) {

		if (node instanceof FileNode) {
			FileNode file = (FileNode) node;
			file.setParent(this);
			children.add(index, file);
		}
	}

	public void remove(int index) {

		children.remove(index);
	}

	public void remove(MutableTreeNode node) {

		if (node instanceof FileNode) {
			children.remove(node);
		}
	}

	public void removeFromParent() {

		parent.remove(this);
	}

	public void setParent(MutableTreeNode node) {

		if (node instanceof FileNode) {
			parent = (FileNode) node;
		}
	}

	public void setUserObject(Object arg0) {

		// What is the user object?!
	}

	public Enumeration<FileNode> children() {

		return (Enumeration<FileNode>) children;
	}

	public boolean getAllowsChildren() {

		return isDirectory();
	}

	public TreeNode getChildAt(int index) {

		return children.get(index);
	}

	public int getChildCount() {

		return children.size();
	}

	public int getIndex(TreeNode child) {

		return children.indexOf(child);
	}

	public TreeNode getParent() {

		return parent;
	}

	// Shallow copy
	public ArrayList<FileNode> getChildren() {

		ArrayList<FileNode> arr = new ArrayList<FileNode>();
		for (FileNode n : children) {
			arr.add(n);
		}
		return arr;
	}

	private FileNode getHead() {

		if (parent == null) {
			return this;
		} else {
			return parent.getHead();
		}
	}

	public FileNode getTranslationRule() {

		FileNode head = getHead();

		return getTranslationRuleAux(head);
	}

	private FileNode getTranslationRuleAux(FileNode curr) {

		for (FileNode child : curr.getChildren()) {
			if (FileType.TRANSLATION_RULE.equals(child.getType())) {
				return child;
			} else {
				return getTranslationRuleAux(child);
			}
		}

		return null;
	}

	public boolean isLeaf() {

		return children.isEmpty();
	}

	public Vector<FileNode> getSubNodes() {

		Vector<FileNode> nodes = new Vector<FileNode>();
		getSubNodes(nodes);
		return nodes;
	}

	private void getSubNodes(Vector<FileNode> nodes) {

		nodes.add(this);
		for (FileNode node : children) {
			node.getSubNodes(nodes);
		}
	}

	public TreePath getTreePath() {

		ArrayList<FileNode> list = new ArrayList<FileNode>();
		TreeNode node = this;
		while (node != null) {
			list.add((FileNode) node);
			node = node.getParent();
		}
		Collections.reverse(list);
		return new TreePath(list.toArray());
	}

	@Override
	public int compareTo(FileNode other) {

		return path.compareToIgnoreCase(other.path);
	}

	public boolean isImageType() {

		String[] imageTypes = new String[] {".jpg", ".jpeg", ".png", ".bmp", ".gif"};

		for (int i = 0; i < imageTypes.length; i++) {
			if (path.toLowerCase().endsWith(imageTypes[i]))
				return true;
		}

		return false;

	}

	public boolean isValidWithUnknowns() {

		boolean valid = true;

		for (FileNode child : children) {
			if (child.isDirectory()) {
				valid = valid && child.isValidWithUnknowns();
			} else if (child.isIncluded() && child.getType() != FileType.UNKNOWN) {
				valid = valid && child.isValid();
			}
		}

		return valid;

	}

	public ArrayList<String> excludeUnknowns() {

		ArrayList<String> unknownWarnings = new ArrayList<String>();
		unknownErrorNum = 0;
		
		for (FileNode child : children) {
			if (child.isDirectory()) {
				ArrayList<String> childWarnings = child.excludeUnknowns();
				unknownWarnings.addAll(childWarnings);
				boolean allExcluded = true;
				for (FileNode grandchild : child.getChildren()) {
					if (grandchild.isIncluded()) {
						allExcluded = false;
						break;
					}
				}
				if (allExcluded) {
					child.exclude();
				}
			} else if (child.isIncluded() && child.getType() == FileType.UNKNOWN) {
				child.exclude();

				String warningMessage = child.name + " is not an associated file and has been excluded.";
				unknownWarnings.add(warningMessage);
				unknownErrorNum++;
			}
		}

		return unknownWarnings;

	}

}