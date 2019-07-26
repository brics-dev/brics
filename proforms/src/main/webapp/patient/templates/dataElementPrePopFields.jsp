<script id="dePrePopFields" type="text/x-handlebars-template">
	{{#each prePopArray}}
		<div class="formrow_1" >
			<label for="{{shortName}}" style="width: 170px;" >
				{{title}}
			</label>
			<input type="text" id="{{shortName}}" name="{{shortName}}" value="{{prePopValue}}"
				{{#if isDateField}} class="dateTimeField validateMe" {{/if}} />
		</div>
	{{/each}}
</script>