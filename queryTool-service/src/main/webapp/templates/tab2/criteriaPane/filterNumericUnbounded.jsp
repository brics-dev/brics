<script id="filterNumericUnbounded" type="text/x-handlebars-template">
	<div class="filterLogicRow filterNumericUnboundedType">
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
			<input type="text" class="filterNumericRangeMinTextBox" placeholder="min" /> - 
			<input type="text" class="filterNumericRangeMaxTextBox" placeholder="max" />
		</div>
	</div>
	<div class="fliterLogicClear clearfix"></div>
</script>