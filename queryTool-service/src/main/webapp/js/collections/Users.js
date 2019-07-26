/**
 * 
 */
QT.Users = Backbone.Collection.extend({
	model : QT.User,
	comparator : function(model) {
		return model.get("lastName").toLowerCase();
	},
	
	initialize : function() {
		
	},
	
	/**
	 * Generates an array of entity map IDs that is stored in the user's assigned permission model.
	 * Only positive entity map IDs will be included in the returned array.
	 * 
	 * @return An array of entity map IDs of the assigned permissions to all of the user models in
	 * this collection.
	 */
	getEntityMapIds : function() {
		var permIds = [];
		
		this.each(function(user) {
			var emId = user.get("assignedPermission").get("entityMapId");
			
			if ( emId > 0 ) {
				permIds.push(emId);
			}
		}, this);
		
		return permIds;
	}
});