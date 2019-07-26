
var breadcrumbSelfReference;
var comparisonEditor;
function initBreadcrumbSelfReference(breadcrumb) {
	breadcrumbSelfReference = breadcrumb;
}

// fix IE indexof problems
if(!Array.indexOf){
    Array.prototype.indexOf = function(obj){
        for(var i=0; i<this.length; i++){
            if(this[i]==obj){
                return i;
            }
        }
        return -1;
    }
}


function Breadcrumb(config) {
	this.compareList = new Array();
	this.compareData = new Array();
	
	this.defaults = {
		orgName			:	"",
		siteName		:	"",
		userName		:	"",
		organizations	:	[],
		sites			:	[],
		users			:	[],
		containerId		:	"",
		orgDefaultText	:	"Organizations",
		siteDefaultText	:	"Sites",
		userDefaultText	:	"Users",
		org				:	{},
		site			:	{},
		user			:	{},
		linkBaseUrl		:	"view/",
		subLinkBaseUrl	:	"switchTo/",
		baseUrl			:	"",
		getSitesUrl		:	"",
		getUsersUrl		:	"",
		onrebuild		:	function() {},
		onbuild			:	function() {},
		comparisonLinkId:	"openComparisonEditorLink"
	};
	this.config = $.extend({}, this.defaults, config);
	this.jQueryElement = null;
	this.element = null;
	initBreadcrumbSelfReference(this);
}

	/**
	 * The main function to build the breadcrumb.  Builds the entire breadcrumb
	 * using the below objects
	 * 
	 * @param config the configuration data from jquery
	 */
	Breadcrumb.prototype.build = function() {
		this.jQueryElement = $("#"+this.config.containerId);
		this.element = document.getElementById(this.config.containerId);
		this.jQueryElement.empty();
		// load in reverse order so we can give references up the line
		this.config.user = new User(this.config);
		this.config.site = new Site(this.config);
		this.config.org = new Org(this.config);
		this.config.org.build();
		this.config.site.build();
		this.config.user.build();
		
		this.buildComparisonEditor();
		this.config.onbuild;
	}
	
	Breadcrumb.prototype.rebuild = function() {
		this.config.org.rebuild();
		this.config.site.rebuild();
		this.config.user.rebuild();
		this.config.onrebuild;
	}
	
	Breadcrumb.prototype.hover = function(orgName) {
		$('#'+orgName).addClass('breadcrumbHeaderActive');
	}
	
	Breadcrumb.prototype.out = function(orgName) {
		$('#'+orgName).removeClass('breadcrumbHeaderActive');
	}
	
	Breadcrumb.prototype.goHome = function() {
		this.changeOrg("Organizations");
		var chartDataHandler = new ChartDataHandler();
		chartDataHandler.setOnComplete(function(data){
			goHomeAjaxCallback(data);
		});
		chartDataHandler.getGlobalData();
		
		var chartDataHandler2 = new ChartDataHandler();
		chartDataHandler2.setOnComplete(function(accessData){
			buildHomeAccessCallback(accessData);
		});
		chartDataHandler2.getGlobalAccessData({});
	}
	
	Breadcrumb.prototype.changeOrg = function(orgName) {
		// change the header element
		this.config.org.name = orgName;
		// clear the list of sites
		this.config.site.emptyElements();
		// get new list of sites
		if (orgName != "Organizations") {
			this.config.org.getSites();
		}
		
		// clear the sub elements since we are selecting a new org
		// we won't have a specific site or user
		this.config.site.name = "";
		this.config.user.name = "";
		this.config.user.emptyElements();
		
		// rebuild all
		this.rebuild();
		
		if (orgName != "Organizations") {
			var chartDataHandler = new ChartDataHandler();
			chartDataHandler.setOnComplete(function(data){
				changeOrgAjaxCallback(data);
			});
			chartDataHandler.getOrgData({
				orgName: orgName
			});
			
			var chartDataHandler2 = new ChartDataHandler();
			chartDataHandler2.setOnComplete(function(accessData){
				buildChangeOrgAccessCallback(accessData);
			});
			chartDataHandler2.getOrgAccessData({
				orgName: orgName
			});
		}
	}
	
	Breadcrumb.prototype.changeSite = function(siteName) {
		this.config.site.name = siteName;
		this.config.user.emptyElements();
		if (siteName != "Sites") {
			this.config.site.getUsers();
		}
		
		// clear the sub-element
		this.config.user.name = "";
		this.config.user.emptyElements();
		
		this.rebuild();
		
		if (siteName != "Sites") {
			var chartDataHandler = new ChartDataHandler();
			chartDataHandler.setOnComplete(function(data){
				changeSiteAjaxCallback(data);
			});
			chartDataHandler.getSiteData({
				siteName: siteName
			});
			
			var chartDataHandler2 = new ChartDataHandler();
			chartDataHandler2.setOnComplete(function(data){
				changeSiteAccessCallback(data);
			});
			chartDataHandler2.getSiteAccessData({
				siteName: siteName
			});
		}
	}
	
	Breadcrumb.prototype.changeUser = function(userName) {
		this.config.user.name = userName;
		this.rebuild();
		
		if (userName != "Users") {
			var chartDataHandler = new ChartDataHandler();
			chartDataHandler.setOnComplete(function(data){
				changeUserAjaxCallback(data);
			});
			chartDataHandler.getUserData({
				userName: userName
			});
			
			var chartDataHandler2 = new ChartDataHandler();
			chartDataHandler2.setOnComplete(function(data){
				changeUserAccessCallback(data);
			});
			chartDataHandler2.getUserAccessData({
				userName: userName
			});
			
			// if we're on the pie chart screen, change tabs to tab 3
			var tabsApi = $(".tabs").data("tabs");
			if (tabsApi.getIndex() == 1) {
				tabsApi.click(2);
			}
		}
	}
	
	Breadcrumb.prototype.addElement = function(type, id, name) {
		if (type == "organization") {
			this.config.org.addElement(id, name);
		} 
		else if (type == "site") {
			this.config.site.addElement(id, name);
		}
		else if (type == "user") {
			this.config.user.addElement(id, name);
		}
	}
	
	Breadcrumb.prototype.removeChildren = function(removeFromType) {
		if (type == "organization") {
			this.config.org.clearChildren();
		}
		else if (type == "site") {
			this.config.site.clearChildren();
		}
		else if (type == "user") {
			this.config.user.clearChildren();
		}
	}
	
	Breadcrumb.prototype.buildComparisonEditor = function() {
		// build the basics
		$('#'+this.config.comparisonLinkId).after('<div id="comparisonEditor" class="overlay"><div class="contentWrap"><div class="comparisonSectionContainer" id="comparisonSectionOrg"><h2 class="comparisonSectionHeader" id="comparisonSectionHeaderOrg">Organizations</h2><div class="clearfix"></div></div><div class="comparisonSectionContainer" id="comparisonSectionSite"><h2 class="comparisonSectionHeader" id="comparisonSectionHeaderSite">Sites</h2><div class="clearfix"></div></div><div class="comparisonSectionContainer" id="comparisonSectionUser"><h2 class="comparisonSectionHeader" id="comparisonSectionHeaderUser">Users</h2><div class="clearfix"></div></div><a href="javascript:breadcrumbSelfReference.buildComparisonViewer()" rel="#comparisonChart" class="showComparison">Show Comparison</a></div></div>');
			
		// tell it to fire up when the link is clicked
		comparisonEditor = $('#'+this.config.comparisonLinkId).overlay({
			closeOnClick: false
		});
	}
	
	Breadcrumb.prototype.addToCompare = function(objType, objName) {
		var objectRef;
		if (objType == "org") {
			objectRef = this.config.org;
		}
		else if (objType == "site") {
			objectRef = this.config.site;
		}
		else if (objType == "user") {
			objectRef = this.config.user;
		}
		
		var element = objectRef.getElement(objectRef.findByName(objName));
		if (element != null) {
			// check that this element is not already in the array
			if (this.findCompareElement(element.name) == null) {
				// add the new element to the compare list
				this.compareList[this.compareList.length] = element;
				$('#addToCompare_'+objName).css("display","none");
				
				// add the "physical" element to the compare list
				// we are giving the listings in the compare list the id "compare_org_NDAR" for example
				if (objType == "org") {
					$('#comparisonSectionOrg .clearfix').before('<div id="compare_org_'+ objName +'" class="comparisonListing"></div>');
					$('#compare_org_'+objName).append('<div class="comparisonListingName">'+objName+'</div><a href="javascript:breadcrumbSelfReference.removeFromCompare(\'org\',\''+objName+'\')">remove</a>');
				}
				else if (objType == "site") {
					$('#comparisonSectionSite').append('<div id="compare_site_'+ objName +'" class="comparisonListing"></div>');
					$('#compare_site_'+objName).append('<div class="comparisonListingName">'+objName+'</div><a href="javascript:breadcrumbSelfReference.removeFromCompare(\'site\',\''+objName+'\')">remove</a>');
				}
				else if (objType == "user") {
					$('#comparisonSectionUser').append('<div id="compare_user_'+ objName +'" class="comparisonListing"></div>');
					$('#compare_user_'+objName).append('<div class="comparisonListingName">'+objName+'</div><a href="javascript:breadcrumbSelfReference.removeFromCompare(\'user\',\''+objName+'\')">remove</a>');
				}
			}
			else {
				// if the element is already in the array, don't add it but alert the user
				alert("That object is already in the compare list");
			}
		}
		else {
			// because we abbreviate "org"
			if (objType == "org") {
				alert("The organization could not be found.  Please report this error to a website administrator");
			}
			else {
				alert("The " + objType + " could not be found.  Please report this error to a website administrator");
			}
		}
	}
	
	Breadcrumb.prototype.removeFromCompare = function(objType, objName) {
		var objectRef;
		if (objType == "org") {
			objectRef = this.config.org;
		}
		else if (objType == "site") {
			objectRef = this.config.site;
		}
		else if (objType == "user") {
			objectRef = this.config.user;
		}
		
		var element = objectRef.getElement(objectRef.findByName(objName));
		if (element != null) {
			if (this.findCompareElement(element.name) != null) {
				var index = this.compareList.indexOf(element);
				if (index != -1) {
					// found it, do it
					this.compareList.splice(index,1);
					$('#compare_'+objType+"_"+objName).remove();
					breadcrumbSelfReference.rebuild();
				}
			}
		}
	}
	
	Breadcrumb.prototype.findCompareElement = function(objName) {
		var sizeOfCompare = this.compareList.length;
		for(var i = 0; i < sizeOfCompare; i++) {
			if (this.compareList[i].name == objName) {
				return this.compareList[i];
			}
		}
		return null;
	}
	
	Breadcrumb.prototype.serializeCompareList = function() {
		var sizeOfCompare = this.compareList.length;
		var data = "";
		for(var i = 0; i < sizeOfCompare; i++) {
			if (data != "") {
				data += ",";
			}
			data += this.compareList[i].type + ":" + this.compareList[i].id;
		}
		return data;
	}
	
	Breadcrumb.prototype.showComparisonCharts = function() {
		// we need to get all of the data, create a div, fill that div
		// with a new highchart, and make it pop up.  not too bad
		this.compareData = new Array();
		for (var i=0; i<this.compareList.length; i++) {
			this.compareData[i] = null;
		}
		
		for (var i=0; i < this.compareList.length; i++) {
			var chartDataHandler = new ChartDataHandler();
			chartDataHandler.setOnComplete(function(data){
				breadcrumbSelfReference.comparisonCallback(data)
			});
			
			if (this.compareList[i].type == "org") {
				chartDataHandler.getOrgData({
					orgName: this.compareList[i].name
				});
			}
			else if (this.compareList[i].type == "site") {
				chartDataHandler.getSiteData({
					siteName: this.compareList[i].name
				});
			}
			else if (this.compareList[i].type == "user") {
				chartDataHandler.getUserData({
					userName: this.compareList[i].name
				});
			}
		}
		
	}
	
	Breadcrumb.prototype.comparisonCallback = function(data) {
		// find the first null entry in the array
		var chartDataHandler = ChartDataHandler.getInstance();
		for (var i = 0; i < breadcrumbSelfReference.compareData.length; i++) {
			if (breadcrumbSelfReference.compareData[i] == null) {
				var seriesName = "";
				if (data.length > 0) {
					if (data[0].refObjectName == "org") {
						seriesName = data[0].creatorOrg.name;
					}
					else if (data[0].refObjectName == "site") {
						seriesName = data[0].creatorSite.userName;
					}
					else if (data[0].refObjectName == "user") {
						seriesName = data[0].creatorUser.userName;
					}
					
					breadcrumbSelfReference.compareData[i] = {
							type : "spline",
							name : seriesName,
							data : chartDataHandler.formatSubjectsForFrequencyChart(data)
						};
				}
				else {
					// ignore this entry - don't even list it since it's empty
					breadcrumbSelfReference.compareData.pop();
				}
				break;
			}
		}
		
		var continueTest = true;
		for (var i = 0; i < this.compareData.length; i++) {
			if (this.compareData[i] == null) {
				continueTest = false;
			}
		}
		if (continueTest) {
			// we're finished with the batch, call the finalization
			breadcrumbSelfReference.comparisonBatchComplete();
		}
	}
	
	Breadcrumb.prototype.buildComparisonViewer = function() {
		if (!document.getElementById('comparisonChart')) {
			$('body').append('<div id="comparisonChart"></div>');
			$('.showComparison').overlay({
				closeOnclick: false,
				load: true,
				mask: {
					color: '#cccccc',
					opacity: 0.9
				},
				onLoad : function() {
					breadcrumbSelfReference.showComparisonCharts();
				}
			});
		}
	}
	
	Breadcrumb.prototype.comparisonBatchComplete = function() {
		//alert("completed " + this.compareData.length + " entries");

		// create the new popup div, draw the chart to it, and make it pop up
		if (!document.getElementById('comparisonChart')) {
			this.buildComparisonViewer();
		}
		else {
			$('#comparisonChart').html("");
		}
		
		// close the compare editor and make the chart pop up
		$('#openComparisonEditorLink').overlay().close();
		$('.showComparison').load();
		var chartBuilder = new ChartBuilder();
		chartBuilder.buildCompareTimeSeries("comparisonChart", this.compareData, null);
	}

/**
 * The base breadcrumb object.  Each instance covers one header/dropdown
 * pair and its interaction.
 * 
 * @param config the configuration object
 */
function BreadCrumbBase() {

}

	BreadCrumbBase.prototype.initVars = function(config) {
		this.mainContainerId = config.containerId;
		this.headerClass = "breadCrumbHeader";
		this.dropdownContainerClass = "breadcrumbDropdownContainer";
		this.linkBaseUrl = config.linkBaseUrl;
		this.addToCompareClass = "addToCompareLink";
		this.compareLinkClass = "compareLink";
	}
	/**
	 * Builds the dropdown for a given top level object.  Extended by the
	 * top level objects below.
	 * 
	 */
	BreadCrumbBase.prototype.setupDropdown = function() {
		// add the dropdown container
		var dropdown = $('<div></div>')
		.attr({id : 'dropdown'})
		.addClass(this.dropdownContainerClass);
		
		for(var i = 0; i<this.elements.length; i++) {
			// exclude the listing in the header
			if (this.elements[i].name != this.name) {
				$('<a></a>')
				.attr({href : "javascript:breadcrumbSelfReference."+this.updateFunctionName+"('" + this.elements[i].name + "')"})
				.text(this.elements[i].name)
				.attr({id : "breadcrumb_"+this.elements[i].name})
				.appendTo(dropdown);
				
				if (breadcrumbSelfReference.compareList.indexOf(this) == -1) {
					$('<a></a>')
					.attr({id : "addToCompare_"+this.elements[i].name})
					.attr({href : "javascript:breadcrumbSelfReference.addToCompare"+"('"+ this.objectName + "','" + this.elements[i].name + "')"})
					.text("compare")
					.addClass(this.addToCompareClass)
					.appendTo(dropdown);
				}
			}
		}
		
		$('<div></div>').appendTo(dropdown);
		
		// set up the qtip
		$('#'+this.headerId).qtip({
			position: {
				my: 'bottom left',
				at: 'bottom right'
		    },
	    	hide: {
	    		fixed: true // Make it fixed so it can be hovered over
	    	},
			relative: true,
			content: dropdown
		});
	}
	

	/**
	 * builds the header and menu dropdown
	 * 
	 */
	BreadCrumbBase.prototype.build = function() {
		var containerNode = $('<div></div>')
		.addClass('breadcrumbHeaderContainer');
		var headerNode = $('<a></a>')
		.attr({id : this.headerId})
		.addClass(this.headerClass);
		
		var name = this.name;
		if (this.objectName == "org") {
			if (this.name == "") {
				name = "Organizations";
			}
			headerNode.attr({href : "javascript:breadcrumbSelfReference.changeOrg"+"('"+ name + "')"});
			headerNode.attr({onMouseOver : "javascript: breadcrumbSelfReference.hover('orgHeader');"});
			headerNode.attr({onMouseOut : "javascript: breadcrumbSelfReference.out('orgHeader');"});
		}
		else if (this.objectName == "site") {
			if (this.name == "") {
				name = "Sites";
			}
			headerNode.attr({href : "javascript:breadcrumbSelfReference.changeSite"+"('"+ name + "')"});
		}
		else if (this.objectName == "user") {
			if (this.name == "") {
				name = "Users";
			}
			headerNode.attr({href : "javascript:breadcrumbSelfReference.changeUser"+"('"+ name + "')"});
		}
		
		if (this.name == "" && this.elements.length == 0) {
			headerNode.hide()
			.html(this.defaultText);
		}
		else if (this.name == "" && this.elements.length > 0) {
			headerNode.show().html(this.defaultText);
		}
		else {
			headerNode.show().html(this.name);
		}
		if ($("#"+this.headerId).length > 0) {
			$("#"+this.headerId).replaceWith(headerNode);
			$("#"+this.containerId).remove();
		}
		else {
			containerNode.appendTo($("#"+this.mainContainerId));
			headerNode.appendTo(containerNode);
//			headerNode.appendTo($("#"+this.mainContainerId));
		}
		
		this.initializeDropdown();
	}
	
	BreadCrumbBase.prototype.clearChildren = function() {
		$('#'+this.containerId).empty();
	}
	
	BreadCrumbBase.prototype.emptyElements = function() {
		this.elements = [];
	}
	
	BreadCrumbBase.prototype.getElement = function(elementIndex) {
		if (this.elements.length > elementIndex && elementIndex >= 0) {
			return this.elements[elementIndex];
		}
		else {
			return null;
		}
	}
	
	BreadCrumbBase.prototype.rebuild = function() {
		this.clearChildren();
		this.build();
	}
	
	BreadCrumbBase.prototype.removeElement = function(name) {
		var index = this.findByName(name);
		if (index != -1) {
			document.getElementById(this.containerId).removeChild(
					document.getElementById(this.containerId).childNodes[index]
			);
		}
	}
	
//	BreadCrumbBase.prototype.addElement = function(id, name, type) {
//		this.elements[this.elements.length] = new MenuItem(id, name, type);
//	}

// extend BreadCrumbBase
Org.prototype = new BreadCrumbBase();
function Org(config) {
	this.initVars(config);
	
	this.name = "";
	this.elements = [];
	this.defaultText = "Organizations";
	this.containerId = "orgDropdown";
	this.dropdownContainerClass = "breadcrumbDropdownContainer";
	this.headerId = "orgHeader";
	this.dropdownInitialized = false;
	this.updateFunctionName = "changeOrg";
	this.objectName = "org";
	
	// initialize needed variables
	this.name = config.orgName;
	this.defaultText = config.orgDefaultText;
	this.baseUrl = config.baseUrl;
	this.getSitesUrl = config.getSitesUrl;
	this.site = config.site;
	
	// initialize the elements array
	for (var i = 0; i < config.organizations.length; i++) {
		this.elements[i] = new MenuItem(config.organizations[i][0], config.organizations[i][1], "org"); 
	}
	
	// call super constructor
	BreadCrumbBase.apply(this, [config]);
}

	/**
	 * Initializes and builds the dropdowns for this object
	 */
	Org.prototype.initializeDropdown = function() {
		this.setupDropdown(this.elements);
	}
	
	Org.prototype.findByName = function(orgName) {
		// the parent version
		var sizeOfList = this.elements.length;
		for (var i = 0; i < sizeOfList; i++) {
			if (this.elements[i].name == orgName) {
				return i;
			}
		}
		return -1;
	}
	
	Org.prototype.addElement = function(id, name) {
		this.elements[this.elements.length] = new MenuItem(id, name, "org");
	}
	
	Org.prototype.getSites = function() {
		// uses ajax to retrieve the sites for this org.  This only happens
		// in cases where we are drilling down to a particular org
		$.post(this.baseUrl + this.getSitesUrl,
			{orgName : this.name},
			function(data) {
				if (data != "") {
					// static function (basically) so we have to use global references
					breadcrumbSelfReference.config.site.clearChildren();
					var sites = data.split(",");
					for(var i = 0; i < sites.length; i++) {
						var siteData = sites[i].split(":");
						breadcrumbSelfReference.config.site.addElement(siteData[0], siteData[1]);
					}
					breadcrumbSelfReference.config.site.rebuild();
				}
			}
		) 
	}
	
Site.prototype = new BreadCrumbBase();
function Site(config) {
	this.initVars(config);
	
	this.name = "";
	this.elements = [];
	this.defaultText = "Sites";
	this.containerId = "siteDropdown";
	this.headerId = "siteHeader";
	this.dropdownInitialized = false;
	this.updateFunctionName = "changeSite";
	this.objectName = "site";
	
	// initialize needed variables
	this.name = config.siteName;
	this.defaultText = config.siteDefaultText;
	this.baseUrl = config.baseUrl;
	this.getUsersUrl = config.getUsersUrl;
	this.user = config.user;
	
	// initialize the elements array
	for (var i = 0; i < config.sites.length; i++) {
		this.elements[i] = new MenuItem(config.sites[i][0], config.sites[i][1], "site"); 
	}
	
	// call super constructor
	BreadCrumbBase.apply(this, [config]);
}

	Site.prototype.initializeDropdown = function(config) {
		this.setupDropdown(config, this.elements);
	}
	
	Site.prototype.findByName = function(siteName) {
		// the parent version
		var sizeOfList = this.elements.length;
		for (var i = 0; i < sizeOfList; i++) {
			if (this.elements[i].name == siteName) {
				return i;
			}
		}
		return -1;
	}
	
	Site.prototype.addElement = function(id, name) {
		this.elements[this.elements.length] = new MenuItem(id, name, "site");
	}
	
	Site.prototype.getUsers = function() {
		// uses ajax to retrieve the users for this site.  This only happens
		// in cases where we are drilling down to a particular site
		$.post(this.baseUrl + this.getUsersUrl,
			{siteName : this.name},
			function(data) {
				if (data != "") {
					breadcrumbSelfReference.config.user.clearChildren();
					var users = data.split(",");
					for(var i = 0; i < users.length; i++) {
						var userData = users[i].split(":");
						breadcrumbSelfReference.config.user.addElement(userData[0], userData[1]);
					}
					breadcrumbSelfReference.config.user.rebuild();
				}
			}
		) 
	}
	
User.prototype = new BreadCrumbBase();
function User(config) {
	this.initVars(config);
	
	this.name = "";
	this.elements = [];
	this.defaultText = "Users";
	this.containerId = "userDropdown";
	this.headerId = "userHeader";
	this.dropdownInitialized = false;
	this.updateFunctionName = "changeUser";
	this.objectName = "user";
	
	// initialize needed variables
	this.name = config.userName;
	this.defaultText = config.userDefaultText;
	
	// initialize the elements array
	for (var i = 0; i < config.users.length; i++) {
		this.elements[i] = new MenuItem(config.users[i][0], config.users[i][1], "user"); 
	}
	
	// call super constructor
	BreadCrumbBase.apply(this, [config]);
}

	User.prototype.initializeDropdown = function() {
		this.setupDropdown(this.elements);
	}
	
	User.prototype.findByName = function(userName) {
		// the parent version
		var sizeOfList = this.elements.length;
		for (var i = 0; i < sizeOfList; i++) {
			if (this.elements[i].name == userName) {
				return i;
			}
		}
		return -1;
	}
	
	User.prototype.addElement = function(id, name) {
		this.elements[this.elements.length] = new MenuItem(id, name, "user");
	}

/**
 * Menu Item class.  Used in the array of menu items
 * 
 * @param id the ID of the menu item (used in urls)
 * @param the name of the menu item.  Displayed as link texr
 */
function MenuItem(id, name, type) {
	this.id = id;
	this.name = name;
	this.type = type;
}