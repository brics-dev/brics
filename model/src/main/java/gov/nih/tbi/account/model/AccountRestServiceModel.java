
package gov.nih.tbi.account.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import gov.nih.tbi.account.model.hibernate.Account;
import gov.nih.tbi.account.model.hibernate.EntityMap;
import gov.nih.tbi.account.model.hibernate.PermissionGroup;
import gov.nih.tbi.account.model.hibernate.PermissionGroupMember;
import gov.nih.tbi.commons.model.hibernate.User;

/**
 * The AccountWebServiceModel is a class that consists of a number of subclasses used by the REST Account web services.
 * These classes allow lists of objects to be returned by these web services.
 * 
 * @author mgree1
 * 
 */

public class AccountRestServiceModel
{

    @XmlRootElement(name = "accessibleEntityIDs")
    public static class LongListWrapper
    {

        @XmlElement(name = "ID")
        Set<Long> set = new HashSet<Long>();

        public LongListWrapper()
        {

        }

        public void addAll(Set<Long> l)
        {

            set.addAll(l);
        }

        public Set<Long> getList()
        {

            return set;
        }
    }

	@XmlRootElement
	@XmlAccessorType(XmlAccessType.FIELD)
	public static class StringListWrapper {
		@XmlElementWrapper(name = "stringList")
		private List<String> stringList;

		public StringListWrapper() {
			stringList = new ArrayList<String>();
		}

		public StringListWrapper(Collection<String> col) {
			if (col != null) {
				stringList = new ArrayList<String>(col);
			} else {
				stringList = new ArrayList<String>();
			}
		}

		public void add(String s) {
			stringList.add(s);
		}

		public void addAll(Collection<String> col) {
			stringList.clear();

			if (col != null) {
				stringList.addAll(col);
			}
		}

		public List<String> getStringList() {
			return stringList;
		}
	}

    @XmlRootElement(name = "Accounts")
    public static class AccountsWrapper
    {

        @XmlElement(name = "account")
        List<Account> account = new ArrayList<Account>();

        public AccountsWrapper()
        {

        }

        public void addAll(List<Account> c)
        {

            account.addAll(c);
        }

        public List<Account> getAccountList()
        {

            return account;
        }
    }

	@XmlRootElement(name = "AccountsMapByUsername")
	@XmlAccessorType(XmlAccessType.FIELD)
	public static class AccountsMapByUsernameWrapper {
		@XmlElementWrapper(name = "accountMap")
		Map<String, Account> accountsMap;

		public AccountsMapByUsernameWrapper() {
			accountsMap = new HashMap<String, Account>();
		}

		public void put(String username, Account account) {
			accountsMap.put(username, account);
		}

		public void putAll(Map<String, Account> am) {
			accountsMap.clear();

			if (am != null) {
				accountsMap.putAll(accountsMap);
			}
		}

		public Map<String, Account> getAccountMap() {
			return accountsMap;
		}
	}

    @XmlRootElement(name = "PermissionGroups")
    public static class PermissionGroupsWrapper
    {

        @XmlElement(name = "permissionGroup")
        List<PermissionGroup> permGrps = new ArrayList<PermissionGroup>();

        public PermissionGroupsWrapper()
        {

        }

        public void addAll(List<PermissionGroup> c)
        {

            permGrps.addAll(c);
        }

        public List<PermissionGroup> getPermissionGroupsList()
        {

            return permGrps;
        }
    }

    @XmlRootElement(name = "EntityMap")
	public static class EntityMapsWrapper {

        @XmlElement(name = "entityMapGroup")
        List<EntityMap> entMaps = new ArrayList<EntityMap>();

		public EntityMapsWrapper() {}

		/**
		 * Replaces all entity maps stored with the ones in the given list.
		 * 
		 * @param emList - The list of new entities to apply to the list.
		 */
		public void addAll(List<EntityMap> emList) {
			entMaps.clear();

			if (emList != null) {
				entMaps.addAll(emList);
			}
        }

		public List<EntityMap> getEntityMapsList() {
            return entMaps;
        }
    }
    
    @XmlRootElement(name = "PermissionAuthority")
    public static class PermissionAuthoritiesWrapper
    {

        @XmlElement(name = "permissionAuthorityGroup")
        List<PermissionAuthority> paMaps = new ArrayList<PermissionAuthority>();

        public PermissionAuthoritiesWrapper()
        {

        }

        public void addAll(List<PermissionAuthority> c)
        {

            paMaps.addAll(c);
        }

        public List<PermissionAuthority> getPermissionAuthorityList()
        {

            return paMaps;
        }
    }

    @XmlRootElement(name = "PermissionGroupMember")
    public static class PermissionGroupMemberWrapper
    {

        @XmlElement(name = "permissionGroupMember")
        List<PermissionGroupMember> pgMembers = new ArrayList<PermissionGroupMember>();

        public PermissionGroupMemberWrapper()
        {

        }

        public void addAll(List<PermissionGroupMember> c)
        {

            pgMembers.addAll(c);
        }

        public List<PermissionGroupMember> getPermissionGroupMembersList()
        {

            return pgMembers;
        }

        public void addAll(Set<PermissionGroupMember> memberSet)
        {

            pgMembers.addAll(memberSet);

        }
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlRootElement(name = "PermissionGroupMemberBoolean")
    public static class PerGrpMemBool
    {

        @XmlElement(name = "working")
        private Boolean working;

        public PerGrpMemBool()
        {

        }

        public Boolean isWorking()
        {

            return this.working;
        }

        public void setWorking(Boolean working)
        {

            this.working = working;
        }

    }
    
    @XmlRootElement(name = "Users")
    public static class UserList
    {
    	@XmlElement(name ="User")
    	List<User> list = new ArrayList<User>();
    	
    	public UserList()
    	{
    		
    	}
    	
    	public void addAll(List<User> users)
    	{
    		list.addAll(users);
    	}
    	
    	public void add(User user)
    	{
    		list.add(user);
    	}

		public List<User> getUsers() 
		{
			return list;
		}
    
    	
    }

}
