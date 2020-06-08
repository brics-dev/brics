/**
 * 
 */
QTDT.Cell = BaseModel.extend({
	idAttribute: "uri",
	studyName : "",
	datasetName : "",
	triplanarName : "",
	imageName : "",
	fileName : "",
	rowUri : "",
	rgFormUri : "",
	rgName : "",
	biosampleRowUri: "",
	biosampleFormName: "",
	biosampleValue: "",
	defaults : {
		originalHtml : "", //original value of cell
		parsedHtml : "", //html value after parsed in view
		html	:	"",		// raw html of rendered version of cell
		text	:	"",		// raw text of rendered version of cell
		data	:	null,	// raw data input from data source (probably same as text)
		location: null, //this will indicated the row and col this cell belongs to
		width : 0, //this is the width of the cell with it's contents
		visible : true, // are we visible
		frozen : false,
		column : null,
		height : 0,
		rowView : null, //this is the view of the row the cell is contained in
		studyId: 0,
		expandWidth: null,
		collapseWidth: null,
		highlightRow: false,
		colorText: false,
		row : null, 
		col : null
	},
	
	
	
	initialize : function(){},
	
	html : function() {
		var htmls = this.get("html") || "";
		if (htmls !== "") {
			return htmls;
		}
		var htmls = "&nbsp;";
		this.set("html", htmls);
		return htmls;
	},
	
	text : function() {
		var texts = this.get("text") || "";
		if (texts !== "") {
			return texts;
		}
		var texts = String(texts);
		this.set("text", texts);
		return texts;
	},
	
	location : function() {	
		//TODO: this should return an array or object with the col model, and row model
		return this.get("location");
	}
	
});