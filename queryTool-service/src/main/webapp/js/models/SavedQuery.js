/**
 * Used in the saved query dialog, NOT the selection list
 */
QT.SavedQuery = BaseModel.extend({
	defaults : {
		id : -1,
		name : "",
		description : "",
		copyFlag : false,
		lastUpdated : "",
		linkedUsers : null
	},
	
	initialize : function() {
		QT.SavedQuery.__super__.initialize.call(this);
		this.set("linkedUsers", new QT.Users());
		this.set("id", System.getUniqueNegId());
	}
});