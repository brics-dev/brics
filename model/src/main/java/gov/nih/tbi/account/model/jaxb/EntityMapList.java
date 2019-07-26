package gov.nih.tbi.account.model.jaxb;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import gov.nih.tbi.account.model.hibernate.EntityMap;

@XmlRootElement(name = "EntityMap")
@XmlAccessorType(XmlAccessType.FIELD)
public class EntityMapList {

	@XmlElement(name = "EntityMap")
	private List<EntityMap> entityMapList = new ArrayList<EntityMap>();

	public EntityMapList() {

	}

	public EntityMapList(List<EntityMap> entityMapList) {
		this.entityMapList.addAll(entityMapList);
	}

	public List<EntityMap> getEntityMapList() {
		return entityMapList;
	}

	public void setEntityMapList(List<EntityMap> entityMapList) {
		this.entityMapList = entityMapList;
	}

	public void add(EntityMap entityMap) {
		entityMapList.add(entityMap);
	}
}
