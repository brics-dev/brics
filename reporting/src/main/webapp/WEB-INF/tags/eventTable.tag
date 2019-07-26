<%@include file="/common/taglibs.jsp"%>
<%@taglib prefix="ndar" tagdir="/WEB-INF/tags"%>
<%@ attribute name="eventLogList" type="java.util.Collection" required="true"%> 

<div id="eventTable" class="dataTableContainer dataTableJSON">
	<idt:jsontable name="eventLogList" id="eventLogs" decorator="gov.nih.tbi.taglib.datatableDecorators.EventLogListDecorator">
		<idt:setProperty name="basic.msg.empty_list" value="There are no event log at this time." />
		<idt:column title="Date" property="createTime" styleClass="nowrap" decorator="gov.nih.tbi.taglib.datatableDecorators.DateColumnDecorator"/>
		<idt:column title="User" property="user" />
		<idt:column title="Action Taken" property="actionTaken"  />
		<idt:column title="Reason Given" property="comment"  />
	    <idt:column title="Attachments" property="docNameLink" />
	</idt:jsontable>
</div>
