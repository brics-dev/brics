<%@ taglib prefix="s" uri="/struts-tags" %>

<div id="messageContainer" class="ibisMessaging-primaryContainer"></div>
<div id="messagePopUpContainer" class="ibisMessaging-flashContainer ibisMessaging-dialogContainer"></div>

<ul id="messages">
	<s:if test="hasFieldErrors()">
		<li class="error">
			<s:text name="errors.text"/>
			<ul>
				<s:iterator value="fieldErrors">
					<li>
						<s:iterator value="value"><s:property escapeHtml="false" /></s:iterator>
					</li>
				</s:iterator>
			</ul>
		</li>
	</s:if>
	
	<s:if test="hasActionErrors()">
		<s:iterator value="actionErrors">
			<li class="error">
				<s:property escapeHtml="false" />
			</li>
		</s:iterator>
	</s:if>
	
	<s:if test="hasActionMessages()">
		<s:iterator value="actionMessages">
			<li class="success">
				<s:property escapeHtml="false" />
			</li>
		</s:iterator>
	</s:if>
</ul>