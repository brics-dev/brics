/**
 * 
 */
QT.SavedQueryLoadUtil = {
	sqData : {},
	dataCart : null,
	loadingFailed : null,
	
	init : function(dataCart) {
		this.dataCart = dataCart;
		EventBus.on("complete:studiesFormsDataLoad", this.checkForPageSqLoad, this);
	},
	
	loadQuery : function(id) {
		this.clearDataCart(id);
	},
	
	/**
	 * Step 1 of loading a query: clears the data cart
	 */
	clearDataCart : function(id) {
		EventBus.trigger("open:processing", "Clearing current data cart");
		// says "when this event is called, to this but only once per registration"
		EventBus.once("dataCart:clear", function() {
			// won't allow the step 2 to start if there's an error during clear
			this.loadSingleDefinedQuery(id);
			// re-mark the correct saved query radio button
			$('.selectionListRadio[value="'+id+'"]').prop("checked", true);
		}, this);
		
		this.dataCart.clearDataCart();
	},
	
	/**
	 * Step 2 of loading a single defined query: requests the new query's configuration
	 */
	loadSingleDefinedQuery : function(id) {
		var data = {
			id : id
		};

		EventBus.trigger("change:processing", "Downloading defined query configuration");
		$.ajaxSettings.traditional = true;
		$.ajax({
			type: "GET",
			cache : false,
			url : "service/savedQueries/query",
			data : data,
			success : function(data, textStatus, jqXHR) {
				QT.SavedQueryLoadUtil.sqData = data;
				EventBus.trigger("loaded:savedQuery", data);
				EventBus.trigger("change:processing", "Loading your query onto the page");
				// calls the following function in the context of this class
				QT.SavedQueryLoadUtil.loadSavedQueryData.call(QT.SavedQueryLoadUtil, data.query);
			},
			error : function() {
				// TODO: fill in
				EventBus.trigger("close:processing");
			}
		});
	},
	
	/**
	 * Step 3 of loading a single defined query.  Puts the query data into the cart,
	 * selects as needed, etc.
	 * 
	 * structure:
	 *  name : "",
	 * 	forms : { 
	 * 		name : "saved query name?",
	 * 		[
	 * 			filters : [],
	 * 			groups : [
	 * 				{
	 * 					elements : [
	 * 						id : "#",
	 * 						name : "",
	 * 						uri : ""
	 * 					],
	 * 					name : "",
	 * 					uri : ""
	 * 				}
	 * 			],
	 * 			id: #,
	 * 			name : "",
	 * 			studyIds : ["#"],
	 * 			uri : ""
	 *  	]
	 *  studies : [
	 *  	{
	 *  		id : "#",
	 *  		title : "",
	 *  		uri : ""
	 *  	}
	 *  ]
	 */
	loadSavedQueryData : function(data) {
		EventBus.trigger("change:processing", "Loading forms and studies into the data cart");
		// run one form at a time, adding studies and filters as we go.  Don't run
		// the query until after it's all done
		if (!_.isEmpty(data)) {
			var query = QueryTool.page.get("query");
			
			var forms = data.forms;
			var endingNumber = 0;
			for (var i = 0; i < forms.length; i++) {
				endingNumber += forms[i].studyIds.length;
			}
			
			var studies = QueryTool.page.get("studies");
			
			var finalizer = _.after(endingNumber, function() {
				EventBus.off("added:dataCartForm:callback");
				QT.SavedQueryLoadUtil.runQuery.call(QT.SavedQueryLoadUtil);
			});
			EventBus.on("added:dataCartForm:callback", finalizer, this);
			
			if (forms.length > 0) {
				forms.forEach(function(form) {
					_.defer(function(model) {
						try {
							for (var i = 0; i < model.studyIds.length; i++) {
					    		var study = studies.byStudyId(model.studyIds[i]);
					    		if (typeof study !== "undefined" && study != null) {
					    			EventBus.trigger("addToDataCart", model.uri, study.get("uri"));
					    		}
					    		else {
					    			// TODO: can we log this?
					    			if ( window.console && console.log ){
					    			console.log("study " + study.get("uri") + " not found in page - perhaps it is archived or this user does not have access");
					    			}
					    		}
					    	}
						}
						catch(e) {
							if ( window.console && console.log ){
							console.log("There was a problem loading form with uri " + model.uri + 
									" into the data cart.  The form may have been archived.  I will stop trying to load this query.");
							}
							QT.SavedQueryLoadUtil.loadingFailed = "There was a problem loading a form in this saved query";
						}
					}, form);
				}, this);
				_.defer(function() {
					if (QT.SavedQueryLoadUtil.loadingFailed != null) {
						$.ibisMessaging("dialog", "error", QT.SavedQueryLoadUtil.loadingFailed);
						EventBus.trigger("close:processing");
						QT.SavedQueryLoadUtil.loadingFailed = null;
					}
				});
			}
			else {
				// end it, there are no forms
				EventBus.trigger("close:processing");
				$.ibisMessaging("dialog", "info", "This saved query has no forms so loading will not continue.  The saved query is loaded correctly.");
			}
		}
	},
	
	/**
	 * Step 4 in loading a saved query.  Runs the query before adding filters
	 */
	runQuery : function() {
		var formUris = QT.SavedQueryLoadUtil.sqData.query.selectedFormURIList;
		
		// check the form URIs.  If not found, try searching without version in uri
		var formsCollection = QueryTool.page.get("forms");
		for (var k = 0; k < formUris.length; k++) {
			var form = formsCollection.byUnknownUriVersion(formUris[k]);
			if (form) {
				// replace the URI in the list with the versioned one, just in case
				formUris[k] = form.get("uri");
			}
			else {
				// if we still can't find it, tell the user and stop!
				$.ibisMessaging("dialog", "error", "There was a problem finding one of the selected forms in this query");
				EventBus.trigger("close:processing");
				return;
			}
			// no need to do anything if it's already right
		}
		
		
		QueryTool.query.set("selectedForms", formUris);
		QT.SavedQueryLoadUtil.styleUsedForms(formUris);
		var obj = { notOnActivate:true, formUris: formUris };
    	EventBus.trigger("runQuery", obj);
    	// sets up a one-time listener for the query's completion
    	EventBus.listenToOnce(QueryTool.query, "change:tableResults", function() {
    		var data = QT.SavedQueryLoadUtil.sqData;
    		// the query has returned, apply the filters
    		// need to get formUri, rgUri, deUri, rgName, deName
    		// have deUri, rgUri
    		
    		var allFilters = [];
    		// each form can have its own filters, so have to loop over each form
    		var numForms = data.query.forms.length;
    		for (var i = 0; i < numForms; i++) {
    			var form = data.query.forms[i];
    			var numFilters = form.filters.length;
    			for (var j = 0; j < numFilters; j++) {
    				var filter = form.filters[j];
    				// find the names based on URI
    				var deName = "";
    				var rgName = "";
    				var rg = QT.SavedQueryLoadUtil.findMatchingRg(form.groups, filter.groupUri);
    				if (rg != null) {
    					rgName = rg.name;
    					var de = QueryTool.page.get("dataElements").get(filter.elementUri);
    					if (de != null) {
    						deName = de.get("shortName");
    					}
    					else {
    						$.ibisMessaging("dialog", "error", "The data element identified by uri " + filter.elementUri + " was not found.  A filter may not be added correctly");
    					}
    				}
    				else {
    					$.ibisMessaging("dialog", "error", "There is a problem with the defined query configuration.  Cannot continue loading");
    					// full stop
    					return;
    				}
    				
    				// check the form URI
    				var filterForm = QueryTool.page.get("forms").byUnknownUriVersion(filter.formUri);
    				if (filterForm) {
    					filter.formUri = filterForm.get("uri");
        				var localData = {
    						formUri : filter.formUri,
    						groupUri : filter.groupUri,
    						elementUri : filter.elementUri,
    						groupName : rgName,
    						elementName : deName
    					};
        				if (typeof filter.permissibleValues !== "undefined" 
        						&& filter.permissibleValues.length > 0) {
        					filter.selectedPermissibleValues = filter.permissibleValues;
        				}
        				if (filter.maximum != "") {
        					filter.selectedMaximum = filter.maximum;
        				}
        				if (filter.minimum != "") {
        					filter.selectedMinimum = filter.minimum;
        				}
        				if (!filter.dateMin) {
        					filter.dateMin = null;
        				}
        				else {
        					var dateMin = new Date(filter.dateMin);
            				filter.dateMin = dateMin.getMonth() + 1 + "/" + dateMin.getDate() + "/" + dateMin.getFullYear().toString().substr(-2);
        				}
        				
        				if (!filter.dateMax) {
        					filter.dateMax = null;
        				}
        				else {
        					var dateMax = new Date(filter.dateMax);
            				filter.dateMax = dateMax.getMonth() + 1 + "/" + dateMax.getDate() + "/" + dateMax.getFullYear().toString().substr(-2);
        				}
        				
        				var combinedData = $.extend({}, filter, localData);
        				allFilters.push(combinedData);
    				}
    				else {
    					$.ibisMessaging("dialog", "error", "There was a problem with one of the filters in this query.  That filter will not be included");
    				}
 
    			}
    		}
    		
    		var numFilters = allFilters.length;
    		
    		var collectApplyFilters = _.after(numFilters, function() {
				EventBus.off("add:queryFilter", collectApplyFilters);
	    		QT.SavedQueryLoadUtil.applyFilters();
			});
			EventBus.on("add:queryFilter", collectApplyFilters, this);
    		
    		for (var j = 0; j < numFilters; j++) {
    			QT.SavedQueryLoadUtil.getFilterDetails(allFilters[j]);
    		}
    		

    	});
	},
	
	getFilterDetails : function(filter) {
		// this is the only other place we get de filter details other than Query
		$.ajax({
			type: "GET",
			cache : false,
			url : "service/dataCart/deFilterDetails",
			data : {
				formUri : filter.formUri,
				rgName : filter.groupName,
				deName : filter.elementName
			},
			success : function(data, textStatus, jqXHR) {
				var modelData = $.extend({}, filter, data);
				var model = new QT.QueryFilter();
				model.fromResponseJson(modelData);
				EventBus.trigger("add:queryFilter", model);
			},
			error : function() {
				// TODO: fill in
				if ( window.console && console.log ){
				console.log("error getting filter details");
				}
			}
		})
	},
	
	/**
	 * Step 5 in running a saved query.  Just applies the filters which changes the data in
	 * the query results, which redraws the table
	 */
	applyFilters : function() {
		_.defer(function() {
			EventBus.trigger("applyFilters");
		});
		
	},
	
	findMatchingRg : function(groups, uri) {
		for (var i = 0, groupLength = groups.length; i < groupLength; i++) {
			if (groups[i].uri == uri) {
				return groups[i];
			}
		}
	},
	// I'm keeping these two separate because the format of the json COULD change
	findMatchingDe : function(elements, uri) {
		for (var k = 0, elementLength = elements.length; k < elementLength; k++) {
			if (elements[k].uri == uri) {
				return elements[k];
			}
		}
	},
	
	checkForPageSqLoad : function() {
		var urlObj = urlObject(window.location.href);
		if (typeof urlObj.parameters.savedQueryId !== "undefined") {
			this.loadQuery(urlObj.parameters.savedQueryId);
		}
	},
	
	styleUsedForms : function(formUris) {
		if (formUris.length == 1) {
			// just highlight the correct form
			var $form = QT.SavedQueryLoadUtil.getDataCartFormByUri(formUris[0]);
			$form.addClass("dataCartActive");
		}
		else {
			// multiple forms must be moved to the correct container
			var $form = QT.SavedQueryLoadUtil.getDataCartFormByUri(formUris[0]);
			QT.SavedQueryLoadUtil.styleJoinedForm($form, $("#primaryForm"));
			
			if (formUris.length >= 1) {
				var $form = QT.SavedQueryLoadUtil.getDataCartFormByUri(formUris[1]);
				QT.SavedQueryLoadUtil.styleJoinedForm($form, $("#secondaryForm"));
				
				if (formUris.length >= 2) {
					var $form = QT.SavedQueryLoadUtil.getDataCartFormByUri(formUris[2]);
					QT.SavedQueryLoadUtil.styleJoinedForm($form, $("#tertiaryForm"));
				}
			}
		}
	},
	
	getDataCartFormByUri : function(uri) {
		var newId = "refineDataForm_" + uri;
		return $('[id="' + newId + '"]');
	},
	
	styleJoinedForm : function($form, $target) {
		$form.addClass("dataCartActive");
		$form.appendTo($target);
		$form.css("left", 0);
		$form.css("top", 0);
		$form.css("position", "absolute");
	},
	
	/**
	 * Fetches saved query data from the server, and constructs a SavedQuery model 
	 * out of that data.
	 * 
	 * @param id - The ID of the saved query object to retrieve from the server.
	 * @returns A SavedQuery model with data retrieved from the server, or an empty model
	 * if there is no saved query data that is associated with the given ID.
	 * @throws Error When any of the web service calls fail, or when data returned from these web
	 * service calls is invalid.
	 */
	getSavedQueryModel : function(id) {
		var sq = new QT.SavedQuery();
		
		// Set the model fields from the loaded "sqData" object.
		sq.set("id", this.sqData.id);
		sq.set("name", this.sqData.name);
		sq.set("description", this.sqData.description);
		sq.set("lastUpdated", this.sqData.lastUpdated);
		sq.set("copyFlag", this.sqData.copyFlag);
		
		// Get the permissions for the saved query.
		this.setPermsForSavedQuery(sq);
		
		return sq;
	},
	
	updateSavedQueryModel : function(model) {
		this.sqData.name = model.get("name");
		this.sqData.description = model.get("description");
		this.sqData.lastUpdated = model.get("lastUpdated");
		this.sqData.copyFlag = model.get("copyFlag");
	},
	
	/**
	 * Populates the given saved query model with permission data from the server.
	 * 
	 * @param {SavedQuery} sq - A saved query model whose permissions will be updated.
	 */
	setPermsForSavedQuery : function(sq) {
		if ( (typeof sq != "undefined") && (sq != null) ) {
			$.ajax({
				type : "GET",
				cache : false,
				dataType : "xml",
				url : System.urls.getEntitiesByTypeObjId,
				async : false,
				data : {
					entityId : sq.get("id"),
					entityType : "SAVED_QUERY"
				},
				
				success : function(data) {
					var $resp = $(data);
					var userColl = sq.get("linkedUsers");
					
					// Loop over each entity map from the response, and set the permissions for the loaded saved query.
					$resp.find("entityMapGroup").each(function() {
						var $em = $(this);
						var $account = $em.children("account");
						var $usr = $account.children("user");
						var user = new QT.User({
							id : Number($account.children("id").text()),
							userName : $account.children("userName").text(),
							firstName : $usr.children("firstName").text(),
							lastName : $usr.children("lastName").text(),
							email : $usr.children("email").text()
						});
						var perm = user.get("assignedPermission");
						
						// Set the user's assigned permission details.
						perm.set("entityMapId", Number($em.children("id").text()));
						perm.set("entityId", Number($em.children("entityId").text()));
						
						// Convert the PermissionType value to the permission name.
						switch ($em.children("permission").text()) {
							case "READ":
								perm.set("permission", "Read");
								break;
							case "WRITE":
								perm.set("permission", "Write");
								break;
							case "ADMIN":
								perm.set("permission", "Admin");
								break;
							case "OWNER":
								perm.set("permission", "Owner");
								break;
							default:
								console.error("Unknown permission (user:" + user.get("userName") 
									+ ", permission: " + $em.children("permission").text() 
									+ ") encountered during the save query load for edit.");
								throw "Invaild permission encountered while loading the defined query for editing.";
						}
						
						userColl.add(user);
					});
				}
			})
			.fail(function(jqXHR, textStatus, errorThrown) {
				console.error("Could not get a saved query permissions from web service: " + jqXHR.status + " : " + errorThrown);
				throw "Could not load the defined query permissions for editing.";
			});
		}
		else {
			throw "Invalid defined query object encountered.";
		}
	}
};