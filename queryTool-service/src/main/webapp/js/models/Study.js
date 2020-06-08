/**
 * 
 */
QT.Study = BaseModel.extend({
	idAttribute: "uri",
	defaults : {
		selected : false,
		formsCount : 0,
		title : "",
		uri : "",
		shortName : "",
		pi : "",
		studyId : 0,
		
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
	
	// collection of QT.Forms
	forms : null,
	definedQueries : null,
	
	initialize : function() {
		QT.Study.__super__.initialize.call(this);
		
		this.forms = new QT.SelectionForms();
		this.definedQueries = new QT.SelectionDefinedQueries();
	},
	
	hasForm : function(formUri) {
		var matchingForms = this.model.findWhere({uri : formUri});
		return matchingForms.length > 0;
	},
	
	getCollection : function() {
		return this.forms;
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
	
	getSelectionListVisibleVariable : function(tabName) {
		return "isVisibleSelectionList" + tabName;
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