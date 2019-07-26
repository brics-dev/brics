
package gov.nih.tbi.dictionary.validation.view;

import javax.swing.DefaultListModel;

public class SortedListModel extends DefaultListModel
{

    // Default
    private static final long serialVersionUID = 1L;

    public SortedListModel()
    {

        super();
    }

    public void addElement(Object element)
    {

        int index = 0;
        int size = getSize();

        // Determine where to insert element to keep list in sorted order

        for (index = 0; index < size; index++)
        {
            Comparable c = (Comparable) getElementAt(index);

            if (c.compareTo(element) > 0)
                break;
        }

        super.insertElementAt(element, index);
    }

    public void insertElementAt(Object element, int index)
    {

        addElement(element);
    }

}
