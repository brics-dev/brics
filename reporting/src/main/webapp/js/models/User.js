/**
 *
 */
QT.User = BaseModel.extend({
	defaults : {
		id : -1, // The account id
		userName : "",
		firstName : "",
		lastName : "",
		email : "",
		assignedPermission : null,
		permissions : null,
		disabled : false
	},
	
	initialize : function() {
		QT.User.__super__.initialize.call(this);
		this.set("permissions", new QT.Permissions());
		this.set("assignedPermission", new QT.UserPermission());
	}
});