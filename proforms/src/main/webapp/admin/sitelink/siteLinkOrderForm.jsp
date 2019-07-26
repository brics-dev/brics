<jsp:include page="/common/doctype.jsp" />

<%@ taglib uri="/struts-tags" prefix="s" %>
<%@ taglib uri="/WEB-INF/security.tld" prefix="security" %>


<%-- CHECK PRIVILEGES --%>
<security:check privileges="sysadmin"/>

<html>
<%-- Include Header --%>
<s:set var="pageTitle" scope="request">
	<s:text name="sitelink.title.display" />
</s:set>

<jsp:include page="/common/header_struts2.jsp" />

<h3><s:text name="sitelinkorder.title.display"/></h3>
<p><s:text name="sitelinkorder.instructions.display"/></p>
<br>

<s:form theme="simple" name="siteLinkOrderForm">
	<table>
		<tr>
			<td>
				<s:select name="orderedSiteLinks" list="siteLinks" listKey="id" listValue="name" size="10" multiple="true" />
			</td>
			<td align="left" valign="middle" >
				<button type="button" name="upButton" id="upButton" title="Move this URL up"  onclick="swapItem(document.siteLinkOrderForm.orderedSiteLinks, 'UP');">
					<s:text name='button.siteURL.changeOrder.up'/>
				</button>
				<br><br>
				<button type="button" name="downButton" id="downButton" title="Move this URL down"  onclick="swapItem(document.siteLinkOrderForm.orderedSiteLinks, 'DOWN');">
					<s:text name='button.siteURL.changeOrder.down'/>
				</button>
			</td>
		</tr>
		<tr>
			<td>
				&nbsp;
			</td>
		</tr>
		<tr>
			<td>
				<s:submit action="updateSiteLinkOrder" key="button.Save" title ="Click to save changes" onclick="selectAllOptions(document.siteLinkOrderForm.orderedSiteLinks);"/>&nbsp;
				<button type="button" name="cancel" id="cancel" title="Click to cancel (changes will not be saved)." onclick="redirectWithReferrer('<s:property value="#systemPreferences.get('app.webroot')"/>/admin/siteLinkAdmin.action');" ><s:text name='button.Cancel'/></button>
			</td>
		</tr>
	</table>
</s:form>

<%-- Include Footer --%>
<jsp:include page="/common/footer_struts2.jsp" />
</html>