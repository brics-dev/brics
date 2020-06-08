<script id="filterLogicBox" type="text/x-handlebars-template">
	<div id="filterPaneHeaderTitle">
		Query Logic Box
		<a href="javascript:;" class="toggleLogicBox pe-is-i-angle-circle-down" title="This button will show or hide the logic box."></a>
	</div>	
	<textarea id="filterLogicBoxInput" readonly="readonly" style="display:none"></textarea>
	<div class="clearfix"></div>
	<a href="javascript:;" class="buttonWithIcon filterLogicButton disabled" id="filterLogicRunQuery">
		<span class="icon pe-is-i-arrow-right"></span>
		Run Query
	</a>
	<a href="javascript:;" class="buttonWithIcon filterLogicButton disabled" id="filterLogicCopyQuery">
		<span class="icon pe-is-e-add-layer"></span>
		Copy Query
	</a>
	<a href="javascript:;" class="buttonWithIcon filterLogicButton disabled" id="filterLogicClearFilters">
		<span class="icon pe-is-e-trash-1"></span>
		Clear Filters
	</a>
</script>