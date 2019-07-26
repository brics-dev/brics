package gov.nih.nichd.ctdb.form.dao;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.postgresql.util.PSQLException;
import org.w3c.dom.Document;

import gov.nih.nichd.ctdb.common.CtdbConstants;
import gov.nih.nichd.ctdb.common.CtdbDao;
import gov.nih.nichd.ctdb.common.CtdbException;
import gov.nih.nichd.ctdb.common.CtdbLookup;
import gov.nih.nichd.ctdb.common.DuplicateObjectException;
import gov.nih.nichd.ctdb.common.HtmlAttributes;
import gov.nih.nichd.ctdb.common.InvalidRemovalException;
import gov.nih.nichd.ctdb.common.ObjectNotFoundException;
import gov.nih.nichd.ctdb.common.Version;
import gov.nih.nichd.ctdb.common.util.Utils;
import gov.nih.nichd.ctdb.emailtrigger.dao.EmailTriggerDao;
import gov.nih.nichd.ctdb.emailtrigger.domain.EmailTrigger;
import gov.nih.nichd.ctdb.form.common.FormConstants;
import gov.nih.nichd.ctdb.form.common.FormHtmlAttributes;
import gov.nih.nichd.ctdb.form.common.FormResultControl;
import gov.nih.nichd.ctdb.form.common.QuestionRemovalException;
import gov.nih.nichd.ctdb.form.domain.CalculatedFormQuestionAttributes;
import gov.nih.nichd.ctdb.form.domain.CellFormatting;
import gov.nih.nichd.ctdb.form.domain.Form;
import gov.nih.nichd.ctdb.form.domain.FormGroup;
import gov.nih.nichd.ctdb.form.domain.FormLayout;
import gov.nih.nichd.ctdb.form.domain.FormQuestionAttributes;
import gov.nih.nichd.ctdb.form.domain.Section;
import gov.nih.nichd.ctdb.patient.domain.Patient;
import gov.nih.nichd.ctdb.protocol.domain.Interval;
import gov.nih.nichd.ctdb.question.domain.AnswerType;
import gov.nih.nichd.ctdb.question.domain.ConversionFactor;
import gov.nih.nichd.ctdb.question.domain.InstanceType;
import gov.nih.nichd.ctdb.question.domain.Question;
import gov.nih.nichd.ctdb.question.domain.SkipRuleOperatorType;
import gov.nih.nichd.ctdb.question.domain.SkipRuleType;
import gov.nih.nichd.ctdb.question.manager.QuestionManager;
import gov.nih.nichd.ctdb.response.domain.AdministeredForm;
import gov.nih.nichd.ctdb.response.domain.DataEntryWorkflowType;
import gov.nih.nichd.ctdb.util.common.SysPropUtil;
import gov.nih.tbi.dictionary.model.hibernate.eform.adapters.EformMigrationAdapter;
import gov.nih.tbi.dictionary.model.migration.eform.MigratedQuestion;
import gov.nih.tbi.dictionary.model.migration.eform.MigratedSection;

/**
 * FormManagerDao interacts with the Data Layer for the FormManager. The only
 * job of the DAO is to manipulate the data layer.
 * 
 * @author Booz Allen Hamilton
 * @version 1.0
 */
public class FormManagerDao extends CtdbDao {
	private static Logger logger = Logger.getLogger(FormManagerDao.class);
	
	/**
	 * Private Constructor to hide the instance creation implementation of the
	 * FormManagerDao object in memory. This will provide a flexible
	 * architecture to use a different pattern in the future without refactoring
	 * the FormManager.
	 */
	protected FormManagerDao() {

	}

	/**
	 * Method to retrieve the instance of the FormManag`erDao.
	 * 
	 * @return FormManagerDao data object
	 */
	public static synchronized FormManagerDao getInstance() {
		return new FormManagerDao();
	}

	/**
	 * Method to retrieve the instance of the FormManagerDao. This method
	 * accepts a Database Connection to be used internally by the DAO. All
	 * transaction management will be handled at the BusinessManager level.
	 * 
	 * @param conn
	 *            Database connection to be used within this data object
	 * @return FormManagerDao data object
	 */
	public static synchronized FormManagerDao getInstance(Connection conn) {
		FormManagerDao dao = new FormManagerDao();
		dao.setConnection(conn);
		return dao;
	}

	/**
	 * Creates a form for the protocol.
	 * 
	 * @param form
	 *            the form to create
	 * @throws DuplicateObjectException
	 *             is thrown if the form already exists for the protocol
	 * @throws CtdbException
	 *             thrown if any other errors occur while processing
	 */
	public int createForm(Form form) throws DuplicateObjectException, CtdbException {
		PreparedStatement stmt = null;
		ResultSet rs = null;

		try {

			String sql = "select coalesce(max(orderval) + 1, 1) from form where protocolid = ? ";

			// gets the form order value for the protocol ID
			stmt = this.conn.prepareStatement(sql);
			stmt.setLong(1, form.getProtocolId());
			rs = stmt.executeQuery();
			int orderval = 1;
			
			if (rs.next()) {
				orderval = rs.getInt(1);
			}
			
			rs.close();
			stmt.close();
			sql = "insert into form (formid, name, description, xstatusid, createdby, createddate, updatedby, updateddate, version, lockflag, " +
				"formborder, sectionborder, formnamefont, formnamecolor, sectionnamefont, sectionnamecolor, protocolid, orderval, dataentryflag, " +
				"publicflag, header, footer, fontsize, dataentryworkflowtype, cellpadding, attachfiles, enabledataspring, tabdisplay, formtypeid, " +
				"data_structure_name, copyright, allow_multiple_collection_instances, islegacy) values (DEFAULT, ?, ?, ?, ?, CURRENT_TIMESTAMP, ?, " +
				"CURRENT_TIMESTAMP, 1, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,?) ";

			stmt = this.conn.prepareStatement(sql);
			stmt.setString(1, form.getName());
			stmt.setString(2, form.getDescription());
			stmt.setLong(3, form.getStatus().getId());
			stmt.setLong(4, form.getCreatedBy());
			stmt.setLong(5, form.getUpdatedBy());
			stmt.setBoolean(6, form.getLockFlag());
			stmt.setBoolean(7, form.getFormHtmlAttributes().getFormBorder());
			stmt.setBoolean(8, form.getFormHtmlAttributes().getSectionBorder());
			stmt.setString(9, form.getFormHtmlAttributes().getFormFont());
			stmt.setString(10, form.getFormHtmlAttributes().getFormColor());
			stmt.setString(11, form.getFormHtmlAttributes().getSectionFont());
			stmt.setString(12, form.getFormHtmlAttributes().getSectionColor());
			stmt.setLong(13, form.getProtocolId());
			stmt.setInt(14, orderval);
			stmt.setInt(15, form.getSingleDoubleKeyFlag());
			boolean publicFlag = true;
			if(form.getAccessFlag() == 1) {
				//this means private
				publicFlag = false;
			}else {
				//this means public
				publicFlag = true;
			}
			stmt.setBoolean(16, publicFlag);
			stmt.setString(17, form.getFormHeader());
			stmt.setString(18, form.getFormFooter());
			stmt.setInt(19, form.getFormHtmlAttributes().getFormFontSize());
			stmt.setInt(20, form.getDataEntryWorkflow().getValue());
			stmt.setInt(21, form.getFormHtmlAttributes().getCellpadding());
			stmt.setString(22, Boolean.toString(form.isAttachFiles()));
			stmt.setString(23, Boolean.toString(form.isDataSpring()));
			stmt.setString(24, Boolean.toString(form.isTabDisplay()));
			stmt.setLong(25, form.getFormType());
			stmt.setString(26, form.getDataStructureName());
			stmt.setString(27, Boolean.toString(form.isCopyRight()));
			stmt.setString(28, Boolean.toString(form.isAllowMultipleCollectionInstances()));
			stmt.setBoolean(29, false);
			stmt.executeUpdate();
			int formId = this.getInsertId(conn, "form_seq");
			form.setId(formId);
			return formId;
		}
		catch (PSQLException e) {
			if ( e.getSQLState().equals(SysPropUtil.getProperty("postgres_database.unique_constraint_sqlstate")) ) {
				throw new DuplicateObjectException("A form with the same name already exists for the study: " + e.getMessage(), e);
			}
			else {
				throw new CtdbException("Unable to create study form: "+ e.getMessage(), e);
			}
		}
		catch (SQLException e) {
			if ( e.getSQLState().equals(SysPropUtil.getProperty("postgres_database.unique_constraint_sqlstate")) ) {
				throw new DuplicateObjectException("A form with the same name already exists for the study: " + e.getMessage(), e);
			}
			else {
				throw new CtdbException("Unable to create study form: " + e.getMessage(), e);
			}
		}
		finally {
			this.close(rs);
			this.close(stmt);
		}
	}

	/**
	 * Delete the unadministered form. Database cascade delete will be performed
	 * on other tables, such as FORMLAYOUT, SECTION, and SECTIONQUESTION.
	 * 
	 * @param formId
	 *            The unadministered form
	 * @throws CtdbException
	 *             thrown if any errors occur when processing
	 */
	public void deleteForm(int formId, String formName) throws CtdbException {
		PreparedStatement stmt = null;

		try {
			String sql = "delete FROM form where formid = ? ";
			stmt = this.conn.prepareStatement(sql);
			stmt.setLong(1, formId);
			stmt.executeUpdate();
		}
		catch ( SQLException e ) {
			throw new CtdbException("Unable to delete the form named " + formName, e);
		}
		finally {
			this.close(stmt);
		}
	}

	/**
	 * Updates the form status for the protocol.
	 * 
	 * @param form
	 *            the form data object to update
	 * @throws ObjectNotFoundException
	 *             is thrown if the form does not exist
	 * @throws CtdbException
	 *             thrown if any other errors occur while processing
	 */
	public void updateFormStatus(Form form) throws CtdbException {

		PreparedStatement stmt = null;

		try {
			// update form
			String sql = "update form set xstatusid = ?, updatedby = ?, updateddate = CURRENT_TIMESTAMP where formid = ? ";
			
			stmt = this.conn.prepareStatement(sql);
			stmt.setLong(1, form.getStatus().getId());
			stmt.setLong(2, form.getUpdatedBy());
			stmt.setLong(3, form.getId());
			
			int recordsUpdated = stmt.executeUpdate();

			if (recordsUpdated == 0) {
				throw new ObjectNotFoundException("The form with ID: " + form.getId() + " does not exist in the system.");
			}
		}
		catch ( SQLException e ) {
			throw new CtdbException("Unable to update the form: " + e.getMessage(), e);
		}
		finally {
			this.close(stmt);
		}
	}

	public void updateFormStats(int formId) throws CtdbException {
		PreparedStatement stmt = null;

		try {
			String sql = "update form set stats = 3 where formid = ? ";
			stmt = this.conn.prepareStatement(sql);
			stmt.setLong(1, formId);
			int recordsUpdated = stmt.executeUpdate();

			if (recordsUpdated == 0) {
				throw new ObjectNotFoundException("update fail in form with ID: " + formId);
			}
		}
		catch ( SQLException e ) {
			throw new CtdbException("Unable to update the form: " + e.getMessage(), e);
		}
		finally {
			this.close(stmt);
		}
	}

	public void deleteAllRows(int formId) throws CtdbException {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		
		try {
			// find next row num
			String sql = "delete from formlayout where formid = ? ";
			stmt = this.conn.prepareStatement(sql);
			stmt.setLong(1, formId);
			stmt.executeUpdate();
		}
		catch (SQLException e) {
			throw new CtdbException("Unable to delete all rows from form: " + e.getMessage(), e);
		}
		finally {
			this.close(stmt);
			this.close(rs);
		}
	}

	/**
	 * adds a row to a form
	 * 
	 * @param formId
	 * @param numCols
	 * @throws CtdbException
	 */
	public void addRow(int formId, int numCols) throws CtdbException {
		PreparedStatement stmt = null;
		PreparedStatement stmt2 = null;
		ResultSet rs = null;
		
		try {
			// find next row num
			String sql = "select max(formrow) from formlayout where formid = ? ";

			stmt = this.conn.prepareStatement(sql);
			stmt.setLong(1, formId);

			rs = stmt.executeQuery();
			int nextRowId = 1;
			
			if (rs.next()) {
				nextRowId = rs.getInt(1);
			}
			
			nextRowId++;

			sql = "insert into formlayout (formrow, formid, numcols) values (?, ?, ?) ";
			stmt2 = this.conn.prepareStatement(sql);
			stmt2.setLong(1, nextRowId);
			stmt2.setLong(2, formId);
			stmt2.setInt(3, numCols);

			int recordsUpdated = stmt2.executeUpdate();

			if ( recordsUpdated == 0 ) {
				throw new ObjectNotFoundException("The form with ID: " + formId + " does not exist in the system.");
			}
		}
		catch ( SQLException e ) {
			throw new CtdbException("Unable to update the form: " + e.getMessage(), e);
		}
		finally {
			this.close(rs);
			this.close(stmt);
			this.close(stmt2);
		}
	}

	/**
	 * adds a section to a row
	 * 
	 * @param formId
	 * @param rowId
	 * @throws CtdbException
	 */
	public void addSection(int formId, int rowId) throws CtdbException {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		
		try {
			String sql = "select numcols from formlayout where formid = ? and formrow = ? ";
			stmt = this.conn.prepareStatement(sql);
			stmt.setLong(1, formId);
			stmt.setLong(2, rowId);

			rs = stmt.executeQuery();
			rs.next();
			int numCols = rs.getInt(1);
			
			rs.close();
			stmt.close();
			
			if (numCols < 50) {
				sql = "update formlayout set numcols = numcols + 1 where formid = ? and formrow = ? ";

				stmt = this.conn.prepareStatement(sql);
				stmt.setLong(1, formId);
				stmt.setLong(2, rowId);

				if (stmt.executeUpdate() == 0) {
					throw new ObjectNotFoundException("The form with ID: " + formId + " does not exist in the system.");
				}
			}
			else {
				// too many cols
				throw new CtdbException(FormConstants.MAX_SECTIONS_IN_ROW_MESSAGE);
			}
		}
		catch (SQLException e) {
			throw new CtdbException("Unable to update the form: " + e.getMessage(), e);
		}
		finally {
			this.close(stmt);
			this.close(rs);
		}
	}

	/**
	 * swaps two sections in a form
	 * 
	 * @param formId
	 * @param row1
	 * @param row2
	 * @param col1
	 * @param col2
	 * @throws CtdbException
	 */
	public void swapSections(int formId, int row1, int row2, int col1, int col2) throws CtdbException {
		PreparedStatement stmt = null;
		ResultSet rs = null;

		try {
			String sql = "select sectionid from section where formid = ? and formrow = ? and formcol = ? ";
			stmt = conn.prepareStatement(sql);
			stmt.setLong(1, formId);
			stmt.setInt(2, row1);
			stmt.setInt(3, col1);
			
			rs = stmt.executeQuery();
			rs.next();
			int section1id = rs.getInt(1);
			
			rs.close();
			stmt.close();
			
			sql = "select sectionid from section where formid = ? and formrow = ? and formcol = ?";
			stmt = conn.prepareStatement(sql);
			stmt.setInt(1, formId);
			stmt.setInt(2, row2);
			stmt.setInt(3, col2);
			
			rs = stmt.executeQuery();
			rs.next();
			int section2id = rs.getInt(1);
			
			rs.close();
			stmt.close();

			// change 1st to second
			sql = "update section set formrow = ?, formcol = ? where sectionid = ? ";
			stmt = this.conn.prepareStatement(sql);
			stmt.setInt(1, row2);
			stmt.setInt(2, col2);
			stmt.setLong(3, section1id);
			stmt.executeUpdate();
			
			stmt.close();

			stmt = this.conn.prepareStatement(sql.toString());
			stmt.setInt(1, row1);
			stmt.setInt(2, col1);
			stmt.setInt(3, section2id);
			stmt.executeUpdate();
			
			stmt.close();

			stmt = this.conn.prepareStatement("update form set updateddate = CURRENT_TIMESTAMP where formid = ? ");
			stmt.setLong(1, formId);
			stmt.executeUpdate();

		}
		catch (SQLException e) {
			throw new CtdbException("Unable to update the form: " + e.getMessage(), e);
		}
		finally {
			this.close(rs);
			this.close(stmt);
		}
	}

	/**
	 * Updates the form for the protocol. It increments the form version if the
	 * isVersioning is ture.
	 * 
	 * @param form
	 *            the form data object to update
	 * @param isVersioning
	 *            should the form version field be incremented
	 * @throws DuplicateObjectException
	 *             is thrown if the form already exists for the protocol
	 * @throws ObjectNotFoundException
	 *             is thrown if the form does not exist
	 * @throws CtdbException
	 *             thrown if any other errors occur while processing
	 */
	public void updateForm(Form form, boolean isVersioning) throws DuplicateObjectException, CtdbException {
		PreparedStatement stmt = null;
		
		try {
			// update form
			StringBuffer sql = new StringBuffer(200);
			sql.append("update form set description = ?, publicflag = ?, xstatusid = ?, updatedby = ?, ");

			if (isVersioning) {
				sql.append("updateddate = CURRENT_TIMESTAMP, ");
				sql.append("version = version + 1, ");
			}
			else {
				sql.append("updateddate = CURRENT_TIMESTAMP, ");
				sql.append("name = '" + form.getName().replaceAll("'", "''") + "', ");
				sql.append("dataentryflag = " + form.getSingleDoubleKeyFlag() + ", ");
			}
			
			if (form.getLockFlag()) {
				sql.append("lockflag = true, checkedoutby = " + form.getCheckOutBy() + ", ");
				sql.append("checkedoutdate = CURRENT_TIMESTAMP, ");
			}
			else {
				sql.append("lockflag = false, ");
			}
			
			if (form.getImportedDate() != null && form.getImportFileName() != null) {
				sql.append("importeddate = CURRENT_TIMESTAMP, importfilename = '" + form.getImportFileName() + "', ");
			}
			
			sql.append("formborder = ?, sectionborder = ?, formnamefont = ?, formnamecolor = ?, sectionnamefont = ?, sectionnamecolor = ?, header = ?, " +
					"footer = ?, fontsize = ?, dataentryworkflowtype = ?, cellpadding = ?, attachfiles = ?, enabledataspring = ? , tabdisplay = ?, " +
					"formtypeid = ? , copyright = ?, allow_multiple_collection_instances = ?, data_structure_name = ? where formid = ? ");

			stmt = this.conn.prepareStatement(sql.toString());

			stmt.setString(1, form.getDescription());
			boolean publicFlag = true;
			if(form.getAccessFlag() == 1) {
				//this means private
				publicFlag = false;
			}else {
				//this means public
				publicFlag = true;
			}
			stmt.setBoolean(2, publicFlag);
			stmt.setLong(3, form.getStatus().getId());
			stmt.setLong(4, form.getUpdatedBy());
			stmt.setBoolean(5, form.getFormHtmlAttributes().getFormBorder());
			stmt.setBoolean(6, form.getFormHtmlAttributes().getSectionBorder());
			stmt.setString(7, form.getFormHtmlAttributes().getFormFont());
			stmt.setString(8, form.getFormHtmlAttributes().getFormColor());
			stmt.setString(9, form.getFormHtmlAttributes().getSectionFont());
			stmt.setString(10, form.getFormHtmlAttributes().getSectionColor());
			stmt.setString(11, form.getFormHeader());
			stmt.setString(12, form.getFormFooter());
			stmt.setInt(13, form.getFormHtmlAttributes().getFormFontSize());
			stmt.setInt(14, form.getDataEntryWorkflow().getValue());
			stmt.setInt(15, form.getFormHtmlAttributes().getCellpadding());
			stmt.setString(16, Boolean.toString(form.isAttachFiles()));
			stmt.setString(17, Boolean.toString(form.isDataSpring()));
			stmt.setString(18, Boolean.toString(form.isTabDisplay()));
			stmt.setLong(19, form.getFormType());
			stmt.setString(20, Boolean.toString(form.isCopyRight()));
			stmt.setString(21, Boolean.toString(form.isAllowMultipleCollectionInstances()));
			stmt.setString(22, form.getDataStructureName());
			stmt.setLong(23, form.getId());

			int recordsUpdated = stmt.executeUpdate();

			if (recordsUpdated == 0) {
				throw new ObjectNotFoundException("The form with ID: " + form.getId() + " does not exist in the system.");
			}
		}
		catch (PSQLException e) {
			if (e.getSQLState().equals(SysPropUtil.getProperty("postgres_database.unique_constraint_sqlstate"))) {
				throw new DuplicateObjectException("A form with the same name already exists for the study: " + e.getMessage(), e);
			}
			else {
				throw new CtdbException("Unable to create study form: " + e.getMessage(), e);
			}
		}
		catch (SQLException e) {
			if (e.getErrorCode() == Integer.parseInt(SysPropUtil.getProperty("database.unique_constraint_error.code"))) {
				throw new DuplicateObjectException("A form with the same name already exists for the study: " + e.getMessage(), e);
			}
			else {
				throw new CtdbException("Unable to update the study form: " + e.getMessage(), e);
			}
		}
		finally {
			this.close(stmt);
		}
	}

	/**
	 * Updates the form version along with the updateBy and updateDate
	 * 
	 * @param form
	 *            the form object to update
	 * @throws ObjectNotFoundException
	 *             is thrown if the form does not exist
	 * @throws CtdbException
	 *             thrown if any other errors occur while processing
	 */
	public void updateFormVersion(Form form) throws ObjectNotFoundException, CtdbException {
		updateFormVersion(form.getUpdatedBy(), form.getId());
	}

	public void updateFormVersion(int userid, int formId) throws CtdbException {
		PreparedStatement stmt = null;

		try {
			StringBuffer sql = new StringBuffer(200);
			sql.append("update form set updatedby = ?, updateddate = CURRENT_TIMESTAMP, version = version + 1 ");
			sql.append("where formid = ?");

			stmt = this.conn.prepareStatement(sql.toString());
			stmt.setInt(1, userid);
			stmt.setInt(2, formId);

			int recordsUpdated = stmt.executeUpdate();

			if (recordsUpdated == 0) {
				throw new ObjectNotFoundException("The form with ID: " + formId
						+ " does not exist in the system.");
			}
		} catch (SQLException e) {
			throw new CtdbException("Unable to update the form: "
					+ e.getMessage(), e);
		} finally {
			this.close(stmt);
		}
	}

	/**
	 * Updates a form to reflect a question that has been versioned
	 * 
	 * @param sectionId
	 *            the section object to update
	 * @param question
	 *            the question that will be updated on the form
	 * @throws ObjectNotFoundException
	 *             is thrown if the form does not exist
	 * @throws CtdbException
	 *             thrown if any other errors occur while processing
	 */
	public void updateFormQuestion(int sectionId, Question question) throws ObjectNotFoundException, CtdbException {
		PreparedStatement stmt = null;

		try {
			StringBuffer sql = new StringBuffer(200);
			sql.append("update sectionquestion set questionversion = ? ");
			sql.append("where sectionId = ? and questionId = ? ");

			stmt = this.conn.prepareStatement(sql.toString());
			stmt.setInt(1, question.getVersion().getVersionNumber());
			stmt.setInt(2, sectionId);
			stmt.setInt(3, question.getId());

			// update questionattributes w/ new question version
			int recordsUpdated = stmt.executeUpdate();

			stmt.close();

			sql = new StringBuffer(200);
			sql.append("update questionattributes set questionversion = ? where questionid = ? and ");
			sql.append(" questionattributesid= (select questionattributesid from sectionquestion ");
			sql.append("       where questionid = ? and questionversion= ? and sectionid = ? )");
			stmt = this.conn.prepareStatement(sql.toString());
			stmt.setInt(1, question.getVersion().getVersionNumber());
			stmt.setInt(2, question.getId());
			stmt.setInt(3, question.getId());
			stmt.setInt(4, question.getVersion().getVersionNumber());
			stmt.setInt(5, sectionId);
			stmt.executeQuery();

			if (recordsUpdated == 0) {
				throw new ObjectNotFoundException(
						"Could not update the section : " + sectionId + " to reflect new question version");
			}
		}
		catch (SQLException e) {
			throw new CtdbException("Unable to update the section w/ new question version: " + e.getMessage(), e);
		}
		finally {
			this.close(stmt);
		}
	}

	/**
	 * updates the form layout table when a form is being 'saved as'
	 * 
	 * @param form1
	 *            the form being created
	 * @param form2
	 *            the form being copied
	 * @throws CtdbException
	 */
	public void updateFormLayout4SaveAs(Form form1, Form form2) throws CtdbException {
		PreparedStatement stmt = null;
		PreparedStatement stmt2 = null;
		ResultSet rs = null;

		try {
			String sql = "select formrow, numcols from formlayout where formid = ? ";
			stmt = conn.prepareStatement(sql);
			stmt.setInt(1, form2.getId());
			rs = stmt.executeQuery();
			
			sql = "insert into formlayout (formid, formrow, numcols) values (?, ?, ?) ";
			stmt2 = this.conn.prepareStatement(sql);
			
			while ( rs.next() ) {
				stmt2.setInt(1, form1.getId());
				stmt2.setInt(2, rs.getInt(1));
				stmt2.setInt(3, rs.getInt(2));

				int recordsUpdated = stmt2.executeUpdate();

				if ( recordsUpdated == 0 ) {
					throw new CtdbException("error updating form layout for save as");
				}
			}
		}
		catch (SQLException e) {
			throw new CtdbException("error updating form layout for save as " + e.getMessage(), e);
		}
		finally {
			this.close(rs);
			this.close(stmt);
			this.close(stmt2);
		}
	}

	/**
	 * Checks if the form is administered. Uses ID and versionNumber of the
	 * domain object to check to see if it is administered. If the versionNumber
	 * of Version in the domain object is not set (equals to Integer.MIN_VALUE)
	 * or Version in the domain object is null, then the current version of the
	 * form in the data base is used for the check.
	 * 
	 * @param form
	 *            the form object to check
	 * @return if the form is administered
	 * @throws CtdbException
	 *             thrown if any errors occur while processing
	 */
	public boolean isAdministered(Form form) throws CtdbException {
		return this.isAdministered(form.getId());
	}

	public boolean isAdministered(int formId) throws CtdbException {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		int num = 0;

		try {
			StringBuffer sql = new StringBuffer(200);

			// TODO fix this so the updated date is not used to determine if
			// a form version is administered
			sql.append("select count(af.administeredformid) from administeredform af, form f where f.formid = ? and af.formid = f.formid ");
			sql.append(" AND  f.updateddate < af.finallockdate ");
			stmt = this.conn.prepareStatement(sql.toString());
			stmt.setInt(1, formId);
			rs = stmt.executeQuery();
			
			if (rs.next()) {
				num = rs.getInt(1);
			}
		}
		catch (SQLException e) {
			throw new CtdbException("Unable to check is administered for form with id " + formId + " : " + e.getMessage(), e);
		}
		finally {
			this.close(rs);
			this.close(stmt);
		}
		
		return num > 0;
	}

	public boolean isAdministeredSection(List<String> sectionIds) throws CtdbException {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		boolean isAdministered = false;

		try {
			StringBuffer inClause = new StringBuffer("(");
			
			for ( Iterator<String> it = sectionIds.iterator(); it.hasNext(); ) {
				it.next();
				inClause.append("?");
				
				if ( it.hasNext() ) {
					inClause.append(", ");
				}
			}
			
			inClause.append(") ");

			// check to see if the form has been versioned and the section is
			// moved to archive.
			StringBuffer sql = new StringBuffer(200);
			sql.append("select count(sectionid) from sectionarchive where sectionid in ");
			sql.append(inClause);
			stmt = this.conn.prepareStatement(sql.toString());
			
			// Add the values for the in clause.
			int n = 1;
			
			for ( String id : sectionIds ) {
				stmt.setString(n, id);
				n++;
			}
			
			rs = stmt.executeQuery();
			rs.next();
			
			if ( rs.getInt(1) == 0 ) {
				// now check to see if the section is on a form that has been
				// administered and not versioned.
				sql = new StringBuffer(100);
				rs.close();
				stmt.close();
				sql.append("select count(af.formid) from administeredform af, form f, section s ");
				sql.append(" where s.sectionid in ").append(inClause);
				sql.append(" and s.formid = f.formid and f.formid = af.formid and f.updateddate < af.finallockdate ");
				stmt = this.conn.prepareStatement(sql.toString());
				
				// Add in the values for the in clause.
				n = 1;
				
				for ( String id : sectionIds ) {
					stmt.setString(n, id);
					n++;
				}
				
				rs = stmt.executeQuery();
				rs.next();
				
				if (rs.getInt(1) != 0) {
					isAdministered = true;
				}
			}
			else {
				isAdministered = true;
			}
		}
		catch ( SQLException e ) {
			throw new CtdbException("Unable to check is administered sections " + e.getMessage(), e);
		}
		finally {
			this.close(rs);
			this.close(stmt);
		}
		
		return isAdministered;
	}

	public boolean isAdministeredSection(int sectionid) throws CtdbException {
		List<String> al = new ArrayList<String>();
		al.add(Integer.toString(sectionid));
		
		return isAdministeredSection(al);

	}
	
	/**
	 * Creates an archived form.
	 * 
	 * @param formId
	 *            The form object ID to archive
	 * @throws CtdbException
	 *             thrown if any errors occur while processing
	 */
	public void createFormArchive(int formId) throws CtdbException {
		PreparedStatement stmt = null;

		try {
			String sql = "insert into formarchive select * from form where formid = ? ";
			stmt = this.conn.prepareStatement(sql);
			stmt.setInt(1, formId);
			stmt.executeUpdate();

			createFormLayoutArchive(formId);
			this.createSectionArchive(formId);
			this.createSectionQuestionArchive(formId);
			this.createQuestionAttributesArchive(formId);
			this.createCellFormattingArchive(formId);
		}
		catch (SQLException e) {
			throw new CtdbException("Unable to create form archive: " + e.getMessage(), e);
		}
		finally {
			this.close(stmt);
		}
	}

	/**
	 * Creates an archived question attributes.
	 * 
	 * @param formId
	 * @throws CtdbException
	 */
	public void createQuestionAttributesArchive(int formId)
			throws CtdbException {
		PreparedStatement stmt = null;

		try {
			StringBuffer sql = new StringBuffer(200);
			sql.append("insert into questionattributesarchive ");
			sql.append("select qa.questionattributesid, qa.questionid, qa.questionversion, ");
			sql.append("f.formid, f.version as formversion, sq.sectionid, qa.requiredflag, qa.calculatedflag, ");
			sql.append("qa.calculation, qa.skipruleflag, qa.skipruletype, qa.skipruleoperatortype, ");
			sql.append("qa.skipruleequals, qa.halign, qa.valign, qa.textcolor, qa.fontface, qa.fontsize, ");
			sql.append("qa.indent, qa.rangeoperator, qa.rangevalue1, qa.rangevalue2, qa.dtconversionfactor, ");
			sql.append("qa.createdby, qa.createddate, qa.updatedby, qa.updateddate, qa.label, qa.answertype, ");
			sql.append("qa.mincharacters, qa.maxcharacters, qa.horizontaldisplay, qa.textareaHeight, qa.textareawidth, qa.textboxlength, qa.emailtriggerid, qa.dataspring, qa.xhtmltext, qa.horizDisplayBreak ");
			sql.append("from questionattributes qa, sectionquestion sq, section s, form f ");
			sql.append("where qa.questionattributesid = sq.questionattributesid ");
			sql.append("and qa.questionid = sq.questionid ");
			sql.append("and qa.questionversion = sq.questionversion ");
			sql.append("and sq.sectionid = s.sectionid ");
			sql.append("and s.formid = f.formid ");
			sql.append("and f.formid = ? ");

			stmt = this.conn.prepareStatement(sql.toString());
			stmt.setInt(1, formId);
			stmt.executeUpdate();
		} catch (SQLException e) {
			throw new CtdbException(
					"Unable to create question attributes archive for the form: "
							+ e.getMessage(), e);
		} finally {
			this.close(stmt);
		}
	}

	private void createFormLayoutArchive(int formId) throws CtdbException {
		PreparedStatement stmt = null;

		try {
			StringBuffer sql = new StringBuffer(100);
			sql.append("insert into formlayoutarchive select form.formid, form.version as formversion , ");
			sql.append(" formlayout.formrow, formlayout.numcols from ");
			sql.append(" formlayout, form where formlayout.formid = form.formid and ");
			sql.append(" form.formid = ? ");
			stmt = this.conn.prepareStatement(sql.toString());
			stmt.setInt(1, formId);
			stmt.executeUpdate();
		} catch (SQLException e) {
			throw new CtdbException("Unable to create formlayout archive: "
					+ e.getMessage(), e);
		} finally {
			this.close(stmt);
		}
	}

	public void createCellFormattingArchive(int formId) throws CtdbException {
		PreparedStatement stmt = null;

		try {
			StringBuffer sql = new StringBuffer(100);

			sql.append("insert into formcellformattingarchive select form.version as formversion, ");
			sql.append("form.formid, cf.formrow, cf.formcol, cf.width, cf.height, cf.align, cf.valign, cf.textwrap, cf.bgcolor, cf.padding, cf.rowspan, cf.colspan ");
			sql.append("from form, formcellformatting cf where form.formid = ? and form.formid = cf.formid ");
			stmt = this.conn.prepareStatement(sql.toString());
			stmt.setInt(1, formId);
			stmt.executeUpdate();
		} catch (SQLException e) {
			throw new CtdbException(
					"Unable to create form cell formatting archive: "
							+ e.getMessage(), e);
		} finally {
			this.close(stmt);
		}
	}

	/**
	 * Creates an archived section.
	 * 
	 * @param formId
	 *            The form ID to archive
	 * @throws CtdbException
	 *             thrown if any errors occur while processing
	 */
	public void createSectionArchive(int formId) throws CtdbException {
		PreparedStatement stmt = null;

		try {
			StringBuffer sql = new StringBuffer(100);

			sql.append("insert into sectionarchive select section.sectionid, ");
			sql.append("section.formid, form.version as formversion, section.name, ");
			sql.append("section.description, section.createdby, section.createddate, ");
			sql.append("section.updatedby, section.updateddate, section.orderval as sectionorder, formrow, formcol, suppressflag, ");
			sql.append("label, altlabel, intob, collapsable, isresponseimage ");
			sql.append("from section, form where section.formid = ? and section.formid = form.formid ");
			stmt = this.conn.prepareStatement(sql.toString());
			stmt.setInt(1, formId);
			stmt.executeUpdate();
		} catch (SQLException e) {
			throw new CtdbException("Unable to create form archive: "
					+ e.getMessage(), e);
		} finally {
			this.close(stmt);
		}
	}

	/**
	 * Updates the question attributes for the question in form section.
	 * 
	 * @param question
	 *            question object contains the its attributes to update
	 * @throws ObjectNotFoundException
	 *             thrown if the question attributes does not exist
	 * @throws CtdbException
	 *             thrown if any other errors occur during the process
	 */
	public void updateQuestionAttributes(Question question) throws ObjectNotFoundException, CtdbException {
		PreparedStatement stmt = null;
		
		try {
			// update question attributes
			StringBuffer sql = new StringBuffer(200);
			sql.append(" select requiredflag, calculatedflag, calculation, ");
			sql.append(" skipruleflag, skipruletype, skipruleoperatortype, ");
			sql.append(" skipruleequals, halign, valign, ");
			sql.append(" textcolor, fontface, fontsize, indent, ");
			sql.append(" rangeoperator, rangevalue1, rangevalue2, dtconversionfactor, ");
			sql.append(" updatedby, updateddate, answertype, mincharacters, maxcharacters, horizontaldisplay, textareaheight, textareawidth, textboxlength, horizDisplayBreak ");
			sql.append(" , emailtriggerid, dataspring, xhtmltext, data_element_name,prepopulation,prepopulationValue,decimalprecision,hasconversionfactor,conversionfactor,questionattributesid,questionid,questionversion,group_name,showtext,tableheadertype ");
			sql.append(" from questionattributes ");
			sql.append(" where questionattributesid = ? ");
			sql.append(" and questionid = ? ");
			sql.append(" and questionversion = ? ");

			stmt = this.conn
					.prepareCall(sql.toString(),
							ResultSet.TYPE_SCROLL_SENSITIVE,
							ResultSet.CONCUR_UPDATABLE);
			stmt.setLong(1, question.getFormQuestionAttributes().getId());
			stmt.setLong(2, question.getId());
			stmt.setInt(3, question.getVersion().getVersionNumber());

			// Updatable result set.
			ResultSet uprs = stmt.executeQuery();
			if (uprs.next()) {
				// Put it at the current row for update
				uprs.moveToCurrentRow();
				this.setBaseUpdateData(question, uprs);
				// Execute the SQL query

				if (question.getFormQuestionAttributes().isCalculatedQuestion()) {

					this.updateAttachedCalculatedQuestions(question);
				}

				uprs.updateRow();
				uprs.close();
			} else {
				throw new ObjectNotFoundException(
						"Question attributes with ID = "
								+ question.getFormQuestionAttributes().getId()
								+ " does not exist in the system.");
			}

		} catch (SQLException e) {
			throw new CtdbException(
					"Unable to update the question attributes: "
							+ e.getMessage(), e);
		} finally {
			this.close(stmt);
		}
	}
	
	public void clearDataElementAssociationsForForm(int questionAttributesId) throws CtdbException{
		PreparedStatement stmt = null;

		try {
			StringBuffer sql = new StringBuffer(200);
			sql.append("update questionattributes set data_element_name=null, group_name=null where questionattributesid = ?");

			stmt = this.conn.prepareStatement(sql.toString());
			stmt.setInt(1, questionAttributesId);

			stmt.executeUpdate();
		} catch (SQLException e) {
			throw new CtdbException(
					"Unable to clear data element for question");
		} finally {
			this.close(stmt);
		}
	}
	
	public void clearSectionGroupNameAssociationsForForm(int sectionId) throws CtdbException{
		PreparedStatement stmt = null;

		try {
			StringBuffer sql = new StringBuffer(200);
			sql.append("update section set group_name=null where sectionid = ?");

			stmt = this.conn.prepareStatement(sql.toString());
			stmt.setInt(1, sectionId);


			stmt.executeUpdate();
		} catch (SQLException e) {
			throw new CtdbException(
					"Unable to clear group name for section");
		} finally {
			this.close(stmt);
		}
		
	}

	/* added by Ching Heng to update question version in questionAttribute table */
	public void updateQuestionVersionInQuestionAttribute(int questionId,
			int questionVersion) throws ObjectNotFoundException, CtdbException {
		PreparedStatement stmt = null;

		try {
			StringBuffer sql = new StringBuffer(200);
			sql.append("update questionattributes set questionversion=? where questionid = ?");

			stmt = this.conn.prepareStatement(sql.toString());
			stmt.setInt(1, questionVersion);
			stmt.setInt(2, questionId);

			stmt.executeUpdate();
		} catch (SQLException e) {
			throw new CtdbException(
					"Unable to update question version for question "
							+ questionId + " in questionattributes table");
		} finally {
			this.close(stmt);
		}
	}

	/* added by Ching Heng to update question version in sectionquestion table */
	public void updateQuestionVersionInSectionQuestion(int questionId,
			int questionVersion) throws ObjectNotFoundException, CtdbException {
		PreparedStatement stmt = null;

		try {
			StringBuffer sql = new StringBuffer(200);
			sql.append("update sectionquestion set questionversion=? where questionid = ?");

			stmt = this.conn.prepareStatement(sql.toString());
			stmt.setInt(1, questionVersion);
			stmt.setInt(2, questionId);

			stmt.executeUpdate();
		} catch (SQLException e) {
			throw new CtdbException(
					"Unable to update question version for question "
							+ questionId + " in sectionquestion table");
		} finally {
			this.close(stmt);
		}
	}

	/**
	 * Deletes the skip rule questions based on the unique identifier question
	 * id and section id
	 * 
	 * @param questionId
	 *            The question identifier
	 * @param sectionId
	 *            The section identifier
	 * @throws CtdbException
	 *             Thrown if any errors occur
	 */
	public void deleteSkippedQuestions(int questionId, int sectionId)
			throws CtdbException {
		PreparedStatement stmt = null;

		try {
			StringBuffer sql = new StringBuffer(200);
			sql.append("delete FROM skiprulequestion where questionid = ? and sectionid = ? ");

			stmt = this.conn.prepareStatement(sql.toString());
			stmt.setInt(1, questionId);
			stmt.setInt(2, sectionId);

			stmt.executeUpdate();
		} catch (SQLException e) {
			throw new CtdbException(
					"Unable to delete skip questions for question "
							+ questionId + " in the section " + sectionId
							+ " : " + e.getMessage(), e);
		} finally {
			this.close(stmt);
		}
	}

	/**
	 * Associates the skipped rule questions for the question on the
	 * form/section.
	 * 
	 * @param baseQuestionId
	 *            The question unique identifier to associate the skipped rule
	 *            questions for
	 * @param skippedQuestionId
	 *            The skipped rule question id to be associated to
	 * @throws CtdbException
	 *             Thrown if any errors occur
	 */
	public void associateSkippedQuestions(int baseQuestionId,
			int skippedQuestionId, int sectionId, int skippedSectionId)
			throws CtdbException {
		PreparedStatement stmt = null;

		try {
			StringBuffer sql = new StringBuffer(200);
			sql.append("insert into skiprulequestion(questionid, skipquestionid, sectionid,skipsectionid) ");
			sql.append("values(?,?,?,?)");

			stmt = this.conn.prepareStatement(sql.toString());
			stmt.setInt(1, baseQuestionId);
			stmt.setInt(2, skippedQuestionId);
			stmt.setInt(3, sectionId);
			stmt.setInt(4, skippedSectionId);
			stmt.executeUpdate();
		} catch (SQLException e) {
			throw new CtdbException("Unable to associate skipped questions: "
					+ e.getMessage(), e);
		} finally {
			this.close(stmt);
		}
	}

	/**
	 * Retrieves all skip rule sections for a question in the section
	 * 
	 * @param questionId
	 *            the question for the list of skip rule question
	 * @param formId
	 *            the form that the question attached to
	 * @return The list of sections skipped on the form. The list will be empty
	 *         if no sections skipped on the form.
	 * @throws CtdbException
	 *             Thrown if any errors occur while processing
	 */
	public List<Section> getSkipSections(int questionId, int formId) throws CtdbException {
		PreparedStatement stmt = null;
		PreparedStatement stmt2 = null;
		PreparedStatement stmt3 = null;
		ResultSet rs = null;
		ResultSet rs1 = null;
		ResultSet rs2 = null;
		List<Section> sections = new ArrayList<Section>();

		try {
			StringBuffer sql = new StringBuffer(50);
			sql.append("select sectionid ");
			sql.append("from section ");
			sql.append("where formid = ? ");
			stmt = this.conn.prepareStatement(sql.toString());
			stmt.setInt(1, formId);
			rs = stmt.executeQuery();
			
			int sectionId = 0;
			
			sql = new StringBuffer(50);
			sql.append("select count(*) ");
			sql.append("from sectionquestion ");
			sql.append("where sectionid = ? ");
			sql.append("  and questionid != ? ");
			stmt2 = this.conn.prepareStatement(sql.toString());
			
			sql = new StringBuffer(50);
			sql.append("select count(*) ");
			sql.append("from skiprulequestion ");
			sql.append("where sectionid = ? ");
			sql.append("  and questionid = ? ");
			stmt3 = this.conn.prepareStatement(sql.toString());
			
			while ( rs.next() ) {
				sectionId = rs.getInt("sectionid");
				stmt2.setInt(1, sectionId);
				stmt2.setInt(2, questionId);
				rs1 = stmt2.executeQuery();
				
				int count1 = 0;
				
				if (rs1.next()) {
					count1 = rs1.getInt(1);
				}
				
				rs1.close();
				stmt3.setInt(1, sectionId);
				stmt3.setInt(2, questionId);
				rs2 = stmt3.executeQuery();
				
				int count2 = 0;

				if (rs2.next()) {
					count2 = rs2.getInt(1);
				}
				
				rs2.close();

				if (count1 != 0 && count2 != 0 && count1 == count2) {
					sections.add(this.getSection(sectionId));
				}
			}
		}
		catch (SQLException e) {
			throw new CtdbException("Unable to get all skip sections for question " + questionId + " and on the form " + formId + ": "
				+ e.getMessage(), e);
		}
		finally {
			this.close(rs);
			this.close(rs1);
			this.close(rs2);
			this.close(stmt);
			this.close(stmt2);
			this.close(stmt3);
		}
		
		return sections;
	}

	/**
	 * Sets the base data for sql update statement.
	 * 
	 * @param question
	 *            The Question object to get data for setting to
	 *            PreparedStatement
	 * @param rs
	 *            The ResultSet
	 * @throws SQLException
	 *             Thrown if any errors occur
	 */

	private void setBaseUpdateData(Question question, ResultSet rs)
			throws SQLException {
		FormQuestionAttributes formQuestionAttributes = question
				.getFormQuestionAttributes();
		
		rs.updateBoolean(1, formQuestionAttributes.isRequired());
		if (formQuestionAttributes.isCalculatedQuestion()
				&& question.getCalculatedFormQuestionAttributes() != null
				&& question.getCalculatedFormQuestionAttributes()
						.getCalculation() != null
				&& question.getCalculatedFormQuestionAttributes()
						.getCalculation().length() > 0) {
			// calculated question
			rs.updateBoolean(2, true);
			CalculatedFormQuestionAttributes calQuestionAttributes = question
					.getCalculatedFormQuestionAttributes();

			rs.updateString(3, calQuestionAttributes.getCalculation());
			if (calQuestionAttributes.getConversionFactor() != null) {
				rs.updateInt(17, calQuestionAttributes.getConversionFactor()
						.getValue());
			} else {
				rs.updateNull(17);
			}
		} else {
			rs.updateBoolean(2, false);
			rs.updateString(3, "");
			rs.updateNull(17);
		}
		
		rs.updateBoolean(4, formQuestionAttributes.hasSkipRule());

		if (formQuestionAttributes.hasSkipRule()) {
			rs.updateInt(5, formQuestionAttributes.getSkipRuleType().getValue());
			rs.updateInt(6, formQuestionAttributes.getSkipRuleOperatorType()
					.getValue());
		} else {
			rs.updateNull(5);
			rs.updateNull(6);
		}

		rs.updateString(7, formQuestionAttributes.getSkipRuleEquals());
		rs.updateString(8, formQuestionAttributes.getHtmlAttributes()
				.getAlign());
		rs.updateString(9, formQuestionAttributes.getHtmlAttributes()
				.getvAlign());
		rs.updateString(10, formQuestionAttributes.getHtmlAttributes()
				.getColor());
		rs.updateString(11, formQuestionAttributes.getHtmlAttributes()
				.getFontFace());
		rs.updateString(12, formQuestionAttributes.getHtmlAttributes()
				.getFontSize());
		rs.updateInt(13, formQuestionAttributes.getHtmlAttributes().getIndent());
		String op = formQuestionAttributes.getRangeOperator();
		if (op == null) {
			rs.updateInt(14, 0);
		} else {
			rs.updateInt(14, Integer.parseInt(op));
		}

		rs.updateString(15, formQuestionAttributes.getRangeValue1());
		rs.updateString(16, formQuestionAttributes.getRangeValue2());
		rs.updateInt(18, formQuestionAttributes.getUpdatedBy());
		rs.updateTimestamp(19,
				new java.sql.Timestamp(System.currentTimeMillis()));
		rs.updateInt(20, formQuestionAttributes.getAnswerType().getValue());
		rs.updateInt(21, formQuestionAttributes.getMinCharacters());
		rs.updateInt(22, formQuestionAttributes.getMaxCharacters());
		rs.updateBoolean(23, formQuestionAttributes.isHorizontalDisplay());
		rs.updateInt("textareaheight",
				formQuestionAttributes.getTextareaHeight());
		rs.updateInt("textareawidth", formQuestionAttributes.getTextareaWidth());
		rs.updateInt("textboxlength", formQuestionAttributes.getTextboxLength());
		rs.updateBoolean("horizDisplayBreak",
				formQuestionAttributes.isHorizDisplayBreak());
		if (formQuestionAttributes.getEmailTrigger().getId() != Integer.MIN_VALUE
				&& !formQuestionAttributes.isDeleteTrigger()) {
			rs.updateInt("emailtriggerid", formQuestionAttributes
					.getEmailTrigger().getId());
		} else {
			rs.updateNull("emailTriggerId");
		}
		rs.updateBoolean("dataspring", formQuestionAttributes.isDataSpring());
		if (formQuestionAttributes.getHtmlText() != null) {
			rs.updateString("xhtmltext", formQuestionAttributes.getHtmlText());
		} else {
			rs.updateNull("xhtmltext");
		}

		if (formQuestionAttributes.getDataElementName().equals("none")) { 
			rs.updateNull("data_element_name");
			rs.updateNull("group_name");
		} else {

			String fullName = formQuestionAttributes.getDataElementName();
			
			String g = fullName.substring(0, fullName.indexOf("."));
			
			String d = fullName.substring(fullName.indexOf(".")+1,fullName.length());
			
			
			
			rs.updateString("data_element_name",d);
			rs.updateString("group_name",g);
			
		}
		
		if (formQuestionAttributes.isPrepopulation()) {
			rs.updateBoolean("prepopulation", formQuestionAttributes.isPrepopulation());
			rs.updateString("prepopulationValue", formQuestionAttributes.getPrepopulationValue());
		}
		else {
			rs.updateBoolean("prepopulation", formQuestionAttributes.isPrepopulation());
			rs.updateNull("prepopulationValue");
		}

		rs.updateInt("decimalprecision", formQuestionAttributes.getDecimalPrecision());

		if (formQuestionAttributes.isHasUnitConversionFactor()) {
			rs.updateBoolean("hasconversionfactor", formQuestionAttributes.isHasUnitConversionFactor());
			rs.updateString("conversionfactor", formQuestionAttributes.getUnitConversionFactor());
		}
		else {
			rs.updateBoolean("hasconversionfactor", formQuestionAttributes.isHasUnitConversionFactor());
			rs.updateNull("conversionfactor");
		}
		
		
		if(formQuestionAttributes.getTableHeaderType() == 0) {
			rs.updateNull("tableheadertype");
		}else {
			rs.updateInt("tableheadertype",formQuestionAttributes.getTableHeaderType());
		}
		
		rs.updateBoolean("showtext", formQuestionAttributes.isShowText());
	}

	private void updateAttachedCalculatedQuestions(Question question)
			throws CtdbException {
		PreparedStatement stmt = null;

		try {
			// update question attributes
			StringBuffer sql = new StringBuffer(200);
			sql.append("delete from calculatequestion where questionid = ? and sectionid = ?");

			stmt = this.conn.prepareStatement(sql.toString());
			stmt.setLong(1, question.getId());
			stmt.setLong(2, question.getFormQuestionAttributes().getSectionId());
			stmt.executeUpdate();
			if (question.getFormQuestionAttributes().isCalculatedQuestion()) {
				String calcQid = "";
				for (Object tempObj : question.getCalculatedFormQuestionAttributes().getQuestionsToCalculate()) {
					if(tempObj instanceof String){
						calcQid = (String) tempObj;
					}else{
						Question tempQ = (Question) tempObj;
						calcQid = "[S_"+question.getFormQuestionAttributes().getSectionId()+"_Q_"+String.valueOf(tempQ.getId())+"]";
						
					}
					String[] calSectionIdQuestionId = calcQid.split("_");
					int calSectionId = Integer
							.parseInt(calSectionIdQuestionId[1]);
					int calQuestionId;
					if (calSectionIdQuestionId[3].indexOf("]") > -1) {
						calQuestionId = Integer
								.parseInt(calSectionIdQuestionId[3].substring(
										0,
										calSectionIdQuestionId[3].indexOf("]")));
					} else {
						calQuestionId = Integer
								.parseInt(calSectionIdQuestionId[3].substring(
										0, calSectionIdQuestionId[3].length()));
					}
					
					StringBuffer insql = new StringBuffer(200);
					insql.append("insert into calculatequestion (questionid, calculatequestionid, sectionid, calculatesectionid) values (?,?,?,?)");

					PreparedStatement pstmt = this.conn.prepareStatement(insql
							.toString());
					pstmt.setLong(1, question.getId());
					pstmt.setLong(2, calQuestionId);
					pstmt.setLong(3, question.getFormQuestionAttributes()
							.getSectionId());
					pstmt.setLong(4, calSectionId);

					pstmt.execute();
				}

			}
		} catch (Exception e) {
			throw new CtdbException(
					"Unable to update the question attributes: "
							+ e.getMessage(), e);
		} finally {
			this.close(stmt);
		}
	}

	/**
	 * Retrieves a form for the protocol based on the unique ID. The form is
	 * considered as minimun form for only having id, name, description, or
	 * something which can be added later. The form definitely can't have
	 * section and questions retrieved.
	 * 
	 * @param formId
	 *            The Form ID to retrieve
	 * @return Form data object
	 * @throws ObjectNotFoundException
	 *             Thrown if the protocol form does not exist
	 * @throws CtdbException
	 *             Thrown if any other errors occur while processing
	 */

	public Form getMiniForm(int formId) throws ObjectNotFoundException,
			CtdbException {

		PreparedStatement stmt = null;
		ResultSet rs = null;
		Form form = null;
		try {
			StringBuffer sql = new StringBuffer(50);

			sql.append(" SELECT DISTINCT CASE WHEN patientid IS null THEN 0 ELSE 1 END isadministered, form.attachfiles,");
			sql.append(" form.formid, form.name, form.description, form.version, form.dataentryflag, form.publicflag, xformstatus.*, tmp.questioncnt ");
			sql.append(" from form");
			sql.append(" JOIN xformstatus");
			sql.append(" ON form.xstatusid = xformstatus.xstatusid");
			sql.append(" LEFT OUTER JOIN administeredform");
			sql.append(" ON form.formid = administeredform.formid");
			sql.append(" LEFT OUTER JOIN");
			sql.append("    ( select a.formid, count(*) questioncnt ");
			sql.append("        from section a, sectionquestion b ");
			sql.append("       where a.sectionid = b.sectionid ");
			sql.append("       group by a.formid ) tmp ");
			sql.append(" ON form.formid = tmp.formid");
			sql.append(" where form.formid = ? ");

			stmt = this.conn.prepareStatement(sql.toString());
			stmt.setLong(1, formId);

			rs = stmt.executeQuery();
			if (!rs.next()) {
				throw new ObjectNotFoundException("The form with ID: " + formId
						+ " could not be found.");
			} else {
				form = new Form();
				form.setId(rs.getInt("formid"));
				form.setName(rs.getString("name"));
				form.setDescription(rs.getString("description"));
				int ver = rs.getInt("version");
				Version version = new Version(ver);
				form.setVersion(version);
				form.setSingleDoubleKeyFlag(rs.getInt("dataEntryFlag"));

				if (rs.getBoolean("publicFlag")) {
					form.setAccessFlag(2);
				}
				else {
					form.setAccessFlag(1);
				}

				CtdbLookup status = new CtdbLookup(rs.getInt("xstatusid"),
						rs.getString("shortname"), null);
				form.setStatus(status);

				if (rs.getInt("isadministered") == 1) {
					form.setIsAdministered(true);
				} else {
					form.setIsAdministered(false);
				}
				if (rs.getString("attachfiles").equals("true")) {
					form.setAttachFiles(true);
				}

				form.setNumQuestions(rs.getInt("questioncnt"));
			}

			return form;
		} catch (SQLException e) {
			throw new CtdbException("Unable to get protocol form: "
					+ e.getMessage(), e);
		} finally {
			this.close(rs);
			this.close(stmt);
		}
	}

	/**
	 * Retrieves a current form with the most recent version for the protocol
	 * based on the unique identifier
	 * 
	 * @param formId
	 *            The Form ID to retrieve
	 * @return Form data object
	 * @throws ObjectNotFoundException
	 *             is thrown if the protocol form does not exist
	 * @throws CtdbException
	 *             thrown if any other errors occur while processing
	 */
	public Form getForm(int formId) throws ObjectNotFoundException, CtdbException {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		Form form = null;

		try {
			String sql = "select distinct CASE WHEN patientid IS null THEN 0 ELSE 1 END isadministered, form.name, form.formid, form.description, " +
				"form.xstatusid, form.createdby, form.createddate, form.updatedby, form.updateddate, form.version, form.checkedoutby, form.checkedoutdate, " +
				"form.lockflag, form.formborder, form.sectionborder, form.formnamefont, form.formnamecolor, form.protocolid, form.orderval, " +
				"form.importeddate, form.importfilename, form.sectionnamefont, form.sectionnamecolor, form.dataentryflag, form.publicflag, form.header, " +
				"form.footer, form.fontsize, form.dataentryworkflowtype, form.cellpadding, form.attachfiles, form.enabledataspring, form.tabdisplay, " +
				"form.formtypeid, form.data_structure_name ,form.copyright,form.allow_multiple_collection_instances, form.islegacy, form.data_structure_version, " +
				"xformstatus.*, tmp.questioncnt from form JOIN xformstatus ON form.xstatusid = xformstatus.xstatusid LEFT OUTER JOIN " +
				"administeredform ON form.formid = administeredform.formid LEFT OUTER JOIN (select a.formid, count(*) questioncnt from section a, " +
				"sectionquestion b where a.sectionid = b.sectionid group by a.formid) tmp ON form.formid = tmp.formid  where form.formid = ? ";

			stmt = this.conn.prepareStatement(sql);
			stmt.setLong(1, formId);

			rs = stmt.executeQuery();
			
			if (!rs.next()) {
				throw new ObjectNotFoundException("The form with ID: " + formId + " could not be found.");
			}
			
			form = this.rsToForm(rs);
			form.setIsAdministered(rs.getInt("isadministered") == 1);
			form.setNumQuestions(rs.getInt("questioncnt"));
			form.setDataStructureName(rs.getString("data_structure_name"));
			form.setProtocolId(rs.getInt("protocolid"));
			form.setRowList(getSections(formId));
			form.setIntervalList(getIntervals(formId));
			form.setCopyRight(Boolean.parseBoolean(rs.getString("copyright")));
			form.setAllowMultipleCollectionInstances(Boolean.valueOf(rs.getString("allow_multiple_collection_instances")));
			if(rs.getObject("islegacy") == null) {
				form.setLegacy(true);
			}else {
				form.setLegacy(rs.getBoolean("islegacy")); 
			}
			
			
			
			if (rs.getBoolean("publicFlag")) {
				form.setAccessFlag(2);
			}
			else {
				form.setAccessFlag(1);
			}
		}
		catch (SQLException e) {
			throw new CtdbException("Unable to get protocol form: "
					+ e.getMessage(), e);
		}
		finally {
			this.close(rs);
			this.close(stmt);
		}
		
		return form;
	}

	/**
	 * Retrieves a form based on the unique ID for form layout page. The form
	 * has all its sections without questions.
	 * 
	 * @param formId
	 *            The Form ID to retrieve
	 * @return Form data object
	 * @throws ObjectNotFoundException
	 *             is thrown if the protocol form does not exist
	 * @throws CtdbException
	 *             thrown if any other errors occur while processing
	 */
	public Form getFormAndSections(int formId) throws ObjectNotFoundException,
			CtdbException {
		PreparedStatement stmt = null;
		ResultSet rs = null;

		try {
			StringBuffer sql = new StringBuffer(50);

			sql.append(" select distinct CASE WHEN patientid IS null THEN 0 ELSE 1 END isadministered, ");
			sql.append(" form.name, form.formid, form.description, form.xstatusid, form.createdby, form.createddate, form.updatedby, form.updateddate, ");
			sql.append(" form.version, form.checkedoutby, form.checkedoutdate, form.lockflag, form.formborder, form.sectionborder, form.formnamefont, ");
			sql.append(" form.formnamecolor, form.protocolid, form.orderval, form.importeddate, form.importfilename, form.sectionnamefont, form.sectionnamecolor, ");
			sql.append(" form.dataentryflag, form.publicflag, form.header, form.footer, form.fontsize, form.dataentryworkflowtype, form.cellpadding ");
			sql.append(" ,form.attachfiles, form.enabledataspring,form.copyright,form.allow_multiple_collection_instances, form.islegacy, form.data_structure_name, form.data_structure_version, tabdisplay, formtypeid ");
			sql.append(" , xformstatus.*, tmp.questioncnt ");
			sql.append(" from form ");
			sql.append(" JOIN xformstatus ON form.xstatusid = xformstatus.xstatusid ");
			sql.append(" LEFT OUTER JOIN administeredform ON form.formid = administeredform.formid ");
			sql.append(" LEFT OUTER JOIN ");
			sql.append("    ( select a.formid, count(*) questioncnt ");
			sql.append("        from section a, sectionquestion b ");
			sql.append("       where a.sectionid = b.sectionid ");
			sql.append("       group by a.formid ) tmp ");
			sql.append(" ON form.formid = tmp.formid ");
			sql.append(" where form.formid = ? ");

			stmt = this.conn.prepareStatement(sql.toString());
			stmt.setLong(1, formId);

			rs = stmt.executeQuery();
			if (!rs.next()) {
				throw new ObjectNotFoundException("The form with ID: " + formId
						+ " for the form layout could not be found.");
			}
			Form form = this.rsToForm(rs);

			form.setRowList(getSections(formId));
			return form;
		} catch (SQLException e) {
			throw new CtdbException(
					"Unable to get protocol form for form layout: "
							+ e.getMessage(), e);
		} finally {
			this.close(rs);
			this.close(stmt);
		}
	}

	/**
	 * Retrieves a form for the protocol based on the formId and formVersion
	 * 
	 * @param formId
	 *            The Form ID to retrieve
	 * @param formVersion
	 *            The Form version to retrieve
	 * @return Form data object
	 * @throws ObjectNotFoundException
	 *             is thrown if the protocol form does not exist
	 * @throws CtdbException
	 *             thrown if any other errors occur while processing
	 */
	public Form getForm(int formId, int formVersion)
			throws ObjectNotFoundException, CtdbException {
		PreparedStatement stmt = null;
		ResultSet rs = null;

		try {
			String sql = "select distinct case when af.patientid is null then 0 else 1 end as isadministered, fv.*, xfs.* from form_view fv join " +
					"xformstatus xfs on fv.xstatusid = xfs.xstatusid left outer join administeredform af on fv.formid = af.formid " +
					"where fv.formid = ? and fv.version = ? ";

			stmt = this.conn.prepareStatement(sql);
			stmt.setLong(1, formId);
			stmt.setInt(2, formVersion);

			rs = stmt.executeQuery();
			if (!rs.next()) {
				throw new ObjectNotFoundException("The form with ID: " + formId
						+ " and version: " + formVersion
						+ " could not be found.");
			}
			Form form = this.rsToForm(rs);
			if (rs.getInt("isadministered") == 1) {
				form.setIsAdministered(true);
			} else {
				form.setIsAdministered(false);
			}

			form.setNumQuestions(this.getNumberQuestions(formId, formVersion));
			form.setRowList(this.getSections(formId, formVersion));
			form.setIntervalList(this.getIntervals(formId));
			return form;
		} catch (SQLException e) {
			throw new CtdbException("Unable to get protocol form: "
					+ e.getMessage(), e);
		} finally {
			this.close(rs);
			this.close(stmt);
		}
	}

	/**
	 * Retrieves number of questions in a form
	 * 
	 * @param formId
	 *            The form ID
	 * @param formVersion
	 *            The form version
	 * @return number of questions
	 * @throws CtdbException
	 *             thrown if any errors occur while processing
	 */
	public int getNumberQuestions(int formId, int formVersion)
			throws CtdbException {
		PreparedStatement stmt = null;
		ResultSet rs = null;

		try {
			StringBuffer sql = new StringBuffer(50);

			sql.append("select count(*) from section_view a, sectionquestion_view b ");
			sql.append("where a.formid = ? and a.formversion = ? and a.sectionid = b.sectionid ");
			sql.append("and a.formid = b.formid and a.formversion = b.formversion ");
			stmt = this.conn.prepareStatement(sql.toString());
			stmt.setLong(1, formId);
			stmt.setInt(2, formVersion);
			rs = stmt.executeQuery();
			int numQuestions = 0;
			if (rs.next()) {
				numQuestions = rs.getInt(1);
			}

			return numQuestions;
		} catch (SQLException e) {
			throw new CtdbException("Unable to get protocol form: "
					+ e.getMessage(), e);
		} finally {
			this.close(rs);
			this.close(stmt);
		}
	}
	
	
	
	public int getNumberQuestionsWithoutRepeatable(int formId)
			throws CtdbException {
		PreparedStatement stmt = null;
		ResultSet rs = null;

		try {
			StringBuffer sql = new StringBuffer(50);
			sql.append(" SELECT (select count(*) from form f,section s, sectionquestion sq ");
			sql.append(" where f.formid=s.formid and s.sectionid = sq.sectionid and f.formid=? ");
					sql.append(" and s.isrepeatable = 't' and s.repeatedsectionparent = -1)+ ");
							sql.append(" (select count(*) from form f,section s, sectionquestion sq ");
									sql.append(" where f.formid=s.formid and s.sectionid = sq.sectionid ");
											sql.append(" and f.formid=? and s.isrepeatable = 'f' ) AS questioncount ");
			stmt = this.conn.prepareStatement(sql.toString());
			stmt.setLong(1, formId);
			stmt.setLong(2, formId);
			rs = stmt.executeQuery();
			int numQuestions = 0;
			if (rs.next()) {
				numQuestions = rs.getInt(1);
			}

			return numQuestions;
		} catch (SQLException e) {
			throw new CtdbException("Unable to get protocol form: "
					+ e.getMessage(), e);
		} finally {
			this.close(rs);
			this.close(stmt);
		}
	}

	/**
	 * Retrieves forms with only form names and ids for the protocol based on
	 * the unique protocol ID
	 * 
	 * @param protocolId
	 *            The Protocol ID for retrieving all its forms
	 * @return A list of all forms for the protocol
	 * @throws CtdbException
	 *             thrown if any errors occur while processing
	 */
	public List<Form> getFormIdNames(int protocolId) throws CtdbException {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		List<Form> forms = new ArrayList<Form>();

		try {
			StringBuffer sql = new StringBuffer(50);

			sql.append("select distinct CASE WHEN patientid IS null THEN 0 ELSE 1 END isadministered, ");
			sql.append("form.formid, form.name, form.orderval ");
			sql.append("from form");
			sql.append("JOIN xformstatus ON form.xstatusid = xformstatus.xstatusid ");
			sql.append("LEFT OUTER JOIN administeredform");
			sql.append("ON form.formid = administeredform.formid");
			sql.append("WHERE form.formtypeid = 10 AND protocolid = ? ");
			sql.append("order by orderval");

			stmt = this.conn.prepareStatement(sql.toString());
			stmt.setLong(1, protocolId);
			rs = stmt.executeQuery();

			while (rs.next()) {
				Form form = new Form();
				form.setId(rs.getInt("formid"));
				form.setName(rs.getString("name"));

				forms.add(form);
			}
		}
		catch (SQLException e) {
			throw new CtdbException("Unable to get protocol Forms: " + e.getMessage(), e);
		}
		finally {
			this.close(rs);
			this.close(stmt);
		}
		
		return forms;
	}

	/**
	 * Retrieves forms for the protocol based on the unique protocol ID
	 * 
	 * @param protocolId
	 *            The Protocol ID for retrieving all its forms
	 * @return A list of all forms for the protocol
	 * @throws CtdbException
	 *             thrown if any errors occur while processing
	 */
	public List<Form> getForms(int protocolId) throws CtdbException {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		List<Form> forms = new ArrayList<Form>();

		try {
			StringBuffer sql = new StringBuffer(50);

			sql.append("select distinct CASE WHEN patientid IS null THEN 0 ELSE 1 END isadministered, ");
			sql.append("form.name, form.formid, form.description, form.xstatusid, form.createdby, form.createddate, form.updatedby, form.updateddate, ");
			sql.append(" form.version, form.checkedoutby, form.checkedoutdate, form.lockflag, form.formborder, form.sectionborder, form.formnamefont, ");
			sql.append(" form.formnamecolor, form.protocolid, form.orderval, form.importeddate, form.importfilename,  form.sectionnamefont, form.sectionnamecolor, ");
			sql.append(" form.dataentryflag, form.publicflag, form.header, form.footer, form.fontsize, form.dataentryworkflowtype, form.cellpadding ");
			sql.append(" ,form.attachfiles, form.enabledataspring, form.tabdisplay, form.formtypeid ");
			sql.append(" , xformstatus.*, tmp.questioncnt ");
			sql.append("from form");
			sql.append("JOIN xformstatus ON form.xstatusid = xformstatus.xstatusid");
			sql.append("LEFT OUTER JOIN administeredform ON form.formid = administeredform.formid");
			sql.append("LEFT OUTER JOIN ");
			sql.append("   ( select a.formid, count(*) questioncnt ");
			sql.append("       from section a, sectionquestion b ");
			sql.append("      where a.sectionid = b.sectionid ");
			sql.append("      group by a.formid ) tmp");
			sql.append("ON form.formid = tmp.formid");
			sql.append("where protocolid = ? ");
			sql.append("and form.formtypeid = 10");
			sql.append("order by orderval");

			stmt = this.conn.prepareStatement(sql.toString());
			stmt.setLong(1, protocolId);
			rs = stmt.executeQuery();

			while (rs.next()) {
				Form form = this.rsToForm(rs);

				if (rs.getInt("isadministered") == 1) {
					form.setIsAdministered(true);
				} else {
					form.setIsAdministered(false);
				}

				form.setNumQuestions(rs.getInt("questioncnt"));

				forms.add(form);
			}
		}
		catch (SQLException e) {
			throw new CtdbException("Unable to get protocol Forms: " + e.getMessage(), e);
		}
		finally {
			this.close(rs);
			this.close(stmt);
		}
		
		return forms;
	}

	/**
	 * Retrieves forms for the protocol based on the unique protocol ID
	 * 
	 * @param protocolId
	 *            The Protocol ID for retrieving all its forms
	 * @return A list of all forms for the protocol
	 * @throws CtdbException
	 *             thrown if any errors occur while processing
	 */
	public List<Form> getStudyForms(int protocolId) throws CtdbException {
		PreparedStatement stmt = null;
		ResultSet rs = null;

		try {
			List<Form> forms = new ArrayList<Form>();

			StringBuffer sql = new StringBuffer(50);
			sql.append("select distinct CASE WHEN administeredform.patientid IS null THEN 0 ELSE 1 END isadministered, ");
			sql.append("form.name, form.formid, form.description, form.xstatusid, form.createdby, form.createddate, form.updatedby, form.updateddate, ");
			sql.append(" form.version, form.checkedoutby, form.checkedoutdate, form.lockflag, form.formborder, form.sectionborder, form.formnamefont, ");
			sql.append(" form.formnamecolor, form.protocolid, form.orderval, form.importeddate, form.importfilename,  form.sectionnamefont, form.sectionnamecolor, ");
			sql.append(" form.dataentryflag, form.publicflag, form.header, form.footer, form.fontsize, form.dataentryworkflowtype, form.cellpadding ");
			sql.append(" ,form.attachfiles, form.enabledataspring, form.tabdisplay, form.formtypeid, form.copyright, form.data_structure_name, form.data_structure_version ");
			sql.append(" , xformstatus.*, tmp.questioncnt ");
			sql.append(" from form ");
			sql.append(" JOIN xformstatus ON form.xstatusid = xformstatus.xstatusid ");
			sql.append(" LEFT OUTER JOIN administeredform ON form.formid = administeredform.formid ");
			sql.append(" LEFT OUTER JOIN ");
			sql.append("   ( select fi.formid, fi.intervalid ");
			sql.append("       from form_interval fi, interval i");
			sql.append("      where i.protocolid = ? and fi.intervalid = i.intervalid ) formInterval ");
			sql.append(" ON form.formid = formInterval.formid ");
			sql.append(" LEFT OUTER JOIN ");
			sql.append("   ( select a.formid, count(*) questioncnt ");
			sql.append("       from section a, sectionquestion b ");
			sql.append("      where a.sectionid = b.sectionid ");
			sql.append("      group by a.formid ) tmp ");
			sql.append(" ON form.formid = tmp.formid ");
			sql.append(" where protocolid = ? ");
			sql.append(" and form.formtypeid in (10,11,12,13,14,15) ");
			sql.append(" and form.xstatusid in (3,4) ");  // Form is active
			sql.append(" order by orderval ");

			stmt = this.conn.prepareStatement(sql.toString());
			stmt.setLong(1, protocolId);
			stmt.setLong(2, protocolId);
			rs = stmt.executeQuery();

			while (rs.next()) {
				Form form = this.rsToForm(rs);

				if (rs.getInt("isadministered") == 1) {
					form.setIsAdministered(true);
				} else {
					form.setIsAdministered(false);
				}

				form.setNumQuestions(rs.getInt("questioncnt"));
				forms.add(form);
			}
			return forms;
		} catch (SQLException e) {
			throw new CtdbException("Unable to get study Forms: "
					+ e.getMessage(), e);
		} finally {
			this.close(rs);
			this.close(stmt);
		}
	}
	
	/**
	 * Retrieves a list of only subject forms associated with a specific study.
	 * 
	 * @param studyId - The ID of the associated study
	 * @return	A list of Form objects.
	 * @throws CtdbException	When a database error occurs during the query.
	 */
	public List<Form> getStudySubjectForms(long studyId) throws CtdbException
	{
		PreparedStatement stmt = null;
		ResultSet rs = null;
		Form f = null;
		List<Form> formList = new ArrayList<Form>();
		
		String sql = "select distinct CASE WHEN administeredform.patientid IS null THEN 0 ELSE 1 END isadministered, form.name, form.formid, " +
					 "form.description, form.xstatusid, form.createdby, form.createddate, form.updatedby, form.updateddate, form.version, " +
					 "form.checkedoutby, form.checkedoutdate, form.lockflag, form.formborder, form.sectionborder, form.formnamefont, " +
					 "form.formnamecolor, form.protocolid, form.orderval, form.importeddate, form.importfilename,  form.sectionnamefont, " +
					 "form.sectionnamecolor, form.dataentryflag, form.publicflag, form.header, form.footer, form.fontsize, form.dataentryworkflowtype, " +
					 "form.cellpadding,form.attachfiles, form.enabledataspring, form.tabdisplay, form.formtypeid, form.copyright,form.allow_multiple_collection_instances, " +
					 "form.data_structure_name, form.data_structure_version, xformstatus.*, tmp.questioncnt from form " +
					 "JOIN xformstatus ON form.xstatusid = xformstatus.xstatusid " +
					 "LEFT OUTER JOIN administeredform ON form.formid = administeredform.formid " +
					 "LEFT OUTER JOIN ( select fi.formid, fi.intervalid from form_interval fi, interval i where i.protocolid = ? and " +
					 "fi.intervalid = i.intervalid ) formInterval ON form.formid = formInterval.formid " +
					 "LEFT OUTER JOIN ( select a.formid, count(*) questioncnt from section a, sectionquestion b where a.sectionid = b.sectionid " +
					 "group by a.formid ) tmp ON form.formid = tmp.formid " +
					 "where protocolid = ? and " +
					 "form.formtypeid = 10 and " +
					 "form.xstatusid in (3,4) " +  // Form is active
					 "order by orderval ";
		
		try {
			stmt = conn.prepareStatement(sql);
			stmt.setLong(1, studyId);
			stmt.setLong(2, studyId);
			rs = stmt.executeQuery();
			
			// Go through the results and create the Form objects
			while ( rs.next() )
			{
				f = rsToForm(rs);
				
				// Set administered flag
				if ( rs.getInt("isadministered") == 1 )
				{
					f.setIsAdministered(true);
				}
				else
				{
					f.setIsAdministered(false);
				}
				
				// Set the number of questions
				f.setNumQuestions(rs.getInt("questioncnt"));
				
				formList.add(f);
			}
		}
		catch ( SQLException sqle )
		{
			throw new CtdbException("Unable to get study Forms: " + sqle.getLocalizedMessage(), sqle);
		}
		finally
		{
			close(rs);
			close(stmt);
		}
		
		return formList;
	}

	/**
	 * Retrieves forms for the protocol based on the unique protocol ID
	 * 
	 * @param protocolId
	 *            The Protocol ID for retrieving all its forms
	 * @return A list of all forms for the protocol
	 * @throws CtdbException
	 *             thrown if any errors occur while processing
	 */
	public Form getForm(int protocolId, String name) throws CtdbException {
		PreparedStatement stmt = null;
		ResultSet rs = null;

		try {
			StringBuffer sql = new StringBuffer(50);

			sql.append("select distinct CASE WHEN patientid IS null THEN 0 ELSE 1 END isadministered, ");
			sql.append("form.name, form.formid, form.description, form.xstatusid, form.createdby, form.createddate, form.updatedby, form.updateddate, ");
			sql.append(" form.version, form.checkedoutby, form.checkedoutdate, form.lockflag, form.formborder, form.sectionborder, form.formnamefont, ");
			sql.append(" form.formnamecolor, form.protocolid, form.orderval, form.importeddate, form.importfilename,  form.sectionnamefont, form.sectionnamecolor, ");
			sql.append(" form.dataentryflag, form.publicflag, form.header, form.footer, form.fontsize, form.dataentryworkflowtype, form.cellpadding ");
			sql.append(" ,form.attachfiles, form.enabledataspring, form.tabdisplay, form.formtypeid ,form.copyright,form.allow_multiple_collection_instances, form.data_structure_name, form.data_structure_version");
			sql.append(" , xformstatus.*, tmp.questioncnt ");
			sql.append(" from form ");
			sql.append(" JOIN xformstatus ");
			sql.append(" ON form.xstatusid = xformstatus.xstatusid ");
			sql.append(" LEFT OUTER JOIN administeredform ");
			sql.append(" ON form.formid = administeredform.formid ");
			sql.append(" ");
			sql.append(" LEFT OUTER JOIN ");
			sql.append("   ( select a.formid, count(*) questioncnt ");
			sql.append("       from section a, sectionquestion b ");
			sql.append("      where a.sectionid = b.sectionid ");
			sql.append("      group by a.formid ) tmp ");
			sql.append(" ON form.formid = tmp.formid ");
			sql.append(" where protocolid = ? ");
			sql.append(" order by orderval");
			
			stmt = this.conn.prepareStatement(sql.toString());
			stmt.setLong(1, protocolId);
			rs = stmt.executeQuery();
			Form result = null;
			
			while (rs.next()) {
				Form form = this.rsToForm(rs);

				if (rs.getInt("isadministered") == 1) {
					form.setIsAdministered(true);
				} else {
					form.setIsAdministered(false);
				}

				form.setNumQuestions(rs.getInt("questioncnt"));

				if (form.getName().equals(name)) {
					result = form;
					break;
				}

				form.setCopyRight(Boolean.parseBoolean(rs
						.getString("copyright")));
				form.setAllowMultipleCollectionInstances(Boolean.valueOf(rs.getString("allow_multiple_collection_instances")));
			}
			
			return result;
		} catch (SQLException e) {
			throw new CtdbException("Unable to get protocol Forms: "
					+ e.getMessage(), e);
		} finally {
			this.close(rs);
			this.close(stmt);
		}
	}

	/**
	 * Retrieves active forms for the protocol based on the unique protocol ID
	 * 
	 * @param protocolId
	 *            The Protocol ID for retrieving all its active forms
	 * @return A list of all active forms for the protocol
	 * @throws CtdbException
	 *             thrown if any errors occur while processing
	 */
	public List<Form> getActiveAndInProgressForms(int protocolId) throws CtdbException {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		List<Form> forms = new ArrayList<Form>();

		try {
			StringBuffer sql = new StringBuffer(50);

			/*sql.append("select form.name, form.formid ");
			sql.append(" FROM form");
			sql.append(" where protocolid = ? ");
			sql.append(" and formtypeid = 10 ");
			sql.append("	and (xstatusid = 3 or xstatusid = 4)");*/
			
			
			sql.append("select eform.name, eform.eformid ");
			sql.append(" FROM eform");
			sql.append(" where protocolid = ? ");

			
			stmt = this.conn.prepareStatement(sql.toString());
			stmt.setLong(1, protocolId);
			rs = stmt.executeQuery();
			
			while (rs.next()) {

				Form form = new Form();
				form.setId(rs.getInt("eformid"));
				form.setName(rs.getString("name"));

				forms.add(form);
			}
		}
		catch (SQLException e) {
			throw new CtdbException("Unable to get protocol Forms: " + e.getMessage(), e);
		}
		finally {
			this.close(rs);
			this.close(stmt);
		}
		
		return forms;
	}

	/**
	 * Retrieves active forms for the protocol based on the unique protocol ID
	 * 
	 * @param protocolId
	 *            The Protocol ID for retrieving all its active forms
	 * @return A list of all active forms for the protocol
	 * @throws CtdbException
	 *             thrown if any errors occur while processing
	 */
	public List<Form> getActiveForms(int protocolId) throws CtdbException {

		PreparedStatement stmt = null;
		ResultSet rs = null;
		List<Form> forms = new ArrayList<Form>();

		try {
			StringBuffer sql = new StringBuffer(50);

			sql.append("select distinct CASE WHEN patientid IS null THEN 0 ELSE 1 END isadministered, ");
			sql.append("form.name, form.formid, form.description, form.xstatusid, form.createdby, form.createddate, form.updatedby, form.updateddate, ");
			sql.append("form.version, form.checkedoutby, form.checkedoutdate, form.lockflag, form.formborder, form.sectionborder, form.formnamefont, ");
			sql.append("form.formnamecolor, form.protocolid, form.orderval, form.importeddate, form.importfilename,  form.sectionnamefont,  form.sectionnamecolor,");
			sql.append("form.dataentryflag, form.publicflag, form.header, form.footer, form.fontsize, form.dataentryworkflowtype, form.cellpadding ");
			sql.append(",form.attachfiles , form.enabledataspring , form.tabdisplay , form.formtypeid , form.copyright,form.allow_multiple_collection_instances, form.data_structure_name, form.data_structure_version ");
			sql.append(", xformstatus.*, tmp.questioncnt ");
			sql.append(" FROM form");
			sql.append(" LEFT JOIN xformstatus ON (");
			sql.append("	form.xstatusid = xformstatus.xstatusid");
			sql.append("	and (form.xstatusid = 3");
			sql.append("		or form.xstatusid = 4))");
			sql.append(" LEFT OUTER JOIN (SELECT a.formid, count(*) as questioncnt");
			sql.append("       from section a, sectionquestion b");
			sql.append("      where a.sectionid = b.sectionid ");
			sql.append("      group by a.formid ) as tmp ");
			sql.append(" on form.formid = tmp.formid");
			sql.append(" LEFT OUTER JOIN administeredform");
			sql.append(" on form.formid = administeredform.formid");
			sql.append(" where protocolid = ?");
			sql.append(" and formtypeid = 10 ");
			sql.append(" order by orderval");

			stmt = this.conn.prepareStatement(sql.toString());
			stmt.setLong(1, protocolId);
			rs = stmt.executeQuery();
			
			while (rs.next()) {
				Form form = this.rsToForm(rs);

				if (rs.getInt("isadministered") == 1) {
					form.setIsAdministered(true);
				} else {
					form.setIsAdministered(false);
				}

				form.setNumQuestions(rs.getInt("questioncnt"));

				forms.add(form);
			}
		}
		catch (SQLException e) {
			throw new CtdbException("Unable to get protocol Forms: " + e.getMessage(), e);
		}
		finally {
			this.close(rs);
			this.close(stmt);
		}
		
		return forms;
	}

	/**
	 * Retrieves active forms for the protocol based on the unique protocol ID
	 * 
	 * @param protocolIds
	 *            The Protocol ID for retrieving all its active forms
	 * @return A list of all active forms for the protocol
	 * @throws CtdbException
	 *             thrown if any errors occur while processing
	 */
	public List<Form> getAllForms(Set<String> protocolIds, boolean activeFlag) throws CtdbException {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		activeFlag = false; // Current implementation only seach Active Form
							// (csw: 04/14/2007)
		StringBuffer sql = new StringBuffer(50);
		List<Form> forms = new ArrayList<Form>();

		try {
			sql.append("SELECT distinct CASE WHEN patientid IS null THEN 0 ELSE 1 END isadministered ");
			sql.append(" ,form.name, form.formid, form.description, form.xstatusid, form.createdby, form.createddate, form.updatedby, form.updateddate ");
			sql.append(" ,form.version, form.checkedoutby, form.checkedoutdate, form.lockflag, form.formborder, form.sectionborder, form.formnamefont ");
			sql.append(" ,form.formnamecolor, form.protocolid, form.orderval, form.importeddate, form.importfilename,  form.sectionnamefont,  form.sectionnamecolor ");
			sql.append(" ,form.dataentryflag, form.publicflag, form.header, form.footer, form.fontsize, form.dataentryworkflowtype, form.cellpadding ");
			sql.append(" ,form.attachfiles, form.enabledataspring, form.tabdisplay, form.formtypeid ");
			sql.append(" ,xformstatus.*, tmp.questioncnt ");
			sql.append(" FROM form");
			sql.append(" JOIN xformstatus ");
			sql.append(" ON form.xstatusid = xformstatus.xstatusid ");
			sql.append(" and (form.xstatusid = 3 ");
			sql.append(" or form.xstatusid = 4) ");
			sql.append(" LEFT OUTER JOIN administeredform ON form.formid = administeredform.formid");
			sql.append(" LEFT OUTER JOIN ");
			sql.append("   ( select a.formid, count(*) questioncnt ");
			sql.append("       from section a, sectionquestion b ");
			sql.append("      where a.sectionid = b.sectionid ");
			sql.append("      group by a.formid ) tmp ");
			sql.append(" ON form.formid = tmp.formid");
			sql.append(" WHERE protocolid in (");
			
			for (Iterator<String> iter = protocolIds.iterator(); iter.hasNext();) {
				sql.append(iter.next());
				
				if (iter.hasNext()) {
					sql.append(", ");
				}
				else {
					sql.append(" ) ");
				}
			}
			
			sql.append(" and form.formtypeid = 10");
			sql.append(" order by orderval");

			stmt = this.conn.prepareStatement(sql.toString());
			rs = stmt.executeQuery();

			while (rs.next()) {
				Form form = this.rsToForm(rs);

				if (rs.getInt("isadministered") == 1) {
					form.setIsAdministered(true);
				} else {
					form.setIsAdministered(false);
				}

				form.setNumQuestions(rs.getInt("questioncnt"));

				forms.add(form);
			}
			
		}
		catch (SQLException e) {
			throw new CtdbException("Unable to get protocol Forms: " + e.getMessage(), e);
		}
		finally {
			this.close(rs);
			this.close(stmt);
		}
		
		return forms;
	}

	public StringBuffer getJSFormMap() throws CtdbException {
		PreparedStatement stmt = null;
		ResultSet rs = null;

		try {
			StringBuffer sql = new StringBuffer(50);
			sql.append("select formid, name, protocolid from form order by protocolid, name");

			stmt = this.conn.prepareStatement(sql.toString());
			rs = stmt.executeQuery();
			StringBuffer sb = new StringBuffer();
			sb.append("var formMap = new HashMap(); \n");
			sb.append("var formList = new Array();\n ");
			sb.append(" formList[0] = new LabelValueBean('All Forms', '"
					+ Integer.MIN_VALUE + "'); \n");
			rs.next();
			int curProtocolId = rs.getInt("protocolId");
			do {
				if (curProtocolId != rs.getInt("protocolId")) {
					sb.append("formMap.put ('" + curProtocolId
							+ "', formList ); \n");
					sb.append(" formList = new Array(); \n");
					sb.append(" formList[0] = new LabelValueBean('All Forms', '"
							+ Integer.MIN_VALUE + "'); \n");
					curProtocolId = rs.getInt("protocolId");
				}
				sb.append("formList [formList.length] = new LabelValueBean(\""
						+ rs.getString("name") + "\", '"
						+ rs.getString("formid") + "'); \n");

			} while (rs.next());
			sb.append("formMap.put ('" + curProtocolId + "', formList ); \n");

			return sb;
		} catch (SQLException e) {
			throw new CtdbException("Unable to get FormJS Map: "
					+ e.getMessage(), e);
		} finally {
			this.close(rs);
			this.close(stmt);
		}

	}

	/**
	 * gets forms with form status of external
	 * 
	 * @return A list of all active forms for the protocol
	 * @throws CtdbException
	 *             thrown if any errors occur while processing
	 */
	public List<Form> getExternalForms() throws CtdbException {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		List<Form> forms = new ArrayList<Form>();

		try {
			StringBuffer sql = new StringBuffer(50);

			sql.append("select  form.*, xformstatus.*, tmp.questioncnt ");
			sql.append(" from form");
			sql.append(" JOIN xformstatus ON form.xstatusid = xformstatus.xstatusid ");
			sql.append(" LEFT OUTER JOIN administeredform");
			sql.append(" ON form.formid = administeredform.formid");
			sql.append(" LEFT OUTER JOIN");
			sql.append("   ( select a.formid, count(*) questioncnt ");
			sql.append("       from section a, sectionquestion b ");
			sql.append("      where a.sectionid = b.sectionid ");
			sql.append("      group by a.formid ) tmp ");
			sql.append(" ON form.formid = tmp.formid");
			sql.append(" where form.xstatusid = 5");
			sql.append(" and form.formtypeid = 10 ");
			sql.append(" order by orderval");

			stmt = this.conn.prepareStatement(sql.toString());
			rs = stmt.executeQuery();

			while (rs.next()) {
				Form form = this.rsToForm(rs);
				form.setNumQuestions(rs.getInt("questioncnt"));
				form.setRowList(this.getSections(form.getId()));
				forms.add(form);
			}
			return forms;
		} catch (SQLException e) {
			throw new CtdbException("Unable to get external Forms: "
					+ e.getMessage(), e);
		} finally {
			this.close(rs);
			this.close(stmt);
		}
	}

	/**
	 * Method to get nonPatientForms without the sample type and not showing
	 * inactive forms by yogi for data collection landing page
	 * 
	 * @param protocolId
	 * @param formResultControl
	 * @return
	 * @throws CtdbException
	 */
	public List<Form> getFormsWithoutSample(int protocolId, FormResultControl formResultControl) throws CtdbException {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		List<Form> forms = new ArrayList<Form>();
		
		try {
			StringBuffer sql = new StringBuffer(50);
			sql.append("with ctssClauseQuery as (select formId as ctssFormId from form where protocolid = ? ) ");
			sql.append(" select distinct CASE WHEN patientid IS null THEN 0 ELSE 1 END isadministered, CASE WHEN tmp2.hasSkipRule IS null THEN 0 ELSE tmp2.hasSkipRule END hasskiprule,  CASE WHEN hasCalcRule IS null THEN 0 ELSE hasCalcRule END hascalcrule,   ");
			sql.append(" form.name, form.formid, form.description, form.xstatusid, form.createdby, form.createddate, form.updatedby, form.updateddate,form.updateddate as uDate, ");
			sql.append(" form.version, form.checkedoutby, form.checkedoutdate, form.lockflag, form.formborder, form.sectionborder, form.formnamefont, ");
			sql.append(" form.formnamecolor, form.protocolid, form.orderval, form.importeddate, form.importfilename,  form.sectionnamefont, form.sectionnamecolor, ");
			sql.append(" form.dataentryflag, form.publicflag, form.header, form.footer, form.fontsize, form.dataentryworkflowtype, form.cellpadding ");
			sql.append(",form.attachfiles, form.enabledataspring, form.tabdisplay, form.formtypeid, form.copyright, form.data_structure_name, form.data_structure_version,form.allow_multiple_collection_instances, xformtype.name as formTypeName ");
			sql.append(", xformstatus.shortname, tmp.questioncnt, tmp.isImageMap, ccq.ctssFormId ");
			sql.append(" from form");
			sql.append(" JOIN xformstatus ON form.xstatusid = xformstatus.xstatusid");
			sql.append(" LEFT OUTER JOIN administeredform ON form.formid = administeredform.formid");
			sql.append(" LEFT OUTER JOIN ctssClauseQuery ccq ON ccq.ctssFormId = form.formid");
			sql.append(" JOIN xformtype ON form.FORMTYPEID =  xformtype.XFORMTYPEID ");
			sql.append(" LEFT OUTER JOIN");
			sql.append("   ( select a.formid, count(*) questioncnt, max (tmptmp.sectionid) as isImageMap ");
			sql.append("       from section a");
			sql.append("       JOIN sectionquestion b ON a.sectionid = b.sectionid");
			sql.append("       LEFT OUTER JOIN ");
			sql.append("       (select distinct s.sectionid from sectionquestion s, question q where s.questionid = q.questionid and q.type = 9) tmptmp ");
			sql.append("       ON tmptmp.sectionid = a.sectionid");
			sql.append("      group by a.formid ) tmp");
			sql.append(" ON form.formid = tmp.formid");
			sql.append(" LEFT OUTER JOIN       ");
			sql.append("	(select max(CASE WHEN skipruleflag THEN 1 ELSE 0 END) as hasSkipRule, max(CASE WHEN calculatedflag THEN 1 ELSE 0 END) as hasCalcRule, s.formid from questionattributes qa, section s, form f, sectionquestion sq where ");
			sql.append("	sq.sectionid = s.sectionid and qa.questionattributesid = sq.questionattributesid and s.formid = f.formid and f.protocolid = ? group by s.formid ) tmp2 ");
			sql.append(" ON form.formid = tmp2.formid");
			sql.append(" where protocolid = ? ");
			sql.append(" and xformtype.XFORMTYPEID != "+CtdbConstants.FORM_TYPE_SAMPLE);
			sql.append(" and xformstatus.xstatusid !="+CtdbConstants.FORM_STATUS_INACTIVE);
			
			sql.append(formResultControl.getNotInClause());
			sql.append(formResultControl.getSearchClause());
			sql.append(" order by uDate desc ");
			
			stmt = this.conn.prepareStatement(sql.toString());
			stmt.setLong(1, protocolId);
			stmt.setLong(2, protocolId);
			stmt.setLong(3, protocolId);
			
			rs = stmt.executeQuery();

			while (rs.next()) {
				Form form = this.rsToForm(rs);

				int formId = form.getId();

				int[] formsGroupIds = getFormsGroups(formId);

				FormGroup fg;
				String formsGroupName = "";
				if (formsGroupIds != null && formsGroupIds.length > 0) {
					for (int i = 0; i < formsGroupIds.length; i++) {
						fg = getFormGroup(formsGroupIds[i]);
						formsGroupName = formsGroupName + fg.getName() + ", ";
					}
					formsGroupName = formsGroupName.substring(0,
							formsGroupName.lastIndexOf(","));
				}

				form.setFormGroupNames(formsGroupName);

				if (rs.getString("ctssFormId") != null) {
					form.setInCtss(true);
				}
				if (rs.getInt("isadministered") == 1) {
					form.setIsAdministered(true);
				} else {
					form.setIsAdministered(false);
				}
				if (rs.getInt("hasSkipRule") == 1) {
					form.setHasSkipRule(true);
				}
				if (rs.getInt("hasCalcRule") == 1) {
					form.setHasCalculationRule(true);
				}
				if (rs.getString("isImageMap") != null) {
					form.setHasImageMap(true);
				}
				form.setNumQuestions(rs.getInt("questioncnt"));
				form.setFormTypeName(rs.getString("formTypeName"));
				form.setUpdateDate(rs.getTimestamp("uDate"));
				forms.add(form);
			}
		}
		catch (SQLException e) {
			throw new CtdbException("Unable to get protocol Forms: " + e.getMessage(), e);
		}
		finally {
			this.close(rs);
			this.close(stmt);
		}
		
		return forms;
	}

	/**
	 * Retrieves forms for the protocol based on search and sort options in
	 * FormResultControl
	 * 
	 * @param protocolId
	 *            The Protocol ID for retrieving all its forms
	 * @param formResultControl
	 *            The FormResultControl object that wraps the search and sort
	 *            options
	 * @return A list of all forms that matchs the result control option
	 * @throws CtdbException
	 *             thrown if any errors occur while processing
	 */
	public List<Form> getForms(int protocolId,
			FormResultControl formResultControl) throws CtdbException {
		PreparedStatement stmt = null;
		ResultSet rs = null;

		PreparedStatement stmt2 = null;
		ResultSet rs2 = null;

		try {
			List<Form> forms = new ArrayList<Form>();

			StringBuffer sql = new StringBuffer(50);
			
			sql.append(" with ctssClauseQuery as (select formId as ctssFormId from form where protocolid = ? ) ");
			sql.append(" select distinct CASE WHEN patientid  IS null THEN 0 ELSE 1 END isadministered, CASE WHEN tmp2.hasSkipRule IS null THEN 0 ELSE tmp2.hasSkipRule END hasskiprule,  CASE WHEN hasCalcRule IS null THEN 0 ELSE hasCalcRule END hascalcrule,   ");
			sql.append(" form.name, form.formid, form.description, form.xstatusid, form.createdby, form.createddate, form.updatedby, form.updateddate, ");
			sql.append(" form.version, form.checkedoutby, form.checkedoutdate, form.lockflag, form.formborder, form.sectionborder, form.formnamefont, ");
			sql.append(" form.formnamecolor, form.protocolid, form.orderval, form.importeddate, form.importfilename,  form.sectionnamefont, form.sectionnamecolor, ");
			sql.append(" form.dataentryflag, form.publicflag, form.header, form.footer, form.fontsize, form.dataentryworkflowtype, form.cellpadding ");
			sql.append(" ,form.attachfiles, form.enabledataspring, form.tabdisplay, form.formtypeid,form.copyright,form.allow_multiple_collection_instances, form.data_structure_name, form.data_structure_version, xformtype.name as formTypeName ");
			sql.append(" , xformstatus.shortname, tmp.questioncnt, tmp.isImageMap, ccq.ctssFormId ");
			sql.append(" from form");
			sql.append(" JOIN xformstatus ON form.xstatusid = xformstatus.xstatusid ");
			sql.append(" LEFT OUTER JOIN administeredform ON form.formid = administeredform.formid");
			sql.append(" LEFT OUTER JOIN ctssClauseQuery ccq ON ccq.ctssFormId = form.formid ");
			sql.append(" JOIN xformtype ON form.FORMTYPEID =  xformtype.XFORMTYPEID");
			sql.append(" LEFT OUTER JOIN");
			sql.append("   (select a.formid, count(*) questioncnt, max (tmptmp.sectionid) as isImageMap ");
			sql.append("	from section a");
			sql.append("	JOIN sectionquestion b ON a.sectionid = b.sectionid");
			sql.append("	LEFT OUTER JOIN (select distinct s.sectionid from sectionquestion s, question q where s.questionid = q.questionid and q.type = 9) tmptmp ");
			sql.append("	ON tmptmp.sectionid = a.sectionid join question oq on b.questionid = oq.questionid where oq.type <> 12 ");
			sql.append("	group by a.formid ) tmp ");
			sql.append(" ON form.formid = tmp.formid");
			sql.append(" LEFT OUTER JOIN ");
			sql.append("  (select MAX(CASE WHEN skipruleflag THEN 1 ELSE 0 END) as hasSkipRule, MAX(CASE WHEN calculatedflag THEN 1 ELSE 0 END) as hasCalcRule, s.formid from questionattributes qa, section s, form f, sectionquestion sq where ");
			sql.append("            sq.sectionid = s.sectionid and qa.questionattributesid = sq.questionattributesid and s.formid = f.formid and f.protocolid = ? GROUP BY s.formid) tmp2 ");
			sql.append(" ON form.formid = tmp2.formid");
			sql.append(" where protocolid = ? ");
			sql.append(formResultControl.getNotInClause());
			sql.append(formResultControl.getSearchClause());
			sql.append(formResultControl.getSortString());

			stmt = this.conn.prepareStatement(sql.toString());
			stmt.setLong(1, protocolId);
			stmt.setLong(2, protocolId);
			stmt.setLong(3, protocolId);

			rs = stmt.executeQuery();

			StringBuffer sql2 = new StringBuffer(50);
			sql2.append("select distinct f.formid from form f, nonpatientdata np where f.formid = np.formid and f.protocolid = ?");
			stmt2 = this.conn.prepareStatement(sql2.toString());
			stmt2.setLong(1, protocolId);

			rs2 = stmt2.executeQuery();

			List<Integer> administeredNPFormIds = new ArrayList<Integer>();
			while (rs2.next()) {
				int fId = rs2.getInt("formid");
				administeredNPFormIds.add(new Integer(fId));

			}

			while (rs.next()) {
				Form form = this.rsToForm(rs);

				int formId = form.getId();

				int[] formsGroupIds = getFormsGroups(formId);

				FormGroup fg;
				String formsGroupName = "";
				if (formsGroupIds != null && formsGroupIds.length > 0) {
					for (int i = 0; i < formsGroupIds.length; i++) {
						fg = getFormGroup(formsGroupIds[i]);
						formsGroupName = formsGroupName + fg.getName() + ", ";
					}
					formsGroupName = formsGroupName.substring(0,
							formsGroupName.lastIndexOf(","));
				}

				form.setFormGroupNames(formsGroupName);

				if (rs.getString("ctssFormId") != null) {
					form.setInCtss(true);
				}
				if (form.getFormType() == 10) {
					form.setIsAdministered(rs.getBoolean("isadministered"));
				} else {
					boolean match = false;
					for (int i = 0; i < administeredNPFormIds.size(); i++) {
						int administeredNPFormId = administeredNPFormIds.get(i)
								.intValue();
						if (formId == administeredNPFormId) {
							match = true;
							break;
						}
					}
					if (match) {
						form.setIsAdministered(true);
					} else {
						form.setIsAdministered(false);
					}
				}

				form.setHasSkipRule(rs.getBoolean("hasSkipRule"));
				form.setHasCalculationRule(rs.getBoolean("hasCalcRule"));
				if (rs.getString("isImageMap") != null) {
					form.setHasImageMap(true);
				}
			
				form.setNumQuestions(this.getNumberQuestionsWithoutRepeatable(formId));
				form.setFormTypeName(rs.getString("formTypeName"));

				forms.add(form);
			}

			return forms;
		} catch (SQLException e) {
			e.printStackTrace();
			throw new CtdbException("Unable to get protocol Forms: "
					+ e.getMessage(), e);
		} finally {
			this.close(rs);
			this.close(stmt);
		}
	}

	public List<Form> getAllMyForms(int protocolId, FormResultControl formResultControl) throws CtdbException {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		List<Form> forms = new ArrayList<Form>();
		
		try {
			StringBuffer sql = new StringBuffer(50);
			
			sql.append(" with ctssClauseQuery as (select formId as ctssFormId from form where protocolid = ? ) ");
			sql.append("select distinct CASE WHEN patientid IS null THEN 0 ELSE 1 END isadministered, CASE WHEN tmp2.hasSkipRule IS null THEN 0 ELSE tmp2.hasSkipRule END hasskiprule,  CASE WHEN hasCalcRule IS null THEN 0 ELSE hasCalcRule END hascalcrule,   ");
			sql.append("form.name, form.formid, form.description, form.xstatusid, form.createdby, form.createddate, form.updatedby, form.updateddate, ");
			sql.append(" form.version, form.checkedoutby, form.checkedoutdate, form.lockflag, form.formborder, form.sectionborder, form.formnamefont, ");
			sql.append(" form.formnamecolor, form.protocolid, form.orderval, form.importeddate, form.importfilename,  form.sectionnamefont, form.sectionnamecolor, ");
			sql.append(" form.dataentryflag, form.publicflag, form.header, form.footer, form.fontsize, form.dataentryworkflowtype, form.cellpadding ");
			sql.append(" ,form.attachfiles, form.enabledataspring, form.tabdisplay, form.formtypeid, xformtype.name as formTypeName ");
			sql.append(" , xformstatus.shortname, tmp.questioncnt, tmp.isImageMap, ccq.ctssFormId ");
			sql.append("from form");
			sql.append("JOIN xformstatus ON form.xstatusid = xformstatus.xstatusid");
			sql.append("LEFT OUTER JOIN administeredform ON form.formid = administeredform.formid");
			sql.append("LEFT OUTER JOIN ctssClauseQuery ccq ON ccq.ctssFormId = form.formid");
			sql.append("JOIN xformtype ON form.FORMTYPEID =  xformtype.XFORMTYPEID ");
			sql.append("LEFT OUTER JOIN ");
			sql.append("   ( select a.formid, count(*) questioncnt, max (tmptmp.sectionid) as isImageMap ");
			sql.append("       from section a");
			sql.append("       JOIN sectionquestion b ON a.sectionid = b.sectionid");
			sql.append("       LEFT OUTER JOIN (select distinct s.sectionid from sectionquestion s, question q where s.questionid = q.questionid and q.type = 9) tmptmp ");
			sql.append("       ON tmptmp.sectionid = a.sectionid");
			sql.append("      group by a.formid ) tmp");
			sql.append("ON form.formid = tmp.formid");
			sql.append("LEFT OUTER JOIN");
			sql.append("  (select MAX(CASE WHEN skipruleflag THEN 1 ELSE 0 END) as hasSkipRule, MAX(CASE WHEN calculatedflag THEN 1 ELSE 0 END) as hasCalcRule, s.formid from questionattributes qa, section s, form f, sectionquestion sq where ");
			sql.append("            sq.sectionid = s.sectionid and qa.questionattributesid = sq.questionattributesid and s.formid = f.formid and f.protocolid = ? GROUP BY s.formid) tmp2 ");
			sql.append("ON form.formid = tmp2.formid");
			sql.append(" where protocolid = ?");
			sql.append(formResultControl.getNotInClause());
			sql.append(formResultControl.getSearchClause());
			sql.append(formResultControl.getSortString());

			stmt = this.conn.prepareStatement(sql.toString());
			stmt.setLong(1, protocolId);
			stmt.setLong(2, protocolId);
			stmt.setLong(3, protocolId);

			rs = stmt.executeQuery();

			while (rs.next()) {
				Form form = this.rsToForm(rs);

				int formId = form.getId();

				int[] formsGroupIds = getFormsGroups(formId);

				FormGroup fg;
				String formsGroupName = "";
				if (formsGroupIds != null && formsGroupIds.length > 0) {
					for (int i = 0; i < formsGroupIds.length; i++) {
						fg = getFormGroup(formsGroupIds[i]);
						formsGroupName = formsGroupName + fg.getName() + ", ";
					}
					formsGroupName = formsGroupName.substring(0,
							formsGroupName.lastIndexOf(","));
				}

				form.setFormGroupNames(formsGroupName);

				form.setInCtss(rs.getBoolean("ctssFormId"));
				form.setIsAdministered(rs.getBoolean("isadministered"));
				form.setHasSkipRule(rs.getBoolean("hasSkipRule"));
				form.setHasCalculationRule(rs.getBoolean("hasCalcRule"));
				form.setHasImageMap(rs.getBoolean("isImageMap"));
				form.setNumQuestions(rs.getInt("questioncnt"));
				form.setFormTypeName(rs.getString("formTypeName"));

				forms.add(form);
			}
		}
		catch (SQLException e) {
			throw new CtdbException("Unable to get protocol Forms: " + e.getMessage(), e);
		}
		finally {
			this.close(rs);
			this.close(stmt);
		}
		
		return forms;
	}

	// add by sunny
	public List<Form> getOtherStudyMineForms(int currentProtocolId, int userId, FormResultControl formResultControl) throws CtdbException {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		List<Form> forms = new ArrayList<Form>();

		try {
			StringBuffer sql = new StringBuffer(50);
			
			sql.append("select tb.protocolnumber, tb.formid, tb.name, tb.questioncnt, tb.updateddate from ( ");
			sql.append("select distinct protocol.protocolnumber protocolnumber, ");
			sql.append("form.name, form.formid, form.description, form.xstatusid, form.createdby, form.createddate, form.updatedby, form.updateddate, ");
			sql.append(" form.version, form.checkedoutby, form.checkedoutdate, form.lockflag, form.formborder, form.sectionborder, form.formnamefont, ");
			sql.append(" form.formnamecolor, form.protocolid, form.orderval, form.importeddate, form.importfilename, form.sectionnamefont, form.sectionnamecolor, ");
			sql.append(" form.dataentryflag, form.publicflag, form.header, form.footer, form.fontsize, form.dataentryworkflowtype, form.cellpadding ");
			sql.append(" , xformstatus.*, tmp.questioncnt ");
			sql.append(" from protocol");
			sql.append(" JOIN form ON form.protocolid = protocol.protocolid ");
			sql.append(" JOIN xformstatus ON form.xstatusid = xformstatus.xstatusid");
			sql.append(" LEFT OUTER JOIN");
			sql.append("   ( select a.formid, count(*) questioncnt ");
			sql.append("       from section a, sectionquestion b ");
			sql.append("      where a.sectionid = b.sectionid ");
			sql.append("      group by a.formid ) tmp ");
			sql.append(" ON form.formid = tmp.formid");
			sql.append(" where form.protocolid != ? ");
			sql.append(" and form.protocolid != -1 ");
			sql.append(" and protocol.isevent != true ");
			sql.append(" and form.createdby = ?");
			sql.append(" and protocol.deleteflag=false ");
			sql.append(" and form.islegacy=false ");
			sql.append(formResultControl.getPublicFormSearchClause());
			sql.append(formResultControl.getSortString());

			if (formResultControl.getRowLimitStringSurfix() == " ") {
				sql.append(" ) ");
			}
			else {
				sql.append(formResultControl.getRowLimitStringSurfix());
			}
			
			sql.append(" tb");
			stmt = this.conn.prepareStatement(sql.toString());
			stmt.setLong(1, currentProtocolId);
			stmt.setLong(2, userId);
			rs = stmt.executeQuery();

			while (rs.next()) {
				Form form = new Form();
				form.setName(rs.getString("name"));
				form.setId(rs.getInt("formid"));
				form.setUpdatedDate(rs.getTimestamp("updateddate"));
				form.setNumQuestions(rs.getInt("questioncnt"));
				form.setProtocolNumber(rs.getString("protocolnumber"));
				
				forms.add(form);
			}
		}
		catch (SQLException e) {
			throw new CtdbException("Unable to get public forms : " + e.getMessage(), e);
		}
		finally {
			this.close(rs);
			this.close(stmt);
		}
		
		return forms;
	}

	// add by sunny
	public List<Form> getOtherStudyAllForms(int currentProtocolId, int userId, FormResultControl formResultControl) throws CtdbException {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		List<Form> forms = new ArrayList<Form>();
		
		try {
			StringBuffer sql = new StringBuffer(50);
			
			sql.append("select tb.protocolnumber, tb.formid, tb.name, tb.questioncnt, tb.updateddate from ( ");
			sql.append("select distinct protocol.protocolnumber protocolnumber, ");
			sql.append("form.name, form.formid, form.description, form.xstatusid, form.createdby, form.createddate, form.updatedby, form.updateddate, ");
			sql.append(" form.version, form.checkedoutby, form.checkedoutdate, form.lockflag, form.formborder, form.sectionborder, form.formnamefont, ");
			sql.append(" form.formnamecolor, form.protocolid, form.orderval, form.importeddate, form.importfilename, form.sectionnamefont, form.sectionnamecolor, ");
			sql.append(" form.dataentryflag, form.publicflag, form.header, form.footer, form.fontsize, form.dataentryworkflowtype, form.cellpadding ");
			sql.append(" , xformstatus.*, tmp.questioncnt ");
			sql.append(" from protocol");
			sql.append(" JOIN form ON form.protocolid = protocol.protocolid ");
			sql.append(" JOIN xformstatus ON form.xstatusid = xformstatus.xstatusid ");
			sql.append(" LEFT OUTER JOIN");
			sql.append("   ( select a.formid, count(*) questioncnt ");
			sql.append("       from section a, sectionquestion b ");
			sql.append("      where a.sectionid = b.sectionid ");
			sql.append("      group by a.formid ) tmp ");
			sql.append(" ON form.formid = tmp.formid");
			sql.append(" where form.protocolid != ? ");
			sql.append(" and form.protocolid != -1 ");
			sql.append(" and protocol.isevent != true ");
			sql.append(" and ((form.createdby != ? and form.publicflag=true) or  (form.createdby = ? ) )  ");
			sql.append(" and protocol.deleteflag=false ");
			sql.append(" and form.islegacy=false ");
			sql.append(formResultControl.getPublicFormSearchClause());
			sql.append(formResultControl.getSortString());

			if (formResultControl.getRowLimitStringSurfix() == " ") {
				sql.append(" ) ");
			} else {
				sql.append(formResultControl.getRowLimitStringSurfix());
			}
			sql.append(" tb");
			stmt = this.conn.prepareStatement(sql.toString());
			stmt.setLong(1, currentProtocolId);
			stmt.setLong(2, userId);
			stmt.setLong(3, userId);
			rs = stmt.executeQuery();

			while (rs.next()) {
				Form form = new Form();
				form.setName(rs.getString("name"));
				form.setId(rs.getInt("formid"));
				form.setUpdatedDate(rs.getTimestamp("updateddate"));
				form.setNumQuestions(rs.getInt("questioncnt"));
				form.setProtocolNumber(rs.getString("protocolnumber"));
				forms.add(form);
			}
		}
		catch (SQLException e) {
			throw new CtdbException("Unable to get public forms: " + e.getMessage(), e);
		}
		finally {
			this.close(rs);
			this.close(stmt);
		}
		
		return forms;
	}

	/**
	 * Retrieves public forms for protocols not in current protocol based on
	 * search and sort options in FormResultControl.
	 * 
	 * @param currentProtocolId
	 *            The Current Protocol ID for retrieving all public forms not in
	 *            it
	 * @param formResultControl
	 *            The FormResultControl object that wraps the search and sort
	 *            options
	 * @return A list of all public forms that matchs the result control option
	 * @throws CtdbException
	 *             thrown if any errors occur while processing
	 */
	public List<Form> getPublicForms(int currentProtocolId,
			FormResultControl formResultControl) throws CtdbException {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		List<Form> forms = new ArrayList<Form>();

		try {
			StringBuffer sql = new StringBuffer(50);
			
			sql.append("select tb.protocolnumber, tb.formid, tb.name, tb.questioncnt, tb.updateddate from ( ");
			sql.append(" select distinct protocol.protocolnumber protocolnumber, ");
			sql.append(" form.name, form.formid, form.description, form.xstatusid, form.createdby, form.createddate, form.updatedby, form.updateddate, ");
			sql.append(" form.version, form.checkedoutby, form.checkedoutdate, form.lockflag, form.formborder, form.sectionborder, form.formnamefont, ");
			sql.append(" form.formnamecolor, form.protocolid, form.orderval, form.importeddate, form.importfilename, form.sectionnamefont, form.sectionnamecolor, ");
			sql.append(" form.dataentryflag, form.publicflag, form.header, form.footer, form.fontsize, form.dataentryworkflowtype, form.cellpadding ");
			sql.append(" , xformstatus.*, tmp.questioncnt ");
			sql.append(" from protocol");
			sql.append(" JOIN form ON form.protocolid = protocol.protocolid ");
			sql.append(" JOIN xformstatus ON form.xstatusid = xformstatus.xstatusid ");
			sql.append(" LEFT OUTER JOIN");
			sql.append("   ( select a.formid, count(*) questioncnt ");
			sql.append("       from section a, sectionquestion b ");
			sql.append("      where a.sectionid = b.sectionid ");
			sql.append("      group by a.formid ) tmp ");
			sql.append(" ON form.formid = tmp.formid");
			sql.append(" where form.protocolid != ? ");
			sql.append(" and form.protocolid != -1 ");
			sql.append(" and protocol.isevent != true ");
			sql.append(" and form.publicflag = true");
			sql.append(" and protocol.deleteflag=false ");
			sql.append(" and form.islegacy=false ");
			sql.append(formResultControl.getPublicFormSearchClause());
			sql.append(formResultControl.getSortString());

			if (formResultControl.getRowLimitStringSurfix() == " ") {
				sql.append(" ) ");
			} else {
				sql.append(formResultControl.getRowLimitStringSurfix());
			}
			sql.append(" tb");
			stmt = this.conn.prepareStatement(sql.toString());
			stmt.setLong(1, currentProtocolId);

			rs = stmt.executeQuery();

			while (rs.next()) {

				Form form = new Form();
				form.setName(rs.getString("name"));
				form.setId(rs.getInt("formid"));
				form.setUpdatedDate(rs.getTimestamp("updateddate"));
				form.setNumQuestions(rs.getInt("questioncnt"));
				form.setProtocolNumber(rs.getString("protocolnumber"));
				
				forms.add(form);
			}
		}
		catch (SQLException e) {
			throw new CtdbException("Unable to get public forms: " + e.getMessage(), e);
		}
		finally {
			this.close(rs);
			this.close(stmt);
		}
		
		return forms;
	}

	/**
	 * Updates the ordering of forms for the protocol.
	 * 
	 * @param formId
	 *            The form to update
	 * @param orderVal
	 *            The order number for the form
	 * @throws CtdbException
	 *             Thrown if any errors occur while processing
	 */
	public void updateFormOrdering(int formId, int orderVal, int userId)
			throws CtdbException {
		PreparedStatement stmt = null;

		try {
			StringBuffer sql = new StringBuffer(200);
			sql.append("update form set updateddate = CURRENT_TIMESTAMP, orderval = ?, updatedby = ? ");
			sql.append("where formid = ?");

			stmt = this.conn.prepareStatement(sql.toString());
			stmt.setInt(1, orderVal);
			stmt.setLong(2, userId);
			stmt.setLong(3, formId);

			stmt.executeUpdate();
		} catch (SQLException e) {
			throw new CtdbException("Unable to update form order with ID "
					+ formId + ": " + e.getMessage(), e);
		} finally {
			this.close(stmt);
		}
	}

	public void updateFormOrderingBetter(String[] orderedIds, int userId)
			throws CtdbException {
		PreparedStatement stmt = null;

		try {
			StringBuffer sql = new StringBuffer(200);
			sql.append("update form set updateddate = CURRENT_TIMESTAMP, orderval = ?, updatedby = ? ");
			sql.append("where formid = ?");
			stmt = this.conn.prepareStatement(sql.toString());

			for (int i = 0; i < orderedIds.length; i++) {
				stmt.setInt(1, i);
				stmt.setLong(2, userId);
				stmt.setLong(3, Integer.parseInt(orderedIds[i]));

				stmt.addBatch();
			}

			stmt.executeBatch();
		} catch (SQLException e) {
			throw new CtdbException("Unable to update form order with ID: "
					+ e.getMessage(), e);
		} finally {
			this.close(stmt);
		}
	}

	/**
	 * Creates a section for the form.
	 * 
	 * @param section
	 *            The section to create
	 * @throws DuplicateObjectException
	 *             Thrown if the section with the same name already exists for
	 *             the form
	 * @throws CtdbException
	 *             Thrown if any other errors occur while processing
	 */
	public int createSection(Section section) throws DuplicateObjectException,
			CtdbException {
		PreparedStatement stmt = null;
		ResultSet rs = null;

		try {
			StringBuffer sql = new StringBuffer(50);

			// gets the section order value
			sql.append("select coalesce(max(orderval)+1, 1) from section where formid = ?");

			stmt = this.conn.prepareStatement(sql.toString());
			stmt.setLong(1, section.getFormId());

			rs = stmt.executeQuery();

			int orderval = 1;
			
			if (rs.next()) {
				orderval = rs.getInt(1);
			}

			rs.close();
			stmt.close();
			
			sql = new StringBuffer(50);
			sql.append("insert into section(sectionid, formid, name, description, createdby, createddate, updatedby, updateddate, orderval, formrow, formcol, ");
			sql.append(" suppressflag, label, altlabel, intob, collapsable, isresponseimage, isrepeatable, initialrepeatedsections, maxrepeatedsections, repeatedsectionparent, group_name, gridtype, tablegroupid, tableHeaderType ) ");
			sql.append(" values(DEFAULT,?,?,?,?,CURRENT_TIMESTAMP,?,CURRENT_TIMESTAMP,?,?,?,?,?,?,?, ?, ?,?,?,?,?,?,?,?,?)");

			stmt = this.conn.prepareStatement(sql.toString());
			stmt.setLong(1, section.getFormId());
			stmt.setString(2, section.getName());
			stmt.setString(3, section.getDescription());
			stmt.setLong(4, section.getCreatedBy());
			stmt.setLong(5, section.getUpdatedBy());
			stmt.setInt(6, orderval);
			stmt.setInt(7, section.getRow());
			stmt.setInt(8, section.getCol());

			stmt.setBoolean(9, section.isTextDisplayed());
			stmt.setString(10, section.getInstructionalText());
			stmt.setString(11, section.getAltLabel());
			stmt.setBoolean(12, section.isIntob());
			stmt.setBoolean(13, section.isCollapsable());
			stmt.setBoolean(14, section.isResponseImage());
			stmt.setBoolean(15, section.isRepeatable());
			stmt.setInt(16, section.getInitRepeatedSections());
			stmt.setInt(17, section.getMaxRepeatedSections());
			stmt.setInt(18, section.getRepeatedSectionParent());
			
			if (section.getRepeatableGroupName().equals("None")) {
				stmt.setNull(19, java.sql.Types.VARCHAR);
			}
			else {
				stmt.setString(19, section.getRepeatableGroupName());
			}
			
			stmt.setBoolean(20, section.isGridtype());
			
			
			if (section.getTableGroupId() == 0) {
				stmt.setNull(21, java.sql.Types.INTEGER);
			}
			else {
				stmt.setInt(21, section.getTableGroupId());
			}
			
			
			
			if (section.getTableHeaderType() == 0) {
				stmt.setNull(22, java.sql.Types.INTEGER);
			}
			else {
				stmt.setInt(22, section.getTableHeaderType());
			}
			
			
			stmt.executeUpdate();
			int retValue = getInsertId(conn, "section_seq");
			section.setId(retValue);
			return retValue;

		} catch (PSQLException e) {
			e.printStackTrace();
			if (e.getSQLState()
					.equals(SysPropUtil.getProperty("postgres_database.unique_constraint_sqlstate"))) {
				throw new DuplicateObjectException(
						"A section with the same name already exists for the form: "
								+ e.getMessage(), e);
			} else {
				throw new CtdbException("Unable to create form section: "
						+ e.getMessage(), e);
			}
		} catch (SQLException e) {
			if (e.getErrorCode() == Integer.parseInt(SysPropUtil.getProperty("database.unique_constraint_error.code"))) {
				throw new DuplicateObjectException(
						"A section with the same name already exists for the form: "
								+ e.getMessage(), e);
			} else {
				throw new CtdbException("Unable to create form section: "
						+ e.getMessage(), e);
			}
		} finally {
			this.close(stmt);
		}
	}

	/**
	 * Creates a section for the form.
	 * 
	 * @param section
	 *            The section to create
	 * @throws DuplicateObjectException
	 *             Thrown if the section with the same name already exists for
	 *             the form
	 * @throws CtdbException
	 *             Thrown if any other errors occur while processing
	 */
	public int createSection(Section section, boolean returnId)
			throws DuplicateObjectException, CtdbException {
		PreparedStatement stmt = null;
		ResultSet rs = null;

		try {
			StringBuffer sql = new StringBuffer(50);

			// gets the section order value
			sql.append("select coalesce(max(orderval)+1, 1) from section where formid = ?");

			stmt = this.conn.prepareStatement(sql.toString());
			stmt.setLong(1, section.getFormId());

			rs = stmt.executeQuery();

			int orderval = 1;
			if (rs.next()) {
				orderval = rs.getInt(1);
			}

			rs.close();
			stmt.close();
			
			sql = new StringBuffer(50);
			sql.append("insert into section(sectionid, formid, name, description, createdby, createddate, updatedby, updateddate, orderval, formrow, formcol, ");
			sql.append(" suppressflag, label, altlabel, intob, collapsable, isresponseimage, isrepeatable, initialrepeatedsections, maxrepeatedsections, repeatedsectionparent, group_name, gridtype, tablegroupid, tableHeaderType ) ");
			sql.append(" values(DEFAULT,?,?,?,?,CURRENT_TIMESTAMP,?,CURRENT_TIMESTAMP,?,?,?,?,?,?,?, ?, ?,?,?,?,?,?,?,?,?)");

			stmt = this.conn.prepareStatement(sql.toString());
			stmt.setLong(1, section.getFormId());
			stmt.setString(2, section.getName());
			stmt.setString(3, section.getDescription());
			stmt.setLong(4, section.getCreatedBy());
			stmt.setLong(5, section.getUpdatedBy());
			stmt.setInt(6, orderval);
			stmt.setInt(7, section.getRow());
			stmt.setInt(8, section.getCol());
			stmt.setBoolean(9, section.isTextDisplayed());
			stmt.setString(10, section.getInstructionalText());
			stmt.setString(11, section.getAltLabel());
			stmt.setBoolean(12, section.isIntob());
			stmt.setBoolean(13, section.isCollapsable());
			stmt.setBoolean(14, section.isResponseImage());
			stmt.setBoolean(15, section.isRepeatable());

			stmt.setInt(16, section.getInitRepeatedSections());
			stmt.setInt(17, section.getMaxRepeatedSections());
			stmt.setInt(18, section.getRepeatedSectionParent());

			if(section.getRepeatableGroupName().equals("None")) {
				stmt.setNull(19, java.sql.Types.VARCHAR);
			}else {
				stmt.setString(19, section.getRepeatableGroupName());
			}
			
			stmt.setBoolean(20, section.isGridtype());
			
			if (section.getTableGroupId() == 0) {
				stmt.setNull(21, java.sql.Types.INTEGER);
			}
			else {
				stmt.setInt(21, section.getTableGroupId());
			}
			
			
			if (section.getTableHeaderType() == 0) {
				stmt.setNull(22, java.sql.Types.INTEGER);
			}
			else {
				stmt.setInt(22, section.getTableHeaderType());
			}

			stmt.executeUpdate();
			return getInsertId(conn, "section_seq");

		} catch (PSQLException e) {
			e.printStackTrace();
			if (e.getSQLState()
					.equals(SysPropUtil.getProperty("postgres_database.unique_constraint_sqlstate"))) {
				throw new DuplicateObjectException(
						"A section with the same name already exists for the form: "
								+ e.getMessage(), e);
			} else {
				throw new CtdbException("Unable to create form section: "
						+ e.getMessage(), e);
			}
		} catch (SQLException e) {
			if (e.getErrorCode() == Integer.parseInt(SysPropUtil.getProperty("database.unique_constraint_error.code"))) {
				throw new DuplicateObjectException(
						"A section with the same name already exists for the form: "
								+ e.getMessage(), e);
			} else {
				throw new CtdbException("Unable to create form section: "
						+ e.getMessage(), e);
			}
		} finally {
			this.close(stmt);
		}
	}

	/**
	 * Retrieves a section based on the unique ID
	 * 
	 * @param sectionId
	 *            the section to retrieve
	 * @return Section data object
	 * @throws ObjectNotFoundException
	 *             Thrown if the section does not exist
	 * @throws CtdbException
	 *             Thrown if any other errors occur while processing
	 */
	public int getQuestionIdFromSectionQuestion(int sectionId,int questionOrder) throws ObjectNotFoundException,
			CtdbException {
		PreparedStatement stmt = null;
		ResultSet rs = null;

		try {
			StringBuffer sql = new StringBuffer(25);
			sql.append("select questionid from sectionquestion where sectionid = ? and questionorder = ?");
			stmt = this.conn.prepareStatement(sql.toString());
			stmt.setLong(1, sectionId);
			stmt.setLong(2, questionOrder);

			rs = stmt.executeQuery();
			if (!rs.next()) {
				throw new ObjectNotFoundException("The questionId with sectionID: "
						+ sectionId + " and question order " + questionOrder + "could not be found.");
			}

			return rs.getInt(1);
		} catch (SQLException e) {
			throw new CtdbException("Unable to get section: " + e.getMessage(),
					e);
		} finally {
			this.close(rs);
			this.close(stmt);
		}
	}

	/**
	 * Retrieves a section based on the unique ID
	 * 
	 * @param sectionId
	 *            the section to retrieve
	 * @return Section data object
	 * @throws ObjectNotFoundException
	 *             Thrown if the section does not exist
	 * @throws CtdbException
	 *             Thrown if any other errors occur while processing
	 */
	public Section getSection(int sectionId) throws ObjectNotFoundException,
			CtdbException {
		PreparedStatement stmt = null;
		ResultSet rs = null;

		try {
			StringBuffer sql = new StringBuffer(25);
			sql.append("select * from section where sectionid = ?");
			stmt = this.conn.prepareStatement(sql.toString());
			stmt.setLong(1, sectionId);

			rs = stmt.executeQuery();
			if (!rs.next()) {
				throw new ObjectNotFoundException("The section with ID: "
						+ sectionId + " could not be found.");
			}

			return this.rsToSection(rs);
		} catch (SQLException e) {
			throw new CtdbException("Unable to get section: " + e.getMessage(),
					e);
		} finally {
			this.close(rs);
			this.close(stmt);
		}
	}

	public String getFormTypeString(int formType)
			throws ObjectNotFoundException, CtdbException {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			StringBuffer sql = new StringBuffer(50);
			sql.append("select name from xformtype where xformtypeid = ?");

			stmt = this.conn.prepareStatement(sql.toString());
			stmt.setLong(1, formType);

			rs = stmt.executeQuery();
			if (!rs.next()) {
				throw new ObjectNotFoundException(
						"Unable to get form type name");
			}

			return rs.getString("name");

		} catch (SQLException e) {
			throw new CtdbException("Unable to get form Sections: "
					+ e.getMessage(), e);
		} finally {
			this.close(rs);
			this.close(stmt);
		}

	}

	/**
	 * Retrieves sections for the current form based on the unique form ID
	 * 
	 * @param formId - The Form ID for retrieving all its sections
	 * @return A list of all section for the form
	 * @throws CtdbException	Thrown if any errors occur while processing
	 */
	public List<List<Section>> getSections(int formId) throws CtdbException {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		PreparedStatement stmt2 = null;
		ResultSet rs2 = null;
		List<List<Section>> sections = new ArrayList<List<Section>>();
		
		try {
			//String sql = "select formrow, numcols from formlayout where formid = ? order by formrow ";
			String sql = "select DISTINCT MAX(numcols) as numcols, formrow from formlayout where formid = ? GROUP BY formrow order by formrow";
			stmt2 = this.conn.prepareStatement(sql);
			stmt2.setLong(1, formId);
			rs2 = stmt2.executeQuery();
			
			// Prepare the "section" table query.
			sql = "select * from section where formid = ? and formrow = ? order by formcol ";
			stmt = this.conn.prepareStatement(sql);
			
			// Populate the sections list
			while ( rs2.next() ) {
				int numCols = rs2.getInt("numcols");
				int currRow = rs2.getInt("formrow");
				List<Section> row = new ArrayList<Section>(numCols);
				
				stmt.setLong(1, formId);
				stmt.setInt(2, currRow);
				rs = stmt.executeQuery();
				
				// Create the Section objects and add them to the row list
				while ( rs.next() ) {
					Section section = this.rsToSection(rs);
					row.add(section);
				}
				
				rs.close();
				sections.add(row);
			}
		}
		catch (SQLException e) {
			throw new CtdbException("Unable to get form Sections: " + e.getMessage(), e);
		}
		finally {
			this.close(rs);
			this.close(stmt);
			this.close(rs2);
			this.close(stmt2);
		}
		
		return sections;
	}

	/**
	 * Retrieves sections for the current form based on the unique form ID
	 * 
	 * @param formId
	 *            The Form ID for retrieving all its sections
	 * @return A list of all section for the form
	 * @throws CtdbException
	 *             thrown if any errors occur while processing
	 */
	public List<Section> getSectionsNoRows(int formId) throws CtdbException {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		List<Section> sections = new ArrayList<Section>();
		
		try {
			StringBuffer sql = new StringBuffer(50);

			sql = new StringBuffer(50);
			sql.append("select * from section where formid = ? ");
			sql.append("order by formrow, formcol ");

			stmt = this.conn.prepareStatement(sql.toString());
			stmt.setLong(1, formId);
			rs = stmt.executeQuery();

			while (rs.next()) {
				Section section = this.rsToSection(rs);
				sections.add(section);
			}
		}
		catch (SQLException e) {
			throw new CtdbException("Unable to get form Sections for the skip rules: " + e.getMessage(), e);
		}
		finally {
			this.close(rs);
			this.close(stmt);
		}
		
		return sections;
	}

	/**
	 * Checks to see if the question is a child of any skip rules on the form.
	 * 
	 * @param formId
	 *            The form ID to look for all skip rules.
	 * @param question
	 *            The question to check
	 * @return boolean true if the question is a child of any skip rules on the
	 *         form; false otherwise.
	 * @throws CtdbException
	 *             Thrown if any errors occur while processing
	 */
	public boolean isChildSkipRule(int formId, Question question)
			throws CtdbException {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			StringBuffer sql = new StringBuffer(50);

			sql.append("select count(*) ");
			sql.append(" from skiprulequestion sq, section, form ");
			sql.append(" where sq.sectionid = section.sectionid ");
			sql.append(" and section.formid = form.formid ");
			sql.append(" and form.formid = ? ");
			sql.append(" and sq.skipquestionid = ? ");

			stmt = this.conn.prepareStatement(sql.toString());
			stmt.setLong(1, formId);
			stmt.setLong(2, question.getId());
			rs = stmt.executeQuery();

			int num = 0;
			if (rs.next()) {
				num = rs.getInt(1);
			}
			return num > 0;
		} catch (SQLException e) {
			throw new CtdbException(
					"Unable to check if the question is a child of any skip rules on the form: "
							+ e.getMessage(), e);
		} finally {
			this.close(rs);
			this.close(stmt);
		}
	}

	/**
	 * Checks to see if the question is a parent of any skip rules on the
	 * current section.
	 * 
	 * @param formId
	 *            The form ID to look for all skip rules.
	 * @param question
	 *            The question to check
	 * @param currentQuestion
	 *            the current question to exclude for the check
	 * @return boolean true if the question is a child of any skip rules on the
	 *         form; false otherwise.
	 * @throws CtdbException
	 *             Thrown if any errors occur while processing
	 */
	public boolean isParentSkipRuleExcluding(int formId, Question question,
			Question currentQuestion) throws CtdbException {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			StringBuffer sql = new StringBuffer(50);

			sql.append("select count(*) ");
			sql.append(" from skiprulequestion sq, section, form ");
			sql.append(" where sq.sectionid = section.sectionid ");
			sql.append(" and section.formid = form.formid ");
			sql.append(" and form.formid = ? ");
			sql.append(" and sq.questionid = ? ");
			sql.append(" and sq.questionid != ? ");

			stmt = this.conn.prepareStatement(sql.toString());
			stmt.setLong(1, formId);
			stmt.setLong(2, question.getId());
			stmt.setLong(3, currentQuestion.getId());

			rs = stmt.executeQuery();

			int num = 0;
			if (rs.next()) {
				num = rs.getInt(1);
			}
			return num > 0;
		} catch (SQLException e) {
			throw new CtdbException(
					"Unable to check if the question is a parent of any skip rules on the form "
							+ formId + " : " + e.getMessage(), e);
		} finally {
			this.close(rs);
			this.close(stmt);
		}
	}

	/**
	 * Checks to see if the question is a child of any skip rules on the current
	 * section.
	 * 
	 * @param formId
	 *            The form ID to look for all skip rules.
	 * @param question
	 *            The question to check
	 * @param currentQuestion
	 *            the current question to exclude for the check
	 * @return boolean true if the question is a child of any skip rules on the
	 *         form; false otherwise.
	 * @throws CtdbException
	 *             Thrown if any errors occur while processing
	 */
	public boolean isChildSkipRuleExcluding(int formId, Question question,
			Question currentQuestion) throws CtdbException {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			StringBuffer sql = new StringBuffer(50);

			sql.append("select count(*) ");
			sql.append(" from skiprulequestion sq, section, form ");
			sql.append(" where sq.sectionid = section.sectionid ");
			sql.append(" and section.formid = form.formid ");
			sql.append(" and form.formid = ? ");
			sql.append(" and sq.skipquestionid = ? ");
			sql.append(" and sq.questionid != ? ");

			stmt = this.conn.prepareStatement(sql.toString());
			stmt.setLong(1, formId);
			stmt.setLong(2, question.getId());
			stmt.setLong(3, currentQuestion.getId());

			rs = stmt.executeQuery();

			int num = 0;
			if (rs.next()) {
				num = rs.getInt(1);
			}
			return num > 0;
		} catch (SQLException e) {
			throw new CtdbException(
					"Unable to check if the question is a child of any skip rules on the form "
							+ formId + " : " + e.getMessage(), e);
		} finally {
			this.close(rs);
			this.close(stmt);
		}
	}

	/**
	 * Checks to see if the question is involved in any skip rules on the form.
	 * 
	 * @param formId
	 *            The form ID to look for all skip rules.
	 * @param question
	 *            The question to check
	 * @return boolean true if the question is involved in any skip rules on the
	 *         form; false otherwise.
	 * @throws CtdbException
	 *             Thrown if any errors occur while processing
	 */
	public boolean inSkipRules(int formId, Question question,
			Section currentSection) throws CtdbException {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			StringBuffer sql = new StringBuffer(50);

			sql.append("select count(*) ");
			sql.append(" from skiprulequestion sq, section, form ");
			sql.append(" where sq.sectionid = section.sectionid ");
			sql.append(" and sq.sectionid != ? ");
			sql.append(" and section.formid = form.formid ");
			sql.append(" and form.formid = ? ");
			sql.append(" and (sq.questionid = ? or sq.skipquestionid = ? )");

			stmt = this.conn.prepareStatement(sql.toString());
			stmt.setLong(1, currentSection.getId());
			stmt.setLong(2, formId);
			stmt.setLong(3, question.getId());
			stmt.setLong(4, question.getId());
			rs = stmt.executeQuery();

			int num = 0;
			if (rs.next()) {
				num = rs.getInt(1);
			}
			return num > 0;
		} catch (SQLException e) {
			throw new CtdbException(
					"Unable to check if the question is involved in skip rules on the form: "
							+ e.getMessage(), e);
		} finally {
			this.close(rs);
			this.close(stmt);
		}
	}

	/**
	 * Retrieves sections for the form based on the unique form ID and form
	 * Version
	 * 
	 * @param formId
	 *            The form ID for retrieving all its sections
	 * @param formId
	 *            The form version for retrieving all its sections
	 * @return A list of all section for the form
	 * @throws CtdbException
	 *             thrown if any errors occur while processing
	 */
	public List<List<Section>> getSections(int formId, int formVersion) throws CtdbException {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		PreparedStatement stmt2 = null;
		ResultSet rs2 = null;
		List<List<Section>> sections = new ArrayList<List<Section>>();

		try {
			String sql = "select DISTINCT MAX(numcols) as numcols, formrow from formlayout where formid = ? GROUP BY formrow order by formrow ";
			stmt2 = this.conn.prepareStatement(sql);
			stmt2.setLong(1, formId);
			rs2 = stmt2.executeQuery();
			
			sql = "select * from section_view where formid = ? and formversion = ? and formrow = ? order by orderval ";
			stmt = this.conn.prepareStatement(sql);
			
			while ( rs2.next() ) {
				int numCols = rs2.getInt("numcols");
				int currRow = rs2.getInt("formrow");
				List<Section> aRow = new ArrayList<Section>(numCols);
				
				stmt.setLong(1, formId);
				stmt.setInt(2, formVersion);
				stmt.setInt(3, currRow);
				rs = stmt.executeQuery();
				
				while ( rs.next() ) {
					Section section = this.rsToSection(rs);
					aRow.add(section);
				}
				
				rs.close();
				sections.add(aRow);
			}
		}
		catch (SQLException e) {
			throw new CtdbException("Unable to get form Sections: " + e.getMessage(), e);
		}
		finally {
			this.close(rs);
			this.close(stmt);
			this.close(rs2);
			this.close(stmt2);
		}
		
		return sections;
	}

	/**
	 * Updates the section for the form. Versions the form when section metadata
	 * has changed.
	 * 
	 * @param section
	 *            The section data object to update
	 * @throws ObjectNotFoundException
	 *             Thrown if the form does not exist
	 * @throws CtdbException
	 *             Thrown if any other errors occur while processing
	 */
	public void updateSection(Section section) throws ObjectNotFoundException,
			DuplicateObjectException, CtdbException {
		PreparedStatement stmt = null;

		try {
			StringBuffer sql = new StringBuffer(50);
			sql.append("update section set name = ?, description = ?, updatedby = ?, updateddate = CURRENT_TIMESTAMP ");
			sql.append(", formrow = ?, formcol = ?, collapsable = ?, isrepeatable = ?, initialrepeatedsections = ?, maxrepeatedsections = ?, repeatedSectionParent = ?, group_name = ? , gridtype = ?, tablegroupid = ?, tableheadertype = ?");
			sql.append("where sectionid = ?");

			stmt = this.conn.prepareStatement(sql.toString());
			stmt.setString(1, section.getName());
			stmt.setString(2, section.getDescription());
			stmt.setLong(3, section.getUpdatedBy());
			stmt.setInt(4, section.getRow());
			stmt.setInt(5, section.getCol());
			stmt.setBoolean(6, section.isCollapsable());
			stmt.setBoolean(7, section.isRepeatable());
			stmt.setInt(8, section.getInitRepeatedSections());
			stmt.setInt(9, section.getMaxRepeatedSections());
			stmt.setInt(10, section.getRepeatedSectionParent());
			
			if(section.getRepeatableGroupName().equals("None")) {
				stmt.setNull(11, java.sql.Types.VARCHAR);
			}else {
				stmt.setString(11, section.getRepeatableGroupName());
			}
			
			stmt.setBoolean(12, section.isGridtype()); 
			
			
			if (section.getTableGroupId() == 0) {
				stmt.setNull(13, java.sql.Types.INTEGER);
			}
			else {
				stmt.setInt(13, section.getTableGroupId());
			}
			
			
			if (section.getTableHeaderType() == 0) {
				stmt.setNull(14, java.sql.Types.INTEGER);
			}
			else {
				stmt.setInt(14, section.getTableHeaderType());
			}

			stmt.setLong(15, section.getId());
			
			

			int recordsUpdated = stmt.executeUpdate();

			if (recordsUpdated == 0) {
				throw new ObjectNotFoundException("The section with ID: "
						+ section.getId() + " does not exist in the system.");
			}

		} catch (PSQLException e) {
			e.printStackTrace();
			if (e.getSQLState()
					.equals(SysPropUtil.getProperty("postgres_database.unique_constraint_sqlstate"))) {
				throw new DuplicateObjectException(
						"A section with the same name already exists for the form: "
								+ e.getMessage(), e);
			} else {
				throw new CtdbException("Unable to update form section: "
						+ e.getMessage(), e);
			}
		} catch (SQLException e) {
			if (e.getErrorCode() == Integer.parseInt(SysPropUtil.getProperty("database.unique_constraint_error.code"))) {
				throw new DuplicateObjectException(
						"A section with the same name already exists for the form: "
								+ e.getMessage(), e);
			} else {
				throw new CtdbException("Unable to update form section: "
						+ e.getMessage(), e);
			}
		} finally {
			this.close(stmt);
		}
	}

	/**
	 * Updates the section for the form. Versions the form when section metadata
	 * has changed.
	 * 
	 * @param section
	 *            The section data object to update
	 * @throws ObjectNotFoundException
	 *             Thrown if the form does not exist
	 * @throws CtdbException
	 *             Thrown if any other errors occur while processing
	 */
	public void updateRepeatedSectionParent(Section section)
			throws ObjectNotFoundException, DuplicateObjectException,
			CtdbException {
		PreparedStatement stmt = null;

		try {
			StringBuffer sql = new StringBuffer(50);
			sql.append("update section set repeatedsectionparent = ?");
			sql.append("where sectionid = ?");

			stmt = this.conn.prepareStatement(sql.toString());
			stmt.setInt(1, section.getRepeatedSectionParent());
			stmt.setLong(2, section.getId());

			int recordsUpdated = stmt.executeUpdate();

			if (recordsUpdated == 0) {
				throw new ObjectNotFoundException("The section with ID: "
						+ section.getId() + " does not exist in the system.");
			}

		} catch (PSQLException e) {
			e.printStackTrace();
			if (e.getSQLState()
					.equals(SysPropUtil.getProperty("postgres_database.unique_constraint_sqlstate"))) {
				throw new DuplicateObjectException(
						"A section with the same name already exists for the form: "
								+ e.getMessage(), e);
			} else {
				throw new CtdbException("Unable to update form section: "
						+ e.getMessage(), e);
			}
		} catch (SQLException e) {
			if (e.getErrorCode() == Integer.parseInt(SysPropUtil.getProperty("database.unique_constraint_error.code"))) {
				throw new DuplicateObjectException(
						"A section with the same name already exists for the form: "
								+ e.getMessage(), e);
			} else {
				throw new CtdbException("Unable to update form section: "
						+ e.getMessage(), e);
			}
		} finally {
			this.close(stmt);
		}
	}

	/**
	 * Updates the ordering of sections for the form. It archives and versions
	 * the form if the form is administed. This ordering will be used when
	 * displaying sections for the form.
	 * 
	 * @param section
	 *            The section to update
	 * @param orderVal
	 *            The order number for the section
	 * @throws CtdbException
	 *             thrown if any errors occur while processing
	 */
	public void updateSectionOrdering(Section section, int orderVal)
			throws CtdbException {
		PreparedStatement stmt = null;

		try {
			StringBuffer sql = new StringBuffer(200);
			sql.append("update section set updateddate = CURRENT_TIMESTAMP, updatedby = ?, orderval = ? ");
			sql.append("where sectionid = ?");

			stmt = this.conn.prepareStatement(sql.toString());
			stmt.setLong(1, section.getUpdatedBy());
			stmt.setInt(2, orderVal);
			stmt.setLong(3, section.getId());

			stmt.executeUpdate();
		} catch (SQLException e) {
			throw new CtdbException("Unable to update section order with ID "
					+ section.getId() + ": " + e.getMessage(), e);
		} finally {
			this.close(stmt);
		}
	}

	public void updateSectionQuestionQuestionAttributesIDAndQVersion(
			int sectionId, Question question, int qaId) throws CtdbException {

		PreparedStatement stmt = null;

		StringBuffer sql = new StringBuffer(200);
		sql.append("update sectionquestion set questionattributesid = ?, questionversion = ? ");
		sql.append("where sectionId = ? and questionId = ? ");

		try {
			stmt = this.conn.prepareStatement(sql.toString());
			stmt.setLong(1, qaId);
			stmt.setInt(2, question.getVersion().getVersionNumber());
			stmt.setLong(3, sectionId);
			stmt.setLong(4, question.getId());
			stmt.executeUpdate();

		} catch (SQLException e) {
			throw new CtdbException("Unable to remove cell from row"
					+ e.getMessage(), e);
		} finally {
			this.close(stmt);
		}
	}

	public void updateDisplayAttributes(Form form, Map<String, Object> map) throws CtdbException {
		try {
			if (map.get("displaySection") != null) {
				if ( (Boolean) map.get("displaySection") ) {
					updateSectionDisplay((Integer) map.get("sectionId"), 0);
				}
				else {
					updateSectionDisplay((Integer) map.get("sectionId"), 1);
				}
			}
			else {
				// make sure the section is displayed
				updateSectionDisplay((Integer) map.get("sectionId"), 0);
			}
			
			resetSectionQuestionDisplay((Integer) map.get("sectionId"));

			Section oldSection = this.getSection(((Integer) map.get("sectionId")).intValue());
			String oldIText = oldSection.getInstructionalText();
			String newIText = (String) map.get("instructionalText");
			boolean newFormVersionCreated = false;
			
			// get more info for Form Object
			form = this.getForm(form.getId());
			
			if ( (oldIText != null && newIText != null && !oldIText.equals(newIText)) || (oldIText == null && newIText != null) || 
					(oldIText != null && newIText == null) ) {
				if ( this.isAdministered(form) && (form.getStatus().getId() != FormConstants.STATUS_CHECKEDOUT) ) {
					this.createFormArchive(form.getId());
					updateSectionText((String) map.get("instructionalText"), (Integer) map.get("sectionId"));
					this.updateFormVersion(form);
					newFormVersionCreated = true;
				}
				else {
					updateSectionText((String) map.get("instructionalText"), (Integer) map.get("sectionId"));
				}
			}
			else {
				updateSectionText((String) map.get("instructionalText"), (Integer) map.get("sectionId"));
			}

			if (map.get("questionIdsToHide") != null) {
				updateSectionQuestionDisplay((String[]) map.get("questionIdsToHide"), (Integer) map.get("sectionId"));
			}

			if (newFormVersionCreated) {
				if (map.get("textLabel") != null && map.get("questionIdsToSetLabel") != null) {
					updateSectionQuestionTextLabel((String[]) map.get("textLabel"), (String[]) map.get("questionIdsToSetLabel"), 
						(Integer) map.get("sectionId"));
				}
			}
			else {
				String[] labels = (String[]) map.get("textLabel");
				String[] questionIds = (String[]) map.get("questionIdsToSetLabel");
				int sectionId = ((Integer) map.get("sectionId")).intValue();

				for (int i = 0; i < labels.length; i++) {
					String label = labels[i];
					
					if ( label != null ) {
						int questionId = (new Integer(questionIds[i])).intValue();
						FormQuestionAttributes fQAttrs = getFormQuestionAttributes(sectionId, questionId);
						String oldLabel = fQAttrs.getLabel();
						
						if ( (oldLabel != null && label != null && !oldLabel.equals(label)) || (oldLabel == null && label != null) || 
								(oldLabel != null && label == null) ) {
							if (!newFormVersionCreated && (this.isAdministered(form)) && (form.getStatus().getId() != FormConstants.STATUS_CHECKEDOUT)) {
								this.createFormArchive(form.getId());
								updateSectionQuestionTextLabel(label, questionId, sectionId);
								this.updateFormVersion(form);
								newFormVersionCreated = true;
							}
							else {
								updateSectionQuestionTextLabel(label,
										questionId, sectionId);
							}
						}
						else {
							updateSectionQuestionTextLabel(label, questionId, sectionId);
						}
					}
				}
			}
		}
		catch (SQLException e) {
			throw new CtdbException("Unable to update section display", e);
		}
	}

	/**
	 * Updates the table of contents for form sections.
	 * 
	 * @param map
	 *            contains the info to update
	 * @throws DuplicateObjectException
	 * @throws CtdbException
	 */
	public void updateSectionsTOB(Map<String, Object> map) throws CtdbException {
		try {
			resetSectionsInTOBDisplay((Integer) map.get("formId"));
			
			if (map.get("sectionIdsToTOB") != null) {
				updateSectionsInTOBDisplay((String[]) map.get("sectionIdsToTOB"), (Integer) map.get("formId"));
			}
			
			if (map.get("altLabel") != null && map.get("sectionIdsToSetLabel") != null) {
				updateSectionsAltLabel((String[]) map.get("altLabel"), (String[]) map.get("sectionIdsToSetLabel"), (Integer) map.get("formId"));
			}
		}
		catch (SQLException e) {
			throw new CtdbException("Unable to update form sections table of contents", e);
		}
	}

	private void updateSectionsAltLabel(String[] labels, String[] sectionIds,
			Integer formId) throws SQLException {
		PreparedStatement stmt = null;
		try {
			StringBuffer sql = new StringBuffer(25);
			for (int i = 0; i < labels.length; i++) {
				String label = labels[i];
				if (label != null) {
					sql = new StringBuffer(25);
					sql.append("update section ");
					sql.append("set altlabel = ? ");
					sql.append("where formid = ? ");
					sql.append("and sectionid = ? ");
					stmt = this.conn.prepareStatement(sql.toString());
					stmt.setString(1, label);
					stmt.setLong(2, formId.intValue());
					stmt.setLong(3, (new Integer(sectionIds[i])).intValue());
					stmt.executeUpdate();
				}
			}
		} finally {
			this.close(stmt);
		}
	}

	private void resetSectionsInTOBDisplay(Integer formId) throws SQLException {
		PreparedStatement stmt = null;
		try {
			// set all to not in Table Of Contents
			StringBuffer sql = new StringBuffer(25);
			sql.append("update section set intob = 0 where formid = ? ");
			stmt = this.conn.prepareStatement(sql.toString());
			stmt.setLong(1, formId.intValue());
			stmt.executeUpdate();
		} finally {
			this.close(stmt);
		}
	}

	private void updateSectionDisplay(Integer sectionId, int suppressflag)
			throws SQLException {
		PreparedStatement stmt = null;

		try {
			// delete all questions first
			StringBuffer sql = new StringBuffer(25);
			sql.append("update section set suppressflag = ? where sectionid = ?");
			stmt = this.conn.prepareStatement(sql.toString());
			stmt.setBoolean(1,
					Boolean.parseBoolean(Integer.toString(suppressflag)));
			stmt.setLong(2, sectionId.intValue());

			stmt.executeUpdate();
		} finally {
			this.close(stmt);
		}
	}

	private void updateSectionText(String text, Integer sectionId)
			throws SQLException {
		PreparedStatement stmt = null;
		try {
			StringBuffer sql = new StringBuffer(25);
			sql.append("update section set label = ? where sectionid = ?");
			stmt = this.conn.prepareStatement(sql.toString());
			stmt.setString(1, text);
			stmt.setLong(2, sectionId.intValue());

			stmt.executeUpdate();
		} finally {
			this.close(stmt);
		}
	}

	private void updateSectionsInTOBDisplay(String[] sectionIds,
			Integer sectionId) throws SQLException {
		PreparedStatement stmt = null;

		try {
			StringBuffer sql = new StringBuffer(25);
			sql.append("update section set intob = 1 where formid = ? ");
			sql.append(" and sectionid in (");
			for (int i = 0; i < sectionIds.length - 1; i++) {
				sql.append(sectionIds[i] + ", ");
			}
			sql.append(sectionIds[sectionIds.length - 1] + " ) ");

			stmt = this.conn.prepareStatement(sql.toString());
			stmt.setLong(1, sectionId.intValue());
			stmt.executeUpdate();
		} finally {
			this.close(stmt);
		}
	}

	private void updateSectionQuestionDisplay(String[] questionIds,
			Integer sectionId) throws SQLException {
		PreparedStatement stmt = null;

		try {
			// delete all questions first
			StringBuffer sql = new StringBuffer(25);
			sql.append("update sectionquestion set suppressflag = 1 where sectionid = ? ");
			sql.append(" and questionid in (");
			for (int i = 0; i < questionIds.length - 1; i++) {
				sql.append(questionIds[i] + ", ");
			}
			sql.append(questionIds[questionIds.length - 1] + " ) ");

			stmt = this.conn.prepareStatement(sql.toString());
			stmt.setLong(1, sectionId.intValue());
			stmt.executeUpdate();
		} finally {
			this.close(stmt);
		}
	}

	private void updateSectionQuestionTextLabel(String label, int questionId,
			int sectionId) throws SQLException {
		PreparedStatement stmt = null;
		try {
			if (label == null) {
				return;
			}
			StringBuffer sql = new StringBuffer(25);
			sql = new StringBuffer(25);
			sql.append("update questionattributes ");
			sql.append("set label = ? ");
			sql.append("where questionattributesid = ( ");
			sql.append("select questionattributesid ");
			sql.append("from sectionquestion ");
			sql.append("where sectionid = ? ");
			sql.append("and questionid = ? )");
			stmt = this.conn.prepareStatement(sql.toString());
			stmt.setString(1, label);
			stmt.setLong(2, sectionId);
			stmt.setLong(3, questionId);
			stmt.executeUpdate();
		} finally {
			this.close(stmt);
		}
	}

	private void updateSectionQuestionTextLabel(String[] labels,
			String[] questionIds, Integer sectionId) throws SQLException {
		PreparedStatement stmt = null;
		try {
			StringBuffer sql = new StringBuffer(25);
			for (int i = 0; i < labels.length; i++) {
				String label = labels[i];
				if (label != null) {
					sql = new StringBuffer(25);
					sql.append("update questionattributes ");
					sql.append("set label = ? ");
					sql.append("where questionattributesid = ( ");
					sql.append("select questionattributesid ");
					sql.append("from sectionquestion ");
					sql.append("where sectionid = ? ");
					sql.append("and questionid = ? )");
					stmt = this.conn.prepareStatement(sql.toString());
					stmt.setString(1, label);
					stmt.setLong(2, sectionId.intValue());
					stmt.setLong(3, (new Integer(questionIds[i])).intValue());
					stmt.executeUpdate();
				}
			}
		} finally {
			this.close(stmt);
		}
	}

	private void resetSectionQuestionDisplay(Integer sectionId)
			throws SQLException {
		PreparedStatement stmt = null;
		try {
			// set all to not suppress
			StringBuffer sql = new StringBuffer(25);
			sql.append("update sectionquestion set suppressflag = 0 where sectionid = ? ");
			stmt = this.conn.prepareStatement(sql.toString());
			stmt.setLong(1, sectionId.intValue());
			stmt.executeUpdate();
		} finally {
			this.close(stmt);
		}
	}

	/**
	 * Deletes the question order value for the section.
	 * 
	 * @param sectionId
	 *            The section ID that the question belongs to
	 * @throws CtdbException
	 *             Thrown if any errors occur while processing
	 */
	public void deleteQuestionOrdering(int sectionId) throws CtdbException {
		PreparedStatement stmt = null;

		try {
			// delete all questions first
			StringBuffer sql = new StringBuffer(25);
			sql.append("delete from sectionquestion where sectionid = ?");

			stmt = this.conn.prepareStatement(sql.toString());
			stmt.setLong(1, sectionId);
			stmt.executeUpdate();
		} catch (SQLException e) {
			throw new CtdbException(
					"Unable to update question order with section id = "
							+ sectionId + " : " + e.getMessage(), e);
		} finally {
			this.close(stmt);
		}

	}

	/**
	 * Deletes a section from the system based on the unique identifier. It
	 * deletes all section questions as well. Questions are deleted with cascade
	 * delete.
	 * 
	 * @param sectionId
	 *            The Section ID to delete
	 * @throws CtdbException
	 *             thrown if any other errors occur while processing
	 */
	public void deleteSection(int sectionId) throws CtdbException {
		PreparedStatement stmt = null;

		try {
			StringBuffer sql = new StringBuffer(25);
			sql.append("delete FROM section where sectionid = ?");

			stmt = this.conn.prepareStatement(sql.toString());
			stmt.setLong(1, sectionId);

			stmt.executeUpdate();
		} catch (SQLException e) {
			throw new CtdbException("Unable to delete the section: "
					+ e.getMessage(), e);
		} finally {
			this.close(stmt);
		}
	}

	/**
	 * Retrieves the list of intervals for the current form
	 * 
	 * @param formId
	 *            the form ID
	 * @return The list of intervals associated with the form
	 * @throws CtdbException
	 *             thrown if any errors occur while processing
	 */
	public List<Interval> getIntervals(int formId) throws CtdbException {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		List<Interval> intervals = new ArrayList<Interval>();
		
		try {
			StringBuffer sql = new StringBuffer(50);
			sql.append("select * from interval a, administeredform b ");
			sql.append("where b.formid = ? and a.intervalid = b.intervalid");

			stmt = this.conn.prepareStatement(sql.toString());
			stmt.setLong(1, formId);

			rs = stmt.executeQuery();

			while (rs.next()) {
				intervals.add(this.rsToInterval(rs));
			}
		}
		catch (SQLException e) {
			throw new CtdbException("Unable to get form intervals: " + e.getMessage(), e);
		}
		finally {
			this.close(rs);
			this.close(stmt);
		}
		
		return intervals;
	}

	/**
	 * Retrieves the list of intervals for the form with version
	 * 
	 * @param formId
	 *            the form ID
	 * @param formVersion
	 *            the form ID
	 * @return The list of intervals associated with the form
	 * @throws CtdbException
	 *             thrown if any errors occur while processing
	 */
	public List<Interval> getIntervals(int formId, int formVersion) throws CtdbException {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		List<Interval> intervals = new ArrayList<Interval>();
		
		try {
			StringBuffer sql = new StringBuffer(50);
			sql.append("select * from interval a, administeredform b ");
			sql.append("where b.formid = ? and b.formversion = ? and a.intervalid = b.intervalid");

			stmt = this.conn.prepareStatement(sql.toString());
			stmt.setLong(1, formId);
			stmt.setInt(2, formVersion);

			rs = stmt.executeQuery();

			while (rs.next()) {
				intervals.add(this.rsToInterval(rs));
			}
		}
		catch (SQLException e) {
			throw new CtdbException("Unable to get form intervals: "
					+ e.getMessage(), e);
		}
		finally {
			this.close(rs);
			this.close(stmt);
		}
		
		return intervals;
	}

	/**
	 * Retrieves all form versions from the archive table
	 * 
	 * @param formId
	 *            the form ID for retrieving all its versions
	 * @return A list of all versions of the form
	 * @throws CtdbException
	 *             thrown if any errors occur while processing
	 */
	public List<Form> getFormVersions(int formId) throws CtdbException {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		List<Form> forms = new ArrayList<Form>();

		try {
			StringBuffer sql = new StringBuffer(200);
			
			sql.append("select form_view.*, xformstatus.*, created.username created_username, updated.username updated_username, checkedout.username checkedout_username, ");
			sql.append(" to_char (form_view.updateddate, 'MM-DD-YYYY HH24:MI') as updateddatetime, ");
			sql.append(" to_char (form_view.createddate, 'MM-DD-YYYY HH24:MI') as createddatetime ");
			sql.append(" from form_view");
			sql.append(" JOIN xformstatus ON form_view.xstatusid = xformstatus.xstatusid ");
			sql.append(" JOIN usr created ON form_view.createdby = created.usrid ");
			sql.append(" JOIN usr updated ON form_view.updatedby = updated.usrid");
			sql.append(" LEFT OUTER JOIN usr checkedout ON form_view.checkedoutby = checkedout.usrid");
			sql.append(" where formid = ? ");
			sql.append(" order by version desc");

			stmt = this.conn.prepareStatement(sql.toString());
			stmt.setLong(1, formId);
			rs = stmt.executeQuery();
			
			while (rs.next()) {
				Form form = this.rsToForm(rs);
				form.setCreatedByUsername(rs.getString("created_username"));
				form.setUpdatedByUsername(rs.getString("updated_username"));
				form.setCheckedOutByUsername(rs.getString("checkedout_username"));
				SimpleDateFormat format = new SimpleDateFormat();
				
				try {
					format.applyPattern("MM-dd-yyyy HH:mm");
					form.setUpdatedDate(format.parse(rs
							.getString("updateddatetime")));
					form.setCreatedDate(format.parse(rs
							.getString("createddatetime")));
				}
				catch (Exception e) {
					// if there is an exception, just use
					// date w/ no time
				}
				
				forms.add(form);
			}
		}
		catch (SQLException e) {
			throw new CtdbException("Unable to get the form versions: " + e.getMessage(), e);
		}
		finally {
			this.close(rs);
			this.close(stmt);
		}
		
		return forms;
	}

	/**
	 * Retrieves all form versions from the archive table
	 * 
	 * @param formId
	 *            the form ID for retrieving all its versions
	 * @return A list of all versions of the form
	 * @throws CtdbException
	 *             thrown if any errors occur while processing
	 */
	public List<Version> getFormVersionIds(int formId) throws CtdbException {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		List<Version> versions = new ArrayList<Version>();
		
		try {
			
			StringBuffer sb = new StringBuffer(
					"select version from form_view where formid=?");
			stmt = this.conn.prepareStatement(sb.toString());
			stmt.setLong(1, formId);
			rs = stmt.executeQuery();

			while (rs.next()) {
				versions.add(new Version(rs.getInt("version")));
			}
		}
		catch (SQLException e) {
			throw new CtdbException("Unable to get the form versions: "
					+ e.getMessage(), e);
		}
		finally {
			this.close(rs);
			this.close(stmt);
		}
		
		return versions;
	}

	/**
	 * Retrieves the forms associated with an interval.
	 * 
	 * @param intervalId
	 *            The interval ID
	 * @throws CtdbException
	 *             Thrown if any errors occur while processing
	 */
	public List<Form> getFormsForInterval(int intervalId) throws CtdbException {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		List<Form> forms = new ArrayList<Form>();
		
		try {
			StringBuffer sql = new StringBuffer(50);
			sql.append("select a.*, c.* from form a, administeredform b, xformstatus c ");
			sql.append("where a.formid = b.formid and a.xstatusid = c.xstatusid and b.intervalid = ?");
			
			stmt = this.conn.prepareStatement(sql.toString());
			stmt.setLong(1, intervalId);
			
			rs = stmt.executeQuery();
			Form form;
			while (rs.next()) {
				form = this.rsToForm(rs);
				form.setRowList(getSections(form.getId()));
				form.setIntervalList(getIntervals(form.getId()));
				forms.add(form);
			}
		}
		catch (SQLException e) {
			throw new CtdbException("Unable to get the forms: " + e.getMessage(), e);
		}
		finally {
			this.close(rs);
			this.close(stmt);
		}
		
		return forms;
	}

	/**
	 * Retrieves the list of question id in a section in order of their questionorder.
	 * 
	 * @param sectionId - The section ID
	 * @param questionIds - The list of question ids.
	 * @throws CtdbException	Thrown if any errors occur while processing
	 */
	public void getQuestionIdsInSection(int sectionId, List<Integer> questionIds) throws CtdbException {
		PreparedStatement stmt = null;
		ResultSet rs = null;

		try {
			// gets the max question order value for the section ID
			String sql = "select questionid from sectionquestion where sectionid = ? order by questionorder ";

			stmt = this.conn.prepareStatement(sql);
			stmt.setLong(1, sectionId);

			rs = stmt.executeQuery();

			while (rs.next()) {
				questionIds.add(new Integer(rs.getInt("questionid")));
			}
		}
		catch (SQLException e) {
			throw new CtdbException("Unable to get Question for the Section with section ID: " + sectionId + " : " + e.getMessage(), e);
		}
		finally {
			this.close(rs);
			this.close(stmt);
		}
	}

	/**
	 * Retrieves the list of question id in a section in order of their questionorder.
	 * 
	 * @param sectionId - The section ID
	 * @param formVersion - The form version number
	 * @param questionIds - The list of question ids.
	 * @throws CtdbException	Thrown if any errors occur while processing
	 */
	public void getQuestionIdsInSection(int sectionId, int formVersion, List<Integer> questionIds) throws CtdbException {
		PreparedStatement stmt = null;
		ResultSet rs = null;

		try {
			String sql = "select questionid from sectionquestion_view where sectionid = ? and formversion = ? order by questionorder ";

			stmt = this.conn.prepareStatement(sql);
			stmt.setLong(1, sectionId);
			stmt.setInt(2, formVersion);
			rs = stmt.executeQuery();
			
			while (rs.next()) {
				questionIds.add(new Integer(rs.getInt("questionid")));
			}
		}
		catch (SQLException e) {
			throw new CtdbException("Unable to get Question for the Section with section ID: " + sectionId + " : " + e.getMessage(), e);
		}
		finally {
			this.close(rs);
			this.close(stmt);
		}
	}

	public Question getSectionQuestion(int sectionId, int questionId) throws CtdbException {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		Question question = new Question();

		try {
			// gets the max question order value for the section ID
			String sql = "select questionorder,  questionorder_col, questionid, questionversion, questionattributesid from sectionquestion " +
				"where sectionid = ? and questionid = ? ";

			stmt = this.conn.prepareStatement(sql);
			stmt.setLong(1, sectionId);
			stmt.setLong(2, questionId);
			rs = stmt.executeQuery();

			if (rs.next()) {
				question.setId(rs.getInt("questionid"));
				question.setQuestionOrder(rs.getInt("questionorder"));
				question.setQuestionOrderCol(rs.getInt("questionorder_col"));
				question.setVersion(new Version(rs.getInt("questionversion")));
				
				FormQuestionAttributes attrs = new FormQuestionAttributes();
				attrs.setId(rs.getInt("questionattributesid"));
				question.setFormQuestionAttributes(attrs);
			}
		}
		catch (SQLException e) {
			throw new CtdbException("Unable to get Question for the Section with section ID: " + sectionId + " : " + e.getMessage(), e);
		}
		finally {
			this.close(rs);
			this.close(stmt);
		}
		
		return question;
	}

	/**
	 * Adds questions to a section.
	 * 
	 * @param sectionId - The section ID for questions to add to
	 * @param questionList - The list of questions to be added to the section
	 * @throws DuplicateObjectException
	 *		Thrown if the question already exists in the section based on the unique constraints
	 * @throws CtdbException
	 *		Thrown if any other errors occur while processing
	 */
	public void addQuestionsToSection(int sectionId, List<Question> questionList) throws CtdbException, DuplicateObjectException {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		Question q = null;
		
		try {
			String sql = "insert into sectionquestion (questionid, questionversion, sectionid, questionorder, questionattributesid, questionorder_col) " +
				"values (?, ?, ?, ?, ?, ?) ";
			
			stmt = this.conn.prepareStatement(sql.toString());
			stmt.setInt(3, sectionId);

			for ( Iterator<Question> it = questionList.iterator(); it.hasNext(); ) {
				q = it.next();
				stmt.setLong(1, q.getId());
				stmt.setInt(2, q.getVersion().getVersionNumber());
				
				// --
				// we can now trust the data coming from the formbuilder so
				// we can use questionOrder and questionOrder_col directly
				// - assuming they're set here at least?  Are they?  
				// TODO: JP
				stmt.setInt(4, q.getQuestionOrder());
				stmt.setLong(5, q.getFormQuestionAttributes().getId());
				stmt.setInt(6, q.getQuestionOrderCol());
				stmt.executeUpdate();
			}
		}
		catch ( PSQLException e ) {
			if ( e.getSQLState().equals(SysPropUtil.getProperty("postgres_database.unique_constraint_sqlstate")) ) {
				throw new DuplicateObjectException("A question with the name " + q.getName() + " already exists in the section: " + e.getMessage(), e);
			}
			else {
				throw new CtdbException( "Unable to attach Questions to the Section with section ID: " + sectionId + " : " + e.getMessage(), e);
			}
		}
		catch ( SQLException e ) {
			if ( e.getErrorCode() == Integer.parseInt(SysPropUtil.getProperty("database.unique_constraint_error.code")) ) {
				throw new DuplicateObjectException("A question with the name " + q.getName() + " already exists in the section: " + e.getMessage(), e);
			}
			else {
				throw new CtdbException("Unable to attach Questions to the Section with section ID: " + sectionId + " : " + e.getMessage(), e);
			}
		}
		finally {
			this.close(rs);
			this.close(stmt);
		}
	}

	/**
	 * Checks to see if the skip rules need to update for moving questions
	 * between sections.
	 * 
	 * @param orgSectionId
	 *            The original section ID that the question moved from
	 * @param sectionId
	 *            The new section ID for the question
	 * @param questionId
	 *            The question need to have skip rules updated.
	 * @throws CtdbException
	 *             Thrown if any errors occur while processing
	 */
	public void updateSkipRules(int orgSectionId, int sectionId, int questionId) throws CtdbException {
		PreparedStatement stmt = null;

		try {
			String sql = "update skiprulequestion set sectionid = ? where sectionid = ? and questionid = ? ";

			stmt = this.conn.prepareStatement(sql);
			stmt.setLong(1, sectionId);
			stmt.setLong(2, orgSectionId);
			stmt.setLong(3, questionId);

			int recordsUpdated = stmt.executeUpdate();

			if (recordsUpdated == 0) {
				throw new ObjectNotFoundException("The skip rule questions with section ID " + orgSectionId + " and question ID " + 
					questionId + " do not exist in the system.");
			}
		}
		catch (SQLException e) {
			throw new CtdbException("Unable to update the skip rule questions: " + e.getMessage(), e);
		}
		finally {
			this.close(stmt);
		}
	}

	/**
	 * Gets the section ID from section question with formId and questionId.
	 * 
	 * @param formId - The form that has the section
	 * @param questionId - The question that in the section
	 * @return Section the Section object
	 * @throws CtdbException	Thrown if any errors occur while processing
	 */
	public Section getSectionByFormAndQuestion(int formId, int questionId) throws CtdbException {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		Section s = null;
		
		try {
			String sql = "select section.* from sectionquestion sq, section, form where sq.questionid = ? and sq.sectionid = section.sectionid and " +
				"section.formid = form.formid and form.formid = ? ";
			
			stmt = this.conn.prepareStatement(sql);
			stmt.setLong(1, questionId);
			stmt.setLong(2, formId);
			rs = stmt.executeQuery();
			
			if (!rs.next()) {
				throw new ObjectNotFoundException("The section with form ID " + formId + " and question ID " + 
					questionId + " could not be found.");
			}
			
			s = this.rsToSection(rs);
		}
		catch (SQLException e) {
			throw new CtdbException("Unable to get section by formId and questionId: " + e.getMessage(), e);
		}
		finally {
			this.close(rs);
			this.close(stmt);
		}
		
		return s;
	}

	/**
	 * Adds questions to a section.
	 * 
	 * @param sectionId
	 *            The section ID for questions to add to
	 * @param questionList
	 *            The list of questions to be added to the section
	 * @param questionAttributes
	 *            Form question attributes object contains the user information
	 * @throws DuplicateObjectException
	 *             Thrown if the question already exists in the section based on
	 *             the unique constraints
	 * @throws CtdbException
	 *             Thrown if any other errors occur while processing
	 */
	public void addQuestionsToSection(int sectionId, List<Question> questionList, FormQuestionAttributes questionAttributes, 
			Integer[] rows, Integer[] cols) throws CtdbException, DuplicateObjectException {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		Question q = null;
		
		try {
			String sql = "";

			// gets the max question order value for the section ID
			sql = "select coalesce(max(questionorder) + 1, 1) from sectionquestion where sectionid = ? ";

			stmt = this.conn.prepareStatement(sql);
			stmt.setLong(1, sectionId);
			rs = stmt.executeQuery();
			
			int orderval = 1;
			
			if (rs.next()) {
				orderval = rs.getInt(1);
			}

			rs.close();
			stmt.close();
			
			sql = "insert into sectionquestion (questionid, questionversion, sectionid, questionorder, questionattributesid, questionorder_col) " +
				"values (?, ?, ?, ?, ?, ?) ";

			stmt = this.conn.prepareStatement(sql);
			stmt.setLong(3, sectionId);

			int questionAttributesId = Integer.MIN_VALUE;
			int questionIndex = 0;
			
			for (Iterator<Question> it = questionList.iterator(); it.hasNext();) {
				q = it.next();
				q.setQuestionOrder(rows[questionIndex]);
				q.setQuestionOrderCol(cols[questionIndex]);
				questionIndex++;
				
				if (q.getInstanceType().equals(InstanceType.VISUAL_SCALE_QUESTION)) {
					questionAttributes.getHtmlAttributes().setAlign("center");
				}
				else {
					questionAttributes.getHtmlAttributes().setAlign("left");
				}
				
				questionAttributesId = this.createDefaultQuestionAttributes(q, questionAttributes);
				stmt.setLong(1, q.getId());
				stmt.setInt(2, q.getVersion().getVersionNumber());
				
				// we have to change the below because we can't assume it's all
				// incremental now with columns
				// --
				// we can now trust the data coming from the formbuilder so
				// we can use questionOrder and questionOrder_col directly
				// - assuming they're set here at least?  Are they?  
				// TODO: JP
				//stmt.setInt(4, orderval++);
				// instead, I will put it here as an "in case"
				orderval++;
				
				if (q.getQuestionOrder() != 0) {
					stmt.setInt(4, q.getQuestionOrder());
				}
				else {
					stmt.setInt(4, orderval);
				}
				
				stmt.setLong(5, questionAttributesId);
				stmt.setInt(6, q.getQuestionOrderCol());
				stmt.executeUpdate();
				q.getFormQuestionAttributes().setId(questionAttributesId);
			}
		}
		catch (PSQLException e) {
			if (e.getSQLState().equals(SysPropUtil.getProperty("postgres_database.unique_constraint_sqlstate"))) {
				throw new DuplicateObjectException("A question with the name " + q.getName() + " already exists in the section: " + e.getMessage(), e);
			}
			else {
				throw new CtdbException("Unable to attach Questions to the Section with section ID ?: " + sectionId + " : " + e.getMessage(), e);
			}
		}
		catch (SQLException e) {
			if (e.getErrorCode() == Integer.parseInt(SysPropUtil.getProperty("database.unique_constraint_error.code"))) {
				throw new DuplicateObjectException("A question with the name " + q.getName() + " already exists in the section: " + e.getMessage(), e);
			}
			else {
				throw new CtdbException("Unable to attach Questions to the Section with section ID ?: " + sectionId + " : " + e.getMessage(), e);
			}
		}
		finally {
			this.close(rs);
			this.close(stmt);
		}
	}

	/**
	 * Deletes the question attributes before deleting questions from the section.
	 * 
	 * @param deletedQuestions - The list of questions to be deleted from the section.
	 * @param sectionId - The section Id that has the deleted questions.
	 * @throws CtdbException	Thrown if any errors occur while processing
	 * @author Ching Heng (modified)
	 */
	public void deleteQuestionAttributes(List<Question> deletedQuestions, int sectionId) throws CtdbException {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		List<String> questionAttributesIds = new ArrayList<String>();
		List<String> emailTriggerIds = new ArrayList<String>();

		try {
			// get questionAttributesIds and emailTriggerIds
			StringBuffer sql = new StringBuffer("select qa.questionattributesid, qa.emailtriggerid from questionattributes qa, sectionquestion sq " +
				"where sq.sectionid = ? and qa.questionattributesid = sq.questionattributesid and qa.questionid in (");
			
			for (Iterator<Question> it = deletedQuestions.iterator(); it.hasNext();) {
				Question question = it.next();
				
				if (it.hasNext()) {
					sql.append(question.getId() + ", ");
				}
				else {
					sql.append(question.getId() + ") ");
				}
			}
			
			stmt = this.conn.prepareStatement(sql.toString());
			stmt.setLong(1, sectionId);
			rs = stmt.executeQuery();
			
			while (rs.next()) {
				questionAttributesIds.add(rs.getString("questionattributesid"));
				emailTriggerIds.add(rs.getString("emailtriggerid"));
			}
			
			rs.close();
			stmt.close();

			// To delete question attributes
			sql = new StringBuffer("delete from questionattributes where questionattributesid in (");
			
			for (Iterator<String> it = questionAttributesIds.iterator(); it.hasNext();) {
				String qAttrId = it.next();
				
				if (it.hasNext()) {
					sql.append(qAttrId + ", ");
				}
				else {
					sql.append(qAttrId + ") ");
				}
			}
			
			stmt = this.conn.prepareStatement(sql.toString());
			stmt.executeUpdate();
			stmt.close();

			// To delete email trigger value
			StringBuffer sql2 = new StringBuffer();
			
			for (Iterator<String> it = emailTriggerIds.iterator(); it.hasNext();) {
				String triggerId = it.next();
				
				if (it.hasNext()) {
					sql2.append(triggerId + ", ");
				}
				else {
					sql2.append(triggerId + ") ");
				}
			}

			sql = new StringBuffer("delete FROM emailtriggervalues where emailtriggerid in (");
			sql.append(sql2);
			stmt = this.conn.prepareStatement(sql.toString());
			stmt.executeUpdate();
			stmt.close();

			// To delete e-mail trigger
			sql = new StringBuffer("delete FROM emailtrigger where emailtriggerid in (");
			sql.append(sql2);
			stmt = this.conn.prepareStatement(sql.toString());
			stmt.executeUpdate();

		}
		catch (SQLException e) {
			throw new CtdbException("Unable to delete the question attributes for the section id = " + sectionId + 
				" : " + e.getMessage(), e);
		}
		finally {
			this.close(rs);
			this.close(stmt);
		}
	}

	/**
	 * Deletes the question attributes before deleting questions from the section.
	 * 
	 * @param deletedQuestion - The question to be deleted from the section.
	 * @param sectionId - The section Id that has the deleted questions.
	 * @throws CtdbException	Thrown if any errors occur while processing
	 * @author Ching Heng
	 */
	public void deleteSingleQuestionAttribute(int deletedQuestionId, int sectionId) throws CtdbException {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		int questionattributesid = Integer.MIN_VALUE;
		int emailtriggerid = Integer.MIN_VALUE;
		
		try {
			String sql = "select qa.questionattributesid,qa.emailtriggerid from questionattributes qa, sectionquestion sq where " +
				"sq.sectionid = ? and qa.questionattributesid = sq.questionattributesid and qa.questionid = ? ";
			
			stmt = this.conn.prepareStatement(sql);
			stmt.setLong(1, sectionId);
			stmt.setLong(2, deletedQuestionId);
			rs = stmt.executeQuery();
			
			if (rs.next()) {
				questionattributesid = rs.getInt("questionattributesid");
				emailtriggerid = rs.getInt("emailtriggerid");
			}
			
			rs.close();
			stmt.close();
			
			// To delete e-mail trigger value
			sql = "delete FROM emailtriggervalues where emailtriggerid = ? ";
			stmt = this.conn.prepareStatement(sql);
			stmt.setLong(1, emailtriggerid);
			stmt.executeUpdate();
			stmt.close();

			// To delete question attribute
			sql = "delete from questionattributes where questionattributesid = ? ";
			stmt = this.conn.prepareStatement(sql);
			stmt.setLong(1, questionattributesid);
			stmt.executeUpdate();
			stmt.close();

			// To delete e-mail trigger
			sql = "delete FROM emailtrigger where emailtriggerid = ? ";
			stmt = this.conn.prepareStatement(sql.toString());
			stmt.setLong(1, emailtriggerid);
			stmt.executeUpdate();
		}
		catch (SQLException e) {
			throw new CtdbException("Unable to delete the single question attribute for the section id = " + 
				sectionId + " : " + e.getMessage(), e);
		}
		finally {
			this.close(rs);
			this.close(stmt);
		}
	}

	/**
	 * Deletes the question attributes before deleting questions from the
	 * section.
	 * 
	 * @param q
	 *            the question w/ attached questions to delete
	 * @param sectionId
	 *            The section Id that has the deleted questions.
	 * @throws CtdbException
	 *             Thrown if any errors occur while processing
	 */
	public void removeDependentCalculatedQuestions(int questionId, int sectionId) throws CtdbException {
		PreparedStatement stmt = null;

		try {
			String sql = "delete from calculatequestion where sectionid = ? and questionid = ? ";

			stmt = this.conn.prepareStatement(sql);
			stmt.setLong(1, sectionId);
			stmt.setLong(2, questionId);

			stmt.executeUpdate();
		}
		catch (SQLException e) {
			throw new CtdbException("Unable to delete the question attributes for the section id = " + sectionId + " : " + e.getMessage(), e);
		}
		finally {
			this.close(stmt);
		}
	}

	/**
	 * Deletes the question skip rules before deleting questions from the
	 * section.
	 * 
	 * @param deletedQuestions
	 *            The list of questions to be deleted from the section.
	 * @param sectionId
	 *            The section Id that has the deleted questions.
	 * @throws CtdbException
	 *             Thrown if any errors occur while processing
	 */
	public void deleteSkipRules(List<Question> deletedQuestions, int sectionId) throws CtdbException {
		PreparedStatement stmt = null;

		try {
			StringBuffer sql = new StringBuffer(200);
			sql.append("delete from skiprulequestion ");
			sql.append("where sectionid = ? ");
			sql.append("and skipquestionid in ( ");
			sql.append("select skipquestionid ");
			sql.append("from skiprulequestion ");
			sql.append("where sectionid = ? ");
			sql.append("and questionid in ( ");

			int listSize = deletedQuestions.size();
			int i = 0;
			
			for ( Question question : deletedQuestions ) {
				i = i + 1;
				
				if ( i < listSize ) {
					sql.append(question.getId() + ",");
				}
				else {
					sql.append(question.getId() + ")");
				}
			}
			
			sql.append(")");
			stmt = this.conn.prepareStatement(sql.toString());
			stmt.setLong(1, sectionId);
			stmt.setLong(2, sectionId);
			stmt.executeUpdate();
		}
		catch (SQLException e) {
			throw new CtdbException("Unable to delete the question skip rules for the section id = " + sectionId + " : " + e.getMessage(), e);
		}
		finally {
			this.close(stmt);
		}
	}

	public void validateFormVersionQuestionRemoval(Form f, List<Question> deletedQuestions) throws CtdbException, QuestionRemovalException {
		PreparedStatement stmt = null;
		ResultSet rs = null;

		try {
			StringBuffer sb = new StringBuffer("select r.responseid from response r, administeredform af where ");
			sb.append(" r.administeredformid = af.administeredformid  and af.formid = ? ");
			sb.append(" and r.questionid in  ( ");
			
			for ( Question q : deletedQuestions ) {
				sb.append(Long.toString(q.getId()) + " , ");
			}
			
			sb.deleteCharAt(sb.length() - 1);
			sb.append(" ) ");
			
			stmt = this.conn.prepareStatement(sb.toString());
			stmt.setLong(1, f.getId());
			rs = stmt.executeQuery();
			
			if ( rs.next() ) {
				throw new QuestionRemovalException("This question is on an administered form version ");
			}
		}
		catch (SQLException e) {
			throw new CtdbException("verify question removal w.r.t. form versioninging: " + e.getMessage(), e);
		}
		finally {
			this.close(rs);
			this.close(stmt);
		}
	}

	/**
	 * Creates the default attributes for each question attached to the form
	 * section.
	 * 
	 * @param question
	 *            Question object contains the attributes and the user
	 *            information
	 * @return The question attributes id for the new record in
	 *         QUESTIONATTRIBUTES table
	 */
	public int createDefaultQuestionAttributes(Question question, FormQuestionAttributes questionAttributes) throws CtdbException {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		int newId = -1;

		try {
			String sql = "insert into questionattributes(questionattributesid, questionid, questionversion, indent, createdby, createddate, " +
				"updatedby, updateddate, answertype, mincharacters, maxcharacters, horizontalDisplay, horizDisplayBreak, halign, data_element_name, " +
				"prepopulation,prepopulationValue,decimalprecision,hasconversionfactor,conversionfactor,showtext,tableheadertype) values (DEFAULT, ?, ?, ?, ?, " +
				"CURRENT_TIMESTAMP, ?, CURRENT_TIMESTAMP, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) ";

			stmt = this.conn.prepareStatement(sql);
			stmt.setLong(1, question.getId());
			stmt.setInt(2, question.getVersion().getVersionNumber());
			stmt.setInt(3, questionAttributes.getHtmlAttributes().getIndent());
			stmt.setLong(4, questionAttributes.getCreatedBy());
			stmt.setLong(5, questionAttributes.getUpdatedBy());
			stmt.setInt(6, 1);
			stmt.setInt(7, questionAttributes.getMinCharacters());
			stmt.setInt(8, questionAttributes.getMaxCharacters());
			stmt.setBoolean(9, questionAttributes.isHorizontalDisplay());
			stmt.setBoolean(10, questionAttributes.isHorizDisplayBreak());
			stmt.setString(11, questionAttributes.getHtmlAttributes().getAlign());
			stmt.setString(12, questionAttributes.getDataElementName());
			stmt.setBoolean(13, questionAttributes.isPrepopulation());
			stmt.setString(14, questionAttributes.getPrepopulationValue());
			stmt.setInt(15, questionAttributes.getDecimalPrecision());
			stmt.setBoolean(16, questionAttributes.isHasUnitConversionFactor());
			stmt.setString(17, questionAttributes.getUnitConversionFactor());
			stmt.setBoolean(18, questionAttributes.isShowText());
			if(questionAttributes.getTableHeaderType() == 0) {
				stmt.setNull(19, java.sql.Types.INTEGER);
			}else {
				stmt.setInt(19, questionAttributes.getTableHeaderType());
			}
			
			
			
			

			stmt.executeUpdate();
			newId = getInsertId(conn, "questionattributes_seq");
		}
		catch (SQLException e) {
			throw new CtdbException("Unable to create form question attributes: " + e.getMessage(), e);
		}
		finally {
			this.close(rs);
			this.close(stmt);
		}
		
		return newId;
	}

	/**
	 * Gets the form question attributes for editing in the section.
	 * 
	 * @param sectionId
	 *            The section which the question attached to.
	 * @param questionId
	 *            The question object that has the attributes
	 * @return form question attributes data object
	 * @throws ObjectNotFoundException
	 *             Thrown if the form question attributes do not exist
	 * @throws CtdbException
	 *             Thrown if any other errors occur while processing
	 */
	public FormQuestionAttributes getFormQuestionAttributes(int sectionId, int questionId) throws ObjectNotFoundException, CtdbException {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		FormQuestionAttributes qAttrs = null;

		try {
			String sql = "select questionattributes.*, et.*, etv.* from sectionquestion JOIN questionattributes ON " +
				"sectionquestion.questionattributesid = questionattributes.questionattributesid LEFT OUTER JOIN emailtrigger et ON " +
				"et.emailtriggerid = questionattributes.emailtriggerid LEFT OUTER JOIN emailtriggervalues etv ON " +
				"et.emailtriggerid = etv.emailtriggerid where sectionquestion.sectionid = ? and sectionquestion.questionid = ? ";

			stmt = this.conn.prepareStatement(sql);
			stmt.setInt(1, sectionId);
			stmt.setInt(2, questionId);

			rs = stmt.executeQuery();

			for (int i = 0; rs.next(); i++) {
				if (i == 0) {
					qAttrs = this.rsToQuestionAttributes(rs);
				}
				
				qAttrs.getEmailTrigger().getTriggerAnswers().add(rs.getString("answer"));
			}
			
			if (qAttrs == null) {
				throw new ObjectNotFoundException("The question attributes for the section: " + sectionId + "  and question: " + 
					questionId + " could not be found.");
			}

			if (qAttrs instanceof CalculatedFormQuestionAttributes) {
				try {
					QuestionManager qm = new QuestionManager();
					((CalculatedFormQuestionAttributes) qAttrs).setQuestionsToCalculate(qm.getCalculateQuestions(
						(CalculatedFormQuestionAttributes) qAttrs, sectionId));

				} catch (Exception e) {
					throw new CtdbException(e.getMessage() + " failed to get the questionmanager");
				}
			}
		}
		catch (SQLException e) {
			throw new CtdbException("Unable to get form question attributes: " + e.getMessage(), e);
		}
		finally {
			this.close(rs);
			this.close(stmt);
		}
		
		return qAttrs;
	}
	
	public List<Integer> getOrphanQuestionIds(int formId) throws CtdbException {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		List<Integer> orphanQuestionIds = new ArrayList<Integer>();
		
		try {
			String sql = "select q.questionid from question q where name like ? " + 
							"and q.questionid not in " + 
							" ( " +
							"select  distinct sq.questionid from sectionquestion sq, section s, form f where " +
							"sq.sectionid = s.sectionid and " + 
							"s.formid = f.formid and " +
							"f.formid = ? " +
							"order by sq.questionid " +
							") " +
							"order by questionid";

			stmt = this.conn.prepareStatement(sql);
			stmt.setString(1, "" + formId + "%");
			stmt.setInt(2, formId);

			rs = stmt.executeQuery();
			
			while (rs.next()) {
				Integer qId = new Integer(rs.getInt("questionid"));
				orphanQuestionIds.add(qId);
			}
		}
		catch (SQLException e) {
			throw new CtdbException("Unable to get form question attributes: " + e.getMessage(), e);
		}
		finally {
			this.close(rs);
			this.close(stmt);
		}
		
		return orphanQuestionIds;
	}

	/**
	 * Adds the skip rule questions to the section ID for form "Save As"
	 * function.
	 * 
	 * @param oldSectionId
	 *            The section ID for skip rule question to copy from
	 * @param sectionId
	 *            The section ID for skip rule questions to add to
	 * @throws CtdbException
	 *             Thrown if any errors occur while processing
	 */
	public void addSkipRuleQuestionsForSaveAs(int oldSectionId, int sectionId, HashMap<Integer, Integer> oldSectionIdNewSectionIdMap, HashMap<Integer, Integer> oldQuestionIdNewQuestionIdMap) throws CtdbException {
		PreparedStatement stmt = null;
		PreparedStatement stmt2 = null;
		ResultSet rs = null;

		try {
			String sql = "select * from skiprulequestion where sectionid = ? ";
			
			stmt = this.conn.prepareStatement(sql);
			stmt.setLong(1, oldSectionId);
			rs = stmt.executeQuery();
			
			sql = "insert into skiprulequestion (questionid, skipquestionid, sectionid, skipsectionid) values (?, ?, ?, ?) ";
			stmt2 = this.conn.prepareStatement(sql);

			while (rs.next()) {
				int oldQuestionId = rs.getInt("questionid");
				int oldSkipQuestionId = rs.getInt("skipquestionid");
				int newQuestionId = oldQuestionIdNewQuestionIdMap.get(new Integer(oldQuestionId)).intValue();
				int newSkipQuestionId = oldQuestionIdNewQuestionIdMap.get(new Integer(oldSkipQuestionId)).intValue();
				
				stmt2.setLong(1, newQuestionId);
				stmt2.setLong(2, newSkipQuestionId);
				stmt2.setLong(3, sectionId);
				int oldSkipSecId = rs.getInt("skipsectionid");
				int newSkipSecId = oldSectionIdNewSectionIdMap.get(new Integer(oldSkipSecId)).intValue();
				stmt2.setLong(4, newSkipSecId);
				stmt2.executeUpdate();
			}
		}
		catch (SQLException e) {
			throw new CtdbException("Unable to add Skip Rule Questions for Save As to the Section with section ID: " + sectionId + 
				" : " + e.getMessage(), e);
		}
		finally {
			this.close(rs);
			this.close(stmt);
			this.close(stmt2);
		}
	}

	/**
	 * Adds the calculated questions to the section ID for form "Save As"
	 * function.
	 * 
	 * @param oldSectionId - The section ID for calculated question to copy from
	 * @param sectionId - The section ID for calculated questions to add to
	 * @throws CtdbException	Thrown if any errors occur while processing
	 */
	public void addCalculationsForSaveAs(int oldSectionId, int sectionId, HashMap<Integer, Integer> oldSectionIdNewSectionIdMap, HashMap<Integer, Integer> oldQuestionIdNewQuestionIdMap) throws CtdbException {
		PreparedStatement stmt = null;
		PreparedStatement stmt2 = null;
		ResultSet rs = null;

		try {
			String sql = "select * from calculatequestion where sectionid = ? ";
			stmt = this.conn.prepareStatement(sql);
			stmt.setLong(1, oldSectionId);
			rs = stmt.executeQuery();
			
			sql = "insert into calculatequestion(questionid, calculatequestionid, sectionid, orderval, calculatesectionid) " +
				"values (?, ?, ?, ?, ?) ";
			stmt2 = this.conn.prepareStatement(sql);

			while (rs.next()) {
				int oldQuestionId = rs.getInt("questionid");
				int oldCalculatequestionid = rs.getInt("calculatequestionid");
				int newQuestionId = oldQuestionIdNewQuestionIdMap.get(new Integer(oldQuestionId)).intValue();
				int newCalculatequestionid = oldQuestionIdNewQuestionIdMap.get(new Integer(oldCalculatequestionid)).intValue();
				
				
				stmt2.setLong(1, newQuestionId);
				stmt2.setLong(2, newCalculatequestionid);
				stmt2.setLong(3, sectionId);
				stmt2.setInt(4, rs.getInt("orderval"));
				stmt2.setInt(5, oldSectionIdNewSectionIdMap.get(rs.getInt("calculatesectionid")));
				stmt2.executeUpdate();
			}
		}
		catch (SQLException e) {
			throw new CtdbException("Unable to add Calculated Questions for Save As to the Section with section ID: " + sectionId + 
					" : " + e.getMessage(), e);
		}
		finally {
			this.close(rs);
			this.close(stmt);
			this.close(stmt2);
		}
	}
	
	/**
	 * Updates the calculate question formula when save as
	 */
	public String updateCalculationFormula(HashMap<Integer, Integer> oldQuestionIdNewQuestionIdMap, HashMap<Integer, Integer> oldSectionIdNewSectionIdMap,
			Question question) throws ObjectNotFoundException, CtdbException {
		String formula=question.getCalculatedFormQuestionAttributes().getCalculation();
		
		if (formula != null) {
			ArrayList<Integer> indicesList = new ArrayList<Integer>();
			int s_ind = -1;
			
			for (int fromInd=0;fromInd<formula.length();) {
				s_ind = formula.indexOf("S_",fromInd);
				logger.info("*** " + s_ind);
				
				if (s_ind != -1) {
					indicesList.add(new Integer(s_ind));
					fromInd = s_ind + 1;
				}
				else {
					break;
				}
			}
            
            if (indicesList.size() > 0) {
                for (int i = indicesList.size() - 1; i >= 0; i--) {
                    int sInd = ((Integer)indicesList.get(i)).intValue();
                    int qInd = formula.indexOf("_Q", sInd);
                    
                    if (!oldQuestionIdNewQuestionIdMap.isEmpty()) {
                    	int bracketInd = formula.indexOf("]",qInd);
	                    int oldQueId = Integer.parseInt(formula.substring(qInd+3, bracketInd));
	                    int newQueId = oldQuestionIdNewQuestionIdMap.get(new Integer(oldQueId)).intValue();
	                    formula = formula.substring(0, qInd+3) + newQueId + formula.substring(bracketInd, formula.length());  
                    }
                    
                    int oldSecId = Integer.parseInt(formula.substring(sInd+2, qInd));
                    int newSecId = oldSectionIdNewSectionIdMap.get(new Integer(oldSecId)).intValue();
                    
                    formula = formula.substring(0, sInd+2) + newSecId + formula.substring(qInd, formula.length());                               
               }
            }
            
            logger.info(formula);
		}
		else {
			formula = "";
		}
		
		return formula;
    }
	
	/**
	 * Adds questions to a section.
	 * 
	 * @param sectionId
	 *            The section ID for questions to add to
	 * @param questionList
	 *            The list of questions to be added to the section
	 * @throws DuplicateObjectException
	 *             Thrown if the question already exists in the section based on
	 *             the unique constraints
	 * @throws CtdbException
	 *             Thrown if any other errors occur while processing
	 */
	public void addQuestionsToSectionForSaveAs(int oldSectionId, int sectionId, List<Question> questionList, boolean isSameDataStructureId,
			boolean nonCopyrightToCopyright, HashMap<Integer, Integer> oldSectionIdNewSectionIdMap, HashMap<Integer, Integer> oldQuestionIdNewQuestionIdMap) throws CtdbException, DuplicateObjectException {
		PreparedStatement stmt = null;
		PreparedStatement attrStmt = null;
		ResultSet rs = null;
		Question q = null;

		try {
			StringBuffer attrSql = new StringBuffer("insert into questionattributes ( ");
			attrSql.append(" questionattributesid, questionid, questionversion, ");
			attrSql.append(" requiredflag, calculatedflag, calculation, skipruleflag, skipruletype, skipruleoperatortype, skipruleequals, ");
			attrSql.append(" halign, valign, textcolor, fontface, fontsize, indent, rangeoperator, rangevalue1, rangevalue2, dtconversionfactor, ");
			attrSql.append(" createdby, createddate, updatedby, updateddate, label, answertype, mincharacters, maxcharacters, horizontaldisplay, ");
			attrSql.append(" textareaheight,textareawidth, horizDisplayBreak, textboxlength, emailtriggerid, dataspring, data_element_name,prepopulation,prepopulationValue,decimalprecision,hasconversionfactor,conversionfactor,group_name,showtext,tableheadertype) ");
			attrSql.append(" (select ?, ");
			attrSql.append(" ?, questionversion, ");
			attrSql.append(" requiredflag, calculatedflag, ?, skipruleflag, skipruletype, skipruleoperatortype, skipruleequals, ");
			attrSql.append(" halign, valign, textcolor, fontface, fontsize, indent, rangeoperator, rangevalue1, rangevalue2, dtconversionfactor, ");
			attrSql.append(" createdby, createddate, updatedby, updateddate, label, answertype, mincharacters, maxcharacters, horizontaldisplay, ");
			attrSql.append(" textareaheight, textareawidth, horizDisplayBreak, textboxlength, ?, dataspring, data_element_name,prepopulation,prepopulationValue,decimalprecision,hasconversionfactor,conversionfactor,group_name,showtext,tableheadertype  ");
			attrSql.append(" from questionattributes where questionattributesid = ");
			attrSql.append(" (select questionattributesid from sectionquestion where sectionid = ? and questionid = ? )");
			attrSql.append(" )  ");

			StringBuffer sql = new StringBuffer(100);
			sql.append("insert into sectionquestion(sectionid, questionattributesid, questionid, questionversion, questionorder, questionorder_col, suppressflag ) ");
			sql.append(" (select ?, ?, ?, questionversion, questionorder, questionorder_col, suppressflag from ");
			sql.append(" sectionquestion where questionid = ? and sectionid = ? ) ");

			int attrId = getSetSequenceValues(conn, "questionattributes_seq", questionList.size() + 15);
			stmt = this.conn.prepareStatement(sql.toString());
			attrStmt = this.conn.prepareStatement(attrSql.toString());

			for ( Iterator<Question> it = questionList.iterator(); it.hasNext(); ) {
				q = it.next();

				int newQuestionId = oldQuestionIdNewQuestionIdMap.get(new Integer(q.getId())).intValue();

				stmt.setLong(1, sectionId);
				stmt.setLong(2, attrId);
				stmt.setLong(3, newQuestionId);
				stmt.setLong(4, q.getId());
				stmt.setLong(5, oldSectionId);

				attrStmt.setLong(1, attrId);
				String newFomular = updateCalculationFormula(oldQuestionIdNewQuestionIdMap, oldSectionIdNewSectionIdMap, q);


				attrStmt.setLong(2, newQuestionId);

				attrStmt.setString(3, newFomular);

				if (q.getFormQuestionAttributes().getEmailTrigger().getId() != Integer.MIN_VALUE) {
					//this means the old question had an email trigger and so we need to create a new one first
					EmailTriggerDao.getInstance(conn).createEmailTrigger(q.getFormQuestionAttributes().getEmailTrigger());
					attrStmt.setLong(4, q.getFormQuestionAttributes().getEmailTrigger().getId());        
				}
				else {
					attrStmt.setNull(4, java.sql.Types.INTEGER);
				}

				attrStmt.setLong(5, oldSectionId);
				attrStmt.setLong(6, q.getId());

				attrStmt.addBatch();
				stmt.addBatch();
				attrId++;
			}

			attrStmt.executeBatch();
			stmt.executeBatch();
		}
		catch (PSQLException e) {
			if (e.getSQLState().equals(SysPropUtil.getProperty("postgres_database.unique_constraint_sqlstate"))) {
				throw new DuplicateObjectException("A question with the name " + q.getName() + " already exists in the section: " + 
						e.getMessage(), e);
			}
			else {
				throw new CtdbException("Unable to attach Questions to the Section with section ID: " + sectionId + 
						" : " + e.getMessage(), e);
			}
		}
		catch (SQLException e) {
			if (e.getErrorCode() == Integer.parseInt(SysPropUtil.getProperty("database.unique_constraint_error.code"))) {
				throw new DuplicateObjectException("A question with the name " + q.getName() + " already exists in the section: " + 
						e.getMessage(), e);
			}
			else {
				throw new CtdbException("Unable to attach Questions to the Section with section ID: " + 
						sectionId + " : " + e.getMessage(), e);
			}
		}
		finally {			
			this.close(rs);
			this.close(stmt);
			this.close(attrStmt);
		}
	}

	/**
	 * Creates section question archive for all questions for all secitons in
	 * the form.
	 * 
	 * @param formId
	 *            The form id
	 * @throws CtdbException
	 *             Thrown if any errors occur while processing
	 */
	private void createSectionQuestionArchive(int formId) throws CtdbException {
		PreparedStatement stmt = null;
		
		try {
			String sql = "insert into sectionquestionarchive select sq.questionid, sq.questionversion, sq.questionorder, sq.questionorder_col, sq.sectionid, " +
				"form.formid, form.version as formversion, sq.suppressflag, sq.questionattributesid from sectionquestion sq, form, section " +
				"where form.formid = ? and form.formid = section.formid and section.sectionid = sq.sectionid ";
			
			stmt = this.conn.prepareStatement(sql);
			stmt.setLong(1, formId);
			stmt.executeUpdate();
		}
		catch (SQLException e) {
			throw new CtdbException("Unable to create section question archive for the form. " + e.getMessage(), e);
		}
		finally {
			this.close(stmt);
		}
	}

	public void deleteRow(int formId, int rowId) throws CtdbException {
		PreparedStatement stmt = null;
		
		try {
			String sql = "delete from formlayout where formid = ? and formrow = ? ";
			stmt = this.conn.prepareStatement(sql);
			stmt.setLong(1, formId);
			stmt.setInt(2, rowId);
			stmt.executeUpdate();

			stmt.close();
			sql = "update formlayout set formrow = formrow - 1 where formid = ? and formrow > ? ";
			stmt = conn.prepareStatement(sql);
			stmt.setLong(1, formId);
			stmt.setInt(2, rowId);
			stmt.executeUpdate();

			stmt.close();
			sql = "update section set formrow = formrow - 1 where formid = ? and formrow > ? ";
			stmt = conn.prepareStatement(sql);
			stmt.setLong(1, formId);
			stmt.setInt(2, rowId);
			stmt.executeUpdate();
		}
		catch (SQLException e) {
			throw new CtdbException("Unable to delete the formrow (id = " + rowId + ") from the form, with id " + formId + 
				" : " + e.getMessage(), e);
		}
		finally {
			this.close(stmt);
		}
	}

	public void swapAllSections(int row1, int row2, int formid) throws CtdbException {
		PreparedStatement stmt = null;
		ResultSet rs = null;

		try {
			String sql = "select sectionid from section where formid = ? and formrow = ? ";
			stmt = this.conn.prepareStatement(sql);
			stmt.setLong(1, formid);
			stmt.setInt(2, row2);
			rs = stmt.executeQuery();
			
			StringBuffer row1Sections = new StringBuffer("(");
			boolean noSections = true;
			
			while (rs.next()) {
				noSections = false;
				row1Sections.append(rs.getString("sectionid")).append(", ");
			}
			
			row1Sections.deleteCharAt(row1Sections.length() - 1);
			row1Sections.append(")");

			rs.close();
			stmt.close();
			
			sql = "update section set formrow = ? where formrow = ? and formid = ? ";
			stmt = this.conn.prepareStatement(sql);
			stmt.setInt(1, row2);
			stmt.setInt(2, row1);
			stmt.setLong(3, formid);
			stmt.executeUpdate();

			if (!noSections) {
				stmt.close();
				sql = "update section set formrow =? where sectionid in " + row1Sections.toString();
				stmt = this.conn.prepareStatement(sql);
				stmt.setInt(1, row1);
				stmt.executeUpdate();
			}
		}
		catch (SQLException e) {
			throw new CtdbException("Unable to swap all sections " + e.getMessage(), e);
		}
		finally {
			this.close(stmt);
			this.close(rs);
		}
	}

	public void swapRows(int row1, int row2, int formId) throws CtdbException {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		
		try {
			String sql = "select numcols from formlayout where formid = ? and formrow = ? ";
			stmt = conn.prepareStatement(sql);
			stmt.setLong(1, formId);
			stmt.setInt(2, row1);
			rs = stmt.executeQuery();
			rs.next();
			String row1Cols = rs.getString(1);
			
			rs.close();
			stmt.close();
			
			sql = "select numcols from formlayout where formid = ? and formrow = ? ";
			stmt = conn.prepareStatement(sql);
			stmt.setLong(1, formId);
			stmt.setInt(2, row2);
			rs = stmt.executeQuery();
			rs.next();
			String row2Cols = rs.getString(1);
			
			rs.close();
			stmt.close();
			
			sql = "update formlayout set numcols = ? where formid = ? and formrow = ? ";
			stmt = conn.prepareStatement(sql);
			stmt.setString(1, row1Cols);
			stmt.setLong(2, formId);
			stmt.setInt(3, row2);
			stmt.executeUpdate();

			stmt.setString(1, row2Cols);
			stmt.setLong(2, formId);
			stmt.setInt(3, row1);
			stmt.executeUpdate();
		}
		catch (SQLException e) {
			throw new CtdbException("Unable to swap all rows " + e.getMessage(), e);
		}
		finally {
			this.close(stmt);
			this.close(rs);
		}
	}

	public void deleteCellFromLayout(int formId, int row, int col) throws CtdbException {
		PreparedStatement stmt = null;
		ResultSet rs = null;

		try {
			String sql = "update formlayout set numcols = numcols - 1 where formid = ? and formrow = ? ";
			stmt = conn.prepareStatement(sql);
			stmt.setLong(1, formId);
			stmt.setInt(2, row);
			stmt.executeUpdate();
		}
		catch (SQLException e) {
			throw new CtdbException("Unable to remove cell from row. " + e.getMessage(), e);
		}
		finally {
			this.close(stmt);
			this.close(rs);
		}
	}

	public void updateSectionsAfterDeleteCell(int formId, int row, int col) throws CtdbException {
		PreparedStatement stmt = null;

		try {
			String sql = "update section set formcol = formcol - 1 where formid = ? and formrow = ? and formcol > ? ";
			
			stmt = conn.prepareStatement(sql);
			stmt.setLong(1, formId);
			stmt.setInt(2, row);
			stmt.setInt(3, col);
			stmt.executeUpdate();
		}
		catch (SQLException e) {
			throw new CtdbException("Unable to remove cell from row. " + e.getMessage(), e);
		}
		finally {
			this.close(stmt);
		}

	}

	/**
	 * Deletes a section question based on a section ID and a question ID
	 * 
	 * @param sectionId
	 *            The section ID
	 * @param questionId
	 *            The question ID to delete
	 * @throws CtdbException
	 *             Thrown if any errors occur while processing
	 */
	public void deleteSectionQuestion(int sectionId, int questionId) throws CtdbException {
		PreparedStatement stmt = null;

		try {
			String sql = "delete from sectionquestion where sectionid = ? and questionid = ? ";

			stmt = this.conn.prepareStatement(sql);
			stmt.setLong(1, sectionId);
			stmt.setLong(2, questionId);

			stmt.executeUpdate();
		}
		catch (SQLException e) {
			throw new CtdbException("Unable to delete the question(id = " + questionId + ") from the section with id " + sectionId + 
				" : " + e.getMessage(), e);
		}
		finally {
			this.close(stmt);
		}
	}

	/**
	 * Transforms a ResulSet object into a Form object
	 * 
	 * @param rs
	 *            ResultSet to transform to Form object
	 * @return Form data object
	 * @throws SQLException
	 *             thrown if any errors occur while retrieving data from result
	 *             set
	 */
	private Form rsToForm(ResultSet rs) throws SQLException {
		Form form = new Form();
		form.setId(rs.getInt("formid"));
		form.setName(rs.getString("name"));
		form.setDescription(rs.getString("description"));
		CtdbLookup status = new CtdbLookup(rs.getInt("xstatusid"),
				rs.getString("shortname"), null);
		form.setStatus(status);
		form.setCreatedBy(rs.getInt("createdby"));
		form.setCreatedDate(rs.getDate("createddate"));
		form.setUpdatedBy(rs.getInt("updatedby"));
		form.setUpdatedDate(rs.getTimestamp("updateddate"));
		int ver = rs.getInt("version");
		Version version = new Version(ver);
		form.setVersion(version);
		if (rs.getBoolean("lockFlag")) {
			form.setLockFlag(true);
		} else {
			form.setLockFlag(false);
		}

		form.setCheckOutBy(rs.getInt("checkedoutby"));
		form.setCheckOutDate(rs.getDate("checkedoutdate"));
		form.setProtocolId(rs.getInt("protocolid"));
		form.setOrderValue(rs.getInt("orderval"));
		form.setImportedDate(rs.getDate("importeddate"));
		form.setImportFileName(rs.getString("importfilename"));
		form.setSingleDoubleKeyFlag(rs.getInt("dataentryflag"));
		form.setCopyRight(rs.getBoolean("copyright"));
		form.setAllowMultipleCollectionInstances(rs.getBoolean("allow_multiple_collection_instances"));
		
		if (rs.getBoolean("publicFlag")) {
			form.setAccessFlag(2);
		}
		else {
			form.setAccessFlag(1);
		}
		

		form.setDataStructureName(rs.getString("data_structure_name"));

		FormHtmlAttributes formHtmlAttributes = new FormHtmlAttributes();
		if (rs.getBoolean("formborder")) {
			formHtmlAttributes.setFormBorder(true);
		} else {
			formHtmlAttributes.setFormBorder(false);
		}
		if (rs.getBoolean("sectionborder")) {
			formHtmlAttributes.setSectionBorder(true);
		} else {
			formHtmlAttributes.setSectionBorder(false);
		}

		formHtmlAttributes.setFormFont(rs.getString("formnamefont"));
		formHtmlAttributes.setFormColor(rs.getString("formnamecolor"));
		formHtmlAttributes.setSectionFont(rs.getString("sectionnamefont"));
		formHtmlAttributes.setSectionColor(rs.getString("sectionnamecolor"));
		formHtmlAttributes.setFormFontSize(rs.getInt("fontsize"));
		formHtmlAttributes.setCellpadding(rs.getInt("cellpadding"));

		form.setFormHtmlAttributes(formHtmlAttributes);

		form.setFormHeader(rs.getString("header"));
		form.setFormFooter(rs.getString("footer"));

		form.setDataEntryWorkflow(DataEntryWorkflowType.getByValue(rs
				.getInt("dataentryworkflowtype")));
		form.setAttachFiles(Boolean.parseBoolean(rs.getString("attachfiles")));
		form.setDataSpring(Boolean.parseBoolean(rs
				.getString("enabledataspring")));
		form.setTabDisplay(Boolean.parseBoolean(rs.getString("tabdisplay")));
		form.setFormType(rs.getInt("formtypeid"));
		
		return form;
	}

	/**
	 * Transforms a ResultSet object into a FormQuestionAttributes object
	 * 
	 * @param rs
	 *            ResultSet to transform to Question Attributes object
	 * @return Question Object
	 * @throws SQLException
	 *             Thrown if any errors occur while retrieving data from result
	 *             set
	 */
	private FormQuestionAttributes rsToQuestionAttributes(ResultSet rs)
			throws SQLException {

		FormQuestionAttributes questionAttributes = new FormQuestionAttributes();

		// determine what type of question transforming rs into
		boolean calQuestion = false;
		boolean calculatedFlag = rs.getBoolean("calculatedflag");

		if (calculatedFlag == true) {

			calQuestion = true;
			questionAttributes = new CalculatedFormQuestionAttributes();
			((CalculatedFormQuestionAttributes) questionAttributes)
					.setCalculation(rs.getString("calculation"));

			((CalculatedFormQuestionAttributes) questionAttributes)
					.setConversionFactor(ConversionFactor.getByValue(rs
							.getInt("dtconversionfactor")));

		}

		if (rs.getString("data_element_name") == null) {
			questionAttributes.setDataElementName("none");
		} else {
			questionAttributes.setDataElementName(rs.getString("group_name") + "." + rs.getString("data_element_name"));
		}
		
		//prepopulation
		questionAttributes.setPrepopulation(rs.getBoolean("prepopulation"));
		
		if(rs.getBoolean("prepopulation")) {
			questionAttributes.setPrepopulationValue(rs.getString("prepopulationvalue"));
		}
		else {
			questionAttributes.setPrepopulationValue("");
		}
		
		//decimalprecision
		questionAttributes.setDecimalPrecision(rs.getInt("decimalprecision"));
		
		//unit conversion factor
		questionAttributes.setHasUnitConversionFactor(rs.getBoolean("hasconversionfactor"));
		questionAttributes.setUnitConversionFactor(rs.getString("conversionfactor"));
		
		questionAttributes.setIsCalculatedQuestion(calQuestion);
		questionAttributes.setId(rs.getInt("questionattributesid"));
		questionAttributes.setQuestionId(rs.getInt("questionid"));
		questionAttributes.setVersion(new Version(rs.getInt("questionversion")));
		questionAttributes.setRequired(rs.getBoolean("requiredflag"));
		
		// Skip rule
		boolean skipRule = rs.getBoolean("skipruleflag");
		
		if (skipRule) {
			questionAttributes.setSkipRuleType(SkipRuleType.getByValue(rs.getInt("skipruletype")));
			questionAttributes.setSkipRuleOperatorType(SkipRuleOperatorType.getByValue(rs.getInt("skipruleoperatortype")));
			questionAttributes.setSkipRuleEquals(rs.getString("skipruleequals"));
		}
		
		questionAttributes.setHasSkipRule(skipRule);
		
		// HTML Attributes
		HtmlAttributes attributes = new HtmlAttributes();
		attributes.setAlign(rs.getString("halign"));
		attributes.setvAlign(rs.getString("valign"));
		attributes.setColor(rs.getString("textcolor"));
		attributes.setFontFace(rs.getString("fontface"));
		attributes.setFontSize(rs.getString("fontsize"));
		attributes.setIndent(rs.getInt("indent"));
		questionAttributes.setHtmlAttributes(attributes);
		
		questionAttributes.setCreatedBy(rs.getInt("createdby"));
		questionAttributes.setCreatedDate(new Date(rs.getTimestamp("createddate").getTime()));
		questionAttributes.setUpdatedBy(rs.getInt("updatedby"));
		questionAttributes.setUpdatedDate(new Date(rs.getTimestamp("updateddate").getTime()));
		questionAttributes.setRangeOperator(rs.getString("rangeoperator"));
		questionAttributes.setRangeValue1(rs.getString("rangevalue1"));
		questionAttributes.setRangeValue2(rs.getString("rangevalue2"));
		questionAttributes.setLabel(rs.getString("label"));
		questionAttributes.setAnswerType(AnswerType.getByValue(rs.getInt("answertype")));
		questionAttributes.setMinCharacters(rs.getInt("mincharacters"));
		questionAttributes.setMaxCharacters(rs.getInt("maxcharacters"));
		questionAttributes.setHorizontalDisplay(rs.getBoolean("horizontaldisplay"));
		questionAttributes.setHorizDisplayBreak(rs.getBoolean("horizDisplayBreak"));
		questionAttributes.setTextareaWidth(rs.getInt("textareawidth"));
		questionAttributes.setTextareaHeight(rs.getInt("textareaheight"));
		questionAttributes.setTextboxLength(rs.getInt("textboxlength"));
		
		if (rs.getString("emailTriggerid") != null && rs.getInt("emailTriggerid") != Integer.MIN_VALUE) {
			EmailTrigger et = new EmailTrigger();
			et.setVersion(new Version(rs.getInt("version")));
			et.setToEmailAddress(rs.getString("toemailaddress"));
			et.setCcEmailAddress(rs.getString("ccemailaddress"));
			et.setId(rs.getInt("emailtriggerid"));
			et.setSubject(rs.getString("subject"));
			et.setBody(rs.getString("body"));
			et.setCreatedBy(rs.getInt("createdby"));
			et.setCreatedDate(rs.getDate("createddate"));
			et.setUpdatedBy(rs.getInt("updatedby"));
			et.setUpdatedDate(rs.getDate("updateddate"));
			questionAttributes.setEmailTrigger(et);
			questionAttributes.getEmailTrigger().setId(rs.getInt("emailtriggerid"));
		}

		questionAttributes.setDataSpring(Boolean.parseBoolean(rs.getString("dataspring")));
		questionAttributes.setHtmlText(rs.getString("xhtmltext"));
		
		questionAttributes.setTableHeaderType(rs.getInt("tableheadertype"));
		questionAttributes.setShowText(rs.getBoolean("showText"));
		

		return questionAttributes;
	}

	/**
	 * Transforms a ResulSet object into a Section object
	 * 
	 * @param rs
	 *            ResultSet to transform to Section object
	 * @return Section data object
	 * @throws SQLException
	 *             thrown if any errors occur while retrieving data from result
	 *             set
	 */
	private Section rsToSection(ResultSet rs) throws SQLException {
		Section section = new Section();
		section.setId(rs.getInt("sectionid"));
		section.setFormId(rs.getInt("formid"));
		section.setName(rs.getString("name"));
		section.setDescription(rs.getString("description"));
		section.setCreatedBy(rs.getInt("createdby"));
		section.setCreatedDate(rs.getDate("createddate"));
		section.setUpdatedBy(rs.getInt("updatedby"));
		section.setUpdatedDate(rs.getDate("updateddate"));
		section.setOrderValue(rs.getInt("orderval"));
		section.setRow(rs.getInt("formrow"));
		section.setCol(rs.getInt("formcol"));
		section.setTextDisplayed(rs.getBoolean("suppressflag"));
		section.setInstructionalText(rs.getString("label"));
		section.setIntob(rs.getBoolean("intob"));
		section.setCollapsable(rs.getBoolean("collapsable"));
		section.setResponseImage(rs.getBoolean("isresponseimage"));
		section.setRepeatable(rs.getBoolean("isrepeatable"));
		section.setInitRepeatedSections(rs.getInt("initialrepeatedsections"));
		section.setMaxRepeatedSections(rs.getInt("maxrepeatedsections"));
		section.setRepeatedSectionParent(rs.getInt("repeatedsectionparent"));
		section.setGridtype(rs.getBoolean("gridtype")); 
		section.setTableGroupId(rs.getInt("tablegroupid")); 
		section.setTableHeaderType(rs.getInt("tableheadertype"));
		
		if (rs.getString("group_name") == null) {
			section.setRepeatableGroupName("None");
		}
		else {
			section.setRepeatableGroupName(rs.getString("group_name"));
		}

		section.setAltLabel(rs.getString("altlabel"));
		
		return section;
	}

	/**
	 * Transforms a ResulSet object into an Interval object
	 * 
	 * @param rs
	 *            ResultSet to transform to Interval object
	 * @return Interval data object
	 * @throws SQLException
	 *             thrown if any errors occur while retrieving data from result
	 *             set
	 */
	private Interval rsToInterval(ResultSet rs) throws SQLException {
		Interval interval = new Interval();
		interval.setId(rs.getInt("intervalid"));
		interval.setProtocolId(rs.getInt("protocolid"));
		interval.setName(rs.getString("name"));
		interval.setDescription(rs.getString("description"));
		interval.setVersion(new Version(rs.getInt("version")));
		interval.setCreatedBy(rs.getInt("createdby"));
		interval.setCreatedDate(rs.getDate("createddate"));
		interval.setUpdatedBy(rs.getInt("updatedby"));
		interval.setUpdatedDate(rs.getDate("updateddate"));
		interval.setSelfReportStart(rs.getInt("selfreportstart"));
		interval.setSelfReportEnd(rs.getInt("selfreportend"));
		
		return interval;
	}

	public void updateFormQuestionCalcAttributes(AdministeredForm admForm) throws CtdbException {
		PreparedStatement stmt = null;
		ResultSet rs = null;

		try {
			StringBuffer sql = new StringBuffer(200);
			sql.append("select q.QUESTIONID, q.CALCULATEDFLAG, q.CALCULATION, q.ANSWERTYPE, s.SECTIONID, q.DTCONVERSIONFACTOR ");
			sql.append(" from section s, administeredform a, questionattributes q, sectionquestion sq ");
			sql.append(" where ");
			sql.append(" a.ADMINISTEREDFORMID = ?");
			sql.append(" and a.FORMID = s.FORMID ");
			sql.append(" and sq.SECTIONID = s.SECTIONID ");
			sql.append(" and sq.QUESTIONATTRIBUTESID = q.QUESTIONATTRIBUTESID ");
			sql.append(" and q.CALCULATEDFLAG = 1 ");

			stmt = this.conn.prepareStatement(sql.toString());
			stmt.setLong(1, admForm.getId());

			rs = stmt.executeQuery();

			HashMap<Integer, HashMap<Integer, List<Object>>> sections = new HashMap<Integer, HashMap<Integer, List<Object>>>();
			
			while (rs.next()) {
				int sectionid = rs.getInt("SECTIONID");
				int qid = rs.getInt("QUESTIONID");
				String calculation = rs.getString("CALCULATION");
				int conversionfactor = rs.getInt("DTCONVERSIONFACTOR");
				ConversionFactor cf = ConversionFactor.getByValue(conversionfactor);
				int answertype = rs.getInt("ANSWERTYPE");
				AnswerType aType = AnswerType.getByValue(answertype);
				ArrayList<Object> al = new ArrayList<Object>();
				
				al.add(calculation);
				al.add(cf);
				al.add(aType);
				
				Integer sid = new Integer(sectionid);
				
				if (!sections.containsKey(sid)) {
					sections.put(sid, new HashMap<Integer, List<Object>>());
				}
				
				sections.get(sid).put(new Integer(qid), al);
			}
			
			for ( List<Section> row : admForm.getForm().getRowList() ) {
				if ( sections.isEmpty() ) {
					break;
				}
				
				for ( Section s : row ) {
					if (s == null) {
						continue;
					}

					Integer secid = new Integer(s.getId());
					
					if (sections.containsKey(secid)) {
						HashMap<Integer, List<Object>> questions = sections.get(secid);
						
						for ( Question q : s.getQuestionList() ) {
							if ( questions.isEmpty() ) {
								break;
							}
							
							Integer qid = new Integer(q.getId());
							
							if (questions.containsKey(qid)) {
								List<Object> al = questions.get(qid);
								
								if (q.getFormQuestionAttributes() == null) {
									q.setFormQuestionAttributes(new FormQuestionAttributes());
								}
								
								if (q.getCalculatedFormQuestionAttributes() == null) {
									q.setCalculatedFormQuestionAttributes(new CalculatedFormQuestionAttributes());
								}
								
								q.getFormQuestionAttributes().setIsCalculatedQuestion(true);
								q.getCalculatedFormQuestionAttributes().setCalculation((String) al.get(0));
								q.getCalculatedFormQuestionAttributes().setConversionFactor((ConversionFactor) al.get(1));
								q.getFormQuestionAttributes().setAnswerType((AnswerType) al.get(2));
								q.getCalculatedFormQuestionAttributes().setAnswerType((AnswerType) al.get(2));

								QuestionManager qm = new QuestionManager();
								q.getCalculatedFormQuestionAttributes().setQuestionsToCalculate(qm.getCalculateQuestions(
										q.getCalculatedFormQuestionAttributes(), secid.intValue()));

								questions.remove(qid);
							}
						}
						sections.remove(secid);
					}
				}
			}
		}
		catch (SQLException e) {
			throw new CtdbException("Unable to create form question attributes for Save As the form: " + e.getMessage(), e);
		}
		finally {
			this.close(rs);
			this.close(stmt);
		}
	}

	public void updateAdministeredForms(Patient patient, List<AdministeredForm> activeAdminForms) throws CtdbException {
		PreparedStatement stmt = null;

		try {
			StringBuffer sql = new StringBuffer(50);

			sql.append("update administeredform set patientversion = ? ");
			sql.append("where administeredformid in (");
			
			for (Iterator<AdministeredForm> i = activeAdminForms.iterator(); i.hasNext();) {
				AdministeredForm af = i.next();
				sql.append(af.getId());
				
				if (i.hasNext()) {
					sql.append(",");
				}
			}
			
			sql.append(") ");
			sql.append("and patientid = ?");

			stmt = this.conn.prepareStatement(sql.toString());
			stmt.setInt(1, patient.getVersion().getVersionNumber());
			stmt.setLong(2, patient.getId());

			stmt.executeUpdate();
		}
		catch (SQLException e) {
			throw new CtdbException("Unable to update admin forms for patient " + patient.getId(), e);
		}
		finally {
			this.close(stmt);
		}
	}

	public void createFormGroup(FormGroup fg, int[] formIds) throws CtdbException {
		PreparedStatement stmt = null;

		try {
			String sql = "insert into formgroup (formgroupid, protocolid, formgroupname, formgroupdescription, formgrouporder) " +
				"values (DEFAULT, ?, ?, ?, ?) ";
			
			stmt = this.conn.prepareStatement(sql);
			stmt.setInt(1, fg.getProtocolId());
			stmt.setString(2, fg.getName());
			stmt.setString(3, fg.getDescription());
			stmt.setInt(4, getFormGroupNext(fg.getProtocolId()));
			stmt.executeUpdate();
			int id = getInsertId(conn, "formgroup_seq");
			
			if (formIds != null && formIds.length > 0) {
				associateFormsToGroup(formIds, id);
			}
		}
		catch (SQLException e) {
			throw new CtdbException("Unable to create form group. " + e.getMessage(), e);
		}
		finally {
			this.close(stmt);
		}
	}

	/**
	 * Delete the unadministered form. Database cascade delete will be performed
	 * on other tables, such as FORMLAYOUT, SECTION, and SECTIONQUESTION.
	 * 
	 * @param formGroupId
	 *            form group to be deleted
	 * @throws CtdbException
	 *             thrown if any errors occur when processing
	 */
	public void deleteFormGroup(int formGroupId) throws CtdbException {
		PreparedStatement stmt = null;
		StringBuffer sql = new StringBuffer(25);
		
		try {
			sql.append("delete from formgroupform where formgroupid = ?");
			stmt = this.conn.prepareStatement(sql.toString());
			stmt.setLong(1, formGroupId);
			stmt.executeUpdate();
			stmt.close();

			sql = new StringBuffer(25);
			sql.append("delete from formgroup where formgroupid = ?");
			stmt = this.conn.prepareStatement(sql.toString());
			stmt.setLong(1, formGroupId);
			stmt.executeUpdate();
		}
		catch (SQLException e) {
			throw new CtdbException("Unable to delete the form group: " + e.getMessage(), e);
		}
		finally {
			this.close(stmt);
		}
	}

	private int getFormGroupNext(int protocolid) throws CtdbException {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			StringBuffer sql = new StringBuffer(
					" select max(formgrouporder) from formgroup where protocolid = ? ");
			stmt = this.conn.prepareStatement(sql.toString());
			stmt.setLong(1, protocolid);

			rs = stmt.executeQuery();
			rs.next();

			if (rs.getString(1) == null) {
				return 1;
			} else {
				return rs.getInt(1) + 1;
			}
		} catch (SQLException e) {
			throw new CtdbException("Unable to get next form group order"
					+ e.getMessage(), e);
		} finally {
			this.close(stmt);
		}

	}

	public void associateFormsToGroup(List<Form> forms, FormGroup fg) throws CtdbException {
		PreparedStatement stmt = null;

		try {
			String sql = "insert into formgroupform (formid, formgroupid) values (?, ?)";
			stmt = this.conn.prepareStatement(sql.toString());

			for ( Form f : forms ) {
				stmt.setLong(1, f.getId());
				stmt.setLong(2, fg.getId());
				stmt.executeUpdate();
			}

		}
		catch (SQLException e) {
			throw new CtdbException("Unable to create form group" + e.getMessage(), e);
		}
		finally {
			this.close(stmt);
		}
	}

	public void associateFormToGroups(int formId, int[] formGroupIds)
			throws CtdbException {

		PreparedStatement stmt = null;

		try {
			StringBuffer sql = new StringBuffer(
					"insert into formgroupform (formid, formgroupid) values (?, ?)");

			for (int i = 0; i < formGroupIds.length; i++) {
				stmt = this.conn.prepareStatement(sql.toString());
				stmt.setLong(1, formId);
				stmt.setLong(2, formGroupIds[i]);

				stmt.executeUpdate();
				stmt.close();
				stmt = null;
			}

		} catch (SQLException e) {
			throw new CtdbException("Unable to associate form to groups"
					+ e.getMessage(), e);
		} finally {
			this.close(stmt);
		}
	}

	public void disassociateFormsFromGroup(List<Form> forms, FormGroup fg) throws CtdbException {
		PreparedStatement stmt = null;

		try {
			String sql = "delete from formgroupform where formid = ? and formgroup = ? ";
			stmt = this.conn.prepareStatement(sql);

			for (Form f : forms) {
				stmt.setLong(1, f.getId());
				stmt.setLong(2, fg.getId());
				stmt.addBatch();
			}
			
			stmt.executeBatch();
		}
		catch (SQLException e) {
			throw new CtdbException("Unable to create form group" + e.getMessage(), e);
		}
		finally {
			this.close(stmt);
		}
	}

	/**
	 * This method associates forms to a group given a collection of form ids
	 * and the form group
	 * 
	 * @param formIds
	 * @param fg
	 * @throws CtdbException
	 */
	public void associateFormsToGroup(int[] formIds, int fgId)
			throws CtdbException {

		PreparedStatement stmt = null;

		try {
			String sql = "insert into formgroupform (formid, formgroupid) values (?, ?) ";
			stmt = this.conn.prepareStatement(sql);

			for (int i = 0; i < formIds.length; i++) {
				stmt.setLong(1, formIds[i]);
				stmt.setLong(2, fgId);
				stmt.addBatch();
			}
			
			stmt.executeBatch();
		}
		catch (SQLException e) {
			throw new CtdbException("Unable to create form group.", e);
		}
		finally {
			this.close(stmt);
		}
	}

	/**
	 * This method disassociates forms from a group given a collection of form
	 * ids and the form group
	 * 
	 * @param formIds
	 * @param fg
	 * @throws CtdbException
	 */
	public void disassociateFormsFromGroup(int[] formIds, FormGroup fg)
			throws CtdbException {

		PreparedStatement stmt = null;

		try {
			StringBuffer sql = new StringBuffer(
					"delete from formgroupform where formid = ? and formgroupid = ?");

			for (int i = 0; i < formIds.length; i++) {
				stmt = this.conn.prepareStatement(sql.toString());
				stmt.setLong(1, formIds[i]);
				stmt.setLong(2, fg.getId());
				stmt.executeUpdate();
				stmt.close();
				stmt = null;
			}

		} catch (SQLException e) {
			throw new CtdbException("Unable to create form group"
					+ e.getMessage(), e);
		} finally {
			this.close(stmt);
		}
	}

	public void disassociateFormsFromGroup(List<Form> forms, int formGroupId) throws CtdbException {
		FormGroup fg = new FormGroup();
		fg.setId(formGroupId);
		this.disassociateFormsFromGroup(forms, fg);
	}

	public FormGroup getFormGroup(int formGroupId) throws CtdbException {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		FormGroup fg = new FormGroup();

		try {
			String sql = "select * from formgroup where formgroupid = ? ";

			stmt = this.conn.prepareStatement(sql);
			stmt.setLong(1, formGroupId);

			rs = stmt.executeQuery();
			if (rs.next()) {
				int id = rs.getInt("formgroupid");
				
				fg.setId(id);
				fg.setName(rs.getString("formgroupname"));
				fg.setDescription(rs.getString("formgroupdescription"));
				fg.setProtocolId(rs.getInt("protocolId"));
				fg.setAssociatedForms(Utils.convertListToString(getAssociatedFormNamesForFormGroup(id)));
			}
			else {
				throw new CtdbException("Unable to get form group, no record found.");
			}

		}
		catch (SQLException e) {
			throw new CtdbException("Unable to get form group.", e);
		}
		finally {
			this.close(rs);
			this.close(stmt);
		}
		
		return fg;
	}

	public void updateFormGroup(FormGroup fg, int[] formIds)
			throws CtdbException {

		PreparedStatement stmt = null;
		try {
			StringBuffer sql = new StringBuffer(
					"update formgroup set formgroupname = ?, formgroupdescription = ?  where formgroupid = ?");

			stmt = this.conn.prepareStatement(sql.toString());
			stmt.setString(1, fg.getName());
			stmt.setString(2, fg.getDescription());
			stmt.setLong(3, fg.getId());
			stmt.executeUpdate();

			// need to determine what associated forms to update
			List<Integer> assoc = new ArrayList<Integer>();
			List<Integer> disassoc = new ArrayList<Integer>();

			// loop through current attached forms first to determine if there
			// are any forms we need to disassociate
			int[] currentFormIds = getAssociatedFormIdsForFormGroup(fg.getId());
			if (currentFormIds != null && currentFormIds.length > 0) {
				for (int i = 0; i < currentFormIds.length; i++) {
					int currFormId = currentFormIds[i];
					// now loop through what user has checked off
					boolean match = false;
					if (formIds != null && formIds.length > 0) {
						for (int k = 0; k < formIds.length; k++) {
							int formId = formIds[k];
							if (currFormId == formId) {
								match = true;
								break;
							}
						}

					}
					if (match == false) {
						// need to put this in the dissassoc list
						Integer dInt = new Integer(currFormId);
						disassoc.add(dInt);
					}
				}
			}

			// this next loop is to determine which ones to associate...becasue
			// we dont need to associate ones that are already associates
			if (formIds != null && formIds.length > 0) {
				for (int i = 0; i < formIds.length; i++) {
					int formId = formIds[i];
					// now loop to see through the current forms to see if its
					// already there
					boolean match = false;
					if (currentFormIds != null && currentFormIds.length > 0) {
						for (int k = 0; k < currentFormIds.length; k++) {
							int currFormId = currentFormIds[k];
							if (formId == currFormId) {
								match = true;
								break;
							}
						}

					}
					if (match == false) {
						// need to put in the assoc list
						Integer aInt = new Integer(formId);
						assoc.add(aInt);
					}
				}
			}

			// now its time to disassociate and associate forms
			if (disassoc.size() > 0) {
				Object[] dIntArr = disassoc.toArray();
				int[] disassocArr = new int[dIntArr.length];
				for (int i = 0; i < dIntArr.length; i++) {
					Integer dInt = (Integer) dIntArr[i];
					disassocArr[i] = dInt.intValue();
				}
				disassociateFormsFromGroup(disassocArr, fg);
			}

			if (assoc.size() > 0) {
				Integer[] aIntArr = assoc.toArray(new Integer[0]);
				int[] assocArr = new int[aIntArr.length];
				
				for ( int i = 0; i < aIntArr.length; i++ ) {
					Integer aInt = aIntArr[i];
					assocArr[i] = aInt.intValue();
				}
				
				associateFormsToGroup(assocArr, fg.getId());
			}
		}
		catch (SQLException e) {
			throw new CtdbException("Unable to get form group" + e.getMessage(), e);
		}
		finally {
			this.close(stmt);
		}
	}

	public int[] getAssociatedFormIdsForFormGroup(int id) throws CtdbException {
		List<Integer> formIds = new ArrayList<Integer>();
		PreparedStatement stmt = null;
		ResultSet rs = null;
		int[] formids = null;
		
		try {
			String sql = "select * from formgroupform where formgroupid = ? ";

			stmt = this.conn.prepareStatement(sql);
			stmt.setLong(1, id);
			rs = stmt.executeQuery();

			while ( rs.next() ) {
				formIds.add(rs.getInt("formid"));
			}
			
			// Convert the array list to int array.
			formids = ArrayUtils.toPrimitive(formIds.toArray(new Integer[0]));
		}
		catch (SQLException e) {
			throw new CtdbException("Unable to get form group", e);
		}
		finally {
			this.close(rs);
			this.close(stmt);
		}
		
		return formids;
	}

	public List<String> getAssociatedFormNamesForFormGroup(int id) throws CtdbException {

		ArrayList<String> associatedFormNames = new ArrayList<String>();
		PreparedStatement stmt = null;
		ResultSet rs = null;
		
		try {
			String sql = "select name from form f, formgroupform fgf where f.formid = fgf.formid and fgf.formgroupid = ? ";
			stmt = this.conn.prepareStatement(sql);
			stmt.setLong(1, id);
			rs = stmt.executeQuery();
			
			while ( rs.next() ) {
				String name = rs.getString("name");
				associatedFormNames.add(name);
			}
		}
		catch (SQLException e) {
			throw new CtdbException("Unable to get form group" + e.getMessage(), e);
		}
		finally {
			this.close(stmt);
			this.close(rs);
		}
		
		return associatedFormNames;
	}

	public String getStatusName(int id) throws CtdbException {
		String status = "";
		PreparedStatement stmt = null;
		ResultSet rs = null;

		try {
			String sql = "select shortname from xformstatus where xstatusid = ? ";

			stmt = this.conn.prepareStatement(sql);
			stmt.setLong(1, id);
			rs = stmt.executeQuery();

			while (rs.next()) {
				status = rs.getString("shortname");
			}
		}
		catch (SQLException e) {
			throw new CtdbException("Unable to get status" + e.getMessage(), e);
		}
		finally {
			this.close(stmt);
			this.close(rs);
		}

		return status;
	}
	
	public boolean isFormLegacy(int formid) throws CtdbException {
		boolean isLegacy = false;
		PreparedStatement stmt = null;
		ResultSet rs = null;

		try {
			String sql = "select islegacy from form where formid = ? ";

			stmt = this.conn.prepareStatement(sql);
			stmt.setLong(1, formid);
			rs = stmt.executeQuery();

			while (rs.next()) {
				if (rs.getObject("islegacy") == null) {
					isLegacy = true;
				}
				else {
					isLegacy = rs.getBoolean("islegacy");
				}
			}
		}
		catch (SQLException e) {
			throw new CtdbException("Unable to get status" + e.getMessage(), e);
		}
		finally {
			this.close(stmt);
			this.close(rs);
		}

		return isLegacy;
	}

	public List<FormGroup> getFormGroups(int protocolId) throws CtdbException {
		List<FormGroup> groups = new ArrayList<FormGroup>();
		PreparedStatement stmt = null;
		ResultSet rs = null;
		
		try {
			String sql = "select * from formgroup where protocolid = ? order by formgrouporder ";

			stmt = this.conn.prepareStatement(sql);
			stmt.setLong(1, protocolId);
			rs = stmt.executeQuery();
			
			while (rs.next()) {
				FormGroup fg = new FormGroup();
				int id = rs.getInt("formgroupid");
				
				fg.setId(id);
				fg.setDescription(rs.getString("formgroupdescription"));
				fg.setName(rs.getString("formgroupname"));
				fg.setOrderValue(rs.getInt("formgrouporder"));
				fg.setProtocolId(protocolId);
				fg.setAssociatedForms(Utils.convertListToString(getAssociatedFormNamesForFormGroup(id)));
				
				groups.add(fg);
			}
		}
		catch (SQLException e) {
			throw new CtdbException("Unable to get form group" + e.getMessage(), e);
		}
		finally {
			this.close(rs);
			this.close(stmt);
		}
		
		return groups;
	}

	public int[] getFormsGroups(int formId) throws CtdbException {
		int[] groups = null;
		List<Integer> fgIds = new ArrayList<Integer>();
		PreparedStatement stmt = null;
		ResultSet rs = null;
		
		try {
			String sql = "select formgroupid from formgroupform where formid = ? ";
			
			stmt = this.conn.prepareStatement(sql);
			stmt.setLong(1, formId);
			rs = stmt.executeQuery();
			
			while (rs.next()) {
				fgIds.add(rs.getInt("formgroupid"));
			}
			
			// Convert list to primitive array
			groups = ArrayUtils.toPrimitive(fgIds.toArray(new Integer[0]));
		}
		catch (SQLException e) {
			throw new CtdbException("Unable to get groups for form.", e);
		}
		finally {
			this.close(stmt);
			this.close(rs);
		}
		
		return groups;
	}

	public List<FormGroup> getAssociatedFormGroups(int formId) throws CtdbException {
		List<FormGroup> groups = new ArrayList<FormGroup>();
		PreparedStatement stmt = null;
		ResultSet rs = null;
		
		try {
			String sql = "select formgroup.* from formgroup, formgroupform where formgroup.formgroupid = formgroupform.formgroupid and " +
				"formgroupform.formid = ? ";
			
			stmt = this.conn.prepareStatement(sql);
			stmt.setLong(1, formId);
			rs = stmt.executeQuery();
			
			while (rs.next()) {
				FormGroup fg = new FormGroup();
				fg.setId(rs.getInt("formgroupid"));
				fg.setDescription(rs.getString("formgroupdescription"));
				fg.setName(rs.getString("formgroupname"));
				fg.setOrderValue(rs.getInt("formgrouporder"));
				fg.setProtocolId(rs.getInt("protocolid"));
				groups.add(fg);
			}
		}
		catch (SQLException e) {
			throw new CtdbException("Unable to get groups for form" + e.getMessage(), e);
		}
		finally {
			this.close(stmt);
			this.close(rs);
		}
		
		return groups;
	}

	public List<FormGroup> getAvailiableFormGroups(int formId, int protocolId) throws CtdbException {
		List<FormGroup> groups = new ArrayList<FormGroup>();
		PreparedStatement stmt = null;
		ResultSet rs = null;
		
		try {
			String sql = "select formgroup.* from formgroup where formgroupid not in (select formgroupform.formgroupid from " +
				"formgroupform where formid = ?) and formgroup.protocolid = ? ";

			stmt = this.conn.prepareStatement(sql.toString());
			stmt.setLong(1, formId);
			stmt.setLong(2, protocolId);
			rs = stmt.executeQuery();
			
			while (rs.next()) {
				FormGroup fg = new FormGroup();
				fg.setId(rs.getInt("formgroupid"));
				fg.setDescription(rs.getString("formgroupdescription"));
				fg.setName(rs.getString("formgroupname"));
				fg.setOrderValue(rs.getInt("formgrouporder"));
				fg.setProtocolId(rs.getInt("protocolid"));
				groups.add(fg);
			}
		}
		catch (SQLException e) {
			throw new CtdbException("Unable to get groups for form" + e.getMessage(), e);
		}
		finally {
			this.close(stmt);
			this.close(rs);
		}
		
		return groups;
	}

	public void disassociateFormGroups(int formId) throws CtdbException {

		PreparedStatement stmt = null;
		try {
			StringBuffer sql = new StringBuffer(
					"delete from formgroupform where formid = ?");

			stmt = this.conn.prepareStatement(sql.toString());
			stmt.setLong(1, formId);

			stmt.executeUpdate();
		} catch (SQLException e) {
			throw new CtdbException("Unable to delete form gropu associations"
					+ e.getMessage(), e);
		} finally {
			this.close(stmt);
		}
	}

	public void deleteFormIntervalAssociations(int formId) throws CtdbException {

		PreparedStatement stmt = null;
		try {
			StringBuffer sql = new StringBuffer(
					"delete from form_interval where formid = ?");

			stmt = this.conn.prepareStatement(sql.toString());
			stmt.setLong(1, formId);

			stmt.executeUpdate();
		} catch (SQLException e) {
			throw new CtdbException(
					"Unable to delete form interval associations"
							+ e.getMessage(), e);
		} finally {
			this.close(stmt);
		}
	}

	public void deleteFormFormatting(int formId) throws CtdbException {

		PreparedStatement stmt = null;
		try {
			StringBuffer sql = new StringBuffer(
					"delete from formcellformatting where formid = ?");

			stmt = this.conn.prepareStatement(sql.toString());
			stmt.setLong(1, formId);

			stmt.executeUpdate();
		} catch (SQLException e) {
			throw new CtdbException(
					"Unable to delete form formatting associations"
							+ e.getMessage(), e);
		} finally {
			this.close(stmt);
		}
	}

	public void deleteFormFormattingArchive(int formId) throws CtdbException {

		PreparedStatement stmt = null;
		try {
			StringBuffer sql = new StringBuffer(
					"delete from formcellformattingarchive where formid = ?");

			stmt = this.conn.prepareStatement(sql.toString());
			stmt.setLong(1, formId);

			stmt.executeUpdate();
		} catch (SQLException e) {
			throw new CtdbException(
					"Unable to delete form formatting associations"
							+ e.getMessage(), e);
		} finally {
			this.close(stmt);
		}
	}

	public void updatedFormGroupOrder(String[] ids) throws CtdbException {
		PreparedStatement stmt = null;

		try {
			StringBuffer sql = new StringBuffer(
					"update formgroup set formgrouporder=? where formgroupid = ?");

			for (int i = 0; i < ids.length; i++) {
				stmt = this.conn.prepareStatement(sql.toString());
				stmt.setInt(1, i);
				stmt.setLong(2, Long.valueOf(ids[i]));

				stmt.executeUpdate();
				stmt.close();
				stmt = null;
			}

		} catch (SQLException e) {
			throw new CtdbException("Unable to create form group"
					+ e.getMessage(), e);
		} finally {
			this.close(stmt);
		}
	}

	public void createFormIntervalAssociations(int formId, int protocolId)
			throws CtdbException {
		PreparedStatement stmt = null;
		// String mandatoryFlag = "false";

		try {
			StringBuffer sql = new StringBuffer(
					"insert into form_interval select ?, intervalid, 'false'  from interval where protocolid = ?");
			stmt = conn.prepareStatement(sql.toString());
			stmt.setLong(1, formId);
			stmt.setLong(2, protocolId);
			stmt.executeUpdate();

		} catch (SQLException e) {
			throw new CtdbException(
					"Unable to associate new form to all intervals"
							+ e.getMessage(), e);
		} finally {
			this.close(stmt);
		}
	}

	/**
	 * Gets the forms that are currently associated to an interval
	 * 
	 * @param intervalId
	 * @return key = formId, bucket = FormName
	 * @throws CtdbException
	 */
	public HashMap<String, String> getAssociatedFormsForInterval(int intervalId)
			throws CtdbException {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		HashMap<String, String> forms = new HashMap<String, String>();
		try {
			StringBuffer sql = new StringBuffer(
					"select fi.formid, f.name from form f, form_interval fi where fi.formid = f.formid and fi.intervalid = ? ");

			stmt = conn.prepareStatement(sql.toString());
			stmt.setLong(1, intervalId);
			rs = stmt.executeQuery();
			while (rs.next()) {

				forms.put(rs.getString("formId"), rs.getString("name"));
			}
			return forms;

		} catch (SQLException e) {
			throw new CtdbException(
					"Unable to associate new form to all intervals"
							+ e.getMessage(), e);
		} finally {
			this.close(stmt);
			this.close(rs);
		}
	}

	/**
	 * gets forms availiable to be associated ot an interval
	 * 
	 * @param intervalId
	 * @return
	 * @throws CtdbException
	 */

	public HashMap<String, String> getAvailiableFormsForInterval(int intervalId)
			throws CtdbException {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		HashMap<String, String> forms = new HashMap<String, String>();
		try {
			StringBuffer sql = new StringBuffer(
					"select f.formid, f.name from form f, interval i where ");
			sql.append("  f.formid not in (select formid from form_interval where intervalid = ? ) ");
			sql.append(" and f.protocolid = (select protocolid from interval where intervalid = ? ) ");
			sql.append("and f.formtypeid = 10 ");// patient forms

			stmt = conn.prepareStatement(sql.toString());
			stmt.setLong(1, intervalId);
			stmt.setLong(2, intervalId);
			rs = stmt.executeQuery();
			while (rs.next()) {

				forms.put(rs.getString("formid"), rs.getString("name"));
			}
			return forms;

		} catch (SQLException e) {
			throw new CtdbException(
					"Unable to associate new form to all intervals"
							+ e.getMessage(), e);
		} finally {
			this.close(stmt);
			this.close(rs);
		}
	}

	public void associateFormsToInterval(List<String> formIds, int intervalId) throws CtdbException {
		PreparedStatement stmt = null;

		try {
			String sql = "insert into form_interval (formid, intervalid) values (?, ?) ";

			stmt = conn.prepareStatement(sql);
			
			for (String formId : formIds) {
				stmt.setLong(1, Long.valueOf(formId));
				stmt.setLong(2, intervalId);
				stmt.addBatch();
			}
			
			stmt.executeBatch();
		}
		catch (SQLException | NumberFormatException e) {
			throw new CtdbException("Unable to associate forms to interval " + 
					Long.toString(intervalId) + ".", e);
		}
		finally {
			this.close(stmt);
		}
	}

	public void disassociateFormsFromInterval(int intervalId,
			ArrayList<String> formIdsNotToDissassoicate) throws CtdbException,
			InvalidRemovalException {
		PreparedStatement stmt = null;

		try {
			Set<String> currentlyAssociatedForms = getAssociatedFormsForInterval(
					intervalId).keySet();
			currentlyAssociatedForms.removeAll(formIdsNotToDissassoicate);
			formIdsNotToDissassoicate.removeAll(currentlyAssociatedForms);

			StringBuffer sql = new StringBuffer(
					"delete form_interval where formid = ? and intervalid = "
							+ intervalId + " ");
			stmt = conn.prepareStatement(sql.toString());

			for (Iterator<String> i = currentlyAssociatedForms.iterator(); i
					.hasNext();) {

				stmt.setLong(1, Long.valueOf(i.next()));
				stmt.addBatch();
			}

			stmt.executeBatch();

		} catch (SQLException e) {
			if (e.getMessage().indexOf("!>>!") > -1) {
				// this error was thrown by the inprogress removal check
				// trigger.

				InvalidRemovalException ire = new InvalidRemovalException(
						"Removal of In Progress Form from interval association detected "
								+ e.getMessage());
				ire.setOffendingRowIndicator(e.getMessage().substring(
						e.getMessage().indexOf("<<!!>>") + 6,
						e.getMessage().indexOf("<<^^>>")));
				throw ire;
			} else {
				throw new CtdbException(
						"Unable to delete all forms from interval "
								+ e.getMessage(), e);
			}
		} finally {
			this.close(stmt);
		}
	}

	public void saveCellFormatting(CellFormatting cf) throws CtdbException {
		PreparedStatement stmt = null;
		try {
			StringBuffer sql = new StringBuffer(200);
			sql.append("insert into formcellformatting (formid, formrow, formcol, height, width, padding, align, valign, textwrap, bgcolor, rowspan, colspan ) values ");
			sql.append(" (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? ) ");

			stmt = this.conn.prepareStatement(sql.toString());
			stmt.setLong(1, cf.getFormId());
			stmt.setInt(2, cf.getRow());
			stmt.setInt(3, cf.getCol());
			stmt.setInt(4, cf.getHeight());
			stmt.setInt(5, cf.getWidth());
			stmt.setInt(6, cf.getPadding());
			stmt.setString(7, cf.getAlign());
			stmt.setString(8, cf.getValign());
			stmt.setString(9, cf.getWrap());
			stmt.setString(10, cf.getBgColor());
			stmt.setInt(11, cf.getRowSpan());
			stmt.setInt(12, cf.getColSpan());

			stmt.executeUpdate();

		} catch (SQLException sqle) {
			throw new CtdbException(" Failure inserting form cell formatting "
					+ sqle.getMessage(), sqle);
		}

	}

	public List<CellFormatting> getCellFormatting(int formId) throws CtdbException {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		List<CellFormatting> results = new ArrayList<CellFormatting>();
		
		try {
			stmt = conn.prepareStatement("select * from formcellformatting where formid = ? ");
			stmt.setLong(1, formId);

			rs = stmt.executeQuery();
			
			while (rs.next()) {
				results.add(rsToCellFormatting(rs));
			}

		}
		catch (SQLException sqle) {
			throw new CtdbException(" Failure getting form cell formatting " + sqle.getMessage(), sqle);
		}
		finally {
			close(rs);
			close(stmt);
		}
		
		return results;
	}

	public Map<String, CellFormatting> getCellFormattingMap(int formId, int formVersion) throws CtdbException {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		Map<String, CellFormatting> results = new HashMap<String, CellFormatting>();
		
		try {
			stmt = conn
					.prepareStatement("select * from formcellformattingarchive where formid = ? and formversion = ? ");
			stmt.setLong(1, formId);
			stmt.setInt(2, formVersion);

			rs = stmt.executeQuery();
			
			while (rs.next()) {
				CellFormatting cf = rsToCellFormatting(rs);
				results.put(Integer.toString(cf.getRow()) + "-" + Integer.toString(cf.getCol()), cf);
			}
		}
		catch (SQLException sqle) {
			throw new CtdbException(" Failure getting form cell formatting " + sqle.getMessage(), sqle);
		}
		
		return results;
	}
	
	public Map<String, CellFormatting> getCellFormattingMap(int formId) throws CtdbException {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		Map<String, CellFormatting> results = new HashMap<String, CellFormatting>();
		
		try {
			stmt = conn.prepareStatement("select * from formcellformatting where formid = ? ");
			stmt.setLong(1, formId);
			
			rs = stmt.executeQuery();
			while (rs.next()) {
				CellFormatting cf = rsToCellFormatting(rs);
				results.put(Integer.toString(cf.getRow()) + "-" + Integer.toString(cf.getCol()), cf);
			}
		}
		catch (SQLException sqle) {
			throw new CtdbException(" Failure getting form cell formatting " + sqle.getMessage(), sqle);
		}
		
		return results;
	}

	public List<CellFormatting> getCellFormatting(int formId, int row, int col) throws CtdbException {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		List<CellFormatting> results = new ArrayList<CellFormatting>();
		
		try {
			stmt = conn
					.prepareStatement("select * from formcellformatting where formid = ? and formrow = ? and formcol = ? ");
			stmt.setLong(1, formId);
			stmt.setInt(2, row);
			stmt.setInt(3, col);

			rs = stmt.executeQuery();
			while (rs.next()) {
				results.add(rsToCellFormatting(rs));
			}
		}
		catch (SQLException sqle) {
			throw new CtdbException(" Failure getting form cell formatting " + sqle.getMessage(), sqle);
		}
		
		return results;
	}

	private CellFormatting rsToCellFormatting(ResultSet rs) throws SQLException {
		CellFormatting cf = new CellFormatting();
		cf.setFormId(rs.getInt("formid"));
		cf.setRow(rs.getInt("formrow"));
		cf.setCol(rs.getInt("formcol"));
		cf.setHeight(rs.getInt("height"));
		cf.setWidth(rs.getInt("width"));
		cf.setAlign(rs.getString("align"));
		cf.setValign(rs.getString("valign"));
		cf.setBgColor(rs.getString("bgcolor"));
		cf.setPadding(rs.getInt("padding"));
		cf.setWrap(rs.getString("textwrap"));
		cf.setRowSpan(rs.getInt("rowspan"));
		cf.setColSpan(rs.getInt("colspan"));
		return cf;
	}

	/**
	 * Stores form xml to db, called when form is marked as external
	 * 
	 * @param formId
	 * @param xml
	 * @throws CtdbException
	 */
	public void storeXml(int formId, Document xml) throws CtdbException {
		PreparedStatement stmt = null;
		
		try {
			stmt = this.conn.prepareStatement("update form set formxml = ? where formId = ? ");
			stmt.setBytes(1, xml.toString().getBytes());
			stmt.setLong(2, formId);
			stmt.executeUpdate();
		}
		catch (SQLException sqle) {
			throw new CtdbException(" Failure inserting xml for form id : " + formId + " : " + sqle.getMessage(), sqle);
		}
		finally {
			close(stmt);
		}
	}

	/**
	 * Stores the html imported to the db, stores when import is invoked
	 * 
	 * @param formId
	 * @param file
	 *            - the struts formfile object used to capture the imported form
	 * @throws CtdbException
	 */
	public void storeHTML(int formId, File file) throws CtdbException {

		PreparedStatement stmt = null;
		ResultSet res = null;
		try {
			conn.setAutoCommit(false);

			stmt = this.conn
					.prepareStatement("update form set formhtml = ? where formId = ? ");
			stmt.setString(1, file.toString());
			stmt.setInt(2, formId);
			stmt.executeUpdate();

			// stmt =
			// this.conn.prepareStatement("select formhtml from form where formid = ? for update",
			// ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
			// stmt.setInt(1, formId);
			//
			// res = stmt.executeQuery();
			// res.next();
			// Clob cb = res.getClob("formHtml");
			//
			// OutputStream os = cb.setAsciiStream(0);
			//
			// byte [] cbuf = new byte[4096];
			// InputStream ir = file.getInputStream();
			// int bytesRead = ir.read(cbuf);
			// while (bytesRead != -1) {
			// os.write(cbuf, 0, bytesRead);
			// bytesRead = ir.read(cbuf);
			// }
			//
			// os.flush();
			// os.close();

			conn.commit();
		} catch (SQLException sqle) {
			System.err.println("SQL EXCEPTION DURING STORE HTML : "
					+ sqle.getMessage());
			sqle.printStackTrace();
		}
		// catch (FileNotFoundException fnfe) {
		// System.err.println("filenot found exception during store html");
		// } catch (IOException ioe) {
		// System.err.println ("IO exceptoin during store html " +
		// ioe.getMessage());
		// ioe.printStackTrace();
		// }
		finally {
			close(stmt);
			close(res);
		}

	}

	// added by Ching Heng for getting the email trigger version number, for
	// update question attribute
	public int getEmailTriggerVersion(int questionAttributeId)
			throws CtdbException {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		int versionNumber = Integer.MIN_VALUE;
		try {
			stmt = conn
					.prepareStatement("select version from emailtrigger where emailtriggerid =(select emailtriggerid from questionattributes where questionattributesid=?)");
			stmt.setLong(1, questionAttributeId);
			rs = stmt.executeQuery();
			if (rs.next()) {
				versionNumber = rs.getInt("version");
			}
			return versionNumber;

		} catch (SQLException sqle) {
			throw new CtdbException(" Failure getting email trigger version "
					+ sqle.getMessage(), sqle);
		} finally {
			close(stmt);
			close(rs);
		}
	}
	
	
	
	
	

		public String[] getDataElementGroupAndName(int sectionid, int questionid) throws CtdbException {
			PreparedStatement stmt = null;
			ResultSet rs = null;
			String groupName = null;
			String dataElementName = null;
			String[] resultArr = new String[2];
			try {
				stmt = conn.prepareStatement("select group_name,data_element_name from questionattributes where questionattributesid = (select questionattributesid from sectionquestion where sectionid = ? and questionid = ?)");
				stmt.setLong(1, sectionid);
				stmt.setLong(2, questionid);
				rs = stmt.executeQuery();
				if (rs.next()) {
					groupName = rs.getString("group_name");
					dataElementName =  rs.getString("data_element_name");
				}
				
				if(groupName == null) {
					return null;
				}else {
					resultArr[0] = groupName;
					resultArr[1] = dataElementName;
							
					return resultArr;
				}
					
					
			} catch (SQLException sqle) {
				throw new CtdbException(" Failure getting email trigger version "
						+ sqle.getMessage(), sqle);
			} finally {
				close(stmt);
				close(rs);
			}
		}
		
		
		
		public String getDataStructureName(int formid) throws CtdbException {
			PreparedStatement stmt = null;
			ResultSet rs = null;
			String data_structure_name = null;
			String data_structure_version = null;
			try {
				stmt = conn.prepareStatement("select data_structure_name,data_structure_version from form where formid = ?");
				stmt.setLong(1, formid);
				rs = stmt.executeQuery();
				if (rs.next()) {
					data_structure_name = rs.getString("data_structure_name");
					data_structure_version =  rs.getString("data_structure_version");
				}
				

					return data_structure_name + "_" + data_structure_version;

					
					
			} catch (SQLException sqle) {
				throw new CtdbException(" Failure getting email trigger version "
						+ sqle.getMessage(), sqle);
			} finally {
				close(stmt);
				close(rs);
			}
		}
	
	
	
	
	
	

	/**
	 * Method to get the formId for given form name in particular protocol
	 * 
	 * @param protocolId
	 * @param name
	 * @return formId
	 * @throws ObjectNotFoundException
	 * @throws CtdbException
	 */

	public int getFormID(int protocolId, String name)
			throws ObjectNotFoundException, CtdbException {
		int formId = Integer.MIN_VALUE;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
		
			stmt = conn.prepareStatement("select formid from form  where protocolid = ? and name = ?");
			stmt.setInt(1, protocolId);
			stmt.setString(2, name);
			rs = stmt.executeQuery();
			if (rs.next()) {
				formId = rs.getInt("formid");
			}
			return formId;
		} catch (SQLException sqle) {
			throw new CtdbException(
					" Failure getting FormId for  provided form name "
							+ sqle.getMessage(), sqle);
		} finally {
			close(stmt);
			close(rs);
		}
	}
	
	public int getFormIdByShortName(int protocolId, String shortName)
			throws ObjectNotFoundException, CtdbException {
		int formId = Integer.MIN_VALUE;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
		
			stmt = conn.prepareStatement("select eformid from eform  where protocolid = ? and shortname = ?");
			stmt.setInt(1, protocolId);
			stmt.setString(2, shortName);
			rs = stmt.executeQuery();
			if (rs.next()) {
				formId = rs.getInt("eformid");
			}
			return formId;
		} catch (SQLException sqle) {
			throw new CtdbException(
					" Failure getting FormId for  provided form name "
							+ sqle.getMessage(), sqle);
		} finally {
			close(stmt);
			close(rs);
		}
	}

	
	/**
	 * Method to get the shortname for given eformid in particular protocol
	 * 
	 * @param protocolId
	 * @param name
	 * @return formId
	 * @throws ObjectNotFoundException
	 * @throws CtdbException
	 */

	public String getEFormShortNameByEFormId(int eformId)
			throws ObjectNotFoundException, CtdbException {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		String shortName = "";
		try {
		
			stmt = conn.prepareStatement("select shortname from eform  where eformid = ?");
			stmt.setInt(1, eformId);
			rs = stmt.executeQuery();
			if (rs.next()) {
				shortName = rs.getString("shortname");
			}
			return shortName;
		} catch (SQLException sqle) {
			throw new CtdbException(
					" Failure getting FormId for  provided form name "
							+ sqle.getMessage(), sqle);
		} finally {
			close(stmt);
			close(rs);
		}
	}
	
	
	/**
	 * Method to get the name for given eformid in particular protocol
	 * 
	 * @param protocolId
	 * @param name
	 * @return formId
	 * @throws ObjectNotFoundException
	 * @throws CtdbException
	 */

	public String getEFormNameByEFormId(int eformId)
			throws ObjectNotFoundException, CtdbException {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		String shortName = "";
		try {
		
			stmt = conn.prepareStatement("select name from eform  where eformid = ?");
			stmt.setInt(1, eformId);
			rs = stmt.executeQuery();
			if (rs.next()) {
				shortName = rs.getString("name");
			}
			return shortName;
		} catch (SQLException sqle) {
			throw new CtdbException(
					" Failure getting FormId for  provided form name "
							+ sqle.getMessage(), sqle);
		} finally {
			close(stmt);
			close(rs);
		}
	}
	
	
	
	
	
	/**
	 * Method to get the shortname for given eformid in particular protocol
	 * 
	 * @param protocolId
	 * @param name
	 * @return formId
	 * @throws ObjectNotFoundException
	 * @throws CtdbException
	 */

	public String getEFormShortNameByAFormId(int aformId)
			throws ObjectNotFoundException, CtdbException {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		String shortName = "";
		try {
		
			stmt = conn.prepareStatement("select shortname from eform, administeredform af where af.administeredformid = ? and af.eformid = eform.eformid");
			stmt.setInt(1, aformId);
			rs = stmt.executeQuery();
			if (rs.next()) {
				shortName = rs.getString("shortname");
			}
			return shortName;
		} catch (SQLException sqle) {
			throw new CtdbException(
					" Failure getting FormId for  provided form name "
							+ sqle.getMessage(), sqle);
		} finally {
			close(stmt);
			close(rs);
		}
	}
	

	public String[] getEFormShortNameAndProtocolIdByAFormId(int aformId) throws ObjectNotFoundException, CtdbException {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		String[] shortNameAndProtocolId = new String[2];
		try {
		
			stmt = conn.prepareStatement("select shortname, protocolid from eform, administeredform af where af.administeredformid = ? and af.eformid = eform.eformid");
			stmt.setInt(1, aformId);
			rs = stmt.executeQuery();
			if (rs.next()) {
				shortNameAndProtocolId[0] = rs.getString("shortname");
				shortNameAndProtocolId[1] = rs.getString("protocolid");
			}
			return shortNameAndProtocolId;
		} catch (SQLException sqle) {
			throw new CtdbException(
					" Failure getting FormId for  provided form name "
							+ sqle.getMessage(), sqle);
		} finally {
			close(stmt);
			close(rs);
		}
	}
	
	
	
	
	
	
	
	
	
	
	
	/**
	 * Method to get the formName for given form formId in particular protocol
	 * @param protocolId
	 * @param formId
	 * @return
	 * @throws ObjectNotFoundException
	 * @throws CtdbException
	 */
	public String getFormNameNP(int protocolId, int formId)
			throws ObjectNotFoundException, CtdbException {
		String formName = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
		
			stmt = conn
					.prepareStatement("select f.name formName  from nonpatientdata npd join form f ON npd.formid = f.formid where f.protocolid =? and npd.nonpatientdataid = ?");
			stmt.setInt(1, protocolId);
			stmt.setInt(2, formId);
			rs = stmt.executeQuery();
			if (rs.next()) {
				formName = rs.getString("formName");
			}
			return formName;
		} catch (SQLException sqle) {
			throw new CtdbException(
					" Failure getting FormName for  provided form formId "
							+ sqle.getMessage(), sqle);
		} finally {
			close(stmt);
			close(rs);
		}
	}
	
	/**
	 * Method to get the formName for given form formId in particular protocol
	 * @param protocolId
	 * @param formId
	 * @return
	 * @throws ObjectNotFoundException
	 * @throws CtdbException
	 */
	public String getFormNameP(int protocolId, int formId)
			throws ObjectNotFoundException, CtdbException {
		String formName = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
		
			stmt = conn
					.prepareStatement("select f.name formName  from administeredform af join form f ON af.formid = f.formid where f.protocolid =? and af.administeredformid = ?");
			stmt.setInt(1, protocolId);
			stmt.setInt(2, formId);
			rs = stmt.executeQuery();
			if (rs.next()) {
				formName = rs.getString("formName");
			}
			return formName;
		} catch (SQLException sqle) {
			throw new CtdbException(
					" Failure getting FormName for  provided form formId "
							+ sqle.getMessage(), sqle);
		} finally {
			close(stmt);
			close(rs);
		}
	}
	
	
	public String getFormNameForEFormId(int protocolId, int eformId)
			throws ObjectNotFoundException, CtdbException {
		String formName = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
		
			stmt = conn
					.prepareStatement("select name formName from eform where protocolid = ? and eformid = ?");
			stmt.setInt(1, protocolId);
			stmt.setInt(2, eformId);
			rs = stmt.executeQuery();
			if (rs.next()) {
				formName = rs.getString("formName");
			}
			return formName;
		} catch (SQLException sqle) {
			throw new CtdbException(
					" Failure getting FormName for  provided form formId "
							+ sqle.getMessage(), sqle);
		} finally {
			close(stmt);
			close(rs);
		}
	}
	
	/**
	 * Get the formLayout information like row and column for given formId and studyId
	 * @param formId
	 * @param studyId
	 * @return
	 * @throws ObjectNotFoundException
	 * @throws CtdbException
	 */
	public ArrayList<FormLayout> getFormLayoutRowAndColumn(int formId, int studyId) throws ObjectNotFoundException, CtdbException {
		FormLayout formLayout = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		ArrayList<FormLayout> formLayoutList = new ArrayList<FormLayout> ();
		try {
			stmt = conn.prepareStatement("select fl.formrow formRow,fl.numcols formCol from formlayout fl,form f where f.formid = fl.formid and f.formid =? and f.protocolid= ?");
			stmt.setInt(1, formId);
			stmt.setInt(2, studyId);
			rs = stmt.executeQuery();
			while (rs.next()) {
				formLayout = new FormLayout();
				formLayout.setFormColNo(rs.getInt("formCol"));
				formLayout.setFormRowNo(rs.getInt("formRow"));
				formLayoutList.add(formLayout);
			}
			return formLayoutList;
		} catch (SQLException sqle) {
			throw new CtdbException(" Failure getting row and col for provided studyId and  formId "+ sqle.getMessage(), sqle);
		} finally {
			close(stmt);
			close(rs);
		}
	}
	
	
	/**
	 * 
	 * Method to get the sectionGroupList for parentSectionId
	 * @param parentSectionId
	 * @return sectionGroupList
	 * @throws CtdbException
	 */
	public List<Integer> getRepeableSectionGroup(int parentSectionId) throws CtdbException {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		List<Integer> results = new ArrayList<Integer>();
		
		try {
			String sql = "select sectionid from section where repeatedsectionparent = ? union select sectionid from section where sectionid = ? ";
			stmt = conn.prepareStatement(sql);
			stmt.setLong(1, parentSectionId);
			stmt.setLong(2, parentSectionId);
			rs = stmt.executeQuery();
			
			while (rs.next()) {
				results.add(new Integer(rs.getInt("sectionid")));
			}
		}
		catch (SQLException sqle) {
			throw new CtdbException(" Failure getting section group list " + sqle.getMessage(), sqle);
		}
		
		return results;
	}
	
	
	
	/**
	 * 
	 * Method to get the parent repeatable section ids for all repeatable groups in form
	 * @param parentSectionId
	 * @return sectionGroupList
	 * @throws CtdbException
	 */
	public List<Integer> getRepeableSectionParentSectionIds(int formId) throws CtdbException {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		List<Integer> results = new ArrayList<Integer>();
		
		try {
			String sql = "select sectionid from section where isrepeatable = true and repeatedsectionparent = -1 and formid = ?";
			stmt = conn.prepareStatement(sql);
			stmt.setLong(1, formId);
			rs = stmt.executeQuery();
			
			while (rs.next()) {
				results.add(new Integer(rs.getInt("sectionid")));
			}
		}
		catch (SQLException sqle) {
			throw new CtdbException(" Failure getting section group list " + sqle.getMessage(), sqle);
		}
		
		return results;
	}
	
	
	
	
	
	/**
	 * Method to get unique list of questions for a given study and form 
	 * @param formId
	 * @param studyId
	 * @return
	 * @throws CtdbException
	 */
	public List<Integer> getUniqueQuestionsForFormInStudy(int formId, int studyId) throws CtdbException {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		List<Integer> results = new ArrayList<Integer>();
		
		try {
			stmt = conn
					.prepareStatement("select distinct sq.questionid qid from section s, sectionquestion sq, form f where s.sectionid = sq.sectionid and s.formid =? and f.formid = s.formid and f.protocolid =?");
			stmt.setInt(1, formId);
			stmt.setInt(2, studyId);
			rs = stmt.executeQuery();
			while (rs.next()) {
				results.add(rs.getInt("qid"));
			}
		}
		catch (SQLException sqle) {
			throw new CtdbException(" Unable to get question list for selected form and study " + sqle.getMessage(), sqle);
		}
		
		return results;
	}
	
	/***
	 * Added method to insert data into formLayout table for given formId rowNo and Column for XML export import functionality
	 * @param formId
	 * @param rowNo
	 * @param colNo
	 * @throws CtdbException
	 */
	 public void addRowInFormLayoutTableForGivenFormId(int formId,int rowNo, int colNo) throws CtdbException {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		
		try {
			String sql = "insert into formlayout  (formid, formrow, numcols) values (?, ?, ?) ";
			stmt = this.conn.prepareStatement(sql);
			stmt.setLong(1, formId);
			stmt.setLong(2, rowNo);
			stmt.setLong(3, colNo);
			stmt.executeUpdate();
		}
		catch (SQLException e) {
			throw new CtdbException("Unable to insert record in formlayout table: "+ e.getMessage(), e);
		}
		finally {
			this.close(stmt);
			this.close(rs);
		}
	}

	/** 
	 * Note: Form name must be unique within study, not globally unique 
	 * 
	 * @param name
	 * @param studyId
	 * @return
	 * @throws CtdbException
	 */
	public boolean isFormNameExist(String name, int studyId)  throws CtdbException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        boolean isDuplicated = false;
        
        // Proceed only if the subject ID is valid
        if( (name == null) || (name.length() == 0) ) {
        	return false;
        }

        try {
            String sql = "select count(1) from form where protocolid =? and upper(name) = upper(?) ";
            
            stmt = this.conn.prepareStatement(sql);
            stmt.setInt(1, studyId);
            stmt.setString(2, name);
            
            rs = stmt.executeQuery();
            int count = 0;
            
            if  (rs.next() ) {
                count = rs.getInt(1);
            }
            
            if ( count > 0 ) {
                isDuplicated = true;
            }
        }
        catch ( SQLException e ) {
            throw new CtdbException("Unable to check if the form name already exists in the system: " + e.getMessage(), e);
        }
        finally {
        	this.close(rs);
            this.close(stmt);
        }
        
        return isDuplicated;
	}
	
	/**
	 * Gets a JSON array of forms that have not been migrated yet.
	 * 
	 * TODO Remove this method once eform migration is done.
	 * 
	 * @return A JSON array of forms to migrate. The object attributes are the same as the
	 * corresponding form table column names.
	 * @throws CtdbException When a database error occurs.
	 * @throws JSONException When there is an error converting results to JSON objects.
	 */
	public JSONArray getFormsToMigrate() throws CtdbException, JSONException {
		JSONArray formArray = new JSONArray();
		PreparedStatement stmt = null;
		ResultSet rs = null;

		try {
			String sql = "select f.formid, f.version, f.name, f.description, f.islegacy, s.protocolnumber, u.username " +
					"from form f join protocol s on f.protocolid = s.protocolid join usr u on f.createdby = u.usrid " +
					"where f.ismigrated = false and s.deleteflag = false and f.name not like '%X%-%DO NOT USE%' union " +
					"select distinct f.formid, f.version, f.name, f.description, f.islegacy, s.protocolnumber, u.username " +
					"from form f join protocol s on f.protocolid = s.protocolid join usr u on f.createdby = u.usrid left " +
					"outer join administeredform af on f.formid = af.formid where f.ismigrated = false and " +
					"s.deleteflag = true and af.administeredformid is not null and f.name not like '%X%-%DO NOT USE%' ";

			stmt = this.conn.prepareStatement(sql);
			rs = stmt.executeQuery();

			// Translate the SQL results to form JSON objects and place them in the array.
			while (rs.next()) {
				JSONObject formJson = new JSONObject();
				
				formJson.put("formid", rs.getLong("formid"));
				formJson.put("version", rs.getInt("version"));
				formJson.put("name", rs.getString("name"));
				formJson.put("description", rs.getString("description"));
				formJson.put("islegacy", rs.getBoolean("islegacy"));
				formJson.put("protocolnumber", rs.getString("protocolnumber"));
				formJson.put("username", rs.getString("username"));
				
				// Add form JSON to array.
				formArray.put(formJson);
			}
		}
		catch ( SQLException e ) {
			throw new CtdbException("Unable to get listing of forms to migrate: " + e.getMessage(), e);
		}
		finally {
			close(rs);
			close(stmt);
		}

		return formArray;
	}
	
	
	
	public void updateProformsTablesForMigration(int formId, EformMigrationAdapter eformMigrationAdapter) throws CtdbException {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		String sql = "";
		
		String sql1 = "";
		String sql2 = "";
		String sql3 = "";
		String sql4 = "";
		String sql5 = "";
		String sql6 = "";
		String sql7 = "";
		PreparedStatement stmt1 = null;
		PreparedStatement stmt2 = null;
		PreparedStatement stmt3 = null;
		PreparedStatement stmt4 = null;
		PreparedStatement stmt5 = null;
		PreparedStatement stmt6 = null;
		PreparedStatement stmt7 = null;
		
		String name = "";
		String dataStructureName = "";
		boolean allowMultipleCollectionInstances = false;
		java.sql.Date createdDate = null;
		java.sql.Date updatedDate = null;
		int protocolId = -1;
		int eformId = -1;
		String shortName = eformMigrationAdapter.getEformShortName();
		//List<MigratedQuestion> migratedQuestionList = eformMigrationAdapter.getQuestionList();
		List<MigratedSection> migratedSectionList = eformMigrationAdapter.getSectionList();
		
		try {
		
		//get basic info from form table
		sql = "select * from form where formid = ?";
		stmt = this.conn.prepareStatement(sql);
		stmt.setInt(1, formId);
		rs = stmt.executeQuery();
		while (rs.next()) {
			name = rs.getString("name");
			dataStructureName = rs.getString("data_structure_name");
			allowMultipleCollectionInstances = Boolean.valueOf(rs.getString("allow_multiple_collection_instances"));
			createdDate = rs.getDate("createddate");
			updatedDate = rs.getDate("updateddate");
			protocolId = rs.getInt("protocolid");
		}
		
		rs.close();
		stmt.close();
		
		//now insert into eform table
		sql = "insert into eform(eformid, shortname,name,data_structure_name,protocolid,allow_multiple_collection_instances,updateddate,createddate ) values (DEFAULT,?,?,?,?,?,?,?)";
		stmt = this.conn.prepareStatement(sql);
		stmt.setString(1, shortName);
		stmt.setString(2, name);
		stmt.setString(3, dataStructureName);
		stmt.setInt(4, protocolId);
		stmt.setBoolean(5,allowMultipleCollectionInstances);
		stmt.setDate(6, updatedDate);
		stmt.setDate(7, createdDate);
		stmt.executeUpdate();
		stmt.close(); 
		eformId = getInsertId(conn, "eform_seq");
		
		
		//update form_eform_migration table 
		sql = "insert into form_eform_migration(formid, eformid) values (?,?)"; 
		stmt = this.conn.prepareStatement(sql);
		stmt.setInt(1, formId);
		stmt.setInt(2, eformId);
		stmt.executeUpdate();
		stmt.close(); 
		

		//update visit type (form_interval)  table
		sql = "update form_interval set eformid = ? where formid = ?";
		stmt = this.conn.prepareStatement(sql);
		stmt.setInt(1, eformId);
		stmt.setInt(2, formId);
		stmt.executeUpdate();
		stmt.close(); 
		
		
		//update administeredform  table
		sql = "update administeredform set eformid = ? where formid = ?";
		stmt = this.conn.prepareStatement(sql);
		stmt.setInt(1, eformId);
		stmt.setInt(2, formId);
		stmt.executeUpdate();
		stmt.close(); 
		
		
		//update sentemail table
		sql = "update sentemail set eformid = ? where formid = ?";
		stmt = this.conn.prepareStatement(sql);
		stmt.setInt(1, eformId);
		stmt.setInt(2, formId); 
		stmt.executeUpdate();
		stmt.close();
		
		
		// Get a string list of administered form IDs for this form.
		sql = "select af.administeredformid from administeredform af where af.formid = ?";
		stmt = this.conn.prepareStatement(sql);
		stmt.setLong(1, formId);
		rs = stmt.executeQuery();
		String adminFormIds = createStringListing(rs);
		rs.close();
		stmt.close();
		
		
		// Get a string list of response draft IDs for this form.
		sql = "select rd.responsedraftid from responsedraft rd, dataentrydraft ded, administeredform af where " +
				"rd.dataentrydraftid = ded.dataentrydraftid and ded.administeredformid = af.administeredformid and af.formid = ?";
		stmt = this.conn.prepareStatement(sql);
		stmt.setLong(1, formId);
		rs = stmt.executeQuery();
		String responseDraftIds = createStringListing(rs);
		rs.close();
		stmt.close();
		
		
		// Get a string list of response IDs for this form.
		sql = "select r.responseid from response r, administeredform af where  r.administeredformid = af.administeredformid and af.formid = ?";
		stmt = this.conn.prepareStatement(sql);
		stmt.setLong(1, formId);
		rs = stmt.executeQuery();
		String responseIds = createStringListing(rs);
		rs.close();
		stmt.close();
		
		
		sql1 = "update visibleadminsteredsection set dict_sectionid = ? where sectionid = ? and administeredformid in (" + adminFormIds + ");";
		sql2 = "update responsedraft set dict_sectionid = ? where sectionid = ? and responsedraftid in (" + responseDraftIds + ");";
		sql3 = "update responsedraft set dict_questionid = ? where questionid = ? and sectionid = ? and responsedraftid in (" + responseDraftIds + ");";
		sql4 = "update response set dict_sectionid = ? where sectionid = ? and responseid in (" + responseIds + ");";
		sql5 = "update response set dict_questionid = ? where questionid = ? and sectionid = ? and responseid in (" + responseIds + ");";
		sql6 = "update responseedit set dict_sectionid = ? where sectionid = ? and administeredformid in (" + adminFormIds + ");";
		sql7 = "update responseedit set dict_questionid = ? where questionid = ? and sectionid = ? and administeredformid in (" + adminFormIds + ");";
		stmt1 = this.conn.prepareStatement(sql1);
		stmt2 = this.conn.prepareStatement(sql2);
		stmt3 = this.conn.prepareStatement(sql3);
		stmt4 = this.conn.prepareStatement(sql4);
		stmt5 = this.conn.prepareStatement(sql5);
		stmt6 = this.conn.prepareStatement(sql6);
		stmt7 = this.conn.prepareStatement(sql7);
		
		for (MigratedSection migratedSection : migratedSectionList) {
			long proformsSectionId = migratedSection.getProformsSectionId();
			long dictionarySectionId = migratedSection.getDictionarySectionId();
			 
			stmt1.setLong(1, dictionarySectionId);
			stmt1.setLong(2, proformsSectionId);
			stmt1.executeUpdate();
			
			stmt2.setLong(1, dictionarySectionId);
			stmt2.setLong(2, proformsSectionId);
			stmt2.executeUpdate();
			
			stmt4.setLong(1, dictionarySectionId);
			stmt4.setLong(2, proformsSectionId);
			stmt4.executeUpdate();
			
			stmt6.setLong(1, dictionarySectionId);
			stmt6.setLong(2, proformsSectionId);
			stmt6.executeUpdate();
			
			List<MigratedQuestion> migratedQuestionList = migratedSection.getMigratedQuestion();

			for (MigratedQuestion migratedQuestion : migratedQuestionList) {
				long proformsQuestionId = migratedQuestion.getProformsQuestionId();
				long dictionaryQuestionId = migratedQuestion.getDictionaryQuestionId();

				
				stmt3.setLong(1, dictionaryQuestionId);
				stmt3.setLong(2, proformsQuestionId);
				stmt3.setLong(3, proformsSectionId);
				stmt3.executeUpdate();
				
				stmt5.setLong(1, dictionaryQuestionId);
				stmt5.setLong(2, proformsQuestionId);
				stmt5.setLong(3, proformsSectionId);
				stmt5.executeUpdate();	
				
				stmt7.setLong(1, dictionaryQuestionId);
				stmt7.setLong(2, proformsQuestionId);
				stmt7.setLong(3, proformsSectionId);
				stmt7.executeUpdate();	
			}

		}
		
		stmt1.close();
		stmt2.close();
		stmt3.close();
		stmt4.close();
		stmt5.close();
		stmt6.close();
		stmt7.close();
		
		
		//update ismigrated in form table
		sql = "update form set ismigrated = true where formid = ?";
		stmt = this.conn.prepareStatement(sql);
		stmt.setInt(1, formId);
		stmt.executeUpdate();
		stmt.close();
		

		} catch (SQLException e) {
			throw new CtdbException("Unable to update tables for proforms formid: " + formId + ":" + e.getMessage(), e);
		} finally {
			this.close(rs);
			this.close(stmt);
			this.close(stmt1);
			this.close(stmt2);
			this.close(stmt3);
			this.close(stmt4);
			this.close(stmt5);
			this.close(stmt6);
			this.close(stmt7);
		}
	}
	
	private String createStringListing(ResultSet rs) throws SQLException {
		StringBuffer listing = new StringBuffer(500);
		
		// Create the listing
		while (rs.next()) {
			String value = rs.getString(1);
			
			if (value != null) {
				listing.append(value).append(",");
			}
		}
		
		// Remove the comma at the end of the string.
		if (listing.length() > 0) {
			listing.deleteCharAt(listing.length() - 1);
		}
		else {
			listing.append("null");
		}
		
		return listing.toString();
	}
	
	public Integer getAdministeredFormSubmissionStatus(Integer administiredFormId) throws CtdbException{
		String sql = "SELECT xsubmissionstatusid FROM administeredform where administeredformid = ?";
		PreparedStatement preparedQuery = null;
		ResultSet queryResults = null;
		Integer administeredFormSubmissionStatus = null;
		try{
			preparedQuery = this.conn.prepareStatement(sql);
			preparedQuery.setLong(1, administiredFormId);
			queryResults = preparedQuery.executeQuery();
			
			if  (queryResults.next() ) {
				administeredFormSubmissionStatus = queryResults.getInt(1);
            }
		} catch (SQLException sqle) {
			throw new CtdbException(" Failure getting row and col for provided studyId and  formId "+ sqle.getMessage(), sqle);
		} finally {
			close(preparedQuery);
			close(queryResults);
		}
		return administeredFormSubmissionStatus;
	}
}
