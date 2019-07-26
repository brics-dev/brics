/**
 * 
 */
var Cell = BaseModel.extend({
	defaults : {
		html	:	"",		// raw html of rendered version of cell
		text	:	"",		// raw text of rendered version of cell
		data	:	null	// raw data input from data source (probably same as text)
	},
	
	initialize : {
		
	},
	
	html : function() {
		var htmls = this.get("html") || "";
		if (htmls !== "") {
			return htmls;
		}
		var htmls = "<td>" + htmls + "</td>";
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
	}
});