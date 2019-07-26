package gov.nih.nichd.ctdb.util.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import gov.nih.nichd.ctdb.common.CtdbDao;
import gov.nih.nichd.ctdb.common.CtdbException;
import gov.nih.nichd.ctdb.common.CtdbLookup;
import gov.nih.nichd.ctdb.common.ResultControl;
import gov.nih.nichd.ctdb.util.domain.Institute;

/**
 * LookupManagerDao interacts with the Data Layer for the LookupManager.
 * The only job of the DAO is to manipulate the data layer.
 *
 * @author Booz Allen Hamilton
 * @version 1.0
 */
public class LookupManagerDao extends CtdbDao {
    /**
     * Private Constructor to hide the instance
     * creation implementation of the LookupManagerDao object
     * in memory. This will provide a flexible architecture
     * to use a different pattern in the future without
     * refactoring the LookupManager.
     */
    private LookupManagerDao() {

    }

    /**
     * Method to retrieve the instance of the LookupManagerDao.
     *
     * @return LookupManagerDao data object
     */
    public static synchronized LookupManagerDao getInstance() {
        return new LookupManagerDao();
    }

    /**
     * Method to retrieve the instance of the LookupManagerDao. This method
     * accepts a Database Connection to be used internally by the DAO. All
     * transaction management will be handled at the BusinessManager level.
     *
     * @param conn Database connection to be used within this data object
     * @return LookupManagerDao data object
     */
    public static synchronized LookupManagerDao getInstance(Connection conn) {
        LookupManagerDao dao = new LookupManagerDao();
        dao.setConnection(conn);
        return dao;
    }

    /**
     * Returns a list of state CtdbLookup objects
     *
     * @param rc ResultControl object to control sorting
     * @return List of states
     * @throws CtdbException thrown if any errors occur while processing
     */
    public List<CtdbLookup> getStates(ResultControl rc) throws CtdbException {
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            StringBuffer sql = new StringBuffer(25);
            /**
             * JH CCB Updates June 17 2004,
             *
             * User wants None to be first. Ordering by xstateid accomplishes this;
             * however, this may not be an optimum solution.
             */

            sql.append("select * from xstate order by xstateid ");
            sql.append(rc.getSortString());

            stmt = this.conn.prepareStatement(sql.toString());

            rs = stmt.executeQuery();

            List<CtdbLookup> states = new ArrayList<CtdbLookup>();
            while (rs.next()) {
                CtdbLookup state = new CtdbLookup(rs.getInt("xstateid"),
                        rs.getString("code"),
                        rs.getString("name"));
                states.add(state);
            }


            return states;
        }
        catch (SQLException e) {
            throw new CtdbException("Unable to get states: " + e.getMessage(), e);
        }
        finally {
            this.close(rs);
            this.close(stmt);
        }
    }

    /**
     * Returns a list of country CtdbLookup objects
     *
     * @param rc ResultControl object to control sorting
     * @return List of countries
     * @throws CtdbException thrown if any errors occur while processing
     */
    public List<CtdbLookup> getCountries(ResultControl rc) throws CtdbException {
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            StringBuffer sql = new StringBuffer(25);
            sql.append("select * from xcountry order by xcountryid ");
            sql.append(rc.getSortString());

            stmt = this.conn.prepareStatement(sql.toString());

            rs = stmt.executeQuery();

            List<CtdbLookup> countries = new ArrayList<CtdbLookup>();
            while (rs.next()) {
                CtdbLookup country = new CtdbLookup(rs.getInt("xcountryid"),
                        //rs.getString("shortname"),
                        rs.getString("longname"));
                countries.add(country);
            }

            return countries;
        }
        catch (SQLException e) {
            throw new CtdbException("Unable to get countries: " + e.getMessage(), e);
        }
        finally {
            this.close(rs);
            this.close(stmt);
        }
    }

    /**
     * Returns a list of form status CtdbLookup objects
     *
     * @param rc ResultControl object to control sorting
     * @return List of form statuses
     * @throws CtdbException thrown if any errors occur while processing
     */
    public List<CtdbLookup> getRangeOperators(ResultControl rc) throws CtdbException {
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            StringBuffer sql = new StringBuffer(25);
            sql.append("select * from questionrangeoperator order by id ");
            sql.append(rc.getSortString());

            stmt = this.conn.prepareStatement(sql.toString());

            rs = stmt.executeQuery();

            List<CtdbLookup> ops = new ArrayList<CtdbLookup>();
            while (rs.next()) {
                CtdbLookup op = new CtdbLookup(rs.getInt("id"),
                        rs.getString("operator"));
                ops.add(op);
            }

            return ops;
        }
        catch (SQLException e) {
            throw new CtdbException("Unable to get range operators: " + e.getMessage(), e);
        }
        finally {
            this.close(rs);
            this.close(stmt);
        }
    }

    /**
     * Returns a list of form status CtdbLookup objects
     *
     * @param rc ResultControl object to control sorting
     * @return List of form statuses
     * @throws CtdbException thrown if any errors occur while processing
     */
    public List<CtdbLookup> getFormStatuses(ResultControl rc) throws CtdbException {
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            StringBuffer sql = new StringBuffer(25);
            sql.append("select * from xformstatus ");
            sql.append(rc.getSortString());

            stmt = this.conn.prepareStatement(sql.toString());

            rs = stmt.executeQuery();

            List<CtdbLookup> states = new ArrayList<CtdbLookup>();
            while (rs.next()) {
                CtdbLookup state = new CtdbLookup(rs.getInt("xstatusid"),
                        rs.getString("shortname"));
                states.add(state);
            }

            return states;
        }
        catch (SQLException e) {
            throw new CtdbException("Unable to get form statuses: " + e.getMessage(), e);
        }
        finally {
            this.close(rs);
            this.close(stmt);
        }
    }

    /**
     * Returns a list of protocol status CtdbLookup objects
     *
     * @param rc ResultControl object to control sorting
     * @return List of protocol statuses
     * @throws CtdbException thrown if any errors occur while processing
     */
    public List<CtdbLookup> getProtocolStatuses(ResultControl rc) throws CtdbException {
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            StringBuffer sql = new StringBuffer(25);
            sql.append("select * from xprotocolstatus order by xprotocolstatusid");
            sql.append(rc.getSortString());

            stmt = this.conn.prepareStatement(sql.toString());

            rs = stmt.executeQuery();

            List<CtdbLookup> states = new ArrayList<CtdbLookup>();
            while (rs.next()) {
                CtdbLookup status = new CtdbLookup(rs.getInt("xprotocolstatusid"),
                        rs.getString("name"));
                states.add(status);
            }

            return states;
        }
        catch (SQLException e) {
            throw new CtdbException("Unable to get protocol statuses: " + e.getMessage(), e);
        }
        finally {
            this.close(rs);
            this.close(stmt);
        }
    }

    /**
     * Returns a list of Interval types CtdbLookup objects
     *
     * @param rc ResultControl object to control sorting
     * @return List of protocol statuses
     * @throws CtdbException thrown if any errors occur while processing
     */
    public List<CtdbLookup> getIntervalTypes(ResultControl rc) throws CtdbException {
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            StringBuffer sql = new StringBuffer(25);
            sql.append("select * from xintervaltype ");
            sql.append(rc.getSortString());

            stmt = this.conn.prepareStatement(sql.toString());

            rs = stmt.executeQuery();

            List<CtdbLookup> intervalTypes = new ArrayList<CtdbLookup>();
            while (rs.next()) {
                CtdbLookup types = new CtdbLookup(rs.getInt("XINTERVALTYPEID"),
                        rs.getString("INTERVALTYPENAME"));
                intervalTypes.add(types);
            }

            return intervalTypes;
        }
        catch (SQLException e) {
            throw new CtdbException("Unable to get INTERVAL TYPE NAME: " + e.getMessage(), e);
        }
        finally {
            this.close(rs);
            this.close(stmt);
        }
    }
    
    /**
     * Returns a list of protocol type CtdbLookup objects
     *
     * @param rc ResultControl object to control sorting
     * @return List of institutes
     * @throws CtdbException thrown if any errors occur while processing
     */
    public List<CtdbLookup> getUserName(ResultControl rc) throws CtdbException {
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            StringBuffer sql = new StringBuffer(25);
            sql.append("select usrid, username, lastname||', '||firstname lnfn from usr order by lastname asc");
            // sql.append(rc.getSortString());

            stmt = this.conn.prepareStatement(sql.toString());

            rs = stmt.executeQuery();

            List<CtdbLookup> user = new ArrayList<CtdbLookup>();
            while (rs.next()) {
                CtdbLookup type = new CtdbLookup(rs.getInt("usrid"),
                        rs.getString("username"),
                        rs.getString("lnfn"));
                user.add(type);
            }

            return user;
        }
        catch (SQLException e) {
            throw new CtdbException("Unable to get user list: " + e.getMessage(), e);
        }
        finally {
            this.close(rs);
            this.close(stmt);
        }
    }
    
    /**
     * Returns a list of institute CtdbLookup objects
     *
     * @param rc ResultControl object to control sorting
     * @return List of institutes
     * @throws CtdbException thrown if any errors occur while processing
     */
    public List<CtdbLookup> getInstitutes(ResultControl rc) throws CtdbException {
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            StringBuffer sql = new StringBuffer(25);
            sql.append("select * from xinstitute order by orderval, longname ");

            stmt = this.conn.prepareStatement(sql.toString());

            rs = stmt.executeQuery();

            List<CtdbLookup> states = new ArrayList<CtdbLookup>();
            while (rs.next()) {
                CtdbLookup state = new Institute(rs.getInt("xinstituteid"),
                        rs.getString("shortname"),
                        rs.getString("longname"));
                states.add(state);
            }

            return states;
        }
        catch (SQLException e) {
            throw new CtdbException("Unable to get institutes: " + e.getMessage(), e);
        }
        finally {
            this.close(rs);
            this.close(stmt);
        }
    }

    /**
     * Returns a list of Pumls Option CtdbLookup objects
     *
     * @param rc ResultControl object to control sorting
     * @return List of Pre or Post Rx Options
     * @throws CtdbException thrown if any errors occur while processing
     */
    public List<CtdbLookup> getResolutions(ResultControl rc) throws CtdbException {
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            StringBuffer sql = new StringBuffer(25);
            sql.append("select * from resolutions ");
            sql.append(rc.getSortString());
            stmt = this.conn.prepareStatement(sql.toString());

            rs = stmt.executeQuery();

            List<CtdbLookup> options = new ArrayList<CtdbLookup>();
            while (rs.next()) {
                CtdbLookup state = new CtdbLookup(rs.getInt("resolutionid"),
                        rs.getString("resolution"));
                options.add(state);
            }

            return options;
        }
        catch (SQLException e) {
            throw new CtdbException("Unable to get resolutions: " + e.getMessage(), e);
        }
        finally {
            this.close(rs);
            this.close(stmt);
        }
    }

    public List<CtdbLookup> getPatientSex(ResultControl rc) throws CtdbException {
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            StringBuffer sql = new StringBuffer(25);
            sql.append("select xsexid, name as name from xsex order by xsexid");

            stmt = this.conn.prepareStatement(sql.toString());

            rs = stmt.executeQuery();
            List<CtdbLookup> options = new ArrayList<CtdbLookup>();
            while (rs.next()) {
                CtdbLookup option = new CtdbLookup(rs.getInt("xsexid"),
                        rs.getString("name"));
                options.add(option);
            }
            return options;
        }
        catch (SQLException e) {
            throw new CtdbException("Unable to get sex: " + e.getMessage(), e);
        }
        finally {
            this.close(rs);
            this.close(stmt);
        }
    }


    public List<CtdbLookup> getPatientRace(ResultControl rc) throws CtdbException {
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            StringBuffer sql = new StringBuffer(25);
            sql.append("select xraceid, name as name from xrace ");
            //   if (rc.getSortString().equals("")) {
            sql.append("order by orderval");
            //     }else {
            //     sql.append(rc.getSortString());
            //     }
            stmt = this.conn.prepareStatement(sql.toString());

            rs = stmt.executeQuery();
            List<CtdbLookup> options = new ArrayList<CtdbLookup>();
            while (rs.next()) {
                CtdbLookup option = new CtdbLookup(rs.getInt("xraceid"),
                        rs.getString("name"));
                options.add(option);
            }
            return options;
        }
        catch (SQLException e) {
            throw new CtdbException("Unable to get race: " + e.getMessage(), e);
        }
        finally {
            this.close(rs);
            this.close(stmt);
        }
    }

    public List<CtdbLookup> getMaritalStatus(ResultControl rc) throws CtdbException {
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            rc.setSortBy(" xmaritalstatusid ");
            StringBuffer sql = new StringBuffer(25);
            sql.append("select xmaritalstatusid, longname as longname from xmaritalstatus ");
            if (rc.getSortString().equals("")) {
                sql.append(" order by xmaritialstatusid");
            } else {
                sql.append(rc.getSortString());
            }
            stmt = this.conn.prepareStatement(sql.toString());

            rs = stmt.executeQuery();
            List<CtdbLookup> options = new ArrayList<CtdbLookup>();
            while (rs.next()) {
                CtdbLookup option = new CtdbLookup(rs.getInt("xmaritalstatusid"),
                        rs.getString("longname"));
                options.add(option);
            }
            return options;
        }
        catch (SQLException e) {
            throw new CtdbException("Unable to get maritalstatus: " + e.getMessage(), e);
        }
        finally {
            this.close(rs);
            this.close(stmt);
        }
    }

    public List<CtdbLookup> getPreadmit(ResultControl rc) throws CtdbException {
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            StringBuffer sql = new StringBuffer(25);
            sql.append("select * from xpreadmit ");
            sql.append(rc.getSortString());
            stmt = this.conn.prepareStatement(sql.toString());

            rs = stmt.executeQuery();
            List<CtdbLookup> options = new ArrayList<CtdbLookup>();
            while (rs.next()) {
                CtdbLookup option = new CtdbLookup(rs.getInt("xpreadmitid"),
                        rs.getString("name"));
                options.add(option);
            }
            return options;
        }
        catch (SQLException e) {
            throw new CtdbException("Unable to get preadmit: " + e.getMessage(), e);
        }
        finally {
            this.close(rs);
            this.close(stmt);
        }
    }

    public List<CtdbLookup> getPrimaryProtocol(ResultControl rc) throws CtdbException {
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            StringBuffer sql = new StringBuffer(25);
            sql.append("select * from xprimaryprotocol ");
            sql.append(rc.getSortString());
            stmt = this.conn.prepareStatement(sql.toString());

            rs = stmt.executeQuery();
            List<CtdbLookup> options = new ArrayList<CtdbLookup>();
            while (rs.next()) {
                CtdbLookup option = new CtdbLookup(rs.getInt("xprimaryprotocolid"),
                        rs.getString("name"));
                options.add(option);
            }
            return options;
        }
        catch (SQLException e) {
            throw new CtdbException("Unable to get xprimaryprotocol: " + e.getMessage(), e);
        }
        finally {
            this.close(rs);
            this.close(stmt);
        }
    }

    public List<CtdbLookup> getPatientReligion(ResultControl rc) throws CtdbException {
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            StringBuffer sql = new StringBuffer(25);
            rc.setSortBy(" xreligionid ");
            sql.append("select xreligionid, name as name from xreligion ");
            sql.append(rc.getSortString());
            stmt = this.conn.prepareStatement(sql.toString());

            rs = stmt.executeQuery();
            List<CtdbLookup> options = new ArrayList<CtdbLookup>();
            while (rs.next()) {
                CtdbLookup option = new CtdbLookup(rs.getInt("xreligionid"),
                        rs.getString("name"));
                options.add(option);
            }
            return options;
        }
        catch (SQLException e) {
            throw new CtdbException("Unable to get xreligion: " + e.getMessage(), e);
        }
        finally {
            this.close(rs);
            this.close(stmt);
        }
    }

    public List<CtdbLookup> getSecondaryProtocol(ResultControl rc) throws CtdbException {
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            StringBuffer sql = new StringBuffer(25);
            sql.append("select * from xsecondaryprotocol ");
            sql.append(rc.getSortString());
            stmt = this.conn.prepareStatement(sql.toString());

            rs = stmt.executeQuery();
            List<CtdbLookup> options = new ArrayList<CtdbLookup>();
            while (rs.next()) {
                CtdbLookup option = new CtdbLookup(rs.getInt("xsecondaryprotocolid"),
                        rs.getString("name"));
                options.add(option);
            }
            return options;
        }
        catch (SQLException e) {
            throw new CtdbException("Unable to get xsecondaryprotocol: " + e.getMessage(), e);
        }
        finally {
            this.close(rs);
            this.close(stmt);
        }
    }


    public List<CtdbLookup> getEthnicity(ResultControl rc) throws CtdbException {
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            StringBuffer sql = new StringBuffer(25);
            sql.append("select xethnicityid, name as name from xethnicity ");
            sql.append(rc.getSortString());
            stmt = this.conn.prepareStatement(sql.toString());

            rs = stmt.executeQuery();
            List<CtdbLookup> options = new ArrayList<CtdbLookup>();
            while (rs.next()) {
                CtdbLookup option = new CtdbLookup(rs.getInt("xethnicityid"),
                        rs.getString("name"));
                options.add(option);
            }
            return options;
        }
        catch (SQLException e) {
            throw new CtdbException("Unable to get xethnicityid: " + e.getMessage(), e);
        }
        finally {
            this.close(rs);
            this.close(stmt);
        }
    }

    public List<CtdbLookup> getEducation(ResultControl rc) throws CtdbException {
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            StringBuffer sql = new StringBuffer(25);
            sql.append("select * from xeducationlevel order by xeducationlevelid");
            sql.append(rc.getSortString());
            stmt = this.conn.prepareStatement(sql.toString());

            rs = stmt.executeQuery();
            List<CtdbLookup> options = new ArrayList<CtdbLookup>();
            while (rs.next()) {
                CtdbLookup option = new CtdbLookup(rs.getInt("xeducationlevelid"),
                        rs.getString("longname"));
                options.add(option);
            }
            return options;
        }
        catch (SQLException e) {
            throw new CtdbException("Unable to get xeducationlevels: " + e.getMessage(), e);
        }
        finally {
            this.close(rs);
            this.close(stmt);
        }
    }

    public List<CtdbLookup> getOccupation(ResultControl rc) throws CtdbException {
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            StringBuffer sql = new StringBuffer(25);
            sql.append("select * from xoccupation ");
            sql.append(notNull(rc.getSearchClause()));
            sql.append("order by xoccupationid ");
            stmt = this.conn.prepareStatement(sql.toString());

            rs = stmt.executeQuery();
            List<CtdbLookup> options = new ArrayList<CtdbLookup>();
            while (rs.next()) {
                CtdbLookup option = new CtdbLookup(rs.getInt("xoccupationid"),
                        rs.getString("shortname"),
                        rs.getString("longname"));
                options.add(option);
            }
            return options;
        }
        catch (SQLException e) {
            throw new CtdbException("Unable to get xoccupations: " + e.getMessage(), e);
        }
        finally {
            this.close(rs);
            this.close(stmt);
        }
    }

    /**
     * Gets the types of attachments allowable in the cTDB system
     *
     * @param rc
     * @return
     * @throws CtdbException
     */
    public List<CtdbLookup> getAttachmentTypes(ResultControl rc) throws CtdbException {
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            StringBuffer sql = new StringBuffer(25);
            sql.append("select * from xattachmenttype ");
            sql.append(notNull(rc.getSearchClause()));
            sql.append(" order by xattachmenttypeid");
            stmt = this.conn.prepareStatement(sql.toString());

            rs = stmt.executeQuery();
            List<CtdbLookup> options = new ArrayList<CtdbLookup>();
            while (rs.next()) {
                CtdbLookup option = new CtdbLookup(rs.getInt("xattachmenttypeid"),
                        rs.getString("name"),
                        rs.getString("description"));
                options.add(option);
            }
            return options;
        }
        catch (SQLException e) {
            throw new CtdbException("Unable to get attachment types: " + e.getMessage(), e);
        }
        finally {
            this.close(rs);
            this.close(stmt);
        }
    }

    public List<CtdbLookup> getContactTypes(ResultControl rc) throws CtdbException {
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            StringBuffer sql = new StringBuffer(25);
            sql.append("select * from xexternalcontacttype ");
            sql.append(notNull(rc.getSearchClause()));
            sql.append(" order by xexternalcontacttypeid");
            stmt = this.conn.prepareStatement(sql.toString());

            rs = stmt.executeQuery();
            List<CtdbLookup> options = new ArrayList<CtdbLookup>();
            while (rs.next()) {
                CtdbLookup option = new CtdbLookup(rs.getInt("xexternalcontacttypeid"),
                        rs.getString("name"),
                        rs.getString("description"));
                options.add(option);
            }
            return options;
        }
        catch (SQLException e) {
            throw new CtdbException("Unable to get external contact types: " + e.getMessage(), e);
        }
        finally {
            this.close(rs);
            this.close(stmt);
        }
    }

    public StringBuffer getAllOccupationLookupsJS() throws CtdbException {
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            StringBuffer sql = new StringBuffer(25);
            sql.append("select * from xoccupation where parentid is not null order by xoccupationid");
            stmt = this.conn.prepareStatement(sql.toString());
            rs = stmt.executeQuery();
            return getOccupationJS(rs);
        }
        catch (SQLException e) {
            throw new CtdbException("Unable to getalloccupations: " + e.getMessage(), e);
        }
        finally {
            this.close(rs);
            this.close(stmt);
        }

    }


    public List<CtdbLookup> getProtocolDefaults(ResultControl rc) throws CtdbException {
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            StringBuffer sql = new StringBuffer(25);
            sql.append("select * from availiabledefaults ");
            sql.append(" order by 2");
            stmt = this.conn.prepareStatement(sql.toString());

            rs = stmt.executeQuery();
            List<CtdbLookup> options = new ArrayList<CtdbLookup>();
            while (rs.next()) {
                CtdbLookup option = new CtdbLookup(rs.getInt(1),
                        rs.getString(2));
                options.add(option);
            }
            return options;
        }
        catch (SQLException e) {
            throw new CtdbException("Unable to get protocol default optins: " + e.getMessage(), e);
        }
        finally {
            this.close(rs);
            this.close(stmt);
        }
    }


    private StringBuffer getOccupationJS(ResultSet rs) throws CtdbException {
        StringBuffer js = new StringBuffer(" var occupationOptions = new HashMap(); \n");

        try {
            int majorCategory = Integer.MIN_VALUE;
            int counter = 0;
            
            while (rs.next()) {
                if (rs.getInt("parentId") != majorCategory) {
                    if (counter != 0) {
                        js.append(" occupationOptions.put('").append(majorCategory).append("', optionsMap" + counter).append(");  \n");
                    }
                    majorCategory = rs.getInt("parentId");
                    // new category
                    counter++;
                    String curMapName = "optionsMap" + counter;
                    js.append("var " + curMapName).append(" = new HashMap(); \n");
                }
                js.append("optionsMap" + counter).append(".put ('" + rs.getString("xoccupationid") + "', '" + rs.getString("longname") + "'); \n");


            }
            js.append("occupationOptions.put ('").append(majorCategory).append("', optionsMap" + counter).append("); \n");
        } catch (SQLException sqle) {
            throw new CtdbException("Unable to generate occupatoin js : " + sqle + sqle.getMessage());
        }

        return js;
    }


    private String notNull(String s) {
        if (s == null) {
            return "";
        } else {
            return s;
        }
    }

    public List<CtdbLookup> getSecurityQuestions(ResultControl rc) throws CtdbException {
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            StringBuffer sql = new StringBuffer(25);
            sql.append("select * from xsecurityquestions ");
            sql.append(" order by 2");
            stmt = this.conn.prepareStatement(sql.toString());

            rs = stmt.executeQuery();
            List<CtdbLookup> options = new ArrayList<CtdbLookup>();
            while (rs.next()) {
                CtdbLookup option = new CtdbLookup(rs.getInt(1),
                        rs.getString(2));
                options.add(option);
            }
            return options;
        }
        catch (SQLException e) {
            throw new CtdbException("Unable to getsecurity questions optins: " + e.getMessage(), e);
        }
        finally {
            this.close(rs);
            this.close(stmt);
        }
    }

    public List<CtdbLookup> getFromTypes(ResultControl rc) throws CtdbException {
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            StringBuffer sql = new StringBuffer(25);
            sql.append("select * from xformtype ");
            sql.append(" order by ordervalue");
            stmt = this.conn.prepareStatement(sql.toString());

            rs = stmt.executeQuery();
            List<CtdbLookup> options = new ArrayList<CtdbLookup>();
            while (rs.next()) {
                CtdbLookup option = new CtdbLookup(rs.getInt(1),
                        rs.getString(2),
                        rs.getString(3));
                options.add(option);
            }
            return options;
        }
        catch (SQLException e) {
            throw new CtdbException("Unable to get test timepoints : " + e.getMessage(), e);
        }
        finally {
            this.close(rs);
            this.close(stmt);
        }
    }

    public List<CtdbLookup> getBtrisAccess(ResultControl rc) throws CtdbException {
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            StringBuffer sql = new StringBuffer(25);
            sql.append("select * from btris_access order by orderval ");
            //sql.append(" order by ordervalue");
            stmt = this.conn.prepareStatement(sql.toString());

            rs = stmt.executeQuery();
            List<CtdbLookup> options = new ArrayList<CtdbLookup>();
            while (rs.next()) {
                CtdbLookup option = new CtdbLookup(rs.getInt(1),
                        rs.getString(2));
                options.add(option);
            }
            return options;
        }
        catch (SQLException e) {
            throw new CtdbException("Unable to get btrisaccess : " + e.getMessage(), e);
        }
        finally {
            this.close(rs);
            this.close(stmt);
        }
    }
    
    public List<CtdbLookup> getIrbStatus(ResultControl rc) throws CtdbException {
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            StringBuffer sql = new StringBuffer(25);
            sql.append("select * from xirbstatus order by orderval ");
            stmt = this.conn.prepareStatement(sql.toString());

            rs = stmt.executeQuery();
            List<CtdbLookup> options = new ArrayList<CtdbLookup>();
            while (rs.next()) {
                CtdbLookup option = new CtdbLookup(rs.getInt(1),
                        rs.getString(2),
                        rs.getString(3));
                options.add(option);
            }
            return options;
        }
        catch (SQLException e) {
            throw new CtdbException("Unable to get irbstatutses : " + e.getMessage(), e);
        }
        finally {
            this.close(rs);
            this.close(stmt);
        }
    }
    
    public List<CtdbLookup> getPublicationType(ResultControl rc) throws CtdbException {
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            StringBuffer sql = new StringBuffer(25);
            sql.append("select * from xpublicationtype order by orderval ");
            stmt = this.conn.prepareStatement(sql.toString());

            rs = stmt.executeQuery();
            List<CtdbLookup> options = new ArrayList<CtdbLookup>();
            
            while (rs.next()) {
                CtdbLookup option = new CtdbLookup(rs.getInt("publicationtypeid"), rs.getString("name"), rs.getString("description"));
                options.add(option);
            }
            return options;
        }
        catch (SQLException e) {
            throw new CtdbException("Unable to get publications : " + e.getMessage(), e);
        }
        finally {
            this.close(rs);
            this.close(stmt);
        }
    }

    public List<CtdbLookup> getQaQueryClass(ResultControl rc) throws CtdbException {
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            StringBuffer sql = new StringBuffer(25);
            sql.append("select * from qaqueryclass order by orderval ");
            //sql.append(" order by ordervalue");
            stmt = this.conn.prepareStatement(sql.toString());

            rs = stmt.executeQuery();
            List<CtdbLookup> options = new ArrayList<CtdbLookup>();
            while (rs.next()) {
                CtdbLookup option = new CtdbLookup(rs.getInt(1),
                        rs.getString("shortname"),
                        rs.getString("longname"));
                options.add(option);
            }
            return options;
        }
        catch (SQLException e) {
            throw new CtdbException("Unable to get tubetypes : " + e.getMessage(), e);
        }
        finally {
            this.close(rs);
            this.close(stmt);
        }
    }


    public List<CtdbLookup> getQaQueryPriority(ResultControl rc) throws CtdbException {
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            StringBuffer sql = new StringBuffer(25);
            sql.append("select * from qaquerypriority order by orderval ");
            stmt = this.conn.prepareStatement(sql.toString());

            rs = stmt.executeQuery();
            List<CtdbLookup> options = new ArrayList<CtdbLookup>();
            while (rs.next()) {
                CtdbLookup option = new CtdbLookup(rs.getInt(1),
                        rs.getString("shortname"),
                        rs.getString("longname"));
                options.add(option);
            }
            return options;
        }
        catch (SQLException e) {
            throw new CtdbException("Unable to get qaquerypriority : " + e.getMessage(), e);
        }
        finally {
            this.close(rs);
            this.close(stmt);
        }
    }


    public List<CtdbLookup> getQaQueryResolution(ResultControl rc) throws CtdbException {
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            StringBuffer sql = new StringBuffer(25);
            sql.append("select * from qaQueryResolution order by orderval ");
            stmt = this.conn.prepareStatement(sql.toString());

            rs = stmt.executeQuery();
            List<CtdbLookup> options = new ArrayList<CtdbLookup>();
            while (rs.next()) {
                CtdbLookup option = new CtdbLookup(rs.getInt(1),
                        rs.getString("shortname"),
                        rs.getString("longname"));
                options.add(option);
            }
            return options;
        }
        catch (SQLException e) {
            throw new CtdbException("Unable to get qaQueryResolution : " + e.getMessage(), e);
        }
        finally {
            this.close(rs);
            this.close(stmt);
        }
    }


    public List<CtdbLookup> getQaQueryStatus(ResultControl rc) throws CtdbException {
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            StringBuffer sql = new StringBuffer(25);
            sql.append("select * from qaQueryStatus order by orderval ");
            stmt = this.conn.prepareStatement(sql.toString());

            rs = stmt.executeQuery();
            List<CtdbLookup> options = new ArrayList<CtdbLookup>();
            while (rs.next()) {
                CtdbLookup option = new CtdbLookup(rs.getInt(1),
                        rs.getString("shortname"),
                        rs.getString("longname"));
                options.add(option);
            }
            return options;
        }
        catch (SQLException e) {
            throw new CtdbException("Unable to get getQaQueryStatus : " + e.getMessage(), e);
        }
        finally {
            this.close(rs);
            this.close(stmt);
        }
    }


    public List<CtdbLookup> getQaQueryType(ResultControl rc) throws CtdbException {
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            StringBuffer sql = new StringBuffer(25);
            sql.append("select * from qaQueryType order by orderval ");
            //sql.append(" order by ordervalue");
            stmt = this.conn.prepareStatement(sql.toString());

            rs = stmt.executeQuery();
            List<CtdbLookup> options = new ArrayList<CtdbLookup>();
            while (rs.next()) {
                CtdbLookup option = new CtdbLookup(rs.getInt(1),
                        rs.getString("shortname"),
                        rs.getString("longname"));
                options.add(option);
            }
            return options;
        }
        catch (SQLException e) {
            throw new CtdbException("Unable to get getQaQueryType : " + e.getMessage(), e);
        }
        finally {
            this.close(rs);
            this.close(stmt);
        }
      }
    
	public List<CtdbLookup> getEBinderType(ResultControl rc) throws CtdbException
	{
		PreparedStatement stmt = null;
		ResultSet rs = null;
		CtdbLookup opt = null;
		List<CtdbLookup> options = new ArrayList<CtdbLookup>();
		
		String sql = "select * from xbindertype ";
		
		try
		{
			stmt = conn.prepareStatement(sql);
			rs = stmt.executeQuery();
			
			// Populate the options list
			while( rs.next() )
			{
				opt = new CtdbLookup();
				opt.setId(rs.getInt("xbindertypeid"));
				opt.setShortName(rs.getString("typename"));
				opt.setLongName(rs.getString("description"));
				
				options.add(opt);
			}
		}
		catch ( SQLException sqle )
		{
			throw new CtdbException("Unable to get the E-Binder type : " + sqle.getLocalizedMessage(), sqle);
		}
		finally
		{
			close(rs);
			close(stmt);
		}
		
		return options;
	}
}