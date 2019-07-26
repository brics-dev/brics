package gov.nih.nichd.ctdb.question.imagemap.action;

import gov.nih.nichd.ctdb.common.BaseAction;
import gov.nih.nichd.ctdb.common.StrutsConstants;
import gov.nih.nichd.ctdb.question.common.QuestionConstants;
import gov.nih.nichd.ctdb.question.domain.ImageMapQuestion;
import gov.nih.nichd.ctdb.util.domain.LookupType;
import gov.nih.nichd.ctdb.util.manager.LookupManager;

public class DefineMapAction extends BaseAction {

    private int gridResolution;
    private String height;
    private String width;
    private boolean showGrid;
    private String gridFileName;
    private String action;

    public static final String ACTION_MESSAGES_KEY = "DefineMapAction_ActionMessages";

    public String execute() throws Exception {
        try {
            if (session.get(QuestionConstants.QUESTION_IN_PROGRESS) == null) {
                return StrutsConstants.FAILURE;
            }
            
            ImageMapQuestion question = (ImageMapQuestion)session.get(QuestionConstants.QUESTION_IN_PROGRESS);
            
            if (getAction() == null) {
                // comming from redirect
                if (session.get(QuestionConstants.FINISH) != null) {
                	session.remove(QuestionConstants.FINISH);
                    setAction(StrutsConstants.ACTION_PROCESS_EDIT);
                } else if (session.get(QuestionConstants.ORIGINAL_QUESTION_OBJ) == null) {
                    setAction(StrutsConstants.ACTION_ADD_FORM);
                } else {
                    setAction(StrutsConstants.ACTION_EDIT_FORM);
                }
            }
            
            LookupManager lm = new LookupManager();
            request.setAttribute("resolutions", lm.getLookups(LookupType.RESOLUTIONS));
            
            if (action.equalsIgnoreCase(StrutsConstants.ACTION_ADD_FORM)) {
                setAction(StrutsConstants.ACTION_PROCESS_ADD);
                
            } else if (action.equalsIgnoreCase(StrutsConstants.ACTION_EDIT_FORM)) {
                setGridResolution(question.getGridResolution());
                setHeight(question.getHeight());
                setWidth(question.getWidth());
                setShowGrid(question.isShowGrid());
                setAction(StrutsConstants.ACTION_PROCESS_EDIT);
                
            } else if (action.equalsIgnoreCase(StrutsConstants.ACTION_PROCESS_ADD)) {
                saveForm(question);
                session.put(QuestionConstants.QUESTION_IN_PROGRESS, question);
                
            } else if (action.equalsIgnoreCase(StrutsConstants.ACTION_PROCESS_EDIT)) {	
            	session.put("oldGridResolution", question.getGridResolution());
                saveForm(question);
                session.put(QuestionConstants.QUESTION_IN_PROGRESS, question);
            } 
        } catch (Exception e) {
            return StrutsConstants.FAILURE;
        }

        return SUCCESS;
    }

    private void saveForm(ImageMapQuestion question) {
        question.setHeight(getHeight());
        question.setWidth(getWidth());
        question.setGridResolution(getGridResolution());
        question.setShowGrid(isShowGrid());
        question.setGridFileName(getGridFileName());
    }

	public int getGridResolution() {
		return gridResolution;
	}

	public void setGridResolution(int gridResolution) {
		this.gridResolution = gridResolution;
	}

	public String getHeight() {
		return height;
	}

	public void setHeight(String height) {
		this.height = height;
	}

	public String getWidth() {
		return width;
	}

	public void setWidth(String width) {
		this.width = width;
	}

	public boolean isShowGrid() {
		return showGrid;
	}

	public void setShowGrid(boolean showGrid) {
		this.showGrid = showGrid;
	}

	public String getGridFileName() {
		return gridFileName;
	}

	public void setGridFileName(String gridFileName) {
		this.gridFileName = gridFileName;
	}

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}
}
