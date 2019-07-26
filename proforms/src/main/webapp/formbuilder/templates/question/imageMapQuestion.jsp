<script id="imageMapQuestionTemplate" type="text/x-handlebars-template">
		<div class="questionHeader row">
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
					Question Has Validation Requirements<br />
					<div class="questionValidation_string questionValidation_type">
						Answer must be a String<br />
						<span class="label">character minimum :</span> <span name="minCharacters"></span><br />
						<span class="label">character maximum :</span> <span name="maxCharacters"></span><br />
					</div>
					<div class="questionValidation_numeric questionValidation_type">
						Answer must be a Number<br />
						<div class="questionNumeric_range"></div>
					</div>
					<div class="questionValidation_date questionValidation_type">
						Answer must be a Date<br />

					</div>
					<div class="questionValidation_datetime questionValidation_type">
						Answer must be a Date-Time<br />

					</div>
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
				<div class="row">
					<a href="javascript:;" class="fb-icon-small editButton col-s-6"></a>
					<a href="javascript:;" class="fb-icon-small deleteButton col-s-6"></a>
				</div>
			</div>
		</div>
		<div class="questionBody row">
			<div class="col-md-12">
				Question Name: <class="questionName" name="questionName">{{questionName}}</span>
			</div>
			<div class="col-md-12">
				<div class="questionTextDiv" style="display:inline-block">
					<div name="descriptionUp" class="descriptionUp"></div>
					<div name="questionText" class="questionText"></div>
					<div name="descriptionDown" class="descriptionDown"></div>
				</div>
				
			</div>
		</div>
		<div class="col-md-12 mapArea" style="display:inline-block"></div>
		<div class="col-md-12 graphicDisplay">
		</div>
</script>

<script id="checkboxQuestionInputHorizontalDisplay" type="text/x-handlebars-template">
	<div class="col-md-1 checkboxOption"><input type="checkbox"/>&nbsp;&nbsp;<label>{{label}}</label></div>
</script>


<script id="checkboxQuestionInputVerticalDisplay" type="text/x-handlebars-template">
		<div class="col-md-12 checkboxOption"><input type="checkbox"/>&nbsp;&nbsp;<label>{{label}}</label></div>
</script>