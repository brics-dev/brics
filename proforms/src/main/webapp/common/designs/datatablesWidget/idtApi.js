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

			getSelectedOptions: function() {
				return oTable.settings()[0].oInit.selected;
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
				var selectedList = this.getSelectedOptions();
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
				// TODO: fill in
			},

			disableRow: function(rowReference) {
				var disabledList = oTable.settings()[0].oInit.selectionDisabled;
				var id = rowReference.id();
				if (disabledList.indexOf(id) === -1) {
					disabledList.push(id);
					$(rowReference.node()).addClass("idtSelectionDisabled");
					rowReference.data().selectionDisable = true;
				}
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
				var rows = oTable.rows(rowSelector).select();
			},

			/**
			 * Deselects one or more row in the table.
			 * 
			 * @attr rowSelector a datatables rowSelector
			 * @see https://datatables.net/reference/type/row-selector
			 */
			deselectRow: function(rowSelector) {
				var rows = oTable.rows(rowSelector).deselect();
			},

			getSelected: function() {
				return oTable.settings()[0].oInit.selected;
			},

			isSelected: function(rowSelector) {
				var row = oTable.row(rowSelector);
				return (row) ? $.inArray(row.id(), oTable.settings()[0].oInit.selected) : false;
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
