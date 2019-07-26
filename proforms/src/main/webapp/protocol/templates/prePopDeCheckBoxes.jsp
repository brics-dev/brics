<script id="prePopDeCheckBoxTemplate" type="text/x-handlebars-template">
	<table id="prepopDataElements" >
		{{#each prePopDeArray}}
			<tr id="{{shortName}}_prepop" >
				<td style="width: 20px;" >
					<input type="checkbox" value="{{shortName}}" class="prePopDeChkBox" {{#if isSelected}} checked="checked" {{/if}} 
						{{#if isUnchangeable}} disabled="disabled" {{/if}}/>
				</td>
				<td class="prePopTitle" >{{title}}</td>
			</tr>
		{{/each}}
	</table>
</script>