package gov.nih.tbi.dictionary.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import gov.nih.tbi.commons.util.LongHashMapAdapter;

@XmlRootElement(name = "UpdateFormStructureReference")
@XmlAccessorType(XmlAccessType.FIELD)
public class UpdateFormStructureReferencePayload implements Serializable {

	private static final long serialVersionUID = -5926065296153362290L;

	@XmlAttribute
	private Long oldFormStructureId;

	@XmlAttribute
	private Long newFormStructureId;

	@XmlJavaTypeAdapter(LongHashMapAdapter.class)
	private HashMap<Long, Long> oldNewRepeatableGroupIdMap;

	public Long getOldFormStructureId() {
		return oldFormStructureId;
	}

	public void setOldFormStructureId(Long oldFormStructureId) {
		this.oldFormStructureId = oldFormStructureId;
	}

	public Long getNewFormStructureId() {
		return newFormStructureId;
	}

	public void setNewFormStructureId(Long newFormStructureId) {
		this.newFormStructureId = newFormStructureId;
	}

	public HashMap<Long, Long> getOldNewRepeatableGroupIdMap() {
		return oldNewRepeatableGroupIdMap;
	}

	public void setOldNewRepeatableGroupIdMap(HashMap<Long, Long> oldNewRepeatableGroupIdMap) {
		this.oldNewRepeatableGroupIdMap = oldNewRepeatableGroupIdMap;
	}
}
