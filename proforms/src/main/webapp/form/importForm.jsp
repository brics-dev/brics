<jsp:include page="/common/doctype.jsp" />
<%@ page import="gov.nih.nichd.ctdb.common.CtdbConstants"%>
<%@ page import="gov.nih.nichd.ctdb.form.domain.Form"%>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html"%>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic"%>
<%@ taglib uri="/WEB-INF/security.tld" prefix="security"%>
<%@ taglib uri="/WEB-INF/display.tld" prefix="display"%>

<%-- CHECK PRIVILEGES --%>
<security:check privileges="importexportforms" />

<jsp:useBean id="user" scope="session" class="gov.nih.nichd.ctdb.security.domain.User" />

<html:html>
	<%-- Include Header --%>
	<s:set var="pageTitle" scope="request">
		<bean:message key="form.import.pagetitle" />
	</s:set>
	<jsp:include page="/common/header.jsp" />
	
	<script language="javascript">
	
		function cancelImportForm() {
			var url = "<%= webRoot %>/form/formHome.do";
			redirectWithReferrer(url);
		}
	
	</script>
	
	<s:if test="%{#param.questionsOnForm != null}">
		<label>The form contains the following questions : ${param.questionsOnForm}</label>
		<br/>
		<label>The imported file contains the following questions : ${param.questionsImported}</label>
	</s:if>
	
	<br/>
	<html:form action="importFormProcess" enctype="multipart/form-data">
		<html:hidden property="action" />
		<html:hidden property="id" />
	
		<div class="formrow_1">
			<label for="file">
				<bean:message key="form.import.filename.display" />
			</label>
			<html:file property="file" size="30" maxlength="50" />
		</div>
	
		<div class="formrow_1">
			<input type="submit" value="Submit" title="Submit" />
			<input type="reset" value="<bean:message key='button.Reset'/>" title="Reset" />
			<input type="button" value="<bean:message key='button.Cancel'/>" title="Cancel" 
				onclick="cancelImportForm();" />
		</div>
	</html:form>
	
	<%-- Include Footer --%>
	<jsp:include page="/common/footer.jsp" />
</html:html>
