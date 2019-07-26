var RepeatableGroups = Backbone.Collection.extend({
	initialize : function() {
		this.model = RepeatableGroup;
	},
	
	/**
	 * Finds a data element within this collection by name
	 * 
	 * @param name the name to search by
	 * @return DataElement model if found, otherwise undefined
	 */
	byName : function(name) {
		return this.findWhere({repeatableGroupName: name});
	}
});