var ImageMapProcessView = BaseView.extend({
	
	imageName : null,
	
	imageOptions : null,
	
	dialogTitle : "Defing Image Map",
	
	buttons : [{
				id: "saveImageMap",
				text: "Save",
				click: function(){
					var mainThis = FormBuilder.page.get("imageMapProcessView");
					mainThis.model.set("imageFileName",mainThis.imageName);
					mainThis.model.set("imageOption",mainThis.imageOptions);
					//if(mainThis.model.get("questionId") < 0){
					$('#imageMapSpace').html('');	
					$('#imageMapSpace').html(mainThis.createResultHtml(mainThis.imageName,mainThis.imageOptions));
					//}
					$(this).dialog("close");
				}
				},
	           {
				text: "Cancel",
				click: function() {
					$(this).dialog("close");
				}
	           }
	          ],
	
	initialize : function() {
		ImageMapProcessView.__super__.initialize.call(this);
		this.template = "<iframe id='imageTypeFr' src='' frameborder='0' scrolling='auto' width=640 height=830></iframe>";
	},
	
	close : function() {
		this.Dialog.close();
		ImageMapProcessView.__super__.close.call(this);
	},
	
	render : function(model) {
		this.model = model;
		this.$el.html(this.template);
		this.Dialog.initAndOpen(this);
		ImageMapProcessView.__super__.render.call(this);
	},
	
	destroy : function() {
		this.Dialog.destroy(this);
		ImageMapProcessView.__super__.destroy.call(this);
	},
	
	Dialog : {
		initAndOpen : function(view) {
			view.$el.dialog({
				title : view.dialogTitle,
				modal : true,
				width : 700,
				buttons : view.buttons,
				
				close : function(event, ui) {
					$(this).dialog("destroy");
				},
				
				open : function() {
					$('#saveImageMap').button().hide();
					var qM = FormBuilder.page.get("imageMapProcessView").model;
					if(qM.get("imageFileName") === null){
						$('#imageTypeFr').attr("src",baseUrl+'/question/addImageType.action');						
					}else{
						$('#imageTypeFr').attr("src",baseUrl+'/question/editImageType.action?id='+qM.get("questionId"));
					}
				}
			});
		},
		
		close : function(view) {
			if (this.isOpen(view)) {
				view.$el.dialog("close");
			}
		},		
		destroy : function(view) {
			this.close(view);
			view.$el.dialog("destroy");
		}
	},
	
	showButton : function(result){
		$('#saveImageMap').button().show();
		this.imageName = result[0];
		var options = JSON.parse(result[1]);
		var optionString = '';
		for(var i=0; i < options.length; i++){
			optionString += options[i]+Config.alienSymbol;
		}
		this.imageOptions = optionString ;
	},
	
	createResultHtml : function(imageName,imageOptions){
		var options = '';
		if(imageOptions != null){
			var imageMapOptions = imageOptions.split(Config.alienSymbol);
			for(var i=0; i<imageMapOptions.length;i++){
				options += '<option>'+imageMapOptions[i]+'</option>';
			}
		}
		
		var html = 		
		"<table frame='box'>"+
		  "<tr aling='left'>"+
			"<td>"+
				"<img  width='80' height='80' border='0' src='"+baseUrl+"/images/questionimages/"+imageName+"' >"+
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
});