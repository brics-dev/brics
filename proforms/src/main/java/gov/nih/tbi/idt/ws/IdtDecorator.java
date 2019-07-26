package gov.nih.tbi.idt.ws;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.List;

import javax.servlet.jsp.JspException;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.struts2.ServletActionContext;

import gov.nih.nichd.ctdb.security.domain.Privilege;
import gov.nih.nichd.ctdb.security.domain.User;
import gov.nih.nichd.ctdb.util.common.SysPropUtil;

/**
 * Provides decoration ability for objects on a list to be retrieved during building
 * an IdTable.  This is a philosophical child of org.apache.taglibs.display.Decorator.
 * It provides the same functionality but this one does not have a page context.
 * 
 * @author jpark1
 *
 */
public class IdtDecorator {
	protected IdtDecorator original;
	
	protected Object obj = null;
	protected Object collection = null;
	protected int rowIndex;
	
	public IdtDecorator(IdtDecorator orig) {
		addDecorated(orig);
	}
	
	/**
	 * Not preferred but usable constructor.
	 * Should be used along with addDecorated
	 */
	public IdtDecorator() {
		original = null;
	}
	
	public void init(Object collection) {
		this.collection = collection;
	}
	
	public String initRow(Object rowObj, int rowIndex) {
		obj = rowObj;
		this.rowIndex = rowIndex;
		return "";
	}
	
	public List getList() {
        if (this.collection instanceof List)
            return (List) this.collection;
        else
            throw new RuntimeException("This function is only supported if the given collection is a java.util.List.");
	}
	
	public Collection getCollection() {
        if (this instanceof Collection)
            return (Collection) this.collection;
        else
            throw new RuntimeException("This function is only supported if the given collection is a java.util.Collection.");
	}
	
	public Object getObject() {
		return obj;
	}
	
	/**
	 * Replace original decorated IdtDecorator
	 * 
	 * @param orig
	 */
	public void addDecorated(IdtDecorator orig) {
		original = orig;
	}
	
	protected Object getProperty(String property) {
		Object output = null;
		try {
			output = PropertyUtils.getProperty(obj, property);
		} catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
			e.printStackTrace();
		}
		return output;
	}
	
    /**
     * Gets the application webroot from ApplicationResources
     *
     * @return Application Web Root (http://.....)
     */
    public String getWebRoot() throws JspException {
    	 return SysPropUtil.getProperty("app.webroot");
    }
    
    /**
     * Checks a user's privilege based on the code passed in
     *
     * @param privCode The privilege to check for
     * @return True if the user has the privilege, false otherwise
     */
    protected boolean checkPrivilege(String privCode) {
        //get User object from session
        User user = (User) ServletActionContext.getRequest().getAttribute("user");

        Privilege privilegeToCheck = new Privilege();
        privilegeToCheck.setCode(privCode);
        boolean hasPriv = user.hasPrivilege(privilegeToCheck);
        return hasPriv;
    }    
}
