package gov.nih.nichd.ctdb.protocol.tag;

import java.util.Iterator;
import java.util.List;

import javax.servlet.jsp.JspException;

import gov.nih.nichd.ctdb.common.CtdbException;
import gov.nih.nichd.ctdb.common.CtdbLookup;
import gov.nih.nichd.ctdb.protocol.domain.Interval;
import gov.nih.nichd.ctdb.util.domain.LookupType;
import gov.nih.nichd.ctdb.util.manager.LookupManager;
import gov.nih.tbi.dictionary.model.hibernate.eform.BasicEform;
import gov.nih.tbi.idt.ws.IdtDecorator;

/**
 * ProtocolListDecorator enables a table to display protocol information without
 * java coding.
 *
 * @author Booz Allen Hamilton
 * @version 1.0
 */
public class IntervalHomeDecorator extends IdtDecorator
{

	/** Creates a new instance of ProtocolListDecorator */
	public IntervalHomeDecorator()
	{
		super();
	}

	/**
	 * retrieves the checkbox
	 * 
	 * @return
	 */
	public String getIntervalIdCheckbox() {
		String text = "";
		Interval interval = (Interval) this.getObject();
		int id = interval.getId();
		
		text = "<input type=\"checkbox\" name=\"selectedIntervalId\" value=\"" + id + "\" />";


		return text;
	}

	/**
	 * Retrieves the interval Type of an interval.
	 *
	 * @return  String HTML string displaying the description on a Row
	 * @throws CtdbException 
	 */
	public String getIntervalTypeName() throws CtdbException
	{
		Interval visitType = (Interval) this.getObject();
		LookupManager luMan = new LookupManager();
		List<CtdbLookup> intervalTypes = luMan.getLookups(LookupType.INTERVAL_TYPE);
		String type = "";
		
		if ( visitType.getIntervalType() > 0 )
		{
			for ( CtdbLookup lookUp : intervalTypes )
			{
				if ( lookUp.getId() == visitType.getIntervalType() )
				{
					type = lookUp.getShortName();
					break;
				}
			}
		}
		
		return type;
	}


	/**
	 * Retrieves the interval Type of an interval.
	 *
	 * @return  String HTML string displaying the description on a Row
	 */
	public String getIntervalFormNames() throws JspException
	{
		Interval interval = (Interval) this.getObject();
		List<BasicEform> intervalFormList = interval.getIntervalEFormList();
		StringBuffer selectedForms = new StringBuffer(100);
		int counter = 1;
		int listSize = intervalFormList.size();
		
		for ( Iterator<BasicEform> it = intervalFormList.iterator(); it.hasNext(); )
		{
			BasicEform curr = it.next();
	        Long formId = curr.getId();
	        String url = this.getWebRoot() + "/form/viewFormDetail.action?source=popup&id=" + curr.getId();
	        
			selectedForms.append("<a href=\"Javascript:popupWindowWithMenu('" + url + " ');\">" + curr.getShortName()+ "</a>");
			counter++;
			
			// Add comma, "and", or neither
			if ( it.hasNext() )
			{
				if ( listSize == 2 )
				{
					selectedForms.append(" and ");
				}
				else if ( counter < listSize )
				{
					selectedForms.append(", ");
				}
				else
				{
					selectedForms.append(", and ");
				}
			}
		}
		
		return selectedForms.toString();
	}

	/**
	 * Retrieves the description of an interval in a way that the description length
	 *			is limited to about 90 characters on display.
	 *
	 * @return  String HTML string displaying the description on a Row
	 */
	public String getDescr()
	{
		Interval interval = (Interval) this.getObject();

		String description = interval.getDescription();

		if(description == null)
			return description;

		int index = description.indexOf(" ", 90);

		if(index != -1)
			return description.substring(0, index) + "...";

		else
			return description;

	}

}
