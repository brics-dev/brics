/**
 * 
 */

SavedQueryViewEngine.SavedQueryDes = Backbone.Collection.extend({
	initialize : function() {
		this.model = SavedQueryViewEngine.SavedQueryDe;
	},
	
	getById : function(id) {
		return this.findWhere({id : id});
	},
	
	getByUri : function(uri) {
		return this.findWhere({uri : uri});
	},
	
	getByName : function(name) {
		return this.findWhere({name : name});
	}
});