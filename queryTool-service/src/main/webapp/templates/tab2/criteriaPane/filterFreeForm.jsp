<script id="filterFreeForm" type="text/x-handlebars-template">
	<div class="filterLogicRow filterFreeFormType">
		<select class="filterLogicSelect" currentValue="none">
			<option value="none" class="filterLogicSingle"></option>
			<option value="!" class="filterLogicSingle">NOT</option>
			{{#if showFilterLogicOr }}
			<option value="||" class="filterLogicMultiple">OR</option>
			{{/if}}

			{{#if showFilterLogicAnd }}
			<option value="&&" class="filterLogicMultiple">AND</option>
			{{/if}}
		</select>

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
			<input type="text" class="filterFreeFormTextBox" />
		</div>
	</div>
	<div class="fliterLogicClear clearfix"></div>
</script>