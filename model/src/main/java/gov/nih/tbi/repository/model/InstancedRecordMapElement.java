package gov.nih.tbi.repository.model;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAnyElement;

//maynot need this
//@XmlRootElement(name = "instancedRowMapElement")
public class InstancedRecordMapElement implements Serializable
{
    
    /**
     * 
     */
    private static final long serialVersionUID = -3778132067464130944L;

    
    @XmlAnyElement
    public InstancedRow instancedRow;
}
