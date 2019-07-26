<jsp:include page="/common/doctype.jsp" />
<%@ page import="gov.nih.nichd.ctdb.common.Version"%>
<%@ page import="gov.nih.nichd.ctdb.form.form.FormForm"%>
<%@ taglib uri="/struts-tags" prefix="s"%>
<%@ taglib uri="/WEB-INF/display.tld" prefix="display" %>
<%@ taglib uri="/WEB-INF/security.tld" prefix="security" %>

<%-- CHECK PRIVILEGES --%>
<security:check privileges="viewforms"/>

<%
	FormForm formForm = (FormForm) request.getAttribute("formForm");
	String action = formForm.getAction();
	request.setAttribute("action", action);
%>

<html>

	<s:set var="pageTitle" scope="request">
		<s:text name="form.addform.formdetail" />
	</s:set>
	
	<%-- Include popUp Header --%>
	<jsp:include page="/common/popUpHeader_struts2.jsp" />	
	<script src="<s:property value='#webRoot'/>/common/js/template.js" type="text/javascript"></script>
	<link rel="stylesheet" type="text/css" href="<s:property value="#systemPreferences.get('app.stylesheet')"/>">
	<link rel="stylesheet" type="text/css" href="<s:property value='#webRoot'/>/common/css/dataCollection.css">
	
	
	<script type="text/javascript">
	
		$(document).ready(function() {
			
		});
	
	</script>
	 
	<body>
		<h3><s:property value="#request.title" escapeHtml="false" /> [<s:property value="#request.type" escapeHtml="false" />]</h3>		
		
		<table  valign="top" cellpadding="50" width="100%">
			<tr>
				<td valign="top" align="left" COLSPAN=2  style='padding-top: 3px; padding-right: 10px; padding-bottom: 3px; padding-left: 10px;'>
					<span><b>Details:</b></span>
					<hr>
				</td>
				<td></td>
			</tr>
			<tr>
				<td valign="top" align="left"  style='padding-top: 3px; padding-right: 10px; padding-bottom: 3px; padding-left: 10px;'>
					<b>Description:</b>					
				</td>
				<td valign="top" align="left">
					<span><s:property value="#request.description" escapeHtml="false" /></span>
				</td>
			</tr>
			<tr>
				<td valign="top" align="left"  style='padding-top: 3px; padding-right: 10px; padding-bottom: 3px; padding-left: 10px;'>
					<b>Acknowledgement:</b>
				</td>
				<td valign="top" align="left">
					<span>PROMIS Health Organization and Assessment Center <sup>SM</sup>: <a href="https://assessmentcenter.net/documents/Assessment%20Center%20Terms%20and%20Conditions%20v7.1.pdf" target="_blank">View full acknowledgement</a></span>
				</td>
			</tr>
			<tr>
				<td valign="top" align="left"  style='padding-top: 3px; padding-right: 10px; padding-bottom: 3px; padding-left: 10px;'>
					<b>Terms of use:</b>
				</td>
				<td valign="top" align="left">
					<span>You understand and agree that the PROMIS Health Organization and PROMIS Cooperative Group 
							provides access to PROMIS instruments (e.g., item banks, short forms, profile measures) subject
							 to the PROMIS Terms and Conditions (PTAC). The PROMIS Health Organization/Cooperative Group reserves 
							 the right to update the PTAC at any time. Changes in the 
							 PTAC will apply to new<a href="https://www.assessmentcenter.net/documents/PROMIS%20Terms%20and%20Conditions%20v8.1.pdf" target="_blank">...show more</a></span>
				</td>
			</tr>
			<tr>
			</tr>
		</table>
	
		<jsp:include page="/common/messages_struts2.jsp" />
	</body>
</html>