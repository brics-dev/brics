package gov.nih.cit.brics.file.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import gov.nih.cit.brics.file.exception.MultiTenantSetupException;
import gov.nih.cit.brics.file.multitenant.MultiTenantDataSource;

@Component
public class FileDataSourceUtil {
	private static final Logger logger = LoggerFactory.getLogger(FileDataSourceUtil.class);
	private static final String CONN_TEST_QUERY = "SELECT 1";

	public static final String DATASOURCE_DRIVER_CLASS_NAME = "spring.datasource.driver-class-name";
	public static final String DATASOURCE_URL_KEY = "spring.datasource.url";
	public static final String DATASOURCE_USERNAME_KEY = "spring.datasource.username";
	public static final String DATASOURCE_PASSWORD_KEY = "spring.datasource.password";

	public static final String HIKARI_MAX_POOL_SIZE_KEY = "spring.datasource.hikari.maximum-pool-size";
	public static final String HIKARI_IDLE_TIMEOUT_KEY = "spring.datasource.hikari.idle-timeout";
	public static final String HIKARI_MAX_LIFETIME_KEY = "spring.datasource.hikari.max-lifetime";

	public FileDataSourceUtil() {}

	/**
	 * Create a multi-tenant data source object for the specified database configuration.
	 * 
	 * @param confDirPath - The path to the root configuration directory.
	 * @param propFileName - The name of the property file containing the needed database configuration information.
	 * @param connectionPoolNameFormat - A string format for the name of each Hikari connection pool created.
	 * @return A MultiTenantDataSource object containing a collection of Hikari connection pools to databases of the
	 *         environment referenced the in root configuration directory.
	 * @throws MultiTenantSetupException When there is an error finding or reading property files from the given
	 *         configuration directory.
	 */
	public MultiTenantDataSource createMultiTenantDataSource(String confDirPath, String propFileName,
			String connectionPoolNameFormat) throws MultiTenantSetupException {
		// Construct the root configuration directory path and File object.
		String confRootPath = confDirPath + File.separator + FileRepositoryConstants.DATASOURCES_DIRECTORY;
		File confDir = new File(confRootPath);

		// Check if the config file directory is actually a directory and is readable.
		if (!confDir.isDirectory()) {
			logger.error("The root configuration directory ('{}') doesn't exist or is not a directory.", confRootPath);
			throw new MultiTenantSetupException("The configuration directory doesn't exist or is not a directory");
		} else if (!confDir.canRead()) {
			logger.error("The root configuration directory ('{}') is not readable.", confRootPath);
			throw new MultiTenantSetupException("The configuration directory is not readable.");
		}

		// Get a list of meta data source files.
		List<File> clientDirs = Arrays.asList(confDir.listFiles());
		List<File> propFiles = new ArrayList<>(clientDirs.size());

		for (File dir : clientDirs) {
			// Verify that the current file object is a directory
			if (dir.isDirectory()) {
				String propPath = dir.getAbsolutePath() + File.separator + propFileName;
				File propFile = new File(propPath);

				// Check if the file exists and is readable, then add it to the prop list if it is.
				if (propFile.isFile() && propFile.canRead()) {
					propFiles.add(propFile);
				} else {
					logger.warn("The following property file either doesn't exist or is not readable: {}.", propPath);
				}
			} else {
				logger.warn("The following client directory is not a directory: {}.", dir.getAbsolutePath());
			}
		}

		// Check if the list of property files is empty, and terminate the process if no prop files are found.
		if (propFiles.isEmpty()) {
			throw new MultiTenantSetupException("No data source property files were found.");
		}

		// Create a map of data sources.
		String defaultTenantId = null;
		Map<Object, Object> resolvedDataSources = new HashMap<>();

		for (File propFile : propFiles) {
			Properties properties = new Properties();

			// Load the properties from the prop file.
			try (FileInputStream inFile = new FileInputStream(propFile)) {
				properties.load(inFile);
			} catch (IOException e) {
				String msg = String.format("Error while reading properties from the following file: %1$s.",
						propFile.getAbsolutePath());

				throw new MultiTenantSetupException(msg, e);
			}

			// Create the Hikari config object for this tenant data source.
			String tenantId = propFile.getParentFile().getName();
			HikariConfig dsConfig = new HikariConfig();

			// Initialize the Hikari configuration object.
			dsConfig.setPoolName(String.format(connectionPoolNameFormat, tenantId));
			dsConfig.setConnectionTestQuery(CONN_TEST_QUERY);
			dsConfig.setDriverClassName(properties.getProperty(DATASOURCE_DRIVER_CLASS_NAME));
			dsConfig.setJdbcUrl(properties.getProperty(DATASOURCE_URL_KEY));
			dsConfig.setUsername(properties.getProperty(DATASOURCE_USERNAME_KEY));
			dsConfig.setPassword(properties.getProperty(DATASOURCE_PASSWORD_KEY));
			dsConfig.setMaximumPoolSize(Integer.valueOf(properties.getProperty(HIKARI_MAX_POOL_SIZE_KEY)));
			dsConfig.setIdleTimeout(Long.valueOf(properties.getProperty(HIKARI_IDLE_TIMEOUT_KEY)));
			dsConfig.setMaxLifetime(Long.valueOf(properties.getProperty(HIKARI_MAX_LIFETIME_KEY)));

			// Create and put the Hikari data source in the data source map.
			resolvedDataSources.put(tenantId, new HikariDataSource(dsConfig));

			// Check if the default tenant ID is set.
			if (defaultTenantId == null) {
				defaultTenantId = tenantId;
			}
		}

		// Create the final multi-tenant data source object.
		MultiTenantDataSource dataSource = new MultiTenantDataSource();
		dataSource.setDefaultTargetDataSource(resolvedDataSources.get(defaultTenantId));
		dataSource.setLenientFallback(false);
		dataSource.setTargetDataSources(resolvedDataSources);

		// Finalize the initialization of the mutil-tenant data source.
		dataSource.afterPropertiesSet();

		return dataSource;
	}
}
