
package gov.nih.tbi.dictionary.portal;

import gov.nih.tbi.CoreConstants;
import gov.nih.tbi.PortalConstants;
import gov.nih.tbi.commons.model.DataElementStatus;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;

import org.apache.log4j.Logger;

public class DataElementSummaryAction extends BaseDictionaryAction
{

    /**
	 * 
	 */
    private static final long serialVersionUID = 767142714116705164L;

    static Logger logger = Logger.getLogger(DataElementSummaryAction.class);

    /***************************************** VARIABLES ************************************************/
    private Long draftPercent;
    private Long publishPercent;
    private Long commonPercent;
    private Long total;
    private Long draftTotal;
    private Long publishTotal;
    private Long commonTotal;

    /***************************************** GETTERS/SETTERS ******************************************/

    /**
     * @return the draftPercent
     */
    public Long getDraftPercent()
    {

        return draftPercent;
    }

    /**
     * @param draftPercent
     *            the draftPercent to set
     */
    public void setDraftPercent(Long draftPercent)
    {

        this.draftPercent = draftPercent;
    }

    /**
     * @return the publishPercent
     */
    public Long getPublishPercent()
    {

        return publishPercent;
    }

    /**
     * @param publishPercent
     *            the publishPercent to set
     */
    public void setPublishPercent(Long publishPercent)
    {

        this.publishPercent = publishPercent;
    }

    /**
     * @return the commonPercent
     */
    public Long getCommonPercent()
    {

        return commonPercent;
    }

    /**
     * @param commonPercent
     *            the commonPercent to set
     */
    public void setCommonPercent(Long commonPercent)
    {

        this.commonPercent = commonPercent;
    }

    /**
     * @return the total
     */
    public Long getTotal()
    {

        return total;
    }

    /**
     * @param total
     *            the total to set
     */
    public void setTotal(Long total)
    {

        this.total = total;
    }

    /**
     * @return the draftTotal
     */
    public Long getDraftTotal()
    {

        return draftTotal;
    }

    /**
     * @param draftTotal
     *            the draftTotal to set
     */
    public void setDraftTotal(Long draftTotal)
    {

        this.draftTotal = draftTotal;
    }

    /**
     * @return the publishTotal
     */
    public Long getPublishTotal()
    {

        return publishTotal;
    }

    /**
     * @param publishTotal
     *            the publishTotal to set
     */
    public void setPublishTotal(Long publishTotal)
    {

        this.publishTotal = publishTotal;
    }

    /**
     * @return the commonTotal
     */
    public Long getCommonTotal()
    {

        return commonTotal;
    }

    /**
     * @param commonTotal
     *            the commonTotal to set
     */
    public void setCommonTotal(Long commonTotal)
    {

        this.commonTotal = commonTotal;
    }

    /***************************************** HELPERS **************************************************/

    private Long calculatePercent(Long num, Long total)
    {

        return Math.round(((double) num / (double) total) * 100);
    }

    /*****************************************
     * ACTIONS
     * 
     * @throws UnsupportedEncodingException
     * @throws MalformedURLException
     **************************************************/

    public String typeGraph() throws MalformedURLException, UnsupportedEncodingException
    {

        total = dictionaryManager.getNumDEWithStatusAndCategory(null, null);
        commonTotal = dictionaryManager.getNumDEWithCategory(staticManager
                .getCategoryByName(CoreConstants.COMMON_DATA_ELEMENT));
        publishTotal = dictionaryManager.getNumDEWithStatusAndCategory(DataElementStatus.PUBLISHED.getId(),
                staticManager.getCategoryByName(CoreConstants.UNIQUE_DATA_ELEMENT));
        draftTotal = total - commonTotal - publishTotal;

        commonPercent = calculatePercent(commonTotal, total);
        publishPercent = calculatePercent(publishTotal, total);
        draftPercent = 100 - commonPercent - publishPercent;

        return PortalConstants.ACTION_TYPE_GRAPH;
    }

}
