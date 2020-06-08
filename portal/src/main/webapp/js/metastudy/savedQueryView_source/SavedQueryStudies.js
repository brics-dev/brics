/**
 * 
 */

SavedQueryViewEngine.SavedQueryStudies = Backbone.Collection.extend({
	initialize : function() {
		this.model = SavedQueryViewEngine.SavedQueryStudy;
	},
	
	getById : function(id) {
		return this.findWhere({id : id});
	}
});