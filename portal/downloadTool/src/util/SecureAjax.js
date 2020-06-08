import Identity from './Identity';

/**
 * Handles JWT-enabled AJAX calls including renewal and retry if needed.
 */
var SecureAjax = function(jwt, settings) {
	if (!jwt) {
		jwt = Identity.jwt;
	}
	
	var oldErrorFn = settings.error;
	
	if (jwt == "" || Identity.isAlmostExpired()) {
		Identity.renew({
			success: function() {
				SecureAjax(jwt, settings);
			},
			error: settings.error
		});
	}
	else {
		if (Identity.isExpired()) {
			settings.error({status: 401}, "unauthorized");
		}
		
		var finalSettings = $.extend(settings, {
			cache: false,
			contentType: "json",
			beforeSend: function(xhr) {
				xhr.setRequestHeader("Authorization", 'Bearer ' + jwt);
			},
			xhrFields: {
				withCredentials: true
			}
		});
	
		// if an error happens here, and it's a 403, try renewing then re-call the
		// original
		// otherwise, call the original error function (if it exists)
		finalSettings.error = function(xhr, textStatus, errorThrown) {
			if (xhr.status == 403) {
				Identity.renew(settings);
			}
			else {
				if (oldErrorFn) {
					oldErrorFn(xhr, textStatus, errorThrown);
				}
			}
		};
	
		return $.ajax(finalSettings);
	}
};

module.exports = SecureAjax;