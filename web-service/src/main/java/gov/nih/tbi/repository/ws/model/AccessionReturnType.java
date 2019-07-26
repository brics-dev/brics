package gov.nih.tbi.repository.ws.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

@XmlType(namespace="http://tbi.nih.gov/RepositorySchema")
@XmlAccessorType(XmlAccessType.FIELD)
public enum AccessionReturnType
{
	VALID,
	INVALID,
	ERROR,
	PSUEDO_GUID;
}
