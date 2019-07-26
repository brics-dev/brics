package gov.nih.nichd.ctdb.drugDevice.manager;

import gov.nih.nichd.ctdb.common.CtdbException;
import gov.nih.nichd.ctdb.common.CtdbManager;
import gov.nih.nichd.ctdb.common.DuplicateObjectException;
import gov.nih.nichd.ctdb.common.ObjectNotFoundException;
import gov.nih.nichd.ctdb.drugDevice.dao.DrugDeviceDao;
import gov.nih.nichd.ctdb.drugDevice.domain.DrugDevice;

import java.sql.Connection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * Developed by CIT.
 * @author Shashi Rudrappa
 * Date: May 01, 2012
 * @version 1.0
 */

public class DrugDeviceManager extends CtdbManager
{
	/**
	 * Creates or updates drug devices for a target study
	 * 
	 * @param drugDeviceHashMap - A map of drug devices associated with the target study
	 * @param ProtocolId - The ID of the associated study
	 * @param conn - The database connection used to persist any changes
	 * @throws ObjectNotFoundException		Could not find an object during creation or update
	 * @throws DuplicateObjectException		A duplicate entry was attempted during site creation
	 * @throws CtdbException	If there are any failures with adding or updating the study's drug devices
	 */
	public void createUpdateDrugDevices (Map<String, DrugDevice> drugDeviceHashMap, int ProtocolId, Connection conn) throws ObjectNotFoundException, DuplicateObjectException, CtdbException
	{
		Set<Entry<String, DrugDevice>> setDrugDevice = drugDeviceHashMap.entrySet();
		DrugDeviceDao dao = DrugDeviceDao.getInstance(conn);
		Entry<String, DrugDevice> elem = null;
		DrugDevice dd = null;
		
		for ( Iterator<Entry<String,DrugDevice>> it = setDrugDevice.iterator(); it.hasNext(); )
		{
			elem = it.next();
			dd = elem.getValue();
			System.out.println("Drug Device Manager================== " + elem.getKey());
			
			if ( dd.getDrugDeviceActionFlag() != null )
			{
				if ( dd.getDrugDeviceActionFlag().equalsIgnoreCase("add_drugDevice") )
				{
					dd.setProtocolId(ProtocolId); // This negative while adding a new protocol
					dao.createDrugDevice(dd);
				}
				else if ( dd.getDrugDeviceActionFlag().equalsIgnoreCase("edit_drugDevice") )
				{
					dao.updateDrugDevice(dd);
				}
			}
		}
	}
	
	public void createDrugDevices (List<DrugDevice> drugDeviceList) throws ObjectNotFoundException, DuplicateObjectException, CtdbException
	{
		Connection conn = null;
		
		try
		{
			conn = CtdbManager.getConnection(CtdbManager.AUTOCOMMIT_TRUE);
			DrugDeviceDao dao = DrugDeviceDao.getInstance(conn);
			
			for ( DrugDevice d : drugDeviceList )
			{
				dao.createDrugDevice(d);
			}
		}
		finally
		{
			close(conn);
		}
	}

	public void updateDrugDevices (List<DrugDevice> drugDeviceList)  throws ObjectNotFoundException, DuplicateObjectException, CtdbException
	{
		Connection conn = null;
		
		try
		{
			conn = CtdbManager.getConnection(CtdbManager.AUTOCOMMIT_TRUE);
			DrugDeviceDao dao = DrugDeviceDao.getInstance(conn);
			
			for ( DrugDevice d : drugDeviceList )
			{
				dao.updateDrugDevice(d);
			}
		}
		finally
		{
			close(conn);
		}
	}    
	
	public void createDrugDevice (DrugDevice d) throws CtdbException
	{
		Connection conn = null;
		
		try
		{
			conn = CtdbManager.getConnection(CtdbManager.AUTOCOMMIT_TRUE);
			DrugDeviceDao dao = DrugDeviceDao.getInstance(conn);
			dao.createDrugDevice(d);
		}
		finally
		{
			close(conn);
		}
	}

	public void updateDrugDevice (DrugDevice d) throws CtdbException
	{
		Connection conn = null;
		
		try
		{
			conn = CtdbManager.getConnection(CtdbManager.AUTOCOMMIT_TRUE);
			DrugDeviceDao dao = DrugDeviceDao.getInstance(conn);
			dao.updateDrugDevice(d);
		}
		finally
		{
			close(conn);
		}
	}    

	public void deleteDrugDevices (int[] selectedDrugDeviceIds) throws CtdbException
	{
		Connection conn = null;
		
		try
		{
			conn = CtdbManager.getConnection(CtdbManager.AUTOCOMMIT_TRUE);
			DrugDeviceDao dao = DrugDeviceDao.getInstance(conn);

			for ( int i = 0; i < selectedDrugDeviceIds.length; i++ )
			{
				int selectedDrugDeviceId =  selectedDrugDeviceIds[i];
				dao.deleteDrugDevice(selectedDrugDeviceId);
				System.out.println("selectedDrugDeviceId[i]------------------------------"+selectedDrugDeviceId);
			}
		}
		finally
		{
			close(conn);
		}
	}

	public void deleteDrugDevices (List<DrugDevice> drugDeviceList) throws CtdbException
	{
		Connection conn = null;
		
		try
		{
			conn = CtdbManager.getConnection(CtdbManager.AUTOCOMMIT_TRUE);
			DrugDeviceDao dao = DrugDeviceDao.getInstance(conn);
			
			for ( DrugDevice d : drugDeviceList )
			{
				dao.deleteDrugDevice(d.getId());
			}
		}
		finally
		{
			close(conn);
		}
	}
	
	public DrugDevice getDrugDevice (int ddId) throws CtdbException
	{
		Connection conn = null;
		
		try
		{
			conn = CtdbManager.getConnection();
			DrugDeviceDao dao = DrugDeviceDao.getInstance(conn);
			DrugDevice d = dao.getDrugDevice(ddId);
			
			return d;
		}
		finally
		{
			close(conn);
		}
	}

	public List<DrugDevice> getDrugDevices (int protocolId) throws CtdbException
	{
		Connection conn = null;
		
		try
		{
			conn = CtdbManager.getConnection();
			DrugDeviceDao dao = DrugDeviceDao.getInstance(conn);
			List<DrugDevice> drugdevices = dao.getDrugDevices(protocolId);

			return drugdevices;
		}
		finally
		{
			close(conn);
		}
	}
}
