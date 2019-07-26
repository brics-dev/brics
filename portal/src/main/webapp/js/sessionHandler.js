/**
 * This file will pop up a confirmation dialog to notify the end user before the
 * session expires. If user chooses to continue the work, it will submit a dummy
 * server request to keep the session alive.
 */

/* global $, TemplateManager */
var timeoutThreshold = 30;	// Timeout threshold is in minutes.
var notifyThreshold = timeoutThreshold - 10;  // Notify user 10 minute before
												// timeout.
var environment = window.location.origin; // To detect whether local or
											// server(dev/stage/demo/uat/prod)
											// and direct to logout url
var startTime = (new Date()).getTime();
var notifyTimeoutId;		// This is the timeout ID that references the
							// function that will show the warning.
var countdownIntervalId;	// Interval ID that references a function that will
							// periodically check if the session is expired.

$(document).ready(function() {
	// Start the a timeout function to check the session timeout and notify the
	// user about the pending session expiration.
	var notifyTime = notifyThreshold * 60 * 1000;
	notifyTimeoutId = setTimeout(checkNotifyUser, notifyTime);
	$("body").on("click", extendSession);
});

function extendSession() {
	// If user is doing action clicking and loading different pages extend
	// CAS/Application session to so we don't get 500 error
	$.ajax({
		url : "/portal/ws/account/account/keepAlive",
		success : function() {
			clearTimeout(notifyTimeoutId);
			startTime = (new Date()).getTime();
			
			// Reset the session expiration notification display.
			var notifyTime = startTime + (notifyThreshold * 60 * 1000);
			var elapsedTime = notifyTime - startTime;
			
			notifyTimeoutId = setTimeout(checkNotifyUser, elapsedTime);
		}
	})
	.fail(function() {
		clearTimeout(notifyTimeoutId);
		console.log("Couldn't extend the session timeout. Resetting the notification function.");
		
		if ( !isSessionExpired() ) {
			// Reset the notification display.
			var notifyTime = startTime + (notifyThreshold * 60 * 1000);
			var currentTime = (new Date()).getTime();
			var elapsedTime = notifyTime - currentTime;
			
			// Check if there is still time before a notification is needed.
			if ( elapsedTime > 0 ) {
				notifyTimeoutId = setTimeout(checkNotifyUser, elapsedTime);
			}
			// The notification threshold has already passed. Notify the user
			// now.
			else {
				notifyTimeoutId = setTimeout(checkNotifyUser, 0);
			}
		}
		else {
			doLogout();
		}
	});
}

function checkNotifyUser() {
	if (!isSessionExpired()) {
		// Check if the notify div is on the page, and create it if it is not.
		if ( $("#sessionDiv").length == 0 ) {
			$("body").append('<div id="sessionDiv" style="display:none">Your session is about to expire, please click Ok button if you need more time.</div>');
			$("#sessionDiv").dialog({
				autoOpen: false,
				title: "Warning",
				resizable: false,
				height: 140,
				modal: true,
				buttons: {
					"Ok": function() {
						clearInterval(countdownIntervalId);
						extendSession();
						$("body").on("click", extendSession);
						$(this).dialog("close");
					},
					"Cancel": function() {
						clearInterval(countdownIntervalId); 
						doLogout();
					}
				},
				
				open: function() {
					$("body").off("click", extendSession);
					$(this).siblings().find(".ui-dialog-titlebar-close").hide();
				}
			});
		}
		
		var currentTime = (new Date()).getTime();
		var elapsedTime = (currentTime - startTime) / 1000;
		
		// Check if the notify threshold has been reached, since the start time
		// could have been reset.
		if (elapsedTime >= (notifyThreshold * 60)) {
			// Warn the user that their session is about to expire.
			$("#sessionDiv").dialog("open");
			
			// Check session expiration 10 seconds, while the dialog is open.
			countdownIntervalId = setInterval(function() {
				// If the session has expired, force a logout.
				if (isSessionExpired()) {
					clearInterval(countdownIntervalId); 
					doLogout();
				} 
			}, 10000);
		}
	}
	// Session has expired force a log out.
	else {
		clearInterval(countdownIntervalId);
		doLogout();
	}
}

function doLogout() {
	if (window.location.href.indexOf("local") > -1 || window.location.href.indexOf("127") > -1) {
		window.location.replace(environment + "/portal/jsp/login.jsp");
	} else {
		window.location.replace(environment + "/portal/logout");
    }
}

function isSessionExpired() {
	var currentTime = (new Date()).getTime();
	var elapsedTime = (currentTime - startTime) / 1000;	// Time elapsed since
														// page was loaded in
														// seconds
	
	if (elapsedTime > (timeoutThreshold * 60)) {
		return true;
	} else {
		return false;
	}
}
