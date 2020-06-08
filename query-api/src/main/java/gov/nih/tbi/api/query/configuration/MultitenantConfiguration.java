package gov.nih.tbi.api.query.configuration;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import gov.nih.tbi.api.query.multitenant.MultiTenantDataSource;
import gov.nih.tbi.api.query.multitenant.MultiTenantMetaDataCache;
import gov.nih.tbi.service.cache.QueryMetaDataCache;
import gov.nih.tbi.service.model.MetaDataCache;

/**
 * 
 * This class is largely inspired by
 * https://fizzylogic.nl/2016/01/24/make-your-spring-boot-application-multi-tenant-aware-in-2-steps/ and intended to
 * allow for multiple database connections per application
 *
 */
@Configuration
public class MultitenantConfiguration {
	public static final String META_FILENAME = "meta.properties";
	public static final String QT_RDF_FILENAME = "qt-rdf.properties";
	public static final String DEFAULT_TENANT_ID = "fitbir";

	@Autowired
	private DataSourceProperties properties;

	@Autowired
	private BricsConfiguration config;

	@Primary
	@Bean
	public MetaDataCache metaDataCache() {
		MultiTenantMetaDataCache multiTenantCache = new MultiTenantMetaDataCache();

		String path = config.getConfDir() + File.separator + BricsConfiguration.DATASOURCES_DIRECTORY;
		File parentDirectory = Paths.get(path).toFile();
		// should only be directories in there, but we'll check later
		File[] clientFolders = parentDirectory.listFiles();
		File[] files = new File[clientFolders.length];
		for (int i = 0; i < clientFolders.length; i++) {
			File clientFolder = clientFolders[i];
			if (clientFolder.isDirectory()) {
				files[i] = new File(clientFolder.getPath() + File.separator + QT_RDF_FILENAME);
			}
		}

		for (File propertyFile : files) {
			if (propertyFile != null) {
				String tenantId = propertyFile.getParentFile().getName();
				MetaDataCache newCache = new QueryMetaDataCache();
				multiTenantCache.putMetaDataCache(tenantId, newCache);
			}
		}

		return multiTenantCache;
	}

	@Primary
	@Bean
	@Qualifier("rdfConnection")
	public DataSource rdfConnection() {
		String path = config.getConfDir() + File.separator + BricsConfiguration.DATASOURCES_DIRECTORY;
		File parentDirectory = Paths.get(path).toFile();
		// should only be directories in there, but we'll check later
		File[] clientFolders = parentDirectory.listFiles();
		File[] files = new File[clientFolders.length];
		for (int i = 0; i < clientFolders.length; i++) {
			File clientFolder = clientFolders[i];
			if (clientFolder.isDirectory()) {
				files[i] = new File(clientFolder.getPath() + File.separator + QT_RDF_FILENAME);
			}
		}

		Map<Object, Object> resolvedDataSources = new HashMap<>();

		for (File propertyFile : files) {
			if (propertyFile != null) {
				Properties tenantProperties = new Properties();
				@SuppressWarnings("unchecked")
				DataSourceBuilder<MultiTenantDataSource> dataSourceBuilder =
						(DataSourceBuilder<MultiTenantDataSource>) DataSourceBuilder
								.create(this.getClass().getClassLoader());

				try {
					tenantProperties.load(new FileInputStream(propertyFile));
					String tenantId = propertyFile.getParentFile().getName();

					// Assumption: The tenant database uses the same driver class
					// as the default database that you configure.
					dataSourceBuilder.driverClassName(properties.getDriverClassName())
							.url(tenantProperties.getProperty("rdfconnection.jdbcUrl"))
							.username(tenantProperties.getProperty("rdfconnection.username"))
							.password(tenantProperties.getProperty("rdfconnection.password"));

					if (properties.getType() != null) {
						dataSourceBuilder.type(properties.getType());
					}

					resolvedDataSources.put(tenantId, dataSourceBuilder.build());
				} catch (IOException e) {
					// Ooops, tenant could not be loaded. This is bad.
					// Stop the application!
					e.printStackTrace();
					return null;
				}
			}
		}

		// Create the final multi-tenant source.
		// It needs a default database to connect to.
		// Make sure that the default database is actually an empty tenant database.
		// Don't use that for a regular tenant if you want things to be safe!
		MultiTenantDataSource dataSource = new MultiTenantDataSource();
		dataSource.setDefaultTargetDataSource(resolvedDataSources.get(DEFAULT_TENANT_ID));
		dataSource.setLenientFallback(false);
		dataSource.setTargetDataSources(resolvedDataSources);

		// Call this to finalize the initialization of the data source.
		dataSource.afterPropertiesSet();

		return dataSource;
	}

	/**
	 * Defines the data source for the application
	 * 
	 * @return
	 */
	@Bean
	@Qualifier("metaConnection")
	@ConfigurationProperties(prefix = "spring.datasource")
	public DataSource dataSource() {
		String path = config.getConfDir() + File.separator + BricsConfiguration.DATASOURCES_DIRECTORY;
		File parentDirectory = Paths.get(path).toFile();
		// should only be directories in there, but we'll check later
		File[] clientFolders = parentDirectory.listFiles();
		File[] files = new File[clientFolders.length];
		for (int i = 0; i < clientFolders.length; i++) {
			File clientFolder = clientFolders[i];
			if (clientFolder.isDirectory()) {
				files[i] = new File(clientFolder.getPath() + File.separator + META_FILENAME);
			}
		}

		Map<Object, Object> resolvedDataSources = new HashMap<>();

		for (File propertyFile : files) {
			if (propertyFile != null) {
				Properties tenantProperties = new Properties();
				@SuppressWarnings("unchecked")
				DataSourceBuilder<MultiTenantDataSource> dataSourceBuilder =
						(DataSourceBuilder<MultiTenantDataSource>) DataSourceBuilder
								.create(this.getClass().getClassLoader());

				try {
					tenantProperties.load(new FileInputStream(propertyFile));
					String tenantId = propertyFile.getParentFile().getName();

					// Assumption: The tenant database uses the same driver class
					// as the default database that you configure.
					dataSourceBuilder.driverClassName(properties.getDriverClassName())
							.url(tenantProperties.getProperty("spring.datasource.url"))
							.username(tenantProperties.getProperty("spring.datasource.username"))
							.password(tenantProperties.getProperty("spring.datasource.password"));

					if (properties.getType() != null) {
						dataSourceBuilder.type(properties.getType());
					}

					resolvedDataSources.put(tenantId, dataSourceBuilder.build());
				} catch (IOException e) {
					// Ooops, tenant could not be loaded. This is bad.
					// Stop the application!
					e.printStackTrace();
					return null;
				}
			}
		}

		// Create the final multi-tenant source.
		// It needs a default database to connect to.
		// Make sure that the default database is actually an empty tenant database.
		// Don't use that for a regular tenant if you want things to be safe!
		MultiTenantDataSource dataSource = new MultiTenantDataSource();
		dataSource.setDefaultTargetDataSource(resolvedDataSources.get(DEFAULT_TENANT_ID));
		dataSource.setLenientFallback(false);
		dataSource.setTargetDataSources(resolvedDataSources);

		// Call this to finalize the initialization of the data source.
		dataSource.afterPropertiesSet();

		return dataSource;
	}
}
