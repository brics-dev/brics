package gov.nih.brics.cas.config;

import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;

public class CasContextClosedHandler implements ApplicationListener<ContextClosedEvent> {
	private static final Logger logger = Logger.getLogger(CasContextClosedHandler.class.getName());

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
				logger.log(Level.WARNING, "Error deregistering driver: " + d.toString(), e);
			}
		}
	}

}
