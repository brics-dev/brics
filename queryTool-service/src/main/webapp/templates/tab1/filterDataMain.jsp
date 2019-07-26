<script id="filterDataMain" type="text/x-handlebars-template">
	<div class="filterPane fullHeight">
		<div class="filterPaneContent">
			<div class="filterDataTabsContainer">
				<ul class="row">
					<li class=""><a href="#StudiesTab">Studies</a></li>
					<li class=""><a href="#FormsTab">Forms</a></li>
					<li class=""><a href="#DataElementsTab">Data Elements</a></li>
					<li class=""><a href="#DefinedQueriesTab">Defined Queries</a></li>
				</ul>
			</div>
		</div>
		<div class="row tabContainer">
			<div class="tab-content">
				<div id="StudiesTab" class="tab-pane selectionListPanel">
					loading studies...
				</div>
				<div id="FormsTab" class="tab-pane selectionListPanel">
					loading forms...
				</div>
				<div id="DataElementsTab" class="tab-pane selectionListPanel">
					loading data elements...
				</div>
				<div id="DefinedQueriesTab" class="tab-pane selectionListPanel">
					loading saved queries...
				</div>
			</div>
		</div>
	</div>
	<div class="resultPane fullHeight">
		<div class="resultPaneSelectStudies resultPaneSelect">
			<!-- fill in from filterDataResultPane -->
		</div>
		<div class="resultPaneSelectForms resultPaneSelect">
			<!-- fill in from filterDataResultPane -->
		</div>
		<div class="resultPaneSelectDataElements resultPaneSelect">
			<!-- fill in from filterDataResultPane -->
		</div>
		<div class="resultPaneSelectDefinedQueries resultPaneSelect">
			<!-- fill in from filterDataResultPane -->
		</div>
	</div>
</script>