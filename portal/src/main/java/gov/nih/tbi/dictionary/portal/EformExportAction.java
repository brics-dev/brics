package gov.nih.tbi.dictionary.portal;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.TreeSet;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;

import gov.nih.tbi.PortalConstants;
import gov.nih.tbi.dictionary.model.QuestionDisplayOrderComparator;
import gov.nih.tbi.dictionary.model.hibernate.eform.Eform;
import gov.nih.tbi.dictionary.model.hibernate.eform.QuestionAnswerOption;
import gov.nih.tbi.dictionary.model.hibernate.eform.Section;
import gov.nih.tbi.dictionary.model.hibernate.eform.SectionQuestion;




/**
 * This action class is responsible for exporting the Eform to XML
 * 
 * 
 * @author pandyan
 *
 */
public class EformExportAction extends BaseDictionaryAction {

	
	private static final long serialVersionUID = 1L;

	private InputStream inputStream;

	private String fileName;
	
	private Long eformId;

	
	
	
	
	/**
	 * Marshals out Eform Object to XML.
	 * 
	 * @return
	 */
	public String export() {
		System.out.println("in export");
		
		try {
		Eform eform = eformManager.getEformNoLazyLoad(eformId);
		
		for(Section section : eform.getSectionList()){
			for(SectionQuestion sq : section.getSectionQuestion()){
				if(sq.getQuestion().getQuestionAnswerOption() != null && !sq.getQuestion().getQuestionAnswerOption().isEmpty()){
					TreeSet<QuestionAnswerOption> sortedQAOList = new TreeSet<QuestionAnswerOption>();
					sortedQAOList.addAll(sq.getQuestion().getQuestionAnswerOption());
					sq.getQuestion().setQuestionAnswerOption(sortedQAOList);
				}
			}

				List<SectionQuestion> sortedSQList = new ArrayList<SectionQuestion>();
				sortedSQList.addAll(section.getSectionQuestion());
				Collections.sort(sortedSQList, new QuestionDisplayOrderComparator());
		}
		
		TreeSet<Section> sortedSectionList = new TreeSet<Section>();
		sortedSectionList.addAll(eform.getSectionList());
		eform.setSectionList(sortedSectionList);
		
		JAXBContext jaxContext = JAXBContext.newInstance(Eform.class);
		
		Marshaller marshaller = jaxContext.createMarshaller();

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
		marshaller.marshal(eform, baos);

		inputStream = new ByteArrayInputStream(baos.toByteArray());
		
		fileName = eform.getTitle() + ".xml";
		
		}catch(Exception ex) {
			throw new RuntimeException("Error marshalling to XML", ex);
		}
		return PortalConstants.ACTION_EXPORT;
	}
	
	public InputStream getInputStream() {

		return inputStream;
	}

	public void setInputStream(InputStream inputStream) {

		this.inputStream = inputStream;
	}

	public String getFileName() {

		return fileName;
	}

	public void setFileName(String fileName) {

		this.fileName = fileName;
	}

	public void setEformId(Long eformId) {
		this.eformId = eformId;
	}

	public Long getEformId() {
		return this.eformId;
	}	
}
