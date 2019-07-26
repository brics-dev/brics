
package gov.nih.tbi.portal.listeners;

import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import org.apache.log4j.Logger;

public class MySessionListener implements HttpSessionListener {

	private static Logger logger = Logger.getLogger(MySessionListener.class);
	private static int activeSessions = 0;

	public void sessionCreated(HttpSessionEvent se) {

		activeSessions++;
	}

	public void sessionDestroyed(HttpSessionEvent se) {

		HttpSession session = se.getSession();

		activeSessions--;

		long currentTime = System.currentTimeMillis();
		long lastAccessed = session.getLastAccessedTime();
		long timeElapsed = (currentTime - lastAccessed) / (1000 * 60);

		long usedMemory = (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / (1024 * 1024);

		if (logger.isDebugEnabled()) {
			logger.debug(
					"NOTE!!! Session destroyed: ID=" + session.getId() + " timeElapsed in minutes is " + timeElapsed);
			logger.debug("Used memory " + usedMemory + " MB, number of active sessions" + activeSessions);
		}
	}
}
