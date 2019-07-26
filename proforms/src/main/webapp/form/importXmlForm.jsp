<jsp:include page="/common/doctype.jsp" />
<%@ page import="gov.nih.nichd.ctdb.common.CtdbConstants" %>
<%@ page import="gov.nih.nichd.ctdb.form.domain.Form" %>
<%@ taglib uri="/WEB-INF/security.tld" prefix="security" %>
<%@ taglib uri="/WEB-INF/display.tld" prefix="display" %>
<%@ taglib uri="/struts-tags" prefix="s" %>

<%-- CHECK PRIVILEGES --%>
<security:check privileges="importexportforms" />

<html> 
<%-- Include Header --%>
<s:set var="pageTitle" scope="request">
	<s:text name="form.import.pagetitle" />
</s:set>

<jsp:include page="/common/header_struts2.jsp" />

<script type="text/javascript">

function cancelImportForm() {
	var url = "<s:property value='#webRoot'/>/form/formHome.action";
	redirectWithReferrer(url);
}

$(document).ready(function(){
	$("input#formSubmit").click(function(){
		// evaluate any empty fields
		var errmsg = "";
		
		if ($("div#formNameDiv").is(":visible")) {
			if ( $.trim($("#newFormName").val() ) == ""){
				errmsg += "Form name is required\n";
			}
			else if ( $.trim($("#newFormName").val()).length >50) {
				errmsg += "New form name is over the the maximum of 50 characters\n";
			}
		}
		
 		if (errmsg != ""){
			$("div#messageContainer div").remove();
			$.ibisMessaging("primary", "error", errmsg);
			return false;
		}
 		
		var fields= $("div#questionNameDiv input[type='text']").serializeArray();
		$("input#newNames").val(JSON.stringify(fields) );
		//$("form[name='currentForm']").submit();
	});
});

</script>

<br/>
<s:form theme="simple"  method="post" name="currentForm" enctype="multipart/form-data">
	<s:hidden name="xmlImportForm.action" />
	<s:hidden name="id" />
	<s:hidden name="xmlImportForm.newNames" id="newNames" />

 	<div id="fileInputDiv" <s:if test="%{xmlImportForm.formNameError}"> class="hidden" </s:if> >
		<div class="formrow_1">
			<label for="file">
				<s:text name="form.import.filename.display" /></label>
			<s:file name="xmlImportForm.document" size="30" maxLength="50" />
		</div>	
	</div>
 	<div id="formNameDiv" <s:if test="%{!xmlImportForm.formNameError}"> class="hidden" </s:if> >
 		<h3 id="renameFormName" class="toggleable">Rename Form Name</h3>
		<div>
			<div class="formrow_2">
				<label>Existing Form Name</label>
				<span>
                        <s:property value="xmlImportForm.existingFormName"/>
				</span>
			</div>
			<div class="formrow_2">
				<label>New Form Name</label>
				<s:textfield name="xmlImportForm.newFormName" id = "newFormName" />
			</div>
		</div>
	</div>
	<div id="formStructureDataElemetnsDiv" <s:if test="%{!xmlImportForm.formNameError}"> class="hidden" </s:if> >
	</div>
	
	<div class="formrow_1">
		<input	type="button" value="<s:text name='button.Cancel'/>"
			title="Cancel" onclick="cancelImportForm();" />
		<s:submit action="importXmlForm" id="formSubmit" value="Submit" title="Submit" /> 
	</div>
</s:form>

<%-- Include Footer --%>
<jsp:include page="/common/footer_struts2.jsp" />
</html>
