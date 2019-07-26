/**
 * Effectively a snippet class that will be merged into the concrete Question
 * View class.  The properties here will NOT overwrite anything in the concrete
 * class.
 * 
 * 
 */
var QuestionImageMapDecorator = {
	// note: horizDisplayBreak and horizontalDisplay are only enabled for
	// select, multi-select, radio, and checkbox
	commonsName : "QuestionImageMapDecorator",
	events : {
		
	},
	//add validation 
	validationRules : [
	           
	],
	
	config : {
		name : "QuestionImageMapDecorator",
		render : "renderImageMapCommons"
	},
	
	renderImageMapCommons : function(){
		var imageMapeFileName = this.model.get("imageFileName");
		if(this.$("#dialog_editQuestion_tab1_container1").length < 1) {
			var container1 = "<div id='dialog_editQuestion_tab1_container1' class='col-md-6'></div>";
			this.$("#dialog_editQuestion_tab1 .row").wrapAll(container1);
			var container2 = "<div id='dialog_editQuestion_tab1_container2' class='col-md-6'></div>";
			this.$("#dialog_editQuestion_tab1").append(container2);
			this.$("#dialog_editQuestion_tab1_container2").html("<div class='col-md-4' id='imageMapSpace'></div>");
			
			var html;
			if (imageMapeFileName == null) {
				html = "<span class= 'question-changeImageMap ui-state-error'>"+Config.language.noImageMapDefined+Config.language.clickHereFix+"</span><span class='requiredStar'>*</span>";
			}else{
				html = this.createMapHtml(this.model.get('imageFileName'),this.model.get('imageOption'));
					/*"<div>Click Image to edit</div>" +
					   "<div><a href='javascript:;' class='question-changeImageMap'><img src="+baseUrl+"/images/questionimages/"+imageMapeFileName+"  width='130' height='130'></a></p></div>";*/
			
			}
			this.$('#imageMapSpace').html(html);
		}
	},
	
	createMapHtml : function(imageName,imageOptions){
		var options = '';
		if(imageOptions != null){
			var imageMapOptions = imageOptions.split(Config.alienSymbol);
			for(var i=0; i<imageMapOptions.length;i++){
				options += '<option>'+imageMapOptions[i]+'</option>';
			}
		}
		
		var html =
			"<div style='float:left;display:inline-block;'><span>Click Image to edit<p class='requiredStar'>*</p></span></div>" +
			"<table frame='box'>"+
			  "<tr aling='left'>"+
				"<td>"+
					"<a href='javascript:;' class='question-changeImageMap'>"+
					"<img  width='80' height='80' border='0' src='"+baseUrl+"/images/questionimages/"+imageName+"' >"+
					"</a>"+
				"</td>"+
				"<td valign='top'>"+
					"<select MULTIPLE>"+
					  options+
					"</select>"+
				"</td>"+
			  "</tr>"+
		    "</table>";
		
		return html;
	}
	
};