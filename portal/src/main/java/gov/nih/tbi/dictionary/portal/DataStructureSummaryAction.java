
package gov.nih.tbi.dictionary.portal;

import gov.nih.tbi.PortalConstants;
import gov.nih.tbi.commons.model.StatusType;

import org.apache.log4j.Logger;

public class DataStructureSummaryAction extends BaseDictionaryAction
{

    /**
	 * 
	 */
    private static final long serialVersionUID = -7242918619656452944L;

    static Logger logger = Logger.getLogger(DataStructureSummaryAction.class);

    /***************************************** VARIABLES ************************************************/
    private Long draftPercent;
    private Long archivePercent;
    private Long publishPercent;
    private Long total;
    private Long draftTotal;
    private Long archiveTotal;
    private Long publishTotal;

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
     * @return the archivePercent
     */
    public Long getArchivePercent()
    {

        return archivePercent;
    }

    /**
     * @param archivePercent
     *            the archivePercent to set
     */
    public void setArchivePercent(Long archivePercent)
    {

        this.archivePercent = archivePercent;
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
     * @return the archiveTotal
     */
    public Long getArchiveTotal()
    {

        return archiveTotal;
    }

    /**
     * @param archiveTotal
     *            the archiveTotal to set
     */
    public void setArchiveTotal(Long archiveTotal)
    {

        this.archiveTotal = archiveTotal;
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

    /***************************************** HELPERS **************************************************/

    private Long calculatePercent(Long num, Long total)
    {

        return Math.round(((double) num / (double) total) * 100);
    }

    /***************************************** ACTIONS **************************************************/

    /**
     * Called in an ajax call. Gathers the data required to draw a graph of types of data structures.
     * 
     * @return
     */
    public String typeGraph()
    {

        // Get the totals
        publishTotal = dictionaryManager.getNumDSWithStatus(StatusType.PUBLISHED.getId());
        archiveTotal = dictionaryManager.getNumDSWithStatus(StatusType.ARCHIVED.getId());
        total = dictionaryManager.getNumDSWithStatus(null);
        draftTotal = total - publishTotal - archiveTotal;

        // Calculate the percentages
        publishPercent = calculatePercent(publishTotal, total);
        archivePercent = calculatePercent(archiveTotal, total);
        draftPercent = 100 - archivePercent - publishPercent;

        return PortalConstants.ACTION_TYPE_GRAPH;
    }

}
