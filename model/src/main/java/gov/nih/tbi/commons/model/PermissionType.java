
package gov.nih.tbi.commons.model;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

public enum PermissionType
{
    READ("Read"), WRITE("Write"), ADMIN("Admin"), OWNER("Owner");

    private String name;

	private static final Map<String, PermissionType> lookup = new HashMap<String, PermissionType>();

	static {
		for (PermissionType pt : EnumSet.allOf(PermissionType.class)) {
			lookup.put(pt.getName(), pt);
		}
	}

    PermissionType(String name)
    {

        this.name = name;
    }

    public String getName()
    {

        return name;
    }
    
    /**
     * Compares the hierarchy of two permission types and returns comparator values 
     * For example, read permission would be less than write permission.
     * @param arg1
     * @param arg2
     * @return -1 if arg1 is less than arg2, 0 if equal, 1 if arg1 is greater than arg2
     */
    public static Integer compare(PermissionType arg1, PermissionType arg2) {
        
        if(arg1 == null || arg2 == null) {
            return -1;
        }
        
        switch(arg1) {
            case READ: 
            	 if(READ.equals(arg2)) {
                     return 0;
                 } else {
                     return -1;
                 }
            case WRITE:
                if(ADMIN.equals(arg2) || OWNER.equals(arg2)) {
                    return -1;
                } else {
                    return 1;
                }
            case ADMIN:
                if(OWNER.equals(arg2))  {
                    return -1;
                } else {
                    return 1;
                }
            case OWNER:
                return 0;
            default: return -1;
        }
    }

    public Boolean contains(PermissionType permission)
    {

        if (permission != null)
        {
            if (permission.equals(PermissionType.READ))
            {
                if (this.equals(PermissionType.READ) || this.equals(PermissionType.WRITE)
                        || this.equals(PermissionType.ADMIN) || this.equals(PermissionType.OWNER))
                {
                    return true;
                }
            }

            if (permission.equals(PermissionType.WRITE))
            {
                if (this.equals(PermissionType.WRITE) || this.equals(PermissionType.ADMIN)
                        || this.equals(PermissionType.OWNER))
                {
                    return true;
                }
            }

            if (permission.equals(PermissionType.ADMIN))
            {
                if (this.equals(PermissionType.ADMIN) || this.equals(PermissionType.OWNER))
                {
                    return true;
                }
            }

            if (permission.equals(PermissionType.OWNER))
            {
                if (this.equals(PermissionType.OWNER))
                {
                    return true;
                }
            }
        }

        return false;
    }

	public static PermissionType getByName(String permName) {
		return lookup.get(permName);
	}
}
