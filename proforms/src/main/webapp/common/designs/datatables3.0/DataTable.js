/**
 * 
 */
var DataTable = BaseModel.extend({
	selectedRows 			: {}, 		// row IDs for selected rows in the form of {rowId : true}
	tempData 				: {},		// temporary storage of table data (for render)
	
	defaults : {
		id					: null, 	// unique ID of the table (from IDT.getId())
		rendered			: false,	// is this table rendered?
		$el					: null,		// the $el from the view.  Allows for searching
		datatable			: null,		// reference to the result of the dataTable call
		inputType			: "none",	// type of selection input this table uses
		exportRow			: "",		// text content for export row
		selectionDisable	: false,	// is selection disabled?
		disableSelectAll	: false,	// is select all disabled?
		config				: {},		// any configuration for the datatable beyond what is already calculated
		expandedDataIndex	: -1,		// index of expanded data column
		filterData			: {}		// holds filter settings (state of filter inputs) as a htmlId => value object
	},
	
	initialize : function() {
		this.selectedRows = {};
		
		this.on("init:table", this.clearTempData);
	},
	
	loadData : function(rawData) {
		this.loadColumns(rawData.columns);
		this.loadRows(rawData.data);
	},
	
	clearTempData : function() {
		delete this.tempData;
	},
	
	loadColumns : function(columns) {
		// modify the columns any way needed
		for (var i = 0; i < columns.length; i++) {
			var column = columns[i];
			column['name'] = column.title.toLowerCase();
			
			if (column.title == "ibisDatatable_expandedData") {
				column.visible = false;
			}
		}
	},
	
	loadRows : function(rows) {
		// loops over the new rows but calls loadSingleRow to actually do the work
		// @see https://www.youtube.com/watch?v=SiMHTK15Pik
		if(rows.length > 9000) {
			var theNumber = rows.length;
			var j = 0;
			var grouping = 20;
			
			var that = this;
			setTimeout(function loopFunction() {
			    try {
			        // perform your work here, create tables rows here.
			    	var delta = theNumber - j;
			    	var loopLength = Math.floor(delta/grouping) ? grouping + j : theNumber;
			    	for (var i = j; i < loopLength; i++) { 
			    		var row = that.loadSingleRow(rows[i]);
					    j++;
			    	}
			    }
			    catch(e) {
			        // handle any exception
			    	console.log(e);
			    }
			    
			    if (j < theNumber) {
			        setTimeout(loopFunction, 0); // timeout loop
			    }
			   
			},0);
			
		} else {
			// if we have less than 9k rows, just loop
			for (var i = 0; i < rows.length; i++) {
				var row = this.loadSingleRow(rows[i]);
			}
		}
	},
	
	table : function() {
		return this.get("datatable");
	},
	
	numRows : function() {
		var table = this.table();
		if (table != null) {
			var rows = table.rows();
			return rows.count();
			//return table.rows().count();
		}
		else {
			return 0;
		}
	},
	
	loadSingleRow : function(rowData) {
		// add row disabled value into the object
		rowData['disabled'] = false;
		// TODO: determine expanded column (if any)
	},
	
	addNewRow : function(row) {
		var rowApi = this.table().row.add(row);
		this.trigger("add:row", row);
	},
	
	removeRow : function(rowId) {
		var row = this.row(rowId);
		this.removeSelectedRow(row);
		var newTable = null;
		if (row.length > 0) {
			newTable = row.remove();
		}
		this.trigger("remove:row", row);
	},
	
	removeAllRows : function() {
		return this.table().clear();
	},
	
	/**
	 * Selects the given row if not already selected.  Fires the selection event.
	 * If this table has input type = radio, deselects all other rows
	 * 
	 * @param rowReference either a String saveValue or DataTables API Row object
	 */
	toggleRow : function(rw) {
		if (typeof rw !== "object") {
			rw = this.rowByValue(rw);
		}
		
		if (rw.length > 0 && !this.get("selectionDisable")) {
			if (this.get("inputType") == "radio") {
				this.deselectAll();
			}
			
			if (!this.isSelected(rw)) {
				this.addSelectedRow(rw);
				this.trigger("select:row", rw, this);
				EventBus.trigger("select:row", rw, this);
			}
			else {
				this.removeSelectedRow(rw);
				this.trigger("deselect:row", rw, this);
				EventBus.trigger("deselect:row", rw, this);
			}
		}
	},
	
	/**
	 * Selects a single row.  The row can be sent as either an ID string or
	 * as a DataTables API Row object.
	 * 
	 * @param rw {String} row ID of the row to select
	 * @param rw {DataTables API row} datatables row with single row in results
	 * @sideEffect triggers "select:row"
	 */
	selectRow : function(rw) {
		if (typeof rw !== "object") {
			rw = this.row(rw);
		}
		if (rw.length > 0 && !this.get("selectionDisable")) {
			if (this.get("inputType") == "radio") {
				this.deselectAll();
			}
			
			if (!this.isSelected(rw)) {
				this.addSelectedRow(rw);
				this.trigger("select:row", rw, this);
			}
		}
	},
	
	/**
	 * Deselects a single row.  The row can be sent as either an ID string or
	 * as a DataTables API Row object.
	 * 
	 * @param rw {String} row ID of the row to deselect
	 * @param rw {DataTables API row} datatables row with single row in results
	 * @sideEffect triggers "deselect:row"
	 */
	deselectRow : function(rw) {
		// if the parameter is the row ID, get the row object
		if (typeof rw !== "object") {
			rw = this.row(rw);
		}
		if (rw.length > 0 
				&& this.isSelected(rw) 
				&& !this.get("selectionDisable")) {
			this.removeSelectedRow(rw);
			this.trigger("deselect:row", rw.id(), this);
		}
	},
	
	/**
	 * Selects a set of rows.  Can be used for select all and select filtered.
	 * Does not deselect - can only add to the selection list.
	 * 
	 * @param rows {DataTable API rows} the rows to select
	 * @sideEffect triggers select:set
	 */
	selectSet : function(rows) {
		var model = this;
		var saveVals = [];
		rows.every(function(rowIdx, tableLoop, rowLoop) {
			var rowId = model.table().row(rowIdx).id();
			model.selectRow(rowId);
			saveVals.push(rowIdx);
		});
		this.trigger("select:set", saveVals, this);
	},

	deselectAll : function() {
		this.selectedRows = {};
		this.trigger("deselect:all", this);
	},
	
	selectAll : function() {
		var table = this.table();
		var model = this;
		table.rows().eq(0).each(function(index) {
			model.addSelectedRow(table.row(index));
		});
		this.trigger("select:all", this);
	},
	
	/**
	 * Selects the given row.  Performs no validation and is intended
	 * to be used internally AFTER another function verifies that the
	 * row is not selected already.
	 * 
	 * @param row {DataTables API} row reference to select
	 * @return row that was selected
	 */
	addSelectedRow : function(row) {
		this.selectedRows[row.id()] = true;
		return row;
	},
	
	/**
	 * Deselects the given row API reference.
	 * 
	 * @param row {DataTables API} row reference to deselect
	 * @return row that was deselected
	 */
	removeSelectedRow : function(row) {
		if (typeof row !== "object") {
			row = this.row(row);
		}
		
		var rowId = row.id();
		if (this.selectedRows[rowId]) {
			delete this.selectedRows[rowId];
		}
		
		return row;
	},
	
	row : function(reference) {
		return this.table().row(reference);
	},
	
	hasRow : function(saveVal) {
		return this.rowByValue(saveVal).length > 0;
	},
	
	isSelected : function(row) {
		return this.isSelectedSaveVal(row.id());
	},
	
	isSelectedSaveVal : function(saveVal) {
		return (typeof this.selectedRows[saveVal] !== "undefined");
	},
	
	getSelectedSaveVals : function() {
		return Object.keys(this.selectedRows);
	},
	
	numSelected : function() {
		return Object.keys(this.selectedRows).length;
	},
	
	/**
	 * Uses the saveVal property to retrieve a row
	 */
	rowByValue : function(value) {
		return this.row("#" + value);
	},
	
	/**
	 * Gets the column API object by header title.  NOTE: case insensitive.
	 * 
	 * We store the column title as the column name when creating the table
	 * so this uses the datatables column() api method
	 * 
	 * @attr title {string} header title of the column (case insensitive)
	 */
	colByName : function(title) {
		return this.table().column(title.toLowerCase());
	},
	
	/**
	 * Gets a particular column's index given the column's title.
	 * 
	 * @attr title {string} header title of the column (case insensitive)
	 * @return {number} index if found; otherwise -1
	 */
	colIndexByName : function(title) {
		var col = this.colByName(title);
		if (col.length > 0) {
			return col.index();
		}
		else {
			return -1;
		}
	},
	
	/**
	 * Gets data from the intersection of the selected row(s) and the columns
	 * specified by colTitle, the header text of the requested column.
	 * 
	 * @param colTitle the title of the column to find
	 * @param html (optional, default false) get HTML as opposed to text?
	 * @return string[] of column contents
	 */
	cellData : function(colTitle, html) {
		if (typeof html === "undefined") {
			html = false;
		}
		
		var output = [];
		var length = this.numSelected();
		var selectedRowsKeys = this.selectedRows.keys();
		for (var i = 0; i < length; i++) {
			var cell = this.table().cell(this.selectedRowsKeys[i], colTitle);
			if (cell.length > 0) {
				var cellDataHtml = cell.data();
				if (html) {
					output.push(cellDataHtml);
				}
				else {
					output.push($(cellDataHtml).text());
				}
			}
		}
		return output;
	},
	
	/**
	 * Disables a single row in the datatable back-end.
	 * This method does NOT change the rendering of the checkbox.
	 * 
	 * @param saveValue {string} ID of the row to disable
	 * @return {DataTable API Row} row acted upon
	 */
	disableSingleRow : function(saveValue) {
		var row = this.rowByValue(saveValue);
		if (row) {
			var data = row.data();
			data['disabled'] = true;
			row.data(data);
		}
		this.trigger("disable:row", this, row);
		return row;
	},
	
	/**
	 * Enables a single row in the datatable back-end.
	 * This method does NOT change the rendering of the checkbox.
	 * 
	 * @param saveValue {string} ID of the row to enable
	 * @return {DataTable API Row} row acted upon
	 */
	enableSingleRow : function(saveValue) {
		var row = this.rowByValue(saveValue);
		if (row) {
			var data = row.data();
			data['disabled'] = false;
			row.data(data);
		}
		this.trigger("enable:row", this, row);
		return row;
	}
});

