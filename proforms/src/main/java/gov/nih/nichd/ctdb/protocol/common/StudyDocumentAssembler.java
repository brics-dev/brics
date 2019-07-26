package gov.nih.nichd.ctdb.protocol.common;

import gov.nih.nichd.ctdb.attachments.domain.Attachment;
import gov.nih.nichd.ctdb.common.CtdbAssembler;
import gov.nih.nichd.ctdb.common.CtdbLookup;
import gov.nih.nichd.ctdb.protocol.form.StudyDocumentForm;

public class StudyDocumentAssembler extends CtdbAssembler
{
	public static void formToDomain(StudyDocumentForm form, Attachment attach)
	{
		// Set form fields
		attach.setId(form.getId());
		attach.setAssociatedId(form.getStudyId());
		attach.setName(form.getTitle());
		attach.setAuthors(form.getAuthors());
		attach.setDescription(form.getDescription());
		attach.setPublicationType(new CtdbLookup(form.getPublicationType()));
		attach.setUrl(form.getUrl());
		attach.setPubMedId(form.getPubmedId());
	}
	
	public static void domainToForm(Attachment attach, StudyDocumentForm form)
	{
		// Set form fields
		form.setId(attach.getId());
		form.setStudyId(attach.getAssociatedId());
		form.setTitle(attach.getName());
		form.setAuthors(attach.getAuthors());
		form.setDescription(attach.getDescription());
		form.setPublicationType(attach.getPublicationType().getId());
		form.setUrl(attach.getUrl());
		form.setPubmedId(attach.getPubMedId());
		form.setFileName(attach.getFileName());
	}
}
