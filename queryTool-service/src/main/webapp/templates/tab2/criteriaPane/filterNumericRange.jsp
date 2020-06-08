<script id="filterNumericRange" type="text/x-handlebars-template">
	<div class="filterLogicRow filterNumericRangeType">
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
		<div class="filterNumericRangeInputContainer">
			<div><input type="text" class="filterNumericRangeMinTextBox" /></div>
			<div class="filterNumericRangeSlider"></div>
			<div><input type="text" class="filterNumericRangeMaxTextBox" /></div>
		</div>
	</div>
	<div class="fliterLogicClear clearfix"></div>
</script>