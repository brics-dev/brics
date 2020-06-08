package gov.nih.tbi.dictionary.portal;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.struts2.result.StreamResult;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import gov.nih.tbi.PortalConstants;
import gov.nih.tbi.dictionary.model.hibernate.FormLabel;
import gov.nih.tbi.idt.ws.IdtInterface;
import gov.nih.tbi.idt.ws.InvalidColumnException;
import gov.nih.tbi.idt.ws.Struts2IdtInterface;

public class FormLabelAction extends BaseDictionaryAction {

	private static final long serialVersionUID = -2449263636428093822L;
	private static final Logger logger = Logger.getLogger(FormLabelAction.class);
	
	private List<FormLabel> formLabels;
	private String currentLabel;
	
	public String listFormLabels() {
		formLabels = dictionaryService.getFormLabels();
		return PortalConstants.ACTION_LIST;
	}
	
	/**
	 * To build the output for the form label datatable.
	 * @return
	 */
	public String getFormLabelOutput() {
		if (formLabels == null) {
			formLabels = dictionaryService.getFormLabels();
		}
		
		try {
			IdtInterface idt = new Struts2IdtInterface();
			ArrayList<FormLabel> outputList = new ArrayList<FormLabel>(formLabels);
			idt.setList(outputList);
			idt.setTotalRecordCount(outputList.size());
			idt.setFilteredRecordCount(outputList.size());
			idt.output();
		} catch (InvalidColumnException e) {
			logger.error("invalid column", e);
		}
		return null;
	}
	
	public String openFormLabelDialog() {
		return PortalConstants.ACTION_FORM_LABEL_DIALOG;
	}
	
	public StreamResult addFormLabel() {
		if (!StringUtils.isEmpty(currentLabel)) {
			FormLabel fl = new FormLabel();
			fl.setLabel(currentLabel);
			fl.setCreatedDate(new Date());
			fl.setCreatedBy(this.getAccount().getUser().getFullName());
			dictionaryService.saveFormLabel(fl);
		}
		
		return new StreamResult(new ByteArrayInputStream((SUCCESS).getBytes()));
	}
	
	
	public StreamResult editFormLabel() {
		
		String labelIdToEdit = getRequest().getParameter("labelIdToEdit");
		
		if (!StringUtils.isEmpty(labelIdToEdit) && !StringUtils.isEmpty(currentLabel)) {
			Long labelId = Long.parseLong(labelIdToEdit);
			
			FormLabel formLabel = dictionaryService.getFormLabel(labelId);
			if (formLabel != null) {
				dictionaryService.updateFormLabel(formLabel, currentLabel);
			}
		}
		
		return new StreamResult(new ByteArrayInputStream((SUCCESS).getBytes()));
	}
	
	
	public String deleteFormLabels() {
		String labelIdsJson = getRequest().getParameter("labelIdsJson");
		
		if (!StringUtils.isEmpty(labelIdsJson)) {
			JsonElement element = new JsonParser().parse(labelIdsJson);
			JsonArray labelIdsJsonArr = element.getAsJsonArray();
			
			if (labelIdsJsonArr != null && labelIdsJsonArr.size() > 0) {
				for (int i = 0; i < labelIdsJsonArr.size(); i++) {
					String labelIdStr = labelIdsJsonArr.get(i).getAsString();
					long labelId = Long.parseLong(labelIdStr);
					dictionaryService.deleteFormLabelById(labelId);
				}
			}
		}
		
		return null;
	}

	
	public List<FormLabel> getFormLabels() {
		return formLabels;
	}

	public void setFormLabels(List<FormLabel> formLabels) {
		this.formLabels = formLabels;
	}

	public String getCurrentLabel() {
		return currentLabel;
	}

	public void setCurrentLabel(String currentLabel) {
		this.currentLabel = currentLabel;
	}

}
