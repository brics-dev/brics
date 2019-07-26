package gov.nih.tbi.doi.util;

import java.text.SimpleDateFormat;

import gov.nih.tbi.ModelConstants;
import gov.nih.tbi.ModulesConstants;
import gov.nih.tbi.account.model.hibernate.Account;
import gov.nih.tbi.commons.model.BricsInstanceType;
import gov.nih.tbi.commons.model.EntityType;
import gov.nih.tbi.commons.model.hibernate.User;
import gov.nih.tbi.doi.model.OSTIProductType;
import gov.nih.tbi.doi.model.OSTIRecord;
import gov.nih.tbi.metastudy.model.hibernate.MetaStudy;
import gov.nih.tbi.repository.model.hibernate.BasicDataset;
import gov.nih.tbi.repository.model.hibernate.Study;

public class DoiUtil {

	public static final String META_STUDY_PRODUCT_TYPE_SPECIFIC = "Collection of data artifacts and documentation.";
	public static final String STUDY_PRODUCT_TYPE_SPECIFIC = "Contributed, uploaded, and stored research data.";

	private static final String META_STUDY_LANDING_URL_PATH = "meta_study_profile/";
	private static final String STUDY_LANDING_URL_PATH = "study_profile/";

	private ModulesConstants modulesConstants;

	public DoiUtil(ModulesConstants modulesConstants) {
		this.modulesConstants = modulesConstants;
	}

	/**
	 * Translates a model object to a OSTIRecord object, such that all of the required OSTI/IAD fields and some of the
	 * optional ones will be filled in with data from the given MetaStudy and Account objects. The underline mapping
	 * logic will be assigned to more object specific methods based on the given entity type.
	 * 
	 * @param et - The entity type of the given model object. It is used to determine which model specific mapping logic
	 *        to use to map the given model to an OSTIRecord object.
	 * @param model - The object that data will be read from.
	 * @param creator - The account of the user that will be assigned as the creator of the generated DOI record.
	 * @return A DOI record object constructed from the data from the passed in model and account objects.
	 * @throws IllegalArgumentException When there is a validation error from setting incorrect data in the OSTIRecord
	 *         object, the given model is not yet supported, or when the given object is not an instance of a supported
	 *         model object.
	 */
	public OSTIRecord modelToDoiRecord(EntityType et, Object model, Account creator) throws IllegalArgumentException {
		OSTIRecord record = null;

		switch (et) {
			case META_STUDY:
				if (model instanceof MetaStudy) {
					MetaStudy ms = (MetaStudy) model;

					record = metaStudyToDoiRecord(ms, creator);
				} else {
					throw new IllegalArgumentException("The given model object is NOT an instance of MetaStudy.");
				}

				break;
			case STUDY:
				if (model instanceof Study) {
					Study s = (Study) model;

					record = studyToDoiRecord(s, creator);
				} else {
					throw new IllegalArgumentException("The given model object is NOT an instance of Study.");
				}

				break;
			case DATASET:
				if (model instanceof BasicDataset) {
					BasicDataset ds = (BasicDataset) model;

					record = dataSetToDoiRecord(ds, creator);
				} else {
					throw new IllegalArgumentException("The given model object is NOT an instance of BasicDataset.");
				}

				break;
			default:
				throw new IllegalArgumentException("The enity type " + et.getName() + " is not supported.");
		}

		return record;
	}

	/**
	 * Translates a MetaStudy object to a OSTIRecord object, such that all of the required OSTI/IAD fields and some of
	 * the optional ones will be filled in with data from the given MetaStudy and Account objects.
	 * 
	 * @param metaStudy - The MetaStudy object to read data from.
	 * @param creator - The account that will be set as the creator of the generated DOI record.
	 * @return A DOI record object constructed from the data from the passed in meta study and account objects.
	 * @throws IllegalArgumentException When there is a validation error from setting incorrect data in the OSTIRecord
	 *         object.
	 */
	private OSTIRecord metaStudyToDoiRecord(MetaStudy metaStudy, Account creator) throws IllegalArgumentException {
		OSTIRecord record = new OSTIRecord();
		User creatorUser = creator.getUser();

		// Set the required OSTI/IAD fields.
		record.setTitle(metaStudy.getTitle());
		record.setCreators(creatorUser.getFullName());
		record.setPublisher(ModelConstants.DOI_PUBLISHER_VAL);
		record.setLandingPageUrl(getLandingPageForMetaStudy(metaStudy));
		record.setProductType(OSTIProductType.COLLECTION);
		record.setProductTypeSpecific(DoiUtil.META_STUDY_PRODUCT_TYPE_SPECIFIC);

		//// Translate the publication date to a string in form of MM/dd/yyyy.
		SimpleDateFormat dateFormatter = new SimpleDateFormat(OSTIRecord.DOI_DATE_FORMAT);
		record.setPublicationDate(dateFormatter.format(metaStudy.getPublishedDate()));

		//// Fill in contact info for the DOI record.
		record.setContactName(creatorUser.getFirstName() + " " + creatorUser.getLastName());
		record.setContactOrganization(creator.getAffiliatedInstitution());
		record.setContactEmail(creatorUser.getEmail());
		record.setContactPhone(creator.getPhone());

		// Set the optional OSTI/IAD fields.
		String desc = metaStudy.getAbstractText();

		//// Check if the description will need to be truncated.
		if (desc.length() > OSTIRecord.DOI_DESCRIPTION_MAX_LENGTH) {
			desc = desc.substring(0, OSTIRecord.DOI_DESCRIPTION_MAX_LENGTH);
		}

		record.setDescription(desc);
		record.setProjectNumbers(metaStudy.getPrefixId());
		record.setDoiInfix(BricsInstanceType.valueOf(modulesConstants.getModulesOrgName().trim().toUpperCase()));

		return record;
	}

	/**
	 * Constructs the URL of the landing page for this meta study.
	 * 
	 * @param metaStudy - The meta study, which is need for getting the ID.
	 * @return The fully qualified landing page URL for this meta study.
	 */
	private String getLandingPageForMetaStudy(MetaStudy metaStudy) {
		return modulesConstants.getModulesPublicURL() + META_STUDY_LANDING_URL_PATH + metaStudy.getId();
	}

	/**
	 * Translates a Study object to a OSTIRecord object, such that all of the required OSTI/IAD fields and some of the
	 * optional ones will be filled in with data from the given Study and Account objects.
	 * 
	 * @param study - The repository study object to read data from.
	 * @param creator - The account that will be set as the creator of the generated DOI record.
	 * @return A DOI record object constructed from the data from the passed in repository study and account objects.
	 * @throws IllegalArgumentException When there is a validation error from setting incorrect data in the OSTIRecord
	 *         object.
	 */
	private OSTIRecord studyToDoiRecord(Study study, Account creator) throws IllegalArgumentException {
		OSTIRecord record = new OSTIRecord();
		User creatorUser = creator.getUser();

		// Set the required OSTI/IAD fields.
		record.setTitle(study.getTitle());
		record.setCreators(creatorUser.getFullName());
		record.setPublisher(ModelConstants.DOI_PUBLISHER_VAL);
		record.setLandingPageUrl(getLandingPageForStudy(study));
		record.setProductType(OSTIProductType.COLLECTION);
		record.setProductTypeSpecific(DoiUtil.STUDY_PRODUCT_TYPE_SPECIFIC);

		//// Translate the publication date to a string in form of MM/dd/yyyy.
		SimpleDateFormat dateFormatter = new SimpleDateFormat(OSTIRecord.DOI_DATE_FORMAT);
		record.setPublicationDate(dateFormatter.format(study.getDateCreated()));

		//// Fill in contact info for the DOI record.
		record.setContactName(creatorUser.getFirstName() + " " + creatorUser.getLastName());
		record.setContactOrganization(creator.getAffiliatedInstitution());
		record.setContactEmail(creatorUser.getEmail());
		record.setContactPhone(creator.getPhone());

		// Set the optional OSTI/IAD fields.
		String desc = study.getAbstractText();

		//// Check if the description will need to be truncated.
		if (desc.length() > OSTIRecord.DOI_DESCRIPTION_MAX_LENGTH) {
			desc = desc.substring(0, OSTIRecord.DOI_DESCRIPTION_MAX_LENGTH);
		}

		record.setDescription(desc);
		record.setProjectNumbers(study.getPrefixedId());
		record.setDoiInfix(BricsInstanceType.valueOf(modulesConstants.getModulesOrgName().trim().toUpperCase()));

		return record;
	}

	/**
	 * Constructs the URL of the landing page for this study.
	 * 
	 * @param study - The study, which is need for getting the ID.
	 * @return The fully qualified landing page URL for this study.
	 */
	private String getLandingPageForStudy(Study study) {
		return modulesConstants.getModulesPublicURL() + STUDY_LANDING_URL_PATH + study.getId();
	}

	/**
	 * Translates a BasicDataset object to a OSTIRecord object, such that all of the required OSTI/IAD fields and some
	 * of the optional ones will be filled in with data from the given BasicDataset and Account objects.
	 * 
	 * @param dataset - The dataset object to read data from.
	 * @param creator - The account that will be set as the creator of the generated DOI record.
	 * @return A DOI record object constructed from the data from the passed in dataset and account objects.
	 * @throws IllegalArgumentException When there is a validation error from setting incorrect data in the OSTIRecord
	 *         object.
	 */
	private OSTIRecord dataSetToDoiRecord(BasicDataset dataset, Account creator) throws IllegalArgumentException {
		// TODO : Implement this method once the story for DOI integration with datasets is created.

		throw new IllegalArgumentException("Support for datasets is not implemented yet.");
	}

	public ModulesConstants getModulesConstants() {
		return modulesConstants;
	}

	public void setModulesConstants(ModulesConstants modulesConstants) {
		this.modulesConstants = modulesConstants;
	}

}
