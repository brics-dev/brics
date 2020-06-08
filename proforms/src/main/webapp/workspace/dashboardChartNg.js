/**
 * 
 */
	
	var dashboardApp = angular.module('dashboardApp', ['highcharts-ng', 'ngSanitize', 'ui.select']);	
//	var selectedSiteId = "";
	dashboardApp.factory('chartFiltersFactory', function() {
		var chartFilters = {
				currentStudyId : "",
				selectedSiteId : "",
				selectedGuidId : "",
				selectedStatusId : ""
			}
		var chartFiltersFactory = {};
		chartFiltersFactory.getChartFilters = function(){
			return chartFilters;
		}
		chartFiltersFactory.setChartFilters = function(currentStudyId, selectedSiteId, selectedGuidId, selectedStatusId) {
			chartFilters.currentStudyId = currentStudyId;
			chartFilters.selectedSiteId = selectedSiteId;
			chartFilters.selectedGuidId = selectedGuidId;
			chartFilters.selectedStatusId = selectedStatusId;
		}
		
		return chartFiltersFactory;
	});
	dashboardApp.service('ReportDashboardService', function($q, $http){
		
		this.getSiteList = function(chartFilters) {

			return $http({
				method: 'GET',
			    url: basePath+"/getDashboardFilterAction!getDashBoardSitesList.action",
			    params: {"studyId": chartFilters.currentStudyId} ,
			    cache: false
			}).then(function (response) {
//			    console.log("getSiteList(): "+JSON.stringify(response.data));
			    return response.data;
			}, function(err){
			    return "[]";
			});
		};
		
		this.getGuidList = function(chartFilters) {

			return $http({
				method: 'GET',
			    url: basePath+"/getDashboardFilterAction!getDashBoardGuidsList.action",
			    params: {"studyId": chartFilters.currentStudyId} ,
			    cache: false
			}).then(function (response) {
//			    console.log("getGuidList(): "+JSON.stringify(response.data));
			    return response.data;
			}, function(err){
			    return "[]";
			});
		};
		
		this.getGuidListForSite = function(selectedSiteId) {
			
			return $http({
				method: 'GET',
			    url: basePath+"/getDashboardFilterAction!getDashBoardGuidsListForSite.action",
			    params: {"selectedSiteId": selectedSiteId} ,
			    cache: false
			}).then(function (response) {
//			    console.log("getGuidList(): "+JSON.stringify(response.data));
			    return response.data;
			}, function(err){
			    return "[]";
			});
		};
		
		this.getCollStatusList = function() {

			return $http({
				method: 'GET',
			    url: basePath+"/getDashboardFilterAction!getDashBoardCollStatusList.action",
			    cache: false
			}).then(function (response) {
//			    console.log("getCollStatusList(): "+JSON.stringify(response.data));
			    return response.data;
			}, function(err){
			    return "[]";
			});
		};
		
		this.getDataForVTSubChart = function(chartFilters) {

			return $http({
				method: 'GET',
			    url: basePath+"/workspace/reportingDashboardJsonAction!reportingDashboard.action",
			    params: {"params": JSON.stringify(chartFilters)
			    		},
			    cache : false
			}).then(function (response) {
//			    console.log("getDataForVTSubChart(): "+JSON.stringify(response.data));
			    return response.data;
			}, function(err){
			    return "[]";
			});
		};
		
		this.getDrillDownDataForStatusEformChart = function(chartFilters, visitTypeName, seriesName) {
//			console.log("getDrillDownDataForStatusEformChart chartFilters: "+JSON.stringify(chartFilters));
			//reset the selectedStatusId to empty since it should always shows all statuses for drilldown chart
			chartFilters.selectedStatusId = "";
			return $http({
				method: 'GET',
			    url: basePath+"/workspace/reportingDashboardJsonAction!getDrillDownForStatusEformJson.action",
			    params: {"params": JSON.stringify(chartFilters),
			    		 "visitTypeName": visitTypeName,
			    		 "seriesName": seriesName
			    		},
			    cache : false
			}).then(function (response) {
//			    console.log("getDrillDownDataForStatusEformChart(): "+JSON.stringify(response.data));
			    return response.data;
			}, function(err){
			    return "[]";
			});
		};
		
		this.getStatusList = function() {

			return $http({
				method: 'GET',
			    url: basePath+"/getDashboardOverallStatusAction!getDashBoardOverAllStatusList.action",
			    cache: false
			}).then(function (response) {
			    return response.data;
			}, function(err){
			    return "[]";
			});
		};
		
		this.getStudyInfo = function() {

			return $http({
				method: 'GET',
			    url: basePath+"/getDashboardOverallStatusAction!getDashBoardStudyInformation.action",
			    cache: false
			}).then(function (response) {
			    return response.data;
			}, function(err){
			    return "[]";
			});
		}

	}); //end ReportDashboardService
	
	dashboardApp.controller('DashboardCtrl',  function($scope, $location, chartFiltersFactory) {
		$scope.chartFilters = chartFiltersFactory.getChartFilters();
		
		var currentStudyId = $location.absUrl().split('?')[1].split('=')[1];
		var selectedSiteId = $scope.chartFilters.selectedSiteId;
		var selectedGuidId = $scope.chartFilters.selectedGuidId;
		var selectedStatusId = $scope.chartFilters.selectedStatusId;
		chartFiltersFactory.setChartFilters(currentStudyId, selectedSiteId, selectedGuidId, selectedStatusId);

		$scope.$on('listenToSeletedSite', function(event, data){
			selectedSiteId = data.selectedSiteId;
			
			if(selectedSiteId != ""){
				$scope.$broadcast("listenToDisableCollStatus");
				if ($scope.chartFilters.selectedStatusId != "") {
					selectedStatusId = "";				
				}
				$scope.$broadcast("listenToLoadGuidsForSite", selectedSiteId);
			} else {
				$scope.$broadcast("listenToResetCollStatusToDefault");
				if ($scope.chartFilters.selectedStatusId != "") {
					selectedStatusId = "";				
				}
				$scope.$broadcast('listenToLoadGuidsForStudy');
			} 
			chartFiltersFactory.setChartFilters(currentStudyId, selectedSiteId, selectedGuidId, selectedStatusId);
    		$scope.$broadcast("listenToChartFilters", chartFiltersFactory.getChartFilters());
		 });
		
		$scope.$on('listenToSeletedGuid', function(event, data){
			selectedGuidId =  data.selectedGuidId;
			
			if($scope.chartFilters.selectedSiteId != ""){
				$scope.$broadcast("listenToDisableCollStatus");
				if ($scope.chartFilters.selectedStatusId != "") {
					selectedStatusId = "";				
				}
			} 
			chartFiltersFactory.setChartFilters(currentStudyId, selectedSiteId, selectedGuidId, selectedStatusId);
			$scope.$broadcast("listenToChartFilters", chartFiltersFactory.getChartFilters());
		});
		
		$scope.$on('listenToSeletedCollStatus', function(event, data){
			selectedStatusId =  data.selectedCollStatusId;			
			chartFiltersFactory.setChartFilters(currentStudyId, selectedSiteId, selectedGuidId, selectedStatusId);
			$scope.$broadcast("listenToChartFilters", chartFiltersFactory.getChartFilters());
		});
		
	}); //end DashboardCtrl
	
	dashboardApp.controller('DashboardSiteCtrl',  function($scope, $rootScope, ReportDashboardService, chartFiltersFactory) {
		$scope.sites = {
			 siteOptions : [{id:'', name: ''}],
			 length : 0
		};
		$scope.chartFilters = chartFiltersFactory.getChartFilters();
//		console.log("DashboardSiteCtrl $scope.chartFilters "+JSON.stringify($scope.chartFilters));
		$scope.getSites = function(chartFilters) {
			ReportDashboardService.getSiteList(chartFilters).then(function(data) { 
				var siteArr = JSON.parse(data);
				$scope.sites.length = siteArr.length;
				$.merge($scope.sites.siteOptions, siteArr);
	
				if($scope.sites.length > 1) {
					$scope.sites.siteOptions[0].name = 'Select or search a SITE in the list...';
				} else {				
					$scope.sites.siteOptions[0].name = '------ No Sites Available ------';
				}
			});
		};
		
		$scope.getSites($scope.chartFilters);
		$scope.sites.selectedOption = $scope.sites.siteOptions[0];
		
		$scope.changedSelectedSite = function(selectedItem) {
//			console.log("changedSelectedSite.selectedItem: "+JSON.stringify(selectedItem));

			$scope.$emit("listenToSeletedSite", {selectedSiteId: selectedItem.id});
			$scope.sites.selectedOption = selectedItem;
		};

		$scope.$on("listenToResetSiteToDefault", function(event){ 
			$scope.changedSelectedSite($scope.sites.siteOptions[0])			
		});

	}); // DashboardSiteCtrl

	dashboardApp.controller('DashboardGuidCtrl',  function($scope, $rootScope, ReportDashboardService, $timeout, chartFiltersFactory) {
		$scope.guids = {
			guidOptions : [{id:'', guid: ''}],
			length : 0
		};
		
		$scope.chartFilters = chartFiltersFactory.getChartFilters();
//		console.log("DashboardGuidCtrl $scope.chartFilters "+JSON.stringify($scope.chartFilters));
		$scope.getGuids = function(chartFilters) {
			ReportDashboardService.getGuidList(chartFilters).then(function(data) { 
				var guidArr = JSON.parse(data);
				$scope.guids.length = guidArr.length;
				$.merge($scope.guids.guidOptions, guidArr);
				if($scope.guids.length > 1) {
					$scope.guids.guidOptions[0].guid = 'Select or search a GUID in the list...';
				} else {
					$scope.guids.guidOptions[0].guid = '------ No GUIDs Available ------';
				}
				$scope.guids.selectedOption = $scope.guids.guidOptions[0];
			});
		};

		$scope.getGuids($scope.chartFilters);
		
		$scope.changedSelectedGuid = function(selectedItem) {
//			console.log("changedSelectedGuid.$scope.chartFilters: "+JSON.stringify($scope.chartFilters));
//			console.log("changedSelectedGuid.selectedItem: "+JSON.stringify(selectedItem));

			$scope.$emit("listenToSeletedGuid", {selectedGuidId: selectedItem.id});
			$scope.guids.selectedOption = selectedItem;	
		};
		
		$scope.loadGuidsForSite = function(selectedSiteId){
			ReportDashboardService.getGuidListForSite(selectedSiteId).then(function(data) {
				var guidArr = JSON.parse(data);
				$scope.resetguids = {
						guidOptions : [{id:'', guid: ''}],
						length : 0
					};
				$scope.guids = angular.copy($scope.resetguids);
				$scope.guids.length = guidArr.length + 1;
				$.merge($scope.guids.guidOptions, guidArr);
				if($scope.guids.length > 1) {
					$scope.guids.guidOptions[0].guid = 'Select or search a GUID in the list...';
				} else {
					$scope.guids.guidOptions[0].guid = '------ No GUIDs Available ------';
				}
				$scope.guids.selectedOption = $scope.guids.guidOptions[0];
			});
		};
		
		$scope.$on("listenToLoadGuidsForSite", function(event, selectedSiteId){
			$scope.loadGuidsForSite(selectedSiteId);
		});
		
		$scope.$on("listenToResetGuidToDefault", function(event){ 
			$scope.changedSelectedGuid($scope.guids.guidOptions[0]);		
		});
		
		$scope.$on("listenToLoadGuidsForStudy", function(event){
			$scope.resetguids = {
					guidOptions : [{id:'', guid: ''}],
					length : 0
				};
			$scope.guids = angular.copy($scope.resetguids);
			$scope.chartFilters = chartFiltersFactory.getChartFilters();
			$scope.getGuids($scope.chartFilters);
		});
		
	  }); //end DashboardGuidCtrl
	
	dashboardApp.controller('DashboardCollStatusCtrl',  function($scope, ReportDashboardService, $timeout, chartFiltersFactory) {
		$scope.collStatuses = {
			statusOptions : [{id:'', name: ''}],
			length : 0
		};
		$scope.greyOutCollStatus = false;
		$scope.getCollStatuses = function() {
			ReportDashboardService.getCollStatusList().then(function(data) { 
				var collStatusArr = JSON.parse(data);
//				console.log("DashboardCollStatusCtrl collStatusArr "+JSON.stringify(collStatusArr));
				$scope.collStatuses.length = collStatusArr.length;
				$.merge($scope.collStatuses.statusOptions, collStatusArr);
				if($scope.collStatuses.length > 1) {
					$scope.collStatuses.statusOptions[0].name = 'Select or search a Status in the list...';
				} 
			});
		};

		$scope.getCollStatuses();
		$scope.collStatuses.selectedOption = $scope.collStatuses.statusOptions[0];
		
		$scope.changedSelectedCollStatus = function(selectedItem) {
//			console.log("changedSelectedCollStatus.selectedItem: "+JSON.stringify(selectedItem));
			$scope.$emit("listenToSeletedCollStatus", {selectedCollStatusId: selectedItem.id});
			$scope.collStatuses.selectedOption = selectedItem;
		};

		$scope.$on("listenToResetCollStatusToDefault", function(event){			
			$scope.collStatuses.statusOptions[0].name = 'Select or search a Status in the list...';
			$scope.collStatuses.statusOptions[0].id = '';
//			$scope.collStatuses.selectedOption = $scope.collStatuses.statusOptions[0];
			$scope.changedSelectedCollStatus($scope.collStatuses.statusOptions[0]);

			$scope.greyOutCollStatus = false;
//			$("#dbCollStatusFilter").show();
		});
		$scope.disableCollStatus = function() {
//			$.merge($scope.collStatuses.statusOptions, new Array());
			$scope.collStatuses.statusOptions[0].name ='------ No Status Available ------';
			$scope.collStatuses.statusOptions[0].id ='';
			$scope.collStatuses.selectedOption = $scope.collStatuses.statusOptions[0];
			$scope.collStatuses.length = 0;
			$scope.greyOutCollStatus = true;
//			$("#dbCollStatusFilter").hide();
		};
		$scope.$on("listenToDisableCollStatus", function(event){ 
//			console.log("listenToDisableCollStatus");
			$scope.disableCollStatus();		
		});
	  }); //end DashboardCollStatusCtrl
	
	dashboardApp.controller('DashboardChartCtrl',  function($scope, $rootScope, ReportDashboardService, chartFiltersFactory) {
		
		$scope.chartSeries = [];
		$scope.chartCategories = [];
		$scope.chartFilters = chartFiltersFactory.getChartFilters();
		$scope.showBtn = true;
		
		ReportDashboardService.getDataForVTSubChart($scope.chartFilters).then(function(data) {
//			console.log("getDataForVTSubChart() data: "+data);
			var dataArr = JSON.parse(data);
			$.merge($scope.chartSeries, dataArr);

			if(dataArr.length > 0){
				var categoriesArr = dataArr[0].categories;
				$.merge($scope.chartCategories, categoriesArr);
			}
//			console.log("DashboardChartCtrl getDataForVTSubChart dataArr: "+JSON.stringify(dataArr));
			
			var allDataLength = 0;
			for(var i = 0; i < dataArr.length; i++){
				if(dataArr[i].id == "all"){
					allDataLength = dataArr[i].data.length;
				}
			}
			if($scope.chartFilters.currentStudyId == "all"){
				$scope.showBtn = false;
			}
			if(allDataLength == 0){
				var msg = "There are no data associated to this protocol. The Visit Type vs Subject Count Bar Chart will be blank.";
				var listeningStr = "";
				setTimeout(function () {
			        $scope.$apply(function(){
			        	$scope.showBtn = false;
			        });
			    }, 1000);
			
//				$scope.showNoDataMsg(msg, listeningStr);
//				$('#chartContainer').prepend("<h4 style='margin-left: 300px;'>NO DATA for Chart</h4>");
				$.ibisMessaging("close", {type:"primary"}); 
				$.ibisMessaging("primary", "warning", "NO DATA for Chart");
				$rootScope.$broadcast("listenToDisableCollStatus");
			}

		});
		
		$scope.drillDownSeries = [];
		$scope.getDrillDownData = function(chartFilters, visitTypeName, seriesName) {
			ReportDashboardService.getDrillDownDataForStatusEformChart(chartFilters, visitTypeName, seriesName).then(function(data) {
//				console.log("getDrillDownDataForStatusEformChart: "+JSON.stringify(data));
				$.merge($scope.drillDownSeries, JSON.parse(data));
			});
		}
		
		$scope.collStatusArr = new Array();
		$scope.getCollStatuses = function() {
			ReportDashboardService.getCollStatusList().then(function(data) { 
				var statusArr = JSON.parse(data);
				for(var i = 0; i < statusArr.length; i ++){
					$scope.collStatusArr.push(statusArr[i].name);
				}
			});
		};
		$scope.getCollStatuses();
		
		$scope.chartTitle = 'Study Status';
		$scope.yAxisText = 'Subject Visit (count)';
		$scope.dashBoardChartConfig = {
		  	chart: {
		  	 	type: 'column',
		        events: {
		        	drilldown: function(e) {
		        		if (!e.seriesOptions) {
		        			var visitTypeName = e.point.name;
		        			var seriesName = e.point.series.name;
		        			var chart = this;
		        			$scope.drillDownSeries = [];
		        			$scope.getDrillDownData($scope.chartFilters, visitTypeName, seriesName);
		        			chart.showLoading("<div style='display: inline; white-space: nowrap;'>Loading Drilldown Chart ...</div>");		        		
			        		
		        			var loadDrillDown = function () {
//		        				console.log("getDrillDownDataForStatusEformChart $scope.drillDownSeries[0]: "+JSON.stringify($scope.drillDownSeries[0]));
			        			if(typeof $scope.drillDownSeries[0] !== "undefined") {
				        			chart.hideLoading();
			        				var series = $scope.drillDownSeries[0];
//			        				console.log("getDrillDownDataForStatusEformChart series: "+JSON.stringify(series));
				        			chart.setTitle({text: 'Data Collection Status vs eForms Count Chart'});
					        		chart.yAxis[0].axisTitle.attr({text: 'eForms(count)'});
					        		chart.xAxis[0].categories = series.drilldownCategory;	
			                        chart.addSeriesAsDrilldown(e.point, series);
			        			} else {
			        				setTimeout(function () {
			        					loadDrillDown();
				                    }, 1000);
			        			}			        			
		        			};
		        			loadDrillDown();
		        		}
		        	},
		        	drillup: function(e) {
		        		var chart = this;
		        		chart.setTitle({text:$scope.chartTitle});
		        		chart.yAxis[0].axisTitle.attr({text: $scope.yAxisText});
		        		chart.xAxis[0].categories = $scope.chartCategories;
		        	}
		        }
		  	},
		  	series: $scope.chartSeries,
		  	drilldown: {
		  		series: [],
		  	}, 
		  	title: {
		    	text: $scope.chartTitle
		  	},
		  	xAxis: {
		  		categories: $scope.chartCategories,
		    },
		  	yAxis: {		           
		         title: {
		             text: $scope.yAxisText,
		             align: 'middle'
		         },
		         type: "logarithmic",
		         stackLabels: {
		             enabled: true,
		             style: {
		               fontWeight: 'bold'
		             }
		         }		         
		    },
		    plotOptions: {
		        column: {
		          stacking: 'normal',
		        }
		    },
		    credits : {
		         enabled: false
		    },
		    exporting: {
		    	fallbackToExportServer: false,
		    	libURL: basePath + "/common/js/hichart/exporting-7.1.0.js",
		        url: exportServerUrl
		    },
		  	tooltip : {
		  		 shared: true,
                 enabled: true,
                 headerFormat: '',
                 formatter: function() {
                	 var points = this.points;
                	 var pointsLength = points.length;
                	 var tooltipHtml = '', headerHtml = '', pointHtml = '', pointStatusHtml = '', pointGuidHtml = '', eformNamesHtml = '';
                	 var totalY = 0;

                	 for(var i = 0; i < pointsLength; i++){
                		 if(pointsLength == 1){
                     		 headerHtml = '<h4 style="font-size:12px">'+points[0].series.name+'</h4><br/>';
                        	 pointHtml = points[0].key + ': <b>'+points[0].y+'</b><br/>';
                    	 } else {
//                		 	 console.log("splitNames: "+JSON.stringify(points[i].series.name));
                     		 if(i < 4 && jQuery.inArray(points[i].series.name, $scope.collStatusArr) !== -1){ //site column with 4 statuses
                     			 pointStatusHtml += '<span style="color:' + points[i].color + '">\u25CF</span>  ' + points[i].series.name + ': <b>'+points[i].y+'</b><br/>';
                     			 if($scope.chartFilters.selectedSiteId != ""){ //only site column needs total
                     				totalY += points[i].y;
                     			 }                    			
                     		 } else { //guid column or other column without site                       			
                     			pointGuidHtml += '<span style="color:' + points[i].color + '">\u25CF</span>  '+ points[i].series.name + ': <b>'+points[i].y+'</b><br/>';
                     		 }                     		 
                     	 }
    		  			 //drill down tooltip showing eform name list
    		  			 if(typeof points[i].point.eforms !== "undefined"){
    		  				 var eformNames = points[i].point.eforms;
    		  				 eformNamesHtml += "<b>eForms:</b><br/>";
    		  				 for(var j = 0; j < eformNames.length; j ++){
    		  					eformNamesHtml += '<span style="color:'+points[i].color+'">\u25CF</span>  '+eformNames[j]+'<br/>';
    		  				 }
    		  			 }
                	 }


                	 if(totalY != 0){
                		 pointStatusHtml += 'Total: '+totalY+'<br/>';
                	 }  
                	 if($scope.chartFilters.selectedSiteId != "") { //site or siteGuid
                		 pointHtml += pointStatusHtml + pointGuidHtml;
                	 } else { // guid or guidStatus
                		 pointHtml += pointGuidHtml + pointStatusHtml;
                	 }
                	 
		  			 tooltipHtml = headerHtml + pointHtml + eformNamesHtml;
		  			 return tooltipHtml;
		  		 },
		         valueSuffix: ' subject visit data'
		    }
		}
		
		$scope.$on("listenToChartFilters", function(event,data){
			$scope.chartFilters = data;
			$scope.chartSeries = [];
			$scope.chartCategories = [];
//			$scope.drillDownSeries = [];
			
			$scope.getChartData = function(chartFilters) {
//				console.log("DashboardChartCtrl listenToChartFilters chartFilters: "+JSON.stringify(chartFilters));
				ReportDashboardService.getDataForVTSubChart(chartFilters).then(function(data) {

					var dataArr = JSON.parse(data);
					$.merge($scope.chartSeries, dataArr);
					
					if(dataArr.length > 0){
						var categoriesArr = dataArr[0].categories;
						$.merge($scope.chartCategories, categoriesArr);

					}
//					console.log("DashboardChartCtrl listenToChartFilters $scope.chartSeries: "+JSON.stringify($scope.chartSeries));
//					console.log("DashboardChartCtrl listenToChartFilters $scope.chartCategories: "+JSON.stringify($scope.chartCategories));
					
					var siteDataLength = 0, guidDataLength = 0, siteGuidDataLen = 0, allDataLength = 0, 
						statusDataLen = 0, guidStatusDataLen = 0;
					for(var i = 0; i < dataArr.length; i++){
//						console.log("dataArr[i].id: "+dataArr[i].id);
						var sitePatt = new RegExp("^site[0-9]{1}$");
						if(sitePatt.test(dataArr[i].id)){
							siteDataLength += dataArr[i].data.length;
						}
						if(dataArr[i].id == "guid"){
							guidDataLength = dataArr[i].data.length;
						}
						if(dataArr[i].id == "siteGuid"){ 
							siteGuidDataLen = dataArr[i].data.length;
						}
						if(dataArr[i].id == "all"){
							allDataLength = dataArr[i].data.length;
						}
						if(dataArr[i].id == "status"){
							statusDataLen = dataArr[i].data.length;
						}
						if(dataArr[i].id == "guidStatus"){
							guidStatusDataLen = dataArr[i].data.length;
						}

					}

					if(siteDataLength > 0 || guidDataLength > 0 || siteGuidDataLen > 0 || allDataLength > 0
							|| statusDataLen > 0 ||  guidStatusDataLen > 0 ){
						$scope.dashBoardChartConfig.series = $scope.chartSeries;
				    	$scope.dashBoardChartConfig.xAxis.categories = $scope.chartCategories;
					} 

					var msg = "";
					var listeningStr = "";
					if(chartFilters.selectedSiteId > 0 && chartFilters.selectedGuidId == "" 
							&& siteDataLength == 0){
						msg = "There are no data associated to the selected site. Please select other site!";
						listeningStr = "listenToResetSiteToDefault";
						$scope.showNoDataMsg(msg, listeningStr);
					}
					if(chartFilters.selectedSiteId == "" && chartFilters.selectedGuidId > 0 
							&& chartFilters.selectedStatusId == "" && guidDataLength == 0){
						msg = "There are no data associated to the selected guid. Please select other guid!";
						listeningStr = "listenToResetGuidToDefault";
						$scope.showNoDataMsg(msg, listeningStr);
					}
					if(chartFilters.selectedSiteId > 0 && chartFilters.selectedGuidId > 0 
							&& siteGuidDataLen == 0){
						msg = "There are no data associated to the selected site and guid. Please select other site or guid!";
						listeningStr = "listenToResetGuidToDefault";
						$scope.showNoDataMsg(msg, listeningStr);
					}
					if(chartFilters.selectedSiteId == "" && chartFilters.selectedGuidId == "" 
						&& chartFilters.selectedStatusId > 0 && statusDataLen == 0){
						msg = "There are no data associated to the selected data collection status. Please select other data collection status!";
						listeningStr = "listenToResetCollStatusToDefault";
						$scope.showNoDataMsg(msg, listeningStr);
					}
					if(chartFilters.selectedSiteId == "" && chartFilters.selectedGuidId > 0 
						&& chartFilters.selectedStatusId > 0 && guidStatusDataLen == 0){
						msg = "There are no data associated to the selected guid and data collection status. Please select other guid or data collection status!";
						listeningStr = "listenToResetCollStatusToDefault";
						$scope.showNoDataMsg(msg, listeningStr);
					}
				    						
				});
			}; //end $scope.getChartData

			$scope.getChartData($scope.chartFilters);
		});
		
		$scope.showNoDataMsg = function(msg, listeningStr){
			var dlgId = $.ibisMessaging("dialog", "info", 
				msg	, {
				modal : true,
				buttons : [
					{
						text : "Close",
						click : function() {
							$.ibisMessaging("close", {id: dlgId});
							if(listeningStr.length > 0){
								$rootScope.$broadcast(listeningStr);
							}
						}
					}
				],
				width: "400px",
					title: "No Data"
			}); //end dialog
		};
	}); //end DashboardChartCtrl
	
	
	dashboardApp.controller('DashboardStatusCtrl',  function($scope, ReportDashboardService, $timeout) {

		ReportDashboardService.getStatusList().then(function(data) { 
			$scope.statusData = JSON.parse(data);
			});
		
	  }); //end DashboardStatusCtrl
	
	dashboardApp.controller('DashboardSubjectCtrl',  function($scope, ReportDashboardService, $timeout) {

		ReportDashboardService.getStudyInfo().then(function(data) { 
			$scope.studyInfo = JSON.parse(data);
			
			var mapToSort = $scope.studyInfo.studyMap;
            $scope.studyInfo.sortedStudyMap = orderByKey(mapToSort);
            function orderByKey(mapToSort){
                let ordered = {};
                Object.keys(mapToSort).sort().forEach(function(key) {
                  ordered[key] = mapToSort[key];
                });

                return ordered;
            }
		},
		function(err){
		    return "[]";
		});
	  }); //end DashboardSubjectCtrl
