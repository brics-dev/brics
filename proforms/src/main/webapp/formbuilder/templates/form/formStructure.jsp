<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/display.tld" prefix="display" %>
<%@ taglib uri="/WEB-INF/datatables.tld" prefix="idt" %>
<%@ page import="gov.nih.nichd.ctdb.common.rs,java.util.Locale" %>
<%@ taglib uri="/struts-tags" prefix="s"%>
<% Locale l = request.getLocale(); %>
<script id="formStructureTemplate" type="text/x-handlebars-template">
	<div id="fsErrorContainer"></div>
	Please select a form structure for this form
	<div id="formDataStructures" class="dataTableContainer dataTableJSON" >
		<table id="formStructureTable" >
		</table>
	</div>
</script>