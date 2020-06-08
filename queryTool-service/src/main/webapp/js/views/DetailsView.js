/**
 * 
 */
QT.DetailsView = BaseView.extend({
	events : {
		
	},
	
	initialize : function() {
		EventBus.on("open:details",this.open,this);
		EventBus.on("close:details", this.close, this);
	},
	
	render : function() {
		this.setup();
		this.open();	
	},
	
	setup : function() {
		this.$el = $("#detailsDialog");
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
				url = "service/dataCart/deDetailPage?deName=" + deShortName;
			}
			else {
				$.ibisMessaging("dialog", "warning", "You cannot view this information because you do not currently have access to the Dictionary module");
				proceed = false;
			}
		}else {
			if (System.user.hasAccessToRepository) {
				var studyId = model.studyId || model.get("studyId");
				url = System.urls.base + "/study/viewStudyAction!lightbox.ajax?studyId=" + studyId;
			}
			else {
				$.ibisMessaging("dialog", "warning", "You cannot view this information because you do not currently have access to the Repository module");
				proceed = false;
			}
		}
		if (proceed) {
			window.open(url, "_blank", "width=100%, height="+Config.getMaxDialogHeight() - 50);
		}
		
	}
});