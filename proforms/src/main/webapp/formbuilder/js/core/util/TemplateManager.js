/**
 * 
 */
var TemplateManager = {
	/**
	 * Holds all already-compiled full templates.  These are FUNCTIONS that are
	 * passed model attributes to become HTML entities.
	 */
	templates : {},
	
	/**
	 * Adds the given template to the cache with the given name.  This allows
	 * for pre-compilation and retrieval later.
	 * 
	 * @param name the name to store the new template as
	 * @param template Function the template to store
	 * @throws Exception 
	 */
	addTemplate : function(name, template) {
		if (!_.isFunction(template)) {
			throw new Error("template " + name + " is invalid during registration");
		}
		this.templates[name] = template;
		return template;
	},
	
	/**
	 * Retrieves the given template by name.  If that name is not yet registered,
	 * attempts to grab the template, compile it, register it, and return it.
	 * 
	 * @param templateName the name of the template to retrieve
	 * @throws Error if template cannot be found after all tries
	 */
	getTemplate : function(templateName) {
		var template = this.templates[templateName];
		
		// if the template's not yet cached, try to obtain it from the page
		if (typeof template === "undefined" || template === null) {
			try {
				template = this._retrieveTemplate(templateName);
			}
			catch(e) {
				throw e;
			}
		}

		return template;
	},
	
	/**
	 * Initializes all partials currently listed on the page.
	 * Once this is run, these partials can be used forever.
	 */
	initPartials : function() {
		$(".handlebarsPartial").each(function() {
			var $this = $(this);
			Handlebars.registerPartial($this.attr("id"), $this.html());
		});
	},
	
	/**
	 * Initializes a single partial by name.  This registers the partial with
	 * Handlebars so it can be used later.
	 * 
	 * @param name the name (id) of the partial
	 * @throws Error if not found or empty
	 */
	initOnePartial : function(name) {
		var $element = $("#" + name);
		if ($element.length > 0) {
			var html = $element.html();
			if (html !== "") {
				Handlebars.registerPartial($element.attr("id"), html);
			}
			else {
				throw new Error("partial " + name + " is empty");
			}
		}
		else {
			throw new Error("partial " + name + " not found");
		}
	},
	
	/**
	 * Initialize asynchronously-loaded templates.  These templates take the form
	 * <script id="<name>" src="<url>" type="<text/x-handlebars-template>"></script>
	 * 
	 * These must be used carefully because, if an asynch template is attempted to be used
	 * before it's loaded with this function, it will throw an error.  These are NOT
	 * retrieved when needed like others.
	 * 
	 * @param callback any after-complete callback function
	 */
	initAsync : function(callback) {
		$('script[type="text/x-handlebars-template"]').each(function() {
			var $this = $(this);
			$.ajax({
				url: $this.attr('src'),
				success : function(data) {
					TemplateManager.addTemplate($this.attr("id"), Handlebars.compile(data));
					callback();
				}
			});
		});
	},
	
	/**
	 * Loads a single template asynchronously.  This template should take the form
	 * <script id="<name>" src="<url>" type="<text/x-handlebars-template>"></script>
	 * 
	 * @param callback any after-complete callback function
	 */
	loadAsync : function(name, callback) {
		$('#' + name).each(function() {
			var $this = $(this);
			$.ajax({
				url: $this.attr('src'),
				success : function(data) {
					var tmp = TemplateManager.addTemplate($this.attr("id"), Handlebars.compile(data));
					callback(tmp);
				}
			});
		});
	},
	
	/**
	 * Loads a single template asynchronously even without the script tag already added to the page.
	 * 
	 * @param name String the name to give the template
	 * @param source URL source url of the template file
	 * @param callback Function any callback to perform after the template loads
	 */
	loadNewAsync : function(name, source, callback) {
		$("body").append('<script id="' + name + '" src="' + source + '"  type="text/x-handlebars-template"></script>');
		TemplateManager.loadAsync(name, callback);
	},
	
	/**
	 * Try to retrieve the given template name (element ID) from the page.
	 * Caches the template to this.temmplates if found
	 * 
	 * @param name the name of the template to find
	 * @returns function template if found, otherwise null
	 * @throws Error if the template is not found or is empty
	 */
	_retrieveTemplate : function(name) {
		var $element = $("#" + name);
		if ($element.length > 0) {
			var html = $element.html();
			if (html !== "") {
				var template = Handlebars.compile(html);
				this.addTemplate(name, template);
				return template;
			}
			else {
				throw new Error("template " + name + " is empty");
			}
		}
		else {
			throw new Error("template " + name + " not found");
		}
	}
	
};