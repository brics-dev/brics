/**
 * 
 */
QTDT.LengthMenu = BaseModel.extend({
	idAttribute: "uri",
	defaults : {
		options: [10,20,50,100], //offset
		current: 20,
		optionCollection : null,
		htmlOptions: null
		
	},
	
	initialize : function(){
		//create htmlOptions
		var options = this.get("options");
		var optionCollection = new QTDT.LengthMenuOptions();
		 for (var i = 0; i <= options.length; i++) {
		      
			 optionCollection.create({
					length 				: options[i]
						});
		    }
		 this.set("optionCollection",optionCollection);
		 this.set("htmlOptions",optionCollection.toJSON());
		
	}
	
	
	
	
	
});