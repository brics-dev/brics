
package gov.nih.tbi.dictionary.validation.model;

import java.util.ArrayDeque;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class FileNodeIterator implements Iterator<FileNode>
{

    ArrayDeque<FileNode> nodes = new ArrayDeque<FileNode>();

    public FileNodeIterator(FileNode root)
    {

        nodes.add(root);
    }

    public boolean hasNext()
    {

        if (!nodes.isEmpty())
        {
            return true;
        }
        return false;
    }

    public FileNode next()
    {

        if (nodes.isEmpty())
        {
            throw new NoSuchElementException();
        }
        FileNode node = nodes.pop();
        for (int i = 0; i < node.getChildCount(); i++)
        {
            nodes.add((FileNode) node.getChildAt(i));
        }
        return node;
    }

    public void remove()
    {

        throw new UnsupportedOperationException();
    }

}
