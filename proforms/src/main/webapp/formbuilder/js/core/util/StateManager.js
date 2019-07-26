var StateManager = {
	changeSinceLastSave : true,
	changeSet : {},
	watchedModel : null,
	watchedModelChanged : false,
	
	init : function() {
		EventBus.on("save:global", this.formSave, this);
		
		EventBus.on("add:section", this.create, this);
		EventBus.on("add:question", this.create, this);
		
		EventBus.on("save:section", this.saveModel, this);
		EventBus.on("save:question", this.saveModel, this);
		
		EventBus.on("delete:section", this.remove, this);
		EventBus.on("delete:question", this.remove, this);
		
		EventBus.on("open:sectionEditor", this.onEditorOpen, this);
		EventBus.on("open:questionEditor", this.onEditorOpen, this);
		EventBus.on("open:formEditor", this.onEditorOpen, this);
		EventBus.on("close:activeEditor", this.onEditorClose, this);
		
		// a change in the form attribute of page denotes creation of the form
		EventBus.listenTo(FormBuilder.page, "change:form", this.createForm);
	},
	
	/**
	 * When an editor opens, we want to watch any changes to the values in the
	 * model associated with the editor.  This sets up that monitoring.
	 * 
	 * @param model the model being watched
	 * @param reset whether to reset the changeset or not
	 */
	onEditorOpen : function(model, reset) {
		if (typeof reset === "undefined") {
			reset = true;
		}
		
		if (!FormBuilder.page.get("loadingData")) {
			if (reset) {
				this.resetChangeSet();
			}
			if (typeof this.watchedModel !== "undefined") {
				this.onEditorClose();
			}
			this.watchedModel = model;
			EventBus.listenTo(model, "change", this.watchedModelChange, this);
		}
	},
	
	/**
	 * Stops watching the watched model and clears the changeSet
	 */
	onEditorClose : function() {
		if (!FormBuilder.page.get("loadingData")) {
			this.resetChangeSet();
			EventBus.stopListening(this.watchedModel);
		}
	},
	
	/**
	 * Responds to the watched model changing any of its attributes.  This
	 * maintains the changeSet to keep track of any changed attributes and
	 * historical updates.  This will STOP watching and clear the changeSet
	 * when the editor closes.
	 * 
	 * @param model the "watched model" @see onEditorOpen
	 */
	watchedModelChange : function(model) {
		StateManager.watchedModelChanged = true;
		var changed = model.changed;
		for (var property in changed) {
			if (changed.hasOwnProperty(property)) {
				var currentValue = model.get(property);
				var changeSetElement = StateManager.changeSet[property];
				if (typeof changeSetElement === "undefined") {
					var previousValue = model.previous(property);
					// setting here, so don't use changeSetElement
					if (previousValue != currentValue) {
						StateManager.changeSet[property] = new StateManager.Change(property, previousValue, currentValue);
					}
				}
				else if (currentValue != changeSetElement.previousValue && currentValue != changeSetElement.originalValue) {
					// we're not updating to the same value this field had before, so just update
					StateManager.changeSet[property].newValue = currentValue;
				}
				else {
					// change exists AND previous value (one of them) = new value, so just remove the change
					delete StateManager.changeSet[property];
					if (Object.keys(StateManager.changeSet).length == 0) {
						StateManager.watchedModelChanged = false;
					}
				}
			}
		}
	},
	
	resetChangeSet : function() {
		this.watchedModelChanged = false;
		this.changeSet = {};
	},
	
	/**
	 * Responds to the save form command to change the save status
	 */
	formSave : function() {
		this.changeSinceLastSave = false;
	},
	
	/**
	 * This runs any time ANYTHING on the form changes
	 */
	formModify : function() {
		this.changeSinceLastSave = true;
	},
	
	/**
	 * Responds to the 
	 * @param form
	 */
	createForm : function(form) {
		EventBus.listenTo(form, "change", this.formModify);
	},
	
	/**
	 * Responds to the creation of any new watched model type
	 * other than form - because it's special
	 * 
	 * @param model the model that was created
	 */
	create : function(model) {
		if (!FormBuilder.page.get("loadingData")) {
			EventBus.listenTo(model, "change", this.formModify);
		}
	},
	
	/**
	 * When a watched model is deleted, this removes this class's listeners
	 * from it.
	 * 
	 * @param model the model being deleted
	 */
	remove : function(model) {
		var deleteImagesURL = '';
		var namesToDelete = ["Delete All Images"]; //CtdbConstants
		var qId = model.get('questionId');
		if( qId < 0 ){ // it is a new question
		}else{ // it is a edit question
			deleteImagesURL = baseUrl+'/question/deleteQuestionImage.action?qId='+qId+'&namesToDelete='+namesToDelete;
            this.doDeleteGraphicAjax(deleteImagesURL, namesToDelete);
		}
		
		EventBus.stopListening(model);
	},
	
	doDeleteGraphicAjax : function(deleteImagesURL, namesToDelete){ 
        //var thisView = this;
        $.ajax({
               type:"post",
               url:deleteImagesURL,
 /*            beforeSend: function(){
                     this.hideEditorWarning();
               },*/
               success: function(response){                                 
               },
               error: function(e){
                     alert("error" + e );
               }
        });
        //this.launchSaving(this.model.get('questionId')); // once up uncommend, this need to be deleted
 },

	
	/**
	 * Responds to a save in a model.  This is intended to mark the model as
	 * "no longer new" meaning it shouldn't be deleted next time the editor
	 * closes.
	 * 
	 * @param model the model being saved
	 */
	saveModel : function(model) {
		if (model.get("isNew")) {
			model.set("isNew", false, {silent: true});
		}
	}
};

/**
 * Instantiable class to represent a change in a model
 */
StateManager.Change = function(attribute, previousValue, newValue) {
	this.name = attribute;
	this.previousValue = previousValue;
	this.newValue = newValue;
	this.originalValue = previousValue;
};
StateManager.Change.prototype.getPrevious = function() {
	return this.previousValue;
};
StateManager.Change.prototype.getNew = function() {
	return this.newValue;
};
StateManager.Change.prototype.getOriginal = function() {
	return this.originalValue;
};
