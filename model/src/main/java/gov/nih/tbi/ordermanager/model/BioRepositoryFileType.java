
package gov.nih.tbi.ordermanager.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlRootElement;

@Entity
@Table(name = "Bio_Repository_Filetype")
@XmlRootElement(name = "BioRepository_Filetype")
public class BioRepositoryFileType implements Serializable
{

   
	

	/**
	 * 
	 */
	private static final long serialVersionUID = -1088855812053352190L;

	@Id
    private Long id;

    @Column(name = "NAME")
    private String name;

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
}
