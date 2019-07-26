<%@ tag body-content="scriptless"%>
<%@ attribute name="value" type="java.util.Date" required="true"%>
<%@ attribute name="format"  required="false"%>
<%@ tag import="java.text.SimpleDateFormat"%>

<%
	SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
	if(format != null && format == "long"){
		 dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	}
	
	if (value != null)
	{
%>
<%=dateFormat.format(value)%>
<% } %>