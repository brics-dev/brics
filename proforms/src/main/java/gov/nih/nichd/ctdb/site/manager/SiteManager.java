package gov.nih.nichd.ctdb.site.manager;

import java.sql.Connection;
import java.util.List;
import java.util.Map;

import gov.nih.nichd.ctdb.common.CtdbException;
import gov.nih.nichd.ctdb.common.CtdbManager;
import gov.nih.nichd.ctdb.common.DuplicateObjectException;
import gov.nih.nichd.ctdb.common.ErrorMessage;
import gov.nih.nichd.ctdb.patient.dao.PatientManagerDao;
import gov.nih.nichd.ctdb.site.dao.SiteDao;
import gov.nih.nichd.ctdb.site.domain.Site;
import gov.nih.nichd.ctdb.util.dao.AddressDao;

public class SiteManager extends CtdbManager {
	public void createUpdateSites(List<Site> studySites, List<String> selectedBricsStudySiteIds, int protocolId,
			Connection conn) throws CtdbException {
		// First get the current sites in db
		List<String> currentBricsStudySiteIds = getBricsStudySiteIdsForProtocol(protocolId);

		// Need to determine if to delete
		for (String currentBricsStudySiteId : currentBricsStudySiteIds) {
			if (!selectedBricsStudySiteIds.contains(currentBricsStudySiteId)) {
				// delete!!!
				// need the site id and the address id!
				int addressId = getAddressIdForSite(currentBricsStudySiteId);
				int siteId = getSiteIdForSite(currentBricsStudySiteId);
				Site site = new Site();

				site.setId(siteId);
				site.getAddress().setId(addressId);
				deleteSite(site, conn);
			}
		}

		// Now need to determine which to add which to update
		for (Site site : studySites) {
			String bricsStudySiteId = site.getBricsStudySiteId();

			if (currentBricsStudySiteIds.contains(bricsStudySiteId)) {
				// update!!
				// need the address id!
				int addressId = getAddressIdForSite(bricsStudySiteId);
				site.getAddress().setId(addressId);
				site.setProtocolId(protocolId);
				updateStudySite(site, conn);
			} else {
				// add!!!
				site.setProtocolId(protocolId);
				createStudySite(site, conn);
			}
		}
	}

	private void createStudySite(Site s, Connection conn) throws CtdbException {
		AddressDao aDao = AddressDao.getInstance(conn);
		SiteDao dao = SiteDao.getInstance(conn);
		aDao.createAddress(s.getAddress());
		dao.createStudySite(s);
	}

	private void updateStudySite(Site s, Connection conn) throws CtdbException {
		AddressDao aDao = AddressDao.getInstance(conn);
		SiteDao dao = SiteDao.getInstance(conn);
		aDao.updateAddress(s.getAddress());
		dao.updateStudySite(s);
	}

	/**
	 * Delete the specified site and associated address from the database.
	 * 
	 * @param s - The site to be removed.
	 * @throws CtdbException When an error occurs while deleting.
	 */
	private void deleteSite(Site s, Connection conn) throws CtdbException {
		AddressDao aDao = AddressDao.getInstance(conn);
		SiteDao dao = SiteDao.getInstance(conn);
		PatientManagerDao pmDao = PatientManagerDao.getInstance(conn);

		pmDao.deassociatePatientSite(s.getId());
		dao.deleteSite(s);
		aDao.deleteAddress(s.getAddress().getId());
	}

	public boolean isCountryIDAndCountryNameValid(int countryId, String countryName) throws CtdbException {
		Connection conn = null;
		try {
			conn = CtdbManager.getConnection(CtdbManager.AUTOCOMMIT_TRUE);
			SiteDao dao = SiteDao.getInstance(conn);

			return dao.isCountryIdAndCountryNameValid(countryId, countryName);
		} finally {
			close(conn);
		}
	}

	public boolean isStateIdAndStateNameValid(int stateId, String stateName) throws CtdbException {
		Connection conn = null;
		try {
			conn = CtdbManager.getConnection(CtdbManager.AUTOCOMMIT_TRUE);
			SiteDao dao = SiteDao.getInstance(conn);

			return dao.isStateIdAndStateNameValid(stateId, stateName);
		} finally {
			close(conn);
		}
	}

	public void createStudySites(List<Site> siteList) throws DuplicateObjectException, CtdbException {
		Connection conn = null;

		try {
			conn = CtdbManager.getConnection(CtdbManager.AUTOCOMMIT_TRUE);
			AddressDao aDao = AddressDao.getInstance(conn);
			SiteDao dao = SiteDao.getInstance(conn);

			for (Site s : siteList) {
				int addressId = aDao.createAddress(s.getAddress());

				s.getAddress().setId(addressId);
				dao.createStudySite(s);
			}
		} finally {
			close(conn);
		}
	}

	public void updateStudySites(List<Site> siteList) throws DuplicateObjectException, CtdbException {
		Connection conn = null;

		try {
			conn = CtdbManager.getConnection(CtdbManager.AUTOCOMMIT_TRUE);
			AddressDao aDao = AddressDao.getInstance(conn);
			SiteDao dao = SiteDao.getInstance(conn);

			for (Site s : siteList) {
				aDao.updateAddress(s.getAddress());
				dao.updateStudySite(s);
			}
		} finally {
			close(conn);
		}
	}

	/**
	 * Delete the specified listing of sites from the database, as well as any associated addresses of each site.
	 * 
	 * @param deleteSiteList - The list of sites to be removed.
	 * @throws CtdbException When an error occurs while deleting.
	 */
	public void deleteSites(List<Site> deleteSiteList, List<ErrorMessage> errorList) throws CtdbException {
		Connection conn = null;
		ErrorMessage errorMsg = null;

		try {
			conn = CtdbManager.getConnection(CtdbManager.AUTOCOMMIT_FALSE);
			AddressDao aDao = AddressDao.getInstance(conn);
			SiteDao dao = SiteDao.getInstance(conn);
			PatientManagerDao pmDao = PatientManagerDao.getInstance(conn);

			for (Site s : deleteSiteList) {
				try {
					pmDao.deassociatePatientSite(s.getId());
					dao.deleteSite(s);
					aDao.deleteAddress(s.getAddress().getId());
					commit(conn);
				} catch (Exception e) {
					rollback(conn);

					errorMsg = new ErrorMessage();
					errorMsg.setObjId(s.getId());
					errorMsg.setObjName(s.getName());
					errorMsg.setException(e);
					errorList.add(errorMsg);
				}
			}
		} finally {
			rollback(conn);
			close(conn);
		}
	}

	public Site getSite(int siteId) throws CtdbException {
		Site s = null;
		Connection conn = null;
		try {
			conn = CtdbManager.getConnection();
			AddressDao aDao = AddressDao.getInstance(conn);
			SiteDao dao = SiteDao.getInstance(conn);

			s = dao.getSite(siteId);
			s.setAddress(aDao.getAddress(s.getAddress().getId()));
		} finally {
			close(conn);
		}

		return s;
	}

	public List<Site> getSites(int protocolId) throws CtdbException {
		List<Site> sites = null;
		Connection conn = null;

		try {
			conn = CtdbManager.getConnection();
			SiteDao dao = SiteDao.getInstance(conn);
			sites = dao.getSites(protocolId);
		} finally {
			close(conn);
		}

		return sites;
	}

	public List<String> getBricsStudySiteIdsForProtocol(long protocolId) throws CtdbException {
		Connection conn = null;
		List<String> bricsStudySiteIds = null;

		try {
			conn = CtdbManager.getConnection();
			SiteDao dao = SiteDao.getInstance(conn);
			bricsStudySiteIds = dao.getBricsStudySiteIdsForProtocol(protocolId);
		} finally {
			close(conn);
		}

		return bricsStudySiteIds;
	}

	public int getAddressIdForSite(String bricsStudySiteId) throws CtdbException {
		Connection conn = null;

		try {
			conn = CtdbManager.getConnection();
			SiteDao dao = SiteDao.getInstance(conn);
			int addressId = dao.getAddressIdForSite(bricsStudySiteId);

			return addressId;
		} finally {
			close(conn);
		}
	}

	public int getSiteIdForSite(String bricsStudySiteId) throws CtdbException {
		Connection conn = null;

		try {
			conn = CtdbManager.getConnection();
			SiteDao dao = SiteDao.getInstance(conn);
			int siteId = dao.getSiteIdForSite(bricsStudySiteId);

			return siteId;
		} finally {
			close(conn);
		}
	}

	public Map<Integer, String> getAllSiteIdsAndNames() throws CtdbException {
		Map<Integer, String> siteMap = null;
		Connection conn = null;

		try {
			conn = CtdbManager.getConnection();
			SiteDao dao = SiteDao.getInstance(conn);
			siteMap = dao.getAllSiteIdsAndNames();
		} finally {
			close(conn);
		}

		return siteMap;
	}
}
