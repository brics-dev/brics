/**
 * 
 */
QT.Filters.DeHideShowFilter = QT.Filter.extend({
	name : "DeHideShowFilter",
	
	equals : function(filter) {
		// we want to process every filter because the filter processor for
		// Data Elements hides all DEs before running the filter
		return false;
	}
});