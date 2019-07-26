
package gov.nih.tbi.commons.model;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;

@XmlEnum(String.class)
public enum InputRestrictions
{
	@XmlEnumValue("Free-Form Entry")
    FREE_FORM(0L, "Free-Form Entry"), 
    @XmlEnumValue("Single Pre-Defined Value Selected")
    SINGLE(1L, "Single Pre-Defined Value Selected"),
    @XmlEnumValue("Multiple Pre-Defined Values Selected")
    MULTIPLE(2L,"Multiple Pre-Defined Values Selected");

    private Long id;
    private String value;
    
    InputRestrictions(String value) 
    {
    	this.value = value;
        for(InputRestrictions restrictions:values())
        {
            if(value.equals(restrictions.getValue()))
            {
                this.id = restrictions.getId();
            }
        }
    }

    InputRestrictions(Long id, String value)
    {

        this.id = id;
        this.value = value;
    }

    public Long getId()
    {

        return id;
    }

    public String getValue()
    {

        return value;
    }
    
    public static InputRestrictions getByValue(String value)
    {
        for(InputRestrictions restrictions:values())
        {
            if(value.equals(restrictions.getValue()))
            {
                return restrictions;
            }
        }
        
        return null;
    }
}
