<jsp:include page="/common/doctype.jsp" />
<%@ page import="gov.nih.nichd.ctdb.common.CtdbConstants"%>
<%@ page import="gov.nih.nichd.ctdb.common.StrutsConstants"%>
<%@ page import="gov.nih.nichd.ctdb.protocol.domain.Protocol"%>
<%@ page import="gov.nih.nichd.ctdb.security.domain.User" %>
<%@ page import="gov.nih.nichd.ctdb.common.CtdbLookup"%>
<%@ page import="java.util.List" %>
<%@ page import="gov.nih.nichd.ctdb.common.rs,java.util.Locale" %>
<%@ taglib uri="/struts-tags" prefix="s"%>
<%@ taglib uri="/WEB-INF/display.tld" prefix="display" %>
<%@ taglib uri="/WEB-INF/security.tld" prefix="security" %>

<%-- CHECK PRIVILEGES --%>

<html>

<%-- Include Header --%>
<s:set var="pageTitle" scope="request">
	<s:text name="myworkspace.pickstudy.title.display" />
</s:set>
<jsp:include page="/common/header_struts2.jsp" />

<%
	User user = (User) session.getAttribute(CtdbConstants.USER_SESSION_KEY);
	Locale l = request.getLocale();
%>

<script type="text/javascript">

var canEditStudy = <%= Boolean.toString(user.isCreateStudy()) %>;

function deleteProtocols() {
	var url = "";
	var basePath = "<s:property value="#webRoot"/>";
	var selected_Protocol_Id = new Array();

	if (confirm("<s:text name="study.alert.remove" />")) {
		selected_Protocol_Id = IbisDataTables.getSelectedOptions(IbisDataTables.getTable($("#protocolSnapshotContainer")));
		url = basePath + "/protocol/protocolDelete.action?id=" + selected_Protocol_Id;
		redirectWithReferrer(url);
	} else {
		return false;
	}
}

function viewAudit() {
	var url = "";
	var basePath = "<s:property value="#webRoot"/>";
	var table = IbisDataTables.getTable($("#protocolSnapshotContainer"));
	
	var selectedProtocolId = IbisDataTables.getSelectedOptions(table)[0];
	
	if (IbisDataTables.countSelectedOptions(table) != 0) {
		url = basePath + "/protocol/protocolAudit.action?id=" + selectedProtocolId;
		Javascript:popupWindow(url);
	}
}


/**** Tasks to do on page load ****/
$(document).ready(function()
{
	/**** Register event handlers ****/
	$("#create").click(function(event)
	{
		$("#createStudySection").dialog({
			title: "<s:text name="study.create.title"/>",
			width: "778px"
		});
	});
});
</script>





<div class="dataTableContainer" id="protocolSnapshotContainer">
	<div>
		<p><s:text name="myworkspace.pickstudy.subinstruction.display"/></p>
	</div>

	<ul>
		<security:hasProtocolPrivilege privileges="viewaudittrails">
			<li><input type="button" value="<s:text name='button.ViewAudit' />" name="ViewAudit" id="ViewAudit" onClick="viewAudit()"  title= "<s:text name="tooltip.viewAudit.study" />"/></li>
		</security:hasProtocolPrivilege>
		<security:hasProtocolPrivilege privilege="addeditprotocols">
			<li><input type="button" value="<s:text name='button.Delete' />" name="Delete" id="Delete" onClick="deleteProtocols()" class="enabledOnMany" title= "<s:text name="tooltip.delete" />"/></li>
		</security:hasProtocolPrivilege>
		
		<%
			if (user.isSysAdmin()) {
		%>
				<li>
					<input type="button" value="<s:text name='myworkspace.pickstudy.CreateStudy' />" name="Create" id="create" 
						class="alwaysEnabled" title="<s:text name="tooltip.myworkspace.pickstudy.CreateStudy" />"/>
				</li>
		<%
			}
		%>
	</ul>
	<display:table scope="request" name="protocolList" decorator="gov.nih.nichd.ctdb.protocol.tag.ProtocolInboxDecorator">
		<display:setProperty name="basic.msg.empty_list" value="There are no studies to display at this time." />
		<display:column nowrap="true" property="protocolIdCheckbox" title="" align="center"/>
		<display:column nowrap="true" property="switchStudyLink" title='<%=rs.getValue("study.add.number.display",l)%>' />	
		<display:column property="name" title='<%=rs.getValue("protocol.add.name.display",l)%>' />
		<display:column property="studyTypeName" title='<%=rs.getValue("study.add.type.display",l)%>'/>
		<display:column property="status.shortName" title='<%=rs.getValue("protocol.add.status.display",l)%>' visible="false" />
	</display:table>
</div>

<div id="createStudySection" class="hidden">
	<s:form action="createStudyFromDashboard" namespace="/protocol" method="post" id="createStudyForm">
		<s:hidden name="protoForm.id" value="-1"/>
		<s:hidden name="protoForm.welcomeUrl" value=""/>
		<s:hidden name="protoForm.status" value="1"/>
		<s:hidden name="protoForm.addedFromDashboard" value="true"/>
		
		<p style="text-align:left;">
			<i><label class="requiredInput"></label><s:text name="study.create.requiredSymbol"/></i>
		</p>
		<br>
		<div class="formrow_1">
			<label class="requiredInput"><s:text name="study.add.name.display" /></label>
			<s:textfield name="protoForm.name" maxlength="400" size="45" />
		</div>
		<div class="formrow_1">
			<label class="requiredInput"><s:text name="study.add.number.display" /></label>
			<s:textfield name="protoForm.protocolNumber" maxlength="50" size="45" />
		</div>
		<div class="formrow_1">
			<label class="requiredInput"><s:text name="study.add.type.display" /></label>
			<s:select id="protoForm.protocolType" cssClass="protocolType" name="protocolType" list="#request.xStudyTypes"
					listKey="id" listValue="shortName+':'+longName" />
		</div>
		<div class="formrow_1">
			<input type="submit" id="createStudyBtn" value="<s:text name='button.pickstudy.createStudy'/>" title="Click to create the protocol"/>
		</div>
	</s:form>
</div>

<%-- Include Footer --%>
<jsp:include page="/common/footer_struts2.jsp" />
</html>