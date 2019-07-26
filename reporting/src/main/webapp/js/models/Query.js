/**
 * 
 */
QT.Query = BaseModel.extend({

    defaults: {
        formDetails: null,
        tableResults: {},
        offset: 0,
        limit: 20,
        sortColName: "",
        sortOrder: "asc",
        selectedForms: [],
        totalRecords: 0,
        formUris: [],
        hideShowColButtonText: "Hide All Blank Columns",
        hideShowColButtonClass: "hideBlankCol",
        queryType: "full" // || "data" for only-data queries.  Used for determining how the rest of the page should handle this
    },

    filters: null,

    initialize: function() {
        this.filters = new QT.QueryFilters();
        this.set("formDetails", new QT.QueryForms());
        EventBus.on("runQuery", this.performQuery, this);
        EventBus.on("runJoinQuery", this.performQuery, this);
        EventBus.on("runExpandQuery", this.expandQuery, this);
        EventBus.on("runCollapseQuery", this.collapseQuery, this);
        EventBus.on("runSort", this.applySort, this);
        EventBus.on("applyFilters", this.applyFilters, this);
        EventBus.on("applyFiltersWithoutRendering", this.applyFiltersWithoutRendering, this);
        EventBus.on("query:dataAvailable", this.updateSelectCriteria, this);
        EventBus.on("remove:filter", this.removeFilter, this);
        EventBus.on("remove:visualFilter", this.removeVisualFilter, this);
        EventBus.on("addBiosample", this.addBiosample, this);
        EventBus.on("addBiosamples", this.addBiosamples, this);
        EventBus.on("clearDataCart", this.onClearDataCart, this);
        EventBus.on("query:reset", this.removeAllFilters, this);
        EventBus.on("query:reRun", this.reRunQuery, this);
        EventBus.on("query:hideAllBlankColumns", this.hideAllBlankColumns, this);
        EventBus.on("query:showAllBlankColumns", this.showAllBlankColumns, this);
        EventBus.on("query:selectDe", this.selectDataElement, this);
        EventBus.on("query:deselectDe", this.deselectDataElement, this);
    },

    loadForms: function(formArray) {
        var collection = this.get("formDetails");
        for (var i = 0; i < formArray; i++) {
            var form = formArray[i];
            var formModel = collection.create(form);
            formModel.loadRgs(form.repeatableGroups);
        }

        this.set("formDetails", collection);
    },

    applyFiltersWithoutRendering: function() {
        $.ajaxSettings.traditional = true;
        var query = this;
        var filtersJson = JSON.stringify(this.filters.toJson());
        $.ajax({
            type: "POST",
            cache: false,
            url: "service/dataCart/applyFiltersWithoutResults",
            data: {
                filters: filtersJson,
                offset: this.get("offset"),
                limit: this.get("limit"),
                sortColName: this.get("sortColName"),
                sortOrder: this.get("sortOrder")
            },
            success: function(data, textStatus, jqXHR) {
                // Do nothing
            },
            error: function() {
            	if ( window.console && console.log ){
                console.log("error applying filters");
            	}
            }
        });
    },

    applyFilters: function() {
        EventBus.trigger("open:processing", "Applying Filters");
        EventBus.trigger("DataTableView:removeQueryListener");
        $.ajaxSettings.traditional = true;
        var query = this;
        var filtersJson = JSON.stringify(this.filters.toJson());
        $.ajax({
            type: "POST",
            cache: false,
            url: "service/dataCart/applyFilters",
            data: {
                filters: filtersJson,
                offset: this.get("offset"),
                limit: this.get("limit"),
                sortColName: this.get("sortColName"),
                sortOrder: this.get("sortOrder")
            },
            success: function(data, textStatus, jqXHR) {
                query.set("queryType", "data");
                query.filtersQueryComplete.call(query, data, true);
                EventBus.trigger("renderResults", this);
                EventBus.trigger("DataTableView:addQueryListener", this);
            },
            error: function() {
                // TODO: fill in
                EventBus.trigger("close:processing");
                if ( window.console && console.log ){
                console.log("error applying filters");
                }
            }
        });
    },

    filtersQueryComplete: function(data, updateSelectCriteria) {
        if (typeof data.error !== "undefined") {
            $.ibisMessaging("dialog", "error", "There was an error processing your query.");
        } else {
            if (this.get("tableResults") != undefined || this.get("tableResults").headers != undefined) {
                $.extend(this.get("tableResults"), data);
                // manually triggering here so we can use $.extend above to merge results into original
                this.trigger("change:tableResults", this.get("tableResults"));
            } else {
                this.set("tableResults", data);
            }

            if (typeof updateSelectCriteria == "undefined") {
                updateSelectCriteria = true;
            }
            if (updateSelectCriteria) {
                this.updateSelectCriteria();
            }
        }
        EventBus.trigger("close:processing");
    },

    queryComplete: function(data, updateSelectCriteria) {
        if (typeof data.error !== "undefined") {
            $.ibisMessaging("dialog", "error", "There was an error processing your query.");
        } else {
            if (typeof updateSelectCriteria == "undefined") {
                updateSelectCriteria = true;
            }
            // removes the slash before double quotes
            // there may not be header information if we're doing apply filter
            if (typeof data.header !== "undefined") {
                data.header = JSON.parse(data.header.replace('\"', '"'));
            }

            EventBus.trigger("change:dataTab", "resultsDatatable");
            data.data = JSON.parse(data.data.replace('\"', '"'));
            this.set("tableResults", data);
            this.set("totalRecords", data.data.iTotalRecords);

            if (updateSelectCriteria) {
                this.updateSelectCriteria();
            }
        }
    },
    paginateQueryComplete: function(data, updateSelectCriteria) {
        if (typeof data.error !== "undefined") {
            $.ibisMessaging("dialog", "error", "There was an error processing your query during pagination.");
        } else {
            if (typeof updateSelectCriteria == "undefined") {
                updateSelectCriteria = true;
            }
            this.set("tableResults", data);
            this.set("totalRecords", data.iTotalRecords);

            if (updateSelectCriteria) {
                this.updateSelectCriteria();
            }
        }
    },

    /**
     * performs a query, gets the result table
     * 
     * @param formUris array of form URIs
     */
    performQuery: function(obj) {
        var formUris = obj.formUris;

        if (formUris && formUris.length > 0) {
            this.set("formUris", formUris);
            $.ajaxSettings.traditional = true;
            var query = this;
            //We assume that when the query is ran we are getting initial data, so if these values have changed, they should be updated to default
            this.set("offset", 0);
            this.set("limit", 20);
            this.set("sortColName", "");
            this.set("sortOrder", "asc");
            EventBus.trigger("open:processing", "performing query...");
            $.ajax({
                type: "GET",
                cache : false,
                url: "service/dataCart/runQuery",
                data: {
                    formUris: formUris,
                    offset: this.get("offset"),
                    limit: this.get("limit"),
                    sortColName: this.get("sortColName"),
                    sortOrder: this.get("sortOrder")
                },
                success: function(data, textStatus, jqXHR) {
                    query.set("queryType", "full");
                    if (typeof data.allHaveGuid !== "undefined") {
                        $.ibisMessaging("dialog", "warning", "A form you are attempting to Join does not have a valid GUID, please select again.");
                        EventBus.trigger("close:processing");
                    } else if (typeof data.hasMatchingGuid !== "undefined") {
                        $.ibisMessaging("dialog", "warning", "There is no matching data between the selected forms.");
                        EventBus.trigger("close:processing");
                    } else {
                        EventBus.trigger("close:processing");
                        query.queryComplete.call(query, data, true);
                    }

                },
                error: function() {
                    // TODO: fill in
                }
            });
        } else {
            EventBus.trigger("close:processing");
        }
    },

    reRunQuery: function() {
        this.performQuery({
            notOnActivate: false,
            formUris: this.get("formUris")
        });
    },

    updateSelectCriteria: function() {
        var query = this;
        $.ajaxSettings.traditional = true;
        $.ajax({
            type: "GET",
            cache : false,
            url: "service/dataCart/selectedFormDetails",
            success: function(data, textStatus, jqXHR) {
                query.set("formDetails", data);
                EventBus.trigger("query:formDetailsAvailable");
            },
            error: function() {
                // TODO: fill in
            }
        });
    },

    getDeFilterDetails: function(formUri, rgUri, deUri, rgName, deName, extraData) {
        if (typeof extraData == "undefined") {
            extraData = {};
        }

        var query = this;
        $.ajaxSettings.traditional = true;
        $.ajax({
            type: "GET",
            cache : false,
            url: "service/dataCart/deFilterDetails",
            data: {
                formUri: formUri,
                rgName: rgName,
                deName: deName
            },
            success: function(data, textStatus, jqXHR) {
                var localData = {
                    formUri: formUri,
                    groupUri: rgUri,
                    elementUri: deUri,
                    groupName: rgName,
                    elementName: deName
                };
                var combinedData = $.extend({}, data, localData, extraData);
                var model = new QT.QueryFilter();
                model.fromResponseJson(combinedData);
                EventBus.trigger("add:queryFilter", model);
            },
            error: function() {
                // TODO: fill in
            }
        });
    },

    deselectDataElement: function(colModel) {
        $.ajaxSettings.traditional = true;
        $.ajax({
            type: "POST",
            cache: false,
            url: "service/dataCart/element/deselect",
            data: {
                formUri: colModel.formUri,
                rgName: colModel.rgName,
                deName: colModel.get("name")
            },
            success: function(data, textStatus, jqXHR) {
                // TODO: do we need to handle this?  Probably update local data somehow
            },
            error: function() {
                // TODO: fill in
            }
        });
    },

    selectDataElement: function(colModel) {
        $.ajaxSettings.traditional = true;
        $.ajax({
            type: "POST",
            cache: false,
            url: "service/dataCart/element/select",
            data: {
                formUri: colModel.formUri,
                rgName: colModel.rgName,
                deName: colModel.get("name")
            },
            success: function(data, textStatus, jqXHR) {
                // TODO: do we need to handle this?  Probably update local data somehow
            },
            error: function() {
                // TODO: fill in
            }
        });
    },

    removeAllFilters: function() {
        this.filters.reset();
        EventBus.trigger("applyFiltersWithoutRendering");
    },

    changeDisplayOption: function(id) {
        $.ajaxSettings.traditional = true;
        var query = this;
        $.ajax({
            type: "GET",
            cache : false,
            url: "service/dataCart/changeDisplayOption",
            data: {
                displayOption: id
            },
            success: function(data, textStatus, jqXHR) {
                EventBus.trigger("DataTableView:removeQueryListener", this);
                query.set("tableResults", data);
                query.set("totalRecords", data.iTotalRecords);
                EventBus.trigger("renderResults", this);
                EventBus.trigger("DataTableView:addQueryListener", this);
            },
            error: function() {
                // TODO: fill in
            }
        });
    },

    removeVisualFilter: function(queryFilterModel) {
        var formUri = queryFilterModel.get("formUri");
        var groupUri = queryFilterModel.get("groupUri");
        var elementUri = queryFilterModel.get("elementUri");

        var filters = this.filters;
        for (var i = 0; i < filters.length; i++) {
            var filter = filters.at(i);
            var fUri = filter.get("formUri");
            var gUri = filter.get("groupUri");
            var eUri = filter.get("elementUri");
            if (formUri == fUri && groupUri == gUri && elementUri == eUri) {
                filters.remove(filter);
                break;
            }
        }
    },

    removeFilter: function(queryFilterModel) {
        this.removeVisualFilter(queryFilterModel);
        EventBus.trigger("applyFilters");
    },

    /**
     * Responds to clearing the data cart.  This empties the list.  Each view
     * handles removing itself but this takes care of the models.
     */
    onClearDataCart: function() {
        this.reset();
    },

    applySort: function() {
        //this removeQueryListener, prevents the entire table from redrawing we just want to redraw the results
        EventBus.trigger("DataTableView:removeQueryListener");
        this.performPaginateQuery();

    },
    paginate: function() {
        this.performPaginateQuery();
    },

    /**
     * performs a query, gets the result table
     * 
     * @param formUris array of form URIs
     */
    performPaginateQuery: function() {
        var formUris = this.get("selectedForms");
        if (formUris.length > 0) {
            $.ajaxSettings.traditional = true;
            var query = this;
            EventBus.trigger("open:processing", "performing query...");
            $.ajax({
                type: "GET",
                cache : false,
                url: "service/dataCart/dataWithPagination",
                data: {
                    formUris: formUris,
                    offset: this.get("offset"),
                    limit: this.get("limit"),
                    sortColName: this.get("sortColName"),
                    sortOrder: this.get("sortOrder")
                },
                success: function(data, textStatus, jqXHR) {
                    query.set("queryType", "data");
                    if (typeof data.fail !== "undefined") {
                        $.ibisMessaging("dialog", "warning", "An error occurred within pagination.");
                    } else {

                        query.paginateQueryComplete.call(query, data, true);
                    }
                    EventBus.trigger("close:processing");
                    EventBus.trigger("renderResults", this);
                    EventBus.trigger("DataTableView:addQueryListener", this);

                },
                error: function() {
                    // TODO: fill in
                }
            });
        } else {
            EventBus.trigger("close:processing");
        }
    },
    expandQuery: function(expandModel) {
        EventBus.trigger("DataTableView:removeQueryListener");
        $.ajaxSettings.traditional = true;
        var query = this;

        var filtersJson = JSON.stringify(this.filters.toJson());
        $.ajax({
            type: "GET",
            cache : false,
            url: "service/dataCart/expandRepeatableGroup",
            data: {
                rowUri: expandModel.rowUri,
                rgFormUri: expandModel.rgFormUri,
                rgName: expandModel.rgName
            },
            success: function(data, textStatus, jqXHR) {
                query.set("queryType", "data");
                query.expandQueryComplete.call(query, data, true);

                EventBus.trigger("renderResults", this);
                EventBus.trigger("DataTableView:addQueryListener", this);

            },
            error: function() {
                // TODO: fill in
            	if ( window.console && console.log ){
                console.log("error running expand query")
            	}
            }
        });
    },
    expandQueryComplete: function(data, updateSelectCriteria) {
        if (typeof data.error !== "undefined") {
            $.ibisMessaging("dialog", "error", "There was an error processing your expand query.");
        } else {
            this.set("tableResults", data);

            if (typeof updateSelectCriteria == "undefined") {
                updateSelectCriteria = true;
            }
            if (updateSelectCriteria) {
                this.updateSelectCriteria();
            }
        }
    },
    collapseQuery: function(expandModel) {
        EventBus.trigger("DataTableView:removeQueryListener");
        $.ajaxSettings.traditional = true;
        var query = this;

        $.ajax({
            type: "GET",
            cache : false,
            url: "service/dataCart/collapseRepeatableGroup",
            data: {
                rowUri: expandModel.rowUri,
                rgFormUri: expandModel.rgFormUri,
                rgName: expandModel.rgName
            },
            success: function(data, textStatus, jqXHR) {
                query.set("queryType", "data");
                query.expandQueryComplete.call(query, data, true);
                EventBus.trigger("renderResults", this);
                EventBus.trigger("DataTableView:addQueryListener", this);

            },
            error: function() {
                // TODO: fill in
            	if ( window.console && console.log ){
                console.log("error running expand query")
            	}
            }
        });
    },
    addBiosample: function(biosampleCellModel) {

        $.ajaxSettings.traditional = true;
        var query = this;

        $.ajax({
            type: "POST",
            cache: false,
            url: "service/biosample/addToQueue",
            dataType: "json",
            data: {
                rowUri: biosampleCellModel.biosampleRowUri,

                formName: biosampleCellModel.biosampleFormName,

                value: biosampleCellModel.biosampleValue,

                columnName: biosampleCellModel.get("column").get("name")
            },
            success: function(data, textStatus, jqXHR) {
                if (data.success == "duplicate") {
                    $.ibisMessaging("dialog", "success", "The item you tried to add is already in your queue.");
                } else if (data.success == "unique") {
                    var queueLink = "The item(s) has been added to your queue. Visit <a href=\"" +
                        System.urls.account + "/ordermanager/openQueue.action\"> your queue here. </a>"
                    $.ibisMessaging("dialog", "success", queueLink);
                }


            },
            error: function(jqXHR, textStatus, errorThrown) {
            	if ( window.console && console.log ){
                console.log(jqXHR);
                console.log(textStatus);
                console.log(errorThrown);
                // TODO: fill in
                console.log("error running add biosample query")
            	}
            }
        });

    },
    addBiosamples: function(biosampleColModel) {
        $.ajaxSettings.traditional = true;
        var query = this;
        var rowUris = biosampleColModel.get("biosampleArray").join();
        EventBus.trigger("open:processing", "Adding Biosamples...");
        $.ajax({
            type: "POST",
            cache: false,
            url: "service/biosample/validateAddManyToQueue",
            dataType: "json",
            data: {
                rowUris: rowUris
            },
            success: function(data, textStatus, jqXHR) {


                EventBus.trigger("close:processing");
                if (data.success == "duplicates") {

                    $("<div>The following biosample(s) you tried to add is already in your queue: " + data.rowUris.join() + ".  Continue? </div>").attr('id', 'confirmBioSampleDiv').appendTo('body');
                    $("#confirmBioSampleDiv").dialog({
                        dialogClass: "",
                        title: "Warning",
                        resizable: false,
                        height: "auto",
                        modal: true,
                        buttons: [{
                                text: "Continue",
                                click: function() {
                                    query.addManyBiosamples(rowUris);
                                    $(this).dialog("close");
                                    clearInterval(countdownIntervalId);
                                    $("#confirmBioSampleDiv").remove();
                                }
                            },
                            {
                                text: "Cancel",
                                click: function() {
                                    $(this).dialog("close");
                                    $("#confirmBioSampleDiv").remove();
                                }
                            }
                        ]
                    });
                } else {
                    query.addManyBiosamples(rowUris);
                }



            },
            error: function(jqXHR, textStatus, errorThrown) {
            	if ( window.console && console.log ){
                console.log(jqXHR);
                console.log(textStatus);
                console.log(errorThrown);
                // TODO: fill in
                console.log("error running add biosample query");
            	}
            }
        });

    },
    addManyBiosamples: function(rowUris) {
        EventBus.trigger("open:processing", "Adding Biosamples...");
        $.ajax({
            type: "POST",
            cache: false,
            url: "service/biosample/addManyToQueue",
            dataType: "json",
            data: {
                rowUris: rowUris
            },
            success: function(data, textStatus, jqXHR) {
                EventBus.trigger("close:processing");

                var queueLink = "The item(s) has been added to your queue. Visit <a href=\"" +
                    System.urls.account + "/ordermanager/openQueue.action\"> your queue here. </a>"
                $.ibisMessaging("dialog", "success", queueLink);
            },
            error: function(jqXHR, textStatus, errorThrown) {
            	if ( window.console && console.log ){
                console.log(jqXHR);
                console.log(textStatus);
                console.log(errorThrown);
                // TODO: fill in
                console.log("error running add biosample query");
            	}
            }
        });

    },
    hideAllBlankColumns: function() {
        EventBus.trigger("open:processing", "Hiding blank columns");
        this.set("hideShowColButtonText", "Show All Blank Columns");

        $.ajax({
            type: "GET",
            cache : false,
            url: "service/dataCart/columnsWithNoData",
            success: function(data, textStatus, jqXHR) {
                data.forEach(function(col) {

                    EventBus.trigger("hamburgerview:showHideCol", {
                        formUri: col.formUri,
                        rgName: col.rgName,
                        rgUri: col.rgUri,
                        deUri: col.deUri,
                        deName: col.deName,
                        // inverting this because the state has NOT been changed yet
                        // and visible denotes what it should be set TO
                        visible: false
                    });


                });

                EventBus.trigger("close:processing");
            },
            error: function() {
                // TODO: fill in
                EventBus.trigger("close:processing");
                if ( window.console && console.log ){
                console.log("error hiding all");
                }
            }
        });
    },

    showAllBlankColumns: function() {
        EventBus.trigger("open:processing", "Showing blank columns");
        this.set("hideShowColButtonText", "Hide All Blank Columns");

        $.ajax({
            type: "GET",
            cache : false,
            url: "service/dataCart/columnsWithNoData",
            success: function(data, textStatus, jqXHR) {
                data.forEach(function(col) {

                    EventBus.trigger("hamburgerview:showHideCol", {
                        formUri: col.formUri,
                        rgName: col.rgName,
                        rgUri: col.rgUri,
                        deUri: col.deUri,
                        deName: col.deName,
                        // inverting this because the state has NOT been changed yet
                        // and visible denotes what it should be set TO
                        visible: true
                    });


                });

                EventBus.trigger("close:processing");
            },
            error: function() {
                // TODO: fill in
                EventBus.trigger("close:processing");
                if ( window.console && console.log ){
                console.log("error showing all");
                }
            }
        });
    },

    reset: function() {
        this.filters.reset();
        this.set("formDetails", new QT.QueryForms());
        this.set("offset", 0);
        this.set("limit", 10);
        this.set("sortColName", "");
        this.set("sortOrder", "asc");
        this.set("selectedForms", []);
        this.set("totalRecords", 0);
    }
});