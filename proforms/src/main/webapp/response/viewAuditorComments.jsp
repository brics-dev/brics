<jsp:include page="/common/doctype.jsp" />
<%@ page import="gov.nih.nichd.ctdb.common.rs,java.util.Locale" %>
<%@ page import="gov.nih.nichd.ctdb.protocol.domain.Protocol"%>
<%@ page import="gov.nih.nichd.ctdb.security.domain.User"%>
<%@ page import="gov.nih.nichd.ctdb.common.CtdbConstants"%>
<%@ page buffer="100kb" %>
<%@ taglib uri="/WEB-INF/security.tld" prefix="security" %>
<%@ taglib uri="http://java.sun.com/jstl/core" prefix="c" %>
<%@ taglib uri="/struts-tags" prefix="s" %>
<%@ taglib uri="/WEB-INF/datatables.tld" prefix="idt" %>

<%-- CHECK PRIVILEGES --%>
<security:check privileges="viewprotocols, addeditprotocols, validuser"/>
<% 
	Locale l = request.getLocale();
	User user = (User) session.getAttribute(CtdbConstants.USER_SESSION_KEY);
	Protocol protocol = (Protocol) session.getAttribute(CtdbConstants.CURRENT_PROTOCOL_SESSION_KEY);
%>

<html>

<s:set var="pageTitle" scope="request">
	<s:text name="report.viewAuditorComments" />
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
	
	//view auditor comments summary
	$('#viewqueriesSumTable').idtTable({
		idtUrl: "<s:property value='#webRoot'/>/response/getVQSum.action",
		dom: 'Bfrtip',
	    language: {
	        "emptyTable": "There are no auditor comments at this time."
	    },
		columns: [
			{
				title:"Subject",
				data: "subject",
				name:"subject",
				parameter: "subject"
			},
			{
				title:"E-form",
				data: "eformName",
				name:"eformName",
				parameter: "eformName"
			},
			{
				title:"# of Auditor Comments",
				data: "countLk",
				name:"countLk",
				parameter: "countLk"
			}
		],	        
		buttons: [
			{
				extend: "collection",
				title: 'View Auditor Comments Summary Report - '+now,
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
	
	//View Auditor Comments Details Table
	$('#responseListTableVQ').idtTable({
		idtUrl: "<s:property value='#webRoot'/>/response/getAuditorCommentsDetailList.action",
		dom: 'Bfrtip',
	    language: {
	        "emptyTable": "There are no auditor comments to display at this time."
	    },
		columns: [
			{
				title:"Subject",
				data: "subject",
				name:"subject",
				parameter: "subject"
			},
			{
				title:"E-form",
				data: "eformName",
				name:"eformName",
				parameter: "eformName"
			},
			{
				title:"Date/Time",
				data: "editDate",
				name:"editDate",
				parameter: "editDate",
				render: IdtActions.formatDateWithSeconds()
			},
			{
				title:"Question Text",
				data: "quesText",
				name:"quesText",
				parameter: "quesText"
			},
			{
				title:"Audit Status",
				data: "audStatus",
				name:"audStatus",
				parameter: "audStatus"
			}
					
		],	        
		buttons: [
			{
				extend: "collection",
				title: 'View Audit Comments Report - '+now,
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

<h3 class="toggleable" style="cursor: pointer;">
	<a class="toggleable" href="javascript:;">
		<s:text name="report.viewAuditorComments" />
	</a>
</h3>
<div>

	<table border=0 width="100%" style="margin-top:10px;">		
        <tr>
			<td class="subPageTitle"><h3><s:text name="report.viewAuditorComments.Summary"/></h3></td>
		</tr>	        
        <tr style="margin-top:10px;">
            <td style="width:100%;">
            <hr/>		
            	<div id="viewqueriesSumContainer" class="idtTableContainer brics" style="margin:30px 0 0 5px; width: 98%;">
					<table id="viewqueriesSumTable"></table>
				</div>
			</td>
        </tr>

        <tr>
	        <td><img src="<s:property value="#imageRoot"/>/spacer.gif" width="1" height="15" alt="" border="0"/></td>
	    </tr>
        <tr>
			<td class="subPageTitle"><h3><s:text name="report.viewAuditorComments.Details"/></h3></td>
		</tr>	        
        <tr style="margin-top:10px;">
            <td style="width:100%;">
            <hr/>		
            	<div id="responseListContainerVQ" class="idtTableContainer brics" style="margin:30px 0 0 5px; width: 98%;">
					<table id="responseListTableVQ"></table>
				</div>
			</td>
        </tr>
	 </table>

</div>

<jsp:include page="/common/footer_struts2.jsp" />
</html>