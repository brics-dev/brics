var FormView = BaseView.extend({
	$formContainer : $(),
	initialize : function() {
		this.$formContainer = Config.containerDiv;
		this.listenTo(this.model.sections, 'add', this.createSection);
		EventBus.on("delete:section", this.deleteSection, this);
		
	},
	render : function() {
		RepeatableSectionProcessor.init();
	},
	
	/**
	 * Adds a section to the page
	 */
	createSection : function(section) {
		var view = new SectionView({model: section});
		// render the new section after the active one
		var renderAfter = section.get("renderAfter");
		if (renderAfter != null) {
			view.render(renderAfter);
		}
		else {
			view.render($("." + Config.identifiers.section+"."+Config.styles.active));
		}

		EventBus.trigger("create:section", section);
		if (!section.isRepeatableChild()) {
			EventBus.trigger("change:activeSection",section);
			if(section.get("isNew")) {
				if(!section.get("disableEditor")) {
					EventBus.trigger("open:sectionEditor", section);
				}else {
					section.set("disableEditor",false);
				}
			}
		}
	},
	
	/**
	 * Responds to the delete:section event to actually remove the section
	 * from the form.
	 * 
	 * @param section the Section Model to remove
	 */
	deleteSection : function(section) {
		if (this.model.sections.contains(section)) {
//			if (section.isRepeatParent()) {
//				// delete all section children
//				var children = FormBuilder.form.sections.getRepeatableChildren(section);
//				_.each(children, function(child){
//					this.model.removeSection(child);
//				});
//			}
			if(section.hasCalculationQuestion()){
				$.ibisMessaging("dialog", "error", Config.language.hasCal);
			}else if(section.hasSkipQuestion()){
				$.ibisMessaging("dialog", "error", Config.language.hasSkip);
			}
			else if (!RepeatableSectionProcessor.checkCanDelete(section)) {
				$.ibisMessaging("dialog", "error", Config.language.hasSkipOrCalc);
			}
			else{
				if(!section.get("isNew") && section.get("tableHeaderType")==0){
					this.confirmDeleting(section);										
				}else{
					var newActive = this.chooseNewActiveSection(section);
					this.model.removeSection(section);
					EventBus.trigger("change:activeSection", newActive);
				}
			}			
		}
	},
	
	confirmDeleting : function(section){
		var mainThis = this;
		$.ibisMessaging("dialog", "warning", Config.language.confirmDelete, {
		 buttons: [
			{
				text: "Yes",
				click : function() {
					$(this).dialog("close");
					var newActive = mainThis.chooseNewActiveSection(section);
					
					// in case this section is on a row, refresh the rows
					SectionDragHandler.refreshBeforeDeleteSection(section);
					
					mainThis.model.removeSection(section);
					EventBus.trigger("change:activeSection", newActive);
				}
			},
			{
				text: "No",
				click : function() {
					$(this).dialog("close");
				}
			}
		],
		modal: true});
	},
	
	/**
	 * Choose which section (if any) should be the new active section after
	 * removing the "current" active section.
	 * 
	 * @param section the section being removed
	 */
	chooseNewActiveSection : function(section) {
		var activeSection = $(".section." + Config.styles.active);
		var allVisibleSections = $(".section");
		var indexInAll = allVisibleSections.index(activeSection);
		// not found!  Find the last one and make it active
		if (indexInAll == -1) {
			var newActiveDivId = allVisibleSections.not(activeSection).last().attr("id");
			return FormBuilder.form.getSectionByDivId(newActiveDivId);
		}
		else if (indexInAll == 0) {
			// the first one.  Are there more?
			if (allVisibleSections.length > 1) {
				// there are, choose the NEXT one
				var newActiveDivId = allVisibleSections.eq(1).attr("id");
				return FormBuilder.form.getSectionByDivId(newActiveDivId);
			}
			else {
				return null;
			}
		}
		else {
			// otherwise, there exists a section before this one
			var newActiveDivId = allVisibleSections.eq(indexInAll - 1).attr("id");
			return FormBuilder.form.getSectionByDivId(newActiveDivId);
		}
	},
	
	close : function() {
		this.stopListening();
	}
});