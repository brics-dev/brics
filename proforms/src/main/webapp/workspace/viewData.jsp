<jsp:include page="/common/doctype.jsp" />

<%@ page import="org.json.JSONArray,org.json.JSONObject"%>
<%@ taglib uri="/struts-tags" prefix="s" %>
<%@ taglib uri="/WEB-INF/security.tld" prefix="security" %>
<%-- CHECK PRIVILEGES --%>
<security:check privileges="viewprotocols, addeditprotocols, validuser"/>
	<s:set var="pageTitle" scope="request">
		<s:text name="View Data" />
	</s:set>
	<jsp:include page="/common/header_struts2.jsp" />
	<jsp:include page="/workspace/dashboardJsCssLib.jsp" />
<html data-ng-app="viewDataApp">


<body>
<!-- using the id topNavContainer for this div tag to get the respective color for different instances -->
<div id="topNavContainer" style="text-align: center; padding: 4px 5px; color: white; font-weight: bold; font-size: 12px; font-family: Arial, Helvetica, sans-serif">View Data</div>

<div data-ng-controller="viewDataCtrl">
	<div id="backEndErrors" style="margin-top: 5px; border:1px"; tabindex="0"></div>
	
	<div id="viewDataContainer" style="margin-top: 15px">
	<b>Site:</b><div id="noSite" style="margin-left: 40px"></div>
		<div data-ng-repeat = "s in sites track by s.id">
			<input type= "checkbox" id="'{{s.id}}'" name="'{{s.name}}'" 
				data-ng-model="s.checked" data-ng-change="selectedSites()" 
				style="vertical-align: bottom; margin-left: 40px" />
				{{s.name}}
		</div>
		
	<div id="guidFilter" style="margin-top: 15px;">
	<b><s:text name="GUID" />:</b>
		    <ui-select ng-model="guids.selectedOption" theme="select2" on-select="selectedGuid(guids.selectedOption)" 
		    	ng-disabled="selectedSiteIds.length > 0 && selectedGuidId.length > 0 ">
		    	<ui-select-match>
		    		{{$select.selected.guid}}
		    	</ui-select-match>
		    	<ui-select-choices repeat="patient in guids.guidOptions | filter:  $select.search" position="down">
		    		<div ng-bind-html="patient.guid | highlight: $select.search"></div>
		    	</ui-select-choices>
		    </ui-select>
	   		<span class="input-group-btn">
	       		<button ng-click="selectedGuid(guids.guidOptions[0])" class="btn btn-default">
	         		<span>Clear</span>
	       		</button>
	     	</span>
	</div>
	</br>
	<p>Please select one or more sites or guid in order to show the chart(s) below.</p>
	     		
	<div id="visitTypeChkBoxes" style="margin-top: 5px; " data-ng-show="showVisitTypes">
	<b>Visit Types:</b> 
		<div data-ng-repeat = "i in intervals track by i.id">
			<input type= "checkbox" id="'{{i.id}}'" name="interval" 
				data-ng-model="i.checked" data-ng-change="showEformBtn()"
				style="vertical-align: bottom; margin-left: 40px" />
				{{i.name}} 
		</div>
	</div>
	<button data-ng-click = "selectedIntervals()" data-ng-show="showEformButton" style="margin-top: 15px;">Get eForms</button>
	<div id="errorMsg"></div>
	<div class="eformsAndCharts">	
		<div class="eformsContainer" data-ng-show="showEforms" style="margin-top: 5px; ">
		<b> eForm Data Elements:</b> <br><br>
			<div data-ng-repeat = "e in eforms">
				<b>{{e.name}}</b>
				<div data-ng-repeat = "de in e.deWithCalcRule">
					<input type= "checkbox" id="'{{de.qid}}'" name="'{{e.shortName}}'" 
						data-ng-model="de.checked" data-ng-change="selectedDE(e,de)" 
						style="vertical-align: bottom; margin-left: 40px" />
						{{de.dename}}
				</div>
			</div>
			
		</div>
		<div  class="chartContainer" id="chartContainer"></div>
	</div>

	<div id="buttonrow" data-ng-show="showPdfBtn">
		<button id="export-pdf">Export to PDF</button>
	</div>
</div>
</div>
 	

		
<script type="text/javascript" src="/proforms/workspace/viewDataNg.js"></script>
<script type="text/javascript">
var basePath = "<s:property value='#webRoot'/>";
var exportServerUrl = '<s:property  value="#systemPreferences.get('highcharts.exportserver.url')"/>';
</script>

</body>
</html>
<%-- Include Footer --%>
<jsp:include page="/common/footer_struts2.jsp" />