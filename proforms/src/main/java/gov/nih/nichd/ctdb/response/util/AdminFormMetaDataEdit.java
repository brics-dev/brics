package gov.nih.nichd.ctdb.response.util;

/**
 * Created by Booz Allen Hamilton
 * Date: Nov 24, 2004
 * 
 */
public class AdminFormMetaDataEdit {

    private String column;
    private String beforeValue;
    private String afterValue;
    private String reason;
    private int userId;
    private int adminFormId;

    public AdminFormMetaDataEdit (String column, String beforeValue, String afterValue, String reason, int userId, int adminFormId) {
        this.column = column;
        this.beforeValue = beforeValue;
        this.afterValue = afterValue;
        this.reason = reason;
        this.userId  = userId;
        this.adminFormId = adminFormId;
    }

    public String getColumn() {
        return column;
    }

    public void setColumn(String column) {
        this.column = column;
    }

    public String getBeforeValue() {
        return beforeValue;
    }

    public void setBeforeValue(String beforeValue) {
        this.beforeValue = beforeValue;
    }

    public String getAfterValue() {
        return afterValue;
    }

    public void setAfterValue(String afterValue) {
        this.afterValue = afterValue;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getAdminFormId() {
        return adminFormId;
    }

    public void setAdminFormId(int adminFormId) {
        this.adminFormId = adminFormId;
    }
}
