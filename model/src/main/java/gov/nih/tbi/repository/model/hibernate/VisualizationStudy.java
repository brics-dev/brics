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

import gov.nih.tbi.commons.model.RecruitmentStatus;
import gov.nih.tbi.commons.model.StudyStatus;
import gov.nih.tbi.commons.model.StudyType;
import gov.nih.tbi.repository.model.SubmissionType;

/**
 * Model for studies to use in Visualization
 * 
 * @author Ryan
 */


@Table(name = "STUDY")
@XmlRootElement(name = "study")
@XmlAccessorType(XmlAccessType.FIELD)
public class VisualizationStudy implements Serializable {

	private static final long serialVersionUID = -3963107794602437604L;

	@Expose
	private Integer studyId;
	
	private Study study;
	
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

	@Override
	public String toString() {
		return "VisualizationStudy [studyId=" + studyId + ", totalDataFileSize=" + totalDataFileSize + "]";
	}




	
}
