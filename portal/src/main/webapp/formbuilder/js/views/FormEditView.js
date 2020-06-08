var FormEditView = EditorView
		.extend({
			// a function
			templateName : "editFormTemplate",
			dialogTitle : "Edit Form",
			buttons : [ {
				text : "Save and Continue",
				"class":"ui-priority-primary",
				click : _.debounce(function() {
					FormBuilder.page.get("activeEditorView").save();
				}, 1000, true)
			}, {
				text : "Cancel",
				"class":"ui-priority-secondary",
				click : function() {
					FormBuilder.page.get("activeEditorView").cancel();
				}
			} ],
			validationRules : [
					new ValidationRule({
						fieldName : "name",
						required : true,
						description : Config.language.formName
					}),
					new ValidationRule({
						fieldName : "dataStructureName",
						required : true,
						description : Config.language.noFormStructureDefined
					}),
					new ValidationRule({
						fieldName : "shortName",
						required : true,
						description : Config.language.shortName
					}),
					new ValidationRule({
						fieldName : "description",
						description : Config.language.descriptionLong,
						match : function(model) {
							if (model.get("description").length >= 1001) {
								return false;
							}
							return true;
						}
					}),
					new ValidationRule({
						fieldName : "name",
						description : Config.language.formNameLong,
						match : function(model) {
							if (model.get("name").length >= 101) {
								return false;
							}
							return true;
						}
					}),
					new ValidationRule({
						fieldName : "allowMultipleCollectionInstances",
						description : Config.language.multiInstanceDoubleData,
						match : function(model) {
							var double = Number(model.get("dataEntryFlag"));
							var multiInstance = model.get("allowMultipleCollectionInstances");
							if (double == 2 && multiInstance) {
								return false;
							}
							return true;
						}
					}),
					new ValidationRule({
						fieldName : "formHeader",
						description : Config.language.formHeaderLong,
						match : function(model) {
							if (model.get("formHeader").length >= 501) {
								return false;
							}
							return true;
						}
					}),
					new ValidationRule({
						fieldName : "formFooter",
						description : Config.language.formHeaderLong,
						match : function(model) {
							if (model.get("formFooter").length >= 501) {
								return false;
							}
							return true;
						}
					})
					
					
					
			],

			events : {
				"click #form-changeFormStructure" : "initChangeFormStructure",
				"change #form-type":"formTypeOnChange",	
				"change #nonPatientFormType":"nonPatientFormTypeOnChange",	
				"keypress #form-name" : "formNameEnter"
			},

			initialize : function() {
				FormEditView.__super__.initialize.call(this);
				this.template = TemplateManager.getTemplate(this.templateName);
				
			},			
			
			render : function() {
				this.dialogTitle = "Edit eForm | " + this.$el.html(this.model.get("name")).text();
				this.model.set("name", this.$el.html(this.model.get("name")).text());
				this.model.set("description", this.$el.html(this.model.get("description")).text());
				this.$el.html(this.template(this.model.attributes));
				FormEditView.__super__.render.call(this, this.model);

				this.formNameRender();
				this.formStatusRender();
				
				//need to show/hide the form type
				var formTypeId = this.model.get("formtypeid");
				if(formTypeId == 10) {
					this.$('#nonPatientFormTypeDiv').hide();
					this.$('#form-type').val("Subject");
					if(this.model.get("formid") != 0) {
						this.$("#form-type").prop("disabled",true);
					} 
				}else {
					this.$('#nonPatientFormTypeDiv').show();
					this.$('#form-type').val("Non-Subject");
					var npFormType = String(formTypeId);
					this.$('#nonPatientFormType').val(npFormType);
					if(this.model.get("formid") != 0) {
						this.$("#form-type").prop("disabled",true);
						this.$("#nonPatientFormType").prop("disabled",true);
					} 
				}
				this.disableMultipleInstances();
				
				return this;
			},
			closeDialog : function() {
				this._closeDialog(this);
			},

			/**
			 * This is called during render to set a "display value" for the
			 * form structure. It will be replaced by ModelBinder if the FS is
			 * changed.
			 */
			formNameRender : function() {
				if (!this.model.get("dataStructureName")) {
					this.$('div[name="dataStructureName"]').html(
							'<a href="javascript:;" id="form-changeFormStructure">'
								+
							'<span class="' + Config.styles.errorField + '">'
									
									+ Config.language.noFormStructureDefined
									+ Config.language.clickHereFix + '</span></a>');
				}
			},

			formStatusRender : function() {
				this.$("#form-status").text(
						Config.formStatusNames[this.model.get("status")]);
			},
			
			formNameEnter:function(event) {
				  if ( event.which == 13 ) {
					     event.preventDefault();
					     $('.ui-dialog-buttonset .ui-priority-primary.ui-button:visible').click();
					  }
			},
			
			
			
			formTypeOnChange : function() {
				if(this.$('#form-type').val() == "Subject"){
					this.$('#nonPatientFormTypeDiv').hide();
					this.model.set("formtypeid",10);
				}else {
					this.$('#nonPatientFormTypeDiv').show();
					var nonPatientFormType = Number(this.$('#nonPatientFormType').val());
					this.model.set("formtypeid",nonPatientFormType);
					
				}
				
			},
			
			
			nonPatientFormTypeOnChange : function() {
				var nonPatientFormType = Number(this.$('#nonPatientFormType').val());
				this.model.set("formtypeid",nonPatientFormType);
				
			},

			initChangeFormStructure : function() {
					EventBus.trigger("open:processing", Config.language.loadFormStructTable);
					EventBus.trigger("open:formStructure");
			},

			save : function() {
				if (this.validate()) {
					EventBus.trigger("open:processing", Config.language.creatingForm);
					
					// If the form structure linkage has changed, unlink the data element from all questions
					if ( typeof StateManager.changeSet["dataStructureName"] != "undefined" ) {
						var questions = this.model.getAllQuestionsInForm();
						
						questions.forEach(function(question) {
							question.removeDE();
						});
					}
					
					var formInfoURL;
					
					if (Config.formInfoMode == 'add' && this.model.get("formId") !== 0) {
						formInfoURL = Config.baseUrl + Config.urls.saveFormInfo;
						
					}
					else {
						formInfoURL = Config.baseUrl + Config.urls.getFormInfo;
					}
					
					
					var view = this;
					
					//Back End validation for short name
					$.ajax({
						  type: "POST",
						  url: baseUrl+"validateEformAction!validateEform.action",
						  data: {shortName:this.model.get("shortName"),eFormAction:"continue"},
						  dataType :"json",
						  success: function(response, status, jqxhr) {
							  $.ibisMessaging("close", {type:"primary"}); 
							  var data = $.parseJSON(response);
							  for (var i=0; i<data.length; i++){
								  	$.ibisMessaging("primary", "error", data[i].msgType,{container: "#errorContainer"});
							  }
							  EventBus.trigger("close:processing");
							  if(data.length==0){
								  view.addEditFormInfor(formInfoURL, view.model.serializeModel());
								  this.closeDialog();
							  }
							
						  },
						  error : function(e) {
							 // alert("error"+e);
						  }
					});
					
					return true;
				}
				else {
					return false;
				}
				
		
				
				
			

			},

			cancel : function() {
				if (Config.copyMode=='true') {
					var sessionFormid=Config.formForm.formid;
					var deleteUrl = baseUrl+"/eformDeleteAction!delete.action";
					EventBus.trigger("open:processing", Config.language.cleaningCopiedData);
					$.ajax({
						type : "POST",
						url : deleteUrl,
						data : {eformId:sessionFormid},
						success : function(data) {
							var redirectUrlToList = baseUrl + 'eFormSearchAction!list.action';
							redirectWithReferrer(redirectUrlToList);
						}
						});
					
				}
				if ( Config.formInfoMode == 'add') {
					//var url = baseUrl + '/form/formHome.action';
					//redirectWithReferrer(url);
					var url = baseUrl + 'eFormSearchAction!list.action';
					redirectWithReferrer(url);
				}
			
				
				// Revert changes to the form structure value and any questions
				if ( typeof StateManager.changeSet["dataStructureName"] != "undefined" ) {
					this.model.set("dataStructureName", StateManager.changeSet["dataStructureName"].previousValue);
					this.model.set("dataStructureRadio", StateManager.changeSet["dataStructureRadio"].previousValue);
				}
				
				this.closeDialog();
			},
			
			disableMultipleInstances : function(){
			    if(FormBuilder.form.get('isCAT') && FormBuilder.form.get("measurementType") != 'shortForm'){
					$('.advanced_chk').prop('checked', false);
					$('.advanced_chk').prop("disabled", true);
				}else{
					if(Config.formInfoMode == 'edit'){
						$('#advanced_cat_msg').hide();
					}
				}
			},

			addEditFormInfor : function(formInfoURL, params) {
				var thisView = this;

				$.ajax({
					type : "POST",
					url : formInfoURL,
					data : params,
					beforeSend : function() {
						
					},
					
					success : function(data) {
						var successFlag = true;
						var activeEditor = FormBuilder.page.get("activeEditorView");
						
						switch (data) {
						case 'errors.duplicate.form':
							activeEditor.showEditorWarning(Config.language.dupFormError);
							activeEditor.highlightFailedFields([{name: "name", description: ""}]);
							successFlag = false;
							break;
						case 'errors.save.data':
							activeEditor.showEditorWarning(Config.language.saveFormError);
							successFlag = false;
							break;
						case 'errors.required.data.elements':
							activeEditor.showEditorWarning(Config.language.requriedDeError);
							break;
						case 'errors.web.service':
							activeEditor.showEditorWarning(Config.language.fsWsCallError);
							successFlag = false;
							break;
						case 'webserviceException':
							activeEditor.showEditorWarning(Config.language.fsWsCallError);
							successFlag = false;
							break;
							//TODO 
							//Hanlde error for duplicate Short Name
							
						default:
								//only nneed to build the form if you are starting form building for first time
							if ((Config.formInfoMode == 'add' && Config.builderStarted == 'false')) {							
								var response = $.parseJSON(data);
								var dataElementJsonArray = response['dataElements'];
								var repeatableGroupsJsonArray = response["repeatableGroupList"];
								var allGroupsJsonArray = response["allGroupsList"];
								
								//added by Ching-Heng
							    var isCAT = response["isCAT"];
							    FormBuilder.form.set("isCAT", isCAT);
							    var measurementType = response["measurementType"];
							    FormBuilder.form.set("measurementType", measurementType);							    
							    FormBuilder.page.get("formEditView").disableMultipleInstances();
							    
								FormBuilder.form.loadDataElements(dataElementJsonArray);
								FormBuilder.form.loadRepeatableGroups(repeatableGroupsJsonArray);
							    FormBuilder.form.loadAllGroups(allGroupsJsonArray);								
								
								// only auto build sections if just created the form but not if editing
								if (Config.formInfoMode == 'add' && Config.autoGenerate) {
									AutoBuildSectionUtil.generate();
									// the above calls the below after it is finished
									//AutoBuildQuestionUtil.generate();
								}
								Config.formInfoMode = 'edit';
								Config.builderStarted = 'true';
								
								successFlag = true;
								EventBus.trigger("close:processing");
							
								if(successFlag) {
									EventBus.trigger("close:activeEditor");
									if(FormBuilder.form.get('isCAT') && FormBuilder.form.get("measurementType") != 'shortForm'){
										$('#catMsg').show();
									}
								}						
							}else {
								EventBus.trigger("close:activeEditor");
							}
						
						
						}
					},
					
					
					error : function(e) {
						EventBus.trigger("close:processing");
						$.ibisMessaging("dialog", "error", Config.language.saveFormError);
					}
				});
			}
		});