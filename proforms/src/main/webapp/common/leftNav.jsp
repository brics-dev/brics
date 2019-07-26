<%@ page import="gov.nih.nichd.ctdb.common.CtdbConstants,
                 gov.nih.nichd.ctdb.security.domain.User"%>
<%@ page import="gov.nih.nichd.ctdb.protocol.domain.Protocol"%>
<%@ page import="gov.nih.nichd.ctdb.common.navigation.LeftNavController"%>
<%@ page import="gov.nih.nichd.ctdb.common.navigation.SubNavLink"%>
<%@ taglib uri="/WEB-INF/security.tld" prefix="security" %>
<%!

/*
<div>
	<ul class="subnavigation">
		<li>
			<a></a>
			<ul>
				<li>
					<a></a>
				</li>
			</ul>
		</li>
		<li>
		</li>
	</ul>
</div>
*/
public String interpretLink(SubNavLink link) {
	String interpretedUrl = link.getUrl();
	if (link.getUrl().equals("")) {
		return "";
	}
	if (link.getUrl().startsWith("/")) {
		link.setUrl(link.getUrl().substring(1));
	}
	if (!interpretedUrl.contains("http") && !link.getUrl().startsWith("javascript:")) {
		interpretedUrl = (String)CtdbConstants.GLOBAL_XSL_PARAMETER_MAP.get("webroot") + "/" + interpretedUrl;
	}
	else {
		interpretedUrl = link.getUrl().replaceAll("::webroot::", (String)CtdbConstants.GLOBAL_XSL_PARAMETER_MAP.get("webroot"));
	}
	return interpretedUrl;
}

public boolean isDisabled(int[] disabledLinks, int currentLinkIndex) {
	if (disabledLinks == null || disabledLinks.length < 1) {
		return false;
	}
	for (int linkId : disabledLinks) {
		if (linkId == currentLinkIndex) {
			return true;
		}
	}
	return false;
}
%>
<div id="leftNav">
<ul class="subnavigation">
<%
LeftNavController leftNav = (LeftNavController) request.getAttribute(CtdbConstants.NAVIGATION_LEFTNAV_KEY);
if (leftNav == null) {
	// if we can't find the leftNav in the normal request (this happens in validation failed cases)
	// try and see if it's in the session
	leftNav = (LeftNavController) request.getSession().getAttribute(CtdbConstants.NAVIGATION_LEFTNAV_KEY);
}
if (leftNav != null) {
	for(SubNavLink link : leftNav.getLinks()) {
		String interpretedUrl = interpretLink(link);
		String linkClass = "";
		/* Draw all first-level links.
		 * Only draw the second level UL and links if we are on that active link
		 */
		
		// if the header doesn't have a url, set its url to the first sublink's url
		if (interpretedUrl.equals("") && link.getSubLinks().size() > 0) {
			interpretedUrl = interpretLink(link.getSubLinks().get(0));
		}
		
		// is this link active?
		linkClass = (leftNav.isHighlighted(link)) ? "active-sub" : "";
		
		if (isDisabled(leftNav.getDisabledLinks(), link.getNickname())) {
			interpretedUrl = "javascript:;";
			linkClass += " disabledLeftNav";
		}
		
		%>
		<security:hasProtocolPrivilege privilege="<%=link.permissionsString() %>">
		<li class="<%= linkClass %>">
			<a href="<%= interpretedUrl %>" class="leftNavHeader"><%= link.getLinkText() %></a>
			<% 
			// only expand if we need to and this link is active
			if (link.getSubLinks().size() > 0 && linkClass.contains("active-sub")) { %>
				<ul class="tertiary-links">
				<%
				for (SubNavLink subLink : link.getSubLinks()) {
					interpretedUrl = interpretLink(subLink);
					// is this link active?
					linkClass = (leftNav.isHighlighted(subLink)) ? "active-ter" : "";
					
					if (isDisabled(leftNav.getDisabledLinks(), subLink.getNickname())) {
						interpretedUrl = "javascript:;";
						linkClass += " disabledLeftNav";
					}
					
					%>
					<security:hasProtocolPrivilege privilege="<%= subLink.permissionsString() %>">
					
					<li class="<%= linkClass %>">
					<% if (subLink.getLinkText().indexOf("Help") >= 0)	{%>
						<a href="<%= interpretedUrl %>" class="" target="_blank"><%= subLink.getLinkText() %></a>
					<%} else {%>
						<a href="<%= interpretedUrl %>" class=""><%= subLink.getLinkText() %></a>
					<%} %>
					</li>
					
					</security:hasProtocolPrivilege>
					<% 
				}
				%>
				</ul>
			<%
			}
			%>
		</li>
		</security:hasProtocolPrivilege>
		<% 
	}
}
%>
</ul>
</div>
<div id="leftNavHandle" class="hide">
&nbsp;
</div>