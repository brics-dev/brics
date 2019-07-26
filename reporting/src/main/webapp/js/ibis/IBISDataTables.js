/**
 * API object that starts this whole shindig and keeps track of everything.
 * Acts as bootstrap AND API document
 * 
 * JSON Data format stored in HTML:
 * <div class="dataTableContainer dataTableJSON">
 * 	<table>
 *   <script>
 *    {
 *     aaData : [
 *      ["one", "element", "per", "cell", ...],
 *      ...
 *     ],
 *     aoColumns : [
 *      {sTitle: "column header"}, 
 *      {...}, 
 *      ...
 *     ],
 *     config : {...any datatables config options...}
 *    }
 *   </script>
 *  </table>
 * </div>
 * 
 * NOTE: the config attribute overwrites ANY configuration to the datatables
 * config, including caluclated values.  Use at your own risk.  It is optional.
 * 
 * The same schema (for the JSON) must be maintained for translating DOM tables
 * into their JSON counterparts. 
 */
var IbisDataTables = IDT = {
	dataTables 			: {},	// reference to original jquery.DataTable object after initialization
	tableReferences 	: [],	// array of datatable save IDs (key into dataTables object)
	models				: null,	// DataTables collection of DataTable models
	isStarted			: false,// is the IDT initialized?
	
	/**
	 * Starts up this whole show
	 */
	initialize : function() {
		this.models = new DataTables();
		this.isStarted = true;
	},
	
	/**
	 * Creates a new table view and model and adds them to the proper
	 * collections here.
	 */
	createNewTable : function($table) {
		var id = this.getId($table);
		var model = new DataTable({
			$el : $table,
			id 	: id
		});
		
		var view = new DataTableView({
			model : model
		});
		
		this.models.add(model);
		
		return view;
	},
	
	/**
	 * Get the model which holds the data for table visualized as $table.
	 */
	getTableModel : function($table) {
		var id = this.getId($table);
		return this.models.findWhere({id: id});
	},
	
	// --- backward compatible methods for old IDT ---
	
	/**
	 * Builds a single table with JS Object config into table
	 * $table that has surrounding div with
	 * .dataTableContainer.dataTableJSON classes
	 * 
	 * @param config the JSON data to fill the table with
	 * @param $table jQuery reference to the table to fill
	 * @param view (Optional) the backbone view to use
	 * 
	 * New use is to build a single JSON table
	 */
	buildSingleData : function(config, $table, view) {
		if (!this.isStarted) {
			this.initialize();
		}
		// clean out the table element.
		$table.empty().addClass("table");

		// build the model and view
		if (typeof view === "undefined") {
			view = this.createNewTable($table);
		}
		
		if (view.model.get("aaData").length < 1) {
			view.model.set("aaData", config.aaData);
			view.model.set("aoColumns", config.aoColumns);
			view.model.set("exportRow", config.exportRow);
			if (typeof config.config !== "undefined") {
				view.model.set("config", config.config);
			}
		}
		
		
		// render and go
		view.render();
	},
	
	buildSingleDom : function($table) {
		if (!this.isStarted) {
			this.initialize();
		}
		// read table itself into JSON format.
		var view = this.createNewTable($table);
		var $headerElements = view.getHeaderCells();
		var $headerRow = $headerElements.eq(0).parent("tr");
		// remove the row itself
		$headerRow.remove();
		// get the export row if it exists
		view.getExportRow();
		// now for the other rows
		view.getBodyRows($table);
		// compile the data
		var json = {
			aaData : view.model.get("aaData"),
			aoColumns : view.model.get("aoColumns"),
			exportRow : view.model.get("exportRow")
		};
		
		// call up buildSingleData to do the rest
		this.buildSingleData(json, $table, view);
	},
	
	/**
	 * Performed a full table build and render on DOM-loaded data
	 * In this version, this will be our primary loop to process
	 * all tables on a page
	 */
	fullBuild : function($table) {
		if (!this.isStarted) {
			this.initialize();
		}
		var tableRef = $table || $(".dataTableContainer > table");
		tableRef.each(function() {
			_.defer(function($table) {
				// ensures no already-processed tables are accepted
				if (!$table.hasClass("dataTable")) {
					if ($table.find('> script[type="text/json"]').length > 0
							|| $table.parents(".dataTableContainer").hasClass("dataTableJSON")) {
						var json = JSON.parse($table.find("script").html().trim());
						IDT.buildSingleData(json, $table);
					}
					else {
						IDT.buildSingleDom($table);
					}
				}
			}, $(this));
		});
	},
	
	/**
	 * De-renders the data table and removes its model from the "models" collection. The 
	 * underlining DOM objects will still be intact, and will need to be removed separately 
	 * if desired.
	 * 
	 * @param $table - A jQuery reference to the table to destroy.
	 */
	derenderTable : function($table) {
		var model = this.getTableModel($table);
		
		if ( typeof model != "undefined" ) {
			EventBus.trigger("derender:table", model);
			this.models.remove(model);
		}
	},
	
	/**
	 * Checks if the table is already initialized
	 * 
	 * @param $table the table to check
	 * @returns {Boolean} true if already initialized; otherwise false
	 */
	isInit : function($table) {
		var model = this.getTableModel($table);
		if (typeof model !== "undefined") {
			return (this.dataTables[model.get("id")]) ? true : false;
		}
		else {
			return false;
		}
	},
	
	/**
	 * Gets the ID of a table.  Optionally sets a unique ID if one is not
	 * already set.
	 * 
	 * @param $table the table of which to get the ID
	 * @returns string ID of the table;
	 */
	getId : function($table) {
		if ($table.attr("id") != null) {
			return $table.attr("id");
		}
		
		var newId = null;
		if ($table.hasClass("dataTableContainer") && typeof $table.attr("id") !== "undefined") {
			newId = $table.attr("id");
		}
		else {
			var newId = $table.parents(".dataTableContainer").eq(0).attr("id");
			if (newId != null) {
				newId += "_table";
			}
		}
		
		if (newId == null) {
			// the local page name (ie /form/createForm) with a unique integer index of the given table in the list of all tables
			newId = window.location.pathname.split(".")[0].replace(/\//g, "_") + "_" + $(".dataTableContainer > table, .dataTableContainer > .dataTables_wrapper > table").index($table);
		}
		
		$table.attr("id", newId);
		return newId;
	},
	
	getInputForRow : function($row) {
		return $row.find("td").eq(0).find("input");
	},
	
	/**
	 * Gets all currently-visible inputs for the given table.  This will ONLY
	 * return the list of first-column inputs.
	 * 
	 * @param $table jquery reference to the table
	 */
	getVisTableRowInputs : function($table) {
		var $rows = $table.find("tbody tr");
		var $inputs = $();
		$rows.each(function() {
			$inputs = $inputs.add($(this).find('input[type="checkbox"], input[type="radio"]').eq(0));
		});
		return $inputs;
	},
	
//	/**
//	 * Gets the jquery input list for a given $table
//	 * NOTE: this gets ALL inputs for this table
//	 * 
//	 * @param $table the table to get the inputs for
//	 */
//	getAllInputs : function($table) {
//		var $inputs = $();
//		var model = this.getTableModel($table);
//		model.rows.forEach(function(row) {
//			$inputs.add(row.get("$input"));
//		});
//		return $inputs;
//	},
	
	hasRowValue : function($table, saveVal) {
		var model = this.getTableModel($table);
		var output = false;
		model.rows.forEach(function(row) {
			if (row.get("saveValue") == saveVal) {
				output = true;
				return;
			}
		});
		return output;
	},
	
	/**
	 * Encapsulates the calculation of a "selected value" for a given input
	 * 
	 * @param element jQuery reference to the element or model to calculate the save value
	 * @returns {String} the save value
	 */
	getSaveValue : function(element) {
		var $input = element;
		if (typeof element.cid !== "undefined") {
			//$input = element.get("$input");
			// get input from row data and select it
			$input = element.getInput();
		}
		var inputId = $input.attr("id");
		var inputValue = $input.attr("value");
		var saveValue = "";
		if (inputId && inputId != null) {
			saveValue = this.cleanSaveVal(inputId);
		}
		else if (inputValue && inputValue != null) {
			saveValue = this.cleanSaveVal(inputValue);
		}
		else {
			// as a last resort, get the text of the entire row
			saveValue = $input.parents("tr").eq(0).text();
		}
		return saveValue;
	},
	
	cleanSaveVal : function(saveValue) {
		if (!IDT.isSaveValClean(saveValue)) {
			saveValue = saveValue.replace(/\./g, "\\.").replace(/ /g, "\\ ");
		}
		return saveValue;
	},
	
	/**
	 * Determine if a particular saveValue is cleaned (escaped)
	 */
	isSaveValClean : function(saveValue) {
		var hasPeriod = saveValue.indexOf(".") > -1;
		var hasSpace = saveValue.indexOf(" ") > -1;
		var hasEscapedPeriod = saveValue.indexOf("\\.") > -1;
		var hasEscapedSpace = saveValue.indexOf("\\ ") > -1;
		//return (hasPeriod && !hasEscapedPeriod) || (hasSpace && !hasEscapedSpace);
		return (!hasPeriod || hasEscapedPeriod) && (!hasSpace || hasEscapedSpace);
	},
	
	setRowSelected : function($row) {
		var $input = this.getInputForRow($row);
		var saveVal = this.getSaveValue($input);
		var $table = this.getTable($row);
		if (typeof $table !== "undefined") {
			this.getTableModel($table).selectRow(saveVal);
		}
	},
	
	setRowDeselected : function($row) {
		var $input = this.getInputForRow($row);
		var saveVal = this.getSaveValue($input);
		var $table = this.getTable($row);
		if (typeof $table !== "undefined") {
			this.getTableModel($table).deselectRow(saveVal);
		}
	},
	
	hasOption : function($table, saveValue) {
		var model = this.getTableModel($table);
		return model.hasRow(saveValue);
	},
	
	/**
	 * Determines if the given checkbox is in the selected options list
	 * 
	 * @param $checkbox a jquery checkbox in a datatable whose id to add
	 * @param saveValue (optional) the save value of the element
	 * @param $table (optional) the table the value lives in
	 */
	isSelectedOption : function($checkbox, saveValue, $table) {
		if (typeof $table === "undefined") {
			$table = this.getTable($checkbox);
		}
		if (typeof saveValue === "undefined") {
			saveValue = this.getSaveValue($checkbox);
		}
		var model = this.getTableModel($table);
		return model.isSelectedSaveVal(saveValue);
	},
	
	/**
	 * Add an ID to the list of selected options for use later
	 * 
	 * @param $checkbox a jquery checkbox in a datatable whose id to add
	 * @param saveValue (optional) the save value of the element
	 * @param $table (optional) the table the value lives in
	 */
	addSelectedOption : function($checkbox, saveValue, $table) {
		if (typeof $table === "undefined") {
			$table = this.getTable($checkbox);
		}
		if (typeof saveValue === "undefined") {
			saveValue = this.getSaveValue($checkbox);
		}
		var model = this.getTableModel($table);
		model.selectRow(saveValue);
	},
	
	/**
	 * Adds the specified value to the list of selected options.  This ONLY
	 * takes a string value, NOT a jquery element.  This allows for loading
	 * currently-invisible values to the selected options array.
	 * 
	 * Priority:
	 * 	1. input ID
	 * 	2. input value
	 * 	3. (not preferred) the text of the entire row (reasonably unique)
	 * 
	 * @param $table the table to add the value to
	 * @param value the value to store.  No checking is done on this!
	 */
	addSelectedOptionValue : function($table, value) {
		// this is effectively just the same thing as addSelectedOption - just
		// an overriding of that method
		var model = this.getTableModel($table);
		model.selectRow(value);
	},
	
	/**
	 * Removes an ID from the list of selected options
	 * 
	 * @param $checkbox a jquery checkbox in a datatable whose id to remove
	 * @param saveValue (optional) the save value of the element
	 * @param $table (optional) the element's table
	 */
	removeSelectedOption : function($checkbox, saveValue, $table) {
		if (typeof $table === "undefined") {
			$table = this.getTable($checkbox);
		}
		if (typeof saveValue === "undefined") {
			saveValue = this.getSaveValue($checkbox);
		}
		var model = this.getTableModel($table);
		model.deselectRow(saveValue);
	},
	
	/**
	 * Removes the specified value to the list of selected options.  This ONLY
	 * takes a string value, NOT a jquery element.  This allows for removing
	 * currently-invisible values from the selected options array.
	 * 
	 * Priority:
	 * 	1. input ID
	 * 	2. input value
	 * 	3. (not preferred) the text of the entire row (reasonably unique)
	 * 
	 * @param $table the table to remove the value from
	 * @param value the value to remove.  No checking is done on this!
	 */
	removeSelectedOptionValue : function($table, value) {
		var model = this.getTableModel($table);
		model.deselectRow(value);
	},
	
	/**
	 * Clears all selected options from the table
	 * 
	 * @param $table the table to act upon
	 */
	clearSelected : function($table) {
		var model = this.getTableModel($table);
		model.deselectAll();
	},
	
	/**
	 * Adds one or many rows to the table
	 * 
	 * row can be either a single row's data (as defined in the data format
	 * at the top of this document), an array of row data, a single Row object,
	 * or an array of Row objects. 
	 * 
	 * @param $table jquery reference to the table
	 * @param row as defined above 
	 */
	addRow : function($table, row) {
		var model = this.getTableModel($table);
		if (row != null) {
			if (_.isArray(row)) {
				if (row[0] instanceof Row || _.isArray(row[0])) {
					for (var i = 0; i < row.length; i++) {
						model.addNewRow(row[i]);
					}
				}
				else {
					model.addNewRow(row);
				}
	 		}
			else {
				model.addNewRow(row);
			}
		}
	},
	
	/**
	 * Removes one or many rows from the table
	 * 
	 * @row either a single saveValue or an array of saveValues
	 */
	removeRow : function($table, row) {
		var model = this.getTableModel($table);
		if (row != null && _.isArray(row)) {
			for (var i = 0; i < row.length; i++) {
				model.removeRow(row[i]);
			}
		}
		else {
			model.removeRow(row);
		}
	},
	
	/**
	 * Removes all rows from the table
	 * 
	 * @table jQuery reference to the table
	 */
	removeAllRows : function($table) {
		var model = this.getTableModel($table);
		model.removeAllRows();
	},
	
	/**
	 * Obtain the table element that contains the given element
	 * 
	 * @param $element the element whose datatable parent to obtain
	 * @returns the (.dataTableContainer > table) element parent of the given element
	 */
	getTable : function($element) {
		var $parent = $element;
		if (!$element.hasClass("dataTableContainer")) {
			$parent = $element.parents(".dataTableContainer").eq(0);
		}
		var element = $parent.find("table").eq(0);
		return (typeof element == "undefined") ? null : $(element);
	},
	
	/**
	 * Obtain the .dataTableContainer element that contains the given element
	 * 
	 * @param $element the element whose datatable parent to obtain
	 * @returns the .dataTableContainer element parent of the given element
	 */
	getTableContainer : function($element) {
		var $parent = $element.parents(".dataTableContainer").eq(0);
		return (typeof $parent == "undefined") ? null : $parent;
	},
	
	/**
	 * Gets the selectedOptions array from the given table
	 * 
	 * @param $table the table whose selectedOptions we need to grab
	 * @returns the selectedOptions array
	 */
	getSelectedOptions : function($table) {
		var model = this.getTableModel($table);
		return model.getSelectedSaveVals();
	},
	
	/**
	 * Gets the selected checkboxes as a jquery object (array)
	 * NOTE: this will only give VISIBLE elements
	 * 
	 * @param $table the table whose selectedOptions we need to grab
	 */
	getSelectedOptionsJq : function($table) {
		var selectedOptions = this.getSelectedOptions($table);
		var $output = $(); // empty jquery object
		
		// if there are no selected options, don't bother
		if (selectedOptions.length > 0) {
			// NOTE: this checkbox could list is ALL inputs in the table, not just the first column
			var $checkboxes = IDT.getVisTableRowInputs($table);
			
			// loop over checkboxes (inputs) possible in the table (unlikely to be over 15)
			// by looping over inputs, we are more likely to have a smaller loop size
			for (var i = 0; i < $checkboxes.length; i++) {
				var $checkbox = $checkboxes.eq(i);
				var saveVal = IDT.getSaveValue($checkbox);
				if (selectedOptions.indexOf(saveVal) !== -1) {
					$output = $output.add($checkbox);
				}
			}
		}
		
		return $output;
	},
	
	/**
	 * Convenience method to set the selectedOptions array for a given table
	 * 
	 * @param $table the table whose selectedOptions we neet to set
	 * @param selectedOptions the selectedOptions options to set
	 */
	setSelectedOptions : function($table, selectedOptions) {
		var model = this.getTableModel($table);
		model.deselectAll();
		model.selectSet(selectedOptions);
	},
	
	/**
	 * Returns the number of selected checkboxes throughout the table
	 * 
	 * @param $table the table to check
	 * @returns integer number of selected options
	 */
	countSelectedOptions : function($table) {
		var model = this.getTableModel($table);
		return model.selectedRows.length;
	},
	
	/**
	 * Disables the entire ability to select ANY row in the table.
	 * 
	 * @param $table the table to apply the changes to
	 */
	disableSelection : function($table) {
		var model = this.getTableModel($table);
		model.set("selectionDisable", true);
		$table.find("input").prop("disabled", true);
	},
	
	/**
	 * Enables the entire ability to select ANY row in the table.
	 * 
	 * @param $table the table to apply the changes to
	 */
	enableSelection : function($table) {
		var model = this.getTableModel($table);
		model.set("selectionDisable", false);
		$table.find("input").prop("disabled", false);
	},
	
	/**
	 * Disables selection of a single row.  Row will render as disabled.
	 * 
	 * @param $table jquery reference to the table
	 * @param reference either a saveValue or a jquery reference to the row
	 */
	disableSingleRow : function($table, reference) {
		var model = this.getTableModel($table);
		var saveVal = "";
		
		if (reference instanceof jQuery) {
			saveVal = IDT.getSaveValue(IDT.getInputForRow($row));
		}
		else {
			saveVal = reference;
		}
		
		model.disableSingleRow(saveVal);
	},
	
	/**
	 * Disables selection of a single row.  Row will render normally.
	 * 
	 * @param $table jquery reference to the table
	 * @param reference either a saveValue or a jquery reference to the row
	 */
	enableSingleRow : function($table, reference) {
		var model = this.getTableModel($table);
		var saveVal = "";
		
		if (reference instanceof jQuery) {
			saveVal = IDT.getSaveValue(IDT.getInputForRow($row));
		}
		else {
			saveVal = reference;
		}
		
		model.enableSingleRow(saveVal);
	},
	
	/**
	 * Disables the select all checkbox
	 * @param $table the table to apply the changes to
	 */
	disableSelectAll : function($table) {
		var model = this.getTableModel($table);
		model.set("disableSelectAll", true);
	},
	
	/**
	 * Enables the select all checkbox
	 * @param $table the table to apply the changes to
	 */
	enableSelectAll : function($table) {
		var model = this.getTableModel($table);
		model.set("disableSelectAll", false);
	},
	
	filterTable : function($table, filterText) {
		var model = this.getTableModel($table);
		EventBus.trigger("filter:table", model, filterText);
	},
	
	/**
	 * Saves the search and pagination details to a cookie for propagation
	 * across page refresh.
	 * 
	 * @param oSettings settings data from datatables
	 * @param oData table state data from datatables
	 */
	saveTableState : function(oSettings, oData) {
		// not actually used
	},
	
	/**
	 * Loads the search and pagination details from a cookie for propagaition
	 * across page refresh.
	 * 
	 * @param oSettings settings data from datatables
	 * @returns the complete state data to be loaded into the table
	 */
	loadTableState : function(oSettings) {
		// not actually used
	},
	
	/**
	 * Handles changing a checkbox selection after a click.  Also allows for
	 * programmatic selection change.
	 * 
	 * @param $checkbox the checkbox to change
	 * @param event (optional) the click or other event
	 * @param debounce (optional) whether to disable the checkbox for a short time
	 */
	changeCheckboxSelection : function($checkbox, debounce, event) {
		if (typeof debounce === "undefined") {
			debounce = true;
		}
		
		// stop the click from going up to the row
		if (typeof event !== "undefined") {
			event.stopPropagation();
		}
		// there is not a good way to capture clicks within a very short amount of time
		// some browsers capture a single click then doubleclick, some do two clicks then doubleclick
		// so, disable the clicked checkbox for 1/4 second to disable double clicks
		
		var saveVal = IDT.getSaveValue($checkbox);
		$checkbox.prop("disabled", true);
		setTimeout(function() {$checkbox.prop('disabled', false);}, 250);
		this.model.toggleRow(saveVal);
	},
	
	/**
	 * Responds to the table becoming visible.  This resizes the table's
	 * header and footer to be the same width as the table itself.
	 * 
	 * @param $tableRef jquery reference to the <table> element
	 */
	recalculateHeaderFooterWidths : function($tableRef) {
		$tableRef.each(function() {
			var $tableContainer = $(this).parents(".dataTableContainer");
			var paddingCorrection = 10;
			if ((BrowserDetect.browser == "Explorer")) {
				paddingCorrection += 2;
			}
			
			$tableContainer.find(".fg-toolbar").width($(this).width() - paddingCorrection);
		});
	},
	
	/**
	 * Gets the specified column's content in the table for the selected
	 * (checked) row(s).
	 * 
	 * @param $table jquery reference to the table (@see getTable())
	 * @param headerText the exact th header title from the table
	 * @param html get HTML in the cell or just text?
	 * @return an array of column content (string[]).
	 */
	getCellContent : function($table, headerText, html) {
		var model = this.getTableModel($table);
		return model.cellData(headerText, html);
	},
	
	/**
	 * Gets the number of rows in the table.
	 * 
	 * @param $table jquery reference to the table (@see getTable())
	 */
	getCountRows : function($table) {
		var model = this.getTableModel($table);
		return model.rows.length;
	}
};