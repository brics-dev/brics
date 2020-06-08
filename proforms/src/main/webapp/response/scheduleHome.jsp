<jsp:include page="/common/doctype.jsp" />
<jsp:include page="/common/header_struts2.jsp" />
<%@ page import="org.json.JSONArray,org.json.JSONObject"%>
<%@ taglib uri="/struts-tags" prefix="s"%>
<%@ taglib uri="/WEB-INF/security.tld" prefix="security" %>

<html>
<head>
<style>

  input[type="button"]{
	margin: 10px 150px 0 0; width: 150px; vertical-align: baseline;
	float: right;
  }

  .lblFont{
	font-weight: bold;
  }
  .scheduleFilter {
  	margin: 0 0 10px 0;
  	width: 100%;
  }
  .filterLable {
  	margin-left: 107px; 
  	width: 100px;
  	display: inline-block;
  }

  .ui-autocomplete {
    max-height: 100px;
    overflow-y: auto;
    /* prevent horizontal scrollbar */
    overflow-x: hidden;
  }

  * html .ui-autocomplete {
    height: 200px;
    width : 200px
  }
  
  .ui-menu .ui-menu-item {
  text-align:left
  }
  .custom-combobox {
    position: relative;
    display: inline-block;
    margin-left:50px;
  }
  .custom-combobox-toggle {
    position: absolute;
    top: 0;
    bottom: 0;
    margin-left: -1px;
    padding: 0;
  }
  .custom-combobox-input {
    margin: 0;
    padding: 5px 10px;
    background-color:transparent;
    width: 300px;
  }
</style>
</head>

<%-- <s:hidden id="protocolData" value="scheduleHome.protocolsList"/> --%>

<h1><s:text name="report.schedule.generateSchedule"/></h1>
<s:form theme="simple" method="post" enctype="multipart/form-data" id="ScheduleReport" style="width: 80%;">
<s:hidden name="currProtoId" id="currProtoId" />
<s:hidden name="selectedProtocolId" id="selectedProtocolId" />
<s:hidden name="selectedClinicalLocId" id="selectedClinicalLocId" />
<s:hidden name="selectedSubjectId" id="selectedSubjectId" />
<s:hidden name="scheduleStartDateStr" id="scheduleStartDateStr" />
<s:hidden name="scheduleEndDateStr" id="scheduleEndDateStr" />
	<div id="searchDateContainer" style="margin-top: 60px; margin-left:30px">
		<b><s:text name="report.schedule.duration"/></b>
		<label for="schedulestartdate" style="margin-left: 50px;" class="lblFont"><s:text name="report.schedule.startdate"/></label>
		<s:textfield name="scheduleStartDateStr" id="scheduleStartDate" maxlength="10" cssClass="dateField"/>
		<label for="scheduleenddate" style="margin-left:30px;" class="lblFont"><s:text name="report.schedule.enddate"/></label>
		<s:textfield name="scheduleEndDateStr" id="scheduleEndDate" maxlength="10" cssClass="dateField"/>
	</div>

	<div id="schedule_search_container" style="margin-left: 30px; margin-top: 10px;">
		<div class="scheduleFilter">
			<b><s:text name="report.schedule.shceduleFilters" /></b>
			<%-- <input type="button" style="margin-left: 30px;" value="<s:text name='report.schedule.compiled' />" title="Compiled" 
								alt="Compiled" onclick="generateReportByCompiled()" /> --%>
		</div>
		
		<div class="scheduleFilter">
			<%-- <input type="button" value="<s:text name='report.schedule.protocol' />" 
				title="Protocol" alt="Protocol" onclick="generateReportByProtocol()" /> --%>
			<label for="protocolSearch" class="lblFont filterLable"><s:text name='report.schedule.protocol'/></label>
			<s:select name="protocolSearch" id="protocolSearch" list="protocolList" listKey="id" listValue="name" 
				cssStyle="vertical-align: baseline; margin-left:50px"/>	
		</div>
		
		
		<div class="scheduleFilter">
			<%-- <input type="button" value="<s:text name='report.schedule.clinicalLocation' />" 
				title="Location" alt="Protocol" onclick="generateReportByLocation()" /> --%>
			<label for="clinicalLocSearch" class="lblFont filterLable"><s:text name='report.schedule.clinicalLocation'/></label>
			<s:select name="clinicalLocList" id="clinicalLocSearch" list="clinicalLocList" listKey="id" listValue="name"
				cssStyle="vertical-align: baseline; margin-left:50px"/>
		</div>	
		
		<div class="scheduleFilter">
			<%-- <input type="button" class="btnSearch" value="<s:text name='report.schedule.subject' />" 
				title="Subject" alt="Subject" onclick="generateReportBySubject()" /> --%>
			<label for="patientSearch" class="lblFont filterLable"><s:text name='report.schedule.subject'/></label>
			<s:select name="patientList" id="patientSearch" list="patientList" listKey="id" listValue="lastNameFirstName"
				cssStyle="vertical-align: baseline; margin-left:50px"/>
		</div>
		<input type="button" id="btnGeneReport" value="<s:text name='report.schedule.generateReport' />" 
				title="Generate Report" alt="Generate Report" onclick="generateScheduleReport()" />
	</div>
</s:form>
<br/>
<div class="idtTableContainer brics" id="scheduleReportTableDiv" style="width: 100%; margin-top: 50px;">
	<table id="scheduleReportTable" class="table table-striped table-bordered" width="100%">
	</table>   
</div>
</div>
</html>
<script type="text/javascript">
var basePath = "<s:property value="#webRoot"/>";
</script>
<script type="text/javascript" src="/proforms/response/autocomplete-combobox.js"></script>
<script type="text/javascript" src="/proforms/response/scheduleHomeJs.js"></script>

<%-- Include Footer --%>
<jsp:include page="/common/footer_struts2.jsp" />