<script id="dataElementSelectionList" type="text/x-handlebars-template">
	<div class="selectDeButtonContainer">
		<a href="javascript:;" class="selectDeButton buttonPrimary">
			<span class="glyphicon pe-is-e-zoom" aria-hidden="true"></span>
			Select Data Elements
		</a>
		<div class="selectDeDescription">Data Elements are hidden until selected using the button to the left</div>
		
	</div>
	<div class="currentlyFiltering">
		<div class="currentlyFilteringText"></div>
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
		<div class="clearfix"></div>
	</div>
	<div class="filterTextContainer">
		<div class="filterText"></div>
		<div class="filterPillsContainer"></div>
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
			<div class="clearfix"></div>
		</div>
		<div class="selectionList">
			<div id="selectionListEmpty">There are no results to display</div>
			
		</div>
	</div>
	<div class="clearfix"></div>
</script>