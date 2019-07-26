
package gov.nih.tbi.dictionary.validation.view;

import java.util.TreeSet;

import javax.swing.DefaultListSelectionModel;
import javax.swing.JTree;
import javax.swing.ListSelectionModel;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreePath;

public class TreeListSelectionModel extends DefaultListSelectionModel implements TreeSelectionListener
{

    // Default
    private static final long serialVersionUID = 1L;

    JTree tree;

    public TreeListSelectionModel(JTree tree)
    {

        this.tree = tree;
        tree.addTreeSelectionListener(this);
        this.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
    }

    public void valueChanged(TreeSelectionEvent event)
    {

        TreeSet<Integer> addSet = new TreeSet<Integer>();
        TreeSet<Integer> removeSet = new TreeSet<Integer>();

        for (TreePath p : event.getPaths())
        {
            int row = tree.getRowForPath(p);
            if (event.isAddedPath(p))
            {
                addSet.add(row);
            }
            else
            {
                removeSet.add(row);
            }
        }

        while (!addSet.isEmpty())
        {
            int start = addSet.first();
            int finish = start;
            while (addSet.contains(finish))
            {
                addSet.remove(finish);
                finish++;
            }
            addSelectionInterval(start, --finish);
            this.fireValueChanged(start, --finish, true);
        }

        while (!removeSet.isEmpty())
        {
            int start = removeSet.first();
            int finish = start;
            while (removeSet.contains(finish))
            {
                removeSet.remove(finish);
                finish++;
            }
            removeSelectionInterval(start, --finish);
            this.fireValueChanged(start, --finish, true);
        }
    }

}
