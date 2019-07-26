<script id="textBlockTemplate" type="text/x-handlebars-template">
		<div class="clearfix">
			<div class="questionHeader row">
				<div class="questionHeaderLeft col-md-10">
					<a href="javascript:;" class="questionLinked{{#unless required}} off{{/unless}}"></a>
					<div class="statusTooltip">
						<span class="label">This question is linked to dataElement: </span>
						<span name="dataElementName"></span><br />
					</div>

					<a href="javascript:;" class="questionRequired{{#unless required}} off{{/unless}}"></a>
					<div class="statusTooltip">This question is required</div>

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
						<a href="javascript:;" class="fb-icon-small editButton col-s-1"></a>
						<a href="javascript:;" class="fb-icon-small deleteButton col-s-1"></a>
					</div>
				</div>
			</div>
		</div>
		<div class="questionBody row">
			<div class="textblockContent col-md-12">
			</div>
		</div>
</script>