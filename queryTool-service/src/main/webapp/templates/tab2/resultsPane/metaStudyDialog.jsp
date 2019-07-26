<script id="metaStudyDialog" type="text/x-handlebars-template">
	<div id="metaStudyDialog" >
		<div id="metaStudyMessages">
		</div>
		<div class="dialogMessage">
		Send the query, datatable results, or both to the meta study
		</div>
		<div id="metaStudyTable">
			<table class="std_table" id="metaStudyTable_table">
				<tr>
					<th></th>
					<th>Meta Study Title</th>
					<th>Description</th>
				</tr>
						
			</table>
			<div id="metaStudyNotification">
			</div>
		</div>
		
		<div class="savedQueryFields">
			<form>
				<div class="savedQueryField_saveFilters formrow_1">
					<label>Save query filters to meta study:</label>
					<div class="savedQueryField">
						<input type="radio" name="saveFilters" id="savedQueryField_saveFilters_yes" value="yes" />
						<label class="inputLabel" for="savedQueryField_saveFilters_yes">Yes</label>
						<input type="radio" name="saveFilters" id="savedQueryField_saveFilters_no" value="no" />
						<label class="inputLabel" for="savedQueryField_saveFilters_no">No</label>
					</div>
				</div>
				<div class="savedQueryField_name formrow_1">
					<label for="savedQueryField_name" class="requiredInput">Saved Query Name:<span class="requiredStar">*</span></label>
					<input type="text" name="queryName" id="savedQueryField_name" />
					<span id="validateQueryName"></span>
				</div>			
				<div class="savedQueryField_description formrow_1">
					<label for="savedQueryField_desc">Description:</label>
					<textarea id="savedQueryField_desc" name="queryDescription"></textarea>
				</div>
				<div class="savedQueryField_saveData formrow_1">
					<label>Save filtered data file to meta study:</label>
					<div class="savedQueryField">
						<input type="radio" name="saveData" id="savedQueryField_saveData_yes" value="yes" />
						<label class="inputLabel" for="savedQueryField_saveData_yes">Yes</label>
						<input type="radio" name="saveData" id="savedQueryField_saveData_no" value="no" />
						<label class="inputLabel" for="savedQueryField_saveData_no">No</label>
					</div>
				</div>
				<div class="savedQueryField_dataName formrow_1">
					<label class="requiredInput">Data File Name<span class="requiredStar">*</span></label>
					<input type="text" name="dataName" id="savedQueryField_dataName" />
					<span id="validateDataName"></span>
				</div>
				<div class="savedQueryField_dataDesc formrow_1">
					<label class="requiredInput">Description<span class="requiredStar">*</span></label>
					<textarea id="savedQueryField_dataDesc" name="dataDescription"></textarea>
				</div>
			</form>
		</div>
	</div>
</script>

<script id="metaStudyDialogItem" type="text/x-handlebars-template">
<tr>
	<td><input type="radio" name="metaStudySelector" class="metaStudySelector" value="{{id}}" /></td>
	<td>{{title}}</td>
	<td>{{abstractText}}</td>
</tr>
</script>