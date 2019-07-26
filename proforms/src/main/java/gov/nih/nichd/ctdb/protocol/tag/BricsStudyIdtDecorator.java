package gov.nih.nichd.ctdb.protocol.tag;

import gov.nih.nichd.ctdb.common.tag.ActionIdtDecorator;
import gov.nih.nichd.ctdb.protocol.domain.BricsStudy;
import gov.nih.nichd.ctdb.util.common.SysPropUtil;

public class BricsStudyIdtDecorator extends ActionIdtDecorator
{
	public BricsStudyIdtDecorator()
	{
		super();
	}
	

	/**
	 * Generates the radio button for the current BRICS Study record.  The button will be
	 * disabled if the study is linked to another ProFoRMS study.
	 * 
	 * @return	The HTML tag for the BRICS Study radio button.
	 */
	public String getTitle()
	{
		BricsStudy study = (BricsStudy) getObject();
		String html = "";
		
		String title = study.getTitle();
		String studyId = study.getPrefixedId();
		
		String host = SysPropUtil.getProperty("brics.modules.home.url");
		//String host = "http://pdbp-dd-local.cit.nih.gov:8787/";
		
		String url = host + "portal/study/viewStudyAction!viewStudyCrossDomain.ajax?studyId=" + studyId;
		html = "<a class=tdLink href=\"Javascript:popupIFrame('"+studyId+"');\">" + title + "</a>";
		
		
		return html;
	}
}
