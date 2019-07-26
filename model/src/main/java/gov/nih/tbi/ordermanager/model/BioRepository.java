
package gov.nih.tbi.ordermanager.model;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlRootElement;

@Entity
@Table(name = "Bio_Repository")
@XmlRootElement(name = "BioRepository")
public class BioRepository implements Serializable
{

    /**
	 * 
	 */
	private static final long serialVersionUID = 662782982089491140L;

	@Id
    private Long id;

    @Column(name = "NAME")
    private String name;

    @Column(name = "DESCRIPTION")
    private String description;

    @ManyToMany(fetch = FetchType.EAGER, cascade = { CascadeType.ALL })
    @JoinTable(name = "bio_repository_bio_repository_file_type", joinColumns = { @JoinColumn(name = "bio_repository_id") }, inverseJoinColumns = { @JoinColumn(name = "filetype_id") })
    private Set<BioRepositoryFileType> requiredFileTypes = new HashSet<BioRepositoryFileType>();

    /**
     * @return the id
     */
    public Long getId()
    {

        return id;
    }

    /**
     * @return the name
     */
    public String getName()
    {

        return name;
    }

    /**
     * @return the description
     */
    public String getDescription()
    {

        return description;
    }
    
    /**
     * @return the requiredFileTypes
     */
    public Set<BioRepositoryFileType> getRequiredFileTypes()
    {

        return requiredFileTypes;
    }

    @Override
    public int hashCode()
    {

        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
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
        
        BioRepository other = (BioRepository) obj;
        if (id == null)
        {
            if (other.id != null)
                return false;
        }
        else
            if (!id.equals(other.id))
                return false;
        return true;
    }
    
}
