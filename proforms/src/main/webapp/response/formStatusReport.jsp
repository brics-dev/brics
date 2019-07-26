<jsp:include page="/common/doctype.jsp" />

<%@ page import="gov.nih.nichd.ctdb.common.rs,java.util.Locale" %>
<%@ taglib uri="/struts-tags" prefix="s"%>
<%@ taglib uri="/WEB-INF/display.tld" prefix="display"%>
<%@ taglib uri="/WEB-INF/security.tld" prefix="security"%>
<%@ page buffer="100kb"%>

<% Locale l=request.getLocale(); %>
<html>
<s:set var="pageTitle" scope="request">
	<s:text name="report.FormStatusReport" />
</s:set>

<jsp:include page="/common/header_struts2.jsp" />

<p><s:text name="report.formStatusReport.instruction" /></p>
<br>

<div class="dataTableContainer" id="discrepancyTableDiv">
	<display:table name="formStatusReport" scope="session" export="true" >
		<display:setProperty name="basic.msg.empty_list"
			value="There are no data collection records to display at this time." />
		<display:column property="afName" title='<%=rs.getValue("form.forms.formInformation.formName",l)%>' />
		<display:column property="guId" title="Subject GUID" />
		<display:column property="afStatus" title='<%=rs.getValue("form.forms.formInformation.status",l)%>' />
		<display:column property="asingleDoubleEntry" title='<%=rs.getValue("report.entryType",l)%>' />
		<display:column property="auDate" title='<%=rs.getValue("app.label.lcase.lastupdated",l)%>' />
		
		<display:setProperty name="export.excel.include_header" value="true" />
		<display:setProperty name="export.csv.include_header" value="true" />
		<display:setProperty name="export.excel.filename" value='<%=rs.getValue("report.FormStatusReport",l).replaceAll(" ", "_") + ".xls"%>' />
		<display:setProperty name="export.csv.filename" value='<%=rs.getValue("report.FormStatusReport",l).replaceAll(" ", "_") + ".csv"%>' />
		<display:setProperty name="export.excel" value="true" />
		<display:setProperty name="export.csv" value="true" />
		<display:setProperty name="export.xml" value="false" />
	</display:table>
</div>

<jsp:include page="/common/footer_struts2.jsp" />
</html>