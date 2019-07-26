/**
 * 
 */
var DataTable = BaseModel.extend({
	rows 			: null,	// Rows collection of Row models
	columns 		: null, // TableCols collection of TableCol models
	selectedRows 	: null, // Rows collection of Row models that are selected
	
	defaults : {
		id					: null, 	// unique ID of the table (from IDT.getId())
		rendered			: false,	// is this table rendered?
		$el					: null,		// the $el from the model.  Allows for searching
		aaData				: [],		// original data for the table.  Empty after building
		aoColumns			: [],		// the aoColumns JSON that builds the table
		datatable			: null,		// reference to the result of the dataTable call
		inputType			: "none",	// type of selection input this table uses
		exportRow			: "",		// text content for export row
		selectionDisable	: false,	// is selection disabled?
		disableSelectAll	: false,	// is select all disabled?
		config				: {},		// any configuration for the datatable beyond what is already calculated
		expandedDataIndex	: -1		// index of expanded data column
	},
	
	initialize : function() {
		this.rows = new Rows();
		this.columns = new TableCols();
		this.selectedRows = new Rows();
	},
	
	loadData : function(rawData) {
		this.loadColumns(rawData.aoColumns);
		this.loadRows(rawData.aaData);
	},
	
	loadColumns : function(aoColumns) {
		this.set("aoColumns", aoColumns);
		
		for (var i = 0; i < aoColumns.length; i++) {
			var column = aoColumns[i];
			var isVisible = !(column.sTitle == "ibisDatatable_expandedData");
			
			var isSortable = true;
			if (i == 0 && this.get("inputType") !== "none") {
				isSortable = false;
			}
			
			this.columns.create({
				title		: column.sTitle,
				sortable	: isSortable,
				visible 	: isVisible,
				index		: i
			});
		}
	},
	
	loadRows : function(aaData) {
		this.set("aaData", aaData);
		// rows are arranged as a 2-dimensional array of cells.  So a single row
		// is a standard array of individual String cells
		
		// find the index of any column that is set to be for expanded data
		var expandedDataIndex = this.get("expandedDataIndex");
		for (var j = 0; j < this.columns.length; j++) {
			if (this.columns.at(j).get("visible") === false) {
				expandedDataIndex = j;
				break;
			}
		}
		this.set("expandedDataIndex", expandedDataIndex);
		
		for (var i = 0; i < aaData.length; i++) {
			var row = this.loadSingleRow(aaData[i]);
			this.determineDisabledRow(row);
		}
	},
	
	loadSingleRow : function(row) {
		var expandedDataIndex = this.get("expandedDataIndex");
		if (row.length > 0) {
			var expandedData = "";
			if (expandedDataIndex >= 0) {
				expandedData = row[expandedDataIndex];
			}
			
			var index = this.rows.length;
			
			var row = this.rows.create({
				saveValue			: null,
				data 				: row,
				expandedData 		: expandedData,
				expandedDataIndex 	: expandedDataIndex,
				index				: index
			});
			
			return row;
		}
	},
	
	removeSingleRow : function(row) {
		this.deselectRow(row.get("saveValue"));
		row.set("index", this.rows.indexOf(row));
		this.rows.remove(row);
	},
	
	addNewRow : function(row) {
		if (row instanceof Row) {
			// this will re-create the data but that's okay
			// it also ensures that expandeddata is correct
			this.loadSingleRow(row.get("data"));
		}
		else {
			this.loadSingleRow(row);
		}
	},
	
	removeRow : function(saveValue) {
		var row = this.row(saveValue);
		if (typeof row !== "undefined") {
			this.removeSingleRow(row);
		}
	},
	
	removeAllRows : function() {
		var rowArray = this.rows.toArray();
		for (var i = rowArray.length; i > 0; i--) {
			this.removeSingleRow(rowArray[i-1]);
		}
	},
	
	/**
	 * Selects the given row if not already selected.  Fires the selection event.
	 * If this table has input type = radio, deselects all other rows
	 */
	toggleRow : function(saveVal) {
		var rw = this.row(saveVal);
		if (typeof rw !== "undefined" && !this.get("selectionDisable")) {
			if (this.get("inputType") == "radio") {
				this.deselectAll();
			}
			
			if (!this.isSelected(rw)) {
				this.addSelectedRow(rw);
				EventBus.trigger("select:row", saveVal, this);
			}
			else {
				this.removeSelectedRow(rw);
				EventBus.trigger("deselect:row", saveVal, this);
			}
		}
	},
	
	selectRow : function(saveVal) {
		var rw = this.row(saveVal);
		if (typeof rw !== "undefined" && !this.get("selectionDisable")) {
			if (this.get("inputType") == "radio") {
				this.deselectAll();
			}
			
			if (!this.isSelected(rw)) {
				this.addSelectedRow(rw);
				EventBus.trigger("select:row", saveVal, this);
			}
		}
	},
	
	deselectRow : function(saveVal) {
		var rw = this.row(saveVal);
		if (typeof rw !== "undefined" 
				&& this.isSelected(rw) 
				&& !this.get("selectionDisable")) {
			this.removeSelectedRow(rw);
			EventBus.trigger("deselect:row", saveVal, this);
		}
	},
	
	selectSet : function(saveVals) {
		for (var i = 0; i < saveVals.length; i++) {
			this.selectRow(saveVals[i]);
		}
		EventBus.trigger("select:set", saveVals, this);
	},
	
	deselectAll : function() {
		// we need the remove event, so we have to do this
		// "the old fashioned way"
		var len = this.selectedRows.length;
		for (var i = 0; i < len; i++) {
			this.selectedRows.pop();
		}
		EventBus.trigger("deselect:all", this);
	},
	
	selectAll : function() {
		var len = this.rows.length;
		for (var i = 0; i < len; i++) {
			this.addSelectedRow(this.rows.at(i));
		}
		EventBus.trigger("select:all", this);
	},
	
	addSelectedRow : function(row) {
		if (row.get("disabled") === false) {
			this.selectedRows.add(row);
		}
	},
	
	removeSelectedRow : function(row) {
		this.selectedRows.remove(row);
	},
	
	row : function(saveVal) {
		return this.rows.findWhere({saveValue: saveVal});
	},
	
	hasRow : function(saveVal) {
		return this.row(saveVal) ? true : false;
	},
	
	isSelected : function(row) {
		return this.selectedRows.contains(row);
	},
	
	isSelectedSaveVal : function(saveVal) {
		var row = this.row(saveVal);
		return this.isSelected(row);
	},
	
	getSelectedSaveVals : function() {
		var selectedSaveVals = [];
		this.selectedRows.each(function(row) {
			selectedSaveVals.push(row.get("saveValue"));
		});
		return selectedSaveVals;
	},
	
	/**
	 * Uses the value priorities shown below to find a particular row and return
	 * that row if found.
	 * 
	 * THIS IS THE SAME AS ROW()
	 * 
	 * Priority:
	 * 	1. input ID
	 * 	2. input value
	 * 	3. (not preferred) the text of the entire row (reasonably unique)
	 */
	rowByValue : function(value) {
		return this.row(value);
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
		var output = [];
		
		if (typeof html === "undefined") {
			html = false;
		}
		
		var column = this.columns.indexByName(colTitle);
		if (!column) {
			output = [""];
		}
		else {
			colIndex = column.get("index");
			for (var i = 0; i < this.selectedRows.length; i++) {
				var row = this.selectedRows.at(i);
				if (html) {
					output.push(row.getCellDataText(colIndex));
				}
				else {
					output.push(row.getCellDataHtml(colIndex));
				}
			}
		}
		return output;
	},
	
	determineDisabledRow : function(rowModel) {
		if (rowModel.getInput().is(":disabled")) {
			this.disableRowByModel(rowModel);
		}
		
	},
	
	disableSingleRow : function(saveValue) {
		this.disableRowByModel(this.rowByValue(saveValue));
	},
	
	disableRowByModel : function(rowModel) {
		rowModel.set("disabled", true);
		EventBus.trigger("disable:row", this, rowModel);
	},
	
	enableSingleRow : function(saveValue) {
		var row = this.rowByValue(saveValue);
		row.set("disabled", false);
		EventBus.trigger("enable:row", this, row);
	}
});