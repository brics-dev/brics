package gov.nih.tbi.config;

import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Enumeration;

import org.apache.log4j.Logger;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.stereotype.Component;

@Component
public class QueryToolContextClosedHandler implements ApplicationListener<ContextClosedEvent> {
	private static final Logger logger = Logger.getLogger(QueryToolContextClosedHandler.class);

	@Override
	public void onApplicationEvent(ContextClosedEvent event) {
		// Unregister all JDBC drivers.
		logger.info("Unregistering all JDBC drivers...");

		Enumeration<Driver> drivers = DriverManager.getDrivers();

		while (drivers.hasMoreElements()) {
			Driver d = drivers.nextElement();

			try {
				DriverManager.deregisterDriver(d);
				logger.info("Deregistered JDBC driver: " + d.toString());
			} catch (SQLException e) {
				logger.warn("Error deregistering driver: " + d.toString(), e);
			}
		}
	}

}
