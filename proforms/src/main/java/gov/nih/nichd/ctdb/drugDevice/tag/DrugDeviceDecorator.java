package gov.nih.nichd.ctdb.drugDevice.tag;

import gov.nih.nichd.ctdb.common.tag.ActionDecorator;
import gov.nih.nichd.ctdb.drugDevice.domain.DrugDevice;

/**
 * Developed by CIT.
 * @author Shashi Rudrappa
 * Date: May 01, 2012
 * @version 1.0
 */
public class DrugDeviceDecorator extends ActionDecorator {
	/**
	 * returns the site id display string
	 * @return  String, the status
	 */
	public String getDrugDeviceIdCheckbox() {
		DrugDevice domainObject = (DrugDevice) this.getObject();
		int drugDeviceId = domainObject.getId();
		
		return "<input type=\"checkbox\" value=\"" + drugDeviceId + "\" />";
	}
}
