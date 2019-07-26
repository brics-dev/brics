/**
 * 
 */
QTDT.CellView = BaseView.extend({
	 events: {
		    "click .expandText"   : "expandText",
		    "click .collapseText"   : "collapseText",
		    "click .expandData" : "expandData",
		    "click .collapseData" : "collapseData",
		    "click .viewTriplanar" : "viewTriplanar",
		    "click .downloadImage" : "downloadImage",
		    "click .biosample" : "addBiosample",
		    "click .bioSampleItem" : "addItemToBioSampleList",
		    "click .study_details" : "viewStudyDetails",
		    "click .downloadFile" : "downloadFile"
		    
		    },
	
	
	initialize : function() {
		
		 this.$el = $('<td>', { 
			   id : "cell_"+this.model.cid,
			   "data-id": this.cid,
				"class" : 'cell'
			});
		 
		 
		 
		 if(this.model.get("frozen")) {
			 EventBus.on("frozencellview:removeall",this.destroyFrozen,this);
			//i need this because you can't calculate widths until the object is rendered on the screen
			 EventBus.on("cell:calculateFrozenWidth",this.calcWidth,this);
			// this.listenTo(this.model, "change:width", this.resizeWidth);
			 EventBus.on("cell:updateFrozenWidth",this.resizeWidth,this);
		 } else {
			 EventBus.on("cellview:removeall",this.destroy,this);
			//i need this because you can't calculate widths until the object is rendered on the screen
			 EventBus.on("cell:calculateWidth",this.calcWidth,this);
			// this.listenTo(this.model, "change:width", this.resizeWidth);
			 EventBus.on("cell:updateWidth",this.resizeWidth,this);
			 
			 
		 }
		 
		 EventBus.on("cell:recordCollapseWidth",this.recordCollapseWidth,this);
		 this.listenTo(this.model, "change:visible", this.toggleVisibility);
		this.listenTo(this.model, "change:width", this.resizeWidth);
		
		 
		 this.parseHtml();
			
	},

		
	
	
render: function(){
		
		this.$el.append($('<h5>',{"class": "colCell"}).html(this.model.html()));
		
		return this;
		
	},
	recordCollapseWidth: function (){
		this.model.set("collapseWidth",this.$el.find('h5').width());
	},
	createBioSampleOrderButton : function (tempBioArray, nTd) {

		// tempBioArray[0] = formName
		// tempBioArray[1] = rowUri
		// tempBioArray[2] = value:rowIsDerived
		
		var value = tempBioArray[2];
		var rowIsDerived = tempBioArray[2].substr(tempBioArray[2].indexOf(":")+1);

		var checkBox = '<input name="bioSampleItem" class="bioSampleItem" type="checkbox" value="'
				+ tempBioArray[1] + '|' + tempBioArray[0] + '|' + value + '|' + rowIsDerived
				+ '" style="float:left; margin-right:5px;" />';

		// this is use to create the add to biosample queue button
		var output = checkBox
		 + ' <a title="Click on this link to add this Item to your Order Manager Queue" href="javascript:void(0);" class="biosample ui-corner-all" style="float:left; min-width: 90px;" data-rowuri="'+ tempBioArray[1]
		 +'" data-formname="'
		 + tempBioArray[0]
		 + '" data-value="'+value+'">'
		 + value + '</a><div style="clear:both"></div>';
		
		 return output;

		 
		
	
	},
	createTriPlanarLink : function(triPlanarArray) {
		this.model.studyName = triPlanarArray[0].replace("'", "\\'");
		this.model.datasetName = triPlanarArray[1];
		this.model.triplanarName = triPlanarArray[2];

		if (this.model.triplanarName.lastIndexOf("/") >= 0) {
			this.model.triplanarName = this.model.triplanarName.substr(this.model.triplanarName.lastIndexOf("/") + 1);
		}

		var link = '<a href="javascript:void(0);" class="viewTriplanar">' + this.model.triplanarName + "</a>";
		
		return link;
	},
	viewTriplanar: function () {
		var url = "/query/triplanar.jsp?studyName=" + this.model.studyName + "&datasetName=" + this.model.datasetName +"&triplanarName="+encodeURIComponent(this.model.triplanarName);
		window.open(url, "_blank");
	},
	createThumbnailLink : function(thumbnailArray) {
		this.model.studyName = thumbnailArray[0].replace("'", "\\'");
		this.model.datasetName = thumbnailArray[1];
		this.model.imageName = thumbnailArray[2];

		if (this.model.imageName.lastIndexOf("/") >= 0) {
			this.model.imageName = this.model.imageName.substr(this.model.imageName.lastIndexOf("/") + 1);
		}

		var linkId = this.model.datasetName.replace(/[^a-zA-Z0-9]/g, '_') + "_link";
		var link = '<a id="' + linkId
				+ '" href="javascript:void(0);" class="downloadImage">' + this.model.imageName + "</a>";
		
		return link;
	},
	downloadImage : function () {
		EventBus.trigger("open:image", this.model);
	},
	
	expandText : function() {

		var htmlStr = linkifyStr(this.model.get('parsedHtml'));
		var text = htmlStr;
		text += '<a href="javascript:void(0);" class="collapseText"><b>(less)</b></a>';
		
		//we have to calculate new widths for the cells in this column because we are using divs instead of a table.
		var h5Container = this.$el.find('h5');
		//set collapse width
		if(this.model.get('collapseWidth') == null) { 
			this.model.set('collapseWidth',h5Container.width());
		}
		//remove current width
		h5Container.css('width', 'auto');
		h5Container.html(text);
		//get new width with content
		var newWidth = h5Container.width();
		//set expand width
		this.model.set('expandWidth',h5Container.width());
		//update other cells with new width
		this.model.set("width",newWidth);
		this.model.get('column').get('originalView').adjustColWidth(newWidth,true);
		

	},

	collapseText : function(i, obj) {

		var text = this.model.get('parsedHtml').slice(0, 38)
				+ '... <a href="javascript:void(0);" class="expandText"><b>(more)</b></a>';

		//we have to calculate new widths for the cells in this column because we are using divs instead of a table.
		var h5Container = this.$el.find('h5');
		//remove current width
		h5Container.css('width', 'auto');
		h5Container.html(text);
		
		//get collapse width
		var collapseWidth = this.model.get('collapseWidth') == null ? 0 : this.model.get('collapseWidth');
		//get new width with content
		var newWidth = (collapseWidth < h5Container.width()) ? collapseWidth : h5Container.width();
		this.model.set("width",newWidth);
		//update other cells with new width
		this.model.get('column').get('originalView').adjustColWidth(newWidth,true);

	},
	//use this to expand repeatable groupd data
	expandData : function() {		
		EventBus.trigger("runExpandQuery", this.model);
		
	},//use this to close repeatable group
	collapseData : function() {

		EventBus.trigger("runCollapseQuery", this.model);
	},
	

	resizeWidth: function() {
		var newWidth = this.model.get("width");
		if(newWidth == 0) { 
			//this.$el.width(newWidth);
			this.$el.css("display", "none");
		} else {
			this.$el.width(newWidth);
			var h5Container = this.$el.find("h5");
			h5Container.width(newWidth);
		}
		
		
	},
	calcWidth : function() {
			//this.model.set("width",-1);
		this.stopListening(this.model, "change:width");
		this.model.set("width",this.$el.width() + 5); //add 5px for space to read data
		this.listenTo(this.model, "change:width", this.resizeWidth);
		
		
	},
	toggleVisibility : function() {
		
		if(this.model.get("visible")) {
			this.$el.css("display","table-cell");
			
		} else {
			this.$el.css("display", "none");
		
			
		}
	},
	destroy : function() {
		this.model.set("frozenWidth",0);
		this.model.set("width",0);
		 EventBus.off("cell:updateWidth",this.resizeWidth,this);
		 EventBus.off("cell:calculateWidth",this.calcWidth,this);
		 EventBus.off("cellview:removeall",this.destroy,this);
		this.close();
		QTDT.CellView.__super__.destroy.call(this);
	},
	destroyFrozen : function() {
		
		this.model.set("frozen",false);
		this.destroy();
	},
	parseHtml : function() {
		
		var modelHtml = this.model.html();
		var finalHtml = this.model.get("originalHtml");

		//should this text be disabled, TODO: add css disabled class
		if (finalHtml.indexOf("%greydis%") >= 0) {
	
			finalHtml = finalHtml.replace("%greydis%","");
			
			this.$el.addClass("disabled");
		}
		
		this.model.set("parsedHtml",finalHtml);
		
		
		
		if (finalHtml.toLowerCase().indexOf("tbiosample")  >= 0) {
			
			var tempBioArray = finalHtml.substr(finalHtml.indexOf(":") + 1).split(",");
			if (tempBioArray.length > 0) {
				finalHtml = this.createBioSampleOrderButton(tempBioArray); //TODO: replace nTd
			} else {
				finalHtml = "";
			}
		} else if (finalHtml.toLowerCase().indexOf("tri-planar:") >= 0) {
			
			var triPlanarArray = finalHtml.substr(finalHtml.indexOf(":") + 1).split(",");
			finalHtml = this.createTriPlanarLink(triPlanarArray);
			
		} else if (finalHtml.indexOf("Thumbnail:") >= 0) {
			
			var thumbnailArray = finalHtml.substr(finalHtml.indexOf(":") + 1).split(",");
			
			if (thumbnailArray.length == 3) {
				finalHtml = this.createThumbnailLink(thumbnailArray);
			} else {
				finalHtml = "";
			}
		} else if (finalHtml.indexOf("File:") >= 0){
			
			var fileArray = finalHtml.substr(finalHtml.indexOf(":") + 1).split(",");
			
			if (fileArray.length == 3) {
				finalHtml = this.createFileLink(fileArray);
			} else{
				finalHtml = "";
			}
			
		} else if (finalHtml.toLowerCase().indexOf("-disabledbutton") >= 0) {
			// create expand repeatable group button for empty sets
			finalHtml = '<a href="javascript:void(0);" class="expandDataEmpty ui-corner-all">Empty Group</a><br>';
		} else if (finalHtml.toLowerCase().indexOf("rgbutton") >= 0) {
			
			// create expand repeatable
			// group button
			
			var tempArray = finalHtml.substr(finalHtml.indexOf(":") + 1).split(",");
			
			
			formName = this.model.get("column").get("parent").get("parent").get("name");
	    	var form = QueryTool.page.get("forms").byShortName(formName);
			formUri = "";
	    	if(typeof form === 'object') {
	    		formUri = form.get("uri");
	    	}
			
			this.model.rowUri = tempArray[1];
			this.model.rgFormUri = formUri;
			this.model.rgName = tempArray[3].replace('&', '%26');
			
			finalHtml = '<a href="javascript:void(0);" class="expandData ui-corner-all">Expand Group</a><br>';
			
			
			
		} else if (finalHtml.toLowerCase().indexOf("collapsebutton") >= 0) {
			// create close repeatable
			// group button
			var tempArray = finalHtml.substr(finalHtml.indexOf(":") + 1).split(",");
			
			formName = this.model.get("column").get("parent").get("parent").get("name");
	    	var form = QueryTool.page.get("forms").byShortName(formName);
			formUri = "";
	    	if(typeof form === 'object') {
	    		formUri = form.get("uri");
	    	}
			
			this.model.rowUri = tempArray[1];
			this.model.rgFormUri = formUri;
			this.model.rgName = tempArray[3].replace('&', '%26');
			
			finalHtml = '<a id="' + tempArray[1].replace(/[`~!@#$%^&*()_|+\-=?;:'",.<>\{\}\[\]\\\/]/gi,'')
							+ '" href="javascript:void(0);" class="collapseData ui-corner-all">Collapse Group</a><br>';
			
		} else if (finalHtml.length > 38) {
			// if there is a lot of text
			// we need to allow the user
			// to expand the cell
			var newString = finalHtml;
			var cutText = finalHtml.slice(0, 38);
			if (finalHtml.indexOf("%greydis%") >= 0) {
				newString = finalHtml.replace("%greydis%","");
				cutText = newString.slice(0, 38);
			}
			
			this.model.set("html",newString);
			finalHtml = cutText + '...<a href="javascript:void(0);" class="expandText"><b>(more)</b></a>';
		} 
		
		
		else if (this.model.get("column").get("name") == "Study ID") { 
			var studyId = parseInt(finalHtml);
			if(!isNaN(studyId) ) {
				var studyArray = QueryTool.page.get("studies").where({"studyId": studyId});
				var study = studyArray[0];
				var studyName = study.get("title");
				this.model.set("studyId",studyId );
				this.model.studyName = studyName;
				finalHtml = '<a href="javascript:void(0);" class="study_details" title="'+studyName+'">'+finalHtml +'</a><br>';
			}
		} else {
			//is there a link within the content for this cell if so let's make linkify
			var exp = /(\b(((https?|ftp|file|):\/\/)|www[.])[-A-Z0-9+&@#\/%?=~_|!:,.;]*[-A-Z0-9+&@#\/%=~_|])/i;
			if (exp.test(finalHtml)) {
				
				finalHtml = linkifyStr(finalHtml);
				
				
			} 
		}
		
		this.model.set("html",finalHtml);
	},
	addBiosample : function(e) {
		this.model.biosampleRowUri = $(e.target).data("rowuri");
		this.model.biosampleValue = $(e.target).data("value");
		this.model.biosampleFormName = $(e.target).data("formname");
		EventBus.trigger("addBiosample", this.model);
		
	},
	addItemToBioSampleList : function(e) {
		
		var bioSampleArray = this.model.get("column").get("biosampleArray");
		if ($(e.target).attr("checked")) {
			bioSampleArray.push($(e.target).val());
		} else {
			index = $.inArray($(e.target).val(), bioSampleArray);
			bioSampleArray.splice(index, 1);
		}
		
	},
	viewStudyDetails : function() {
		// it's a study
		EventBus.trigger("open:details", this.model);
		
	},
	createFileLink : function (fileArray) {	
		this.model.studyName = fileArray[0].replace("'", "\\'");
		this.model.datasetName = fileArray[1];
		this.model.fileName = fileArray[2];

		if (this.model.fileName.lastIndexOf("/") >= 0) {
			this.model.fileName = this.model.fileName.substr(this.model.fileName.lastIndexOf("/") + 1);
		}

		var linkId = this.model.datasetName.replace(/[^a-zA-Z0-9]/g, '_') + "_link";
		var link = '<a id="' + linkId
				+ '" href="javascript:void(0);" class="downloadFile">' + this.model.fileName + "</a>";
		
		return link;
	},
	
	downloadFile : function () {		
		EventBus.trigger("download:file", this.model);			
	}
	
	
});