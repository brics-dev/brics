/**
 * 
 */
var DataElement = BaseModel.extend({
	defaults : {
		dataElementName 	: 	"",
		dataElementType 	: 	"",
		requiredType		:	"",
		description			:	"",
		suggestedQuestion	:	"",
		restrictionId		:	0,
		restrictionName		:	"",
		valueRangeList		:	"",
		size				:	0,
		max					:	0,
		min					:	0,
		associated			:	false,
		isGroupRepeatable	:	false,
		order				:	0,
		title				:	"",
		
		associatedToQuestion:	false
	},
	
	initialize : function() {
		
	},
	
	
	
	associate : function(questionModel) {
		this.set("associatedToQuestion", true);
	},
	
	deassociate : function(questionModel) {
		this.set("associatedToQuestion", false);
	},
	
	getRGName : function() {
		var deName = this.get("dataElementName");
		var index = deName.indexOf(".");
		return deName.substring(0,index);
	}
});