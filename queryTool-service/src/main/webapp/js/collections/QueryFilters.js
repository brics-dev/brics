/**
 * 
 */
QT.QueryFilters = Backbone.Collection.extend({
	
	initialize : function() {
		this.model = QT.QueryFilter;
		
	},
	
	findByElementAndGroup : function(elementUri,groupUri) {
		return this.findWhere({
			elementUri : elementUri,
			groupUri : groupUri	
		});	
	},
	
	toJson : function() {
		var outputArr = [];
		this.forEach(function(Queryfilter){
			outputArr.push(Queryfilter.toJsonObject());
		});
		return outputArr;
	}
});