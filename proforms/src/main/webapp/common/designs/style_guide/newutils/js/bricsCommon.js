/**
 * @requires jQuery
 * 
 * Provides page start-up processing for all BRICS/ProFoRMS front-end standard
 * javascript processing including:
 * 
 * * tooltips					-	implemented	-	added
 * * datatables					-	implemented	-	added
 * * collapsible headings		-	implemented	-	added
 * * file upload				-	
 * * messaging					-	implemented -	added
 * 
 */

$(document).ready(function() {
	/**
	 * Implements jquery UI's tooltips
	 * 
	 * Add tooltip triggering by adding a "title" attribute to an HTML element
	 */
	$( document ).tooltip();
	
	
	/**
	 * Initializes all datatables on the page
	 */
	if (typeof dataTablesDisabled == "undefined" || dataTablesDisabled != true) { 
		if (typeof IbisDataTables != "undefined") {
			IbisDataTables.fullBuild();
		}
	}
	
	
	/**
	 * Initializes all date and datetime input fields
	 */
	DatePickerController.initAllPickers();
	
	/**
	 * Initiaalizes all expandable/contractable sections
	 */
	$(".toggleable").expandSection();
	
	
	/**
	 * Initialize the IBIS Messaging system.  Adds the messages container if
	 * needed.
	 */
	if (typeof $.ibisMessaging !== "undefined") {
		if ($("#messages").length < 1) {
			$("body").prepend('<div id="messageContainer"></div><ul id="messages"></ul>');
		}
		$("#messages").ibisMessaging({
			primaryContainer: "#messageContainer"
		});
	}
	
	convertFileInputs();
	disableAfterSingleClick();
	//enableDoubleClickOnRadios();
	
	
	
});


function IE8Fix() {
	// add "trim" functionality to String in IE8ish
	if(typeof String.prototype.trim !== 'function') {
	  String.prototype.trim = function() {
	    return this.replace(/^\s+|\s+$/g, ''); 
	  };
	}
}

/**
 * Hide file input fields and replace with a pretty field and icon
 */
function convertFileInputs() {
	// this is just an index for creating IDs.  That's why we subtract 1
	var fileInputCount = $(".fileInput").length - 1;
	if (!(BrowserDetect.browser == "Explorer")) {
		$('input[type="file"]:visible').each(function() {
			var input = $(this);
			if (input.css("display") != "none") {
				input.css("display","none");
				var inputId = "fileInput_" + fileInputCount;
				input.after('<input type="text" id="' + inputId + '" class="fileInput" />');
				$("#"+inputId).css("width", $(this).css("width"));
				$("#"+inputId).click(function() {
					input.click();
				}).keydown(function(e) {
					if(e.which != 9){
					input.click();
					}
				});
				$("#"+inputId).val(input.val());
				input.change(function() {
					input.next().val(input.val());
				});
				fileInputCount++;
			}
		});
	}
}

/**
 * Given a button or set of buttons that are given the class "singleClick",
 * disable the button after being clicked once.
 */
function disableAfterSingleClick() {
	$("#mainContent").on("click", 'input[type="button"].singleClick, button.singleClick', function() {
		$(this).prop('disabled', true);
		var $button = $(this);
		setTimeout(function() {reEnableButton($button);}, 2000);
	});
}
function reEnableButton($button) {
	$button.prop('disabled', false);
}

/**
 * Enables double-clicking to clear radio boxes
 */
function enableDoubleClickOnRadios() {
	$('body').on('dblclick', 'input[type="radio"]', function() {
		$(this).prop("checked", false);
		$(this).trigger("change");
	});
}

/**
 * Redirects to the specified URL.  If IE, it sets the referrer.
 * Added to resolve the access denied error due to a missing 'referrer' header when using IE.
 */
function redirectWithReferrer(url, target){
	//re-define for IE
	var targetStr = "";
	if (typeof target == "string") {
		targetStr = target;
	}
    var referLink = document.createElement('a');
    referLink.href = url;
    referLink.target = targetStr;
    document.body.appendChild(referLink);
    referLink.click();
}

function openPopup(url, name, specs) {
	var wnd = null;
	
	if (typeof name == "undefined" || name == "") {
		name = "newwindow";
	}
	
	if ((BrowserDetect.browser == "Explorer")) {
		if ($("#redirectLink").length < 1) {
			// if we don't currently have a redirect link on the page, create one
			$("body").append('<a href="javascript:void(0)" id="redirectLink" target="_self" style="display:none">click</a>');
		}
		// change the properties of the link and click it
		wnd = window.open("", name, specs);
		var link = document.getElementById("redirectLink");
		link.target = name;
		link.href = url;
		link.click();
	}
	else {
		wnd = window.open(url, name, specs);
	}
	return wnd;
}

