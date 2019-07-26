var TableLayoutEditView = EditorView.extend({
	dialogTitle : "Add/Edit Table",
	templateName : "editTableTemplate",
	validNumCols : false,
	validNumRows : false,
	currentMoveQTextToHeaders : false,
	
	validationRules : [
	                   
	         //this validation rule tests to make sure number of rows is a whole number greater than 0         
			new ValidationRule({
				   fieldName : "numCols",
				   match : function(model) {
			
					   var numCols = FormBuilder.page.get("activeEditorView").$("#numCols").val().trim();
					   if(numCols == "") {
						   this.description = "A valid whole number greater than 0 and not exceeding 12 must be entered for Number of Columns";
						   validNumCols = false;
						  return false; 
					   }else {
						 //check that its a whole number greater than 0;
						   if (isNaN(numCols) ) {
							   this.description = "A valid whole number greater than 0 and not exceeding 12 must be entered for Number of Columns";
							   validNumCols = false;
							   return false;
						   }else {
							   var numColsNumber = Number(numCols);
							   if(numColsNumber > 0 && numColsNumber <= 12 && numColsNumber % 1 === 0) {
								   validNumCols = true;
								   return true;
							   }else {
								   this.description = "A valid whole number greater than 0 and not exceeding 12 must be entered for Number of Columns";
								   validNumCols = false;
								   return false;
							   }
						   }
					   }
				   }
			}),
			
			
			 //this validation rule tests to make sure number of columns is a whole number greater than 0 
			new ValidationRule({
				   fieldName : "numRows",
				   match : function(model) {

					   var numRows = FormBuilder.page.get("activeEditorView").$("#numRows").val().trim();
					   if(numRows == "") {
						  this.description = "A valid whole number greater than 0 must be entered for Number of Rows";
						  validNumRows = false;
						  return false; 
					   }else {
						   //check that its a whole number greater than 0;
						   if (isNaN(numRows) ) {
							  this.description = "A valid whole number greater than 0 must be entered for Number of Rows";
							  validNumRows = false;
							  return false;
						   }else {
							   var numRowsNumber = Number(numRows);
							   if(numRowsNumber > 0 && numRowsNumber % 1 === 0) {
								   validNumRows = true;
								   return true;
							   }else {
								   this.description = "A valid whole number greater than 0 must be entered for Number of Rows";
								   validNumRows = false;
								   return false;
							   }
						   }
					   }
				   }
			}),
			
			
			
			//this validation rule tests that for repeatable section, the num rows (same as initial times viewed for repeatable section) 
			//is less than max number of times viewed
			new ValidationRule({
				   fieldName : "numRows",
				   match : function(model) {

					   if(validNumRows) {
						   var isRepeatable = model.get("isRepeatable");
						   if(isRepeatable) {
							   var numRows = FormBuilder.page.get("activeEditorView").$("#numRows").val().trim();
							   var numRowsNumber = Number(numRows);
							   var max = Number(model.get("maxRepeatedSecs"))
							   if(numRowsNumber > max) {
								   this.description = "Value entered for Number of Rows must not exceed Maximum Number of Times Viewed for Repeatable Section";
								   validNumRows = false;
								   return false;
							   }else {
								   return true;
							   }
							   
						   }
						   return true;
					   }else {
						   return false; 
					   }
					  
					   
				   }
			}),
			
			
			//this validation rule tests that for NON-repeatable sections, the number of columns times the number of rows
			//equals the number of questions in that section
			new ValidationRule({
				   fieldName : "numCols",
				   match : function(model) {

					   if(validNumRows && validNumCols) {
						   var isRepeatable = model.get("isRepeatable");
						   if(!isRepeatable) {
							   var numRows = FormBuilder.page.get("activeEditorView").$("#numRows").val().trim();
							   var numCols = FormBuilder.page.get("activeEditorView").$("#numCols").val().trim();
							   var numRowsNumber = Number(numRows);
							   var numColsNumber = Number(numCols);
							   var activeSectionDivId = model.get("divId");
							   var $section = $("#" + activeSectionDivId);
							   var numQuestions = $section.find(".question").not(".rowHeader").length
							   
							   if(numColsNumber * numRowsNumber != numQuestions) {
								   this.description = "The Number of Columns multiplied by the Number of Rows must equal the Number of Questions in the section";
								   return false;
							   }else {
								   return true;
							   }
							   
						   }
						   return true;
					   }else {
						   return true; 
					   }
					  
					   
				   }
			})
   
	],
	
	
	initialize : function() {
		TableLayoutEditView.__super__.initialize.call(this);
		this.template = TemplateManager.getTemplate(this.templateName);
		EventBus.on("reset:tableSection",this.removeTable,this);
	},
	
	events : {
		"click #colHeaders" : "enableDisableMoveQTextHeadersInput"
	},
	

	
	enableDisableMoveQTextHeadersInput : function() {
		var checked = this.$("#colHeaders").prop("checked");
		if(checked) {
			this.$("#removeQTextHeaders").prop("disabled", false);	
		}else {
			this.$("#removeQTextHeaders").prop("checked", false);
			this.$("#removeQTextHeaders").prop("disabled", true);
		}
		
	},
	
	
	render : function(model) {
		// this transforms the editor to reference the primary section of a table group if the selected section is
		// already a part of one.  For example, if the user selects the column header section of a table group, this
		// will reference the section with the questions instead of the column header.
		var tableGroup = model.get("tableGroupId");
		if (tableGroup != 0) {
			this.model = FormBuilder.form.getPrimarySectionForTableGroup(tableGroup);
		}
		else {
			this.model = model;
		}
		
		this.dialogTitle = "Add/Edit Table on Section " + model.get("name");
		
		//need to render for either add mode or edit mode
		var isGridtype = this.model.get("gridtype");
		var isRepeatable = this.model.get("isRepeatable");
		var numQuestions = this.getNonHeaderQuestionCount();


		this.$el.html(this.template(this.model.attributes));
		TableLayoutEditView.__super__.render.call(this, this.model);
		
		
		var tableGroupId = this.model.get("tableGroupId");
		if(isGridtype) {
			//this means it is already a table and we are in edit mode
			if(isRepeatable) {
				//if section is repeatable, then prepopulate the numCols with the number of questions in the section and then disable it
				this.$("#numCols").val(numQuestions);
				this.$("#numCols").prop("disabled", true);
				var initRepeatedSecs = this.model.get("initRepeatedSecs");
				this.$("#numRows").val(initRepeatedSecs);
				
				var $section = $("#" + this.model.get("divId"));
				var $firstQues = $section.find(".question").eq(0);
				if($firstQues.hasClass("rowHeader")) {
					this.$("#rowHeaders").prop("checked", true);
				}else {
					this.$("#rowHeaders").prop("checked", false);
				}
				
				
				var columnHeaderSectionModel = FormBuilder.form.getColHeaderForTableGroup(tableGroupId);
				if(typeof columnHeaderSectionModel == "undefined" || columnHeaderSectionModel == null) {
					this.$("#colHeaders").prop("checked", false);
					this.$("#removeQTextHeaders").prop("checked", false);
					this.$("#removeQTextHeaders").prop("disabled", true);	
				}else {
					this.$("#colHeaders").prop("checked", true);
					this.$("#removeQTextHeaders").prop("disabled", false);	
					
					
					var qTexts = [];
					var questions = $section.find(".question").not(".rowHeader");
					for(var i=0;i<numQuestions;i++) {
						var $ques = questions.eq(i);
						var q = this.model.getQuestionByDivId($ques.attr("id"));
						var questionText = q.get("questionText");
						qTexts.push(questionText);
					} 	
					
					var $columnHeaderSection = $("#" + columnHeaderSectionModel.get("divId"));
					var columnHeaders = $columnHeaderSection.find(".columnHeader");
					var chTexts = [];
					
					
					var k = 0;
					if($firstQues.hasClass("rowHeader")) {
						k = 1;
					}
					
					for(var i=k;i<columnHeaders.length;i++) {
						var $cHeader= columnHeaders.eq(i);
						var ch = columnHeaderSectionModel.getQuestionByDivId($cHeader.attr("id"));
						var htmlText = ch.get("htmlText");
						htmlText = htmlText.substring(3,htmlText.indexOf("</p>"))
						chTexts.push(htmlText);
					} 

					if(qTexts.length == chTexts.length) {
						var blah = true;
						for(var i=0;i<qTexts.length;i++) {
							if(qTexts[i] != chTexts[i]) {
								blah = false;
								break;
							}	
						}
						if(blah) {
							this.$("#removeQTextHeaders").prop("checked", true);	
						}else {
							this.$("#removeQTextHeaders").prop("checked", false);	
						}
					}
					

					
					
					
					
				}
				
				
				
				var tableHeaderSectionModel = FormBuilder.form.getTableHeaderForTableGroup(tableGroupId);
				if(typeof tableHeaderSectionModel == "undefined" || tableHeaderSectionModel == null) {
					this.$("#showTableTitle").prop("checked", false);

				}else {
					this.$("#showTableTitle").prop("checked", true);
				}

				//to do: ctable headers, other options
			}else {
				this.$("#numCols").val("");
				this.$("#numRows").val("");
				var $section = $("#" + this.model.get("divId"));
				var $firstQues = $section.find(".question").eq(0);
				var columnCount = SectionDragHandler.getFormGridClassCount($firstQues);
				if($firstQues.hasClass("rowHeader")) {
					columnCount = columnCount -1;
					this.$("#rowHeaders").prop("checked", true);
				}else {
					this.$("#rowHeaders").prop("checked", false);
				}
				var columnHeaderSectionModel = FormBuilder.form.getColHeaderForTableGroup(tableGroupId);
				if(typeof columnHeaderSectionModel == "undefined" || columnHeaderSectionModel == null) {
					this.$("#colHeaders").prop("checked", false);
					this.$("#removeQTextHeaders").prop("checked", false);
					this.$("#removeQTextHeaders").prop("disabled", true);	
				}else {
					this.$("#colHeaders").prop("checked", true);
					this.$("#removeQTextHeaders").prop("disabled", false);	
					
					
					
					var qTexts = [];
					var questions = $section.find(".question").not(".rowHeader");
					for(var i=0;i<columnCount;i++) {
						var $ques = questions.eq(i);
						var q = this.model.getQuestionByDivId($ques.attr("id"));
						var questionText = q.get("questionText");
						qTexts.push(questionText);
					} 	
					
					var $columnHeaderSection = $("#" + columnHeaderSectionModel.get("divId"));
					var columnHeaders = $columnHeaderSection.find(".columnHeader");
					var chTexts = [];
					
					
					var k = 0;
					if($firstQues.hasClass("rowHeader")) {
						k = 1;
					}
					
					for(var i=k;i<columnHeaders.length;i++) {
						var $cHeader= columnHeaders.eq(i);
						var ch = columnHeaderSectionModel.getQuestionByDivId($cHeader.attr("id"));
						var htmlText = ch.get("htmlText");
						htmlText = htmlText.substring(3,htmlText.indexOf("</p>"))
						chTexts.push(htmlText);
					} 

					if(qTexts.length == chTexts.length) {
						var blah = true;
						for(var i=0;i<qTexts.length;i++) {
							if(qTexts[i] != chTexts[i]) {
								blah = false;
								break;
							}	
						}
						if(blah) {
							this.$("#removeQTextHeaders").prop("checked", true);	
						}else {
							this.$("#removeQTextHeaders").prop("checked", false);	
						}
					}
				}
				//to do: table headers, other options
				
				var tableHeaderSectionModel = FormBuilder.form.getTableHeaderForTableGroup(tableGroupId);
				if(typeof tableHeaderSectionModel == "undefined" || tableHeaderSectionModel == null) {
					this.$("#showTableTitle").prop("checked", false);
				}else {
					this.$("#showTableTitle").prop("checked", true);
				}
				
				//question...user may have generated a 4 x 2 table and then removed a question...now goes to edit it...what to display?
				//for the time being...i will say nothing
				if(numQuestions % columnCount == 0) {
					//this means we still have a table with same column count in all rows
					var rowCount = numQuestions/columnCount;
					this.$("#numCols").val(columnCount);
					this.$("#numRows").val(rowCount);
				}	
			}
			
			// we are in edit mode, so we need to check if the text is hidden
			this.setShowTextCheckbox(this.isQTextVisible());
		}else {
			//this means it is not a table so we are going in add mode
			if(isRepeatable) {
				//if section is repeatable, then prepopulate the numCols with the number of questions in the section and then disable it
				this.$("#numCols").val(numQuestions);
				this.$("#numCols").prop("disabled", true);
				var initRepeatedSecs = this.model.get("initRepeatedSecs");
				this.$("#numRows").val(initRepeatedSecs);
				this.$("#rowHeaders").prop("checked", false);
				this.$("#colHeaders").prop("checked", false);
				this.$("#showTableTitle").prop("checked", false);
			}else {
				
				this.$("#numCols").val("");
				this.$("#numRows").val("");
				this.$("#rowHeaders").prop("checked", false);
				this.$("#colHeaders").prop("checked", false);
				this.$("#showTableTitle").prop("checked", false);
				
			}
			
			// we are in add mode, so the checkbox will always be checked
			this.setShowTextCheckbox(true);
		}
		
		
		currentMoveQTextToHeaders = FormBuilder.page.get("activeEditorView").$("#removeQTextHeaders").prop("checked");
		
		return this;
	},
	
	save : function() {

		var numRows = FormBuilder.page.get("activeEditorView").$("#numRows").val().trim();
		var numCols = FormBuilder.page.get("activeEditorView").$("#numCols").val().trim();
		var includeRowHeaders = FormBuilder.page.get("activeEditorView").$("#rowHeaders").prop("checked");
		var includeColHeaders = FormBuilder.page.get("activeEditorView").$("#colHeaders").prop("checked");
		var includeTableTitle = FormBuilder.page.get("activeEditorView").$("#showTableTitle").prop("checked");
		var moveQTextToHeaders = FormBuilder.page.get("activeEditorView").$("#removeQTextHeaders").prop("checked");
		
		
		var $section = $("#" + this.model.get("divId"));
		var activeSectionDivId = this.model.get("divId");
		var sectionId = this.model.get("id");
		var sectionModel = this.model;
		var isRepeatable = this.model.get("isRepeatable");
		
		var saveSuccess = TableLayoutEditView.__super__.save.call(this);  //runs validation
		if (saveSuccess) {
			//set the gridlayout flag to true for this section
			this.model.set("gridtype",true);

			var numRowsNumber = Number(numRows);
			var numColsNumber = Number(numCols);
			
			
			var newTableGroupId = FormBuilder.form.getLargestTableGroupId() + 1;
			
			
			if(includeRowHeaders) {
				numColsNumber = numColsNumber + 1;
			}
			
			
			
			//if repeatable, then set the initRepeatedSecs to what user typed in numRows...becasue it may be different
			
			if(isRepeatable) {
				this.model.set("initRepeatedSecs",numRowsNumber);	
				//now that initRepeatedSecs has been set, lets set numRowsNumber to 1 becasue in the case of repeatable sections, all the questions go in 1 row
				//and the initRepeatedSecs tells us how many child secstions to open up so it looks like a table with that many rows
				numRowsNumber = 1;
			}
			

			//to do
			//if we are including col headers, we will actually put them in their own section just before the table section with all the questions
			// we do this so we can handle repeatable sections nicely...and since we are doing it for repeatable, we might as well do it the same
			//way as for non-repeatable
			
			
			this.showHideQText();
			
			if(includeTableTitle) {
				//first we check if there is already a tableheader section...if so, delete any existing table header in it
				var currenTableGroupId = this.model.get("tableGroupId");
				var tableHeaderSectionModel = FormBuilder.form.getTableHeaderForTableGroup(currenTableGroupId);
				var tableHtmlText = "<p>Put the table header text here</p>";
				
				
				if(typeof tableHeaderSectionModel == "undefined" || tableHeaderSectionModel == null) {
					//need to determine the render after element..it needs to go before column header section if it exists
					var columnHeaderSectionModel = FormBuilder.form.getColHeaderForTableGroup(currenTableGroupId);
					var $previous; 
					if(typeof columnHeaderSectionModel == "undefined" || columnHeaderSectionModel == null) {
						$previous = SectionDragHandler.getPreviousNElements($section, 1);
						
					}else {
						$previous = $("#" + columnHeaderSectionModel.get("divId"));
					}

					var renderAft;
					if ($previous != null) { 
						renderAft = $previous;
					}
					else {
						renderAft =  $("."+Config.identifiers.formContainer); 
					}
					tableHeaderSectionModel = FormBuilder.form.addSection({
						name 			: "table header",
						renderAfter :  renderAft,
						gridtype    :  true,
						tableHeaderType : Config.tableHeaderTypes.tableHeader,
						disableEditor: true,
						tableGroupId : newTableGroupId
					});
					
					EventBus.trigger("add:section", tableHeaderSectionModel);
					tableHeaderSectionModel.set("isNew", false);
					
					var $tableHeaderSectionModel = $("#" + tableHeaderSectionModel.get("divId"));
					$tableHeaderSectionModel.addClass(Config.tableHeaderClassNames.tableHeader);
				}else {
					//this means that there is an existing table header section
					//so check if there are any existing table header...and delete them first
					//first set the the table group id
					tableHeaderSectionModel.set("tableGroupId",newTableGroupId);
					
					var $tableHeaderSection = $("#" + tableHeaderSectionModel.get("divId"));
					
					var $existingTableHeaders = $tableHeaderSection.find(".tableHeader");
					if($existingTableHeaders.length > 0) {
						$existingTableHeaders.each(function() {
							var $existingTableHeader = $(this);
							var existingTableHeader = tableHeaderSectionModel.getQuestionByDivId($existingTableHeader.attr("id"));
							tableHtmlText = existingTableHeader.get("htmlText");
							EventBus.trigger("delete:question",existingTableHeader);
						});
					}
				}
				//now create and add new table header
				var tableHeaderSectionId = tableHeaderSectionModel.get("id");

				var config =  {
						questionText:"Text Block", 
						htmlText: tableHtmlText
				}
				var defaults = {
						questionName : "",
						questionText : "Header Text",
						sectionId	 : tableHeaderSectionId,
						disableEditor: true,
						tableHeaderType : Config.tableHeaderTypes.tableHeader
				};
				var question = $.extend({}, defaults, config);
				question.questionType = Config.questionTypes.textblock;
				var newQuestion = tableHeaderSectionModel.addQuestion(question);
				// save to database
				newQuestion.saveToDatabase(true);
				newQuestion.set("isNew", false);
				//add rowHeader class
				var $question = $("#" + newQuestion.get("newQuestionDivId"));
				$question.addClass(Config.tableHeaderClassNames.tableHeader);
				
				EventBus.trigger("add:question", newQuestion);
				
				
			}else {
				
				//need to delete the table header section and its table header if the section exists
				
				var currentTableGroupId = this.model.get("tableGroupId");
				var tableHeaderSectionModel = FormBuilder.form.getTableHeaderForTableGroup(currentTableGroupId);
				
				if(typeof tableHeaderSectionModel != "undefined" && tableHeaderSectionModel != null) {
					
					EventBus.trigger("delete:section",tableHeaderSectionModel);
				}
				
			}
			
			
			
			
			
			
			
			if(includeColHeaders) {
				//first we check if there is already a colheader section...if so, delete any existing column headers in it
				var currenTableGroupId = this.model.get("tableGroupId");
				var columnHeaderSectionModel = FormBuilder.form.getColHeaderForTableGroup(currenTableGroupId);
				var columnHtmlText = "<p>Put the column header text here</p>";
				
				
				
				if(typeof columnHeaderSectionModel == "undefined" || columnHeaderSectionModel == null) {
					//this means there is no header section so there is no need to delete any existing column headers
					//so.....first create the column header section
					//need to determine the render after element
					var $previous = SectionDragHandler.getPreviousNElements($section, 1);
					
					
					var renderAft;
					if ($previous != null) { 
						renderAft = $previous;
					}
					else {
						renderAft =  $("."+Config.identifiers.formContainer); 
					}
					columnHeaderSectionModel = FormBuilder.form.addSection({
						name 			: "column header",
						renderAfter :  renderAft,
						gridtype    :  true,
						tableHeaderType : Config.tableHeaderTypes.columnHeader,
						disableEditor: true,
						tableGroupId : newTableGroupId
					});
					
					EventBus.trigger("add:section", columnHeaderSectionModel);
					columnHeaderSectionModel.set("isNew", false);
					
					var $columnHeaderSectionModel = $("#" + columnHeaderSectionModel.get("divId"));
					$columnHeaderSectionModel.addClass(Config.tableHeaderClassNames.columnHeader);

				}else {
					//this means that there is an existing column header section
					//so check if there are any existing column headers...and delete them first
					//first set the the table group id
					columnHeaderSectionModel.set("tableGroupId",newTableGroupId);
					
					var $columnHeaderSection = $("#" + columnHeaderSectionModel.get("divId"));
					
					var $existingColumnHeaders = $columnHeaderSection.find(".columnHeader");
					
					if($existingColumnHeaders.length > 0) {
						$existingColumnHeaders.each(function() {
							var $existingColumnHeader = $(this);
							var existingColumnHeader = columnHeaderSectionModel.getQuestionByDivId($existingColumnHeader.attr("id"));
							columnHtmlText = existingColumnHeader.get("htmlText");
							EventBus.trigger("delete:question",existingColumnHeader);
						});
					}
				}
				
				
				//need to add this in case of if user did move q text to headers and then decided to uncheck it...we want the html text to be
				//put column header text here
				if(currentMoveQTextToHeaders == true && moveQTextToHeaders == false) {
					columnHtmlText = "<p>Put the column header text here</p>";
				}
				
				
				
				
				
				var qTexts = [];
				if (moveQTextToHeaders) {
					var questions = $section.find(".question").not(".rowHeader");
					//Number(numCols) is just the number of questions we want in a row...disregarding rowheader
					//so get the texts of the questions that make up the first row
					var qColsNumber = Number(numCols);
					for(var i=0;i<qColsNumber;i++) {
						var $ques = questions.eq(i);
						var q = this.model.getQuestionByDivId($ques.attr("id"));
						var questionText = q.get("questionText");
						qTexts.push(questionText);
					} 	
				}
				
				
				
				
				
				var columnHeaderSectionId = columnHeaderSectionModel.get("id");
				//now create and add new column headers
				for(var i=0;i<numColsNumber;i++) {
					var renderAfterElement;
					var text;
					if(includeRowHeaders && i==0) {
						text = "";
					}else {
						if(moveQTextToHeaders) {
							if(includeRowHeaders) {
								text = "<p>" + qTexts[i-1] + "</p>";
							}else {
								text = "<p>" + qTexts[i] + "</p>";
							}
						}else {
							text = columnHtmlText;
						}
					}
					
					var config =  {
						questionText:"Text Block", 
						htmlText: text
					}
					var defaults = {
							questionName : "",
							questionText : "Header Text",
							sectionId	 : columnHeaderSectionId,
							disableEditor: true,
							tableHeaderType : Config.tableHeaderTypes.columnHeader
						};
					var question = $.extend({}, defaults, config);
					question.questionType = Config.questionTypes.textblock;
					var newQuestion = columnHeaderSectionModel.addQuestion(question);
					// save to database
					newQuestion.saveToDatabase(true);
					newQuestion.set("isNew", false);
					//add rowHeader class
					var $question = $("#" + newQuestion.get("newQuestionDivId"));
					$question.addClass(Config.tableHeaderClassNames.columnHeader);
					
					EventBus.trigger("add:question", newQuestion);
				}	

				//put them all in the same row...only 1 row since they are column headers
				var $row = $();
				var $currQues;
				var $columnHeaderSection = $("#" + columnHeaderSectionModel.get("divId"));
				for(var col=0;col<numColsNumber;col++) {
					
					if(col == 0) {
						$currQues = $columnHeaderSection.find(".question").eq(0);
					}else {
						$currQues = $currQues.next();
					}
					
					$row = $row.add($currQues);

				}

				//now that all the questions have been added to the row...
				QuestionDragHandler.equalWidth($row);
				QuestionDragHandler.enforceRowHeight($row);
				EventBus.trigger("resize:section",columnHeaderSectionModel);	
				EventBus.trigger("change:section", columnHeaderSectionModel);

			}else {
				//need to delete the column header section and its column headers if the section exists
				
				var currentTableGroupId = this.model.get("tableGroupId");
				var columnHeaderSectionModel = FormBuilder.form.getColHeaderForTableGroup(currentTableGroupId);
				
				if(typeof columnHeaderSectionModel != "undefined" && columnHeaderSectionModel != null) {
					
					EventBus.trigger("delete:section",columnHeaderSectionModel);
				}

			}

			
			
			//now create all the textblocks for rowheaders...the row headers will go in same section as all the other questions
			if(includeRowHeaders) {
				
				
				

				var rowHtmlText = "<p>Put the row header text here</p>";
				//first delete any exsisting row headers first
				var $existingRowHeaders = $section.find(".rowHeader");

				
				
				if($existingRowHeaders.length > 0) {
					$existingRowHeaders.each(function() {
						var $existingRowHeader = $(this);
						var existingRowHeader = sectionModel.getQuestionByDivId($existingRowHeader.attr("id"));
						EventBus.trigger("delete:question",existingRowHeader);
						rowHtmlText = existingRowHeader.get("htmlText"); 
					});
				}
				

				
				//now create and add new row headers
				for(var i=0;i<numRowsNumber;i++) {
					var renderAfterElement;
					
					if(i == 0) {
						renderAfterElement = $section;
					}else {
						var index = (i * numColsNumber) - 1;
						renderAfterElement = $section.find(".question").eq(index);	
					}
					var config =  {
						questionText:"Text Block", 
						htmlText: rowHtmlText,
						renderAfter : renderAfterElement
					}
					var defaults = {
							questionName : "",
							questionText : "Header Text",
							sectionId	 : sectionId,
							disableEditor: true,
							tableHeaderType : Config.tableHeaderTypes.rowHeader
						};
					var question = $.extend({}, defaults, config);
					question.questionType = Config.questionTypes.textblock;
					var newQuestion = this.model.addQuestion(question);
					// save to database
					newQuestion.saveToDatabase(true);
					//add rowHeader class
					newQuestion.set("isNew", false);
					var $question = $("#" + newQuestion.get("newQuestionDivId"));
					$question.addClass(Config.tableHeaderClassNames.rowHeader);
					
					if(isRepeatable) {
						EventBus.trigger("create:repeatableQuestions", newQuestion);
					}
					
					EventBus.trigger("add:question", newQuestion);


					
					
				}	
			}else {
				//not including row headers so delete any existing row headers
				var $existingRowHeaders = $section.find(".rowHeader");
				
				$existingRowHeaders.each(function() {
					var $existingRowHeader = $(this);
					var existingRowHeader = sectionModel.getQuestionByDivId($existingRowHeader.attr("id"));
					EventBus.trigger("delete:question",existingRowHeader);
				});
				

				
				
			}

			var $currQues = null;
			//ok now we are ready to do the logic to put the questions in the table ordering
			for(var row=0;row<numRowsNumber;row++) {
				var $row = $();
				for(var col=0;col<numColsNumber;col++) {
					
					if(row == 0 && col == 0) {
						$currQues = $section.find(".question").eq(0);
					}else {
						$currQues = $currQues.next();
					}
					
					$row = $row.add($currQues);

				}
	
				//now that all the questions have been added to the row...
				QuestionDragHandler.equalWidth($row);
				QuestionDragHandler.enforceRowHeight($row);

			}
			
			$section.addClass(Config.tableHeaderClassNames.tablePrimary);
			this.model.set("tableGroupId",newTableGroupId);

			EventBus.trigger("resize:section",this.model);	
			EventBus.trigger("change:section", this.model);
		}
	},
	
	/**
	 * Finds whether the question text in this section is visible or not...have to find the first non-textblcock to determine this
	 * 
	 * @return true if the text is visible, otherwise false
	 */
	isQTextVisible : function() {
		if (this.model.questions.length > 0) {

			for(var i=0;i<this.model.questions.length;i++) {
				var q = this.model.questions.at(i);
				var questionType = q.get("questionType");
				if(questionType != "12") {
					var $question = $("#" + q.get("newQuestionDivId"));
					var visible = $question.find(".questionText").is(":visible");
					return visible;
				}
			}
			
			return false;
		}
		return false;
	},
	
	setShowTextCheckbox : function(isVisible) {
		if (isVisible) {
			this.$("#showQText").prop("checked", true);
		}
		else {
			this.$("#showQText").prop("checked", false);
		}
	},
	
	/**
	 * If the checkbox is checked, then set showText on each question in the
	 * primary section to TRUE.  Otherwise set it to FALSE.
	 */
	showHideQText : function() {
		// note: this.model is always the primary section (also, if repeatable,
		// it is the repeatable parent)
		var $checkbox = this.$("#showQText");
		if ($checkbox.is(":checked")) {
			this.model.questions.forEach(function(question) {
				question.set("showText", true);
				EventBus.trigger("resize:question", question);
			});
		}
		else {
			this.model.questions.forEach(function(question) {
				question.set("showText", false);
				EventBus.trigger("resize:question", question);
			});
		}
		// for good measure, resize the section
		EventBus.trigger("resize:section", this.model);
	},
	
	cancel : function() {
		TableLayoutEditView.__super__.cancel.call(this);
	},
	
	removeTable : function(section) {
		// this transforms the editor to reference the primary section of a table group if the selected section is
		// already a part of one.  For example, if the user selects the column header section of a table group, this
		// will reference the section with the questions instead of the column header.
		var tableGroup = section.get("tableGroupId");
		if (tableGroup != 0) {
			this.model = FormBuilder.form.getPrimarySectionForTableGroup(tableGroup);
		}
		else {
			this.model = section;
		}
		
		this.model.set("gridtype",false);
		
		this.model.questions.forEach(function(question) {
			question.set("showText", true);
			question.set("tableHeaderType", Config.tableHeaderTypes.none);
		});
		
		
		//remove row headers
		var $section = $("#" + this.model.get("divId"));
		var $existingRowHeaders = $section.find(".rowHeader");
		var sectionModel = this.model;
		$existingRowHeaders.each(function() {
			var $existingRowHeader = $(this);
			var existingRowHeader = sectionModel.getQuestionByDivId($existingRowHeader.attr("id"));
			EventBus.trigger("delete:question",existingRowHeader);
		});
		
		//to do...remove column headers, table header  (delete that header section)
		//need to delete the column header section and its column headers if the section exists
		
		var currentTableGroupId = this.model.get("tableGroupId");
		var columnHeaderSectionModel = FormBuilder.form.getColHeaderForTableGroup(currentTableGroupId);
		
		if(typeof columnHeaderSectionModel != "undefined" && columnHeaderSectionModel != null) {
			
			EventBus.trigger("delete:section",columnHeaderSectionModel);
		}
		

		var tableHeaderSectionModel = FormBuilder.form.getTableHeaderForTableGroup(currentTableGroupId);
		
		if(typeof tableHeaderSectionModel != "undefined" && tableHeaderSectionModel != null) {
			
			EventBus.trigger("delete:section",tableHeaderSectionModel);
		}
		

		
		

		var numQuestions = this.getNonHeaderQuestionCount();
		var $section = $("#" + this.model.get("divId"));
		var $currQues = null;
		//ok now we are ready to do the logic to put the questions in the table ordering
		for(var i=0;i<numQuestions;i++) {
			var $row = $();
				if(i == 0) {
					$currQues = $section.find(".question").eq(0);
				}else {
					$currQues = $currQues.next();
				}
				
				$row = $row.add($currQues);

			//now that all the questions have been added to the row...
			QuestionDragHandler.equalWidth($row);
			QuestionDragHandler.enforceRowHeight($row);

		}
		
		
		this.model.set("tableGroupId",0);
		
		//remove primary table class
		$section.removeClass(Config.tableHeaderClassNames.tablePrimary);
		
		EventBus.trigger("resize:section",this.model);
		EventBus.trigger("change:section", this.model);
		EventBus.trigger("change:activeSection", this.model);
	},
	
	close : function() {
		return TableLayoutEditView.__super__.close.call(this);
	},
	
	
	getNonHeaderQuestionCount : function() {
		var $section = $("#" + this.model.get("divId"));
		return $section.find(".question").not(".rowHeader").length;
		
	}
});