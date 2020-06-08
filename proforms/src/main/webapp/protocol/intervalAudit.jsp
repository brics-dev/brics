<jsp:include page="/common/doctype.jsp" />
<%@ taglib uri="/struts-tags" prefix="s" %>
<%@ taglib uri="/WEB-INF/datatables.tld" prefix="idt" %>
<%@ taglib uri="/WEB-INF/security.tld" prefix="security" %>

<%-- CHECK PRIVILEGES --%>
<security:check privileges="viewaudittrails, viewintervals, addeditintervals"/>

<html>

<s:set var="pageTitle" scope="request">
	<s:text name="interval.audit.title.display" />
</s:set>
<jsp:include page="/common/popUpHeader_struts2.jsp" />

<body>
<div id="wrap">
	<div class="formrow_1">
		<img src="<s:property value="#imageRoot"/>/spacer.gif" width="1" height="10" alt="" border="0" />
	</div>
	
	 <h3 align="left">
	 	<s:text name="interval.audit.title.display"/>
	 </h3>
	    
	<div id="intervalAuditDiv">
		<div class="dataTableContainer" id="intervalAuditDisplayTable">
			<idt:jsontable  name="versions" scope="request" decorator="gov.nih.nichd.ctdb.protocol.tag.ProtocolAuditDecorator">
	            <idt:setProperty name="basic.msg.empty_list" value="There are no versions to display at this time."/>
	            <idt:column property="version" title="Version" />
	            <idt:column property="name" title="Name"/>
	            <idt:column align="center" property="createdDate" title="Date&nbsp;Created" decorator="gov.nih.nichd.ctdb.common.tag.DateTimeColumnDecorator"/>
	            <idt:column align="center" property="updatedByUsername" title="Modified&nbsp;By"/>
	            <idt:column align="center" property="updatedDate" title="Last&nbsp;Modified" decorator="gov.nih.nichd.ctdb.common.tag.DateTimeColumnDecorator"/>
			</idt:jsontable>
		</div>
	</div>
	<div class="formbutton">
		<input type="button" value="<s:text name='button.Close'/>" id="bntCloseAudit" onClick="javascript:window.close();" title= "Click to close"/> 
	</div>
</div>
</body>
</html>