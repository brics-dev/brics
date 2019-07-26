<jsp:include page="/common/doctype.jsp" />
<%@ page import="java.util.List" %>
<%@ page import="java.util.HashMap" %>
<%@ page import="java.util.Map" %>
<%@ page import="org.json.JSONObject" %>
<%@ taglib uri="/struts-tags" prefix="s" %>
<%@ taglib uri="/WEB-INF/display.tld" prefix="display" %>
<%@ taglib uri="/WEB-INF/security.tld" prefix="security" %>

<%-- CHECK PRIVILEGES --%>
<security:check privileges="addeditquestions"/>

<s:bean name="gov.nih.nichd.ctdb.util.common.SystemPreferences" var="systemPreferences" />

<script src="<s:property value="#systemPreferences.get('app.webroot')"/>/common/js/jquery-1.7.2.js" type="text/javascript"></script>

<script type="text/javascript">
	$(document).ready(function(){
		if(typeof(parent.FormBuilder) !== 'undefined'){
			parent.FormBuilder.page.get("activeEditorView").finishQuestionAddEdit();
		}else{
			parent.finishQuestionAddEdit();
		}
	});
</script>

<%
	List<String> names = (List<String>)request.getSession().getAttribute("questionImageNames");
	JSONObject jsonObject = new JSONObject(new HashMap());
	jsonObject.put("graphicNames", names);
%>

<html>
	<table>
		<tr>
			<td>
				<input type='hidden' name='graphicJSON' id='graphicJSON' value='<%=jsonObject%>'>
				<% out.print(jsonObject); %>
			</td>
		</tr>
	</table>
</html>