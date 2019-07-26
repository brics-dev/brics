package gov.nih.nichd.ctdb.patient.domain;

import gov.nih.nichd.ctdb.common.ResultControl;

import java.util.Comparator;

/**
 * PatientComparator handles sorting of patients.
 *
 * @author Booz Allen Hamilton
 * @version 1.0
 */
public class PatientComparator
{
    /**
     * Constant to be used to sort patients by last name in ascending order
     */
    public static final Comparator LAST_NAME_ASC = new LastNameComparator(ResultControl.SORT_ASC);

    /**
     * Constant to be used to sort patients by last name in descending order
     */
    public static final Comparator LAST_NAME_DESC = new LastNameComparator(ResultControl.SORT_DESC);

    /**
     * Handles comparing patients last names
     */
    private static class LastNameComparator implements Comparator
    {
        private String order = ResultControl.SORT_ASC;

        /**
         * Constructor for private inner class LastNameComparator which
         * sets the sorting order
         *
         * @param order
         */
        public LastNameComparator(String order)
        {
            this.order = order;
        }

        /**
         * Compares patient last names
         *
         * @param o1 Patient 1 to compare
         * @param o2 Patient 2 to compare
         * @throws ClassCastException if either o1 or o2 are not Patient objects
         * @return The value <code>0</code> if the Last Names are equal; a value less than
         *		   <code>0</code> if Patient 1's Last Name is
         *		   greater than Patient 2's Last Name; and a value greater than
         *		   <code>0</code> if Patient 1's Last Name is
         *		   less than Patient 2's Last Name.
         */
        public int compare(Object o1, Object o2) throws ClassCastException
        {
            Patient p1 = (Patient) o1;
            Patient p2 = (Patient) o2;

            int compareResult = p1.getLastName().compareToIgnoreCase(p2.getLastName());

            if(this.order.equalsIgnoreCase(ResultControl.SORT_DESC))
            {
                if(compareResult != 0)
                {
                    if(compareResult < 0)
                    {
                        compareResult = 1;
                    }
                    else
                    {
                        compareResult = -1;
                    }
                }
            }

            return compareResult;
        }
    }

    /**
     * Constant to be used to sort patients by NIH Record Number in ascending order
     */
    public static final Comparator NIH_REC_NUM_ASC = new NihRecNumComparator(ResultControl.SORT_ASC);

    /**
     * Constant to be used to sort patients by NIH Record Number in descending order
     */
    public static final Comparator NIH_REC_NUM_DESC = new NihRecNumComparator(ResultControl.SORT_DESC);

    /**
     * Handles comparing patients NIH Record Numbers
     */
    private static class NihRecNumComparator implements Comparator
    {
        private String order = ResultControl.SORT_ASC;

        /**
         * Constructor for private inner class NihRecNumComparator which
         * sets the sorting order
         *
         * @param order
         */
        public NihRecNumComparator(String order)
        {
            this.order = order;
        }

        /**
         * Compares two NIH Record Numbers
         * @param o1 Patient 1 to compare
         * @param o2 Patient 2 to compare
         * @throws ClassCastException if either o1 or o2 are not Patient objects
         * @return The value <code>0</code> if the NIH Record Numbers are equal; a value less than
         *		   <code>0</code> if Patient 1's NIH Record Number is
         *		   greater than Patient 2's NIH Record Number; and a value greater than
         *		   <code>0</code> if Patient 1's NIH Record Number is
         *		   less than Patient 2's NIH Record Number.
         */
        public int compare(Object o1, Object o2) throws ClassCastException
        {
            Patient p1 = (Patient) o1;
            Patient p2 = (Patient) o2;

            int compareResult = p1.getSubjectId().compareToIgnoreCase(p2.getSubjectId());

            if(this.order.equalsIgnoreCase(ResultControl.SORT_DESC))
            {
                if(compareResult != 0)
                {
                    if(compareResult < 0)
                    {
                        compareResult = 1;
                    }
                    else
                    {
                        compareResult = -1;
                    }
                }
            }

            return compareResult;
        }
    }

    /**
     * Constant to be used to sort patients by first name in ascending order
     */
    public static final Comparator FIRST_NAME_ASC = new FirstNameComparator(ResultControl.SORT_ASC);

    /**
     * Constant to be used to sort patients by first name in descending order
     */
    public static final Comparator FIRST_NAME_DESC = new FirstNameComparator(ResultControl.SORT_DESC);

    /**
     * Handles comparing patient's first names
     */
    private static class FirstNameComparator implements Comparator
    {
        private String order = ResultControl.SORT_ASC;

        /**
         * Constructor for private inner class FirstNameComparator which
         * sets the sorting order
         *
         * @param order
         */
        public FirstNameComparator(String order)
        {
            this.order = order;
        }

        /**
         * Compares two patients first names
         *
         * @param o1 Patient 1 to compare
         * @param o2 Patient 2 to compare
         * @throws ClassCastException if either o1 or o2 are not Patient objects
         * @return The value <code>0</code> if the First Names are equal; a value less than
         *		   <code>0</code> if Patient 1's First Name is
         *		   greater than Patient 2's First Name; and a value greater than
         *		   <code>0</code> if Patient 1's First Name is
         *		   less than Patient 2's First Name.
         */
        public int compare(Object o1, Object o2) throws ClassCastException
        {
            Patient p1 = (Patient) o1;
            Patient p2 = (Patient) o2;

            int compareResult = p1.getFirstName().compareToIgnoreCase(p2.getFirstName());

            if(compareResult == 0)
            {
                //first names are equal
                String p1MiddleName = p1.getMiddleName();
                String p2MiddleName = p2.getMiddleName();

                String p1Mi = null;
                if(p1MiddleName != null && !p1MiddleName.trim().equalsIgnoreCase(""))
                {
                    p1Mi = p1MiddleName.substring(0, 1);
                }

                String p2Mi = null;
                if(p2MiddleName != null && !p2MiddleName.trim().equalsIgnoreCase(""))
                {
                    p2Mi = p2MiddleName.substring(0, 1);
                }

                if(p1Mi == null && p2Mi == null)
                {
                    compareResult = 0;
                }
                else if(p1Mi != null && p2Mi == null)
                {
                    compareResult = 1;
                }
                else if(p1Mi == null && p2Mi != null)
                {
                    compareResult = -1;
                }
                else if(p1Mi != null && p2Mi != null)
                {
                    compareResult = p1Mi.compareToIgnoreCase(p2Mi);
                }
            }

            if(this.order.equalsIgnoreCase(ResultControl.SORT_DESC))
            {
                if(compareResult != 0)
                {
                    if(compareResult < 0)
                    {
                        compareResult = 1;
                    }
                    else
                    {
                        compareResult = -1;
                    }
                }
            }

            return compareResult;
        }
    }
}
