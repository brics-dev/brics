<script id="viewSavedQueryTemplate" type="text/x-handlebars-template">
<div class="viewQuery_studyList">
	<div class="viewQuery_header viewQuery_studiesHeader">
		<div class="viewQuery_headerText">Studies</div>
	</div>
	<div class="viewQuery_studiesContainer">
	<!-- for all studies -->
	</div>
</div>

<div class="viewQuery_rightSide">
	<div class="viewQuery_header viewQuery_queryHeader">
		<span class="viewQuery_detailsPlusMinus viewQuery_minus"> - </span>
		<span class="viewQuery_detailsTitle">Saved Query Details:</span> 
		<span class="viewQuery_queryName" name="name">{{name}}</span>
	</div>
	<div class="viewQuery_queryDetails">
		<div class="viewQuery_label">Name</div>
		<div class="viewQuery_value" name="name">{{name}}</div>
		<div class="clearfix"></div>

		<div class="viewQuery_label">Description</div>
		<div class="viewQuery_value" name="description">{{description}}</div>
		<div class="clearfix"></div>

		<div class="viewQuery_label">Permissions</div>
		<div class="viewQuery_value">
			<table class="display-data">
				<thead>
					<tr>
						<th>User/Permission Group</th>
						<th>Permission</th>
					</tr>
				</thead>
				<tbody>
					{{#each linkedUsers}}
					<tr>
						<td>{{lastName}}, {{firstName}}</td>
						<td>{{assignedPermission.permission}}</td>
					</tr>
					{{/each}}
				</tbody>
			</table>

		</div>
		<div class="clearfix"></div>
	</div>
	
	<div class="viewQuery_header viewQuery_formsHeader">Forms <span class="viewQuery_formCount"></span></div>
	<div class="viewQuery_formList">
		Select a Study in the left menu to view its Forms.
		<!-- for all forms -->
		<!-- formItem template -->
	</div>
</div>
</script>

<script id="formItem" type="text/x-handlebars-template">
<div class="viewQuery_formHeader">
	<span class="viewQuery_plusMinus viewQuery_plus"> + </span>
	<span class="viewQuery_formName" name="name">{{name}}</span>
	<span class="viewQuery_formFiltered"></span>
</div>
<div class="viewQuery_formData">
				
	<!-- For all groups in form -->
	<!-- rgItem template -->
</div>
</script>

<script id="rgItem" type="text/x-handlebars-template">
<div class="viewQuery_rgName" name="name">{{name}}</div>
<div class="viewQuery_deListContainer">
	<!-- For all data elements in RG -->
	<!--  deItem template -->
</div>
</script>

<script id="deItem" type="text/x-handlebars-template">
	<div class="viewQuery_filterIcon"></div>
	<div class="viewQuery_filterText">
		<div class="viewQuery_deTitle" name="name">{{name}}</div>
		<div class="viewQuery_filterValue"></div>
	</div>
	<div class="clearfix"></div>
</script>

<script id="study" type="text/x-handlebars-template">
<a class="viewQuery_studyItemLink">
	<span class="viewQuery_studyName" name="title">{{title}}</span> 
	(<span class="viewQuery_studyFormCount" name="formCount">{{formCount}}</span>)
	<span class="viewQuery_studyFiltered"></span>
</a>
</script>
