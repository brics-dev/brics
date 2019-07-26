<script id="selectDeDialogTemplate" type="text/x-handlebars-template">
	<div id="selectDe_error"></div>
	<div class="selectDe_header">
		<ul class="nav">
			<li class="selectDe_searchLocations">
				Search Locations
				<span class="glyphicon pe-is-i-angle-down"></span>
				<ul>
					<li>
						<div class="selectDe_searchLocationDropdown">
							<div><input type="checkbox" checked="checked" value="keyword" name="locationKeyword" class="selectDe_locationCheckbox">Key Words</div>
							<div><input type="checkbox" checked="checked" value="description" name="locationDescription" class="selectDe_locationCheckbox">Description</div>
							<div><input type="checkbox" checked="checked" value="permissibleValue" name="locationPermissibleValue" class="selectDe_locationCheckbox">Permissible Values</div>
							<div><input type="checkbox" checked="checked" value="title" name="locationTitle" class="selectDe_locationCheckbox">Title</div>
							<div><input type="checkbox" checked="checked" value="label" name="locationLabel" class="selectDe_locationCheckbox">Labels</div>
							<div><input type="checkbox" checked="checked" value="externalId" name="locationExternalId" class="selectDe_locationCheckbox">External ID</div>
							<div><input type="checkbox" checked="checked" value="varName" name="locationVarName" class="selectDe_locationCheckbox">Variable Name</div>
						</div>
					</li>
				</ul>
			</li>
		</ul>
		<input type="text" class="selectDe_textSearch" name="textSearch" />
		<a href="javascript:;" id="selectDe_searchButton" class="buttonPrimary">
			<span class="glyphicon pe-is-e-zoom"></span>
			Search
		</a>
		<div class="selectDe_wholeWordSelector">
			<input type="checkbox" id="selectDe_wholeWord" name="wholeWord" />
			<label for="selectDe_wholeWord">Whole Word or Phrase</label>
		</div>
		<div class="clearfix"></div>
	</div>
	<div class="selectDe_body">
		<div class="selectDe_facets">
			<div class="facet-form-field">
				<span class="selectDe_facetHeader">Element Type</span>
				<ul id="elementTypeFilters">
					<li>
						<input type="checkbox" value="CDE" name="CDE" class="selectDe_selectionCheckbox" />
						<label>Common Data Element</label>
					</li>
					<li>
						<input type="checkbox" value="UDE" name="UDE" class="selectDe_selectionCheckbox" />
						<label>Unique Data Element</label>
					</li>
				</ul>
			</div>
			<div class="facet-form-field">
				<span class="selectDe_facetHeader">Disease</span>
				<ul id="diseaseFilters"></ul>
			</div>
			<div class="facet-form-field">
				<span class="selectDe_facetHeader">Population</span>
				<ul id="populationFilters"></ul>
			</div>
		</div>
		<div id="selectDe_mainArea">
			<div id="selectDe_tableWrapper">
				<table id="selectDeTableContainer" class="dataTable">
				</table>
			</div>
		</div>
		<div class="clearfix"></div>
	</div>
	<div class="selectDe_footer">
	
	</div>
</script>

<script id="selectDeDialogFilterItem" type="text/x-handlebars-template">
<li>
	<div class="selectDe_filterItem">
		<input type="checkbox" value="{{filter}}" class="selectDe_selectionCheckbox" />
		<label>{{filter}}</label>
	</div>
</li>
</script>