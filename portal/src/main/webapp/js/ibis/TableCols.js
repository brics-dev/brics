/**
 * 
 */
var TableCols = Backbone.Collection.extend({
	initialize : function() {
		this.model = TableCol;
	},
	
	indexByName : function(title) {
		return this.findWhere({"title" : title});
	}
});