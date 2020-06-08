/**
 *
 */
let Application;
export default Application = BaseModel.extend({
  defaults: {
	  apiBaseUrl: ""
  },

  initialize: function() {
    Application.__super__.initialize.call(this);
  }
});
