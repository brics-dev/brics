<jsp:include page="/common/doctype.jsp" />
<%@ taglib uri="/WEB-INF/security.tld" prefix="security" %>
<%@ taglib uri="/struts-tags" prefix="s"%>

<%-- CHECK PRIVILEGES --%>
<security:check privileges="sysadmin"/>

<html>

<%-- Include Header --%>
<jsp:include page="/common/header_struts2.jsp" />

<%-- Presentation Logic Only Below--%>
<h2>Update DataSubmission Table by Admin Form ID:</h2>

This field takes a list of admin form ids separated by a comma. <br><br>

<s:form id="addCollection" theme="simple" method="post" action="addCollection" name="addCollection" enctype="multipart/form-data" >
	<div class="formrow_1">
		<label for="adminFormId" class="requiredInput">Admin Form IDs</label>
		<s:textarea name="adminFormId" id="adminFormId" maxlength="400" />
	</div>
</s:form>

<br><br>
<%-- <s:submit method="setCollectionForMirth" value="Submit"/>  --%>
<input type="button" value="<s:text name='button.Submit' />" onclick="submitTheFormStructure()" title="Click to update data submission table with collection data."/>

<%-- Include Footer --%>
<jsp:include page="/common/footer_struts2.jsp" />
</html>

<script type="text/javascript">
function submitTheFormStructure() {
	var theForm = document.forms['addCollection'];
	theForm.submit();

}
</script>