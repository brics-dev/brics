<script id="filterFreeFormLarge" type="text/x-handlebars-template">
	<div class="filterLogicRow filterFreeFormLargeType">
		{{#if showFilterLogicSelect }}
		<select class="filterLogicSelect" currentValue="none">
			<option value="none"></option>
			<option value="!" class="filterLogicSingle">NOT</option>

			{{#if showFilterLogicOr }}
			<option value="||" class="filterLogicMultiple">OR</option>
			{{/if}}

			{{#if showFilterLogicAnd }}
			<option value="&&" class="filterLogicMultiple">AND</option>
			{{/if}}
		</select>
		{{/if}}
		<div class="filterInputContainer">
			<div class="toggleBox {{showInclusiveExactToggle}}">
				<strong>Inclusive</strong>&nbsp;
				<label class="switch">
  					<input name="mode" class="filterMode" type="checkbox">
  					<span class="slider round"></span>
				</label>
				&nbsp;<strong>Exact</strong>
				<br>
			</div>
			<textarea rows="2" cols="25" class="filterFreeFormTextBox" title="Entries should be delimited by a semi-colon(;) or new line"/>
		</div>
	</div>
	<div class="fliterLogicClear clearfix"></div>
</script>