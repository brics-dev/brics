/**
 * 
 */

import EulaView from "../../page/views/EulaView";
import DownloadToolView from "../../page/views/DownloadToolView";
import Page from "../../page/models/Page";
import Identity from "../../util/Identity";

var application = require('../templates/application.hbs');

let ApplicationView;
export default ApplicationView = BaseView.extend({
	tagName: "ApplicationView",
	modal: null,
	events: {
		'click #dt-eula-buttonAccept': 'goToMainPage',
		'click #dt-eula-buttonDecline' : 'closeTool'
	},

	initialize: function() {
		ApplicationView.__super__.initialize.call(this);
	},
	render: function(config) {
		this.config = config;
		Identity.initConfig(config);
		this.setElement(config.container);
		this.$el.html(application());
		this.goToEula();
	},
	
	goToPage: function(pageName, view) {	
		var modalView = null;
		if (!!this.model.get("activePage")) {
			this.model.get("activePage").destroy();
		}			
		modalView = new view({
			model: new Page(),
			config: this.config,
			container: ".pageContainer"
		});
		modalView.render(this);
		this.model.set("activePage", modalView);
		return modalView;
	},
	
	goToEula: function(e) {
		if (e) {
			e.preventDefault();
		}
		this.goToPage("eula", EulaView);
	},

	goToMainPage: function(e) {
		if (e) {
			e.preventDefault();
		}
		this.goToPage("downloadTool", DownloadToolView);
	},

	closeTool : function(e) {
		if (e) {
			e.preventDefault();
		}
		this.$el.remove();
	}
});
