package gov.nih.tbi.repository.ws.model;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

@XmlType(namespace="http://tbi.nih.gov/RepositorySchema")
@XmlAccessorType(XmlAccessType.FIELD)
public class Accession implements Serializable
{
	/**
	 * 
	 */
	private static final long	serialVersionUID	= 6574246106709204531L;
	
	private String value;
	private AccessionReturnType returnValue;
	private String comment;
	
	public Accession(  )
	{
	}
	
	public Accession( String data )
	{
		value = data;
	}
	
	public String getValue()
	{
		return value;
	}
	public void setValue( String value )
	{
		this.value = value;
	}
	public AccessionReturnType getReturnValue()
	{
		return returnValue;
	}
	public void setReturnValue( AccessionReturnType returnValue )
	{
		this.returnValue = returnValue;
	}

	public String getComment()
	{
		return comment;
	}

	public void setComment( String comment )
	{
		this.comment = comment;
	}
}
