/**
 * 
 */
 var viewDataApp = angular.module('viewDataApp', ['highcharts-ng', 'ngSanitize', 'ui.select']);
 //var chartArray = [];
 
 viewDataApp.service('ViewDataService', function($q, $http){
	 
	 this.getViewDataJson = function() {
		 
		 return $http({
				method: 'GET',
			    url: basePath+"/populateViewDataAction!populateViewData.action",
				cache: false
			}).then(function (response) {
			    return response.data;
			}, function(err){
				console.log("error in getEforms ajax");
			    return [];
			});
	 }
	 
	 this.getGuidForViewData = function(){
		 
		 return $http({
				method: 'GET',
			    url: basePath+"/populateViewDataAction!getGuidForViewData.action",
			    cache: false
			}).then(function (response) {
			    return response.data;
			}, function(err){
				console.log("error in getEforms ajax");
			    return [];
			});
	 }
	 
	 this.getGuidListForSites = function(selectedSiteIds){
		 
		 return $http({
				method: 'GET',
			    url: basePath+"/populateViewDataAction!getGuidsForSelectedSites.action",
			    params: {"selectedSiteIds": JSON.stringify(selectedSiteIds)},
			    cache: false
			}).then(function (response) {
			    return response.data;
			}, function(err){
				console.log("error in getEforms ajax");
			    return [];
			});
	 }
	 
	 this.getVTListByGuid = function(guidId){
		 
		 return $http({
				method: 'GET',
			    url: basePath+"/populateViewDataAction!getVisitTypeListByGuid.action",
			    params: {"guidId": JSON.stringify(guidId)} ,
			    cache: false
			}).then(function (response) {
			    return response.data;
			}, function(err){
			    console.log("error in getEforms ajax");
			    return [];
			});
	 }
	 
	 this.geteForms = function(intervalIds){
		 
		 return $http({
				method: 'GET',
			    url: basePath+"/populateViewDataAction!geteFormsForInterval.action",
			    params: {"intervalIds": JSON.stringify(intervalIds)} ,
			    cache: false
			}).then(function (response) {
			    return response.data;
			}, function(err){
			   console.log("error in getEforms ajax");
			    return err.data;
			});
	 }
	 
	 this.drawGraph = function(siteIds, guidId, intervalIds, eforms) {
		 
		 return $http({
				method: 'POST',
				url: basePath+"/populateViewDataAction!viewDataGraph.action",
				params: {"siteIds": JSON.stringify(siteIds), 
						 "guidId": JSON.stringify(guidId), 
					     "intervalIds": JSON.stringify(intervalIds), 
					     "eforms": JSON.stringify(eforms)
				},
				cache: false
			}).then(function (response) {
			    return response.data;
			},// function(err){
			function(err){
				return err.data;
			});
	 }
	 
 });
 
 viewDataApp.controller('viewDataCtrl',  function($scope, $filter, $compile, ViewDataService) {
		  $scope.showEforms=false;
	 	  $scope.showVisitTypes=false;
	 	  $scope.showPdfBtn=false;
		  $scope.viewData = [];
		  $scope.sites = [];
		  $scope.chartArray = [];
		  $scope.guids = {
				  guidOptions : [{id:'', guid: ''}],
				  length : 0
			  };
		  $scope.intervals = [];
		  $scope.selectedSiteIds = ""; 
		  $scope.selectedIntervalIds = "";
		  $scope.selectedGuidId = "";
		  var eformArray = [];
		  
		  //when loading viewData.jsp, displaying following by default
		  ViewDataService.getViewDataJson().then(function(data) { 
			 $scope.viewData = JSON.parse(data);
			 $scope.sites=$scope.viewData.siteOptions;
			 $scope.intervals = $scope.viewData.intervalOptions;

			 if($scope.sites.length == 0){
				 $("#noSite").append("<font color=red>no site associated</font>");
			 }else{
				 $("#noSite").text("");
			 }
			 //guid
			 getGuidList();
	 	  });
		  
		  //when selecting any sites or guid
	 
		 $scope.selectedSites = function (){
		 	//reset guid
			$scope.guids.selectedOption = $scope.guids.guidOptions[0];
			var selectedSiteArr = $filter('filter')($scope.sites, {checked: true});
			$scope.selectedSiteIds = selectedSiteArr.map(function (siteObj){
				 return siteObj["id"];
			 }); 
			
			if($scope.selectedSiteIds != "" && $scope.selectedSiteIds.length > 0){
				getGuidListForSites(); //get Guids for selected sites
			} else{
				$scope.resetguids = {
					guidOptions : [{id:'', guid: ''}],
					length : 0
				};
				$scope.guids = angular.copy($scope.resetguids);
				getGuidList();
			}
			
 //show visit type checkbox list with existing list
			 eformArray = [];
			 $scope.intervals = [];
			 $.merge($scope.intervals, $scope.viewData.intervalOptions);
			 if($scope.selectedSiteIds != "" && $scope.intervals.length > 0){
				 $scope.showVisitTypes = true;
			 } else {
				 $scope.showVisitTypes = false;
				 $scope.selectedIntervalIds = "";
				 $scope.showEforms=false;
				 $scope.showEformButton = false;
				 $scope.showPdfBtn=false;
				 $("#chartContainer").empty();
				 $("#errorMsg").text("");
				 angular.forEach($scope.intervals, function(intrv){
					 intrv.checked=false;
				 })
				 
			 }		 
		  }

		 function getGuidList(){
			 ViewDataService.getGuidForViewData().then(function(data) {
				 var guidArr = JSON.parse(data);
				 $scope.guids.length = guidArr.length +1;
				 $.merge($scope.guids.guidOptions, guidArr);
				 if($scope.guids.length > 1) {
					$scope.guids.guidOptions[0].guid = 'Select or search a GUID in the list...';
				 } else {
					$scope.guids.guidOptions[0].guid = '------ No GUIDs Available ------';
				 }
				 $scope.guids.selectedOption = $scope.guids.guidOptions[0];
			 });
		 }
		 
		 function getGuidListForSites(){
			 ViewDataService.getGuidListForSites($scope.selectedSiteIds).then(function(data) {
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
		 }
		 
		 
		 $scope.selectedGuid = function(selectedItem) {
				$scope.guids.selectedOption = selectedItem;	
				$scope.selectedGuidId = selectedItem.id;
				
				//clear values
				$scope.selectedIntervalIds = ""
				$scope.showEforms=false;
				$scope.showEformButton = false;
				$scope.showPdfBtn=false;
				$("#chartContainer").empty();
				$("#errorMsg").text("");
				eformArray=[];
				angular.forEach($scope.intervals, function(intrv){ //uncheck visit types loaded on selection of GUID
					 intrv.checked=false;
				})
				angular.forEach($scope.viewData.intervalOptions, function(intrv){//uncheck visit types loaded for sites if it was previosly selected 
					 intrv.checked=false;
				})
					
				if($scope.selectedGuidId != ""){
					ViewDataService.getVTListByGuid($scope.selectedGuidId).then(function(data) {
						var guidVTArr = JSON.parse(data);
	//					console.log("guidVTArr: "+guidVTArr);
						$scope.intervals = [];
						$.merge($scope.intervals, guidVTArr);
	//					console.log("guidVT $scope.intervals "+JSON.stringify($scope.intervals));
						 if($scope.intervals.length > 0){
							$scope.showVisitTypes = true;
						} 
					})
				}else {
					$scope.showVisitTypes = false;
					
					$scope.selectedSites();
				}
		  };
		 
		  $scope.showEformBtn = function(){
			  $scope.showEforms=false;
			  $scope.showPdfBtn=false;
			  $("#chartContainer").empty();//clear charts
			  $("#errorMsg").text("");//clear error messages
			  eformArray=[];
			  var selectedIntervalArr = $filter('filter')($scope.intervals, {checked: true});
			  $scope.selectedIntervalIds = selectedIntervalArr.map(function (intervalObj){
				  return intervalObj["id"];
			  });
			  if($scope.selectedIntervalIds != "" && $scope.selectedIntervalIds.length > 0){
					$scope.showEformButton = true;
				} else {
					$scope.showEformButton = false;
				}
		  }
		  //when selecting any visit types
		  
		  $scope.selectedIntervals = function (){
			  $("#chartContainer").empty();//clear charts
			  $("#errorMsg").text("");//clear error messages
			  eformArray=[];
			  var selectedIntervalArr = $filter('filter')($scope.intervals, {checked: true});
			  $scope.selectedIntervalIds = selectedIntervalArr.map(function (intervalObj){
				  return intervalObj["id"];
			  });
			  ViewDataService.geteForms($scope.selectedIntervalIds).then(function(data) {
				//  console.log('eformsJson: '+data);
				  $scope.eforms = JSON.parse(data);
				  var checkForErrors = $scope.eforms;
				  if (checkForErrors.hasOwnProperty("backEndErrors")){
	  					var div = $("#backEndErrors");
	  					div.html("<font color=red><b>"+ checkForErrors.backEndErrors+"</b></font><br/>");
	  					setTimeout(	$("#backEndErrors").focus(),500)
	  					return;
	  			  }
				  
				  if($scope.eforms && $scope.eforms != "" && $scope.eforms.length > 0){
				  	$scope.showEforms=true;
				  }else{
					  $("#errorMsg").append("<font color=red>no eForms with score associated</font>");
				  }
			  });
		  }
		  
		  		  
		  function constructEformArray(e, de){
			  var eformObject = {};
			  var deObject = {};
			  var deObjectArr = [];
			  
			  eformObject.name = e.name;
			  eformObject.shortName = e.shortName;
			  deObject.qid = de.qid;
			  deObject.dename = de.dename;
			  deObjectArr.push(deObject);
			  eformObject.deQidArr = deObjectArr;
			  eformArray.push(eformObject);
		  }
		  
		  $scope.selectedDE = function(e, de){
			  
			  var eformFound = false;
			  if(eformArray == undefined || eformArray.length == 0){
				  constructEformArray(e, de);
			  }else{
				  if(de.checked == true){
					  for(var i=0; i<eformArray.length; i++){
						  var eformObj = eformArray[i];
						  if(eformObj.shortName == e.shortName){//append the selected dataelements in the eform object
							  var deObject = {};
							  var deArr = eformObj.deQidArr;
							  deObject.qid = de.qid;
							  deObject.dename = de.dename
							  deArr.push(deObject);
							  eformFound = true;
							  break;
						  }
					  }
					  if(!eformFound){
						  constructEformArray(e, de);
					  }
				  }else if(de.checked == false){
					  for(var i=0; i<eformArray.length; i++){//if dataelement is deselected, then remove it
						  var eformObj = eformArray[i];
						  if(eformObj.shortName == e.shortName){
							  var deArr = eformObj.deQidArr;
							  for(var j=0; j<deArr.length; j++){
								  if(deArr[j].qid == de.qid){
									  deArr.splice(j,1);
									  break;
								  }
							  }
						  }
					  }
				  }
			  }
			  $scope.selectedEforms = eformArray;
			  if(eformArray && eformArray.length > 0){
				  $scope.drawGraph();
			  }else{
				  $("#chartContainer").empty();
				  $scope.showPdfBtn=false;
			  }
		  }
	
		  $scope.drawGraph = function(){
			
		  	ViewDataService.drawGraph($scope.selectedSiteIds, $scope.selectedGuidId, $scope.selectedIntervalIds, 
		  			$scope.selectedEforms).then(function(data) {
		  				$("#chartContainer").empty();
		  				
		  				$("#errorMsg").text("");
		  				var eformArr = JSON.parse(data);
		  				if (eformArr.hasOwnProperty("backEndErrors")){
		  					var div = $("#backEndErrors");
		  					div.html("<font color=red><b>"+ eformArr.backEndErrors+"</b></font><br/>");
		  					setTimeout(	$("#backEndErrors").focus(),500)
		  					return;
		  				}
		  				if(!eformArr || eformArr.length == 0){
		  					$scope.viewDataChartConfig.navigation.buttonOptions.enabled = false;
		  				}else{
		  					$scope.viewDataChartConfig.navigation.buttonOptions.enabled = true;
		  				}
		  				chartArray = [];
		  				var chartCount=0;
		  				var colors=["#7cb5ec", "#90ed7d", "#f15c80", "#8085e9", "#f7a35c", "#2b908f", "#FA8072", "#C39BD3", "#91e8e1"];
		  				var i=0;
		  				
		  				for(var key in eformArr){
		  					i=0;
		  					if(eformArr.hasOwnProperty(key)){
			  					if (eformArr[key].scores == undefined || eformArr[key].scores==null ){
			  						var div = $("#errorMsg");
			  						div.append("<font color=red>"+ eformArr[key].ename + " has no score for the dataelement " + eformArr[key].dename+"</font><br/>");
			  						continue;
			  					}			  					
			  					var scoresArr = eformArr[key].scores;			  					
			  					var yaxisArr=[];
			  					 for(var skey in scoresArr){
			  						var yaxisData={};
			  						if(scoresArr.hasOwnProperty(skey)){
			  							yaxisData.y = scoresArr[skey];
			  							yaxisData.color = colors[i];
			  							i++;
			  							yaxisArr.push(yaxisData);
			  						}
			  					}
			  					$scope.viewDataChartConfig.title.text = eformArr[key].ename;
			  					$scope.viewDataChartConfig.subtitle.text = eformArr[key].dename;
			  					$scope.viewDataChartConfig.series = [];
			  					$scope.viewDataChartConfig.series.push({data:yaxisArr});
			  					$scope.viewDataChartConfig.xAxis.categories = eformArr[key].inames;
 			  					var divContainer = angular.element(document.querySelector('#chartContainer')); 			  					
				  				chartCount++;
				  				var divId = 'chartdiv'+chartCount;
				  				var divForChart = '<div id='+divId+'></div>';
				  				var appendHtml = $compile(divForChart)($scope);
				  				divContainer.append(appendHtml);
				  				var chart = Highcharts.chart(divId, $scope.viewDataChartConfig);
				  				chartArray.push(chart);
				  				if(chartArray != undefined && chartArray.length >0){
				  					setTimeout(function () {//if we don't use set timeout, value is not refreshing in jsp
				  				        $scope.$apply(function(){
				  				        	$scope.showPdfBtn = true;
				  				        });
				  				    }, 1000);
								}
				  			}
		  			}
		  	});
		  	
		  }	
	
		$scope.viewDataChartConfig = {
				
				  chart: {
					  		type: 'column'
				    	},
					
						 title:  {
					    	text: ''
					    },
					    
					    subtitle: {
					    	text: ''
					    },
					    
					    tooltip: {
					    	pointFormat: '<b>Score:{point.y}</b>'
					    },
						 
						xAxis: {
					        type: 'Visit Type',
					        allowDecimals: false,
					        title: {
					            text: "Visit Type"
					        },
					        
					        categories: []        
					    },
					    
					    series: [], 
					    
						yAxis: {
					        title: {
					            text: "Score"
					        }
					    },
						
						credits: {
							enabled: false	
						},
						
						navigation: {
							buttonOptions: {
								
							}
						},
						legend: {
							enabled: false
						}
		  } 
		 
});

 /**
  * Create a global getSVG method that takes an array of charts as an
  * argument
  */
 Highcharts.getSVG = function (charts) {
   	 var svg = "";
     Highcharts.each(charts, function (chart) {
         svg = svg + chart.getSVG();
     });

     return svg;
 };
 
 /**
  * Create a global exportCharts method that takes an array of charts as an
  * argument, and exporting options as the second argument
  */
 Highcharts.exportCharts = function (charts, options) {

     // Merge the options
     options = Highcharts.merge(Highcharts.getOptions().exporting, options);

     // Post to export server
     Highcharts.post(options.url, {
         filename: options.filename || 'chart',
         type: options.type,
         width: options.width,
         svg: Highcharts.getSVG(charts)
     });
 };
 
 // Set global default options for all charts
 Highcharts.setOptions({
     exporting: {
         fallbackToExportServer: false // Ensure the export happens on the client side or not at all
     }
 });

 
 $('#export-pdf').click(function () {

     Highcharts.exportCharts(chartArray, {
         type: 'application/pdf',
         url: exportServerUrl
     });
 });
