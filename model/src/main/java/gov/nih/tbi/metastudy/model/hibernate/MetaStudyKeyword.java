
package gov.nih.tbi.metastudy.model.hibernate;

import gov.nih.tbi.repository.model.hibernate.Keyword;

import java.io.Serializable;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue(value="M")
public class MetaStudyKeyword extends Keyword implements Serializable
{

	private static final long serialVersionUID = -1047590153446644157L;

    /**********************************************************************/
    public MetaStudyKeyword()
    {

    }

    public MetaStudyKeyword(MetaStudyKeyword keyword)
    {

        this.id = keyword.getId();
        this.keyword = keyword.getKeyword();
        this.count = keyword.getCount();
    }

    public MetaStudyKeyword(String keyword)
    {

        this.keyword = keyword;
    }

    public Long getId()
    {

        return id;
    }

    public void setId(Long id)
    {

        this.id = id;
    }

    public String getKeyword()
    {

        return keyword;
    }

    public void setKeyword(String keyword)
    {

        this.keyword = keyword;
    }

    public Long getCount()
    {

        return count;
    }

    public void setCount(Long count)
    {

        this.count = count;
    }
    
    @Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((keyword == null) ? 0 : keyword.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof MetaStudyKeyword))
			return false;
		MetaStudyKeyword other = (MetaStudyKeyword) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (keyword == null) {
			if (other.keyword != null)
				return false;
		} else if (!keyword.equals(other.keyword))
			return false;
		return true;
	}
}
