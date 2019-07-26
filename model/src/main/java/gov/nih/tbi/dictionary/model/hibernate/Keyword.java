
package gov.nih.tbi.dictionary.model.hibernate;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

@XmlType(namespace = "http://tbi.nih.gov/DictionarySchema")
@XmlAccessorType(XmlAccessType.FIELD)
public class Keyword implements Serializable
{

    private static final long serialVersionUID = -1047590153446644157L;

    /**********************************************************************/

    private String uri;

    private String keyword;

    private Long count;

    /**********************************************************************/
    public Keyword()
    {

    }

    public Keyword(Keyword keyword)
    {

        this.uri = keyword.getUri();
        this.keyword = keyword.getKeyword();
        this.count = keyword.getCount();
    }

    public Keyword(String keyword, Long count)
    {

        this.keyword = keyword;
        this.count = count;
    }

    public String getUri()
    {

        return uri;
    }

    public void setUri(String uri)
    {

        this.uri = uri;
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

        return count == null ? new Long(0L) : count; // because empty count just means it's 0 right?
    }

    public void setCount(Long count)
    {

        this.count = count;
    }

    /**********************************************************************/

    @Override
    public String toString()
    {

        return "Keyword [URI=" + uri + ", keyword=" + keyword + ", count=" + count + "]";
    }

    @Override
    public int hashCode()
    {

        final int prime = 31;
        int result = 1;
        result = prime * result + ((keyword == null) ? 0 : keyword.hashCode());
        result = prime * result + ((uri == null) ? 0 : uri.hashCode());
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
        Keyword other = (Keyword) obj;
        if (keyword == null)
        {
            if (other.keyword != null)
                return false;
        }
        else
            if (!keyword.equals(other.keyword))
                return false;
        if (uri == null)
        {
            if (other.uri != null)
                return false;
        }
        else
            if (!uri.equals(other.uri))
                return false;
        return true;
    }
}
