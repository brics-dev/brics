package gov.nih.tbi.api.query.service.model;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import gov.nih.tbi.api.query.data.entity.UserToken;
import gov.nih.tbi.service.model.PermissionModel;

/**
 * Singleton object that holds a map of user's permission model in the application scope, it should be cleared out on a
 * regular basis to keep its content up-to-date. Key: userName, value: PermissionModel as in Query Tool
 * 
 * @author jim3
 */
@Component
@Scope("application")
public class UserPermissionCache implements Serializable {

	private static final long serialVersionUID = -4553661287112339286L;
	private static final Logger log = LogManager.getLogger(UserPermissionCache.class);

	private static final Map<UserToken, PermissionModel> userPermissionMap =
			new ConcurrentHashMap<UserToken, PermissionModel>();
	
	private UserPermissionCache() {}

	public static synchronized boolean userPermissionExists(UserToken userToken) {
		return userPermissionMap.containsKey(userToken);
	}

	public static synchronized PermissionModel getUserPermissionModel(UserToken userToken) {
		return userPermissionMap.get(userToken);
	}

	public static synchronized void addUserPermission(UserToken userToken, PermissionModel permissionModel) {
		// Remove the other userToken(s) from the same user. Otherwise we end up with a bunch of user tokens from the
		// same user.
		Set<UserToken> sameUsers = new HashSet<>();

		for (UserToken currentToken : userPermissionMap.keySet()) {
			if (currentToken.sameUser(userToken)) {
				sameUsers.add(currentToken);
			}
		}

		for (UserToken sameUser : sameUsers) {
			userPermissionMap.remove(sameUser);
		}

		userPermissionMap.put(userToken, permissionModel);
	}

	public static synchronized void clearCache() {
		userPermissionMap.clear();
		log.info("The user permission cache has been cleared");
	}

	public static synchronized void removeUserToken(UserToken userToken) {
		userPermissionMap.remove(userToken);
	}
}
