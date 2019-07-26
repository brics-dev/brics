/**
 * 
 */
QT.SelectDe = BaseModel.extend({
	defaults : {
		diseaseFiltersOptions : [],
		populationFiltersOptions : [],
		
		selectedElementTypes : [],
		selectedDiseases : [],
		selectedPopulations : [],
		
		selectedDataElements : [],
		
		iDisplayStart : 0,
		iSortCol_0: 0,
		iDisplayLength: 15,
		sEcho: 1,
		textSearch : "",
		sSortDir_0 : "",
		
		CDE : false,
		UDE : false,
		
		locationKeyword : true,
		locationDescription : true,
		locationPermissibleValue : true,
		locationTitle : true,
		locationLabel : true,
		locationExternalId : true,
		locationVarName : true,
		
		wholeWord : false,
		aoColumns : [
		    {
		    	"sTitle" : ""
		    },
		    {
		    	"sTitle" : "Title"
		    },
		    {
		    	"sTitle" : "Variable Name"
		    },
		    {
		    	"sTitle" : "Type"
		    },
		]
	},
	
	initialize : function() {
		EventBus.on("add:filterDataElement", this.addSelectedDataElement, this);
		EventBus.on("remove:filterDataElement", this.removeSelectedDataElement, this);
	},
	
	loadOptions : function(filterOptions) {
		this.set("diseaseFiltersOptions", filterOptions.diseases);
		this.set("populationFiltersOptions", filterOptions.populations);
	},

	setup : function() {
		var view = this;
		$.ajax({
			type: "GET",
			cache : false,
			url : "service/deSelect/deSelectFilterOptions",
			data : {},
			traditional : true,
			async: true,
			success : function(data, textStatus, jqXHR) {
				view.loadOptions(data);
			},
			error : function(data) {
				// TODO: handle error
			}
		})
	},
	
	reset : function() {
		this.set("iDisplayStart", 0);
		this.set("iSortCol_0", 0);
		this.set("iDisplayLength", 15);
		this.set("sEcho", 1);
		this.set("textSearch", "");
		this.set("CDE", false);
		this.set("UDE", false);
		this.set("locationKeyword", true);
		this.set("locationDescription", true);
		this.set("locationPermissibleValue", true);
		this.set("locationTitle", true);
		this.set("locationLabel", true);
		this.set("locationExternalId", true);
		this.set("locationVarName", true);
		this.set("wholeWord", false);
		this.set("selectedDataElements", []);
	},
	
	addSelectedDataElement : function(uri) {
		var elements = this.get("selectedDataElements");
		if (elements.indexOf(uri) == -1) {
			elements.push(uri);
		}
	},
	
	removeSelectedDataElement : function(uri) {
		var elements = this.get("selectedDataElements");
		var index = elements.indexOf(uri);
		if (index != -1) {
			elements.splice(index, 1);
			this.set("selectedDataElements", elements);
		}
	}
});