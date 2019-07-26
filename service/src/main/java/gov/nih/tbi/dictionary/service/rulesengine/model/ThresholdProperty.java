
package gov.nih.tbi.dictionary.service.rulesengine.model;

import gov.nih.tbi.commons.model.RepeatableType;

/**
 * 
 * @author mgree1
 * 
 */
public class ThresholdProperty
{

    Integer threshold;
    RepeatableType repeatableType;

    public ThresholdProperty()
    {

        this.repeatableType = null;
        this.threshold = null;
    }

    public ThresholdProperty(RepeatableType repeatableType, int threshold)
    {

        this.repeatableType = repeatableType;
        this.threshold = threshold;
    }

    /**
     * @return the threshold
     */
    public Integer getThreshold()
    {

        return threshold;
    }

    /**
     * @param threshold
     *            the threshold to set
     */
    public void setThreshold(Integer threshold)
    {

        this.threshold = threshold;
    }

    /**
     * @return the repeatableType
     */
    public RepeatableType getRepeatableType()
    {

        return repeatableType;
    }

    /**
     * @param repeatableType
     *            the repeatableType to set
     */
    public void setRepeatableType(RepeatableType repeatableType)
    {

        this.repeatableType = repeatableType;
    }
}
