<script id="editTableTemplate" type="text/x-handlebars-template">
	<div id="dialog_editTable" class="formcreator_dialog">
		<div class="editorErrorContainer"></div>
		<ul>
			<li><a href="#dialog_editTable_tab1">Table Layout</a></li>
		</ul>
		<div class="tabcontainer" id="dialog_editTable_tab1">
			<div class="row clearfix">
				<div class="col-md-2">
					<label for="numCols" class="required">Number of Columns</label>
				</div>
				<div class="col-md-2">
					<input type="text" id="numCols" name="numCols" value="" style="width: 100%" />
				</div>
				<div class="col-md-3">
					<label for="colHeaders" class="">Include Column Headers</label>
				</div>
				<div class="col-md-1">
					<input type="checkbox" id="colHeaders" name="colHeaders" style="" />
				</div>
				<div class="col-md-3">
					<label for="removeQTextHeaders" class="">Move Question Text To Headers</label>
				</div>
				<div class="col-md-1 center">
					<input type="checkbox" name="copyTextToHeaders" id="removeQTextHeaders" disabled="true" style="" />
				</div>
			</div>
			<div class="row clearfix">
				<div class="col-md-2">
					<label for="numRows" class="required">Number of Rows</label>
				</div>
				<div class="col-md-2">
					<input type="text" id="numRows" name="numRows" value="" style="width: 100%" />
				</div>
				<div class="col-md-3">
					<label for="rowHeaders" class="">Include Row Headers</label>
				</div>
				<div class="col-md-1">
					<input type="checkbox" id="rowHeaders" name="rowHeaders" style="" />
				</div>
				<div class="col-md-3">
					<label for="showQText" class="">Show Question Text</label>
				</div>
				<div class="col-md-1 center">
					<input type="checkbox" name="qTextOptions" id="showQText" style="" />
				</div>
			</div>
			<div class="row clearfix">
				<div class="col-md-4"><!-- empty --></div>
				<div class="col-md-3">
					<label for="showTableTitle" class="">Include Table Title</label>
				</div>
				<div class="col-md-2">
					<input type="checkbox" id="showTableTitle" name="showTableTitle" style="" />
				</div>
			</div>
		</div>
		<div class="clearfix"><!-- clear --></div>
	</div>
</script>