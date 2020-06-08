
package gov.nih.tbi.ordermanager.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Entity
@Table(name = "ORDERABLE_MAP")
public class OrderableMap implements Serializable
{

    private static final long serialVersionUID = 1734395154902740308L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "ORDERABLE_MAP_SEQ")
    @SequenceGenerator(name = "ORDERABLE_MAP_SEQ", sequenceName = "ORDERABLE_MAP_SEQ", allocationSize = 1)
    private Long id;

    @Column(name = "FOREIGN_ID")
    private String foreignId;
    @Column(name = "FOREIGN_SRC")
    private String foreignSrc;
    @Column(name = "LOCAL_ID")
    private String localId;

    public Long getId()
    {

        return id;
    }

    public void setId(Long id)
    {

        this.id = id;
    }

    public String getForeignId()
    {

        return foreignId;
    }

    public void setForeignId(String foreignId)
    {

        this.foreignId = foreignId;
    }

    public String getForeignSrc()
    {

        return foreignSrc;
    }

    public void setForeignSrc(String foreignSrc)
    {

        this.foreignSrc = foreignSrc;
    }

    public String getLocalId()
    {

        return localId;
    }

    public void setLocalId(String localId)
    {

        this.localId = localId;
    }
}
