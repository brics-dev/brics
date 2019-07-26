<%@include file="/common/taglibs.jsp"%>

<table class="display-data full-width">
	<thead>
		<tr>
			<th>Privilege</th>
			<th>Status</th>
			<th>Expiration Date</th>
		</tr>
	</thead>
	<tbody>
		<c:forEach var="accountRole" items="${sessionAccountEdit.account.accountRoleList}">
			<tr>
				<td>${accountRole.roleType.title}</td>
				<td>${accountRole.roleStatus.name}</td>
				<s:if test="%{accountRole.expirationDate!=null}">
					<td>${accountRole.expirationDate}</td>
				</s:if>
				<s:else>
					<td>No Expiration Date</td>
				</s:else>
			</tr>
		</c:forEach>
		<c:forEach var="permissionGroupMember" items="${sessionAccountEdit.account.permissionGroupMemberList}">
			<tr>
				<td>${permissionGroupMember.permissionGroup.groupName}</td>
				<td>${permissionGroupMember.permissionGroupStatus.name}</td>
				<s:if test="%{permissionGroupMember.permissionGroup.expirationDate!=null}">
					<td>${permissionGroupMember.permissionGroup.expirationDate}</td>
				</s:if>
				<s:else>
					<td>-</td>
				</s:else>
			</tr>
		</c:forEach>
	</tbody>
</table>