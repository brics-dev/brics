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
			_.defer(function() {
				QT.SavedQueryLoadUtil.loadSingleDefinedQuery(id);
			});
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
			// for reloading the base data on the back end
			query.set("onReload", true);
			query.set("applyFiltersEnabled",true);
			query.set("outputSelectionOption",data.outputCode);
			if(data.outputCode == "Permissible Value Description"){
				query.set("outputCodeSelection", data.outputCode);
			} else{
				query.set("outputCodeSelection", "pv");
			}
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
		if(formUris.length > 1) {
			QueryTool.dataCart.set("joinFormsAvailable",true);
			QueryTool.query.set("currentlyJoined", true);
			QueryTool.query.set("singleFormRequest", false);
		} else {
			QueryTool.query.set("currentlyJoined", false);
			QueryTool.query.set("singleFormRequest", true);
			
		}
		
		//Create filter objects here
		EventBus.once("load:sqFilters", QT.SavedQueryLoadUtil.onChangeTableResults);
		//set the selected forms such that we can add teh filters without run query
		var obj = { loadsq: true, notOnActivate:false, formUris:formUris };
		EventBus.trigger("runSelectForms", obj);
		
	},
	
	/**
	 * Responds to changing table results - part of Step 4
	 */
	onChangeTableResults : function() {
		var data = QT.SavedQueryLoadUtil.sqData;
		//the query has returned, apply hidden columns.
		var forms = data.query.forms;
		if (forms.length > 0) {
			forms.forEach(function(form) {
				_.defer(function(model) {
					try {
						// some saved queries use "groups", others use "repeatableGroups"
						var groupsValue = model.groups || model.repeatableGroups;
						if (typeof groupsValue != "undefined") {
							for (var g = 0; g < groupsValue.length; g++) {
								var group = groupsValue[g];
								// some saved queries use "elements", others use "dataElements"
								var elementsValue = group.elements || group.dataElements;
								// if this information isn't available, make all columns visible
								if (typeof elementsValue !== "undefined") {
									for(var e = 0; e < elementsValue.length; e++){
										var element = elementsValue[e];
										if(element.selected !== undefined && !element.selected ){
											var elementsUriObj = { 
													formName: model.shortName, 
													formUri: model.uri, 
													rgName : group.name, 
													rgUri : group.uri , 
													deName : element.shortName, 
													deUri: element.uri, 
													visible: false };
											EventBus.trigger("addTohiddenColumnsProperties", elementsUriObj);
											EventBus.trigger("query:deselectDe", elementsUriObj);
										}
									}
								}
					    	}
						}
					}
					catch(e) {
						if ( window.console && console.log ){
						console.log("There was a problem hiding a hidden column");
						}
						QT.SavedQueryLoadUtil.loadingFailed = "There was a problem loading a form in this saved query";
					}
				}, form);
			});
		}
		
		
		// the query has returned, apply the filters
		// need to get formUri, rgUri, deUri, rgName, deName
		// have deUri, rgUri
		
		var allFilters = [];
		var filterIndex = 0;
		var displayableFiltersCount = 0;
		// each form can have its own filters, so have to loop over each form
		var numForms = data.query.forms.length;
		for (var i = 0; i < numForms; i++) {
			var form = data.query.forms[i];
			// this map is used to enable creating hierarchical filters from single-dimensional filters
			var existingFilterMap = {};
			var numFilters = form.filters.length;
			for (var j = 0; j < numFilters; j++) {
				var filter = form.filters[j];
				var localData = QT.SavedQueryLoadUtil.getFilterLocalData(filter, form, filterIndex);
				filterIndex++;
				if (localData == null) {
					return;
				}
				
				// rename some values for main filter and subfilters
				QT.SavedQueryLoadUtil.filterRenameAndSetDefaults(filter);
				var combinedFilter = $.extend({}, filter, localData);
				if (typeof combinedFilter.filters !== "undefined") {
    				for (var k = 0, subFilterLen = combinedFilter.filters.length; k < subFilterLen; k++) {
    					QT.SavedQueryLoadUtil.filterRenameAndSetDefaults(combinedFilter.filters[k]);
    				}
    				allFilters.push(combinedFilter);
    				displayableFiltersCount++;
				}
				else {
					// this is an old-format filter that may or may not be hierarchial
					var convertedFilter = QT.SavedQueryLoadUtil.convert1dFilterTo2d(combinedFilter, existingFilterMap);
					if (convertedFilter != null) {
						allFilters.push(convertedFilter);
						displayableFiltersCount++;
					}
				}
			}
		}
		var numCompiledFilters = allFilters.length;
		QT.SavedQueryLoadUtil.applyFilterExpression(allFilters, data.query.filterExpression);
		if( displayableFiltersCount > 0){
			// sets up a collector for the parallelized process of finalizing the filters
			var collectApplyFilters = _.after(displayableFiltersCount, function() {
				QueryTool.query.set("doApplyFilter",true);
				EventBus.off("rendered:queryFilter", collectApplyFilters);
				EventBus.trigger("sort:filters");
				var obj = { notOnActivate:true, formUris: QueryTool.query.get("selectedForms") };
				QT.SavedQueryLoadUtil.applyFilters(); 
				EventBus.trigger("runOnlyQuery", obj);
				EventBus.trigger("set:queryState", 1);
			});
			EventBus.on("rendered:queryFilter", collectApplyFilters, this);
		} else {
			var obj = { notOnActivate:true, formUris: QueryTool.query.get("selectedForms") };
			EventBus.trigger("runQuery", obj);
			EventBus.trigger("set:queryState", 1);
		}
		
		
		for (var m = 0; m < numCompiledFilters; m++) {
			//TODO: really should create some filter registry for filters that don't exist as a data element, or another solution
			var highlightFilter = allFilters[m];
			if(highlightFilter.elementName === "highlight_diagnosis") {
				var modelData = $.extend({}, highlightFilter, {
					name: "DiagnosChangeInd",
					permissibleValues: ["No", "Yes"],
					type: "Change in Diagnosis",
					inputRestrictions: "Radio Values",
					elementName: "highlight_diagnosis",
					filterMode : "CHANGE_IN_DIAGNOSIS",
				});
				var model = new QT.QueryFilter();
				model.fromResponseJson(modelData);
				EventBus.trigger("add:queryFilter", model);
			} else {
				QT.SavedQueryLoadUtil.getFilterDetails(highlightFilter);
			}
		}
	},
	
	/**
	 * Given a single filter from a saved query, convert it to a 2D (hierarchial) filter in the new format.
	 * If this filter already has a parent filter on the page, it will not create a new one but will
	 * add this filter as a new sub-filter.  Otherwise, this function will create a new parent filter.
	 * 
	 * @return if a new parent filter is created, the filter.  Otherwise, null
	 */
	convert1dFilterTo2d : function(filter, existingFilterMap) {
		// if a parent filter for this one already exists, use that.  Otherwise, create a parent and put this
		// as the first subfilter
		var filterId = filter.formUri + "_" + filter.groupUri + "_" + filter.elementUri;
		var existingParentFilter = existingFilterMap[filterId];
		var tempQueryFilter = new QT.QueryFilter();
		if (typeof existingParentFilter !== "undefined") {
			// parent filter exists, set this as a subfilter
			var index = existingParentFilter.filters.length;
			filter.filterName = tempQueryFilter.generateFilterName(index, filter.formUri, filter.elementUri);
			existingParentFilter.filters.push(filter);
			return null;
		}
		else {
			// no parent filter exists. Create one and add this filter as the first subfilter
			filter.filters = [];
			var newSubFilter = $.extend(true, {}, filter);
			newSubFilter.filterName = tempQueryFilter.generateFilterName(0, newSubFilter.formUri, newSubFilter.elementUri, newSubFilter.groupName);
			filter.filters.push(newSubFilter);
			existingFilterMap[filterId] = filter;
			return filter;
		}
	},
	
	/**
	 * Gathers Form, Group, and Element information needed to build the filter.
	 * 
	 * @return localData object if successful, otherwise null
	 */
	getFilterLocalData : function(filter, form, filterIndex) {
		var rg = QT.SavedQueryLoadUtil.findMatchingRg(form.groups, filter.groupUri);
		var de = QueryTool.page.get("dataElements").get(filter.elementUri);
		var filterForm = QueryTool.page.get("forms").byUnknownUriVersion(form.uri);
		
		if (rg == null) {
			$.ibisMessaging("dialog", "error", "There is a problem with the defined query configuration.  Cannot continue loading");
			return null;
		}
		
		if (!filterForm) {
			$.ibisMessaging("dialog", "error", "There was a problem with one of the filters in this query.  That filter will not be included");
			return null;
		}
		
		var deName = "";
		if (de != null) {
			deName = de.get("shortName");
		} else if (filter.elementUri === "change_in_diagnosis") {
			deName = "highlight_diagnosis";
		}
		else {
			// check for DE by splitting off the end of the URI = shortname
			de = QueryTool.page.get("dataElements").byShortName(filter.elementUri.split("/").pop());
			if (de != null) {
				deName = de.get("shortName");
			}
			else {
				// we tried
				$.ibisMessaging("dialog", "error", "The data element identified by uri " + filter.elementUri + " was not found.  A filter may not be added correctly");
				return null;
			}
		}
		
		var localData = {
			formUri : filterForm.get("uri"),
			groupUri : filter.groupUri,
			elementUri : filter.elementUri,
			groupName : rg.name,
			elementName : deName,
			sortOrder: filterIndex
		};
		
		return localData;
	},
	
	filterRenameAndSetDefaults : function(filter) {
		if (typeof filter.permissibleValues !== "undefined" 
				&& filter.permissibleValues.length > 0) {
			filter.selectedPermissibleValues = filter.permissibleValues;
		}
		if (filter.maximum != null) {
			filter.selectedMaximum = filter.maximum;
		}
		if (filter.minimum != null) {
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
	},
	
	/**
	 * Applies the given filter expression to the filters in {filters}.
	 * The filters should be hierarchical Objects (not models) at this point
	 * NOTE: filterExpression could be empty, in which case we need to set defaults
	 * 
	 * @param filters a hierarchical list of QueryFilter instances (first dimension is parent filters)
	 * @param filterExpression the filter expression that needs to be applied to these filters
	 */
	applyFilterExpression : function(filters, filterExpression) {
		/**
		 * If the parent doesn't exist, it's probably a saved query.  That could be an old saved query OR a new one
		 * Old saved queries use logicBefore as default &&
		 * Old saved queries have no groupingBefore or groupingAfter
		 * New saved queries have a filterExpression which tells us the logicBefore, groupingBefore, and groupingAfter
		 */
		var parentFiltersLen = filters.length;
		if (typeof filterExpression === "undefined" || filterExpression == "" || filterExpression == null) {
			for (var i = 0; i < parentFiltersLen; i++) {
				var oldParentFilter = filters[i];
				oldParentFilter.logicBefore = "&&";
				var childFiltersLen = oldParentFilter.filters;
				for (var j = 0; j < childFiltersLen; j++) {
					var oldChildFilter = oldParentFilter.filters[j];
					oldChildFilter.groupingBefore = 0;
					oldChildFilter.groupingAfter = 0;
				}
			}
		}
		else {
			/**
			 * Break down the filterExpression into parent-filter groups.  Then check it for groupingBefore,
			 * then PARENT logicBefore, then break down by logicBefores to give to the kids
			 * Symbols:
			 * !  - NOT - only child filters
			 * && - AND - parent or child filters
			 * || - OR  - parent or child filters
			 * (  - grouping before of however many are strung together - parent filters
			 * )  - grouping after of however many are strung together - parent filters
			 */

			filterExpression = QT.FilterExpressionUtil.preprocess(filterExpression);
			var parentParts = QT.FilterExpressionUtil.splitIntoParents(filterExpression);
			
			// now all the parent parts are in parentParts
			for (var parentIndex = 0, parentPartsLen = parentParts.length; parentIndex < parentPartsLen; parentIndex++) {
				var parentPart = parentParts[parentIndex];
				var parentFilter = filters[parentIndex];
				
				// if this is the first parent filter, there will be no logicBefore and we can go straight to
				// groupingBefore and groupingAfter.  Default the first parent filter's logicBefore to ""
				if (parentIndex == 0) {
					parentFilter.logicBefore = "";
				}
				else {
					parentFilter.logicBefore = QT.FilterExpressionUtil.determineLogicBefore(parentPart);
					parentPart = QT.FilterExpressionUtil.removeLogicBefore(parentPart);
				}
				
				parentFilter.groupingBefore = QT.FilterExpressionUtil.determineGroupingBefore(parentPart);
				parentPart = QT.FilterExpressionUtil.removeGroupingBefore(parentPart);
				parentFilter.groupingAfter = QT.FilterExpressionUtil.determineGroupingAfter(parentPart);
				parentPart = QT.FilterExpressionUtil.removeGroupingAfter(parentPart);
				
				// split parentPart into the children, ensure the two groups are the same size, then process
				var childParts = QT.FilterExpressionUtil.splitIntoChildren(parentPart);
				var childPartsLen = childParts.length;
				var childIndex, childFilter;
				if (childPartsLen == parentFilter.filters.length) {
					for (childIndex = 0; childIndex < childPartsLen; childIndex++) {
						parentFilter.filters[childIndex].logicBefore = QT.FilterExpressionUtil.determineLogicBefore(childParts[childIndex]);
					}
				}
				else {
					// ERROR
					$.ibisMessaging("dialog", "error", "There was a problem with the logic statements in this query's sub-filters. They will be reset to defaults");
					for (childIndex = 0; childIndex < childPartsLen; childIndex++) {
						childFilter = parentFilter.filters[childIndex];
						if (childIndex == 0) {
							childFilter.logicBefore = "";
						}
						else {
							childFilter.logicBefore = "&&";
						}
					}
				}
			}
		}
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
				if ( window.console && console.log ){
				console.log("error getting filter details");
				}
				EventBus.trigger("close:processing");
			}
		})
	},
	
	/**
	 * Step 5 in running a saved query.  Just displays the filters which changes the data in
	 * the query results, which redraws the table
	 */
	applyFilters : function() {
		_.defer(function() {
			EventBus.trigger("viewFilters");
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
			QT.SavedQueryLoadUtil.styleJoinedForm($form, $("#firstForm"));
			
			if (formUris.length >= 1) {
				var $form = QT.SavedQueryLoadUtil.getDataCartFormByUri(formUris[1]);
				QT.SavedQueryLoadUtil.styleJoinedForm($form, $("#secondForm"));
				
				if (formUris.length >= 2) {
					var $form = QT.SavedQueryLoadUtil.getDataCartFormByUri(formUris[2]);
					QT.SavedQueryLoadUtil.styleJoinedForm($form, $("#thirdForm"));
				}
			}
			
			if (formUris.length >= 3) {
				var $form = QT.SavedQueryLoadUtil.getDataCartFormByUri(formUris[3]);
				QT.SavedQueryLoadUtil.styleJoinedForm($form, $("#fourthForm"));
			}
			
			if (formUris.length >= 4) {
				var $form = QT.SavedQueryLoadUtil.getDataCartFormByUri(formUris[4]);
				QT.SavedQueryLoadUtil.styleJoinedForm($form, $("#fifthForm"));
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
		sq.set("dateCreated", this.sqData.dateCreated);
		sq.set("lastUpdated", this.sqData.lastUpdated);
		sq.set("copyFlag", this.sqData.copyFlag);
		sq.set("outputCode", this.sqData.outputCode);
		
		// Get the permissions for the saved query.
		this.setPermsForSavedQuery(sq);
		
		return sq;
	},
	
	updateSavedQueryModel : function(model) {
		this.sqData.name = model.get("name");
		this.sqData.description = model.get("description");
		this.sqData.lastUpdated = model.get("lastUpdated");
		this.sqData.dateCreated = model.get("dateCreated");
		this.sqData.copyFlag = model.get("copyFlag");
		this.sqData.outputCode = model.get("outputCode");
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