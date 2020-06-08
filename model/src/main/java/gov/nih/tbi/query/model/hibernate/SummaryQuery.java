package gov.nih.tbi.query.model.hibernate;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Java representation of the contents of the summary_query table.
 * 
 * @author Bill Puschmann
 *
 */
@Entity
@Table(name = "summary_query")
@XmlRootElement(name = "summaryQuery")
public class SummaryQuery implements Serializable {

	private static final long serialVersionUID = 3342317590409505018L;

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SUMMARY_QUERY_SEQ")
	@SequenceGenerator(name = "SUMMARY_QUERY_SEQ", sequenceName="SAVED_QUERY_SEQ", allocationSize = 1)
	private Long id;
	
	/**
	 * Name by which the query is called - for reference
	 */
	@Column(name="SHORTNAME")
	private String shortname;
	
	/**
	 * The query itself
	 */
	@Column(name="QUERY")
	private String query;
	
	/**
	 * Whether or not the query contains the SITE keyword that must be replaced before execution.
	 */
	@Column(name="REQUIRES_SITE")
	private Boolean requiresSite;

	/**
	 * Whether or not the query contains the STUDY keyword that must be replaced before execution.
	 */
	@Column(name="REQUIRES_STUDY")
	private Boolean requiresStudy;
	
	/**
	 * Whether or not the query contains the Number keyword that must be replaced before execution.
	 */
	@Column(name="REQUIRES_NUMBER")
	private Boolean requiresNumber;
	
	
	/**
	 * Whether or not the query contains the Text keyword that must be replaced before execution.
	 */
	@Column(name="REQUIRES_TEXT")
	private Boolean requiresText;
	
	
	/**
	 * Whether or not the query contains the should output in sparql json.
	 */
	@Column(name="REQUIRES_SPARQL")
	private Boolean requiresSparql;
	
	
	/**
	 * What instance does this belong to.
	 */
	@Column(name="INSTANCE")
	private String instance;
	
	/**
	 * Whether or not the query is used for summary data file.
	 */
	@Column(name="FORSUMMARYDATA")
	private Boolean forSummaryData;
	
	/**
	 * Whether or not the query contains the should output in postgres json.
	 */
	@Column(name="REQUIRES_POSTGRES")
	private Boolean requiresPostgres;
	
	/**
	 * Whether or not the query contains the should output to get proforms data.
	 */
	@Column(name="REQUIRES_PROFORMS")
	private Boolean requiresProforms;
	
	@Column(name="NEEDS_OBJECT_MAPPING")
	private Boolean NeedsObjectMapping;

	public SummaryQuery() { super(); }

	public Long getId() {
		return id;
	}
	public void setId(Long id) {
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
	
	public String getInstance() {
		return instance;
	}

	public void setInstance(String instance) {
		this.instance = instance;
	}

	public Boolean getForSummaryData() {
		return forSummaryData;
	}

	public void setForSummaryData(Boolean forSummaryData) {
		this.forSummaryData = forSummaryData;
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

	@Override
	public String toString() {
		return "SummaryQuery [id=" + id + ", name="+shortname+"]";
	}

	public Boolean getNeedsObjectMapping() {
		return NeedsObjectMapping;
	}

	public void setNeedsObjectMapping(Boolean needsObjectMapping) {
		NeedsObjectMapping = needsObjectMapping;
	}

	
}
