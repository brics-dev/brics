
package gov.nih.tbi.ordermanager.model;

import gov.nih.tbi.repository.model.hibernate.UserFile;

import java.io.Serializable;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;

/**
 * 
 * @author vpacha
 * 
 */
@Entity
@Table(name = "Order_Manager_Document")
public class OrderManagerDocument implements Serializable
{

    private static final long serialVersionUID = 1232395795335763942L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "ORDER_MANAGER_DOCUMENT_SEQ")
    @SequenceGenerator(name = "ORDER_MANAGER_DOCUMENT_SEQ", sequenceName = "ORDER_MANAGER_DOCUMENT_SEQ", allocationSize = 1)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "BIOSPECIMEN_ORDER_ID")
    private BiospecimenOrder biospecimenOrder;

    @OneToOne(optional = false, targetEntity = UserFile.class, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "USER_FILE_ID")
    private UserFile userFile;

    @ManyToOne(optional = true)
    @JoinColumn(name = "FILETYPE_ID")
    private BioRepositoryFileType fileType;

    @Column(name = "DESCRIPTION")
    private String description;

    @Transient
    private boolean removeFile;

    public Long getId()
    {

        return id;
    }

    public void setId(Long id)
    {

        this.id = id;
    }

    public BiospecimenOrder getOrder()
    {

        return biospecimenOrder;
    }

    public void setOrder(BiospecimenOrder order)
    {

        this.biospecimenOrder = order;
    }

    public UserFile getUserFile()
    {

        return userFile;
    }

    public void setUserFile(UserFile userFile)
    {

        this.userFile = userFile;
    }

    public String getDescription()
    {

        return description;
    }

    public void setDescription(String description)
    {

        this.description = description;
    }

    public void setRemoveFile(boolean removeFile)
    {

        this.removeFile = removeFile;
    }

    public boolean getRemoveFile()
    {

        return removeFile;
    }

    /**
     * @return the fileType
     */
    public BioRepositoryFileType getFileType()
    {

        return fileType;
    }

    /**
     * @param fileType
     *            the fileType to set
     */
    public void setFileType(BioRepositoryFileType fileType)
    {

        this.fileType = fileType;
    }

}
