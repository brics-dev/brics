/**
 * 
 */
var DataTableView = BaseView.extend({
	exportRow : null,
	baseConfig : {
		"bJQueryUI": true,
		"bDeferRender" : true,
		"bLengthChange": false,
		"iDisplayLength": 15,
		"bProcessing": true,
		"bAutoWidth": false,
		"sPaginationType" : "full_numbers",
		"aaSorting" : [], // disables initial sorting
		"fnInitComplete" : function(oSettings, json) {
			// moves the process buttons to the top of the data table
			OpButtonController.initTable($(this));
			
			// propagate this method to any further callback
			if (typeof fnInitComplete == "function") {
				fnInitComplete(oSettings, json);
			}
		},
		"fnRowCallback" : function(nRow, aData, iDisplayIndex, iDispalyIndexFull) {
			// note: in the JSON load version, all rows exist but aren't visible
			// at this point
			// this function is called when the table is first created for all
			// rows but also on each row when paginated

//			// pass any other call on through
			if (typeof fnRowCallback == "function") {
				fnRowCallback(nRow, aData, iDisplayIndex, iDisplayIndexFull);
			}
		},
		"fnPreDrawCallback" : function(settings) {
//			var tableRef = $(this).parents(".dataTableContainer");
//			$table = tableRef.find("table").eq(0);
//			$table.find("input").unbind("click");
//			$table.find("tbody tr").unbind("click");
		},
		"fnDrawCallback" : function(settings) {
			var tableRef = $(this).parents(".dataTableContainer");
			$table = tableRef.find("table").eq(0);
			var model = IDT.getTableModel($table);
//			IbisDataTables.drawRedrawInitCheckboxes(tableRef);
//			IbisDataTables.initClickableRows($(this));
//			IbisDataTables.setupExpandedData($(this), settings.oInstance);
			
			// propagate this method to any further callback
			if (typeof fnDrawCallback == "function") {
				fnDrawCallback(tableRef);
			}
			
			EventBus.trigger("draw:table", model);
		},
		fnCreatedRow : function(nRow, aData, iDataIndex) {
//			// propagate this method to any further callback
//			if (typeof fnCreatedRow == "function") {
//				fnCreatedRow(nRow, aData, iDataIndex);
//			}
		}
	},
	
	events : {
		'click input[type="checkbox"]' : "selectCheckbox",
		'click input[type="radio"]' : "selectRadio",
		'click tr' : "selectRow",
		'click .selectAll' : "selectAll",
		'click .selectFilter' : "selectFiltered",
		'click .selectNone' : "selectNone"
	},
	
	initialize : function() {
		this.$el = this.model.get("$el");
		this.listenTo(this.model.selectedRows, "add", this.onAddSelectedRow);
		this.listenTo(this.model.selectedRows, "remove", this.onRemoveSelectedRow);
		
		this.listenTo(this.model.rows, "add", this.onAddRow);
		this.listenTo(this.model.rows, "remove", this.onRemoveRow);
		//EventBus.on("select:row", this.select, this);
		//EventBus.on("deselect:row", this.deselect, this);
		EventBus.on("draw:table", this.onDraw, this);
		EventBus.on("derender:table", this.deRender, this);
		EventBus.on("disable:row", this.disableSingleRow, this);
		EventBus.on("enable:row", this.enableSingleRow, this);
		EventBus.on("filter:table", this.filterTable, this);
	},
	
	render : function() {
		var compiledData = {
			aaData: this.model.get("aaData"),
			aoColumns : this.model.get("aoColumns")
		};
		this.determineInputType(compiledData);
		this.createTransformDom();
		this.startupDatatables(compiledData);
		this.initInfoRow();
		this.setStyles();
		this.drawSelectAll();
		
		if (!this.isVisible()) {
			this.listenForVisible();
		}
		
		EventBus.trigger("init:table", this.model);
	},
	
	/**
	 * Actually runs the datatables init command
	 */
	startupDatatables : function(compiledData) {
		var config = $.extend({}, this.config, compiledData, this.specificConfig());
		this.model.loadData(compiledData);
		var dt = this.$el.dataTable(config);
		var id = this.model.get("id");
		
		this.model.set("datatable", dt);
		this.model.set("rendered", true);
		IDT.dataTables[id] = dt;
	},
	
	/**
	 * Set up config object specific to this table.  Modifies the standard
	 * config.
	 */
	specificConfig : function() {
		var cfg = {};
		// set the language for the controls
		cfg.oLanguage = DataTableLanguage.language();
		
		// does the first column need to be sorted?
		var tableNoSort = [];
		if (this.model.get("inputType") != "none") {
			tableNoSort[0] = 0;
		}
		// outlines whether to sort on the first column or not
		cfg.aoColumnDefs = [
				             { "bSortable": false, 
				            	 "aTargets": tableNoSort, 
				            	 "bSearchable": false
				             },
				             {"sDefaultContent": "none", "aTargets": ["_all"]}
						];
		
		// if the model has a filled in config object, merge that too
		var modelConfig = this.model.get("config") || {};
		return $.extend({}, this.baseConfig, cfg, modelConfig);
	},
	
	/**
	 * What is the selection input for this table?  checkbox, radio, or none
	 */
	determineInputType : function(data) {
		var inputType = "none";
		if (data.aaData.length > 0) {
			try {
				var $element = $(data.aaData[0][0]).eq(0);
				// if aaData[0][0] is regular text, element won't exist, so we check that here
				if ($element.length > 0) {
					if ($element.is('[type="radio"]') || $element.find('[type="radio"]').length > 0) {
						inputType = "radio";
					}
					else if ($element.is('input[type="checkbox"]') || $element.find('input[type="checkbox"]').length > 0) {
						inputType = "checkbox";
					}
				}
			}
			catch(e) {
				// in the case of an exception here, we've already set the type correctly
			}
		}
		this.model.set("inputType", inputType);
	},
	
	/**
	 * Gets the header cells from the DOM version of a table.  This is used by
	 * the API to break down the initial DOM table.
	 * 
	 * @param $table jQuery reference to the table to get the cells from
	 * @return Object containing the column headers html in aoColumn format
	 */
	getHeaderCells : function() {
		var $table = this.$el;
		var $tbodyTh = $("> tbody > tr > th" , $table);
		var $th = $("> tr > th" , $table);
		var $thead = $("> thead > tr > td" , $table);
		var $headerElements = $();
		
		if ($tbodyTh.length > 0) {
			$headerElements = $tbodyTh;
		} 
		else if ($th.length > 0) {
			$headerElements = $th;
		}
		else if ($thead.length > 0) {
			$headerElements = $thead;
		}
		else {
			// no head exists, use the first row
			$headerElements = $table.find("tr").eq(0);
		}
		
		var aoColumns = [];
		// using a regular loop here instead of each ensures
		// that we don't do jquery's multithreading and get the headers
		// out of order
		for (var i = 0; i < $headerElements.length; i++) {
			var $this = $headerElements.eq(i);
			if ($this.hasClass("ibisDatatable_expandedData")) {
				aoColumns.push({"bVisible": false});
			}
			else {
				var colspan = $this.attr("colspan");
				if (!colspan) {
					colspan = 1;
				}
				// so if colspan is 2, we give two columns with the same title here
				for (var j = 0; j < colspan; j++) {
					aoColumns.push({sTitle: $this.text()});
				}
			}
		}
		
		this.model.set("aoColumns", aoColumns);
		return $headerElements;
	},
	
	/**
	 * Interprets the row data for this table by reading the data from the DOM
	 */
	getBodyRows : function($table) {
		var aaData = [];
		var $tbody = $("> tbody > tr", $table);
		var $trs = $("> tr", $table);
		var $rows = $();
		if ($tbody.length > 0) {
			$rows = $tbody;
		}
		else {
			$rows = $trs;
		}
		
		for (var i = 0; i < $rows.length; i++) {
			var $this = $rows.eq(i);
			// each row is one entry in parent array, each cell is entry in
			// sub-array
			var rowArr = [];
			var $tds = $this.children("td");
			for (var j = 0; j < $tds.length; j++) {
				rowArr.push($tds.eq(j).html());
			}
			aaData.push(rowArr);
		}
		this.model.set("aaData", aaData);
		return $rows;
	},
	
	setStyles : function() {
		this.$("thead tr").addClass("tableRowHeader").attr("role", "row");
		this.$("th").addClass("tableCellHeader").attr("role", "columnheader");
	},
	
	drawSelectAll : function() {
		var inputType = this.model.get("inputType");
		if (inputType != "none" && inputType != "radio") {
			var $targetCell = this.$el.find("th.tableCellHeader").eq(0);
	//		here's some sample code needed for select all vs filtered:
	//		var table = IDT.models.at(0).get("datatable");
	//		table.fnFilter('test');
	//		table._('tr', {"filter":"applied"});
	//		NOTE: the above will give an error if the data is not filtered
			var html = '<nav class="idtNav"><ul><li class="selectAllIcon"><img src="' + baseUrl + '/formbuilder/images/checkbox_unchecked.png" />';
			html += '<ul><li><a href="javascript:;" class="selectAllMenu selectAll"><img src="' + baseUrl + '/formbuilder/images/checkbox_checked.png" />Select All</a></li>';
			html += '<li><a href="javascript:;" class="selectAllMenu selectFilter"><img src="' + baseUrl + '/formbuilder/images/checkbox_indeterminate.png" />Select Filtered</a></li>';
			html += '<li><a href="javascript:;" class="selectAllMenu selectNone"><img src="' + baseUrl + '/formbuilder/images/checkbox_unchecked.png" />Select None</a></li>';
			html += '</ul></li></nav>';
			$targetCell.html(html);
			this.determineSelectAllIcon();
		}
	},
	
	determineSelectAllIcon : function() {
		var selectedRowsLength = this.model.selectedRows.length;
		var allRowsLength = this.model.rows.length;
		if (selectedRowsLength == allRowsLength) {
			this.selectAllIconToChecked();
		}
		else if (selectedRowsLength > 0) {
			this.selectAllIconToPartial();
		}
		else {
			this.selectAllIconToUnchecked();
		}
	},
	
	selectAllIconToChecked : function() {
		var inputType = this.model.get("inputType");
		if (inputType != "none" && inputType != "radio") {
			this.$(".selectAllIcon > img").attr("src", baseUrl + "/formbuilder/images/checkbox_checked.png");
		}
	},
	
	selectAllIconToPartial : function() {
		var inputType = this.model.get("inputType");
		if (inputType != "none" && inputType != "radio") {
			this.$(".selectAllIcon > img").attr("src", baseUrl + "/formbuilder/images/checkbox_indeterminate.png")
		}
	},
	
	selectAllIconToUnchecked : function() {
		var inputType = this.model.get("inputType");
		if (inputType != "none" && inputType != "radio") {
			this.$(".selectAllIcon > img").attr("src", baseUrl + "/formbuilder/images/checkbox_unchecked.png")
		}
	},
	
	/**
	 * Create any needed DOM elements (table?) and/or transform
	 * the current DOM elements to what is needed by IDT
	 */
	createTransformDom : function() {
		// TODO: finish
	},
	
	/**
	 * Answers the question "does this row have a selection input?"
	 * 
	 * @param $row jquery reference to the row
	 * @return jquery reference to input if a selection input exists, otherwise false
	 */
	rowHasInput : function($row) {
		var $input = IDT.getInputForRow($row);
		if ($input.length > 0) {
			return $input;
		}
		else {
			return false;
		}
	},
	
	onDraw : function(tableModel) {
		// gets all visible rows, gets their save value (if applicable) and
		// checks to see if it's selected.  If so, makes sure it's marked as
		// selected.  Otherwise, deselect
		var mainView = this;
		if (_.isEqual(tableModel, this.model)) {
			this.$el.find("tbody tr").each(function() {
				var $row = $(this);
				var $input = IDT.getInputForRow($row);
				var saveValue = IDT.getSaveValue($input);
				if (mainView.model.isSelectedSaveVal(saveValue)) {
					mainView.setRowSelected($row);
					$input.prop("checked", true);
				}
				else {
					mainView.setRowDeselected($row);
					$input.prop("checked", false);
				}
			});
		}
	},
	
	/**
	 * Responds to additions in the selectedRows collection
	 * 
	 * @param model the model that was acted upon (added or removed)
	 * @param collection the collection that was acted upon
	 */
	onAddSelectedRow : function(model, collection) {
		this.select(model);
		this.updateSelectedItemCount();
		this.determineSelectAllIcon();
	},
	
	/**
	 * Responds to removals in the selectedRows collection
	 * 
	 * @param model the model that was acted upon (added or removed)
	 * @param collection the collection that was acted upon
	 */
	onRemoveSelectedRow : function(model, collection) {
		this.deselect(model);
		this.updateSelectedItemCount();
		this.determineSelectAllIcon();
	},
	
	/**
	 * Responds to additions to the rows collection
	 * 
	 * @param model the model that was acted upon (added or removed)
	 * @param collection the collection that was acted upon
	 */
	onAddRow : function(model, collection) {
		if (this.model.get("rendered") === true) {
			var dt = this.model.get("datatable");
			dt.fnAddData(model.get("data"));
		}
	},
	
	/**
	 * Responds to removals in the rows collection
	 * 
	 * @param model the model that was acted upon (added or removed)
	 * @param collection the collection that was acted upon
	 */
	onRemoveRow : function(model, collection) {
		if (this.model.get("rendered") === true) {
			var dt = this.model.get("datatable");
			dt.fnDeleteRow(model.get("index"));
		}
	},
	
	select : function(rowModel) {
		var $row = this.getRowJqueryBySaveValue(rowModel.get("saveValue"));
		if ($row.length > 0) {
			this.findInputOnRow($row).prop("checked", true);
			this.setRowSelected($row);
		}
	},
	
	deselect : function(rowModel) {
		var $row = this.getRowJqueryBySaveValue(rowModel.get("saveValue"));
		if ($row.length > 0) {
			this.findInputOnRow($row).prop("checked", false);
			this.setRowDeselected($row);
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
	
	/*
	 * The below methods are user-interaction only.  They should not be used for
	 * programmatic control of selected options.  Those methods are in the API
	 * class: IbisDataTables.
	 */
	
	/**
	 * Responds to the user clicking on a selection input checkbox
	 */
	selectCheckbox : function(event) {
		event.stopImmediatePropagation();
		// the target is event.target
		// our process here is to determine if the row was selected previously and
		// if it was, deselect it (via the DataTable function, no other way!)
		// if it isn't, select it
		var $checkbox = $(event.target);
		var saveVal = IDT.getSaveValue($checkbox);
		this.model.toggleRow(saveVal);
	},
	
	/**
	 * Responds to the user clicking on a selection input radio button
	 */
	selectRadio : function(event) {
		event.stopImmediatePropagation();
		// luckily, selecting a radio is as easy as a checkbox thanks to our
		// methods in the model!
		var $radio = $(event.target);
		event.stopImmediatePropagation();
		var saveVal = IDT.getSaveValue($radio);
		this.model.toggleRow(saveVal);
	},
	
	/**
	 * Responds to the user clicking on a row somewhere other than a selection
	 * input.
	 */
	selectRow : function(event) {
		event.stopImmediatePropagation();
		// ...but rows are a bit more difficult because we have to find out if
		// there is an input at all.  boooo
		if (!$(event.target).is("a, input, select, label")) {
			var $input = IDT.getInputForRow($(event.currentTarget));
			if ($input.length > 0) {
				this.model.toggleRow(IDT.getSaveValue($input));
			}
		}
	},
	
	selectAll : function(event) {
		event.stopImmediatePropagation();
		
		if ( !this.model.get("disableSelectAll") ) {
			this.model.selectAll();
		}
	},
	
	selectFiltered : function(event) {
		event.stopImmediatePropagation();
		
		if ( !this.model.get("disableSelectAll") ) {
			var datatable = this.model.get("datatable");
			try {
				var rowData = datatable._('tr', {"filter":"applied"});
				var rowCount = rowData.length;
				var saveValsToSelect = [];
				for (var i = 0; i < rowCount; i++) {
					var row = rowData[i];
					var $input = $(row[0]);
					if ($input.length > 0) {
						saveValsToSelect.push(IDT.getSaveValue($input));
					}
				}
				this.model.selectSet(saveValsToSelect);
			}
			catch(err) {
				// the data is not filtered, so call selectAll
				this.selectAll(event);
			}
		}
	},
	
	selectNone : function(event) {
		event.stopImmediatePropagation();
		
		if ( !this.model.get("disableSelectAll") ) {
			this.model.deselectAll();
		}
	},
	
	disableSingleRow : function(dataTable, row) {
		if (_.isEqual(dataTable, this.model)) {
			// this appears convoluted but we want to get the actual input
			// on the page, not the input from the row model
			var saveValue = row.get("saveValue");
			var $row = this.getRowJqueryBySaveValue(saveValue);
			this.findInputOnRow($row).prop("disabled", true);
		}
	},
	
	enableSingleRow : function(dataTable, row) {
		if (_.isEqual(dataTable, this.model)) {
			// this appears convoluted but we want to get the actual input
			// on the page, not the input from the row model
			var saveValue = row.get("saveValue");
			var $row = this.getRowJqueryBySaveValue(saveValue);
			this.findInputOnRow($row).prop("disabled", false);
		}
	},
	
	/**
	 * Obtains a jquery reference to the specified row.  Only works for
	 * currently-visible rows.  Used for selecting/deselecting rows
	 * 
	 * @return jquery reference to row or empty jquery reference if not found
	 */
	getRowJqueryBySaveValue : function(saveValue) {
		var idOrValue = IDT.cleanSaveVal(saveValue);
		var $input = this.$('input[id="' + idOrValue + '"]');
		if ($input.length > 0) {
			return $input.parents("tr").eq(0);
		}
		$input = this.$('input[value="' + idOrValue + '"]');
		if ($input.length > 0) {
			return $input.parents("tr").eq(0);
		}
		
		// saveValue = row text
		var $output = $();
		this.$("tr").each(function() {
			var $this = $(this);
			var rowText = $this.text();
			if (rowText == saveValue) {
				$output = $this;
				return;
			}
		});
		return $output;
	},
	
	findInputOnRow : function($row) {
		return $row.find("input").eq(0);
	},
	
	
	/* ------ InfoRowManager methods --- */
	initInfoRow : function() {
		this.getExportRow();
		this.placeExportRow();
		this.createSelectedItemsLabel();
	},
	
	getInfoRow : function() {
		return this.$el.parents(".dataTableContainer").find(".dataTables_info");
	},
	
	getSelectedItemsContainer : function() {
		return this.$el.parents(".dataTableContainer").find(".rowCount");
	},
	
	getTotalItemsContainer : function() {
		return this.$el.parents(".dataTableContainer").find(".totalRowCount");
	},
	
	getRowWordContainer : function() {
		return this.$el.parents(".dataTableContainer").find(".rowWord");
	},
	
	placeExportRow : function() {
		var exportRow = this.model.get("exportRow");
		if (exportRow !== null) {
			var $exportDiv = $("<div></div>");
			$exportDiv.css("float", "left").addClass("tableExportAction").html(exportRow);
			var $infoRow = this.getInfoRow();
			if ($infoRow.length > 0) {
				$infoRow.after($exportDiv);
			}
		}
	},
	
	getExportRow : function() {
		var $exportRow = this.$el.find("tr.tableRowAction");
		if ($exportRow.length > 0) {
			this.model.set("exportRow", this.$el.find("td.tableCellAction").html());
		}
		// the export row exists in a table all by itself inside a blank row. Weird but that's struts for ya
		$exportRow.parents("tr").eq(0).remove();
	},
	
	removeExportRow : function() {
		if (this.model.get("exportRow") !== null) {
			this.$el.find("td.tableCellAction").parents("tr").last().remove();
		}
	},
	
	createSelectedItemsLabel : function() {
		if (this.getSelectedItemsContainer().length < 1 && this.model.get("inputType") != "none") {
			var $infoRow = this.getInfoRow();
			var $defaultDiv = $('<div class="rowCountContainer">(<span class="rowCount">0</span> <span class="rowWord">Rows</span> Selected of <span class="totalRowCount">0</span>)</div>');
			$infoRow.after($defaultDiv);
		}
	},
	
	/**
	 * Updates the selected item count in the table's info row
	 * @count the new count to place in the info row
	 */
	updateSelectedItemCount : function() {
		this.createSelectedItemsLabel();
		var count = this.model.selectedRows.length;
		var totalCount = this.model.rows.length;
		var $countContainer = this.getSelectedItemsContainer();
		$countContainer.text(count);
		var $totalCountContainer = this.getTotalItemsContainer();
		$totalCountContainer.text(totalCount);
		this.updateRowWord(count);
	},
	
	getCountSelectedItems : function() {
		return this.getSelectedItemsContainer().text();
	},
	
	updateRowWord : function(count) {
		var word = "Rows";
		if (count == 1) {
			word = "Row";
		}
		this.getRowWordContainer().text(word);
	},
	
	filterTable : function(model, filterText) {
		if (_.isEqual(model, this.model)) {
			model.get("datatable").fnFilter(filterText);
		}
	},
	
	isVisible : function() {
		return this.$el.is(":visible");
	},
	
	/**
	 * Checks every TIMEOUT milliseconds to see if this table has become
	 * visible.  If not, we wait again.  If so, recalculate the
	 * header/footer widths and stop listening.
	 */
	listenForVisible : function() {
		var timeout = 500;
		var view = this;
		
		if (this.isVisible()) {
			IDT.recalculateHeaderFooterWidths(this.$el);
		}
		else {
			setTimeout(function() {
				view.listenForVisible.call(view);
			}, timeout);
		}
	},
	
	deRender : function(model) {
		if (_.isEqual(model, this.model)) {
			this.close();
		}
	},
	
	close : function() {
		EventBus.off("draw:table", this.onDraw, this);
		EventBus.off("derender:table", this.deRender, this);
		EventBus.off("filter:table", this.filterTable, this);
		var dt = this.model.get("datatable");
		dt.fnDestroy();
		DataTableView.__super__.close.call(this);
	},
	
	destroy : function() {
		this.close();
		DataTableView.__super__.destroy.call(this);
	}
});