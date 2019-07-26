/**
 * 
 */

function ChartBuilder() {
	this.chartDefaults = null;
	this.chart = null;
}

ChartBuilder.prototype.buildTimeSeries = function(targetId, data, config, seriesName) {
	
	this.chartDefaults = {
		chart: {
			renderTo: targetId,
			height: 398,
			width: 720,
			zoomType: 'x'
		},
		title: {
			text: "GUID Activity Since Creation"
		},
		subtitle: {
			text: document.ontouchstart ===undefined ?
					'Click and drag in the plot area to zoom in' :
						'Drag to zoom in'
		},
		credits : {
			enabled: false
		},
		xAxis: {
			type: 'datetime',
			dateTimeLabelFormats: {
				day: '%e %b %Y'
			},
			labels : {
				rotation: 45,
				align: "left"
			},
			maxZoom: 7*24*3600000 // 7 days
		},
		yAxis: {
			title: {
				text: "GUIDs Registered"
			},
			min: 0
		},
		series: [{
			type: "spline",
			name: seriesName,
			data: data,
			animation: false
		}]
	};
	
	if (config != null) {
		var configuration = $.extend({}, this.chartDefaults, config);
	}
	else {
		var configuration = this.chartDefaults;
	}
	
	this.chart = new Highcharts.Chart(configuration);
	return this.chart;
}

ChartBuilder.prototype.buildCompareTimeSeries = function(targetId, data, config) {
	this.chartDefaults = {
		chart: {
			renderTo: targetId,
			height: 398,
			width: 720,
			zoomType: 'x'
		},
		title: {
			text: "Comparison Report Time Series (Empty Elements not Shown)"
		},
		subtitle: {
			text: document.ontouchstart ===undefined ?
					'Click and drag in the plot area to zoom in' :
						'Drag to zoom in'
		},
		credits : {
			enabled: false
		},
		xAxis: {
			type: 'datetime',
			dateTimeLabelFormats: {
				day: '%e %b %Y'
			},
			labels : {
				rotation: 45,
				align: "left"
			},
			maxZoom: 7*24*3600000 // 7 days
		},
		yAxis: {
			title: {
				text: "GUIDs Registered"
			},
			min: 0
		},
		series: data
	};
	
	if (config != null) {
		var configuration = $.extend({}, this.chartDefaults, config);
	}
	else {
		var configuration = this.chartDefaults;
	}
	
	this.chart = new Highcharts.Chart(configuration);
	return this.chart;
}

ChartBuilder.prototype.buildGuidList = function(targetId, data, config) {
	$('#'+targetId).empty();
	$('#'+targetId).append('<h2 id="statisticsGuidListTitle">All GUIDs Registered for This Object (' + data.length + ' Total)</h2>');
	$('#'+targetId).append('<table id="statisticsGuidList"><thead><th align="left">GUID</th><th align="left">Date Registered</th></thead></table>');
	for (var i=0; i<data.length; i++) {
		var date = new Date(parseInt(data[i].date));
		var months = ["January","February","March","April","May","June","July","August","September","October","November","December"];
		$('#'+targetId+" table").append(
			'<tr><td>'
			+'<a href="guidAction!view.action?guid=' + data[i].guid + '">'
			+data[i].guid
			+'</a></td><td>'
			+months[date.getMonth()]
			+" "
			+date.getDate()
			+", "
			+date.getFullYear()
			+'</td></tr>'
		);
	}
	
	// if the function argument is given to overlay,
	// it is assumed to be the onBeforeLoad event listener
	$("a.guidLink[rel]").each(function(i) {
		$(this).overlay({
			oneInstance: false,
			closeOnClick: false,
			effect: 'apple',
			onBeforeLoad: function() {
				// grab wrapper element inside content
				var wrap = this.getOverlay().find(".contentWrap");
				// load the page specified in the trigger
				wrap.load(this.getTrigger().attr("href"));
			}
		});
	});
}

ChartBuilder.prototype.buildPie = function(targetId, data, config, onclick) {
	if (typeof seriesName == undefined) {
		seriesName = "GUIDs Registered";
	}
	this.chartDefaults = {
		chart: {
			renderTo: targetId,
			plotShadow: false,
			height: 398,
			width: 720
		},
		title: {
			text: "GUIDs Registered Percentages"
		},
		credits : {
			enabled: false
		},
		tooltip: {
			formatter: function() {
				return '<b>' + this.point.name + '</b><br/>' + this.y + ' GUIDs';
			}
		},
		legend : {
			align: "right",
			verticalAlign: "middle",
			layout: "vertical"
		},
		plotOptions: {
			pie: {
				allowPointSelect: true,
				dataLabels: {
					enabled: false
				},
				showInLegend: true
			}
		},
		series: [{
			type: 'pie',
			name: 'GUIDs Registered',
			point: {
				events: {
					click: onclick
				}
			},
			data: data
		}]
	};
	
	if (config != null) {
		var configuration = $.extend({}, this.chartDefaults, config);
	}
	else {
		var configuration = this.chartDefaults;
	}
	this.chart = new Highcharts.Chart(configuration);
	return this.chart;
}