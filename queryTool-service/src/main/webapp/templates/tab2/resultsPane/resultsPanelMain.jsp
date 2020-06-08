<script type="text/x-handlebars-template" id="resultsPanelMain">
<ul>
	<li class="resultsTabLink">
		<a href="#resultsSelectCriteria">Select Criteria</a>
	</li>
	<li class="resultsTabLink">
		<a href="#resultsDatatable" id="dataTableViewTab">Datatable View</a>
	</li>
	<div class="resultsTabsButtonContainer">
	
		<!--<a href="javascript:;" id="rboxButton" class="buttonWithIcon disabled" style="display:none">
			<span class="icon pe-is-i-inside"></span>
			Rbox
		</a> -->
	
		<ul id="dataOptions" class="nav disabled">
			<li class="buttonWithIcon">
				<a href="javascript:;">Download Options</a>
				<span class="icon pe-is-i-inside"></span>
				<ul>
					<li>
						<a href="javascript:;" id="downloadToQueue">
						Download to Queue
						</a>
					</li>
					<li>
						<a href="javascript:;" id="sendToMetaStudy">
						Send to Meta Study
						</a>
					</li>
					<li>
						<a href="javascript:;" id="viewDownloadQueue">
						View Download Queue
						</a>
					</li>
				</ul>
			</li>
		</ul>
		<ul id="outputCodesDropdown" class="nav disabled">
			<li class="buttonWithIcon">
				<a href="javascript:;" class="outputCodeDropdown">Permissible Value</a>
				<span class="icon right pe-is-i-angle-down"></span>
				<ul id="outputCodesDropdownList">
					<li>
						<a href="javascript:;" id="pv" class="outputCodeOption">Permissible Value</a>
					</li>
					<li>
						<a href="javascript:;" id="Permissible Value Description" class="outputCodeOption">Permissible Value Description</a>
					</li>
					<li>
						<a href="javascript:;" id="outputCode" class="outputCodeOption">Output Code</a>
					</li>
				</ul>

			</li>
		</ul>
	</div>
</ul>
<div class="tab-content">
	<div id="resultsSelectCriteria" class="resultsTab">
		<div class="clearfix"></div>
	</div>
	<div id="resultsDatatable" class="resultsTab">
		<div id="pageCountControl"></div>
		<div id="tableTitleHeader" class="group">
			<div id="formDescriptions" >
				<span class="formJoinDescription"></span>
        	</div>
			<div id="queryResultsCount"></div>
		</div>
		<div id="tableNotificationMessage">
		</div>
		<div id="resultsDatatableContainer">

		</div>
	</div>

</div>
</script>

<script type="text/x-handlebars-template" id="schemaList">
	{{#each schemaList}}
		<li class="schemaOutputCodes"><a href="javascript:;" id="{{{this}}}" class="outputCodeOption">{{{this}}}</a></li>
	{{/each}}
</script>
