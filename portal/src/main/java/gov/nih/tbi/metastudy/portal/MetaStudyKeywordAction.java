
package gov.nih.tbi.metastudy.portal;

import gov.nih.tbi.PortalConstants;
import gov.nih.tbi.metastudy.model.hibernate.MetaStudyKeyword;
import gov.nih.tbi.metastudy.model.hibernate.MetaStudyLabel;

import java.io.UnsupportedEncodingException;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class MetaStudyKeywordAction extends BaseMetaStudyAction
{

    /**
	 * 
	 */
    private static final long serialVersionUID = -5567532008046754402L;

    /******************************************************************************************************/

    private Set<MetaStudyKeyword> availableKeywords;
    private Set<MetaStudyLabel> availableLabels;

    /******************************************************************************************************/

    /**
     * Retrieves the list of keywords available for this user to attach to the current data element
     * 
     * @return list of keywords that can be attached
     */
    public Set<MetaStudyKeyword> getAvailableKeywords()
    {

        String searchKey = getRequest().getParameter("keywordSearchKey");

        if (searchKey == null || searchKey.trim().equals(PortalConstants.EMPTY_STRING))
        {
            searchKey = "";
        }

        // Query for the keywords and add them to the list of available keywords
        Set<MetaStudyKeyword> availableKeywords = new LinkedHashSet<MetaStudyKeyword>();
        List<MetaStudyKeyword> searchedKeywords = metaStudyManager.searchKeywords(searchKey);
        
        if(searchedKeywords != null && !searchedKeywords.isEmpty()){
        	for (MetaStudyKeyword key : searchedKeywords)
        	{
        		availableKeywords.add(key);
        	}
        }
        
        // Add list of keywords added this session
        for (MetaStudyKeyword key : getSessionMetaStudy().getNewKeywords())
        {
            availableKeywords.add(key);
        }

        // Strip out any keywords that are already attached to the data element
        if (sessionMetaStudy != null && sessionMetaStudy.getMetaStudy() != null
                && sessionMetaStudy.getMetaStudy().getMetaStudyKeywords() != null)
        {
            Set<MetaStudyKeyword> keywordList = sessionMetaStudy.getMetaStudy().getMetaStudyKeywords();
            for (MetaStudyKeyword k : keywordList)
            {
                for (MetaStudyKeyword keyword : availableKeywords)
                {
                    if (keyword.getKeyword().equalsIgnoreCase(k.getKeyword()))
                    {
                        availableKeywords.remove(keyword);
                        break;
                    }
                }
            }
        }

        return availableKeywords;
    }

    /**
     * Retrieves the list of labels available for this user to attach to the current data element
     * 
     * @return list of labels that can be attached
     */
    public Set<MetaStudyLabel> getAvailableLabels()
    {

        String searchKey = getRequest().getParameter("labelSearchKey");

        if (searchKey == null || searchKey.trim().equals(PortalConstants.EMPTY_STRING))
        {
            searchKey = "";
        }

        // Query for the keywords and add them to the list of available keywords
        LinkedHashSet<MetaStudyLabel> availableLabels = new LinkedHashSet<MetaStudyLabel>();
        
        List<MetaStudyLabel> searchedLabls = metaStudyManager.searchLabels(searchKey);
        
        if(searchedLabls != null && !searchedLabls.isEmpty()){
        	for (MetaStudyLabel label : searchedLabls){
        		availableLabels.add(label);
        	}	
        }
        
        // Add list of keywords added this session
        for (MetaStudyLabel label : getSessionMetaStudy().getNewLabels())
        {
            availableLabels.add(label);
        }

        // Strip out any keywords that are already attached to the data element
        if (sessionMetaStudy != null && sessionMetaStudy.getMetaStudy() != null
                && sessionMetaStudy.getMetaStudy().getMetaStudyLabels() != null)
        {
            Set<MetaStudyLabel> labelList = sessionMetaStudy.getMetaStudy().getMetaStudyLabels();
            for (MetaStudyLabel l : labelList)
            {
                for (MetaStudyLabel label : availableLabels)
                {
                    if (label.getLabel().equalsIgnoreCase(l.getLabel()))
                    {
                        availableLabels.remove(label);
                        break;
                    }
                }
            }
        }

        return availableLabels;
    }

    /**
     * Retrieves the list of keywords that are attached to this data structure
     * 
     * @return
     * @throws UnsupportedEncodingException
     */
    public Set<MetaStudyKeyword> getCurrentKeywords() throws UnsupportedEncodingException
    {

        Set<MetaStudyKeyword> current = new LinkedHashSet<MetaStudyKeyword>();

        if (sessionMetaStudy != null && sessionMetaStudy.getMetaStudy() != null
                && sessionMetaStudy.getMetaStudy().getMetaStudyKeywords() != null)
        {
            // Add each keyword from the keywordElement list to current
            for (MetaStudyKeyword k : sessionMetaStudy.getMetaStudy().getMetaStudyKeywords())
            {
            	k.setCount(metaStudyManager.getMetaStudyKeywordCount(k.getKeyword()));
                current.add(k);
            }
        }
        return current;
    }

    /**
     * Retrieves the list of keywords that are attached to this data structure
     * 
     * @return
     * @throws UnsupportedEncodingException
     */
    public Set<MetaStudyLabel> getCurrentLabels() throws UnsupportedEncodingException
    {

        LinkedHashSet<MetaStudyLabel> current = new LinkedHashSet<MetaStudyLabel>();

        if (sessionMetaStudy != null && sessionMetaStudy.getMetaStudy() != null
                && sessionMetaStudy.getMetaStudy().getMetaStudyLabels() != null)
        {
            // Add each keyword from the keywordElement list to current
            for (MetaStudyLabel l : sessionMetaStudy.getMetaStudy().getMetaStudyLabels())
            {
            	l.setCount(metaStudyManager.getMetaStudyLabelCount(l.getLabel()));
                current.add(l);
            }
        }
        return current;
    }

    /******************************************************************************************************/

    public String searchKeywords()
    {

        String searchKey = getRequest().getParameter("keywordSearchKey");

        if (searchKey == null || searchKey.trim().equals(PortalConstants.EMPTY_STRING))
        {
            searchKey = "";
        }

        availableKeywords = new HashSet<MetaStudyKeyword>();
        // for (MetaStudyKeyword key : dictionaryManager.searchKeywords(searchKey))
        for (MetaStudyKeyword key : availableKeywords)
        {
            availableKeywords.add(key);
        }

        return PortalConstants.ACTION_SEARCH;
    }

    public String searchLabels()
    {

        String searchKey = getRequest().getParameter("labelSearchKey");

        if (searchKey == null || searchKey.trim().equals(PortalConstants.EMPTY_STRING))
        {
            searchKey = "";
        }

        availableLabels = new HashSet<MetaStudyLabel>();
        // for (MetaStudyLabel key : dictionaryManager.searchLabels(searchKey))
        for (MetaStudyLabel key : availableLabels)
        {
            availableLabels.add(key);
        }

        return PortalConstants.ACTION_SEARCH;
    }
    
    public String view(){
    	return "success";
    }

    public String current()
    {

        return PortalConstants.ACTION_CURRENT;
    }

}
