package gov.nih.nichd.ctdb.protocol.tag;

import gov.nih.nichd.ctdb.common.tag.ActionDecorator;
import gov.nih.nichd.ctdb.contacts.domain.ExternalContact;
import gov.nih.nichd.ctdb.util.domain.Address;

public class StudyContactDecorator extends ActionDecorator
{
	public String getcontactsCheckBox()
	{
		ExternalContact ec = (ExternalContact) getObject();
		String html = "<input type=\"checkbox\" name=\"contactChkBx\" value=\"" + ec.getId() + "\"";
		
		return html;
	}
	
	public String getDisplayableAddress()
	{
		Address a = ((ExternalContact) getObject()).getAddress();
		String address = "";
		
		// Construct a string representation of the address object
		if ( !a.getAddressOne().isEmpty() )
		{
			address = a.getAddressOne();
			
			// Check if a comma is needed
			if ( !a.getAddressTwo().isEmpty() || !a.getCity().isEmpty() || (a.getState().getId() > 0) || 
					!a.getZipCode().isEmpty() || (a.getCountry().getId() > 0) )
			{
				address += ", ";
			}
		}
		
		if ( !a.getAddressTwo().isEmpty() )
		{
			address += a.getAddressTwo();
			
			// Check if a comma is needed
			if ( !a.getCity().isEmpty() || (a.getState().getId() > 0) || !a.getZipCode().isEmpty() || (a.getCountry().getId() > 0) )
			{
				address += ", ";
			}
		}
		
		if ( !a.getCity().isEmpty() )
		{
			address += a.getCity();
			
			// Check if a comma is needed
			if ( (a.getState().getId() > 0) || !a.getZipCode().isEmpty() || (a.getCountry().getId() > 0) )
			{
				address += ", ";
			}
		}
		
		if ( a.getState().getId() > 0 )
		{
			address += a.getState().getShortName();
			
			// Check if comma or space is needed
			if ( a.getZipCode().isEmpty() && (a.getCountry().getId() > 0) )
			{
				address += ", ";
			}
			else if ( !a.getZipCode().isEmpty() )
			{
				address += " ";
			}
		}
		
		if ( !a.getZipCode().isEmpty() )
		{
			address += a.getZipCode();
			
			// Check if a comma is needed
			if ( a.getCountry().getId() > 0 )
			{
				address += ", ";
			}
		}
		
		if ( a.getCountry().getId() > 0 )
		{
			address += a.getCountry().getShortName();
		}
		
		return address;
	}
}
