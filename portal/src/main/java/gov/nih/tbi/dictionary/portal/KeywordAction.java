
package gov.nih.tbi.dictionary.portal;

import gov.nih.tbi.PortalConstants;
import gov.nih.tbi.dictionary.model.hibernate.Keyword;

import java.util.HashSet;
import java.util.Set;

public class KeywordAction extends BaseDictionaryAction
{

    /**
	 * 
	 */
    private static final long serialVersionUID = -5567532008046754402L;

    /******************************************************************************************************/

    private Set<Keyword> availableKeywords;
    private Set<Keyword> availableLabels;

    /******************************************************************************************************/

    /**
     * Returns the list of availableKeywords that are not already part of the data element by stripping the ones already
     * added from the availableKeywords List
     * 
     * @return
     */
    public Set<Keyword> getAvailableKeywords()
    {

        // If the availableKeywords list has not been built, then
        if (availableKeywords == null)
        {
            searchKeywords();
        }

        // This loop removes all the elements that are already attached to the data element
        if (sessionDataElement != null && sessionDataElement.getDataElement() != null
                && sessionDataElement.getDataElement().getKeywords() != null)
        {
            Set<Keyword> keywordList = sessionDataElement.getDataElement().getKeywords();
            for (Keyword k : keywordList)
            {
                for (Keyword keyword : availableKeywords)
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
     * Returns the list of availableLabels that are not already part of the data element by stripping the ones already
     * added from the availableKeywords List
     * 
     * @return
     */
    public Set<Keyword> getAvailableLabels()
    {

        // If the availableKeywords list has not been built, then
        if (availableLabels == null)
        {
            searchLabels();
        }

        // This loop removes all the elements that are already attached to the data element
        if (sessionDataElement != null && sessionDataElement.getDataElement() != null
                && sessionDataElement.getDataElement().getLabels() != null)
        {
            Set<Keyword> labelList = sessionDataElement.getDataElement().getLabels();
            for (Keyword l : labelList)
            {
                for (Keyword label : availableLabels)
                {
                    if (label.getKeyword().equalsIgnoreCase(l.getKeyword()))
                    {
                        availableLabels.remove(label);
                        break;
                    }
                }
            }
        }

        return availableLabels;
    }

    public Set<Keyword> getCurrentKeywords()
    {

        if (sessionDataElement != null && sessionDataElement.getDataElement() != null
                && sessionDataElement.getDataElement().getKeywords() != null)
        {

            return sessionDataElement.getDataElement().getKeywords();
        }

        return new HashSet<Keyword>();
    }

    public Set<Keyword> getCurrentLabels()
    {

        if (sessionDataElement != null && sessionDataElement.getDataElement() != null
                && sessionDataElement.getDataElement().getLabels() != null)
        {

            return sessionDataElement.getDataElement().getLabels();
        }

        return new HashSet<Keyword>();
    }

    /******************************************************************************************************/

    public String searchKeywords()
    {

        String searchKey = getRequest().getParameter("keywordSearchKey");

        if (searchKey == null || searchKey.trim().equals(PortalConstants.EMPTY_STRING))
        {
            searchKey = "";
        }

        availableKeywords = new HashSet<Keyword>();
        for (Keyword key : dictionaryManager.searchKeywords(searchKey))
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

        availableLabels = new HashSet<Keyword>();
        for (Keyword key : dictionaryManager.searchLabels(searchKey))
        {
            availableLabels.add(key);
        }

        return PortalConstants.ACTION_SEARCH;
    }

    public String current()
    {

        return PortalConstants.ACTION_CURRENT;
    }

}
