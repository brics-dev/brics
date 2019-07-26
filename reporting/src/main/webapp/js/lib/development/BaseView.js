/**
 * BaseView.js
 * 
 * Provides the most basic functionality for a View in the ProFoRMS Form Builder.
 * 
 * To extend:
 * var x = BaseView.extend({});
 * 
 * NOTE: ClassName.__super__.methodName.call(this, [parameters]) only needs to
 * be called when the child class overrides methods in this class and then only
 * from the overriding method.  Otherwise, just use 
 * this.mothodName([parameters]);
 * 
 * @author Joshua Park
 */
var BaseView = Backbone.View.extend({
	template : function(){},
	_modelBinder : undefined,
	model : null,
	bindings : "",
	
	events : {},
	
	/**
	 * Sets up the view to handle data.  This will probably need to be
	 * overridden by a more appropriate initializer.  However, this is a useful
	 * method so it can be called with __super__.
	 * 
	 * @example ClassName.__super__.initialize.call(this, [parameters]);
	 * 
	 * @returns View
	 */
	initialize : function() {
		this._modelBinder = new Backbone.ModelBinder();
		return this;
	},
	
	/**
	 * Carries out any needed after-render operations such as resetting
	 * the model binding.
	 * 
	 * @example ClassName.__super__.render.call(this, [parameters]);
	 * @param (optional) passedModel the model to set.
	 */
	render : function(passedModel) {
		var model = passedModel || this.model;
		this.setModel(model);
	},
	
	/**
	 * Sets the model for this view to the specified model to allow binding.
	 * Unbinds from the previous model (if any) and binds to the new model.
	 * 
	 * @param model the Model to set up (can be an object other than a Model)
	 * @throws Error if there was an error during binding
	 */
	setModel : function(model) {
		try {
			// do NOT need to unbind here.  Done inside _modelBinder.bind()
			this.model = model;
			this._bindToModel(model);
		}
		catch(e) {
			throw new Error("There was an error during model binding");
		}
	},
	
	/**
	 * Binds this View to the given Mode using the ModelBinder plugin.
	 * If already bound, this will unbind the old model
	 * 
	 * @param model Model to bind this View to
	 * @param bindings (optional) defines specific binding options
	 * @throws Error if bind fails
	 */
	_bindToModel : function(model) {
		if (_.isUndefined(this.bindings) || !_.isString(this.bindings)) {
			this.bindings = "";
		}
		
		this._modelBinder.bind(
				model, 
				this.el, 
				this.bindings, 
				{changeTriggers: {'' : 'change', 'textarea, [type="text"]' : 'keyup', '[contenteditable]': 'blur'}});
	},
	
	/**
	 * Completely unbinds this.model from this View.
	 */
	_unbindFromModel : function() {
		if (!_.isUndefined(this._modelBinder)) {
			try {
				this._modelBinder.unbind();
			}
			catch(e) {
				// if we failed to unbind, we'll just have to leak that memory
				// it'll be okay
				Log.developer.error("modelbinder.unbind() failed " + this);
			}
		}
	},
	
	/**
	 * Assigns a subview to a given sub-Element of this.el.  Meaning: enables
	 * sub-views to only control part of the div controlled by the main view.
	 * 
	 * @link http://ianstormtaylor.com/assigning-backbone-subviews-made-even-cleaner/
	 * 
	 * @param selector string selector to find the sub-view's parent DOM
	 * @param view Backbone.View subview
	 */
	assign : function(selector, view) {
		var selectors;
	    if (_.isObject(selector)) {
	        selectors = selector;
	    }
	    else {
	        selectors = {};
	        selectors[selector] = view;
	    }
	    if (!selectors) return;
	    _.each(selectors, function (view, selector) {
	        view.setElement(this.$(selector)).render();
	    }, this);
	},
	
	/**
	 * Performs the same operation as this.destroy() except it does NOT remove
	 * the view el from the DOM.  This function is used when switching from
	 * one View to another on the same EL and same model.
	 * 
	 * @returns DOM el (not the jquery one $el)
	 */
	close : function() {
		this._unbindFromModel();
		this.unbind();
		this.stopListening();
		EventBus.stopListening(this);
		return this.el;
	},
	
	/**
	 * Closes this View and cleans up the used memory.  This method should be
	 * overridden by a concrete implementation that clears its specific memory
	 * footprint and then calls this.
	 * 
	 * example of an overriding method:
	 * destroy : function() {
	 * 	this.EditorDialog.destroy(this);
	 * 	SubClass.__super__.destroy.call(this);
	 * }
	 * 
	 * @returns View
	 */
	destroy : function() {
		this.close();
		this.remove();
		return this;
	}
});