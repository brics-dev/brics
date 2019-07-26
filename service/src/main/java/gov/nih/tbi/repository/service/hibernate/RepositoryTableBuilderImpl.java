package gov.nih.tbi.repository.service.hibernate;

import java.sql.SQLException;
import java.util.Formatter;
import java.util.HashMap;
import java.util.Map;

import javax.mail.MessagingException;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import gov.nih.tbi.CoreConstants;
import gov.nih.tbi.account.model.hibernate.Account;
import gov.nih.tbi.commons.model.DataType;
import gov.nih.tbi.commons.model.InputRestrictions;
import gov.nih.tbi.commons.model.RoleType;
import gov.nih.tbi.commons.service.AccountManager;
import gov.nih.tbi.commons.service.UserPermissionException;
import gov.nih.tbi.commons.service.util.MailEngine;
import gov.nih.tbi.commons.util.DaoUtils;
import gov.nih.tbi.commons.util.PostgreUtils;
import gov.nih.tbi.dictionary.model.hibernate.MapElement;
import gov.nih.tbi.dictionary.model.hibernate.RepeatableGroup;
import gov.nih.tbi.dictionary.model.hibernate.StructuralDataElement;
import gov.nih.tbi.dictionary.model.hibernate.StructuralFormStructure;
import gov.nih.tbi.dictionary.model.hibernate.ValueRange;
import gov.nih.tbi.repository.dao.DataStoreDao;
import gov.nih.tbi.repository.dao.DataStoreInfoDao;
import gov.nih.tbi.repository.dao.DataStoreTabularColumnInfoDao;
import gov.nih.tbi.repository.dao.DataStoreTabularInfoDao;
import gov.nih.tbi.repository.model.hibernate.DataStoreInfo;
import gov.nih.tbi.repository.model.hibernate.DataStoreTabularColumnInfo;
import gov.nih.tbi.repository.model.hibernate.DataStoreTabularInfo;
import gov.nih.tbi.repository.service.RepositoryTableBuilder;

/**
 * This is the hibernate implementation of RepositoryTableBuilder
 * 
 * @author Francis Chen
 */
@Service
@Scope("singleton")
public class RepositoryTableBuilderImpl implements RepositoryTableBuilder {
	private static final Logger logger = Logger.getLogger(RepositoryTableBuilderImpl.class);
	private static final long serialVersionUID = -667176050830751453L;

	@Autowired
	private AccountManager accountManager;

	@Autowired
	private DataStoreDao dataStoreDao;

	@Autowired
	private DataStoreInfoDao dataStoreInfoDao;

	@Autowired
	private DataStoreTabularInfoDao dataStoreTabularInfoDao;

	@Autowired
	private DataStoreTabularColumnInfoDao dataStoreTabularColumnInfoDao;

	@Autowired
	private MailEngine mailEngine;

	/**
	 * {@inheritDoc}
	 */
	public boolean createRepositoryStore(StructuralFormStructure dataStructure, String errorToEmail, Account account)
			throws SQLException, UserPermissionException {

		boolean success = true;

		if (!accountManager.hasRole(account, RoleType.ROLE_DICTIONARY_ADMIN)) {
			throw new UserPermissionException("Only Users with Admin permission can publish Form Structure!");
		}

		try {
			DataStoreInfo storeInfo = dataStoreInfoDao.getByDataStructureId(dataStructure.getId());
			// Create and save metadata
			if (storeInfo == null) {
				storeInfo = dataStoreInfoDao.save(new DataStoreInfo(dataStructure.getId(), true, false));
			}

			// We need to create a table for each repeatable group
			for (RepeatableGroup repeatableGroup : dataStructure.getRepeatableGroups()) {
				createRepositoryTable(storeInfo, dataStructure, repeatableGroup);
			}
		} catch (Exception e) {
			// If there is an error during this process, we want to just alert the admins that there
			// was a problem
			e.printStackTrace();
			String htmlMessage = e.getMessage();

			try {
				mailEngine.sendMail("Error: Unable to publish Form Structure to repository database", htmlMessage,
						"error", errorToEmail);
			} catch (MessagingException e1) {
				logger.error("Could not send email for createTableFromDataStructure ERROR!");
			}
		}

		return success;
	}

	private void createRepositoryTable(DataStoreInfo storeInfo, StructuralFormStructure dataStructure,
			RepeatableGroup repeatableGroup) throws SQLException {
		String tableName = getStoreName(dataStructure.getShortName(), repeatableGroup);

		DataStoreTabularInfo tableInfo = dataStoreTabularInfoDao.get(storeInfo, repeatableGroup);
		// Create and save metadata
		if (tableInfo == null) {
			tableInfo = dataStoreTabularInfoDao
					.save(new DataStoreTabularInfo(storeInfo, repeatableGroup.getId(), tableName));
		}

		// Create a column for each element in the repeatable group
		Map<String, String> columns = new HashMap<String, String>(); // element name, column
																	 // type
		for (MapElement mapElement : repeatableGroup.getMapElements()) {
			String columnName = generateColumnName(mapElement);
			String columnType = generateColumnType(mapElement);

			columns.put(columnName, columnType);

			// Create and save metadata
			dataStoreTabularColumnInfoDao
					.save(new DataStoreTabularColumnInfo(tableInfo, mapElement.getId(), columnName, columnType));
		}

		try {
			dataStoreDao.createSequence(tableName + CoreConstants.SEQUENCE_SUFFIX);
			dataStoreDao.createTable(tableName, columns);
		} catch (SQLException e) {
			// throw this shizzle up
			throw e;
		}
	}

	private String generateColumnName(MapElement mapElement) {
		String columnName = mapElement.getStructuralDataElement().getName().toLowerCase();
		return columnName;
	}

	private String generateColumnType(MapElement mapElement) {
		StructuralDataElement element = mapElement.getStructuralDataElement();
		String columnName = element.getName();
		if (PostgreUtils.isReservedColumnName(columnName)) {
			columnName = DaoUtils.getNameSubstitution(columnName);
		}

		String columnType = element.getType().getSqlFormatString();

		// If this is a varying character column, insert the size.
		if (DataType.ALPHANUMERIC.equals(element.getType()) || DataType.BIOSAMPLE.equals(element.getType())
				|| (DataType.NUMERIC.equals(element.getType())
						&& InputRestrictions.MULTIPLE.equals(element.getRestrictions()))) {
			Formatter formatter = new Formatter();

			Integer size = null;

			// if this is a free form, it will have a size
			size = element.getSize();

			// This is a special case
			if (DataType.NUMERIC.equals(element.getType())
					&& InputRestrictions.MULTIPLE.equals(element.getRestrictions())) {
				columnType = DataType.ALPHANUMERIC.getSqlFormatString();
			}

			// else calculate the size
			if (size == null) {
				size = 0;

				for (ValueRange vr : element.getValueRangeList()) {
					// if it can be multiple input, the size must be larger than all of
					// the possible values
					// + separating character
					if (InputRestrictions.MULTIPLE.equals(element.getRestrictions())) {
						size += vr.getValueRange().length() + 1;
					}
					// otherwise it just needs to be the largest size
					else {
						if (vr.getValueRange().length() > size) {
							size = vr.getValueRange().length();
						}
					}
				}
			}

			formatter.format(columnType, size);
			columnType = formatter.toString();
			formatter.close();
		}

		return columnType;
	}

	/**
	 * Takes a repeatable group and returns the name of the table to be created for datastore
	 * 
	 * @param repeatableGroup
	 * @return
	 */
	private String getStoreName(String dataStructureShortName, RepeatableGroup repeatableGroup) {
		return (dataStructureShortName + "_rg" + repeatableGroup.getId()).toLowerCase();
	}
}
