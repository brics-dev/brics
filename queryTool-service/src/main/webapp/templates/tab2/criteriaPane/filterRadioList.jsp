<script id="filterRadioList" type="text/x-handlebars-template">
<div class="filterHeader">
<a href="javascript:;" class="filterClose pe-is-i-close-circle"></a>
<a href="javascript:;" class="filterToggle pe-is-i-angle-circle-up"></a>
<div class="filter_formName"></div> 
<div class="filter_element"></div>
</div>
<div class="filterBodyContainer">
	<div class="filterErrorContainer" id="filterError_{{id}}"></div>
	<div class="filterBody">
		<div class="radioList"></div>
		<div class="includeBlanks"><input type="checkbox" name="selectedBlank" class="includeBlanksCheckbox" />Include Blanks</div>
	</div>
</div>
</script>