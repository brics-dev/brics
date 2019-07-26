package gov.nih.tbi.repository.model.hibernate;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

import com.google.gson.annotations.Expose;


@Entity
@Table(name = "REPORT_TYPE")
@XmlAccessorType(XmlAccessType.FIELD)
public class ReportType implements Serializable {

	private static final long serialVersionUID = -5018328839361182532L;

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "REPORT_TYPE_SEQ")
	@SequenceGenerator(name = "REPORT_TYPE_SEQ", sequenceName = "REPORT_TYPE_SEQ", allocationSize = 1)
	private Long id;

	@Expose
	@Column(name = "report_name")
	private String reportName;

	@Expose
	@Column(name = "report_type")
	private String reportType;

	@Expose
	@Column(name = "instance")
	private String instance;


	@Expose
	@Column(name = "is_active")
	private Boolean isActive;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getReportName() {
		return reportName;
	}

	public void setReportName(String reportName) {
		this.reportName = reportName;
	}

	public String getReportType() {
		return reportType;
	}

	public void setReportType(String reportType) {
		this.reportType = reportType;
	}

	public String getInstance() {
		return instance;
	}

	public void setInstance(String instance) {
		this.instance = instance;
	}

	public Boolean getIsActive() {
		return isActive;
	}

	public void setIsActive(Boolean isActive) {
		this.isActive = isActive;
	}

}
