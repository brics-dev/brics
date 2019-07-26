package gov.nih.tbi.repository.model.hibernate;

import java.io.Serializable;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;


@Entity
@DiscriminatorValue(value="S")
@XmlAccessorType(XmlAccessType.FIELD)
public class StudySupportingDocumentation extends SupportingDocumentation implements Serializable {

	private static final long serialVersionUID = 5622704520312389253L;

}
