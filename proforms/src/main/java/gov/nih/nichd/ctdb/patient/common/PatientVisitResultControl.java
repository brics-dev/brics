package gov.nih.nichd.ctdb.patient.common;

import java.util.Date;

import gov.nih.nichd.ctdb.common.ResultControl;

public class PatientVisitResultControl extends ResultControl{

	/**
	 * PatientVisitResultControl handles searching and sorting of patient records in the system.
	 *
	 * @version 1.0
	 */

    	private int visitDateId = Integer.MIN_VALUE;
	    private int patientId = Integer.MIN_VALUE;
	    private int protocolId = Integer.MIN_VALUE;
	    private Date startDate = new Date();
	    private Date endDate = new Date();

	    public Date getStartDate() {
			return startDate;
		}

		public void setStartDate(Date startDate) {
			this.startDate = startDate;
		}

		public Date getEndDate() {
			return endDate;
		}

		public void setEndDate(Date endDate) {
			this.endDate = endDate;
		}

		public int getProtocolId()
	    {
	        return protocolId;
	    }

	    public void  setProtocolId(int protocolId)
	    {
	        this.protocolId = protocolId;
	    }

	    public int getPatientId()
	    {
	        return patientId;
	    }

	    public void  setPatientId(int pId)
	    {
	        this.patientId = pId;
	    }

		public int getVisitDateId() {
			return visitDateId;
		}

		public void setVisitDateId(int visitDateId) {
			this.visitDateId = visitDateId;
		}
	    
	    private String getVisitDateIdSearchString()
	    {
	    	if(this.visitDateId != Integer.MIN_VALUE){
               return  " and pv.visitdateid = " + this.visitDateId + " ";
	    	}
	    	else{
	    		return " " ;
	    	}
	    }

	    private String getProtocolIdSearchString()
	    {
	    	if(this.protocolId != Integer.MIN_VALUE){
	    		return  " and pv.protocolid = " + this.protocolId + " ";
	    	}
	    	else{
	    		return " " ;
	    	}
	    }
	    private String getPatientIdSearchString()
	    {
	    	if(this.patientId != Integer.MIN_VALUE){
	    		return  " and pv.patientid = " + this.patientId + " ";
	    	}
	    	else{
	    		return " " ;
	    	}
	    }


	    /**
	     * Gets the Search Clause for this SQL operation to determine the results to be returned.
	     *
	     * @return The string representation of the search clause for this SQL operation
	     */
	    public String getSearchClause()
	    {
	        StringBuffer clause = new StringBuffer(180);
	        clause.append(this.getVisitDateIdSearchString() + " ");
	        clause.append(this.getPatientIdSearchString() + " ");
	        clause.append(this.getProtocolIdSearchString() + " ");
	        return clause.toString();
	    }

	}
