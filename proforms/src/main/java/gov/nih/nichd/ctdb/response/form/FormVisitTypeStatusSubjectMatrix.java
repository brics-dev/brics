package gov.nih.nichd.ctdb.response.form;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
/**
 * POJO for get/set SubjectMatrix Dashboard
 * @author khanaly
 *
 */
public class FormVisitTypeStatusSubjectMatrix implements Serializable {
	private static final long serialVersionUID = 2998188343296925471L;
	
	/**
	 * This will get set data collection status
	 */
	private String collectionStatus;
	/**
	 * This is will get set form name
	 */
	private String formName;
	
	/**
	 * This variable will get set if the particular form is required or not in a visit type 
	 */
	private boolean isFormRequiredInVisitType;

    private Map<String, Boolean> isRequiredPerVisitType = new HashMap<String, Boolean>();

	/**
	 * This is get set visit type previously called interval
	 */
	private String visitType;
	
	/**
	 * Map for visity type and data collection status
	 */
	private boolean isFormAssociatedToInterval;
	
	Map<String, String> visitTypeStatusMap;
	
	public String getCollectionStatus() {
		return collectionStatus;
	}
	public void setCollectionStatus(String collectionStatus) {
		this.collectionStatus = collectionStatus;
	}
	public String getFormName() {
		return formName;
	}
	public void setFormName(String formName) {
		this.formName = formName;
	}


	public String getVisitType() {
		return visitType;
	}
	public void setVisitType(String visitType) {
		this.visitType = visitType;
	}
	public Map<String, String> getVisitTypeStatusMap() {
		return visitTypeStatusMap;
	}
	public void setVisitTypeStatusMap(Map<String, String> visitTypeStatusMap) {
		this.visitTypeStatusMap = visitTypeStatusMap;
	}
	public boolean isFormRequiredInVisitType() {
		return isFormRequiredInVisitType;
	}
	public void setFormRequiredInVisitType(boolean isFormRequiredInVisitType) {
		this.isFormRequiredInVisitType = isFormRequiredInVisitType;
	}
	public boolean isFormAssociatedToInterval() {
		return isFormAssociatedToInterval;
	}
	public void setFormAssociatedToInterval(boolean isFormAssociatedToInterval) {
		this.isFormAssociatedToInterval = isFormAssociatedToInterval;
	}
    public Map <String, Boolean> getIsRequiredPerVisitType()
    {

        return isRequiredPerVisitType;
    }
    public void setIsRequiredPerVisitType(Map <String, Boolean> isRequiredPerVisitType)
    {

        this.isRequiredPerVisitType = isRequiredPerVisitType;
    }
    
    public Boolean isRequiredByVisitType(String visitTypeName){
                return isRequiredPerVisitType.containsKey(visitTypeName) && isRequiredPerVisitType.get(visitTypeName)==true ;
    }
}
