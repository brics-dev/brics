/**
 * Used in the selection list, NOT in the saved query dialog
 */
QT.DefinedQuery = BaseModel.extend({
	defaults : {
		id : -1,
		selected : false,
		studiesCount : 0,
		name : "",
		uri : "",
		idAttribute : "id",
		
		// selection list
		isVisibleSelectionListStudiesTab : true,
		isVisibleSelectionListFormsTab : true,
		isVisibleSelectionListDataElementsTab : true,
		isVisibleSelectionListDefinedQueriesTab : true,
		
		// selection list checkboxes
		isSelectedSelectionListStudiesTab : false,
		isSelectedSelectionListFormsTab : false,
		isSelectedSelectionListDataElementsTab : false,
		isSelectedSelectionListDefinedQueriesTab : false,
		
		// tiles
		isVisibleTilesStudiesTab : true,
		isVisibleTilesFormsTab : true,
		isVisibleTilesDataElementsTab : true,
		isVisibleTilesDefinedQueriesTab : true,
		
		// tiles highlight
		isHighlightedTilesStudiesTab : true,
		isHighlightedTilesFormsTab : true,
		isHighlightedTilesDataElementsTab : true,
		isHighlightedTilesDefinedQueriesTab : true,
		
		// data cart.  NOT the only storage location.  The DataCart model
		// manages this property
		isInDataCart : false
	},
	
	studies : null,
	
	initialize : function() {
		QT.DefinedQuery.__super__.initialize.call(this);
		this.studies= new QT.SelectionStudies;
	},
	
	hasStudy : function(studyUri) {
		var matchingStudies = this.model.findWhere({uri : studyUri});
		return matchingStudies.length > 0;
	},
	
	getCollection : function() {
		return this.studies;
	},
	
	isInDataCart : function() {
		return this.get("isInDataCart");
	},
	
	isSelectionListVisible : function(tabName) {
		return this.get("isVisibleSelectionList" + tabName);
	},
	
	isSelectionListSelected : function(tabName) {
		return this.get("isSelectedSelectionList" + tabName);
	},
	
	isTileVisible : function(tabName) {
		return this.get("isVisibleTiles" + tabName);
	},
	
	isTileHighlighted : function(tabName) {
		return this.get("isHighlightedTiles" + tabName);
	},
	
	setSelectionListVisible : function(tabName, value) {
		this.set("isVisibleSelectionList" + tabName, value);
	},
	
	setSelectionListSelected : function(tabName, value) {
		this.set("isSelectedSelectionList" + tabName, value);
	},
	
	setTileVisible : function(tabName, value) {
		this.set("isVisibleTiles" + tabName, value);
	},
	
	setTileHightlight : function(tabName, value) {
		this.set("isHighlightedTiles" + tabName, value);
	},
	
	equals : function(otherModel) {
		if (typeof otherModel.get("uri") === "undefined") {
			return false;
		}
		return this.get("uri") == otherModel.get("uri");
	}
});