var Page = {
	editorDefaultConfig : {
		autoOpen: false,
		width: "100%",
		position: {my: "bottom center", at: "bottom+20px", of: $(window)},
		create: function(event, ui) {
			$(event.target).parent().css('position', 'fixed');
		},
		resizeStop: function(event, ui) {
			var position = [(Math.floor(ui.position.left) - $(window).scrollLeft()),
							 (Math.floor(ui.position.top) - $(window).scrollTop())];
			$(event.target).parent().css('position', 'fixed');
			$(dlg).dialog('option','position',position);
		},
		draggable: false
	},
	
	form : null,
	sectionEditView : null,
	questionEditView : null,
	formEditView : null,
	activeEditorView : null,
	
	setActiveEditor : function(view) {
		if (this.activeEditorView != null) {
			this.destroyActiveEditor();
		}
		this.activeEditorView = view;
	},
	
	destroyActiveEditor : function() {
		if (this.activeEditorView !== null) {
			this.activeEditorView.close();
			this.activeEditorView = null;
		}
	}
};

var Form = Backbone.Model.extend({ 
	status : {
		ACTIVE : "active",
		INACTIVE : "inactive"
	},
	
	defaults : {
		name : "form 1",
		id : 0,
		status : this.status.INACTIVE
	},
	
	sections : null,
	questions : null,
	
	initialize: function(){
		this.sections = new Sections();
	},
	
	section : function(id) {
		return this.sections.get(id);
	},
	
	addSection : function(config) {
		this.sections.create(config);
		//alert(Page.form.sections.length);
	},
	
	save : function() {
		console.log("save form");
	}
});

var Sections = Backbone.Collection.extend({
	initialize : function() {
		this.model = Section;
	},

});

var Section = Backbone.Model.extend({
	defaults : {
		active : false,
		id : "-1",
		name : "New Section",
		sectionText : "",
		collapsible : false,
		responseImage : false,
		repeatable : false,
		repeatableParent : null,
		repeatMin : 0,
		repeatMax : 0,
		repeatGroup : "",
		divId : ""
	},
	
	url : "",
	
	initialize : function(){
		this.id = $(".section").length * -1;
//		if (!this.repeatable || this.repeatableParent == "-1") {
//			this.render();
//		}
//		
//		Page.sectionEditView.render(this);
	},
	
	save : function() {
		console.log("save section");
	}
});

var FormView = Backbone.View.extend({
	//this.sections.create(model);
	template: function() {},
	model : null,
	$formContainer : $(),
	initialize : function() {
		this.$formContainer = $("#app");
		this.listenTo(Page.form.sections, 'add', this.addSection);
		//this.listenTo(Page.form.sections, 'remove', this.removeSection);
		EventBus.on("deleteSection", this.deleteSection);
	},
	render : function() {},
	
	/**
	 * Adds a section to the page
	 */
	addSection : function(section) {
		var view = new SectionActiveView({model: section});
		view.render();
	},
	
	deleteSection : function(section) {
		
		var prevSec = $(".activeSection").prev(".section");
		if(prevSec.length > 0) {
			prevSec.addClass("activeSection");
			
		}else {
			var nextSec = $(".activeSection").next(".section");
			if(nextSec.length > 0) {
				nextSec.addClass("activeSection");
			}
		}
		
		Page.form.sections.remove(section);

	},
	
	close : function() {
		this.stopListening();
	}
});

var SectionInactiveView = BaseView.extend({
	className : "section",
	
	initialize : function() {
		SectionInactiveView.__super__.initialize.call(this);
		// this.template = TemplateManager.getTempate("sectionTemplate");
		this.template = Handlebars.compile($("#sectionInactiveTemplate").html());
	},
	
	render : function() {
		this.$el.html(this.template(this.model.attributes));
		$("#app").append(this.$el);
		this.setModel.call(this, this.model);
	}
});

var SectionActiveView = BaseView.extend({
	className : "section",
	events : {
		"click .editButton" : "edit",
		"click .deleteButton" : "deleteSection"
	},
	
	initialize : function() {
		SectionActiveView.__super__.initialize.call(this);
		// this.template = TemplateManager.getTempate("sectionTemplate");
		this.template = Handlebars.compile($("#sectionActiveTemplate").html());
		
	},
	
	render : function() {
		this.$el.html(this.template(this.model.attributes));
		$("#app").append(this.$el);
		this.setModel.call(this, this.model);
		EventBus.trigger("changeActiveSection",this);
	},
	
	edit : function() {
		Page.sectionEditView.render(this.model);
	},
	
	deleteSection : function() {
		alert("deleting section");
		//alert( this.model.get("name"));
		//Page.form.sections.remove(this.model);
		//alert(Page.form.sections.length);
		//this.remove();
		//this.unbind();
		
		
		
		EventBus.trigger("deleteSection",this.model);
		this.destroy();
		
		
	}
});

$(document).ready(function() {
	Page.form = new Form();
	var formView = new FormView({model: Page.form});
	
	Page.formEditView = new FormEditView({model: Page.form});
	Page.sectionEditView = new SectionEditView();
	
	var section1 = Page.form.addSection({name:'Section One'});
	var section2 = Page.form.addSection({name:'Section Two'});
	var section3 = Page.form.addSection({name:'Section Three'});
	var section4 = Page.form.addSection({name:'Section Four'});
	var section5 = Page.form.addSection({name:'Section Five'});
	
	
});