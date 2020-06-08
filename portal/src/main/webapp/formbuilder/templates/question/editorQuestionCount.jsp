<script id="editQuestionCountTabLabel" type="text/x-handlebars-template">
	<li><a href="#dialog_editQuestion_count">Count</a></li>
</script>

<script id="editQuestionCountTab" type="text/x-handlebars-template">
<div class="tabcontainer" id="dialog_editQuestion_count">
	<div class="row clearfix">

	<div id='countRuleContainer1' class='col-md-6'>
	
		<div class="row clearfix">
			<div class="col-md-12">
				<input type="button" id="selectCountQuestions" value="Select Questions" />
			</div>
		</div>
	</div>
	
	<div id='countRuleContainer2' class='col-md-6'>
		<div class="row clearfix">
			<div class="col-md-offset-4 col-md-4">
				<label>Selected questions</label>
			</div>
		</div>
		<div class="row clearfix">
			<div class="col-md-12  divQuestionsToCount">
				<div id="questionsToCountDisplay" class="questionsToCountDisplay"></div>
			</div>
		</div>
	</div >
</div>
</script>