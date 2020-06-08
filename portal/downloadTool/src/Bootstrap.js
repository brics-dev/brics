import Application from './application/models/Application';
import ApplicationView from './application/views/ApplicationView';
import './styles/style.scss';

var DownloadToolClient = {
	application: null,
	applicationView: null,

	initialize: function() {
		TemplateManager.initPartials();
		this.application = new Application();
		this.applicationView = new ApplicationView({
			model: this.application
		});

		return this;
	},

	render: function(config) {
		if (this.application == null) {
			this.initialize();
		}

		this.applicationView.render(config);
	}
};

module.exports = DownloadToolClient;