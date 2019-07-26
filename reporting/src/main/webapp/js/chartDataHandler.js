/**
 * This file handles all data handling for creating a chart with the needed data
 * including requesting the data.
 * 
 * This file does NOT handle actually creating the charts
 */

var chartDataHandlerSelfReference;
function setChartDataSelfReference(handler) {
	chartDataHandlerSelfReference = handler;
}

function ChartDataHandler(config) {
	this.data = null;

	this.defaults = {
		"getGlobalSubjectsUrl" : "statistics!ajaxGetGlobalSubjects.ajax",
		"getOrgSubjectsUrl" : "statistics!ajaxGetOrgSubjects.ajax",
		"getSiteSubjectsUrl" : "statistics!ajaxGetSiteSubjects.ajax",
		"getUserSubjectsUrl" : "statistics!ajaxGetUserSubjects.ajax",
		"getGlobalAccessUrl" : "statistics!ajaxGetGlobalAccess.ajax",
		"getOrgAccessUrl" : "statistics!ajaxGetAccessForOrg.ajax",
		"getSiteAccessUrl" : "statistics!ajaxGetAccessForSite.ajax",
		"getUserAccessUrl" : "statistics!ajaxGetAccessForUser.ajax",
		oncomplete : null
	};
	this.config = $.extend({}, this.defaults, config);

	setChartDataSelfReference(this);
}

ChartDataHandler.getInstance = function() {
	if (chartDataHandlerSelfReference == null) {
		setChartDataSelfReference(this);
	}
	return chartDataHandlerSelfReference;
}

ChartDataHandler.prototype.setOnComplete = function(oncomplete) {
	this.config.oncomplete = oncomplete;
}

/**
 * ajax's a request to get all subject data matching the given parameters.
 * Returns this data as an array of Subject objects (defined below, configured
 * and constructed within the action and returned as json).
 * 
 * The DATE fields should contain milliseconds since the epoch.
 */
ChartDataHandler.prototype.getGlobalData = function(config) {
	var defaults = {
		startDate : null,
		endDate : null
	};
	config = $.extend({}, defaults, config);

	var postData = {
		"startDate" : escape(config.startDate),
		"endDate" : escape(config.endDate)
	};

	// compile the data for ajax
	var chartDataHandler = this;
	$
			.post(
					this.config.getGlobalSubjectsUrl,
					postData,
					function(data) {
						try {
							var subjectArray = jQuery.parseJSON(data);
							chartDataHandler.data = new Array();
							for ( var i = 0; i < subjectArray.length; i++) {
								chartDataHandler.data.push($.extend({},
										Subject, subjectArray[i]));
							}

							// call the callback
							if (chartDataHandler.config.oncomplete != null) {
								chartDataHandler.config
										.oncomplete(chartDataHandler.data);
							}
						} catch (error) {
//							 alert("00000000 \n" + data);

//							 alert("an error has occurred: " + error.message);
							alert("The data could not be obtained correctly.  You may have been logged out.");
							chartDataHandler.data = null;
						}
					});
	return this;
}

/**
 * ajax's a request to get all subject data matching the given parameters.
 * Returns this data as an array of Subject objects (defined below, configured
 * and constructed within the action and returned as json).
 * 
 * The DATE fields should contain milliseconds since the epoch.
 */
ChartDataHandler.prototype.getOrgData = function(config) {
	var defaults = {
		orgName : null,
		orgId : null,
		startDate : null,
		endDate : null,
		user : null,
		site : null
	};
	config = $.extend({}, defaults, config);

	if (config.orgName != null && config.orgId != null) {
		alert("An organization identifier was not specified.  Please alert a system administrator of the error");
	} else {
		if (config.orgName != null) {
			var postData = {
				"orgName" : escape(config.orgName),
				"startDate" : escape(config.startDate),
				"endDate" : escape(config.endDate),
				"userName" : escape(config.user),
				"siteName" : escape(config.site)
			};
		} else {
			var postData = {
				"orgId" : escape(config.orgId),
				"startDate" : escape(config.startDate),
				"endDate" : escape(config.endDate),
				"userName" : escape(config.user),
				"siteName" : escape(config.site)
			};
		}

		// allows us to pass this particular instance into the ajax callback
		// below
		var chartDataHandler = this;
		// compile the data for ajax
		$.post(this.config.getOrgSubjectsUrl, postData, function(data) {
			try {
				try {
					var subjectArray = $.parseJSON(data);
					chartDataHandler.data = new Array();
					for ( var i = 0; i < subjectArray.length; i++) {
						chartDataHandler.data.push($.extend({}, Subject,
								subjectArray[i]));
					}
				} catch (error) {
					alert("internal error 1: " + error.message);
				}

				// call the callback
				try {
					if (chartDataHandler.config.oncomplete != null) {
						chartDataHandler.config
								.oncomplete(chartDataHandler.data);
					}
				} catch (error) {
					alert("internal error 2: " + error.message);
				}
			} catch (error) {
				alert("an error has occurred " + error.message);
				chartDataHandler.data = null;
			}
		});
		return this;
	}
}

/**
 * ajax's a request to get all subject data matching the given parameters.
 * Returns this data as an array of Subject objects (defined below, configured
 * and constructed within the action and returned as json).
 * 
 * The DATE fields should contain milliseconds since the epoch.
 */
ChartDataHandler.prototype.getSiteData = function(config) {
	var defaults = {
		siteName : null,
		siteId : null,
		startDate : null,
		endDate : null,
		user : null
	};
	config = $.extend({}, defaults, config);

	if (config.siteName != null && config.siteId != null) {
		alert("A Site identifier was not specified.  Please alert a system administrator of the error");
	} else {
		if (config.siteName != null) {
			var postData = {
				"siteName" : escape(config.siteName),
				"startDate" : escape(config.startDate),
				"endDate" : escape(config.endDate),
				"userName" : escape(config.user)
			};
		} else {
			var postData = {
				"siteId" : escape(config.siteId),
				"startDate" : escape(config.startDate),
				"endDate" : escape(config.endDate),
				"userName" : escape(config.user)
			};
		}

		var chartDataHandler = this;
		// compile the data for ajax
		$.post(this.config.getSiteSubjectsUrl, postData, function(data) {
			
			try {
				var subjectArray = $.parseJSON(data);
				chartDataHandler.data = new Array();
				for ( var i = 0; i < subjectArray.length; i++) {
					chartDataHandler.data.push($.extend({}, Subject,
							subjectArray[i]));
				}

				// call the callback
				if (chartDataHandler.config.oncomplete != null) {
					chartDataHandler.config.oncomplete(chartDataHandler.data);
				}
			} catch (error) {
//				alert("BBBBBBB \n" + data);
				
				alert("an error has occurred " + error.message);
				chartDataHandler.data = null;
			}
		});
		return this;
	}
}

/**
 * ajax's a request to get all subject data matching the given parameters.
 * Returns this data as an array of Subject objects (defined below, configured
 * and constructed within the action and returned as json).
 * 
 * The DATE fields should contain milliseconds since the epoch.
 */
ChartDataHandler.prototype.getUserData = function(config) {
	var defaults = {
		userName : null,
		userId : null,
		startDate : null,
		endDate : null,
	};
	config = $.extend({}, defaults, config);

	if (config.userName != null && config.userId != null) {
		alert("A User identifier was not specified.  Please alert a system administrator of the error");
	} else {
		if (config.userName != null) {
			var postData = {
				"userName" : escape(config.userName),
				"startDate" : escape(config.startDate),
				"endDate" : escape(config.endDate)
			};
		} else {
			var postData = {
				"userId" : escape(config.userId),
				"startDate" : escape(config.startDate),
				"endDate" : escape(config.endDate)
			};
		}

		var chartDataHandler = this;
		// compile the data for ajax
		$.post(this.config.getUserSubjectsUrl, postData, function(data) {
			try {
				var subjectArray = $.parseJSON(data);
				chartDataHandler.data = new Array();
				for ( var i = 0; i < subjectArray.length; i++) {
					chartDataHandler.data.push($.extend({}, Subject,
							subjectArray[i]));
				}

				// call the callback
				if (chartDataHandler.config.oncomplete != null) {
					chartDataHandler.config.oncomplete(chartDataHandler.data);
				}
			} catch (error) {
//				alert("CCCCCCC \n" + data);
				alert("an error has occurred " + error.message);
				chartDataHandler.data = null;
			}
		});
		return this;
	}
}

/**
 * ajax's a request to get all subject data matching the given parameters.
 * Returns this data as an array of Subject objects (defined below, configured
 * and constructed within the action and returned as json).
 * 
 * The DATE fields should contain milliseconds since the epoch.
 */
ChartDataHandler.prototype.getGlobalAccessData = function(config) {
	var defaults = {
		startDate : null,
		endDate : null
	};
	config = $.extend({}, defaults, config);

	var postData = {};

	var chartDataHandler = this;
	// compile the data for ajax
	$
			.post(
					this.config.getGlobalAccessUrl,
					postData,
					function(data) {
						try {
							var subjectArray = jQuery.parseJSON(data);
							chartDataHandler.data = new Array();
							for ( var i = 0; i < subjectArray.length; i++) {
								chartDataHandler.data.push($.extend({},
										AccessEntry, subjectArray[i]));
							}

							// call the callback
							if (chartDataHandler.config.oncomplete != null) {
								chartDataHandler.config
										.oncomplete(chartDataHandler.data);
							}
						} catch (error) {
//							 alert("DDDDDDD \n" + data);

							// alert("an error has occurred: " + error.message);
							alert("The data could not be obtained correctly.  You may have been logged out.");
							chartDataHandler.data = null;
						}
					});
	return this;
}

/**
 * ajax's a request to get all access data matching the given parameters.
 * Returns this data as an array of AccessEntry objects (defined below,
 * configured and constructed within the action and returned as json).
 * 
 * The DATE fields should contain milliseconds since the epoch.
 */
ChartDataHandler.prototype.getOrgAccessData = function(config) {
	var defaults = {
		orgName : null,
		orgId : null
	};
	config = $.extend({}, defaults, config);

	if (config.orgName != null && config.orgId != null) {
		alert("An organization identifier was not specified.  Please alert a system administrator of the error");
	} else {
		if (config.orgName != null) {
			var postData = {
				"orgName" : escape(config.orgName)
			};
		} else {
			var postData = {
				"orgId" : escape(config.orgId)
			};
		}

		var chartDataHandler = this;
		// compile the data for ajax
		$.post(this.config.getOrgAccessUrl, postData, function(data) {
			try {
				try {
					var dataArray = $.parseJSON(data);
					chartDataHandler.data = new Array();
					for ( var i = 0; i < dataArray.length; i++) {
						chartDataHandler.data.push($.extend({}, AccessEntry,
								dataArray[i]));
					}
				} catch (error) {
					alert("internal error 1: " + error.message);
				}

				// call the callback
				try {
					if (chartDataHandler.config.oncomplete != null) {
						chartDataHandler.config
								.oncomplete(chartDataHandler.data);
					}
				} catch (error) {
					alert("internal error 2: " + error.message);
				}
			} catch (error) {
				alert("an error has occurred " + error.message);
				chartDataHandler.data = null;
			}
		});
		return this;
	}
}

/**
 * ajax's a request to get all subject data matching the given parameters.
 * Returns this data as an array of AccessEntry objects (defined below,
 * configured and constructed within the action and returned as json).
 * 
 * The DATE fields should contain milliseconds since the epoch.
 */
ChartDataHandler.prototype.getSiteAccessData = function(config) {
	var defaults = {
		siteName : null,
		siteId : null
	};
	config = $.extend({}, defaults, config);

	if (config.siteName != null && config.siteId != null) {
		alert("A Site identifier was not specified.  Please alert a system administrator of the error");
	} else {
		if (config.siteName != null) {
			var postData = {
				"siteName" : escape(config.siteName)
			};
		} else {
			var postData = {
				"siteId" : escape(config.siteId)
			};
		}

		var chartDataHandler = this;
		// compile the data for ajax
		$.post(this.config.getSiteAccessUrl, postData, function(data) {
			try {
				var dataArray = $.parseJSON(data);
				chartDataHandler.data = new Array();
				for ( var i = 0; i < dataArray.length; i++) {
					chartDataHandler.data.push($.extend({}, AccessEntry,
							dataArray[i]));
				}

				// call the callback
				if (chartDataHandler.config.oncomplete != null) {
					chartDataHandler.config.oncomplete(chartDataHandler.data);
				}
			} catch (error) {
				alert("an error has occurred " + error.message);
				chartDataHandler.data = null;
			}
		});
		return this;
	}
}

/**
 * ajax's a request to get all access data matching the given parameters.
 * Returns this data as an array of AccessEntry objects (defined below,
 * configured and constructed within the action and returned as json).
 * 
 * The DATE fields should contain milliseconds since the epoch.
 */
ChartDataHandler.prototype.getUserAccessData = function(config) {
	var defaults = {
		userName : null,
		userId : null
	};
	config = $.extend({}, defaults, config);

	if (config.userName != null && config.userId != null) {
		alert("A User identifier was not specified.  Please alert a system administrator of the error");
	} else {
		if (config.userName != null) {
			var postData = {
				"userName" : escape(config.userName)
			};
		} else {
			var postData = {
				"userId" : escape(config.userId)
			};
		}

		var chartDataHandler = this;
		// compile the data for ajax
		$.post(this.config.getUserAccessUrl, postData, function(data) {
			try {
				var dataArray = $.parseJSON(data);
				chartDataHandler.data = new Array();
				for ( var i = 0; i < dataArray.length; i++) {
					chartDataHandler.data.push($.extend({}, Subject,
							dataArray[i]));
				}

				// call the callback
				if (chartDataHandler.config.oncomplete != null) {
					chartDataHandler.config.oncomplete(chartDataHandler.data);
				}
			} catch (error) {
//				alert("GGGGGGGGG \n" + data);
				
				alert("an error has occurred " + error.message);
				chartDataHandler.data = null;
			}
		});
		return this;
	}
}

/**
 * takes an array of Subject data and formats it for insertion into one of the
 * frequency chart objects
 */
ChartDataHandler.prototype.formatSubjectsForFrequencyChart = function(data) {
	var dataOut = [];
	var findDataElement = function(dayStartTimestamp) {
		for ( var i = 0; i < dataOut.length; i++) {
			if (dataOut[i][0] == dayStartTimestamp) {
				return i;
			}
		}
		return -1;
	};

	for ( var i = 0; i < data.length; i++) {
		var date = new Date();
		date.setTime(data[i].date);
		date.setHours(0);
		date.setMinutes(0);
		date.setSeconds(0);
		date.setMilliseconds(0);
		var entryIndex = findDataElement(date.getTime());
		if (entryIndex == -1) {
			dataOut.push([ date.getTime(), 1 ]);
		} else {
			dataOut[entryIndex][1] = dataOut[entryIndex][1] + 1;
		}
	}
	return dataOut;
}

ChartDataHandler.prototype.formatForCompare = function(data) {
	// TODO: complete
}

ChartDataHandler.prototype.formatForOrgPie = function(data) {
	var dataOut = [];
	var findDataElement = function(siteUsername) {
		for ( var i = 0; i < dataOut.length; i++) {
			if (dataOut[i][0] == siteUsername) {
				return i;
			}
		}
		return null;
	};

	for ( var i = 0; i < data.length; i++) {
		var siteUsername = data[i].creatorSite.userName;
		var siteIndex = findDataElement(siteUsername);

		if (siteIndex == null) {
			dataOut[dataOut.length] = [ siteUsername, 1 ];
		} else {
			dataOut[siteIndex][1] = dataOut[siteIndex][1] + 1;
		}
	}
	return dataOut;
}

ChartDataHandler.prototype.formatForSitePie = function(data) {
	var dataOut = [];
	var findDataElement = function(username) {
		for ( var i = 0; i < dataOut.length; i++) {
			if (dataOut[i][0] == username) {
				return i;
			}
		}
		return null;
	};

	for ( var i = 0; i < data.length; i++) {
		var username = data[i].creatorUser.userName;
		var index = findDataElement(username);

		if (index == null) {
			dataOut[dataOut.length] = [ username, 1 ];
		} else {
			dataOut[index][1] = dataOut[index][1] + 1;
		}
	}

	return dataOut;
}

ChartDataHandler.prototype.formatForAllPie = function(data) {
	var dataOut = [];
	var findDataElement = function(orgName) {
		for ( var i = 0; i < dataOut.length; i++) {
			if (dataOut[i][0] == orgName) {
				return i;
			}
		}
		return null;
	};

	var dataLength = data.length;
	for ( var i = 0; i < dataLength; i++) {
		var orgName = data[i].creatorOrg.name;
		var orgIndex = findDataElement(orgName);

		if (orgIndex == null) {
			dataOut[dataOut.length] = [ orgName, 1 ];
		} else {
			dataOut[orgIndex][1] = dataOut[orgIndex][1] + 1;
		}
	}

	return dataOut;
}

ChartDataHandler.prototype.formatAccessForFrequencyChart = function(data) {
	var dataOut = [];
	var findDataElement = function(dayStartTimestamp) {
		for ( var i = 0; i < dataOut.length; i++) {
			if (dataOut[i][0] == dayStartTimestamp) {
				return i;
			}
		}
		return -1;
	};

	for ( var i = 0; i < data.length; i++) {
		var date = new Date();
		date.setTime(data[i].date);
		date.setHours(0);
		date.setMinutes(0);
		date.setSeconds(0);
		date.setMilliseconds(0);
		var entryIndex = findDataElement(date.getTime());
		if (entryIndex == -1) {
			dataOut.push([ date.getTime(), 1 ]);
		} else {
			dataOut[entryIndex][1] = dataOut[entryIndex][1] + 1;
		}
	}
	return dataOut;
}

ChartDataHandler.prototype.formatAccessForOrgPie = function(data) {
	var dataOut = [];
	var findDataElement = function(siteUsername) {
		for ( var i = 0; i < dataOut.length; i++) {
			if (dataOut[i][0] == siteUsername) {
				return i;
			}
		}
		return null;
	};

	for ( var i = 0; i < data.length; i++) {
		var siteUsername = data[i].site.name;
		var siteIndex = findDataElement(siteUsername);

		if (siteIndex == null) {
			dataOut[dataOut.length] = [ siteUsername, 1 ];
		} else {
			dataOut[siteIndex][1] = dataOut[siteIndex][1] + 1;
		}
	}
	return dataOut;
}

ChartDataHandler.prototype.formatAccessForSitePie = function(data) {
	var dataOut = [];
	var findDataElement = function(username) {
		for ( var i = 0; i < dataOut.length; i++) {
			if (dataOut[i][0] == username) {
				return i;
			}
		}
		return null;
	};

	for ( var i = 0; i < data.length; i++) {
		var username = data[i].user.name;
		var index = findDataElement(username);

		if (index == null) {
			dataOut[dataOut.length] = [ username, 1 ];
		} else {
			dataOut[index][1] = dataOut[index][1] + 1;
		}
	}
	return dataOut;
}

ChartDataHandler.prototype.formatAccessForAllPie = function(data) {
	var dataOut = [];
	var findDataElement = function(orgName) {
		for ( var i = 0; i < dataOut.length; i++) {
			if (dataOut[i][0] == orgName) {
				return i;
			}
		}
		return null;
	};

	var dataLength = data.length;
	for ( var i = 0; i < dataLength; i++) {
		var orgName = data[i].organization.name;
		var orgIndex = findDataElement(orgName);

		if (orgIndex == null) {
			dataOut[dataOut.length] = [ orgName, 1 ];
		} else {
			dataOut[orgIndex][1] = dataOut[orgIndex][1] + 1;
		}
	}

	return dataOut;
}

/**
 * Outlines the class used by all output from the server. Each entry in the
 * table returned will include this format (in JSON).
 */
Subject = {
	id : 0,
	guid : "",
	date : "",
	refObjectName : "",
	creatorOrg : {
		id : 0,
		name : "",
		dateCreated : "",
		deleted : false
	},
	creatorSite : {
		id : 0,
		userName : "",
		dateCreated : "",
		orgId : 0,
		deleted : false
	},
	creatorUser : {
		id : 0,
		siteId : 0,
		userName : "",
		dateCreated : "",
		deleted : false
	}
}

AccessEntry = {
	id : 0,
	url : "",
	userAgent : "",
	ip : "",
	organization : {
		id : 0,
		name : "",
		dateCreated : "",
		deleted : false
	},
	site : {
		id : 0,
		name : "",
		dateCreated : "",
		deleted : false
	},
	user : {
		id : 0,
		name : "",
		dateCreated : "",
		deleted : false
	},
	date : ""
}