
package gov.nih.tbi.account.model;

/**
 * Interface used to abstract permission groups and accounts into the same table
 */
public interface PermissionAuthority
{

    public Long getId();

    public String getDisplayName();

    public String getDisplayKey();

    /**
     * This value will be null if it is not set manually. But for the sessionAccount it should always be set (set in
     * AccountDetailService).
     * 
     * @return
     */
    public String getDiseaseKey();

    public void setDiseaseKey(String diseaseKey);
}
