<jsp:include page="/common/doctype.jsp" />
<jsp:include page="/common/header_struts2.jsp" />
<%@ page import="gov.nih.nichd.ctdb.security.domain.User" %>
<%@ taglib uri="/struts-tags" prefix="s"%>
<%@ taglib uri="/WEB-INF/security.tld" prefix="security" %>

<html>
<%
	User user = (User) request.getSession().getAttribute("user");
	boolean isSysAdmin = user.isSysAdmin();
%>
<h1><s:text name="Randomization List"/></h1>
<s:form name="importRandomList" id="importRandomList" theme="simple" method="post" enctype="multipart/form-data" style="width: 80%;">
	<div>
	</br>
	<p>An administrator can import a randomization list. Please select a file to import. Note: The selected file must be in CSV format.</p> 
	<p>For reference purposes, you may download the <a href=<s:url value="/template/RandomizationListTemplate.csv" /> style="color:darkblue;">Randomization Import
			Template</a>.
	</p></br> 
	</div>
	<div style="width: 80%">
	<div class="formrow_1">
		<label for="new_file_upload">File:</label>
		<s:file name="upload" cssClass="textfield float-left" />
	</div>
	<div class="formrow_1">
		<input type="button" value="Import" onClick="importRandomizationList()" />
	</div>
	</div>
</s:form>
<div class="idtTableContainer brics" id="randomizationListTableDiv" style="width: 100%; margin-top: 10px;">
	<table id="randomizationListTable" class="table table-striped table-bordered" width="100%">
	</table>   
</div>
</div>
</html>
<script type="text/javascript">
var basePath = "<s:property value="#webRoot"/>";
var isSysAdmin = <%= isSysAdmin %>;
$('document').ready( function() {
	if(isSysAdmin) {
		$("#importRandomList").show();
	} else {
		$("#importRandomList").hide();
	}

	$("#randomizationListTable").idtTable({
		idtUrl: basePath + "/protocol/randomizationAction!getProtoRandomizationIdtList.action",
		idtData: {
			primaryKey: 'id'
		},
		columns: [
			{
				title: "Sequence",
				data: "sequence",
				name: "sequence",
				parameter: "sequence"
			},
			{
				title: "Group Name",
				data: "groupName",
				name: "groupName",
				parameter: "groupName"
			}
			<security:hasProtocolPrivilege privilege="unblindedRandomization">
			,{
				title: "Group Description",
				data: "groupDescription",
				name: "groupDescription",
				parameter: "groupDescription"
			}
			</security:hasProtocolPrivilege>
		]
	});
});

function importRandomizationList() {
	var theForm = document.forms['importRandomList'];
	theForm.action = basePath + '/protocol/randomizationAction!importRandomizationList.action';
    theForm.submit();
}
</script>

<%-- Include Footer --%>
<jsp:include page="/common/footer_struts2.jsp" />