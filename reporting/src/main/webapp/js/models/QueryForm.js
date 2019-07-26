/**
 * 
 */
QT.QueryForm = BaseModel.extend({
	idAttribute: "uri",
	defaults : {
		uri : "",
		repeatableGroups : null
	},
	
	loadRgs : function(rgArray) {
		var collection = new QT.QueryRgs();
		
		for (var i = 0; i < rgArray; i++) {
			var rg = rgArray[i];
			var rgModel = collection.create(rg);
			rgModel.loadElements(rg.dataElements);
		}

		this.set("repeatableGroups", collection);
	}
});