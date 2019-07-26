/**
 * 
 */
QTDT.Pager = BaseModel.extend({
	idAttribute: "uri",
	defaults : {
		currentPage: 1, //offset
		totalPages: 10,
		perPage: 20,
		/**
		   * How many links are shown around the current page.
		   *
		   * @field
		   * @public
		   * @type Number
		   * @default 4
		   */
		  innerWindow: 4,

		  /**
		   * How many links are around the first and the last page.
		   *
		   * @field
		   * @public
		   * @type Number
		   * @default 1
		   */
		  outerWindow: 1,
		  
		  links : ""
		
	},
	
	
	
	initialize : function(){
		
	}
});