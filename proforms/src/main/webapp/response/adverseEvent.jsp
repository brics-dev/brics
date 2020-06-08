<jsp:include page="/common/doctype.jsp" />
<%@ page import="gov.nih.nichd.ctdb.common.rs,java.util.Locale" %>
<%@ page import="gov.nih.nichd.ctdb.protocol.domain.Protocol"%>
<%@ page import="gov.nih.nichd.ctdb.security.domain.User"%>
<%@ page import="gov.nih.nichd.ctdb.common.CtdbConstants"%>
<%@ taglib uri="/WEB-INF/security.tld" prefix="security" %>
<%@ taglib uri="http://java.sun.com/jstl/core" prefix="c" %>
<%@ taglib uri="/struts-tags" prefix="s" %>

<%-- CHECK PRIVILEGES --%>
<security:check privileges="viewprotocols, addeditprotocols, validuser"/>
<% 
	Locale l = request.getLocale();
	User user = (User) session.getAttribute(CtdbConstants.USER_SESSION_KEY);
	Protocol protocol = (Protocol)session.getAttribute(CtdbConstants.CURRENT_PROTOCOL_SESSION_KEY);
%>

<html>
<s:set var="pageTitle" scope="request">
	<s:text name="report.ae.adverseEvent" />
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
	
	//adverse event
	$('#aeTable').idtTable({
		idtUrl: "<s:property value='#webRoot'/>/response/getAEs.action",
		dom: 'Bfrtip',
	    language: {
	        "emptyTable": "There are no adverse event at this time."
	    },
		columns: [
			{
				title:"Report Date",
				data: "answerUpddate",
				name:"answerUpddate",
				parameter: "answerUpddate"
			},
			{
				title:"Start Date",
				data: "aeStartdate",
				name:"aeStartdate",
				parameter: "aeStartdate"
			},
			{
				title:"End Date",
				data: "aeEnddate",
				name:"aeEnddate",
				parameter: "aeEnddate"
			},
			{
				title:"Subject Info",
				data: "subjectLk",
				name:"subjectLk",
				parameter: "subjectLk"
			}
		],	        
		buttons: [
			{
				extend: "collection",
				title: 'Adverse_Event_Report_'+now,
				buttons: [
		            {
		                extend: 'csv',
		                text: 'csv',
		                className: 'btn btn-xs btn-primary p-5 m-0 width-35 assets-export-btn  buttons-csv',
		                title: 'export_filename',
		                extension: '.csv',
		                name: 'csv',
		                exportOptions: {
		                  orthogonal: 'export'
		                },
		                enabled: true,
		                action: IdtActions.exportAction()
		            },	
					{
						extend: 'pdf',
						text: 'pdf',
						className: 'btn btn-xs btn-primary p-5 m-0 width-35 assets-export-btn buttons-pdf',
						title: 'export_filename',
						extension: '.pdf',
						name: 'pdf',
						exportOptions: {
							orthogonal: 'export'
						},
						enabled: true,
						action: IdtActions.exportAction()
					},
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
	});
});
 </script>
 
<s:set var="displayClinicalPoint" value="#systemPreferences.get('display.protocol.clinicalPoint')" />
 <h3 class="toggleable" style="cursor: pointer;">
	<a class="toggleable" href="javascript:;">
		<s:text name="report.ae.adverseEvent" />
	</a>
</h3>
<div>
<s:if test="#displayClinicalPoint">
	<table border=0 width="100%" style="margin-top:10px;">		
        <tr>
			<td class="subPageTitle"><h3><s:text name="report.ae.adverseEvent"/></h3></td>
		</tr>	        
        <tr style="margin-top:10px;">
            <td style="width:100%;">
            <hr/>		
            	<div id="aeContainer" class="idtTableContainer brics" style="margin:30px 0 0 5px; width: 98%;">
					<table id="aeTable"></table>
				</div>
			</td>
        </tr>
	 </table>
</s:if>
</div>
<jsp:include page="/common/footer_struts2.jsp" />
</html>