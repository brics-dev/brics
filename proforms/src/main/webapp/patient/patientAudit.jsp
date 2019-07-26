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
	<s:text name="patient.audit.title.display" />
</s:set>
<jsp:include page="/common/popUpHeader_struts2.jsp" />

<body>
	<div id="wrap">
		<s:set var="disallowingPII" value="#systemPreferences.get('guid_with_non_pii')" />
		
		<h3 align="left">
			<s:text name="patient.audit.title.display" />
		</h3>
		<p align="left"><s:text name='patient.audit.instruction'/></p>
		<br>
		<div class="dataTableContainer" id="patientAuditContent">
			<display:table border="0" cellpadding="1" cellspacing="1" width="60%" name="patientVersions" 
					scope="request" decorator="gov.nih.nichd.ctdb.patient.tag.PatientAuditDecorator">
				<display:setProperty name="basic.msg.empty_list" value="There are no versions to display at this time." />
				<display:column property="version" title="Version" />
				
        		<s:if test="#disallowingPII == 1">
					<display:column property="guid" title='<%=rs.getValue("response.resolveHome.tableHeader.subjectGUID",l)%>' />
				</s:if>
				<s:else>
					<display:column property="lastName" title='<%=rs.getValue("patient.lastname.display",l)%>' />
					<display:column property="firstName" title='<%=rs.getValue("patient.firstname.display",l)%>' />
				</s:else>
				
				<display:column property="subjectId" title='<%=rs.getValue("patient.scheduleVisit.subjectID.display",l)%>' />
				<display:column property="createdDate" title='<%=rs.getValue("question.version.question.date.created.display",l)%>'
					decorator="gov.nih.nichd.ctdb.common.tag.DateColumnDecorator" />
				<display:column property="updatedByUsername" title="Modified&nbsp;By" />
				<display:column property="updatedDate" title="Last&nbsp;Modified"
					decorator="gov.nih.nichd.ctdb.common.tag.DateColumnDecorator" />
			</display:table>
		</div>

		<h3 align="left"><s:text name='patient.audit.subinstruction'/></h3>
		<br>
		<div class="dataTableContainer" id="patientAuditDetail">
			<display:table border="0" cellpadding="1" cellspacing="1" width="60%" name="patientChanges" 
					scope="request" decorator="gov.nih.nichd.ctdb.patient.tag.PatientAuditDetailDecorator">
				<display:setProperty name="basic.msg.empty_list" value="There are no versions to display at this time." />
 				<display:column property="version" title="Subject Version" />
				<display:column property="fieldName" title="Field Name" />
 				<display:column property="fieldValueOriginal" title="Original Value" />
 				<display:column property="fieldValueUpdated" title="Updated Value" />
 				<display:column property="updatedByUsername" title="Modified&nbsp;By" />
				<display:column property="updatedDate" title="Last&nbsp;Modified" 	decorator="gov.nih.nichd.ctdb.common.tag.DateColumnDecorator" />
			</display:table>
		</div>
		<div class="formbutton">
			<input type="button" value="<s:text name='button.Close'/>" id="bntCloseAudit" onClick="window.close()" title="Click to close" />
		</div>
	</div>
</body>
</html>