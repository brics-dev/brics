/**
 * controls the table operation buttons based on number of checkboxes checked.
 * Called by the datatable.
 */
var OpButtonController = {
	initialized : false,
	
	/**
	 * Handle the case where no checkboxes are selected
	 * 
	 * @param button jQuery reference to the button (called inside each())
	 */
	noneSelected : function(button) {
		if (!button.hasClass("alwaysEnabled") && !button.hasClass("enabledOnNone")) {
			button.prop("disabled",true);
		}
		else {
			button.prop("disabled", false);
		}
	},
	
	/**
	 * Handle the case where one checkbox is selected
	 * 
	 * @param button jQuery reference to the button (called inside each())
	 */
	oneSelected : function(button) {
		if (!button.hasClass("alwaysDisabled")) {
			button.prop('disabled',false);
		}
		else {
			button.prop('disabled',true);
		}
	},
	
	/**
	 * Handle the case where many checkboxes are selected
	 * 
	 * @param button jQuery reference to the button (called inside each())
	 */
	manySelected : function(button) {
		if (!button.hasClass("alwaysEnabled") && !button.hasClass("enabledOnMany")) {
			button.prop("disabled",true);
		}
		else {
			button.prop("disabled", false);
		}
	},
	
	/**
	 * Initializes the datatable's operation buttons (if any exist).
	 * This method is called ONLY ONCE when the table is created.
	 * These operations include:
	 * 		Move the buttons to their correct location
	 * @param $table the table to initialize
	 */
	initTable : function($table) {
		var parentULRef = $table.parent().parent().children("ul");
		var $dataTableContainer = parentULRef.parents(".dataTableContainer").eq(0);
		if (parentULRef.length > 0) {
			var headerRef = $table.parent().children(":first");
			$(headerRef).prepend($(parentULRef));
		}
		// add a clearfix to the header
		$(".ui-widget-header").append('<div class="clearboth"></div>');
		
		/*
		 * Controls action buttons at the top of data tables based on the number of checkboxes
		 * checked inside the table. .dataTableContainer input[type="submit"], 
		 */
		$dataTableContainer.find('input[type="submit"], input[type="button"], button').prop('disabled', true);
		$dataTableContainer.find('input[type="button"].alwaysEnabled, button.alwaysEnabled, input[type="button"].enabledOnNone, button.enabledOnNone').prop('disabled', false);
		$dataTableContainer.find('input[type="submit"].alwaysEnabled, submit.alwaysEnabled, input[type="submit"].enabledOnNone, submit.enabledOnNone').prop('disabled', false);
		
		if (!this.initialized) {
			EventBus.on("select:row", function(saveVal, tableModel) {
				this.selectedChange(null, tableModel.get("$el"));
			}, this);
			
			EventBus.on("deselect:row", function(saveVal, tableModel) {
				this.selectedChange(null, tableModel.get("$el"));
			}, this);
			
			EventBus.on("select:all", function(tableModel) {
				this.selectedChange(null, tableModel.get("$el"));
			}, this);
			
			EventBus.on("deselect:all", function(tableModel) {
				this.selectedChange(null, tableModel.get("$el"));
			}, this);
			
			EventBus.on("select:set", function(saveVals, tableModel) {
				this.selectedChange(null, tableModel.get("$el"));
			}, this);
			
			this.initialized = true;
		}
	},
	
	/**
	 * Initializes the datatable's operation buttons (if they exist).
	 * This method is called each time the table is changed (search, page, etc)
	 * These operations include:
	 * 		Set the buttons to their default state
	 * @param $table
	 */
	drawRedrawTable : function($tableContainer) {
		$tableContainer.find('input[type="button"], button').each(function(index, value){
			OpButtonController.noneSelected($(this));
		});
	},
	
	/**
	 * Changes the state of the operation buttons.
	 * Called when a checkbox's state changes.
	 * 
	 * @param $checkbox jquery object representing the checkbox being changed
	 * @param $table (optional) jquery reference to the table (rather than checkbox)
	 */
	selectedChange : function($checkbox, $table) {
		var numSelected = 0;
		var $tableContainerRef = $();
		numSelected = (typeof $table === "undefined") ? IDT.countSelectedOptions(IDT.getTable($checkbox)) :  IDT.countSelectedOptions($table);
		$tableContainerRef = (typeof $table === "undefined") ? IDT.getTableContainer($checkbox) : IDT.getTableContainer($table);
		
		$tableContainerRef.find('input[type="submit"], input[type="button"], button').each(function(index, value){
			if (numSelected < 1) {
				// disable all buttons except those with class "enabledOnNone" or "alwaysEnabled"
				OpButtonController.noneSelected($(this));
			}
			else if (numSelected == 1) {
				// enable all buttons except the ones with class "alwaysDisabled"
				OpButtonController.oneSelected($(this));
			}
			else {
				// many are checked
				// disable all buttons except those with class "alwaysEnabled" or "enabledOnMany"
				OpButtonController.manySelected($(this));
			}
		});
	}
};