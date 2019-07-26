<%@ page import="gov.nih.nichd.ctdb.common.CtdbConstants" %>
<%@ page import="gov.nih.nichd.ctdb.security.domain.User" %>
<%@ page import="gov.nih.nichd.ctdb.protocol.domain.Protocol" %>
<%@ page import="java.util.List, java.util.ArrayList" %>
<%@ taglib uri="/struts-tags" prefix="s" %>
<%@ taglib uri="/WEB-INF/security.tld" prefix="security" %>


<s:bean name="gov.nih.nichd.ctdb.util.common.SystemPreferences" var="systemPreferences" />
<s:set var="webRoot" value="#systemPreferences.get('app.webroot')"/>

<script type="text/javascript">
$(document).ready(function() {
	$nextFocusElement = $();
	
	$('#perspectiveSelect a').each(function(index) {
	    $(this).attr('tabindex', index);
	});
	
	$("#perspectiveSelectContainer").keydown(function(event) {
		if (event.which == 9) {
			if (event.shiftKey) {
				$(this).removeClass("active");
			}
			else {
				// tab to the first link in the set
				var $this = $(this);
				$this.addClass("active");
				$nextFocusElement = $(document.activeElement);
				var $linkList = $this.next("ul").find("a");
				var $first = $linkList.eq(0);
				event.preventDefault();
				$first.focus();
				$linkList.last().keydown(function(event) {
					if (event.which == 9 && !event.shiftKey) {
						$("#perspectiveSelectContainer").removeClass("active");
						$nextFocusElement.focus();
					}
				});
				$linkList.first().keydown(function(event) {
					if (event.which == 9 && event.shiftKey) {
						event.preventDefault();
						$("#perspectiveSelectContainer").removeClass("active");
						$("#perspectiveSelectContainer").focus();
					}
				});
			}
		}
	}).mouseleave(function() {
		$(this).removeClass("active");
	});
	
	$("#perspectiveSelectContainer").click(function(event) {
		event.stopPropagation();
		$(this).toggleClass("active");
	});
});


</script>
<s:set var="noStudy" scope="request">
	<s:text name="myworkspace.studyselect.noneselected" />
</s:set>
<% 
String noSubjectText = (String)request.getAttribute("noStudy");
Protocol protocol = ((Protocol)session.getAttribute(CtdbConstants.CURRENT_PROTOCOL_SESSION_KEY));
String protocolNumber;
String protocolUrl;
if (protocol != null) {
	protocolNumber = protocol.getProtocolNumber();
	protocolUrl = "dashboard.action?id="+String.valueOf(protocol.getId());
} else {
	protocolNumber = noSubjectText;
	protocolUrl = "pickStudy.action?id=0";
}
%>

<nav id="perspectiveSelect">
	<ul>
		<li>
			<%
			if (protocolNumber.equals(noSubjectText)) {
			%>
				<a id="perspectiveSelectContainer" tabindex="0" href="javascript:;">
					<%=protocolNumber %>
				</a>
			<%
			} else {
			%>
				<a id="perspectiveSelectContainer" tabindex="0" href="<s:property value="#webRoot"/>/<%=protocolUrl %>">
					<%=protocolNumber %>
				</a>
			<%
			}
			%>
			<%
			List<Protocol> protocolsList = (List<Protocol>) session.getAttribute(CtdbConstants.USER_PROTOCOL_LIST);
			if (protocolsList == null) {
				protocolsList = new ArrayList<Protocol>();
			}
			%>
			<ul id="perspectiveDropdown">
			<%
			if (!protocolNumber.equals(noSubjectText)) {
				%>
				<li><a tabindex="1" href="<s:property value="#webRoot"/>/pickStudy.action?id=0"><s:text name="myworkspace.studyselect.noneselected" /></a></li>
				<%
			}
			
			for (Protocol prot : protocolsList) {
				if (!prot.getProtocolNumber().equals(protocolNumber)) {
			%>
				<li><a tabindex="2" href="<s:property value="#webRoot"/>/dashboard.action?id=<%=prot.getId() %>"><%=prot.getProtocolNumber() %></a></li>		
			<%
				}
			}
			%>
			</ul>
		</li>
	</ul>
</nav>