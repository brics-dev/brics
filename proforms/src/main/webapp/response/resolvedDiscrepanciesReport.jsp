<jsp:include page="/common/doctype.jsp" />

<%@ page import="gov.nih.nichd.ctdb.common.rs, java.util.Locale" %>
<%@ taglib uri="/struts-tags" prefix="s" %>
<%@ taglib uri="/WEB-INF/display.tld" prefix="display" %>
<%@ taglib uri="/WEB-INF/datatables.tld" prefix="idt" %>
<%@ page buffer="100kb" %>

<% Locale l = request.getLocale(); %>

<html>

	<s:set var="pageTitle" scope="request">
		<s:text name="report.DiscrepancyReport" />
	</s:set>
	<jsp:include page="/common/header_struts2.jsp" />
	<p><s:text name="report.discrepancy.instruction" /></p>
	<br>
	
	<div class="dataTableContainer" id="discrepancyTableDiv">
		<idt:jsontable name="disReport" scope="session" export="true" >
			<idt:setProperty name="basic.msg.empty_list"
				value="There are no data collection records to display at this time." />
			<idt:column property="ddvGuId" title='<%=rs.getValue("report.subjectGUID", l)%>' />
			<idt:column property="ddvFormName" title='<%=rs.getValue("form.name.display", l)%>' />
			<idt:column property="resolveCount" title='<%=rs.getValue("report.numOfQuestionsResolved", l)%>' />
			<idt:column property="updateDate" title='<%=rs.getValue("response.resolveHome.tableHeader.resolvedDate", l)%>' />
			<idt:column property="userName" title='<%=rs.getValue("response.resolveHome.tableHeader.resolvedBy", l)%>' />
			
			<idt:setProperty name="export.excel.include_header" value="true" />
			<idt:setProperty name="export.csv.include_header" value="true" />
			<idt:setProperty name="export.excel.filename" value='<%=rs.getValue("report.DiscrepancyReport",l).replaceAll(" ", "_") + ".xls"%>' />
			<idt:setProperty name="export.csv.filename" value='<%=rs.getValue("report.DiscrepancyReport",l).replaceAll(" ", "_") + ".csv"%>' />
			<idt:setProperty name="export.excel" value="true" />
			<idt:setProperty name="export.csv" value="true" />
			<idt:setProperty name="export.xml" value="false" />
		</idt:jsontable>
	</div>
	
	<jsp:include page="/common/footer_struts2.jsp" />
</html>