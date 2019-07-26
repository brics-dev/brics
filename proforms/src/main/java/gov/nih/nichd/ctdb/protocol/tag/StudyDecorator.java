package gov.nih.nichd.ctdb.protocol.tag;

import gov.nih.nichd.ctdb.common.tag.ActionDecorator;
import gov.nih.nichd.ctdb.drugDevice.domain.DrugDevice;
import gov.nih.nichd.ctdb.site.domain.Site;

public class StudyDecorator extends ActionDecorator
{
	/**
	 * Generates the check boxes for the site table 
	 * 
	 * @return	The HTML code for the site check box
	 */
	public String getSiteIdCheckbox()
	{
		Site site = (Site) this.getObject();
		int siteId = site.getId();
		String html = "";
		
		html = "<input type=\"checkbox\" name=\"siteIdChkBox\" value=\"" + siteId + "\" />";
		
		return html;
	}
	
	/**
	 * Generates the check boxes for the drug device table
	 * 
	 * @return	The HTML code for the drug device check box
	 */
	public String getDrugDeviceIdCheckbox()
	{
		DrugDevice device = (DrugDevice) this.getObject();
		int drugDeviceId = device.getId();
		String html = "";
		
		html = "<input type=\"checkbox\" name=\"drugDeviceChkBox\" value=\"" + drugDeviceId + "\" />";
		
		return html;
	}
}
