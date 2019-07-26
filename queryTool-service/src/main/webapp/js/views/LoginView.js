/**
 * 
 */
QT.LoginView = BaseView.extend({
	
	events : {
		
	},
	
	initialize : function() {
		Config.containerDiv = $("#loginContainer");
		this.$el = Config.containerDiv;
		this.template = TemplateManager.getTemplate("login");
		window.onresize = function(event) {
			EventBus.trigger("window:resize", window.innerHeight);
		};
		
		EventBus.on("window:resize", this.onWindowResize, this);
	
		
		QT.LoginView.__super__.initialize.call(this);
	},
	
	render : function() {
		$(window).scrollTo(0);
		this.$el.html(this.template(this.model.attributes));
	},
	
	
	

	onWindowResize : function() {
		this.resizeMainContainer(Window.innerHeight);
	},
	
	
	
	
	/**
	 * The main container is the container around the main tabs, excluding the header.
	 * This function sizes that area to be the entire window view height minus the 
	 * header's height.
	 * NOTE: this does not work well if there is a horzontal scrollbar
	 */
	resizeMainContainer : function(innerHeight) {
		var headerHeight = $("#header").height(); // maybe outerHeight()?
		var navHeight = $("#navigation").height();
		$("#mainContent").height(innerHeight - headerHeight - navHeight - Config.windowHeightOffset);
	}
	
	

});