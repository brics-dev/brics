/**
 * 
 */
var Rows = Backbone.Collection.extend({
	initialize : function() {
		this.model = Row;
	},
	
	bySaveVal : function(saveValue) {
		return this.findWhere({saveValue: saveValue});
	}
});