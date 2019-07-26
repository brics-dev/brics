/**
 * 
 */
QT.QueryRg = BaseModel.extend({
	idAttribute: "uri",
	defaults : {
		uri : "",
		name : "",
		position : 0,
		dataElements : null
	},
	
	loadElements : function(deArray) {
		var collection = new QT.QueryDes();
		
		for (var i = 0; i < deArray; i++) {
			var de = deArray[i];
			var deModel = collection.create(de);
		}

		this.set("dataElements", collection);
	}
});