<jsp:include page="/common/doctype.jsp" />
<%@ page import="gov.nih.nichd.ctdb.common.rs, java.util.Locale" %>
<%@ taglib uri="/WEB-INF/security.tld" prefix="security" %>
<%@ taglib uri="/struts-tags" prefix="s"%>

<%-- CHECK PRIVILEGES --%>
<security:check privileges="sysadmin"/>

<html>

<%-- Include Header --%>
<jsp:include page="/common/header_struts2.jsp" />

<%-- Presentation Logic Only Below--%>
<h2>Search for Admin Form with GUID, Form Structure Short Name, and Visit Type:</h2>

This field makes a list of Admin Forms, collected by their GUID, Form Structure Short Name, and Visit Type. <br><br>

Please ensure that for each textbox, the GUIDs, Form Structure ShortNames, and Visit Types are comma separated lists or directly pasted from their CSV or Excel columns.  <br><br>

<s:form id="searchFormParams" theme="simple" method="post" action="searchFormParams" name="searchFormParams" enctype="multipart/form-data" >
	<div class="formrow_1">
		<label for="guidList" class="requiredInput">GUIDs</label>
		<s:textarea name="guidList" id="guidList" />
	</div>
	<div class="formrow_1">
		<label for="shortnameList" class="requiredInput">eForm Short Names</label>
		<s:textarea name="shortnameList" id="shortnameList" />
	</div>
	<div class="formrow_1">
		<label for="visitList" class="requiredInput">Visit Types</label>
		<s:textarea name="visitList" id="visitList" />
	</div>
</s:form>


<div class="formrow_1">
	<div style="float: left;margin-right: 10px;margin-left: 100px;margin-bottom: 30px">
		<input type="button" value="<s:text name='button.Search'/>" id="formSearchBtn" title = "Click to search" 
				onclick="submitSearchFormParams()"/> 
	</div>
</div>

<script type="text/javascript">

function submitSearchFormParams() {
	var theForm = document.forms['searchFormParams'];
	var guidSplit = $("#guidList").val().replace("\n", ",").split(",");
	var shortNameSplit = $("#shortnameList").val().replace("\n", ",").split(",");
	var visitSplit = $("#visitList").val().replace("\n", ",").split(",");
	var guidList = $("#guidList").val();
	var shortNameList = $("#shortnameList").val();
	var visitTypeList = $("#visitList").val();
	var errorMsg = "Error: Received one or more empty text boxes.";
	var errorLength = "Error: Lists are of unequal length"
	
	 if (guidSplit.length !== shortNameSplit.length || guidSplit.length !== visitSplit.length){
		$.ibisMessaging("dialog", "error", errorLength)		
	}else if($("#guidList").val().length == 0 || $("#shortnameList").val().length == 0 || $("#visitList").val().length == 0){
		$.ibisMessaging("dialog", "error", errorMsg);
	}else{
		//var newUrl = "<s:property value='#webRoot'/>/admin/searchFormParams.action?guidSplit="+guidSplit+"&shortnameSplit="+shortnameSplit+"&visitSplit="+visitSplit;
		$("#formSearchTable").idtApi("getOptions").requestData = {
			guidList : guidList,
			shortNameList : shortNameList,
			visitList : visitTypeList
		};
		//$("#formSearchTable").idtApi("getTableApi").ajax.url(newUrl);
		$("#formSearchTable").idtApi("getTableApi").ajax.reload();
	}
	
	
}

$(document).ready(function() {
	var basePath = "<s:property value="#systemPreferences.get('app.webroot')"/>";
	$("#formSearchTable").idtTable({
		idtUrl: "<s:property value='#webRoot'/>/admin/searchFormParams.action",
		ajaxMethod:'POST',
		idtData: {
			primaryKey: 'studyPrefixId'
		},
		dom: 'Bfrtip',
        columns: [
        	{
                name: 'studyPrefixId',
                title: '<%=rs.getValue("form.studyid.display", request.getLocale())%>',
                parameter: 'studyPrefixId',
                data: 'studyPrefixId'
            },
            {
                name: 'protocolNum',
                title: '<%=rs.getValue("form.protocol.display", request.getLocale())%>',
                parameter: 'protocolNum',
                data: 'protocolNum'
            },
            {
                name: 'adminFormId',
                title: '<%=rs.getValue("form.adminid.display", request.getLocale())%>',
                parameter: 'adminFormId',
                data: 'adminFormId'
            },
            {
                name: 'guid',
                title: '<%=rs.getValue("form.guid.display", request.getLocale())%>',
                parameter: 'guid',
                data: 'guid'
            },
            {
                name: 'shortName',
                title: '<%=rs.getValue("form.shortname.display", request.getLocale())%>',
                parameter: 'shortName',
                data: 'shortName'
            },
            {
                name: 'visitType',
                title: '<%=rs.getValue("form.visittype.display", request.getLocale())%>',
                parameter: 'visitType',
                data: 'visitType'
            }
        ],
        buttons: [
        	{
				extend: "collection",
				title: 'List_of_Search_Results',
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
	

})

</script>

<div id="adminFormSearchResults" class="idtTableContainer brics">
	<table id="formSearchTable" class="table table-striped table-bordered" width="100%">
	</table>
</div>


<%-- Include Footer --%>
<jsp:include page="/common/footer_struts2.jsp" />
</html>

