package gov.nih.tbi.account.model.hibernate;

import java.io.Serializable;
import java.util.Objects;

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

import com.google.gson.annotations.Expose;

import gov.nih.tbi.account.model.PermissionAuthority;
import gov.nih.tbi.commons.model.EntityType;
import gov.nih.tbi.commons.model.PermissionType;

/**
 * Model for Visualization EntityMap. Contains information of user and their entities to use in Visualization.
 * 
 * @author Sofia Zaim
 * 
 */
@Table(name = "ACCOUNT")
@XmlRootElement(name = "account")
@XmlAccessorType(XmlAccessType.FIELD)
public class VisualizationEntityMap implements Serializable {


	/**
	 * 
	 */
	private static final long serialVersionUID = -5922219566886251856L;

	@Expose
	private Integer accountId;
	
	@Expose
	private String userName;
	
	@Expose
	private Integer entityId;
	
	@Expose
	private Integer typeId;
	
	@Expose
	private Integer permissionTypeId;
	
	@Expose
	private String groupName;
	
	@Expose
	private Integer permissionGroupStatusId;
	
	@Expose
	private Integer permissionGroupMemberId;
	

	public VisualizationEntityMap() {

	}

	public VisualizationEntityMap(Integer accountId, String userName, Integer entityId, Integer typeId, Integer permissionTypeId, String groupName, Integer permissionGroupStatusId,
			Integer permissionGroupMemberId) {

		this.setAccountId(accountId);
		this.setUserName(userName);
		this.setEntityId(entityId);
		this.setTypeId(typeId);
		this.setPermissionTypeId(permissionTypeId);
		this.setGroupName(groupName);
		this.setPermissionGroupStatusId(permissionGroupStatusId);
		this.setPermissionGroupMemberId(permissionGroupMemberId);

	}

	public Integer getAccountId() {
		return accountId;
	}

	public void setAccountId(Integer accountId) {
		this.accountId = accountId;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public int getEntityId() {
		return entityId;
	}
	
	public void setEntityId(Integer entityId) {
		this.entityId = entityId;
	}
	
	public int getTypeId() {
		return typeId;
	}
	
	public void setTypeId(Integer typeId) {
		this.typeId = typeId;
	}
	
	public int getPermissionTypeId() {
		return permissionTypeId;
	}

	public void setPermissionTypeId(Integer permissionTypeId) {
		this.permissionTypeId = permissionTypeId;
	}

	public String getGroupName() {
		return groupName;
	}

	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}
	
	public int getPermissionGroupStatusId() {
		return permissionGroupStatusId;
	}

	public void setPermissionGroupStatusId(Integer permissionGroupStatusId) {
		this.permissionGroupStatusId = permissionGroupStatusId;
	}
	
	public int getPermissionGroupMemberId() {
		return permissionGroupMemberId;
	}

	public void setPermissionGroupMemberId(Integer permissionGroupMemberId) {
		this.permissionGroupMemberId = permissionGroupMemberId;
	}
	
	@Override
	public String toString() {
		return "VisualizationEntityMap [entityId=" + entityId + ", permissionTypeId=" + permissionTypeId + ", groupName=" + groupName +"]";
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(accountId, userName, entityId, typeId, permissionTypeId, groupName, permissionGroupStatusId, permissionGroupMemberId);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof VisualizationEntityMap)) {
			return false;
		}
		VisualizationEntityMap other = (VisualizationEntityMap) obj;
		return Objects.equals(accountId, other.accountId) && Objects.equals(userName, other.userName)
				&& Objects.equals(entityId, other.entityId) && Objects.equals(typeId, other.typeId)
				&& Objects.equals(permissionTypeId, other.permissionTypeId) 
				&& Objects.equals(groupName, other.groupName) 
				&& Objects.equals(permissionGroupStatusId, other.permissionGroupStatusId)
				&& Objects.equals(permissionGroupMemberId, other.permissionGroupMemberId);
	}


}
