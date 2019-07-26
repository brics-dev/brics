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
	Import HL7 Data By Subject
</s:set>

<%
	String patientId = (String) request.getAttribute("patientId");
	String patientDisplayLabel = (String) request.getAttribute("patientDisplayLabel");
%>

<jsp:include page="/common/header.jsp" />

<script type="text/javascript">

function intervalOnChange() {

 	var intervalIdElm = document.getElementsByName("intervalId")[0];
	var optionIntervalId =  intervalIdElm.options[intervalIdElm.selectedIndex].value;


	var url = "<%= webRoot %>/response/dataCollection.do?action=process_populateFormNamesAJAX&optionIntervalId=" + optionIntervalId;

	$.ajax({
		type: "POST",
		url: url,
		data: optionIntervalId,
        beforeSend: function(){
			
        },
		success: function(response) {
			var selectObject = document.getElementsByName("theFormId")[0];
			for(var i=selectObject.length;--i >= 0;) {
				var optionElement = selectObject.options[i];
				selectObject.removeChild(optionElement);
			}
			var formsJSONArray=JSON.parse(response);
				
			for(var i=0;i<formsJSONArray.length;i++) {
				
					var fId = formsJSONArray[i].id;
					var fName = formsJSONArray[i].name;
					
					
					var option = document.createElement("option");
				
					option.appendChild(document.createTextNode(fName));
					option.setAttribute("value",fId);
					selectObject.appendChild(option)				
			}
			
			var formNameElm = document.getElementsByName("theFormId")[0];
			var optionFormId =  formNameElm.options[formNameElm.selectedIndex].value;

			var formId = document.getElementsByName("formId")[0];
			formId.value = optionFormId;
		},
		error: function(e) {
			alert("error" + e );
		}
	});
}

function formNameOnChange() {
	var formNameElm = document.getElementsByName("theFormId")[0];
	var optionFormId =  formNameElm.options[formNameElm.selectedIndex].value;

	var formId = document.getElementsByName("formId")[0];
	formId.value = optionFormId;	
}

function cancel() {
	var url = "<%= webRoot %>/response/collectDataPreviousHome.do" 
	redirectWithReferrer(url);
}

function checkAndSubmit(){
	if($('#dataSourceURL').val()==<%= Integer.MIN_VALUE %>){
		alert("Data source is required");
	}else if($('#intervalId').val()==<%= Integer.MIN_VALUE %>){
		alert("Visit Type is required");
	}else if($('#theFormId').val()==<%= Integer.MIN_VALUE %>){
		alert("Form is required");
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
		<html:hidden property="subjectId" value="<%=patientId%>" />
		
		<div class="formrow_1">
			<label for="dataSourceURL" class="requiredInput">Data Source</label>
			<html:select property="dataSourceURL" styleId="dataSourceURL">
				<option value="<%= Integer.MIN_VALUE %>">- Please Select</option>
				<html:options collection="dataSourceURLOptions" property="value" labelProperty="label" />
			</html:select>
		</div>
		
		<div class="formrow_1">
			<label for="patientRecordId"> <bean:message
					key="response.collect.label.inhrecord" /></label>
			<html:text property="patientRecordId" size="20" maxlength="50"
				value="<%=patientDisplayLabel%>" disabled="true"/>
		</div>
		
		<div class="formrow_1">
			<label for="intervalId" class="requiredInput"><bean:message
					key="response.label.interval" /></label>
			<html:select property="intervalId" styleId="intervalId" onchange="intervalOnChange()">
				<option value="<%= Integer.MIN_VALUE %>">- Please Select</option>
				<html:options collection="intervalOptions" property="value"
					labelProperty="label" />
				<option value="-1">Other</option>
			</html:select>
		</div>

		<div id="formNameDiv" class="formrow_1">
			<label for="formName" class="requiredInput"> <bean:message key="response.collect.label.formname" /></label>
			<html:select property="theFormId" styleId="theFormId" onchange="formNameOnChange()">
				<option value="<%= Integer.MIN_VALUE %>">- Please Select</option>
				<html:options collection="activeforms" property="value"
					labelProperty="label" />
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
			<input type="button" value="<bean:message key='button.Cancel' />" title="Click to cancel (changes will not be saved)." alt="Cancel" onclick="cancel()" /> 
			<input type="button" value="Submit the import request" onclick="checkAndSubmit()"/>
		</div>
	</html:form>
</body>

<jsp:include page="/common/footer.jsp" />
