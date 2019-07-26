/**
 * 
 */
var AutoBuildSectionUtil = {
	generate : function() {
		FormBuilder.page.set("loadingData", true);
		var rgArray = FormBuilder.form.allGroups.toArray();
		var numSections = rgArray.length;
		var j = 0;
		var pageView = FormBuilder.pageView;
		EventBus.trigger("close:processing");
		EventBus.trigger("open:processing", "Drawing Sections");
		if (numSections > 0) {
			setTimeout(function addSingleSection() {
				try {
					var repeatableGroup = rgArray[j];
					var section = AutoBuildSectionUtil.convertRGToSection(repeatableGroup);
					var orgRepeatedSectionParent = section.repeatedSectionParent;
					// we need to build those repeatable groups.  Ugh
					section.isNew = false;
					if(orgRepeatedSectionParent != "-1"){	
						section.repeatedSectionParent = orgRepeatedSectionParent;				
					}
					
					var sectionModel = FormBuilder.form.addSection(section, true);
					EventBus.trigger("add:section", sectionModel);
					pageView.setRepeatGroupUsedBy(sectionModel);
					
					if (section.isRepeatable) {
						// calls the change section processor to propagate
						// the repeated section children
						EventBus.trigger("change:section", sectionModel);
					}
					
					// percent complete.  numQuestions will never be zero here
					pc = (j / numSections) * 100;
					FormBuilder.page.get("processingView").setValueTo(pc);
				}
				catch(e) {
					Log.developer.error(e);
					var message = "A problem occurred loading one of your sections.  I will try to load the others but this form will not be correct. ";
					if (Config.devmode) {
						message += e;
					}
					$.ibisMessaging("dialog", "error", message);
				}
				
				j++;
				if (j < numSections) {
					setTimeout(addSingleSection, 0); // timeout loop
				}
				else {
					SectionDragHandler.assignPositions();
//					EventBus.trigger("close:activeEditor");
		
					// proceed to add all questions
					AutoBuildQuestionUtil.generate();
				}
			});
		}
	},
	
	convertRGToSection : function(repeatableGroup) {
		var groupName = repeatableGroup.get("repeatableGroupName");
		var numSectionsOnPage = FormBuilder.form.sections.length;
		var sectionObj = {
				name 					: 	groupName,
				repeatableGroupName 	: 	groupName,
				id 						: 	"S_" + (numSectionsOnPage + 1) * -1,
				repeatedSectionParent 	: 	"-1"
			};
		sectionObj.isRepeatable=true;
		
		var groupType = repeatableGroup.get("repeatableGroupType");
		var threshold = repeatableGroup.get("repeatableGroupThreshold");
		
		if(groupType == "Exactly"){
			if(threshold == 0) {
				sectionObj.initRepeatedSecs = "1";
				sectionObj.maxRepeatedSecs = Config.sections.defaultRepeat;
			}else {
				if (threshold == "1") {
					sectionObj.isRepeatable = false;
				}
				sectionObj.initRepeatedSecs = threshold;
				sectionObj.maxRepeatedSecs = threshold;
			}
		} else if(groupType == "At Least"){
			if(threshold == 0) {
				sectionObj.initRepeatedSecs = "1";
				sectionObj.maxRepeatedSecs = Config.sections.defaultRepeat;
			}else {
				sectionObj.initRepeatedSecs = threshold;
				sectionObj.maxRepeatedSecs = Config.sections.defaultRepeat;
			}

		} else if(groupType == "Up To"){
			if(threshold == 0) {
				sectionObj.initRepeatedSecs = "1";
				sectionObj.maxRepeatedSecs = Config.sections.defaultRepeat;
				
			}else {
				if (threshold == "1") {
					sectionObj.isRepeatable = false;
				}
				sectionObj.initRepeatedSecs = "1";
				sectionObj.maxRepeatedSecs = threshold;
			}
		}
		return sectionObj;
	}
};