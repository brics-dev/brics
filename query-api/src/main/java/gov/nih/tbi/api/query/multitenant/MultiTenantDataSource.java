package gov.nih.tbi.api.query.multitenant;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

public class MultiTenantDataSource extends AbstractRoutingDataSource {

	@Override
	public Connection getConnection() throws SQLException {
		DataSource ds = determineTargetDataSource();
		return ds.getConnection();
	}

	@Override
	protected Object determineCurrentLookupKey() {
		return TenantContext.getCurrentTenant();
	}
}