/**
 * 
 */
let Identity;
export default Identity = {
	CLAIM_AUTHORITIES: "auth",
	CLAIM_ORG_ID: "org",
	CLAIM_USR_ID: "id",
	CLAIM_FULL_NAME: "fullName",
	CLAIM_EXPIRATION: "exp",
	CLAIM_USERNAME: "sub",
	
	GET_JWT_URL_TAIL : "/portal/repository/downloadQueueAction!getJwt.action",
	request_error_message: "Unable to authenticate to the backing service. Please log out and try again",
	
	config: {},
	jwtBody: {},
	jwt : "",
	
	initConfig : function(config) {
		this.config = config;
	},
	
	init : function(config) {
		this.initConfig(config);
		this.getFromServer();
	},
	
	load : function(strJwt) {
		this.jwt = strJwt;
		let jwtParts = strJwt.split(".");
		this.jwtBody = JSON.parse(atob(jwtParts[1]));
	},
	
	getClaim : function(claimName) {
		return this.jwtBody[claimName];
	},
	
	startup : function() {
		// TODO: handle renewing the JWT
	},
	
	getFromServer : function() {
		let getJwtUrl = 
				window.location.protocol + 
				"//" + 
				window.location.hostname + 
				":" + 
				window.location.port + 
				this.GET_JWT_URL_TAIL;

		let identity = this;
		$.ajax({
			url: getJwtUrl,
			cache: false,
			success : function(data, textStatus, xhr) {
				identity.load(data);
			},
			error: function(xhr, textStatus, errorThrown) {
				alert(identity.request_error_message);
			}
		});
	},
	
	/**
	 * Attempts to renew the JWT. Uses the success and/or error callback
	 * settings from the passed-in settings after this finishes.
	 */
	renew : function(settings) {
		// for BRICS session management.
		if (extendSession) {
			extendSession();
		}
		
		let renewUrl =
			window.location.protocol + 
			"//" + 
			window.location.hostname + 
			":" + 
			window.location.port + 
			this.GET_JWT_URL_TAIL;
		let renewSettings = {
			url: renewUrl,
			cache: false,
			success: function(data, textStatus, xhr) {
				if (xhr.status == 200) {
					Identity.load(data);
				}
				if (settings.success) {
					settings.success(data, textStatus, xhr);
				}
			},
			error: function(xhr, textStatus, errorThrown) {
				if (console) {
					console.log("failed to renew JWT from url " + renewUrl);
				}
				if (settings.error) {
					settings.error(xhr, textStatus, errorThrown);
				}
			}
		};
		$.ajax(renewSettings);
	},
	
	getExpiration : function() {
		return this.getClaim(Identity.CLAIM_EXPIRATION, true) * 1000;
	},
	
	getTimeUntilExpiration : function() {
		return this.getExpiration() - (new Date).getTime();
	},

	isExpired : function() {
		return (new Date).getTime() > this.getExpiration();
	},
	
	isAlmostExpired : function() {
		// default expiration buffer is 15 minutes 
		// (longer than the session expiration warning) = 900000ms
		let expirationBuffer = this.config.accessExpirationBuffer;
		if (!expirationBuffer) {
			expirationBuffer = 900000;
		}
		return (new Date).getTime() > this.getExpiration() - expirationBuffer;
	}
};