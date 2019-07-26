<script id="editQuestionPrepopulationTabLabel" type="text/x-handlebars-template">
	<li><a href="#dialog_editQuestion_prepopulation">Prepopulation</a></li>
</script>

<script id="editQuestionPrepopulationTab" type="text/x-handlebars-template">
	<div class="tabcontainer" id="dialog_editQuestion_prepopulation">
		<div class="row clearfix">
			<div class="col-md-4">
				<label for="prepopulation">
					Prepopulate Question Value During Data Collection
				</label>
			</div>
			<div class="col-md-2">
				<input id="prepopulation" type="checkbox" name="prepopulation" />
			</div>
		</div>
		<div class="row prePopDependent  clearfix">
			<div class="col-md-3">
				<label for="answerRequired" class="required">
					Value to prepopulate
				</label>
			</div>
			<div class="col-md-3">
				<select name="prepopulationValue">
			 		<option value="primarySiteName">Primary Site Name</option>
			 		<option value="visitType">Visit Type</option>
					<option value="visitDate">Visit Date</option>
					<option value="guid">GUID</option>
				</select>
			</div>
		</div>
	</div>
</script>