<%@include file="/common/taglibs.jsp"%>

<div class="form-field">
	<label for="currentDataElement.aliasList">Aliases : </label>
	<table class="display-data">
		<s:iterator var="alias" value="%{currentDataElement.aliasList}" status="rowstatus">
			<s:if test="#rowstatus.odd == true">
				<tr>
			</s:if>
			<s:else>
				<tr class="stripe">
			</s:else>
			<td><c:out value="${alias.name}" /></td>
			<td>
				<div class="button">
					<input type='button' value='X' onclick='removeAlias("${alias.name}")' />
				</div>
			</td>
			</tr>
		</s:iterator>
	</table>
</div>
<div class="form-field">
	<label for="aliasName" class="label">New Alias: </label>
	<div style="float: left;">
		<s:textfield id="aliasField" name="aliasName" cssClass="textfield" />
	</div>
	<div class="button margin-left">
		<input type='button' value='+' onclick="javascript:createAlias()" />
	</div>
	<s:fielderror fieldName="aliasName" />
</div>
