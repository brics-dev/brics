package gov.nih.nichd.ctdb.contacts.manager;

import java.sql.Connection;
import java.util.List;

import gov.nih.nichd.ctdb.common.CtdbException;
import gov.nih.nichd.ctdb.common.CtdbManager;
import gov.nih.nichd.ctdb.common.DuplicateObjectException;
import gov.nih.nichd.ctdb.contacts.dao.ExternalContactDao;
import gov.nih.nichd.ctdb.contacts.domain.ExternalContact;
import gov.nih.nichd.ctdb.util.dao.AddressDao;

public class ExternalContactManager extends CtdbManager {
	/**
	 * Creates a record in the database from the data in the given contact.
	 * 
	 * @param ec - The contact whose data will be persisted to the database.
	 * @throws DuplicateObjectException	When a duplicate contact record or name is already in the database.
	 * @throws CtdbException	When there is a database error while creating the contact record in the database.
	 */
	public void createExternalContact (ExternalContact ec) throws DuplicateObjectException, CtdbException  {
		Connection conn = null;
		
		try {
			conn = CtdbManager.getConnection(CtdbManager.AUTOCOMMIT_FALSE);
			
			if ( !isContactNameDuplicate(ec) ) {
				AddressDao.getInstance(conn).createAddress(ec.getAddress());
				ExternalContactDao.getInstance(conn).createExternalContact(ec);
				commit(conn);
			}
			else {
				throw new DuplicateObjectException(ec.getName() + " is already in the database.");
			}
		}
		finally {
			rollback(conn);
			close(conn);
		}
	}
	
	/**
	 * Updates the database record with the data in the given contact.
	 * 
	 * @param ec - The contact whose data will be persisted to the database.
	 * @throws DuplicateObjectException	When a duplicate name is found in the database.
	 * @throws CtdbException	When there is a database error while records are being updated.
	 */
	public void updateExternalContact(ExternalContact ec) throws DuplicateObjectException, CtdbException {
		Connection conn = null;
		
		try {
			conn = getConnection(CtdbManager.AUTOCOMMIT_FALSE);
			
			if ( !isContactNameDuplicate(ec) ) {
				AddressDao.getInstance(conn).updateAddress(ec.getAddress());
				ExternalContactDao.getInstance(conn).updateExternalContact(ec);
				commit(conn);
			}
			else {
				throw new DuplicateObjectException(ec.getName() + " is already in the database.");
			}
		}
		finally {
			rollback(conn);
			close(conn);
		}
	}
	
	/**
	 * The given contact's name will be compared to all other contacts in the database that are
	 * associated with the same study. The method will test if the given contact's name is not 
	 * already in the database. The comparison will ignore the case of the contact's name.
	 * 
	 * @param ec - The ExternalContact object that will be compared to other contacts in the database.
	 * @return	True if the contact's name is already in the database.
	 * @throws CtdbException	When there is an issue retrieving existing contacts from the database.
	 */
	public boolean isContactNameDuplicate(ExternalContact ec) throws CtdbException {
		boolean isNameDuplicate = false;
		Connection conn = null;
		List<ExternalContact> ecs = null;
		
		try {
			conn = getConnection();
			ecs = ExternalContactDao.getInstance(conn).getExternalContacts(ec.getProtocolId());
		}
		finally {
			close(conn);
		}
		
		// Verify the given contact name is unique (ignoring case)
		for ( ExternalContact dbContact : ecs ) {
			if ( (dbContact.getId() != ec.getId()) && dbContact.getName().equalsIgnoreCase(ec.getName()) ) {
				isNameDuplicate = true;
				break;
			}
		}
		
		return isNameDuplicate;
	}
	
	public ExternalContact getExternalContact(long contactId) throws CtdbException
	{
		Connection conn = null;
		ExternalContact ec = null;
		
		try
		{
			conn = getConnection();
			ec = ExternalContactDao.getInstance(conn).getExternalContact(contactId);
			ec.setAddress(AddressDao.getInstance(conn).getAddress(ec.getAddress().getId()));
		}
		finally
		{
			this.close(conn);
		}
		
		return ec;
	}
	
	public List<ExternalContact> getExternalContacts(long protocolId) throws CtdbException {
		Connection conn = null;
		List<ExternalContact> ecs = null;
		
		try {
			conn= CtdbManager.getConnection();
			ecs = ExternalContactDao.getInstance(conn).getExternalContacts(protocolId);
			
			for ( ExternalContact ec : ecs ) {
				ec.setAddress(AddressDao.getInstance(conn).getAddress(ec.getAddress().getId()));
			}
		}
		finally {
			this.close(conn);
		}
		
		return ecs;
	}
	
	public void deleteExternalContact(long ecId) throws CtdbException {
		Connection conn = null;
		ExternalContactDao ecDao = ExternalContactDao.getInstance(conn);
		AddressDao aDao = AddressDao.getInstance(conn);
		ExternalContact ec = null;
		
		try {
			conn = CtdbManager.getConnection(CtdbManager.AUTOCOMMIT_TRUE);
			ec = ecDao.getExternalContact(ecId);
			aDao.deleteAddress(ec.getAddress().getId());
			ecDao.deleteExternalContact(ecId);
		} finally {
			this.close(conn);
		}
	}
	
	/**
	 * Deletes external contact and its associated addresses based on the given list
	 * of contact IDs
	 * 
	 * @param externalContactIds - A list of external contact IDs to be deleted
	 * @param contactsDeleted - A list of contact names that were successfully deleted
	 * @param failedDeletion -  A list of contacts (names or IDs) that could not be deleted
	 * @throws CtdbException	Thrown when a connection could not be established to the database
	 */
	public void deleteExternalContacts(List<Integer> externalContactIds, List<String> contactsDeleted, List<String> failedDeletion) throws CtdbException
	{
		Connection conn = null;
		ExternalContact contact = null;
		
		try
		{
			conn =  getConnection(CtdbManager.AUTOCOMMIT_TRUE);
			AddressDao addressDao = AddressDao.getInstance(conn);
			ExternalContactDao contactDao = ExternalContactDao.getInstance(conn);
			
			for ( Integer contactId : externalContactIds )
			{
				try
				{
					contact = contactDao.getExternalContact(contactId);
					
					// Delete the associated address and contact, then add the contact to the deleted list
					addressDao.deleteAddress(contact.getAddress().getId());
					contactDao.deleteExternalContact(contact.getId());
					contactsDeleted.add(contact.getName());
				}
				catch ( CtdbException ce )
				{
					// Log the exception to standard out
					System.out.println(ce.getMessage());
					ce.printStackTrace();
					
					// Added the failed deletion attempt in the failure list
					if ( contact != null )
					{
						failedDeletion.add(contact.getName());
					}
					else
					{
						failedDeletion.add("Contact " + contactId.toString());
					}
				}
				
				contact = null;
			}
		}
		finally
		{
			close(conn);
		}
	}
}
