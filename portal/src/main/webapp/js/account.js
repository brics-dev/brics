// Contains common functions used by Account modules.


function expirationDateInit() {
	$(".roleCheckbox").each(function() {
		var $chkBx = $(this);
		var id = $chkBx.attr("id").split("_")[1];
		var $exprDtSection = $("#expirationDate" + id);
		
		if(!$chkBx.is(":checked")) {
			$exprDtSection.find(".date-picker").val("");
			$exprDtSection.hide();
		} else {
			$exprDtSection.show();
		}
	});
}

//Account role expiration date constructer.
function RoleExpiration(roleType, exprDate) {
	this.roleTypeId = roleType;
	this.expirationDate = exprDate;
}

/** 
 * Converts all of the expiration date text fields in the included accountPrivilegesInterface.jsp page 
 * to an array of RoleExpiration JavaScript objects. The array will then be converted to a JSON string
 * and stored in a hidden input field for submission.
 */
function convertExpirationDatesToJSON() {
	var dateArray = [];
	
	// Add all of the expiration dates to the date array.
	$(".date-picker").each(function() {
		var $txtBx = $(this);
		var $ckBx = $txtBx.parents("li:first").find("input:checkbox");
		
		if ($ckBx.is(":checked")) {
			var strArray = $txtBx.attr("id").split("_");
			var roleType = parseInt($.trim(strArray[1]));
			var expireDate = $.trim($txtBx.val());
			
			// Add new expiration date to the array.
			dateArray.push(new RoleExpiration(roleType, expireDate));
		}
	});
	
	// Convert the date array to JSON, and added it to the hidden input.
	$("#expireDateJson").val(JSON.stringify(dateArray));
}

function convertExpirationDatesToJSONWithoutChkBx() {
	var dateArray = [];

	// Add all of the expiration dates to the date array.
	$(".date-picker.no-chkbox").each(function() {
		var $txtBx = $(this);
		var strArray = $txtBx.attr("id").split("_");
		var roleType = parseInt($.trim(strArray[1]));
		var expireDate = $.trim($txtBx.val());
		
		// Add new expiration date to the array.
		dateArray.push(new RoleExpiration(roleType, expireDate));
	});

	// Convert the date array to JSON, and added it to the hidden input.
	$("#expireDateJson").val(JSON.stringify(dateArray));
}


/**
 * Removes any hidden expiration date inputs when the associated role checkbox is not checked.
 */
function processHiddenExpireDateFields() {
	$(".hiddenExpireDate").each(function() {
		var $expDate = $(this);
		var id = $expDate.attr("id").split("_")[1];
		var $ckBox = $expDate.siblings("#role_" + id);
		
		// If the checkbox is not check, clear the expiration date.
		if (!$ckBox.is(":checked")) {
			$expDate.remove();
		}
	});
}


function updateState() {
	if ($('.country option:selected').text() == "United States of America") {
		$('#state').show();
	} else {
		$('#state').hide();
		$("#state").find("option:selected").removeAttr("selected");
	}
}


function countryFunction() {
	$('.country').change( function() {
		if ($('.country option:selected').text() == "United States of America") {
			$('#state').fadeIn();
		} else {
			$('#state').fadeOut();
			$("#state").find("option:selected").removeAttr("selected");
		}
	});
}

function openEmailSetting(emailReportType) {
	var actionUrl;
	
	if(emailReportType === "ACCOUNT_REQUEST") {
		actionUrl = "accountReportsAction!getAutomatedRequestReportLightbox.ajax";
	} else if(emailReportType === "ACCOUNT_RENEWAL") {
		actionUrl = "accountReportsAction!getAutomatedRenewalReportLightbox.ajax";
	}
	
	$.ajax({
		type: "GET",
		url:actionUrl,
		cache: false,
		data: {},
		success:function(data) {
			$("#automatedReportDashBoard").html(data);
			$("#automatedReportDashBoardDialog").dialog({
				title: "Automated Report of Dashboard",
				height: 400,
				width: 600,
				buttons : [
					{
						id: "cancelBtn",
						text: "Cancel",
						click: function() {
							$(this).dialog('close');
						}
					},
					{
						id: "saveBtn",
						text: "Save",
						click: function() {
							
							var emailReportFrequency = $('input:radio[name=frequency]:checked').val();
							
							//if user selects weekly but doesn't specify the date, default to Monday;
							if(emailReportFrequency == 'weekly'){
								emailReportFrequency = $('input:radio[name=frequencyWeekly]:checked').val();	
							} 
							
							if(emailReportType === "ACCOUNT_REQUEST") {
								var isValid = validateStatusSelected();
								if(!isValid) {
									$("#selectStatusError").show();
									return;
								}
								
								var emailReportStatus = "";
									
								$('.statusCheckbox:checked').each(function() {
									emailReportStatus = emailReportStatus + $(this).val() + ",";
								});
								
								emailReportStatus = emailReportStatus.substring(0, emailReportStatus.length - 1)
								
								saveRequestEmailReportSetting(emailReportFrequency,emailReportType,emailReportStatus);
							} else if(emailReportType === "ACCOUNT_RENEWAL") {
								saveRenewalEmailReportSetting(emailReportFrequency,emailReportType);
							}
							
							$(this).dialog('close');
						}
					}
				]
			});
			$("#automatedReportDashBoardDialog").dialog('open');			
		}	
	});
}


function validateStatusSelected() {
	var isCheckedReq  =  $('#requested').prop('checked');
	var isCheckedPen  =  $('#pending').prop('checked');
	var isCheckedCha  =  $('#changeRequested').prop('checked');
	if(isCheckedReq || isCheckedPen || isCheckedCha) {
		return true;
	}else {
		return false;
	}
	
	
}

function saveRequestEmailReportSetting(emailReportFrequency,emailReportType,emailReportStatus) {
	$.ajax({
		type: "POST",
		cache: false,
		url: "accountReportsAction!saveEmailReportSetting.ajax",
		data: {"emailReportFrequency": emailReportFrequency,
			   "emailReportType": emailReportType,
			   "emailReportStatus": emailReportStatus},
		success: function(data) {
	
		}
	});
}

function saveRenewalEmailReportSetting(emailReportFrequency,emailReportType) {
	$.ajax({
		type: "POST",
		cache: false,
		url: "accountReportsAction!saveEmailReportSetting.ajax",
		data: {"emailReportFrequency": emailReportFrequency,
			   "emailReportType":emailReportType},
		success: function(data) {
	
		}
	});
}


