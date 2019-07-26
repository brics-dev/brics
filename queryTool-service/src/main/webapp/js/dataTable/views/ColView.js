/**
 * 
 */
QTDT.ColView = BaseView.extend({
    events: {
        "click .lock": "freeze",
        "click .unlock": "unfreeze",
        "click .sorting": "sortAsc",
        "click .sort_asc": "sortDesc",
        "click .sort_desc": "noSort",
        "click #sampleCheckAll": "checkAllBioSamples"

    },
    frozen: false,
    hbview: null,


    initialize: function() {

        this.$el = $('<th>', {
            id: this.model.cid,
            "class": 'cell',
            view: this.cid
        });
        
        var thisView = this;
        
        if (this.model.get("headerType") == "dataElement") {
        	
            this.$el.resizable({
                ghost: true,
                handles: 'e',
                resizeHeight: false,
                maxHeight: 28,
                minHeight: 28,
                start: function(event,ui) {
                	
                },
                resize: function(event,ui) {
                	
                },
                stop: function(event, ui) {
                    thisView.adjustColWidth(ui.size.width);
                    thisView.$el.css("left", "inherit");
                    thisView.$el.css("height", "inherit");
                    $(".ui-resizable-helper").remove();
                }
            });
        }






        //if this is a frozen column add a specific remove event
        if (this.model.get("frozen")) {
            EventBus.on("frozencolview:removeall", this.destroy, this);
            this.listenTo(this.model, "change:frozenWidth", this.adjustFrozenElWidth);
            this.listenTo(this.model, "change:frozenColspanAmt", this.updateFrozenColspan);
        } else {
            EventBus.on("colview:removeall", this.destroy, this);
            this.listenTo(this.model, "change:width", this.adjustElWidth);
            this.listenTo(this.model, "change:colspanAmt", this.updateColspan);
        }

        //we only need this trigger for one speficic column
        if (this.model.get("name") == "OrderableBiosampleID") {
            EventBus.on("col:addSelectedSamples", this.addSelectedSamples, this);
            EventBus.on("col:deselectSelectAll", this.deselectSelectAll, this)
            EventBus.on("col:checkSelectedBiosamples", this.checkSelectedBiosamples, this)

        }




        this.listenTo(this.model, "change:visible", this.toggleVisibility);
        EventBus.on("column:addVisibleListener", this.addVisibleListener, this);
        EventBus.on("column:removeVisibleListener", this.removeVisibleListener, this);
        EventBus.on("column:showColumn", this.showColumn, this);
    },


    render: function() {

        switch (this.model.get("headerType")) {
            case "dataElement":
                var colspanAmt = 1;

                break;
            case "repeatableGroup":
                var colspanAmt = this.model.get("children").where({ visible: true }).length;
                break;
            case "form":
                var colspanAmt = 0;
                var formRgs = this.model.get("children").where({ visible: true });

                for (var i in formRgs) {
                    rgModel = formRgs[i];
                    colspanAmt += rgModel.get("children").where({ visible: true }).length;
                }
                break;

        }
        this.model.set("colspanAmt", -1); //I need this to force a change
        this.model.set("colspanAmt", colspanAmt);

        if (this.model.get("frozen")) {


            switch (this.model.get("headerType")) {
                case "dataElement":
                    var colspanAmt = 1;

                    break;
                case "repeatableGroup":
                    var colspanAmt = this.model.get("children").where({ visible: true, frozen: true }).length;
                    break;
                case "form":
                    var colspanAmt = 0;
                    var formRgs = this.model.get("children").where({ visible: true, frozen: true });

                    for (var i in formRgs) {
                        rgModel = formRgs[i];
                        colspanAmt += rgModel.get("children").where({ visible: true, frozen: true }).length;
                    }
                    break;

            }
            this.model.set("frozenColspanAmt", -1); //I need this to force a change
            this.model.set("frozenColspanAmt", colspanAmt);
        }



        var hType = this.model.get("headerType");
        var h5Class = (hType == "dataElement") ? "colhdr hdrcolor" : "colhdr ";
        var modelName = this.model.get("name");
        var h5Html = (hType == "dataElement") ? "<div class='colValue'>" + modelName + "</div>" : modelName; //;
        h5Html += (modelName == "OrderableBiosampleID") ? '<div class="colValue"><input id="sampleCheckAll" type="checkbox" value="0"/>&nbsp;Select All</div>' : "";
        HBHtml = "";
        (hType == "repeatableGroup" && (this.model.get("name") != "Repeatable Groups:" && this.model.get("name") != "")) ? HBHtml = '<span class="rghbmenu hbblack"></span>': (hType == "dataElement" && !(this.model.get("name") == "GUID" && this.model.get("index") == 0)) ? (hType == "dataElement" && this.model.get("name") != "Study ID" && this.model.get("name") != "Dataset") ? HBHtml = '<span class="hbmenu"></span>' : "" : "";
        //this is for the repeatable group label column, such that when you freeze the columns the table isn't misaligned
        HBHtml += (hType == "repeatableGroup" && this.model.get("name") == "Repeatable Groups:") ? '<span style="height: 12px;display: inline-block;"></span>' : "";


        h5Html = HBHtml + h5Html;
        var frozenClass = (this.model.get("frozen")) ? "fa fa-lock unlock" : "fa fa-unlock lock";
        h5Html += (hType == "dataElement") ? ' <a href="javascript:void(0);" class="icon ' + frozenClass + '">&nbsp</a>' : "";
        
        var cellObj = {
				colId : this.model.get("col") || 0
	            };
		var cellObjJson = JSON.stringify(cellObj);
		
		var sOrder = this.model.get("sortDir");
		var sorting = "sorting";
		
		if(sOrder == "asc") {
			sorting = "sort_asc";	
		} else if(sOrder == "desc") {
			sorting = "sort_desc";
		}
		
        h5Html += (hType == "dataElement" && !this.model.get("doesRepeat")) ? '<div class="'+sorting+'" data-model=\''+cellObjJson+'\'></div>' : "";
        this.$el.append($('<h5>', { "class": h5Class, id: (modelName == "OrderableBiosampleID") ? "OrderableBiosampleID" : "" }).html(h5Html));

        //if we have joined forms the first column for GUID does not need a hamburger menu
        //if((hType == "dataElement" && !(this.model.get("name") == "GUID" && this.model.get("index") == 0)) || hType == "repeatableGroup") {
        this.createHM();
        //	}
        this.$el.children('h5').append('<div class="space-line"></div>');

        //if the adjusted width was previously set use set the width of the column to that. 
        var adjustedWidth = this.model.get("adjustedWidth");
        if (adjustedWidth != -1) {
            this.$el.width(adjustedWidth);
            this.$el.children('h5').width(adjustedWidth);
            this.model.set("width", adjustedWidth);
        }
        return this;


    },
    /**
     * Creates the hamburger menu and its options
     */
    createHM: function() {
        HamburgerMenu = this;
        this.hbview = new QTDT.HamburgerView({ model: this.model });
        var hType = this.model.get("headerType");
        if (hType == "dataElement") {
            this.$el.children('h5').children('.hbmenu').append(this.hbview.render().$el.prop('outerHTML'));
        } else if (hType == "repeatableGroup") {
            this.$el.children('h5').children('.rghbmenu').append(this.hbview.render().$el.prop('outerHTML'));
        }
        
    },
    freeze: function() {
        this.model.set("frozen", true);
        //call dataTableView to re-render table
        EventBus.trigger("column:lock", this);
    },
    unfreeze: function() {
        this.model.set("frozen", false);
        this.frozen = false;
        //call dataTableView to re-render table
        EventBus.trigger("column:unlock", this);
    },
    adjustElWidth: function(e) {

        var newWidth = this.model.get("width");

        this.$el.width(newWidth);

        var h5Container = this.$el.find("h5");
        h5Container.width(newWidth);

        if (newWidth == 0) {
            this.$el.hide();
            this.$el.css("display", "none");
        } else {
            this.$el.show();
        }
    },
    adjustFrozenElWidth: function(e) {
        var newWidth = this.model.get("frozenWidth");
        this.$el.width(newWidth);

        var h5Container = this.$el.find("h5");
        h5Container.width(newWidth);

        if (newWidth == 0) {
            this.$el.hide();
            this.$el.css("display", "none");
        } else {
            this.$el.show();
        }

    },
    adjustColWidth: function(newWidth, ellipse) {
    	ellipse = (typeof ellipse === "undefined") ? false : ellipse;
    	var cells = this.model.get("cells");
    	if(ellipse) {
    		//check if newWidth is less than other column widths
    		var largestWidth = newWidth;
    		cells.each(function(cell) {
                if(largestWidth < cell.get("width")) {
                	largestWidth = cell.get("width");
                };
            });
    		newWidth = largestWidth;
    	}
        this.model.set("adjustedWidth", newWidth);
        var h5Container = this.$el.find("h5");
        h5Container.width(0);
        this.model.set("width", newWidth);
        h5Container.width(newWidth);

        if (this.model.get("frozen")) {
            this.model.set("frozenWidth", 0);
            this.model.set("frozenWidth", newWidth);

            var frozenH5Container = this.model.get("frozenView").$el.find("h5");
            frozenH5Container.width(newWidth);
        }
        if (this.model.get("headerType") == "dataElement") {
           
            var colWidth = this.model.get("width"); //(this.model.get("visible")) ? this.model.get("width") : 0; //if the column is not visible then the width is 0
            var maxWidth = 0;
            if (colWidth > 200) {
                maxWidth = colWidth; //TODO: Change this to a value is we want to max out the width of the columns
            } else {
                maxWidth = colWidth;
            }
            $model = this.model;

            cells.each(function(cell) {
            	if(!ellipse) {
	                cell.set("width", -1);
	                cell.set("width", maxWidth);
            	}
            });


            this.model.set("width", maxWidth);
            this.$el.width(maxWidth);
            if (maxWidth == 0) {
                this.$el.hide();
                this.$el.css("display", "none");
            }

            this.adjustParentWidthForResize();


        }
    },
    toggleVisibility: function() {
        if (this.model.get("visible")) {
            this.$el.show("fast");
        } else {
            this.$el.css("display", "none");
        }
        if (this.model.get("parent") != null && this.model.get("parent").get("parent") != null) {
            this.adjustParentWidthForHiding();
        }

    },

    adjustParentWidthForHiding: function() {

        var parent = this.model.get("parent");
        var grandParent = this.model.get("parent").get("parent");


        var kids = parent.get("children");
        var width = 0;
        var frozenWidth = 0;
        var widthDelta = 0;
        var tempColspan = 0;
        kids.each(function(kid) {
                width += (kid.get("visible")) ? kid.get("width") : 0;
                frozenWidth += (kid.get("visible") && kid.get("frozen")) ? kid.get("width") : 0;
                tempColspan += (kid.get("visible")) ? 1 : 0;
            })
            //set parent to invisible if all the kids are not visible
        if (width == 0) {
            parent.set("visible", false);
        } else {
            parent.set("visible", true);
        }
        parent.set("width", -1);
        parent.set("width", width);

        parent.set("colspanAmt", tempColspan);
        if (!this.model.get("visible")) {
            parent.set("frozenWidth", -1);
            parent.set("frozenWidth", frozenWidth);
        }




        var kids = grandParent.get("children");
        var width = 0;
        var frozenWidth = 0;
        var tempColspan = 0;
        kids.each(function(kid) {
            grandKids = kid.get("children");
            grandKids.each(function(grandKid) {

                tempColspan += (grandKid.get("visible")) ? 1 : 0;
                width += (grandKid.get("visible")) ? grandKid.get("width") : 0;
                widthDelta += (!grandKid.get("visible")) ? grandKid.get("width") : 0;
                frozenWidth += (grandKid.get("visible") && grandKid.get("frozen")) ? grandKid.get("width") : 0;
            })

        })

        //set parent to invisible if all the kids are not visible
        if (width == 0) {
            grandParent.set("visible", false);
        } else {
            grandParent.set("visible", true);
        }

        grandParent.set("width", -1);
        grandParent.set("width", width);
        grandParent.set("colspanAmt", -1);
        grandParent.set("colspanAmt", tempColspan);
        if (!this.model.get("visible")) {
            grandParent.set("frozenWidth", -1);
            grandParent.set("frozenWidth", frozenWidth);
        }

        tableWidthDeltaObj = { visible: false, widthDelta: widthDelta };
        EventBus.trigger("DataTableView:changeTableWidth", tableWidthDeltaObj);
        EventBus.trigger("DataTableView:adjustFrozenColumns", this);

    },
    adjustParentWidthForResize: function() {

        var parent = this.model.get("parent");
        var grandParent = this.model.get("parent").get("parent");


        var kids = parent.get("children");
        var width = 0;
        var frozenWidth = 0;
        var widthDelta = 0;
        var tempColspan = 0;
        kids.each(function(kid) {
            width += (kid.get("visible")) ? kid.get("width") : 0;
            frozenWidth += (kid.get("visible") && kid.get("frozen")) ? kid.get("width") : 0;
            tempColspan += (kid.get("visible")) ? 1 : 0;
        })
        parent.set("width", -1);
        parent.set("width", width);
        parent.get("originalView").$el.find("h5").width(width);
        parent.set("colspanAmt", tempColspan);
        if (this.model.get("frozen")) {
            parent.set("frozenWidth", -1);
            parent.set("frozenWidth", frozenWidth);
            var frozenH5Container = parent.get("frozenView").$el.find("h5");
            frozenH5Container.width(frozenWidth);
        }


        var kids = grandParent.get("children");
        var width = 0;
        var frozenWidth = 0;
        var tempColspan = 0;
        kids.each(function(kid) {
            grandKids = kid.get("children");
            grandKids.each(function(grandKid) {

                tempColspan += (grandKid.get("visible")) ? 1 : 0;
                width += (grandKid.get("visible")) ? grandKid.get("width") : 0;
                widthDelta += (!grandKid.get("visible")) ? grandKid.get("width") : 0;
                frozenWidth += (grandKid.get("visible") && grandKid.get("frozen")) ? grandKid.get("width") : 0;
            })

        })

        grandParent.set("width", -1);
        grandParent.set("width", width);
        grandParent.get("originalView").$el.find("h5").width(width);
        grandParent.set("colspanAmt", tempColspan);
        if (this.model.get("frozen")) {
            grandParent.set("frozenWidth", -1);
            grandParent.set("frozenWidth", frozenWidth);
            var frozenH5Container = grandParent.get("frozenView").$el.find("h5");
            frozenH5Container.width(frozenWidth);
        }

        tableWidthDeltaObj = { visible: true, widthDelta: width };
        EventBus.trigger("DataTableView:changeTableWidth", tableWidthDeltaObj);


    },
    sortAsc: function() {
        $(".sort_asc").each(function(s) {
            $(this).switchClass("sort_asc", "sorting", 1000, "easeInOutQuad");
        });

        $(".sort_desc").each(function(s) {
            $(this).switchClass("sort_desc", "sorting", 1000, "easeInOutQuad");
        });

        this.$el.find(".sorting").switchClass("sorting", "sort_asc", 1000, "easeInOutQuad");

        QueryTool.query.set("sortColName", this.model.getFullName());
        QueryTool.query.set("sortOrder", "asc");

        EventBus.trigger("runSort", this);

    },
    sortDesc: function() {
        $(".sort_asc").each(function(s) {
            $(this).switchClass("sort_asc", "sorting", 1000, "easeInOutQuad");
        });

        $(".sort_desc").each(function(s) {
            $(this).switchClass("sort_desc", "sorting", 1000, "easeInOutQuad");
        });

        this.$el.find(".sorting").switchClass("sorting", "sort_desc", 1000, "easeInOutQuad");
        // call the event to query
        QueryTool.query.set("sortColName", this.model.getFullName());
        QueryTool.query.set("sortOrder", "desc");
        EventBus.trigger("runSort", this);

    },
    noSort: function() {
        this.$el.find(".sort_desc").switchClass("sort_desc", "sorting", 1000, "easeInOutQuad");
        // call the event to query
        QueryTool.query.set("sortColName", "");
        QueryTool.query.set("sortOrder", "asc");

        EventBus.trigger("runSort", this);
    },
    checkAllBioSamples: function(e) {
        bioSampleArray = this.model.get("biosampleArray");
        if ($(e.target).attr("checked")) {

            $('[name="bioSampleItem"]').each(function() {
                $(this).attr("checked", true);

                if ($.inArray($(this).val(), bioSampleArray) == -1) {
                    bioSampleArray.push($(this).val());
                }
            });
        } else {

            $('[name="bioSampleItem"]').each(function() {
                $(this).attr("checked", false);
                index = $.inArray($(this).val(), bioSampleArray);
                bioSampleArray.splice(index, 1);

            });

        }


    },
    addSelectedSamples: function() {
        bioSampleArray = this.model.get("biosampleArray");

        if (bioSampleArray.length > 0) {

            EventBus.trigger("addBiosamples", this.model);
            //addBulkSampleCommand([{name: 'rowUris', value: bioSampleArray.join()}]);

        } else {
            //TODO: display message saying not selected samples
            $.ibisMessaging("dialog", "error", "You have not selected any samples to send to your cart.");
            //ajaxModule.showOMButtons();
            return false;
        }
    },
    addVisibleListener: function() {
        this.listenTo(this.model, "change:visible", this.toggleVisibility);

    },
    removeVisibleListener: function() {
        this.stopListening(this.model, "change:visible");
    },
    updateColspan: function() {
        var colspanAmt = this.model.get("colspanAmt");

        this.$el.attr('colspan', colspanAmt);


    },
    updateFrozenColspan: function() {
        var frozenColspanAmt = this.model.get("frozenColspanAmt");

        this.$el.attr('colspan', frozenColspanAmt);


    },
    deselectSelectAll: function() {
        if ($('#sampleCheckAll').length > 0) {
            $('#sampleCheckAll').attr("checked", false);
        }
    },
    checkSelectedBiosamples: function() {
        bioSampleArray = this.model.get("biosampleArray");
        // add item to array when check, remove when unchecked
        if ($('[name="bioSampleItem"]').length > 0) {
            $('[name="bioSampleItem"]').each(function(e) {
                if ($.inArray($(this).val(), bioSampleArray) != -1) {
                    $(this).attr("checked", true);
                }
            });
        }
    },
    destroy: function() {
        this.model.set("frozenWidth", 0);
        this.model.set("frozenColspanAmt", 0);
        QTDT.ColView.__super__.destroy.call(this);
    },
    showColumn: function() {
        this.model.show();
    }
});