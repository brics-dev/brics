<jsp:include page="/common/doctype.jsp" />
<%@ page import="gov.nih.nichd.ctdb.protocol.domain.Protocol,gov.nih.nichd.ctdb.common.CtdbConstants"%>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html"%>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic"%>
<%@ taglib uri="/WEB-INF/display.tld" prefix="display"%>
<%@ taglib uri="/WEB-INF/security.tld" prefix="security"%>
<security:check privileges="dataentry, dataentryoversight" />

<jsp:useBean id="importHL7DataForm" scope="request" class="gov.nih.nichd.ctdb.response.form.ImportHL7DataForm" />
	
<s:set var="pageTitle" scope="request">
	Import HL7 Data By Form
</s:set>

<%
	String formName = (String) request.getAttribute("formName");
%>

<jsp:include page="/common/header.jsp" />

<script type="text/javascript">
function cancel() {
	var url = "<%= webRoot %>/response/collectDataPreviousHome.do" 
	redirectWithReferrer(url);
}

function checkAndSubmit(){
	if($('#dataSourceURL').val()==<%= Integer.MIN_VALUE %>){
		alert("Data source is required");
	}else if($('#intervalId').val()==<%= Integer.MIN_VALUE %>){
		alert("Visit Type is required");
	}else if($('#importType').val()==<%= Integer.MIN_VALUE %>){
		alert("Import Type is required");
	}else{
		$('#myForm').submit();
	}
}
</script>


<body>
	<html:form styleId="myForm" enctype="multipart/form-data">
		<html:hidden property="action" styleId="pageAction" />
		<html:hidden property="theFormId" />
		
		<div class="formrow_1">
			<label for="dataSourceURL" class="requiredInput">Data Source</label>
			<html:select property="dataSourceURL" styleId="dataSourceURL">
				<option value="<%= Integer.MIN_VALUE %>">- Please Select</option>
				<html:options collection="dataSourceURLOptions" property="value" labelProperty="label" />
			</html:select>
		</div>
		<div class="formrow_1">
			<label for="intervalId" class="requiredInput"><bean:message key="response.label.interval" /></label>
			<html:select property="intervalId" styleId="intervalId">
				<option value="<%= Integer.MIN_VALUE %>">- Please Select</option>
				<html:options collection="intervalOptions" property="value" labelProperty="label" />
				<option value="-1">Other</option>
			</html:select>
		</div>
		<div class="formrow_1">
			<label for="importType" class="requiredInput">Select Import Type</label>
			<html:select property="importType" styleId="importType">
				<option value="<%= Integer.MIN_VALUE %>">- Please Select</option>
				<option value="1">Demographic</option>
			</html:select>
		</div>
		<div class="formrow_1">
			<label for="formName"> <bean:message key="response.collect.label.formname" /></label>
				<html:text property="formName" size="20" maxlength="50" disabled="true" value="<%=formName%>" />
		</div>
		<div class="formrow_1">
			<input type="button" value="<bean:message key='button.Cancel' />" title="Click to cancel (changes will not be saved)." alt="Cancel" onclick="cancel()" /> 
			<input type="button" value="Submit the import request" onclick="checkAndSubmit()"/>
		</div>
	</html:form>
</body>

<jsp:include page="/common/footer.jsp" />
