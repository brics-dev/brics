
package gov.nih.tbi.dictionary.model.hibernate;

import java.io.Serializable;

import gov.nih.tbi.dictionary.model.rdf.SemanticDataElement;

public class SubDomainElement implements Serializable
{

    private static final long serialVersionUID = -1787758936952090370L;

    private Disease disease;
    private Domain domain;
    private SubDomain subDomain;

    public SubDomainElement()
    {

    }

    public SubDomainElement(Disease disease, Domain domain, SubDomain subDomain)
    {

        this.disease = disease;
        this.domain = domain;
        this.subDomain = subDomain;
    }

    public Disease getDisease()
    {

        return disease;
    }

    public void setDisease(Disease disease)
    {

        this.disease = disease;
    }

    public Domain getDomain()
    {

        return domain;
    }

    public void setDomain(Domain domain)
    {

        this.domain = domain;
    }

    public SubDomain getSubDomain()
    {

        return subDomain;
    }

    public void setSubDomain(SubDomain subDomain)
    {

        this.subDomain = subDomain;
    }

    /**
     * 
     * The getMultiFields method gather the string representation of the Disease, Subgroup and Classification
     * 
     * The string will be "{Disease},{Domain},{SubDomain}"
     * 
     * @return
     */
    public String getMultiFields()
    {

        StringBuilder multipleField = new StringBuilder();
        multipleField.append(getDisease().getName() + "," + getDomain().getName() + "," + getSubDomain().getName());
        return multipleField.toString();
    }
    
    public String getMultiFieldsWithDes()
    {
    	 StringBuilder multipleField = new StringBuilder();
         multipleField.append("Disease: "+ getDisease().getName() + "|Domain: " + getDomain().getName() + "|Sub domain: " + getSubDomain().getName());
         return multipleField.toString();
    }

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((disease == null) ? 0 : disease.hashCode());
		result = prime * result + ((domain == null) ? 0 : domain.hashCode());
		result = prime * result + ((subDomain == null) ? 0 : subDomain.hashCode());
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
		SubDomainElement other = (SubDomainElement) obj;
		if (disease == null) {
			if (other.disease != null)
				return false;
		} else if (!disease.equals(other.disease))
			return false;
		if (domain == null) {
			if (other.domain != null)
				return false;
		} else if (!domain.equals(other.domain))
			return false;
		if (subDomain == null) {
			if (other.subDomain != null)
				return false;
		} else if (!subDomain.equals(other.subDomain))
			return false;
		return true;
	}
    
}
