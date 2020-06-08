package gov.nih.cit.brics.file.multitenant;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

import com.zaxxer.hikari.HikariDataSource;

public class MultiTenantDataSource extends AbstractRoutingDataSource implements AutoCloseable {
	private List<HikariDataSource> resolvedDataSources;

    @Override
    protected Object determineCurrentLookupKey() {
        return TenantContext.getCurrentTenant();
    }

	@Override
	public void setTargetDataSources(Map<Object, Object> targetDataSources) {
		// Copy the data source map to the internal list.
		resolvedDataSources =
				targetDataSources.values().stream().map(v -> (HikariDataSource) v).collect(Collectors.toList());

		super.setTargetDataSources(targetDataSources);
	}

	@Override
	public void close() throws Exception {
		for (HikariDataSource dataSource : resolvedDataSources) {
			dataSource.close();
		}
	}

}