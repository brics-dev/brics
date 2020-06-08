package gov.nih.nichd.ctdb.response.domain;

import org.w3c.dom.Document;

import gov.nih.nichd.ctdb.common.CtdbDomainObject;
import gov.nih.nichd.ctdb.common.TransformationException;

public class SummaryQueryPF extends CtdbDomainObject {


	/**
	 * 
	 */
	private static final long serialVersionUID = -4009814095985498436L;

	private Integer id;
	
	/**
	 * Name by which the query is called - for reference
	*/
	private String shortname;
	
	/**
	 * The query itself
	 */
	private String query;
	
	/**
	 * Whether or not the query contains the SITE keyword that must be replaced before execution.
	 */
	private Boolean requiresSite;
	
	/**
	 * Whether or not the query contains the STUDY keyword that must be replaced before execution.
	 */
	private Boolean requiresStudy;
	
	/**
	 * Whether or not the query contains the Number keyword that must be replaced before execution.
	 */
	private Boolean requiresNumber;
	
	/**
	 * Whether or not the query contains the Text keyword that must be replaced before execution.
	 */
	private Boolean requiresText;
	
	/**
	 * Whether or not the query contains the should output in postgres json.
	 */
	private Boolean requiresPostgres;
	
	/**
	 * Whether or not the query contains the should output to get proforms data.
	 */
	private Boolean requiresProforms;
	
	/**
	 * Whether or not the query contains the should output in sparql json.
	 */
	private Boolean requiresSparql;
	
	public int getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public String getShortname() {
		return shortname;
	}
	public void setShortname(String shortname) {
		this.shortname = shortname;
	}
	public String getQuery() {
		return query;
	}
	public void setQuery(String query) {
		this.query = query;
	}
	
	public Boolean getRequiresSite() {
		return requiresSite;
	}

	public void setRequiresSite(Boolean requiresSite) {
		this.requiresSite = requiresSite;
	}

	public Boolean getRequiresStudy() {
		return requiresStudy;
	}

	public void setRequiresStudy(Boolean requiresStudy) {
		this.requiresStudy = requiresStudy;
	}

	public Boolean getRequiresNumber() {
		return requiresNumber;
	}

	public void setRequiresNumber(Boolean requiresNumber) {
		this.requiresNumber = requiresNumber;
	}
	
	public Boolean getRequiresText() {
		return requiresText;
	}

	public void setRequiresText(Boolean requiresText) {
		this.requiresText = requiresText;
	}
	
	public Boolean getRequiresSparql() {
		return requiresSparql;
	}

	public void setRequiresSparql(Boolean requiresSparql) {
		this.requiresSparql = requiresSparql;
	}
	
	public Boolean getRequiresPostgres() {
		return requiresPostgres;
	}

	public void setRequiresPostgres(Boolean requiresPostgres) {
		this.requiresPostgres = requiresPostgres;
	}
	
	public Boolean getRequiresProforms() {
		return requiresProforms;
	}

	public void setRequiresProforms(Boolean requiresProforms) {
		this.requiresProforms = requiresProforms;
	}
    /**
     * This method allows the transformation of a EventData into an XML Document.
     * If no implementation is available at this time,
     * an UnsupportedOperationException will be thrown.
     *
     * @return XML Document
     * @throws TransformationException is thrown if there is an error during the XML tranformation
     * @throws UnsupportedOperationException is thrown if this method is currently unsupported and not implemented.
     */
	public Document toXML() throws TransformationException {
		// TODO Auto-generated method stub
		 throw new UnsupportedOperationException("No Implementation at this time for the method toXML() in EventData.");
	}

}