/**
 * 
 */
QTDT.Row = BaseModel.extend({
	defaults : {
		//id 			:	"",		// the value that identifies this row for outside scripts
		index				:	-1,		// index of row in table
		disabled			:	false,	// is this row disabled?
		cells		: "",   // collection of cell models belonging to this row
		cellViews : new Array(), //views of all the cells
		totalCells : 0 ,  // the amount cells are in this row
		parity : "",
		frozen : false,
		height : 0
		
	},
	
	initialize : function() {
	},
	
	/**
	 * returns the HTML version of this row (as a table row <tr>)
	 */
	html : function() {
		var data = this.get("data");
		var output = '<div class="table-row">';
		for (var i = 0; i < data.length; i++) {
			output += '<div><p class="text">' + data[i] + ' </p></div>';
		}
		output += '<div class="space-line"></div></div>';
		return output;
	},
	render: function(container){
		
		var divid = "dataRow_"+this.get("index");

		var rowcells = this.get("cells");
		
		
		container.append($('<div>', { 
			"id" : divid,
			"class" : 'table-row'
		}));
		
		rowcells.each(function(cellmodel) {
				
				$("#"+divid).append(cellmodel.render());
		});
		
		$("#"+divid).append($('<div>', { 
			"class" : 'space-line'
		}));
		
	}
	
});