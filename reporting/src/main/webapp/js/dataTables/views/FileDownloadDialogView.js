/**
 * 
 */
QTDT.FileDownloadDialogView = BaseView.extend({
	Dialog : null,
	dialogConfig : {
		title: "File Download",
		height : "auto",
   		width : "55%",		
   		buttons: { 
   					"OK": function() { 
   						  $(this).dialog("close"); 
   					} 
   				}
	},
	
	initialize : function() {
		this.Dialog = new QT.Dialog();
		this.template = TemplateManager.getTemplate("fileDownloadTemplate");
		EventBus.on("open:downloadFileDialog",this.openDialog, this);
		EventBus.on("close:downloadFileDialog", this.closeDialog, this);
		EventBus.on("download:file",this.downloadFile,this);
	},
	
	render : function() {
		this.setup();
		this.openDialog();	
	},
	
	setup : function() {
		if (!this.initialized) {
			this.$el = $("#fileDownloadDialog");
			this.$el.html(this.template({}));
			var localConfig = $.extend({}, this.dialogConfig, {$container: this.$el, width:"auto", height:"auto",resizable: true});
			this.Dialog.init(this, this.model, localConfig);
			this.initialized = true;
		}
	},
	
	openDialog : function() {
		this.Dialog.open();
	},
	
	closeDialog : function() {
		this.Dialog.close();
	},
	
	downloadFile :function(model){
	
		$.ajax({
			url: "service/fileTypeDE/getFileSize",
			data: {
				studyName : model.studyName,
				datasetName :model.datasetName ,
				fileName : model.fileName
			},
			dataType: "json",
			success : function(data) {
				
				//convert the file size to MB
				var fileSize = data.fileSize/1000000;
				
				//TO DO --when file is not there!!!
				if(fileSize!=0){
					//TO DO--move this value to constant.js file
					//REQ-891 300MB is the acceptable limit, 
					if(fileSize<300){
						var downloadUrl = "service/fileTypeDE/download?studyName="+ model.studyName + "&datasetName=" + model.datasetName + "&fileName=" +model.fileName;
						window.location.href = downloadUrl;
					}
					else{
						EventBus.trigger("open:downloadFileDialog");
					}
				}
			}
		});
	}
	
});