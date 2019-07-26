package gov.nih.tbi.dictionary.model.hibernate.eform;

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
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

@Entity
@Table(name = "VISUAL_SCALE")
@XmlRootElement(name = "VisualScale")
@XmlAccessorType(XmlAccessType.FIELD)
public class VisualScale implements Serializable{

	private static final long serialVersionUID = 2013536224675384047L;

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "VISUAL_SCALE_SEQ")
	@SequenceGenerator(name = "VISUAL_SCALE_SEQ", sequenceName = "VISUAL_SCALE_SEQ", allocationSize = 1)
	@XmlTransient
	private Long id;

	@Column(name = "START_RANGE")
	private Integer startRange;

	@Column(name = "END_RANGE")
	private Integer endRange;

	@Column(name = "WIDTH_MM")
	private Integer widthMM;

	@Column(name = "LEFT_TEXT")
	private String leftText;

	@Column(name = "RIGHT_TEXT")
	private String rightText;

	@Column(name = "CENTER_TEXT")
	private String centerText;

	@Column(name = "SHOW_HANDLE")
	private Boolean showHandle;
	
	public VisualScale(){}
	
	public VisualScale(VisualScale visualScale){
		this.setId(null);
		this.setStartRange(visualScale.getStartRange());
		this.setEndRange(visualScale.getEndRange());
		this.setWidthMM(visualScale.getWidthMM());
		this.setLeftText(visualScale.getLeftText());
		this.setRightText(visualScale.getRightText());
		this.setCenterText(visualScale.getCenterText());
		this.setShowHandle(visualScale.getShowHandle());
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Integer getStartRange() {
		return startRange;
	}

	public void setStartRange(Integer startRange) {
		this.startRange = startRange;
	}

	public Integer getEndRange() {
		return endRange;
	}

	public void setEndRange(Integer endRange) {
		this.endRange = endRange;
	}

	public Integer getWidthMM() {
		return widthMM;
	}

	public void setWidthMM(Integer widthMM) {
		this.widthMM = widthMM;
	}

	public String getLeftText() {
		return leftText;
	}

	public void setLeftText(String leftText) {
		this.leftText = leftText;
	}

	public String getRightText() {
		return rightText;
	}

	public void setRightText(String rightText) {
		this.rightText = rightText;
	}

	public String getCenterText() {
		return centerText;
	}

	public void setCenterText(String centerText) {
		this.centerText = centerText;
	}

	public Boolean getShowHandle() {
		return showHandle;
	}

	public void setShowHandle(Boolean showHandle) {
		this.showHandle = showHandle;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		VisualScale other = (VisualScale) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "VisualScale [VisualScale id=" + id + ", startRange=" + startRange + ", endRange=" + endRange + "]";
	}
}