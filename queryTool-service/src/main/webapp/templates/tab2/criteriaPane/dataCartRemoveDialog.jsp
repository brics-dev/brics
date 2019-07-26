<script id="dataCartRemoveDialog" type="text/x-handlebars-template">
<input type="hidden" name="formUri" id="removeFormUri" value="" />
<div class="dataCartRemoveHeader">DATA FROM THESE STUDIES</div>
<div class="dataCartItemContainer"></div>
</script>

<script id="dataCartRemoveDialogItem" type="text/x-handlebars-template">
	<div class="dataCartRemoveItem" id="remove_{{uri}}">
		<a href="javascript:;" id="removeStudy_{{uri}}" class="dataCartRemoveItemLink">
			Remove the form data from your study
			<span class="glyphicon pe-is-i-close-circle-f"></span>
		</a>	
		<div class="demoCartRemoveItemName">{{name}}</div>

	</div>
</script>