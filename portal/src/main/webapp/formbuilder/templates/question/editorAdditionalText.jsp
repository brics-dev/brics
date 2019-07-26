<script id="editQuestionAdditionalTextLabel" type="text/x-handlebars-template">
	<li><a href="#dialog_editQuestion_additionalText">Additional Text</a></li>
</script>





<script id="editQuestionAdditionalTextTab" type="text/x-handlebars-template">
<div class="tabcontainer" id="dialog_editQuestion_additionalText">


<div class="row clearfix">

	<div class="col-md-2">
		<label>
			Description Above
		</label>
	</div>
		<div class="col-md-4">
			<div rows="6" cols="50" id="descriptionUp" class="textarea" name="descriptionUp"></div>
		</div>

		<div class="col-md-2">
			<label>
				Description Below
			</label>
		</div>
		<div class="col-md-4">
			<div rows="6" cols="50" id="descriptionDown" class="textarea" name="descriptionDown"></div>
		</div>

</div>
<div class="row clearfix">
	<div class="col-md-2">
	</div>
	<div class="col-md-4 descriptionUp" style="display: none;">
				<br><p><strong>This question text has had advanced formatting.</strong></p>
				<a href="#" class="defaultDescriptionUp">Return to default(no advanced formatting).</a>
	</div>
	<div class="col-md-4 descriptionUpPlaceHolder" style="display: none;">
	</div>
	<div class="col-md-2">
	</div>
	<div class="col-md-4 descriptionDown" style="display: none;">
				<br><p><strong>This question text has had advanced formatting.</strong></p>
				<a href="#" class="defaultDescriptionDown">Return to default(no advanced formatting).</a>
	</div>

</div>

</div>
</script>