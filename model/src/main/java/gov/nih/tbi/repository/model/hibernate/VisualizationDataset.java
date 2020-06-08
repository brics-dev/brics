package gov.nih.tbi.repository.model.hibernate;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.apache.commons.lang.StringUtils;

import com.google.gson.annotations.Expose;

import gov.nih.tbi.commons.model.DatasetStatus;
import gov.nih.tbi.commons.model.RecruitmentStatus;
import gov.nih.tbi.commons.model.StudyStatus;
import gov.nih.tbi.commons.model.StudyType;
import gov.nih.tbi.repository.model.SubmissionType;

/**
 * Model for dataset to use in Visualization
 * 
 * @author Ryan
 */


@Table(name = "DATASET")
@XmlRootElement(name = "dataset")
@XmlAccessorType(XmlAccessType.FIELD)
public class VisualizationDataset implements Serializable {

	private static final long serialVersionUID = -3963107794602437604L;
	
	private Integer id;
	
	@Expose
	private Integer studyId;
	
	private Study study;
	
	@Expose
	private DatasetStatus datasetStatus;
	
	@Expose
	private Integer datasetStatusId;
	
	@Expose
	private BigDecimal totalDataFileSize = BigDecimal.ZERO;

	public Integer getStudyId() {
		return studyId;
	}

	public void setStudyId(Integer studyId) {
		this.studyId = studyId;
	}

	public Study getStudy() {
		return study;
	}

	public void setStudy(Study study) {
		this.study = study;
	}

	public BigDecimal getTotalDataFileSize() {
		return totalDataFileSize;
	}

	public void setTotalDataFileSize(BigDecimal totalDataFileSize) {
		this.totalDataFileSize = totalDataFileSize;
	}

	public DatasetStatus getDatasetStatus() {
		return datasetStatus;
	}

	public void setDatasetStatus(DatasetStatus datasetStatus) {
		this.datasetStatus = datasetStatus;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}
	
	public Integer getDatasetStatusId() {
		return datasetStatusId;
	}

	public void setDatasetStatusId(Integer datasetStatusId) {
		this.datasetStatusId = datasetStatusId;
	}

	@Override
	public String toString() {
		return "VisualizationDataset [studyId=" + studyId + ", datasetStatus="+ datasetStatus+", totalDataFileSize=" + totalDataFileSize + "]";
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, studyId, study, datasetStatus, datasetStatusId, totalDataFileSize);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof VisualizationDataset)) {
			return false;
		}
		VisualizationDataset other = (VisualizationDataset) obj;
		return Objects.equals(id, other.id) && Objects.equals(studyId, other.studyId)
				&& Objects.equals(study, other.study) && Objects.equals(datasetStatus, other.datasetStatus)
				&& Objects.equals(datasetStatusId, other.datasetStatusId) 
				&& Objects.equals(totalDataFileSize, other.totalDataFileSize);
	}


	
}
