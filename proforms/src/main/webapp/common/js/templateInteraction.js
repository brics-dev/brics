/**
 * These actions are those that activate when a person goes to any page on the site.  They interact with standard
 * template items and should be considered a part of ibis core javascript.
 * 
 */
$(function() {
	// choose the link to highlight for left nav
	
	

//	$( "#leftNav" ).accordion({
//		activeSublinkClassName: "activeSubLink",
//		autoHeight: false,
//		navigation: true,
//		navigationFilter: function() {
//			// if main link and first link share a url (assumes the link has sublinks), don't consider it
//			var sublinkref = $(this).parent().next("div").children("a");
//			if (sublinkref.length > 0 && $(sublinkref[0]).attr("href") == $(this).attr("href")) {
//				return false;
//			}
//			
//			var originalLinkUrl = this.href.toLowerCase().split("?")[0].toLowerCase();
//			var originalPageUrl = location.href.toLowerCase().split("?")[0].toLowerCase();
//			
//			
//			
//			var processLinkUrl = originalLinkUrl;
//			var processPageUrl = originalPageUrl;
//			
//			// if either URL has a mask, use that instead of the URL for matching
//			var linkUrlMask = findLeftNavHighlight(this.href.toLowerCase());
//			var pageUrlMask = findLeftNavHighlight(location.href.toLowerCase());
//			if (linkUrlMask != false) {
//				processLinkUrl = linkUrlMask;
//			}
//			if (pageUrlMask != false) {
//				processPageUrl = pageUrlMask;
//			}
//			
//			// if there is a mask that has parameters, use the parameters, so don't split it
//			if (	(processLinkUrl == processPageUrl							// the masked urls
//					|| this.href.toLowerCase() == location.href.toLowerCase()	// the original, full, urls
//					|| originalPageUrl == originalLinkUrl
//						&& (maskUrlHasParameters(linkUrlMask) 
//								|| maskUrlHasParameters(pageUrlMask))			// if a mask url does not exist or if one mask has parameters, the original urls
//					|| originalPageUrl == processLinkUrl
//					|| originalLinkUrl == processPageUrl
//					|| ~processLinkUrl.indexOf(processPageUrl)
//					|| ~processPageUrl.indexOf(processLinkUrl)) 
//					&& this.href.indexOf("#") != this.href.length-1) {
//				return true;
//			}
//			return false;
////			
////			
////			// for now, this compares only the file name (NOT query options) part of the full urls
////			var thisUrlSplit = this.href.toLowerCase().split("?");
////			var currentUrlSplit = location.href.toLowerCase().split("?");
////			// get the parameters in the url and look for a "highlightleftnav" url
////			var maskExists = false;
////			
////			// parameters exist
////			if (currentUrlSplit.length > 1) {
////				var indexOfParameter = currentUrlSplit[1].indexOf("highlightleftnav");
////				if (indexOfParameter > -1) {
////					maskExists = true;
////					var truncatedParameterList = currentUrlSplit[1].substring(indexOfParameter);
////					var indexOfAmp = truncatedParameterList.indexOf("&"); // first occurrence
////					if (indexOfAmp > -1) {
////						// trims the end off the parameter list, giving us a nice
////						truncatedParameterList = truncatedParameterList.substring(0,indexOfAmp);
////					}
////					// so now we have just highlightleftnav=url
////					//var parameterUrl = truncatedParameterList.split("=")[1];
////					var parameterUrl = truncatedParameterList.substring(truncatedParameterList.indexOf("=")+1);
////					parameterUrl = parameterUrl.replace(/\*\*/g,"?").replace(/%3f/g,"?");
////					// see http://stackoverflow.com/questions/1789945/javascript-string-contains for the tilde trick
////					if (~this.href.toLowerCase().indexOf(parameterUrl.toLowerCase())) {
////						return true; // else all this work was worthless
////
////					}
////				}
////			}
////			if (!maskExists && thisUrlSplit[0].toLowerCase() == currentUrlSplit[0].toLowerCase() && this.href.indexOf("#") != this.href.length-1) {
////				return true;
////			}
////			return false;
//		},
//		create: function(event, ui) {
//			$("#"+this.id).accordion("highlightCurrentSublink");
//			if ($("h3.ui-state-active").next("div").find("a").length < 1) {
//				$("h3.ui-state-active").addClass("nosublinks");
//			}
//		}
//	});
//	jQuery(".disableAccordionLink").live("click", function(){
//		redirectWithReferrer(this.href);
//	});
});

/**
 * Finds the "highlightleftnav" parameter in the given url and returns that paramter
 * if it exists.  Otherwise, returns false
 * 
 * @param url the parameter to check
 * @returns string highlightleftnav parameter if exists, otherwise false
 */
function findLeftNavHighlight(url) {
	var urlSplit = url.toLowerCase().split("?");
	if (urlSplit.length > 1) {
		var indexOfParameter = urlSplit[1].indexOf("highlightleftnav");
		if (indexOfParameter > -1) {
			var truncatedParameterList = urlSplit[1].substring(indexOfParameter);
			// first occurrence
			//Since we split off the beginning up until the parameter, this will trim off the end
			var indexOfAmp = truncatedParameterList.indexOf("&");
			if (indexOfAmp > -1) {
				// trims off the end of the parameter list, giving us a nice parameter pair
				truncatedParameterList = truncatedParameterList.substring(0,indexOfAmp);
			}
			// so now we just have highlightleftnav=url
			var parameterUrl = truncatedParameterList.substring(truncatedParameterList.indexOf("=")+1);
			parameterUrl = parameterUrl.replace(/\*\*/g,"?").replace(/%3f/g,"?");
			return parameterUrl;
		}
		return false;
	}
	return false;
}

function maskUrlHasParameters(maskUrl) {
	if (typeof maskUrl != "string") return false;
	return maskUrl.split("?").length > 1;
}

/*
$(document).ready(function() {
	$("#login").tooltip({
		tip: "#login_over",
		position: "bottom center",
		//relative: true,
		offset: [-3,-82]
	});
});
*/
$(document).ready(function() {
//	$("#perspectiveDropdown").contextmenu({
//		position: {my: "top left", at: "bottom left", of : $()}
//	});

	/*var $perspectiveSelect = $("#perspectiveSelectContainer");
	if ($perspectiveSelect.length > 0) {
		$perspectiveSelect.tooltip({
			position: "bottom center",
			relative: true,
			offset: [0,0]
		}).focus(function() {
			$perspectiveSelect.tooltip("open");
		});
	}
	*/
	
	
});
