package gov.nih.nichd.ctdb.protocol.domain;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import gov.nih.nichd.ctdb.common.CtdbDomainObject;
import gov.nih.nichd.ctdb.common.TransformationException;

public class PrepopDataElement extends CtdbDomainObject {
	private static final long serialVersionUID = -5219912918863278446L;
	
	private String shortname;
	private String title;
	private String valueType;
	
	public PrepopDataElement(){
		super();
		shortname = "";
		title = "";
		valueType = "";
	}
	
	public PrepopDataElement(String shortName, String title) {
		super();
		this.shortname = shortName;
		this.title = title;
		this.valueType = "";
	}
	
	public String getShortName() {
		return shortname;
	}

	public void setShortName(String shortname){
		this.shortname = shortname;
	}
	
	public String getTitle() {
		return title;
	}

	public void setTitle(String title){
		this.title = title;
	}
	
	public String getValueType() {
		return valueType;
	}

	public void setValueType(String valueType){
		this.valueType = valueType;
	}
	
	@Override
	public Document toXML() throws TransformationException {
		try	{
			Document document = super.newDocument();
			Element root = super.initXML(document, "prepopdataelement");

			Element nameNode = document.createElement("shortname");
			nameNode.appendChild(document.createTextNode(this.shortname));
			root.appendChild(nameNode);

			Element titleNode = document.createElement("title");
			titleNode.appendChild(document.createTextNode(this.title));
			root.appendChild(titleNode);
			
			Element valueTypeNode = document.createElement("valueType");
			valueTypeNode.appendChild(document.createTextNode(this.valueType));
			root.appendChild(valueTypeNode);

			return document;

		} catch (Exception ex) {
			throw new TransformationException("Unable to transform object " + this.getClass().getName() + " with id = " + this.getId());
		}
	}

}
