<script id="editQuestionOptions" type="text/x-handlebars-template">
<div>

Choose to display either the Permissible Value Description (PVD - Text) or the Permissible Value (PV - Submitted Value) on the eForm.<br/>
	<div class="row clearfix">	
		<div class="col-md-6 optionsHeader">
			<label for="optionChoice" class="PVoptionsLabel">PVD - Text</label><input type="radio" id="optionChoice"/> 		
		</div>
		<div class="col-md-2 optionsHeader">
			<label for="optionScore">Score</label>
		</div>
		<div class="col-md-4 optionsHeader">
			<label for="optionSubmittedValue" class="PVoptionsLabel">PV - Submitted Value</label><input type="radio" id="optionSubmittedValue"/> 
		</div>
	</div>
	<div class="row clearfix">
		<input type="hidden" name="questionOptions" />
		<div class="col-md-12 questionOptionsContainer divOptions">
			<div class="row clearfix inputOptionsRow" style="display: none">
				<div class="col-md-6">
					<input type="text" size="40" class="optionChoiceInput" style="width: 100%" disabled="disabled" />
				</div>
				<div class="col-md-2">
					<input type="text" size="40" class="optionScoreInput" style="width: 100%" />
				</div>
				<div class="col-md-2">
					<input type="text" size="40" class="optionSubmittedValueInput" style="width: 100%" disabled="disabled" />
				</div>
				<div class="col-md-1 col-md-offset-1">
					<a href="javascript:;" class="fb-icon-small addButton addOption"></a>
				</div>
			</div>
		</div>
	</div>
</div>
</script>






<script id="editQuestionOptionsRow" type="text/x-handlebars-template">
<div class='row clearfix optionRow'>
	<div class='col-md-6 optionChoice'>{{option}}</div>
	<div class='col-md-2 optionScore' >{{score}}</div>
	<div class='col-md-2 optionSubmittedValue'>{{submittedValue}}</div>
	<div class="col-md-1 col-md-offset-1">
		<div class=''>
			<a href='javascript:;' class='fb-icon-small editButton'></a>
		</div>
	</div>
	<div class="clearfix"></div>
</div>

</script>
