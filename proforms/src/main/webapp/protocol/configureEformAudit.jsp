<jsp:include page="/common/doctype.jsp" />
<%@ page import="gov.nih.nichd.ctdb.common.rs,java.util.Locale" %>
<%@ taglib uri="/struts-tags" prefix="s"%>
<%@ taglib uri="/WEB-INF/display.tld" prefix="display"%>
<%@ taglib uri="/WEB-INF/security.tld" prefix="security"%>

<%-- CHECK PRIVILEGES --%>
<security:check privileges="viewaudittrails" />

<% Locale l = request.getLocale(); %>

<html>
<s:set var="pageTitle" scope="request">
	<s:text name="protocol.configure.eform.audit.title.display" />
</s:set>
<jsp:include page="/common/popUpHeader_struts2.jsp" />

<body>
<div id="wrap">
<div class="container960">
		<h3 align="left">
			<s:text name="protocol.configure.eform.audit.title.display" />
		</h3>
		

		
		
		<div id="configureEformAuditDetailContainer" class="idtTableContainer brics" style="display:block">
				<table id="configureEformAuditDetailTable" class="table table-striped table-bordered" width="100%">
				</table>
		</div>
		
		
	</div>	
</div>
</body>

<script type="text/javascript">
function getURLParameter(name) {
    return decodeURIComponent((new RegExp('[?|&]' + name + '=' + '([^&;]+?)(&|#|;|$)').exec(location.search) || [null, ''])[1].replace(/\+/g, '%20')) || null;
}
var eId = getURLParameter('eformId');
$(document).ready(function() {
	$('#configureEformAuditDetailTable').idtTable({
		idtUrl: "<s:property value='#webRoot'/>/protocol/getConfigureEformAuditList.action",
	    "language": {
	        "emptyTable": "There are no data entries to display at this time."
	    },
		filterData: {
			eformId: eId
		},
		columns: [
			{
				title:"Full Name",
				data: "username",
				name:"username",
				parameter: "username"
			},
			{
				title:"Date/Time",
				data: "updatedDate",
				name:"updatedDate",
				parameter: "updatedDate",
				render: IdtActions.formatDate()
			},
			{
				title:"Section",
				data: "sectionText",
				name:"sectionText",
				parameter: "sectionText",
				render: IdtActions.ellipsis(25)
			},
			{
				title:"Question",
				data: "questionText",
				name:"questionText",
				parameter: "questionText",
				render: IdtActions.ellipsis(25)
			},
			{
				title:"Action",
				data: "action",
				name:"action",
				parameter: "action"
			}
		]
	});	
	
})
</script>

</html>





