
package gov.nih.tbi.metastudy.model;

import gov.nih.tbi.CoreConstants;
import gov.nih.tbi.metastudy.model.hibernate.MetaStudy;
import gov.nih.tbi.metastudy.model.hibernate.MetaStudyKeyword;
import gov.nih.tbi.metastudy.model.hibernate.MetaStudyLabel;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

/**
 * This class masks the static fields of Meta Study form.
 * 
 */
public class MetaStudyKeywordForm
{

    static Logger logger = Logger.getLogger(MetaStudyKeywordForm.class);

    /*************************************************************/

    protected Set<MetaStudyKeyword> keywords = new HashSet<MetaStudyKeyword>();
    protected Set<MetaStudyLabel> labels = new HashSet<MetaStudyLabel>();

    /*************************************************************************************/

    public MetaStudyKeywordForm()
    {

    }

    /**
     * Constructor fetches data for each column in metaStudy object
     * 
     * @param metaStudy
     */
    public MetaStudyKeywordForm(MetaStudy metaStudy)
    {

        this.keywords = metaStudy.getMetaStudyKeywords();
        this.labels = metaStudy.getMetaStudyLabels();
    }

    /*************************************************************/

    public Set<MetaStudyKeyword> getKeywordsList()
    {

        return keywords;
    }

    public Set<MetaStudyLabel> getLabelsList()
    {

        return labels;
    }

    // Picks up the comma delimited list of attached keywords form the select box
    // on the keywordInterface page
    public void setKeywordsList(String keywordString)
    {

        // If keywords is equal to empty, then the list is empty
        // the function can simply return
        if (keywordString.equals("empty"))
        {
            return;
        }

        for(MetaStudyKeyword keyword: parseKeywordString(keywordString)){
        	keywords.add(keyword);
        }

        return;

    }

    public void setLabelsList(String labelString)
    {

        if (labelString.equals("empty"))
        {
            return;
        }

        for(MetaStudyLabel label : parseLabelString(labelString)){
        	labels.add(label);
        }
        return;
    }

    private Set<MetaStudyKeyword> parseKeywordString(String keywordString)
    {

        String[] keywordArray = keywordString.split(CoreConstants.COMMA);

        Set<MetaStudyKeyword> returnSet = new HashSet<MetaStudyKeyword>();
        

        for (int i = 0; i < keywordArray.length; i++)
        {
            if (!keywordArray[i].trim().equals(""))
            {

                String[] keywordParts = keywordArray[i].trim().split(CoreConstants.UNDERSCORE, 2);

                MetaStudyKeyword metaStudyKeyword = new MetaStudyKeyword();
                metaStudyKeyword.setKeyword(keywordParts[1]);
                metaStudyKeyword.setCount(Long.parseLong(keywordParts[0]));

                returnSet.add(metaStudyKeyword);

            }
        }

        return returnSet;
    }
    
    private Set<MetaStudyLabel> parseLabelString(String labelString)
    {

        String[] labelArray = labelString.split(CoreConstants.COMMA);

        Set<MetaStudyLabel> returnSet = new HashSet<MetaStudyLabel>();

        for (int i = 0; i < labelArray.length; i++)
        {
            if (!labelArray[i].trim().equals(""))
            {

                String[] labelParts = labelArray[i].trim().split(CoreConstants.UNDERSCORE, 2);

                MetaStudyLabel metaStudyLabel = new MetaStudyLabel();
                metaStudyLabel.setLabel(labelParts[1]);
                metaStudyLabel.setId(null);
                metaStudyLabel.setCount(Long.parseLong((labelParts[0])));

                returnSet.add(metaStudyLabel);

            }
        }

        return returnSet;
    }

    public void copyToMetaStudy(MetaStudy metaStudy)
    {
    	//get a list of current keywords/label to compare to session collections
    	Set<MetaStudyKeyword> savedKeywords = metaStudy.getMetaStudyKeywords();
    	Set<MetaStudyLabel> savedLabels = metaStudy.getMetaStudyLabels();
    	
    	// if the meta study doesn't have any keywords they are all new
    	// if the meta study has a new list compare the two to add new ones and remove non associated ones
    	if(savedKeywords != null){
    		metaStudy.setMetaStudyKeywords(copyNewKeywordList(savedKeywords, keywords));
    	} else {
    		metaStudy.setMetaStudyKeywords(keywords);
    	}
    	
    	// if the meta study doesn't have any labels they are all new
    	// if the meta study has a new list compare the two to add new ones and remove non associated ones
    	if(savedLabels != null){
    		metaStudy.setMetaStudyLabels(copyNewLabelList(savedLabels, labels));
    	} else {
    		metaStudy.setMetaStudyLabels(labels);
    	}
    }
    
    //loops through a list of keywords to see what has been added and what has been removed
    private Set<MetaStudyKeyword> copyNewKeywordList(Set<MetaStudyKeyword> savedKeywords, Set<MetaStudyKeyword> sessionKeywords){
    	
    	Set<MetaStudyKeyword> returnSet = new HashSet<MetaStudyKeyword>();
    	
    	//if the session keyword exists in the saved keyword list, use the saved keyword
    	//if the session keyword does not exist in the saved keyword list, it is new and we should add it
    	//this will also satisfy the case that a keyword is removed, it will not be included in the saved list
    	for(MetaStudyKeyword sessionKeyword : sessionKeywords){
    		boolean isNew = true;
    		for(MetaStudyKeyword savedKeyword : savedKeywords){
    			if(savedKeyword.getKeyword().equalsIgnoreCase(sessionKeyword.getKeyword())){
    				isNew= false;
    				returnSet.add(savedKeyword);
    				break;
    			}
    		}
    		if(isNew){
    			returnSet.add(sessionKeyword);
    		}
    	}
    	return returnSet;
    }
    
    //loops through a list of labels to see what has been added or removed
    private Set<MetaStudyLabel> copyNewLabelList(Set<MetaStudyLabel> savedLabels, Set<MetaStudyLabel> sessionlabels){
    	
    	Set<MetaStudyLabel> returnSet = new HashSet<MetaStudyLabel>();
    	
    	//if the session label exists in the saved label list, use the saved label
    	//if the session label does not exist in the saved label list, it is new and we should add it
    	//this will also satisfy the case that a label is removed, it will not be included in the saved list
    	for(MetaStudyLabel sessionLabel : sessionlabels){
    		boolean isNew = true;
    		for(MetaStudyLabel savedLabel : savedLabels){
    			if(savedLabel.getLabel().equalsIgnoreCase(sessionLabel.getLabel())){
    				isNew= false;
    				returnSet.add(savedLabel);
    				break;
    			}
    		}
    		if(isNew){
    			returnSet.add(sessionLabel);
    		}
    	}
    	return returnSet;
    }
}

