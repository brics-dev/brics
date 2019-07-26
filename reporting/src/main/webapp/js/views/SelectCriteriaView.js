/**
 * model: query
 */
QT.SelectCriteriaView = BaseView.extend({
    isRendered: false,

    events: {
        "change .selectCriteriaDeCheckbox": "includeInTable",
        "click .selectCriteriaGroupSelectAll": "selectAll",
        "click .selectCriteriaGroupDeselectAll": "deselectAll",
        "click .selectCriteriaGroupExpandCollapse": "expandCollapseGroup",
        "click .selectCriteriaDeFilter": "filter",
        "click .deInformation": "deInformation",
        "click .hideBlankCol": "hideAllBlankCol",
        "click .showBlankCol": "showAllBlankCol"
    },

    initialize: function() {
        EventBus.on("query:formDetailsAvailable", this.reRender, this);
        EventBus.on("add:queryFilter", this.onAddFilter, this);
        EventBus.on("remove:filter", this.onRemoveFilter, this);
        EventBus.on("clearDataCart", this.onClearDataCart, this);
        EventBus.on("hamburgerview:showHideCol", this.selectDeselectColumn, this);
        EventBus.on("query:reset", this.onClearDataCart, this);
        EventBus.on("runQuery", this.onClearDataCart, this);
        EventBus.on("runJoinQuery", this.onClearDataCart, this);
        EventBus.on("query:reRun", this.onClearDataCart, this);

        QT.SelectCriteriaView.__super__.initialize.call(this);
    },

    /**
     * Data looks like this:
     * forms : [
     * 		uri : "",
     * 		repeatableGroups : [
     * 			uri : "",
     * 			name : "",
     * 			position: 0,
     * 			dataElements : [
     * 				uri : "",
     * 				name: "",
     * 				title : "",
     * 				selected : true
     * 			]
     * 		]
     * ]
     */
    render: function() {
        // re-render if needed
        if (this.isRendered) {
            this.empty();
        }

        if (Object.keys(this.model.get("formDetails")).length == 0) {
            if (this.$(".selectCriteriaNoContent").length == 0) {
                this.$(".clearfix").before("<div class=\"selectCriteriaNoContent\">" + Config.language.selectCriteriaEmpty + "</div>");
            } else {
                this.$(".selectCriteriaNoContent").show();
            }
        } else {
            this.$(".selectCriteriaNoContent").hide();
            var hideAllTemplate = TemplateManager.getTemplate("hideAllBlankColumns");
            var formTemplate = TemplateManager.getTemplate("selectCriteriaForm");
            var groupTemplate = TemplateManager.getTemplate("selectCriteriaGroup");
            var deTemplate = TemplateManager.getTemplate("selectCriteriaDe");
            var data = this.model.get("formDetails");

            //add hide all button
            this.$(" > .clearfix").before($(hideAllTemplate(this.model.toJSON())));
            // overwritten inside the loop
            var $formContainer = "There is no data to display";
            for (var i = 0; i < data.length; i++) {
                var formJson = data[i];
                var form = QueryTool.page.get("forms").get(formJson.uri);
                var $formContainer = $(formTemplate(form.attributes));
                var $groupsContainer = $formContainer.find(".selectCriteriaGroupContainer .clearfix");

                var groups = formJson.repeatableGroups;
                var groupLength = groups.length;

                for (var j = 0; j < groupLength; j++) {
                    var group = groups[j];
                    var $groupContent = $(groupTemplate(group));
                    $groupsContainer.before($groupContent);
                    var $elementsContainer = $groupContent.find(".selectCriteriaDeContainer .clearfix");
                    var dataElements = group.dataElements;
                    var dataElementsLength = dataElements.length;

                    for (var k = 0; k < dataElementsLength; k++) {
                        $elementsContainer.before(deTemplate(dataElements[k]));
                    }
                }
                this.$(" > .clearfix").before($formContainer);


            }

        }

        /*Disable the filter button in select criteria view if there is any filter in filter list*/
        var allFilters = this.model.filters.toJson();
        for (var i = 0; i < allFilters.length; i++) {
            var filter = allFilters[i];
            var $button = $('[uri="' + filter.formUri + '"] [groupname="' + filter.groupName + '"] [dename="' + filter.elementName + '"]').find("a.selectCriteriaDeFilter");
            $button.addClass("disabled").prop("disabled", true);
        }

        QT.SelectCriteriaView.__super__.render.call(this);
        this.isRendered = true;
    },

    reRender: function() {
        if (QueryTool.query.get("queryType") == "full" || this.isRendered == false) {
            this.render();
        }
    },

    empty: function() {
        this.$el.html("<div class=\"clearfix\"></div>");
    },

    selectAll: function(event) {
        var $target = $(event.target);
        var $groupContainer = $target.parents(".selectCriteriaGroup").eq(0);
        $groupContainer.find(".selectCriteriaDeCheckbox").each(function() {
            if (!this.checked) {
                $(this).prop("checked", true).trigger("change");
            }
        });
    },

    deselectAll: function(event) {
        var $target = $(event.target);
        var $groupContainer = $target.parents(".selectCriteriaGroup").eq(0);
        var $thisView = this;
        $groupContainer.find(".selectCriteriaDeCheckbox").each(function() {
            if (this.checked) {
                $(this).prop("checked", false).trigger("change");
            }
        });
    },

    expandCollapseGroup: function(event) {
        var target = event.target;
        var $group = $(target).parents(".selectCriteriaGroup").eq(0);
        var $groupContent = $group.find(".selectCriteriaGroupContent");
        if ($groupContent.is(":visible")) {
            $group.find(".selectCriteriaGroupExpandCollapse").removeClass("pe-is-i-minus-circle").addClass("pe-is-i-plus-circle");
            $groupContent.hide();
        } else {
            // collapse all currently visible
            this.$(".selectCriteriaGroupContent:visible").each(function() {
                $(this).prev(".selectCriteriaGroupExpandCollapse").click();
            });

            $group.find(".selectCriteriaGroupExpandCollapse").removeClass("pe-is-i-plus-circle").addClass("pe-is-i-minus-circle");
            $groupContent.show();
        }
    },

    filter: function(event) {

        var $target = $(event.target);
        if (!$target.is(".selectCriteriaDeFilter")) {
            $target = $target.parents(".selectCriteriaDeFilter").eq(0);
        }
        if (!$target.is(":disabled") && !$target.hasClass("disabled")) {
            var $de = $target.parents(".selectCriteriaDe").eq(0);
            var deUri = $de.attr("uri");
            var deName = $de.attr("deName");

            var $rg = $target.parents(".selectCriteriaGroup").eq(0);
            var rgUri = $rg.attr("uri");
            var rgName = $rg.attr("groupName");

            var formUri = $target.parents(".selectCriteriaForm").eq(0).attr("uri");
            //open the panel for filters
            EventBus.trigger("openFilterPane");
            // this method will retrieve the DE details from the server and then call the eventbus
            this.model.getDeFilterDetails(formUri, rgUri, deUri, rgName, deName);
        }
    },

    /**
     * Responds to a filter being added.  If a button is found matching the DE and RG
     * of the filter, mark it as disabled and other on filter processes.
     */
    onAddFilter: function(queryFilterModel) {
        var rgUri = queryFilterModel.get("groupUri");
        var elementName = queryFilterModel.get("elementName");
        this.disableFilterButton(elementName, rgUri);
    },

    /**
     * Responds to a filter being removed.  If a button is found matching the DE and RG
     * of the filter, mark it enabled and other on filter processes.
     */
    onRemoveFilter: function(queryFilterModel) {
        var rgUri = queryFilterModel.get("groupUri");
        var elementName = queryFilterModel.get("elementName");
        this.enableFilterButton(elementName, rgUri);
    },

    disableFilterButton: function(elementName, rgUri) {
        var $rg = this.$('[uri="' + rgUri + '"].selectCriteriaGroup');
        var $de = $rg.find('[deName="' + elementName + '"]');
        var $button = $de.find(".selectCriteriaDeFilter");
        $button.addClass("disabled").prop("disabled", true);
    },

    enableFilterButton: function(elementName, rgUri) {
        var $rg = this.$('[uri="' + rgUri + '"].selectCriteriaGroup');
        var $de = $rg.find('[deName="' + elementName + '"]');
        var $button = $de.find(".selectCriteriaDeFilter");
        $button.removeClass("disabled").prop("disabled", false);
    },

    /**
     * Responds to changes in the checkbox to show/hide the column.
     * NOTE: this is called AFTER the checkbox changes state
     */
    includeInTable: function(event) {
        var $target = $(event.target);

        var $de = $target.parents(".selectCriteriaDe").eq(0);
        var deUri = $de.attr("uri");
        var deName = $de.attr("deName");

        var $rg = $target.parents(".selectCriteriaGroup").eq(0);
        var rgUri = $rg.attr("uri");
        var rgName = $rg.attr("groupName");

        var formUri = $target.parents(".selectCriteriaForm").eq(0).attr("uri");


        // is this event handled before or after the property is updated?
        if ($target.is(":checked")) {
            this.enableFilterButton(deName, rgUri);
        } else {
            this.disableFilterButton(deName, rgUri);
        }

        EventBus.trigger("hamburgerview:showHideCol", {
            formUri: formUri,
            rgName: rgName,
            rgUri: rgUri,
            deUri: deUri,
            deName: deName,
            // inverting this because the state has NOT been changed yet
            // and visible denotes what it should be set TO
            visible: $target.is(":checked")
        });

        /*Disable the filter button if the clicked item is already in filter list*/
        var inFilterList = false;
        var allFilters = this.model.filters.toJson();
        for (var i = 0; i < allFilters.length; i++) {
            if (allFilters[i].formUri == formUri &&
                allFilters[i].groupName == rgName &&
                allFilters[i].elementName == deName) {
                inFilterList = true;
            }
        }
        if (inFilterList == true) {
            this.disableFilterButton(deName, rgUri);
        }
    },

    selectDeselectColumn: function(specs) {
        // find the right checkbox, mark it checked or unchecked
        // we don't have a way to know whether we're showing or hiding so just use the DOM element
        var $form = this.$('.selectCriteriaForm[uri="' + specs.formUri + '"]');
        var $rg = $form.find('.selectCriteriaGroup[groupname="' + specs.rgName + '"]');
        var $de = $rg.find('.selectCriteriaDe[uri="' + specs.deUri + '"]');
        var $checkbox = $de.find(".selectCriteriaDeCheckbox");
        if (specs.visible != $checkbox.is(":checked")) {
            $checkbox.prop("checked", specs.visible);
        }

        var deName = $de.attr("dename");
        $button = $checkbox.parents(".selectCriteriaDe").find("a.selectCriteriaDeFilter");
        if (specs.visible) {
            $button.removeClass("disabled");
        } else {
            $button.addClass("disabled");
        }
    },

    deInformation: function(event) {
        var $target = $(event.target);
        var deUri = $target.parents(".selectCriteriaDe").eq(0).attr("uri");
        var deModel = QueryTool.page.get("dataElements").get(deUri);
        EventBus.trigger("open:details", deModel);
    },
    hideAllBlankCol: function() {
        var $button = $("#hideShowBlankButton");

        $button.removeClass("hideBlankCol");
        $button.addClass("showBlankCol");
        $button.attr("title", "Selects all data elements that have no data submitted against them.");

        EventBus.trigger("query:hideAllBlankColumns");

    },
    showAllBlankCol: function() {
        var $button = $("#hideShowBlankButton");

        $button.removeClass("showBlankCol");
        $button.addClass("hideBlankCol");
        $button.attr("title", "Deselects all data elements that have no data submitted against them.");

        EventBus.trigger("query:showAllBlankColumns");
    },

    onClearDataCart: function() {
        this.empty();
        this.isRendered = false;
        //resets the hide/show column button back to hide.
        var $button = $("#hideShowBlankButton");
        $button.removeClass("showBlankCol");
        $button.addClass("hideBlankCol");
        this.model.set("hideShowColButtonText", "Hide All Blank Columns");
        EventBus.trigger("column:showColumn");
    }
});