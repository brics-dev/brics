/**
 * Represents knowledge about the current page of the application
 */

let Page;
export default Page = BaseModel.extend({
  defaults: {
  },

  initialize: function() {
    Page.__super__.initialize.call(this);
  }
});
