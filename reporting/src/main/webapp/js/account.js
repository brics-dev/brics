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
