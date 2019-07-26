/**
 * model: dataCart
 */
QT.RefineDataCartView = BaseView.extend({
	
	
	events : {
		"click #resetFormsButton" : "resetForms",
		"click #joinFormsButton" : "joinForms"
	},
	
	
	initialize : function() {
		this.template = TemplateManager.getTemplate("refineDataCart");
		this.listenTo(this.model.forms, "add" ,this.addNewForm);
		
		EventBus.on("window:resize", this.onWindowResize, this);
		// changing tabs is effectively the same as window resizing
		EventBus.on("select:stepTab", this.onWindowResize, this);
		EventBus.on("runQuery", this.onRunQuery, this);
		EventBus.on("runJoinQuery", this.onRunQuery, this);
		EventBus.on("query:reset", this.rerender, this);

		QT.RefineDataCartView.__super__.initialize.call(this);

		EventBus.on("enableDroppables", this.enableDroppables, this);
	},
	
	
	render : function() {
		this.$el.html(this.template(this.model.attributes));
		QT.RefineDataCartView.__super__.render.call(this);
		
		this.resizeDataCartContainer();
		var view = this;
	    this.$( "#firstForm,#secondForm,#thirdForm,#fourthForm,#fifthForm" ).droppable({
	    	accept : ".draggable",
	    	drop : function(event,ui) {
	    		//get ui.draggable 
	    		//ui.draggable.before("<placeholder class="uriOfForm"></placeholder>
	    	
	    		//added to handle the shuffling of forms inside refineDataJoinContainer
	            var $originalContainer = ui.draggable.parent();
	    		var $formId=$originalContainer.get(0).id;
	    				
	    		var uri = ui.draggable.attr("id").replace("refineDataForm_", "");
	    		ui.draggable.before("<placeholder class=\"" + uri + "\"></placeholder>");
	    		$(this).append(ui.draggable.css({
	    			position : "absolute",
	    			top : "0",
	    			left : "0"
	    		}));
	    		
	    		var paddingRight = Number(ui.draggable.css("padding-right").replace("px", ""));
	    		// don't use 90% here
	    		ui.draggable.width(($(this).width() - paddingRight));
	    		
	    		$(this).height(ui.draggable.height());
	    		
	    		//disable join form only if forms are dragged from refineDataFormContainer
	    		if($("#firstForm").has('.draggable').length && $("#secondForm").has('.draggable').length && $formId!="firstForm" && $formId!="secondForm" && $formId!="thirdForm" && $formId!="fourthForm" && $formId!="fifthForm" ){
	    			$("#joinFormsButton").removeClass("disabled");
	    		}
	    		else{
	    			$("#joinFormsButton").addClass("disabled"); 			
	    		}
	    				    		
	    		EventBus.trigger("enableDroppables");
	    	},
    		over: function(event, ui) {
    		    // If the droppable element we're hovered over already contains a .draggable element, 
    		    // don't allow another one to be dropped on it
    		    if($(this).has('.draggable').length) {
    		        $(this).droppable('disable');
    		    }
    		  },
	    	out : function(event, ui) {
	    		$(this).droppable('enable');
	    	}
	    });
	    
	    
	    this.$( ".refineDataFormContainer" ).droppable({
	    	accept : ".draggable",
	    	drop : function(event,ui) {
	    		//get ui.draggable 
	    		//ui.draggable.before("<placeholder class="uriOfForm"></placeholder>
	    		//refineDataForm_http://ninds.nih.gov/dictionary/ibis/1.0/FormStructure/AthenaMOM2_v1.0
	    		// checks if the join button should be enabled
	    		
	    		//TO DO remove the commented out code below that is never called
	    		/*if ($("#primaryForm").find(".droppable").length > 0
	    				&& $("#secondaryForm").find(".droppable").length > 0
	    				&& $("#tertiaryForm").find(".droppable").length > 0) {
	    			$("#joinFormsButton").addClass("disabled");
	    		}*/
	    		
	    		var $originalContainer = ui.draggable.parent();
	    		
	    		//check if the join button should be enabled
	    		//disable the button if form is dragged from primary and secondary 
	    		var $formId=$originalContainer.get(0).id;	
	    		
	    		if($formId=="firstForm" ||$formId=="secondForm" ){
	    			$("#joinFormsButton").addClass("disabled");
	    			$.ibisMessaging("dialog", "warning", "Reset to perform a new form join");
	    		}
	    		// we don't want to remove height assignment on the big list!
	    		if (!$originalContainer.is($(".refineDataFormContainer"))) {
	    			$originalContainer.height("auto");
	    		}
	    		$originalContainer.droppable("enable");
	    		
	    		view.moveFormToList(ui.draggable);
	    		
	    		var paddingRight = Number(ui.draggable.css("padding-right").replace("px", ""));
	    		ui.draggable.width(($(this).width() - paddingRight) * 0.9);
	    		
	    		ui.draggable.removeAttr("width");
	    		EventBus.trigger("enableDroppables");
	    	}
	    	
	    	
	    });
		
		var dcForms = this.model.forms;
		for(var i=0;i<dcForms.length;i++) {
			var dcForm = dcForms.at(i);
			this.addNewForm(dcForm);
		}
		
		this.buildDraggable();
		
		$("#secondForm").droppable('disable');
		$("#thirdForm").droppable('disable');
		$("#fourthForm").droppable('disable');
		$("#fifthForm").droppable('disable');
		
	},
	
	moveFormToList : function($form) {
		var uri = $form.attr("id").replace("refineDataForm_", "");
		var $placeholder = this.$('placeholder[class="' + uri + '"]');
		$placeholder.after($form.css({
			position : "relative",
			top : "auto",
			left : "auto"
		}));
		$placeholder.remove();
		var parentWidth = $form.parent().width()
		var paddingRight = Number($form.css("padding-right").replace("px", ""));
		$form.width((parentWidth - paddingRight) * 0.9);
	},
	
	rerender : function() {
		this.destroyDraggable();
		
		this.empty();
		this.render();
		
		EventBus.trigger("enableDroppables");
		EventBus.trigger("DataTableView:destroyTable",this);
	},
	
	/**
	 * Responds to the cart performing a query.  Need to make sure that the active forms
	 * are the only ones in here that are selected.
	 */
	onRunQuery : function(obj) {
		
		var selectedFormUris = obj.formUris;
		
		this.$el.parent().find(".dataCartForm").removeClass("dataCartActive");
		for (var i = 0; i < selectedFormUris.length; i++) {
			var uri = _.replaceAll(selectedFormUris[i], ".", "\.");
			uri = _.replaceAll(uri, ":", "\:");
			this.$el.parent().find('[id="refineDataForm_' + selectedFormUris[i] + '"]').addClass("dataCartActive");
			
			if (selectedFormUris.length == 1) {
				var view = this;
				// there should be nothing in the first, second, third, fourth, fifth
				this.$("#firstForm,#secondForm,#thirdForm, #fourthForm, #fifthForm").find(".dataCartForm").each(function() {
					view.moveFormToList($(this));
				});
			}
		}
	},
	
	resetForms : function() {
		EventBus.trigger("query:reset");
	},
	
	joinForms : function() {
		if ( window.console && console.log ){
		console.log("joining...");
		}
		if (!this.$("#joinFormsButton").hasClass("disabled")) {
			// get the list of selected form URIs
			var formIds = [];
			var formIds = ["#firstForm", "#secondForm", "#thirdForm", "#fourthForm", "#fifthForm"]
					.map(function(containerId) {
						var $formDiv = this.$(containerId + " .dataCartForm");
						if ($formDiv.length > 0) {
							$formDiv.addClass("dataCartActive");
				    		//refineDataForm_http://ninds.nih.gov/dictionary/ibis/1.0/FormStructure/AthenaMOM2_v1.0
							return $formDiv.attr("id").replace("refineDataForm_", "");
						}
					})
					.filter(function(ids) {
						return typeof ids !== "undefined";
					});
		
			// call the event to query
			QueryTool.query.set("selectedForms", formIds);
			
			var obj = { notOnActivate:false, formUris:formIds };
			EventBus.trigger("runJoinQuery", obj);
		}
	},
	
	buildDraggable: function() {
		this.$( ".draggable" ).draggable({
			revert : "invalid",
			snap: ".droppable",
			snapMode: "corner",
			helper: "clone",
			snapTolerance : 30,
			scroll: false,
			containment: $("#refineDataCartContainer"),
			start : function(event, ui) {
				
			},
			stop: function(event, ui) {
				var $this = $(this);
			}
		});
	},
	
	
	destroyDraggable: function() {
		this.$( ".draggable" ).each(function(index, item) {
			var $this = $(this);
			if (typeof $this.draggable("instance") !== "undefined") {
				$this.draggable("destroy");
			}
		});
	},
	
	rebuildDraggable: function() {
		this.destroyDraggable();
		this.buildDraggable();	
	},
	
	addNewForm : function(newForm) {
		var $container = this.$(".refineDataFormContainer");
		var view = new QT.RefineDataFormView({model : newForm});
		view.render($container);
		this.rebuildDraggable();
	},
	
	enableDroppables : function() {
		if (!$("#firstForm").has('.draggable')) {
			$("#firstForm").droppable('enable');
		}
		
		if($("#firstForm").has('.draggable').length) {
			$("#secondForm").droppable('enable');
		}
		else {
			$("#secondForm").droppable('disable');
			$("#firstForm").droppable('enable');		 
		}
				 
		if($("#secondForm").has('.draggable').length) {
			$("#thirdForm").droppable('enable');
		}
		else {
			$("#thirdForm").droppable('disable');
		}
		
		if($("#thirdForm").has('.draggable').length) {
			$("#fourthForm").droppable('enable');
		}
		else {
			$("#fourthForm").droppable('disable');
		}
		
		if($("#fourthForm").has('.draggable').length) {
			$("#fifthForm").droppable('enable');
		}
		else {
			$("#fifthForm").droppable('disable');
		}
		
		
	},
	
	onWindowResize : function() {
		this.resizeDataCartContainer();
	},
	
	resizeDataCartContainer : function() {
		var containerHeight = 0;
		var windowHeight = window.innerHeight;
		
		var currentTop = this.$el.offset().top;
		var paddingTop = Number(this.$el.css("padding-top").replace("px", ""));
		var paddingBottom = Number(this.$el.css("padding-bottom").replace("px", ""));
		var extraPadding = Config.scrollContainerBottomOffset;
		var borderThickness = 1;
		containerHeight = (windowHeight - currentTop - (paddingTop + paddingBottom) - extraPadding - (borderThickness * 2) - 10) / 2;
		this.$el.height(containerHeight);
		
		this.resizeFormList(containerHeight);
		this.resizeJoinPanel(containerHeight);
	},
	
	resizeFormList : function(parentHeight) {
		var $parent = this.$el;
		var parentPaddingTop = Number($parent.css("padding-top").replace("px", ""));
		var parentPaddingBottom = Number($parent.css("padding-bottom").replace("px", "")); 
		var $element = this.$(".refineDataFormContainer");
		var myTop = $element.offset().top;
		var topDifference = myTop - $parent.offset().top; 
		var finalHeight = parentHeight - topDifference;
		$element.height(finalHeight);
	},
	
	resizeJoinPanel : function(parentHeight) {
		var $parent = this.$el;
		var parentPaddingTop = Number($parent.css("padding-top").replace("px", ""));
		var parentPaddingBottom = Number($parent.css("padding-bottom").replace("px", "")); 
		var $element = this.$(".dataCartActiveFormsContainer");
		var myTop = $element.offset().top;
		var topDifference = myTop - $parent.offset().top;
		var buttonsHeight = this.$(".dataCartActiveFormsButtonsContainer").height();
		var finalHeight = parentHeight - topDifference - buttonsHeight;
		$element.height(finalHeight);
	},
	
	empty : function() {
		this.$el.empty();
	}
	
	
	
	
	
	
	
	
});








