/**
 *
 */
QT.UserPermission = BaseModel.extend({
	defaults : {
		entityMapId : -1,
		entityId : -1,
		permission : "",
		selected : false
	},
	
	initialize : function() {
		QT.UserPermission.__super__.initialize.call(this);
	}
});