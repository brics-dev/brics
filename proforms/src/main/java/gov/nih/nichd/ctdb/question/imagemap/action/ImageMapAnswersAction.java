package gov.nih.nichd.ctdb.question.imagemap.action;

import gov.nih.nichd.ctdb.common.BaseAction;
import gov.nih.nichd.ctdb.common.StrutsConstants;
import gov.nih.nichd.ctdb.question.common.QuestionConstants;
import gov.nih.nichd.ctdb.question.domain.ImageMapOption;
import gov.nih.nichd.ctdb.question.domain.ImageMapQuestion;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
/**
 * Created by Booz Allen Hamilton
 * Date: Aug 31, 2004
 * 
 */
public class ImageMapAnswersAction extends BaseAction  {

	private static final long serialVersionUID = -8018982662503025968L;

	private String[] options = null;
	private String action = null;
	
	public String execute() throws Exception {

		try {	
			try {  
				request.setCharacterEncoding("UTF-8");  
			} catch (UnsupportedEncodingException ex) {}
    	   
    	   
			if (session.get(QuestionConstants.QUESTION_IN_PROGRESS) == null) {
				return StrutsConstants.FAILURE;
			}
					
            if (action == null) {
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
            
            ImageMapQuestion question = (ImageMapQuestion)session.get(QuestionConstants.QUESTION_IN_PROGRESS);
            request.setAttribute("theImageMap", this.getImageMap(question));
            
            if (action.equalsIgnoreCase(StrutsConstants.ACTION_ADD_FORM)) {
            	request.setAttribute("enteredOptions", new ArrayList());
            	
            } else if(action.equalsIgnoreCase(StrutsConstants.ACTION_EDIT_FORM)) {
            	int oldGridResolution=(Integer) session.get("oldGridResolution");
            	if (question.getGridResolution() == oldGridResolution) {
                    request.setAttribute("enteredOptions", getOptions(question.getOptions()));
            	} else {
            		request.setAttribute("enteredOptions", new ArrayList());
            	}
            }
		} catch (Exception e) {
			return StrutsConstants.FAILURE;
		}
		
		return SUCCESS;
	}
	
	
	public String saveImageMapAnswers() {

		List<ImageMapOption> mapOptions = new ArrayList<ImageMapOption>();
		String[] options = getOptions();
		for (int i = 0; i < options.length; i++) {
			ImageMapOption imo = new ImageMapOption();
			imo.buildFromWebString(options[i]);
			mapOptions.add(imo);
		}
		
		if (duplicateOptionExists(mapOptions)) {
			addActionError("Please ensure that the options entered are unique.");
			request.setAttribute("hasErrors", true);
			request.setAttribute("enteredOptions", getOptions(mapOptions));
			return StrutsConstants.EXCEPTION;
		} else {
			session.put("mapOptions", mapOptions);
			
            ImageMapQuestion question = (ImageMapQuestion)session.get(QuestionConstants.QUESTION_IN_PROGRESS);
			question.setOptions(mapOptions);
            request.setAttribute("theImageMap", this.getImageMap(question));

			if (request.getParameter("finish").equals("true")) {
				session.put(QuestionConstants.FINISH, true);
				return QuestionConstants.ACTION_FINISH;
			} else {
				return StrutsConstants.SUCCESS;
			}
		}
	}

    private String[] getOptions(List<ImageMapOption> options) {
        String[] result = new String[options.size()];
        int i = 0;
        for (ImageMapOption option : options) {
            result[i] = option.getWebString();
            i++;
        }
        return result;
    }

    private String getImageMap(ImageMapQuestion q) {
        int resolution = getGridResolution(q.getGridResolution());
        double cellWidth = q.getWidthInt()  / resolution;
        double cellHeight = q.getHeightInt() / resolution;
        request.setAttribute("cellWidth", Double.toString(cellWidth));
        request.setAttribute("cellHeight", Double.toString(cellHeight));
        request.setAttribute("resolution", Integer.toString(resolution));

        String map = "<map name='theMap' id='theMap'>";

        for (int i = 0; i < resolution; i ++) {  // row
            for (int j = 0; j < resolution; j++) {  // column
                int width1 = (int)(i*cellWidth);
                int width2 = (int)((i+1)*cellWidth);
                int height1 = (int) (j*cellHeight);
                int height2 = (int) ((j+1)*cellHeight);

                map += "<area SHAPE=RECT COORDS='"+ width1 + "," + height1 + " " + width2 + "," + height2 + "' ";
                map += "href='Javascript: clickMap ("+ (j+1) +", "+ (i+1) +");'>";
            }
        }
        map += "</map>";
        return map;
    }

    private int getGridResolution (int resolution) {
        int result = 20;
        switch (resolution) {
            case 1 : result = 5; break;
            case 2 : result = 8; break;
            case 3 : result = 10; break;
            case 4 : result = 15; break;
        }
        return result;
    }

    private boolean duplicateOptionExists(List<ImageMapOption> imOptions) {
        if (imOptions.size() < 2) {
            return false;
        }

        List<String> optionsOnly = new ArrayList<String>();
        for (ImageMapOption imOption : imOptions) {
            optionsOnly.add(imOption.getOption());
        }

        for (ImageMapOption imOption : imOptions) {
            String curOption = imOption.getOption();
            int count = 0;
            for (String option : optionsOnly) {
                if (curOption.equals(option)) {
                    count++;
                    if (count > 1) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    
	public String[] getOptions() {
		return options;
	}

	public void setOptions(String[] options) {
		this.options = options;
	}

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

}
