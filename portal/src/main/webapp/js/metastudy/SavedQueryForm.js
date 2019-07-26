/**
 * 
 */
SavedQueryViewEngine.SavedQueryForm = BaseModel.extend({
	savedQueryRgs : null,
	
	defaults : {
		uri : "",
		name : "",
		id : 0,
		filtered : false,
		studyIds : []
	},
	
	addRg : function(repeatableGroup) {
		this.savedQueryRgs.add(repeatableGroup);
	},
	
	loadData : function(dataObj) {
		this.set("uri", dataObj.uri);
		this.set("name", dataObj.name);
		this.set("id", dataObj.id);
		this.set("studyIds", dataObj.studyIds);
		
		this.savedQueryRgs = new SavedQueryViewEngine.SavedQueryRgs();
		var rgs = dataObj.groups;
		if (rgs != null) {
			for (var i = 0; i < rgs.length; i++) {
				var rg = new SavedQueryViewEngine.SavedQueryRg();
				rg.loadData(rgs[i]);
				this.addRg(rg);
			}
		}
		
		var filterConfigDefaults = {
			filtered : false,
			maximum : "",
			minimum : "",
			freeFormValue : "",
			dateMin : "",
			dateMax: "",
			blank: false,
			permissibleValues : []
		};
		
		var filters = dataObj.filters;
		if (filters.length > 0) {
			this.set("filtered", true);
			for (var j = 0; j < filters.length; j++) {
				var filter = filters[j];
				var rg = this.savedQueryRgs.getByUri(filter.groupUri);
				if (rg != null) {
					var de = rg.savedQueryDes.getByUri(filter.elementUri);
					// copies the elements that actually exist in the filter to the
					// config object so it can be set to the DE
					var config = _.clone(filterConfigDefaults);
					for (var key in filter) {
						config[key] = filter[key];
					}
					de.set(config);
				}
			}
		}
	}
});