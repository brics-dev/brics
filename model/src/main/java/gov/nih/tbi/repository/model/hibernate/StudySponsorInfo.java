package gov.nih.tbi.repository.model.hibernate;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Entity
@Table(name = "STUDY_SPONSOR_INFO")
public class StudySponsorInfo implements Serializable {

	private static final long serialVersionUID = -6057854527295110692L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "STUDY_SPONSOR_INFO_SEQ")
    @SequenceGenerator(name = "STUDY_SPONSOR_INFO_SEQ", sequenceName = "STUDY_SPONSOR_INFO_SEQ", allocationSize = 1)
    private Long id;

    @Column(name = "FDA_IND", nullable = false)
    private String fdaInd;

    @Column(name = "SPONSOR")
    private String sponsor;

    public StudySponsorInfo() { 
    }
    
    public StudySponsorInfo(StudySponsorInfo sponsorInfo) {
    	this.fdaInd = sponsorInfo.fdaInd;
    	this.sponsor = sponsorInfo.sponsor;
    }

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getFdaInd() {
		return fdaInd;
	}

	public void setFdaInd(String fdaInd) {
		this.fdaInd = fdaInd;
	}

	public String getSponsor() {
		return sponsor;
	}

	public void setSponsor(String sponsor) {
		this.sponsor = sponsor;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((fdaInd == null) ? 0 : fdaInd.hashCode());
		result = prime * result + ((sponsor == null) ? 0 : sponsor.hashCode());
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
		
		StudySponsorInfo other = (StudySponsorInfo) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		
		if (fdaInd == null) {
			if (other.fdaInd != null)
				return false;
		} else if (!fdaInd.equals(other.fdaInd))
			return false;
		
		if (sponsor == null) {
			if (other.sponsor != null)
				return false;
		} else if (!sponsor.equals(other.sponsor))
			return false;
		
		return true;
	}
	
}
