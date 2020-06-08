	var __autosave = false;
	var countdownIntervalId;
	var warningTimeoutId;
	var startTime = (new Date()).getTime();
	
	$(document).ready(function() {
		// Start the a timeout function to check the session timeout and notify the
		// user about the pending session expiration.
		warningTimeoutId = setTimeout(notifyUser, appWarningThreshold * 60 * 1000);
		$(document).on("click", extendSession);
	});

	
	function extendSession() {
		clearInterval(countdownIntervalId);
		var date = new Date();
		var currentTime = date.getTime();
		var elapsedTime = (currentTime - startTime) / (60 * 1000);
		
		if (isSessionExpired()) {
			console.log("extendSession Session has been already expired ");
			logoutOfSystem();
		} else {
//			console.log("keepalive refresh");
			var casLoginUrl = baseUrl + '/casLoginRedirect.action?unique=' + currentTime;
			$.ajax({
				url : casLoginUrl,
				datatype: "text",
				beforeSend: function(response) {
			        if(response && response.overrideMimeType){
			           	response.overrideMimeType("text/plain;charset=UTF-8");
			        }
			    },
				success : function(response) {
					clearTimeout(warningTimeoutId);
					startTime = (new Date()).getTime();
					
					// Reset the session expiration notification display.
					warningTimeoutId = setTimeout(notifyUser, appWarningThreshold * 60 * 1000);
				}
			})
			.fail(function() {				
				console.log("Couldn't extend the session timeout. Resetting the notification function.");				
				if ( isSessionExpired() ) {
					logoutOfSystem();
				}
			});
		} 

	}
    
	function logoutOfSystem() {
	    if (__autosave) {
	        preformAutosave();
	    } else { 
//	    	console.log("===Log out!!!!!!!!!");
			clearTimeout(warningTimeoutId);
			clearInterval(countdownIntervalId); 
	        top.location.href = baseUrl + '/logout';
	    }
	}

    function notifyUser() {
//    	console.log("======notifyUser");
    	if (!isSessionExpired()) {
    		// Check if the notify div is on the page, and create it if it is not.
    		if ( $("#sessionDiv").length == 0 ) {
    			$("body").append('<div id="sessionDiv" style="display:none">Your session is about to expire, please click Ok button if you need more time.</div>');
    			$("#sessionDiv").dialog({
    				autoOpen: false,
    				title: "Warning",
    				resizable: false,
    				modal: true,
    				buttons: {
    					"Ok": function() {
    						extendSession();
    						$(this).dialog("close");
    					},
    					"Cancel": function() {
    						logoutOfSystem();
    					}
    				},   				
    				open: function() {
//    					console.log("Open Warning!");
    					$(document).off("click", extendSession);
    					$(this).siblings().find(".ui-dialog-titlebar-close").hide();
    				}
    			});
    		}
    		
    		var currentTime = (new Date()).getTime();
    		var elapsedTime = (currentTime - startTime) / (60 * 1000);
    		
    		// Check if the warning threshold has been reached, since the start time could have been reset.
    		if (elapsedTime >= appWarningThreshold) {
    			// Warn the user that their session is about to expire.
    			$("#sessionDiv").dialog("open");
    			
    			// Check session expiration 10 seconds, while the dialog is open.
    			countdownIntervalId = setInterval(function() {
    				// If the session has expired, force a logout.
    				if (isSessionExpired()) {
    					logoutOfSystem();
    				} 
    			}, 10000);
    		}
    	}
    	// Session has expired force a log out.
    	else {
    		logoutOfSystem();
    	}
    }
    function isSessionExpired() {
    	var currentTime = (new Date()).getTime();
    	var elapsedTime = (currentTime - startTime) / (60 * 1000);	
    	
    	if (elapsedTime > appTimeoutThreshold) {
//    		console.log("Session expired!!!!");
    		return true;
    	} else {
    		return false;
    	}
    }

