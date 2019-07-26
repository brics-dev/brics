
package gov.nih.tbi.account.model.hibernate;

import gov.nih.tbi.commons.model.PermissionType;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Entity
@Table(name = "PERMISSION_MAP")
public class PermissionMap implements Serializable
{

    /**
	 * 
	 */
    private static final long serialVersionUID = 8274822163709706071L;

    /**********************************************************************/

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "PERMISSION_MAP_SEQ")
    @SequenceGenerator(name = "PERMISSION_MAP_SEQ", sequenceName = "PERMISSION_MAP_SEQ", allocationSize = 1)
    private Long id;

    @ManyToOne(targetEntity = EntityMap.class)
    @JoinColumn(name = "ENTITY_MAP_ID")
    private EntityMap entityMap;

    @Enumerated(EnumType.ORDINAL)
    @Column(name = "PERMISSION_ID")
    private PermissionType permission;

    /**********************************************************************/

    public Long getId()
    {

        return id;
    }

    public void setId(Long id)
    {

        this.id = id;
    }

    public EntityMap getEntityMap()
    {

        return entityMap;
    }

    public void setEntityMap(EntityMap entityMap)
    {

        this.entityMap = entityMap;
    }

    public PermissionType getPermission()
    {

        return permission;
    }

    public void setPermission(PermissionType permission)
    {

        this.permission = permission;
    }

    /**********************************************************************/

    @Override
    public String toString()
    {

        return "PermissionMap [ id=" + id + ", permissionId=" + permission + "]";
    }

}
