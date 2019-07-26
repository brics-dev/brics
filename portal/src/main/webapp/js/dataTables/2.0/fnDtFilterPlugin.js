(function($, window, document, undefined) {
	if (!$.exist) {
		$.extend({
			exist: function(elm) {
				if (typeof elm == null) return false;
				if (typeof elm != "object") elm = $(elm);
				return elm.length ? true : false;
			}
		});
		$.fn.extend({
			exist: function() {
				return $.exist($(this));
			}
		});
	}
	;

	$.fn.fnDtFilter = function(opts) {
		// < param = oTable > ====> refers to the element $().dataTable()
		// < param = aData > ======> refers to aocolumn options OBJ

		// @formatter:off
		/**
		 * aocolumn = { type: 'select' or 'checkBox'... containerId : ... }
		 * <param = aName > =====> refers to the name of the select <param =
		 * iColumn > ========> refers to column index. aData[iColumn] <param =
		 * defaultVal > =======> refers to default filter value
		 * aocolumn.defaultVal <param = defaultVal > =======> refers to default
		 * filter value aoColumn.options[j].default
		 */
		// @formatter:on
		var filterData = {};
		var array = [];

		function fnCreateSelectOptions(oTable, options, aName, label, iColumnIndex, bRegex, containerId, defaultVal, callBack) {
			//because IE doesn't support default function parameters, I had to explicitly define them manually
			if (!defaultVal) {defaultVal = "";}
			if(!label) {label = aName;}
			if(!iColumnIndex) {iColumnIndex = 0;}
			var currentFilter = defaultVal;
			var optionId = aName.split(' ').join('_');
			
			var r = '<select class="search_init select_filter form-control" id="' + optionId + '"><option value="" name= "' + aName + '" class="search_init">' + label + '</option>';
			var j = 0;
			var iLength = options.length;
			for (j = 0; j < iLength; j++) {
				if (typeof (options[j]) != 'object') {
					var selected = '';
					if (unescape(options[j]) == currentFilter || unescape(options[j]) == unescape(currentFilter))
						selected = 'selected = "selected"';
					r += '<option ' + selected + ' value="' + unescape(options[j]) + '">' + options[j] + '</option>';
				}
				else {
					var selected = '';
					if (bRegex) {

						if (options[j].value == currentFilter) selected = 'selected = "selected"';
						r += '<option ' + selected + ' name= "' + aName + '" value="' + options[j].value + '">'
										+ options[j].label + '</option>';
					}
					else {
						if (unescape(options[j].value) == currentFilter) selected = 'selected = "selected"';
						r += '<option ' + selected + ' name= "' + aName + '" value="' + unescape(options[j].value) + '">'
										+ options[j].label + '</option>';
					}
				}
			}
			var select = $(r + '</select>');
			if ($('#' + containerId).exist() && containerId != 'undefined') {
				$('#' + containerId).append(select);
			}
			else {
				$('#' + oTable.attr("id") + '_wrapper').prepend(select);
			}
			// containerId.wrapInner('<span class="filter_column filter_select"
			// />');

			filterSelectOptions(select, callBack, aName, currentFilter, iColumnIndex, bRegex);

		}

		function filterSelectOptions(select, callBack, aName, currentFilter, iColumnIndex, bRegex) {
			filterData[aName] = unescape(currentFilter);
			if (typeof callBack == 'function' && callBack != 'undefined') {
				oTable.dataTableExt.afnFiltering.push(function(oSettings, aData, iDataIndex) {
					if (oTable.attr("id") != oSettings.sTableId) { return true; }
					return callBack(oSettings, aData, iDataIndex, filterData);

				});
			}
			if (currentFilter != null && currentFilter != "") {
				oTable.fnFilter(unescape(currentFilter), iColumnIndex);

			}

			// on change of value check if test method is defined if not run the
			// default filter
			select.change(function() {
				// var val = $(this).val();
				var $this = $(this);
				filterData[aName] = $this.val();
				if ($this.val() != "") {
					$("option[value='" + (this.value) + "']", this).attr("selected", true).siblings().removeAttr(
									"selected");
					$this.removeClass("search_init");
				}
				else {
					$this.children().removeAttr('selected');
					$this.addClass("search_init");
				}
				if (typeof callBack == 'function' && callBack != 'undefined') {
					oTable.fnDraw();

				}
				else {
					if (bRegex)
						oTable.fnFilter("^" + $this.val(), iColumnIndex, bRegex);

					else {
						oTable.fnFilter(unescape($this.val()), iColumnIndex, bRegex);
					}
				}
				$(oTable.api().table().node()).trigger("idt:filter", {
					filter: this,
					value: $this.val()
				});
			});

		}

		function createRadioButton(oTable, options, aName, iColumnIndex, bRegex, containerId, defaultValue, callBack) {
			if(!iColumnIndex) {iColumnIndex = 0;}
			var currentFilter = defaultValue;
			var r = '<ul class= "search-filter" id="' + aName + '">';
			var j = 0;
			var iLength = options.length;
			for (j = 0; j < iLength; j++) {
				if (typeof (options[j]) == 'object') {
					var checked = '';
					if (bRegex) {
						// Do not escape values if they are explicitely set to
						// avoid escaping special characters in the regexp
						if (options[j].value == currentFilter) checked = 'checked = "checked"';
						r += '<li><input type=radio ' + checked + '  name="' + aName + '"  value="' + options[j].value
										+ '">' + options[j].label + '</li>';
					}
					else {
						if (escape(options[j].value) == currentFilter) checked = 'checked = "checked"';
						r += '<li><input type=radio ' + checked + '  name="' + aName + '"  value="'
										+ escape(options[j].value) + '">' + options[j].label + '</li>';
					}
				}
			}

			var $input = $(r + '</ul>');
			$('#' + containerId).append($input);
			if (currentFilter != null && currentFilter != "") {
				filterData[aName] = currentFilter;
				$.fn.dataTableExt.afnFiltering.push(function(oSettings, aData, iDataIndex) {
					if (oTable.attr("id") != oSettings.sTableId) { return true; }
					return callBack(oSettings, aData, iDataIndex, filterData);
				});

			}

			$($input).children('li').on("change", "input[name=" + aName + "]:radio", function(e) {
				filterData[aName] = $(this).val();
				$(this).attr('checked', true);
				$("input[name=" + aName + "]:not(:checked)").removeAttr('checked');
				oTable.fnDraw();
				// delete the filtered data if we need the original one
				// $.fn.dataTableExt.afnFiltering.pop();

			});

		}

		function createCheckBox(oTable, value, label, aName, iColumnIndex, bRegex, containerId, defaultValue, callBack) {
			if(!iColumnIndex) {iColumnIndex = 0;}
			var currentFilter = defaultValue;
			var uniqueId = oTable.attr("id");
			var r = '<label id="' + aName + '">';
			var checked = '';
			if (bRegex) {
				// Do not escape values if they are explicitely set to avoid
				// escaping special characters in the regexp
				if (value == currentFilter) checked = 'checked = "checked"';
				r += '<input type=checkbox ' + checked + '  name="' + aName + '"  value="' + value + '">' + label;
			}
			else {
				if (escape(value) == currentFilter) checked = 'checked = "checked"';
				r += '<input type=checkbox ' + checked + '  name="' + aName + '"  value="' + escape(value) + '">'
								+ label;
			}

			var $input = $(r + '</label>');
			$('#' + containerId).append($input);
			var checkbox = $input.find("input[name=" + aName + "]:checkbox");

			if (currentFilter != null && currentFilter != "") {
				filterData[aName] = currentFilter;
				$.fn.dataTableExt.afnFiltering.push(function(oSettings, aData, iDataIndex) {
					if (oTable.attr("id") != oSettings.sTableId) { return true; }
					return callBack(oSettings, aData, iDataIndex, filterData);
				});

			}

			$($input).on("click", "input[name=" + aName + "]:checkbox", function(e) {

				if (!(this.checked)) {
					$(this).removeAttr('checked');
					delete filterData[aName];
				}
				else {
					filterData[aName] = $(this).val();
					$(this).attr('checked', true);
				}
				oTable.fnDraw();
				// delete the filtered data if we need the original one
				// $.fn.dataTableExt.afnFiltering.pop();

			});

		}
		/**
		 * aocolumn : { type:'select', columnIndex: 1, containerId: '#', name:
		 * '', bRegex: true or false, defaultValue: '', options: [{value: '',
		 * label: ''}] or ['test1', 'test2'] }
		 */
		var oTable = this;
		var defaults = {
			aoColumns: null
		};
		var properties = $.extend(defaults, opts);

		if ((properties.aoColumns !== null || properties.aoColumns !== 'undefined')
						&& Array.isArray(properties.aoColumns)) {

			return (properties.aoColumns).forEach(function(aocolumn) {
				if (oTable.fnSettings().oFeatures.bServerSide == true) {
					var settings = oTable.fnSettings();

					$(oTable[0]).on('preXhr.dt', function(e, settings, data) {
						settings.clearCache = true;
						var filter = [];
						Object.keys(filterData).forEach(function(key, index) {
							filter.push({
								name: key,
								value: filterData[key]
							})

						});

						$.extend(data, {
							filter: filter
						});
					});

				}

				if (aocolumn == null) { return; }
				switch (aocolumn.type) {
				case 'null':
					break;

				case 'select':
					if (!aocolumn.bRegex) {
						aocolumn.bRegex = false;
					}
					fnCreateSelectOptions(oTable, aocolumn.options, aocolumn.name, aocolumn.label, aocolumn.columnIndex,
									aocolumn.bRegex, aocolumn.containerId, aocolumn.defaultValue, aocolumn.test);
					break;

				case 'radio':
					createRadioButton(oTable, aocolumn.options, aocolumn.name, aocolumn.columnIndex, aocolumn.bRegex,
									aocolumn.containerId, aocolumn.defaultValue, aocolumn.test);
					break;

				case 'checkbox':
					createCheckBox(oTable, aocolumn.value, aocolumn.label, aocolumn.name, aocolumn.columnIndex,
									aocolumn.bRegex, aocolumn.containerId, aocolumn.defaultValue, aocolumn.test);
					break;

				}
			});

		}
		else {
			return;
		}

	}
})(jQuery, window, document);
