package gov.nih.cit.brics.file.util;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.SessionScope;

@Component
@SessionScope
public class FileRepositoryConstants {
	private static final String FILE_SHARE_ROOT_KEY = "fileRepo.system.fileShare.root";
	private static final String LEGACY_FILE_SHARE_ROOT_KEY = "fileRepo.system.legacy.fileShare.root";

	public static final String FILE_API_ROOT = "/files/";
	public static final String MICROSERVICES_DIRECTORY = "microservices";
	public static final String DATASOURCES_DIRECTORY = "datasources";
	public static final String PROP_FILE_EXTENSION = ".properties";
	public static final long DOWNLOAD_CHUNK_SIZE = 10485760L; // 10 MB

	@Value("${conf.dir}")
	private String confDir;

	@Value("${spring.application.name}")
	private String applicationName;

	@Value("${fileRepo.system.fileId.length}")
	private Integer fileIdLength;

	// Tenant Specific Properties
	private String fileShareRoot;
	private String legacyFileShareRoot;

	/**
	 * Loads or refreshes the tenant specific properties from the tenant's property file.
	 * 
	 * @param tenantName - The target tenant to load properties for.
	 * @throws IOException When there is an error reading data from the tenant's property file.
	 */
	public void load(String tenantName) throws IOException {
		Resource serviceProperties = new FileSystemResource(confDir + File.separator + MICROSERVICES_DIRECTORY
				+ File.separator + tenantName + File.separator + applicationName + PROP_FILE_EXTENSION);
		Properties props = PropertiesLoaderUtils.loadProperties(serviceProperties);

		// Set the tenant specific properties.
		fileShareRoot = props.getProperty(FILE_SHARE_ROOT_KEY);
		legacyFileShareRoot = props.getProperty(LEGACY_FILE_SHARE_ROOT_KEY);
	}

	public String getConfDir() {
		return confDir;
	}

	public String getApplicationName() {
		return applicationName;
	}

	public String getFileShareRoot() {
		return fileShareRoot;
	}

	public String getLegacyFileShareRoot() {
		return legacyFileShareRoot;
	}

	public Integer getFileIdLength() {
		return fileIdLength;
	}

}
