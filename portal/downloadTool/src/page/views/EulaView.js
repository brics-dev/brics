
import PageView from './PageView';

var eula = require('../templates/eula.hbs');

let EulaView;
export default EulaView = PageView.extend({
	className: "dt-page-eula",
	modalConfigs: {

	},

	events: {

	},

	initialize: function(params) {
		EulaView.__super__.initialize.call(this, params);
		this.model.set("orgEmail", this.config.orgEmail);
		this.pageName = "eula";
	},

	render: function() {
		this.renderTemplate(eula, this, function() {
			
		});
	}
});
