<script id="editFormTemplate" type="text/x-handlebars-template">
		<form id="dialog_editForm" class="formcreator_dialog">
			<div class="editorErrorContainer"></div>
			<ul>
				<li><a href="#basics">Form Basics</a></li>
				<li><a href="#advanced">Advanced Settings</a></li>
				<li><a href="#format">Form Formatting</a></li>
				<li><a href="#section">Section Formatting</a></li>
			</ul>
			
				<div id="basics" class="tabcontainer">
					<div class="row">
						<div class="col-md-2">
							<label for="form-name">Form Name<span class="requiredStar">*</span></label>
						</div>
						<div class="col-md-2">
							<input type="text" name="name" id="form-name" value="{{name}}">
						</div>
						<div class="col-md-2">
							<label for="form-type">Form Type</label>
						</div>
						<div class="col-md-2">
							<select id="form-type">
								<option>Subject</option>
								<option>Non-Subject</option>
							</select>
						</div>

						<div id="nonPatientFormTypeDiv" style="display:none">
							<div class="col-md-2">
								<label for="nonPatientFormType">Type</label>
							</div>
							<div class="col-md-2">
								<select id="nonPatientFormType">
									<option value="12">Study</option>
									<option value="14">Admin</option>
									<option value="15">Other</option>
								</select>
							</div>
						</div>
					</div>	
                    <div class="row clearfix">
						<div class="col-md-2">
							<label for="form-description">Form Description</label>
						</div>
						<div class="col-md-2">
							<textarea name="description" id="form-description">{{description}}</textarea>
						</div>
						<div class="col-md-2">
							<label for="dataStructureName">Form Structure</label>
						</div>
						<div class="col-md-6">
							<div name="dataStructureName">{{dataStructureName}}</div>
						</div>
					</div>
					<div class="row clearfix">
						<div class="col-md-2">
							<label for="form-status">Form Status</label>
						</div>
						<div class="col-md-2">
							<span id="form-status" name="status">

							</span>
						</div>
					</div>
				</div>
				
				<div class="tabcontainer" id="advanced">
					<div class="row">
						<div class="col-md-2">
							<label for="form-usetab">Use Tab Display</label>
						</div>
						<div class="col-md-2">
							<input type="checkbox" name="tabdisplay" id="form-usetab" />
						</div>
						<div class="col-md-2">
							<label for="form-access">Access</label>
						</div>
						<div class="col-md-2">
							<select name="access" id="form-access">
								<option value="1">Private</option>
								<option value="2">Public</option>
							</select>
						</div>
						<div class="col-md-2">
							<label for="form-dataEntry">Data Entry</label>
						</div>
						<div class="col-md-2">
							<select name="dataEntryFlag" id="form-dataEntry">
							<option value="1">Single Key</option>
							<!--<option value="2">Double Key</option>-->
						</select>
						</div>
                     </div>
                     <div class="row clearfix">
						<div class="col-md-2">
							<label for="form-copyrightedForm">Copyrighted Form</label>
						</div>
						<div class="col-md-2">
							<input type="checkbox" name="copyrightedForm" id="form-copyrightedForm" />
						</div>
						<div class="col-md-2">
							<label for="form-copyrightedForm">Allow Multiple Instances</label>
						</div>
						<div class="col-md-2">
							<input type="checkbox" name="allowMultipleCollectionInstances" id="form-allowMultipleCollectionInstances" title="Allows multiple collection instances for the same combination of Subject, Form name, Visit type and Visit date" />
						</div>
					</div>
				</div>
			
				<div class="tabcontainer" id="format">
					<div class="row">
						<div class="col-md-2">
							<label for="form-header">Form Header</label>
						</div>
						<div class="col-md-2">
							<textarea name="formHeader" id="form-header"></textarea>
						</div>
						<div class="col-md-2">
							<label for="form-footer">Form Footer</label>
						</div>
						<div class="col-md-2">
							<textarea name="formFooter" id="form-footer"></textarea>
						</div>
						<div class="col-md-3">
							<label for="form-borders">Borders Around Entire Form</label>
						</div>
						<div class="col-md-1">
							<input type="checkbox" name="formBorders" id="form-borders" />
						</div>
					</div>
					<div class="row clearfix">
						<div class="col-md-2">
							<label for="form-font">Form Name Font</label>
						</div>
						<div class="col-md-2">
							<select name="formFont" id="form-font">
								<option value="arial">Arial</option>
								<option value="helvetica">Helvetica</option>
								<option value="courier">Courier</option>
								<option value="times">Times</option>
								<option value="avant guard">Avant Guard</option>
								<option value="lucida sans">Lucida Sans</option>
							</select>
						</div>
						<div class="col-md-2">
							<label for="form-fontSize">Form Name Font Size</label>
						</div>
						<div class="col-md-2">
							<select name="fontSize" id="form-fontSize">
								<option value="8">8</option>
								<option value="9">9</option>
								<option value="10">10</option>
								<option value="11">11</option>
								<option value="12">12</option>
								<option value="13">13</option>
								<option value="14">14</option>
								<option value="15">15</option>
								<option value="16">16</option>
								<option value="17">17</option>
								<option value="18">18</option>
							</select>
						</div>
						<div class="col-md-2">
							<label for="form-formcolor">Form Name Color</label>
						</div>
						<div class="col-md-2">
							<select name="formcolor" id="form-formcolor">
							<option value="red">Red</option>
							<option value="green">Green</option>
							<option value="yellow">Yellow</option>
							<option value="blue">Blue</option>
							<option value="purple">Purple</option>
						</select>
						</div>
					</div>
					<div class="row clearfix">
						<div class="col-md-2">
							<label for="form-cellpadding">Element Padding</label>
						</div>
						<div class="col-md-2">
							<select name="cellpadding" id="form-cellpadding">
								<option value="1">1</option>
								<option value="2">2</option>
								<option value="3">3</option>
								<option value="4">4</option>
								<option value="5">5</option>
								<option value="6">6</option>
								<option value="7">7</option>
								<option value="8">8</option>
								<option value="9">9</option>
								<option value="10">10</option>
								<option value="11">11</option>
								<option value="12">12</option>
								<option value="13">13</option>
								<option value="14">14</option>
								<option value="15">15</option>
								<option value="16">16</option>
								<option value="17">17</option>
								<option value="18">18</option>
								<option value="19">19</option>
								<option value="20">20</option>
							</select>
						</div>
					</div>
				</div>
				
				<div class="tabcontainer" id="section">
					<div class="row">
						<div class="col-md-3">
							<label for="form-sectionborder">Borders Around Each Section</label>
						</div>
						<div class="col-md-1">
							<input type="checkbox" name="sectionborder" id="form-sectionborder" />
						</div>
						<div class="col-md-2">
							<label for="form-sectionfont">Section Name Font</label>
						</div>
						<div class="col-md-2">
							<select name="sectionfont" id="form-sectionfont">
								<option value="arial">Arial</option>
								<option value="helvetica">Helvetica</option>
								<option value="courier">Courier</option>
								<option value="times">Times</option>
								<option value="avant guard">Avant Guard</option>
								<option value="lucida sans">Lucida Sans</option>
							</select>
						</div>
						<div class="col-md-2">
							<label for="form-sectioncolor">Section Name Color</label>
						</div>
						<div class="col-md-2">
							<select name="sectioncolor" id="form-sectioncolor">
								<option value="red">Red</option>
								<option value="green">Green</option>
								<option value="yellow">Yellow</option>
								<option value="blue">Blue</option>
								<option value="purple">Purple</option>
							</select>
						</div>
					</div>
				</div>
		</form>
	</script>