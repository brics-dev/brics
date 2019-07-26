
package gov.nih.tbi.dictionary.model;

import gov.nih.tbi.commons.service.ServiceConstants;
import gov.nih.tbi.commons.service.StaticReferenceManager;
import gov.nih.tbi.dictionary.model.hibernate.Category;
import gov.nih.tbi.dictionary.model.hibernate.DataElement;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;

import org.springframework.beans.factory.annotation.Autowired;

/**
 * This class masks the static fields of Data Element form.
 * 
 * @author Francis Chen
 * 
 */
public class DataElementForm {

	@Autowired
	StaticReferenceManager staticManager;

	protected Long id;
	protected String name;
	protected String description;
	protected String shortDescription;
	protected String notes;
	protected String historicalNotes;
	protected String references;
	protected String title;
	protected Category category;


	public DataElementForm() {

	}

	/**
	 * Constructor fetches data for each column in dataElement object
	 * 
	 * @param dataElement
	 */
	public DataElementForm(DataElement dataElement) {

		this.id = dataElement.getId();
		this.name = dataElement.getName();
		this.description = dataElement.getDescription();
		this.shortDescription = dataElement.getShortDescription();
		this.notes = dataElement.getNotes();
		this.historicalNotes = dataElement.getHistoricalNotes();
		this.references = dataElement.getReferences();
		this.title = dataElement.getTitle();
		this.category = dataElement.getCategory();
	}


	public void copyToDataElement(DataElement dataElement) {

		dataElement.setName(name);
		dataElement.setTitle(title);
		dataElement.setCategory(category);
		dataElement.setDescription(description);
		dataElement.setShortDescription(shortDescription);
		dataElement.setNotes(notes);
		dataElement.setHistoricalNotes(historicalNotes);
		dataElement.setReferences(references);
	}

	public Long getId() {

		return id;
	}

	public Category getCategory() {

		return category;
	}

	public void setCategory(String categoryString) throws MalformedURLException, UnsupportedEncodingException {

		if (categoryString != null && !ServiceConstants.EMPTY_STRING.equals(categoryString)) {
			for (Category category : staticManager.getCategoryList()) {
				if (category.getShortName().equals(categoryString)) {
					this.category = category;
				}
			}
		}
	}

	public void setId(Long id) {

		this.id = id;
	}

	public String getName() {

		return name;
	}

	public void setName(String name) {

		if (!ServiceConstants.EMPTY_STRING.equals(name)) {
			this.name = name;
		}
	}

	public String getDescription() {

		return description;
	}

	public void setDescription(String description) {

		if (!ServiceConstants.EMPTY_STRING.equals(description)) {
			this.description = carraigeRemover(description);
		}
	}

	public String getShortDescription() {

		return shortDescription;
	}

	public void setShortDescription(String shortDescription) {

		if (!ServiceConstants.EMPTY_STRING.equals(shortDescription)) {
			this.shortDescription = carraigeRemover(shortDescription);
		}
	}

	public String getNotes() {

		return notes;
	}

	public void setNotes(String notes) {

		if (!ServiceConstants.EMPTY_STRING.equals(notes)) {
			this.notes = carraigeRemover(notes);
		}
	}

	public String getHistoricalNotes() {

		return historicalNotes;
	}

	public void setHistoricalNotes(String historicalNotes) {

		if (!ServiceConstants.EMPTY_STRING.equals(historicalNotes)) {
			this.historicalNotes = carraigeRemover(historicalNotes);
		}
	}

	public String getReferences() {

		return references;
	}

	public void setReferences(String references) {

		if (!ServiceConstants.EMPTY_STRING.equals(references)) {
			this.references = carraigeRemover(references);
		}
	}

	public String getTitle() {

		return title;
	}

	public void setTitle(String title) {

		if (!ServiceConstants.EMPTY_STRING.equals(title)) {
			this.title = carraigeRemover(title);
		}
	}

	public String carraigeRemover(String s) {

		s = s.replace("\r\n", "\n");
		return s;
	}
}
