/**
 *
 */
QT.SaveQueryDialogView = BaseView.extend({
	model : null,
	Dialog : null,
	
	dialogConfigs : {
		buttons : [],
		height : "auto",
		width : "70%",
		position : {my: "center top+10%", at: "center top+10%", of: window},
		
		close : function(event) {
			EventBus.trigger("close:saveQueryDialog");
		}
	},
	
	events : {
		"click #cancelSavedQueryBtn" : "closeDialog",
		"click #saveQueryBtn" : "saveQueryToServer",
		"click #deleteSavedQueryBtn" : "delBtnClickHandler",
		"click .removeLink" : "deleteLinkedUser",
		"click #grantPermissionBtn" : "addLinkedUser",
		"change select.userPermission" : "updateUserPermission",
		"change #queryName" : "updateQueryName",
		"change #queryDesc" : "updateDescription"
	},
	
	initialized : false,
	availableUsers : null,
	editMode : "",
	oldModel : null, 
	deletedUsers : null,
	
	/**
	 * Initializes the SavedQueryDialogView. Listening to open and close events for the saved query dialog.
	 * Listening to the all of the data cart change events to check if the save query button should be disabled or not.
	 * The list of available users is also initialized at this point.
	 */
	initialize : function() {
		this.Dialog = new QT.Dialog();
		this.availableUsers = new QT.Users();
		this.updateAvailableUserList();
		this.deletedUsers = new QT.Users();
		this.template = TemplateManager.getTemplate("saveQueryDialogTemplate");
		
		EventBus.on("open:saveQueryDialog", this.openDialog, this);
		EventBus.on("close:saveQueryDialog", this.dialogCloseListener, this);
		
		QT.SaveQueryDialogView.__super__.initialize.call(this);
	},
	
	/**
	 * Renders the content of the dialog box. This function may be called as needed by any process. It doesn't matter if the 
	 * dialog box is open or not.
	 */
	render : function() {
		var templateModel = this.serializeModel();
		
		// Add in the available user collection.
		templateModel.availableUsers = this.availableUsers.toJSON();
		
		this.$el.html(this.template(templateModel));
		
		// Remove the delete link and disable permission selection for the owner.
		var owner = this.model.get("linkedUsers").find(function(user) {
			return user.get("permissions").findWhere({permission: "Owner"}).get("selected");
		}, this);
		
		if ( typeof owner != "undefined" ) {
			var $row = this.$("#permUsr_" + owner.get("id"));
			
			$row.find("a").remove();
			$row.find("select").prop("disabled", true);
		}
		
		// Ensure that the user selection drop down defaults to the first enabled user.
		this.$("#userSelection option:enabled:first").prop("selected", true);
		
		// Show the delete button if in edit mode.
		if ( this.editMode === "edit" ) {
			this.$("#deleteSavedQueryBtn").show();
		}
		
		// Initialize the Dialog object if needed.
		if ( !this.initialized ) {
			var localConfig = $.extend({}, this.dialogConfigs, {$container: this.$el});
			
			this.Dialog.init(this, this.model, localConfig);
			QT.SaveQueryDialogView.__super__.render.call(this);
			this.initialized = true;
		}
		
		this.canDelete();
	},
	
	/**
	 * Translates the saved query model of this view into a plan JavaScript object. The returned object
	 * can be suitable for JSON string transformation, or to be applied to a Handlebar template.
	 * 
	 * @return Plain JavaScript object of the this view's SavedQuery model.
	 */
	serializeModel : function() {
		var flatModel = this.model.toJSON();
		
		// Flatten the linked users collection.
		flatModel.linkedUsers = flatModel.linkedUsers.toJSON();
		
		for ( var i = 0; i < flatModel.linkedUsers.length; i++ ) {
			flatModel.linkedUsers[i].assignedPermission = flatModel.linkedUsers[i].assignedPermission.toJSON();
			flatModel.linkedUsers[i].permissions = flatModel.linkedUsers[i].permissions.toJSON();
		}
		
		return flatModel;
	},
	
	/**
	 * Handles the "open:saveQueryDialog" event for this view. Depending on the "mode" passed to the event, the view's model
	 * will be set to either a new empty SavedQuery model object, or an existing model object that was passed into the event trigger. 
	 * If the "create" mode is initiated, then control will be transferred to the "openForCreate()" method after an empty SavedQuery model is set.
	 * If the "edit" mode is initiated, then the view's model will be set to the "newModel" parameter and control will be passed to the "openForEdit()" 
	 * function.
	 * 
	 * @param mode - Either "create" or "edit".
	 * @param newModel - Only applies to the "edit" mode. Specifies the new model for this view.
	 * @throws Exception When the mode is invalid or not recognized.
	 */
	openDialog : function(mode, newModel) {
		// Verify that the mode is valid.
		if ( (typeof mode != "undefined") && (mode != null) && (typeof mode == "string") ) {
			// Reset dialog box positioning.
			this.Dialog.config.$container.dialog("option", "position", this.dialogConfigs.position);
			
			switch (mode) {
				case "create":
					// Check if the data cart is empty.
					if ( QueryTool.dataCart.isEmpty() ) {
						return;
					}
					
					this.editMode = mode;
					this.model = new QT.SavedQuery();
					this.openForCreate();
					break;
				case "edit":
					// Check if the new model was passed in as a parameter.
					if ( (typeof newModel == "undefined") || (newModel == null) ) {
						throw "The \"newModel\" parameter must be defined for defined query edit mode.";
					}
					
					this.editMode = mode;
					this.model = newModel;
					this.openForEdit();
					break;
				default:
					throw "Unsupported mode \"" + mode + ".\" Ignorning defined query dialog open request.";
					break;
			} 
		}
		else {
			throw "Invalid mode. Ignoring defined query dialog open request.";
		}
	},
	
	/**
	 * Prepares the saved query dialog for creation mode. The current user is set as the saved query's owner, and is marked as
	 * disabled. The content of the dialog is then rendered with the empty SavedQuery model.
	 */
	openForCreate : function() {
		// Add in the current user as the owner of this saved query.
		this.addCurrentUserAsOwner();
		
		// Clear edit mode specific data.
		this.oldModel = null;
		this.deletedUsers.reset();
		
		this.render();
		this.Dialog.config.$container.dialog("option", "title", "Create Defined Query");
		this.Dialog.open(this);
	},
	
	/**
	 * Prepares the saved query dialog for edit mode. The list of available users is then parsed, and any users that corresponds to
	 * the saved query's permissions is disabled. The dialog is then rendered and opened.
	 */
	openForEdit : function() {
		// Disable the granted users from the available users collection.
		this.disableGrantedUsers();
		
		// Mark all assigned permissions as selected.
		this.syncAssignedPermissionsWithList();
		
		// Clone the current model for later comparison.
		this.oldModel = this.model.clone();
		
		// Clear the deleted users collection.
		this.deletedUsers.reset();
		
		this.render();
		this.Dialog.config.$container.dialog("option", "title", "Edit Defined Query");
		this.Dialog.open(this);
	},
	
	/**
	 * Ensures that the all linked user's assigned permission will be marked as selected. This
	 * function will also verify that there is an owner set for this saved query.
	 */
	syncAssignedPermissionsWithList : function() {
		var hasOwner = false;
		var currentUser = null;
		
		this.model.get("linkedUsers").each(function(user) {
			var currentPerm = user.get("assignedPermission");
			
			if ( currentPerm != null ) {
				// Set the assigned permission as selected, and de-select all others.
				user.get("permissions").each(function(perm) {
					if ( perm.get("permission") == currentPerm.get("permission") ) {
						perm.set("selected", true);
					}
					else {
						perm.set("selected", false);
					}
				}, this);
				
				// Check for owner permission.
				if ( currentPerm.get("permission") === "Owner" ) {
					hasOwner = true;
				}
			}
			else {
				user.set("assignedPermission", new QT.UserPermission());
			}
			
			// Check if this is the current user.
			if ( user.get("userName") == System.user.username ) {
				currentUser = user;
			}
		}, this);
		
		// Check if there was an owner for this saved query.
		if ( !hasOwner ) {
			// Set the current logged in user as the owner.
			if ( currentUser != null ) {
				currentUser.get("permissions").each(function(perm) {
					if ( perm.get("permission") === "Owner" ) {
						perm.set("selected", true);
					}
					else {
						perm.set("selected", false);
					}
				}, this);
			}
			else {
				this.addCurrentUserAsOwner();
			}
		}
	},
	
	/**
	 * Adds the current user in as the owner of this saved query.
	 */
	addCurrentUserAsOwner : function() {
		var currentUser = this.availableUsers.findWhere({userName: System.user.username});
		var owner = currentUser.clone();
		
		owner.get("permissions").findWhere({permission: "Owner"}).set("selected", true);
		this.model.get("linkedUsers").add(owner);
		
		// Disable the current user from the user selection drop down.
		currentUser.set("disabled", true);
	},
	
	/**
	 * Convenience method to call the close function for the underlining Dialog object. Called when the cancel button is clicked, or 
	 * when the changes to the saved query saves correctly.
	 */
	closeDialog : function() {
		this.Dialog.close();
	},
	
	/**
	 * Listener that is tied to the dialog's close callback. Runs clean up operations when the dialog closes. 
	 * Any available users that are disabled are re-enabled, and any messages in the dialog are cleared.
	 */
	dialogCloseListener : function() {
		this.resetDisabledUsers();
		
		// Clear out any messages.
		this.$("#saveQueryMsgs").empty();
	},
	
	/**
	 * Listener for the update events from the query name textbox. Once triggered, the user input is trimmed, validated, and 
	 * then finally applied to the SavedQuery model. If the validation fails, an error message will be displayed on the dialog.
	 * 
	 * @param event - The event object for the name textbox's change event.
	 */
	updateQueryName : function(event) {
		var name = $.trim($(event.currentTarget).val());
		
		// Clear out any messages.
		this.$("#saveQueryMsgs").empty();
		
		// Validate the name.
		if ( name.length == 0 ) {
			$.ibisMessaging("primary", "error", "A name is required.", {container: "#saveQueryMsgs"});
		}
		
		this.model.set("name", name);
	},
	
	/**
	 * Listener for the update events from the query description textarea. Once triggered, the user input is trimmed, and the data
	 * is applied to the SavedQuery model.
	 */
	updateDescription : function(event) {
		var desc = $.trim($(event.currentTarget).val());
		
		this.model.set("description", desc);
	},
	
	/**
	 * Listener for the "Save" button. The associated function is hardened against accidental double clicks. Once triggered, the "Save" 
	 * button is disabled first. Once the button is disabled, the user's data is validated once more, then finally sent to the server 
	 * to be saved to the database. The "Save" button is re-enabled after a successful save, or when an error is detected.
	 */
	saveQueryToServer : _.debounce(function(event) {
		var $saveBtn = $(event.currentTarget);
		
		// Disable the save button.
		$saveBtn.prop("disabled", true);
		$saveBtn.addClass("disabled");
		
		// Clear any old error messages.
		this.$("#saveQueryMsgs").empty();
		
		// Sync the model with the form fields.
		this.model.set("name", this.$("#queryName").val());
		this.model.set("description", this.$("#queryDesc").val());
		
		// If the model data is valid, continue with the save process.
		if ( this.validateModelData() ) {
			// Show processing dialog box.
			EventBus.trigger("open:processing", "Saving the \"" + this.model.get("name") + "\" defined query...");
			
			// Store the selected permission in each linked user.
			this.model.get("linkedUsers").each(function(user) {
				var perm = user.get("permissions").findWhere({selected: true}).get("permission");
				
				// Set the selected permission as the user's assigned permission.
				user.get("assignedPermission").set("permission", perm);
			}, this);
			
			var postData = this.serializeModel();
			var view = this;
			
			// Add in the edit mode and old data.
			postData.editMode = this.editMode;
			
			if (this.oldModel != null) {
				postData.oldName = this.oldModel.get("name");
			}
			
			QT.SavedQueryLoadUtil.updateSavedQueryModel(this.model);
			
			$.ajax({
				type : "POST",
				cache: false,
				contentType : "application/json; charset=UTF-8",
				url : System.urls.savedQueryInternalSave,
				data : JSON.stringify(postData),
				dataType : "json",
				
				success : function(data) {
					// Add an entry in the defined query list.
					var dq = new QT.DefinedQuery({
						id : data.id,
						name : data.name,
						uri : "savedquery"
					});
					
					if (postData.editMode === "edit") {
						QueryTool.page.get("definedQueries").remove(data.id);
					} 
					
					QueryTool.page.get("definedQueries").add(dq, {merge: true});
					
					// Mark the newly created query to be selected
					$("input[name=savedQuerySelectionRadio][value=" + data.id + "]").prop('checked', true).trigger("click");
					
					// Show the success message and close the dialog and processing boxes.
					$.ibisMessaging("flash", "success", "The \"" + data.name + "\" query has been saved successfully.");
					EventBus.trigger("close:processing");
					view.closeDialog();
					
					// Re-enable the save button.
					$saveBtn.prop("disabled", false);
					$saveBtn.removeClass("disabled");
				}
			})
			.fail(function(jqXHR, textStatus, errorThrown ) {
				// If the error response contains any error messages, display them to the user.
				if ( (typeof jqXHR.responseText == "string") && (jqXHR.responseText.length > 0) ) {
					var msgArray = JSON.parse(jqXHR.responseText);
					
					for ( var i = 0; i < msgArray.length; i++ ) {
						$.ibisMessaging("primary", "error", msgArray[i], {container: "#saveQueryMsgs"});
					}
				}
				else {
					$.ibisMessaging("primary", "error", 
						"An error occurred while saving the current query. Please contact the system administrator.",
						{container: "#saveQueryMsgs"});
				}
				
				// Close the processing dialog box.
				EventBus.trigger("close:processing");
				
				// Re-enable the save button.
				$saveBtn.prop("disabled", false);
				$saveBtn.removeClass("disabled");
				
				console.error("Could not save the \"" + view.model.get("name") + "\" query: " + jqXHR.status + " : " + errorThrown);
			});
			
			// Persist any deleted permissions, if in edit mode.
			if ( this.editMode === "edit" ) {
				this.processPermissionDeletions();
			}
		}
		else {
			// Re-enable the save button.
			$saveBtn.prop("disabled", false);
			$saveBtn.removeClass("disabled");
		}
	}, 3000, true),
	
	/**
	 * Runs validation tests against the SavedQuery model. Currently only the query name is validated to be non-empty and unique 
	 * in the system.
	 * 
	 * @return True if and only if the model passes all validation tests.
	 */
	validateModelData : function() {
		var isValid = true;
		var dialogConfig = {
			container: "#saveQueryMsgs"
		};
		var queryName = this.model.get("name");
		
		// Validate the query name.
		if ( queryName.length != 0 ) {
			// Check if there is a duplicate name in the system.
			if ( this.editMode == "create" || ((this.oldModel != null) && (this.oldModel.get("name") != queryName)) ) {
				var encName = encodeURIComponent(queryName);
				
				$.ajax({
					url : System.urls.savedQueryUnique + "/" + encName,
					cache : false,
					async : false,
					type : "GET",
					dataType : "text",
					
					success : function(data) {
						if ( data == "false" ) {
							$.ibisMessaging("primary", "error", "A query named \"" + queryName + "\" is already in the system.", dialogConfig);
							isValid = false;
						}
					},
					
					error : function(jqXHR, textStatus, errorThrown) {
						console.error("Could not test the uniqueness of the query name: " + jqXHR.status + " : " + errorThrown);
						$.ibisMessaging("primary", "error", 
							"Could not properly validate the query name. Please try to submit again.", dialogConfig);
						isValid = false;
					}
				});
			}
			
			// Check if there are any illegal special characters (\ / : * ? | < >) in the saved query name.
			if ( queryName.search(/[\\\/:\*\?\|\<\>]/) != -1 ) {
				$.ibisMessaging("primary", "error", 
						"The query name cannot contain the following special characters: \\ / : * ? | < >", dialogConfig);
				isValid = false;
			}
		}
		else {
			$.ibisMessaging("primary", "error", "A name is required.", dialogConfig);
			isValid = false;
		}
		
		// Verify that the "copyFlag" is not true, which would mean that it is registered with a meta study.
		if ( this.model.get("copyFlag") ) {
			$.ibisMessaging("primary", "error", 
					"The Query Tool cannot save changes to defined queries that are registered to a meta study.", dialogConfig);
			isValid = false;
		}
		
		return isValid;
	},
	
	/**
	 * Make an Account web service call to remove any permissions that are marked for deletion.
	 */
	processPermissionDeletions : function() {
		if ( !this.deletedUsers.isEmpty() ) {
			var view = this;
			var url = System.urls.unregisterEntitiesByIdList;
			var postData = {
				ids : this.deletedUsers.getEntityMapIds()	
			};
			
			// Send delete request for the listed entity map IDs.
			$.post(url, postData, function(data) {
				$.ibisMessaging("flash", "success", "The requested user permissions have been removed from the " 
						+ view.model.get("name") + " defined query.");
				view.deletedUsers.reset();
			})
			.fail(function(jqXHR, textStatus, errorThrown) {
				console.error("Could not delete user permissions: " + jqXHR.status + " : " + errorThrown);
				$.ibisMessaging("flash", "error", "The user permissions could not be removed from the defined query.");
			});
		}
	},
	
	
	/**
	 * Updates the list of available users with accounts returned by a request for all accounts with the "ROLE_QUERY" role. 
	 * If the web service call fails, the error is silently logged to the browser's console.
	 */
	updateAvailableUserList : function() {
		var url = "service/accounts";
		var view = this;
		
		// Query the account rest web service for all users that have access to the Query Tool.
		$.get(url, function(data) {
			var $xml = $(data);
			
			// Loop over all accounts returned and add them to the accounts array.
			$xml.find("account").each(function() {
				var $account = $(this);
				var $user = $account.find("user");
				var user = new QT.User({
					id : Number($account.find("id:first").text()),
					userName : $account.find("userName:first").text(),
					firstName : $user.find("firstName").text(),
					lastName : $user.find("lastName").text(),
					email : $user.find("email").text()
				});
				
				// Add the user account to the available user collection.
				view.availableUsers.add(user);
			});
		}, "xml")
		.fail(function(jqXHR, textStatus, errorThrown) {
			console.error("Could not get listing of Query Tool accounts from the Account Rest Web Service.");
			console.error("Text status: " + textStatus);
			console.error("Error thrown: " + errorThrown);
		});
	},
	
	/**
	 * Iterates over the model's linked users, and disables the corresponding users listed in the available users list.
	 */
	disableGrantedUsers : function() {
		this.model.get("linkedUsers").each(function(user, index) {
			this.availableUsers.get(user.get("id")).set("disabled", true);
		}, this);
	},
	
	/**
	 * Searches the available users list for any disabled users, then re-enables them.
	 */
	resetDisabledUsers : function() {
		var resetArray = this.availableUsers.where({disabled: true});
		
		// Re-enable any disabled users.
		for ( var i = 0; i < resetArray.length; i++ ) {
			resetArray[i].set("disabled", false);
		}
	},
	
	/**
	 * Listens for any click events on a "remove" link in the permissions table. Once activated, the user and the permission in that
	 * row is removed from the model's linked user list and from the table. The associated user in the available user dropdown is also
	 * re-enabled.
	 * 
	 * @param event - The event object for the "remove" link's click event.
	 */
	deleteLinkedUser : function(event) {
		var $link = $(event.currentTarget);
		var userId = $link.attr("id").split("_")[1];
		
		try {
			// Remove the user from the model.
			var delUser = this.model.get("linkedUsers").remove(userId);
			
			// Add user to the deleted user collection, if needed.
			if ( (this.editMode === "edit") && (delUser.get("assignedPermission").get("entityMapId") > 0) ) {
				this.deletedUsers.add(delUser);
			}
			
			// Re-enable the user in the available user selection.
			this.availableUsers.get(Number(userId)).set("disabled", false);
			
			// Update the dialog box content.
			$link.parents("tr:first").remove();
			this.$("#usr_" + userId).prop("disabled", false);
			
			// Ensure that the user selection drop down defaults to the first enabled user.
			this.$("#userSelection option:enabled:first").prop("selected", true);
		}
		catch (err) {
			console.error("Could not delete a linked user:\n" + err);
			$.ibisMessaging("dialog", "error", "Could not delete the user.");
		}
	},
	
	/**
	 * Listens for any click events on the "Grant Permission" button. Adds the selected user to the model's linked user list 
	 * with the default permission of "Read." A new row is then added to the permissions table for the selected user.
	 * 
	 * @param event - The event object for the "Grant Permission" button's click event.
	 */
	addLinkedUser : function(event) {
		var $userSelector = this.$("#userSelection");
		var selectedId = Number($userSelector.val());
		var selectedUser = this.findSelectedUser(selectedId);
		
		// Check if a user was retrieved from the user collection.
		if ( (typeof selectedUser != "undefined") && (selectedUser != null) ) {
			// Add new user to linked user collection of the model, and re-render the dialog box.
			selectedUser.get("permissions").findWhere({permission: "Read"}).set("selected", true);
			this.model.get("linkedUsers").add(selectedUser);
			this.render();
		}
		else {
			console.error("Failed to add a user to be permission table.");
			$.ibisMessaging("dialog", "error", "Could not add the selected user to the permission table. Double check selection and try again.");
		}
	},
	
	/**
	 * Searches both the deleted and available user collections for a User model that corresponds to the
	 * given user ID.
	 * 
	 * @param id {Number} - The user ID to search for.
	 * @returns A User model with the given ID, or null if no model can be found.
	 */
	findSelectedUser : function(userId) {
		var selectedUser = null;
		
		// Check the deleted user collection first.
		var deletedUsr = this.deletedUsers.get(userId);
		
		//// If the requested user is in the deleted collection, remove it from the collection, and clear the permission selections.
		if ( (typeof deletedUsr != "undefined") && (deletedUsr != null) ) {
			this.deletedUsers.remove(userId);
			selectedUser = deletedUsr;
			
			// Clear the user's permission selection.
			deletedUsr.get("permissions").each(function(perm) {
				if ( perm.get("selected") ) {
					perm.set("selected", false);
				}
			}, this);
			
			// Disable the available user
			this.availableUsers.get(userId).set("disabled", true);
		}
		// Get the selected user from the available user collection, and disable it.
		else {
			var availUsr = this.availableUsers.get(userId);
			
			selectedUser = availUsr.clone();
			availUsr.set("disabled", true);
		}
		
		return selectedUser;
	},
	
	/**
	 * Listens for any change events to any of the permission dropdowns in the permissions table. The permissions of the associated user is then 
	 * updated in the model.
	 * 
	 * @param event - The event object of the permission dropdown's change event.
	 */
	updateUserPermission : function(event) {
		var $select = $(event.currentTarget);
		var userId = Number($select.parents("tr:first").attr("id").split("_")[1]);
		var targetUser = this.model.get("linkedUsers").get(userId);
		var permCollection = targetUser.get("permissions");
		
		// Check if owner was selected.
		if ( $select.val() == "Owner" ) {
			var pervPermVal = permCollection.findWhere({selected: true}).get("permission");
			var view = this;
			var dialogButtons = [{
				text : "Change Owner",
				click : function() {
					view.changeOwner(targetUser);
					$(this).dialog("close");
				}
			},
			{
				text : "Cancel",
				click : function() {
					$select.val(pervPermVal);
					$(this).dialog("close");
				}
			}];
			
			// Show the confirmation dialog box.
			$.ibisMessaging("dialog", "info", "There can only be one Owner. Are you sure you want to change the owner?", {
				title : "Change Owner?",
				buttons : dialogButtons,
				modal : true,
				closeOnEscape : false,
				
				open : function() {
					$(this).siblings().find(".ui-dialog-titlebar-close").hide();
				}
			})
		}
		else {
			// De-select any previously selected permissions.
			permCollection.each(function(permission) {
				if ( permission.get("selected") ) {
					permission.set("selected", false);
				}
			}, this);
			
			// Mark the new permission as selected.
			permCollection.findWhere({permission: $select.val()}).set("selected", true);
		}
	},
	
	/**
	 * Switch the ownership of the saved query, then render the dialog box content.
	 * 
	 * @param newOwner - The user model of the new owner of the saved query.
	 */
	changeOwner : function(newOwner) {
		var oldOwner = this.model.get("linkedUsers").find(function(user) {
			return user.get("permissions").findWhere({permission: "Owner"}).get("selected");
		}, this);
		
		// Change the old owner's permission to Admin.
		oldOwner.get("permissions").findWhere({selected: true}).set("selected", false);
		oldOwner.get("permissions").findWhere({permission: "Admin"}).set("selected", true);
		
		// Set the new owner.
		newOwner.get("permissions").findWhere({selected: true}).set("selected", false);
		newOwner.get("permissions").findWhere({permission: "Owner"}).set("selected", true);
		
		this.render();
	},
	
	delBtnClickHandler : _.debounce(function(event) {
		var view = this;
		var btns = [{
			text : "Yes",
			
			click : _.debounce(function() {
				view.deleteSavedQuery();
				$(this).dialog("close");
			}, 3000, true)
		},
		{
			text : "No",
			
			click : function() {
				$(this).dialog("close");
			}
		}];
		
		// Display the delete confirmation dialog box.
		$.ibisMessaging("dialog", "info", "Are you sure you want to delete your saved query?", {
			title : "Delete Defined Query?",
			buttons : btns,
			modal : true,
			closeOnEscape : false,
			
			open : function() {
				$(this).siblings().find(".ui-dialog-titlebar-close").hide();
			}
		})
	}, 3000, true),
	
	canDelete : function() {
		//#deleteSavedQueryBtn
		var $button = this.$("#deleteSavedQueryBtn");
		if (!$button.hasClass("disabled")) {
			$button.addClass("disabled");
		}
		var id = this.model.get("id");
		
		$.ajaxSettings.traditional = true;
		var view = this;
		$.ajax({
			type: "GET",
			cache : false,
			url : "service/savedQueries/canDelete",
			data : {
				id : id
			},
			success : function(data, textStatus, jqXHR) {
				// Only enable the saved query edit button if the ID is valid
				if ( _.isNumber(id) && id > 0 && data == "true") {
					$button.removeClass("disabled");
				}
				else {
					$button.addClass("disabled");
				}
			},
			error : function() {
				// TODO: fill in
				if ( window.console && console.log ){
				console.log("error getting permission for edit saved query");
				}
			}
		});
	},
	
	/**
	 * Handles the deletion of the current saved query form the system.
	 */
	deleteSavedQuery : function() {
		if ( this.model.get("id") <= 0 ) {
			return;
		}
		
		var $delBtn = this.$("#deleteSavedQueryBtn");
		
		// Disable the delete button.
		$delBtn.prop("disabled", true);
		$delBtn.addClass("disabled");
		
		// Show processing dialog box.
		EventBus.trigger("open:processing", "Deleting the \"" + this.model.get("name") + "\" defined query...");
		
		// Clear any old error messages.
		this.$("#saveQueryMsgs").empty();
		
		var view = this;
		var url = System.urls.savedQueryRemove + "/" + this.model.get("id");
		
		// Call the saved query web service to delete this saved query from the system.
		$.get(url, function() {
			// Send out the "delete:savedQuery" event.
			EventBus.trigger("delete:savedQuery", view.model.get("id"));
			
			// Show the success message and close the dialog and processing boxes.
			EventBus.trigger("close:processing");
			view.closeDialog();
			$.ibisMessaging("flash", "success", "The \"" + view.model.get("name") + "\" query has been deleted from the system.");
			
			// Re-enable the delete button.
			$delBtn.prop("disabled", false);
			$delBtn.removeClass("disabled");
		})
		.fail(function(jqXHR, textStatus, errorThrown) {
			console.error("Could not delete saved query: " + jqXHR.status + " : " + errorThrown);
			EventBus.trigger("close:processing");
			$.ibisMessaging("primary", "error", 
					"The defined query could not be removed from the system. Please try again later.", {container: "#saveQueryMsgs"});
			
			// Re-enable the delete button.
			$delBtn.prop("disabled", false);
			$delBtn.removeClass("disabled");
		});
	}
});