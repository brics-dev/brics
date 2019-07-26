/**
 * 
 */
QTDT.Col = BaseModel.extend({
	formUri : "", 
	rgUri: "", 
	deUri: "", 
	rgName: "", 
	deName: "",
	formName : "",
	formVersion : "",
	defaults : {
		id :  "",
		sortable 	: 	true,
		frozen : false, // this indicates if this column if frozen or not
		visible 	: 	true,
		index		:	0,
		parent : null,
		children: null,
		headerType: "",
		name: "",
		cells : null, //this will hold cell models that belong to this column,
		width : 0, //width of column
		frozenWidth : 0, //width if column is frozen
		biosampleArray : [], //this is an array to keep checkboxes of selected biosamples
		colspanAmt : 1,
		frozenParentWidth : 0,
		originalView : null,
		frozenView : null,
		doesRepeat : false,
		modelView : null,
		adjustedWidth : -1
	},
	
	 
	initialize : function() {
		this.set("children", new QTDT.Cols())
		this.set("cells", new QTDT.ColCells())

	    	deName = this.get("name");
	    	if(deName != "") {
		    	// this method will retrieve the DE details from the server and then call the eventbus
		    	var dataElement = QueryTool.page.get("dataElements").byShortName(deName);
		    	if(typeof dataElement === 'object') {
		    		this.deUri = dataElement.get("uri");
		    	}
	    	}
	    	
	    	if( this.get("parent") != undefined ) {
	    		this.rgName = this.get("parent").get("name");
	    	}
	    	
	    	//TODO: Could improve this and have this populated on load data
	    	if(this.get("parent") != undefined && this.get("parent").get("parent")!= undefined) {
		    	this.formName = this.get("parent").get("parent").get("name");
		    	var form = QueryTool.page.get("forms").byShortName(this.formName);
		    	if(typeof form === 'object') {
		    		this.formUri = form.get("uri");
		    	}
	    	}
	    	
	},
	
	cells : function () {
		return this.get("cells");
	}
	,
	showHide: function () {
		var cells = this.get("cells");
		var toggleVisibility = !this.get("visible");
		
		if(toggleVisibility) { 
			EventBus.trigger("query:selectDe", this);
		} else {
			EventBus.trigger("query:deselectDe", this);
		}
		cells.each(function(cell){
			
				cell.set("visible",toggleVisibility);
				
		});
		
		this.set("visible",toggleVisibility);
	}
	,
	show: function () {
		var cells = this.get("cells");
		
		EventBus.trigger("query:selectDe", this);
		
		cells.each(function(cell){
				cell.set("visible",true);
		});
		
		this.set("visible",true);
	},
	setCurrentShowHide: function() {
		
		EventBus.trigger("hamburgerview:showHideCol", {
    		formUri: this.formUri,
    		rgUri: this.rgUri,
    		rgName: this.rgName,
    		deUri: this.deUri,
    		deName: this.get("name"),
    		visible: !this.get("visible")
    	});
	
	},
	rgChildrenShow: function () {
		var children = this.get("children");
		
		children.each(function(col){
			col.set("visible",true);
			var cells = col.get("cells");
			cells.each(function(cell){
					cell.set("visible",true);
			});
		});
	},
	rgChildrenHide: function () {
		var children = this.get("children");
		children.each(function(col){
			col.set("visible",false);
			var cells = col.get("cells");
			cells.each(function(cell){
					cell.set("visible",false);
			});
		});
	
	},
	getChildrenWidth : function() {
		var col = this;
		var kids = col.get("children");
		var width = 0;
	
		
		kids.each(function(kid){
				width += (kid.get("visible")) ? kid.get("width") : 0;
		})
		col.set("width",width);		
	},
	getFrozenChildrenWidth : function() {
		var col = this;
		var kids = col.get("children");
		var width = 0;
		var thisView = this;
		kids.each(function(kid){
			
			if(col.get("frozen")){
				width += (kid.get("visible")) ? (kid.get("frozen")) ? kid.get("frozenWidth") : 0 : 0;	
			} 
		})
		col.set("frozenWidth",-1);
		col.set("frozenWidth",width);
	
	},
	getFullName : function () {
		var formVer = "";
		var formName = this.formName;
		var rgName = this.rgName;
		if(this.formVersion != "") {
			formVer = "V"+this.formVersion;
		}
		
		//check for Study and DataSet Columns
		if(this.get("name") == "Study ID" && formName == "Forms:" && rgName == "Repeatable Groups:") {
			fullName = "?study";
		} else if(this.get("name") == "Study ID" && rgName == ""){
			//this is for joined forms
			fullName = this.formName+formVer+",?study";		
		}else if(this.get("name") == "Dataset" && formName == "Forms:" && rgName == "Repeatable Groups:") {
			fullName = "?prefixedId";
		} else if(this.get("name") == "Dataset" && rgName == ""){
			//this is for joined forms
			fullName = this.formName+formVer+",?prefixedId";		
		}else if(this.get("name") == "GUID" && formName == "Forms:" && rgName == "Repeatable Groups:") {
			fullName = "?guid";
		}
		else {
			fullName = this.formName+formVer+","+this.rgName+","+this.get("name");
		}
		return fullName;
	}
	
});