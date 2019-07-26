/**
 * 
 */
QT.Form = BaseModel.extend({
	idAttribute: "uri",
	defaults : {
		selected : false,
		studiesCount : 0,
		title : "",
		uri : "",
		shortName : "",
		studiesMapped : [],
		id : 0,
		uriNoVersion : "",
		
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

	// collection
	studies : null,
	dataElements : null,
	
	initialize : function() {
		QT.Form.__super__.initialize.call(this);
		this.studies= new QT.SelectionStudies;
		this.dataElements = new QT.SelectionDataElements;
		var uri = this.get("uri");
		//http://ninds.nih...y/ibis/1.0/FormStructure/NeurologicalExam_v1.0
		this.set("uriNoVersion", this.stripUriVersion(uri));
	},
	
	hasStudy : function(studyUri) {
		var matchingStudies = this.model.findWhere({uri : studyUri});
		return matchingStudies.length > 0;
	},
	
	getCollection : function() {
		return this.studies;
	},
	
	stripUriVersion : function(uri) {
		return uri.replace(/(_v[0-9]+\.[0-9]+)$/g, "");
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