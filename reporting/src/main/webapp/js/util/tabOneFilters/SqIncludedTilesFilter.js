/**
 * 
 */
QT.Filters.SqIncludedTilesFilter = QT.Filter.extend({
	name : "SqIncludedTilesFilter",
	
	equals : function(filter) {
		// let's assume that all filters of this type are never equal
		return false;
	},
	
	/**
	 * Returns a list of all models that should be visible after the filter runs.
	 */
	each : function(listOfVisible) {
		listOfVisible = this.listOfVisible;
		if (_.isArray(this.listOfVisible)) {
			listOfVisible = this.arrToObj(this.listOfVisible);
		}
		return listOfVisible;
	}
	
	// check is exactly the same as the base filter
});