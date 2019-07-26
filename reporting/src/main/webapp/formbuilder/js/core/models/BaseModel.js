/**
 * 
 */
var BaseModel = Backbone.Model.extend({
	save : function() {
		//Log.developer.info("save model " + this);
	},
	
	sync : function() {
		//Log.developer.info("synch model " + this);
	},
	
	set: function(key, value, options) {
	      var attrs;
	      if (key == null) return this;
	      // Handle both `"key", value` and `{key: value}` -style arguments.
	      if (typeof key === 'object') {
	    	  attrs = key;
	    	  options = value;
	      } else {
	    	  (attrs = {})[key] = value;
	      }

	      options || (options = {});
	      
	      var property;
	      for (property in attrs) {
	    	  var attrVal = attrs[property];
	    	  // attrVal is the value of attrs[property]
	    	  
		      // any string process
		      if (typeof attrVal === "string") {
		    	  // trim the input in all cases
		    	  attrs[property] = $.trim( attrVal );
		      }

		      // do any other custom property changes here
	      }
	      Backbone.Model.prototype.set.call( this, attrs, options );	      
	      return this;
	}
});