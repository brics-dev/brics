package gov.nih.nichd.ctdb.site.tag;

import javax.servlet.jsp.JspException;

import gov.nih.nichd.ctdb.common.tag.ActionDecorator;
import gov.nih.nichd.ctdb.site.domain.Site;

/**
 * Created by IntelliJ IDEA.
 * User: breymaim
 * Date: Nov 28, 2007
 * Time: 8:57:47 AM
 * To change this template use File | Settings | File Templates.
 */
public class SiteHomeDecorator extends ActionDecorator {

    public String getDesc () throws JspException {
        Site s = (Site) this.getObject();
        if (s.getDescription() != null && s.getDescription().length() > 80) {
            return s.getDescription().substring(0, 80);
        } else {
            return s.getDescription();
        }
    }

	/**
	 * returns the site id display string
	 * @return  String, the status
	 */
	public String getSiteIdCheckbox()
	{
		Site domainObject = (Site) this.getObject();
		int siteId = domainObject.getId();
		return "<input type=\"checkbox\" value=\"" + siteId + "\" />";
	}
	
	
	/**
	 * returns the site id display string
	 * @return  String, the status
	 */
	public String getPiFullName()
	{
		StringBuffer piFullName = new StringBuffer(200);
		Site domainObject = (Site) this.getObject();
        
		
        if (domainObject.getSitePI().getId() > 0 ) {
        	if((domainObject.getSitePI().getFirstName() != null && !domainObject.getSitePI().getFirstName().equals("")) || (domainObject.getSitePI().getLastName() != null && !domainObject.getSitePI().getLastName().equals(""))){
                if (domainObject.getSitePI().getLastName() != null && !domainObject.getSitePI().getLastName().equals("")) {
                	piFullName.append(domainObject.getSitePI().getLastName());
                }
                if (domainObject.getSitePI().getFirstName() != null && !domainObject.getSitePI().getFirstName().equals("")) {
                	piFullName.append(", "+domainObject.getSitePI().getFirstName());
                }
        	} else if(domainObject.getSitePI().getCtdbLookupStringForDisplay() != null && !domainObject.getSitePI().getCtdbLookupStringForDisplay().equals("")){
        		piFullName.append(domainObject.getSitePI().getCtdbLookupStringForDisplay());
        	} 
        }		
		
		

		return piFullName.toString();
	}
	
	/**
	 * returns the site id display string
	 * @return  String, the status
	 */
	public String getAddress()
	{
		StringBuffer address = new StringBuffer(2000);
		Site domainObject = (Site) this.getObject();
        
        if (domainObject.getAddress().getAddressOne() != null && !domainObject.getAddress().getAddressOne().equals("")) {
        	address.append(domainObject.getAddress().getAddressOne());
        }
        if (address.toString() !=null && !address.toString().equals("") && domainObject.getAddress().getAddressTwo() != null && !domainObject.getAddress().getAddressTwo().equals("")) {
        	address.append(", ");
        }
        if (domainObject.getAddress().getAddressTwo() != null && !domainObject.getAddress().getAddressTwo().equals("")) {
        	address.append(domainObject.getAddress().getAddressTwo());
        }
        if (address.toString() !=null && !address.toString().equals("") && domainObject.getAddress().getCity() != null && !domainObject.getAddress().getCity().equals("")) {
        	address.append(", ");
        }
        if (domainObject.getAddress().getCity() != null && !domainObject.getAddress().getCity().equals("")) {
        	address.append(domainObject.getAddress().getCity());
        }
        if (address.toString() !=null && !address.toString().equals("") && domainObject.getAddress().getState().getId() > 0) {
        	address.append(", ");
        }
        if (domainObject.getAddress().getState().getId() > 0 ) {
        	if(domainObject.getAddress().getState().getLongName() != null && !domainObject.getAddress().getState().getLongName().equals("")){
        		address.append(domainObject.getAddress().getState().getLongName());
        	} else if(domainObject.getAddress().getCtdbLookupStringForStateDisplay() != null && !domainObject.getAddress().getCtdbLookupStringForStateDisplay().equals("")){
        		address.append(domainObject.getAddress().getCtdbLookupStringForStateDisplay());
        	} 
        }
        if (address.toString() !=null && !address.toString().equals("") && domainObject.getAddress().getZipCode() != null && !domainObject.getAddress().getZipCode().equals("")){
        	address.append(" - ");
        }
        if (domainObject.getAddress().getZipCode() != null && !domainObject.getAddress().getZipCode().equals("")) {
        	address.append(domainObject.getAddress().getZipCode());
        }
        if (address.toString() !=null && !address.toString().equals("") && domainObject.getAddress().getCountry().getId() > 1){
        	address.append(", ");
        }
        if (domainObject.getAddress().getCountry().getId() > 1) {
        	if(domainObject.getAddress().getCountry().getShortName() != null && !domainObject.getAddress().getCountry().getShortName().equals("")){
        		address.append(domainObject.getAddress().getCountry().getLongName());
        	} else if(domainObject.getAddress().getCtdbLookupStringForCountryDisplay() != null && !domainObject.getAddress().getCtdbLookupStringForCountryDisplay().equals("")){
        		address.append(domainObject.getAddress().getCtdbLookupStringForCountryDisplay());
        	} 
        }
		return address.toString();
	}
}
