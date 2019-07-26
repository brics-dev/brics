/**
 * Provides the Select All, Select None, and Select Filtered widget addon to the
 * Ibis DataTable (IDT) table. Renders the control and enables its actions. Only
 * draws the control if the table is multi-select and there are no
 * configurations saying it shouldn't be rendered. NOTE: variable thisFileName
 * should be changed if this file is renamed
 */
(function($, window, document, undefined) {
	var thisFileName = "idtSelectSetPlugin.js";

	// main function here
	$.fn.dataTable.SelectSet = function(table, opts) {
		// private methods here

		/**
		 * Determines if the table is selectable in any way.
		 * 
		 * @return boolean true if the table is selectable, otherwise false
		 */
		function isSelectable() {
			// options for settings.select: "multi", "single", false, or not set
			// confused? @see
			// https://stackoverflow.com/questions/784929/what-is-the-not-not-operator-in-javascript
			return !!settings.select;
		}

		/**
		 * Determines if the table is multi-selectable
		 * 
		 * @return boolean true if the table is multi-selectable, otherwise
		 *         false
		 */
		function isMultiSelect() {
			return isSelectable() && settings.selectParams.mode == "multi";
		}

		/**
		 * Determines if Select All should respond to clicks
		 */
		function shouldAllowSelectAll() {
			return isWidgetVisible && settings.selectParams.selectAllEnabled;
		}

		/**
		 * Determines if Select None should respond to clicks
		 */
		function shouldAllowSelectNone() {
			return isWidgetVisible && settings.selectParams.selectNoneEnabled;
		}

		/**
		 * Determines if Select Filtered should respond to clicks
		 */
		function shouldAllowSelectFiltered() {
			return isWidgetVisible && settings.selectParams.selectFilteredEnabled;
		}

		/**
		 * Renders the control and registers the event click handlers
		 */
		function render() {
			var $wrapper = $(api.table().node()).parent();
			var $columnHeader = $wrapper.find("th:first-child");
			var baseUrl = getWidgetBaseDirectory();
			if (baseUrl !== "") {
				var templateUrl = baseUrl + "templates/selectSet.hbs";
				TemplateManager.loadNewAsync("selectSet", templateUrl, function(template) {
					$columnHeader.prepend(template({}));
					$widget = $columnHeader.find(".idt_selectAllSelector");
					registerEvents();
				});
			}
			// else, don't try to render because you'll fail
		}

		/**
		 * Registers the event handlers including open/close the menu and all
		 * sub-menu item click handlers.
		 */
		function registerEvents() {
			$widget.find(".idt_selectAllHeader").on("click", openCloseDropdownCallback);
			if (settings.serverSide) {
				$widget.find(".idt_selectAll").on("click", selectAllServerCallback);
			}
			else {
				$widget.find(".idt_selectAll").on("click", selectAllCallback);
			}
			$widget.find(".idt_selectFilter").on("click", selectFilteredCallback);
			$widget.find(".idt_selectNone").on("click", selectNoneCallback);

			$(api.table().node()).parent();
		}

		this.onSelect = function(selectedRowsLength) {
			if ($widget) {
				var fullLength = api.settings()[0].fnRecordsTotal();
				if (selectedRowsLength === 0) {
					$widget.removeClass("idt_selectAll").removeClass("idt_selectFilter").addClass("idt_selectNone");
				}
				else if (selectedRowsLength == fullLength) {
					$widget.removeClass("idt_selectFilter").removeClass("idt_selectNone").addClass("idt_selectAll");
				}
				else {
					$widget.removeClass("idt_selectAll").removeClass("idt_selectNone").addClass("idt_selectFilter");
				}
			}
		};

		function openCloseDropdownCallback(event) {
			event.stopPropagation();
			// I don't use toggle here because we still have the hover
			// controlled
			// in CSS
			var $menu = $widget.find(".idt_selectAllMenu");
			// native JS because jquery doesn't have this
			if ($menu[0].hasAttribute("style")) {
				$menu.removeAttr("style");
			}
			else {
				$menu.css("display", "block");
			}
		}

		function selectAllCallback(event) {
			event.stopPropagation();
			if (shouldAllowSelectAll()) {
				api.rows(function(idx, data, node) {
					return !data.selectionDisable;
				}).select();
			}
			/*check the checkbox*/
			$widget.find(".idt_selectNone").removeClass("idt_selectNone").addClass("idt_selectAll");
			/*uncheck the checkbox in selectAll*/
			$widget.find(".idt_selectAll").each(function(){
				if($(this).text().trim()!="Select All"){
					$(this).removeClass("idt_selectAll").addClass("idt_selectNone");
				}
			});
			/*uncheck the checkbox in selectFiltered*/
			$widget.find(".idt_selectFilter").removeClass("idt_selectFilter").addClass("idt_selectNone");
		}

		function selectAllServerCallback(event) {
			event.stopPropagation();
			var url = settings.idtUrl;
			var totalRecordCount = api.settings()[0].fnRecordsTotal();

			var data = {
				start: 0,
				length: totalRecordCount,
				columns: [{
					parameter: settings.idtData.primaryKey,
					data: "id"
				}]
			}

			$.ajax({
				type: "GET",
				url: url,
				dataType: "json",
				data: data,
				success: function(json) {
					// I really wish I didn't have to modify this here
					opts.selected = json.data.map(function(row) {
						api.row("#" + row.id).select();
						return row.id;
					});

					api.rows(function(idx, data, node) {
						return !data.selectionDisable;
					}).select().draw(false);
					// this.onSelect(totalRecordCount);
				}
			});
		}

		function selectNoneCallback(event) {
			event.stopPropagation();
			if (shouldAllowSelectNone()) {
				api.rows().deselect();
				// ugh
				opts.selected = [];
			}
			/*check the checkbox*/
			$widget.find(".idt_selectNone").removeClass("idt_selectNone").addClass("idt_selectAll");
			/*uncheck the checkbox in selectAll*/
			$widget.find(".idt_selectAll").each(function(){
				if($(this).text().trim()!="Select None"){
					$(this).removeClass("idt_selectAll").addClass("idt_selectNone");
				}
			});
			/*uncheck the checkbox in selectFiltered*/
			$widget.find(".idt_selectFilter").removeClass("idt_selectFilter").addClass("idt_selectNone");
		}

		function selectFilteredCallback(event) {
			event.stopPropagation();
			if (shouldAllowSelectFiltered()) {
				api.rows(":not(.idtSelectionDisabled)", {
					search: 'applied'
				}).select();
			}
			$widget.find(".idt_selectAllItem").each(function(){
				if($(this).text().trim()!="Select Filtered"){
					$(this).removeClass("idt_selectAll").addClass("idt_selectNone");
				} else {
					$(this).removeClass("idt_selectAll").removeClass("idt_selectNone").addClass("idt_selectFilter");
				}
			})
		}

		/**
		 * Gets the base directory of this plugin. This is used by other places
		 * in this plugin to reference other needed files. This is done by
		 * finding the script tag for THIS FILE on the containing page and
		 * getting the source attribute for that tag and removing this file
		 * name.
		 * 
		 * @return base directory (.../datatablesWidget) or empty string if not
		 *         found
		 */
		function getWidgetBaseDirectory() {
			var $thisScriptTag = $('script[src*="' + thisFileName + '"]');
			if ($thisScriptTag.length) {
				// will output something like //domain.tld/datatablesWidget/
				return $thisScriptTag.attr("src").replace(thisFileName, "");
			}
			else {
				return "";
			}
		}

		// private properties here
		// jquery reference to this widget after rendering
		var $widget = null;
		// API instance of this table
		var api = new $.fn.dataTable.Api(table);
		// is the widget visible?
		var isWidgetVisible = false;
		// has the widget been initialized?
		var isInitialized = false;
		// is this table fed by server data or local data?
		var isServerData = false;

		// default configuration
		var defaults = {
			selectParams: {
				selectAllEnabled: true,
				selectNoneEnabled: true,
				selectFilteredEnabled: true,
				mode: "multi"
			}
		};

		// code to run immediately here, this is the body of the main function

		var tmpOpts = opts || {};
		var settings = $.extend({}, defaults, tmpOpts);
		settings.selectParams.mode = tmpOpts.select.style;

		// it doesn't make sense to show select all if the table isn't
		// multiselect
		if (isMultiSelect()) {
			render();
			isWidgetVisible = true;
		}
		isInitialized = true;
		return this;
	};
})(jQuery, window, document);

// register to both the capital-D DataTable and little-d dataTable namespaces
$.fn.DataTable.SelectSet = $.fn.dataTable.SelectSet;
