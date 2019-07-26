<%@ tag body-content="scriptless"%>
<%@ attribute name="accountList" type="java.util.Collection" required="true"%>

<%@taglib prefix="s" uri="/struts-tags"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@taglib prefix="ndar" tagdir="/WEB-INF/tags"%>

<table class="display-data full-width">
	<tr>
		<!-- 	An array of the table headers. The first value in each pair is the name of the header and the second value is the string that will be used to sort the hibernate results -->
		<s:iterator var="name"
			value="{{'userName', 'userName', '15%'}, {'name', 'u.lastName', '16%'}, {'email', 'u.email', '16%'}, 
			{'institution', 'affiliatedInstitution', '15%'}, {'submitted', 'applicationDate', '12%'}, 
			{'last updated', 'lastUpdatedDate', '15%'}, {'status', 'accountStatus', '11%'}}">
			<th style="width:<s:property value="#name[2]" />"  >
				<div class="no-wrap"  >
					<a href="javascript:accountSetSort('<s:property value='#name[1]' />')"> <s:property value="#name[0]" />
					</a>
					<s:if test="#name[1] == sort">
						<s:if test="ascending">
							<img src='<s:url value="/images/brics/common/icon-down.png"/>'>
						</s:if>
						<s:else>
							<img src='<s:url value="/images/brics/common/icon-up.png"/>'>
						</s:else>
					</s:if>
				</div>
			</th>
		</s:iterator>
	</tr>
	<c:forEach var="account" items="${accountList}" varStatus="status">
		<c:choose>
			<c:when test="${status.count%2 == 0}">
				<tr class="stripe">
			</c:when>
			<c:otherwise>
				<tr>
			</c:otherwise>
		</c:choose>
		<c:if test="${account.accountStatus.id == 3 || account.accountStatus.id == 4}">
			<td><a href="/portal/accountAdmin/viewAccountRequest!viewAccountRequest.action?accountId=<c:out value="${account.id}" />"><c:out
						value="${account.userName}" /></a></td>
		</c:if>
		<c:if test="${account.accountStatus.id != 3 && account.accountStatus.id != 4}">
			<td><a href="/portal/accountAdmin/viewUserAccount!viewUserAccount.action?accountId=<c:out value="${account.id}" />"><c:out
						value="${account.userName}" /></a></td>
		</c:if>
		<td><c:out value="${account.user.fullName}" /></td>
		<td><c:out value="${account.user.email}" /></td>
		<td><c:out value="${account.affiliatedInstitution}" /></td>
		<td><ndar:dateTag value='${account.applicationDate}' /></td>
		<td><ndar:dateTag value='${account.lastUpdatedDate}' /></td>
		<td><c:out value="${account.accountStatus.name}" /></td>
		</tr>
		<!-- Warning due to <tr> tag being created inside a choose tag -->
	</c:forEach>
</table>
<script type="text/javascript">

</script>