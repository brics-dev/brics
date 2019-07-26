
package gov.nih.tbi.account.portal;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.log4j.Logger;

import gov.nih.tbi.PortalConstants;
import gov.nih.tbi.account.model.PermissionAuthority;
import gov.nih.tbi.account.model.hibernate.EntityMap;
import gov.nih.tbi.account.ws.RestAccountProvider;
import gov.nih.tbi.commons.model.EntityType;
import gov.nih.tbi.commons.model.PermissionType;
import gov.nih.tbi.commons.portal.BaseAction;
import gov.nih.tbi.portal.PortalUtils;

/**
 * Abstract class that serves as the base for the permission management pages
 */
public abstract class AbstractEntityPermissionManagementAction extends BaseAction implements Serializable {

	private static final long serialVersionUID = -5445127580628438492L;
	private static Logger logger = Logger.getLogger(AbstractEntityPermissionManagementAction.class);

	/******************************************************************************/

	public String selectedAuthorityType;

	public String selectedAuthorityId;

	public String selectedPermissionName;

	private List<PermissionAuthority> permissionAuthorities;

	/******************************************************************************/

	public abstract Long getEntityId();

	public abstract EntityType getEntityType();

	public abstract List<EntityMap> getEntityMapList();

	public abstract List<EntityMap> getRemovedMapList();

	public abstract String getActionName();

	public abstract List<String> getEntityMapAuthNameList();

	/**
	 * Used to get abstracted items for the dropdown selection.
	 * 
	 * Removes any duplicates between retrieved authorities and things with permissions.
	 * 
	 * @throws MalformedURLException
	 * @throws UnsupportedEncodingException
	 */
	public List<PermissionAuthority> getPermissionAuthorities()
			throws MalformedURLException, UnsupportedEncodingException {

		if (permissionAuthorities == null) {
			permissionAuthorities = new ArrayList<PermissionAuthority>();

			// This code will only pull users from the current account modules
			String accountUrl = modulesConstants.getModulesAccountURL(getDiseaseId());
			RestAccountProvider restProvider =
					new RestAccountProvider(accountUrl, PortalUtils.getProxyTicket(accountUrl));
			permissionAuthorities.addAll(restProvider.getAccountsWithRole(getEntityType().getRole().getName()));
			accountUrl = modulesConstants.getModulesAccountURL(getDiseaseId());
			restProvider = new RestAccountProvider(accountUrl, PortalUtils.getProxyTicket(accountUrl));
			permissionAuthorities.addAll(restProvider.getPrivatePermissionGroups());

			for (PermissionAuthority pa : permissionAuthorities) {
				pa.setDiseaseKey(getDiseaseId().toString());
			}

		}

		Collections.sort(permissionAuthorities, new Comparator<PermissionAuthority>() {
			@Override
			public int compare(PermissionAuthority pa1, PermissionAuthority pa2) {

				return pa1.getDisplayName().compareToIgnoreCase(pa2.getDisplayName());
			}
		});

		return permissionAuthorities;
	}

	/**
	 * @throws MalformedURLException
	 * @throws UnsupportedEncodingException
	 ****************************************************************************/

	public abstract String load() throws MalformedURLException, UnsupportedEncodingException;

	public String list() {

		return PortalConstants.ACTION_VIEW;
	}

	/**
	 * This method does the action of adding / removing, and updating permissions TODO: THis method should be made
	 * global so it can be used with QT, right now it's duplicated.
	 * 
	 * @throws MalformedURLException
	 * @throws NumberFormatException
	 * @throws UnsupportedEncodingException
	 */
	public String changePermission() throws NumberFormatException, MalformedURLException, UnsupportedEncodingException {

		// selectedAuthorityId must be in the format <diseaseId>;<authorityId>
		String stringValue[] = selectedAuthorityId.split(";");
		if (stringValue.length != 2) {
			throw new IllegalArgumentException("Bad selectedAuthorityId");
		}
		String diseaseKey = stringValue[0];
		Long selectedId = Long.valueOf(stringValue[1]);

		if ("NONE".equals(selectedPermissionName)) {
			EntityMap removalEntityMap = null;

			for (EntityMap em : this.getEntityMapList()) {
				if (("ACCOUNT".equals(selectedAuthorityType) && em.getAccount() != null
						&& em.getAccount().getId().equals(selectedId))
						|| ("PERMISSION_GROUP".equals(selectedAuthorityType) && em.getPermissionGroup() != null
								&& em.getPermissionGroup().getId().equals(selectedId))) {
					removalEntityMap = em;
					break;
				}
			}

			this.getEntityMapList().remove(removalEntityMap);
			this.getRemovedMapList().add(removalEntityMap);
			this.getEntityMapAuthNameList().remove(removalEntityMap.getAuthority().getDisplayName());
		} else {
			PermissionType selectedPermission = PermissionType.valueOf(selectedPermissionName);

			boolean found = false;
			for (EntityMap em : this.getEntityMapList()) {
				// If the selected permission is 'OWNER' then set the old owner to 'ADMIN'
				if (selectedPermission.equals(PermissionType.OWNER)) {
					if (em.getPermission().equals(PermissionType.OWNER)) {
						em.setPermission(PermissionType.ADMIN);
					}
				}

				if (("ACCOUNT".equals(selectedAuthorityType) && em.getAccount() != null
						&& em.getAccount().getId().equals(selectedId))
						|| ("PERMISSION_GROUP".equals(selectedAuthorityType) && em.getPermissionGroup() != null
								&& em.getPermissionGroup().getId().equals(selectedId))) {

					found = true;
					em.setPermission(selectedPermission);
				}
			}

			// Add a new entity map
			if (!found) {
				EntityMap em = new EntityMap();
				em.setEntityId(getEntityId());
				em.setType(getEntityType());

				if ("ACCOUNT".equals(selectedAuthorityType)) {
					String accountUrl = modulesConstants.getModulesAccountURL(getDiseaseId());
					String proxyTicket = PortalUtils.getProxyTicket(accountUrl);
					RestAccountProvider accountProvider = new RestAccountProvider(accountUrl, proxyTicket);
					em.setAccount(accountProvider.getAccountById(selectedId));
					logger.info("Entity after account ws: " + em.toString());

				} else if ("PERMISSION_GROUP".equals(selectedAuthorityType)) {
					String accountUrl = modulesConstants.getModulesAccountURL(getDiseaseId());
					String proxyTicket = PortalUtils.getProxyTicket(accountUrl);
					RestAccountProvider accountProvider = new RestAccountProvider(accountUrl, proxyTicket);
					em.setPermissionGroup(accountProvider.getPermissionGroupById(selectedId));
				}
				
				em.setPermission(selectedPermission);
				em.getAuthority().setDiseaseKey(diseaseKey);
				getEntityMapList().add(em);
				getEntityMapAuthNameList().add(em.getAuthority().getDisplayName());
			}
		}

		return "inner";
	}

	
	public PermissionType[] getPermissionTypeList() {

		return PermissionType.values();
	}

	public void setSelectedAuthorityType(String selectedAuthorityType) {

		this.selectedAuthorityType = selectedAuthorityType;
	}

	public void setSelectedAuthorityId(String selectedAuthorityId) {

		this.selectedAuthorityId = selectedAuthorityId;
	}

	public void setSelectedPermissionName(String selectedPermissionName) {

		this.selectedPermissionName = selectedPermissionName;
	}

}
