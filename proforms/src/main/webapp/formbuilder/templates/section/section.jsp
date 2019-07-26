<script id="sectionTemplate" type="text/x-handlebars-template">
		<div class="sectionHeader row">
			<div class="sectionHeaderLeft col-md-11">
				<a href="javascript:;" class="sectionExpandCollapse{{#if editorCollapsed}} collapsed{{/if}}"></a>
				<a href="javascript:;" class="sectionRepeatable{{#unless isRepeatable}} off{{/unless}}"></a>
				<div class="statusTooltip">
				Repeating Section<br />
				<span class="label">min :</span> <span name="initRepeatedSecs"></span><br />
				<span class="label">max :</span> <span name="maxRepeatedSecs"></span><br />
				<span class="label">group:</span> <span name="repeatableGroupName"></span><br />
				</div>
					
				<a href="javascript:;" class="sectionCollapsible{{#unless isCollapsable}} off{{/unless}}"></a>
				<div class="statusTooltip">This section is collapsible</div>

				<a href="javascript:;" class="sectionGridType{{#unless gridtype}} off{{/unless}}"></a>
				<div class="statusTooltip">This section will be displayed without border or margins</div>
					
				<span class="sectionName" name="name">{{name}}</span>
			</div>
			<div class="sectionHeaderRight col-md-1">
				<div class="row">
					<a href="javascript:;" class="fb-icon-small editButton col-s-6"></a>
					<a href="javascript:;" class="fb-icon-small deleteButton col-s-6"></a>
				</div>
			</div>
		</div>
		<div name="description"></div>
		<div class="questionContainer row">
			{{!-- {{#each questions}}
				{{> question}}
			{{/each}} --}}
		</div>
</script>