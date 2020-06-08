/**
 * 
 */
SavedQueryViewEngine.SavedQueryRg = BaseModel.extend({
	savedQueryDes : null,
	
	defaults : {
		uri : "",
		name : "",
		id : 0
	},
	
	addDe : function(repeatableGroup) {
		this.savedQueryDes.add(repeatableGroup);
	},
	
	loadData : function(dataObj) {
		this.set("uri", dataObj.uri);
		this.set("name", dataObj.name);
		this.set("id", dataObj.id);
		
		this.savedQueryDes = new SavedQueryViewEngine.SavedQueryDes();
		var des = dataObj.elements;
		for (var i = 0; i < des.length; i++) {
			var de = new SavedQueryViewEngine.SavedQueryDe();
			de.loadData(des[i]);
			this.addDe(de);
		}
	}
});