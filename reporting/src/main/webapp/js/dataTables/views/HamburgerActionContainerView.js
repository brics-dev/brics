/**
 * model: Col
 */
QTDT.HamburgerActionContainerView = BaseView.extend({
	
	
	DEOptions : [
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
	     		} ],

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
		"click .goToDetails" : "goToDetails"
	    },
	
	
	initialize : function() {
		
		 this.$el = $('<div>', { 
			 
				"class" : 'actionContainer',
				view : this.cid
			});
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
		
		
		for (var i = 0; i < HBOptions.length; i++) {
			linkList += '<li><a href="javascript:void(0)" class="hblink '
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
    
    onClickShowHide : function() {
    	EventBus.trigger("hamburgerview:showHideCol", {
    		formUri: this.model.formUri,
    		rgUri: this.model.rgUri,
    		rgName: this.model.rgName,
    		deUri: this.model.deUri,
    		deName: this.model.get("name"),
    		// inverting this because the state has NOT been changed yet
    		// and visible denotes what it should be set TO
    		visible: !this.model.get("visible"),
    	});
    	this.destroy();
    	$('.actionContainer').slideUp(300);
    	$('.actionContainer').remove();
    },
    
    toggleRgShow: function() {
    	this.model.get("children").each(function(col) {
    		EventBus.trigger("hamburgerview:showHideCol", {
        		formUri: col.formUri,
        		rgUri: col.rgUri,
        		rgName: col.rgName,
        		deUri: col.deUri,
        		deName: col.get("name"),
        		visible: true,
        	});
    	});
    	
    	this.model.rgChildrenShow();
    	
    	this.destroy();
    	$('.actionContainer').slideUp(300);
    	$('.actionContainer').remove();
    },
    
    toggleRgHide : function() {
    	this.model.get("children").each(function(col) {
    		EventBus.trigger("hamburgerview:showHideCol", {
        		formUri: col.formUri,
        		rgUri: col.rgUri,
        		rgName: col.rgName,
        		deUri: col.deUri,
        		deName: col.get("name"),
        		visible: false,
        	});
    	});
    	
    	this.model.rgChildrenHide();
    	
    	this.destroy();
    	$('.actionContainer').slideUp(300);
    	$('.actionContainer').remove();
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
    
    goToDetails : function() {
    	var dataElement = QueryTool.page.get("dataElements").byShortName(this.model.get("name"));
    	EventBus.trigger("open:details", dataElement);
    },
    
    destroy : function() {
    	
		this.close();
		QTDT.HamburgerActionContainerView.__super__.destroy.call(this);
	}
});