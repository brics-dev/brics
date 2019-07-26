/**
 * 
 */
 QT.Permissions = Backbone.Collection.extend({
	 model : QT.UserPermission,
	 
	 initialize : function() {
		 // Add the default permission types to the list.
		 var permTypes = Config.permissionTypes;
		 
		 for ( var i = 0; i < permTypes.length; i++ ) {
			 var perm = new QT.UserPermission();
			 
			 perm.set("permission", permTypes[i]);
			 this.add(perm);
		 }
	 }
 });