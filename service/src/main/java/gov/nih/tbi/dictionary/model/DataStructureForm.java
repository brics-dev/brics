package gov.nih.tbi.dictionary.model;

import gov.nih.tbi.commons.service.ServiceConstants;
import gov.nih.tbi.commons.service.StaticReferenceManager;
import gov.nih.tbi.dictionary.dao.hibernate.DataElementDaoImpl;
import gov.nih.tbi.dictionary.model.hibernate.Disease;
import gov.nih.tbi.dictionary.model.hibernate.DiseaseStructure;
import gov.nih.tbi.dictionary.model.hibernate.FormStructure;
import gov.nih.tbi.repository.model.SubmissionType;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

public class DataStructureForm {

	static Logger logger = Logger.getLogger(DataElementDaoImpl.class);

	/**********************************************************************/
	@Autowired
	StaticReferenceManager staticManager;

	protected String title;
	protected String description;
	protected Boolean validatable;
	protected SubmissionType fileType;
	protected Boolean isCopyrighted;
	protected String shortName;
	protected String organization;
	protected long dateCreated;
	protected String createdBy;
	protected Set<DiseaseStructure> diseaseList;
	protected String currentOrg;
	protected String isInstancesRequiredForValue;
	protected FormStructureStandardization standardization;

	/**********************************************************************/

	public DataStructureForm() {

	}

	public DataStructureForm(FormStructure dataStructure, String orgName) {

		Field[] fields = this.getClass().getDeclaredFields();

		for (int i = 0; i < fields.length; i++) {
			Field current = fields[i];

			if (!current.getName().equals("logger") && !ServiceConstants.STATIC_MANAGER.equals(current.getName())
					&& !"currentOrg".equals(current.getName())) {
				try {
					if ("dateCreated".equals(current.getName())) {
						if (dataStructure.getDateCreated() != null) {
							this.dateCreated = dataStructure.getDateCreated().getTime();
						} else {
							this.dateCreated = new Date().getTime();
						}
					} else if ("isInstancesRequiredForValue".equals(current.getName())) {
						if (dataStructure.getInstancesRequiredFor() != null
								&& dataStructure.getInstancesRequiredFor().contains(new InstanceRequiredFor(orgName))) {
							setIsInstancesRequiredForValue(orgName);
						} else if (dataStructure.getInstancesRequiredFor() != null
								&& !dataStructure.getInstancesRequiredFor().contains(new InstanceRequiredFor(orgName))) {
							setIsInstancesRequiredForValue("");
						}
					} else {
						String getMethodName =
								"get" + current.getName().substring(0, 1).toUpperCase()
										+ current.getName().substring(1);

						Method setMethod = dataStructure.getClass().getMethod(getMethodName);

						Object value = setMethod.invoke(dataStructure);

						current.set(this, value);
					}
				} catch (Exception e) {
					e.printStackTrace();
					logger.error("There was an exception in dataStructureForm dataStructureFrom(): " + e.toString());
				}
			}
		}
	}

	/**********************************************************************/

	public String getTitle() {

		return title;
	}

	public void setTitle(String title) {

		this.title = title;
	}

	public String getDescription() {

		return description;
	}

	public void setDescription(String description) {

		this.description = description;
	}

	public Boolean getValidatable() {

		return validatable;
	}

	public void setValidatable(Boolean validatable) {

		this.validatable = validatable;
	}

	public String getShortName() {

		return shortName;
	}

	public void setShortName(String shortName) {

		this.shortName = shortName;
	}

	public String getOrganization() {

		return organization;
	}

	public void setOrganization(String organization) {

		this.organization = organization;
	}

	public Set<DiseaseStructure> getDiseaseList() {

		return diseaseList;
	}

	public SubmissionType getFileType() {

		return fileType;
	}

	public void setFileType(Long fileType) {

		if (fileType != null) {
			for (SubmissionType type : SubmissionType.values()) {
				if (fileType.equals(type.getId())) {
					this.fileType = type;
				}
			}
		}
	}

	public FormStructureStandardization getStandardization() {

		return standardization;
	}

	public void setStandardization(String standardization) {

		if (standardization != null) {
			for (FormStructureStandardization type : FormStructureStandardization.values()) {
				if (standardization.equals(type.getName())) {
					this.standardization = type;
				}
			}
		}
	}

	public void setDiseaseList(String[] diseaseIds) throws NumberFormatException, MalformedURLException,
			UnsupportedEncodingException {

		Set<DiseaseStructure> diseaseStrucures = new HashSet<DiseaseStructure>();

		for (String id : diseaseIds) {
			for (Disease diseaseOption : staticManager.getDiseaseList()) {
				if (diseaseOption.getId() == Long.parseLong(id)) {
					DiseaseStructure newDisease = new DiseaseStructure();
					newDisease.setDisease(diseaseOption);
					diseaseStrucures.add(newDisease);
				}
			}
		}

		this.diseaseList = diseaseStrucures;
	}

	public Boolean getIsCopyrighted() {

		return isCopyrighted;
	}

	public void setIsCopyrighted(Boolean isCopyrighted) {

		this.isCopyrighted = isCopyrighted;
	}

	public long getDateCreated() {

		return dateCreated;
	}

	public void setDateCreated(long dateCreated) {

		this.dateCreated = dateCreated;
	}

	public String getCreatedBy() {

		return createdBy;
	}

	public void setCreatedBy(String createdBy) {

		this.createdBy = createdBy;
	}

	public String getCurrentOrg() {

		return currentOrg;
	}

	public void setCurrentOrg(String currentOrg) {

		this.currentOrg = currentOrg;
	}

	public String getIsInstancesRequiredForValue() {

		return isInstancesRequiredForValue;
	}

	public void setIsInstancesRequiredForValue(String isInstancesRequiredForValue) {

		this.isInstancesRequiredForValue = isInstancesRequiredForValue;
	}

	/**********************************************************************/
	/**
	 * Reads data from the text fields on the page and writes to the dataStructure object
	 * 
	 * @param dataStructure
	 * @param annotation
	 * @throws SecurityException
	 * @throws NoSuchMethodException
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 */

	public void adapt(FormStructure dataStructure, Boolean enforceStaticFields) {

		Field[] fields = this.getClass().getDeclaredFields();

		for (int i = 0; i < fields.length; i++) {
			Field current = fields[i];

			if (!"logger".equals(current.getName()) && !ServiceConstants.STATIC_MANAGER.equals(current.getName())
					&& !"currentOrg".equals(current.getName())) {
				try {
					if (enforceStaticFields == false || current.getAnnotation(StaticField.class) == null) {
						Object value = current.get(this);
						// field is the required
						if ("isInstancesRequiredForValue".equals(current.getName())) {
							// if the value isn't null it needs to be added to the list && if the list doesn't already
							// contain the value
							if (!isInstancesRequiredForValue.trim().isEmpty()) {
								dataStructure.addInstancesRequiredFor(isInstancesRequiredForValue);
							} else {
								// if it is null or empty we need to make sure we remove it from the list
								if (dataStructure.getInstancesRequiredFor() != null
										&& dataStructure.getInstancesRequiredFor().contains(
												new InstanceRequiredFor(currentOrg))) {
									dataStructure.getInstancesRequiredFor().remove(new InstanceRequiredFor(currentOrg));
								} else {
									// just create an empty array
									dataStructure.addInstancesRequiredFor("");
								}
							}
						} else {

							String setMethodName =
									"set" + current.getName().substring(0, 1).toUpperCase()
											+ current.getName().substring(1);
							Method setMethod = dataStructure.getClass().getMethod(setMethodName, current.getType());

							setMethod.invoke(dataStructure, value);

							if (ServiceConstants.DISEASE_LIST.equals(current.getName())) {
								for (DiseaseStructure diseaseStructure : dataStructure.getDiseaseList()) {
									diseaseStructure.setFormStructure(dataStructure.getFormStructureSqlObject());
								}
							}
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
					logger.error("There was an exception in dataStructureForm adapt(): " + e.toString());
				}
			}
		}
	}
}
