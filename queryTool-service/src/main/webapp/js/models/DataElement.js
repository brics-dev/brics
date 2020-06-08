/**
 * 
 */
QT.DataElement = BaseModel.extend({
	idAttribute: "uri",
	defaults : {
		selected : false,
		formsCount : 0,
		title : "",
		uri : "",
		shortName : "",
		
		// selection list
		isVisibleSelectionListStudiesTab : true,
		isVisibleSelectionListFormsTab : true,
		isVisibleSelectionListDataElementsTab : false,
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
	
	forms : null,
	
	initialize : function() {
		QT.DataElement.__super__.initialize.call(this);
		this.forms= new QT.SelectionForms;
	},
	
	fromWebservice : function(wsObj) {
		var uriSplit = wsObj.rdfURI.split("/");
		
		this.set({
			title : wsObj.label,
			uri : wsObj.rdfURI,
			formsCount : wsObj.count,
			shortName : uriSplit[uriSplit.length - 1]
		},{silent: true});
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