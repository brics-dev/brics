<script type="text/x-handlebars-template" id="hideAllBlankColumns">
    <div id="hideAllBlankCol">
        <a href="javascript:;" id="hideShowBlankButton" class="buttonWithIcon hideBlankCol" title="Deselects all data elements that have no data submitted against them.">
            <span class="icon pe-is-i-inside"></span><span name="hideShowColButtonText">{{hideShowColButtonText}}</span>
        </a>
        <div class="clearfix"></div>
    </div>
</script>

<script type="text/x-handlebars-template" id="selectCriteriaForm">
    <div class="selectCriteriaForm" uri="{{uri}}">
		<div class="selectCriteriaFormHeader">
			<a href="javascript:;" class="formExpandCollapse pe-is-i-minus-circle"></a>
        	<div class="selectCriteriaFormName">{{title}}</div>
			<a href="javascript:;" class="formAddRemoveAll addRemoveAllButton checkbox checkboxChecked"></a>
			<div class="selectCriteriaShowHideText">Show All/Hide in Datatable</div>
		</div>
        <div class="selectCriteriaGroupContainer selectCriteriaCollapsible">
            <div class="clearfix"></div>
        </div>
	<div class="clearfix"></div>
    </div>
</script>

<script type="text/x-handlebars-template" id="selectCriteriaGroup">
    <div class="selectCriteriaGroup" uri="{{uri}}" groupName="{{name}}">
        <div class="selectCriteriaGroupHeader">
            <a href="javascript:;" class="selectCriteriaGroupExpandCollapse pe-is-i-plus-circle"></a>
            {{name}}
			<a href="javascript:;" class="sectionAddRemoveAll addRemoveAllButton checkbox checkboxChecked"></a>
            <div class="clearfix"></div>
        </div>
        <div class="selectCriteriaGroupContent selectCriteriaCollapsible">
            <div class="selectCriteriaTableHeader">
                <div class="selectCriteriaDeNameContainer">Data element name</div>
                <div class="selectCriteriaDeFilterContainer">Filter</div>
				<div class="selectCriteriaDeCheckboxContainer">Show in datatable</div>
                <div class="clearfix"></div>
            </div>
            <div class="selectCriteriaDeContainer">
                <div class="clearfix"></div>
            </div>
        </div>
    </div>
</script>

<script type="text/x-handlebars-template" id="selectCriteriaDe">
    <div class="selectCriteriaDe" uri="{{uri}}" deName="{{name}}" filterType="{{filterType}}">
        <div class="selectCriteriaDeNameContainer">
            {{title}}
            <a href="javascript:;" class="deInformation pe-is-i-info-circle"></a>
        </div>
        <div class="selectCriteriaDeFilterContainer">
            <a href="javascript:;" class="selectCriteriaDeFilter">
                <span class="icon"></span>
            </a>
        </div>
		<div class="selectCriteriaDeCheckboxContainer">
			<input type="checkbox" class="selectCriteriaDeCheckbox" id="{{id}}" checked="checked" />
			<label for="{{id}}"></label>
        </div>
        <div class="clearfix"></div>
    </div>
</script>