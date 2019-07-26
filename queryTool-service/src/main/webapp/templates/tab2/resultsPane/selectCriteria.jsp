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
        <div class="selectCriteriaFormName">{{title}}</div>
        <div class="selectCriteriaGroupContainer">
            <div class="clearfix"></div>
        </div>
    </div>
</script>

<script type="text/x-handlebars-template" id="selectCriteriaGroup">
    <div class="selectCriteriaGroup" uri="{{uri}}" groupName="{{name}}">
        <div class="selectCriteriaGroupHeader">
            <a href="javascript:;" class="selectCriteriaGroupExpandCollapse pe-is-i-plus-circle"></a>
            {{name}}
            <div class="clearfix"></div>
        </div>
        <div class="selectCriteriaGroupContent">
            <div class="selectCriteriaGroupButtons">
                <a href="javascript:;" class="selectCriteriaGroupSelectAll buttonWithIcon">
                    <span class="icon pe-is-i-check-square-1"></span> Select All
                </a>
                <a href="javascript:;" class="selectCriteriaGroupDeselectAll buttonWithIcon">
                    <span class="icon pe-is-i-close-square"></span> Deselect All
                </a>
                <span class="selectCriteriaGroupButtonsText">Data Elements in the <span class="selectCriteriaGroupSelectionName">{{name}}</span> group</span>
                <div class="clearfix"></div>
            </div>
            <div class="selectCriteriaTableHeader">
                <div class="selectCriteriaDeNameContainer">Data element name</div>
                <div class="selectCriteriaDeCheckboxContainer">Include in datatable</div>
                <div class="selectCriteriaFilterContainer">Filter by this element</div>
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
        <div class="selectCriteriaDeCheckboxContainer">
            <input type="checkbox" class="selectCriteriaDeCheckbox" checked="checked" />
        </div>
        <div class="selectCriteriaDeFilterContainer">
            <a href="javascript:;" class="selectCriteriaDeFilter buttonWithIcon">
                <span class="icon pe-is-i-sliders-circle"></span>
                <span class="selectCriteriaDeFilterText">Filter</span>
            </a>
        </div>
        <div class="clearfix"></div>
    </div>
</script>