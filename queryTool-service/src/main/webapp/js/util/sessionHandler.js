/**
 * This file will pop up a confirmation dialog to notify the end user ten minute 
 * before the session expires.  If user chooses to continue the work, it will 
 * submit a dummy server request to keep the session alive.
 */

var timeoutThreshold = 30;	// Timeout threshold is 30 min
var notifyTime = 10;		// Notify user 10 minute before timeout
var startTime = (new Date()).getTime();
var notifyTimeoutId;		// This is the timeout that we send out the warning
var countdownIntervalId;	// Count down every 10 seconds to close out the warning dialog in time and alert user session has expired.  

$(document).ready(function() {
	clearTimeout(notifyTimeoutId);
	notifyTimeoutId = setTimeout(notifyUser, (timeoutThreshold - notifyTime)*60*1000);
	$("body").on('click', extendSession);
});

function extendSession() {
	//If user is doing action clicking and loading different pages extend CAS/Application session so we don't get 500 error
 	$.ajax({
 		url: "service/keepAlive",
 		async: true
 	}).done(function(data, textStatus, xhr) {
 		if (xhr.status == 200) {
	 		clearInterval(countdownIntervalId); 
	 		clearTimeout(notifyTimeoutId);
	 		startTime = (new Date()).getTime();
	 		notifyTimeoutId = setTimeout(notifyUser, (timeoutThreshold - notifyTime)*60*1000);
	 		
 		} else {
 	 		console.log("Couldn't extend the session timeout " + xhr.status);
 	 		if (isSessionExpired()) {
 				doLogout();
 			}
 		}
 		
 	}).fail(function(xhr)  {
 		console.log("Couldn't extend the session timeout " + xhr.status);
 		if (isSessionExpired()) {
			doLogout();
		}
 	});
}

function notifyUser() {

	// If the query is still running, try extending the session first.
	if ($("#processDescription").length && $("#progressBar").length) {
		extendSession();
		
		if ($("#sessionDiv").length) {
			$("#sessionDiv").dialog("close");
		}
	}
	
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
	window.location.replace(window.location.origin + "/query/logout");
}


function isSessionExpired() {
	var currentTime = (new Date()).getTime();
	var elapsedTime = (currentTime - startTime)/1000;	// Time elapsed since page was loaded in seconds
	
	if (elapsedTime > (timeoutThreshold * 60)) {
		console.log("Session expired QT!!!!");
		return true;
	} else {
		return false;
	}
}

