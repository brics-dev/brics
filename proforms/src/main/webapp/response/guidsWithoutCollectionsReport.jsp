<jsp:include page="/common/doctype.jsp" />

<%@ page import="gov.nih.nichd.ctdb.common.rs, java.util.Locale" %>
<%@ taglib uri="/struts-tags" prefix="s" %>
<%@ taglib uri="/WEB-INF/datatables.tld" prefix="idt" %>
<%@ taglib uri="/WEB-INF/security.tld" prefix="security" %>
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


<style type="text/css">
	#guidsWithoutCollectionsTable_wrapper {
		overflow: visible;
	}
	
</style>

<html>
	<%-- Include Header --%>
	<s:set var="pageTitle" scope="request">
		<s:text name="report.guidsWithoutCollectionsReport"/>
	</s:set>
	<jsp:include page="/common/header_struts2.jsp" />

	
	
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
	$("#guidsWithoutCollectionsTable").idtTable({
		idtUrl: "<s:property value='#webRoot'/>/response/getGuidsWithoutCollectionsReportsList.action",
		dom: 'Bfrtip',
        columns: [
        	<%if (SUBJECT_DISPLAY_TYPE  == CtdbConstants.PATIENT_DISPLAY_ID) {%>
        	{
                name: 'nrn',
                title: '<%= rs.getValue("patient.label.SubjectID", l) %>',
                parameter: 'nRN',
                data: 'nrn'
            },
            <%}%>
            <%if (SUBJECT_DISPLAY_TYPE  == CtdbConstants.PATIENT_DISPLAY_GUID) {%>
            {
                name: 'guid',
                title: '<%=rs.getValue("report.subjectGUID",l)%>',
                parameter: 'guId',
                data: 'guid'
            },
            <%}%>
            <%if (SUBJECT_DISPLAY_TYPE  == CtdbConstants.PATIENT_DISPLAY_MRN) {%>
            {
                name: 'mrn',
                title: '<%= rs.getValue("patient.mrn.display", l) %>',
                parameter: 'mRN',
                data: 'mrn'
            },
            <%}%>

        ],
        buttons: [
			{
				extend: "collection",
				title: 'GUIDs_Without_Collections_'+now,
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
						
					}				
				]
			}			
		]
	})		
});
</script>
	
	<p><s:text name="report.guidsWithoutCollectionsReport.instruction"/></p>
	<br>
	<div id="guidsWithoutCollectionsContainer" class="idtTableContainer brics">
		<table id="guidsWithoutCollectionsTable" class="table table-striped table-bordered" width="100%">
		</table>
	</div>	
	<iframe id="txtArea1" style="display:none"></iframe>
	<%-- Include Footer --%>
	<jsp:include page="/common/footer_struts2.jsp" />
</html>