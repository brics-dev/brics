<jsp:include page="/common/doctype.jsp" />

<%@ taglib uri="/struts-tags" prefix="s" %>
<%@ taglib uri="/WEB-INF/security.tld" prefix="security" %>


<%-- CHECK PRIVILEGES --%>
<security:check privileges="sysadmin"/>

<html>
<s:set var="pageTitle" scope="request">
	<s:text name="sitelink.title.display"/>
</s:set>

<jsp:include page="/common/header_struts2.jsp" />

<s:set var="actionName" value="%{#context['struts.actionMapping'].name}"></s:set>
<h3>
	<s:if test="%{#actionName == 'addSiteLink'}">
		<s:text name="sitelink.addurl.display"/>
	</s:if>
	<s:if test="%{#actionName == 'editSiteLink'}">
    	<s:text name="sitelink.editurl.display"/>
    </s:if>
</h3>

<div>
	<s:if test="%{#actionName.equals('addSiteLink')}">
		<s:text name="sitelink.addurlinstruction.display"/>
	</s:if>
	<s:if test="%{#actionName.equals('editSiteLink')}">
		<s:text name="sitelink.editurlinstruction.display"/>
	</s:if>
	<br/>
	<label class="requiredInput"></label> 
	<i><s:text name="sitelink.requiredSymbol.display"/></i>
	<br/><br/>
</div>

<s:form theme="simple" method="post">
	<s:hidden name="id"/>
	
	<!-- Form input fields -->
	<div class="formrow_1">
		<label class="requiredInput">
			<s:text name="sitelink.name.display"/>
		</label>
		<s:textfield name="name" size="40" maxlength="50"/>
	</div>
	<div class="formrow_1">
		<label class="requiredInput">
			<s:text name="sitelink.address.display"/>
		</label>
		<s:textfield name="address" size="40" maxlength="255"/>
	</div>
	<div class="formrow_1">
		<label>
			<s:text name="sitelink.description.display"/>
		</label>
		<s:textarea name="description" cols="50" rows="5"/>
	</div>
	
	<!-- Form buttons -->
	<div class="formrow_1">
		<input type="button" id="cancel" title="Click to cancel(changes will not be saved.)" value="<s:text name='button.Cancel'/>" 
			onclick="redirectWithReferrer('<s:property value="#systemPreferences.get('app.webroot')"/>/admin/siteLinkAdmin.action');" />
		<input type="reset" id="reset" value="<s:text name='button.Reset'/>" title = "Click to clear fields"/>
		<s:submit action="saveSiteLink" key="button.Save" title ="Click to save changes"/>
	</div>
</s:form>

<%-- Include Footer --%>
<jsp:include page="/common/footer_struts2.jsp" />
</html>