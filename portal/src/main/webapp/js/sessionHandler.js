/**
 * This file will pop up a confirmation dialog to notify the end user before the
 * session expires. If user chooses to continue the work, it will submit a dummy
 * server request to keep the session alive.
 */

/* global $, TemplateManager */
var timeoutThreshold = 30;	// Timeout threshold is in 30 minutes.
var notifyTime = 10;		// Notify user 10 minute before timeout
var startTime = (new Date()).getTime();
var notifyTimeoutId;		// This is the timeout ID that references the function that will show the warning.
var countdownIntervalId;	// Count down every 10 seconds to close out the warning dialog in time and alert user session has expired.  

$(document).ready(function() {
	// Start the a timeout function to check the session timeout and notify the user about the pending session expiration.
	clearTimeout(notifyTimeoutId);
	notifyTimeoutId = setTimeout(notifyUser, (timeoutThreshold - notifyTime)*60*1000);
	$("body").on("click", extendSession);
});


function extendSession() {
	clearInterval(countdownIntervalId); 
	
	// If user is doing action clicking and loading different pages extend
	// CAS/Application session to so we don't get 500 error
	// Call action instead of WS directly so that when in dictionary, both dictionary and portal servers are pinged
	$.ajax({
		url : "keepAlive!keepAlive.ajax",
	    success: function(data) {
	    	if (data == "success") {
				clearTimeout(notifyTimeoutId);
				startTime = (new Date()).getTime();
				notifyTimeoutId = setTimeout(notifyUser, (timeoutThreshold - notifyTime)*60*1000);
	    	} else {
	    		console.log("Couldn't extend the session timeout.");
	    		if (isSessionExpired()) {
	    			doLogout();
	    		}
	    	}
		}
	})
	.fail(function() {
		console.log("Couldn't extend the session timeout.");
		if (isSessionExpired()) {
			doLogout();
		}
	});
}

function notifyUser() {
	if (isSessionExpired()) {
		$("#sessionDiv").dialog("close");
		doLogout();
		
	} else {
		if ($("#sessionDiv").length == 0) {
			$("body").append('<div id="sessionDiv" style="display:none">Your session is about to expire, please click Ok button if you need more time.</div>');
			$("#sessionDiv").dialog({
				autoOpen: false,
				title: "Warning",
				resizable: false,
				height: 140,
				modal: true,
				buttons: {
					"Ok": function() {
						extendSession();
						$(this).dialog("close");
					},
					"Cancel": function() {
						doLogout();
					}
				},
				
				open: function() {
					$("body").off("click", extendSession);
					$(this).siblings().find(".ui-dialog-titlebar-close").hide();
				}
			});
		}
		
		var elapsedTime = (new Date()).getTime() - startTime;
		
		// Check if the notify threshold has been reached
		if (elapsedTime >= (timeoutThreshold - notifyTime)*60*1000) {
			$("#sessionDiv").dialog("open");
			
			// Check session expiration 10 seconds, while the dialog is open.
			countdownIntervalId = setInterval(function() { 
				if (isSessionExpired()) {
					doLogout();
				} 
			}, 10000);
		}
	}
}

function doLogout() {
	clearTimeout(notifyTimeoutId);
	clearInterval(countdownIntervalId); 
	window.location.replace(window.location.origin + "/portal/logout");
}

function isSessionExpired() {
	var currentTime = (new Date()).getTime();
	var elapsedTime = (currentTime - startTime) / 1000;	// Time elapsed since page was loaded in seconds
	
	if (elapsedTime > (timeoutThreshold * 60)) {
		console.log("Session expired!!!!");
		return true;
	} else {
		return false;
	}
}
