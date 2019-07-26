package gov.nih.nichd.ctdb.response.domain;

import gov.nih.nichd.ctdb.common.ResultControl;
import gov.nih.nichd.ctdb.patient.domain.Patient;

import java.util.Comparator;

/**
 * AdministeredFormComparator handles sorting of administered forms involving patient info, since
 * patient data is encrypted in the database.
 *
 * @author Booz Allen Hamilton
 * @version 1.0
 */
public class AdministeredFormComparator
{
    /**
     * Constant to be used to sort patients by NIH Record Number in ascending order
     */
    public static final Comparator NIH_REC_NUM_ASC = new NihRecNumComparator(ResultControl.SORT_ASC);

    /**
     * Constant to be used to sort patients by NIH Record Number in descending order
     */
    public static final Comparator NIH_REC_NUM_DESC = new NihRecNumComparator(ResultControl.SORT_DESC);

    public static final Comparator PATIENT_LAST_NAME_ASC = new PatientLastNameComparator (ResultControl.SORT_ASC);
    public static final Comparator PATIENT_LAST_NAME_DESC = new PatientLastNameComparator (ResultControl.SORT_DESC);


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
         * @param order The order to sort
         */
        public NihRecNumComparator(String order)
        {
            this.order = order;
        }

        /**
         * Compares two NIH Record Numbers
         * @param o1 AdministeredForm with Patient 1 to compare
         * @param o2 AdministeredForm with Patient 2 to compare
         * @throws ClassCastException if either o1 or o2 are not Patient objects
         * @return The value <code>0</code> if the NIH Record Numbers are equal; a value less than
         *		   <code>0</code> if Patient 1's NIH Record Number is
         *		   greater than Patient 2's NIH Record Number; and a value greater than
         *		   <code>0</code> if Patient 1's NIH Record Number is
         *		   less than Patient 2's NIH Record Number.
         */
        public int compare(Object o1, Object o2) throws ClassCastException
        {
            AdministeredForm aform1 = (AdministeredForm) o1;
            AdministeredForm aform2 = (AdministeredForm) o2;
            Patient p1 = aform1.getPatient();
            Patient p2 = aform2.getPatient();

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

     private static class PatientLastNameComparator implements Comparator
    {
        private String order = ResultControl.SORT_ASC;

        /**
         * Constructor for private inner class NihRecNumComparator which
         * sets the sorting order
         *
         * @param order The order to sort
         */
        public PatientLastNameComparator(String order)
        {
            this.order = order;
        }

        /**
         * Compares two NIH Record Numbers
         * @param o1 AdministeredForm with Patient 1 to compare
         * @param o2 AdministeredForm with Patient 2 to compare
         * @throws ClassCastException if either o1 or o2 are not Patient objects
         * @return The value <code>0</code> if the NIH Record Numbers are equal; a value less than
         *		   <code>0</code> if Patient 1's NIH Record Number is
         *		   greater than Patient 2's NIH Record Number; and a value greater than
         *		   <code>0</code> if Patient 1's NIH Record Number is
         *		   less than Patient 2's NIH Record Number.
         */
        public int compare(Object o1, Object o2) throws ClassCastException
        {
            AdministeredForm aform1 = (AdministeredForm) o1;
            AdministeredForm aform2 = (AdministeredForm) o2;
            Patient p1 = aform1.getPatient();
            Patient p2 = aform2.getPatient();

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
}
