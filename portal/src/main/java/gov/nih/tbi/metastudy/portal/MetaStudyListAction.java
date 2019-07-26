package gov.nih.tbi.metastudy.portal;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import gov.nih.tbi.PortalConstants;
import gov.nih.tbi.account.model.hibernate.EntityMap;
import gov.nih.tbi.commons.model.EntityType;
import gov.nih.tbi.commons.model.PermissionType;
import gov.nih.tbi.commons.service.AccountManager;
import gov.nih.tbi.commons.service.MetaStudyManager;
import gov.nih.tbi.idt.ws.IdtInterface;
import gov.nih.tbi.idt.ws.InvalidColumnException;
import gov.nih.tbi.idt.ws.Struts2IdtInterface;
import gov.nih.tbi.metastudy.model.hibernate.MetaStudy;
import gov.nih.tbi.taglib.datatableDecorators.MetaStudyIdtListDecorator;

public class MetaStudyListAction extends BaseMetaStudyAction {
	static Logger logger = Logger.getLogger(MetaStudyListAction.class);

	private static final long serialVersionUID = 3640580630389549231L;

	@Autowired
	private MetaStudyManager metaStudyManager;

	@Autowired
	protected AccountManager accountManager;

	private HashMap<Long, EntityMap> permissionList;

	private List<MetaStudy> metaStudyList = new ArrayList<MetaStudy>();

	// Comparator to be used by sorting, it compares titles of two meta studies in alphabetic order
	private static final Comparator<MetaStudy> metaStudyComparator = new Comparator<MetaStudy>() {
		@Override
		public int compare(MetaStudy ms1, MetaStudy ms2) {
			if (ms1 != null && ms2 != null && ms1.getTitle() != null && ms2.getTitle() != null) {
				return ms1.getTitle().compareToIgnoreCase(ms2.getTitle());
			} else {
				return ms1.getId().compareTo(ms2.getId());
			}
		}
	};

	public String list() {

		try {
			if (!getIsMetaStudyAdmin()) {
				List<EntityMap> metaStudyEntities =
						getEntitiesForCurrentUser(PermissionType.READ, EntityType.META_STUDY, false);

				Set<Long> ids = new HashSet<Long>();
				for (EntityMap entity : metaStudyEntities) {
					ids.add(entity.getEntityId());
				}

				metaStudyList = metaStudyManager.getMetaStudyListByIds(ids);
				setPermissionList(metaStudyEntities);

			} else {
				// no need to permission check if you are an admin
				metaStudyList = metaStudyManager.getAllMetaStudies();

				// specifically call out which meta studies the admin user has owner priv.
				// with every other Priv, admin will trump
				setPermissionList(getEntitiesForCurrentUser(PermissionType.OWNER, EntityType.META_STUDY, true));
			}

		} catch (UnsupportedEncodingException e) {
			logger.error("Exception occured when fetching meta study list " + e.getMessage());
			e.printStackTrace();
			return PortalConstants.ACTION_LIST;
		}

		sortMetaStudies();
		return PortalConstants.ACTION_LIST;
	}

	// url: http://fitbir-portal-local.cit.nih.gov:8081/portal/metastudy/metaStudyListAction!getMetaStudyTableList.action
	public String getMetaStudyTableList() {
		try {
			IdtInterface idt = new Struts2IdtInterface();
			list();
			ArrayList<MetaStudy> outputList = new ArrayList<MetaStudy>(getMetaStudyList());
			idt.setList(outputList);
			idt.decorate(new MetaStudyIdtListDecorator());
			idt.setTotalRecordCount(outputList.size());
			idt.setFilteredRecordCount(outputList.size());
			idt.output();
		} catch (InvalidColumnException e) {
			logger.error("invalid column: " + e);
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * The default sort is on user's permission by Owner, Admin, Write, then Read and with a secondary sort by Title in
	 * ascending order.
	 */
	private void sortMetaStudies() {

		List<MetaStudy> ownerList = new ArrayList<MetaStudy>();
		List<MetaStudy> adminList = new ArrayList<MetaStudy>();
		List<MetaStudy> writeList = new ArrayList<MetaStudy>();
		List<MetaStudy> readList = new ArrayList<MetaStudy>();

		for (MetaStudy ms : metaStudyList) {
			Long id = ms.getId();
			if (permissionList.containsKey(id)) {
				EntityMap em = permissionList.get(id);
				if (em.getPermission() == PermissionType.OWNER) {
					ownerList.add(ms);
				} else if (em.getPermission() == PermissionType.ADMIN) {
					adminList.add(ms);
				} else if (em.getPermission() == PermissionType.WRITE) {
					writeList.add(ms);
				} else if (em.getPermission() == PermissionType.READ) {
					readList.add(ms);
				}
			} else {
				// Admin can view all meta studies. Here permission list only contains meta studies of which admin user
				// has owner privilege, and all the rest are admin permission by default.
				adminList.add(ms);
			}
		}

		Collections.sort(ownerList, metaStudyComparator);
		Collections.sort(adminList, metaStudyComparator);
		Collections.sort(writeList, metaStudyComparator);
		Collections.sort(readList, metaStudyComparator);

		metaStudyList.clear();
		metaStudyList.addAll(ownerList);
		metaStudyList.addAll(adminList);
		metaStudyList.addAll(writeList);
		metaStudyList.addAll(readList);
	}


	public List<MetaStudy> getMetaStudyList() {
		return metaStudyList;
	}

	public void setMetaStudyList(List<MetaStudy> metaStudyList) {
		this.metaStudyList = metaStudyList;
	}

	public HashMap<Long, EntityMap> getPermissionList() {

		return permissionList;
	}

	public void setPermissionList(List<EntityMap> permissionList) {

		HashMap<Long, EntityMap> mapList = new HashMap<Long, EntityMap>();

		for (EntityMap entity : permissionList) {
			mapList.put(entity.getEntityId(), entity);
		}
		setPermissionList(mapList);
	}

	public void setPermissionList(HashMap<Long, EntityMap> permissionList) {

		this.permissionList = permissionList;
	}

	public EntityMap getPermissionList(int index) {

		if (index + 1 > permissionList.size()) {
			return null;
		}
		return permissionList.get(index);
	}
}
