<script id="filterDateRange" type="text/x-handlebars-template">
	<div class="filterLogicRow filterDateRangeType">
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
			<input type="text" class="filterDateMinTextBox" placeholder="begin date" /> -
			<input type="text" class="filterDateMaxTextBox" placeholder="end date" />
		</div>
	</div>
	<div class="fliterLogicClear clearfix"></div>
</script>