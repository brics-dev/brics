<script id="editSectionTemplate" type="text/x-handlebars-template">
	<div id="dialog_editSection" class="formcreator_dialog">
		<div class="editorErrorContainer"></div>
		<ul>
			<li><a href="#dialog_editSection_tab1">Section Information</a></li>
			<li><a href="#dialog_editSection_tab2">Repeatable Section</a></li>
		</ul>
		<div class="tabcontainer" id="dialog_editSection_tab1">
			<div class="row clearfix">
				<div class="col-md-3">
					<label for="sectionName">Section Name</label>
				</div>
				<div class="col-md-3">
					{{#if isManuallyAdded }}
						<input type="text" id="sectionName" value="{{name}}" name="name" style="width: 100%" />
					{{else}}
						<div name="name" id="sectionName" style="width: 100%"></div>
					{{/if}}
				</div>
				<div class="col-md-3">
					<label for="description">Section Text</label>
				</div>
				<div class="col-md-3">
					<input type="text" id="description" value="{{description}}" name="description" style="width: 100%" />
				</div>
			</div>
			<div class="row clearfix">
				<div class="col-md-3">
					<label for="isCollapsable">Collapsible</label>
				</div>
				<div class="col-md-1">
					<input type="checkbox" id="isCollapsable" name="isCollapsable" {{#if isCollapsable}}checked="checked"{{/if}} name="__collapsible" />
				</div>
			</div>
		</div>
		
		<div class="tabcontainer" id="dialog_editSection_tab2">
			<div class="row clearfix">
				<div class="col-md-3">
					<label for="isRepeatable">Repeatable</label>
				</div>
				<div class="col-md-3">
					<input type="checkbox" name="isRepeatable" id="isRepeatable" value="true" disabled="disabled" />
				</div>

				<div class="col-md-3 repeatableInput">
					<label for="repeatableGroup">Link to Repeatable Group<span id="grinfor"></span></label>
				</div>
				<div class="col-md-3 repeatableInput">
					<div id="repeatableGroupName" name="repeatableGroupName" style="width: 100%"></div>
				</div>
				<div class="clearfix"></div>
			</div>
			<div class="row clearfix">
				<div class="col-md-3 repeatableInput">
					<label for="initRepeatedSecs">Number of Times Sections Viewed</label>
				</div>
				<div class="col-md-3 repeatableInput">
					<input type="text" id="initRepeatedSecs" name="initRepeatedSecs" value="{{initRepeatedSecs}}" style="width: 100%" />
				</div>

				<div class="col-md-3 repeatableInput">
					<label for="maxRepeatedSecs">Maximum Number of Times Viewed</label>
				</div>
				<div class="col-md-3 repeatableInput">
					<input type="text" id="maxRepeatedSecs" name="maxRepeatedSecs" value="{{maxRepeatedSecs}}" style="width:100%" />
				</div>
			</div>
		</div>
	</div>
</script>