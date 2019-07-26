 <script id="visualScaleQuestionEditTemplate" type="text/x-handlebars-template"> 
<div id="dialog_editTextbox" class="formcreator_dialog">
		<div class="editorErrorContainer"></div>
		<ul>
			<li><a href="#dialog_editQuestion_tab1">Basic Settings</a></li>
		</ul>
		<!--div class="row">
				<label class="col-md-6" for="visualQuestionText">Please enter the answer options for the question and arrange the order.</label>
			  </div-->
		
<div class="tabcontainer row clearfix">
		<div class="col-md-12" id="dialog_editQuestion_tab1">
			<div class="row">
				<div class="dialog_editQuestion_tab1_container1 col-md-6">
					<div class="row clearfix">
						<div class="col-md-6">
							<label for="questionType">Question Type<span class="requiredStar">*</span></label>
						</div>
						<div class="col-md-6">
							<select class="questionInput questionType" name="questionType">
								<option value="12">Text Block</option>
		                        <option value="11">File</option>
		                        <option value="10">Visual Scale</option>
		                        <%--<option value="9">Image Map</option>--%>
		                        <option value="6">Checkbox</option>
		                        <option value="5">Multi-Select</option>
		                        <option value="4">Radio</option>
		                        <option value="3">Select</option>
		                        <option value="2">Textarea</option>
		                        <option value="1">Textbox</option>
							</select>
						</div>
					</div>
					<div class="row clearfix">
						<div class="col-md-6">
							<label for="collapsible">Text</label>
						</div>
						<div class="col-md-6">
							<div id="questionText" class="textarea" name="questionText"></div>
						</div>
					</div>
					<div class="row clearfix selectQuestionformat" style="display: none;">	
						<div class="col-md-6">
						</div>
						<div class="col-md-6">
							<br><p><strong>This question text has had advanced formatting.</strong></p>
							<a href="#" class="defaultText">Return to default(no advanced formatting)</a><br>
						</div>
					</div>
					<div class="row clearfix">	
						<div class="col-md-6">
							<label>Data Element</label>
						</div>
						<div class="col-md-6">
							<div name="dataElementName">{{dataElementName}}</div>
						</div>
					</div>
				</div>
				<div class="dialog_editQuestion_tab1_container2 col-md-6">
					<div class="row clearfix">	
						<div class="col-md-3">
							<label for="scaleRangeMinimum">Scale Range Minumum</label>
						</div>
						<div class="col-md-3">
							<input type="text" name="vscaleRangeStart" id="scaleRangeMinimum" />
						</div>
						<div class="col-md-3">
							<label for="centerText">Center Text</label>
						</div>
						<div class="col-md-3">
							<input type="text" name="vscaleCenterText" id="centerText" />
						</div>
					</div>
					<div class="row clearfix">
						<div class="col-md-3">
							<label for="scaleRangeMaximum">Scale Range Maximum</label>
						</div>
						<div class="col-md-3">
							<input type="text" name="vscaleRangeEnd" id="scaleRangeMaximum" />
						</div>
						<div class="col-md-3">
							<label for="leftText">Left Text</label>
						</div>
						<div class="col-md-3">
							<input type="text" name="vscaleLeftText" id="leftText" />
						</div>
					</div>
					<div class="row clearfix">
						<div class="col-md-3">
							<label for="scaleWidth">Scale Width (mm) :</label>
						</div>
						<div class="col-md-3">
							<input type="text" name="vscaleWidth" id="scaleWidth" />
						</div>
						<div class="col-md-3">
							<label for="rightText">Right Text</label>
						</div>
						<div class="col-md-3">
							<input type="text" name="vscaleRightText" id="rightText" />
						</div>
					</div>
				</div>
			</div>
		</div>
</div>	
			
	</div>
</script>