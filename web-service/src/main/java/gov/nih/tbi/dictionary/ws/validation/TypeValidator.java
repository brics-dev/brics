
package gov.nih.tbi.dictionary.ws.validation;

import gov.nih.tbi.ModelConstants;
import gov.nih.tbi.commons.model.DataType;
import gov.nih.tbi.dictionary.model.hibernate.MapElement;
import gov.nih.tbi.dictionary.model.hibernate.StructuralFormStructure;

import java.util.Date;
import java.util.List;
import java.util.Vector;

public class TypeValidator extends CellValidator implements ConditionalValidator
{

    public TypeValidator()
    {

        super();
    }

    public TypeValidator(List<StructuralFormStructure> dictionary)
    {

        super(dictionary);
    }

    public boolean validate(MapElement iElement, String data)
    {

        String type = iElement.getStructuralDataElement().getType().name();

        return isType(data, type);
    }

    public String getConstraintType(String rowRef) throws RuntimeException
    {

        String[] split = rowRef.substring(1).split("\\" + ValidationConstants.VALUE_REFERENCE_DIVIDER);
        MapElement current = getElement(split[0], split[1]);

        return current.getStructuralDataElement().getType().getValue();
    }

    // TODO: Add location of failure; throw Parse Exception??
    public boolean validateConstraint(String operator, String rowRef, Vector<String> valueRange, String type,
            MapElement iElement) throws RuntimeException
    {

        // need to check operator type
        if (ValidationUtil.isRangeOperator(operator))
        {

            for (String value : valueRange)
            {
                // need to check #ref, type matches the type
                if (ValidationUtil.isColRef(value) || ValidationUtil.isRowRef(value))
                {
                    if (!refIsType(value, type))
                    {
                        throw new RuntimeException(value + "'s type does not match " + rowRef + "'s type, " + type);
                        // return false;
                    }
                    return true;
                }
                else
                {
                    if (value.contains(ValidationConstants.VALUE_RANGE_DELIMITER))
                    {
                        if (!typeIsNumeric(type))
                        {
                            throw new RuntimeException(value + " - Ranges are only valid for numeric types");
                            // return false;
                        }
                        String[] minMax = value.split(ValidationConstants.VALUE_RANGE_DELIMITER);
                        for (String s : minMax)
                        {
                            if (!isType(s.trim(), type))
                            {
                                throw new RuntimeException(s + " type does not match " + rowRef + "'s type, " + type);
                                // return false;
                            }
                        }
                    }
                    else
                        if (value.endsWith(ValidationConstants.VALUE_RANGE_BOTTOM_BOUND))
                        {
                            if (!typeIsNumeric(type))
                            {
                                throw new RuntimeException(value + " - Ranges are only valid for numeric types");
                                // return false;
                            }
                            String s = value.substring(0, value.length() - 1);
                            if (!isType(s, type))
                            {
                                throw new RuntimeException(s + " type does not match " + rowRef + "'s type, " + type);
                                // return false;
                            }
                        }
                        else
                        {
                            if (!isType(value, type))
                            {
                                throw new RuntimeException(value + " type does not match " + rowRef + "'s type, "
                                        + type);
                                // return false;
                            }
                        }
                }
            }

        }
        else
        {

            if (ValidationUtil.isNumberOperator(operator))
            {
                if (!typeIsNumeric(type))
                {
                    throw new RuntimeException(operator + " - Numeric operators are not valid for type, " + type);
                    // return false;
                }
            }

            String value = valueRange.get(0);
            // need to check #ref type matches the type
            if (ValidationUtil.isRowRef(value))
            {
                if (!refIsType(value, type))
                {
                    throw new RuntimeException(value + "'s type does not match " + rowRef + "'s type, " + type);
                    // return false;
                }
                return true;
                // }else if (ValidationUtil.isColRef(value)){ - Should have been checked already in AST build!!
                // return false;
            }
            else
            {
                if (!isType(value, type))
                {
                    throw new RuntimeException(value + " type does not match " + rowRef + "'s type, " + type);
                    // return false;
                }
            }
        }

        return true;
    }

    public boolean refIsType(String ref, String type) throws RuntimeException
    {

        String[] split = ref.substring(1).split("\\" + ValidationConstants.VALUE_REFERENCE_DIVIDER);
        MapElement current = getElement(split[0], split[1]);

        String curType = current.getStructuralDataElement().getType().getValue();
        ;

        return type.equals(curType);
    }

    // The format string for us is always empty (we are not using in data element)
    public boolean isType(String value, String type)
    {

        String format = null;
        boolean result = false;

        if (type == null || value == null)
        {
            return false;
        }

        value = value.trim();

        DataType elementType = DataType.valueOf(type);
        
        switch (elementType) {
	        case ALPHANUMERIC:{
	        	return true;
	        }
	        case NUMERIC: {
	        	result = isFloat(value, format);
	        	break;
	        }
	        case THUMBNAIL: 
	        case FILE:
	        case TRIPLANAR: {
	        	result = isFile(value, format);
	        	break;
	        }
	        case DATE: {
	        	result = isDate(value);
	        	break;
	        }
	        case BIOSAMPLE: {
	        	 // format is always set to be null. the feature was originally implemented in case a biosample
	        	 // format is determined. keeping the functionality in with the rewrite.
		        if (format == null || format.equals(ModelConstants.EMPTY_STRING)) {
		        	return true;
		        }
		        result = value.matches(format);
		        break;
	        }
	        case GUID: {
	        	result = isGuid(value, format);
	        	break;
	        }
	        default : return false;
        }

        return result;
    }

    private boolean isDate(String s)
    {

        Date date = ValidationUtil.parseDate(s);
        if (date != null)
            return true;
        return false;
    }

    private boolean isGuid(String s, String format)
    {

        // if (format == null || format.equals(ModelConstants.EMPTY_STRING)){
        // return s.equals("FITBIR*") || s.matches("^FITBIR(\\w)*");
        // }
        // return s.matches(format);

        return true;
    }

    private boolean isFloat(String s, String format)
    {

        try
        {

            String[] dataArr = s.split(";");

            for (String val : dataArr)
            {
                Float.parseFloat(val);

                if (format != null && !format.equals(ModelConstants.EMPTY_STRING) && !val.matches(format))
                {
                    return false;
                }
            }

            return true;

        }
        catch (NumberFormatException e)
        {
            return false;
        }
    }

    private boolean isFile(String s, String format)
    {

        if (format == null || format.equals(ModelConstants.EMPTY_STRING))
        {
            return (s.matches("(/.*)*(\\.)(\\w)*"));
        }
        return s.matches(format);
    }

    public boolean typeIsNumeric(String type)
    {

        return (type.equalsIgnoreCase("Integer") || type.equalsIgnoreCase("Float") || type.equalsIgnoreCase("Date"));
    }
}
