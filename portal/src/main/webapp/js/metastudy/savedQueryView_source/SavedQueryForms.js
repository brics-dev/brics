/**
 * 
 */

SavedQueryViewEngine.SavedQueryForms = Backbone.Collection.extend({
	initialize : function() {
		this.model = SavedQueryViewEngine.SavedQueryForm;
	},
	
	getById : function(id) {
		return this.findWhere({id : id});
	},
	
	getByName : function(name) {
		return this.findWhere({name : name});
	},
	
	getByUri : function(uri) {
		return this.findWhere({uri : uri});
	}
});