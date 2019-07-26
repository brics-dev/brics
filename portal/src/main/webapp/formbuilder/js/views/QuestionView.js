/**
 * 
 */
// TODO: complete!
var QuestionView  = BaseView.extend({
	className : "well question",
	
	events : {
		"click .questionHeader .editButton" : "edit",
		"click .questionHeader .deleteButton" : "deleteQuestion",
		"click" : "selectionClickHandler",
		"mouseenter .questionRequired" : "showRequiredTooltip",
		"mouseout .questionRequired" : "hideRequiredTooltip",
		"mouseenter .questionValidation" : "showValidationTooltip",
		"mouseout .questionValidation" : "hideValidationTooltip",
		"mouseenter .questionPrepopulation" : "showPrepopulationTooltip",
		"mouseout .questionPrepopulation" : "hidePrepopulationTooltip",
		"mouseenter .questionCalculation" : "showCalculationTooltip",
		"mouseout .questionCalculation" : "hideCalculationTooltip",
		"mouseenter .questionSkiprule" : "showSkipruleTooltip",
		"mouseout .questionSkiprule" : "hideSkipruleTooltip",
		"mouseenter .questionEmail" : "showEmailTooltip",
		"mouseout .questionEmail" : "hideEmailTooltip",
		"mouseenter .questionLinked" : "showDataElementTooltip",
		"mouseout .questionLinked" : "hideDataElementTooltip",
		"mouseenter .questionConversion" : "showConversionFactorTooltip",
		"mouseout .questionConversion" : "hideConversionFactorTooltip",
		"mouseover" : "hoverIn",
		"mouseout" : "hoverOut",
		"click .qgraphics" : "goImgWin"
	},
	
	initialize : function() {
		this.id = this.model.get("newQuestionDivId");
		QuestionView.__super__.initialize.call(this);
		// in initialize, we are creating the new section, so it should be active
		this.listenTo(this.model, "change:active", this.afterChangeActive);
		this.listenTo(this.model, "change:required", this.afterChangeRequired);
		this.listenTo(this.model, "change:validation", this.afterChangeValidation);
		this.listenTo(this.model, "change:minCharacters", this.afterChangeValidation);
		this.listenTo(this.model, "change:maxCharacters", this.afterChangeValidation);
		this.listenTo(this.model, "change:calculatedQuestion", this.afterChangeCalculation);
		this.listenTo(this.model, "change:prepopulation", this.afterChangePrepop);
		this.listenTo(this.model, "change:hasUnitConversionFactor", this.afterChangeConversionFactor);
		this.listenTo(this.model, "change:skipRuleOperatorType", this.afterChangeSkipRule);
		this.listenTo(this.model, "change:skipRuleType", this.afterChangeSkipRule);
		this.listenTo(this.model, "change:emailTrigger", this.afterChangeEmailTrigger);
		this.listenTo(this.model, "change:dataElementName", this.afterChangeDataElement);
		
		
		this.listenTo(this.model, "change:answerType", this.changeAnswerType);
		this.listenTo(this.model, "change:rangeOperator", this.changeRangeOperator);
		this.listenTo(this.model, "change:color", this.changeColor);
		this.listenTo(this.model, "change:fontFace", this.changeFont);
		this.listenTo(this.model, "change:align", this.changeHAlign);
		this.listenTo(this.model, "change:vAlign", this.changeVAlign);
		this.listenTo(this.model, "change:fontSize", this.changeFontSize);
		this.listenTo(this.model, "change:horizDisplayBreak", this.changeHorizDisplayBreak);
		this.listenTo(this.model, "change:indent", this.changeIndent);
		this.listenTo(this.model, "change:newQuestionDivId", this.changeDivId);
		this.listenTo(this.model, "remove", this.destroy);
		// event to tell the rest of the page about this new active question
		// is in sectionView when the question is actually added 
		
		//added by Ching Heng
		this.listenTo(this.model, "change:graphicNames", this.addEditGraphics);
		this.listenTo(this.model, "change:questionName", this.uppercase);
		this.listenTo(this.model, "change:imageFileName", this.addImageMap);
		this.listenTo(this.model, "change:imageOption", this.addImageMapOptions);
		this.listenTo(this.model, "change:tableHeaderType", this.showHideText);
		this.listenTo(this.model, "change:showText", this.showHideText);
		
		// handle re-rendering a question
		EventBus.on("reRender:question", this.reRender, this);
		return this;
	},
	
	render : function($after) {
		this.model.calculateDivId(true);
		this.$el.attr("id", this.model.get("newQuestionDivId"));
		this.$el.html(this.template(this.model.attributes));
		this.$el.addClass(this.typeClassName);
		// if not doing a re-render (question already visible)
		if (!this.$el.is(":visible")) {
			// if we're loading from db, ignore any other positioning data
			// and just append this question to the end of the section AS DEFINED
			// IN THE QUESTION MODEL
			if (FormBuilder.page.get("loadingData")) {
				this.renderToModelSection();
			}
			else {
				var sectionId = this.model.get("sectionId");
				// if $after is a section, prepend it to the section
				if (typeof $after !== "undefined" && $after.length > 0 && $after.hasClass(Config.identifiers.section)) {
					$after.find("."+Config.identifiers.questionContainer).prepend(this.$el);
				}
				else if (typeof $after !== "undefined" && $after.length > 0) {
					this.renderAfter($after);
				}
				else if (sectionId) {
					this.renderToModelSection();
				}
				else {
					// get the active section's div ID and append into it
					this.renderToActiveSection();
				}
			}
			this.model.set("renderAfter", null);
		}
		
		this.setupTooltips();
		QuestionView.__super__.render.call(this);
		// if adding a new section,
		// event to tell the rest of the page about this new activesection
		// is in formView when the section is actually added
		this.changeAnswerType();
		this.setupSortable();
		this.addEditGraphics();
		this.addImageMap();
		this.addImageMapOptions();
		this.afterChangePrepop();
		this.afterChangeDataElement();
		this.afterChangeSkipRule();
		this.afterChangeRequired();
		this.afterChangeValidation();
		this.afterChangeCalculation();
		this.afterChangeEmailTrigger();
		this.changeRangeOperator();
		this.changeColor();
		this.changeFont();
		this.changeHAlign();
		this.changeVAlign();
		this.changeFontSize();
		this.changeHorizDisplayBreak();
		this.changeIndent();
		this.needDisableDelete();
		this.afterChangeActive();
		this.showHideText();

		return this;
	},
	
	/**
	 * Handles the QuestionView side of re-rendering: closing the current
	 * rendering.
	 * 
	 * Note: this calls close so it does NOT clean up the $el or this
	 * view itself.  So, destroy() should be called once the SectionView
	 * is finished with its part.
	 */
	reRender : function(question) {
		if (question.cid === this.model.cid) {
			this.$el.removeClass(this.typeClassName);
			this.undelegateEvents();
			this.close();
		}
	},
	
//	changeDispatcher : function(valueChanged) {
//		switch(valueChanged):
//			case "active"
//	},
	
	/**
	 * Renders the question to the section stored in this.model.sectionId.
	 * This ignores any $after parameter in this.render()
	 */
	renderToModelSection : function() {
		var section = FormBuilder.form.section(this.model.get("sectionId"));
		if (section != null) {
			var sectionDivId = section.get("divId");
			$("#" + sectionDivId).find("."+Config.identifiers.questionContainer).append(this.$el);
		}
	},
	
	renderToActiveSection : function() {
		var divId = FormBuilder.page.get("activeSection").get("divId");
		$("#" + divId).find("."+Config.identifiers.questionContainer).append(this.$el);
	},
	
	renderAfter : function($after) {
		$after.after(this.$el);
	},
	
	setupSortable : function() {
		QuestionDragHandler.refresh();
	},
	
	setupTooltips : function() {
		this.$(".statusTooltip").each(function() {
			var link = $(this).prevAll("a").first();
			var linkPosition = link.position();
			$(this).css({
				"top": linkPosition.top + link.height(),
				"left": linkPosition.left + link.height()
			});
		});
	},
	
	hoverIn : function() {
		// no longer needed since we removed drag/drop questions and sections
		//EventBus.trigger("disable:sectionSortable");
	},
	
	hoverOut : function() {
		// no longer needed since we removed drag/drop questions and sections
		//EventBus.trigger("enable:sectionSortable");
	},
	
	isActive : function() {
		return this.model.get("active");
	},
	
	needDisableDelete : function() {
		if (this.model.get("required")) {
			this.disableDelete();
		}
		else {
			this.enableDelete();
		}
	},
	
	disableDelete : function() {
		var $button = this.$(".deleteButton");
		if (!$button.hasClass("disabled")) {
			$button.addClass("disabled");
		}
	},
	
	enableDelete : function() {
		var $button = this.$(".deleteButton");
		if ($button.hasClass("disabled")) {
			$button.removeClass("disabled");
		}
	},
	
	showConversionFactorTooltip : function() {
		if (this.model.get("hasUnitConversionFactor")) {
			this.$(".questionConversion").next(".statusTooltip").show();
		}
	},
	
	hideConversionFactorTooltip : function() {
		this.$(".questionConversion").next(".statusTooltip").hide();
	},
	
	showRequiredTooltip : function() {
		if (this.model.get("required")) {
			this.$(".questionRequired").next(".statusTooltip").show();
		}
	},
	
	hideRequiredTooltip : function() {
		this.$(".questionRequired").next(".statusTooltip").hide();
	},
	
	showValidationTooltip : function() {
		if (this.model.get("validation")) {
			this.$(".questionValidation").next(".statusTooltip").show();
		}
	},
	
	hideValidationTooltip : function() {
		this.$(".questionValidation").next(".statusTooltip").hide();
	},
	
	showPrepopulationTooltip : function() {
		if (this.model.get("prepopulation")) {
			this.$(".questionPrepopulation").next(".statusTooltip").show();
		}
	},
	
	hidePrepopulationTooltip : function() {
		this.$(".questionPrepopulation").next(".statusTooltip").hide();
	},
	
	showCalculationTooltip : function() {
		if (this.model.get("calculatedQuestion")) {
			
			var str = this.model.get("calculation");
			
			var str2 = "";
			
			if(str != "") {
				str2 = this.model.reformatCalculationDisplay(str);
				
			}
			

			this.$(".questionCalcStatus").html(str2);
			
			this.$(".questionCalculation").next(".statusTooltip").show();
		}
	},
	
	hideCalculationTooltip : function() {
		this.$(".questionCalculation").next(".statusTooltip").hide();
	},
	
	showSkipruleTooltip : function() {
        var skipRuleOperatorType = this.model.get("skipRuleOperatorType");
        if (Number(skipRuleOperatorType) != -2147483648) {
        	this.$(".questionSkiprule").next(".statusTooltip").show();
        }
	},
	
	hideSkipruleTooltip : function() {
		this.$(".questionSkiprule").next(".statusTooltip").hide();
	},
	
	showEmailTooltip : function() {
		if (this.model.get("emailTrigger")) {
			this.$(".questionEmail").next(".statusTooltip").show();
		}
	},
	
	hideEmailTooltip : function() {
		this.$(".questionEmail").next(".statusTooltip").hide();
	},
	
	showDataElementTooltip : function() {
		if (this.model.get("dataElementName") != 'none') {
			this.$(".questionLinked").next(".statusTooltip").show();
		}
	},
	
	hideDataElementTooltip : function() {
		this.$(".questionLinked").next(".statusTooltip").hide();
	},
	
	showHideText : function() {
		var showText = this.model.get("showText");
		if (!showText) {
			// hide the text
			this.$(".questionText").hide();
		}
		else {
			// show the text
			this.$(".questionText").show();
		}
	},
	
	/**
	 * Sets the class of this.$el to active or inactive, chosen by the
	 * model's "active" attribute
	 */
	afterChangeActive : function() {
		this.$el.removeClass(Config.styles.inactive);
		this.$el.removeClass(Config.styles.active);
		
		if (this.isActive()) {
			this.$el.addClass(Config.styles.active);
			this.$(".editButton, .deleteButton").show();
		}
		else {
			this.$el.addClass(Config.styles.inactive);
			this.$(".editButton, .deleteButton").hide();
		}
	},
	
	/**
	 * Removes both active and inactive classes from this section
	 */
	removeClass : function() {
		this.$el.removeClass(Config.styles.inactiveSection);
		this.$el.removeClass(Config.styles.activeSection);
	},
	
	/**
	 * Click section handler
	 * 
	 * Triggers the global change active section activity
	 */
	selectionClickHandler : function(event) {
//		event.stopImmediatePropagation();
		if (!this.isActive()) {
			EventBus.trigger("change:activeQuestion", this.model);
		}
	},
	
	edit : function() {
		EventBus.trigger("open:questionEditor", this.model);
	},
	
	deleteQuestion : function(event) {
		// this might not be obvious but, when deleting a question,
		// we move the active question to the previous (or next)
		// question, so we have to make sure a click event doesn't
		// get called on this section after that runs - causing THIS
		// question (now invisible) to become the active one
		event.stopImmediatePropagation();
		
		//do not allow for delete question if this form is a CAT form (added by Ching-Heng)
		if(FormBuilder.form.get("isCAT") && FormBuilder.form.get("measurementType")!='shortForm'){
			var msg = "<div class='ibisMessaging-message ibisMessaging-primary ibisMessaging-error'>This instrument is a computer adaptive test (CAT), so its questions are generated dynamically based on answers given. Because it is dynamic, a question can not be deleted.</div>";
			$.ibisMessaging("dialog", "info", msg, {
				width: 500,
				dialogClass : "formBuilder_editor",
				modal : true,
				buttons: [
				{
					text: "Ok",
					click : function() {
						$(this).dialog("close");
				    }
				}]				               
			});		
			return;
		}
		
		//do not allow for delete question if question in parennt or children has skip or calc dependant
		var section = FormBuilder.form.section(this.model.get("sectionId"));
		if(section.get("isRepeatable")) {
			var repeatedQuestions = RepeatableSectionProcessor.getQuestionRepeatedCopies(this.model, section);
			for (var m = 0; m < repeatedQuestions.length; m++) {
				var repQuestion = repeatedQuestions[m];
				
				if(repQuestion.get("skipRuleDependent")){
					$.ibisMessaging("dialog", "error", Config.language.deleteSkipQuestion);
					return;
				}else if(repQuestion.get("calDependent")){
					$.ibisMessaging("dialog", "error", Config.language.deleteCalQuestion);
					return;
				}
				
			}
		}
		
		
		
		
		// not allowed to delete if the question is required
		if (!this.model.get("required")) {
			EventBus.trigger("delete:question",this.model);
		}
	},

	changeDivId : function() {
		this.$el.attr("id", this.model.get("newQuestionDivId"));
	},
	
	// Since these require listening to the model and changing something outside
	// a NAME element, we have to have listen and function
	
	changeColor : function() {
		this.$(".questionText").css("color", this.model.get("color"));
	},
	
	changeFont : function() {
		var font = this.model.get("fontFace");
		var element = this.$(".questionText");
		element.css("font-family", font);
	},
	
	changeHAlign : function() {
		this.$(".questionBody").css("text-align", this.model.get("align"));
	},
	
	changeVAlign : function() {
		this.$(".questionText").css("vertical-align", this.model.get("vAlign"));
	},
	
	changeFontSize : function() {
		// this is from an analysis of <font size=""> +/-(increment) vs point 
		// size.  For the sizes we use, a 5th order polynomial matches at R^2=1
		// for browser standard rounding to the 0.5pt.
		// y = 0.0229x5 - 0.0625x4 - 0.1146x3 + 0.9375x2 + 2.7167x + 10
		var modelSize = Number(this.model.get("fontSize"));
		var fontSize = 0.0229*Math.pow(modelSize,5) - 0.0625*Math.pow(modelSize,4) - 0.1146*Math.pow(modelSize,3) + 0.9375*Math.pow(modelSize,2) + 2.7167*modelSize + 10;
		this.$(".questionText").css("font-size", fontSize+"pt");
		EventBus.trigger("resize:question", this.model);
	},
	
	changeHorizDisplayBreak : function() {
		var horizDispBreak = this.model.get("horizDisplayBreak");
		if (horizDispBreak) {
			this.$(".questionTextDiv").css("display", "block");
		}
		else {
			this.$(".questionTextDiv").css("display", "inline-block");
		}
		EventBus.trigger("resize:question", this.model);
	},
	
	changeIndent : function() {
		var indent = this.model.get("indent");
		if (typeof indent === "undefined" || $.trim(indent) === "") {
			this.model.set("indent", 0);
		}
		else {
			this.$(".questionText").css("padding-left", this.model.get("indent") + "px");
		}
	},
	
	afterChangeRequired : function() {
		var req = this.model.get("required");
		if (req) {
			this.$(".questionRequired").removeClass("off");
		}
		else {
			this.$(".questionRequired").addClass("off");
		}
	},
	
	afterChangeConversionFactor : function() {
		var cf = this.model.get("hasUnitConversionFactor");
		if (cf) {
			this.$(".questionConversion").removeClass("off");
		}
		else {
			this.$(".questionConversion").addClass("off");
		}
	},
	
	afterChangeValidation : function() {
		var validation = this.model.get("validation");
		if (validation) {
			this.$(".questionValidation").removeClass("off");
		}
		else {
			this.$(".questionValidation").addClass("off");
		}
	},
	
	changeAnswerType : function() {
		var type = this.model.get("answerType");

		this.$(".questionValidation_type").hide();
		switch(type) {
			case '1':
			case 1:
				this.$(".questionValidation_string").show();
				break;
			case '2':
			case 2:
				this.$(".questionValidation_numeric").show();
				break;
			case '3':
			case 3:
				this.$(".questionValidation_date").show();
				break;
			case '4':
			case 4:
				this.$(".questionValidation_datetime").show();
				break;
			default:
				throw new Error("the answer type chosen does not match the allowed values");
		}
	},
	
	changeRangeOperator : function() {
		var rangeOperator = null;
		var rangeOne = this.model.get("rangeValue1");
		var rangeTwo = this.model.get("rangeValue2");
		
		// Check if the range operator is a number.
		if ( typeof this.model.get("rangeOperator") == "number" ) {
			rangeOperator = this.model.get("rangeOperator").toString();
		}
		else {
			rangeOperator = this.model.get("rangeOperator");
		}
		
		switch(rangeOperator) {
			case "":
			case "none":
				break;
			case "0":
				this.$(".questionNumeric_range").hide();
				break;
			case "isequalto":
				break;
			case "1":
				this.$(".questionNumeric_range").show().text("Value must be equal to " + rangeOne);
				break;
			case "lessthan":
				break;
			case "2":
				this.$(".questionNumeric_range").show().text("Value must be less than " + rangeOne);
				break;
			case "greaterthan":
				break;
			case "3":
				this.$(".questionNumeric_range").show().text("Value must be greater than " + rangeOne);
				break;
			case "between":
				break;
			case "4":
				this.$(".questionNumeric_range").show().text("Value must be between " + rangeOne + " and " + rangeTwo);
				break;
			default:
				throw new Error("the answer type chosen does not match the allowed values");
		}
	},
	
	afterChangeCalculation : function() {
		var calculatedQuestion = this.model.get("calculatedQuestion");
		if (calculatedQuestion) {
			this.$(".questionCalculation").removeClass("off");

		}
		else {
			this.$(".questionCalculation").addClass("off");
		}
	},
	
	afterChangePrepop : function() {
		var prepopulation = this.model.get("prepopulation");
		if (prepopulation) {
			this.$(".questionPrepopulation").removeClass("off");
		}
		else {
			this.$(".questionPrepopulation").addClass("off");
		}
	},
	
	afterChangeSkipRule : function() {
		var skipRuleOperatorType = this.model.get("skipRuleOperatorType");

		if (Number(skipRuleOperatorType) == -2147483648) {
			this.$(".questionSkiprule").addClass("off");
		}
		else {
			this.$(".questionSkiprule").removeClass("off");
		}
	},
	
	afterChangeEmailTrigger : function() {
		var emailTrigger = this.model.get("emailTrigger");
		if (emailTrigger) {
			this.$(".questionEmail").removeClass("off");
		}
		else {
			this.$(".questionEmail").addClass("off");
		}
	},
	
	afterChangeDataElement : function() {
		var dataElementName = this.model.get("dataElementName");
		if (dataElementName != 'none') {
			this.$(".questionLinked").removeClass("off");
		}
		else {
			this.$(".questionLinked").addClass("off");
		}
	},
	
	// added by Ching Heng
	goImgWin : function(event){
		var $element = $(event.currentTarget);
		var myImage =$element.attr('src');
		var myHeight = 240;
		myHeight += 24;
		var myWidth = 302;
		myWidth += 24;
		TheImgWin = openPopup(myImage,'image','height=' +
								myHeight + ',width=' + myWidth +
		                        ',toolbar=no,directories=no,status=no,' +
		                        'menubar=no,scrollbars=no,resizable=yes');
		TheImgWin.moveTo(100,50);
		TheImgWin.focus();
	},
	
	addEditGraphics : function(){
		var graphicNames = this.model.get("graphicNames");
		if(graphicNames.length == 0){ // users do NOT upload the graphics or there is no graphic
			this.$(".graphicDisplay").empty();
		}else{ // they do
			var grphicTemplate = TemplateManager.getTemplate("graphics");
			this.$(".graphicDisplay").html(grphicTemplate);
			for(var i=0;i<graphicNames.length;i++){
				//this.$('.qFraphic_'+i).show();
				var view=this;
				
				$.ajax({
					  type: "POST",
					  url: baseUrl+"renderImageAction!renderImage.action",
					  data: {questionId:this.model.get("questionId"),imageFileName:graphicNames[i]},
					  success: function (data) {
						  var imgArray = JSON.parse(data);
						  for(i=0;i<imgArray.length;i++){
							 
							  var $_img =  view.$('.qFraphic_'+i) ;
							  $_img.attr("src", imgArray[i]).show();
						  }
						//var $_img =  view.$('.qFraphic_'+i+':visible');
						//$_img.attr("src",data);
						
					  },
					  error : function(e) {
						 // alert("error"+e);
					  }
				});
			}	
		}
	},
	
	addImageMap :function(){
		if(this.model.get("questionType") == Config.questionTypes.imageMap){
			var imageMapName = this.model.get("imageFileName");
			var imageMapTemplate ='';
			if(this.$(".mapArea").html() == ''){
				imageMapTemplate = TemplateManager.getTemplate("imageMapTemplate");
			}else{
				imageMapTemplate = this.$(".mapArea").html();
			}
			this.$(".mapArea").html(imageMapTemplate);
			if(imageMapName != null){
				this.$('.Qmap').attr("src",baseUrl+"/images/questionimages/"+imageMapName);
			}
		}
	},
	
	addImageMapOptions : function(){
		var a=0;
		if(this.model.get("questionType") == Config.questionTypes.imageMap){
			var imageMapOptionString = this.model.get("imageOption");
			var imageMapTemplate ='';
			if(this.$(".mapArea").html().length == ''){
				imageMapTemplate = TemplateManager.getTemplate("imageMapTemplate");
			}else{
				imageMapTemplate = this.$(".mapArea").html();
			}
			this.$(".mapArea").html(imageMapTemplate);
			if(imageMapOptionString != null){
				var imageMapOptions = imageMapOptionString.split(Config.alienSymbol);
				this.$('.QmapOptions').html('');
				for(var i=0; i<imageMapOptions.length;i++){
					this.$('.QmapOptions').append('<option>'+imageMapOptions[i]+'</option>');
				}
			}
		}
	},
	
	uppercase: function(){
		var upperCase = this.model.get("questionName").toUpperCase();
		this.model.set("questionName",upperCase,{silent:true});
	},
});