<%@ taglib uri="/struts-tags" prefix="s"%>

<s:if test="hasActionErrors()">
	<div class="error">
		<s:text name="errors.header"/>
		<s:iterator value="actionErrors">
			<li style="margin-left: 20px;">
				<s:property escapeHtml="false" />
			</li>
		</s:iterator>
	</div>
</s:if>

