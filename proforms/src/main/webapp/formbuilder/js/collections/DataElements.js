var DataElements = Backbone.Collection.extend({
	initialize : function() {
		this.model = DataElement;
	},
	
	/**
	 * Finds a data element within this collection by data element name
	 * 
	 * @param dataElementName the name to search by
	 * @return DataElement model if found, otherwise undefined
	 */
	byName : function(dataElementName) {
		return this.findWhere({dataElementName: dataElementName});
	},
	
	/**
	 * Finds a data element within this collection by title
	 * 
	 * @param title the title to search by
	 * @return DataElement model if found, otherwise undefined
	 */
	byTitle : function(title) {
		return this.findWhere({title: title});
	},
	
	/**
	 * Find a data element with the given deName and Order
	 */
	byGroupAndOrder : function(rgName, order) {
		var arrDEs = this.where({order: order});
		for (var i = 0; i < arrDEs.length; i++) {
			var de = arrDEs[i];
			var arrNameSplit = de.get("dataElementName").split(".");
			if (arrNameSplit[0] == rgName) {
				return de;
			}
		}
		return null;
	}
});