<script id="selectQuestionTemplate" type="text/x-handlebars-template">
		<div class="questionHeader row clearfix">
			<div class="questionHeaderLeft col-md-10">
				<a href="javascript:;" class="questionLinked{{#unless required}} off{{/unless}}"></a>
				<div class="statusTooltip">
					<span class="label">This question is linked to dataElement: </span>
					<span name="dataElementName"></span><br />
				</div>				
				<a href="javascript:;" class="questionRequired{{#unless required}} off{{/unless}}"></a>
				<div class="statusTooltip">This question is required</div>

				<div class="statusTooltip">This question is linked to data element <span name="dataElementName"></span></div>

				<a href="javascript:;" class="questionValidation{{#unless validation}} off{{/unless}}" id="questionValidationIcon"></a>
				<div class="statusTooltip" id="statusTooltip_validation">
				</div>

				<a href="javascript:;" class="questionCalculation{{#unless calculatedQuestion}} off{{/unless}}"></a>
				<div class="statusTooltip" id="statusTooltip_calculation">
					<span class="label">Calculation Rule:</span><br />
					<div class="questionCalcStatus"></div>
				</div>

				<a href="javascript:;" class="questionPrepopulation{{#unless prepopulation}} off{{/unless}}"></a>
				<div class="statusTooltip" id="statusTooltip_prepopulation">
					<span class="label">Prepopulated With:</span><br />
					<span name="prepopulationValue"></span><br />
				</div>

				<a href="javascript:;" class="questionSkiprule{{#unless skiprule}} off{{/unless}}"></a>
				<div class="statusTooltip" id="statusTooltip_skiprule">
					<span class="label">This question has Skip Rule applied</span>
				</div>

				<a href="javascript:;" class="questionConversion{{#unless unitConversionFactor}} off{{/unless}}"></a>
				<div class="statusTooltip" id="statusTooltip_questionConversion">					
					The answer to this question will be modified before submission
				</div>

				<a href="javascript:;" class="questionEmail{{#unless emailTrigger}} off{{/unless}}"></a>
				<div class="statusTooltip" id="statusTooltip_email">					
					<span class="label">When answer is </span>
					<span name="triggerAnswers"></span><br />
					<span class="label"> sent mail to </span>
					<span name="toEmailAddress"></span><br />
				</div>
			</div>
			<div class="questionHeaderRight col-md-2">
				<div class="row clearfix">
					<a href="javascript:;" class="fb-icon-small editButton col-s-6"></a>
					<a href="javascript:;" class="fb-icon-small deleteButton col-s-6"></a>
				</div>
			</div>
		</div>
		<div class="questionBody row clearfix">
			<div class="col-md-12">
				Data Element: <class="dataElementName" name="dataElementName">{{dataElementName}}</span>
			</div>
			<div class="col-md-12">
				<div name="descriptionUp" class="descriptionUp"></div>
			</div>
			<div class="col-md-12">
				<div class="questionTextDiv" style="display:inline-block">
					<div name="questionText" class="questionText"></div>
				</div>
				<div class="selectedQuestionContainer" style="display:inline-block">     
					<select class="questionInput selectedQuestion" name="selectedQuestion">
					</select>
				</div>
				
			</div>
			<div class="col-md-12">
				<div name="descriptionDown" class="descriptionDown"></div>
			</div>
		</div>
		<div class="col-md-12 graphicDisplay">
		</div>
</script>

<script id="selectQuestionInput"  type="text/x-handlebars-template">
    <div class="col-md-3 selectOption">
		<option value="{{optionValue}}">{{optionText}}</option>
    </div>
</script>

<script id="selectQuestionInputOther"  type="text/x-handlebars-template">
		<option value="{{optionValue}}" class="otherspecify">{{optionText}}</option>
</script>

