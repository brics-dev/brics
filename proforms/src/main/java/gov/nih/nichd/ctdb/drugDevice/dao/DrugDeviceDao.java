package gov.nih.nichd.ctdb.drugDevice.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import gov.nih.nichd.ctdb.common.CtdbDao;
import gov.nih.nichd.ctdb.common.CtdbException;
import gov.nih.nichd.ctdb.common.DuplicateObjectException;
import gov.nih.nichd.ctdb.common.InvalidRemovalException;
import gov.nih.nichd.ctdb.common.ObjectNotFoundException;
import gov.nih.nichd.ctdb.drugDevice.domain.DrugDevice;

/**
 * Developed by CIT.
 * @author Shashi Rudrappa
 * Date: May 01, 2012
 * @version 1.0
 */
public class DrugDeviceDao extends CtdbDao {

	/**
	 * Private Constructor to hide the instance
	 * creation implementation of the DrugDeviceDao object
	 * in memory. This will provide a flexible architecture
	 * to use a different pattern in the future without
	 * refactoring the DrugDeviceDao.
	 */
	private DrugDeviceDao() {

	}

	/**
	 * Method to retrieve the instance of the DrugDeviceDao.
	 *
	 * @return DrugDeviceDao data object
	 */
	public static synchronized DrugDeviceDao getInstance() {
		return new DrugDeviceDao();
	}

	/**
	 * Method to retrieve the instance of the DrugDeviceDao. This method
	 * accepts a Database Connection to be used internally by the DAO. All
	 * transaction management will be handled at the BusinessManager level.
	 *
	 * @param conn Database connection to be used within this data object
	 * @return DrugDeviceDao data object
	 */
	public static synchronized DrugDeviceDao getInstance(Connection conn) {
		DrugDeviceDao dao = new DrugDeviceDao();
		dao.setConnection(conn);
		return dao;
	}

	public void createDrugDevice (DrugDevice d) throws  ObjectNotFoundException, DuplicateObjectException, CtdbException{

		PreparedStatement stmt = null;
		try {
			StringBuffer sb = new StringBuffer();
			sb.append("insert into DRUGDEVICE (DRUGDEVICEID, PROTOCOLID, FDAIND, SPONSOR, createdby, createddate, updatedby, updateddate ) values ( ");
			sb.append( " DEFAULT, ?, ?, ?, ?, CURRENT_TIMESTAMP, ?, CURRENT_TIMESTAMP )");
			//d.setId(this.getSequenceValue(conn, "DRUGDEVICE_SEQ"));
			stmt = this.conn.prepareStatement(sb.toString());
			//stmt.setInt(1, d.getId());
			stmt.setLong(1, Long.valueOf(d.getProtocolId()));
			stmt.setString(2, d.getFdaInd());
			stmt.setString(3, d.getSponsor());
			stmt.setLong(4, Long.valueOf(d.getCreatedBy()));
			stmt.setLong(5, Long.valueOf(d.getUpdatedBy()));

			stmt.executeUpdate();
		} catch (SQLException sqle) {
			if (sqle.getMessage().toUpperCase().indexOf("UK_") > 0) {
				System.out.println("-------------------------------"+sqle);
				throw new DuplicateObjectException("Duplicate name protocol Drug & Device  ");
			} else {
				System.out.println("-------------------------------"+sqle);
				throw new CtdbException("Failure creating Drug & Device : " + sqle.getMessage() + sqle);
			}
		} finally {
			close(stmt);
		}
	}

	public void updateDrugDevice (DrugDevice d) throws  ObjectNotFoundException, DuplicateObjectException, CtdbException{     
		PreparedStatement stmt = null;
		try {

			StringBuffer sb = new StringBuffer();
			sb.append("update DRUGDEVICE set FDAIND = ?, SPONSOR = ?, ");
			sb.append(" updatedby = ?, updateddate = CURRENT_TIMESTAMP where DRUGDEVICEID = ? ");
			stmt = this.conn.prepareStatement(sb.toString());
			stmt.setString(1, d.getFdaInd());
			stmt.setString(2, d.getSponsor());
			stmt.setLong(3, Long.valueOf(d.getUpdatedBy()));
			stmt.setLong(4, Long.valueOf(d.getId()));

			stmt.executeUpdate();
		} catch (SQLException sqle) {
			if (sqle.getMessage().toUpperCase().indexOf("UK_") > 0) {
				throw new DuplicateObjectException("Duplicate drug & device");
			} else {
				throw new CtdbException("Failure updating drug & device: " + sqle.getMessage() + sqle);
			}
		} finally {
			close(stmt);
		}
	}


	public void deleteDrugDevice (int ddId) throws CtdbException {

		PreparedStatement stmt = null;
		try {

			StringBuffer sb = new StringBuffer();
			sb.append("delete FROM DRUGDEVICE where DRUGDEVICEID = ?  ");
			stmt = this.conn.prepareStatement(sb.toString());
			stmt.setLong(1, Long.valueOf(ddId));

			stmt.executeUpdate();
		} catch (SQLException sqle) {
			if (sqle.getMessage().toLowerCase().indexOf("fk") > 0) {
				throw new InvalidRemovalException (" Cannot Delete Drug & Device ");
			} else {
				throw new CtdbException("Failure deleting Drug & Device : " + sqle.getMessage() + sqle);
			}
		} finally {
			close(stmt);
		}
	}

	public DrugDevice getDrugDevice (int drugDeviceId)throws CtdbException {

		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {

			StringBuffer sb = new StringBuffer();
			sb.append("select DRUGDEVICE.* from DRUGDEVICE ");
			sb.append("where DRUGDEVICE.DRUGDEVICEID = ? ");

			stmt = this.conn.prepareStatement(sb.toString());
			stmt.setLong(1, Long.valueOf(drugDeviceId));
			rs  = stmt.executeQuery();
			if (rs.next()) {
				return rsToDrugDevice(rs);
			} else {
				throw new CtdbException("DRUG DEVICE Not Found : ");
			}
		} catch (SQLException sqle) {
			throw new CtdbException("Failure getting DRUG DEVICE : " + sqle.getMessage() + sqle);
		} finally {
			close(stmt);
			close (rs);
		}
	}

	public List<DrugDevice> getDrugDevices (int protocolId)throws CtdbException {

		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			StringBuffer sb = new StringBuffer();
			sb.append("select DRUGDEVICE.* from DRUGDEVICE ");
			sb.append("where DRUGDEVICE.protocolid = ? ");
			stmt = this.conn.prepareStatement(sb.toString());
			stmt.setLong(1, Long.valueOf(protocolId));
			rs  = stmt.executeQuery();
			List<DrugDevice> alist = new ArrayList<DrugDevice>();
			while (rs.next()){
				alist.add(rsToDrugDevice(rs));
			}
			return alist;
		} catch (SQLException sqle) {
			sqle.printStackTrace();
			throw new CtdbException("Failure getting drug and device : " + sqle.getMessage() + sqle);
		} finally {
			close(stmt);
			close (rs);
		}
	}

	private DrugDevice rsToDrugDevice (ResultSet rs) throws SQLException {
		DrugDevice d = new DrugDevice();

		d.setId(rs.getInt("DRUGDEVICEID"));
		d.setProtocolId(rs.getInt("PROTOCOLID"));
		d.setFdaInd(rs.getString("FDAIND"));
		d.setSponsor(rs.getString("SPONSOR"));

		return d;
	}
}
