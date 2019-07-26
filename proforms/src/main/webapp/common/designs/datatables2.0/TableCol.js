/**
 * Represents a single column in the datatable.  This does not hold data but
 * does contain settings information for the table
 */
var TableCol = BaseModel.extend({
	defaults : {
		sortable 	: 	true,
		visible 	: 	true,
		title		:	"",
		index		:	0
	},
	
	initialize : function() {
	},
	
	dtObj : function() {
		return {
			// TODO: translate this model to the column object needed for the table
		};
	}
});