/**
 * 
 */
QT.QueryFilters = Backbone.Collection.extend({
	
	initialize : function() {
		this.model = QT.QueryFilter;
		this.comparator = "sortOrder";
	},
	
	findByElementAndGroup : function(elementUri,groupUri) {
		return this.findWhere({
			elementUri : elementUri,
			groupUri : groupUri	
		});	
	},
	
	toJson : function() {
		var outputArr = [];
		this.forEach(function(Queryfilter){
			var childFilters = Queryfilter.get("filters");
			if (childFilters instanceof Backbone.Collection) {
				childFilters.forEach(function(childFilter) {
					outputArr.push(childFilter.toJsonObject());
				});
			}
			
			//add special filters
			var specialFilters = Queryfilter.get("specialFilters");
			if (specialFilters instanceof Backbone.Collection) {
				specialFilters.forEach(function(specialFilter) {
					outputArr.push(specialFilter.toJsonObject());
				});
			}
		});
		return outputArr;
	},
	
	/*
	 * This method is used to check if filters are empty, and if they are don't include them in the
	 * output array
	 */
	toJsonForQuery : function() {
		var outputArr = [];
		this.forEach(function(Queryfilter){
			var childFilters = Queryfilter.get("filters");
			if (childFilters instanceof Backbone.Collection) {
				childFilters.forEach(function(childFilter) {
					if(!childFilter.isBlank()) {
						outputArr.push(childFilter.toJsonObject());
					}
				});
			}
			
			//add special filters
			var specialFilters = Queryfilter.get("specialFilters");
			if (specialFilters instanceof Backbone.Collection) {
				specialFilters.forEach(function(specialFilter) {
						outputArr.push(specialFilter.toJsonObject());	
				});
			}
		});
		return outputArr;
	},
	
	/**
	 * compiles the filter tree into its correct full string expression
	 */
	getExpression : function() {
		var output = "";
		var specialFilters = [];
		for (var i = 0, parentFiltersLength = this.length; i < parentFiltersLength; i++) {
			var parentFilter = this.at(i);
			var childFilters = parentFilter.get("filters");
			specialFilters = parentFilter.get("specialFilters");
			if (i != 0 && output != "") {
				if(!this.parentFilterIsBlank(i)){
					// AND is the default logicBefore for non-first filters
					output += parentFilter.get("logicBefore") || "&&";
				}
			}
			
			var groupingBefore = (parentFilter.get("groupingBefore")) ? parentFilter.get("groupingBefore") : 0;
			for (var j = 0; j < groupingBefore; j++) {
				output += "(";
			}
			
			var emptyChildren = true;
			if(childFilters.length > 0 ) {
				// adding an open parenthesis for the child filters
				var childFiltersOutput = "(";
				for (var k = 0, childLength = childFilters.length; k < childLength; k++) {
					var childFilter = childFilters.at(k);
					if(!childFilter.isBlank()) {
						emptyChildren = false;
						var childLogicBefore = childFilter.get("logicBefore");
						// adjust for subsequent children having NOT logic in front of them
						if (k != 0) {
							if (childLogicBefore == "!") {
								childLogicBefore = "&&!";
							}
							childFiltersOutput += " " + childLogicBefore + " ";
						} else if(childLogicBefore == "!") {
							childFiltersOutput += " " + childLogicBefore + " ";
						}
						childFiltersOutput += childFilter.get("filterName") || childFilter.generateFilterName(k);
					}
				}
				childFiltersOutput += ")";
				
				if(!emptyChildren) {
					output += childFiltersOutput;
				}
			}
			
			
			//add special filters here
			if(specialFilters.length > 0) {
				//add an && if we are appending to child filters
				if(childFilters.length > 0 && !emptyChildren) {
					output += " || (";
				} else {
					output += "(";
				}
				
				for (var s = 0, specialLength = specialFilters.length; s < specialLength; s++) {
					var specialFilter = specialFilters.at(s);
					if(s != 0) {
						output += " && ";
					}
					output += specialFilter.get("filterName");
				}
				output += ")";
			}
			
			var groupingAfter = (parentFilter.get("groupingAfter")) ? parentFilter.get("groupingAfter") : 0;
			for (var m = 0; m < groupingAfter; m++) {
				output += ")";
			}
		}
		return output;
	},
	parentFilterIsBlank : function (key) {
		var parentFilter = this.at(key);
		var childFilters = parentFilter.get("filters");
		var specialFilters = parentFilter.get("specialFilters");
		
		if(childFilters.length > 0 ) {
			for (var k = 0, childLength = childFilters.length; k < childLength; k++) {
				var childFilter = childFilters.at(k);
				if(!childFilter.isBlank()) {
					return false;
				}
			}
		}
		
		
		//add special filters here
		if(specialFilters.length > 0) {
			return false;
		}
		
		return true;
		
	}
});