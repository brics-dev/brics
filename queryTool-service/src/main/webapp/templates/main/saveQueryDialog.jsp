<script id="saveQueryDialogTemplate" type="text/x-handlebars-template">
	<div id="saveQueryDialog" >
		<div id="saveQueryMsgs">
		</div>
		
		<div id="saveQueryFields">
			<div class="formrow_1">
				<label for="queryName" class="requiredInput">
					Name: <span class="requiredStar">*</span>
				</label>
				<input type="text" id="queryName" size="50" maxlength="100" value="{{name}}" />
			</div>
			
			<div class="formrow_1">
				<label for="queryDesc">Description:</label>
				<textarea id="queryDesc" rows="2" cols="20" maxlength="300">{{description}}</textarea>
			</div>

		<div class="formrow_1">
		<label>Created Date:</label>
		<div name="dateCreated">{{dateCreated}}</div>
		</div>

		<div class="formrow_1">
		<label>Last Updated Date:</label>
		<div name="lastUpdated">{{lastUpdated}}</div>
		</div>
			
			<div class="formrow_1">
				<label for="userSelection">Group/User:</label>
				<select id="userSelection">
					{{#each availableUsers}}
						<option id="usr_{{id}}" value="{{id}}" {{#if disabled}} disabled="disabled" {{/if}} >
							{{lastName}}, {{firstName}}
						</option>
					{{/each}}
				</select>
				<input type="button" id="grantPermissionBtn" class="buttonPrimary" value="Grant Permission" 
					title="Click to grant a permission for the selected user." />
			</div>
			
			<table id="permissionsTable" class="std_table">
				<thead>
					<tr>
						<th>USER/PERMISSION GROUP</th>
						<th>PERMISSION</th>
						<th>REMOVE?</th>
					</tr>
				</thead>
				<tbody>
					{{#each linkedUsers}}
						<tr id="permUsr_{{id}}">
							<td>
								{{lastName}}, {{firstName}}
							</td>
							<td>
								<select class="userPermission">
									{{#each permissions}}
										<option {{#if selected}} selected="selected" {{/if}} value="{{permission}}" >
											{{permission}}
										</option>
									{{/each}}
								</select>
							</td>
							<td>
								<a href="javascript:;" id="delUsr_{{id}}" class="removeLink" >remove</a>
							</td>
						</tr>
					{{/each}}
				</tbody>
			</table>
			
			<div id="saveQueryButtonFooter" class="formbutton" >
				<input type="button" id="cancelSavedQueryBtn" class="buttonSecondary" value="Cancel" title="Click to discard changes and close this dialog box."/>
				<input type="button" id="deleteSavedQueryBtn" class="buttonSecondary" style="display: none;"
					value="Delete" title="Click to delete the current defined query from the system."/>
				<input type="button" id="saveQueryBtn" class="buttonPrimary" value="Save" title="Click to save the query."/>
			</div>
		</div>
	</div>
</script>