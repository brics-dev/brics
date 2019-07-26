package gov.nih.tbi.commons.model;

import javax.xml.bind.annotation.adapters.XmlAdapter;

public class WebServiceStringToLongAdapter extends XmlAdapter<String, Long>{
	 
	public Long unmarshal(String s) {
	        return Long.parseLong(s);
	    }
	 
	    public String marshal(Long number) {
	        if (number == null) return "";
	        return number.toString();
	    }
     }