
package gov.nih.tbi.ordermanager.model;

/**
 * 
 * @author vpacha
 * 
 */
public enum FundingStatus
{
    CURRENTLY_FUNDED(0L, "CURRENTLY FUNDED"), UNDER_REVIEW(1L, "UNDER REVIEW");

    private Long id;
    private String value;

    private FundingStatus(Long id, String value)
    {

        this.id = id;
        this.value = value;
    }

    public Long getId()
    {

        return this.id;
    }

    public String getValue()
    {

        return this.value;
    }

}
