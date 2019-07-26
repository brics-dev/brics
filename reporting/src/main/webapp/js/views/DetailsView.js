/**
 * 
 */
QT.DetailsView = BaseView.extend({
	Dialog : null,
	dialogConfig : {
		buttons : []
	},
	
	events : {
		
		
	},
	
	initialize : function() {
		this.Dialog = new QT.Dialog();
		this.template = TemplateManager.getTemplate("details");
		EventBus.on("open:details",this.open,this);
		EventBus.on("close:details", this.close, this);
	},
	
	render : function() {
		this.setup();
		this.open();	
	},
	
	setup : function() {
		if (!this.initialized) {
			this.$el = $("#detailsDialog");
			var localConfig = $.extend({}, this.dialogConfig, {$container: this.$el});
			this.Dialog.init(this, this.model, localConfig);
			this.initialized = true;
		}
	},
	
	open : function(model) {
		/*
		 * NOTE: this calls the authenticated dictionary webservice calls.  Because we can't guarantee that the user has a session in dictionary 
		 * via the request here, we have to open the session before this is called.  Therefore, in PageView, we have a function to open an iframe
		 * (hidden) to the dictionary home page which begins the session before the user can click the links here.
		 * It's terrible but it works.
		 */
		var url = "";
		var proceed = true;
		if (model instanceof QT.Form) {
			if (System.user.hasAccessToDictionary) {
				var formShortName = model.get("shortName");
				url = System.urls.dictionary + "/dictionary/dataStructureAction!lightboxView.ajax?dataStructureName=" + formShortName + "&queryArea=true&publicArea=true";
			}
			else {
				$.ibisMessaging("dialog", "warning", "You cannot view this information because you do not currently have access to the Dictionary module");
				proceed = false;
			}
		}else if (model instanceof QT.DataElement) {
			if (System.user.hasAccessToDictionary) {
				var deShortName = model.get("shortName");
				url = System.urls.dictionary + "/dictionary/dataElementAction!viewDetails.ajax?dataElementName=" + deShortName + "&queryArea=true&publicArea=true";
			}
			else {
				$.ibisMessaging("dialog", "warning", "You cannot view this information because you do not currently have access to the Dictionary module");
				proceed = false;
			}
		}else {
			if (System.user.hasAccessToRepository) {
				var studyId = model.get("studyId");
				url = System.urls.base + "/study/viewStudyAction!lightbox.ajax?studyId=" + studyId;
			}
			else {
				$.ibisMessaging("dialog", "warning", "You cannot view this information because you do not currently have access to the Repository module");
				proceed = false;
			}
		}
		if (proceed) {
			this.$el.html(this.template({}));
			
			this.$("#detailsFr").attr("height", Config.getMaxDialogHeight() - 50).attr("src",url);
			this.Dialog.open();
		}
		
	},
	
	styleDialog : function() {
		// Private
		// inject the query CSS styles into an iframe
		// takes a string that is the ID element of a frame
		var id = "detailsFr";
		var iframe = this.iframeRef(document.getElementById(id));
		// drop in our styles but override body background color
		$("body", iframe)
				.append(
						"<link rel='stylesheet' type='text/css' href='/query/css/scss/instances/"+System.environment+"/style.css'>")
				.css($("body", iframe).css("background-color", "#fff"));
	},
	
	iframeRef: function (frameRef) {
		return frameRef.contentWindow ? frameRef.contentWindow.document
				: frameRef.contentDocument
	}
	
	
	
	
	
	
	
	
	
});