package gov.nih.tbi.api.query.multitenant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TenantContext {
	private static Logger logger = LoggerFactory.getLogger(TenantContext.class);
	private static ThreadLocal<String> currentTenant = new InheritableThreadLocal<>();

	private TenantContext() {
	}

	public static String getCurrentTenant() {
		return currentTenant.get();
	}

	public static void setCurrentTenant(String tenant) {
		if (logger.isDebugEnabled()) {
			logger.debug(String.format("Setting tenant to %s", tenant));
		}
		currentTenant.set(tenant);
	}

	public static void clear() {
		currentTenant.set(null);
	}
}
