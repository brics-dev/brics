/**
 * Performs operations necessary to maintain proper positioning, size,
 * and data for dragging and sorting questions in the editor.
 * 
 * JP NOTE: The way this works is not ideal but is the only way that I have 
 * found TO WORK so far.  I'll improve it when I can.  Also, oh gosh I wish
 * jqueryUI had a better way of doing this!
 */
var QuestionDragHandler = {
	/**
	 * Current dragging intersection question/row
	 */
	$intersects : null,
	/**
	 * Reference to the sortable object once it has been initialized
	 */
	instance : null,
	/**
	 * Is a question being currently dragged?
	 */
	moving : false,
	
	/**
	 * The question's original section
	 */
	originalSectionDiv : null,
	
	/**
	 * Sets up the sortable.  This handles the initialization of the sortable
	 * widgets and such.  
	 */
	init : function() {
		EventBus.on("resize:question", QuestionDragHandler.onQuestionResize, QuestionDragHandler);
		EventBus.on("resize:section", QuestionDragHandler.refreshRows, QuestionDragHandler);
		EventBus.on("delete:section", QuestionDragHandler.refresh, QuestionDragHandler);
		
		// because of CRIT-2055 and the requirements around more closely mapping the form builder to the form structure
		// we no longer want to drag/drop questions.  Disable that functionality.
/*		QuestionDragHandler.instance = $("." + Config.identifiers.section + " ." + Config.identifiers.questionContainer).sortable({
			items: "." + Config.identifiers.question,
			connectWith: "." + Config.identifiers.questionContainer,
			placeholder: "sectionPlaceholder",
			start : function(event, ui) {
				QuestionDragHandler.originalSectionDiv = ui.item.parents(".section");
				QuestionDragHandler.moving = true;
				
				var $rowparent = ui.item.parents(".row").eq(0);
				ui.item.css("top", (event.originalEvent.pageY - $rowparent.offset().top) - 20 + "px");
				ui.item.css("left", (event.originalEvent.pageX - $rowparent.offset().left) - 20 + "px");
				
				QuestionDragHandler.setupIntersections(ui.item);
				QuestionDragHandler.$intersects = QuestionDragHandler.getQuestionsOnRow(ui.item);
				EventBus.trigger("change:activeQuestion", FormBuilder.form.getQuestionByDivId(ui.item.attr("id")));
			},
			stop : function(event, ui) {
				QuestionDragHandler.moving = false;
				//QuestionDragHandler.$intersects = null;
				var $newSectionDiv = ui.item.parents(".section");
				var oldSection = FormBuilder.form.getSectionByDivId(QuestionDragHandler.originalSectionDiv.attr("id"));
							
				if (!$newSectionDiv.is(QuestionDragHandler.originalSectionDiv)) {
					// get the question model

					var questionModel = FormBuilder.form.getQuestionByDivId(ui.item.attr("id"));
					
					// check that the target doesn't have this question already
					var newSection = FormBuilder.form.getSectionByDivId($newSectionDiv.attr("id"));
					var qmatch = newSection.questions.findWhere({
						questionId: questionModel.get("questionId")
					});
					
					if (typeof qmatch !== "undefined") {
						$(this).sortable('cancel');
						$.ibisMessaging("flash", "warning", "The target section already contains that question");
					}
					else {
						// set the new section to be the active section
						EventBus.trigger("change:activeSection", newSection);
						
						// move the actual question model object to the new section model
						oldSection.questions.remove(questionModel);
						newSection.questions.add(questionModel);
						
						QuestionDragHandler.updateDataElement(oldSection,newSection,questionModel);
						
						QuestionDragHandler.updateRepeatableSection(oldSection,newSection,questionModel);
						// reminder: the div has already moved
						
						questionModel.set("sectionId", newSection.get("id"));
						questionModel.calculateDivId(true);
						
						// don't worry about repeatable sections here
						
						QuestionDragHandler.updateQuestionToSkip(oldSection,newSection,questionModel);
					}
					EventBus.trigger("resize:section", newSection);
				}
				QuestionDragHandler.enforceRowHeight(ui.item.add(QuestionDragHandler.$intersects));
				EventBus.trigger("resize:section", oldSection);
			},
			change : function(event, ui) {
				QuestionDragHandler.setupIntersections(ui.item);
			},
			sort : function(event, ui) {
				// we can't use the page as the base point because .row gives a position:relative
				//var $rowparent = ui.item.parents(".row").eq(0);
				//ui.item.css("top", (event.pageY - $rowparent.offset().top) - 20 + "px");
				//ui.item.css("left", (event.pageX - $rowparent.offset().left) - 20 + "px");
				QuestionDragHandler.decideUpdateRows(ui.item);
			}
		}).disableSelection();
*/
	},
	
	/**
	 * After sortable is set up, we have to completely destroy it and then
	 * set it up again to make it work correctly when adding new questions
	 */
	destroy : function() {
		EventBus.off("resize:question", QuestionDragHandler.onQuestionResize, QuestionDragHandler);
		EventBus.off("resize:section", QuestionDragHandler.refreshRows, QuestionDragHandler);
		EventBus.off("delete:section", QuestionDragHandler.refresh, QuestionDragHandler);
/*		QuestionDragHandler.instance.sortable("destroy");*/
	},
	
	/**
	 * Destroys the sortable and then re-initializes it
	 */
	refresh : function() {
/*		this.refreshInstanceElements();
		if (QuestionDragHandler.instance.data("ui-sortable")) {
			this.destroy();	
		}
		this.init();*/
	},
	
	/**
	 * Reduces the instance element list to only those that actually reside on the page
	 * 
	 * protects against calling destroy when not initialized.
	 * comparison of verification methods at http://jsperf.com/closest-vs-contains
	 * I am using the "document contains element" method because it is the fastest
	 * of the pure jquery options and doesn't require an additional method call
	 */
	refreshInstanceElements : function() {
		QuestionDragHandler.instance = QuestionDragHandler.instance.filter(function() {
			return $(this).parents().is("body");
		});
	},
	
	/**
	 * On any question resize, we need to process resizing not only this
	 * question but all others in the row.
	 * 
	 * @param question the question view that has been changed
	 */
	onQuestionResize : function(questionModel) {
		if (!FormBuilder.page.get("loadingData")) {
			var $question = $("#"+questionModel.get("newQuestionDivId"));
			// make sure we actually found the question div - could be a repeatable child
			if ($question.length > 0) {
				QuestionDragHandler.enforceHeightQuestion($question);
			}
			
			var parentSection = FormBuilder.form.getQuestionParentSection(questionModel);
			if (parentSection != null) {
				EventBus.trigger("resize:section", parentSection);
			}
		}
	},
	
	resizeAllQuestionsSections : function() {
		QuestionDragHandler.resizeAllQuestions();
	},
	
	/**
	 * Step 2 in the initial build phase.  Proceeds to resizeAllSections
	 * after this finishes
	 */
	resizeAllQuestions : function() {
		EventBus.trigger("close:processing");
		EventBus.trigger("open:processing", "Resizing Questions");
		var questions = $(".question");
		var numQuestions = questions.length;
		var j = 0;
		setTimeout(function addSingleQuestion() {
			try {
				QuestionDragHandler.enforceHeightQuestion(questions[j]);
				
				pc = (j / numQuestions) * 100;
				FormBuilder.page.get("processingView").setValueTo(pc);
			}
			catch (e) {
			}

			j++;
			if (j < numQuestions) {
				setTimeout(addSingleQuestion, 0); // timeout loop
			}
			else {
				QuestionDragHandler.resizeAllSections();
			}
		}, 0);
	},
	
	/**
	 * Step 3 in the initial build phase.  Proceeds to finalizeFullResize
	 * after this finishes.
	 */
	resizeAllSections : function() {
		EventBus.trigger("close:processing");
		EventBus.trigger("open:processing", "Resizing Sections");
		var sections = FormBuilder.form.getVisibleSections();
		var numSections = sections.length;
		var k = 0;
		
		setTimeout(function addSingleSection() {
			try {
				EventBus.trigger("resize:section", sections[k]);
				
				pc = (k / numSections) * 100;
				FormBuilder.page.get("processingView").setValueTo(pc);
			}
			catch (e) {
			}

			k++;
			if (k < numSections) {
				setTimeout(addSingleSection, 0); // timeout loop
			}
			else {
				QuestionDragHandler.finalizeFullResize();
			}
		}, 0);
		
	},
	
	/**
	 * Finished with both resizing Questions and Sections.  Finish this
	 * up.
	 */
	finalizeFullResize : function() {
		EventBus.trigger("close:processing");
	},
	
	refreshRows : function() {
		// effectively: "for each row of questions"
		var $processedQuestions = $();
		$(".question:visible").each(function() {
			var $question = $(this);
			if (!$processedQuestions.is($question)) {
				var $row = QuestionDragHandler.getQuestionsOnRow($question);
				QuestionDragHandler.equalWidth($row);
				$processedQuestions = $processedQuestions.add($row);
				QuestionDragHandler.enforceRowHeight($row);
			}
		});
	},
	
	/**
	 * Gets all sections on the same row as $question
	 * 
	 * @param $question a "seed" section to find the others
	 * @return jquery object containing all sections in the row
	 */
	getQuestionsOnRow : function($question) {
		var $row = $question;
		var countInCurrentRow = QuestionDragHandler.getCountInCurrentRow($question);
		if (countInCurrentRow == 1) {
			return $question;
		}
		var positionInRow = QuestionDragHandler.getPositionInRow($question);
		
		// from position back to 0 and position forward to length - 1
		if (positionInRow > 0) {
			$row = $row.add(QuestionDragHandler.getPreviousNElements($question, positionInRow));
		}
		
		var rowLengthMinusOne = countInCurrentRow - 1;
		if (positionInRow != rowLengthMinusOne) {
			var $others = QuestionDragHandler.getNextNElements($question, rowLengthMinusOne - positionInRow);
			$row = $row.add($others);
		}
		
		return $row;
	},
	
	/**
	 * Calculates if two questions are on the "same row" on the page.
	 * 
	 * @param $questionOne jQuery object representing the first question
	 * @param $questionTwo jQuery object representing the second question
	 * @returns {Boolean} true if the two are on the same horizontal row, otherwise false
	 */
	areQuestionsOnSameRow : function($questionOne, $questionTwo) {
		var topOne = Math.floor($questionOne.position().top);
		var topTwo = Math.floor($questionTwo.position().top); ;
		if (Math.abs(topOne - topTwo) < 2 * Config.questionDragTolerance) {
			return true;
		}
		return false;
	},
	
	/**
	 * Decides if, based on position of the dragging question, this question or
	 * any others need to be updated.
	 * 
	 * Decides whether we are entering or leaving a row based on number of 
	 * questions in the current row as opposed to the number in the previous
	 * row (as calculated in start() OR when changing rows here)
	 * 
	 * This function is called from the movement listener in init
	 */
	decideUpdateRows : function($question) {
		if (QuestionDragHandler.moving) {
			var $previousRow = QuestionDragHandler.$intersects;
			var $currentRow = QuestionDragHandler.getQuestionsOnRow($question);
			var questionModel = FormBuilder.form.getQuestionByDivId($question.attr("id"));
			if (questionModel && !questionModel.get("repeatable")) {
				if ($previousRow.length == 1 && $currentRow.length > 1) {
					// newly intersected $intersectRow
					QuestionDragHandler.questionEnter($question, $currentRow);
					QuestionDragHandler.$intersects = $currentRow;
					// NOTE: through testing, a section can't be dragged directly from one row
					// to another without leaving the first because of the padding.
				}
				else if ($previousRow.length > 1 && $currentRow.length == 1) {
					// left intersect with $intersects
					QuestionDragHandler.questionLeave($question, $previousRow);
					QuestionDragHandler.$intersects = $currentRow;
				}
				// else either intersecting the same row 
				// or not intersecting and not changing
			}
		}
	},
	
	/**
	 * Responds to a question entering a row
	 * 
	 * @param $draggingQuestion the dragging question div
	 * @param $enterRow the row being entered
	 */
	questionEnter : function($draggingQuestion, $enterRow) {
		var $allQuestions = $enterRow.add($draggingQuestion);
		$allQuestions.addClass("rowHighlight");
		setTimeout(function(){$allQuestions.removeClass("rowHighlight");}, 2000);
		QuestionDragHandler.$intersects = $allQuestions;
		QuestionDragHandler.equalWidth($allQuestions);
		QuestionDragHandler.enforceRowHeight($allQuestions);
	},
	
	/**
	 * Responds to a question leaving a row
	 * 
	 * @param $draggingQuestion the dragging question div
	 * @param $leaveRow the row being left
	 */
	questionLeave : function($draggingQuestion, $leaveRow) {
		QuestionDragHandler.$intersects = null;
		QuestionDragHandler.equalWidth($leaveRow.not($draggingQuestion));
		QuestionDragHandler.enforceRowHeight($leaveRow);
		QuestionDragHandler.fullWidth($draggingQuestion);
	},
	
	/**
	 * Removes any bootstrap css classes that define spacing/sizing
	 * @param $elements
	 */
	resetSpacing : function($elements) {
		$elements.removeClass("formGrid-1 formGrid-2 formGrid-3 formGrid-4 formGrid-5 formGrid-6 formGrid-7 formGrid-8 formGrid-9 formGrid-10 formGrid-11 formGrid-12");
	},
	
	/**
	 * Resizes all questions in the given $row to the same width
	 * 
	 * @param $row jQuery object with one or more questions
	 */
	equalWidth : function($row) {
		var length = $row.length;
		if (length > Config.maxColumns) {
			length = Config.maxColumns;
		}
		this.resetSpacing($row);
		
		$row.addClass("formGrid-"+length);
	},
	
	fullWidth : function($question) {
		QuestionDragHandler.resetSpacing($question);
		$question.addClass("formGrid-1");
	},
	
	/**
	 * Ensures all questions in a row are the same height: the height of the
	 * tallest question in that row.
	 * 
	 * @see stackoverflow answer at http://stackoverflow.com/questions/6060992/element-with-the-max-height-from-a-set-of-elements
	 * 
	 * @param $row the row to set heights for
	 */
	enforceRowHeight : function($row) {
		$row.css("height", "auto");
		var maxHeight = 0;
		// this is probably not obvious but we reverse so that the LAST question
		// in the row is resized first
		$row.each(function() {
			var height = $(this).height();
			if (height > maxHeight) {
				maxHeight = height;
			}
		});
		//$row.reverse().height(maxHeight);
		$row.reverse().each(function() {
			$(this).height(maxHeight);
		});
	},
	
	/**
	 * Ensures all questions in a row are the same height: the height of the
	 * tallest question in that row.
	 * 
	 * @param $question the question to check
	 */
	enforceHeightQuestion : function($question) {
		var $row = QuestionDragHandler.getQuestionsOnRow($question);
		QuestionDragHandler.enforceRowHeight($row);
	},
	
	//added by Ching Heng
	updateRepeatableSection : function(oldSection,newSection,questionModel){
		if(oldSection.get("isRepeatable")){
			RepeatableSectionProcessor.deleteQuestionInRepeatableSectoin(questionModel,oldSection);
		}
		if(newSection.get("isRepeatable")){
			RepeatableSectionProcessor.createQuestionInRepeatableSectoin(questionModel,newSection);
		}
	},
	
	updateQuestionToSkip : function(oldSection,newSection,questionModel){
		FormBuilder.form.sections.forEach(function(section){
			section.questions.forEach(function(question){
				var questionSkipArray = question.get("questionsToSkip");
				if(questionSkipArray.length>0){
					for(var j=0;j<questionSkipArray.length;j++){
						var waht = oldSection.get("id")+"_Q_"+questionModel.get("questionId");
						if(questionSkipArray[j]==waht){
							questionSkipArray[j]=newSection.get("id")+"_Q_"+questionModel.get("questionId");
						}
					}
					question.set("questionsToSkip",questionSkipArray);
				}
			});
		});
	},
	
	/**
	 * Upon Edit (process), assign the correct positioning classes to all
	 * of the questions on the form.
	 */
	assignPositions : function() {
		var sections = FormBuilder.form.sections;
		sections.forEach(function(section) {
			var questionsArray = section.getQuestionsInPageOrder();
			// we know the questions are in order, so work BACKWARD
			for (var i = questionsArray.length-1; i >= 0; i--) {
				// we only need to do anything if this col is > 1
				if (questionsArray[i].get("questionOrder_col") > 1) {
					// get the previous sectionsArray[i].col-1 sections and size them all
					var arrRow = new Array();
					do {
						arrRow.push(questionsArray[i]);
						i--;
					} while (questionsArray[i+1].get("questionOrder_col") > 1);
					
					i++; // correct for the last i--
					
					//var eachWidth = ((containerWidth - 2 - (12 * arrRow.length)) / arrRow.length);
					for (var j = 0; j < arrRow.length; j++) {
						//$("#"+arrRow[j].id).css("width", eachWidth+"px");
						var $question = $("#" + arrRow[j].get("newQuestionDivId"));
						QuestionDragHandler.resetSpacing($question);
						$question.addClass("formGrid-"+arrRow.length);
					}
				}
			}
		});
		QuestionDragHandler.refreshRows();
	},
	
	updateDataElement : function(oldSection,newSection,questionModel){
		if(oldSection.get("isRepeatable") && newSection.get("isRepeatable")){
			// is still repeatable, must check if rgroup changed
			if (oldSection.get('repeatableGroupName') != newSection.get('repeatableGroupName')) {
				questionModel.set('dataElementName', 'none');
			}
		} else if (oldSection.get("isRepeatable") && !newSection.get("isRepeatable")) {
			questionModel.set('dataElementName', 'none');
		} else if (!oldSection.get("isRepeatable") && newSection.get("isRepeatable")) {
			questionModel.set('dataElementName', 'none');
		}
		else{}
	},
	
	getDiv : function(questionModel) {
		var $question = $("#" + questionModel.get("newQuestionDivId"));
		if ($question.length > 0) {
			return $question;
		}
	},
	
	/**
	 * Uses the question div classes to count the number of questions on the same row as the provided questiondiv.
	 * 
	 * @param $questionDiv div the row to count
	 * @returns {Number} number of questions on this row
	 */
	getFormGridClassCount : function($questionDiv) {
		if ($questionDiv.length == 0) {
			return 0;
		}
		else if ($questionDiv.hasClass("formGrid-1")) {
			return 1;
		}
		else if ($questionDiv.hasClass("formGrid-2")) {
			return 2;
		}
		else if ($questionDiv.hasClass("formGrid-3")) {
			return 3;
		}
		else if ($questionDiv.hasClass("formGrid-4")) {
			return 4;
		}
		else if ($questionDiv.hasClass("formGrid-5")) {
			return 5;
		}
		else if ($questionDiv.hasClass("formGrid-6")) {
			return 6;
		}
		else if ($questionDiv.hasClass("formGrid-7")) {
			return 7;
		}
		else if ($questionDiv.hasClass("formGrid-8")) {
			return 8;
		}
		else if ($questionDiv.hasClass("formGrid-9")) {
			return 9;
		}
		else if ($questionDiv.hasClass("formGrid-10")) {
			return 10;
		}
		else if ($questionDiv.hasClass("formGrid-11")) {
			return 11;
		}
		else if ($questionDiv.hasClass("formGrid-12")) {
			return 12;
		}
	},
	
	/**
	 * Counts the number of questions in the current question model's row.
	 * 
	 * @param questionModel the question model to check OR jQuery reference to the question
	 * @returns (Number) number of questions in the current row
	 */
	getCountInCurrentRow : function(question) {
		if (!(question instanceof jQuery)) {
			question = this.getDiv(question);
		}
		return QuestionDragHandler.getFormGridClassCount(question);
	},
	
	/**
	 * Counts the number of questions in the question's previous row.
	 * 
	 * @param question the question model to start search on OR jQuery reference to the question
	 * @return (Number) number of questions in the previous row
	 */
	getCountInPreviousRow : function(question) {
		// this function is long and looks complicated but really the only complex processing here is
		// getting the index of the question in the current row
		if (!(question instanceof jQuery)) {
			question = this.getDiv(question);
		}

		// get the last question in the previous row - use the grid class to count
		var indexInRow = QuestionDragHandler.getPositionInRow(question);
		
		// we want to go to the beginning of this row, then one more
		for (var i = 0; i < indexInRow + 1; i++) {
			question = question.prev("." + Config.identifiers.question);
			if (question.length == 0) {
				return 0;
			}
		}

		return QuestionDragHandler.getFormGridClassCount(question);		
	},
	
	/**
	 * Counts the number of questions in the question's next row.
	 * 
	 * @param questionModel the question model to start search on
	 * 
	 * @return (Number) number of questions in the next row
	 */
	getCountInNextRow : function(question) {
		// this function is long and looks complicated but really the only complex processing here is
		// getting the index of the question in the current row
		if (!(question instanceof jQuery)) {
			question = this.getDiv(question);
		}
		var $nextAll = question.nextAll("." + Config.identifiers.question);
		// note: if the list of next is empty, then this is the last question in the section
		var nextLength = $nextAll.length;
		if (nextLength == 0) {
			return 0;
		}
		// get the first question in the next row - use the grid class to count
		var indexInRow = QuestionDragHandler.getPositionInRow(question);
		var currentRowLength = QuestionDragHandler.getFormGridClassCount(question);
		
		// get the nextIndexInAll'th question in the set of nextAll
		var nextIndexInAll = currentRowLength - indexInRow - 1;
		
		var $lastInNextRow = $nextAll.eq(nextIndexInAll);
		return QuestionDragHandler.getFormGridClassCount($lastInNextRow);		
	},
	
	/**
	 * Gets this question's position within the row
	 * 
	 * Index 0 = first, increments from left after that
	 * 
	 * @param question the question model to check OR jQuery reference to the question
	 * @return indexed position within the question's row
	 */
	getPositionInRow : function(question) {
		if (!(question instanceof jQuery)) {
			question = this.getDiv(question);
		}
		
		var index = 0;
		if (this.getFormGridClassCount(question) > 1) {
			// we count to the left since 1 question to the left = index of 1
			// and 2 questions to the left = index of 2
			while (true) {
				var $previous = question.prev("." + Config.identifiers.question);
				if ($previous.length < 1 || !this.areQuestionsOnSameRow(question, $previous)) {
					break;
				}
				index++;
				question = $previous;
			}
		}
		return index;
	},
	
	getPreviousNElements : function(question, numberToGet) {
		if (!(question instanceof jQuery)) {
			question = this.getDiv(question);
		}
		var $list = null;
		
		for (var i = 0; i < numberToGet; i++) {
			question = question.prevAll("." + Config.identifiers.question).eq(0);
			if (question.length < 1) {
				break;
			}
			
			// jquery can't add to a zero-length list so we have to replace
			// do I need a clone here?
			if ($list == null) {
				$list = question;
			}
			else {
				$list = $list.add(question);
			}
		}
		
		return $list;
	},
	
	getNextNElements : function(question, numberToGet) {
		if (!(question instanceof jQuery)) {
			question = this.getDiv(question);
		}
		var $list = null;
		
		for (var i = 0; i < numberToGet; i++) {
			question = question.nextAll("." + Config.identifiers.question).eq(0);
			if (question.length < 1) {
				break;
			}

			// jquery can't add to a zero-length list so we have to replace
			// do I need a clone here?
			if ($list == null) {
				$list = question;
			}
			else {
				$list = $list.add(question);
			}
		}
		
		return $list;
	}
};