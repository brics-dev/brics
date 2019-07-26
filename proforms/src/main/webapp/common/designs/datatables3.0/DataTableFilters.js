/**
 * Manages filters across all tables on the current page.  Includes 
 * adding/removing and managing state of the filters.
 */
var DataTableFilters = {
	filters : {
		
	},
	filtersForAll : [],
	
	defaults : {
		
	},
	
	init : function(config) {
		
	},
	
	start : function() {
		var keys = _.keys(this.filters);
		for (var i = 0; i < keys.length; i++) {
			var id = keys[i];
			// constructs a function for the particular table
			$.fn.dataTableExt.afnFiltering.push(this.constructTest(id));
		}
	},
	
	add : function(filterObj) {
		var containerId = filterObj.dtContainerId;
		if (containerId == null) {
			this.filtersForAll.push(filterObj);
			// add to all and also add to the others
			var otherNames = _.keys(this.filters);
			for (var i = 0; i < otherNames.length; i++) {
				this.filters[otherNames[i]].push(filterObj);
			}
		}
		else {
			if (typeof this.filters[containerId] === "undefined") {
				// if the filter container doesn't exist, create and add all of "all" 
				this.copyAllTo(containerId);
			}
			this.filters[containerId].push(filterObj);
		}
	},
	
	copyAllTo : function(containerId) {
		for (var i = 0; i < this.filtersForAll.length; i++) {
			this.filters[containerId].push(this.filtersForAll[i]);
		}
	},
	
	constructTest : function(containerId) {
		var filters = this.filters[containerId];
		return function(oSettings, aData, iDataIndex) {
			for (var i = 0; i < filters.length; i++) {
				if (!filters[i].test(oSettings, aData, iDataIndex)) {
					return false;
				}
			}
		}
	},
	
	/**
	 * Tests a single row of the table with logic in all of the registered filters.
	 * The filters operate in the order they were added.
	 * 
	 * @param {Object} oSettings table's settings
	 * @param {String[]} aData holds the row's data 
	 * @param iDataIndex
	 * @returns {Boolean}
	 */
	test : function(oSettings, aData, iDataIndex) {// constructed by constructTest()
	}
};


/**
 * 
 */

/**
 * Constructor for DataTableFilter objects
 */
function DataTableFilter(config) {
	this.value = "";			// current value of the filter setting
	
	var defaults = {
		dtContainerId : null,	// ID of the .dataTableContainer container for this filter.  If null, applies this filter to all tables on the page
		name: "",				// name of the filter setting to toggle with the input
		defaultValue: "",		// default value of the filter setting
		
		test : function(oSettings, aData, iDataIndex) {
			return true;
		}
	};
	
	this.config = $({}, defaults, config || {});
}
/**
 * 
 * @param oSettings
 * @param aData
 * @param iDataIndex
 * @returns {Boolean}
 */
DataTableFilter.prototype.test = function(oSettings, aData, iDataIndex) {
	return this.config.test(oSettings, aData, iDataIndex);
}