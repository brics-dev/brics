/**
 * Allows validation of editor fields and other validation tasks.
 * 
 * Pass Validation.validate() a set of ValidationRule objects and a model
 * to compare those rules against.  validate() will return true on success
 * or false on error.  The failed fields will be stored in failedFields and
 * can be retrieved by using getFailedFields()
 */
var Validation = {
	failedFields : [],
	errorMessage : "",
	/**
	 * Validates the field based on the settings of this object.
	 * Loops through rules and runs validation on each
	 * 
	 * @param rules array of ValidationRule to check against
	 * @param model the model to check against
	 * @return boolean true if passed; otherwise false
	 */
	validate : function(rules, model) {
		this.resetFailedFields();
		this.resetErrorMessage();
		
		for (var i = 0; i < rules.length; i++) {
			var rule = rules[i];
			if (rule.required && !this.require(rule, model)) {
				this.failedFields.push({name:rule.fieldName, description: rule.description});
			}
			
			if (rule.match) {
				if (!this.match(rule, model)) {
					this.failedFields.push({name:rule.fieldName, description: " " + rule.description});
					this.errorMessage += "<br />"+rule.description;
				}
			}
		}
		return this.failedFields.length == 0;
	},
	
	/**
	 * Validates a required field
	 * 
	 * @param model the model to check against
	 * @return boolean true if passed; otherwise false
	 */
	require : function(rule, model) {
		var fieldValue = model.get(rule.fieldName);
		if (fieldValue == null || fieldValue == "") {
			return false;
		}
		return true;
	},
	
	/**
	 * Matches the field value to the regex supplied at instantiation
	 * OR uses the passed match function
	 * 
	 * @return boolean true if passed; otherwise false
	 */
	match : function(rule, model) {
		if (typeof rule.match === "function") {
			return rule.match(model);
		}
		else {
			var fieldValue = model.get(rule.fieldName);
			var match = fieldValue.match(rule.match);
			if (match) {
				return true;
			}
			else {
				return false;
			}
		}
	},
	
	resetFailedFields : function() {
		this.failedFields = [];
	},
	
	resetErrorMessage : function() {
		this.errorMessage = "Some fields in this editor did not validate: ";
	},
	
	getFailedFields : function() {
		return this.failedFields;
	}
};

/**
 * This is an instantiable class allowing the developer to set up an array
 * of ValidationRule elements to pass to the Validation class for validating.
 * 
 * If a matchParam is not specified, the field is assumed to be required only.
 * 
 * @param field String the field name to validate
 * @param matchParam (optional) a regex string to match or a comparison function
 * @param reqd (optional) whether the field is required.  Added to allow both required AND match types
 * @returns
 */
function ValidationRule(config) {
	this.defaults = {
		fieldName : "",
		match : /.+/igm,
		required: false,
		description: ""
	};
	var settings = $.extend({}, this.defaults, config);
	var values = _.keys(settings);
	for (var i = 0; i < values.length; i++) {
		this[values[i]] = settings[values[i]];
	}
	
	if (typeof this.match === "undefined") {
		// if there is no regex string, we assume they meant that it's required
		this.required = true;
	}
	
	if (this.fieldName === "") {
		throw new Error("the field name was undefined when initializing ValidationRule");
	}
}