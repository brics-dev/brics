<jsp:include page="/common/doctype.jsp" />

<%@ taglib uri="/struts-tags" prefix="s"%>
<%@ taglib uri="/WEB-INF/display.tld" prefix="display" %>
<%@ taglib uri="/WEB-INF/security.tld" prefix="security" %>

<%-- CHECK PRIVILEGES --%>
<security:check privileges="viewaudittrails, viewprotocols, addeditprotocols"/>

<html>
<s:set var="pageTitle" scope="request">
	<s:text name="protocol.audit.title.display" />
</s:set>
<jsp:include page="/common/popUpHeader_struts2.jsp" />

<body>
	<div id="wrap">
		<h3 align="left"><s:text name="protocol.audit.title.display"/></h3>
		
		<div id="protocolAuditDiv">
			<div class="dataTableContainer" id="protocolAuditDisplayTable">
		       <display:table border="0" width="100%" cellpadding="4" cellspacing="1" name="versions" scope="request" 
		       			decorator="gov.nih.nichd.ctdb.protocol.tag.ProtocolAuditDecorator">
		           <display:setProperty name="basic.msg.empty_list" value="There are no versions to display at this time."/>
		           <display:column property="versionDec" title="Version" />
		           <display:column property="protocolNumber" title="Protocol Number"/>
		           <display:column property="name" title="Name"/>
		           <display:column property="createdDate" title="Date Created" decorator="gov.nih.nichd.ctdb.common.tag.DateTimeColumnDecorator"/>
		           <display:column property="updatedByUsername" title="Modified By"/>
		           <display:column property="updatedDate" title="Last Modified" decorator="gov.nih.nichd.ctdb.common.tag.DateTimeColumnDecorator"/>
		       </display:table>
			</div>
		</div>
		<div class="formbutton">
			<input type="button" value="<s:text name='button.Close'/>" id="bntCloseAudit" onClick="window.close()" title= "Click to close"/> 
		</div>
	</div>
</body>
</html>