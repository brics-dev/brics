
package gov.nih.tbi.download.Model;

import gov.nih.tbi.commons.model.DownloadStatus;
import gov.nih.tbi.repository.model.hibernate.UserFile;

public class DownloadItem
{

    private int tableIndex;
    private String dest;
    private UserFile userFile;

    private DownloadStatus downloadStatus;
    private Long id;

	public DownloadItem(int index, String dest, UserFile userFile, DownloadStatus status, Long id)
    {
		this.downloadStatus = status;
        this.id =id;
        this.tableIndex = index;
        this.dest = dest;
        this.userFile = userFile;
    }

    public UserFile getUserFile()
    {

        return userFile;
    }

    public Long getUserFileSize()
    {

        return getUserFile().getSize();
    }

    public String getDest()
    {

        return dest;
    }


    public int getTableIndex()
    {

        return tableIndex;
    }

    public void setTableIndex(int tableIndex)
    {

        this.tableIndex = tableIndex;
    }

    public DownloadStatus getDownloadStatus()
    {

        return downloadStatus;
    }

    public void setDownloadStatus(DownloadStatus downloadStatus)
    {

        this.downloadStatus = downloadStatus;
    }


	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}
}
