
package gov.nih.tbi.dictionary.model;

import gov.nih.tbi.CoreConstants;
import gov.nih.tbi.dictionary.model.hibernate.DataElement;
import gov.nih.tbi.dictionary.model.hibernate.Keyword;

import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;

/**
 * This class masks the static fields of Data Element form.
 * 
 * @author Francis Chen
 * 
 */
public class KeywordForm
{

    static Logger logger = Logger.getLogger(KeywordForm.class);

    /*************************************************************/

    protected Set<Keyword> keywords;
    protected Set<Keyword> labels;

    /*************************************************************************************/

    public KeywordForm()
    {

    }

    /**
     * Constructor fetches data for each column in dataElement object
     * 
     * @param dataElement
     */
    public KeywordForm(DataElement dataElement)
    {

        this.keywords = dataElement.getKeywords();
        this.labels = dataElement.getLabels();
    }

    /*************************************************************/

    public Set<Keyword> getKeywords()
    {

        return keywords;
    }

    public Set<Keyword> getLabels()
    {

        return labels;
    }

    // Picks up the comma delimited list of attached keywords form the select box
    // on the keywordInterface page
    public void setKeywordList(String keywordString)
    {

        // If keywords is equal to empty, then the list is empty
        // the function can simply return
        if (keywordString.equals("empty"))
        {
            return;
        }

        this.keywords = parseKeywordString(keywordString);

    }

    public void setLabelList(String labelString)
    {

        if (labelString.equals("empty"))
        {
            return;
        }

        this.labels = parseKeywordString(labelString);
    }

    private Set<Keyword> parseKeywordString(String keywordString)
    {

        String[] keywordArray = keywordString.split(CoreConstants.COMMA);

        Set<Keyword> returnSet = new HashSet<Keyword>();

        for (int i = 0; i < keywordArray.length; i++)
        {
            if (!keywordArray[i].trim().equals(""))
            {

                String[] keywordParts = keywordArray[i].trim().split(CoreConstants.UNDERSCORE, 3);

                Keyword keyword = new Keyword();
                keyword.setCount(Long.valueOf(keywordParts[1]));
                keyword.setKeyword(keywordParts[2]);

                returnSet.add(keyword);

            }
        }

        return returnSet;
    }

    public void copyToDataElement(DataElement dataElement)
    {

        dataElement.setKeywords(keywords);
        dataElement.setLabels(labels);
    }
}
