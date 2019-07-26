<script id="editTextblockTemplate" type="text/x-handlebars-template">
	<div id="dialog_editTextblock" class="formcreator_dialog">
		<div class="editorErrorContainer"></div>
		<ul>
			<li><a href="#dialog_editTextblock_tab1">Basic Settings</a></li>
		</ul>
		<div class="tabcontainer" id="dialog_editTextblock_tab1">
			<div class="row">
				<div class="span2">
					<label for="collapsible" class="left">Text<span class="requiredStar">*</span></label>
				</div>
				<div class="span10">
					<textarea class="tinymce"></textarea>
					<input type="hidden" id="dialog_editTextblock_editorField" name="htmlText" />
				</div>
			</div>
		</div>
	</div>
</script>