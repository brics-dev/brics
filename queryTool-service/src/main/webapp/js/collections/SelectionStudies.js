/**
 * 
 */
QT.SelectionStudies = Backbone.Collection.extend({
	initialize : function() {
		this.model = QT.Study;
	},
	
	byStudyId : function(studyId) {
		if (typeof studyId == "string") {
			studyId = Number(studyId);
		}
		return this.findWhere({studyId: studyId});
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