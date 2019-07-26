<script id="filterDataResultPane" type="text/x-handlebars-template">
	<div class="filterPaneContent">
			<div class="filterDataTabsContainer">
				<ul class="row resultPanelUl" style="">
					<li class="resultPaneLi">Studies</li>
				</ul>
			</div>
	</div>
<div class="currentlyFilteringContainer">
	<div class="currentlyFiltering">
		<div class="currentlyFilteringText"></div>
		<div class="currentlyFilteringPills">
			<div class="selectionListPill textFilterPill">
				<span class="pillContent"></span>
				<a href="javascript:;" class="closeButton pe-is-i-close-circle-f"></a>
			</div>
		</div>
		<div class="clearfix"></div>
	</div>
	<div class="resultPaneHeader">
<div class="resultPaneSearch">
		<input type="text" class="resultPaneSearchText" placeholder="Search Studies" />
		<a href="javascript:;" class="resultPaneSearchButton buttonPrimary"><span class="glyphicon pe-is-e-zoom" aria-hidden="true"></span>Search</a>
		<a href="javascript:;" class="resultPaneSearchReset buttonSecondary">Reset Search</a>&nbsp;&nbsp;
		<div class="resultPaneCount"></div>
<ul id="displayOptions" class="nav">
			<li class="buttonWithIcon">
				<a href="javascript:;">Display Options</a>
				<span class="icon pe-is-e-tools"></span>
				<ul>
					<li>
						<a href="javascript:;" class="hideNotAvailable">
						Hide Not Available
						</a>
					</li>
					<li>
						<a href="javascript:;" class="expandAll">
						Expand All
						</a>
					</li>
					<li>
						<a href="javascript:;" class="collapseAll">
						Collapse All
						</a>
					</li>
				</ul>
			</li>
		</ul>
		<div class="clearfix"></div> 
	</div>

		
		
		<div class="clearfix"></div>
	</div>
	<div class="resultPaneContent">
		<div class="resultPaneListEmpty">There are no results to display</div>
		<div class="clearfix resultPaneContentClear"></div>
	</div>
</div>
</script>