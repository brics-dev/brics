package gov.nih.brics.cas.logging;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import gov.nih.brics.cas.model.LimitedUser;

public class LimitedUserRowMapper implements RowMapper<LimitedUser> {
	
	private static final String COLUMN_USER_FIRSTNAME = "first_name";
	private static final String COLUMN_USER_LASTNAME = "last_name";
	private static final String COLUMN_USER_EMAIL = "email";
	private static final String COLUMN_ID = "account_id";
	private static final String COLUMN_USERNAME = "user_name";
	
	/**
	 * Performs the mapping between the resultSet and the LimitedUser object.
	 * 
	 * @param rs ResultSet response of one row from the database call
	 * @param rowNum index of the result
	 * @return LimitedUser resulting from the resultSet
	 * @throws SQLException if a SQLException is encountered getting
	 * column values (that is, there's no need to catch SQLException)
	 */
	@Override
	public LimitedUser mapRow(ResultSet rs, int rowNum) throws SQLException {
		LimitedUser user = new LimitedUser();
		user.setFirst_name(rs.getString(COLUMN_USER_FIRSTNAME));
		user.setLast_name(rs.getString(COLUMN_USER_LASTNAME));
		user.setEmail(rs.getString(COLUMN_USER_EMAIL));
		user.setId(rs.getLong(COLUMN_ID));
		user.setUsername(rs.getString(COLUMN_USERNAME));
		
		return user;
	}

}
