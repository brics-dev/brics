var DataElementsView2 = BaseView.extend({

	
	dialogTitle : Config.language.deDialogTitle,
	
	initialize : function() {
		DataElementsView2.__super__.initialize.call(this);
   		this.template = TemplateManager.getTemplate("dataElementsTemplate2");
   	},
   	
   	
	buttons : [{
		id : "dataElementAddBtn",
		text : Config.language.addBtnTitle,
		"class" : "ui-priority-primary",
		
		click : _.debounce(function() {
			$(this).siblings().find("#dataElementAddBtn").prop("disabled", true);
			var selectedOptions = IDT.getSelectedOptions($('#dataElementsTable2'));
			var j = 0;
			var numSelectedOptions = selectedOptions.length;
			if(selectedOptions.length > 0) {
				
				EventBus.trigger("open:Processing", "Adding Data Element(s) to form...");
			
					
				var savingFunctions = [];
	
					
					(function addDE() {
					
						var selectedOption = selectedOptions[j];
						var rgNameAndDeName = selectedOption.replace(/\\/g, "");
						var deName = rgNameAndDeName.substring(rgNameAndDeName.indexOf(".") + 1,rgNameAndDeName.length);
						var groupName = rgNameAndDeName.substring(0,rgNameAndDeName.indexOf("."));
						var formId = FormBuilder.form.get("formid");
						var formIdAndDeName = formId + "_" + deName;
		
						var questionId;
						
			
						///****************need to chnage it back to rgAndnAME
						//first see if questionid can be obtained from the existingQuestionsToDelete list...this means question still exists in database
						var found = false;

						for (var i = 0; i < FormBuilder.form.existingQuestionsToDelete.length; i++) {
							var quest = FormBuilder.form.existingQuestionsToDelete[i];
							//if(typeof quest !== "undefined" || quest == null) {
								var questDeName = quest.dataElementName;
								if(questDeName == rgNameAndDeName) {
									found = true;
									questionId = quest.questionId;
									break;
								}
							//}	
						}
		


							var de = FormBuilder.form.dataElements.byName(rgNameAndDeName);

							var sectionName = de.getRGName();
							
							var sectionModel = FormBuilder.form.getSectionByName(sectionName);
							
							if(typeof sectionModel == "undefined" || sectionModel == null) {
								//make section first!
								
								//var repeatableGroup = FormBuilder.form.allGroups.findWhere({repeatableGroupName: sectionName});
								var repeatableGroup = FormBuilder.form.allGroups.findWhere({repeatableGroupName: sectionName});
								
								var section = AutoBuildSectionUtil.convertRGToSection(repeatableGroup);
								section.isNew = false; 
								sectionModel = section;
								

								//objects in allgroups and repgroups seem to be different...so doing it this way
								var repG = FormBuilder.form.repeatableGroups.findWhere({repeatableGroupName: repeatableGroup.get("repeatableGroupName")});
								var rgIndex = FormBuilder.form.repeatableGroups.indexOf(repG);

								if(rgIndex > 0) {
									var previousRepeatableGroup;
									var previousSectionModel;
									for(var k = rgIndex; k > 0; k--) {
										previousRepeatableGroup = FormBuilder.form.repeatableGroups.at(k - 1);
										previousSectionModel = FormBuilder.form.getSectionByName(previousRepeatableGroup.get("repeatableGroupName"));
										if(typeof previousSectionModel != "undefined") {
											break;
										}
									}
									if(typeof previousSectionModel != "undefined") {
										var previousSectionDiv = $("#" + previousSectionModel.get("divId"));
										section.renderAfter = previousSectionDiv;
									}else {
										section.renderAfter =  $("."+Config.identifiers.formContainer); 
									}
									
									
									
								}else {
									
									section.renderAfter =  $("."+Config.identifiers.formContainer); 

								}

								var sectionModel = FormBuilder.form.addSection(section, true);
								EventBus.trigger("add:section", sectionModel);
								FormBuilder.pageView.setRepeatGroupUsedBy(sectionModel);
								
								if (section.isRepeatable) {
									// calls the change section processor to propagate
									// the repeated section children
									EventBus.trigger("change:section", sectionModel);
								}
								SectionDragHandler.assignPositions();
							}
							
							
							var q
							if(!found) {
							   q = AutoBuildQuestionUtil.generateSingle(questionId,de);
							   
							}else {
								
								for (var i = 0; i < FormBuilder.form.existingQuestionsToDelete.length; i++) {
									//if(typeof quest !== "undefined" || quest == null) {
										var quest = FormBuilder.form.existingQuestionsToDelete[i];
										var questDeName = quest.dataElementName;
										var secId = quest.sectionId;
										if(questDeName == rgNameAndDeName && sectionModel.get("id") == secId) {
											q = quest;
											//FormBuilder.form.existingQuestionsToDelete.remove(quest);
											break;
										}
								     //}
								}
								
								
							}
							q.autoCreated = false;
							q.existingQuestion = true;
							

							
							
							
							
							
							// find question position
							var deOrderInSection = de.get("order");						
							var questionsInSection = sectionModel.getQuestionsInPageOrder();
							var firstQuestion = questionsInSection[0];
							var deOfFirst = null;
							var deOfFirstOrder = null;
							if (typeof firstQuestion !== "undefined") {
								deOfFirst = FormBuilder.form.dataElements.byName(firstQuestion.get("dataElementName"));
								deOfFirstOrder = deOfFirst.get("order");
							}
							
							
		
							if (deOfFirst == null || deOrderInSection < deOfFirstOrder) {
								// renderAfter should be the section div
								var sectionDiv = $("#" + sectionModel.get("divId"));
								q.renderAfter = sectionDiv;
						
							}
							else {
								// renderAfter is the question with order = deOrderInSection - 1
								var previousDe = null;
								for (var i = questionsInSection.length; i > 0; i--) {
									var questionInSection = questionsInSection[i - 1];
									var d = FormBuilder.form.dataElements.byName(questionInSection.get("dataElementName"));
									var dOrder = d.get("order");
									if (dOrder < deOrderInSection) {
										previousDe = d;
										break;
									}
								}
								
								
								
								
								
								
								var previousQuestionModel = FormBuilder.form.findQuestionsWhere({dataElementName : previousDe.get("dataElementName")})[0];
								var questionDiv = $("#" + previousQuestionModel.get("newQuestionDivId"));
								q.renderAfter = questionDiv;
								//q.set("renderAfter", questionDiv);
								
								
							}
							
							
							var questionModel = sectionModel.addQuestion(q);
							if(!found) {
								var isNew = true;
								questionModel.set("dataElementName", rgNameAndDeName);
								savingFunctions.push(questionModel.saveToDatabase(isNew));

							}
							
							if(questionId == -1) {
								//this could happen if user is editing an inported form in which user had
								//deleted some recommended des....so
								//we need to build the question on backend first!
								
								var isNew = true;
								
								savingFunctions.push(questionModel.saveToDatabase(isNew));

								
								
							}
							
							EventBus.trigger("add:question", questionModel);
							
							if(sectionModel.isRepeatableParent()){
								//create the repeatable child questions...but only do it if not found in the list
								if(!found) {
									EventBus.trigger("create:repeatableQuestions", questionModel);
								}else {
									var children = FormBuilder.form.sections.getRepeatableChildren(sectionModel);
									for (var k = 0; k < children.length; k++) {
										var repSecId = children[k].get("id");
										

										for (var i = 0; i < FormBuilder.form.existingQuestionsToDelete.length; i++) {
											var quest = FormBuilder.form.existingQuestionsToDelete[i];
											var questDeName = quest.dataElementName;
											var questSecId = quest.sectionId;
											if(rgNameAndDeName == questDeName && repSecId == questSecId) {
												q = quest;
												children[k].addQuestion(q);
												break;
											}
										}
										
	
									}
								}
							}
							
							// scroll to the new question
							var questionDiv = $('#' + questionModel.get("newQuestionDivId"));
							FormBuilder.pageView.scrollPageTo(questionDiv);
							
							//EventBus.trigger("open:questionEditor", questionModel);
							//var isNew = false;
							//questionModel.saveToDatabase(isNew);
		
							j++;
							if(j<numSelectedOptions) {
								//this is to loop back to addDE
								addDE(); 
							}else {
								QuestionDragHandler.resizeAllSections();
								FormBuilder.page.get("dataElementsView2").$el.dialog("close");
								
								
								//make question active
								EventBus.trigger("change:activeQuestion", questionModel);
								
								
								
							}
							
						
						
						
						
						
					})();  //end addDE
					
					
					// this waits until all saves are complete then performs the ID check
					$.when.apply($, savingFunctions).done(function() {
						// handles any non-saved questions - maybe there's a problem somewhere?
						FormBuilder.form.getAllQuestionsInForm().forEach(function(question) {
							if (question.get("questionId") < 0) {
								var commonQuestions = FormBuilder.form.findFilteredQuestionsWhere({questionName : question.get("questionName")}, function(testQuestion) {
									return testQuestion.cid != question.cid;
								});
								if (commonQuestions.length > 0) {
									// all of those should have the same question ID, version number, and version letter
									question.set("questionId", commonQuestions[0].get("questionId"));
									question.set("questionVersionLetter", commonQuestions[0].get("questionVersionLetter"));
									question.set("questionVersionNumber", commonQuestions[0].get("questionVersionNumber"));
									// recalculate div ID so it can be displayed correctly
									question.calculateDivId(true);
								}
							}
						});
					});

					EventBus.trigger("close:Processing");
				
			}	//end if select options > 0
		}, 1000, true)
	},
	
	{
		text: Config.language.cancelBtnTitle,
		"class":"ui-priority-secondary",
		click: function() {
			$(this).dialog("close");
		}
	}],
   	
   	
   	
   	render : function() {
   		// Bind to events

		EventBus.on("close:dataElementDialog", this.cleanUpView, this);
   		

   		
   		// Since this can be a time-heavy operation, let's let the UI update if needed
		// this also gives time for the Processing display to tell the user what's going on
   		_.delay(_(function() {
   	   		this.$el.html(this.template());
   	   		this.DataElementsDialog.initAndOpen(this);
   		}).bind(this), 1);
   		
   		DataElementsView2.__super__.render.call(this);
   	},
   	
   	
   	DataElementsDialog : {
   		initAndOpen : function(view) {
   			view.$el.dialog({
   				title : view.dialogTitle,
				modal : true,
				width : '98%',
				maxHeight : Config.getMaxDialogHeight(),
				buttons : view.buttons,
				dialogClass : "formBuilder_dialog_noclose",
				
				close : function(event, ui) {
		   			IDT.derenderTable($("#dataElementsTable2"));
		   			$("#dataElementsTable2").empty();
		   			
					EventBus.trigger("close:dataElementDialog");
					$(this).dialog("destroy");
				},
				
				open : function(event, ui) {
					var availableDataElementsArray = [];
					var dataElements = FormBuilder.form.dataElements;
					
					
					dataElements.forEach(function(dataElement) {
						var rgAndDeName = dataElement.get("dataElementName");
						var index = rgAndDeName.indexOf(".");
						var rGroup = rgAndDeName.substring(0,index);
						var deName = rgAndDeName.substring(index+1,rgAndDeName.length);
						
						

						var showRow = true;
						//kkkkkk

						
						// If data element is already associated, then dont display it
		
						var questions = FormBuilder.form.getAllQuestionsInForm();
							
							questions.forEach(function(question){

								var assocDE = question.get("dataElementName");
								//var assocRG = question.getGroupName();

								if (assocDE == rgAndDeName) {
									
									showRow = false;
									
									
									return false;
								}

							});
						
						if (showRow) {
							var DEFullName = dataElement.get("dataElementName");
							var index = DEFullName.indexOf(".");
							var group = DEFullName.substring(0,index);
							var deName = DEFullName.substring(index+1);
							var deType = dataElement.get("dataElementType");
							var restrictionName = dataElement.get("restrictionName");
							var description = dataElement.get("description");
							var requiredType = dataElement.get("requiredType");
							var suggestedQuestion = dataElement.get("suggestedQuestion");
							var de = [];
							
							// Build data element row.
							de.push("<input type=\"checkbox\" name=\"dataElementRadio\" value=\"" +  DEFullName  +  "\" />");
							de.push(deName);
							de.push(group);
							de.push(deType);
							de.push(restrictionName);
							de.push(description);
							de.push(requiredType);
							de.push(suggestedQuestion);
							
							availableDataElementsArray.push(de);
						}
					});   //end big loop

					var availableDataElementsColumns = [
						{"sTitle":""},
						{"sTitle":"Data Element"},
						{"sTitle":"Group Name"},
						{"sTitle":"Data Type"},
						{"sTitle":"Input Restrictions"},
						{"sTitle":"Short Description"},
						{"sTitle":"Required Type"},
						{"sTitle":"Preferred Question Text"}
					];
					
					var availableDataElementsTable = {
						"aaData" : availableDataElementsArray,
						"aoColumns" : availableDataElementsColumns
				    };
					
					IDT.buildSingleData(availableDataElementsTable, $("#dataElementsTable2"));
					
					view.$el.dialog({
						position: {
							my : "center",
							at : "center",
							of : window
						}
					});
					
					EventBus.trigger("close:processing");
				} // end open
   			}); //end view dialog
   		}, //end init and open
   		
   		
   		
   		
   		close : function(view) {
   			view.$el.dialog("close");
		},
		
		isOpen : function(view) {
			return view.$el.dialog("isOpen");
		}
   		
   	} //end DED
	
	
	
	
	
	
});