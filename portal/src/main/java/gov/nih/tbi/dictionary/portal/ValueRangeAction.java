
package gov.nih.tbi.dictionary.portal;

import java.util.Iterator;

import gov.nih.tbi.PortalConstants;
import gov.nih.tbi.commons.model.DataType;
import gov.nih.tbi.dictionary.model.hibernate.DataElement;
import gov.nih.tbi.dictionary.model.hibernate.ValueRange;

public class ValueRangeAction extends DataElementAction
{

    private static final long serialVersionUID = 2472892554140016280L;
    /*********************************************************************/

    String permissibleValue;
    String valueDescription;
    Integer outputCode;
    Boolean numeric; 

    /*********************************************************************/

    public String getPermissibleValue()
    {

        return permissibleValue;
    }

    public void setPermissibleValue(String permissibleValue)
    {

        this.permissibleValue = permissibleValue;
    }

    public String getValueDescription()
    {

        return valueDescription;
    }

    public void setValueDescription(String valueDescription)
    {

        this.valueDescription = valueDescription;
    }

    public Integer getOutputCode() {
		return outputCode;
	}

	public void setOutputCode(Integer outputCode) {
		this.outputCode = outputCode;
	}

    public Boolean getNumeric()
    {

        return numeric;
    }

    public void setNumeric(Boolean numeric)
    {

        this.numeric = numeric;
    }

    /*********************************************************************/

    public String display()
    {

        return PortalConstants.ACTION_VIEW;
    }

    /**
     * Creates a new ValueRange and adds it to the list in session.
     * 
     * @return String
     */
    public String create(){

        DataElement currentDataElement = getSessionDataElement().getDataElement();
        ValueRange newValueRange = new ValueRange();

        newValueRange.setDataElement(currentDataElement.getStructuralObject());

        if (!PortalConstants.EMPTY_STRING.equals(permissibleValue))
        {
            newValueRange.setValueRange(permissibleValue.trim());
        }
        
        newValueRange.setDescription(valueDescription);
        
        if(outputCode != null){
        	newValueRange.setOutputCode(outputCode);
        }
       
        if(numeric ==true){
        	currentDataElement.setType(DataType.NUMERIC);
        }else{
        	currentDataElement.setType(DataType.ALPHANUMERIC);
        }
        
        getSessionDataElement().setDataElement(currentDataElement);
        
        currentDataElement.addValueRange(newValueRange);
        
        getSessionDataElement().setDataElement(currentDataElement);

        permissibleValue = null;
        valueDescription = null;
        outputCode = null;
        
       return PortalConstants.ACTION_VIEW;
    }

    /**
     * Removes ValueRange with permissible value from session.
     * 
     * @return String
     */
    public String remove()
    {

    	DataElement currentDataElement = getSessionDataElement().getDataElement();

        Iterator<ValueRange> iter = currentDataElement.getValueRangeList().iterator();
         while (iter.hasNext())
         {
             if (iter.next().getValueRange().equals(permissibleValue))
             {
                 iter.remove();
                 getSessionDataElement().setDataElement(currentDataElement);
             }
         }
         
         iter = currentDataElement.getSemanticValueRangeList().iterator();
         while (iter.hasNext())
         {
             if (iter.next().getValueRange().equals(permissibleValue))
             {
                 iter.remove();
                 getSessionDataElement().setDataElement(currentDataElement);
             }
         }

   
         return PortalConstants.ACTION_VIEW;
    }
}
