package gov.nih.nichd.ctdb.config;

import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Enumeration;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;

import com.zaxxer.hikari.HikariDataSource;

public class ProformsContextClosedHandler implements ApplicationListener<ContextClosedEvent> {
	private static final Logger logger = Logger.getLogger(ProformsContextClosedHandler.class);

	@Autowired
	HikariDataSource mainDataSource;

	@Override
	public void onApplicationEvent(ContextClosedEvent event) {
		// Check if the database data source is closed.
		if (!mainDataSource.isClosed()) {
			logger.info("Shutting down the database data source...");
			mainDataSource.close();
		}

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
