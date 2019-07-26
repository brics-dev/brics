var IS_GUID_VALIDATED = false;

function refreshErrors(error){
	errorDiv = document.getElementById("validationErrors");
	if(errorDiv){
		errorDiv.parentNode.removeChild(errorDiv);
	}
	if(error != null && error.length > 0) {
		errorDiv = document.createElement("div");
		errorDiv.id = 'validationErrors';
		errorDiv.className ='ibisMessaging-message ibisMessaging-primary ibisMessaging-error';
		errorDiv.innerHTML = error ;
		var messageContainer = document.getElementById("messageContainer");
		messageContainer.innerHTML="";
		messageContainer.insertBefore(errorDiv,messageContainer.firstChild);
	}
	return false;
};

$(function() {
	initGuid = function(actionName) {
		$(window).load(function() {
			$("#runningValidate").hide();
		});

		if (actionName == 'addPatient') {
			IS_GUID_VALIDATED = false;
			$("input#btnValidateGUID").show();
			$("img#invalidGuid,img#validGuid").hide();						
		}
		// "editing mode"
		else {
			var currentGuid = $("input#guid").val();
			
			if (currentGuid != null && $.trim(currentGuid).length > 0) {
				IS_GUID_VALIDATED = true;
				$("img#invalidGuid,input#btnValidateGUID").hide();
				$("img#validGuid").show();
			}
			else {
				$("input#btnValidateGUID").show().prop("disabled", true);
				$("img#invalidGuid,img#validGuid").hide();
			}
		}
	};
	

	$("input#guid").bind("keyup change paste input", function(event){ 
		event.stopImmediatePropagation();
		$("input#btnValidateGUID").show();
		$("img#validGuid, img#invalidGuid").hide();
		
		if ($.trim($(this).val()).length == 0) {
			$("input#btnValidateGUID").prop("disabled", true);
		}
		else {
			$("input#btnValidateGUID").prop("disabled", false);
		}
		
		IS_GUID_VALIDATED = false;
	});
	
	
	GUIDValidation = function() {
		IS_GUID_VALIDATED = false;
		
		var guid = $.trim($("input#guid").val());
		
		if (guid.length == 0) {
			refreshErrors("A GUID or Pseudo-GUID entry is required<br/>");
		}
		else {
			refreshErrors("");
		}
		
		var jwt = "";
		
		// Get the JWT from the server.
		$.ajax({
			type: "GET",
			url: "getJwt.action",
			dataType: "json",
			success: function(data) {
				data = data.replace(/\\/g, '');
				var jwtObj = JSON.parse(data);
				var jwt = jwtObj.jwt;
				if ( (typeof jwt != "string") || (jwt.length == 0) ) {
					console.error("Couldn't get the JWT string from the server.");
					return;
				}
				
				// Call the centralized GUID web service to validate the GUID (with the JWT in its header.)
				$.ajax({
					  type: "GET",
					  url: guidWsBaseUrl + "ws/v1/valid/guid/" + guid,
					  headers: {
						  "Authorization" : "JWT " + jwt
					  },
					  dataType: 'text',
					  success: function(data) {
						  if ((typeof data != "undefined") && (data != null)) {
							 var data = JSON.parse(data);
							 if (typeof data.doesGuidExist !== "undefined") {
								 if (data.doesGuidExist) {
										$("img#invalidGuid,input#btnValidateGUID").hide();
										$("img#validGuid").show();
										IS_GUID_VALIDATED = true;
										refreshErrors("");
									 }
									 else {
										$("img#invalidGuid").show();
										refreshErrors("GUID is invalid.");
									 }
							 } 
							 else {
								 refreshErrors("GUID service failed to validate the GUID.");
							 }
						  }
						  else {
							  refreshErrors("GUID service failed to validate the GUID.");
						  }
					  },
					  error : function(e) {
						  refreshErrors("GUID service failed to validate the GUID.");
					  }
				});
			},
			error: function(jqXHR, textStatus, errorThrown) {
				console.error("Could't get the JWT form the portal. " + "Error reason: " + errorThrown);
				refreshErrors("GUID service failed to validate the GUID.");
			}
		});
	};

	
	$("input#btnValidateGUID").click(function() {
		$('#runningValidate').show();
		GUIDValidation();
		$('#runningValidate').hide();
	});
});
