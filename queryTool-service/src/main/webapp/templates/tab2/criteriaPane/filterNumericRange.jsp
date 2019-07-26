<script id="filterNumericRange" type="text/x-handlebars-template">
<div class="filterHeader">
<a href="javascript:;" class="filterClose pe-is-i-close-circle"></a>
<a href="javascript:;" class="filterToggle pe-is-i-angle-circle-up"></a>
<div class="filter_formName"></div> 
<div class="filter_element"></div>
</div>
<div class="filterBodyContainer">
	<div class="filterErrorContainer" id="filterError_{{id}}"></div>
	<div class="filterBody">
		<div class="filterNumericRangeValidation"></div>
		<div class="filterNumericRangeSlider"></div>
		<div><input type="text" name="selectedMinimum" class="filterNumericRangeMinTextBox" /></div>
		<div><input type="text" name="selectedMaximum" class="filterNumericRangeMaxTextBox" /></div>
		<div class="includeBlanks"><input type="checkbox" name="selectedBlank" class="includeBlanksCheckbox" />Include Blanks</div>
	</div>
</div>
</script>