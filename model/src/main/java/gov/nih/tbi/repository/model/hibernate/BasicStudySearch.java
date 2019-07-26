package gov.nih.tbi.repository.model.hibernate;

import java.io.Serializable;
import java.math.BigInteger;

import com.google.gson.annotations.Expose;

public class BasicStudySearch implements Serializable {

	private static final long serialVersionUID = -3963107794602437604L;

	@Expose
	private Integer id;

	@Expose
	private String title;

	@Expose
	private String abstractText;
	
	@Expose
	private String principleName = "";
	
	@Expose
	private String institution = "";
	
	@Expose
	private String fundingSource = "";
	
	@Expose
	private BigInteger privateDatasetCount;
	
	@Expose
	private BigInteger sharedDatasetCount;
	
	@Expose
	private BigInteger clinicalDataCount;
	
	@Expose
	private BigInteger genomicDataCount;
	
	@Expose
	private BigInteger imagingDataCount;

	public BasicStudySearch() {

	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getAbstractText() {
		return abstractText;
	}

	public void setAbstractText(String abstractText) {
		this.abstractText = abstractText;
	}

	public String getPrincipleName() {
		return principleName;
	}

	public void setPrincipleName(String principleName) {
		this.principleName = principleName;
	}

	public String getInstitution() {
		return institution;
	}

	public void setInstitution(String institution) {
		this.institution = institution;
	}

	public String getFundingSource() {
		return fundingSource;
	}

	public void setFundingSource(String fundingSource) {
		this.fundingSource = fundingSource;
	}

	public BigInteger getPrivateDatasetCount() {
		return privateDatasetCount;
	}

	public void setPrivateDatasetCount(BigInteger privateDatasetCount) {
		this.privateDatasetCount = privateDatasetCount;
	}

	public BigInteger getSharedDatasetCount() {
		return sharedDatasetCount;
	}

	public void setSharedDatasetCount(BigInteger sharedDatasetCount) {
		this.sharedDatasetCount = sharedDatasetCount;
	}

	public BigInteger getClinicalDataCount() {
		return clinicalDataCount;
	}

	public void setClinicalDataCount(BigInteger clinicalDataCount) {
		this.clinicalDataCount = clinicalDataCount;
	}

	public BigInteger getGenomicDataCount() {
		return genomicDataCount;
	}

	public void setGenomicDataCount(BigInteger genomicDataCount) {
		this.genomicDataCount = genomicDataCount;
	}

	public BigInteger getImagingDataCount() {
		return imagingDataCount;
	}

	public void setImagingDataCount(BigInteger imagingDataCount) {
		this.imagingDataCount = imagingDataCount;
	}

	@Override
	public String toString() {
		return "BasicStudySearch [title=" + title + ", abstractText=" + abstractText + "]";
	}
	
}
