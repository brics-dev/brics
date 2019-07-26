package gov.nih.nichd.ctdb.protocol.common;

import gov.nih.nichd.ctdb.common.CtdbAssembler;
import gov.nih.nichd.ctdb.common.CtdbLookup;
import gov.nih.nichd.ctdb.protocol.domain.Protocol;
import gov.nih.nichd.ctdb.protocol.form.StudyForm;

public class StudyAssembler extends CtdbAssembler
{
	public static void formToDomain(StudyForm form, Protocol study)
	{
		study.setId(form.getId());
		study.setProtocolNumber(form.getStudyNumber());
		study.setPrincipleInvestigator(form.getPrincipleInvestigator());
		study.setWelcomeUrl(form.getStudyUrl());
		study.setDescription(form.getDescription());
		study.setStatus(new CtdbLookup(form.getStatus()));
		study.setPatientDisplayType(form.getPatientDisplayType());
		study.setUseEbinder(form.isUseEBinder());
		study.setLockFormIntervals(form.isLockVisitTypes());
		study.setAutoAssociatePatientRoles(form.isAutoAssociatePatientRoles());
		study.setAutoIncrementSubject(form.isAutoIncrementSubject());
		
	}
	
	public static void domainToForm(Protocol study, StudyForm form)
	{
		
	}
}
