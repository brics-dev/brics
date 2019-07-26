/**
 * Local QueryTool JavaScript library that enables HTTP requests of web services and Struts 2 actions.
 */
QT.HttpRequster = function() {};

/**
 * This is a wrapper function around jQuery's ajax() function. A default failure listener is attached to the
 * returned jqXHR promise object that will redirect the page to the login URL, if a 401 response is encountered.
 * Additional event listeners can be attached to the returned jqXHR object.
 * 
 * @param settings - A configuration object that must conform to the same config object used with the jQuery 
 * ajax() function. This parameter is optional, but is highly recommended.
 * @param url - The URL of the target web service or Struts action. This parameter is optional.
 * @return The jqXHR object created from calling the jQuery.ajax() function. This promise object can
 * be used to attach more event listeners to the HTTP request and/or cancel the request.
 */
QT.HttpRequster.prototype.ajax = function(settings, url) {
	var config = {};
	
	// Add the url parameter to the config object, if able.
	if ( (typeof url == "string") && (url.length > 0) ) {
		config.url = url;
	}
	
	// Merge the config object with the settings, if able.
	if ( (typeof settings == "object") && (settings != null) ) {
		$.extend(config, settings);
	}
	
	// Initiate a HTTP request with the passed in settings object.
	var promise = $.ajax(config);
	
	// Add in a default failure callback that will check for any 401 error responses.
	promise.fail(function(jqXHR, textStatus, errorThrown) {
		if ( jqXHR.status == 401 ) {
			console.error("The current user's session is not valid. Redirecting to login page...");
			window.location.assign(System.urls.login);
		}
	});
	
	return promise;
};

/**
 * A convenience function for initiating HTTP GET requests. Only the "url" or settings parameter is required. The target URL of the request
 * must also be given in the "url" parameter or as the "url" property of the "settings" parameter, otherwise an error will be thrown.
 * 
 * @param url - The URL of the target web service or Struts action (can also be specified in the "settings"
 * configuration object). Please note that the URL in the "settings" configuration object takes precedence.
 * @param requData - Optional string or object (preferred) which contains the query parameters for the HTTP request.
 * The query parameters can also be included in the "settings" configuration object, which will take precedence over
 * this parameter.
 * @param dataType - An optional string indicating the type of data that is expected back from the server. This is the 
 * same strings that are used with the jQuery ajax calls. The data type can also be given in the "settings" configuration
 * object, and the data type in that object will override the value provided to this parameter.
 * @param success - An optional function, which will be called only when the HTTP request was successful. This function may
 * also be specified in the "settings" configuration object, which will override this parameter. The parameters used in your
 * callback function are the save as the callback used in jQuery ajax functions.
 * @param settings - An optional configuration object, which mirrors the same object used in a jQuery ajax call. 
 * If the properties of this object conflict with the other parameters, then data from the settings object will
 * take precedence. If the other parameters provides data that is not already part of the settings object, then
 * that data will be merged into the settings object.
 * @return The jqXHR object created from calling the jQuery.ajax() function. This promise object can
 * be used to attach more event listeners to the HTTP request and/or cancel the request.
 */
QT.HttpRequster.prototype.get = function(url, requData, dataType, success, settings) {
	var config = {
		type : "GET"
	};
	
	// Assign the parameters to the config object.
	if ( (typeof url == "string") && (url.trim().length > 0) ) {
		config.url = url.trim();
	}
	// Check if there is a URL specified in the configuration object. If not throw an error.
	else if ( (typeof settings == "undefined") || (typeof settings.url != "string") || (settings.url.trim().length == 0) ) {
		throw "An URL must be specified for this HTTP GET request.";
	}
	
	if ( (typeof requData != "undefined") && (requData !== null) ) {
		config.data = requData;
	}
	
	if ( (typeof dataType == "string") && (dataType.trim().length > 0) ) {
		config.dataType = dataType.trim();
	}
	
	if ( (typeof success == "function") ) {
		config.success = success;
	}
	
	if ( (typeof settings == "object") && (settings != null) ) {
		// Merge settings object with the config object.
		$.extend(config, settings);
	}
	
	// Call the ajax function to complete the HTTP request.
	return this.ajax(config);
};

/**
 * A convenience function for initiating HTTP POST requests. The "url" and "requData" or the "settings" parameter 
 * is required. If a "settings" object is not given, and either the "url" or "requData" parameter is undefined, 
 * then the an error will be thrown.
 * 
 * @param url - The URL of the target web service or Struts action (can also be specified in the "settings"
 * configuration object). Please note that the URL in the "settings" configuration object takes precedence.
 * @param requData - String or object (preferred) which contains the query parameters for the HTTP 
 * request. The query parameters can also be included in the "settings" configuration object, which will take 
 * precedence over this parameter.
 * @param dataType - An optional string indicating the type of data that is expected back from the server. This is the 
 * same strings that are used with the jQuery ajax calls. The data type can also be given in the "settings" configuration
 * object, and the data type in that object will override the value provided to this parameter.
 * @param success - An optional function, which will be called only when the HTTP request was successful. This function may
 * also be specified in the "settings" configuration object, which will override this parameter. The parameters used in your
 * callback function are the save as the callback used in jQuery ajax functions.
 * @param settings - An optional configuration object, which mirrors the same object used in a jQuery ajax call. 
 * If the properties of this object conflict with the other parameters, then data from the settings object will
 * take precedence. If the other parameters provides data that is not already part of the settings object, then
 * that data will be merged into the settings object.
 * @return The jqXHR object created from calling the jQuery.ajax() function. This promise object can
 * be used to attach more event listeners to the HTTP request and/or cancel the request.
 */
QT.HttpRequster.prototype.post = function(url, requData, dataType, success, settings) {
	var config = {
		type : "POST"
	};
	
	// Assign the parameters to the config object.
	if ( (typeof url == "string") && (url.trim().length > 0) ) {
		config.url = url.trim();
	}
	//// Check if there is a URL specified in the configuration object. If not, throw an error.
	else if ( (typeof settings == "undefined") || (typeof settings.url != "string") || (settings.url.trim().length == 0) ) {
		throw "An URL must be specified for this HTTP POST request.";
	}
	
	//// Assign request data, if given.
	if ( typeof requData != "undefined" ) {
		if ( (typeof requData == "object") && (reqData != null) ) {
			config.data = requData;
		}
		else if ( (typeof requData == "string") && (requData.trim().length > 0) ) {
			config.data = requData.trim();
		}
	}
	//// Check if data is specified in the configuration object. If not, throw an error.
	else if ( (typeof settings != "undefined") && (typeof settings.data != "undefined") ) {
		if ( ((typeof settings.data == "string") && (settings.data.trim().length == 0)) || (settings.data === null) ) {
			throw "The body of of the POST request is empty.";
		}
	}
	else {
		throw "The body of the POST request is not defined.";
	}
	
	if ( (typeof dataType == "string") && (dataType.trim().length > 0) ) {
		config.dataType = dataType.trim();
	}
	
	if ( (typeof success == "function") ) {
		config.success = success;
	}
	
	if ( (typeof settings == "object") && (settings != null) ) {
		// Merge settings object with the config object.
		$.extend(config, settings);
	}
	
	// Call the ajax function to complete the HTTP request.
	return this.ajax(config);
};
