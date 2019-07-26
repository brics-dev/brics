/**
 * This file will pop up a confirmation dialog to notify the end user ten minute 
 * before the session expires.  If user chooses to continue the work, it will 
 * submit a dummy server request to keep the session alive.
 */

var timeoutThreshold = 30;	// Timeout threshold is 60 min
var notifyTime = 10;			// Notify user 10 minute before timeout
var environment = window.location.origin;//To detect whether local or server(dev/stage/demo/uat/prod) and direct to logout url
var startTime = (new Date()).getTime();
var notifyIntervalId;		// This is the interval that we send out the warning
var countdownIntervalId;	// Count down every 10 seconds to close out the warning dialog in time and alert user session has expired.  
//var processMsg = "Performing query...";

$(document).ready(function() {
	notifyIntervalId = setInterval(function() { 
		notifyUser();
    }, (timeoutThreshold - notifyTime)*60*1000);

	$("body").on('click', function(y) {
		// y.preventDefault();
		//If user is doing action clicking and loading different pages extend CAS/Application session to so we don't get 500 error
	 	$.ajax({
			  url: "service/keepAlive",
			  async: true
			}).done(function(data) {
				startTime = (new Date()).getTime();
				//alert( "success" +data);
			}).fail(function(xhr)  {
				//alert( "fail" );
			});
	});
});

function notifyUser() {

	if (isSessionExpired()) {
		clearInterval(countdownIntervalId); 
		clearInterval(notifyIntervalId);
		//$("#sessionDialog").remove();
		
		$("#sessionDiv").dialog("close");
		alert("Your session has expired, please login again.");
		doLogout();
	} else {
		//createDialog("Warning", "Your session is about to expire, please click Ok button if you need more time.");
		
		$("<div>Your session is about to expire, please click Ok button if you need more time.</div>").attr('id','sessionDiv').appendTo('body');  
		$( "#sessionDiv" ).dialog({
			dialogClass: "",
			title: "Warning",
			resizable: false,
        	height: 140,
        	modal: true,
        	buttons : [{
        		text : "Ok",
        		click: function() {
        			$.ajax({
          			  url: "service/keepAlive",
          			  async: true
          			}).done(function(data) {
          				//alert( "success"+ data);
          				startTime = (new Date()).getTime();
          			}).fail(function(xhr)  {
          				//alert( "fail" );
          			});
        			$(this).dialog("close");   			
            		clearInterval(countdownIntervalId);
        		}
        	},
        	{
        		text : "Cancel",
        		click : function() {
        			$(this).dialog("close");
                	//window.location.replace("http://fitbir-portal-local.cit.nih.gov:8080/portal/jsp/login.jsp");
	                doLogout();	
        		}	
        	}]
		});
		
		
		// check session expiration 10 seconds
		countdownIntervalId = setInterval(function() { 
			if (isSessionExpired()) {
				clearInterval(countdownIntervalId); 
				clearInterval(notifyIntervalId);
				$("#sessionDiv").dialog("close");
				alert("Your session has expired, please login again.");
			} 
		}, 10*1000);   
	}
	
	extendSessionWithProcessingView();
}

function doLogout() {
	if(window.location.href.indexOf("local") > -1 || window.location.href.indexOf("127") > -1){
    	window.location.replace(environment+"/query/jsp/login.jsp");       
    }else{
    	window.location.replace(environment+"/query/logout");   
    }
}


function isSessionExpired() {
	var currentTime = (new Date()).getTime();
	var elapsedTime = (currentTime - startTime)/1000;	// Time elapsed since page was loaded in seconds
	
	if (elapsedTime > (timeoutThreshold * 60)) {
		return true;
	} else {
		return false;
	}
}

function extendSessionWithProcessingView() {

	if ( $("#processDescription").length && $("#progressBar").length ){

	 	$.ajax({
			  url: "service/keepAlive",
			  async: true
			}).done(function(data) {
				startTime = (new Date()).getTime();
			}).fail(function(xhr)  {
			});

		clearInterval(countdownIntervalId); 
		clearInterval(notifyIntervalId);
		if ($( "#sessionDiv" ).length) {
			$("#sessionDiv").dialog("close");
		}
	} 
}

