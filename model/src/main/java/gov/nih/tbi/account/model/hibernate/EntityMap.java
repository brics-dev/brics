
package gov.nih.tbi.account.model.hibernate;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import gov.nih.tbi.account.model.PermissionAuthority;
import gov.nih.tbi.commons.model.EntityType;
import gov.nih.tbi.commons.model.PermissionType;

@Entity
@Table(name = "ENTITY_MAP")
@XmlRootElement(name = "entityMap")
@XmlAccessorType(XmlAccessType.FIELD)
public class EntityMap implements Serializable
{

    /**
	 * 
	 */
    private static final long serialVersionUID = -7455669599946145751L;

    public static final String ACCOUNT = "account";
    public static final String PERMISSION_GROUP = "permissionGroup";

    /**********************************************************************/

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "ENTITY_MAP_SEQ")
    @SequenceGenerator(name = "ENTITY_MAP_SEQ", sequenceName = "ENTITY_MAP_SEQ", allocationSize = 1)
    private Long id;

    @Enumerated(EnumType.ORDINAL)
    @Column(name = "TYPE_ID")
    private EntityType type;

    @Column(name = "ENTITY_ID")
    private Long entityId;

	@ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ACCOUNT_ID")
    private Account account;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PERMISSION_GROUP_ID")
    private PermissionGroup permissionGroup;

    // @OneToMany( fetch=FetchType.EAGER,
    // cascade=CascadeType.ALL,
    // mappedBy="entityMap",
    // targetEntity=PermissionMap.class)
    // @MapKey(name="permission")
    // private Map<PermissionType, PermissionMap> permissions;

    @Enumerated(EnumType.ORDINAL)
    @Column(name = "PERMISSION_TYPE_ID")
    private PermissionType permission;

    public EntityMap() {}

    public EntityMap(Account _account, EntityType _type, Long _entityId) {
        this.account = _account;
        this.type = _type;
        this.entityId = _entityId;
    }
    
    public EntityMap(Account account, EntityType type, Long entityId, PermissionType permission) {
        this.setAccount(account);
        this.setType(type);
        this.setEntityId(entityId);
        this.setPermission(permission);
    }


    /**********************************************************************/

    public Long getId()
    {

        return id;
    }

    public void setId(Long id)
    {

        this.id = id;
    }

    public EntityType getType()
    {

        return type;
    }

    public void setType(EntityType type)
    {

        this.type = type;
    }

    public Long getEntityId()
    {

        return entityId;
    }

    public void setEntityId(Long entityId)
    {

        this.entityId = entityId;
    }

    public Account getAccount()
    {

        return account;
    }

    public void setAccount(Account account)
    {

        this.account = account;
    }

    public PermissionGroup getPermissionGroup()
    {

        return permissionGroup;
    }

    public void setPermissionGroup(PermissionGroup permissionGroup)
    {

        this.permissionGroup = permissionGroup;
    }

    public PermissionType getPermission()
    {

        return permission;
    }

    public void setPermission(PermissionType permission)
    {

        this.permission = permission;
    }

    public PermissionAuthority getAuthority()
    {

        if (account != null)
        {
            return account;
        }
        else
            if (permissionGroup != null)
            {
                return permissionGroup;
            }

        return null;
    }

    public Long getDiseaseKey()
    {

        Long diseaseKey = null;
        if (account != null && account.getDiseaseKey() != null)
        {
            diseaseKey = Long.valueOf(account.getDiseaseKey());
        }
        else
            if (permissionGroup != null && permissionGroup.getDiseaseKey() != null)
            {
                diseaseKey = Long.valueOf(permissionGroup.getDiseaseKey());
            }
        return diseaseKey;
    }

    /**********************************************************************/

    @Override
	public String toString() {
		return "EntityMap [entityId=" + entityId + ", id=" + id + ", permission=" + permission + ", typeId=" + type
				+ "]";
    }
}
