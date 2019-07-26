
package gov.nih.tbi.dictionary.validation.view;

import gov.nih.tbi.dictionary.validation.model.FileNode;

import javax.swing.AbstractListModel;
import javax.swing.JTree;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.text.Position;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

//A tree model that listens to 
public class TreeListModel extends AbstractListModel implements TreeModelListener, TreeExpansionListener
{

    // Default
    private static final long serialVersionUID = 1L;

    JTree tree;
    TreeModel model;

    public TreeListModel(JTree tree)
    {

        this.tree = tree;
        tree.addTreeExpansionListener(this);
        model = tree.getModel();
        model.addTreeModelListener(this);
    }

    public Object getElementAt(int index)
    {

        TreePath path = tree.getPathForRow(index);
        Object node = path.getLastPathComponent();
        if (node instanceof FileNode)
        {
            return node;
        }
        return null;
    }

    public int getSize()
    {

        return tree.getRowCount();
    }

    public void treeNodesChanged(TreeModelEvent event)
    {

        TreePath path, parent, next;
        int index0, index1;

        path = event.getTreePath();
        if (tree.isVisible(path))
        {
            index0 = tree.getRowForPath(path);
            if (tree.isExpanded(index0))
            {
                parent = path.getParentPath();
                next = tree.getNextMatch(parent.toString(), index0, Position.Bias.Forward);
                if (next != null)
                {
                    index1 = tree.getRowForPath(next);
                    this.fireContentsChanged(event.getSource(), index0, index1);
                }
            }
            this.fireContentsChanged(event.getSource(), index0, index0);
        }
    }

    public void treeNodesInserted(TreeModelEvent event)
    {

        TreePath path, parent, next;
        int index0, index1;

        path = event.getTreePath();
        if (tree.isVisible(path))
        {
            index0 = tree.getRowForPath(path);
            if (tree.isExpanded(index0))
            {
                parent = path.getParentPath();
                next = tree.getNextMatch(parent.toString(), index0, Position.Bias.Forward);
                if (next != null)
                {
                    index1 = tree.getRowForPath(next);
                    this.fireContentsChanged(event.getSource(), index0, index1);
                }
            }
            this.fireIntervalAdded(event.getSource(), index0, index0);
        }
    }

    public void treeNodesRemoved(TreeModelEvent event)
    {

        TreePath path, parent, next;
        int index0, index1;

        path = event.getTreePath();
        if (tree.isVisible(path))
        {
            index0 = tree.getRowForPath(path);
            if (tree.isExpanded(index0))
            {
                parent = path.getParentPath();
                next = tree.getNextMatch(parent.toString(), index0, Position.Bias.Forward);
                if (next != null)
                {
                    index1 = tree.getRowForPath(next);
                    this.fireContentsChanged(event.getSource(), index0, index1);
                }
            }
            this.fireIntervalRemoved(event.getSource(), index0, index0);
        }
    }

    public void treeStructureChanged(TreeModelEvent event)
    {

        treeNodesChanged(event);
    }

    public void treeCollapsed(TreeExpansionEvent event)
    {

        treeNodesInserted(new TreeModelEvent(event.getSource(), event.getPath()));
    }

    public void treeExpanded(TreeExpansionEvent event)
    {

        treeNodesRemoved(new TreeModelEvent(event.getSource(), event.getPath()));
    }

}
