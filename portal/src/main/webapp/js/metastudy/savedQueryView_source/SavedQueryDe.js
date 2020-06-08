/**
 * 
 */
SavedQueryViewEngine.SavedQueryDe = BaseModel.extend({
	defaults : {
		uri : "",
		name : "",
		id : 0,
		filtered : false,
		maximum : "",
		minimum : "",
		freeFormValue : "",
		dateMin : "",
		dateMax: "",
		blank: "",
		permissibleValues : []
	},
	
	addRg : function(repeatableGroup) {
		this.savedQueryRgs.add(repeatableGroup);
	},
	
	loadData : function(dataObj) {
		this.set("uri", dataObj.uri);
		this.set("name", dataObj.name);
		this.set("id", dataObj.id);
		// the filter values will be filled later, within SavedQueryForm
	},
	
	getRenderString : function() {
		var output = "";
		var maximum = this.get("maximum");
		var minimum = this.get("minimum");
		var freeFormValue = this.get("freeFormValue");
		var dateMin = this.get("dateMin");
		var dateMax = this.get("dateMax");
		var blank = this.get("blank");
		var permissibleValues = this.get("permissibleValues");
		
		output = "";
		if (this.get("filtered")) {
			if (freeFormValue != "") {
				output += " = \"" + freeFormValue + "\"";
			}
			else if (maximum != "" && minimum != "") {
				if (output != "") {
					output += " and ";
				}
				output += " between " + minimum + " and " + maximum;
			}
			else if (dateMin != "" && dateMax != "") {
				if (output != "") {
					output += " and ";
				}
				output += " between " + dateMin + " and " + dateMax;
			}
			else if (maximum != "") {
				if (output != "") {
					output += " and ";
				}
				output += " < " + maximum;
			}	
			else if (minimum != "") {
				if (output != "") {
					output += " and ";
				}
				output += " > " + minimum;
			}
			else if (dateMin != "") {
				if (output != "") {
					output += " and ";
				}
				output += " after " + dateMin;
			}
			else if (dateMax != "") {
				if (output != "") {
					output += " and ";
				}
				output += " before " + dateMax;
			}
			else if (permissibleValues.length > 0) {
				if (output != "") {
					output += " and ";
				}
				output += " one of " + permissibleValues.join(", ");
			}
			
			if (blank) {
				if (output != "") {
					output += " and ";
				}
				output += " including empty values";
			}
		}
		
		return output;
	}
});