package gov.nih.nichd.ctdb.protocol.domain;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import gov.nih.nichd.ctdb.common.CtdbDomainObject;
import gov.nih.nichd.ctdb.common.TransformationException;
import gov.nih.nichd.ctdb.form.domain.Form;
import gov.nih.tbi.dictionary.model.hibernate.eform.BasicEform;

/**
 * Interval DomainObject for the NICHD CTDB Application
 *
 * @author  Booz Allen Hamilton
 * @version 1.0
 */
public class Interval extends CtdbDomainObject
{
	private static final long serialVersionUID = 6219800444334503277L;
	public static final String INTERVAL_ORDER_BY_NAME = "name";
	public static final String INTERVAL_ORDER_BY_ORDERVAL = "orderval";
	
	private String name;
	private String description;
	private int protocolId;
	private int intervalType;
	private String intervalTypeName;
	private String category;
	private List<Form> intervalFormList;
	private List<BasicEform> intervalEFormList;
	private List<PrepopDataElement> prepopDateElementList;
	private List<IntervalClinicalPoint> intervalClinicalPointList;
	
	private int selfReportStart;
	private int selfReportEnd;

	private Integer orderval;

	/**
	 * Default Constructor for the Interval Domain Object
	 */
	public Interval()
	{
		super();
		name = "";
		description = "";
		protocolId = Integer.MIN_VALUE;
		intervalType = 0;
		intervalTypeName = "";
		category = "";
		intervalFormList = new LinkedList<Form>();
		intervalEFormList = new LinkedList<BasicEform>();
		selfReportStart = 15;
		selfReportEnd = 15;
		prepopDateElementList = new LinkedList<PrepopDataElement>();
		intervalClinicalPointList = new ArrayList<IntervalClinicalPoint>();
	}

	/**
	 * Gets the interval's name
	 *
	 * @return The interval's name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Sets the interval's name
	 *
	 * @param name The interval's name
	 */
	public void setName(String name) {
		if ( name != null ) {
			this.name = name;
		}
		else {
			this.name = "";
		}
	}

	/**
	 * Gets the interval's description
	 *
	 * @return The protocol's description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Sets the protocol's description
	 *
	 * @param description The protocol's description
	 */
	public void setDescription(String description) {
		if ( description != null ) {
			this.description = description;
		}
		else {
			this.description = "";
		}
	}

	/**
	 * Gets the protocol's ID
	 *
	 * @return Protocol ID
	 */
	public int getProtocolId()
	{
		return protocolId;
	}

	/**
	 * Sets the protocol associated with the interval.  An interval
	 * can only be associated with one protocol.
	 *
	 * @param protocolId The protocol ID to associate the interval with.
	 */
	public void setProtocolId(int protocolId)
	{
		this.protocolId = protocolId;
	}

	/**
	 * Determines if an object is equal to the current Interval Object.
	 * Equal is based on if the interval name are equal.
	 *
	 * @param   o The object to determine if it is equal to the current Interval
	 * @return  True if the object is equal to the Interval.
	 *          False if the object is not equal to the Interval.
	 */
	public boolean equals(Object o)
	{
		if ( this == o )
		{
			return true;
		}
		
		if( !(o instanceof Interval) )
		{
			return false;
		}
		
		// Check interval
		final Interval interval = (Interval) o;
		
		return name.equals(interval.name) && (intervalType == interval.intervalType) && category.equals(interval.category) && 
				description.equals(interval.description);
	}

	/**
	 * This method allows the transformation of a Interval into an XML Document.
	 * If no implementation is available at this time,
	 * an UnsupportedOperationException will be thrown.
	 *
	 * @return      XML Document
	 * @throws   TransformationException is thrown if there is an
	 *              error during the XML tranformation
	 */
	public Document toXML() throws TransformationException
	{
		try
		{
			Document document = super.newDocument();
			Element root = super.initXML(document, "interval");

			Element nameNode = document.createElement("name");
			nameNode.appendChild(document.createTextNode(this.name));
			root.appendChild(nameNode);

			Element descNode = document.createElement("description");
			descNode.appendChild(document.createTextNode(this.description));
			root.appendChild(descNode);

			return document;

		}
		catch (Exception ex)
		{
			throw new TransformationException("Unable to transform object " + this.getClass().getName() + " with id = " + this.getId());
		}
	}
	
	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		if ( category != null ) {
			this.category = category;
		}
		else {
			this.category = "";
		}
	}

	public List<Form> getIntervalFormList() {
		return intervalFormList;
	}

	public void setIntervalFormList(List<Form> intervalFormList) {
		this.intervalFormList.clear();
		
		if ( intervalFormList != null ) {
			this.intervalFormList.addAll(intervalFormList);
		}
	}


	public int getIntervalType() {
		return intervalType;
	}

	public void setIntervalType(int intervalType) {
		this.intervalType = intervalType;
	}

	public String getIntervalTypeName() {
		return intervalTypeName;
	}

	public int getSelfReportStart() {
		return selfReportStart;
	}

	public void setSelfReportStart(int selfReportStart) {
		this.selfReportStart = selfReportStart;
	}

	public int getSelfReportEnd() {
		return selfReportEnd;
	}

	public void setSelfReportEnd(int selfReportEnd) {
		this.selfReportEnd = selfReportEnd;
	}

	public void setIntervalTypeName(String intervalTypeName) {
		this.intervalTypeName = intervalTypeName;
	}

	public List<BasicEform> getIntervalEFormList() {
		return intervalEFormList;
	}

	public void setIntervalEFormList(List<BasicEform> intervalEFormList) {
		this.intervalEFormList.clear();
		
		if ( intervalEFormList != null ) {
			this.intervalEFormList.addAll(intervalEFormList);
		}
	}
	
	public List<PrepopDataElement> getPrepopulateDEList() {
		return prepopDateElementList;
	}

	public void setPrepopulateDEList(List<PrepopDataElement> prepopDateElementList) {
		this.prepopDateElementList.clear();
		
		if ( prepopDateElementList != null ) {
			this.prepopDateElementList.addAll(prepopDateElementList);
		}
	}
	
	public List<IntervalClinicalPoint> getIntervalClinicalPointList() {
		return intervalClinicalPointList;
	}

	public void setIntervalClinicalPointList(List<IntervalClinicalPoint> intervalClinicalPointList) {
		this.intervalClinicalPointList.clear();
		
		if ( intervalClinicalPointList != null ) {
			this.intervalClinicalPointList.addAll(intervalClinicalPointList);
		}
	}
	
	public Integer getOrderval() {
		return orderval;
	}

	public void setOrderval(Integer orderval) {
		this.orderval = orderval;
	}
	
}
