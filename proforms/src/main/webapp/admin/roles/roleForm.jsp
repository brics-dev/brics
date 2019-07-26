<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">

<%@ page import="gov.nih.nichd.ctdb.common.rs, java.util.Locale" %>
<%@ taglib uri="/struts-tags" prefix="s" %>
<%@ taglib uri="/WEB-INF/display.tld" prefix="display" %>
<%@ taglib uri="/WEB-INF/security.tld" prefix="security" %>


<%-- CHECK PRIVILEGES --%>
<security:check privileges="sysadmin"/>

<html>

<%-- Include Header --%>
<s:set var="pageTitle" scope="request">
	<s:text name="role.title.display"/>: 
	<s:if test="%{#context['struts.actionMapping'].name.equals('addRole')}">
		<s:text name="role.subtitle.add.display"/>
	</s:if>
	<s:if test="%{#context['struts.actionMapping'].name.equals('editRole')}">
		<s:text name="role.subtitle.edit.display"/>
	</s:if>
	<s:text name="role.subtitle.role.display"/>
</s:set>
<jsp:include page="/common/header_struts2.jsp" />

<s:form theme="simple" method="post">
	<s:hidden name="id"/>
	
	<div>
    	<p><s:text name="role.subtitle.role.instruction"/></p>
    	<label class="requiredInput"></label> 
    	<i><s:text name="role.subtitle.role.requiredSymbol"/></i>
    	<br><br>
    </div>
	
	<div class="formrow_1">
		<label for="rolename" class="requiredInput"><s:text name="role.name.display"/></label>
		<s:textfield id="rolename" name="name" size="40" maxlength="50"/>
	</div>
	<div class="formrow_1">
		<label>&nbsp;</label>
		<span class="formItemHelp"><s:text name="role.name.formatNote"/></span>
	</div>
	<div class="formrow_1">
		<label for="roleDescription"><s:text name="role.description.display"/></label>
		<s:textarea id="roleDescription" name="description" cols="50" rows="5"/>
	</div>

	<h4 class="requiredInput"><s:text name="role.privileges.display"/></h4>
	<div class="formrow_1">
		<label></label>
		<s:a href="javascript:selectAll(document.forms[0]);" cssStyle="formItemLabel">
			<s:text name="role.checkall.display"/>
		</s:a>
	</div>
	
	<s:iterator value="#session.privileges" begin="0" end="#session.offset - 1">
		<div class="formrow_2">
        	<label><s:property value="name"/></label>
        	<s:checkbox name="selectedPrivileges" fieldValue="%{id}" value="%{id in selectedPrivileges}"/>
        </div>
	</s:iterator>
	<s:iterator value="#session.privileges" begin="#session.offset">
		<div class="formrow_2">
        	<label><s:property value="name"/></label>
        	<s:checkbox name="selectedPrivileges" fieldValue="%{id}" value="%{id in selectedPrivileges}"/>
        </div>
	</s:iterator>

	<div class="formrow_1">
		<input type="button" value="<s:text name='button.Cancel'/>" 
				onclick="location.replace('<s:property value="#systemPreferences.get('app.webroot')"/>/admin/rolesAdmin.action')" 
				title ="Click to cancel (changes will not be saved)."/>
		<input type="reset" value="<s:text name='button.Reset'/>" title = "Click to reset the form"/>
		<s:submit action="saveRole" key="button.Save" title ="Click to save changes"/>
	</div>

</s:form>
<%-- Include Footer --%>
<jsp:include page="/common/footer_struts2.jsp" />
</html>