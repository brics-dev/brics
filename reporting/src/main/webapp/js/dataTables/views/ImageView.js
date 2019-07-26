/**
 * 
 */
QTDT.ImageView = BaseView.extend({
	Dialog : null,
	dialogConfig : {
		buttons : []
	},
	
	events : {
		
		
	},
	
	initialize : function() {
		this.Dialog = new QT.Dialog();
		this.template = TemplateManager.getTemplate("image");
		EventBus.on("open:image",this.open,this);
		EventBus.on("close:image", this.close, this);
	},
	
	render : function() {
		this.setup();
		this.open();	
	},
	
	setup : function() {
		if (!this.initialized) {
			this.$el = $("#imageDialog");
			var localConfig = $.extend({}, this.dialogConfig, {$container: this.$el, width:"auto", height:"auto",resizable: true});
			this.Dialog.init(this, this.model, localConfig);
			this.initialized = true;
		}
	},
	
	open : function(model) {
		var url = System.urls.query + "/service/thumbnail/download?studyName=" + model.studyName + 
						"&datasetName=" + model.datasetName + "&imageName=" + model.imageName; 
			
		this.$el.html(this.template({}));
		this.$("#imageFr").attr("frameborder", "0").attr("scrolling", "no").attr("src",url);
		

		this.Dialog.open();
	},
	
	styleDialog : function(iframev) {
//		// Private
//		// inject the query CSS styles into an iframe
//		// takes a string that is the ID element of a frame
		
		var img = $(iframev).contents().find("img")
		var id = "imageFr";
		
		if($(iframev).contents().find("img")[0]){ 
			var imgH = $(iframev).contents().find("img")[0].naturalHeight;
			var imgW = $(iframev).contents().find("img")[0].naturalWidth;
		}
		
		if(imgH != null) {
			$("#imageFr").height(imgH);
			$("#imageFr").width(imgW);
			
		}
		
		
//				.append(
//						"<link rel='stylesheet' type='text/css' href='css/instances/pdbp/iframe.css'>")
//				.css($("body", iframe).css("background-color", "#fff"));
	},
	
	iframeRef: function (frameRef) {
		return frameRef.contentWindow ? frameRef.contentWindow.document
				: frameRef.contentDocument
	}
	
	
	
	
	
	
	
	
	
});