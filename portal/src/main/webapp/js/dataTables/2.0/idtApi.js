(function($) {
	$.idtApi = $.fn.idtApi = function(method) {

		/* Utility and Object Classes ******************************* */

		/* End Utility and Object Classes ******************************* */
		var oTable = $(this).DataTable();
		var methods = {
			getRows: function() {
				return oTable.rows().data();
			},

			getOptions: function() {
				return oTable.settings()[0].oInit;
			},

			getSettings: function() {
				return oTable.settings()[0];
			},

			getSelectionDisabled: function() {
				return oTable.settings()[0].oInit.selectionDisabled;
			},

			getTableApi: function() {
				return oTable;
			},

			getApiRow: function(reference) {
				return oTable.row(reference);
			},

			removeRow: function(reference) {
				var rows = oTable.rows(reference);
				var selectedList = this.getSelected();
				rows.every(function(rowIdx, tableLoop, rowLoop) {
					var foundIndex = selectedList.indexOf(this.id());
					if (foundIndex > -1) {
						selectedList.splice(foundIndex, 1);
					}
				});

				rows.remove();
				return oTable;
			},

			addRow: function(rowApiObject) {
				return oTable.rows.add(rowApiObject).draw(false);
			},

			disableRow: function(rowReference) {
				var disabledList = oTable.settings()[0].oInit.selectionDisabled;
				rowReference.every(function(rowIdx, tableLoop, rowLoop) {
					var id = this.id();
					if (disabledList.indexOf(id) === -1) {
						disabledList.push(id);
						$(this.node()).addClass("idtSelectionDisabled");
						this.data().selectionDisable = true;
					}
				});

			},

			disableSelection: function() {
				var disabledList = oTable.settings()[0].oInit.selectionDisabled;
				oTable.rows().every(function() {
					var id = this.id();
					if (disabledList.indexOf(id) === -1) {
						disabledList.push(id);
						$(this.node()).addClass("idtSelectionDisabled");
						this.data().selectionDisable = true;
					}
				});
			},

			enableSelection: function() {
				oTable.settings()[0].oInit.selectionDisabled = [];
				oTable.rows().every(function() {
					$(this.node()).removeClass("idtSelectionDisabled");
					this.data().selectionDisable = false;
				});
			},

			enableRow: function(rowReference) {
				var disabledList = oTable.settings()[0].oInit.selectionDisabled;
				var id = rowReference.id();
				var index = disabledList.indexOf(id);
				if (index !== -1) {
					disabledList.splice(index, 1);
					$(rowReference.node()).removeClass("idtSelectionDisabled");
					rowReference.data().selectionDisable = false;
				}
			},

			/**
			 * Selects one or more row in the table.
			 * 
			 * @attr rowSelector a datatables rowSelector
			 * @see https://datatables.net/reference/type/row-selector
			 */
			selectRow: function(rowSelector) {
				return oTable.rows(rowSelector).select();
			},

			/**
			 * Deselects one or more row in the table.
			 * 
			 * @attr rowSelector a datatables rowSelector
			 * @see https://datatables.net/reference/type/row-selector
			 */
			deselectRow: function(rowSelector) {
				return oTable.rows(rowSelector).deselect();
			},

			deselectAll: function() {
				return oTable.rows().deselect();
			},

			/**
			 * Gets a list of all row IDs that are currently selected
			 * 
			 * @return String[] rowIds
			 */
			getSelected: function() {
				return oTable.settings()[0].oInit.selected;
			},

			/**
			 * alias of getSelected()
			 */
			getSelectedOptions: function() {
				return methods.getSelected();
			},

			isSelected: function(rowSelector) {
				var row = oTable.row(rowSelector);
				return (row) ? $.inArray(row.id(), oTable.settings()[0].oInit.selected) : false;
			},

			cellContent: function(headerText, html) {
				var output = [];
				html = html || false;

				// get all column indexes
				var columnSelector = -1;
				var columns = oTable.columns().every(function(index) {
					var header = this.header();
					var text = header.innerText.trim() || header.textContent.trim();
					if (text == headerText) {
						columnSelector = index;
						return;
					}
				});

				var rowSelector = oTable.settings()[0].oInit.selected.map(function(selected) {
					return "#" + selected;
				});
				if (columnSelector > -1 && rowSelector.length > 0) {
					output = oTable.cells(rowSelector, columnSelector).data().toArray().map(function(cell) {
						return (html) ? cell : $("<td>" + cell + "</td>").text();
					});
				}
				return output;
			},

			destroy: function() {
				oTable.destroy();
			},

			draw: function() {
				oTable.draw();
			}
		};

		// ------Method calling logic---------------------------------
		if (methods[method]) {
			return methods[method].apply(this, Array.prototype.slice.call(arguments, 1));
		}
		else if (typeof method === 'object' || !method) {
			return methods.init.apply(this, arguments);
		}
		else {
			$.error('Method ' + method + ' does not exist on jquery.idtApi');
		}
	};

})(jQuery);
