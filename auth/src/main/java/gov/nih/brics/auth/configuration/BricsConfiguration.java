package gov.nih.brics.auth.configuration;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.stereotype.Component;

@Component
public class BricsConfiguration {
	
	public static final String MICROSERVICES_DIRECTORY = "microservices";
	public static final String DATASOURCES_DIRECTORY = "datasources";

	private int accountExpirationDays;
	private String orgName;
	private int passwordBatchSize;
	private int maxIntervalFailCount;
	private int maxTotalFailCount;
	private long tempLockoutMinutes;
	private long tempLockoutIntervalWindow;
	private Long defaultDisease;
	
	@Value("${conf.dir}")
	private String confDir;
	
	@Value("${spring.application.name}")
	private String applicationName;
	
	@Value("${microservice.authentication.jwt.secret}")
	private String secretKey;
	
	@Value("${microservice.authentication.jwt.token-validity-in-milliseconds}")
	private long tokenValidityInMilliseconds;
	
	public void load(String tenantName) throws IOException {
		// not only do datasource properties change in multi-tenancy, but there are some instance properties that will as well
		// so reload those
		Resource serviceProperties = new FileSystemResource(confDir + File.separator + MICROSERVICES_DIRECTORY + File.separator + tenantName +  File.separator + applicationName + ".properties");
		Properties props = PropertiesLoaderUtils.loadProperties(serviceProperties);
		accountExpirationDays = Integer.valueOf(props.getProperty("brics.security.account.expirationDays"));
		orgName = props.getProperty("server.orgName");
		passwordBatchSize = Integer.valueOf(props.getProperty("brics.security.passwords.batchsize"));
		maxIntervalFailCount = Integer.valueOf(props.getProperty("brics.security.login.maxIntervalFailCount"));
		maxTotalFailCount = Integer.valueOf(props.getProperty("brics.security.login.maxTotalFailCount"));
		tempLockoutMinutes = Long.valueOf(props.getProperty("brics.security.login.tempLockoutMinutes"));
		tempLockoutIntervalWindow = Long.valueOf(props.getProperty("brics.security.login.tempLockoutIntervalWindow"));
		defaultDisease = Long.valueOf(props.getProperty("brics.security.defaultDisease"));
	}

	public int getAccountExpirationDays() {
		return accountExpirationDays;
	}

	public void setAccountExpirationDays(int accountExpirationDays) {
		this.accountExpirationDays = accountExpirationDays;
	}

	public String getSecretKey() {
		return secretKey;
	}

	public void setSecretKey(String secretKey) {
		this.secretKey = secretKey;
	}

	public long getTokenValidityInMilliseconds() {
		return tokenValidityInMilliseconds;
	}

	public void setTokenValidityInMilliseconds(long tokenValidityInMilliseconds) {
		this.tokenValidityInMilliseconds = tokenValidityInMilliseconds;
	}

	public String getOrgName() {
		return orgName;
	}

	public void setOrgName(String orgName) {
		this.orgName = orgName;
	}

	public int getPasswordBatchSize() {
		return passwordBatchSize;
	}

	public void setPasswordBatchSize(int passwordBatchSize) {
		this.passwordBatchSize = passwordBatchSize;
	}

	public int getMaxIntervalFailCount() {
		return maxIntervalFailCount;
	}

	public void setMaxIntervalFailCount(int maxIntervalFailCount) {
		this.maxIntervalFailCount = maxIntervalFailCount;
	}

	public int getMaxTotalFailCount() {
		return maxTotalFailCount;
	}

	public void setMaxTotalFailCount(int maxTotalFailCount) {
		this.maxTotalFailCount = maxTotalFailCount;
	}

	public long getTempLockoutMinutes() {
		return tempLockoutMinutes;
	}

	public void setTempLockoutMinutes(long tempLockoutMinutes) {
		this.tempLockoutMinutes = tempLockoutMinutes;
	}

	public long getTempLockoutIntervalWindow() {
		return tempLockoutIntervalWindow;
	}

	public void setTempLockoutIntervalWindow(long tempLockoutIntervalWindow) {
		this.tempLockoutIntervalWindow = tempLockoutIntervalWindow;
	}

	public Long getDefaultDisease() {
		return defaultDisease;
	}

	public void setDefaultDisease(Long defaultDisease) {
		this.defaultDisease = defaultDisease;
	}

	public String getConfDir() {
		return confDir;
	}

	public void setConfDir(String confDir) {
		this.confDir = confDir;
	}
}
