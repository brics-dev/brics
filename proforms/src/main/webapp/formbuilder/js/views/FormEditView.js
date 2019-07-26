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
						fieldName : "name",
						description : Config.language.formNameLong,
						match : function(model) {
							if (model.get("name").length >= 50) {
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
				this.dialogTitle = "Edit Form | " + this.model.get("name");
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
					
					this.addEditFormInfor(formInfoURL, this.model.serializeModel());
					
					return true;
				}
				else {
					return false;
				}

			},

			cancel : function() {
				if ( Config.formInfoMode == 'add' ) {
					var url = baseUrl + '/form/formHome.action';
					redirectWithReferrer(url);
				}
				
				// Revert changes to the form structure value and any questions
				if ( typeof StateManager.changeSet["dataStructureName"] != "undefined" ) {
					this.model.set("dataStructureName", StateManager.changeSet["dataStructureName"].previousValue);
					this.model.set("dataStructureRadio", StateManager.changeSet["dataStructureRadio"].previousValue);
				}
				
				this.closeDialog();
			},

			addEditFormInfor : function(formInfoURL, params) {
				var thisView = this;
				
				$.ajax({
					type : "POST",
					url : formInfoURL,
					data : params,
					beforeSend : function() {

					},
					
					success : function(response) {
						var successFlag = true;
						var activeEditor = FormBuilder.page.get("activeEditorView");
						
						switch (response) {
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
						default:
							var splits = response.split("::");
							var formID = splits[2];
							thisView.model.set("formid", formID);
							thisView.model.set("id", formID);

							var formName = splits[0];
							FormBuilder.form.set("name", formName);

							var formStatus = splits[1];
							if (!$.isNumeric(formStatus)) {
								formStatus = Config.formStatusInverse[formStatus];
							}
							FormBuilder.form.set("status", formStatus);

							var isDataSpring = splits[3];
							// we don't actually use dataspring any more
							// but let's keep it just in case
							FormBuilder.form.set("isDataSpring",
									(isDataSpring == "true"));

							var dataStructureJSON = $.parseJSON(splits[5]);

							// other splits exist as well but not needed
							// here:
							// 6 = sectionsJSONArrJSONString
							// 7 = questionsJSONArrJSONString

							var dataElementJsonArray = dataStructureJSON['dataElements'];
							var repeatableGroupsJsonArray = dataStructureJSON["repeatableGroupList"];
							var allGroupsJsonArray = dataStructureJSON["allGroupsList"];
							
							var doesFSContainDeprecatedOrRetiredDEs = dataStructureJSON["doesFSContainDeprecatedOrRetiredDEs"];
							if(doesFSContainDeprecatedOrRetiredDEs) {
								$.ibisMessaging("dialog", "warning", Config.language.fsContainsDeprecatedOrRetiredDEs,{modal:true});
							}
							
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
							successFlag = true;
							
						}
						EventBus.trigger("close:processing");
						
						if(successFlag) {
							EventBus.trigger("close:activeEditor");
						}
						
					},
					
					error : function(e) {
						EventBus.trigger("close:processing");
						$.ibisMessaging("dialog", "error", Config.language.saveFormError);
					}
				});
			}
		});