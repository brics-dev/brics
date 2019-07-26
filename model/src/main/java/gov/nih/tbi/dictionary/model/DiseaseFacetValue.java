
package gov.nih.tbi.dictionary.model;

public class DiseaseFacetValue
{

    private String disease;
    private String domain;
    private String subdomain;

    public String getDisease()
    {

        return disease;
    }

    public void setDisease(String disease)
    {

        this.disease = disease;
    }

    public String getDomain()
    {

        return domain;
    }

    public void setDomain(String domain)
    {

        this.domain = domain;
    }

    public String getSubdomain()
    {

        return subdomain;
    }

    public void setSubdomain(String subdomain)
    {

        this.subdomain = subdomain;
    }

    @Override
    public int hashCode()
    {

        final int prime = 31;
        int result = 1;
        result = prime * result + ((disease == null) ? 0 : disease.hashCode());
        result = prime * result + ((domain == null) ? 0 : domain.hashCode());
        result = prime * result + ((subdomain == null) ? 0 : subdomain.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {

        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        DiseaseFacetValue other = (DiseaseFacetValue) obj;
        if (disease == null)
        {
            if (other.disease != null)
                return false;
        }
        else if (!disease.equals(other.disease))
            return false;
        if (domain == null)
        {
            if (other.domain != null)
                return false;
        }
        else if (!domain.equals(other.domain))
            return false;
        if (subdomain == null)
        {
            if (other.subdomain != null)
                return false;
        }
        else if (!subdomain.equals(other.subdomain))
            return false;
        return true;
    }

    @Override
    public String toString()
    {

        return "DiseaseFacetValue [disease=" + disease + ", domain=" + domain + ", subdomain=" + subdomain + "]";
    }

}
