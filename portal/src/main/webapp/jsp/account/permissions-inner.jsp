<%@include file="/common/taglibs.jsp"%>

<div id="authoritiesDivId">
	<div class="float-left">
		<select class="textfield, large-font">
			<s:iterator value="permissionAuthorities" var="permissionAuthority">
				<s:if test="%{entityMapAuthNameList.contains(displayName) || 
						(entityMapAuthNameList.isEmpty() && displayName.equals(account.displayName))}">
					<option value="<s:property value='displayKey'/>" disabled>
						<s:property value="displayName"/>
					</option>
				</s:if>
				<s:else>
					<option value="<s:property value='displayKey'/>">
						<s:property value="displayName"/>
					</option>
				</s:else>
			</s:iterator>
		</select>
	</div>
	<div class="button margin-left">
		<input type="button" value="Grant Permission" onClick="javascript: addAuthority()" />
	</div>
</div>

<br/><br/><br/>

<table class="display-data full-width" cellSpacing="0" cellPadding="0">
	<tr>
		<th>User / Permission Group</th>
		<th>Permission</th>
		<th>Remove?</th>
	</tr>
	<s:iterator var="entityMap" value="entityMapList" status="rowstatus">
		<%-- DETERMINE ACCOUNT vs PERMISSION_GROUP --%>
		<s:if test="%{account != null}">
			<s:set var="rowType" value="'ACCOUNT'" />
			<s:set var="rowId" value="%{account.id}" />
			<s:set var="disease" value="%{account.diseaseKey}" />
			<s:set var="isHidden" value="false" />
		</s:if>
		<s:else>
			<s:set var="rowType" value="'PERMISSION_GROUP'" />
			<s:set var="rowId" value="%{permissionGroup.id}" />
			<s:set var="disease" value="%{permissionGroup.diseaseKey }" />
		</s:else>
		<s:set var="entityPermission" value="#entityMap.permission" />
		<s:if test="%{account != null || !permissionGroup.publicStatus}">
			<tr class="Entities">
				<td><s:property value="authority.displayName"/></td>
				<td><s:select id="'%{disease};%{rowId}'" list="permissionTypeList" listValue="name" value="permission" cssClass="large"
						onchange="changePermission('%{rowType}', '%{disease};%{rowId}', this.options[this.selectedIndex].value, '%{entityPermission}')" />
				</td>
				<td><a href="#"
					onClick="javascript: clickAndDisable(this); changePermission('<s:property value='rowType'/>', '${disease};${rowId}', 'NONE', '${entityPermission}')">remove</a></td>
			</tr>
		</s:if>
	</s:iterator>
</table>
