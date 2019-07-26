<%@ tag body-content="scriptless"%>
<%@ attribute name="action" required="true"%>
<%@ attribute name="value" required="true"%>
<%@ attribute name="paramName" required="false"%>
<%@ attribute name="paramValue" required="false"%>

<a
	href="<%=action %>
<% if (paramName != null && paramValue != null) { %>
?<%=paramName %>=<%=paramValue %>
 <% } %>
 "><%=value %></a>