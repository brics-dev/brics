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
        currentlyJoined: false, // This will tell us if a joined query has been successfully processed.
        singleFormRequest: false, // This will tell us if a user is just asked for a single form to be queried
        applyFiltersEnabled : false, //this will tell us if there are filters that need to be ran.
        onReload: false, //this tells us if we are reloading objects
        hideShowColButtonText: "Hide All Blank Columns",
        hideShowColButtonClass: "hideBlankCol",
        queryType: "full", // || "data" for only-data queries.  Used for determining how the rest of the page should handle this
        doApplyFilter: false,
        outputSelectionOption : "Permissible Value",
        outputCodeSelection: "pv"
    },

    filters: null,

    initialize: function() {
        this.filters = new QT.QueryFilters();
        this.set("formDetails", new QT.QueryForms());
        EventBus.on("runQuery", this.performQuery, this);
        EventBus.on("runOnlyQuery", this.performQuery, this);
        EventBus.on("runJoinQuery", this.performQuery, this);
        EventBus.on("runExpandQuery", this.expandQuery, this);
        EventBus.on("runCollapseQuery", this.collapseQuery, this);
        EventBus.on("runSort", this.applySort, this);
        EventBus.on("runSelectForms", this.selectForms, this);
        EventBus.on("applyFiltersWithoutRendering", this.applyFiltersWithoutRendering, this);
        EventBus.on("query:dataAvailable", this.updateSelectCriteria, this);
        EventBus.on("remove:filter", this.removeFilter, this);
        EventBus.on("remove:visualFilter", this.removeVisualFilter, this);
        EventBus.on("addBiosample", this.addBiosample, this);
        EventBus.on("addBiosamples", this.addBiosamples, this);
        EventBus.on("clearDataCart", this.onClearDataCart, this);
        EventBus.on("query:reset", this.queryReset, this);
        EventBus.on("query:reRun", this.reRunQuery, this);
        EventBus.on("query:hideAllBlankColumns", this.hideAllBlankColumns, this);
        EventBus.on("query:showAllBlankColumns", this.showAllBlankColumns, this);
        EventBus.on("query:selectDe", this.selectDataElement, this);
        EventBus.on("query:deselectDe", this.deselectDataElement, this);
        EventBus.on("query:runRefreshRepeatbleGroup", this.refreshRG, this);
        EventBus.on("query:dragUpdateSelectCriteria",this.dragToUpdateSelectCriteria,this);
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
            	 if (typeof data.status !== "undefined") {
                  	if(data.status = "401") {
                  		//redirect
                  		window.location.href = "/query/logout";
                  		return;
                  	}
                  }
            },
            error: function() {
            	if ( window.console && console.log ){
                console.log("error applying filters");
            	}
            }
        });
    },
   
    filtersQueryComplete: function(data, updateSelectCriteria) {
    	 if (typeof data.status !== "undefined") {
         	if(data.status = "401") {
         		//redirect
         		window.location.href = "/query/logout";
         		return;
         	}
         }
        if (typeof data.error !== "undefined") {
            $.ibisMessaging("dialog", "error", "There was an error processing your query.");
        } else {
            if (this.get("tableResults") != undefined || this.get("tableResults").headers != undefined) {
            	
            	if(this.get("tableResults").data != undefined) {
             	   this.get("tableResults").data = data;
                }
               var newData = $.extend(this.get("tableResults"), data);
               
               //manually triggering here so we can use $.extend above to merge results into original
               //This is a bad hack, but the extend doesn't doesn't trigger the listenTo for changes in tableResults
               this.set("tableResults",{});
               this.set("tableResults", newData); 
            } else {
                this.set("tableResults", data);
            }
            
            if (typeof updateSelectCriteria == "undefined") {
                updateSelectCriteria = true;
            }
            if (updateSelectCriteria) {
                this.updateSelectCriteria(false);
            }
        }
        EventBus.trigger("close:processing");
    },

    queryComplete: function(data, updateSelectCriteria) {
        if (typeof data.status !== "undefined") {
        	if(data.status = "401") {
        		//redirect
        		window.location.href = "/query/logout";
        		return;
        	}
        }
        
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
            //For initial joins with filters, check if we need to run filters
            
            
            
            //This is used to aid in workflow as user goes from rendering join forms and single forms
            if(!this.get("onReload")) {
            	//Assuming this was a single form request
                this.set("singleFormRequest",true);
                var formUris = this.get("formUris");
	            if(formUris.length > 1) {
	            	updateSelectCriteria = false;
	            	this.set("currentlyJoined",true);
	            	this.set("singleFormRequest",false);
	            } else if (this.get("currentlyJoined")) {
	            	this.set("currentlyJoined",false);
	            }
            } 
            if (updateSelectCriteria) {
                this.updateSelectCriteria(true);
            } else {
	            //render table
				EventBus.trigger("renderTable");
            }
        }
    },
    paginateQueryComplete: function(data, updateSelectCriteria) {
    	 if (typeof data.status !== "undefined") {
         	if(data.status = "401") {
         		//redirect
         		window.location.href = "/query/logout";
         		return;
         	}
         }
        if (typeof data.error !== "undefined") {
            $.ibisMessaging("dialog", "error", "There was an error processing your query during pagination.");
        } else {
            if (typeof updateSelectCriteria == "undefined") {
                updateSelectCriteria = true;
            }
            this.set("tableResults", data);
            this.set("totalRecords", data.iTotalRecords);
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
            this.set("limit", 100);
            this.set("sortColName", "");
            this.set("sortOrder", "asc");
            var processMessage = "Performing query...";
            if(formUris.length > 3) {
            	processMessage = "Performing query...  Returned query results will vary depending on the size of the dataset and/or form structures you are joining.";
            }
            
            EventBus.trigger("open:processing", processMessage);
            var filtersJson = replaceAllOccurrencesInString(JSON.stringify(query.filters.toJsonForQuery()), "\\n", ";");
            
            if(filtersJson.length > 0) {
				this.set("doApplyFilter",true);
            } else {
            	this.set("doApplyFilter",false);
            }
            
            $.ajax({
                type: "POST",
                cache : false,
                url: "service/dataCart/runQuery",
                data: {
                    formUris: formUris,
                    offset: this.get("offset"),
                    limit: this.get("limit"),
                    sortColName: this.get("sortColName"),
                    sortOrder: this.get("sortOrder"),
                    filters: filtersJson,
        			filterExpression: query.filters.getExpression(),
        			doApplyFilter: this.get("doApplyFilter")
                },
                success: function(data, textStatus, jqXHR) {
                	query.set("doApplyFilter",false);
                    query.set("queryType", "full");

                    var showData = true;
                    
                    if (typeof data.formInfo !== "undefined") {
                    	var missingGuidDialog = "";
                    	for(var i = 0; i < data.formInfo.length; i++) {
                    		var currentForm = data.formInfo[i];
                    		if(currentForm.hasGuidColumn !== "undefined" && currentForm.hasGuidColumn === false) {
                    			missingGuidDialog = missingGuidDialog.concat("The JOIN consists of ").concat(currentForm.name).concat(" which does not has GUIDS.  Please remove this Form Structure and Redo your Join. You should be able to see the data for this FS by loading it individually.&nbsp;");
                    			//dont show data if one of the forms don't have a GUID
                    			showData = false;
                    		}
                    		
                    		if(!currentForm.hasGuidData !== "undefined" && currentForm.hasGuidData === false) {
                    			missingGuidDialog = missingGuidDialog.concat("At least one record from ").concat(currentForm.name).concat(" is missing data for GUID.  Records that do not have a GUID are not included in the join result.&nbsp;");
        					}
                    	}
        				
        				if(missingGuidDialog != "") {
        					$.ibisMessaging("dialog", "warning", missingGuidDialog);
        				}
                    }
        				
                    EventBus.trigger("close:processing");
                    
                    if(showData === true) {
                    	query.queryComplete.call(query, data, true);
                    }
                },
                error: function(err,x,a) {
                    // TODO: fill in
                	$.ibisMessaging("dialog", "error", "There is an error processing your request.");
                	EventBus.trigger("close:processing");
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

    updateSelectCriteria: function(renderTable) {
        var query = this;
        $.ajaxSettings.traditional = true;
        $.ajax({
            type: "GET",
            cache : false,
            url: "service/dataCart/selectedFormDetails",
            success: function(data, textStatus, jqXHR) {
            	 if (typeof data.status !== "undefined") {
                  	if(data.status = "401") {
                  		//redirect
                  		window.location.href = "/query/logout";
                  		return;
                  	}
                  }
                query.set("formDetails", data);
                $(".containerFontIcon").hide();
                //TODO: Do we want to change this for joins?
                EventBus.trigger("query:formDetailsAvailable");
                if(renderTable) {
                	EventBus.trigger("renderTable");
                }
            },
            error: function() {
                // TODO: fill in
            	console.log('error updating select criteria');
            }
           
        });
    },
    dragToUpdateSelectCriteria: function() {
        var query = this;
        query.set("queryType", "full");
        this.updateSelectCriteria(false);
        
    },

    getDeFilterDetails: function(formUri, rgUri, deUri, rgName, deName, extraData) {
        if (typeof extraData == "undefined") {
            extraData = {};
        }
        if (System.environment === "pdbp") {
        	deName = deName == "highlight_diagnosis" ? "GUID" : deName;
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
            	if (typeof data.status !== "undefined") {
                 	if(data.status = "401") {
                 		//redirect
                 		window.location.href = "/query/logout";
                 		return;
                 	}
                 }
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
                deName: colModel.deName
            },
            success: function(data, textStatus, jqXHR) {
            	if (typeof data.status !== "undefined") {
                 	if(data.status = "401") {
                 		//redirect
                 		window.location.href = "/query/logout";
                 		return;
                 	}
                 }
            	EventBus.trigger("processed:columnCheckBox");
                // TODO: do we need to handle this?  Probably update local data somehow
            },
            error: function() {
                // TODO: fill in
            	EventBus.trigger("processed:columnCheckBox");
            }
        });
    },

    selectDataElement: function(colModel) {
    	if(colModel.formUri !== "" && colModel.rgName !== "" && colModel.deName !== "") {
    		
	        $.ajaxSettings.traditional = true;
	        $.ajax({
	            type: "POST",
	            cache: false,
	            url: "service/dataCart/element/select",
	            data: {
	                formUri: colModel.formUri,
	                rgName: colModel.rgName,
	                deName: colModel.deName
	            },
	            success: function(data, textStatus, jqXHR) {
	            	if (typeof data.status !== "undefined") {
	                 	if(data.status = "401") {
	                 		//redirect
	                 		window.location.href = "/query/logout";
	                 		return;
	                 	}
	                 }
	            	EventBus.trigger("processed:columnCheckBox");
	                // TODO: do we need to handle this?  Probably update local data somehow
	            },
	            error: function() {
	                // TODO: fill in
	            	EventBus.trigger("processed:columnCheckBox");
	            }
	        });
    	}
    },
    
	removeAllFilters : function(conf) {
		EventBus.trigger("removeAll:filters");
		if((conf != undefined && conf.applyFilters) || conf == undefined) {
			_.defer(function() {
				EventBus.trigger("applyFilters");
				EventBus.trigger("populateQueryBox");
			});
		}
	},
    
    changeDisplayOptionVirtTable: function(displayOptionObj) {
    	 var query = this;
   	  return new Promise(function (resolve, reject) {
          
   		$.ajaxSettings.traditional = true;
	        $.ajax({
	            type: "GET",
	            cache : false,
	            url: "service/dataCart/changeDisplayOption",
	            data: {
	                displayOption: displayOptionObj.displayOption
	            },
	            success: function(data, textStatus, jqXHR) {
	            	if (typeof data.status !== "undefined") {
	                 	if(data.status = "401") {
	                 		//redirect
	                 		window.location.href = "/query/logout";
	                 		return;
	                 	}
	                 }
	                query.set("tableResults", data);
	                query.set("totalRecords", data.iTotalRecords);
	            	resolve(data);
	            },
	            error: function() {
	                // TODO: there should be an error message
	            	reject();
	            }
	        });
       });
        
    },
    resetChangeDisplayOption: function(displayOption) {
   	 var query = this;
  	  $.ajaxSettings.traditional = true;
	        $.ajax({
	            type: "GET",
	            cache : false,
	            url: "service/dataCart/changeDisplayOption",
	            data: {
	                displayOption: displayOption
	            },
	            success: function(data, textStatus, jqXHR) {
	                query.set("tableResults", data);
	                query.set("totalRecords", data.iTotalRecords);
	            	
	            },
	            error: function() {
	                // TODO: there should be an error message
	            	
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
        return this.scrollVirtTable(this.get("offset"),this.get("limit"));

    },
    paginate: function() {
        this.performPaginateQuery();
    },

    /**
     * TODO: i think we can delete this
     * performs a query, gets the result table
     * 
     * @param formUris array of form URIs
     */
    performPaginateQuery: function() {
        var formUris = this.get("selectedForms");
        if (formUris.length > 0) {
            $.ajaxSettings.traditional = true;
            var query = this;
            EventBus.trigger("open:processing", "Performing query...");
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

                },
                error: function() {
                    // TODO: fill in
                	EventBus.trigger("close:processing");
                }
            });
        } else {
            EventBus.trigger("close:processing");
        }
    },
    
 // request data for the virtual table when scrolling.
	updateVirtTable : function(offset, limit, type, valueObj) {
		switch(type){
			case 1: //scroll table
				return this.scrollVirtTable(offset,limit);
			break;
			case 2: //expand repeatable group
				return this.expandRgVirtTable(valueObj);
			break;
			case 3: //collapse repeatable group
				return this.collapseRgVirtTable(valueObj);
			break;
			case 4: //sort
				return this.applySort();
			break;
			case 5: //filters
				return this.applyFiltersVirtTable();
			break;
			case 6: //Display Options
				return this.changeDisplayOptionVirtTable(valueObj);
			break;
			default: 
				return this.scrollVirtTable(offset,limit);
		}
     
	},

    // request data for the virtual table when scrolling.
	scrollVirtTable : function(offset, limit) {
      var query = this;
	  return new Promise(function (resolve, reject) {
        var formUris = query.get("selectedForms");
        if (formUris.length > 0) {
             
          //re-use pagination query for scrolling the virtual table.
          $.ajaxSettings.traditional = true;
          $.ajax({
            type: "GET",
            cache : false,
            url: "service/dataCart/dataWithPagination",
            data: {
                formUris: formUris,
                offset: offset,
                limit: limit,
                sortColName: query.get("sortColName"),
                sortOrder: query.get("sortOrder")
            },
            success: function(data, textStatus, jqXHR) {
                query.set("queryType", "data");
                if (typeof data.fail !== "undefined") {
                    $.ibisMessaging("dialog", "warning", "An error occurred within scrollVirtTable.");
                    reject();
                } else {
                	// success, return the data
                	query.paginateQueryComplete.call(query, data, true);
                	resolve(data);
                }
            },
            error: function() {
            	EventBus.trigger("close:processing");
                // TODO: can we get an error message?
            	reject();
            }
          });
        } else {
          resolve();
        }
      });
	},
	expandRgVirtTable: function(expandModel) {
	        var query = this;
	  	  return new Promise(function (resolve, reject) {
		        $.ajaxSettings.traditional = true;
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
		            	if (typeof data.status !== "undefined") {
		                 	if(data.status = "401") {
		                 		//redirect
		                 		window.location.href = "/query/logout";
		                 		return;
		                 	}
		                 }
		                query.set("queryType", "data");
		                query.expandQueryComplete.call(query, data, true);
		                resolve(data);
	
		            },
		            error: function() {
		                // TODO: fill in
		            	if ( window.console && console.log ){
		                console.log("error running expand query")
		            	}
		            	 $.ibisMessaging("dialog", "error", "There was an error processing your repeatable group.");
		            	reject();
		            }
		        });
	  	  });
	    },
	    collapseRgVirtTable: function(expandModel) {
	        var query = this;
	        return new Promise(function(resolve, reject) {
	        	$.ajaxSettings.traditional = true;
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
		            	if (typeof data.status !== "undefined") {
		                 	if(data.status = "401") {
		                 		//redirect
		                 		window.location.href = "/query/logout";
		                 		return;
		                 	}
		                 }
		                query.set("queryType", "data");
		                query.expandQueryComplete.call(query, data, true);
		                resolve(data);
	
		            },
		            error: function() {
		                // TODO: fill in
		            	if ( window.console && console.log ){
		                console.log("error running expand query")
		            	}
		            	 $.ibisMessaging("dialog", "error", "There was an error processing your repeatable group.");
			            	reject();
		            }
		        });
	        });
	    },
	    applyFiltersVirtTable: function() {
	    	var query = this;
	        EventBus.trigger("open:processing", "Applying Filters");
	        return new Promise(function(resolve, reject) {
		        $.ajaxSettings.traditional = true;
		        
		        var filtersJson = replaceAllOccurrencesInString(JSON.stringify(query.filters.toJsonForQuery()), "\\n", ";");
		        
		        $.ajax({
		            type: "POST",
		            cache: false,
		            url: "service/dataCart/applyFilters",
		            data: {
		                filters: filtersJson,
		                offset: query.get("offset"),
		                limit: query.get("limit"),
		                sortColName: query.get("sortColName"),
		                sortOrder: query.get("sortOrder"),
		                filterExpression: query.filters.getExpression()
		            },
		            success: function(data, textStatus, jqXHR) {
		                query.set("queryType", "data");
		                query.filtersQueryComplete.call(query, data, true);
		                resolve(data);
		            },
		            error: function() {
		                // TODO: fill in
		                EventBus.trigger("close:processing");
		                $.ibisMessaging("dialog", "error", "There was an error processing your filter.");
		            	reject();
		                if ( window.console && console.log ){
		                console.log("error applying filters");
		                }
		            }
		        });
		       
	        	
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
                this.updateSelectCriteria(false);
            }
        }
    },
    refreshRG: function(cellModel) {

	    $.ajaxSettings.traditional = true;
	    var query = this;

	    $.ajax({
	        type: "GET",
	        cache : false,
	        url: "service/dataCart/refreshRepeatableGroup",
	        data: {
	            rowUri: cellModel.rowUri,
	            rgFormUri: cellModel.rgFormUri,
	            rgName: cellModel.rgName
	        },
	        success: function(data, textStatus, jqXHR) {
	        	if (typeof data.status !== "undefined") {
	             	if(data.status = "401") {
	             		//redirect
	             		window.location.href = "/query/logout";
	             		return;
	             	}
	             }
	            if (typeof data.error !== "undefined") {
	                $.ibisMessaging("dialog", "error", "There was an error processing your refresh repeatable group.");
	            } else {
	            	query.set("tableResults", data);
	            }

	        },
	        error: function() {
	        	if ( window.console && console.log ){
	        		console.log("error running refresh repeatable group")
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

                columnName: biosampleCellModel.columnName
            },
            success: function(data, textStatus, jqXHR) {
            	if (typeof data.status !== "undefined") {
                 	if(data.status = "401") {
                 		//redirect
                 		window.location.href = "/query/logout";
                 		return;
                 	}
                 }
                if (data.success == "duplicate") {
                    $.ibisMessaging("dialog", "success", "The item you tried to add is already in your queue.");
                } else if (data.success == "unique") {
                    var queueLink = "The item(s) has been added to your queue. Visit <a href=\"" +
                        System.urls.account + "/ordermanager/openQueue.action\"> your queue here. </a>"
                    $.ibisMessaging("dialog", "success", queueLink);
                }


            },
            error: function(jqXHR, textStatus, errorThrown) {
            	$.ibisMessaging("dialog", "error", "There was an error adding to your queue.");
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
        var selectAllBiosamples = biosampleColModel.get('selectAllChecked');
        var bioFormName = biosampleColModel.get('bioFormName');
        var rowUris = biosampleColModel.get("biosampleArray").join();
        var removedRowUris = biosampleColModel.get("unselectedBioSampleArray").join();
        EventBus.trigger("open:processing", "Adding Biosamples...");
        $.ajax({
            type: "POST",
            cache: false,
            url: "service/biosample/validateAddManyToQueue",
            dataType: "json",
            data: {
                rowUris: rowUris,
                selectAll: selectAllBiosamples,
                bioFormName: bioFormName,
                unselectedRowUris: removedRowUris,
                
            },
            success: function(data, textStatus, jqXHR) {
            	if (typeof data.status !== "undefined") {
                 	if(data.status = "401") {
                 		//redirect
                 		window.location.href = "/query/logout";
                 		return;
                 	}
                 }


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
                                    query.addManyBiosamples(rowUris,bioFormName,removedRowUris,selectAllBiosamples);
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
                    query.addManyBiosamples(rowUris,bioFormName,removedRowUris,selectAllBiosamples);
                }



            },
            error: function(jqXHR, textStatus, errorThrown) {
            	
            	 EventBus.trigger("close:processing");
            	 $.ibisMessaging("dialog", "error", "There as an error processing your addition of biosamples");
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
    addManyBiosamples: function(rowUris,bioFormName,removedRowUris,selectAll) {
        EventBus.trigger("open:processing", "Adding Biosamples...");
        $.ajax({
            type: "POST",
            cache: false,
            url: "service/biosample/addManyToQueue",
            dataType: "json",
            data: {
                rowUris: rowUris,
                selectAll: selectAll,
                unselectedRowUris: removedRowUris,
                bioFormName: bioFormName
            },
            success: function(data, textStatus, jqXHR) {
            	if (typeof data.status !== "undefined") {
                 	if(data.status = "401") {
                 		//redirect
                 		window.location.href = "/query/logout";
                 		return;
                 	}
                 }
                EventBus.trigger("close:processing");

                var queueLink = "The item(s) has been added to your queue. Visit <a href=\"" +
                    System.urls.account + "/ordermanager/openQueue.action\"> your queue here. </a>"
                $.ibisMessaging("dialog", "success", queueLink);
            },
            error: function(jqXHR, textStatus, errorThrown) {
            	 EventBus.trigger("close:processing");
            	 $.ibisMessaging("dialog", "error", "There as an error processing your addition of biosamples");
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
            	if (typeof data.status !== "undefined") {
                 	if(data.status = "401") {
                 		//redirect
                 		window.location.href = "/query/logout";
                 		return;
                 	}
                 }
                data.forEach(function(col) {

                    EventBus.trigger("hamburgerview:showHideCol", {
                    	formName: col.formName,
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
            	if (typeof data.status !== "undefined") {
                 	if(data.status = "401") {
                 		//redirect
                 		window.location.href = "/query/logout";
                 		return;
                 	}
                 }
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
    
    /**
     * sets selected form objects in java, this is only used when a user drags forms
     * into join fields
     * 
     * @param formUris array of form URIs
     */
    selectForms: function(obj) {
        var formUris = obj.formUris;

      
            this.set("formUris", formUris);
            $.ajaxSettings.traditional = true;
            var query = this;
            var processMessage = "Selecting forms...";
           // EventBus.trigger("open:processing", processMessage);
            $.ajax({
                type: "GET",
                cache : false,
                url: "service/dataCart/setSelectedForms",
                data: {
                    formUris: formUris
                },
                success: function(data, textStatus, jqXHR) {
                	
                	//if join is available and we have turn on run Query button
                	if(query.get("selectedForms").length > 1) {
                		$("#filterLogicRunQuery").removeClass("disabled");
                	}
                	
                	if (typeof data.status !== "undefined") {
                     	if(data.status = "401") {
                     		//redirect
                     		window.location.href = "/query/logout";
                     		return;
                     	}
                     }
                    query.set("queryType", "full");
                   if (typeof data.error !== "undefined") {
                        $.ibisMessaging("dialog", "warning", "There is an error selecting your form.");
                    } else {
                    	if(obj.updateSelectCriteria == undefined) {
                    		//After the forms have been selected and placed into the join form fields, update the select criteria fields
                    		EventBus.trigger("query:dragUpdateSelectCriteria");	
                    	}
                    	
                    	if(obj.loadsq !== undefined) {
                    		EventBus.trigger("load:sqFilters");
                    	}
                    }
                   
                },
                error: function() {
                    // TODO: fill in
                	$.ibisMessaging("dialog", "error", "There is an error processing your request.");
                	//EventBus.trigger("close:processing");
                }
            });
       
    },
    
    queryReset: function(conf) {
    	this.removeAllFilters(conf);
    },
    reset: function() {
        this.filters.reset();
        this.set("formDetails", new QT.QueryForms());
        this.set("formUris",[]);
        this.set("offset", 0);
        this.set("limit", 100);
        this.set("sortColName", "");
        this.set("sortOrder", "asc");
        this.set("selectedForms", []);
        this.set("totalRecords", 0);
    }
});


replaceAllOccurrencesInString = function(str, replaceWhat, replaceWith){
    return str.split(replaceWhat).join(replaceWith);

}