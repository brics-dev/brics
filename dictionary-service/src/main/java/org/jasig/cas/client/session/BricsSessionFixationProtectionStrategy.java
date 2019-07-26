package org.jasig.cas.client.session;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.cas.client.util.CommonUtils;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.security.web.authentication.session.SessionFixationProtectionEvent;
import org.springframework.util.Assert;
import org.springframework.web.util.WebUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The default implementation of {@link SessionAuthenticationStrategy} when using < Servlet 3.1.
 * <p>
 * Creates a new session for the newly authenticated user if they already have a session (as a
 * defence against session-fixation protection attacks), and copies their session attributes across
 * to the new session. The copying of the attributes can be disabled by setting
 * {@code migrateSessionAttributes} to {@code false} (note that even in this case, internal Spring
 * Security attributes will still be migrated to the new session).
 * <p>
 * This approach will only be effective if your servlet container always assigns a new session Id
 * when a session is invalidated and a new session created by calling
 * {@link HttpServletRequest#getSession()}.
 * <p>
 * <h3>Issues with {@code HttpSessionBindingListener}</h3>
 * <p>
 * The migration of existing attributes to the newly-created session may cause problems if any of
 * the objects implement the {@code HttpSessionBindingListener} interface in a way which makes
 * assumptions about the life-cycle of the object. An example is the use of Spring session-scoped
 * beans, where the initial removal of the bean from the session will cause the
 * {@code DisposableBean} interface to be invoked, in the assumption that the bean is no longer
 * required.
 * <p>
 * We'd recommend that you take account of this when designing your application and do not store
 * attributes which may not function correctly when they are removed and then placed back in the
 * session. Alternatively, you should customize the {@code SessionAuthenticationStrategy} to deal
 * with the issue in an application-specific way.
 *
 * @author Luke Taylor
 * @since 3.0
 */
public class BricsSessionFixationProtectionStrategy implements SessionAuthenticationStrategy, ApplicationEventPublisherAware {
	protected final Log logger = LogFactory.getLog(this.getClass());
	private SessionMappingStorage sessionMappingStorage;
	/**
	 * Used for publishing events related to session fixation protection, such as
	 * {@link SessionFixationProtectionEvent}.
	 */
	private ApplicationEventPublisher applicationEventPublisher = new NullEventPublisher();
	/**
	 * If set to {@code true}, a session will always be created, even if one didn't exist at the
	 * start of the request. Defaults to {@code false}.
	 */
	private boolean alwaysCreateSession;
	/**
	 * Indicates that the session attributes of an existing session should be migrated to the new
	 * session. Defaults to <code>true</code>.
	 */
	boolean migrateSessionAttributes = true;

	/**
	 * In the case where the attributes will not be migrated, this field allows a list of named
	 * attributes which should <em>not</em> be discarded.
	 */
	private List<String> retainedAttributes = null;

	/**
	 * Called to extract the existing attributes from the session, prior to invalidating it. If
	 * {@code migrateAttributes} is set to {@code false}, only Spring Security attributes will be
	 * retained. All application attributes will be discarded.
	 * <p>
	 * You can override this method to control exactly what is transferred to the new session.
	 *
	 * @param session the session from which the attributes should be extracted
	 * @return the map of session attributes which should be transferred to the new session
	 */
	protected Map<String, Object> extractAttributes(HttpSession session) {
		return createMigratedAttributeMap(session);
	}

	/**
	 * Called when a user is newly authenticated.
	 * <p>
	 * If a session already exists, and matches the session Id from the client, a new session will
	 * be created, and the session attributes copied to it (if {@code migrateSessionAttributes} is
	 * set). If the client's requested session Id is invalid, nothing will be done, since there is
	 * no need to change the session Id if it doesn't match the current session.
	 * <p>
	 * If there is no session, no action is taken unless the {@code alwaysCreateSession} property is
	 * set, in which case a session will be created if one doesn't already exist.
	 */
	public void onAuthentication(Authentication authentication, HttpServletRequest request,
			HttpServletResponse response) {
		boolean hadSessionAlready = request.getSession(false) != null;

		if (!hadSessionAlready && !alwaysCreateSession) {
			// Session fixation isn't a problem if there's no session

			return;
		}

		// Create new session if necessary
		HttpSession session = request.getSession();

		if (hadSessionAlready && request.isRequestedSessionIdValid()) {

			String originalSessionId;
			String newSessionId;
			Object mutex = WebUtils.getSessionMutex(session);
			synchronized (mutex) {
				// We need to migrate to a new session
				originalSessionId = session.getId();

				session = applySessionFixation(request);
				newSessionId = session.getId();
			}

			if (originalSessionId.equals(newSessionId)) {
				logger.warn(
						"Your servlet container did not change the session ID when a new session was created. You will"
								+ " not be adequately protected against session-fixation attacks");
			}

			onSessionChange(originalSessionId, session, authentication);
		}
	}

	/**
	 * Called when the session has been changed and the old attributes have been migrated to the new
	 * session. Only called if a session existed to start with. Allows subclasses to plug in
	 * additional behaviour. *
	 * <p>
	 * The default implementation of this method publishes a {@link SessionFixationProtectionEvent}
	 * to notify the application that the session ID has changed. If you override this method and
	 * still wish these events to be published, you should call {@code super.onSessionChange()}
	 * within your overriding method.
	 *
	 * @param originalSessionId the original session identifier
	 * @param newSession the newly created session
	 * @param auth the token for the newly authenticated principal
	 */
	protected void onSessionChange(String originalSessionId, HttpSession newSession, Authentication auth) {
		applicationEventPublisher
				.publishEvent(new SessionFixationProtectionEvent(auth, originalSessionId, newSession.getId()));
	}

	final HttpSession applySessionFixation(HttpServletRequest request) {
		HttpSession session = request.getSession();
		String originalSessionId = session.getId();
		if (logger.isDebugEnabled()) {
			logger.debug("Invalidating session with Id '" + originalSessionId + "' "
					+ (migrateSessionAttributes ? "and" : "without") + " migrating attributes.");
		}

		Map<String, Object> attributesToMigrate = extractAttributes(session);

		session.invalidate();
		session = request.getSession(true); // we now have a new session

		if (logger.isDebugEnabled()) {
			logger.debug("Started new session: " + session.getId());
		}

		transferAttributes(attributesToMigrate, session);

		// my stuff
		logger.debug("Custom listener! Old ID: " + originalSessionId + " New ID: " + session.getId());
		if (sessionMappingStorage == null) {
			sessionMappingStorage = SingleSignOutHttpSessionListener.getSessionMappingStorage();
		}

		String ticket = CommonUtils.safeGetParameter(request, "ticket");
		logger.debug("Ticket discovered: " + ticket);
		if (ticket == null || ticket.equals("")) {
			logger.error(
					"Unable to retrieve ST/PT from request. This should not happen because Fixation should only occur after a successful cas authentication (with a ST/PT)");
			return session;
		}
		logger.debug("Removing old session via session id if there is one.");
		sessionMappingStorage.removeBySessionById(originalSessionId);
		logger.debug("Adding new session and linking it to our ST");
		sessionMappingStorage.addSessionById(ticket, session);

		return session;
	}

	/**
	 * @param attributes the attributes which were extracted from the original session by
	 *        {@code extractAttributes}
	 * @param newSession the newly created session
	 */
	void transferAttributes(Map<String, Object> attributes, HttpSession newSession) {
		if (attributes != null) {
			for (Map.Entry<String, Object> entry : attributes.entrySet()) {
				newSession.setAttribute(entry.getKey(), entry.getValue());
			}
		}
	}

	@SuppressWarnings("unchecked")
	private HashMap<String, Object> createMigratedAttributeMap(HttpSession session) {
		HashMap<String, Object> attributesToMigrate = null;

		if (migrateSessionAttributes || retainedAttributes == null) {
			attributesToMigrate = new HashMap<String, Object>();

			Enumeration enumer = session.getAttributeNames();

			while (enumer.hasMoreElements()) {
				String key = (String) enumer.nextElement();
				if (!migrateSessionAttributes && !key.startsWith("SPRING_SECURITY_")) {
					// Only retain Spring Security attributes
					continue;
				}
				attributesToMigrate.put(key, session.getAttribute(key));
			}
		} else {
			// Only retain the attributes which have been specified in the retainAttributes list
			if (!retainedAttributes.isEmpty()) {
				attributesToMigrate = new HashMap<String, Object>();
				for (String name : retainedAttributes) {
					Object value = session.getAttribute(name);

					if (value != null) {
						attributesToMigrate.put(name, value);
					}
				}
			}
		}
		return attributesToMigrate;
	}

	/**
	 * Defines whether attributes should be migrated to a new session or not. Has no effect if you
	 * override the {@code extractAttributes} method.
	 * <p>
	 * Attributes used by Spring Security (to store cached requests, for example) will still be
	 * retained by default, even if you set this value to {@code false}.
	 *
	 * @param migrateSessionAttributes whether the attributes from the session should be transferred
	 *        to the new, authenticated session.
	 */
	public void setMigrateSessionAttributes(boolean migrateSessionAttributes) {
		this.migrateSessionAttributes = migrateSessionAttributes;
	}

	/**
	 * @deprecated Override the {@code extractAttributes} method instead
	 */
	@Deprecated
	public void setRetainedAttributes(List<String> retainedAttributes) {
		Assert.notNull(retainedAttributes);
		this.retainedAttributes = retainedAttributes;
	}

	/**
	 * Sets the {@link ApplicationEventPublisher} to use for submitting
	 * {@link SessionFixationProtectionEvent}. The default is to not submit the
	 * {@link SessionFixationProtectionEvent}.
	 *
	 * @param applicationEventPublisher the {@link ApplicationEventPublisher}. Cannot be null.
	 */
	public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
		Assert.notNull(applicationEventPublisher, "applicationEventPublisher cannot be null");
		this.applicationEventPublisher = applicationEventPublisher;
	}

	public void setAlwaysCreateSession(boolean alwaysCreateSession) {
		this.alwaysCreateSession = alwaysCreateSession;
	}

	protected static final class NullEventPublisher implements ApplicationEventPublisher {

		@Override
		public void publishEvent(ApplicationEvent event) {
		}

		@Override
		public void publishEvent(Object event) {
		}
		
	}
}
