<jsp:include page="/common/doctype.jsp" />
<%@ taglib uri="/struts-tags" prefix="s"%>
<%@ taglib uri="/WEB-INF/datatables.tld" prefix="idt" %>
<%@ taglib uri="/WEB-INF/security.tld" prefix="security" %>

<%-- CHECK PRIVILEGES --%>
<security:check privileges="viewaudittrails"/>

<html>
	<s:set var="pageTitle" scope="request">
		<s:text name="app.title" />
	</s:set>
	<jsp:include page="/common/popUpHeader_struts2.jsp" />  
    <body>
		<div id="wrap">
			<h3 align="left"><s:text name='patient.attachment.auditLog'/></h3>
			<p align="left"><s:text name='patient.attachment.auditLog.instruction'/></p><br>
			<div class="dataTableContainer" id="auditContent">
				<idt:jsontable name="_attachmentsAudit" scope="request" decorator="gov.nih.nichd.ctdb.attachments.tag.AttachmentHomeDecorator" >
		            <idt:setProperty name="basic.msg.empty_list" value="There are no versions to display at this time."/>
		            <idt:column property="version" title="Version" />
		            <idt:column property="patientAttachmentName" title="Name"/>
		            <idt:column property="fileName" title="File name" />
		            <idt:column property="description" title="Description" />
		            <idt:column property="changeReason" title="Change Reason" />
		            <idt:column align="center" property="createdDate" title="Date Created" decorator="gov.nih.nichd.ctdb.common.tag.DateColumnDecorator"/>
		            <idt:column align="center" property="updatedByUsername" title="Modified By"/>
		            <idt:column align="center" property="updatedDate" title="Last Modified" decorator="gov.nih.nichd.ctdb.common.tag.DateColumnDecorator"/>
		        </idt:jsontable>
			</div>
			<div class="formbutton">
				<input type="button" value="<s:text name='button.Close'/>" id="bntCloseAudit" onClick="window.close()" /> 
			</div>
		</div>
	</body>
</html>