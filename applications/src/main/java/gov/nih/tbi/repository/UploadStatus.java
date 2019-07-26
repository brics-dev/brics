
package gov.nih.tbi.repository;

public enum UploadStatus
{
    INITIALIZING("Initializing"), QUEUED("Queued"), UPLOADING("Uploading"), COMPLETED("Completed"), 
    	CANCELLED("Cancelled"), RETRY("Retrying");

    String name;

    UploadStatus(String name)
    {

        this.name = name;
    }

    public String getName()
    {

        return name;
    }
}
