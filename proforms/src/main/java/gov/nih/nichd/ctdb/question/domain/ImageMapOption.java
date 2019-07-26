package gov.nih.nichd.ctdb.question.domain;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.StringTokenizer;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import gov.nih.nichd.ctdb.common.CtdbDomainObject;
import gov.nih.nichd.ctdb.common.TransformationException;
/**
 * Created by Booz Allen Hamilton
 * Date: Aug 24, 2004
 * 
 */
public class ImageMapOption extends CtdbDomainObject {
	private static final long serialVersionUID = 1300011022568078098L;
	
	private String option;
    private String value;
    private HashMap<String, List<String>> coordinates;
    boolean selected = false;
    /* coordinates : key = row, bucket = columns */

    public ImageMapOption () {
       this.coordinates = new HashMap<String, List<String>>();
    }

    public String getOption() {
        return option;
    }

    public void setOption(String option) {
        this.option = option;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public HashMap<String, List<String>> getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(HashMap<String, List<String>> coordinates) {
        this.coordinates = coordinates;
    }

    public String getWebString () {
        String str = option + " : (";
        for ( String row : coordinates.keySet() ) {
            List<String> cols = coordinates.get(row);
            for ( String colm : cols ) {
                str += row + "," + colm;
                str += ")(";
            }
        }

        return str.substring(0, str.length() -1);
    }

    public void buildFromWebString (String str) {
        this.option = str.substring(0, str.indexOf(":"));
        str = str.substring(str.indexOf(":"), str.length());
        String cords = str.substring(str.indexOf("(") +1, str.lastIndexOf(")"));
        StringTokenizer tok = new StringTokenizer(cords, ")(");
        
        while ( tok.hasMoreElements() ) {
            String coord = tok.nextToken();
            String[] tmp = coord.split(",");
            if (this.coordinates.get(tmp[0]) != null) {
                // this row has begun, just update the column
                (coordinates.get(tmp[0])).add(tmp[1]);
            } else {
                // need new row
                List<String> l = new ArrayList<String>();
                l.add(tmp[1]);
                coordinates.put(tmp[0], l);
            }
        }
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

	/**
	 * This method allows the transformation of a ImageMapOption into an XML
	 * Document. If no implementation is available at this time, an
	 * UnsupportedOperationException will be thrown.
	 *
	 * @return XML Document
	 * @throws TransformationException
	 *             is thrown if there is an error during the XML tranformation
	 */
	public Document toXML() throws TransformationException {
		Document document = super.newDocument();
		Element root = super.initXML(document, "option");

		if (this.option != null) {
			Element displayNode = document.createElement("display");
			displayNode.appendChild(document.createTextNode(this.option));
			root.appendChild(displayNode);
		}
		
		root.setAttribute("value", this.value);
		root.setAttribute("selected", Boolean.toString(this.selected));

		return document;
	}

    /**
     * Compare if this Object is equal to Object o through member-wise comparison.
     *
     * @param o Object the object to compare with
     * @return True if they are equal, false otherwise
     */
    public boolean equals(Object o)
    {
        if(this == o)
        {
            return true;
        }
        if(!(o instanceof ImageMapOption))
        {
            return false;
        }

        final ImageMapOption option = (ImageMapOption) o;
        if( (this.option == null && option.option != null)
            || (this.option != null && option.option == null)) {
            return false;
        }
        else if (this.option != null && option.option != null && !this.option.trim().equals(option.option.trim()))
        {
            return false;
        }

/*        if( (this.value == null && option.value != null)
            || (this.value != null && option.value == null)) {
            return false;
        }
        else if (this.value != null && option.value != null && !this.value.trim().equals(option.value.trim()))
        {
            return false;
        }*/

        return true;
    }
}
