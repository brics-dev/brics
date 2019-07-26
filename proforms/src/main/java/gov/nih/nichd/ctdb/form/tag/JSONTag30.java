package gov.nih.nichd.ctdb.form.tag;

/*
 * Modified from apache display tabletag for JSON display for our datatables
 */
import java.lang.reflect.InvocationTargetException;
import java.net.URLEncoder;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.PageContext;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.taglibs.display.ColumnDecorator;
import org.apache.taglibs.display.Decorator;
import org.apache.taglibs.display.StringUtil;
import org.apache.taglibs.display.TemplateTag;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class JSONTag30 extends TemplateTag {
	private static final long serialVersionUID = 5817879750530687567L;
	private static final int EXPORT_TYPE_NONE = -1;
    private static final int EXPORT_TYPE_CSV = 1;
    private static final int EXPORT_TYPE_EXCEL = 2;
    private static final int EXPORT_TYPE_XML = 3;

    private static final int SORT_ORDER_DECENDING = 1;
    private static final int SORT_ORDER_ASCEENDING = 2;

    private List columns = new ArrayList(10);
    private List<IdtFilterTag> filterTags = new ArrayList<IdtFilterTag>();
    
    private IdtConfigTag configTag = null;

    private Object list = null;
    private Decorator dec = null;

    private String name = null;
    private String property = null;
    private String length = null;
    private String offset = null;
    private String scope = null;
    private String pagesize = null;
    private String decorator = null;
    private String export = null;

    private String width = null;
    private String height = null;
    private String rules = null;
    private String vspace = null;
    private String clazz = null;

    private String requestURI = null;
    private Properties prop = null;

    private String display = null; // This variable hold the value of the data has to be displayed without the table.

    // Variables that get set by investigating the request parameters

    private int sortColumn = -1;
    private int sortOrder = SORT_ORDER_ASCEENDING;
    private int pageNumber = 1;
    private int exportType = EXPORT_TYPE_NONE;
    
    //private JSONObject output = new JSONObject();

    // -------------------------------------------------------- Accessor methods

    /**
     * Returns the offset that the person provided as an int. If the user does
     * not provide an offset, or we can't figure out what they are trying to
     * tell us, then we default to 0.
     *
     * @return the number of objects that should be skipped while looping through
     *    the list to display the table
     */
    private int getOffsetValue() {
        int offsetValue = 0;

        if (this.offset != null) {
            try {
                offsetValue = Integer.parseInt(this.offset);
            }
            catch (NumberFormatException e) {
                Integer offsetObject = (Integer) pageContext.findAttribute(offset);
                if (offsetObject == null) {
                    offsetValue = 0;
                }
                else {
                    offsetValue = offsetObject.intValue();
                }
            }
        }
        if (offsetValue < 0) {
            offsetValue = 0;
        }

        return offsetValue;
    }

    /**
     * Returns the length that the person provided as an int. If the user does
     * not provide a length, or we can't figure out what they are trying to tell
     * us, then we default to 0.
     *
     * @return the maximum number of objects that should be shown while looping
     *    through the list to display the values.  0 means show all objects.
     */
    private int getLengthValue() {
        int lengthValue = 0;

        if (this.length != null) {
            try {
                lengthValue = Integer.parseInt(this.length);
            }
            catch (NumberFormatException e) {
                Integer lengthObject = (Integer) pageContext.findAttribute(length);
                if (lengthObject == null) {
                    lengthValue = 0;
                }
                else {
                    lengthValue = lengthObject.intValue();
                }
            }
        }
        if (lengthValue < 0) {
            lengthValue = 0;
        }

        return lengthValue;
    }

    /**
     * Returns the pagesize that the person provided as an int.  If the user does
     * not provide a pagesize, or we can't figure out what they are trying to tell
     * us, then we default to 0.
     *
     * @return the maximum number of objects that should be shown on any one page
     *    when displaying the list.  Setting this value also indicates that the
     *    person wants us to manage the paging of the list.
     */
    private int getPagesizeValue() {
        int pagesizeValue = 0;

        if (this.pagesize != null) {
            try {
                pagesizeValue = Integer.parseInt(this.pagesize);
            }
            catch (NumberFormatException e) {
                Integer pagesizeObject = (Integer) pageContext.findAttribute(this.pagesize);
                if (pagesizeObject == null) {
                    pagesizeValue = 0;
                }
                else {
                    pagesizeValue = pagesizeObject.intValue();
                }
            }
        }

        if (pagesizeValue < 0) {
            pagesizeValue = 0;
        }

        return pagesizeValue;
    }

    // ---------------------------------------- Communication with interior tags

    /**
     * Called by interior column tags to help this tag figure out how it is
     * supposed to display the information in the List it is supposed to
     * display
     *
     * @param obj an internal tag describing a column in this tableview
     */
    public void addColumn(JSONColumnTag30 obj) {
        columns.add(obj);
    }
    
    public void addConfigTag(IdtConfigTag configTag) {
    	this.configTag = configTag;
    }
    
    public void addFilterTag(IdtFilterTag filtertag) {
    	this.filterTags.add(filtertag);
    }

    // --------------------------------------------------------- Tag API methods

    /**
     * When the tag starts, we just initialize some of our variables, and do a
     * little bit of error checking to make sure that the user is not trying
     * to give us parameters that we don't expect.
     */
    public int doStartTag() throws JspException {
        columns = new ArrayList(10);

        HttpServletRequest req = (HttpServletRequest) this.pageContext.getRequest();

        this.loadDefaultProperties();

        this.pageNumber = 1;
        if (req.getParameter("page") != null) {
            this.pageNumber = 1;
            try {
                this.pageNumber = Integer.parseInt(req.getParameter("page"));
            }
            catch (NumberFormatException e) {
                // It's ok to ignore, if they give us something bogus, we default
                // to showing the first page (index = 1)
            }
        }

        if (req.getParameter("sort") != null) {
            this.sortColumn = 0;
            try {
                this.sortColumn = Integer.parseInt(req.getParameter("sort"));
            }
            catch (NumberFormatException e) {
                // It's ok to ignore, if they give us something bogus, we default
                // to sorting the first column (index = 0)
            }

            this.sortOrder = SORT_ORDER_ASCEENDING;
            if (req.getParameter("order") != null) {
                if (req.getParameter("order").equalsIgnoreCase("dec")) {
                    this.sortOrder = SORT_ORDER_DECENDING;
                }
            }
        }

        this.exportType = EXPORT_TYPE_NONE;
        if (this.export != null) {
            if (req.getParameter("exportType") != null) {
                this.exportType = EXPORT_TYPE_NONE;
                try {
                    this.exportType = Integer.parseInt(req.getParameter("exportType"));
                }
                catch (NumberFormatException e) {
                    // It's ok to ignore, if they give us something bogus, we default
                    // to showing the first page (index = 1)
                }
            }
        }

        return super.doStartTag();
    }

    /**
     * Draw the table.  This is where everything happens, we figure out what
     * values we are supposed to be showing, we figure out how we are supposed
     * to be showing them, then we draw them.
     */
    public int doEndTag() throws JspException {
        HttpServletResponse res = (HttpServletResponse) this.pageContext.getResponse();
        JspWriter out = pageContext.getOut();

        List viewableData = this.getViewableData();

        // Figure out where this data is going, if this is an export, then
        // we don't add the header and footer information
        // Get the data back in the representation that the user is after, do they
        // want HTML/XML/CSV/PDF/etc...

        /* JBO 2003/03/29: I added the toString() for 1.3 compatibility */
        String dataOut = "";
        if (this.exportType == EXPORT_TYPE_NONE) {
            dataOut = this.getHTMLData(viewableData).toString();
        }
        else if (this.exportType == EXPORT_TYPE_CSV) {
        	dataOut = this.getRawData(viewableData).toString();
        }
        else if (this.exportType == EXPORT_TYPE_EXCEL) {
        	dataOut = this.getExcelData(viewableData).toString();
        }
        else if (this.exportType == EXPORT_TYPE_XML) {
        	dataOut = this.getXMLData(viewableData).toString();
        }
        
        // adjust the buffer to be the correct size for data needed (better than a fixed size)
        StringBuffer buf = new StringBuffer(dataOut.length() + 100);
        buf.append(dataOut);
        dataOut = null;

        // FIXME - Narayan had code to show raw data via an attribute, I've
        // broken it and need to fix it...

        // When writing the data, if it it's a normal HTML display, then go ahead
        // and write the data within the context of the web page, for any of the
        // other export types, we need to clear our buffer before we can write
        // out the data

        int returnValue = EVAL_PAGE;
        if (this.exportType == EXPORT_TYPE_NONE) {
            write(buf);
        }
        else {
            // If we can't clear the buffer, then it has probably already been written to
        	// the browser.  On JSP pages in the ProFoRMS system, the buffer can be set
        	// at the top of a JSP page with the tag: <%@ page autoFlush="true" buffer="1094kb"%>

            try {
            	out.clear();

                String fileName = "";
                if (this.exportType == EXPORT_TYPE_CSV) {
                    res.setContentType(prop.getProperty("export.csv.mimetype"));
                    fileName = prop.getProperty("export.csv.filename"); // added by Joshua Park for filename handling
                    if (fileName == null || fileName.isEmpty()) {
                    	fileName = "export.csv";
                    }
                    res.setHeader("Content-Disposition", "attachment;filename=" + fileName); // added by Joshua Park for filename handling
                    out.write(buf.toString());
                    out.flush();
                }
                else if (this.exportType == EXPORT_TYPE_EXCEL) {
                    res.setContentType(prop.getProperty("export.excel.mimetype"));
                    fileName = prop.getProperty("export.excel.filename"); // added by Joshua Park for filename handling
                    if (fileName == null || fileName.isEmpty()) {
                    	fileName = "export.xls";
                    }
                    res.setHeader("Content-Disposition", "attachment;filename=" + fileName); // added by Joshua Park for filename handling
                    out.write(buf.toString());
                    out.flush();
                }
                else if (this.exportType == EXPORT_TYPE_XML) {
                    res.setContentType(prop.getProperty("export.xml.mimetype"));
                    fileName = prop.getProperty("export.xml.filename"); // added by Joshua Park for filename handling
                    if (fileName == null || fileName.isEmpty()) {
                    	fileName = "export.xml";
                    }
                    res.setHeader("Content-Disposition", "attachment;filename=" + fileName); // added by Joshua Park for filename handling
                    out.write(buf.toString());
                    out.flush();
                }

                returnValue = SKIP_PAGE;
            }
            catch (java.io.IOException ioe) {
                // FIXME - Beef up this error message and give the user some hints
                // on what they can do to resolve this...
                write(ioe.toString());
                ioe.printStackTrace();
            }
        }
        return returnValue;
    }
    
    /**
     * Constructs the config javascript from the config object and local logic.
     * 
     * @return String representation of the javascript to output
     */
    private String buildConfigJavascript() {
    	String output = "";
    	JSONObject localConfig = new JSONObject();
    	// config comes from the tag but also some decisions in this class
    	String requestUri = this.getRequestURI();
    	
    	try {
	    	if (requestUri != null && !requestUri.equals("")) {
	    		localConfig.put("serverSide", true);
	    		localConfig.put("ajax", requestUri);
	    	}
	    	else {
	    		localConfig.put("serverSide", false);
	    	}
	    	
	    	output = localConfig.toString();
	    	// also trim the last character from localConfig
	    	output = trimLastCharIf(output, "}");
	    	output = trimFirstCharIf(output, "{");
	    	
	    	if (this.configTag != null) {
		    	String configFromTag = this.configTag.getConfig();
	
		    	// trim off the braces from the input object
		    	configFromTag = configFromTag.trim().replace("\t", "").replace("\n", "").replace("\r", "");
		    	configFromTag = trimLastCharIf(configFromTag.trim(), "}");
		    	configFromTag = trimFirstCharIf(configFromTag.trim(), "{");
		    	
		    	// it is impossible to NOT have input here because of the logic above
		    	// so add a comma
		    	output += ",";
		    	output += configFromTag;
	    	}

	    	output = "{" + output + "}";
    	}
    	catch(JSONException e) {
    		e.printStackTrace();
    		output = "{}";
    	}

    	return output;
    }
    
    private String addFilters() {
    	String output = "";
    	
    	for (int i = 0; i < this.filterTags.size(); i++) {
    		if (!output.equals("")) {
    			output += ",";
    		}
    		
    		// TODO: complete
    		
    		output += this.filterTags.get(i).getFilter();
    	}
    	
    	return "[" + output + "]";
    }
    
    private String addButtons() {
    	String output = "";
    	
    	// TODO: fill in
    	
    	return "[" + output + "]";
    }

    /**
     * Given an Object, let's do our best to iterate over it
     */
    public static Iterator getIterator(Object o) throws JspException {

        Iterator iterator = null;

        if (o instanceof Collection) {
            iterator = ((Collection) o).iterator();
        }
        else if (o instanceof Iterator) {
            iterator = (Iterator) o;
        }
        else if (o instanceof Map) {
            iterator = ((Map) o).entrySet().iterator();
            /*
             *   This depends on importing struts.util.* -- see remarks in the import section of the file
             *
             * } else if( o instanceof Enumeration ) {
             *     iterator = new IteratorAdapter( (Enumeration)o );
             * }
            */
        }
        else {
            throw new JspException("I do not know how to iterate over '" + o.getClass() + "'.");
        }
        return iterator;
    }

    /**
     * This returns a list of all of the data that will be displayed on the
     * page via the table tag.  This might include just a subset of the total
     * data in the list due to to paging being active, or the user asking us
     * to just show a subset, etc.
     *
     * <p>The list that is returned from here is not the original list, but it
     * does contain references to the same objects in the original list, so that
     * means that we can sort and reorder the list, but we can't mess with the
     * data objects in the list.</p>
     */
    public List getViewableData() throws JspException {
        List viewableData = new ArrayList();
        Object collection = this.list;

        if (collection == null) {
            collection = this.lookup(this.pageContext, this.name,
                                     this.property, this.scope, false);
        }

        // Should put a check here that allows the user to choose if they
        // want this to error out or not...  Right now if we can't find a pointer
        // to their list or if they give us a null list, we just create an
        // empty list.

        if (collection == null) {
            collection = new ArrayList();
        }

        // Constructor an iterator that we use to actually go through the list
        // they have provided (no matter what type of data collection set they
        // are using


        /* YIKES! If we use a RowSetDynaClass to wrap a ResultSet, we'd
         * copy the entire set to a List -- not good at all if paging
         * (though functional -- I tested). Furthermore, RowSetDynaClass
         * is not yet officially released, so I'm leaving this out for
         * now.
         *
         * Anyway, the proper semantics would involve a class like ResultSetDynaClass,
         * but with some changes to the Iterator I'l trying to work on.
         *
         * if (collection instanceof ResultSet)
         *   try {
         *       collection = new RowSetDynaClass((ResultSet)collection).getRows ();
         *   } catch (java.sql.SQLException e) {
         *       throw new JspException( "Problems iterating over ResultSet.", e);
         *       }
         */

        if (collection.getClass().isArray())
            collection = Arrays.asList((Object[]) collection);

        Iterator iterator = getIterator(collection);

        // If the user has changed the way our default behavior works, then we
        // need to look for it now, and resort things if needed before we ask
        // for the viewable part. (this is a bad place for this, this should be
        // refactored and moved somewhere else).

        // Load our table decorator if it is requested
        this.dec = this.loadDecorator();
        if (this.dec != null) this.dec.init(this.pageContext, collection);

        if (!prop.getProperty("sort.behavior").equalsIgnoreCase("page")) {
            // If they have changed the default sorting behavior of the table
            // tag, and just clicked on a column, then reset their page number
            HttpServletRequest req = (HttpServletRequest) this.pageContext.getRequest();
        }

        // If they have asked for an subset of the list via the offset or length
        // attributes, then only fetch those items out of the master list.
        int offsetValue = this.getOffsetValue();
        int lengthValue = this.getLengthValue();

        for (int i = 0; i < offsetValue; i++) {
            if (iterator.hasNext()) {
                iterator.next();
            }
        }

        int cnt = 0;
        while (iterator.hasNext()) {
            if (lengthValue > 0 && ++cnt > lengthValue) {
                break;
            }
            viewableData.add(iterator.next());
        }

        return viewableData;
    }

    private StringBuffer getHTMLData(List viewableData) throws JspException {
        StringBuffer buf = new StringBuffer(8000);

        // build an array of column decorator objects - 1 for each column tag
        ColumnDecorator[] colDecorators = new ColumnDecorator[columns.size()];
        for (int c = 0; c < columns.size(); c++) {
            String columnDecorator = ((JSONColumnTag30) columns.get(c)).getDecorator();
            colDecorators[c] = loadColumnDecorator(columnDecorator);
            if (colDecorators[c] != null)
                colDecorators[c].init(this.pageContext, this.list);
        }

        // Ok, start bouncing through our list.........

        // we now write this output as javascript, not JSON (javascript can contain functions)
        // so there is a bit more manual building going on here
        buf.append("<table id=\"" + this.getName()+ "\"><script type=\"text/javascript\">\n");
        buf.append("DataTableConfig." + this.getName() + " = {");
        buf.append("columns:" + this.getTableHeader());
        buf.append(",config:" + this.buildConfigJavascript());
        buf.append(",filters:" + this.addFilters() + "");
        buf.append(",buttons:" + this.addButtons());
        buf.append(",data:" + buildTableData(viewableData, colDecorators).toString());
        buf.append(",exportRow:" + this.getTableFooter());
        buf.append("};"); // closes off the object
        buf.append("\n</script></table>\n");

        if (this.dec != null) this.dec.finish();
        this.dec = null;

        // paulsenj:columndecorator
        for (int c = 0; c < columns.size(); c++)
            if (colDecorators[c] != null)
                colDecorators[c].finish();

        return buf;
    }

    /**
     * This returns a table of data in CSV format
     */
    private StringBuffer getRawData(List viewableData) throws JspException {
        StringBuffer buf = new StringBuffer(8000);
        Iterator iterator = viewableData.iterator();
        // iterate through all the objects and dispaly 'em.

        int rowcnt = 0;
        boolean decorated = prop.getProperty("export.decorated").equalsIgnoreCase("true");

        if (prop.getProperty("export.amount").equalsIgnoreCase("list")) {
            iterator = getIterator(this.list);
        }

        // If they want the first line to be the column titles, then spit them out

        if (prop.getProperty("export.csv.include_header").equalsIgnoreCase("true")) {
            for (int i = 0; i < this.columns.size(); i++) {
            	JSONColumnTag30 tag = (JSONColumnTag30) this.columns.get(i);

                String header = tag.getTitle();
                if (header == null) {
                    header = StringUtil.toUpperCaseAt(tag.getProperty(), 0);
                }
                // keeps invisible data from the table out of the csv
                else if (!header.equals("")) {
                	buf.append(header);
	                if (this.columns.size() > (i + 1)) buf.append(",");
                }
            }
            buf.append("\n");
        }

        while (iterator.hasNext()) {
            Object obj = iterator.next();

            if (decorated && this.dec != null) {
                String rt = this.dec.initRow(obj, rowcnt, rowcnt + this.getOffsetValue());
                if (rt != null) buf.append(rt);
            }

            pageContext.setAttribute("smartRow", obj);

            for (int i = 0; i < this.columns.size(); i++) {
            	JSONColumnTag30 tag = (JSONColumnTag30) this.columns.get(i);
	            // keeps invisible data from the table out of the csv
	            if (!tag.getTitle().equals("")) {
	                Object value = null;
	                if (tag.getValue() != null) {
	                    value = tag.getValue();
	                }
	                else {
	                    if (tag.getProperty().equalsIgnoreCase("table_index")) {
	                        value = String.valueOf(rowcnt);
	                    }
	                    else {
	                        value = this.lookup(pageContext, "smartRow",
	                                            tag.getProperty(), null, decorated);
	                    }
	                }
	
	                if (value == null && tag.getNulls() == null) {
	                    value = "";
	                }
	
	                if (tag.getDoubleQuote() != null) {
	                    buf.append("\"" + value + "\"");
	                }
	                else {
	                    buf.append(value.toString());
	                }
	
	                if (this.columns.size() > (i + 1)) {
	                    /**
	                     *  Do not add comma at the end of the line
	                     */
	                    buf.append(",");
	                }
            	}
            }

            buf.append("\n");
            rowcnt++;
        }

        return buf;
    }

    /**
     * This returns a table of data in Excel format
     */
    private StringBuffer getExcelData(List viewableData) throws JspException {
        StringBuffer buf = new StringBuffer(8000);
        Iterator iterator = viewableData.iterator();

        int rowcnt = 0;
        boolean decorated = prop.getProperty("export.decorated").equalsIgnoreCase("true");

        if (prop.getProperty("export.amount").equalsIgnoreCase("list")) {
            /**
 Ok.  This is soon to be an unsupported tag.  But I can't
get the whole list when I export through excel, even
though I have set the export.amount to equal list.

I found that when we are building the view of "viewableData", it is shortening it to the page
size, even though it should be exporting the entire list.

This addition keeps all things the same, but makes sure
that the entire list is exported
             */
            Object obj = this.lookup(this.pageContext, this.name,
                                     this.property, this.scope, false);
            iterator = getIterator(obj);
        }

        // If they want the first line to be the column titles, then spit them out

        if (prop.getProperty("export.excel.include_header").equalsIgnoreCase("true")) {
            for (int i = 0; i < this.columns.size(); i++) {
            	JSONColumnTag30 tag = (JSONColumnTag30) this.columns.get(i);

                String header = tag.getTitle();
                if (header == null) {
                    header = StringUtil.toUpperCaseAt(tag.getProperty(), 0);
                }
                else if (!header.equals("")) {
	                buf.append(header);
	                if (this.columns.size() > (i + 1)) buf.append("\t");
                }
            }
            buf.append("\n");
        }

        while (iterator.hasNext()) {
            Object obj = iterator.next();

            if (decorated && this.dec != null) {
                String rt = this.dec.initRow(obj, rowcnt, rowcnt + this.getOffsetValue());
                if (rt != null) buf.append(rt);
            }

            pageContext.setAttribute("smartRow", obj);

            for (int i = 0; i < this.columns.size(); i++) {
            	JSONColumnTag30 tag = (JSONColumnTag30) this.columns.get(i);
            	if (!tag.getTitle().equals("")) {
	                Object value = null;
	                if (tag.getValue() != null) {
	                    value = tag.getValue();
	                }
	                else {
	                    if (tag.getProperty().equalsIgnoreCase("table_index")) {
	                        value = String.valueOf(rowcnt);
	                    }
	                    else {
	                        value = this.lookup(pageContext, "smartRow",
	                                            tag.getProperty(), null, decorated);
	                    }
	                }
	
	                if (value == null && tag.getNulls() == null) {
	                    value = "";
	                }
	
	                if (tag.getDoubleQuote() != null) {
	                    buf.append("\"" + value + "\"");
	                }
	                else {
	                    buf.append(value);
	                }
	
	                if (this.columns.size() > (i + 1)) {
	                    /**
	                     *  Do not add comma at the end of the line
	                     */
	                    buf.append("\t");
	                }
            	}
            }

            rowcnt++;
            //buf.append( "<br>\n" );
            buf.append("\n");
        }

        return buf;
    }

    /**
     * This returns a table of data in XML format
     *
     * FIXME - this obviously needs cleaned up...
     */
    private StringBuffer getXMLData(List viewableData) throws JspException {
        StringBuffer buf = new StringBuffer(8000);
        Iterator iterator = viewableData.iterator();
        // iterate through all the objects and dispaly 'em.

        int rowcnt = 0;
        boolean decorated = prop.getProperty("export.decorated").equalsIgnoreCase("true");

        if (prop.getProperty("export.amount").equalsIgnoreCase("list")) {
            iterator = getIterator(this.list);
        }

        buf.append("<table>\n");
        while (iterator.hasNext()) {
            Object obj = iterator.next();

            if (decorated && this.dec != null) {
                String rt = this.dec.initRow(obj, rowcnt, rowcnt + this.getOffsetValue());
                if (rt != null) buf.append(rt);
            }

            pageContext.setAttribute("smartRow", obj);
            buf.append("<row>\n");

            for (int i = 0; i < this.columns.size(); i++) {
            	JSONColumnTag30 tag = (JSONColumnTag30) this.columns.get(i);

                Object value = null;
                if (tag.getValue() != null) {
                    value = tag.getValue();
                }
                else {
                    if (tag.getProperty().equalsIgnoreCase("table_index")) {
                        value = String.valueOf(rowcnt);
                    }
                    else {
                        value =
                                this.lookup(pageContext, "smartRow",
                                            tag.getProperty(), null, decorated);
                    }
                }

                if (value == null && tag.getNulls() == null) {
                    value = "";
                }

                // TODO - need to escape this or something...
                buf.append("<column>" + value + "</column>\n");
            }
            rowcnt++;
            buf.append("</row>\n");
        }
        buf.append("</table>\n");
        return buf;
    }

    // --------------------------------------------------------- Utility Methods

    /**
     * Generates the table header, including the first row of the table which
     * displays the titles of the various columns
     */
    private String getTableHeader() {
        JSONArray aoColumns = new JSONArray();
        
        int titleColumnsToSkip = 0;

        for (int i = 0; i < this.columns.size(); i++) {
            if (titleColumnsToSkip > 0) {
                titleColumnsToSkip--;
                continue;
            }

            JSONColumnTag30 tag = (JSONColumnTag30) this.columns.get(i);

            String header = tag.getTitle();
            if (header == null) {
                header = StringUtil.toUpperCaseAt(tag.getProperty(), 0);
            }
            
            boolean visible = tag.isVisible();

            JSONObject column = new JSONObject();
            try {
            	column.put("title", header);
            	column.put("data", makeValidPropertyName(tag.getProperty()));
            	if (!visible) {
            		column.put("visible", visible);
            	}
            	if (!tag.getOrderable()) {
            		column.put("orderable", tag.getOrderable());
            	}
            	aoColumns.put(column);
            }
            catch(Exception e) {
            	// should never happen since we're working with strings
            }
        }

        return aoColumns.toString();
    }
    
    private JSONArray buildTableData(List viewableData, ColumnDecorator[] colDecorators) {
    	JSONArray aaData = new JSONArray();
    	StringBuffer buf = new StringBuffer(8000);
    	int rowcnt = 0;
        
        Iterator iterator = viewableData.iterator();
        while (iterator.hasNext()) {
            Object obj = iterator.next();

            // run initRow on the table decorator if there is one
            if (this.dec != null) {
                String rt = this.dec.initRow(obj, rowcnt, rowcnt + this.getOffsetValue());
                if (rt != null) buf.append(rt);
            }

            // run initRow on the column decorator if there is one
            for (int c = 0; c < columns.size(); c++) {
                if (colDecorators[c] != null) {
                    colDecorators[c].initRow(obj, rowcnt, rowcnt + (this.getPagesizeValue() * (this.pageNumber - 1)));
                }
            }

            // I'm not really sure what smartRow is all about...
            pageContext.setAttribute("smartRow", obj);


            if (this.dec != null) {
                String rt = this.dec.startRow();
                if (rt != null) buf.append(rt);
            }

            // Start building the row to be displayed...
            JSONObject row = new JSONObject();

            // Bounce through our columns and pull out the data from this object
            // that we are currently focused on (lives in "smartRow").
            for (int i = 0; i < this.columns.size(); i++) {
            	JSONColumnTag30 tag = (JSONColumnTag30) this.columns.get(i);

                // Get the value to be displayed for the column
                Object value = null;
                if (tag.getValue() != null) {
                    value = tag.getValue();
                }
                else {
                	// if the property is "ff", insert the number of rows, not an interpreted value
                    if (tag.getProperty().equalsIgnoreCase("ff")) {
                        value = String.valueOf(rowcnt);
                    }
                    else {
                    	try {
                        value = this.lookup(pageContext, "smartRow", tag.getProperty(), null, true);

                        // paulsenj:columndecorator
                        // if columndecorator supplied for this column, call it to decorate the value
                        // ed - I call the column decorator after the table decorator is called
                        // feel free to modify this as you wish
                        if (colDecorators[i] != null)
                            value = colDecorators[i].decorate(value);
                    	}
                    	catch(JspException e) {
                    		value = colDecorators[i].decorate(value);
                    	}
                    }
                }

                // By default, we show null values as empty strings, unless the
                // user tells us otherwise.

                if (value == null || value.equals("null")) {
                    if (tag.getNulls() == null || tag.getNulls().equalsIgnoreCase("false")) {
                        value = "";
                    }
                }

                // begin handling long text.  We chop the extra off and replace it with a truncated version
                // String to hold what's left over after value is chopped
                String beginning = "";
                String tempValue = "";
                if (value != null) {
                    tempValue = value.toString();
                }

                // trim the string if a maxLength or maxWords is defined
                int maxLength = tag.getMaxLength();
                int maxWords = tag.getMaxWords();
                boolean longerThan = false;
                if (maxLength > 0 || maxWords > 0) {
	                if (maxLength > 0 && tempValue.length() > maxLength) {
	                	beginning = tempValue.substring(0, maxLength);
	                	longerThan = true;
	                }
	                else if (tag.getMaxWords() > 0) {
	                    StringBuffer tmpBuffer = new StringBuffer();
	                    StringTokenizer st = new StringTokenizer(tempValue);
	                    int numTokens = st.countTokens();
	                    if (numTokens > tag.getMaxWords()) {
	                    	
	                        int x = 0;
	                        while (st.hasMoreTokens() && (x < tag.getMaxWords())) {
	                            tmpBuffer.append(st.nextToken() + " ");
	                            x++;
	                        }
	                    }
	                    beginning = tmpBuffer.toString();
	                    longerThan = true;
	                }
	                
	                if (longerThan) {
		                value = "<span class=\"dtLess\">" 
	                			+ beginning 
	                			+ "<a href=\"javascript:;\" class=\"dtShowMore\">...</a></span>"
	                			+ "<span class=\"dtMore\">" 
	                			+ tempValue 
	                			+ "<a href=\"javascript:;\" class=\"dtShowLess\"> (less)</a></span>";
		                
	                    value = tempValue.substring(0, maxLength) + "...";
	                }
                }
                
                // calculate row ID and insert it if possible
                if (tag.isRowInput()) {
                	// TODO: fill in row id as DT_RowId : "value"
                }
                else if (!row.has("DT_RowId")) {
                	String strValue = value.toString();
                	String attribute = getHtmlAttributeFromString(strValue, "id");
                	if (!attribute.equals("")) {
                		try {
                			row.put("DT_RowId", attribute);
                		}
                		catch(JSONException e) {
                			// well...not much we can do about that except log it
                			System.out.println("failed to add row ID to row in datatable from ID attribute");
                		}
                	}
                	else {
                    	attribute = getHtmlAttributeFromString(strValue, "value");
                    	if (!attribute.equals("")) {
                    		try {
                    			row.put("DT_RowId", attribute);
                    		}
                    		catch(JSONException e) {
                    			// well...not much we can do about that except log it
                    			System.out.println("failed to add row ID to row in datatable from ID attribute");
                    		}
                    	}
                	}
                }

                // Ok, let's write this column's cell...
            	try {
            		row.put(makeValidPropertyName(tag.getProperty()), value.toString());
            	}
            	catch(Exception e) {
            		// if an error happens here, it is better to catch it now
            		// than to risk the entire table
            	}
            }

            aaData.put(row);
            
            // decorators
            if (this.dec != null) {
                String rt = this.dec.finishRow();
                if (rt != null) buf.append(rt);
            }

            // paulsen:columndecorator
            for (int c = 0; c < columns.size(); c++)
                if (colDecorators[c] != null)
                    colDecorators[c].finishRow();

            rowcnt++;
        }
        return aaData;
    }
    
    /**
     * Attempts to find a particular attribute in the given HTML input string.  If not found,
     * returns an empty string
     * 
     * @param input the string to search
     * @param attributeName attribute name to find
     * @return contents of the attribute or empty string if not found
     */
    public String getHtmlAttributeFromString(String input, String attributeName) {
    	String output = "";
    	String attributeWithEquals = attributeName += "=\"";
    	String attributeWithQuotes = attributeName += "=\\\"";
    	
    	int indexOfAttribute = input.indexOf(attributeWithEquals);
    	int indexOfQuotesAttribute = input.indexOf(attributeWithQuotes);
    	if (indexOfAttribute != -1) {
    		indexOfAttribute += attributeWithEquals.length();
    		String strippedString = input.substring(indexOfAttribute);
    		int endIndex = strippedString.indexOf("\"");
    		output = strippedString.substring(0, endIndex);
    	}
    	else if (indexOfQuotesAttribute != -1) {
    		indexOfAttribute += attributeWithQuotes.length();
    		String strippedString = input.substring(indexOfQuotesAttribute);
    		int endIndex = strippedString.indexOf("\"");
    		output = strippedString.substring(0, endIndex);
    	}
    	
    	return output;
    }

    /**
     * Generates table footer with links for export commands.
     */
    private String getTableFooter() {
        StringBuffer buf = new StringBuffer(1000);

        HttpServletRequest req = (HttpServletRequest) this.pageContext.getRequest();
        String url = this.requestURI;
        if (url == null) {
            url = req.getRequestURI();
        }

        // flag to determine if we should use a ? or a &
        int index = url.indexOf('?');
        String separator = "";
        if (index == -1) {
            separator = "?";
        }
        else {
            separator = "&";
        }

        // Put the page stuff there if it needs to be there...

        String qString = req.getQueryString();
        if (qString != null && !qString.equals("")) {
            url += separator + URLEncoder.encode(qString) + "&";
        }
        else {
            url += separator;
        }

        if (this.export != null) {
            // Figure out what formats they want to export, make up a little string

            String formats = "";
            if (prop.getProperty("export.csv").equalsIgnoreCase("true")) {
                formats += "<a href=\"" + url + "exportType=1\">" +
                        prop.getProperty("export.csv.label") + "</a>\n";
            }

            if (prop.getProperty("export.excel").equalsIgnoreCase("true")) {
                if (!formats.equals("")) formats += prop.getProperty("export.banner.sepchar");
                formats += "<a href=\"" + url + "exportType=2\">" +
                        prop.getProperty("export.excel.label") + "</a>\n";
            }

            if (prop.getProperty("export.xml").equalsIgnoreCase("true")) {
                if (!formats.equals("")) formats += prop.getProperty("export.banner.sepchar");
                formats += "<a href=\"" + url + "exportType=3\">" +
                        prop.getProperty("export.xml.label") + "</a>\n";
            }

            Object[] objs = {formats};
            buf.append(MessageFormat.format(prop.getProperty("export.banner"), objs));
        }

        String bufferedOutput = buf.toString();
        bufferedOutput = bufferedOutput.replace("\n", "");
        bufferedOutput = bufferedOutput.replace("\r", "");
        return "\'" + bufferedOutput + "\'";
    }
    
    private String makeValidPropertyName(String propertyName) {
    	return propertyName.replace(".", "");
    }
	
    public void setList(Object v) {
        this.list = v;
    }

    public void setName(String v) {
        this.name = v;
    }

    public void setProperty(String v) {
        this.property = v;
    }

    public void setLength(String v) {
        this.length = v;
    }

    public void setOffset(String v) {
        this.offset = v;
    }

    public void setScope(String v) {
        this.scope = v;
    }

    public void setPagesize(String v) {
        this.pagesize = v;
    }

    public void setDecorator(String v) {
        this.decorator = v;
    }

    public void setExport(String v) {
        this.export = v;
    }

    public void setWidth(String v) {
        this.width = v;
    }

    public void setHeight(String v) {
        this.height = v;
    }

    public void setRules(String v) {
        this.rules = v;
    }

    public void setVspace(String v) {
        this.vspace = v;
    }

    public void setStyleClass(String v) {
        this.clazz = v;
    }

    public void setDisplay(String display) {
        this.display = display;
    }

    public Object getList() {
        return this.list;
    }

    public String getName() {
        return this.name;
    }

    public String getProperty() {
        return this.property;
    }

    public String getLength() {
        return this.length;
    }

    public String getOffset() {
        return this.offset;
    }

    public String getScope() {
        return this.scope;
    }

    public String getPagesize() {
        return this.pagesize;
    }

    public String getDecorator() {
        return this.decorator;
    }

    public String getExport() {
        return this.export;
    }

    public String getWidth() {
        return this.width;
    }

    public String getHeight() {
        return this.height;
    }

    public String getRules() {
        return this.rules;
    }

    public String getVspace() {
        return this.vspace;
    }

    public String getStyleClass() {
        return this.clazz;
    }

    public void reset() {
        this.pageNumber = 1;
        this.sortColumn = 1;
    }

    public String getDisplay() {
        return this.display;
    }

    public String getRequestURI() {
        return requestURI;
    }

    public void setRequestURI(String requestURI) {
        this.requestURI = requestURI;
    }

    /**
     * This functionality is borrowed from struts, but I've removed some
     * struts specific features so that this tag can be used both in a
     * struts application, and outside of one.
     *
     * Locate and return the specified bean, from an optionally specified
     * scope, in the specified page context.  If no such bean is found,
     * return <code>null</code> instead.
     *
     * @param pageContext Page context to be searched
     * @param name Name of the bean to be retrieved
     * @param scope Scope to be searched (page, request, session, application)
     *  or <code>null</code> to use <code>findAttribute()</code> instead
     *
     * @exception JspException if an invalid scope name is requested
     */
    public Object lookup(PageContext pageContext, String name, String scope)
            throws JspException {

        Object bean = null;
        if (scope == null)
            bean = pageContext.findAttribute(name);
        else if (scope.equalsIgnoreCase("page"))
            bean = pageContext.getAttribute(name, PageContext.PAGE_SCOPE);
        else if (scope.equalsIgnoreCase("request"))
            bean = pageContext.getAttribute(name, PageContext.REQUEST_SCOPE);
        else if (scope.equalsIgnoreCase("session"))
            bean = pageContext.getAttribute(name, PageContext.SESSION_SCOPE);
        else if (scope.equalsIgnoreCase("application"))
            bean = pageContext.getAttribute(name, PageContext.APPLICATION_SCOPE);
        else {
            Object[] objs = {name, scope};
            String msg =
                    MessageFormat.format(prop.getProperty("error.msg.cant_find_bean"), objs);

            throw new JspException(msg);
        }

        return (bean);
    }

    /**
     * This functionality is borrowed from struts, but I've removed some
     * struts specific features so that this tag can be used both in a
     * struts application, and outside of one.
     *
     * Locate and return the specified property of the specified bean, from
     * an optionally specified scope, in the specified page context.
     *
     * @param pageContext Page context to be searched
     * @param name Name of the bean to be retrieved
     * @param property Name of the property to be retrieved, or
     *  <code>null</code> to retrieve the bean itself
     * @param scope Scope to be searched (page, request, session, application)
     *  or <code>null</code> to use <code>findAttribute()</code> instead
     *
     * @exception JspException if an invalid scope name is requested
     * @exception JspException if the specified bean is not found
     * @exception JspException if accessing this property causes an
     *  IllegalAccessException, IllegalArgumentException,
     *  InvocationTargetException, or NoSuchMethodException
     */
    public Object lookup(PageContext pageContext, String name,
                         String property, String scope, boolean useDecorator)
            throws JspException {
        if (useDecorator && this.dec != null) {
            // First check the decorator, and if it doesn't return a value
            // then check the inner object...

            try {
                if (property == null) return this.dec;
                return (PropertyUtils.getProperty(this.dec, property));
            }
            catch (IllegalAccessException e) {
                Object[] objs = {property, this.dec};
                throw new JspException(
                        MessageFormat.format(prop.getProperty("error.msg.illegal_access_exception"), objs));
            }
            catch (InvocationTargetException e) {
                Object[] objs = {property, this.dec};
                throw new JspException(
                        MessageFormat.format(prop.getProperty("error.msg.invocation_target_exception"), objs));
            }
            catch (NoSuchMethodException e) {
                // We ignore this puppy and just fall down to the bean lookup below.
            }
        }

        // Look up the requested bean, and return if requested
        Object bean = this.lookup(pageContext, name, scope);
        if (property == null) return (bean);

        if (bean == null) {
            Object[] objs = {name, scope};
            throw new JspException(
                    MessageFormat.format(prop.getProperty("error.msg.cant_find_bean"), objs));
        }

        // Locate and return the specified property

        try {
            return (PropertyUtils.getProperty(bean, property));
        }
        catch (IllegalAccessException e) {
            Object[] objs = {property, name};
            throw new JspException(
                    MessageFormat.format(prop.getProperty("error.msg.illegal_access_exception"), objs));
        }
        catch (InvocationTargetException e) {
            Object[] objs = {property, name};
            throw new JspException(
                    MessageFormat.format(prop.getProperty("error.msg.invocation_target_exception"), objs));
        }
        catch (NoSuchMethodException e) {
            Object[] objs = {property, name};
            throw new JspException(
                    MessageFormat.format(prop.getProperty("error.msg.nosuchmethod_exception"), objs));
        }
    }
    
    private String trimLastCharIf(String operant, String compare) {
    	String lastCharacter = Character.toString(operant.charAt(operant.length() - 1));
    	if (lastCharacter.equals(compare)) {
    		operant = operant.substring(0, operant.length() - 1);
    	}
    	return operant;
    }

    private String trimFirstCharIf(String operant, String compare) {
    	String lastCharacter = Character.toString(operant.charAt(0));
    	if (lastCharacter.equals(compare)) {
    		operant = operant.substring(1, operant.length());
    	}
    	return operant;
    }
    
    /**
     * If the user has specified a decorator, then this method takes care of
     * creating the decorator (and checking to make sure it is a subclass of
     * the TableDecorator object).  If there are any problems loading the
     * decorator then this will throw a JspException which will get propogated
     * up the page.
     */
    private Decorator loadDecorator()
            throws JspException {
        if (this.decorator == null || this.decorator.length() == 0) return null;

        try {
            Class c = Class.forName(this.decorator);

            // Probably should really be TableDecorator, need to move towards that
            // in the future, if I did it now, it would break stuff, and I have
            // a feeling I'm going to make changes to the decorator more in a little
            // bit, so I would rather just do it all at once...

            if (!Class.forName("org.apache.taglibs.display.Decorator").isAssignableFrom(c)) {
                throw new JspException(prop.getProperty("error.msg.invalid_decorator"));
            }

            return (Decorator) c.newInstance();
        }
        catch (Exception e) {
            throw new JspException(e.toString());
        }
    }

    /**
     * paulsenj:columndecorator
     * If the user has specified a column decorator, then this method takes care of
     * creating the decorator (and checking to make sure it is a subclass of
     * the ColumnDecorator object).  If there are any problems loading the
     * decorator then this will throw a JspException which will get propogated
     * up the page.
     */
    private ColumnDecorator loadColumnDecorator(String columnDecorator) throws JspException {
        if (columnDecorator == null || columnDecorator.length() == 0)
            return null;

        try {
            Class c = Class.forName(columnDecorator);

            // paulsenj - removed 'jakarta' from class name
            if (!Class.forName("org.apache.taglibs.display.ColumnDecorator").isAssignableFrom(c))
                throw new JspException("column decorator class is not a subclass of ColumnDecorator.");

            return (ColumnDecorator) c.newInstance();
        }
        catch (Exception e) {
            throw new JspException(e.toString());
        }
    }

    /**
     * Called by the setProperty tag to override some default behavior or text
     * string.
     */
    public void setProperty(String name, String value) {
        this.prop.setProperty(name, value);
    }

    /**
     * This sets up all the default properties for the table tag.
     */
    private void loadDefaultProperties() {
        this.prop = new Properties();

        prop.setProperty("basic.show.header", "true");
        prop.setProperty("basic.msg.empty_list", "Nothing found to display");

        prop.setProperty("sort.behavior", "page");

        prop.setProperty("export.banner", "Export options: {0}");
        prop.setProperty("export.banner.sepchar", " | ");
        prop.setProperty("export.csv", "true");
        prop.setProperty("export.csv.label", "CSV");
        prop.setProperty("export.csv.mimetype", "text/csv");
        prop.setProperty("export.csv.include_header", "false");
        prop.setProperty("export.excel", "true");
        prop.setProperty("export.excel.label", "Excel");
        prop.setProperty("export.excel.mimetype", "application/vnd.ms-excel");
        prop.setProperty("export.excel.include_header", "false");
        prop.setProperty("export.xml", "true");
        prop.setProperty("export.xml.label", "XML");
        prop.setProperty("export.xml.mimetype", "text/xml");
        prop.setProperty("export.amount", "page");
        prop.setProperty("export.decorated", "true");

        prop.setProperty("paging.banner.placement", "top");
        prop.setProperty("paging.banner.item_name", "item");
        prop.setProperty("paging.banner.items_name", "items");
        prop.setProperty("paging.banner.no_items_found", "No {0} found.");
        prop.setProperty("paging.banner.one_item_found", "1 {0} found.");
        prop.setProperty("paging.banner.all_items_found", "{0} {1} found, displaying all {2}");
        prop.setProperty("paging.banner.some_items_found", "{0} {1} found, displaying {2} to {3}");
        prop.setProperty("paging.banner.include_first_last", "false");
        prop.setProperty("paging.banner.first_label", "First");
        prop.setProperty("paging.banner.last_label", "Last");
        prop.setProperty("paging.banner.prev_label", "Prev");
        prop.setProperty("paging.banner.next_label", "Next");
        prop.setProperty("paging.banner.group_size", "8");

        prop.setProperty("error.msg.cant_find_bean", "Could not find bean {0} in scope {1}");
        prop.setProperty("error.msg.invalid_bean", "The bean that you gave me is not a Collection I understand: {0}");
        prop.setProperty("error.msg.no_column_tags", "Please provide column tags.");
        prop.setProperty("error.msg.illegal_access_exception", "IllegalAccessException trying to fetch property {0} on bean {1}");
        prop.setProperty("error.msg.invocation_target_exception", "InvocationTargetException trying to fetch property {0} on bean {1}");
        prop.setProperty("error.msg.nosuchmethod_exception", "NoSuchMethodException trying to fetch property {0} on bean {1}");
        prop.setProperty("error.msg.invalid_decorator", "Decorator class is not a subclass of TableDecorator");
        prop.setProperty("error.msg.invalid_page", "Invalid page ({0}) provided, value should be between 1 and {1}");
    }
}

