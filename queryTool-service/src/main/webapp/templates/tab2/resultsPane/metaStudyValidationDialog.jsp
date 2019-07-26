<script id="metaStudyValidationDialogTemplate" type="text/x-handlebars-template">
	<div id="sendToMetaStudyValidationDialog">
		<div class="metaStudyValidationDialogMessage">
			You have some named items that will overwrite existing items in this meta study.
			</br>
 			Please review these warnings and only continue if you are certain you want to overwrite the item(s).
			</br>
			<ul>
			{{#if queryMessage}}
				<li>{{queryMessage}}</li>
			{{/if}}

			{{#if fileMessage}}
				<li>{{fileMessage}}</li>
			{{/if}}

			</ul>
		</div>
	</div>
</script>