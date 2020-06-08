<script id="filterRadioList" type="text/x-handlebars-template">
<div class="filterLogicRow">
	<div class="filterErrorContainer" id="filterError_{{id}}"></div>
	<select class="filterLogicSelect floatLeft" currentValue="none">
			<option value="none" class="filterLogicSingle"></option>
			<option value="!" class="filterLogicSingle">NOT</option>
			{{#if showFilterLogicOr }}
			<option value="||" class="filterLogicMultiple">OR</option>
			{{/if}}

			{{#if showFilterLogicAnd }}
			<option value="&&" class="filterLogicMultiple">AND</option>
			{{/if}}
		</select>
		<div class="floatLeft">
		<div class="radioList"></div>
	</div>
<div class="clearfix"></div>
</div>
</script>