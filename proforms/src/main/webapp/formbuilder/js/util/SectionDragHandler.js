/**
 * Performs operations necessary to maintain proper positioning, size,
 * and data for dragging and sorting sections in the editor.
 * 
 * JP NOTE: The way this works is not ideal but is the only way that I have 
 * found TO WORK so far.  I'll improve it when I can.  Also, oh gosh I wish
 * jqueryUI had a better way of doing this!
 */
var SectionDragHandler = {
	/**
	 * Current dragging intersection section/row
	 */
	$intersects : null,
	/**
	 * Reference to the sortable object once it has been initialized
	 */
	instance : null,
	/**
	 * Is a section being currently dragged?
	 */
	moving : false,
	
	/**
	 * Sets up the sortable.  This handles the initialization of the sortable
	 * widgets and such.  
	 */
	init : function() {
		EventBus.on("resize:section", this.onSectionResize, this);
		EventBus.on("disable:sectionSortable", this.disable, this);
		EventBus.on("enable:sectionSortable", this.enable, this);
		EventBus.on("expand:section", this.expandSection, this);
		EventBus.on("collapse:section", this.collapseSection, this);
		
		// because of CRIT-2055 and the requirements around more closely mapping the form builder to the form structure
		// we no longer want to drag/drop sections.  Disable that functionality.
/*		SectionDragHandler.instance = Config.containerDiv.sortable({
			items: "." + Config.identifiers.section,
			start : function(event, ui) {
				SectionDragHandler.moving = true;
				SectionDragHandler.setupIntersections(ui.item);
				SectionDragHandler.$intersects = SectionDragHandler.getSectionsOnRow(ui.item);
				EventBus.trigger("change:activeSection", FormBuilder.form.getSectionByDivId(ui.item.attr("id")));
			},
			stop : function(event, ui) {
				SectionDragHandler.moving = false;
				//SectionDragHandler.$intersects = null;
				SectionDragHandler.enforceRowHeight(ui.item.add(SectionDragHandler.$intersects));
			},
			change : function(event, ui) {
				SectionDragHandler.setupIntersections(ui.item);
			},
			sort : function(event, ui) {
				ui.item.css("top", event.pageY - 20 + "px");
				ui.item.css("left", event.pageX - 20 + "px");
				SectionDragHandler.decideUpdateRows(ui.item);
			}
		}).disableSelection();
*/
	},
	
	/**
	 * After sortable is set up, we have to completely destroy it and then
	 * set it up again to make it work correctly when adding new sections
	 */
	destroy : function() {
		EventBus.off("resize:section", this.onSectionResize, this);
		EventBus.off("disable:sectionSortable", this.disable, this);
		EventBus.off("enable:sectionSortable", this.enable, this);
		EventBus.off("expand:section", this.expandSection, this);
		EventBus.off("collapse:section", this.collapseSection, this);
		/*Config.containerDiv.sortable("destroy");*/
	},
	
	disable : function() {
		SectionDragHandler.instance.sortable("disable");
	},
	
	enable : function() {
		SectionDragHandler.instance.sortable("enable");
	},
	
	/**
	 * Destroys the sortable and then re-initializes it
	 */
	refresh : function() {
		this.destroy();
		this.init();
	},
	
	/**
	 * Responds to a section 
	 * @param sectionModel
	 */
	expandSection : function(sectionModel) {
		// get section div
		var $section = $("#"+sectionModel.get("divId"));
		
		// get sections on row (if any)
		var $row = SectionDragHandler.getSectionsOnRow($section);
		
		// collapse all sections on that row
		$row.each(function() {
			// get model for section div
			var rowSectionModel = FormBuilder.form.getSectionByDivId($(this).attr("id"));
			
			// make sure we actually need to update
			if (rowSectionModel.get("editorCollapsed") === true) {
				rowSectionModel.set("editorCollapsed", false);
			}
		});
	},
	
	collapseSection : function(sectionModel) {
		// get section div
		var $section = $("#"+sectionModel.get("divId"));
		
		// get sections on row (if any)
		var $row = SectionDragHandler.getSectionsOnRow($section);
		
		// collapse all sections on that row
		$row.each(function() {
			// get model for section div
			var rowSectionModel = FormBuilder.form.getSectionByDivId($(this).attr("id"));
			
			// make sure we actually need to update
			if (rowSectionModel.get("editorCollapsed") === false) {
				rowSectionModel.set("editorCollapsed", true);
			}
		});
	},
	
	/**
	 * On any section resize event, we need to process resizing not only this
	 * section but all others in the row.
	 * 
	 * @param sectionModel the section model that has changed
	 */
	onSectionResize : function(sectionModel) {
		if (!sectionModel.isRepeatableChild()) {
			var $section = $("#"+sectionModel.get("divId"));
			SectionDragHandler.enforceHeightSection($section);
		}
	},
	
	refreshRows : function() {
		// effectively: "for each row of sections"
		var $processedSections = $();
		$(".section:visible").each(function() {
			var $section = $(this);
			if (!$processedSections.is($section)) {
				var $row = SectionDragHandler.getSectionsOnRow($section);
				SectionDragHandler.equalWidth($row);
				$processedSections = $processedSections.add($row);
				SectionDragHandler.enforceHeightSection($section);
			}
		});
	},
	
	/**
	 * Gets all sections on the same row as $section
	 * 
	 * @param $section a "seed" section to find the others
	 * @return jquery object containing all sections in the row
	 */
	getSectionsOnRow : function($section) {
		var $row = $section;
		var countInCurrentRow = SectionDragHandler.getCountInCurrentRow($section);
		if (countInCurrentRow == 1) {
			return $section;
		}
		var positionInRow = SectionDragHandler.getPositionInRow($section);
		
		// from position back to 0 and position forward to length - 1
		if (positionInRow > 0) {
			$row = $row.add(SectionDragHandler.getPreviousNElements($section, positionInRow));
		}
		
		var rowLengthMinusOne = countInCurrentRow - 1;
		if (positionInRow != rowLengthMinusOne) {
			$row = $row.add(SectionDragHandler.getNextNElements($section, rowLengthMinusOne - positionInRow));
		}
		
		return $row;
	},
	
	/**
	 * Calculates if two sections are on the "same row" on the page.  This
	 * function only accepts jQuery objects so repeatable children cannot be
	 * considered here (even though repeatable children should never be able to
	 * be on the same row as another anyway).
	 * 
	 * @param $sectionOne jQuery object representing the first section
	 * @param $sectionTwo jQuery object representing the second section
	 * @returns {Boolean} true if the two are on the same horizontal row, otherwise false
	 */
	areSectionsOnSameRow : function($sectionOne, $sectionTwo) {
		var topOne = Math.floor($sectionOne.position().top);
		var topTwo = Math.floor($sectionTwo.position().top); ;
		if (Math.abs(topOne - topTwo) < 2 * Config.sectionDragTolerance) {
			return true;
		}
		return false;
	},
	
	/**
	 * Decides if, based on position of the dragging section, this section or
	 * any others need to be updated.
	 * 
	 * Decides whether we are entering or leaving a row based on number of 
	 * sections in the current row as opposed to the number in the previous
	 * row (as calculated in start() OR when changing rows here)
	 * 
	 * This function is called from the movement listener in init
	 */
	decideUpdateRows : function($section) {
		if (SectionDragHandler.moving) {
			var $previousRow = SectionDragHandler.$intersects;
			var $currentRow = SectionDragHandler.getSectionsOnRow($section);
			var sectionModel = FormBuilder.form.getSectionByDivId($section.attr("id"));
			if (sectionModel && !sectionModel.get("isRepeatable")) {
				if ($previousRow.length == 1 && $currentRow.length > 1) {
					// newly intersected $intersectRow
					SectionDragHandler.sectionEnter($section, $currentRow);
					SectionDragHandler.$intersects = $currentRow;
					// NOTE: through testing, a section can't be dragged directly from one row
					// to another without leaving the first because of the padding.
				}
				else if ($previousRow.length > 1 && $currentRow.length == 1) {
					// left intersect with $intersects
					SectionDragHandler.sectionLeave($section, $previousRow);
					SectionDragHandler.$intersects = $currentRow;
				}
				// else either intersecting the same row 
				// or not intersecting and not changing
			}
		}
	},
	
	/**
	 * Responds to a section entering a row
	 * 
	 * @param $draggingSection the dragging section div
	 * @param $enterRow the row being entered
	 */
	sectionEnter : function($draggingSection, $enterRow) {
		var idrow = "";
		$enterRow.each(function() {
			idrow += " " + $(this).attr("id");
		});
		var $allSections = $enterRow.add($draggingSection);
		$allSections.addClass("rowHighlight");
		setTimeout(function(){$allSections.removeClass("rowHighlight");}, 2000);
		SectionDragHandler.$intersects = $allSections;
		SectionDragHandler.equalWidth($allSections);
		SectionDragHandler.enforceRowHeight($allSections);
	},
	
	/**
	 * Responds to a section leaving a row
	 * 
	 * @param $draggingSection the dragging section div
	 * @param $leaveRow the row being left
	 */
	sectionLeave : function($draggingSection, $leaveRow) {
		var idrow = "";
		$leaveRow.each(function() {
			idrow += " " + $(this).attr("id");
		});
		SectionDragHandler.$intersects = null;
		SectionDragHandler.equalWidth($leaveRow.not($draggingSection));
		SectionDragHandler.enforceRowHeight($leaveRow);
		SectionDragHandler.fullWidth($draggingSection);
		
	},
	
	/**
	 * Removes any bootstrap css classes that define spacing/sizing
	 * @param $elements
	 */
	resetSpacing : function($elements) {
		$elements.removeClass("formGrid-1 formGrid-2 formGrid-3 formGrid-4 formGrid-5 formGrid-6 formGrid-7 formGrid-8 formGrid-9 formGrid-10 formGrid-11 formGrid-12");
	},
	
	/**
	 * Resizes all sections in the given $row to the same width
	 * 
	 * @param $row jQuery object with one or more sections
	 */
	equalWidth : function($row) {
		var length = $row.length;
		if (length > Config.maxColumns) {
			length = Config.maxColumns;
		}
		this.resetSpacing($row);
		$row.addClass("formGrid-"+length);
	},
	
	fullWidth : function($section) {
		this.resetSpacing($section);
		$section.addClass("formGrid-1");
	},
	
	/**
	 * Ensures all sections in a row are the same height: the height of the
	 * tallest section in that row.
	 * 
	 * @see stackoverflow answer at http://stackoverflow.com/questions/6060992/element-with-the-max-height-from-a-set-of-elements
	 * 
	 * @param $row the row to set heights for
	 */
	enforceRowHeight : function($row) {
		$row.css("height", "auto");
		var maxHeight = 0;
		$row.each(function() {
			var height = $(this).height();
			if (height > maxHeight) {
				maxHeight = height;
			}
		});
		$row.height(maxHeight);
	},
	
	/**
	 * Ensures all sections in a row are the same height: the height of the
	 * tallest section in that row.
	 * 
	 * @param $section the section to check
	 */
	enforceHeightSection : function($section) {
		var $row = this.getSectionsOnRow($section);
		this.enforceRowHeight($row);
	},
	
	/**
	 * The section is being deleted, so we must re-arrange the other sections in that row (if any).
	 * 
	 * NOTE: THIS NEEDS THE SECTION TO STILL BE ON THE PAGE TO WORK
	 * 
	 * @param sectionModel the section model being deleted
	 */
	refreshBeforeDeleteSection : function(sectionModel) {
		var $section = SectionDragHandler.getDiv(sectionModel);
		var currentRowLength = SectionDragHandler.getCountInCurrentRow($section);
		if (currentRowLength > 1) {
			var $row = SectionDragHandler.getSectionsOnRow($section);
			$row = $row.not($section);
			SectionDragHandler.equalWidth($row);
		}
	},
	
	/**
	 * Upon Edit (process), assign the correct positioning classes to all
	 * of the sections on the form.
	 */
	assignPositions : function() {
		// this is a section Model array
		var sectionsArray = FormBuilder.form.getSectionsInPageOrder();
		
		// we know the sections are in order, so work BACKWARD
		for (var i = sectionsArray.length-1; i >= 0; i--) {
			// we only need to do anything if this col is > 1
			if (sectionsArray[i].get("col") > 1) {
				// get the previous sectionsArray[i].col-1 sections and size them all
				var arrRow = new Array();
				do {
					arrRow.push(sectionsArray[i]);
					i--;
				} while (sectionsArray[i+1].get("col") > 1);
				
				i++; // correct for the last i--
				
				//var eachWidth = ((containerWidth - 2 - (12 * arrRow.length)) / arrRow.length);
				for (var j = 0; j < arrRow.length; j++) {
					//$("#"+arrRow[j].id).css("width", eachWidth+"px");
					$("#" + arrRow[j].get("divId")).addClass("formGrid-"+arrRow.length);
				}
			}
		}
		
	},
	
	/* start of layout editor utils */
	
	getDiv : function(model) {
		var $section = $("#" + model.get("divId"));
		if ($section.length > 0) {
			return $section;
		}
	},
	
	/**
	 * Uses the section div classes to count the number of sections on the same row as the provided sectiondiv.
	 * 
	 * @param $div div the row to count
	 * @returns {Number} number of sections on this row
	 */
	getFormGridClassCount : function($div) {
		if ($div.length == 0) {
			return 0;
		}
		else if ($div.hasClass("formGrid-1")) {
			return 1;
		}
		else if ($div.hasClass("formGrid-2")) {
			return 2;
		}
		else if ($div.hasClass("formGrid-3")) {
			return 3;
		}
		else if ($div.hasClass("formGrid-4")) {
			return 4;
		}
		else if ($div.hasClass("formGrid-5")) {
			return 5;
		}
		else if ($div.hasClass("formGrid-6")) {
			return 6;
		}
		else if ($div.hasClass("formGrid-7")) {
			return 7;
		}
		else if ($div.hasClass("formGrid-8")) {
			return 8;
		}
		else if ($div.hasClass("formGrid-9")) {
			return 9;
		}
		else if ($div.hasClass("formGrid-10")) {
			return 10;
		}
		else if ($div.hasClass("formGrid-11")) {
			return 11;
		}
		else if ($div.hasClass("formGrid-12")) {
			return 12;
		}
	},
	
	/**
	 * Counts the number of sections in the current section model's row.
	 * 
	 * @param element the section model to check OR jQuery reference to the section
	 * @returns (Number) number of sections in the current row
	 */
	getCountInCurrentRow : function(element) {
		if (!(element instanceof jQuery)) {
			element = this.getDiv(element);
		}
		return SectionDragHandler.getFormGridClassCount(element);
	},
	
	/**
	 * Counts the number of sections in the section's previous row.
	 * 
	 * @param element the section model to start search on OR jQuery reference to the section
	 * @return (Number) number of sections in the previous row
	 */
	getCountInPreviousRow : function(element) {
		// this function is long and looks complicated but really the only complex processing here is
		// getting the index of the section in the current row
		if (!(element instanceof jQuery)) {
			element = this.getDiv(element);
		}

		// get the last section in the previous row - use the grid class to count
		var indexInRow = SectionDragHandler.getPositionInRow(element);
		
		// we want to go to the beginning of this row, then one more
		for (var i = 0; i < indexInRow + 1; i++) {
			element = element.prev("." + Config.identifiers.section);
			if (element.length == 0) {
				return 0;
			}
		}

		return SectionDragHandler.getFormGridClassCount(element);		
	},
	
	/**
	 * Counts the number of sections in the section's next row.
	 * 
	 * @param element the section model to start search on
	 * 
	 * @return (Number) number of sections in the next row
	 */
	getCountInNextRow : function(element) {
		// this function is long and looks complicated but really the only complex processing here is
		// getting the index of the section in the current row
		if (!(element instanceof jQuery)) {
			element = this.getDiv(element);
		}
		var $nextAll = element.nextAll("." + Config.identifiers.section);
		// note: if the list of next is empty, then this is the last section
		var nextLength = $nextAll.length;
		if (nextLength == 0) {
			return 0;
		}
		// get the first section in the next row - use the grid class to count
		var indexInRow = SectionDragHandler.getPositionInRow(element);
		var currentRowLength = SectionDragHandler.getFormGridClassCount(element);
		
		// get the nextIndexInAll'th section in the set of nextAll
		var nextIndexInAll = currentRowLength - indexInRow - 1;
		
		var $lastInNextRow = $nextAll.eq(nextIndexInAll);
		return SectionDragHandler.getFormGridClassCount($lastInNextRow);		
	},
	
	/**
	 * Gets this section's position within the row
	 * 
	 * Index 0 = first, increments from left after that
	 * 
	 * @param element the section model to check OR jQuery reference to the section
	 * @return indexed position within the section's row
	 */
	getPositionInRow : function(element) {
		if (!(element instanceof jQuery)) {
			element = this.getDiv(element);
		}
		
		var index = 0;
		if (this.getFormGridClassCount(element) > 1) {
			// we count to the left since 1 section to the left = index of 1
			// and 2 sections to the left = index of 2
			while (true) {
				var $previous = element.prev("." + Config.identifiers.section);
				if ($previous.length < 1 || !this.areSectionsOnSameRow(element, $previous)) {
					break;
				}
				index++;
				element = $previous;
			}
		}
		return index;
	},
	
	getPreviousNElements : function(element, numberToGet) {
		if (!(element instanceof jQuery)) {
			element = this.getDiv(element);
		}
		var $list = null;
		
		for (var i = 0; i < numberToGet; i++) {
			element = element.prev("." + Config.identifiers.section);
			if (element.length < 1) {
				break;
			}
			
			// jquery can't add to a zero-length list so we have to replace
			// do I need a clone here?
			if ($list == null) {
				$list = element;
			}
			else {
				$list = $list.add(element);
			}
		}
		
		return $list;
	},
	
	getNextNElements : function(element, numberToGet) {
		if (!(element instanceof jQuery)) {
			element = this.getDiv(element);
		}
		var $list = null;
		
		for (var i = 0; i < numberToGet; i++) {
			element = element.next();
			if (element.length < 1) {
				break;
			}

			// jquery can't add to a zero-length list so we have to replace
			// do I need a clone here?
			if ($list == null) {
				$list = element;
			}
			else {
				$list = $list.add(element);
			}
		}
		
		return $list;
	}
};