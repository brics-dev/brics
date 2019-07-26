<%@ page import="gov.nih.nichd.ctdb.common.CtdbConstants,
				gov.nih.nichd.ctdb.protocol.domain.Protocol,
				gov.nih.nichd.ctdb.security.domain.User,
				gov.nih.nichd.ctdb.common.rs,java.util.Locale" %>
<%@ taglib uri="/struts-tags" prefix="s" %>

<script type="text/javascript" src="/proforms/workspace/dashboardChartNg.js"></script>
<link rel="stylesheet" type="text/css" href="<s:property value="#webRoot"/>/common/css/dataCollection.css">
<div ng-app="dashboardApp" ng-controller="DashboardCtrl" id="dashboardChart">
	<div ng-if="chartFilters.currentStudyId != 'all'">
		<div id="dashboardSubStatusDiv" class="ng-scope">
		<table cellspacing="0" cellpadding="0" width="100%">
			<tbody>
				<tr>
				<td width="50%">
					<div ng-controller="DashboardSubjectCtrl" >
					<div ng-if="studyInfo.length!=0">
					  	<table cellspacing="0" cellpadding="0">
					    	<tr>
					    		<th><b><s:text name="Subjects Currently enrolled"/></b></th>
					    		<td>&nbsp;&nbsp;&nbsp;{{studyInfo.enrollSubjects}}/{{studyInfo.totSubjects}} </td>
					    	</tr>
					    	<tr>
						   		<th><b><s:text name="Study Duration"/></b> </th>
						   		<td>&nbsp;&nbsp;&nbsp;{{studyInfo.studyDuration}}</td>
						   	</tr>
						</table>
						<br/>
						<table cellspacing="0" cellpadding="0" >
						   	<tr>
						    	<td ng-repeat="(key, value) in studyInfo.studyMap">
						    	<div ng-if="value.length!=0">
		  					    	<div class="dashboartooltip badge blue" id="popUpMSDiv12">{{key}}
										<span class="tooltiptext"><ul><b>Major Milestones for {{key}}</b></ul><br><ul ng-repeat="v in value"> - {{v}}</ul></span>
		  					    	</div>
							    </div>
							    <div ng-if="value.length==0">
							    	<div class="badge">{{key}}</div>
							    </div>
							   	</td>
							</tr>
						</table>
					</div>
					</div>
				</td>
				<td width="50%" align="right">
					<div ng-controller="DashboardStatusCtrl">
				    <div ng-if="statusData.length!=0">
				    <table cellspacing="0" cellpadding="0">
					    <tr>
					    	<td colspan="2"><b><th align="left">Overall Status</th></b> </td>
					    </tr>
						<colgroup span="5"></colgroup>
					   	<tbody>
					     	<tr class="status">
					       		<div><th>Assessment Type</th></div>
					      		<div><th>Complete<div class="greenCircle statusBox"></div></th> </div>
								<div><th>&nbsp;&nbsp;In complete<div class="redCircle floatRight statusBox"></div> </th></div>
								<div><th>&nbsp;&nbsp;In progress<div class="blue floatRight statusBox"></div> </th></div>
								<div><th>&nbsp;&nbsp;Deviations<div class="yellowCircle floatRight statusBox"></div></th></div>
					     	</tr>
					     	<tr ng-repeat="status in statusData">
							    <td align="left">{{ status.assessmentType }}</td>
							    <td align="center">{{ status.compVal }}</td>
							    <td align="center">{{ status.inCompVal }}</td>
							    <td align="center">{{ status.inProgVal }}</td>
							    <td align="center">{{ status.devVal }}</td>
							 </tr>
					 	</tbody>
					 </table>
					 </div>
				</div>
				</td>
					
				</tr>
				</tbody>
				</table>
			
		</div>
		<br/>
		<br/>
	
		<div ng-controller="DashboardSiteCtrl" id="dbSiteFilter">
			<span class="filter-name-span"><s:text name="Site" />:</span>
		    <ui-select ng-model="sites.selectedOption" theme="select2" on-select="changedSelectedSite(sites.selectedOption)">
		    	<ui-select-match>{{$select.selected.name}}</ui-select-match>
		    	<ui-select-choices repeat="site in sites.siteOptions | filter:  $select.search" position="down">
		    		<div ng-bind-html="site.name | highlight: $select.search"></div>
		    	</ui-select-choices>
		    </ui-select>
		    <span class="input-group-btn">
	       		<button ng-click="changedSelectedSite(sites.siteOptions[0])" class="btn btn-default">
	         		<span>Clear</span>
	       		</button>
	     	</span>
	 	</div>
	 	<br/>
	 	<div ng-controller="DashboardGuidCtrl" id="dbGuidFilter" >
			<span class="filter-name-span"><s:text name="GUID" />:</span>
		    <ui-select ng-model="guids.selectedOption" theme="select2" on-select="changedSelectedGuid(guids.selectedOption)">
		    	<ui-select-match>
		    		{{$select.selected.guid}}
		    	</ui-select-match>
		    	<ui-select-choices repeat="patient in guids.guidOptions | filter:  $select.search" position="down">
		    		<div ng-bind-html="patient.guid | highlight: $select.search"></div>
		    	</ui-select-choices>
		    </ui-select>
	   		<span class="input-group-btn">
	       		<button ng-click="changedSelectedGuid(guids.guidOptions[0])" class="btn btn-default">
	         		<span>Clear</span>
	       		</button>
	     	</span>
	 	</div>
	 	<br/>
	 	<div ng-controller="DashboardCollStatusCtrl" id="dbCollStatusFilter">
			<span class="filter-name-span"><s:text name="Data Collection Status" />:</span>
		    <ui-select ng-model="collStatuses.selectedOption" theme="select2" on-select="changedSelectedCollStatus(collStatuses.selectedOption)"
		        ng-disabled="greyOutCollStatus">
		    	<ui-select-match>
		    		{{$select.selected.name}}
		    	</ui-select-match>
		    	<ui-select-choices repeat="status in collStatuses.statusOptions | filter:  $select.search" position="down">
		    		<div ng-bind-html="status.name | highlight: $select.search"></div>
		    	</ui-select-choices>
		    </ui-select>
	   		<span class="input-group-btn">
	       		<button ng-click="changedSelectedCollStatus(collStatuses.statusOptions[0])" class="btn btn-default">
	         		<span>Clear</span>
	       		</button>
	     	</span>
	 	</div>
 	
 		<br/>
 	
	 	<div ng-controller="DashboardChartCtrl">
		    <div id="chartContainer" class="row">
		         <highchart id="visitTypeSubjectChart" config="dashBoardChartConfig" class="span9" ></highchart>
			</div>
			<div ng-show="showBtn">
				<input type="button" value="View Data" onclick="loadViewData()" />
		 	</div>
		</div>
	</div>
</div>
<div id="popupContainer"></div>
