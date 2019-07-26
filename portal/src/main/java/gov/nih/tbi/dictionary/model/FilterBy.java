
package gov.nih.tbi.dictionary.model;

import gov.nih.tbi.commons.model.StatusType;

public enum FilterBy
{

    ALL("All", null), UNATTACHED("Unattached", null), DRAFT("From " + StatusType.DRAFT.getType() + " Form",
            StatusType.DRAFT), AWAITING_PUBLICATION("From " + StatusType.AWAITING_PUBLICATION.getType() + " Form",
            StatusType.AWAITING_PUBLICATION), PUBLISHED("From " + StatusType.PUBLISHED.getType() + " Form",
            StatusType.PUBLISHED), ARCHIVED("From " + StatusType.ARCHIVED.getType() + " Form", StatusType.ARCHIVED);

    /******************************************************************************************************/

    private String value;
    private StatusType status;

    /******************************************************************************************************/

    private FilterBy(String value, StatusType status)
    {

        this.value = value;
        this.status = status;
    }

    public StatusType getStatus()
    {

        return status;
    }

    public String toString()
    {

        return value;
    }

}
