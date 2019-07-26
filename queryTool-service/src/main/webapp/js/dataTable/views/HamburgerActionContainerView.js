/**
 * model: Col
 */
QTDT.HamburgerActionContainerView = BaseView.extend({
	
	
	DEOptions : [],

	     		Rgmenuoptions : [
	     		// Commented out in response to CRIT-1794. This is requested
	     		// functionality but has a time-consuming bug in it
	     		{
	     			title : 'Show Entire Group',
	     			callback : "rgShow"
	     		}, {
	     			title : 'Hide Entire Group',
	     			callback : "rgHide"
	     		} ],
	 
	 events: {
	    "click .showHide"   : "onClickShowHide",
	    "click .applyFilter" : "applyFilter",
	    "click .rgShow"   : "toggleRgShow",
		"click .rgHide"   : "toggleRgHide",
		"click .goToDetails" : "goToDetails",
		"click .applyDiagnosisFilter" : "applyDiagnosisFilter"
	    },
	
	
	initialize : function() {
		
		 this.$el = $('<div>', { 
			 
				"class" : 'actionContainer',
				view : this.cid
			});
		 
		 this.DEOptions = [
	     		// Commented out in response to CRIT-1794. This is requested
	     		// functionality but has a time-consuming bug in it
	     		{
	     			title : 'Hide Column',
	     			callback : "showHide"
	     		}, {
	     			title : 'Go to Details Page',
	     			callback : "goToDetails"
	     		}, {
	     			title : 'Apply Filter',
	     			callback : "applyFilter"
	     		} ];
		 
		//add a filter specifically for GUID
			var deName =  this.model.get("name");
			if(deName === "GUID" && System.environment === "pdbp") {
				var diagnosisOption = this.DEOptions.filter(function(option){ return option.title === "Filter on Diagnosis" });
				
				if(diagnosisOption.length === 0){
					this.DEOptions.push({
		     			title : 'Filter on Diagnosis Change',
		     			callback : "applyDiagnosisFilter"
		     		});
				}
					
				//this.events = Object.assign({"click .applyDiagnosisFilter" : "applyDiagnosisFilter" }, this.events);
				
				
			}
	    	
		 
		if (this.model.get("frozen")) {
			EventBus.on("frozenhamburgerview:removeall",this.destroy,this);
		} else {
			EventBus.on("hamburgerview:removeall",this.destroy,this);
			
		}
		
		
	},
	
	render: function() {
		linkList = '';
		HBOptions = [];
		
		if(this.model.get("headerType") == "dataElement"){
			HBOptions = this.DEOptions; 
		} else if (this.model.get("headerType") == "repeatableGroup") { 
			HBOptions = this.Rgmenuoptions; 
		}
		
		var cellObj = {
				colId : this.model.get("col") || 0
	            };
		var cellObjJson = JSON.stringify(cellObj);
		
		for (var i = 0; i < HBOptions.length; i++) {
			linkList += '<li><a href="javascript:void(0)" data-model=\''+cellObjJson+'\' class="hblink '
					+ HBOptions[i].callback
					+ '">'
					+ HBOptions[i].title
					+ '</a></li>';
		}
		actionContainer = '<ul>'
				+ linkList
				+ '</ul>';
		this.$el.append(actionContainer);
		
      return this;
    },
    applyFilter: function() {
    	var deUri = "";
    	var formUri = "";
    	var deName = this.model.get("name");
    	var rgName = this.model.get("parent").get("name");
    	var formName = this.model.get("parent").get("parent").get("name");
    	
    	
    	// this method will retrieve the DE details from the server and then call the eventbus
    	var dataElement = QueryTool.page.get("dataElements").byShortName(this.model.get("name"));
    	if(typeof dataElement === 'object') {
    		deUri = dataElement.get("uri");
    	}
    	
    	var form = QueryTool.page.get("forms").byShortName(formName);
    	if(typeof form === 'object') {
    		formUri = form.get("uri");
    	}
   
    	
    	$('[uri="'+formUri+'"] [groupname="'+rgName+'"] [dename="'+deName+'"] .selectCriteriaDeFilter').click();
    	
    	
    	this.destroy();
    	$('.actionContainer').slideUp(300);
    	$('.actionContainer').remove();
    
    },
    applyDiagnosisFilter: function() {
    	var deUri = "";
    	var formUri = "";
    	var deName = this.model.get("name");
    	var rgName = this.model.get("parent").get("name");
    	var formName = this.model.get("parent").get("parent").get("name");
    	
    	
    	// this method will retrieve the DE details from the server and then call the eventbus
    	var dataElement = QueryTool.page.get("dataElements").byShortName(this.model.get("name"));
    	if(typeof dataElement === 'object') {
    		deUri = dataElement.get("uri");
    	}
    	
    	var form = QueryTool.page.get("forms").byShortName(formName);
    	if(typeof form === 'object') {
    		formUri = form.get("uri");
    	}
   
    	
    	$('[uri="'+formUri+'"] [groupname="'+rgName+'"] [dename="highlight_diagnosis"][filterType="CHANGE_IN_DIAGNOSIS"] .selectCriteriaDeFilter').click();
    	
    	
    	this.destroy();
    	$('.actionContainer').slideUp(300);
    	$('.actionContainer').remove();
    
    },
    
    goToDetails : function() {
    	var dataElement = QueryTool.page.get("dataElements").byShortName(this.model.get("name"));
    	EventBus.trigger("open:details", dataElement);
    },
    
    destroy : function() {
    	var deName =  this.model.get("name");
		if(System.environment === "pdbp") {
			this.DEOptions = this.DEOptions.filter(function(option){ return option.title !== "Filter on Diagnosis" });
		}
		this.close();
		QTDT.HamburgerActionContainerView.__super__.destroy.call(this);
	}
});