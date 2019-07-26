<%@ page import="gov.nih.nichd.ctdb.util.common.Message,
                 gov.nih.nichd.ctdb.util.common.MessageHandler,
                 java.util.ArrayList"
                 %>
<%@ taglib uri="/struts-tags" prefix="s" %>
<%
ArrayList<Message> sessMessages = (ArrayList<Message>)session.getAttribute(MessageHandler.SESSION_KEY_MESSAGEHANDLER);
MessageHandler messages = new MessageHandler();
messages.init(sessMessages);
ArrayList<Message> errorMessages = (messages == null) ? new ArrayList<Message>() : messages.getAllErrorMessages();
ArrayList<Message> messageMessages = (messages == null) ? new ArrayList<Message>() : messages.getAllMessageMessages();
ArrayList<Message> infoMessages = (messages == null) ? new ArrayList<Message>() : messages.getAllInfoMessages();
ArrayList<Message> warningMessages = (messages == null) ? new ArrayList<Message>() : messages.getAllWarningMessages();
%>
<div id="messageContainer"></div>

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
<%
if (errorMessages.size() > 0) {
	for (int i = 0; i < errorMessages.size(); i++) {%>
		<li class="error">
		<%= errorMessages.get(i).getMessage() %>
		</li><%
		messages.removeMessage(errorMessages.get(i));
	} 
}
%>

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
<%
if (messageMessages.size() > 0) {
	for (int i = 0; i < messageMessages.size(); i++) {%>
	<li class="success">
	<%= messageMessages.get(i).getMessage() %>
	</li><%
	messages.removeMessage(messageMessages.get(i));
	} 
}

if (warningMessages.size() > 0) {
	for (int i = 0; i < warningMessages.size(); i++) {%>
	<li class="warning">
	<%= warningMessages.get(i).getMessage() %>
	</li><%
	messages.removeMessage(warningMessages.get(i));
	} 
}

if (infoMessages.size() > 0) {
	for (int i = 0; i < infoMessages.size(); i++) {%>
	<li class="info">
	<%= infoMessages.get(i).getMessage() %>
	</li><%
	messages.removeMessage(infoMessages.get(i));
	} 
}


messages.save(request);
%>
</ul>
