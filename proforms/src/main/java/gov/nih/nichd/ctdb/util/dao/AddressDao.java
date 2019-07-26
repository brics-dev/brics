package gov.nih.nichd.ctdb.util.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import gov.nih.nichd.ctdb.common.CtdbDao;
import gov.nih.nichd.ctdb.common.CtdbException;
import gov.nih.nichd.ctdb.common.CtdbLookup;
import gov.nih.nichd.ctdb.common.ObjectNotFoundException;
import gov.nih.nichd.ctdb.util.domain.Address;

/**
 * Created by IntelliJ IDEA.
 * User: breymaim
 * Date: Jun 26, 2007
 * Time: 9:57:34 AM
 * To change this template use File | Settings | File Templates.
 */
public class AddressDao extends CtdbDao {
	
    private AddressDao () {
    	
    }
    
    public static synchronized AddressDao getInstance () {
        return new AddressDao();
    }
    
    public static synchronized AddressDao getInstance (Connection conn) {
        AddressDao dao = new AddressDao();
        dao .setConnection(conn);
        return dao;
    }
    
    private static final String selectStarFromAddress = "select address.addressid, " + getDecryptionFunc("address.addressone") + " as addressone, " +
		getDecryptionFunc("address.addresstwo") + " as addresstwo, " + getDecryptionFunc("address.city") + " as city, " +
		getDecryptionFunc("address.xstateid") + " as xstateid, " + getDecryptionFunc("address.zipcode") + " as zipcode, " +
		"address.createdby, address.createddate, address.updatedby, address.updateddate, " + getDecryptionFunc("address.xcountryid") + " as xcountryid ";
    
	/**
	 * Creates an Address in the CTDB System. All address data that
	 * is stored into the database will be encrypted.
	 *
	 * @param address The address to create
	 * @throws gov.nih.nichd.ctdb.common.CtdbException thrown if any other errors occur while processing
	 */
	public int createAddress(Address address) throws CtdbException {
		PreparedStatement stmt = null;
		
		try {
			
			String sql = "insert into address(addressid, addressone, addresstwo, city, xstateid, zipcode, " +
						 "createdby, createddate, updatedby, updateddate, xcountryid) " +
						 "values (DEFAULT, " + CtdbDao.STANDARD_ENCRYPTION_FUNCT + ", " + // addressone
						 CtdbDao.STANDARD_ENCRYPTION_FUNCT + ", " + // addresstwo
						 CtdbDao.STANDARD_ENCRYPTION_FUNCT + ", " + // city
						 CtdbDao.STANDARD_ENCRYPTION_FUNCT + ", " + // xstateid
						 CtdbDao.STANDARD_ENCRYPTION_FUNCT + ", " + // zipcode
						 "?, CURRENT_TIMESTAMP, ?, CURRENT_TIMESTAMP,  " + // createdby, createddate, updatedby, and updateddate
						 CtdbDao.STANDARD_ENCRYPTION_FUNCT + ") "; // xcountryid
			
			stmt = this.conn.prepareStatement(sql);
			stmt.setString(1, notNull(address.getAddressOne()));
			stmt.setString(2, notNull(address.getAddressTwo()));
			stmt.setString(3, notNull(address.getCity()));
			
			if ( (address.getState() != null) && (address.getState().getId() > 0) ) {
				stmt.setString(4, Integer.toString(address.getState().getId()));
			}
			else {
				stmt.setNull(4, java.sql.Types.VARCHAR);
			}
			
			stmt.setString(5, notNull(address.getZipCode()));
			
			if (address.getCreatedBy() == Integer.MIN_VALUE) {
				address.setCreatedBy(address.getUpdatedBy());
			}
			
			stmt.setLong(6, address.getCreatedBy());
			stmt.setLong(7, address.getUpdatedBy());
			
			if ( (address.getCountry() != null) && (address.getCountry().getId() > 0) ) {
				stmt.setString(8, Integer.toString(address.getCountry().getId()));
			}
			else {  // set country to next of kin placeholder
				stmt.setNull(8, java.sql.Types.VARCHAR);
			}
			
			stmt.executeUpdate();
			address.setId(this.getInsertId(conn, "address_seq"));
		}
		catch ( SQLException e ) {
			// Check the sql state
			if ( e.getSQLState().contains("39000") ) {
				throw new CtdbException("Column encryption failed: " + e.getMessage(), e);
			}
			else {
				throw new CtdbException("Unable to create new address: " + e.getMessage(), e);
			}
		}
		finally {
			this.close(stmt);
		}
		
		return address.getId();
	}
	
	/**
	 * Updates an Address in the CTDB System. All address data that
	 * is stored into the database will be encrypted.
	 *
	 * @param address The address to update
	 * @throws CtdbException thrown if any other errors occur while processing
	 */
	public void updateAddress(Address address) throws CtdbException {
		PreparedStatement stmt = null;
		
		try {
			
			String sql = "update address set addressone = " + CtdbDao.STANDARD_ENCRYPTION_FUNCT + ", " + "addresstwo = " + CtdbDao.STANDARD_ENCRYPTION_FUNCT + 
					", city = " + CtdbDao.STANDARD_ENCRYPTION_FUNCT + ", xstateid = " + CtdbDao.STANDARD_ENCRYPTION_FUNCT + ", zipcode = " + 
					CtdbDao.STANDARD_ENCRYPTION_FUNCT + ", updatedby = ?, updateddate = CURRENT_TIMESTAMP, xcountryid = " + CtdbDao.STANDARD_ENCRYPTION_FUNCT + 
					" where addressid = ? ";
			
			stmt = this.conn.prepareStatement(sql);
			stmt.setString(1, address.getAddressOne());
			stmt.setString(2, address.getAddressTwo());
			stmt.setString(3, address.getCity());
 			
			if ( (address.getState() != null) && (address.getState().getId() > 0) ) {
				stmt.setString(4, Integer.toString(address.getState().getId()));
			}
			else {
				stmt.setNull(4, java.sql.Types.VARCHAR);
			}
			stmt.setString(5, address.getZipCode());
			stmt.setLong(6, address.getUpdatedBy());
			
			if ( (address.getCountry() != null) && (address.getCountry().getId() > 0) ) {
				stmt.setString(7, Integer.toString(address.getCountry().getId()));
			}
			else {  // set country to next of kin placeholder
				stmt.setNull(7, java.sql.Types.VARCHAR);
			}
			stmt.setLong(8, address.getId());
			
			int recordsUpdated = stmt.executeUpdate();
			
			if (recordsUpdated == 0) {
				throw new ObjectNotFoundException("The address with ID: " + address.getId() + " does not exist in the system.");
			}
		}
		catch (SQLException e) {
			// Check the sql state
			if ( e.getSQLState().contains("39000") ) {
				throw new CtdbException("Column encryption failed: " + e.getMessage(), e);
			}
			else {
				throw new CtdbException("Unable to update address: " + e.getMessage(), e);
			}
		}
		finally {
			this.close(stmt);
		}
	}
	
	/**
	 * Deletes the address record from the database (hard delete).
	 * 
	 * @param addressId - The ID of the address record to be deleted
	 * @throws CtdbException	If an error occurred during the deletion.
	 */
	public void deleteAddress(long addressId) throws CtdbException
	{
		PreparedStatement stmt = null;
		
		try
		{
			String sql = "delete FROM address where addressid = ? ";
			stmt = this.conn.prepareStatement(sql);
			stmt.setLong(1, addressId);
			stmt.executeUpdate();
		}
		catch (SQLException e)
		{
			throw new CtdbException("Unable to delete address: " + e.getLocalizedMessage(), e);
		}
		finally
		{
			this.close(stmt);
		}
	}
	
	/**
	 * Retrieves a Address from the system based on the unique identifier.
	 *
	 * @param addressId The unique identifier of the Address to retrieve
	 * @return Address data object
	 * @throws ObjectNotFoundException thrown if the address does not exist in the system
	 * @throws CtdbException           thrown if any other errors occur while processing
	 */
	public Address getAddress(long addressId) throws CtdbException {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		Address addr = new Address();
		
		try {
			if ( addressId != Integer.MIN_VALUE ) {
				
				String sql = selectStarFromAddress + ", xstate.code, xstate.name,  xcountry.longname " +
					"from address left join xstate on cast(" + getDecryptionFunc("address.xstateid") + 
					"as bigint) = xstate.xstateid left join xcountry on cast(" + getDecryptionFunc("address.xcountryid") + 
					" as bigint) = xcountry.xcountryid where address.addressid = ? ";
				
				stmt = this.conn.prepareStatement(sql);
				stmt.setLong(1, addressId);
				rs = stmt.executeQuery();
				
				if (!rs.next()) {
					throw new ObjectNotFoundException("The address with ID: " + addressId + " could not be found.");
				}
				
				addr = rsToAddress(rs);
			}
		}
		catch (SQLException e) {
			// Check the sql state
			if ( e.getSQLState().contains("39000") ) {
				throw new CtdbException("Column decryption failed: " + e.getMessage(), e);
			}
			else {
				throw new CtdbException("Unable to get retrieve address: " + e.getMessage(), e);
			}
		}
		finally {
			this.close(rs);
			this.close(stmt);
		}
		
		return addr;
	}
	
	/**
	 * Transforms a ResulSet object into a Address object
	 *
	 * @param rs ResultSet to transform to Address object
	 * @return Address data object
	 * @throws SQLException thrown if any errors occur while retrieving data from result set
	 */
	private Address rsToAddress(ResultSet rs) throws SQLException, NumberFormatException {
		Address address = new Address();
		address.setId(rs.getInt("addressid"));
		address.setAddressOne(rs.getString("addressone"));
		address.setAddressTwo(rs.getString("addresstwo"));
		address.setCity(rs.getString("city"));
		
		//state
		if(rs.getString("xstateid") == null) {
			CtdbLookup state = new CtdbLookup(Integer.MIN_VALUE, "", "");
			address.setState(state);
		}else {
			CtdbLookup state = new CtdbLookup(Integer.parseInt(rs.getString("xstateid").trim()), rs.getString("code"), rs.getString("name"));
			address.setState(state);
		}
		
		
		address.setZipCode(rs.getString("zipcode"));
		address.setCreatedBy(rs.getInt("createdby"));
		address.setCreatedDate(rs.getDate("createddate"));
		address.setUpdatedBy(rs.getInt("updatedby"));
		address.setUpdatedDate(rs.getDate("updateddate"));
		
		// country
		if(rs.getString("xcountryid") == null) {
			CtdbLookup country = new CtdbLookup(Integer.MIN_VALUE, "", "");
			address.setCountry(country);
		}else {
			CtdbLookup country = new CtdbLookup(Integer.parseInt(rs.getString("xcountryid").trim()), "", rs.getString("longname"));
			address.setCountry(country);
		}
		

		
		return address;
	}
	
	/**
	 * Checks for null String objects and passes back an empty string if null
	 * 
	 * @param s - The string to test for null
	 * @return	If the passed in string is null, the empty string is returned, otherwise the 
	 * original string is passed back.
	 */
	private final String notNull (String s) {
		if (s == null) {
		 return "";
		}
		
		return s;
	}
}
