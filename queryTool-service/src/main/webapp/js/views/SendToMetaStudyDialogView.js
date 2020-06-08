/**
 * template: metaStudyDialog.jsp
 * model: page
 */
QT.SendToMetaStudyDialogView = BaseView.extend({
	model: null,
	Dialog : null,
	
	selectedMetaStudy : 0,
	
	dialogConfigs : {
		title: "Send to Meta Study",
		buttons : [{
			text: "Save",
			click : function() {
				var view = QueryTool.page.get("metaStudyDialogView");
				view.save.call(view);
			}
		}, {
			text: "Cancel",
			click: function() {
				
				EventBus.trigger("close:sendToMetaStudy");
			}
		}],
		height : "auto",
		width : "70%",
		position : {my: "center top+10%", at: "center top+10%", of: window},
		
		close : function(event) {
			EventBus.trigger("close:saveQueryDialog");
		}
	},
	
	events : {
		
	},
	
	initialized : false,
	
	initialize : function() {
		this.Dialog = new QT.Dialog();
		this.template = TemplateManager.getTemplate("metaStudyDialog");
		
		EventBus.on("open:sendToMetaStudy", this.openDialog, this);
		EventBus.on("close:sendToMetaStudy", this.closeDialog, this);
		EventBus.on("open:linkSavedQueryData", this.linkSavedQueryData,this);
		
		QT.SendToMetaStudyDialogView.__super__.initialize.call(this);
	},
	
	render : function() {
		this.$el.html(this.template());
		this.resetInputs();
		
		if ( !this.initialized ) {
			var localConfig = $.extend({}, this.dialogConfigs, {$container: this.$el});
			
			this.Dialog.init(this, this.model, localConfig);
			QT.SendToMetaStudyDialogView.__super__.render.call(this);
			this.initialized = true;
		}
		
		// note: I don't show the processing dialog here because this list is populating in the
		// background
		var query = this;
		$.ajaxSettings.traditional = true;
		$.ajax({
			type: "GET",
			cache : false,
			url : "service/metaStudies/",
			success : function(data, textStatus, jqXHR) {
											
					var $table = $("#metaStudyTable_table");
					var itemTemplate = TemplateManager.getTemplate("metaStudyDialogItem");
					for (var i = 0, len = data.length; i < len; i++) {
						var study = data[i];
						var html = itemTemplate(study);
						$table.append(html);
					}
			},
			error : function() {
				// TODO: fill in
			}
		})
	},
	
	openDialog : function() {
		// reset the inputs
		this.resetInputs();
		
		var query = this;
		var dialog = this.Dialog;
		$.ajaxSettings.traditional = true;
		$.ajax({
			type: "GET",
			cache : false,
			url : "service/metaStudies/",
			success : function(data, textStatus, jqXHR) {
								
				if(data.length==0){
					
					var messageBtns = [{
						text : "OK",
						
						click : function() {
							$(this).dialog("close");
						}
					}];
					
					var notificationLink = 'You do not have any available Meta Studies. To create or request access to a Meta Study,'+ '<a href="'+System.urls.dataRepo+'/metastudy/metaStudyAction!create.action"> click here. </a>';	 
					
					$.ibisMessaging("dialog", "error", notificationLink, {
						width : "650",
						buttons : messageBtns,
						title : "Send to Meta Study"
					});
				}
				else{
					dialog.open(this);
				}
			}
		});
	},
	
	/**
	 * Convenience method to call the close function for the underlining Dialog object. Called when the cancel button is clicked, or 
	 * when the changes to the saved query saves correctly.
	 */
	closeDialog : function() {
		this.$(".metaStudySelector").prop("checked", false);
		this.Dialog.close();
	},
	
	resetInputs : function() {
		this.$("#savedQueryField_saveFilters_yes").prop("checked", false);
		this.$("#savedQueryField_saveFilters_no").prop("checked", true);
		this.$("#savedQueryField_name").val("");
		this.$("#savedQueryField_desc").val("");
		
		this.$("#savedQueryField_saveData_yes").prop("checked", false);
		this.$("#savedQueryField_saveData_no").prop("checked", true);
		this.$("#savedQueryField_dataName").val("");
		this.$("#savedQueryField_dataDesc").val("");
		
		this.$("#validateQueryName").empty();
		this.$("#validateDataName").empty();
	},
	
	save : function() {
		// get input values
		var selectedMetaStudy = this.$(".metaStudySelector:checked").val();
		
		var sendQueryFilters = this.$('input[name="saveFilters"]:checked').val();
		var queryName = this.$("#savedQueryField_name").val();
		var queryDescription = this.$("#savedQueryField_desc").val();
		var sendData = this.$('input[name="saveData"]:checked').val();
		var dataName = this.$("#savedQueryField_dataName").val();
		var dataDescription = this.$("#savedQueryField_dataDesc").val();
		
		var validationPassed = true;
		// validate values
		
		// closes all old messages
		$.ibisMessaging("close", {type: "primary"});
		
		if (!selectedMetaStudy) {
			$.ibisMessaging("primary", "error", "You must select a Meta Study to send the saved query.", {container: "#metaStudyMessages"});
			validationPassed = false;
		}
		
		if (sendQueryFilters != "yes" && sendData != "yes") {
			$.ibisMessaging("primary", "error", "You must select to either send the query or data", {container: "#metaStudyMessages"});
			validationPassed = false;
		}
		
		if (sendQueryFilters == "yes") {
			if (queryName == "" || queryName == null) {
				$.ibisMessaging("primary", "error", "The saved query name is a required field when sending a saved query to the meta study", {container: "#metaStudyMessages"});
				validationPassed = false;
			}
			
			if (queryName.length > 100) {
				$.ibisMessaging("primary", "error", "The saved query name is too long.  It must be shorter than 100 characters", {container: "#metaStudyMessages"});
				validationPassed = false;
			}
			
			var regex = /[\\\/:\*\?\|\<\>]/g
			if (queryName.match(regex)) {
				$.ibisMessaging("primary", "error", "The saved query name cannot contain the characters \, /, :, *, ?, |, <, or >", {container: "#metaStudyMessages"});
				validationPassed = false;
			}
			
		}
		
		if (sendData == "yes") {
			if (dataName == "" || dataName == null) {
				$.ibisMessaging("primary", "error", "The saved data file name is a required field when sending data to the meta study", {container: "#metaStudyMessages"});
				validationPassed = false;
			}
			if (dataDescription == "" || dataDescription == null) {
				$.ibisMessaging("primary", "error", "The data description is a required field when sending data to the meta study", {container: "#metaStudyMessages"});
				validationPassed = false;
			}
		}
		
		if (validationPassed) {
			$.ibisMessaging("close", {type: "primary"});
			
			var view = this;
			
			var data = {
					sendQueryFilters : sendQueryFilters,
					queryName : queryName,
					sendData : sendData,
					dataName : dataName,
					metaStudyId : selectedMetaStudy,
					filterExpression : QueryTool.query.filters.getExpression()
			};
			
			$.ajax({
				type : "POST",
				cache : false,
				url : "service/metaStudies/validateLinkSavedQueryData",
				data : data,
				success : function(data, textStatus, jqXHR) {
					
					$("#validateQueryName").empty();
					$("#validateDataName").empty();
					var imageName ='<img alt="" src="/query/images/icon-warning.gif">';
						
					 if(data.queryNameUnique==false || data.dataNameUnique==false){
						
						
						if(data.queryNameUnique==false ){
							var validateQueryName = "There is an existing saved query in this Meta Study with this name." +
									"Using this name will overwrite the previous saved query with this same name.";
							
							$("#validateQueryName").html(imageName+validateQueryName);
							
						}
						
						if(data.dataNameUnique==false){
							var validateDataName ="There is an existing saved filtered data file in this Meta Study with this name." +
									"Using this name will overwrite the previous filtered data with the same name.";
							
							$("#validateDataName").html(imageName+validateDataName);
						}
						
						EventBus.trigger("open:sendToMetaStudyValidationDialog",data);
					}	
					else if(data.globalQueryNameUnique==false){
						var validateQueryName = "There is an existing saved query with the same name in a different Meta Study. Query name must be unique";
					
						$("#validateQueryName").html(imageName+validateQueryName);
					}
					else{											
						view.linkSavedQueryData();	
						
					}
				},
				error : function(jqXHR, textStatus, errorThrown) {
		
				}
			});		
		}
		
	},

	linkSavedQueryData : function() {
		
		var selectedMetaStudy = this.$(".metaStudySelector:checked").val();	
		var sendQueryFilters = this.$('input[name="saveFilters"]:checked').val();
		var queryName = this.$("#savedQueryField_name").val();
		var queryDescription = this.$("#savedQueryField_desc").val();
		var sendData = this.$('input[name="saveData"]:checked').val();
		var dataName = this.$("#savedQueryField_dataName").val();
		var dataDescription = this.$("#savedQueryField_dataDesc").val();
		
		var dataForSave = {
				sendQueryFilters : sendQueryFilters,
				queryName : queryName,
				queryDescription : queryDescription,
				sendData : sendData,
				dataName : dataName,
				dataDescription : dataDescription,
				metaStudyId : selectedMetaStudy,
				filterExpression : QueryTool.query.filters.getExpression(),
				outputCode : QueryTool.page.get("query").get("outputCodeSelection")
		};
		
		
		var view = this;
		
		$.ajaxSettings.traditional = true;
		$.ajax({
			type : "POST",
			cache : false,
			url : "service/metaStudies/linkSavedQueryData",
			data : dataForSave,
			success : function(data, textStatus, jqXHR) {
				$.ibisMessaging("flash", "success", "Your data has been successfully linked to Meta Study.");
				view.$(".metaStudySelector").prop("checked", false);
				view.closeDialog();
			},
			error : function(jqXHR, textStatus, errorThrown) {
				// I'm not sure yet what the format of this message will be but I'll figure that out
				// and draw errors the same way I did 
				// TODO: fill in
			}
		});
	}
	
});