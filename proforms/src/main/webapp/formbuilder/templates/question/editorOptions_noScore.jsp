
<script id="editQuestionOptions_noScore" type="text/x-handlebars-template">
	
<div style='float:left; width: 100%;'>

	<div class="row clearfix">	
		<div class="col-md-6">
			<label for="optionChoice" class="optionsHeader required">Text</label>
		</div>
		<div class="col-md-4">
			<label for="optionSubmittedValue" class="optionsHeader">Submitted Value</label>
		</div>
	</div>
	
	<div class="row clearfix">
		<div class="col-md-12 questionOptionsContainer divOptions">
			<div class="row clearfix inputOptionsRow" style="display: none">
				<div class="col-md-3">
					<input type="text"size="40"  class="optionChoiceInput" style="width: 100%" />
				</div>
				<div class="col-md-4">
					<input type="text" size="40" class="optionSubmittedValueInput" style="width: 100%" />
				</div>
				<div class="col-md-1 col-md-offset-1">
					<a href="javascript:;" class="fb-icon-small addButton addOption"></a>
				</div>
			</div>
		</div>
	</div>




</div>






</script>




<script id="editQuestionOptionsRow_noScore" type="text/x-handlebars-template">

<div class='row clearfix optionRow'>
	<div class='col-md-6 optionChoice'>{{option}}</div>
	<div class='col-md-4 optionSubmittedValue'>{{submittedValue}}</div>
	<div class="clearfix"></div>
</div>

</script>