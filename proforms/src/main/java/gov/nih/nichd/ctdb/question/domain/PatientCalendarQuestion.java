package gov.nih.nichd.ctdb.question.domain;

import java.util.ResourceBundle;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import gov.nih.nichd.ctdb.common.TransformationException;
 
/**
 * Question DomainObject for the NICHD CTDB Application
 *
 * @author Booz Allen Hamilton
 * @version 1.0
 */
public class PatientCalendarQuestion extends Question
{
	private static final long serialVersionUID = 5218819376789845530L;
	
	private String[][] calendarData;
    private int[][] responseIds;
    private int numberOfColumns = 43;
    private int numberOfRows = 13;
       
    /**
     * Default Constructor for the PatientCalendarQuestion Domain Object
     */
    public PatientCalendarQuestion()
    {
    	super();
    	this.formQuestionAttributes.setInstanceType(InstanceType.QUESTION);
    	this.type = QuestionType.PATIENT_CALENDAR;
    	calendarData = new String[43][13];
    	responseIds = new int[43][13];
    }
    
    /**
     * Constructor that takes an 2d array as starting data
     * 
     * @param calendarData thecalendarData
     */
    public PatientCalendarQuestion(String[][] calendarData)
    {
    	super();
        this.formQuestionAttributes.setInstanceType(InstanceType.QUESTION);
        this.calendarData = calendarData;
    }
    
    public void clone(PatientCalendarQuestion question)
    {
        super.clone(question); 
    }

    /**
     * Get the entire set ofcalendarData
     * 
     * @return the array containing all of the data
     */
    public String[][] getCalendarData() {
        return calendarData;
    }

    /**
     * set the set ofcalendarData
     * 
     * @param strings the calendar data to set
     */
    public void setCalendarData(String[][] strings) {
    calendarData = strings;
    }


    /**
     * This method obtains data at a given x (col), y (row) coordinate
     * 
     * @param x cooridinate - the column
     * @param y coordinate - the row
     * @return the data at that point
     */
    public String getCalendarData(int x, int y)
    {
        return calendarData[x][y];
    }

    /**
     * Set the data at a specified point on the calendar. 
     * 
     * @param x coordinate - the column
     * @param y coordinate - the row
     * @param data - the data to set
     */
    public void setCalendarData(int x, int y, String data)
    {
        calendarData[x][y] = data; 
    }

    /**
     * Compare if this Object is equal to Object o through member-wise comparison.
     *
     * @param o Object the object to compare with
     * @return True if they are equal, false otherwise
     */
    public boolean equals(Object o)
    {
        return super.equals(o);
    }

    /**
     * This method allows the transformation of a Question into an XML Document.
     * If no implementation is available at this time,
     * an UnsupportedOperationException will be thrown.
     *
     * @return XML Document
     * @throws TransformationException is thrown if there is an error during the XML tranformation
     */
    public Document toXML() throws TransformationException
    {
        Document document = super.newDocument();
        Element root = super.initXML(document, "question");
        ResourceBundle resources = ResourceBundle.getBundle("ApplicationResources");

        root.setAttribute("instanceType", InstanceType.QUESTION.getDispValue());
        root.setAttribute("required", Boolean.toString(super.getFormQuestionAttributes().isRequired()));
        root.setAttribute("type", super.getType().getDispValue());
        root.setAttribute("hasSkipRule", Boolean.toString(this.getFormQuestionAttributes().hasSkipRule()));
        if(this.getFormQuestionAttributes().hasSkipRule())
        {
            root.setAttribute("skipOperator", this.getFormQuestionAttributes().getSkipRuleOperatorType().getDispValue());
            root.setAttribute("skipRule", this.getFormQuestionAttributes().getSkipRuleType().getDispValue());
            root.setAttribute("skipEquals", this.getFormQuestionAttributes().getSkipRuleEquals());
        }
        root.setAttribute("editType", ""); 
    
        Element nameNode = document.createElement("name");
        nameNode.appendChild(document.createTextNode(super.getName()));
        root.appendChild(nameNode);

        Element textNode = document.createElement("text");
        textNode.appendChild(document.createTextNode(super.getText()));
        root.appendChild(textNode);
                             
        String colCount = resources.getString("patient.calendar.columns.number");
        int cols; 
        try 
        {
            cols = Integer.parseInt(colCount);
        }
        catch (NumberFormatException nfe)
        {
            cols = 0;
        }
        
        String colWidth = resources.getString("patient.calendar.columns.width");
        String winWidth = resources.getString("patient.calendar.window.width");
        
        Element columnCount = document.createElement("columncount");
        columnCount.appendChild(document.createTextNode(colCount));
        root.appendChild(columnCount);
        
        Element columnWidth = document.createElement("columnwidth");
        columnWidth.appendChild(document.createTextNode(colWidth));
        root.appendChild(columnWidth);
        
        Element windowWidth = document.createElement("windowwidth");
        windowWidth.appendChild(document.createTextNode(winWidth));
        root.appendChild(windowWidth);
        
        String[] CalendarRowKeys = {              
            "patient.calendar.row.date.",
            "patient.calendar.row.LH.kit.color.",
            "patient.calendar.row.menstruation.",
            "patient.calendar.row.intercourse.",
            "patient.calendar.row.contraception.",
            "patient.calendar.row.pelvic.pain.",
            "patient.calendar.row.vaginal.dryness.",                        
            "patient.calendar.row.headaches.",
            "patient.calendar.row.nausea.",
            "patient.calendar.row.number.flashes.",
            "patient.calendar.row.other.symptoms.",
            "patient.calendar.row.other.medications.",
            "patient.calendar.row.pain.medication.",
        };

        int i, ii;
        
        Element calendarRows = document.createElement("calendarrows");
        Element rowCells;
        Element rowCell; 
        Element calendarRow;  
        Element rowTitle; 
        Element rowIndex;
        Element cellIdentifier; 
        Element cellData;
        Element responseid; 
        Element displaytext;
        
        Element header = document.createElement("calendarheader");

        rowTitle = document.createElement("rowtitle");
        rowTitle.appendChild(document.createTextNode(resources.getString("patient.calendar.row.header.display")));
        header.appendChild(rowTitle);
            
        rowCells = document.createElement("rowcells");
        for (i=1; i < cols+1; i++)
        {
            rowCell = document.createElement("rowcell");
            cellIdentifier = document.createElement("cellidentifier");
            cellIdentifier.appendChild(document.createTextNode(Integer.toString(i)));
            rowCell.appendChild(cellIdentifier);
            rowCells.appendChild(rowCell);                   
        }
        header.appendChild(rowCells);
        root.appendChild(header);

        for (i=0; i<CalendarRowKeys.length; i++)
        {
            calendarRow = document.createElement("calendarrow");

            rowTitle = document.createElement("rowtitle");
            rowTitle.appendChild(document.createTextNode(resources.getString(CalendarRowKeys[i]+"display").replaceAll(" ","&nbsp;")));
            calendarRow.appendChild(rowTitle);
            calendarRow.setAttribute("row", Integer.toString(i));
            rowIndex = document.createElement("idx");
            rowIndex.appendChild(document.createTextNode(Integer.toString(i)));
            calendarRow.appendChild(rowIndex);
            
            rowCells = document.createElement("rowcells");
            for (ii=0; ii<cols; ii++)
            {
                rowCell = document.createElement("rowcell");
                rowCell.setAttribute("cell", Integer.toString(ii));
                cellIdentifier = document.createElement("cellidentifier");
                cellIdentifier.appendChild(document.createTextNode("Q_" + this.getId() + "_pc_" + ii + "_" + i)); // + "_" + resources.getMessage(CalendarRowKeys[i]+"id")));
                cellData = document.createElement("celldata");
                cellData.setAttribute("discrepancy", "none");
                if (calendarData != null)
                {
                    if (ii <calendarData.length && i <calendarData[0].length)
                    {
                            if (calendarData[ii][i] == null)
                            {
                                cellData.appendChild(document.createTextNode(""));
                            }
                        else
                        {
                            cellData.appendChild(document.createTextNode(calendarData[ii][i]));
                        }
                    }
                    else
                    {
                        cellData.appendChild(document.createTextNode(""));
                    }
                }
                else
                {
                    cellData.appendChild(document.createTextNode(""));
                }
                            
                rowCell.appendChild(cellData);
                responseid = document.createElement("responseid");
                responseid.appendChild(document.createTextNode(""));
                displaytext = document.createElement("displaytext");
                displaytext.appendChild(document.createTextNode(""));
                                
                rowCell.appendChild(cellIdentifier);
                rowCell.appendChild(responseid);
                rowCell.appendChild(displaytext);                
                rowCells.appendChild(rowCell);                   
            }
            calendarRow.appendChild(rowCells);                                    
            calendarRows.appendChild(calendarRow);
        }
        root.appendChild(calendarRows);

        Element defaultValueNode = document.createElement("defaultValue");
        defaultValueNode.appendChild(document.createTextNode(""));
        root.appendChild(defaultValueNode);        

        return document;
    }
    /**
     * @return
     */
    public int getNumberOfColumns() {
        return numberOfColumns;
    }

    /**
     * @return
     */
    public int getNumberOfRows() {
        return numberOfRows;
    }

    /**
     * @param i
     */
    public void setNumberOfColumns(int i) {
        numberOfColumns = i;
    }

    /**
     * @param i
     */
    public void setNumberOfRows(int i) {
        numberOfRows = i;
    }

    /**
     * @return
     */
    public int[][] getResponseIds() {
        return responseIds;
    }

    /**
     * @param strings
     */
    public void setResponseIds(int[][] strings) {
        responseIds = strings;
    }

    public int getResponseId(int col, int row)
    {
        return responseIds[col][row];
    }

    public void setResponseId(int col, int row, int value)
    {
        responseIds[col][row]=value;
    }
}
