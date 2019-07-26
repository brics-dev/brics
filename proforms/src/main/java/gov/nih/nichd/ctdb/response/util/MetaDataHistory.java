package gov.nih.nichd.ctdb.response.util;

import java.util.List;
import java.util.Date;
/**
 * MetaDataHistory represents an object from ctdb table administeredformedit
 * and will be used in administered form edit log jsp page.
 *
 * @author Booz Allen Hamilton
 * @version 1.0
 */
public class MetaDataHistory
{
    private String colname;
    private String colvaluebefore;
    private String colvalueafter;
    private String editreason;
    private Date editdate;
    private String editby;
    private String status;

    /**
     * Gets the column name.
     *
     * @return The current colname.
     */
    public String getColname()
    {
        return colname;
    }

    /**
     * Sets the column name value.
     *
     * @param colname The colname to be set for.
     */
    public void setColname(String colname)
    {
        this.colname = colname;
    }

    /**
     * Gets the column value after edit..
     *
     * @return The current colvalueafter
     */
    public String getColvalueafter()
    {
        return colvalueafter;
    }

    /**
     * Sets the current column value after edit.
     *
     * @param colvalueafter The current colvalueafter to be set for.
     */
    public void setColvalueafter(String colvalueafter)
    {
        this.colvalueafter = colvalueafter;
    }

    /**
     * Gets the column value before edit.
     *
     * @return The current colvaluebefore
     */
    public String getColvaluebefore()
    {
        return colvaluebefore;
    }

    /**
     * Sets the current column value before edit.
     *
     * @param colvaluebefore The current colvaluebefore to be set for.
     */
    public void setColvaluebefore(String colvaluebefore)
    {
        this.colvaluebefore = colvaluebefore;
    }

    /**
     * Gets the current editreason
     *
     * @return The current editreason
     */
    public String getEditreason()
    {
        return editreason;
    }

    /**
     * Sets the current editreason for change.
     *
     * @param editreason The current editreason.
     */
    public void setEditreason(String editreason)
    {
        this.editreason = editreason;
    }

    /**
     * Gets the Date that this edit was made.
     *
     * @return The Date for this edit.
     */
    public Date getEditdate()
    {
        return editdate;
    }

    /**
     * Sets the current edit Date.
     *
     * @param editdate The current edit date.
     */
    public void setEditdate(Date editdate)
    {
        this.editdate = editdate;
    }



    /**
     * Gets the current name of the user who made the edit.
     *
     * @return The current user name who made the edit.
     */
    public String getEditby()
    {
        return editby;
    }

    /**
     * Sets the current editor's name.
     *
     * @param editby The current editor's name.
     */
    public void setEditby(String editby)
    {
        this.editby = editby;
    }

    /**
     * Gets the status for the edit
     *
     * @return The status for the edit
     */
    public String getStatus()
    {
        return status;
    }

    /**
     * Sets the status for edit.
     *
     * @param status The status.
     */
    public void setStatus(String status)
    {
        this.status = status;
    }
}