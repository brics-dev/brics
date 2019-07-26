package gov.nih.nichd.ctdb.site.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import gov.nih.nichd.ctdb.common.CtdbDao;
import gov.nih.nichd.ctdb.common.CtdbException;
import gov.nih.nichd.ctdb.common.DuplicateObjectException;
import gov.nih.nichd.ctdb.common.ObjectNotFoundException;
import gov.nih.nichd.ctdb.security.domain.User;
import gov.nih.nichd.ctdb.site.domain.Site;
import gov.nih.nichd.ctdb.util.domain.Address;

public class SiteDao extends CtdbDao {

	/**
	 * Private Constructor to hide the instance creation implementation of the AttachmentDao object in memory. This will
	 * provide a flexible architecture to use a different pattern in the future without re-factoring the AttachmentDao.
	 */
	private SiteDao() {}

	/**
	 * Method to retrieve the instance of the AttachmentDao.
	 *
	 * @return AttachmentDao data object
	 */
	public static synchronized SiteDao getInstance() {
		return new SiteDao();
	}

	/**
	 * Method to retrieve the instance of the AttachmentDao. This method accepts a Database Connection to be used
	 * internally by the DAO. All transaction management will be handled at the BusinessManager level.
	 *
	 * @param conn Database connection to be used within this data object
	 * @return AttachmentDao data object
	 */
	public static synchronized SiteDao getInstance(Connection conn) {
		SiteDao dao = new SiteDao();
		dao.setConnection(conn);
		return dao;
	}

	public void createStudySite(Site s) throws DuplicateObjectException, CtdbException {
		PreparedStatement stmt = null;

		try {
			String sql = "insert into site (siteid, protocolid, name, description, addressid, phonenumber, "
					+ "sitepiid, siteurl, primarysite, bricssiteid, createdby, createddate, updatedby, updateddate) "
					+ "values (DEFAULT, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, ?, CURRENT_TIMESTAMP) ";

			stmt = this.conn.prepareStatement(sql);

			stmt.setLong(1, s.getProtocolId());
			stmt.setString(2, s.getName());
			stmt.setString(3, s.getDescription());
			stmt.setLong(4, s.getAddress().getId());
			stmt.setString(5, s.getPhoneNumber());
			stmt.setLong(6, s.getSitePI().getId());
			stmt.setString(7, s.getSiteURL());
			stmt.setBoolean(8, s.isPrimarySite());
			stmt.setString(9, s.getBricsStudySiteId());
			stmt.setLong(10, s.getCreatedBy());
			stmt.setLong(11, s.getUpdatedBy());

			stmt.executeUpdate();
			s.setId(this.getInsertId(conn, "site_seq"));
		} catch (SQLException e) {
			if (e.getMessage().toUpperCase().indexOf("UK_") > 0) {
				throw new DuplicateObjectException("Duplicate site.", e);
			} else {
				throw new CtdbException("Unable to create a new site.", e);
			}
		} finally {
			close(stmt);
		}
	}

	public void updateStudySite(Site s) throws DuplicateObjectException, CtdbException {
		PreparedStatement stmt = null;
		try {

			String sql = "update site set name = ?, description = ?,  phonenumber = ?, SITEPIID = ?, SITEURL = ?, "
					+ "PRIMARYSITE = ?, BRICSSITEID = ?, updatedby = ?, updateddate = CURRENT_TIMESTAMP where siteid = ? ";

			stmt = this.conn.prepareStatement(sql);

			stmt.setString(1, s.getName());
			stmt.setString(2, s.getDescription());
			stmt.setString(3, s.getPhoneNumber());
			stmt.setLong(4, s.getSitePI().getId());
			stmt.setString(5, s.getSiteURL());
			stmt.setBoolean(6, s.isPrimarySite());
			stmt.setString(7, s.getStudySiteId());
			stmt.setLong(8, s.getUpdatedBy());
			stmt.setLong(9, s.getId());

			stmt.executeUpdate();
		} catch (SQLException sqle) {
			if (sqle.getMessage().toUpperCase().indexOf("UK_") > 0) {
				throw new DuplicateObjectException("Duplicate site name", sqle);
			} else {
				throw new CtdbException("Failure updating site : " + sqle.getMessage() + sqle);
			}
		} finally {
			close(stmt);
		}
	}

	/**
	 * Deletes the specified site from the database.
	 * 
	 * @param s - The site to be deleted
	 * @throws CtdbException When an error occurs while deleting the site.
	 */
	public void deleteSite(Site s) throws CtdbException {
		PreparedStatement stmt = null;

		try {
			String sql = "delete FROM site where siteid = ? ";

			stmt = this.conn.prepareStatement(sql);
			stmt.setLong(1, s.getId());
			stmt.executeUpdate();
		} catch (SQLException sqle) {
			throw new CtdbException("Failure deleting site.", sqle);
		} finally {
			close(stmt);
		}
	}

	public boolean isCountryIdAndCountryNameValid(int countryId, String countryName) throws CtdbException {
		boolean isValid = false;
		PreparedStatement stmt = null;
		ResultSet rs = null;

		try {
			String query = "select longname from xcountry where xcountryid = ? ";

			stmt = this.conn.prepareStatement(query);
			stmt.setLong(1, countryId);
			rs = stmt.executeQuery();

			if (rs.next()) {
				String name = rs.getString("longname");
				if (name.equals(countryName)) {
					isValid = true;
				}
			} else {
				isValid = false;
			}
		} catch (SQLException sqle) {
			throw new CtdbException("Error while looking up country: " + countryName, sqle);
		} finally {
			close(rs);
			close(stmt);
		}

		return isValid;
	}

	public boolean isStateIdAndStateNameValid(int stateId, String stateName) throws CtdbException {
		boolean isValid = false;
		PreparedStatement stmt = null;
		ResultSet rs = null;

		try {
			String query = "select name from xstate where xstateid = ? ";

			stmt = this.conn.prepareStatement(query);
			stmt.setLong(1, stateId);
			rs = stmt.executeQuery();

			if (rs.next()) {
				String name = rs.getString("name");
				if (name.equals(stateName)) {
					isValid = true;
				}
			} else {
				isValid = false;
			}
		} catch (SQLException sqle) {
			throw new CtdbException("Error while looking up state: " + stateName, sqle);
		} finally {
			close(rs);
			close(stmt);
		}

		return isValid;
	}

	public Site getSite(long siteId) throws ObjectNotFoundException, CtdbException {
		PreparedStatement stmt = null;
		ResultSet rs = null;

		try {
			String query = "select s.siteid, s.name, s.description, s.protocolid, s.phonenumber, "
					+ "s.updateddate, s.updatedby, s.siteurl, s.primarysite, s.bricssiteid, s.sitepiid, "
					+ "u.usrid, u.username, u.firstname, u.middlename, u.lastname, ad.addressid, "
					+ getDecryptionFunc("ad.addressone") + " as addressone, " + getDecryptionFunc("ad.addresstwo")
					+ " as addresstwo, " + getDecryptionFunc("ad.city") + " as city, " + getDecryptionFunc("ad.zipcode")
					+ " as zipcode, st.xstateid, st.code, st.name as statename, ct.xcountryid, ct.longname "
					+ "from site s join usr u on s.sitepiid = u.usrid join address ad on s.addressid = ad.addressid "
					+ "left join xstate st on cast(" + getDecryptionFunc("ad.xstateid") + " as bigint) = st.xstateid "
					+ "left join xcountry ct on cast(" + getDecryptionFunc("ad.xcountryid")
					+ " as bigint) = ct.xcountryid where s.siteid = ? ";

			stmt = this.conn.prepareStatement(query);
			stmt.setLong(1, siteId);
			rs = stmt.executeQuery();

			if (rs.next()) {
				return rsToSite(rs);
			} else {
				throw new ObjectNotFoundException("Site not found for ID: " + siteId);
			}
		} catch (SQLException sqle) {
			// Check the sql state
			if (sqle.getSQLState().contains("39000")) {
				throw new CtdbException("Column decryption failed.", sqle);
			} else {
				throw new CtdbException("Failure getting site with ID: " + siteId, sqle);
			}
		} finally {
			close(rs);
			close(stmt);
		}
	}

	public List<String> getBricsStudySiteIdsForProtocol(long protocolId) throws CtdbException {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		List<String> idList = new ArrayList<String>();

		try {
			String query = "select bricssiteid from site where protocolid = ?";
			stmt = this.conn.prepareStatement(query);
			stmt.setLong(1, protocolId);
			rs = stmt.executeQuery();

			while (rs.next()) {

				String studySiteId = rs.getString("bricssiteid");
				idList.add(studySiteId);
			}
		} catch (SQLException sqle) {
			throw new CtdbException("Unable to get BRICS site ID list for protocol: " + protocolId, sqle);
		} finally {
			close(rs);
			close(stmt);
		}

		return idList;
	}

	public int getAddressIdForSite(String bricsStudySiteId) throws CtdbException {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		int addressId = -1;

		try {
			String query = "select addressid from site where bricssiteid = ?";
			stmt = this.conn.prepareStatement(query);
			stmt.setString(1, bricsStudySiteId);
			rs = stmt.executeQuery();

			while (rs.next()) {
				addressId = rs.getInt("addressid");
			}
		} catch (SQLException sqle) {
			throw new CtdbException("Unable to get address ID for BRICS site ID: " + bricsStudySiteId, sqle);
		} finally {
			close(rs);
			close(stmt);
		}

		return addressId;
	}

	public int getSiteIdForSite(String bricsStudySiteId) throws CtdbException {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		int siteId = -1;

		try {
			String query = "select siteid from site where bricssiteid = ? ";

			stmt = this.conn.prepareStatement(query);
			stmt.setString(1, bricsStudySiteId);
			rs = stmt.executeQuery();

			while (rs.next()) {
				siteId = rs.getInt("siteid");
			}
		} catch (SQLException sqle) {
			throw new CtdbException("Unable to get site ID for BRICS site ID: " + bricsStudySiteId, sqle);
		} finally {
			close(rs);
			close(stmt);
		}

		return siteId;
	}

	public List<Site> getSites(long protocolId) throws CtdbException {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		List<Site> alist = new ArrayList<Site>();

		try {
			String query = "select s.siteid, s.name, s.description, s.protocolid, s.phonenumber, "
					+ "s.updateddate, s.updatedby, s.siteurl, s.primarysite, s.bricssiteid, s.sitepiid, "
					+ "u.usrid, u.username, u.firstname, u.middlename, u.lastname, ad.addressid, "
					+ getDecryptionFunc("ad.addressone") + " as addressone, " + getDecryptionFunc("ad.addresstwo")
					+ " as addresstwo, " + getDecryptionFunc("ad.city") + " as city, " + getDecryptionFunc("ad.zipcode")
					+ " as zipcode from site s, usr u, address ad where s.addressid = ad.addressid and "
					+ "s.sitepiid = u.usrid and s.protocolid = ? ";

			stmt = this.conn.prepareStatement(query);
			stmt.setLong(1, protocolId);
			rs = stmt.executeQuery();

			while (rs.next()) {
				alist.add(rsToSite(rs));
			}
		} catch (SQLException sqle) {
			sqle.printStackTrace();

			// Check the sql state
			if (sqle.getSQLState().contains("39000")) {
				throw new CtdbException("Column decryption failed.", sqle);
			} else {
				throw new CtdbException("Failure getting site for protocol: " + protocolId, sqle);
			}
		} finally {
			close(stmt);
			close(rs);
		}

		return alist;
	}

	private Site rsToSite(ResultSet rs) throws SQLException {
		Site s = new Site();
		Address add = new Address();
		User usr = new User();

		s.setId(rs.getInt("siteid"));
		s.setName(rs.getString("name"));
		s.setDescription(rs.getString("description"));
		s.setProtocolId(rs.getInt("protocolid"));
		s.setPhoneNumber(rs.getString("phonenumber"));
		s.setUpdatedDate(rs.getTimestamp("updateddate"));
		s.setUpdatedBy(rs.getInt("updatedby"));
		s.setSiteURL(rs.getString("siteurl"));
		s.setPrimarySite(rs.getBoolean("primarysite"));
		s.setStudySiteId(rs.getString("bricssiteid"));
		s.getSitePI().setId(rs.getInt("sitepiid"));

		add.setId(rs.getInt("addressid"));
		add.setAddressOne(rs.getString("addressone"));
		add.setAddressTwo(rs.getString("addresstwo"));
		add.setCity(rs.getString("city"));
		add.setZipCode(rs.getString("zipcode"));

		usr.setId(rs.getInt("usrid"));
		usr.setUsername(rs.getString("username"));
		usr.setFirstName(rs.getString("firstname"));
		usr.setMiddleName(rs.getString("middlename"));
		usr.setLastName(rs.getString("lastname"));

		s.setAddress(add);
		s.setSitePI(usr);

		return s;
	}

	public Map<Integer, String> getAllSiteIdsAndNames() throws CtdbException {
		Map<Integer, String> siteMap = new HashMap<Integer, String>();
		PreparedStatement stmt = null;
		ResultSet rs = null;

		try {
			String query = "select siteid, name from site ";

			stmt = this.conn.prepareStatement(query);
			rs = stmt.executeQuery();

			while (rs.next()) {
				siteMap.put(rs.getInt("siteid"), rs.getString("name"));
			}
		} catch (SQLException e) {
			throw new CtdbException("Failure getting site data.", e);
		} finally {
			close(rs);
			close(stmt);
		}

		return siteMap;
	}
}
