
package gov.nih.tbi.dictionary.model.hibernate;

import gov.nih.tbi.ModelConstants;
import gov.nih.tbi.commons.model.ConditionalOperators;
import gov.nih.tbi.commons.model.DataType;

import java.io.File;
import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

/**
 * This is the data model for conditions used for conditional logic in map elements
 * 
 * @author Francis Chen
 */
@Entity
@Table(name = "CONDITION")
public class Condition implements Serializable
{

    private static final long serialVersionUID = 4464355325369858896L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "CONDITION_SEQ")
    @SequenceGenerator(name = "CONDITION_SEQ", sequenceName = "CONDITION_SEQ", allocationSize = 1)
    private Long id;

    @Enumerated(EnumType.ORDINAL)
    @Column(name = "OPERATOR_ID")
    private ConditionalOperators operator;

    @OneToOne
    @JoinColumn(name = "MAP_ELEMENT_ID")
    private MapElement mapElement;

    @Column(name = "value")
    private String value;

    public Long getId()
    {

        return id;
    }

    public void setId(Long id)
    {

        this.id = id;
    }

    public MapElement getMapElement()
    {

        return mapElement;
    }

    public void setMapElement(MapElement mapElement)
    {

        this.mapElement = mapElement;
    }

    public String getValue()
    {

        return value;
    }

    public void setValue(String value)
    {

        this.value = value;
    }

    public ConditionalOperators getOperator()
    {

        return operator;
    }

    public void setOperator(ConditionalOperators operator)
    {

        this.operator = operator;
    }

    public boolean evaluate(String value)
    {

        if (value == null || value.equals(ModelConstants.EMPTY_STRING))
        {
            return ConditionalOperators.NEQ.equals(operator);
        }

        if (DataType.ALPHANUMERIC.equals(mapElement.getStructuralDataElement().getType())
                || DataType.GUID.equals(mapElement.getStructuralDataElement().getType()))
        {
            switch (operator)
            {
            case EQ:
                return (this.value.trim().equalsIgnoreCase(value));
            case NEQ:
                return (!this.value.trim().equalsIgnoreCase(value));
            default:
                throw new UnsupportedOperationException(
                        "Cannot evaluate inequality with alphanumeric or GUID map elements.");
            }
        }
        else
            if (DataType.NUMERIC.equals(mapElement.getStructuralDataElement().getType()))
            {
                Integer left = Integer.valueOf(value);
                Integer right = Integer.valueOf(this.value);
                switch (operator)
                {
                case EQ:
                    return (left.compareTo(right) == 0);
                case NEQ:
                    return (left.compareTo(right) != 0);
                case GT:
                    return (left.compareTo(right) > 0);
                case GTE:
                    return (left.compareTo(right) >= 0);
                case LT:
                    return (left.compareTo(right) < 0);
                case LTE:
                    return (left.compareTo(right) <= 0);
                default:
                    throw new UnsupportedOperationException(
                            "Honestly, the program should never reach this point, so I have nothing clever to put here.");
                }
            }
            else
                if (DataType.DATE.equals(mapElement.getStructuralDataElement().getType()))
                {
                    try
                    {
                        SimpleDateFormat format = new SimpleDateFormat("mm/dd/yyyy");
                        Date left = format.parse(value);
                        Date right = format.parse(this.value);

                        switch (operator)
                        {
                        case EQ:
                            return (left.compareTo(right) == 0);
                        case NEQ:
                            return (left.compareTo(right) != 0);
                        case GT:
                            return (left.compareTo(right) > 0);
                        case GTE:
                            return (left.compareTo(right) >= 0);
                        case LT:
                            return (left.compareTo(right) < 0);
                        case LTE:
                            return (left.compareTo(right) <= 0);
                        default:
                            throw new UnsupportedOperationException(
                                    "Honestly, the program should never reach this point, so I have nothing clever to put here.");
                        }
                    }
                    catch (ParseException e)
                    {
                        e.printStackTrace();
                    }
                }
                else
                    if (DataType.FILE.equals(mapElement.getStructuralDataElement().getType())
                            || DataType.THUMBNAIL.equals(mapElement.getStructuralDataElement().getType())
                            || DataType.TRIPLANAR.equals(mapElement.getStructuralDataElement().getType()))
                    {
                        String fileName = new File(value).getName();
                        switch (operator)
                        {
                        case EQ:
                            return (this.value.trim().equalsIgnoreCase(fileName));
                        case NEQ:
                            return (!this.value.trim().equalsIgnoreCase(fileName));
                        default:
                            throw new UnsupportedOperationException(
                                    "Cannot evaluate inequality with file map elements.");
                        }
                    }
                    else
                        if (DataType.BIOSAMPLE.equals(mapElement.getStructuralDataElement().getType()))
                        {
                            /*
                             * BUSINESS LOGIC REGARDING BIOSAMPLE
                             * 
                             */
                        }

        return false;
    }
}
