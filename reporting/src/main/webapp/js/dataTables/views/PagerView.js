/**
 * 
 */
QTDT.PagerView = BaseView.extend({
	events: {
		"click .select_page"   : "goToPage"
	},
	
	/**
	 * A flag to tell if this view is rendered or not
	 */
	rendered : false,
	
	/**
	 * The previous page link label.
	 *
	 * @field
	 * @public
	 * @type String
	 * @default "&laquo; previous"
	 */
	prevLabel: '&laquo;',
	
	/**
	 * The next page link label.
	 *
	 * @field
	 * @public
	 * @type String
	 * @default "next &raquo;"
	 */
	nextLabel: '&raquo;',
	initialize : function() {
		
		this.$el = $("<div>",{id:"pagerContainer"});
		
		this.template = TemplateManager.getTemplate("pagerTemplate"); 
		
	},
	
	render: function() {
		if (this.rendered) {
			this.close();
		}
		else {
			this.rendered = true;
		}
		
		this.listenTo(QueryTool.query, 'change:limit', this.updateView);
		this.listenTo(this.model, 'destroy', this.destroy);
		this.listenTo(this.model, 'change:totalPages', this.render);
		
		EventBus.on("pagerView:render",this.render,this);
		
		this.renderLinks(this.pageLinks());
		this.$el.html(this.template(this.model.attributes));
		this.$el.append('<div class="space-line"></div>');
		return this;
	},
	
	updateView : function() {
		this.model.set("currentPage",1);
		this.model.set("perPage",QueryTool.query.get("limit"));
		this.model.set("totalPages",Math.ceil(QueryTool.query.get("totalRecords") / QueryTool.query.get("limit")));
		this.render();
	 },
	 
	destroy : function() {
	this.close();
		QTDT.PagerView.__super__.destroy.call(this);
	},
	
	goToPage :function(e) {
		EventBus.trigger("DataTableView:removeQueryListener");
		
		var page = $(e.target).attr("data-page") ? $(e.target).attr("data-page") : 1;
		var start = offset = (page - 1) * (this.model && this.model.get("perPage") || 10);
		perPage = this.model.get("perPage");/// this is how many rowse per page
		QueryTool.query.set("offset", start);
		QueryTool.query.paginate();
		// Normalize the offset to a multiple of perPage.
		offset = offset - offset % perPage;
		
		this.model.set("currentPage",Math.ceil((offset + 1) / perPage));
		
		this.render();
	},
	
	pageLinks: function () {
		var links = [];
		var prev = null;
		
		var visible = this.visiblePageNumbers();
		for (var i = 0, l = visible.length; i < l; i++) {
			if (prev && visible[i] > prev + 1) { 
				links.push(this.gapMarker()); 
			}
			links.push(this.pageLinkOrSpan(visible[i], [ 'pager-current active' ]));
			prev = visible[i];
		}
		return links;
	},
		  /**
		   * @returns {Array} The visible page numbers according to the window options.
		   */ 
		  visiblePageNumbers: function () {
		    var windowFrom = this.model.get("currentPage") - this.model.get("innerWindow");
		    var windowTo = this.model.get("currentPage") + this.model.get("innerWindow");
		    var totalPages = this.model.get("totalPages");
		    var outerWindow = this.model.get("outerWindow");
		    // If the window is truncated on one side, make the other side longer
		    if (windowTo > totalPages) {
		      windowFrom = Math.max(0, windowFrom - (windowTo - totalPages));
		      windowTo = totalPages;
		    }
		    if (windowFrom < 1) {
		      windowTo = Math.min(totalPages, windowTo + (1 - windowFrom));
		      windowFrom = 1;
		    }

		    var visible = [];

		    // Always show the first page
		    visible.push(1);
		    // Don't add inner window pages twice
		    for (var i = 2; i <= Math.min(1 + outerWindow, windowFrom - 1); i++) {
		      visible.push(i);
		    }
		    // If the gap is just one page, close the gap
		    if (1 + outerWindow == windowFrom - 2) {
		      visible.push(windowFrom - 1);
		    }
		    // Don't add the first or last page twice
		    for (var i = Math.max(2, windowFrom); i <= Math.min(windowTo, totalPages - 1); i++) {
		      visible.push(i);
		    }
		    // If the gap is just one page, close the gap
		    if (totalPages - outerWindow == windowTo + 2) {
		      visible.push(windowTo + 1);
		    }
		    // Don't add inner window pages twice
		    for (var i = Math.max(totalPages - outerWindow, windowTo + 1); i < totalPages; i++) {
		      visible.push(i);
		    }
		    // Always show the last page, unless it's the first page
		    if (totalPages > 1) {
		      visible.push(totalPages);
		    }

		    return visible;
		  },
		  /**
		   * @param {Number} page A page number.
		   * @param {String} classnames CSS classes to add to the page link.
		   * @param {String} text The inner HTML of the page link (optional).
		   * @returns The link or span for the given page.
		   */
		  
		  //this will be used to create links 
		  pageLinkOrSpan: function (page, classnames, text) {
		    text = text || page;
		   
		    if (page && page != this.model.get("currentPage")) {
		    	
		      return $('<a href="javascript:void(0)" class="select_page"/>').html(text).attr('rel', this.relValue(page)).attr('data-page',page).addClass(classnames[1]).prop('outerHTML');
		    }
		    else {
		      return $('<a/>').html(text).attr('href','javascript:void(0)').attr('data-page',page).addClass(classnames.join(' ')).prop('outerHTML');
		    }
		  },
		  
		  /**
		   * @param {Number} page A page number.
		   * @returns {String} The <tt>rel</tt> attribute for the page link.
		   */
		  relValue: function (page) {
		    switch (page) {
		      case this.previousPage():
		        return 'prev' + (page == 1 ? 'start' : '');
		      case this.nextPage():
		        return 'next';
		      case 1:
		        return 'start';
		      default: 
		        return '';
		    }
		  },
		  /**
		   * @returns {Number} The page number of the previous page or null if no previous page.
		   */
		  previousPage: function () {
		    return this.model.get("currentPage")  > 1 ? (this.model.get("currentPage")  - 1) : null;
		  },

		  /**
		   * @returns {Number} The page number of the next page or null if no next page.
		   */
		  nextPage: function () {
		    return this.model.get("currentPage") < this.model.get("totalPages") ? (this.model.get("currentPage")  + 1) : null;
		  },
		  /**
		   * Render the pagination links.
		   *
		   * @param {Array} links The links for the visible page numbers.
		   */
		  renderLinks: function (links) {
//		    if (this.model.get("totalPages")) {
		    	
		      links.unshift(this.pageLinkOrSpan(this.previousPage(), [ 'pager-disabled', 'pager-prev' ], this.prevLabel));
		     
		      links.push(this.pageLinkOrSpan(this.nextPage(), [ 'pager-disabled', 'pager-next' ], this.nextLabel));
		     this.model.set("links",links);
		      //AjaxSolr.theme('list_items', this.target, links, this.separator);
//		    }
		  },
		  gapMarker: function () {
			    return '<span class="pager-gap">&hellip;</span>';
			  }
		
	
	
	
});