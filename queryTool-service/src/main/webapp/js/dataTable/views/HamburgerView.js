/**
 * model: Col
 */
QTDT.HamburgerView = BaseView.extend({
	
	

	 
	 events: {
	 
	    "click .tableHamburger"   : "openHamburger"
	    },
	
	
	initialize : function() {
		this.template = TemplateManager.getTemplate("hamburgerTemplate"); 
		
		if (this.model.get("frozen")) {
			EventBus.on("frozenhamburgerview:removeall",this.destroy,this);
		} else {
			EventBus.on("hamburgerview:removeall",this.destroy,this);
			//EventBus.on("hamburgerview:showHideCol",this.altToggleShowHide,this);
		}
		
		
	},
	
	render: function() {
		var cellObj = {
				row : this.model.get("row") || 0,
				col : this.model.get("col") || 0
	            };
		var cellObjJson = JSON.stringify(cellObj);
		 //data-model=\''+cellObjJson+'\'
      this.$el.html(this.template(this.model.toJSON()));
      this.$el.children('.tableHamburger').attr( "data-model", cellObjJson );
      return this;
    },
    
    altToggleShowHide: function(specs) {
    	
    	if(this.model.formUri == specs.formUri 
    			&& this.model.rgName == specs.rgName 
    			&& this.model.deUri == specs.deUri) { 
    		this.model.showHide();
    	}
    },
    applyFilter: function() {
    	deUri = "";
    	formUri = "";
    	deName = this.model.get("name");
    	rgName = this.model.get("parent").get("name");
    	formName = this.model.get("parent").get("parent").get("name");
    	
    	
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
    
    },
    
    goToDetails : function() {
    	var dataElement = QueryTool.page.get("dataElements").byShortName(this.model.get("name"));
    	EventBus.trigger("open:details", dataElement);
    },
    
    openHamburger : function() {
    	
    	var $titleHamburger = this.$el.children('.tableHamburger'); 
    	var newContainer = new QTDT.HamburgerActionContainerView({model: this.model});
		$("body").append(newContainer.render().$el);
		var offset = $titleHamburger
		.offset();
		
		$('.actionContainer')
				.offset(
						{
							top : offset.top + 13,
							left : offset.left
						});
		$('.actionContainer')
				.slideDown(300);
    },
    
    destroy : function() {
    	EventBus.off("hamburgerview:removeall",this.destroy,this);
    	EventBus.off("hamburgerview:showHideCol",this.altToggleShowHide,this);
		this.close();
		QTDT.HamburgerView.__super__.destroy.call(this);
	}
});