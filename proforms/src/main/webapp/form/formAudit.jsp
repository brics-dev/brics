<jsp:include page="/common/doctype.jsp" />
<%@ taglib uri="/struts-tags" prefix="s"%>
<%@ taglib uri="/WEB-INF/display.tld" prefix="display" %>
<%@ taglib uri="/WEB-INF/security.tld" prefix="security" %>

<%-- CHECK PRIVILEGES --%>
<security:check privileges="viewaudittrails"/>

<html>
	<s:set var="pageTitle" scope="request">
		<s:text name="form.audit.title.display" />
	</s:set>
	<jsp:include page="/common/popUpHeader_struts2.jsp" />
	
    <body>
	<div id="wrap">
		<div class="container960">
		    <h3 align="left">
		    	<s:text name="form.audit.title.display"/>
		    </h3>
			<div class="dataTableContainer" id="formAuditContent">
				<display:table name="formversions" scope="request" decorator="gov.nih.nichd.ctdb.form.tag.FormAuditDecorator">
					<display:setProperty name="basic.msg.empty_list" value="There are no versions to display at this time."/>
					<display:column property="name" title="Form Name"/>
					<display:column property="description" title="Description"/>
					<display:column property="status.shortName" title="Status" />
					<display:column property="createdDate" title="Date&nbsp;Created" decorator="gov.nih.nichd.ctdb.common.tag.DateTimeColumnDecorator" />
					<display:column property="updatedByUsername" title="Modified&nbsp;By"/>
					<display:column property="updatedDate" title="Last&nbsp;Modified" decorator="gov.nih.nichd.ctdb.common.tag.DateTimeColumnDecorator"/>
					<%-- <display:column align="center" property="checkedOutByUsername" title="Checked&nbsp;Out&nbsp;By"/> --%>
				</display:table>
			</div>
			<div class="formbutton">
				<input type="button" value="<s:text name='button.Close'/>" id="bntCloseAudit" onClick="window.close()" title ="Click to close" /> 
			</div>
		</div>
	</div>
   </body>
</html>





















  