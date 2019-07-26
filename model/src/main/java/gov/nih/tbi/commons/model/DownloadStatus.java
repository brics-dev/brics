
package gov.nih.tbi.commons.model;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * The enumeration for download statuses used by the download tool and download queue.
 * 
 * @author Francis Chen
 */
@XmlRootElement(name = "DownloadStatus")
public enum DownloadStatus
{

	PENDING(0, "PENDING"), READY(1, "READY"), IN_PROGRESS(2, "IN_PROGRESS"), ERROR(3, "ERROR"),
	COMPLETED(4, "COMPLETED"), STOPPED(5, "STOPPED"), ARCHIVED(6, "ARCHIVED"), CANCELLED(7, "CANCELLED");

    private int id;
    private String name;

    DownloadStatus(int id, String name)
    {

        this.id = id;
        this.name = name;
    }

    public void setStatus(DownloadStatus status)
    {

        this.id = status.getId();
        this.name = status.getName();
    }

    public void setID(int id)
    {

        this.id = id;
    }

    public int getId()
    {

        return id;
    }

    public String getName()
    {

        return name;
    }
}
