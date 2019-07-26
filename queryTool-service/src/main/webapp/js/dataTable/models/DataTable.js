/**
 * 
 */
QTDT.DataTable = BaseModel.extend({
	
	rows 			: null,	// Rows collection of Row models
	formColumns     : null,
	rgColumns		:null,
	columns 		: null, // TableCols collection of TableCol models
	selectedFrozenColumn : null,
	frozenRows 			: null,	// Rows collection of frozen Row models
	frozenFormColumns     : null,
	frozenRgColumns		:null,
	frozenColumns 		: null, // TableCols collection of TableCol models
	frozenColumnsExist : false,
	selectedRows 	: null, // Rows collection of Row models that are selected
	headers : null, //TODO: i need to figure out if we need header models for this, it's a row, sooo figure this out
	   rowCellViews : new Array(),
	   frozenRowCellViews : new Array(),
	   rowViews : [],

	
	defaults : {
		id					: null, 	// unique ID of the table (from IDT.getId())
		rendered			: false,	// is this table rendered?
		data				: [],		// original data for the table.  Empty after building
		headers		: [],		// the headers JSON that builds the table
		datatable			: null,		// reference to the result of the dataTable call
		config				: {},		// any configuration for the datatable beyond what is already calculated
		scrollLeft	: 0,	
		scrollTop	: 0,
		originalTableHeaderWidth: 0,
		originalFrozenTableHeaderWidth: 0,
		originalDataBodyWidth: 0,
		originalFrozenDataBodyWidth: 0,
		rows 			: null,	// Rows collection of Row models
		columns 		: null, // TableCols collection of TableCol models
		dataTables 		: null, 
		ColCells        : [],
		RowCells        : [],
		offsetY : 0,
		biosampleArray : [], //this is an array to keep checkboxes of selected biosamples
		unselectedBioSampleArray: [], //this is an array to keep unselected biosamples when a user chooses all samples
		selectAllChecked: false, //this is for biosample ordering
		bioFormName : "",
	},
	
	initialize : function() {
		this.rows = new QTDT.Rows();
		this.formColumns = new QTDT.Cols();
		this.rgColumns = new QTDT.Cols();
		this.columns = new QTDT.Cols();
	},
	//rawData is the query result
	loadData : function(rawData) {  
		var query = QueryTool.page.get("query");
		var customHeader = {"name":"Forms:","version":"","repeatableGroupHeaders":[{"name":"Repeatable Groups:","dataElementHeaders":["Study ID","Dataset"],"doesRepeat":false}]};
		//rawData.header[0].formHeaders = "Forms";
		
		
		if(query.get("selectedForms").length > 1) { 
			customHeader = {"name":"Forms:","version":"","repeatableGroupHeaders":[{"name":"Repeatable Groups:","dataElementHeaders":["GUID"],"doesRepeat":false}]};
			//rawData.header[0].repeatableGroupHeaders.unshift({"name":"Repeatable Groups:","dataElementHeaders":["GUID"],"doesRepeat":false});
			
		} /*else {
			//rawData.header[0].repeatableGroupHeaders.unshift({"name":"Repeatable Groups:","dataElementHeaders":["Study ID","Dataset"],"doesRepeat":false});
		}*/
	
		
		/* for activities such as filter, sort, they will not send header info, since it is not needed. So we will check if header exists, if it doesn't then we won't load them.  Note: If there is an error when loading data with header. i.e the header goes missing, we won't catch it. */
		if(rawData.header !== undefined && !_.isEmpty(rawData.header)) {
			rawData.header.unshift(customHeader);
			this.loadColumns(rawData.header);
		}
		
		this.rows.reset(null);
		this.set("offsetY", 0);
		if(rawData.data !== undefined && !_.isEmpty(rawData.data)) {
			this.loadRows(rawData.data.aaData);
		} else {
			this.loadRows(rawData.aaData);
		}
	},
	//rawData is the query result
	loadRowData : function(rawData) {  
		//this.rows.reset(null);
		
		if(rawData.data !== undefined && !_.isEmpty(rawData.data)) {
			this.loadRows(rawData.data.aaData);
		} else {
			this.loadRows(rawData.aaData);
		}
	},
	
	loadColumns : function(Columns) {
		this.set("Columns", Columns);
		//clear form rows
		this.formColumns.reset(null);
		//clear rg rows
		this.rgColumns.reset(null);
		//clear de rows
		this.columns.reset(null);
		var indexCount = 0;
		for (var i = 0; i < Columns.length; i++) {
			var formColumn = Columns[i];
			
			var isVisible = true;
			
			var isSortable = true;
			//create form header
			var forme = this.formColumns.create({
				id : i,
				name : formColumn.name,
				index		: i,
				headerType : "form"
			});
			
			
			
			var repeatableGroups = formColumn.repeatableGroupHeaders;
			for (var j = 0; j < repeatableGroups.length; j++) {
				
				var rgColumn = repeatableGroups[j];
			
				var rge = this.rgColumns.create({
					id : forme.get("id") + "_" + j,
					name : rgColumn.name,
					index		: j,
					headerType : "repeatableGroup",
					parent : forme
				});
				//add this as child to the form header
				forme.get("children").add(rge)
				var dataElementHeaders = rgColumn.dataElementHeaders;
				var doesRepeat = rgColumn.doesRepeat;
				
				for (var k = 0; k < dataElementHeaders.length; k++) {
					var indexValue = k  + indexCount;
					
					var deColumn = dataElementHeaders[k];
					
					var de = this.columns.create({
						id: rge.get("id") + "_" + k,
						name : deColumn,
						index		: indexValue,
						headerType : "dataElement",
						parent : rge,
						doesRepeat : doesRepeat
					});
					
					de.formVersion = formColumn.version;
					rge.get("children").add(de)
					
				}
				
				indexCount = indexValue+1;
				
				
			}
			
			
		}
		
		
	},
	loadRows : function(Data) {
		
		
		//clear rows
		//this.rows.reset(null);
		//reset column cells, since they are now new.
		this.columns.each(function(column){
			column.get("cells").reset(null);
		});
		for (var i = 0; i < Data.length; i++) {
			var cells = this.loadSingleRow(i,Data[i]);
		}
		
		
	},
	loadSingleRow : function(index, cells) {
			thisDataTable = this;
		if (cells.length > 0) {
			
			var atValue = index + thisDataTable.get("offsetY");
			//console.log("atvalue (" + index + " | " + thisDataTable.get("offsetY") + "):",atValue);
			//if the row already exists we don't need to recreate it
			var row = this.rows.findWhere({"index" :  atValue});
			if(row != undefined) {
				this.rows.remove(row);
			}
			
			var rowCells = new QTDT.ColCells();
		
			for (var j in cells) {
				//create collection of cells in a row
				var newCell = rowCells.create({
					idAttribute     : j,
					html			: cells[j],
					originalHtml    : cells[j],
					column			: (thisDataTable.columns.at(j) == null) ? new QTDT.Col() : thisDataTable.columns.at(j)
				});

				///create collection of cells for the column
				this.columns.each(function(column){
				
					if(column.get("index") == j) {
						column.get("cells").add(newCell);
					}
				});
			}
			
			
			
			
			var row = this.rows.create({
				cells 				: rowCells,
				totalCells			: cells.length,
				index				:  atValue
			});
			
			return row;
		}
	},
	loadFrozenColumns : function(selectedCol){
		this.frozenColumns = new QTDT.Cols();
		this.frozenRgColumns = new QTDT.Cols();
		this.frozenFormColumns  = new QTDT.Cols(); 
		
		var i = selectedCol.get("index");
		if(selectedCol.get("frozen")) {
			
			
			 --i; //we don't need to set the attribute of the already selected column
			while(i >= 0) {
				this.columns.at(i).set("frozen",true);
				--i;
			}
		} else {
			//set parent columns as unfrozen
			selectedCol.get("parent").set("frozen",false);
			selectedCol.get("parent").get("parent").set("frozen",false);
			++ i;  //we don't need to set the attribute of the already selected column
			while(i < this.columns.length) {
				this.columns.at(i).set("frozen",false);
				this.columns.at(i).get("parent").set("frozen",false);
				this.columns.at(i).get("parent").get("parent").set("frozen",false);
				++i;
			}
			
		}
		///create array of col models that are frozen
		var frozenColumnsArray = this.columns.where({frozen: true});
		
		
		if(frozenColumnsArray.length > 0) {
			
			$this = this;
			
			this.frozenColumns.add(frozenColumnsArray);
			
			 
			
			_.each(frozenColumnsArray,function(header){
				//set parents as frozen
				header.get("parent").set("frozen",true);
				header.get("parent").get("parent").set("frozen",true);
				//add frozen rg columns to collection
				$this.frozenRgColumns.add(header.get("parent"));
				//add frozen form columns to collection
				$this.frozenFormColumns.add(header.get("parent").get("parent"));
			});
			//populate frozen rg columns
			//transpose rows here
			fRows = this.frozenRows = new QTDT.Rows();
			
			
			
			var rowCount = 0;
			
			//for performance instead of doing this check every single time it's a good idea to use the first column for init. get the amount of rows based on the cell count for the collumns
			var initModel = this.frozenColumns.at(0);
			rowCount = (rowCount == 0) ? initModel.cells().length : 0;
			//create array of empty row models
			for(var i=0; i < rowCount; ++i) {
				this.frozenRows.create({
					cells 				: new QTDT.ColCells(),
					totalCells			: frozenColumnsArray.length,
					index				: i,
					frozen				: true
				});
			}
			
			var rowCells = new QTDT.ColCells();
			
			this.frozenColumns.each(function(colmodel){
				
				var cellsToTranspose = colmodel.cells();
				var transposeKey = 0;
				cellsToTranspose.each(function(cell){
					var rowToUpdate = fRows.at(transposeKey);
					cell.set("frozen",true);
					rowToUpdate.get("cells").add(cell);
					if(cell.get("rowView") != null ) {
						
						//we need to ensure that the heights of the current table rows are identical to the frozen table
						newRowHeight = cell.get("rowView").$el.height();
						rowToUpdate.set("height",newRowHeight);
					} 
					++transposeKey;
				})
				
			});
			this.frozenColumnsExist = true;
			
		} else {
			this.frozenColumnsExist = false;
		}
	
	},
	clearFrozenColumns : function () {
		
		if(this.frozenColumnsExist) {
			this.frozenColumnsExist = false;
			this.frozenColumns.reset();
			this.frozenRgColumns.reset();
			this.frozenFormColumns.reset(); 
		}
		
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
	
	removeRow : function(id) {
		var row = this.row(id);
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
	selectRow : function(id) {
		var rw = this.row(id);
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
	deselectRow : function(id) {
		var rw = this.row(id);
		if (typeof rw !== "undefined" 
				&& this.isSelected(rw) 
				&& !this.get("selectionDisable")) {
			this.removeSelectedRow(rw);
			EventBus.trigger("deselect:row", id, this);
		}
	},
	row : function(id) {
		return this.rows.findWhere({saveValue: id});
	},
	
	hasRow : function(id) {
		return this.row(id) ? true : false;
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

		closeHamburger : function() {
			$('.actionContainer').slideUp(300);
			$('.actionContainer').remove();
		}
	
	
	
});