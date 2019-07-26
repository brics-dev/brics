
package gov.nih.tbi.util;

import gov.nih.tbi.dictionary.model.hibernate.FormStructure;

import java.util.Comparator;

public class IdComparator implements Comparator<FormStructure>
{

    public int compare(FormStructure list1, FormStructure list2)
    {

        return (int) (list1.getId() - list2.getId());
    }
}