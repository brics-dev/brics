package gov.nih.nichd.ctdb.form.action;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;

import gov.nih.nichd.ctdb.common.BaseAction;
import gov.nih.nichd.ctdb.common.CtdbConstants;
import gov.nih.nichd.ctdb.common.CtdbException;
import gov.nih.nichd.ctdb.common.StrutsConstants;
import gov.nih.nichd.ctdb.common.navigation.LeftNavController;
import gov.nih.nichd.ctdb.common.util.Utils;
import gov.nih.nichd.ctdb.form.common.FormConstants;
import gov.nih.nichd.ctdb.form.common.FormResultControl;
import gov.nih.nichd.ctdb.form.domain.Form;
import gov.nih.nichd.ctdb.form.domain.FormGroup;
import gov.nih.nichd.ctdb.form.manager.FormManager;
import gov.nih.nichd.ctdb.protocol.domain.Protocol;


public class FormGroupHomeAction extends BaseAction {

	private static final long serialVersionUID = -4133557766037195687L;
	private static final Logger logger = Logger.getLogger(FormGroupHomeAction.class);

    public static final String ACTION_MESSAGES_KEY = "FormGroup_ActionMessages";

    private int id = -1;
    private String name = "";
    private String description = "";
    private boolean showAddEdit = false;
    private String selectedFormJson = "[]";

    public void setupPage() throws CtdbException {
    	
        buildLeftNav(LeftNavController.LEFTNAV_FORM_GROUP);
        
    	Protocol p = (Protocol) session.get(CtdbConstants.CURRENT_PROTOCOL_SESSION_KEY);
        FormManager fm = new FormManager();

        request.setAttribute("formGroups", fm.getFormGroups(p.getId()));

        FormResultControl frc = new FormResultControl();
        frc.setFormType("all");
        List<Form> formList = fm.getForms(p.getId(), frc);
        request.setAttribute(FormConstants.PROTOCOLFORMS, formList);
    }
    
    /**
     * Performs the "formGroupHome" action, and is the default action for this action class.
     * This method will build the form group home page, or retrieve an existing form group for
     * editing.
     * 
     */
	public String execute() throws Exception {
    	this.retrieveActionMessages(ACTION_MESSAGES_KEY);
    	
        try {
        	this.setupPage();
        	
            FormManager fm = new FormManager();
            
            if ( id > 0 ) {
            	FormGroup fg = fm.getFormGroup(id);
            	name = fg.getName();
            	description = fg.getDescription();
            	id = fg.getId();
            	showAddEdit = true;
            	
            	// Get associated form IDs and convert the array to JSON
            	int[] formIds = fm.getAssociatedFormIdsForFormGroup(id);
            	JSONArray jArray = new JSONArray(formIds);
            	selectedFormJson = jArray.toString();
            }
        } 
        catch ( CtdbException ce ) {
        	logger.error("Database error occurred while setting up the form group page.", ce);
        	
            return StrutsConstants.FAILURE;
        }
        catch ( JSONException je ) {
        	logger.error("Could not construct a JSON array out of the associated form IDs.", je);
        	
        	return StrutsConstants.FAILURE;
        }
           
    	return SUCCESS;
    }
	
	/**
	 * Performs the "saveFormGroup" action. Saves changes or adds a form group to the system.
	 * 
	 * @return	The struts status flag, which is either success or exception.
	 * @throws CtdbException	When a database error occurs while setting up the JSP when a
     * action error is detected.
	 * @throws JSONException When there is an error converting the select form ID array to JSON.
	 */
    public String saveFormGroup() throws CtdbException, JSONException {
        String strutsResult = BaseAction.SUCCESS;
    	Protocol p = (Protocol) session.get(CtdbConstants.CURRENT_PROTOCOL_SESSION_KEY);
    	
    	// Trim any leading or trailing white space characters from the entered data
    	name = name.trim();
    	description = description.trim();
    	
        try {
        	FormManager fm = new FormManager();
        	
        	// If the user's entered data is valid, then continue.
        	if ( isFormGroupValid() ) {
        		// Verify that the new form group is not a duplicate.
            	List<FormGroup> formGroups = fm.getFormGroups(p.getId());
            	for (FormGroup fg : formGroups) {
            		if ( fg.getId() != id ) {
            			if (fg.getName().equals(name)) {
            				addActionError(getText(StrutsConstants.ERROR_DUPLICATE, new String[]{name}));
            		        this.setupPage();
            				return StrutsConstants.EXCEPTION;
            			}
            		}
            	}
        		
	        	// Construct a FormGroup object out of the user's entered data.
	        	FormGroup fg = new FormGroup();
	            fg.setName(name);
	            fg.setDescription(description);
	            fg.setProtocolId(p.getId());
	            
	            // Construct the form ID array out of the supplied JSON array.
	            JSONArray jArray = new JSONArray(selectedFormJson);
	            int[] formIds = new int[jArray.length()];
	            
	            for ( int i = 0; i < formIds.length; i++ ) {
	            	formIds[i] = jArray.getInt(i);
	            }
	                
	            if ( id > 0 ) {
	            	fg.setId(id);
	                fm.updateFormGroup(fg, formIds);
	                addActionMessage(getText(StrutsConstants.SUCCESS_EDIT_KEY, 
	                		new String[]{name + " " + getText("form.formgroup.singlar.title.lcase")}));
	            }
	            else {
	            	fm.createFormGroup(fg, formIds);
	                addActionMessage(getText(StrutsConstants.SUCCESS_ADD_KEY, 
	                		new String[]{name + " " + getText("form.formgroup.singlar.title.lcase")}));
	            }
	            
	            session.put(FormGroupHomeAction.ACTION_MESSAGES_KEY, getActionMessages());
        	}
        }
        catch ( CtdbException ce ) {
        	logger.error("Database error occurred while saving the" + name + " form group", ce);
            addActionError(getText(StrutsConstants.ERROR_DATABASE_SAVE, 
            		new String[]{name + " " + getText("form.formgroup.singlar.title.lcase")}));
        }
        catch ( JSONException je ) {
        	logger.error("Error occured while converting the JSON array to an integer array.", je);
        	addActionError(getText("errors.formIds.invalid"));
        }
        
        // Check for any action errors
        if ( hasActionErrors() ) {
        	this.setupPage();
        	strutsResult = StrutsConstants.EXCEPTION;
        	showAddEdit = true;
        }
        
    	return strutsResult;
    }
    
    /**
     * Performs the "deleteFormGroup" action. Form groups corresponding to the 
     * given list of IDs will be removed from the system.
     * 
     * @return	The struts status flag, which is either success or exception.
     * @throws CtdbException	When a database error occurs while setting up the JSP when a
     * action error is detected.
     */
	public String deleteFormGroup() throws CtdbException {
        String strutsResult = BaseAction.SUCCESS;
        Locale userLocale = request.getLocale();
		String[] formGroupIdsArr = request.getParameterValues("id[]");
		List<String> fGroupNames = new ArrayList<String>();
		
		// Verify that there are any IDs to delete before continuing.
		if ( (formGroupIdsArr != null) && (formGroupIdsArr.length != 0) ) {
	        try {
	        	FormManager fm = new FormManager();
	        	
	        	// Delete the form groups identified by the IDs in the array.
	        	for ( int i = 0; i < formGroupIdsArr.length; i++ ) {
	        		int id = Integer.parseInt(formGroupIdsArr[i]);
	        		fGroupNames.add(fm.getFormGroup(id).getName());
	        		fm.deleteFormGroup(id);
	        	}
	        	
	    		// Create the deletion success message.
	        	if ( fGroupNames.size() > 1 ) {
	        		String groupNames = Utils.convertListToString(fGroupNames);
	        		addActionMessage(getText(StrutsConstants.SUCCESS_DELETE_MULTI_KEY, 
	        				new String[]{groupNames + " " + getText("form.formgroup.title").toLowerCase(userLocale)}));
	        	}
	        	else {
	        		addActionMessage(getText(StrutsConstants.SUCCESS_DELETE_KEY, 
	        				new String[]{fGroupNames.get(0) + " " + getText("form.formgroup.singlar.title.lcase")}));
	        	}
	        	
	        	session.put(FormGroupHomeAction.ACTION_MESSAGES_KEY, getActionMessages());
	        }
	        catch ( NumberFormatException nfe ) {
	        	logger.error("One of the given IDs is not a number.", nfe);
	        	strutsResult = BaseAction.ERROR;
	        }
	        catch (CtdbException ce) {
	        	logger.error("Database error occurred while deleting form group(s).", ce);
	        	strutsResult = BaseAction.ERROR;
	        }
		}
        
    	return strutsResult;
    }
	
	/**
	 * Validates the entered data for the new form group. Assumes that the entered string
	 * data has already been trimmed.
	 * 
	 * @return	True if and only if all validation tests passed successfully. Action
	 * errors will be created for each failed validation test.
	 */
	private boolean isFormGroupValid() {
		boolean isValid = true;
		
		// Validate the form group name
		if ( !Utils.isBlank(name) ) {
			if ( name.length() > 255 ) {
				addActionError(getText(StrutsConstants.ERROR_MAX_LENGTH, 
						new String[]{getText("form.formgroup.name.display"), "255"}));
				isValid = false;
			}
		}
		else {
			addActionError(getText(StrutsConstants.ERROR_FIELD_REQUIRED, 
					new String[]{getText("form.formgroup.name.display"), getText("errors.singular")}));
			isValid = false;
		}
		
		// Validate the form group description
		if ( description.length() > 255 ) {
			addActionError(getText(StrutsConstants.ERROR_MAX_LENGTH, 
					new String[]{getText("app.label.lcase.description"), "255"}));
			isValid = false;
		}
		
		return isValid;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * @return the showAddEdit
	 */
	public boolean isShowAddEdit() {
		return showAddEdit;
	}

	/**
	 * @param showAddEdit the showAddEdit to set
	 */
	public void setShowAddEdit(boolean showAddEdit) {
		this.showAddEdit = showAddEdit;
	}

	/**
	 * @return the selectedFormJson
	 */
	public String getSelectedFormJson() {
		return selectedFormJson;
	}

	/**
	 * @param selectedFormJson the selectedFormJson to set
	 */
	public void setSelectedFormJson(String selectedFormJson) {
		this.selectedFormJson = selectedFormJson;
	}
}
