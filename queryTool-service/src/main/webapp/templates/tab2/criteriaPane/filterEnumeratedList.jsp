<script id="filterEnumeratedList" type="text/x-handlebars-template">
<div class="filterLogicRow">
		<div class="filterErrorContainer" id="filterError_{{id}}"></div>
		{{#if showFilterLogicSelect }}
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
		{{/if}}
		<div class="floatLeft">
			<div class="toggleBox {{showInclusiveExactToggle}}">
				<strong>Inclusive</strong>&nbsp;
				<label class="switch">
  					<input name="mode" class="filterMode" type="checkbox">
  					<span class="slider round"></span>
				</label>
				&nbsp;<strong>Exact</strong>
<br>
			</div>
			
			<div class="enumeratedList"></div>
		</div>
		<div class="clearfix"></div>

</div>
</script>