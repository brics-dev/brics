/**
 * IbisDataTables class to allow IBIS related features within the IBIS website.
 * 
 * Allows for modifying existing page tables to match the look and operation
 * of the DataTables plugin and the IBIS/ProFoRMS website.
 * 
 * This class allows for full datatables operation but also includes the
 * following abilities:
 * 	* 	Maintains the list of checked checkboxes locally to ensure that the
 * 		correct checkboxes are selected across page changes.
 * 	*	Encapsulates all datatable operations into one class for easier debug
 * 	*	Allows the programmer to retrieve data about the structure of the 
 * 		IbisDataTable itself.
 * 	*	Enables translating standard struts tables into full IBIS Data Tables
 * 	*	Automates deciding whether the first column of a table is sortable or
 * 		not.
 * 
 * @author jpark1
 */
var IbisDataTables = {
	settingsTag : "ibisDataTable",
	dataTables : {},
	tableReferences : [],
	
	/**
	 * The default settings for all data tables.  Will be stored using
	 * $.data()
	 */
	Defaults : function() {
		this.selectedOptions = new Array();
		this.emptyTable = false;
		this.optionInputs = $();
		this.expandedDataRows = new HashTable();
	},
	
	buildSingleData : function(config, $table) {
		var tableRef = (typeof $table === "undefined" || $table == null) ? $(".dataTableContainer.dataTableJSON > table") : $table;
		
		IDT.initSettings($table);
		IDT.setEmpty(tableRef);
		this.setupInputs(tableRef);
		this.setupSelectAll(tableRef);
		this.initExpandedData(tableRef);
		this.initCellSearch(tableRef, config);
		
		this.init(tableRef.parents(".dataTableContainer.dataTableJSON"), config);
		
		this.initClickableRows(tableRef);
		this.fixEmptyTableRow(tableRef);
		
		this.setStyles(tableRef);
		
		if (typeof dataTableBuildCallback == "function") {
			dataTableBuildCallback(tableRef);
		}
	},
	
	/**
	 * Performs a full build of $table table OR, if $table is 
	 * undefined or null, all tables on the page.
	 * 
	 * @param $table a jquery datatable to build
	 */
	fullBuild : function($table) {
		var tableRef = (typeof $table == "undefined" || $table == null) ? $(".dataTableContainer:not(.dataTableJSON) > table") : $table;
		this.translateStrutsTable(tableRef);
		this.removeEmptyTableRow(tableRef);
		this.setupInputs(tableRef);
		this.setupSelectAll(tableRef);
		this.initExpandedData(tableRef);
		this.initCellSearch(tableRef);
		
		this.init(tableRef.parents(".dataTableContainer"));
		
		this.initClickableRows(tableRef);
		this.fixEmptyTableRow(tableRef);
		
		
		if (typeof dataTableBuildCallback == "function") {
			dataTableBuildCallback(tableRef);
		}
	},
	
	/**
	 * Performs the actual datatable initialization call on $container
	 * or, if $container is null, all datatable containers on the page
	 * 
	 * @param $container (optional) a container to initialize as an ibisDataTable
	 */
	init : function($container, config) {
		try {
			var tableList = new Array();
			if (typeof $container == "undefined" || $container == null) {
				tableList = $(".dataTableContainer:not(.dataTableJSON) > table");
			}
			else {
				tableList = $container.children("table");
			}
			
			tableList.each(function() {
				var configExtend = (typeof config === "undefined") ? {}  : config;
				var finalConfig = $.extend({},{
					"bJQueryUI": true,
					//"bDeferRender" : true,
					"bLengthChange": false,
					"iDisplayLength": 15,
					"bProcessing": true,
					"bAutoWidth": false,
					"sPaginationType" : "full_numbers",
					// disables initial sorting
					"aaSorting" : [],
					// defines the language
					"oLanguage" : IbisDataTables.determineLanguage(),
					// outlines whether to sort on the first column or not
					"aoColumnDefs": [
			             { "bSortable": false, 
			            	 "aTargets": IbisDataTables.tableNoSort($(this)), 
			            	 "bSearchable": false
			             },
			             {"sDefaultContent": "none", "aTargets": ["_all"]}
					],
					"aoColumns": IbisDataTables.columnVis($(this)),
					"fnInitComplete" : function(oSettings, json) {
						// moves the process buttons to the top of the data table
						OpButtonController.initTable($(this));
						// if this table has an export row, move it back into place and style it
						IbisDataTables.InfoRowManager.init($(this));
						
						// propagate this method to any further callback
						if (typeof fnInitComplete == "function") {
							fnInitComplete(oSettings, json);
						}
					},
					"fnRowCallback" : function(nRow, aData, iDisplayIndex, iDispalyIndexFull) {
						var $row = $(nRow);
						var $input = $row.find('input[type="checkbox"], input[type="radio"]').eq(0);
						if ($input.length > 0) {
							IDT.setRowDeselected($row);
							if ($input.is(":visible") && IDT.isSelectedOption($input)) {
								IDT.setRowSelected($row);
							}
						}
						
						// pass any other call on through
						if (typeof fnRowCallback == "function") {
							fnRowCallback(nRow, aData, iDisplayIndex, iDisplayIndexFull);
						}
					},
					"fnPreDrawCallback" : function(settings) {
						var tableRef = $(this).parents(".dataTableContainer");
						$table = tableRef.find("table").eq(0);
						$table.find("input").unbind("click");
						$table.find("tbody tr").unbind("click");
					},
					"fnDrawCallback" : function(settings) {
						var tableRef = $(this).parents(".dataTableContainer");

						$table = tableRef.find("table").eq(0);
						if ($table.is(":visible")) {
							IbisDataTables.recalculateHeaderFooterWidths($table);
							IbisDataTables.TableLayoutListener.exclude($table);
						}
						else {
							// add the table to the table layout listener with the recalculate callback
							IbisDataTables.TableLayoutListener.addTable($table, {
								callback: function($table) {
									IDT.recalculateHeaderFooterWidths($table);
								}
							});
						}
						
						IbisDataTables.drawRedrawInitCheckboxes(tableRef);
						IbisDataTables.initClickableRows($(this));
						IbisDataTables.setupExpandedData($(this), settings.oInstance);
						
						// propagate this method to any further callback
						if (typeof fnDrawCallback == "function") {
							fnDrawCallback(tableRef);
						}
					},
					fnCreatedRow : function(nRow, aData, iDataIndex) {
						// propagate this method to any further callback
						if (typeof fnCreatedRow == "function") {
							fnCreatedRow(nRow, aData, iDataIndex);
						}
					}
				}, configExtend);
				var tempTable = $(this).dataTable(finalConfig);
				
				// if the container div has an ID, create a mapping between the datatable and the container ID so we can retrieve it later
				// $(this) is potentially a table, not a container
				var saveId = IbisDataTables.getId($(this));
				IbisDataTables.dataTables[saveId] = tempTable;
				IbisDataTables.tableReferences.push(saveId);
				
				$(this).trigger("tableInit");
			});
		}
		catch (e) {
			alert("One of your data tables could not be initialized correctly");
		}
	},
	
	initCellSearch : function($table, config) {
		$table.each(function() {
			var $tableEach = $(this);
			var cellSearch = {
					tableHeaders : [],
					rowMap : []
				};
				if (typeof config !== "undefined") {
					// the data comes from a config object, not HTML
					for (var i = 0; i < config.aoColumns.length; i++) {
						cellSearch.tableHeaders.push(config.aoColumns[i].sTitle);
					}
					for (var i = 0; i < config.aaData.length; i++) {
						var inputElement = config.aaData[i][0];
						var saveVal = IDT.getSaveValue($(inputElement));
						cellSearch.rowMap[i] = saveVal;
					}
				}
				else {
					// data comes from HTML
					cellSearch.tableHeaders = IDT.getTableHeaders($tableEach);
					$tableEach.find(".selectionInput").each(function(index) {
						var saveVal = IDT.getSaveValue($(this));
						cellSearch.rowMap[index] = String(saveVal);
					});
				}
				IDT.setSettingsOption($tableEach, "cellSearch", cellSearch);
		});
	},
	
	/**
	 * Translates a struts table into a properly-formatted pre-datatable table.
	 * Adds thead and tbody.
	 * 
	 * If the optional parameter is not set or is set to null, all tables on the page
	 * will be converted.
	 * 
	 * Translate a struts table into a sane, structured HTML table
	 * by cutting the <tr><th>...</th></tr> from <tbody> and inserting it into
	 * a <thead>.
	 * 
	 * @param $table (optional) the table to convert
	 */
	translateStrutsTable : function($table) {
		var tableRef = (typeof $table == "undefined" || $table == null) ? $("table") : $table;
		
		tableRef.each(function(index, value) {
			// if this table has an export row, cut it out and put it right before the table
			IbisDataTables.InfoRowManager.removeExportRow($(this));
			
			// remove "valign" from the tds
			$(this).find('[valign]').removeAttr("valign");
			
			// make sure the table doesn't already have a thead
			if ($(this).children("thead").length < 1) {
				// check that the first child of the $(table tbody) contains th elements
				var thref = $(this).find("th");
				// check to make sure the immediate parent table of the th is THIS table.  It could be nested
				if (thref.length > 0) {
					var parentsRef = $(thref[0]).parents("table");
					if (parentsRef.length > 0 && $(parentsRef[0]).is($(this))) {
						var rowRef = $(thref[0]).parent();// this gets the row itself
						if (rowRef.length > 0) {
							$(this).prepend("<thead></thead>");
							$(this).children("thead").append(rowRef);
						}
					}
				}
			}
		});
	},
	
	/**
	 * Initialize the dataTable's IBIS settings
	 * 
	 * @param $table the table to initialize
	 */
	initSettings : function($table) {
		if (!this.isInit($table)) {
			this.setSettings($table, new IbisDataTables.Defaults);
		}
	},
	
	/**
	 * Checks if the table is already initialized
	 * 
	 * @param $table the table to check
	 * @returns {Boolean} true if already initialized; otherwise false
	 */
	isInit : function($table) {
		return (typeof this.getSettings($table) != "undefined");
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
		
		var newId = $table.parents(".dataTableContainer").eq(0).id;
		if (newId != null) {
			newId += "_table";
		}
		else {
			// the local page name (ie /form/createForm) with a unique integer index of the given table in the list of all tables
			newId = window.location.pathname + "_" + $(".dataTableContainer > table").index($table);
		}
		$table.attr("id", newId);
		return newId;
	},
	
	setStyles : function($table) {
		var $th = $table.find("th");
		$th.attr("role", "columnheader");
		$th.parents("tr").eq(0).addClass("tableRowHeader");
	},
	
	/**
	 * Makes a copy of all of the current inputs before the table is paginated.
	 * This ensures that we get a copy of EVERY input.
	 * 
	 * @param $table a jquery table or tables to process
	 */
	setupInputs : function($table) {
		var tableRef = (typeof $table == "undefined" || $table == null) ? $(".dataTableContainer > table") : $table;
		
		tableRef.each(function(index,value) {
			// change this to ensure that we don't grab inputs other than the selection one
			//var $inputs = $(this).find('input[type="checkbox"], input[type="radio"]');
			var $rows = $(this).find("tbody tr td:first-child");
			var $inputs = $();
			$rows.each(function() {
				$inputs = $inputs.add($(this).find('input[type="checkbox"], input[type="radio"]').eq(0));
			});
			IbisDataTables.setSettingsOption($(this), "optionInputs", $inputs);
			$inputs.each(function() {
				$(this).parent("td").css("white-space", "nowrap");
				$(this).addClass("selectionInput");
			});
		});
	},
	
	setupSelectAll : function($table) {
		var $tableRef = (typeof $table == "undefined" || $table == null) ? $(".dataTableContainer > table") : $table;
		$tableRef.each(function(index, value) {
			$checkboxes = $(this).find('input[type="checkbox"]');
			if ($checkboxes.length > 0) {
				// draws the "select all" checkbox
				$(this).find('th').eq(0).html('<input type="checkbox" class="dataTableSelectAll" />');
				$(this).find(".dataTableSelectAll").change(function() {
					var $workingTable = $(this).parents("table").eq(0);
					var $allInputs = IbisDataTables.getAllInputs($workingTable);
					if ($(this).is(":checked")) {
						$allInputs.each(function() {
							IbisDataTables.addSelectedOptionValue($workingTable, IDT.getSaveValue($(this)));
						});
					}
					else {
						$allInputs.each(function() {
							IbisDataTables.removeSelectedOptionValue($workingTable, IDT.getSaveValue($(this)));
						});
					}
					IbisDataTables.InfoRowManager.updateSelectedItemCount($workingTable, IDT.countSelectedOptions($workingTable));
					IbisDataTables.dataTables[$workingTable.attr("id")].fnDraw();
					if (typeof selectAllCallback !== "undefined") {
						selectAllCallback($workingTable);
					}
				});
			}
		});
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
	
	/**
	 * Gets the jquery input list for a given $table
	 * NOTE: this gets ALL inputs for this table
	 * 
	 * @param $table the table to get the inputs for
	 */
	getAllInputs : function($table) {
		return this.getSettingsOption($table, "optionInputs");
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
		var selectedOptions = this.getSelectedOptions($table);
		if (selectedOptions.indexOf(saveValue) < 0) {
			return false;
		}
		else {
			return true;
		}
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
		
		// what value will we enter?  That depends on the input we see
		if (typeof saveValue === "undefined") {
			saveValue = this.getSaveValue($checkbox);
		}
		
		this.addSelectedOptionValue($table, saveValue);
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
		if (value.indexOf(".") > -1 && value.indexOf("\\.") < 0) {
			value = value.replace(".", "\\.");
		}
		if (value.indexOf(" ") > -1 && value.indexOf("\\ ") < 0) {
			value = value.replace(" ", "\\ ");
		}
		var selectedOptions = this.getSelectedOptions($table);
		// checks that the value doesn't already exist
		if (selectedOptions.indexOf(value) < 0) {
			selectedOptions.push(value);
			this.setSettingsOption($table, "selectedOptions", selectedOptions);
		}
		
		IbisDataTables.InfoRowManager.updateSelectedItemCount($table, IDT.countSelectedOptions($table));
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
		var selectedOptions = this.getSelectedOptions($table);
		
		selectedOptions.splice(selectedOptions.indexOf(saveValue), 1);
		this.setSelectedOptions($table, selectedOptions);
		
		IbisDataTables.InfoRowManager.updateSelectedItemCount($table, IDT.countSelectedOptions($table));
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
		var selectedOptions = this.getSelectedOptions($table);
		selectedOptions.splice(selectedOptions.indexOf(value), 1);
		this.setSelectedOptions($table, selectedOptions);
	},
	
	/**
	 * Clears all selected options from the table
	 * 
	 * @param $table the table to act upon
	 */
	clearSelected : function($table) {
		this.setSelectedOptions($table, new Array());
		this.getVisTableRowInputs($table).prop('checked', false);
	},
	
	clearOtherSelected : function($input) {
		this.setSelectedOptions(IbisDataTables.getTable($input), new Array());
		this.getVisTableRowInputs($table).not($input).prop('checked', false);
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
		return this.getSettings($table).selectedOptions;
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
	 * Encapsulates the comparison method between a "selected value" stored
	 * value and a potential matching input.
	 * 
	 * @param $input jQuery reference to the element to check
	 * @param checkValue the string id, value, or row text to check
	 */
	checkSaveValue : function($input, checkValue) {
		return this.getSaveValue($input) == checkValue;
	},
	
	/**
	 * Encapsulates the calculation of a "selected value" for a given input
	 * 
	 * @param $input jQuery reference to the element to calculate the save value
	 * @returns {String} the save value
	 */
	getSaveValue : function($input) {
		var inputId = $input.attr("id");
		var inputValue = $input.attr("value");
		var saveValue = "";
		if (inputId != null) {
			saveValue = inputId.replace(".", "\\.").replace(" ", "\\ ");
		}
		else if (inputValue != null) {
			saveValue = inputValue.replace(".", "\\.").replace(" ", "\\ ");
		}
		else {
			// as a last resort, get the text of the entire row
			saveValue = $input.parents("tr").eq(0).text();
		}
		return saveValue;
	},
	
	/**
	 * Gets a jQuery input element based on its save value.  This ONLY gets
	 * elements currently on the page.
	 * 
	 * @param $table the table parent of the inputs
	 * @param saveValue string save value
	 */
	getJqBySaveValue : function($table, saveValue) {
		// check ID first
		var $check = $table.find("#" + saveValue);
		if ($check.length > 0) return $check;
		
		// then value
		$check = $table.find('input[value="'+saveValue+'"]');
		if ($check.length > 0) return $check;
		
		// then row text(s)
		$table.find("tr").each(function() {
			$check = $(this);
			if ($check.text() == saveValue) return;
		});
		
		return $check;
	},
	
	/**
	 * Convenience method to set the selectedOptions array for a given table
	 * 
	 * @param $table the table whose selectedOptions we neet to set
	 * @param selectedOptions the selectedOptions options to set
	 */
	setSelectedOptions : function($table, selectedOptions) {
		this.setSettingsOption($table, "selectedOptions", selectedOptions);
	},
	
	/**
	 * Returns the number of selected checkboxes throughout the table
	 * 
	 * @param $table the table to check
	 * @returns integer number of selected options
	 */
	countSelectedOptions : function($table) {
		return this.getSelectedOptions($table).length;
	},
	
	/**
	 * Get the settings object from this element's data().  if the settings
	 * are not defined, this method sets them to the defaults then returns those.
	 * 
	 * @param $table the object to obtain settings from
	 * @returns the settings object
	 */
	getSettings : function($table) {
		return settings = $table.data(IbisDataTables.settingsTag);
	},
	
	/**
	 * Sets a new settings object to the ibisDataTable element of the 
	 * element's data() object.  This allows for generic saving of new
	 * settings elements.
	 * 
	 * @param $table the element to save the settings to
	 * @param newSettings the new settings to store
	 */
	setSettings : function($table, newSettings) {
		$table.data(IbisDataTables.settingsTag, newSettings);
	},
	
	/**
	 * Convenience method for the equivalent of 
	 * this.setSettingsOption($table, "emptyTable", true);
	 * 
	 * Allows the programmer to easily set the identified table's 
	 * "empty table" flag.
	 * 
	 * This means that the table has no data rows.
	 * 
	 * @param $table the table which needs to be set to empty
	 */
	setEmpty : function($table) {
		this.setSettingsOption($table, "emptyTable", true);
	},
	
	/**
	 * Convenience method for the equivalent of 
	 * this.getSettingsOption($table, "emptyTable")
	 * 
	 * This obtains the flag marking this table as empty or not.
	 * The table being empty means that there is no data in the table.
	 * 
	 * @param $table the table to check
	 * @returns boolean true if empty, otherwise false
	 */
	isEmpty : function($table) {
		return this.getSettingsOption($table, "emptyTable");
	},
	
	/**
	 * Sets a particular settings option to the given value
	 * 
	 * @param $table the table to set the settings value to
	 * @param settingsKey the settings key to set
	 * @param settingsValue the value to set to the settings key
	 */
	setSettingsOption : function($table, settingsKey, settingsValue) {
		var settings = this.getSettings($table);
		settings[settingsKey] = settingsValue;
		this.setSettings($table, settings);
	},
	
	/**
	 * Gets a particular settings option for the specified element
	 * 
	 * @param $table the table to get the settings option for
	 * @param settingsKey the settings key to get
	 * @returns
	 */
	getSettingsOption : function($table, settingsKey) {
		var settings = this.getSettings($table);
		return settings[settingsKey];
	},
	
	/**
	 * Determines if the table needs to allow sorting on column 1 (index 0) or not
	 * 
	 * @param $table the table to check
	 * @returns {Array} either an empty array if sorting is allowed or an array with [0]=0 for sorting not allowed
	 */
	tableNoSort : function($table) {
		var tableEmpty = IbisDataTables.isEmpty($table);
		if (tableEmpty || $table.find('input[type="checkbox"], input[type="radio"]').length > 0) {
			var ret = new Array();
			ret[0] = 0;
			return ret;
		}
		return new Array();
	},
	
	/**
	 * Disables the entire ability to select ANY row in the table.
	 * 
	 * @param $table the table to apply the changes to
	 */
	disableSelection : function($table) {
		var $tableContainer = $table.parents(".dataTableContainer");
		IDT.disableSelectAll($table);
		IDT.setSettingsOption($table, "disableSelection", true);
		// redraw
		IDT.dataTables[$table.attr("id")].fnDraw();
	},
	
	/**
	 * Enables the entire ability to select ANY row in the table.
	 * 
	 * @param $table the table to apply the changes to
	 */
	enableSelection : function($table) {
		var $tableContainer = $table.parents(".dataTableContainer");
		IDT.enableSelectAll($table);
		IDT.setSettingsOption($table, "disableSelection", false);
		// redraw
		IDT.dataTables[$table.attr("id")].fnDraw();
	},
	
	/**
	 * Disables the select all checkbox
	 * @param $table the table to apply the changes to
	 */
	disableSelectAll : function($table) {
		$table.find(".dataTableSelectAll").prop("disabled", true);
	},
	
	/**
	 * Enables the select all checkbox
	 * @param $table the table to apply the changes to
	 */
	enableSelectAll : function($table) {
		$table.find(".dataTableSelectAll").prop("disabled", false);
	},
	
	/**
	 * Determines if the table needs to hide any of the columns.  This builds the
	 * object to insert into the aoColumns array in the datatables init method.
	 * 
	 * Specifically checks to see if the table contains a td with the class ibisDatatable_expandedData
	 * 
	 * @param $table the table to check
	 */
	columnVis : function($table) {
		var columnCount = $table.find("tbody tr").eq(0).children("td").length;
		var output = [];
		for (var i = 0; i < columnCount; i++) {
			output[i] = null;
		}
		
		// if the table HAS extra data, set that extra data column to hidden
		var columnIndex = $table.find("tbody tr").eq(0).children("td").index($(".ibisDatatable_expandedData"));
		if (columnIndex > -1) {
			output[columnIndex] = {"bVisible": false};
		}
		
		if (output.length == 0) {
			return null;
		}
		return output;
	},
	
	/**
	 * If the table is empty (marked with an empty table message), this method removes the 
	 * empty table message to be replaced with a nicer dataTable one
	 *  
	 * @param $tableListRef a jquery object with an array of datatables to check
	 */
	removeEmptyTableRow : function($table) {
		var tableRef = (typeof $table == "undefined" || $table == null) ? $(".dataTableContainer > table") : $table;
	
		tableRef.each(function(index, value) {
			IbisDataTables.initSettings($(this));
			
			var thCount = $(this).find("th").length;
			// correct for non-datatable tables (bad!!!)
			if (thCount > 0) {
				var thCountInc = thCount+1;
				// matches any td in the table that has a colspan = all columns
				var matchingTdRef = $(this).find('td[colspan="'+thCount+'"], td[colspan="'+thCountInc+'"]');
				if (matchingTdRef.length > 0) {
					// get the parent row and remove it
					matchingTdRef.parent().remove();
					IbisDataTables.setEmpty($(this));
				}
			}
		});
	},
	
	/**
	 * Fixes the messed up empty table row.  Not sure what caused it but this
	 * fixes it.
	 * 
	 * Details: the empty table row drawn by datatable is getting a colspan of 1
	 * 
	 * @param $table jquery reference to the table to change
	 */
	fixEmptyTableRow : function($table) {
		$table.each(function() {
			if (IbisDataTables.isEmpty($(this))) {
				var thCount = $(this).find("th").length;
				var emptyCell = $(this).find(".dataTables_empty");
				if (thCount > 0 && emptyCell.length > 0) {
					emptyCell.attr("colspan", thCount);
				}
			}
		});
	},
	
	/**
	 * Sets the expanded data for a single row.  Adds the row data to the
	 * table's settings.
	 * @param $row jquery reference to the row to add the data to
	 * @param data String data to add
	 */
	setExpandedData : function($row, data) {
		var $table = this.getTable($row);
		var expandedDataRows = this.getSettingsOption($table, "expandedDataRows");
		expandedDataRows.setItem(this.getRowId($row), data);
		this.setSettingsOption($table, "expandedDataRows", expandedDataRows);
	},
	
	/**
	 * Gets the exanded data for the table
	 */
	getExpandedData : function($table) {
		return IbisDataTables.getSettingsOption($table, "expandedDataRows");
	},
	
	/**
	 * Tries to get a reasonably unique identifier for the given row.  If the row
	 * has an input (checkbox, radio button) in the first column and that input
	 * has an ID, the ID will be returned.  If it does not have an ID, the value
	 * will be returned.  If there is no input in the first column, the text of
	 * the first column will be returned.
	 * 
	 * NOTE: this is NOT the same as getSaveValue
	 * 
	 * @param $row jquery reference to the row
	 * @returns string identifier for the row
	 */
	getRowId : function($row) {
		var $input = $row.find("td").eq(0).find("input");
		if ($input.length > 0) {
			if ($input.attr("id") != null) {
				return $input.attr("id");
			}
			else {
				return $input.attr("value");
			}
		}
		else {
			return $row.find("td").eq(0).text();
		}
	},
	
	/**
	 * Initialize the source data for the "expanded data" dropdown.
	 * @param $table jquery reference to the table or tables to initialze
	 */
	initExpandedData : function($table) {
		$table.each(function() {			
			// look for expandeddata within this table
			var columnIndex = $(this).find("tbody tr").eq(0).children("td").index($(".ibisDatatable_expandedData"));
			if (columnIndex > -1) {
				// found expanded data
				$(this).find("tbody tr").each(function() {
					var rowData = $(this).find("td").eq(columnIndex).html();
					IbisDataTables.setExpandedData($(this), rowData);
				});
			}
		});
	},
	
	/**
	 * Initializes the "expanded data" dropdown.  Removes that column from 
	 * the table and uses each row's expanded data cell as the content for
	 * the expanded data dropdown.
	 * 
	 * @param $table the table to act upon (can be multiple)
	 */
	setupExpandedData : function($table, instance) {
		$table.each(function() {
			// if extra data exists for this table, set up the clicks
			var expandedData = IbisDataTables.getExpandedData($(this));
			if (!expandedData.isEmpty()) {
				$(this).find("tbody tr").unbind("select").on("select", function() {
					if (instance.fnIsOpen(this)) {
						instance.fnClose(this);
					}
					else {
						var rowData = expandedData.getItem(IbisDataTables.getRowId($(this)));
						instance.fnOpen(this, rowData, "ibisDatatable_expandedData");
					}
				});
			}
		});
	},
	
	/**
	 * Saves the search and pagination details to a cookie for propagation
	 * across page refresh.
	 * 
	 * @param oSettings settings data from datatables
	 * @param oData table state data from datatables
	 */
	saveTableState : function(oSettings, oData) {
		var state = {
			"iEnd" : oData.iEnd,
			"abVisCols" : oData.abVisCols,
			"iStart" : oData.iStart,
			"oSearch" : {
				"sSearch" : oData.oSearch.sSearch
			}
		};
		$.cookie(oSettings.sCookiePrefix+oSettings.sInstance, JSON.stringify(state));
	},
	
	/**
	 * Loads the search and pagination details from a cookie for propagaition
	 * across page refresh.
	 * 
	 * @param oSettings settings data from datatables
	 * @returns the complete state data to be loaded into the table
	 */
	loadTableState : function(oSettings) {
		// the default state
		var stateDef = {
			aaSorting : [],
			iLength : 15,
			oSearch : {
				bCaseInsensitive : true,
				bRegex : false,
				bSmart : true
			}
		};
		var sData = $.cookie(oSettings.sCookiePrefix+oSettings.sInstance);
		if (sData == null) return null;
		try {
			oData = (typeof $.parseJSON === 'function') ? 
				$.parseJSON(sData) : eval( '('+sData+')' );
		} catch (e) {
			oData = null;
		}
		
		$.extend(true, stateDef, oData);
		return stateDef;
	},
	
	/**
	 * Handles redrawing checkbox states after a table has changed ("redraw")
	 * 
	 * @param $tableContainer the table container to start processing on
	 */
	drawRedrawInitCheckboxes : function($tableContainer) {
		// instead of trying (unsuccessfully) to find the column of the operation checkboxes, just look to see if they exist
		if ($tableContainer.find(".selectionInput")) {
			$tableContainer.find('th').eq(0).width("35").css("padding", "3px 10px");
			
			var isDisabled = IDT.getSettingsOption($table, "disableSelection");
			var checkboxList = IbisDataTables.getVisTableRowInputs($table);
			checkboxList.each(function() {
				var row = $(this).parents("tr").eq(0);
				var $checkbox = $(this);
				var saveValue = IDT.getSaveValue($checkbox);
				
				// check to see if all inputs are disabled by other code
				// note, this !isDisabled covers both undefined and defined false
				if (!isDisabled) {
					$checkbox.prop("disabled", false);
				}
				else {
					$checkbox.prop("disabled", true);
				}
				
				if (IDT.isSelectedOption($checkbox, saveValue, $table)) {
					$checkbox.prop("checked", true);
					IDT.setRowSelected(row);
				}
				else {
					$checkbox.prop("checked", false);
					IDT.setRowDeselected(row);
				}
			});
			
			// set all buttons to their default state wrt the number of buttons selected
			if (checkboxList.length > 0) {
				OpButtonController.selectedChange(checkboxList.eq(0));
			}
			
			// re-build the event binding for checkboxes
			checkboxList.unbind("click").click(function(event) {
				IbisDataTables.changeCheckboxSelection($(this), true, event);
			});
		}
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
		
		// if the input is a radio, we have to clear all other radios
		if ($checkbox.attr("type") == "radio") {
			IbisDataTables.clearOtherRadios($checkbox);
		}
		
		$checkbox.prop("disabled", true);
		setTimeout(function() {$checkbox.prop('disabled', false);}, 250);
		IbisDataTables.validateCheckboxSelect($checkbox);
		$checkbox.trigger("select");
	},
	
	/**
	 * Handles the click operation on a checkbox in the table.  This method decides
	 * whether the checkbox that was clicked is checked or not and calls the
	 * appropriate method.
	 * 
	 * Also sets the class for the row
	 * 
	 * @param $checkbox
	 */
	validateCheckboxSelect : function($checkbox) {
		var saveValue = IDT.getSaveValue($checkbox);
		var $table = IDT.getTable($checkbox);
		
		if (IDT.isSelectedOption($checkbox, saveValue, $table)) {
			$checkbox.prop("checked", false);
			IDT.setRowDeselected($checkbox.parents("tr").eq(0));
			IDT.removeSelectedOption($checkbox, saveValue, $table);
		}
		else {
			$checkbox.prop("checked", true);
			IDT.setRowSelected($checkbox.parents("tr").eq(0));
			IDT.addSelectedOptionValue($table, saveValue);
		}
		
		// send this change to the op button selection change processor
		OpButtonController.selectedChange($checkbox);
	},
	
	/**
	 * Makes the visible rows in a datatable clickable such that the user clicking on the row
	 * will act as if the user had clicked on the input checkbox or radio button.
	 * 
	 * @param $table the table to apply the changes to
	 */
	initClickableRows : function($table) {
		$table.find("tbody tr").unbind("click").click(function(e) {
			e.stopPropagation();
			if (!$(e.target).is("a, input, select, label")) {
				var $input = $(this).find("input").first();
				if (!$input.is(":disabled")) {
					if ($input.is(":checked")) {
						$input.prop("checked", false);
					}
					else {
						$input.prop("checked", true);
					}
		
					// calls the click handler.  NOTE: this doesn't ACTUALLY click it
					// otherwise the above wouldn't be needed
					$input.trigger("click");
				}
			}
		});
	},
	
	/**
	 * Clicks the input.  That does the needed STUFF
	 * 
	 * @param $input the input to click
	 */
	click : function($input) {
		if (!$input.is(":disabled")) {
			if ($input.is(":checked")) {
				$input.prop("checked", false);
			}
			else {
				$input.prop("checked", true);
			}
			
			if ($input.prop("tagName").toLowerCase() == "radio") {
				IDT.clearOtherRadios($input);
			}
			$input.trigger("click");
		}
	},
	
	/**
	 * Prepares the table to receive a new selected RADIO BUTTON.
	 * 
	 * Clears the "selected row" property of any radio button row other than
	 * the given input.  Also gets rid of ALL selected inputs in the selected
	 * array.
	 * 
	 * @param $input the newly-selected input
	 */
	clearOtherRadios : function($input) {
		if ($input.attr("type") == "radio") {
			var $table = IbisDataTables.getTable($input);
			IbisDataTables.setRowDeselected($table.find(".row_selected"));
			IbisDataTables.clearOtherSelected($input);
		}
	},
	
	setRowSelected : function($row) {
		if (!$row.hasClass("row_selected")) {
			$row.addClass("row_selected");
		}
	},
	
	setRowDeselected : function($row) {
		$row.removeClass("row_selected");
	},
	
	/**
	 * Used by the page to recalculate the width of columns after the table
	 * is displayed (normally used when it is initially hidden).  This is to
	 * handle the problem as shown in 
	 * http://www.datatables.net/examples/api/tabs_and_scrolling.html
	 * 
	 * @param tableId (optional) the ID of the table to grab.  This ID MUST be in tableReferences
	 */
	recalculateWidths : function(tableId) {
		var tables = IbisDataTables.dataTables[tableId];
		if (typeof tableId == "undefined") {
			var keys = IbisDataTables.tableReferences;
			tables = new Array();
			for (var i = 0; i < keys.length; i++) {
				tables.push(IbisDataTables.dataTables[keys[i]]);
			}
		}
		
		for (var i = 0; i < tables.length; i++) {
			tables[i].fnAdjustColumnSizing();
		}
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
	 * Gets the list of table headers within the specified table
	 * 
	 * @param $table a jquery reference to the <table> to search
	 * @returns {Array} string array containing the current table headers or empty array if not found
	 */
	getTableHeaders : function($table) {
		var tableHeaders = new Array();
		$table.find("th").each(function() {
			var header = $(this).text();
			tableHeaders.push(header);
		});
		return tableHeaders;
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
		var output = [];
		if (typeof html === "undefined") {
			html = false;
		}
		var tableRef = IDT.dataTables[IDT.getId($table)];
		var cellSearch = IDT.getSettingsOption($table, "cellSearch");
		var selectedChecks = IDT.getSelectedOptions($table);
		var colIndex = _.indexOf(cellSearch.tableHeaders, headerText);
		if (colIndex != -1) {
			for (var i = 0; i < selectedChecks.length; i++) {
				var rowIndex = _.indexOf(cellSearch.rowMap, selectedChecks[i]);
				var cellData = tableRef.fnGetData(rowIndex)[colIndex];
				if (!html && String(cellData).match(/<[a-z][\s\S]*>/i)) {
					// for any HTML element, we have to filter out the tags
					output.push($(cellData).text());
				}
				else {
					output.push(cellData);
				}
			}
		}
		
		return output;
	},
	
	/**
	 * Determines what language to display the datatable in.  Uses the
	 * browser's language setting.
	 * 
	 * @returns Object defining language text
	 */
	determineLanguage : function() {
		if (typeof language == "undefined" || language == "") {
			return oLanguage_en_US;
		}
		
		if (language.indexOf("zh") > -1) {
			return oLanguage_zh_TW;
		}
		else if (language.indexOf("es") > -1) {
			return oLanguage_es;
		}
		else {
			return oLanguage_en_US;
		}
	},
	
	TableLayoutListener : {
		timeout : 500, // milliseconds
		masterTimer : null,
		invisibleTables : $(), // an empty jquery set
		excludeTables : $(),
		callback : function() {},
		
		init : function(config) {
			if (typeof config != "object") {
				throw "configuration is not an object";
			}
			
			for (var k in config) {
				this[k] = config[k];
			}
		},
		
		/**
		 * Adds the given jquery <table> reference to the list of tables
		 * to exclude from processing as invisible.
		 * 
		 * @param $table a jquery reference to a <table> to exclude
		 */
		exclude : function($table) {
			this.excludeTables = this.excludeTables.add($table);
		},
		
		/**
		 * Adds a <table> jquery reference to the list of invisible
		 * tables.  Checks the exclude list to ensure that the table is
		 * not a part of that list before adding.  If this is the first
		 * table in the list, starts the master timer.
		 * 
		 * @param $table jquery reference to a <table> to add
		 * @param config (optional) a configuration object to override the local config if needed
		 */
		addTable : function($table, config) {
			if (this.countTables() == 0 && typeof config != "undefined" && config != null) {
				try {
					this.init(config);
				}
				catch(exception) {
					// fall back to default configs
				}
			}
			
			// we don't want to add the table if it's in the exclude list
			if (!this.excludeTables.is($table)) {
				var tablesPreviousLength = this.countTables();
				this.invisibleTables = this.invisibleTables.add($table);
				if (tablesPreviousLength == 0) {
					this.startTimer();
				}
			}
		},
		
		/**
		 * Removes a table from the invisibleTables list.  If there
		 * are no more tables in the invisible list, calls stopTimer.
		 * 
		 * @param $table the table to remove
		 */
		removeTable : function($table) {
			this.invisibleTables = this.invisibleTables.not($table);
			if (this.invisibleTables.length == 0) {
				this.stopTimer();
			}
		},
		
		/**
		 * Starts the master timer.  Calls mainLoop() in intervals
		 * of timeout milliseconds.
		 */
		startTimer : function() {
			this.masterTimer = setInterval(
					function() {IbisDataTables.TableLayoutListener.mainLoop();}, 
					IbisDataTables.TableLayoutListener.timeout);
		},
		
		/**
		 * Stops the master timer
		 */
		stopTimer : function() {
			clearInterval(this.masterTimer);
		},
		
		/**
		 * Counts the number of tables in invisibleTables.
		 * Allows for shorter counts of this variable
		 * 
		 * @returns integer count of the elements in invisibleTables
		 */
		countTables : function() {
			return this.invisibleTables.length;
		},
		
		/**
		 * Handles the table becoming visible for the first time.
		 * Removes the table from the invisible list, adds it to
		 * the exclude list, and calls the callback function on it.
		 * 
		 * @param $table the table to operate on
		 */
		onVisible : function($table) {
			this.removeTable($table);
			this.exclude($table);
			this.callback($table);
		},
		
		/**
		 * Called on each hit of the master timer.  This checks each
		 * table in invisibleTables to see if any are visible and,
		 * if so, runs onVisible for it/them.
		 */
		mainLoop : function() {
			IbisDataTables.TableLayoutListener.invisibleTables.each(function() {
				if ($(this).is(":visible")) {
					IbisDataTables.TableLayoutListener.onVisible($(this));
				}
			});
		}
		
	}
};


var oLanguage_en_US = {
	"oAria": {
		"sSortAscending": ": activate to sort column ascending",
		"sSortDescending": ": activate to sort column descending"
	},
	"oPaginate": {
		"sFirst": "First",
		"sLast": "Last",
		"sNext": "Next",
		"sPrevious": "Previous"
	},
	"sEmptyTable": "No data available in table",
	"sInfo": "Showing _START_ to _END_ of _TOTAL_ entries",
	"sInfoEmpty": "Showing 0 to 0 of 0 entries",
	"sInfoFiltered": "(filtered from _MAX_ total entries)",
	"sInfoPostFix": "",
	"sInfoThousands": ",",
	"sLengthMenu": "Show _MENU_ entries",
	"sLoadingRecords": "Loading...",
	"sProcessing": "Processing...",
	"sSearch": "Search:",
	"sUrl": "",
	"sZeroRecords": "No matching records found"
};

var oLanguage_zh_TW = {
    "sProcessing":   "處理中...",
    "sLengthMenu":   "顯示 _MENU_ 項結果",
    "sZeroRecords":  "沒有匹配結果",
    "sInfo":         "顯示第 _START_ 至 _END_ 項結果，共 _TOTAL_ 項",
    "sInfoEmpty":    "顯示第 0 至 0 項結果，共 0 項",
    "sInfoFiltered": "(從 _MAX_ 項結果過濾)",
    "sInfoPostFix":  "",
    "sSearch":       "搜索:",
    "sUrl":          "",
    "oPaginate": {
        "sFirst":    "首頁",
        "sPrevious": "上頁",
        "sNext":     "下頁",
        "sLast":     "尾頁"
    }
};

var oLanguage_sp = {
    "sProcessing":     "Procesando...",
    "sLengthMenu":     "Mostrar _MENU_ registros",
    "sZeroRecords":    "No se encontraron resultados",
    "sEmptyTable":     "Ningún dato disponible en esta tabla",
    "sInfo":           "Mostrando registros del _START_ al _END_ de un total de _TOTAL_ registros",
    "sInfoEmpty":      "Mostrando registros del 0 al 0 de un total de 0 registros",
    "sInfoFiltered":   "(filtrado de un total de _MAX_ registros)",
    "sInfoPostFix":    "",
    "sSearch":         "Buscar:",
    "sUrl":            "",
    "sInfoThousands":  ",",
    "sLoadingRecords": "Cargando...",
    "oPaginate": {
        "sFirst":    "Primero",
        "sLast":     "Último",
        "sNext":     "Siguiente",
        "sPrevious": "Anterior"
    },
    "fnInfoCallback": null,
    "oAria": {
        "sSortAscending":  ": Activar para ordernar la columna de manera ascendente",
        "sSortDescending": ": Activar para ordernar la columna de manera descendente"
    }
};

IbisDataTables.InfoRowManager = {
	init : function($table) {
		this.placeExportRow($table);
		this.createSelectedItemsLabel($table);
	},

	getInfoRow : function($table) {
		return $table.parents(".dataTableContainer").find(".dataTables_info");
	},
	
	getSelectedItemsContainer : function($table) {
		return $table.parents(".dataTableContainer").find(".rowCount");
	},
	
	getRowWordContainer : function($table) {
		return $table.parents(".dataTableContainer").find(".rowWord");
	},
	
	/**
	 * Removes the export row (if it exists) from the table and stores in the table's data
	 * 
	 * @param $table the table to act upon
	 */
	removeExportRow : function($table) {
		IDT.initSettings($table);
		if ($table.find("tr.tableRowAction").length > 0) {
			var exportRow = $table.find("td.tableCellAction").html();
			IbisDataTables.setSettingsOption($table, "exportRow", exportRow);
			// find the actual row and remove it
			$table.find("td.tableCellAction").parents("tr").last().remove();
		}
	},
	
	/**
	 * if this table has an export row, move it into place
	 * 
	 * @param $table the table to act upon
	 */
	placeExportRow : function($table) {
		var exportRow = IDT.getSettingsOption($table, "exportRow");
		if (exportRow != null) {
			var $exportDiv = $("<div></div>");
			$exportDiv.css("float", "left").addClass("tableExportAction").html(exportRow);
			var $infoRow = this.getInfoRow($table);
			if ($infoRow.length > 0) {
				$infoRow.after($exportDiv);
			}
		}
	},
	
	createSelectedItemsLabel : function($table) {
		if (this.getSelectedItemsContainer($table).length < 1) {
			var $infoRow = this.getInfoRow($table);
			var $defaultDiv = $('<div class="rowCountContainer">(<span class="rowCount">0</span> <span class="rowWord">Rows</span> Selected)</div>');
			$infoRow.after($defaultDiv);
		}
	},
	
	/**
	 * Updates the selected item count in the table's info row
	 * @param $table the table
	 * @count the new count to place in the info row
	 */
	updateSelectedItemCount : function($table, count) {
		var $countContainer = this.getSelectedItemsContainer($table);
		$countContainer.text(count);
		this.updateRowWord($table, count);
	},
	
	getCountSelectedItems : function($table) {
		var $countContainer = this.getSelectedItemsContainer($table).text();
	},
	
	updateRowWord : function($table, count) {
		var word = "Rows";
		if (count == 1) {
			word = "Row";
		}
		
		this.getRowWordContainer($table).text(word);
	}
};


/**
 * Fix for <= IE8.  Their array object doesn't have indexOf
 */
if (!Array.prototype.indexOf) {
	Array.prototype.indexOf = function(needle) {
        for(var i = 0; i < this.length; i++) {
            if(this[i] === needle) {
                return i;
            }
        }
        return -1;
    };
}

/* Nickname for ibisdatatables to shorten calls */
window.IDT = IbisDataTables;