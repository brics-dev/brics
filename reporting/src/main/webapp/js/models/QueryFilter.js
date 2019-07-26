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
		
		id : 0
		
	},
	
	initialize : function() {
		this.set("id", this.cid, {silent: true});
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
	    "blank" : "selectedBlank"
	},
	
	toJsonObject : function() {
		var output = {};
		
		output.formUri = this.get("formUri"); // required
		output.groupName = this.get("groupName"); // required
		output.elementName = this.get("elementName"); // required
		if(this.get("selectedPermissibleValues") != null) {
			output.permissibleValues = this.get("selectedPermissibleValues");
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
		output.blank = this.get("selectedBlank");
		return output;
	},
	
	fromResponseJson : function(response) {
		// maps the data element definition into our display version of the filter's type
		var responseType = response.type;
		var inputType = response.inputRestrictions;
		
		var inputTypeMap = Config.filterTypeMapping;
		
		var type = Config.filterTypes.freeFormType; 
		if (inputType == inputTypeMap.freeForm) {
			if (responseType == inputTypeMap.alpha) {
				type = Config.filterTypes.freeFormType;
			}
			else if (responseType == inputTypeMap.numeric && response.maximumValue != null && response.minimumValue != null) {
				type = Config.filterTypes.numericRangeType;
			}
			else if (responseType == inputTypeMap.numeric && response.maximumValue == null && response.minimumValue == null) {
				type = Config.filterTypes.numericUnbounded;
			}
			else if (responseType == inputTypeMap.date) {
				type = Config.filterTypes.dateRangeType;
			}
			else if (responseType == inputTypeMap.guid) {
				type = Config.filterTypes.freeFormType;
			}
		}
		else if (inputType == inputTypeMap.singleSelect) {
			type = Config.filterTypes.permissibleValuesType;
		}
		else if (inputType == inputTypeMap.multiSelect) {
			type = Config.filterTypes.permissibleValuesType;
		}
		
		
		this.set(response, {silent: true});
		this.set({
			formUri : response.formUri,
			elementUri : response.elementUri,
			groupUri : response.groupUri,
			filterType : type,
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
			selectedBlank : response.blank
			
		}, {silent : true});
		

	}
	
	
	
});

