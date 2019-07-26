/**
 * 
 */
var Row = BaseModel.extend({
	defaults : {
		saveValue 			:	"",		// the value that identifies this row for outside scripts
		data 				:	[],		// cell data 
		expandedData		:	"",		// expanded row html
		expandedDataIndex	:	-1,		// index of expanded data column
		index				:	-1,		// index of row in table
		disabled			:	false	// is this row disabled?
	},
	
	initialize : function() {
		this.set("saveValue", IDT.getSaveValue(this));
	},
	
	/**
	 * returns the HTML version of this row (as a table row <tr>)
	 */
	html : function() {
		var data = this.get("data");
		var output = '<tr>';
		for (var i = 0; i < data.length; i++) {
			output += '<td>' + data[i] + '</td>';
		}
		output += '</tr>';
		return output;
	},
	
	/**
	 * Gets data from the colIndex cell in this row
	 * @return cell data (html) if found, otherwise UNDEFINED
	 */
	getCellDataHtml : function(colIndex) {
		var data = this.get("data");
		if (colIndex > 0 && colIndex < data.length) {
			return data[colIndex];
		}
		return undefined;
	},
	
	getCellDataText : function(colIndex) {
		var data = this.get("data");
		if (colIndex > 0 && colIndex < data.length) {
			var html = data[colIndex];
			return $(html).text();
		}
		return undefined;
	},
	
	/**
	 * Gets the input for this row.  If there is no input,
	 * return empty jquery $();
	 * 
	 * @return jquery reference to input or empty jquery
	 */
	getInput : function() {
		var $input = $();
		var cellOne = this.get("data")[0];
		try{
			$input = $(cellOne);
		}catch(e){
			// input is already empty so just return it
		}
		return $input;
	}
});