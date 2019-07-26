package gov.nih.nichd.ctdb.protocol.manager;

import java.sql.Connection;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import gov.nih.nichd.ctdb.audit.manager.AuditManager;
import gov.nih.nichd.ctdb.common.CtdbConstants;
import gov.nih.nichd.ctdb.common.CtdbException;
import gov.nih.nichd.ctdb.common.CtdbManager;
import gov.nih.nichd.ctdb.common.DuplicateArchiveObjectException;
import gov.nih.nichd.ctdb.common.DuplicateObjectException;
import gov.nih.nichd.ctdb.common.InvalidRemovalException;
import gov.nih.nichd.ctdb.common.ObjectNotFoundException;
import gov.nih.nichd.ctdb.common.util.Utils;
import gov.nih.nichd.ctdb.drugDevice.manager.DrugDeviceManager;
import gov.nih.nichd.ctdb.form.domain.Form;
import gov.nih.nichd.ctdb.form.manager.FormManager;
import gov.nih.nichd.ctdb.patient.domain.PatientVisitPrepopValue;
import gov.nih.nichd.ctdb.protocol.dao.ProtocolManagerDao;
import gov.nih.nichd.ctdb.protocol.domain.BricsStudy;
import gov.nih.nichd.ctdb.protocol.domain.ClinicalLocation;
import gov.nih.nichd.ctdb.protocol.domain.ConfigureEformAuditDetail;
import gov.nih.nichd.ctdb.protocol.domain.Interval;
import gov.nih.nichd.ctdb.protocol.domain.IntervalClinicalPoint;
import gov.nih.nichd.ctdb.protocol.domain.MilesStone;
import gov.nih.nichd.ctdb.protocol.domain.PointOfContact;
import gov.nih.nichd.ctdb.protocol.domain.PrepopDataElement;
import gov.nih.nichd.ctdb.protocol.domain.Procedure;
import gov.nih.nichd.ctdb.protocol.domain.Protocol;
import gov.nih.nichd.ctdb.protocol.domain.ProtocolClosingOut;
import gov.nih.nichd.ctdb.protocol.domain.ProtocolLink;
import gov.nih.nichd.ctdb.protocol.domain.ProtocolUser;
import gov.nih.nichd.ctdb.security.domain.Role;
import gov.nih.nichd.ctdb.security.domain.User;
import gov.nih.nichd.ctdb.security.manager.SecurityManager;
import gov.nih.nichd.ctdb.site.manager.SiteManager;
import gov.nih.nichd.ctdb.util.common.SysPropUtil;
import gov.nih.tbi.dictionary.model.hibernate.eform.BasicEform;

/**
 * ProtocolManager is a business layer object which interacts with the ProtocolManagerDao. The
 * role of the ProtocolManager is to enforce business rule logic and delegate data layer manipulation
 * to the ProtocolManager.
 *
 * @author CIT
 * @version 1.0
 */
public class ProtocolManager extends CtdbManager
{
	private static final Logger logger = Logger.getLogger(ProtocolManager.class);
	
	/**
	 * Creates a Protocol in the CTDB System.
	 *
	 * @param protocol - The study data from the user.
	 * @throws DuplicateObjectException Thrown if the protocol already exists in the system
	 *         based on the unique constraints.
	 * @throws CtdbException Thrown if any database errors occurred while creating the new study.
	 */
	public Protocol createProtocol(Protocol protocol) throws DuplicateObjectException, CtdbException {
		Connection conn = getConnection(CtdbManager.AUTOCOMMIT_FALSE);
		
		try {
			ProtocolManagerDao dao = ProtocolManagerDao.getInstance(conn);
			Protocol savedProto = dao.createProtocol(protocol);
			
			// Set default study links
			ProtocolLink pl = new ProtocolLink();
			pl.setProtocolId(protocol.getId());
			pl.setAddress("https://exploredata.nichd.nih.gov/cognos8");
			pl.setName("Explore Data");
			pl.setDescription("Explore Data Collected in the CTDB");
			dao.createProtocolLink(pl);
			
			pl.setAddress("http://clinicaltrials.gov");
			pl.setName("Clinical Trials.gov");
			pl.setDescription("Clinical Trials.gov");
			dao.createProtocolLink(pl);
			dao.updateProtocolDefaults(protocol);
			dao.updateBtrisAccess(protocol);
			
			// Save any new sites and/or drug devices and/or audits
			SiteManager sMan = new SiteManager();
			sMan.createUpdateSites(protocol.getStudySites(), protocol.getSelectedBricsStudySiteIds(), protocol.getId(), conn);
			
			if (!protocol.isEnableEsignature()) {
				AuditManager aMan = new AuditManager();
				aMan.createUpdateAudit(protocol, conn);
			}
			// The instance with this property to be false, like pdbp and fitbir will not excute create
			// methods
			if (Boolean.valueOf(SysPropUtil.getProperty("display.protocol.clinicalPoint"))) {
				dao.createProtocolClinicalLocationAsso(savedProto.getId(), savedProto.getClinicalLocationList());
				dao.createProtocolProcedureAsso(savedProto.getId(), savedProto.getProcedureList());
				dao.createProtocolPointOfContactAsso(savedProto.getId(), savedProto.getPointOfContactList());
				dao.createProtocolMilesStoneAsso(savedProto.getId(), savedProto.getMilesStoneList());
			}
			commit(conn);
		}
		finally {
			rollback(conn);
			close(conn);
		}
		
		return protocol;
	}

	/**
	 * Updates a ProFoRMS study in the database.
	 *
	 * @param study - The study data from the user.
	 * @throws ObjectNotFoundException Thrown if the study does not exist in the database.
	 * @throws DuplicateObjectException Thrown if the study already exists in the database.
	 * @throws CtdbException Thrown if any other database errors occur while saving the changes to the study
	 */
	public void updateProtocol(Protocol protocol) 
			throws ObjectNotFoundException, DuplicateObjectException, CtdbException {
		Connection conn = null;

		try {
			conn = CtdbManager.getConnection(CtdbManager.AUTOCOMMIT_FALSE);
			ProtocolManagerDao dao = ProtocolManagerDao.getInstance(conn);
			SiteManager sMan = new SiteManager();
			
			// get current study that is in the database
			Protocol studyInDB = dao.getProtocol(protocol.getId());
			
			// if the study from the database is not equal to the new object, version study and persist the changes to the database
			//PI name are not updated by this method so added & condition to force it to update in case of UI update
			if (!protocol.equals(studyInDB)
					|| (protocol.getPrincipleInvestigator() != null && studyInDB.getPrincipleInvestigator() == null)) {
				dao.versionProtocol(studyInDB);
				dao.updateProtocol(protocol);
			}
			
			// Persist other changes to the study to the database
			dao.updateProtocolDefaults(protocol);
			dao.updateBtrisAccess(protocol);
			
			// Processes any changes to the sites and/or drug devices and/or audits of a study
			sMan.createUpdateSites(protocol.getStudySites(), protocol.getSelectedBricsStudySiteIds(), protocol.getId(), conn);
			
			AuditManager aMan = new AuditManager();
			if (!protocol.isEnableEsignature()) {
				aMan.createUpdateAudit(protocol, conn);
			}
			// The instance with this property to be false, like pdbp and fitbir will not excute update methods
			if (Boolean.valueOf(SysPropUtil.getProperty("display.protocol.clinicalPoint"))) {
				this.updateProtocolClinicalLocationAsso(protocol.getId(), protocol.getClinicalLocationList(), conn);
				dao.deleteProtocolProcedureAsso(protocol.getId());
				dao.createProtocolProcedureAsso(protocol.getId(), protocol.getProcedureList());

				this.updateProtocolPointOfContatctAsso(protocol.getId(), protocol.getPointOfContactList(), conn);
				dao.deleteProtocolMilesStoneAsso(protocol.getId());
				dao.createProtocolMilesStoneAsso(protocol.getId(), protocol.getMilesStoneList());
			}
			commit(conn);
		}
		finally {
			rollback(conn);
			close(conn);
		}
	}
	
	/**
	 * Re-sorts the list of linkable BRICS studies with the current linked study on top of the list.
	 * 
	 * @param currStudy - The current version of the ProFoRMS study from the client browser
	 * @param bricsStudyList - A listing of linkable BRICS studies
	 * @throws CtdbException	When an error occurs when putting the current link study on the top of the list.
	 */
	public void sortBricsStudyList(Protocol protocol, List<BricsStudy> bricsStudyList) throws CtdbException
	{
		// Check for bad list
		if ( bricsStudyList == null || bricsStudyList.isEmpty() )
		{
			return;
		}
		
		// Sort the list by the study title
		Collections.sort(bricsStudyList, new Comparator<BricsStudy>()
		{
			public int compare(BricsStudy bStudy1, BricsStudy bStudy2)
			{
				return bStudy1.getTitle().compareTo(bStudy2.getTitle());
			}
		});
		
		if (protocol != null) {
				// Find the current linked BRICS study and put it at the beginning of the list
				try
				{
					for ( Iterator<BricsStudy> it = bricsStudyList.iterator(); it.hasNext(); )
					{
						BricsStudy bStudy = it.next();
						
						// Check if the current BRICS study is linked to the current ProFoRMS study
						if ( bStudy.getPrefixedId().equals(protocol.getBricsStudyId()) )
						{
							it.remove();
							bricsStudyList.add(0, bStudy);
							break;
						}
					}
				}
				catch ( UnsupportedOperationException uoe )
				{
					logger.error("Couldn't move a linked BRICS study to the beginning of the study list.", uoe);
					throw new CtdbException("Couldn't move a linked BRICS study to the beginning of the study list.");
				}
				catch ( IllegalStateException ise )
				{
					logger.error("Couldn't move a linked BRICS study to the beginning of the study list.", ise);
					throw new CtdbException("Couldn't move a linked BRICS study to the beginning of the study list.");
				}
		}
	}

	/**
	 * Soft deletes a study from the system.
	 *
	 * @param protocolIdList - List of study IDs to be soft deleted
	 * @param user - User deleting the study
	 * @throws ObjectNotFoundException if the study does not exist in the system
	 * @throws DuplicateObjectException if the study already exists in the system
	 *         based on the unique constraints
	 * @throws CtdbException if any other errors occur while processing
	 * @throws UnknownBusinessManagerException 
	 * @throws BusinessManagerAccessException 
	 */
	public List<String> softDeleteProtocol(List<Integer> selectedProtocolIds, User user, 
			List<String> notDeletedProtocolList) throws ObjectNotFoundException, DuplicateObjectException, 
			CtdbException {
		
		Connection conn = null;
		List<String> protocolDeletedList = new ArrayList<String>();

		DateFormat dateFormat = new SimpleDateFormat(CtdbConstants.DATEFORMAT_FOR_SOFT_DELETED_RECORD);
		String formatedDate = dateFormat.format(new Date());
		
		SecurityManager sectMan = new SecurityManager();
		
		try {
			conn = CtdbManager.getConnection(CtdbManager.AUTOCOMMIT_TRUE);
			ProtocolManagerDao dao = ProtocolManagerDao.getInstance(conn);
			
			// get current protocol record
			for (int protocolId : selectedProtocolIds) {
				boolean isPI = sectMan.doesUserHaveRoleForStudy(user.getId(), protocolId, 
						SecurityManager.PRINCIPAL_INVESTIGATOR_ROLE);
				
				if (user.isSysAdmin() || isPI) {
					Protocol deleteProtocol = dao.getProtocol(protocolId);
					protocolDeletedList.add(deleteProtocol.getProtocolNumber());
					
					String protocolNumber = deleteProtocol.getProtocolNumber() + "||" + formatedDate;
					String protocolName = deleteProtocol.getName() + "||" + formatedDate;

					deleteProtocol.setProtocolNumber(protocolNumber);
					deleteProtocol.setName(protocolName);
					deleteProtocol.setDeleteFlag(CtdbConstants.DATABASE_DELETE_FLAG_TRUE);
					deleteProtocol.setUpdatedBy(user.getId());
					deleteProtocol.setId(protocolId);

					dao.softDeleteProtocol(deleteProtocol);
					
				} else {
					Protocol p  = dao.getProtocol(protocolId);
					notDeletedProtocolList.add(p.getProtocolNumber());
				}
			}
		} catch(Exception e) {
			throw new CtdbException("Unknown error occurred while deleting the Study: " + e.getMessage(), e);
		} finally {
			this.close(conn);
		}
		
		return protocolDeletedList;
	}

	/**
	 * Retrieves a Protocol from the system based on the unique identifier.
	 *
	 * @param protocolId The unique identifier of the Protocol to retrieve
	 * @return Protocol data object
	 * @throws ObjectNotFoundException if the patient does not exist in the system
	 * @throws CtdbException if any other errors occur while processing
	 */
	public Protocol getProtocol(int protocolId) throws ObjectNotFoundException, CtdbException
	{
		Connection conn = null;
		try
		{
			conn = CtdbManager.getConnection();
			ProtocolManagerDao dao = ProtocolManagerDao.getInstance(conn);
			Protocol protocol = dao.getProtocol(protocolId);
			FormManager fm = new FormManager();
			protocol.setForms(fm.getActiveForms(protocol.getId()));
			protocol.setProtocolDefaults(dao.getProtocolDefaults(protocol.getId()));
			protocol.setBtrisAccess(dao.getBtrisAccess(protocol.getId()));

			SiteManager sMan = new SiteManager();
			protocol.setStudySites(sMan.getSites(protocolId));

			DrugDeviceManager dMan = new DrugDeviceManager();
			protocol.setDrugDeviceList(dMan.getDrugDevices(protocolId));
			
			AuditManager aMan = new AuditManager();
			protocol.setAudit(aMan.getAuditLast(protocolId));

			return protocol;
		}
		finally
		{
			this.close(conn);
		}
	}

	/**
	 * Retrieves all protocols in the system.
	 *
	 * @return The list of protocols in the system. The list will be empty if no
	 *         protocols exist in the system.
	 * @throws CtdbException if any errors occur while processing
	 */
	public List<Protocol> getProtocols() throws CtdbException
	{
		Connection conn = null;
		try
		{
			conn = CtdbManager.getConnection();
			List<Protocol> protocols = ProtocolManagerDao.getInstance(conn).getProtocols();
			
			return protocols;
		}
		finally
		{
			this.close(conn);
		}
	}
	
	
	
	/**
	 * 
	 * 
	 * 
	 */
	public boolean doesVisitTypeHaveAnySelfReportingForms(int visitTypeId) throws CtdbException
	{
		Connection conn = null;
		try
		{
			conn = CtdbManager.getConnection();
			return ProtocolManagerDao.getInstance(conn).doesVisitTypeHaveAnySelfReportingForms(visitTypeId);

		}
		finally
		{
			this.close(conn);
		}
	}
	
	

	/**
	 * Retrieves all protocols associated with a user.
	 *
	 * @param userId The user ID
	 * @return The list of protocols. The list will be empty if no
	 *         protocols exist in the system.
	 * @throws CtdbException if any errors occur while processing
	 */
	public List<Protocol> getUserProtocols(int userId) throws CtdbException
	{
		Connection conn = null;
		try
		{
			conn = CtdbManager.getConnection();
			List<Protocol> protocols = ProtocolManagerDao.getInstance(conn).getUserProtocols(userId);
			return protocols;
		}
		finally
		{
			this.close(conn);
		}
	}

	public List<ProtocolUser> getProtocolUsers(int protocolId) throws CtdbException
	{
		Connection conn = null;

		try
		{
			conn = CtdbManager.getConnection();
			return ProtocolManagerDao.getInstance(conn).getProtocolUsers(protocolId, Integer.MIN_VALUE);
		}
		finally
		{
			this.close(conn);
		}
	}
	
	/**
	 * Mark any BRICS study in the specified list that are linked to other ProFoRMS studies.  This will be used for
	 * disabling the associated radio button in the web interface.
	 * 
	 * @param study - The current ProFoRMS study
	 * @param bricsStudyList - A list of BRICS studies that are accessible to the current user.
	 * @param linkedStudyNames - Will be populated with a list of BRICS study names that are currently linked to other ProFoRMS studies
	 * @throws CtdbException	If any database errors are encountered.
	 */
	/*public void markOtherLinkedBricsStudies(Protocol study, List<BricsStudy> bricsStudyList, List<String> linkedStudyNames) throws CtdbException
	{
		Connection conn = null;
		List<BricsStudy> linkedStudies = new ArrayList<BricsStudy>();
		
		try
		{
			conn = getConnection(CtdbManager.AUTOCOMMIT_FALSE);
			linkedStudyNames.addAll(ProtocolManagerDao.getInstance(conn).getOtherLinkedBricsStudies(study));
			
			if ( !linkedStudyNames.isEmpty() )
			{
				// Mark the BRICS study if it is linked to another ProFoRMS study
				for ( Iterator<BricsStudy> it = bricsStudyList.iterator(); it.hasNext(); )
				{
					BricsStudy bStudy = it.next();
					
					for ( String bStudyId : linkedStudyNames )
					{
						if ( bStudy.getPrefixedId().equalsIgnoreCase(bStudyId) )
						{
							bStudy.setLinkedInOtherStudy(true);
							linkedStudies.add(bStudy);
							it.remove();
							break;
						}
					}
				}
				
				// Add back the removed linked studies to the main BRICS study list.
				bricsStudyList.addAll(linkedStudies);
			}
		}
		finally
		{
			rollback(conn);
			close(conn);
		}
	}*/
	

	
	/**
	 * Retrieves all protocol users associated with a protocol
	 *
	 * @return  A list of all protocol users of class ProtocolUser.
	 * @throws  CtdbException if any errors occur while processing
	 */
	public List<ProtocolUser> getProtocolUsers(int protocolId, int siteId) throws CtdbException
	{
		Connection conn = null;

		try
		{
			conn = CtdbManager.getConnection();
			return ProtocolManagerDao.getInstance(conn).getProtocolUsers(protocolId, siteId);
		}
		finally
		{
			this.close(conn);
		}
	}

	/**
	 * Retrieves a protocol user's roles.
	 *
	 * @return  A list of all Roles that a protocol user have.  Currently the list always has at most
	 *          one Role object in it, since a protocol user can only have one role in the protocol.
	 * @throws  CtdbException if any errors occur while processing
	 */
	public List<Role> getProtocolRoles(int userId, int protocolId) throws CtdbException
	{
		Connection conn = null;

		try
		{
			conn = CtdbManager.getConnection();
			List<Integer> roleIds = ProtocolManagerDao.getInstance(conn).getProtocolRoles(userId, protocolId);

			List<Role> roles = new ArrayList<Role>();
			SecurityManager sm = new SecurityManager();
			
			for( Integer roleId : roleIds )
			{
				roles.add(sm.getSystemRole(roleId.intValue()));
			}

			return roles;
		}
		finally
		{
			this.close(conn);
		}
	}
	
	public void associateProtocolUser(int protocolId, int siteId, int roleId, int userId) throws CtdbException {
		Connection conn = null;
		conn = CtdbManager.getConnection();
		try {
			ProtocolManagerDao.getInstance(conn).associateProtocolUser(protocolId, siteId, roleId, userId);
		}
		finally {
			this.close(conn);
		}
	}

	/**
	 * Associate users to a protocol.
	 *
	 * @param   protocolId The protocol ID
	 * @param   protocolUsers List of Protocol users
	 * @throws  CtdbException if any other errors occur while processing
	 */
	public void associateProtocolUsers(int protocolId, int siteId, List<ProtocolUser> protocolUsers) throws CtdbException
	{
		Connection conn = null;

		try
		{
			conn = CtdbManager.getConnection();

			// go through protocolUsers,
			// a new user is created if username is not in the user table
			// make sure that the userId properly set from the user table
			SecurityManager sm = new SecurityManager();
			
			for( ProtocolUser protocolUser : protocolUsers )
			{
				String username = protocolUser.getUsername();
				User user = sm.getUser(username);  // a new user is created if username is not in the user table
				protocolUser.setUser(user);        // protocolUser now has the userId properly set from the user table
			}

			ProtocolManagerDao.getInstance(conn).associateProtocolUsers(protocolId,  siteId,protocolUsers);
		}
		finally
		{
			this.close(conn);
		}
	}

	/**
	 * Creates a Protocol link in the CTDB System.
	 *
	 * @param protocolLink The Protocol Link to create
	 * @throws DuplicateObjectException if the ProtocolLink already exists in the system
	 *         based on the unique constraints
	 * @throws CtdbException if any other errors occur while processing
	 */
	public void createProtocolLink(ProtocolLink protocolLink) throws DuplicateObjectException, CtdbException
	{
		Connection conn = null;
		try
		{
			conn = CtdbManager.getConnection();
			ProtocolManagerDao.getInstance(conn).createProtocolLink(protocolLink);
		}
		finally
		{
			this.close(conn);
		}
	}

	/**
	 * Updates a Protocol link in the CTDB System.
	 *
	 * @param protocolLink The Protocol Link to update
	 * @throws ObjectNotFoundException if the ProtocolLink does not exist in the system
	 * @throws DuplicateObjectException if the ProtocolLink already exists in the system
	 *         based on the unique constraints
	 * @throws CtdbException if any other errors occur while processing
	 */
	public void updateProtocolLink(ProtocolLink protocolLink) throws ObjectNotFoundException, DuplicateObjectException, CtdbException
	{
		Connection conn = null;
		try
		{
			conn = CtdbManager.getConnection();
			ProtocolManagerDao.getInstance(conn).updateProtocolLink(protocolLink);
		}
		finally
		{
			this.close(conn);
		}
	}


	/**
	 * Updates the ordering of protocol links in the protocol. A protocol link is a URL that can be
	 * accessed through the CTDB system. This ordering will be used when displaying URLs
	 * to the end user. If a URL is not ordered, it will be ordered by name.
	 *
	 * @param   orderedIds The Protocol link IDs in order of display
	 * @throws  CtdbException if any other errors occur while processing
	 */
	public void updateProtocolLinkOrdering(String[] orderedIds) throws CtdbException
	{
		Connection conn = null;

		try
		{
			conn = CtdbManager.getConnection(CtdbManager.AUTOCOMMIT_TRUE);
			ProtocolManagerDao dao = ProtocolManagerDao.getInstance(conn);
			for(int idx = 0; idx < orderedIds.length; idx++)
			{
				int protocolLinkId = Integer.parseInt(orderedIds[idx]);
				dao.updateProtocolLinkOrdering(protocolLinkId, idx);
			}
		}
		catch(Exception e)
		{
			throw new CtdbException("Unknown error occurred while updating protocol link order: " + e.getMessage(), e);
		}
		finally
		{
			this.close(conn);
		}
	}

	/**
	 * Retrieves a protocol site link from the system based on the unique identifier
	 *
	 * @param   protocolLinkId The SiteLink ID to retrieve
	 * @return  ProtocolLink data object
	 * @throws  ObjectNotFoundException if the site link does not exist in the system
	 * @throws  CtdbException if any other errors occur while processing
	 */
	public ProtocolLink getProtocolLink(int protocolLinkId) throws ObjectNotFoundException, CtdbException
	{
		Connection conn = null;

		try
		{
			conn = CtdbManager.getConnection();
			return ProtocolManagerDao.getInstance(conn).getProtocolLink(protocolLinkId);
		}
		finally
		{
			this.close(conn);
		}
	}

	/**
	 * Retrieves all protocol site links in a protocol
	 *
	 * @return  A list of all protocol site links in a protocol.
	 * @throws  CtdbException if any errors occur while processing
	 */
	public List<ProtocolLink> getProtocolLinks(int protocolId) throws CtdbException
	{
		Connection conn = null;

		try
		{
			conn = CtdbManager.getConnection();
			return ProtocolManagerDao.getInstance(conn).getProtocolLinks(protocolId);
		}
		finally
		{
			this.close(conn);
		}
	}

	/**
	 * Deletes a protocol link from the protocol based on the unique identifier
	 *
	 * @param   protocolLinkId The protocol link ID to delete
	 * @throws  CtdbException if any other errors occur while processing
	 */
	public void deleteProtocolLink(int protocolLinkId) throws CtdbException
	{
		Connection conn = null;

		try
		{
			conn = CtdbManager.getConnection();
			ProtocolManagerDao.getInstance(conn).deleteProtocolLink(protocolLinkId);
		}
		catch(Exception e)
		{
			throw new CtdbException("Unknown error occurred while deleting protocol link: " + e.getMessage(), e);
		}
		finally
		{
			this.close(conn);
		}
	}

	/**
	 * Retrieves all versions of a protocol stored. A version consists
	 * of modifications to the metadata (number,name,description,status) about
	 * a protocol.
	 *
	 * @param protocolId The unique identifier of the Protocol to retrieve
	 * @return A list of all versions for a single protocol. The array
	 *         will ordered by versions such that index 0 will be the first
	 *         protocol version. If the protocol does not exist an empty
	 *         array will be returned.
	 * @throws CtdbException if any errors occur
	 */
	public List<Protocol> getProtocolVersions(int protocolId) throws CtdbException
	{
		Connection conn = null;
		try
		{
			conn = CtdbManager.getConnection();
			return ProtocolManagerDao.getInstance(conn).getProtocolVersions(protocolId);
		}
		finally
		{
			this.close(conn);
		}
	}

	/**
	 * Retrieves the image path for a protocol
	 * @param protocolId The unique identifier of the Protocol
	 */
	public String getImagePath(int protocolId)
	{
		return "";
	}

	/**
	 * Creates a Interval in the CTDB System.
	 *
	 * @param visitType - The visit type to create
	 * @throws DuplicateObjectException if the interval already exists in the system
	 *         based on the unique constraints
	 * @throws CtdbException if any other errors occur while processing.
	 */
	public Interval createVisitType(Interval visitType,int studyId) throws DuplicateObjectException, CtdbException {
		Connection conn = null;
		List<BasicEform> bEformList = visitType.getIntervalEFormList();
		List<PrepopDataElement> prepopDEList = visitType.getPrepopulateDEList();
		
		try {
			conn = CtdbManager.getConnection(CtdbManager.AUTOCOMMIT_FALSE);
			ProtocolManagerDao dao = ProtocolManagerDao.getInstance(conn);
			dao.createVisitType(visitType);
			
			//Create Eform record for eeach Eform On List
			for (BasicEform bEform : bEformList) {
				dao.createEform(bEform, studyId);
				
				// Now associates visit type to the eform
				dao.createVistTypeEFormAssociation(visitType.getId(), bEform);
			}
			
			for (PrepopDataElement prepopDE : prepopDEList) {
				dao.createVistTypePrepopDEAssociation(visitType.getId(), prepopDE.getShortName());
			}
			// The instance with this property to be false, like pdbp and fitbir, will not excute create method
			if (Boolean.valueOf(SysPropUtil.getProperty("display.protocol.clinicalPoint"))) {
				dao.createVisitTypeClinicalPointAsso(visitType.getIntervalClinicalPointList(), visitType.getId());
			}
			this.commit(conn);
		}
		finally {
			this.rollback(conn);
			this.close(conn);
		}
		
		return visitType;
	}
	
	/**
	 * Creates and/or updates the visit type eform associations.
	 * 
	 * @param visitType - The current visit type
	 * @param dao - The protocol DAO object
	 * @throws CtdbException	If there is a database error
	 */
	private void createUpdateVisitTypeFormAssociations(Interval visitType, ProtocolManagerDao dao, int studyId) throws CtdbException
	{		
		List<BasicEform> eformsFromDB = dao.getEformsForInterval(visitType.getId());
		List<BasicEform> eformsFromUser = visitType.getIntervalEFormList();
		List<BasicEform> eformsToCreateList = new ArrayList<BasicEform>();
		List<BasicEform> eformsToUpdateList = new ArrayList<BasicEform>();
		List<BasicEform> eformsToRemoveList = new ArrayList<BasicEform>();
		boolean formFound = false;
		
		// Determine which forms to update or create.
		for ( BasicEform userForm : eformsFromUser )
		{
			for ( BasicEform dbForm : eformsFromDB )
			{
				// Check if the changed form is already in the database. If it is add it to the update list
				if ( dbForm.getShortName().equals(userForm.getShortName())  )
				{
					eformsToUpdateList.add(userForm);
					formFound = true;
					break;
				}
			}
			
			// Add the changed form to the create list if it was not found in the database.
			if ( !formFound )
			{
				eformsToCreateList.add(userForm);
			}
			
			formFound = false;
		}
		
		// Determine which forms should be removed
		for ( BasicEform dbForm : eformsFromDB )
		{
			formFound = false;
			
			for ( BasicEform userForm : eformsFromUser )
			{
				// Check if the user chose a form in the database
				if ( dbForm.getShortName().equals(userForm.getShortName()) )
				{
					formFound = true;
					break;
				}
			}
			
			// If the form is not found add it to the delete list
			if ( !formFound )
			{
				eformsToRemoveList.add(dbForm);
			}
		}
		
		// Persist form changes to the database
		if ( !eformsToCreateList.isEmpty() )
		{
			for (BasicEform be : eformsToCreateList) {
				dao.createEform(be,studyId);
				dao.createVistTypeEFormAssociations(visitType.getId(), be);
			}
		}
		
		if ( !eformsToUpdateList.isEmpty() )
		{
			for(BasicEform be:eformsToUpdateList){
				for ( BasicEform dbForm : eformsFromDB ){
					if(be.getShortName().equals(dbForm.getShortName())){
						BasicEform aBasicEformToUpdate = new BasicEform();
						aBasicEformToUpdate.setId(dbForm.getId());
						aBasicEformToUpdate.setOrderValue(be.getOrderValue());
						aBasicEformToUpdate.setIsMandatory(be.getIsMandatory());
						aBasicEformToUpdate.setIsSelfReport(be.getIsSelfReport());
						dao.updateVistTypeEFormAssociations(visitType.getId(), aBasicEformToUpdate);
					}
				
				}
			}
		}
		
		if ( !eformsToRemoveList.isEmpty() )
		{
			dao.deleteVisitTypeEFormAssociations(visitType.getId(), eformsToRemoveList,studyId);
		}
	}

	/**
	 * Updates a visit type in the database.  This will also record the old version of the visit type
	 * if any changes are detected for auditing purposes.
	 *
	 * @param visitType - The visit type to update
	 * @throws ObjectNotFoundException	If the visit type does not exist in the database
	 * @throws DuplicateObjectException	If the visit type already exists in the database
	 * @throws CtdbException	If any other database errors occur while updating the visit type
	 * @throws DuplicateArchiveObjectException	If the version of the visit type already exists in the database
	 */
	public void updateVisitType(Interval visitType, int protocolId)
			throws ObjectNotFoundException, DuplicateObjectException, CtdbException, DuplicateArchiveObjectException {
		Connection conn = null;
		
		try {
			conn = CtdbManager.getConnection(CtdbManager.AUTOCOMMIT_FALSE);
			ProtocolManagerDao dao = ProtocolManagerDao.getInstance(conn);
			boolean createNewVersion = false;
			Interval currVisitType = dao.getInterval(visitType.getId());
			
			// If the visit type changed, create a new version of the visit type
			if (!visitType.equals(currVisitType)) {
				createNewVersion = true;
				dao.versionInterval(visitType.getId());
			}

			updateVistTypePrepopDEAssociation(visitType, conn);
			dao.updateInterval(visitType, createNewVersion);
			createUpdateVisitTypeFormAssociations(visitType, dao, protocolId);
			// The instance with this property to be false, like pdbp and fitbir, will not excute update method
			if (Boolean.valueOf(SysPropUtil.getProperty("display.protocol.clinicalPoint"))) {
				this.updateIntervalClinicalPointAsso(visitType, conn);
			}
			commit(conn);

		}
		finally {
			rollback(conn);
			close(conn);
		}
	}

	public void deleteIntervals(List<Interval> intervalListToBeDeleted) throws CtdbException {
		Connection conn = null;

		try {
			conn = CtdbManager.getConnection(CtdbManager.AUTOCOMMIT_FALSE);
			ProtocolManagerDao dao = ProtocolManagerDao.getInstance(conn);

			for (Interval s : intervalListToBeDeleted) {
				dao.deleteVisitType(s);
			}

			commit(conn);
		}
		finally {
			rollback(conn);
			close(conn);
		}
	}
	
	/**
	 * Deletes visit types (intervals) based on a list of interval IDs.
	 * 
	 * @param visitTypeIds - A list of visit type IDs to be deleted
	 * @param deletedVisitTypes - A list of visit type names that were deleted
	 * @param failedDeletedVisitTypes - A hash table of visit type names that failed to be deleted
	 * @throws CtdbException	If there is an error while connecting to the database
	 */
	public void deleteVisitTypes(List<Integer> visitTypeIds, List<String> deletedVisitTypes, 
			Map<String, List<String>> failedDeletedVisitTypes) throws CtdbException {
		
		Connection conn = null;
		Interval visitType = null;
		
		try {
			conn = getConnection(CtdbManager.AUTOCOMMIT_TRUE);
			ProtocolManagerDao pDao = ProtocolManagerDao.getInstance(conn);
			
			for (int id : visitTypeIds) {
				try {
					visitType = pDao.getInterval(id);
					
					// Delete the visit type and add its name to the deleted list
					pDao.deleteVisitType(visitType);
					deletedVisitTypes.add(visitType.getName());
				}
				catch (InvalidRemovalException ire) {
					logger.error("Could not remove visit type.", ire);
					failedDeletedVisitTypes.get("collect_data_association").add(visitType.getName());
				}
				catch ( CtdbException ce ) {
					logger.error("Database error.", ce);
					
					if (visitType != null) {
						failedDeletedVisitTypes.get("database_error").add(visitType.getName());
					}
					else {
						failedDeletedVisitTypes.get("database_error").add("Visit Type " + id);
					}
				}
			}

		}
		finally {
			close(conn);
		}
	}
	
	public void deleteInterval(Interval interval) throws CtdbException
	{
		Connection conn = null;
		
		try
		{
			conn = CtdbManager.getConnection(CtdbManager.AUTOCOMMIT_TRUE);
			ProtocolManagerDao dao = ProtocolManagerDao.getInstance(conn);
			dao.deleteVisitType(interval);
		}
		finally
		{
			close(conn);
		}
	}

	/**
	 * Retrieves an Interval from the system based on the unique identifier.
	 *
	 * @param intervalId The unique identifier of the Interval to retrieve
	 * @return Interval data object
	 * @throws ObjectNotFoundException if the interval does not exist in the system
	 * @throws CtdbException if any other errors occur while processing
	 */
	public Interval getInterval(int intervalId) throws ObjectNotFoundException, CtdbException
	{
		Connection conn = null;
		try
		{
			conn = CtdbManager.getConnection();
			return ProtocolManagerDao.getInstance(conn).getInterval(intervalId);
		}
		finally
		{
			this.close(conn);
		}
	}

	/**
	 * Retrieves all intervals in the system associated with a protocl
	 *
	 * @return The list of intervals in the system. The list will be empty if no
	 *         intervals exist in the system.
	 * @throws CtdbException if any errors occur while processing
	 */
	public List<Interval> getIntervals(int protocolId) throws CtdbException
	{
		Connection conn = null;
		try
		{
			conn = CtdbManager.getConnection();
			return ProtocolManagerDao.getInstance(conn).getIntervals(protocolId);
		}
		finally
		{
			this.close(conn);
		}
	}

	/**
	 * Retrieves all intervals in the system associated with a patient
	 *
	 * @return The list of intervals in the system. The list will be empty if no
	 *         intervals exist in the system.
	 * @throws CtdbException if any errors occur while processing
	 */
	public List<Interval> getIntervalsByPatieint(int protocolId, int patientId) throws CtdbException {
		Connection conn = null;
		try {
			conn = CtdbManager.getConnection();
			return ProtocolManagerDao.getInstance(conn).getIntervalsByPatient(protocolId, patientId);
		}
		finally {
			this.close(conn);
		}
	}

	/**
	 * Retrieves all intervals in the system associated with a protocol orderby orderval
	 *
	 * @return The list of intervals in the system. The list will be empty if no
	 *         intervals exist in the system.
	 * @throws CtdbException if any errors occur while processing
	 */
	public List<Interval> getIntervalsOrderByOrderval(int protocolId ) throws CtdbException
	{
		Connection conn = null;
		try
		{
			conn = CtdbManager.getConnection();
			return ProtocolManagerDao.getInstance(conn).getIntervals(protocolId, Interval.INTERVAL_ORDER_BY_ORDERVAL );
		}
		finally {
			this.close(conn);
		}
	}

	/**
	 * Retrieves all intervals in the system associated with a protocl
	 *
	 * @return The list of intervals in the system. The list will be empty if no
	 *         intervals exist in the system.
	 * @throws CtdbException if any errors occur while processing
	 */
	public List<Interval> getStudyIntervals(int protocolId) throws CtdbException
	{
		Connection conn = null;
		try
		{
			conn = CtdbManager.getConnection();
			return ProtocolManagerDao.getInstance(conn).getStudyIntervals(protocolId);
		}
		finally
		{
			this.close(conn);
		}
	}
	
	public void getStudyIntervalMap(HashMap<Integer, Interval> map, int protocolId) throws CtdbException
	{
		Connection conn = null;
		ProtocolManagerDao dao = null;
		List<Interval> intervalList = null;
		
		try
		{
			conn = getConnection();
			dao = ProtocolManagerDao.getInstance(conn);
			intervalList = dao.getStudyIntervals(protocolId);
			
			// Populate the map
			for ( Interval i : intervalList )
			{
				map.put(new Integer(i.getId()), i);
			}
		}
		finally
		{
			this.close(conn);
		}
	}

	public List<Interval> getIntervalsForForm(int protocolId, int eformId) throws CtdbException
	{
		Connection conn = null;
		try
		{
			conn = CtdbManager.getConnection();
			return ProtocolManagerDao.getInstance(conn).getIntervalsForForm(protocolId, eformId);
		}
		finally
		{
			this.close(conn);
		}
	}
	
	public List<Form> getFormsForInterval(int intervalId) throws CtdbException
	{
		Connection conn = null;
		try
		{
			conn = CtdbManager.getConnection();
			return ProtocolManagerDao.getInstance(conn).getFormsForInterval(intervalId);
		}
		finally
		{
			this.close(conn);
		}
	}
	
	/**
	 * Method to get BasicEform list for given visit type id
	 * @param intervalId
	 * @return
	 * @throws CtdbException
	 */
	public List<BasicEform> getEformsForInterval(int intervalId) throws CtdbException
	{
		Connection conn = null;
		try
		{
			conn = CtdbManager.getConnection();
			return ProtocolManagerDao.getInstance(conn).getEformsForInterval(intervalId);
		}
		finally
		{
			this.close(conn);
		}
	}
	
	
	
	
	/**
	 * gets all unique eforms that are currently associated to visit types
	 * @param protocolId
	 * @return
	 * @throws CtdbException
	 */
	public ArrayList<BasicEform> getEformsForAllVisitTypes(int protocolId) throws CtdbException
	{
		Connection conn = null;
		try
		{
			conn = CtdbManager.getConnection();
			return ProtocolManagerDao.getInstance(conn).getEformsForAllVisitTypes(protocolId);
		}
		finally
		{
			this.close(conn);
		}
	}
	
	
	

	
	/**
	 * Function to get all the save hidden elememnts for an eform/protocolid
	 * @param protocolId
	 * @param eformId
	 * @return
	 * @throws CtdbException
	 */
	public ArrayList<String> getPSRHiddenElements(int protocolId, int eformId) throws CtdbException
	{
		Connection conn = null;
		try
		{
			conn = CtdbManager.getConnection();
			return ProtocolManagerDao.getInstance(conn).getPSRHiddenElements(protocolId, eformId);
		}
		finally
		{
			this.close(conn);
		}
	}
	
	/**
	 * Function that saves all the hidden elements for an eform/protocolid
	 * @param protocolid
	 * @param eformid
	 * @param sectionQuestionIds
	 * @throws CtdbException
	 */
	public void updatePSRHiddenElements(int protocolid, int eformid, List<String> sectionQuestionIds, User user) throws CtdbException
	{
		Connection conn = null;
		try
		{
			conn = CtdbManager.getConnection();
			ProtocolManagerDao.getInstance(conn).updatePSRHiddenElements(protocolid, eformid, sectionQuestionIds, user);
		}
		finally
		{
			this.close(conn);
		}
	}
	
	/**
	 * Gets the audit trail for Configuring Eform
	 * @param protocolId
	 * @param eformId
	 * @return
	 * @throws CtdbException
	 */
	public List<ConfigureEformAuditDetail> getConfigureEformAudit(int protocolId, int eformId) throws CtdbException {
		Connection conn = null;
        try {
            conn = CtdbManager.getConnection();
            return ProtocolManagerDao.getInstance(conn).getConfigureEformAudit(protocolId, eformId);
        }
        finally {
            this.close(conn);
        }
	}
	
	
	
	public Map<Integer,String> getProtocolIds() throws CtdbException
	{
		Connection conn = null;
		try
		{
			conn = CtdbManager.getConnection();
			return ProtocolManagerDao.getInstance(conn).getProtocolIds();
		}
		finally
		{
			this.close(conn);
		}
	}
	
	public List<Form> getActiveFormsForInterval(int intervalId) throws CtdbException
	{
		Connection conn = null;
		try
		{
			conn = CtdbManager.getConnection();
			return ProtocolManagerDao.getInstance(conn).getActiveFormsForInterval(intervalId);
		}
		finally
		{
			this.close(conn);
		}
	}
	
	public void updateIntervalOrder (String[] intervals) throws CtdbException {
		Connection conn = null;
		
		try {
			conn = CtdbManager.getConnection(CtdbManager.AUTOCOMMIT_FALSE);
			ProtocolManagerDao dao = ProtocolManagerDao.getInstance(conn);
			
			dao.updateIntervalOrder(intervals);
			this.commit(conn);
		}
		finally {
			this.rollback(conn);
			this.close(conn);
		}
	}
	
	/**
	 * Retrieves the Protocol associated with an interval
	 *
	 * @param intervalname Name of the interval to retrieve
	 * @return  Protocol domain object.
	 * @throws  CtdbException if any errors occur while processing
	 */
	public Protocol getIntervalProtocol(String intervalname) throws CtdbException
	{
		Connection conn = null;
		try
		{
			conn = CtdbManager.getConnection();
			return ProtocolManagerDao.getInstance(conn).getIntervalProtocol(intervalname);
		}
		finally
		{
			this.close(conn);
		}
	}

	/**
	 * Retrieves all versions of a interval stored. A version consists
	 * of modifications to the metadata about a interval.
	 *
	 * @param intervalId The unique identifier of the Interval to retrieve
	 * @return An list of all versions for a single interval. The array
	 *         will ordered by versions such that index 0 will be the first
	 *         interval version. If the interval does not exist an empty
	 *         array will be returned.
	 * @throws CtdbException if any errors occur
	 */
	public List<Interval> getIntervalVersions(int intervalId) throws CtdbException
	{
		Connection conn = null;
		try
		{
			conn = CtdbManager.getConnection();
			return ProtocolManagerDao.getInstance(conn).getIntervalVersions(intervalId);
		}
		finally
		{
			this.close(conn);
		}
	}

	public List<String> getPrepopDENamesForInterval(int intervalId) throws CtdbException {
		Connection conn = null;
		try
		{
			conn = CtdbManager.getConnection();
			return ProtocolManagerDao.getInstance(conn).getPrepopDENamesForInterval(intervalId);
		}
		finally
		{
			this.close(conn);
		}
	}
	
	public List<String> getAllPrepopDENames() throws CtdbException {
		Connection conn = null;
		try
		{
			conn = CtdbManager.getConnection();
			return ProtocolManagerDao.getInstance(conn).getAllPrepopDENames();
		}
		finally
		{
			this.close(conn);
		}
	}
	
	/**
	 * Checks the database for a PrepopDataElement, which corresponds to the given data element short name.
	 * 
	 * @param deShortName - The data element short name to query for.
	 * @return The PrepopDataElement object that is associated with the given data element short name, or null if no
	 * 		   correlation exists.
	 * @throws CtdbException When there is a database error while the pre-population data element lookup is in progress.
	 */
	public PrepopDataElement getPrepopDEByShortName(String deShortName) throws CtdbException {
		PrepopDataElement prePopDe = null;
		
		if ( !Utils.isBlank(deShortName) ) {
			Connection conn = CtdbManager.getConnection();
			
			try	{
				prePopDe = ProtocolManagerDao.getInstance(conn).getPrepopDEByShortName(deShortName);
			}
			finally {
				this.close(conn);
			}
		}
		
		return prePopDe;
	}
	
	/**
	 * Queries the database for pre-population data element objects that corresponds to the given list of data element short
	 * names.
	 * 
	 * @param deShortNameList - A list of data element short names used to find data elements that support pre-population values.
	 * @return A list of PrepopDataElement which are associated with the given list of data element short names.
	 * @throws CtdbException When there is a database error while querying for PrepopDataElement objects.
	 */
	public List<PrepopDataElement> getPrepopDEByShortNameList(List<String> deShortNameList) throws CtdbException {
		List<PrepopDataElement> prePopDeList = null;
		
		if ( (deShortNameList != null) && !deShortNameList.isEmpty() ) {
			Connection conn = CtdbManager.getConnection();
			
			try {
				prePopDeList = ProtocolManagerDao.getInstance(conn).getPrepopDEByShortNameList(deShortNameList);
			}
			finally {
				this.close(conn);
			}
		}
		else {
			prePopDeList = new ArrayList<PrepopDataElement>();
		}
		
		return prePopDeList;
	}

	private void updateVistTypePrepopDEAssociation(Interval visitType, Connection conn) throws CtdbException {
		logger.info("updateVistTypePrepopDEAssociation: ");
		List<PrepopDataElement> prepopDEList = visitType.getPrepopulateDEList();
		List<String> vtPrepopDENameList = new ArrayList<String>();

		for (PrepopDataElement prepopDE : prepopDEList) {
			vtPrepopDENameList.add(prepopDE.getShortName());
		}

		ProtocolManagerDao dao = ProtocolManagerDao.getInstance(conn);
		int visitTypeId = visitType.getId();
		List<String> existdeNameList = dao.getPrepopDENamesForInterval(visitTypeId);

		for (String existDE : existdeNameList) {
			if (!vtPrepopDENameList.contains(existDE)) {
				dao.deleteVistTypePrepopDEAssociation(visitTypeId, existDE);
			}
		}

		for (String currentDE : vtPrepopDENameList) {
			if (!existdeNameList.contains(currentDE)) {
				dao.createVistTypePrepopDEAssociation(visitTypeId, currentDE);
			}
		}
	}

	/**
	 * Gets a list of PrepopDataElement objects from the database for the specified visit type (interval) ID. The list of
	 * objects returned from the database can be conditionally filtered by the second boolean argument. A value of
	 * "true" will cause this method to return <u>all</u> PrepopDataElement objects that the system has for the given
	 * visit type ID. A value of "false" will only return PrepopDataElement objects that allow a user to define its
	 * pre-population value (i.e. Age in Years, etc.). The returned list will also be ordered by the data element title
	 * (the "elementtitle" column of the "prepopdataelement" table).
	 * 
	 * @param intervalId - The interval or visit type ID used to restrict the query for PrepopDataElement objects.
	 * @param completeList - A boolean flag used to indicate whether or not to return all available PrepopDataElement
	 *        objects for the given visit type ID.
	 * @return All PrepopDataElement objects found in the system for the given visit type ID. The listed items may have
	 *         been filtered according to the supplied boolean flag.
	 * @throws CtdbException If there was a database error while getting the list of PrepopDataElement objects.
	 */
	public List<PrepopDataElement> getPrepopDEsForInterval(int intervalId, boolean completeList) throws CtdbException {
		Connection conn = null;
		try
		{
			conn = CtdbManager.getConnection();
			return ProtocolManagerDao.getInstance(conn).getPrepopDEsForInterval(intervalId, completeList);
		}
		finally
		{
			this.close(conn);
		}
	}
	
	/**
	 * Gets a list of available pre-population data elements in the system. If "true" is given for the parameter, then
	 * the list will only contain pre-population data elements whose pre-population value can be defined by a user.
	 * Otherwise, all pre-population data elements in the system will be returned.
	 * 
	 * @param userDefOnly - Used to indicate whether or not to restrict the listed PrepopDataElement object to only ones
	 *        that can be defined by the user.
	 * @return A list of PrepopDataElement objects that are found in the system, and is optionally filtered by the
	 *         passed in boolean flag.
	 * @throws CtdbException When there is a database error while retrieving the PrepopDataElement objects.
	 */
	public List<PrepopDataElement> getAllPrepopDEs(boolean userDefOnly) throws CtdbException {
		List<PrepopDataElement> prePopDeList = null;
		Connection conn = null;
		
		try {
			conn = CtdbManager.getConnection();
			prePopDeList = ProtocolManagerDao.getInstance(conn).getAllPrepopDEs(userDefOnly);
		}
		finally {
			this.close(conn);
		}
		
		return prePopDeList;
	}
	
	public List<PatientVisitPrepopValue> getPvPrepopValuesForInterval(int intervalId, int visitdateId) throws CtdbException {
		Connection conn = null;
		try
		{
			conn = CtdbManager.getConnection();
			return ProtocolManagerDao.getInstance(conn).getPvPrepopValuesForInterval(intervalId, visitdateId);
		}
		finally
		{
			this.close(conn);
		}
	}
	
	public List<ClinicalLocation> getProtocolClinicalLocs(int protocolId) throws CtdbException {
		List<ClinicalLocation> protoClinicalLocList = null;
		Connection conn = null;
		
		try{
			conn = CtdbManager.getConnection();
			protoClinicalLocList = ProtocolManagerDao.getInstance(conn).getProtocolClinicalLocs(protocolId);
		} finally {
			this.close(conn);
		}
		
		return protoClinicalLocList;
		
	}
	
	public List<Procedure> getProtocolProcdeure(int protocolId) throws CtdbException {
		List<Procedure> procedureList = null;
		Connection conn = null;
		
		try{
			conn = CtdbManager.getConnection();
			procedureList = ProtocolManagerDao.getInstance(conn).getProtocolProcedures(protocolId);
		} finally {
			this.close(conn);
		}
		return procedureList;
	}
	
	public List<PointOfContact> getProtocolPointOfContact(int protocolId) throws CtdbException {
		List<PointOfContact> pocList = null;
		Connection conn = null;
		
		try {
			conn = CtdbManager.getConnection();
			pocList = ProtocolManagerDao.getInstance(conn).getProtocolPointOfContacts(protocolId);
		} finally {
			this.close(conn);
		}
		return pocList;
	}
	
	public ClinicalLocation getClinicalLocationById(int clinicLocId) throws CtdbException {
		ClinicalLocation clinicLoc = null;
		Connection conn = null;
		
		try{
			conn = CtdbManager.getConnection();
			clinicLoc = ProtocolManagerDao.getInstance(conn).getClinicalLocationById(clinicLocId);
		} finally {
			this.close(conn);
		}
		
		return clinicLoc;		
	}
	
	public Procedure getProcedureById(int procedureId) throws CtdbException {
		Procedure proc = null;
		Connection conn = null;
		
		try{
			conn = CtdbManager.getConnection();
			proc = ProtocolManagerDao.getInstance(conn).getProcedureById(procedureId);
		} finally {
			this.close(conn);
		}
		
		return proc;		
	}
	
	public PointOfContact getPointOfContactById(int pocId) throws CtdbException {
		PointOfContact poc = null;
		Connection conn = null;
		
		try{
			conn = CtdbManager.getConnection();
			poc = ProtocolManagerDao.getInstance(conn).getPointOfContactById(pocId);
		} finally {
			this.close(conn);
		}
		
		return poc;		
	}
	
	public List<IntervalClinicalPoint> getIntervalClinicalPntsForInterval(int intervalId) throws CtdbException {
		Connection conn = null;
		try
		{
			conn = CtdbManager.getConnection();
			return ProtocolManagerDao.getInstance(conn).getIntervalClinicalPntsForInterval(intervalId);
		}
		finally
		{
			this.close(conn);
		}
	}
	
	public void createVisitTypeClinicalPointAsso(List<IntervalClinicalPoint> intervalCPToCreateList, int visitTypeId) throws CtdbException{
		Connection conn = null;

		try	{
			conn = CtdbManager.getConnection(CtdbManager.AUTOCOMMIT_FALSE);
			ProtocolManagerDao dao = ProtocolManagerDao.getInstance(conn);
			
			dao.createVisitTypeClinicalPointAsso(intervalCPToCreateList, visitTypeId);
			this.commit(conn);
		} finally {
			this.rollback(conn);
			this.close(conn);
		}
	}
	
	private void updateIntervalClinicalPointAsso(Interval visitType, Connection conn) throws CtdbException {
		ProtocolManagerDao dao = ProtocolManagerDao.getInstance(conn);
		List<IntervalClinicalPoint> intervalCPToCreateList = new ArrayList<IntervalClinicalPoint>();
		List<Integer> intervalCPIdToKeepList = new ArrayList<Integer>();
		int visitTypeId = visitType.getId();

		for (IntervalClinicalPoint icp : visitType.getIntervalClinicalPointList()) {
			if (icp.getId() <= 0 || icp.getStatus().equals("added")) {
				intervalCPToCreateList.add(icp);
			} else if (icp.getStatus().equals("edited")) {
				dao.updateIntervalClinicalPointAsso(icp, visitTypeId);
				intervalCPIdToKeepList.add(icp.getId());
			} else {
				intervalCPIdToKeepList.add(icp.getId());
			}
		}

		dao.deleteVisitTypeClinicalPointAsso(intervalCPIdToKeepList, visitTypeId);
		dao.createVisitTypeClinicalPointAsso(intervalCPToCreateList, visitTypeId);
	}
	
	public void deleteVisitTypeClinicalPointAsso(List<Integer> intervalCPIdToKeepList, int visitTypeId) throws CtdbException{
		Connection conn = null;
		try	{
			conn = CtdbManager.getConnection(CtdbManager.AUTOCOMMIT_FALSE);
			ProtocolManagerDao dao = ProtocolManagerDao.getInstance(conn);
			dao.deleteVisitTypeClinicalPointAsso(intervalCPIdToKeepList, visitTypeId);
			this.commit(conn);
		} finally {
			this.rollback(conn);
			this.close(conn);
		}
	}
	
	public List<Procedure> getAllProcdeureList() throws CtdbException {
		List<Procedure> procedureList = null;
		Connection conn = null;
		
		try{
			conn = CtdbManager.getConnection();
			procedureList = ProtocolManagerDao.getInstance(conn).getAllProcedureList();
		} finally {
			this.close(conn);
		}

		return procedureList;
	}
	
	private void updateProtocolClinicalLocationAsso(int protocolId, List<ClinicalLocation> protoClinicalLocationList,
			Connection conn)
			throws CtdbException {
		ProtocolManagerDao dao = ProtocolManagerDao.getInstance(conn);
		List<ClinicalLocation> clinicalLocToCreateList = new ArrayList<ClinicalLocation>();
		List<Integer> clinicalLocIdToKeepList = new ArrayList<Integer>();

		for (ClinicalLocation cl : protoClinicalLocationList) {
			if (cl.getStatus().equals("added") || cl.getId() < 0) {
				clinicalLocToCreateList.add(cl);
			} else if (cl.getStatus().equals("edited")) {
				dao.updateProtocolClinicalLocationAsso(cl);
				clinicalLocIdToKeepList.add(cl.getId());
			} else {
				clinicalLocIdToKeepList.add(cl.getId());
			}
		}

		dao.deleteProtocolClinicalLocationAsso(clinicalLocIdToKeepList, protocolId);
		dao.createProtocolClinicalLocationAsso(protocolId, clinicalLocToCreateList);
	}

	public void deleteProtocolClinicalLocationAsso(List<Integer> clinicalLocIdToKeepList, int protocolId) throws CtdbException{
		Connection conn = null;
		try	{
			conn = CtdbManager.getConnection(CtdbManager.AUTOCOMMIT_FALSE);
			ProtocolManagerDao dao = ProtocolManagerDao.getInstance(conn);

			dao.deleteProtocolClinicalLocationAsso(clinicalLocIdToKeepList, protocolId);
			this.commit(conn);
		} finally {
			this.rollback(conn);
			this.close(conn);
		}
	}

	public void createProtocolClinicalLocationAsso(int protocolId, List<ClinicalLocation> clinicalLocToCreateList) throws CtdbException{
		Connection conn = null;

		try	{
			conn = CtdbManager.getConnection(CtdbManager.AUTOCOMMIT_FALSE);
			ProtocolManagerDao dao = ProtocolManagerDao.getInstance(conn);
			dao.createProtocolClinicalLocationAsso(protocolId, clinicalLocToCreateList);
			this.commit(conn);
		} finally {
			this.rollback(conn);
			this.close(conn);
		}
	}
	
	public void updateProtocolProcedureAsso(int protocolId, List<Procedure> protoProcedureList) throws CtdbException {
		Connection conn = null;

		try {
			conn = CtdbManager.getConnection(CtdbManager.AUTOCOMMIT_FALSE);
			ProtocolManagerDao dao = ProtocolManagerDao.getInstance(conn);

			dao.deleteProtocolProcedureAsso(protocolId);
			dao.createProtocolProcedureAsso(protocolId, protoProcedureList);

			this.commit(conn);
		} finally {
			this.rollback(conn);
			this.close(conn);
		}
	}
	
	public void deleteProtocolProcedureAsso(int protocolId) throws CtdbException{
		Connection conn = null;

		try	{
			conn = CtdbManager.getConnection(CtdbManager.AUTOCOMMIT_FALSE);
			ProtocolManagerDao dao = ProtocolManagerDao.getInstance(conn);
			dao.deleteProtocolProcedureAsso(protocolId);
			this.commit(conn);
		} finally {
			this.rollback(conn);
			this.close(conn);
		}
	}
	public void createProtocolProcedureAsso(int protocolId, List<Procedure> protoProcedureList) throws CtdbException{
		Connection conn = null;

		try	{
			conn = CtdbManager.getConnection(CtdbManager.AUTOCOMMIT_FALSE);
			ProtocolManagerDao dao = ProtocolManagerDao.getInstance(conn);
			dao.createProtocolProcedureAsso(protocolId, protoProcedureList);
			this.commit(conn);
		} finally {
			this.rollback(conn);
			this.close(conn);
		}
	}
	
	private void updateProtocolPointOfContatctAsso(int protocolId, List<PointOfContact> protoPOCList, Connection conn)
			throws CtdbException {
		ProtocolManagerDao dao = ProtocolManagerDao.getInstance(conn);
		List<PointOfContact> pocToCreateList = new ArrayList<PointOfContact>();
		List<Integer> pocIdToKeepList = new ArrayList<Integer>();

		for (PointOfContact poc : protoPOCList) {
			if (poc.getStatus().equals("added") || poc.getId() < 0) {
				pocToCreateList.add(poc);
			} else if (poc.getStatus().equals("edited")) {
				dao.updateProtocolPointOfContactAsso(poc);
				pocIdToKeepList.add(poc.getId());
			} else {
				pocIdToKeepList.add(poc.getId());
			}
		}

		dao.deleteProtocolPointOfContactAsso(pocIdToKeepList, protocolId);
		dao.createProtocolPointOfContactAsso(protocolId, pocToCreateList);
	}
	
	public void deleteProtocolPointOfContactAsso(List<Integer> pocIdToKeepList, int protocolId) throws CtdbException{
		Connection conn = null;

		try	{
			conn = CtdbManager.getConnection(CtdbManager.AUTOCOMMIT_FALSE);
			ProtocolManagerDao dao = ProtocolManagerDao.getInstance(conn);
			dao.deleteProtocolPointOfContactAsso(pocIdToKeepList, protocolId);
			this.commit(conn);
		} finally {
			this.rollback(conn);
			this.close(conn);
		}
	}
	public void createProtocolPointOfContactAsso(int protocolId, List<PointOfContact> pocToCreateList) throws CtdbException{
		Connection conn = null;

		try	{
			conn = CtdbManager.getConnection(CtdbManager.AUTOCOMMIT_FALSE);
			ProtocolManagerDao dao = ProtocolManagerDao.getInstance(conn);
			dao.createProtocolPointOfContactAsso(protocolId, pocToCreateList);
			this.commit(conn);
		} finally {
			this.rollback(conn);
			this.close(conn);
		}
	}
	
	public int createProcedure(Procedure protoProcedure) throws CtdbException{
		Connection conn = null;

		try	{
			conn = CtdbManager.getConnection(CtdbManager.AUTOCOMMIT_FALSE);
			ProtocolManagerDao dao = ProtocolManagerDao.getInstance(conn);
			dao.createProcedure(protoProcedure);
			this.commit(conn);
		} finally {
			this.rollback(conn);
			this.close(conn);
		}

		return protoProcedure.getId();
	}
	
	public void updateProtocolMilesStoneAsso(int protocolId, List<MilesStone> protoMilesStoneList)
			throws CtdbException {
		Connection conn = null;

		try {
			conn = CtdbManager.getConnection(CtdbManager.AUTOCOMMIT_FALSE);
			ProtocolManagerDao dao = ProtocolManagerDao.getInstance(conn);

			dao.deleteProtocolMilesStoneAsso(protocolId);
			dao.createProtocolMilesStoneAsso(protocolId, protoMilesStoneList);

			this.commit(conn);
		} finally {
			this.rollback(conn);
			this.close(conn);
		}
	}
	
	public void deleteProtocolMilesStoneAsso(int protocolId) throws CtdbException{
		Connection conn = null;

		try	{
			conn = CtdbManager.getConnection(CtdbManager.AUTOCOMMIT_FALSE);
			ProtocolManagerDao dao = ProtocolManagerDao.getInstance(conn);
			dao.deleteProtocolMilesStoneAsso(protocolId);
			this.commit(conn);
		} finally {
			this.rollback(conn);
			this.close(conn);
		}
	}
	public void createProtocolMilesStoneAsso(int protocolId, List<MilesStone> protoMilesStoneList) throws CtdbException{
		Connection conn = null;

		try	{
			conn = CtdbManager.getConnection(CtdbManager.AUTOCOMMIT_FALSE);
			ProtocolManagerDao dao = ProtocolManagerDao.getInstance(conn);
			dao.createProtocolMilesStoneAsso(protocolId, protoMilesStoneList);
			this.commit(conn);
		} finally {
			this.rollback(conn);
			this.close(conn);
		}
	}
	
	public List<MilesStone> getProtocolMilesStone(int protocolId) throws CtdbException {
		List<MilesStone> protoMilesStoneList = null;
		Connection conn = null;
		
		try{
			conn = CtdbManager.getConnection();
			protoMilesStoneList = ProtocolManagerDao.getInstance(conn).getProtocolMilesStone(protocolId);
		} finally {
			this.close(conn);
		}
		
		return protoMilesStoneList;
		
	}
	
	public List<ClinicalLocation> getAllClinicalLocs() throws CtdbException {
		List<ClinicalLocation> protoClinicalLocList = null;
		Connection conn = null;
		
		try{
			conn = CtdbManager.getConnection();
			protoClinicalLocList = ProtocolManagerDao.getInstance(conn).getAllClinicalLocs();
		} finally {
			this.close(conn);
		}
		
		return protoClinicalLocList;
		
	}
	
	public Interval getIntervalByClinicalPnt(int clinicalPntId) throws CtdbException {
		Interval interval = null;
		Connection conn = null;
		
		try{
			conn = CtdbManager.getConnection();
			ProtocolManagerDao dao = ProtocolManagerDao.getInstance(conn);
			interval = dao.getIntervalByClinicalPnt(clinicalPntId);
		} finally {
			this.close(conn);
		}
		
		return interval;
		
	}
	
	public List<ProtocolClosingOut> getClosingOutListByStudyId(String studyId) throws CtdbException {
		List<ProtocolClosingOut> closingOutList = null;
		Connection conn = null;
		
		try{
			conn = CtdbManager.getConnection();
			ProtocolManagerDao dao = ProtocolManagerDao.getInstance(conn);
			closingOutList = dao.getClosingOutListByStudyId(studyId);
		} finally {
			this.close(conn);
		}
		return closingOutList;
	}
	
	public void saveProtocolClosingout(ProtocolClosingOut pco) throws CtdbException {
		Connection conn = null;
		
		try{
			conn = CtdbManager.getConnection(CtdbManager.AUTOCOMMIT_FALSE);
			ProtocolManagerDao dao = ProtocolManagerDao.getInstance(conn);
			dao.saveProtocolClosingout(pco);
			this.commit(conn);
		} finally {
			this.rollback(conn);
			this.close(conn);
		}
	}
	
	public boolean checkProtocolClosed(int protocolId) throws CtdbException {
		Connection conn = null;
		boolean closedout = false;
		
		try{
			conn = CtdbManager.getConnection();
			ProtocolManagerDao dao = ProtocolManagerDao.getInstance(conn);
			closedout = dao.checkProtocolClosed(protocolId);
		} finally {
			this.close(conn);
		}
		
		return closedout;
	}
	
}
