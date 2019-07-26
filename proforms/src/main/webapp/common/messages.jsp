<%-- all of the following lines are shown on one line to get rid of newlines in the html.  space them out if you need to
 --%><%@ page import="gov.nih.nichd.ctdb.common.CtdbConstants,
                 gov.nih.nichd.ctdb.security.domain.User,
                 gov.nih.nichd.ctdb.common.CtdbForm,
                 gov.nih.nichd.ctdb.protocol.domain.Protocol,
                 gov.nih.nichd.ctdb.util.common.Message,
                 gov.nih.nichd.ctdb.util.common.MessageHandler,
                 java.util.ArrayList"
                 %><%@ 
                 taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %><%@ 
                 taglib uri="/WEB-INF/struts-html.tld" prefix="html" %><%@ 
                 taglib uri="/WEB-INF/struts-logic.tld" prefix="logic" %><%@ 
                 taglib uri="/WEB-INF/security.tld" prefix="security" %>
                 <jsp:useBean id="user" scope="session" class="gov.nih.nichd.ctdb.security.domain.User"/>
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
<logic:messagesPresent name="error">
	<html:messages id="error">
		<li class="error">
		<bean:write name="error" />
		</li>
	</html:messages>
</logic:messagesPresent>
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
<jsp:include page="/common/validationErrors.jsp" />

<logic:messagesPresent message="true">
	<html:messages id="confirmation" message="true">
	<li class="success">
		<bean:write name="confirmation" />
	</li>
	</html:messages>
</logic:messagesPresent>
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