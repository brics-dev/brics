package gov.nih.brics.auth.util;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import gov.nih.brics.auth.data.entity.UserToken;
import gov.nih.brics.auth.data.repository.SessionLogRepository;
import gov.nih.brics.auth.security.jwt.TokenProvider;
import gov.nih.brics.auth.service.AccountService;
import gov.nih.tbi.account.model.hibernate.Account;
import gov.nih.tbi.account.model.hibernate.SessionLog;

/**
 * Tracks who is logged in for the User Log audit logging and session tracking.
 * Uses 
 * 
 * @author jospark
 *
 */
@Component
@Scope("application")
public class SessionTracker {
	
	@Autowired
	TokenProvider tokenProvider;
	
	@Autowired
	AccountService accountService;
	
	@Autowired
	SessionLogRepository sessionLogRepository;
	
	@PostConstruct
	public void init() {
		activeUsers = new HashMap<>();
		expirations = new HashMap<>();
		
		expireDbSessions();
	}
	
	@Scheduled(fixedDelay=1800000)
	public void watchdog() {
		expireSessions();
	}
	
	// user ID, expiration date
	private HashMap<Long, Long> activeUsers;
	
	// expiration date, user IDs
	private HashMap<Long, Set<Long>> expirations;
	
	public void startSession(String token) {
		UserToken userToken = tokenProvider.parseToken(token);
		Long userId = userToken.getId();
		Long expiration = userToken.getTokenExpiration().getTime();
		if (getExpiration(userId) != null) {
			add(userId, expiration);
			
			// expire any previous db sessions for this user
			expireDbSessions(userId, expiration);
			
			startDbSession(userToken.getUsername());
		}
	}
	
	/**
	 * Used to explicitly remove a session from the tracker and log the session's end.
	 * Should only be used during an explicit logout or after verifying that the session
	 * has ended.
	 * 
	 * NOTE: the expiration time used here is NOW
	 * 
	 * @param token JWT token identifying user to log as out of the system
	 */
	public void endSessionNow(String token) {
		UserToken userToken = tokenProvider.parseToken(token);
		endSessionWithExpiration(userToken.getId(), (new Date()).getTime());
	}
	
	/**
	 * Used to explicitly remove a session from the tracker and log the session's end.
	 * Should only be used during an explicit logout or after verifying that the session
	 * has ended.
	 * 
	 * NOTE: just in case the user has multiple active sessions, this ends them all
	 * 
	 * @param userId ID of the user for whom to end the session
	 * @param expiration expiration unit timestamp (from Date().getTime())
	 */
	public void endSessionWithExpiration(Long userId, Long expiration) {
		expireDbSessions(userId, expiration);
		remove(userId);
	}
	
	/**
	 * Given a new token (with a different expiration than the already-tracked one), 
	 * update the tracking expiration time.
	 * 
	 * Does NOT change the database
	 * 
	 * @param token the NEW token after a token renewal
	 */
	public void updateSession(String token) {
		UserToken userToken = tokenProvider.parseToken(token);
		Long userId = userToken.getId();
		
		if (activeUsers.containsKey(userId)) {
			// do not update the db here
			Long expiration = userToken.getTokenExpiration().getTime();
			remove(userId);
			add(userId, expiration);
		}
	}
	
	/**
	 * Looks through the currently-active sessions to find expired ones (that are
	 * not updated).  If an active session is found, that session is ended with the 
	 * expiration time in the expirations list.
	 * 
	 * Used by the watchdog
	 */
	private void expireSessions() {
		Long now = (new Date()).getTime();
		for (Map.Entry<Long, Set<Long>> expirationGroup : expirations.entrySet()) {
			Long expiration = expirationGroup.getKey();
			if (expiration < now) {
				Set<Long> userIds = expirationGroup.getValue();
				for (Long userId : userIds) {
					endSessionWithExpiration(userId, expiration);
				}
			}
		}
	}
	
	/*
	 * Database interfaces
	 */
	
	/**
	 * Starts a session in the db.
	 * 
	 * @param username the user to start the session for
	 */
	private void startDbSession(String username) {
		Account account = accountService.findByUserName(username);
		SessionLog log = new SessionLog();
		log.setAccount(account);
		log.setEmail(account.getUser().getEmail());
		log.setFullName(account.getUser().getFullName());
		log.setTimeIn(new Date());
		log.setUsername(account.getUserName());
		sessionLogRepository.save(log);
	}
	
	/**
	 * Finds all non-expired entries in the database.  Sets those entries to expire
	 * now.
	 */
	private void expireDbSessions() {
		List<SessionLog> logs = sessionLogRepository.getAllActiveSessions();
		for (SessionLog log : logs) {
			log.setTimeOut(new Date());
		}
		sessionLogRepository.saveAll(logs);
	}
	
	/**
	 * Finds all non-expired entries in the database for a particular user.  Sets those
	 * entries to expire now.
	 * 
	 * @param userId
	 * @param expiration
	 */
	private void expireDbSessions(Long userId, Long expiration) {
		Account account = accountService.findByUserId(userId);
		if (account != null) {
			List<SessionLog> logs = sessionLogRepository.getActiveSessionsForUser(account.getUserName());
			for (SessionLog log : logs) {
				log.setTimeOut(new Date(expiration));
			}
			sessionLogRepository.saveAll(logs);
		}
	}
	
	/*
	 * Local Memory interfaces
	 */
	
	/**
	 * Adds a new entry to the local (in-memory) maps.
	 * Will remove previously-existing entries for the given userId if they exist.
	 * 
	 * @param userId User ID for the session to add to the maps
	 * @param expiration expiration time (in milliseconds since epoch)
	 */
	private void add(Long userId, Long expiration) {
		if (!activeUsers.containsKey(userId)) {
			// if entries already exist for this userId, this method will overwrite
			// those.  So, keep the old expiration so we can remove it from expirations
			Long oldExpiration = activeUsers.get(userId);
			// remove the old expiration from expirations
			if (oldExpiration != null) {
				removeExpiration(userId, oldExpiration);
			}
			
			activeUsers.put(userId, expiration);
			addExpiration(userId, expiration);
		}
	}
	
	/**
	 * Remove the entries in the local (in-memory) maps for the given userId.
	 * 
	 * @param userId the User ID for the session to remove from the maps
	 */
	private void remove(Long userId) {
		if (activeUsers.containsKey(userId)) {
			Long expiration = activeUsers.get(userId);
			activeUsers.remove(userId);
			removeExpiration(userId, expiration);
		}
	}
	
	/**
	 * Removes an expiration within the local (in-memory) map
	 * 
	 * @param userId
	 * @param expiration
	 */
	private void removeExpiration(Long userId, Long expiration) {
		if (expiration != null && userId != null) {
			Set<Long> expirationIds = expirations.get(expiration);
			// NOTE: these conditions are run in sequence so we first check, then remove,
			// then see if something was removed and, if so, check if the list is empty
			if (expirationIds != null && expirationIds.remove(userId) && expirationIds.isEmpty()) {
				expirations.remove(expiration);
			}
		}
	}
	
	/**
	 * Adds an expiration to the local (in-memory) map
	 * 
	 * @param userId
	 * @param expiration
	 */
	private void addExpiration(Long userId, Long expiration) {
		if (expirations.containsKey(expiration)) {
			expirations.get(expiration).add(userId);
		}
		else {
			HashSet<Long> userIds = new HashSet<>();
			userIds.add(userId);
			expirations.put(expiration, userIds);
		}
	}
	
	/**
	 * Gets expiration from the local (in-memory) map for the given user
	 * @param userId
	 * @return
	 */
	private Long getExpiration(Long userId) {
		return activeUsers.get(userId);
	}
}
