package gov.nih.nichd.ctdb.common.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import gov.nih.nichd.ctdb.question.domain.Question;
import gov.nih.nichd.ctdb.response.domain.Response;

public class DataReaderUtil extends DefaultHandler{
	
	private List<Response> instanceData;
	private Response currentResponse;
	private StringBuffer adminFormBuffer;
	private StringBuffer answerBuffer;
	private String adminFormId;
	private Stack<Integer> workingStack;
    private Integer depth;
	
	public String getAdminFormId(){
		return adminFormId;
	}
	public List<Response> getInstanceData(){
		return instanceData;
	}
	
	public DataReaderUtil() {
    	super();
    	
    	adminFormBuffer = new StringBuffer();
    	answerBuffer = new StringBuffer();
    	currentResponse = new Response();
    	
    	instanceData = new ArrayList<Response>();
    	workingStack = new Stack<Integer>();
    	depth = 0;
    }
	
	@Override
	public void startDocument() throws SAXException {
		
	}

	@Override
	public void endDocument() throws SAXException {
	}

	@Override
	public void startElement(String namespaceURI, String localName, String qName, Attributes atts) throws SAXException {
		
		if(depth == 3){
			if(localName.equals("AdminFormId")){
				adminFormBuffer = new StringBuffer();
			}
			else{
				currentResponse = new Response();
				currentResponse.setQuestion(new Question());
				currentResponse.getQuestion().setName(localName);
				instanceData.add(currentResponse);
				answerBuffer = new StringBuffer();
			}
		}
		
		workingStack.push(depth);
		depth++;
		
	}
	
	@Override
	public void endElement(String uri, String localName, String qName) {
		depth = (int)workingStack.pop();
		
		if (adminFormBuffer != null) {
			adminFormId = adminFormBuffer.toString();
			adminFormBuffer = null;
		}
		
		if (answerBuffer != null) {
			List<String> answers = new ArrayList<String>();
			answers.add(answerBuffer.toString());
			currentResponse.setSubmitAnswers(answers);
			answerBuffer = null;
		}
	}

	@Override
	public void characters(char[] ch, int start, int length) {
		
		if (adminFormBuffer != null) {
			adminFormBuffer.append(ch, start, length);
		}
		
		if (answerBuffer != null) {
			answerBuffer.append(ch, start, length);
		}
	}

}
