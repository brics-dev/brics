/**
 * 
 */
QT.QueryFilter = BaseModel.extend({
	
	defaults : {
		// from de
		formUri : "",
		elementUri : "",
		groupUri : "",
		elementName : "",
		groupName : "",
		filterType : Config.filterTypes.freeFormType,
		filterJavaType : "FREE_FORM",
		filterMode : "",
		
		mode: "inclusive", //This is used to define is the filter is chosen for inclusive or exact, only for multi-select
		permissibleValues : [], //array of strings
		maximum : 0,
		minimum : 0,

		//once user fills in the data, these will have values
		selectedPermissibleValues : null, //array of strings
		selectedMaximum : null,
		selectedMinimum : null,
		selectedFreeFormValue : null,
		selectedDateMin : null,
		selectedDateMax : null,
		selectedBlank : false,
		multiData: false,
		selectOther: false,
		
		// this is either empty (for an actual filter) or not (a primary filter)
		filters : null, 
		
		specialFilters: null, //This just contains a special filter, either show blank or diagnosis change
		showFilterLogicSelect: true, //Top drop down
		showFilterLogicOr: true, //Top drop down Or
		showFilterLogicAnd: true, //Top drop down And
		showGenericSelect : true, //bottom drop down
		showAndOption : true, //NOTe: Maybe we should have an property that lists all the options that are wanted
		showBlankFilter: true,
		
		
		logicBefore : "",
		groupingBefore : 0, // can be 0, 1, 2, or 3
		groupingAfter : 0,
		filterName : "",
		
		inputRestrictions : "",
		showInclusiveExactToggle: "hide",
		
		sortOrder: "",
		
		id : 0
		
	},
	
	/**
	 * Initializes this model.
	 * 
	 * @param subFilter boolean true if this represents a sub-filter or falsy otherwise
	 */
	initialize : function(subFilter) {
		this.set("id", this.cid, {silent: true});
		this.set("filters", new QT.QueryChildFilters());
	},
	
	serverParams : {
		"elementUri" : "elementUri",
	    "groupUri" : "groupUri",
	    "permissibleValues" : "selectedPermissibleValues",
	    "maximum" : "selectedMaximum",
	    "minimum" : "selectedMinimum",
	    "freeFormValue" : "selectedFreeFormValue",
	    "dateMin" : "selectedDateMin",
	    "dateMax" : "selectedDateMax",
	    "blank" : "selectedBlank",
	    "mode" : "mode",
	    "multiData": "multiData"
	},
	
	toJsonObject : function() {
		// NOTE: this method now refers to the CHILD FILTER, not the parent
		var output = {};
		output.name = this.get("filterName"); // required
		output.formUri = this.get("formUri"); // required
		output.groupName = this.get("groupName"); // required
		output.elementName = this.get("elementName"); // required
		output.groupUri = this.get("groupUri");
		output.elementUri = this.get("elementUri");
		if(this.get("selectedPermissibleValues") != null) {
			output.permissibleValues = this.get("selectedPermissibleValues");
		}
		if(this.get("filterMode") != null) {
			output.filterMode = this.get("filterMode");
		}
		if(this.get("filterJavaType") != null) {
			output.filterJavaType = this.get("filterJavaType");
		}
		if(this.get("filterType") != null) {
			output.filterType = this.get("filterType");
		}
		if(this.get("mode") != null) {
			output.mode = this.get("mode");
		}
		if(this.get("selectedMaximum") != null) {
			output.maximum = this.get("selectedMaximum");
		}
		if(this.get("selectedMinimum") != null) {
			output.minimum = this.get("selectedMinimum");
		}
		if(this.get("selectedFreeFormValue") != null) {
			output.freeFormValue = this.get("selectedFreeFormValue");
		}
		if(this.get("selectedDateMin") != null) {
			output.dateMin = this.get("selectedDateMin");
		}
		if(this.get("selectedDateMax") != null) {
			output.dateMax = this.get("selectedDateMax");
		}
		output.multiData = this.get("multiData");
		//output.blank = this.get("selectedBlank");
		
		output.logicBefore = this.get("logicBefore");
		output.groupingBefore = this.get("groupingBefore");
		output.groupingAfter = this.get("groupingAfter");
		output.filterName = this.get("filterName");
				
		var templateFilters = [];
		this.get("filters").forEach(function(filter, index) {
			if (!filter.get("filterName")) {
				filter.generateFilterName(index);
			}
			templateFilters.push(filter.toJsonObject());
		});
		output.filters = templateFilters;
		
		return output;
	},
	
	fromResponseJson : function(response) {
		// maps the data element definition into our display version of the filter's type
		var responseType = response.type;
		var inputType = response.inputRestrictions;
		var inputTypeMap = Config.filterTypeMapping;
		
		//TODO: make sure this works for unique types such as show blanks
		var type = Config.filterTypes.freeFormType; 
		var javaType = "FREE_FORM";
		var showAndOption = true;
		var showGenericSelect = true;
		var showFilterLogicSelect = true;
		var showFilterLogicOr = true;
		var showFilterLogicAnd = true;
		var selectOther = false;
		var showBlankFilter = true;
		
		var showInclusiveExactToggleValue = "hide"; //This is used by the multi-select filters
		if (inputType == inputTypeMap.freeForm) {
			 if (response.permissibleValues.length > 0) {
				type = Config.filterTypes.permissibleValuesType;
				javaType = "SINGLE_SELECT";
				selectOther = true;
			 }
			else if(responseType == inputTypeMap.alpha) {
				type = Config.filterTypes.freeFormType;
				javaType = "FREE_FORM";
				showInclusiveExactToggleValue =  "show"; //this is not a boolean because it's css class
			}
			else if (responseType == inputTypeMap.numeric && response.maximumValue != null && response.minimumValue != null) {
				// specifically for the AgeYrs data element in pdbp for non-admins
				if (System.environment == "pdbp"
					&& response.elementName == "AgeYrs"
					&& !System.user.isSysAdmin 
					&& !System.user.isQTAdmin) {
						type = Config.filterTypes.multiRangeType;
						javaType = "MULTI_RANGE";
						showAndOption = false;
						showFilterLogicAnd = false;
				}
				else {
					type = Config.filterTypes.numericRangeType;
					javaType = "RANGED_NUMERIC";
					showAndOption = false;
					showFilterLogicAnd = false;
				}
			}
			else if (responseType == inputTypeMap.numeric && response.maximumValue == null && response.minimumValue == null) {
				type = Config.filterTypes.numericUnbounded;
				javaType = "RANGED_NUMERIC";
				showAndOption = false;
				showFilterLogicAnd = false;
				
			}
			else if (responseType == inputTypeMap.date) {
				type = Config.filterTypes.dateRangeType;
				javaType = "DATE";
				showAndOption = false;
				showFilterLogicAnd = false;
			}
			else if (responseType == inputTypeMap.guid) {
				type = Config.filterTypes.freeFormType;
				javaType = "DELIMITED_MULTI_SELECT";
				showGenericSelect = false;
				showFilterLogicOr = false;
				showFilterLogicAnd = false;
				
			}else if (responseType == inputTypeMap.triplanar 
					|| responseType == inputTypeMap.thumbnail
					|| responseType == inputTypeMap.file) {
				showAndOption = true;
				showFilterLogicAnd = true;	
				showInclusiveExactToggleValue =  "show"; //this is not a boolean because it's css class
			}	
			else if(responseType == inputTypeMap.biosample ) {
				javaType = "DELIMITED_MULTI_SELECT";
				showInclusiveExactToggleValue =  "show"; //this is not a boolean because it's css class
			}
		}
		else if (inputType == inputTypeMap.radioSelect) {
			type = Config.filterTypes.radioFormType;
		}
		else if (inputType == inputTypeMap.singleSelect) {
			type = Config.filterTypes.permissibleValuesType;
			javaType = "SINGLE_SELECT";
			showGenericSelect = false;
			showFilterLogicSelect = false;
			
		}
		else if (inputType == inputTypeMap.multiSelect) {
			type = Config.filterTypes.permissibleValuesType;
			javaType = "MULTI_SELECT";
			showInclusiveExactToggleValue =  "show"; //this is not a boolean because it's css class
		}
		
		if (responseType == inputTypeMap.changeInDiagnosis) {
			javaType = "CHANGE_IN_DIAGNOSIS";
		}
		
		if (responseType == inputTypeMap.dataset) {
			javaType = "DATASET";
			showBlankFilter = false;
		}
		
		var filters = new QT.QueryChildFilters();
		var responseFilters = response.filters;
		// this method is used for loading the parent filter too so it's possible there aren't children
		if (responseFilters != null && responseFilters.length > 0) {
			for (var i = 0, subFiltersLen = responseFilters.length; i < subFiltersLen; i++) {
				var modelData = $.extend({},  response, responseFilters[i]);
				modelData.filters = [];
				var subFilter = new QT.QueryFilter();
				subFilter.fromResponseJson(modelData);
				filters.add(subFilter);
			}
		}
		var specialFilters = new QT.QueryChildFilters(); //NOTE: I thought it a good idea not to recreate a new collection type for this
		if (typeof response.logicBefore === "undefined") {
			response.logicBefore = "&&";
		}
		
		this.set({
			formUri : response.formUri,
			elementUri : response.elementUri,
			groupUri : response.groupUri,
			filterType : type,
			filterJavaType: javaType,
			filterMode : response.filterMode,
			mode: response.mode || "inclusive",
			maximum : response.maximumValue,
			minimum : response.minimumValue,
			permissibleValues : response.permissibleValues,
			groupName : response.groupName,
			elementName : response.elementName,
			
			selectedFreeFormValue : response.freeFormValue,
			selectedDateMin : response.dateMin,
			selectedDateMax : response.dateMax,
			
			selectedPermissibleValues : response.selectedPermissibleValues,
			selectedMaximum : response.selectedMaximum,
			selectedMinimum : response.selectedMinimum,
			
			filters : filters,
			logicBefore : response.logicBefore,
			groupingBefore : response.groupingBefore || 0,
			groupingAfter : response.groupingAfter || 0,
			specialFilters : specialFilters,
			showGenericSelect: (response.showGenericSelect != undefined) ? response.showGenericSelect: showGenericSelect,
			showFilterLogicSelect : showFilterLogicSelect,
			showFilterLogicOr: showFilterLogicOr,
			showFilterLogicAnd : showFilterLogicAnd,
			showAndOption : showAndOption,
			filterName : response.filterName,
			selectedBlank : response.blank,
			multiData: response.multiData,
			inputRestrictions: inputType,
			showInclusiveExactToggle: showInclusiveExactToggleValue,
			selectOther: selectOther,
			showBlankFilter: showBlankFilter,
			sortOrder : response.sortOrder || 0
			
		}, {silent : true});
		

	},
	
	toString : function(index) {
		var compiledString = "";
		var filters = this.get("filters");
		var logicBefore = this.get("logicBefore");
		var filtersLen = filters.length;
		if (filtersLen > 0) {
			for (var j = 0; j < this.get("groupingBefore"); j++) {
				compiledString += "(";
			}
			for (var i = 0; i < filtersLen; i++) {
				var filter = filters.at(i);
				compiledString += filter.toString(i);
			}
			for (var k = 0; k < this.get("groupingAfter"); k++) {
				compiledString += ")";
			}
			return compiledString;
		}
		else {
			// Stringifies this single filter.  This filter should have NO sub-filters
			// general stuff for all filters
			if (logicBefore != "none") {
				compiledString += " " + logicBefore + " ";
			}
			compiledString += this.get("formUri") + "_" + this.get("name") + "_" + index;
			
			return compiledString;
		}
	},
	
	toQueryLogicString : function() {
		var filterType = this.get("filterType");
		var filterName = this.get("filterName");
		var pvOptions = this.get("permissibleValues");
		var selectedPVs = this.get("selectedPermissibleValues") || [];
		var combinedValues = "";
		
		if (filterType == Config.filterTypes.freeFormType) {
				return filterName + " = \"" + this.get("selectedFreeFormValue") + "\"";
		}
		else if (filterType == Config.filterTypes.numericRangeType) {
			return "(" + filterName + " >= " + this.get("selectedMinimum") + " AND " + filterName + " <= " + this.get("selectedMaximum") + ")";
		}
		else if (filterType == Config.filterTypes.dateRangeType) {
			return "(" + filterName + " >= " + this.get("selectedDateMin") + " AND " + filterName + " <= " + this.get("selectedDateMax") + ")";
		}
		else if (filterType == Config.filterTypes.permissibleValuesType) {
			var output = "";
			var mode = this.get("mode");
			var numSelectedPVs = (selectedPVs == null) ? 0 : selectedPVs.length;
			if (mode == "exact") {
				for (var j = 0; j < numSelectedPVs; j++) {
					if (j != 0) {
						combinedValues += " AND ";
					}
					combinedValues += filterName + " = " + selectedPVs[j];
				}
				output = "(" + combinedValues + ")";
			}
			else {
				for (var i = 0; i < numSelectedPVs; i++) {
					if (i != 0) {
						combinedValues += ", ";
					}
					combinedValues += selectedPVs[i];
					if (i == 4) {
						combinedValues += " ...";
						break;
					}
				}
				if(combinedValues != "") {
					output = "(" + filterName + " IN (" + combinedValues + "))";
				}
			}
			
			if (this.get("multiData")) {
				var multiDataOutput = "(" + filterName + ".size > 1)";
				if (output != "") {
					return "(" + output + " AND " + multiDataOutput + ")";
				}
				else {
					return multiDataOutput;
				}
			}
			return output;
		}
		else if (filterType == Config.filterTypes.numericUnbounded) {
			return "(" + filterName + " >= " + this.get("selectedMinimum") + " AND " + filterName + " <= " + this.get("selectedMaximum") + ")";
		}
		else if (filterType == Config.filterTypes.radioFormType) {
			var numSelectedPVs = (selectedPVs == null) ? 0 : selectedPVs.length;
			for (var k = 0; k < numSelectedPVs; k++) {
				if (k != 0) {
					combinedValues += ", ";
				}
				combinedValues += selectedPVs[k];
				if (k == 4) {
					combinedValues += " ...";
					break;
				}
			}
			return "(" + filterName + " IN (" + combinedValues + "))";
		}
		else if (filterType == Config.filterTypes.multiRangeType) {
			for (var k = 0, klen = selectedPVs.length; k < klen; k++) {
				if (k != 0) {
					combinedValues += ", ";
				}
				combinedValues += selectedPVs[k];
			}
			return "(" + filterName + " IN (" + combinedValues + "))";
		}
	},

	/**
	 * Generates a name for this filter.  Requires the formUri and elementName
	 * properties be set.
	 * 
	 * @param index - the index of this filter in the parent filter's "filter" collection
	 * @return String name for this filter
	 * @sideEffect sets the filter name
	 */
	generateFilterName : function(index, formUri, elementUri, groupName) {
		formUri = formUri || this.get("formUri");
		elementUri = elementUri || this.get("elementUri");
		groupName = groupName || this.get("groupName");
		groupName = groupName.split(' ').join('$');
		var elementName = elementUri.substring(elementUri.lastIndexOf('/') + 1);
		var formUriName = formUri.substring(formUri.lastIndexOf('/') + 1);
		var name = formUriName + "_" + groupName + "_" + elementName + "_" + index;
		this.set("filterName", name);
		return name;
	},
	
	/**
	 * This method will check if the filter is blank
	 * 
	 */
	isBlank : function() {
		var isBlank = true;
		
		
		if(this.get("selectedPermissibleValues") != null) {
			isBlank = false;
		}
		if(this.get("selectedMaximum") != null && this.get("selectedMinimum") != null) {
			isBlank = false;
		}
		if(this.get("selectedFreeFormValue") != null) {
			isBlank = false;
		}
		if(this.get("selectedDateMin") != null) {
			isBlank = false;
		}
		if(this.get("selectedDateMax") != null) {
			isBlank = false;
		}
		
		if((this.get("multiData") != null) && (this.get("multiData") != false)) {
			isBlank = false;
		}
		return isBlank;
	}
});

