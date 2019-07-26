package gov.nih.nichd.ctdb.protocol.common;

import gov.nih.nichd.ctdb.attachments.domain.Attachment;
import gov.nih.nichd.ctdb.common.CtdbAssembler;
import gov.nih.nichd.ctdb.common.CtdbException;
import gov.nih.nichd.ctdb.ebinder.domain.Ebinder;
import gov.nih.nichd.ctdb.protocol.form.StudyEbinderForm;
import gov.nih.nichd.ctdb.util.domain.LookupType;
import gov.nih.nichd.ctdb.util.manager.LookupManager;

public class StudyEbinderAssembler extends CtdbAssembler
{
	public static void formToDomain(StudyEbinderForm form, Ebinder binder, LookupManager luMan) throws CtdbException
	{
		// Set binder members
		binder.setId(form.getId());
		binder.setStudyId(form.getStudyID());
		binder.setJsonTree(form.geteBinderTree());
		binder.setType(luMan.getLookup(LookupType.EBINDER_TYPE, form.getType()));
	}
	
	public static void domainToForm(Ebinder binder, StudyEbinderForm form)
	{
		// Set form fields
		form.setId(binder.getId());
		form.setStudyID(binder.getStudyId());
		form.seteBinderTree(binder.getJsonTree());
		form.setType(binder.getType().getId());
	}
	
	public static void formToFile(StudyEbinderForm form, Attachment file, LookupManager luMan) throws CtdbException
	{
		// Set attachment members
		file.setId(form.getAttachId());
		file.setName(form.getAttachName());
		file.setDescription(form.getAttachDescription());
		file.setAuthors(form.getAttachAuthor());
		file.setPublicationType(luMan.getLookup(LookupType.PUBLICATION_TYPE, form.getAttachPubType()));
		file.setUrl(form.getAttachUrl());
		file.setPubMedId(form.getAttachPubMedId());
		file.setFileName(form.getAttachFileName());
	}
	
	public static void fileToForm(Attachment file, StudyEbinderForm form)
	{
		form.setAttachId(file.getId());
		form.setAttachName(file.getName());
		form.setAttachDescription(file.getDescription());
		form.setAttachAuthor(file.getAuthors());
		form.setAttachPubType(file.getPublicationType().getId());
		form.setAttachUrl(file.getUrl());
		form.setAttachPubMedId(file.getPubMedId());
		form.setAttachFileName(file.getFileName());
	}
}
