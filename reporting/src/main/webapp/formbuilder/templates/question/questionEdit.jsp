<script id="editQuestionTemplate" type="text/x-handlebars-template">
	<div id="dialog_editTextbox" class="formcreator_dialog">
		<div class="editorErrorContainer"></div>
		<ul>
			<li><a href="#dialog_editQuestion_tab1">Basic Settings</a></li>
		</ul>
		<div class="tabcontainer row" id="dialog_editQuestion_tab1">
            <div class="row clearfix">
				<div class="col-md-2">
					<label for="questionType">Question Type<span class="requiredStar">*</span></label>
				</div>
				<div class="col-md-2">
					<select class="questionInput questionType" name="questionType">
						<option value="1">Textbox</option>
						<option value="2">Textarea</option>
						<option value="3">Select</option>
 						<option value="5">Multi-Select</option>
						<option value="4">Radio</option>
						<option value="6">Checkbox</option>
 						<option value="11">File</option>
						<option value="10">Visual Scale</option>
					</select>
				</div>
			</div>
			<div class="row clearfix">
				<div class="col-md-2">
					<label for="collapsible">Text</label>
				</div>
				<div class="col-md-2">
					<textarea name="questionText" id="questionText"></textarea>
				</div>
			</div>
			<div class="row clearfix">
				<div class="col-md-2">
					<label>Data Element</label>
				</div>
				<div class="col-md-7">
					<div name="dataElementName" class="displayValue">{{dataElementName}}</div>
				</div>
			</div>		
		</div>
	</div>
</script>