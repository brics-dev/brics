<script id="filterGenericContainer" type="text/x-handlebars-template">
<div class="queryFilter">
	<div class="filterHeader">
	<a href="javascript:;" class="filterClose pe-is-i-close-circle"></a>
	<a href="javascript:;" class="filterToggle pe-is-i-angle-circle-up" title="This button will show or hide this filter box."></a>
	<div class="filter_formName"></div> 
	<div class="filter_element"></div>
	</div>
	<div class="filterBodyContainer">
		<div class="filterErrorContainer" id="filterError_{{id}}"></div>
		<div class="filterBody">
			<div class="filterLogicContainer">
				<!-- filter logic goes here -->
			{{#if showGenericSelect }}
				<select class="genericLogicSelect" currentValue="none">
					<option value="none"></option>
					<option value="!">NOT</option>
					<option value="||" class="filterLogicMultiple">OR</option>
					{{#if showAndOption }}
					  <option value="&&" class="filterLogicMultiple">AND</option>
					{{/if}}
				</select>
 			{{else}}
				<div class="genericLogicSelect">
				</div>
			{{/if}}
			</div>
			{{#if showBlankFilter }}
			<div class="showBlanks"><input type="checkbox" name="selectedBlank" class="includeBlanksCheckbox" />Show Blanks</div>
			{{/if}}
		</div>
	</div>
</div>
</script>

<script id="filterGenericLogicBefore" type="text/x-handlebars-template">
	<div class="filterCombLogicContainer">
		<select class="filterLogic">
			<option value="&&">AND</option>
			<option value="||">OR</option>
		</select>
	</div>
	<div class="filterLogicBeforeContainer">
		<label>
			<input type="checkbox" class="filterLogicBeforeTwo" />
			((
		</label>
		<label>
			<input type="checkbox" class="filterLogicBeforeOne" />
			(
		</label>
	</div>
</script>

<script id="filterGenericLogicAfter" type="text/x-handlebars-template">
	<div class="filterLogicAfterContainer">
		<label>
			<input type="checkbox" class="filterLogicAfterOne" />
			)
		</label>
		<label>
			<input type="checkbox" class="filterLogicAfterTwo" />
			))
		</label>
	</div>
</script>