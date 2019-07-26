var SectionEditView = EditorView.extend({
	dialogTitle : "Edit Section",
	templateName : "editSectionTemplate",
	validationRules : [
	                   new ValidationRule({
	                	   fieldName: "name", 
	                	   required: true, 
	                	   description: Config.language.sectionName
	                   }),
	                   
	                   new ValidationRule({
	                	   fieldName : "initRepeatedSecs",
	                	   description : "A Valid Repeatable Min must be entered",
	                	   match : function(model) {
	                		   if (!model.get("isRepeatable")) {
	                			   return true;
	                		   }
	                		   
	                		   var min = FormBuilder.page.get("activeEditorView").$("#initRepeatedSecs").val();
	                		   if (min.trim() == "") {
	                			   return false;
	                		   }else {
	                			   return true;
	                		   }
	                		   
	                		   
	                	   }
	                   }),
	                   
	                   new ValidationRule({
	                	   fieldName : "maxRepeatedSecs",
	                	   description : "A Valid Maximum Number of times viewed must be entered",
	                	   match : function(model) {
	                		   if (!model.get("isRepeatable")) {
	                			   return true;
	                		   }
	                		   var max =  FormBuilder.page.get("activeEditorView").$("#maxRepeatedSecs").val();
	                		   if (max.trim() == "") {
	                			   return false;
	                		   }else {
	                			   return true;
	                		   }
	                	   }
	                   }),
	                   new ValidationRule({
	                	   fieldName : "maxRepeatedSecs",
	                	   description : Config.language.minMax,
	                	   match : function(model) {
	                		   if (!model.get("isRepeatable")) {
	                			   return true;
	                		   }
	                		   var min = Number(model.get("initRepeatedSecs"));
	                		   var max = Number(model.get("maxRepeatedSecs"));
	                		   if (max >= min) {
	                			   return true;
	                		   }
	                		   
	                		   return false;
	                	   }
	                   }),
	                   new ValidationRule({
	                	   fieldName: "initRepeatedSecs", 
	                	   description:  Config.language.initialMin,
	                	   match : function(model) {
	                		   if (!model.get("isRepeatable")) {
	                			   return true;
	                		   }
	                		   var min = Number(model.get("initRepeatedSecs"));
	                		   if (min > 0) {
	                			   return true;
	                		   }
	                		   
	                		   return false;
	                	   }
	                   }),
	                   new ValidationRule({
	                	   fieldName : "maxRepeatedSecs",
	                	   description : Config.language.lessThan45,
	                	   match : function(model) {
	                		   if (!model.get("isRepeatable")) {
	                			   return true;
	                		   }
	                		   var max = Number(model.get("maxRepeatedSecs"));
	                		   if (max < (Number(Config.sections.maxRepeat) + 1)) {
	                			   return true;
	                		   }
	                		   
	                		   return false;
	                	   }
	                   }),
	                   
	                   new ValidationRule({
	                	   fieldName : "description",
	                	   description : Config.language.sectionText,
	                	   match : function(model) {
	                		   if (model.get("description").length >= Config.sections.descriptionMaxLength) {
	                			   return false;
	                		   }
	                		   return true;
	                	   }
	                   }),
	                   
	                   new ValidationRule({
	                	   fieldName : "maxRepeatedSecs",
	                	   description : "",
	                	   match : function(model) {
	                		   var repeatableGroup = FormBuilder.form.repeatableGroups.byName(model.get("repeatableGroupName"));
	                		   if(typeof repeatableGroup !== "undefined" && repeatableGroup.get("repeatableGroupType") == "At Least"){
	                				if(model.get("maxRepeatedSecs") < repeatableGroup.get("repeatableGroupThreshold")
	                						&& repeatableGroup.get("repeatableGroupThreshold")!=0){
	                					this.description = "";
	                					var s = Config.language.atLeast;
	                					this.description = s + repeatableGroup.get("repeatableGroupThreshold");
	                					return false;
	                				}else{
	                					return true;	
	                				}
	                		   }else{
	                			   return true;	                			   
	                		   }
	                	   }
	                   }),
	                   
	                   new ValidationRule({
	                	   fieldName : "maxRepeatedSecs",
	                	   description : "",
	                	   match : function(model) {
	                		   var repeatableGroup = FormBuilder.form.repeatableGroups.byName(model.get("repeatableGroupName"));

	                		   if(typeof repeatableGroup !== "undefined" && repeatableGroup.get("repeatableGroupType") == "Up To"){
	                			   if(model.get("maxRepeatedSecs") > repeatableGroup.get("repeatableGroupThreshold") 
	                					   && repeatableGroup.get("repeatableGroupThreshold")!=0){
	                				   this.description ="";
	                				   var s = Config.language.upTo;
	                				   this.description = s + repeatableGroup.get("repeatableGroupThreshold");
	                				   return false;
	                				}else{
	                					return true;	
	                				}
	                		   }else{
	                			   return true;	                			   
	                		   }
	                	   }
	                   }),
	                   
	                   new ValidationRule({
	                	   fieldName : "maxRepeatedSecs",
	                	   description : "",
	                	   match : function(model) {
	                		   var repeatableGroup = FormBuilder.form.repeatableGroups.byName(model.get("repeatableGroupName"));
	                		   if(typeof repeatableGroup !== "undefined" && repeatableGroup.get("repeatableGroupType") =="Exactly"){
	                			   if(model.get("maxRepeatedSecs") != repeatableGroup.get("repeatableGroupThreshold")
	                					   && repeatableGroup.get("repeatableGroupThreshold")!=0){
	                				   this.description=""; 
	                				   var s = Config.language.exactly; 
	                				    this.description = s +repeatableGroup.get("repeatableGroupThreshold");
	                					return false;
	                				}else{
	                					return true;	
	                				}
	                		   }else{
	                			   return true;	                			   
	                		   }
	                	   }
	                   }),
	                   
	                   // if maxrepeatedsecs decreases, check that the sections
	                   // do not have calculation or skip rules.
	                   // also, same thing with the "is repeatable" check
	                   new ValidationRule({
	                	   fieldName : "maxRepeatedSecs",
	                	   description : Config.language.updateHasSkipOrCalc,
	                	   match : function(model) {
	                		   var changes = StateManager.changeSet;
	                		   if (typeof changes.isRepeatable !== "undefined" 
	                			   && changes.isRepeatable.newValue == false 
	                			   && changes.isRepeatable.previousValue == true) {
	                			   return RepeatableSectionProcessor.checkCanDelete(model);
	                		   }
	                		   if (typeof changes.maxRepeatedSecs !== "undefined" 
	                			   && Number(changes.maxRepeatedSecs.newValue) < Number(changes.maxRepeatedSecs.previousValue)) {
	                			   return RepeatableSectionProcessor.checkCanDelete(model);
	                		   }
	                		   return true;
	                	   }
	                   })
	                   ],
	
	initialize : function() {
		SectionEditView.__super__.initialize.call(this);
		this.template = TemplateManager.getTemplate(this.templateName);
	},
	
	events : {
		"click #opener" : "openRepeatGroupDiaog",
		"change #repeatableGroupName" : "repeatGroupChange"
	},
	
	render : function(model) {
		this.model = model;
		this.model.on("change:isRepeatable", this.showHideRepeatable, this);
		this.dialogTitle = "Edit Section " + model.get("name");
		
		this.$el.html(this.template(model.attributes));
		SectionEditView.__super__.render.call(this, model);
		this.showHideRepeatable(this.model);
		
		this.$('select[name="questionType"] option').prop("disabled", false);
		this.$('select[name="questionType"] option[value="1"]').prop("disabled", true);
		this.$('select[name="questionType"] option[value="2"]').prop("disabled", true);
		this.$('select[name="questionType"] option[value="9"]').prop("disabled", true);
		this.$('select[name="questionType"] option[value="10"]').prop("disabled", true);
		this.$('select[name="questionType"] option[value="11"]').prop("disabled", true);
		this.$('select[name="questionType"] option[value="12"]').prop("disabled", true);
		this.$('select[name="questionType"] option[value="5"]').prop("disabled", true);
		this.$('select[name="questionType"] option[value="6"]').prop("disabled", true);

		this.showRepeatableGroupOptions();
		this.showIcon();
		this.$('input[name="name"]').focus();
		
		return this;
	},
	
	save : function() {
		var saveSuccess = SectionEditView.__super__.save.call(this);
		if (saveSuccess) {
			EventBus.trigger("save:section", this.model);
		}
	},
	
	cancel : function() {
		SectionEditView.__super__.cancel.call(this);
		
		if (this.model.get("isNew")) {
			EventBus.trigger("delete:section",this.model);
		}
	},
	
	showHideRepeatable : function(model) {
		if (model.get("isRepeatable")) {
			this._makeRepeatable();
		}
		else {
			this._makeNonRepeatable();
		}
	},
	
	close : function() {
		// NOTE: validation has not passed at this point (passed in EditorView)
		// but that's okay since it would have to change again
		EventBus.trigger("change:section", this.model);
		return SectionEditView.__super__.close.call(this);
	},
	
	_makeRepeatable : function() {
		$(".repeatableInput").show();
	},
	
	_makeNonRepeatable : function() {
		$(".repeatableInput").hide();
	},
	
	showRepeatableGroupOptions : function(){
		// the user can no longer change repeatable groups
	},
	
	populateMinMax : function(){
		var repeatableGroup = FormBuilder.form.repeatableGroups.byName($("#repeatableGroupName").val());
		if(typeof repeatableGroup !== "undefined"){
			if(repeatableGroup.get("repeatableGroupType") =="Exactly"){
				if(repeatableGroup.get("repeatableGroupThreshold") == 0) {
					this.model.set("initRepeatedSecs","1");
					this.model.set("maxRepeatedSecs","1");
				}else {
					this.model.set("initRepeatedSecs",repeatableGroup.get("repeatableGroupThreshold"));
					this.model.set("maxRepeatedSecs",repeatableGroup.get("repeatableGroupThreshold"));
				}
				
			} else if(repeatableGroup.get("repeatableGroupType") =="At Least"){
				if(repeatableGroup.get("repeatableGroupThreshold") == 0) {
					this.model.set("initRepeatedSecs","1");
					this.model.set("maxRepeatedSecs","");
				}else {
					this.model.set("initRepeatedSecs",repeatableGroup.get("repeatableGroupThreshold"));
					this.model.set("maxRepeatedSecs","");
				}

			} else if(repeatableGroup.get("repeatableGroupType") =="Up To"){
				if(repeatableGroup.get("repeatableGroupThreshold") == 0) {
					this.model.set("initRepeatedSecs",1);
					this.model.set("maxRepeatedSecs","");
					
				}else {
					this.model.set("initRepeatedSecs",1);
					this.model.set("maxRepeatedSecs",repeatableGroup.get("repeatableGroupThreshold"));	
					
				}
				
				
				
			}
			
		}
	},
	
	repeatGroupChange : function(){	
		this.populateMinMax();
		if(this.model.previous("repeatableGroupName") == 'None'){
			this.setRepeatableGropuModel(this.model);

		}else{
			if(this.model.get("isNew")== true){
				this.setRepeatableGropuModel(this.model);
			}else{
				this.confirmChange();								
			}
		}
		this.showIcon();
	},
	
	showIcon : function(){
		if($("#repeatableGroupName").val() != 'None'){
			$('#grinfor').html('<a href="javascript:;" id="opener" ><img src="'+baseUrl+'/images/icons/info-icon.png" alt="open the dialog"></a>');
		}else{
			$('#grinfor').html('');
		}
	},
	
	openRepeatGroupDiaog : function(){
		FormBuilder.page.get("repeatableGroupView").render(this.model);
	},
	
	confirmChange : function(){
		$.ibisMessaging("dialog", "warning", Config.language.changeRG, {
		 buttons: [
			{
				text: "Continue",
				click : function() {
					$(this).dialog("close");
					var view = FormBuilder.page.get("sectionEditView");
					view.setRepeatableGropuModel(view.model);
				}
			},
			{
				text: "Cancel",
				click : function() {
					$(this).dialog("close");
					var view = FormBuilder.page.get("sectionEditView");
					view.backModel(view.model);
					view.showIcon();
				}
			}
		]});
	},
	
	setRepeatableGropuModel : function(model){
		/*var preRepeatableGroup = FormBuilder.form.repeatableGroups.byName(model.previous("repeatableGroupName"));
		var repeatableGroup = FormBuilder.form.repeatableGroups.byName($("#repeatableGroupName").val());
		if(typeof preRepeatableGroup !== "undefined"){
			preRepeatableGroup.set("usedByotherSection",false);
		}
		if(typeof repeatableGroup !== "undefined"){
			repeatableGroup.set("usedByotherSection",true);
		}
		this.clearQuestionsDE();*/
	},
	

	
	backModel : function(model){
		model.set("repeatableGroupName",model.previous("repeatableGroupName"),{silent:true});
		$("#repeatableGroupName option").filter(function() {
		    return $(this).text() == model.get("repeatableGroupName"); 
		}).prop('selected', true);
	}
});