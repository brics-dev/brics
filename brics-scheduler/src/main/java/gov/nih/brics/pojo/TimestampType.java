package gov.nih.brics.pojo;

import java.sql.Timestamp;

import com.hp.hpl.jena.datatypes.BaseDatatype;
import com.hp.hpl.jena.datatypes.RDFDatatype;
import com.hp.hpl.jena.datatypes.TypeMapper;
import com.hp.hpl.jena.graph.impl.LiteralLabel;


public class TimestampType extends BaseDatatype 
{
/*    {
        System.out.println("^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^");
        System.out.println("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%");
        System.out.println("registering type");
        System.out.println("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%");
        System.out.println("^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^");
        RDFDatatype type = TimestampType.timestampType;
        TypeMapper.getInstance().registerDatatype(type);
    }*/
    
    public static final String TimestampTypeURI = "java:java.sql.Timestamp";
    public static final RDFDatatype timestampType = new TimestampType();

    private TimestampType()
    {

        super(TimestampTypeURI);
    }
    
    public String unparse(Object value)
    {
        Timestamp ts= (Timestamp)value;
        return ts.toString();
    }
    
    public Object parse(String lexicalForm)
    {
        return Timestamp.valueOf(lexicalForm);
    }
    
    public  boolean isEqual(LiteralLabel value1, LiteralLabel value2)
    {
        return value1.getValue().equals(value2.getValue());
    }
    
    public Class<?> getJavaClass()
    {
        return java.sql.Timestamp.class;
    }

    public int getHashCode(LiteralLabel label)
    {
        return label.getValue().hashCode();
    }

}
