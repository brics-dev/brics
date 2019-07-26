<jsp:include page="/common/doctype.jsp" />
<%@ page import="gov.nih.nichd.ctdb.common.rs,java.util.Locale" %>
<%@ taglib uri="/struts-tags" prefix="s" %>
<%@ taglib uri="/WEB-INF/datatables.tld" prefix="idt" %>
<%@ page import="gov.nih.nichd.ctdb.protocol.domain.Protocol"%>
<%@ page import="gov.nih.nichd.ctdb.common.CtdbConstants"%>
<%@ page buffer="100kb" %>
<% 
	Locale l = request.getLocale();
Protocol curProtocol = (Protocol) session.getAttribute(CtdbConstants.CURRENT_PROTOCOL_SESSION_KEY);
int SUBJECT_DISPLAY_TYPE = curProtocol.getPatientDisplayType();
request.setAttribute("SUBJECT_DISPLAY_TYPE", SUBJECT_DISPLAY_TYPE);
String SUBJECT_DISPLAY_LABEL = curProtocol.getPatientDisplayLabel();
request.setAttribute("SUBJECT_DISPLAY_LABEL", SUBJECT_DISPLAY_LABEL);
%>

<html>

<s:set var="pageTitle" scope="request">
	<s:text name="report.frl" />
</s:set>

<jsp:include page="/common/header_struts2.jsp" />

<style>
 #frlReportTable_wrapper {
 	overflow: visible;
 }
</style>

<script type="text/javascript">
//reformat the Date to YYYYMMDD
function getValue(value) {
   return (value < 10) ? "0" + value : value;
};
function getDate () {
   var newDate = new Date();

   var sMonth = getValue(newDate.getMonth() + 1);
   var sDay = getValue(newDate.getDate());
   var sYear = newDate.getFullYear();

   return sYear + sMonth + sDay;
}
var now = getDate();
$(document).ready(function() {
	$("#frlReportTable").idtTable({
		idtUrl: "<s:property value='#webRoot'/>/response/getFormsRequiringLockListList.action",
		dom: 'Bfrtip',
        columns: [
        	<%if (SUBJECT_DISPLAY_TYPE  == CtdbConstants.PATIENT_DISPLAY_ID) {%>
        	{
                name: 'frlId',
                title: '<%= rs.getValue("patient.label.SubjectID", l) %>',
                parameter: 'frl_nrn',
                data: 'frlId'
            },
            <%}%>
            <%if (SUBJECT_DISPLAY_TYPE  == CtdbConstants.PATIENT_DISPLAY_GUID) {%>
            {
                name: 'frlId',
                title: '<%=rs.getValue("report.frl.label.guid",l)%>',
                parameter: 'frl_guid',
                data: 'frlId'
            },
            <%}%>
            <%if (SUBJECT_DISPLAY_TYPE  == CtdbConstants.PATIENT_DISPLAY_MRN) {%>
            {
                name: 'frlId',
                title: '<%= rs.getValue("patient.mrn.display", l) %>',
                parameter: 'frl_mrn',
                data: 'frlId'
            },
            <%}%>
            {
                name: 'frlFormName',
                title: '<%=rs.getValue("report.frl.label.formName",l)%>',
                parameter: 'frl_formName',
                data: 'frlFormName'
            },
            {
                name: 'frlIntervalName',
                title: '<%=rs.getValue("report.frl.label.visitType",l)%>' ,
                parameter: 'frl_intervalName',
                data: 'frlIntervalName'
            }, 
            {
                name: 'frlCreatedDate',
                title: '<%=rs.getValue("report.frl.label.createDate",l)%>',
                parameter: 'frl_createdDate',
                data: 'frlCreatedDate'
            }, 
            {
                name: 'frlUpdateDate',
                title: '<%=rs.getValue("report.frl.label.updateDate",l)%>',
                parameter: 'frl_updateDate',
                data: 'frlUpdateDate'
            },
            {
                name: 'frlRequiredOptional',
                title: '<%=rs.getValue("report.frl.label.requiredOptional",l)%>',
                parameter: 'frl_requiredOptional',
                data: 'frlRequiredOptional'
            }
            

        ],
        buttons: [
			{
				extend: "collection",
				title: 'Forms_Requiring_Lock_'+now,
				buttons: [
					{
		                extend: 'excel',
		                className: 'btn btn-xs btn-primary p-5 m-0 width-35 assets-export-btn  buttons-excel',
		                extension: '.xlsx',
						exportOptions: {
							orthogonal: 'export'
						},
						enabled: true,
						action: IdtActions.exportAction()
						
					},
		            {
		                extend: 'csv',
		                text: 'csv',
		                className: 'btn btn-xs btn-primary p-5 m-0 width-35 assets-export-btn  buttons-csv',
		                extension: '.csv',
		                exportOptions: {
		                  orthogonal: 'export'
		                },
		                enabled: true,
		                action: IdtActions.exportAction()
		            },					
				]
			}			
		]
	})		
});
</script>

<p><s:text name="report.frl.instruction" /></p>
<br>

<div id="frlReportContainer" class="idtTableContainer brics">
	<table id="frlReportTable" class="table table-striped table-bordered" width="100%">
	</table>
</div>	
<jsp:include page="/common/footer_struts2.jsp" />
</html>