<script id="selectionList" type="text/x-handlebars-template">
	<div class="selectionSearchContainer row">
		<input type="text" class="selectionSearchText" placeholder="Search" />
		<a href="javascript:;" class="selectionSearchButton buttonPrimary"><span class="glyphicon pe-is-e-zoom" aria-hidden="true"></span>Search</a>
		<a href="javascript:;" class="selectionSearchReset buttonSecondary">Reset Search</a>
		<div class="clearfix"></div>
	</div>
	<div class="currentlyFiltering">
		
		
		<div class="clearfix"></div>
	</div>
	<div class="filterTextContainer">
		<div class="filterText" style="float:left;"></div>
		<div class="filterPillsContainer" style="float:left;">

			<div class="currentlyFilteringPills">
				<div class="selectionListPill numberSelectedPill">
					<span class="pillContent"></span>
					<a href="javascript:;" class="closeButton pe-is-i-close-circle-f"></a>
				</div>
				<div class="selectionListPill textFilterPill">
					<span class="pillContent"></span>
					<a href="javascript:;" class="closeButton pe-is-i-close-circle-f"></a>
				</div>
			</div>

		</div>
		<div class="clearfix"></div>
	</div>
	<div class="selectionListContainer">
		<div class="selectionListHeader">
			<a href="javascript:;" class="expandCollapseButton" aria-label="collapse filter list">
				<span class="glyphicon pe-is-i-minus-circle" aria-hidden="true"></span>
			</a>
			<div class="filteringType">
				{{filterType}}
			</div>
			<div class="filteringCount">
				{{filterCount}}
			</div>
			<a href="javascript:;" class="resetFilter">Reset Filter</a>
			<div class="clearfix"></div>
		</div>
		<div class="selectionList">
			<div id="selectionListEmpty">There are no results to display</div>
			
		</div>
	</div>
	<div class="clearfix"></div>
</script>