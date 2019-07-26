<jsp:include page="/common/doctype.jsp" />
<%@ page import="gov.nih.nichd.ctdb.protocol.domain.Protocol,
                 gov.nih.nichd.ctdb.form.domain.Form,
                 gov.nih.nichd.ctdb.response.domain.DataEntryWorkflowType,
                 gov.nih.nichd.ctdb.common.*"%>
<%@ page import="gov.nih.nichd.ctdb.common.Image"%>
<%@ page import="gov.nih.nichd.ctdb.common.CtdbConstants" %>
<%@ taglib uri="/struts-tags" prefix="s"%>
<%@ taglib uri="/WEB-INF/display.tld" prefix="display" %>
<%@ taglib uri="/WEB-INF/security.tld" prefix="security" %>



<%-- CHECK PRIVILEGES --%>
<security:check privileges="addeditforms"/>

<jsp:useBean id="user" scope="session" class="gov.nih.nichd.ctdb.security.domain.User"/>



<p>
This form is administered. <br>
Editing this form may affect current data collections. <br>
You can either save this form as a new form or you can edit this form. <br>
</p>
<br>

<div align="right">
		<input type="button" value="Save as New" onclick="administeredSaveAsNew()" id="administeredSaveAsNew" />
		<input type="button" value="Edit form" onclick="administeredEditForm()" id="administeredEditForm" />
</div>
			
			
