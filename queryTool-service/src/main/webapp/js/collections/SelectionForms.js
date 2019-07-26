/**
 * 
 */
QT.SelectionForms = Backbone.Collection.extend({
	initialize : function() {
		this.model = QT.Form;
	},
	
	byShortName : function(shortName) {
		return this.findWhere({shortName: shortName});
	},
	
	byUriNoVersion : function(uriNoVersion) {
		return this.findWhere({uriNoVersion : uriNoVersion});
	},
	
	byUnknownUriVersion : function(uriMaybeVersion) {
		var form = this.get(uriMaybeVersion);
		if (!form) {
			form = this.byUriNoVersion(uriMaybeVersion);
		}
		return form;
	},
	
	allSelected : function(tabName) {
		var propertyName = "isSelectedSelectionList" + tabName;
		var filter = {};
		filter[propertyName] = true;
		return this.where(filter);
	},
	
	allHidden : function(tabName) {
		var propertyName = "isVisibleSelectionList" + tabName;
		var filter = {};
		filter[propertyName] = false;
		return this.where(filter);
	},
	
	allVisible : function(tabName) {
		var propertyName = "isVisibleSelectionList" + tabName;
		var filter = {};
		filter[propertyName] = true;
		return this.where(filter);
	},
	
	allHiddenTiles : function(tabName) {
		var propertyName = "isVisibleTiles" + tabName;
		var filter = {};
		filter[propertyName] = false;
		return this.where(filter);
	},
	
	allVisibleTiles : function(tabName) {
		var propertyName = "isVisibleTiles" + tabName;
		var filter = {};
		filter[propertyName] = true;
		return this.where(filter);
	}
});